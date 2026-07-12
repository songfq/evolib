# 2026-07-10 — Spring Security 访问被拒绝问题

---

## 一、做了什么

- 功能描述：排查并修复 Spring Security 的 `AccessDeniedException` 错误，理解 Spring Security 的路径匹配和权限控制机制
- 涉及文件：
  - `evolib-backend/src/main/java/com/evolib/config/SecurityConfig.java`（安全配置）
- 对应需求：解决浏览器直接访问 `http://localhost:8080/` 被拒绝的问题

---

## 二、代码逻辑

### 文件 1: `SecurityConfig.java`

**修改前**：

```java
.authorizeRequests()
    .antMatchers("/api/v1/auth/login").permitAll()  // ← 只有登录接口公开
    .antMatchers("/api/v1/books/**").hasRole("READER")
    .antMatchers("/api/v1/borrow-records/**").hasRole("CIRCULATION")
    .antMatchers("/api/v1/readers/**").hasRole("CIRCULATION")
    .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()  // ← 其他所有请求都需要认证！
```

> **问题原因**：
> 访问 `http://localhost:8080/` 时，路径 `/` 不匹配任何 `permitAll()` 的规则，最终匹配到 `.anyRequest().authenticated()`，要求用户必须登录（携带 JWT token），否则返回 `AccessDeniedException`。

**修改后**：

```java
.authorizeRequests()
    .antMatchers("/").permitAll()           // ← 根路径公开
    .antMatchers("/api/v1/auth/login").permitAll()  // ← 登录接口公开
    .antMatchers("/api/health").permitAll() // ← 健康检查接口公开
    .antMatchers("/api/v1/books/**").hasRole("READER")
    .antMatchers("/api/v1/borrow-records/**").hasRole("CIRCULATION")
    .antMatchers("/api/v1/readers/**").hasRole("CIRCULATION")
    .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
```

> **为什么要添加这些公开路径？**
> - `/`：根路径，方便测试服务是否启动
> - `/api/health`：健康检查接口，用于监控服务状态

---

## 三、路径匹配流程

```
用户访问 http://localhost:8080/
  → Spring Security 的 SecurityFilterChain
    → 按顺序检查路径匹配规则：
      1. /api/v1/auth/login → 不匹配
      2. /api/v1/books/**   → 不匹配
      3. /api/v1/borrow-records/** → 不匹配
      4. /api/v1/readers/** → 不匹配
      5. /api/v1/admin/**   → 不匹配
      6. anyRequest         → 匹配！要求认证
        → 用户没有登录（没有 JWT token）
          → 返回 AccessDeniedException
```

**修复后的流程**：

```
用户访问 http://localhost:8080/
  → Spring Security 的 SecurityFilterChain
    → 按顺序检查路径匹配规则：
      1. /                  → 匹配！permitAll() 允许访问
        → 直接放行，无需认证
```

---

## 四、关键技术点

| 技术点 | 是什么 | 为什么用 |
| --- | --- | --- |
| `.antMatchers(path).permitAll()` | 允许指定路径匿名访问 | 登录接口必须公开，否则用户无法登录 |
| `.antMatchers(path).hasRole("ROLE")` | 要求用户具有指定角色 | 实现 RBAC（基于角色的访问控制） |
| `.anyRequest().authenticated()` | 所有其他请求必须认证 | 安全兜底，防止遗漏的路径被公开访问 |
| 路径匹配顺序 | 按代码顺序从上到下匹配 | 排在前面的规则优先匹配，所以 `permitAll()` 要放在前面 |
| `@PreAuthorize("hasRole('ROLE')")` | 方法级别的权限控制 | 在 Controller 方法上添加注解，实现细粒度权限控制 |

### 常见错误场景

| 错误场景 | 原因 | 解决方案 |
| --- | --- | --- |
| 访问 `/` 被拒绝 | 根路径没有配置 `permitAll()` | 添加 `.antMatchers("/").permitAll()` |
| 访问 `/api/health` 被拒绝 | 健康检查接口没有公开 | 添加 `.antMatchers("/api/health").permitAll()` |
| 登录接口返回 403 | 登录接口没有配置 `permitAll()` | 添加 `.antMatchers("/api/v1/auth/login").permitAll()` |
| 角色校验失败 | `hasRole()` 会自动添加 `ROLE_` 前缀 | 数据库中存储角色时要带 `ROLE_` 前缀（如 `ROLE_READER`） |