# 2026-07-10 — SecurityConfig 安全配置详解

---

## 一、做了什么

- 功能描述：深入学习 Spring Security 的核心配置类 SecurityConfig，理解它是什么、为什么需要、作用是什么、完整流程以及谁来使用它
- 涉及文件：
  - `evolib-backend/src/main/java/com/evolib/config/SecurityConfig.java`（安全配置类）
  - `evolib-backend/src/main/java/com/evolib/security/JwtAuthenticationFilter.java`（JWT 过滤器）
  - `evolib-backend/src/main/java/com/evolib/security/JwtTokenProvider.java`（JWT 工具类）
- 对应需求：理解项目的安全机制，能够正确配置和调试权限问题

---

## 二、代码逻辑

### 文件 1: `SecurityConfig.java`

```java
// Step 1: 注解声明这是一个配置类
@Configuration              // ← Spring 配置类标识，告诉 Spring 这是一个 Bean 定义类
@EnableWebSecurity          // ← 启用 Spring Security 的 Web 安全功能
@EnableGlobalMethodSecurity(prePostEnabled = true)  // ← 启用方法级别的权限控制（@PreAuthorize）
@RequiredArgsConstructor    // ← Lombok 注解，自动生成构造器注入依赖

public class SecurityConfig {
    
    // Step 2: 注入依赖 — JWT 认证过滤器
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    // Step 3: 定义安全过滤链（核心！）
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ① 禁用 CSRF（前后端分离项目不需要 CSRF）
            .csrf().disable()
            
            // ② 配置会话管理为无状态（JWT 不需要 Session）
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            
            // ③ 配置路径权限规则
            .authorizeRequests()
                .antMatchers("/").permitAll()           // 根路径：公开访问
                .antMatchers("/api/v1/auth/login").permitAll()  // 登录接口：公开访问
                .antMatchers("/api/health").permitAll() // 健康检查：公开访问
                .antMatchers("/api/v1/books/**").hasRole("READER")       // 图书接口：需要 READER 角色
                .antMatchers("/api/v1/borrow-records/**").hasRole("CIRCULATION")  // 借阅接口：需要 CIRCULATION 角色
                .antMatchers("/api/v1/readers/**").hasRole("CIRCULATION") // 读者接口：需要 CIRCULATION 角色
                .antMatchers("/api/v1/admin/**").hasRole("ADMIN")         // 管理员接口：需要 ADMIN 角色
                .anyRequest().authenticated()           // 其他所有请求：必须登录认证
            
            // ④ 添加自定义 JWT 过滤器（在用户名密码认证过滤器之前执行）
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    // Step 4: 定义密码加密器（必须！否则认证会报错）
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // BCrypt 是业界标准的密码加密算法
    }
    
    // Step 5: 定义认证管理器（用于处理用户名密码认证）
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

> **为什么需要 `@Configuration` 注解？**
> Spring 框架通过 `@Configuration` 注解识别配置类，会扫描类中的 `@Bean` 方法，将返回的对象注册到 Spring 容器中，供其他组件使用。

> **为什么需要 `@EnableWebSecurity`？**
> 这个注解会启用 Spring Security 的 Web 安全功能，自动配置一些默认的安全规则，比如默认禁止所有请求、提供默认登录页面等。

> **为什么需要 `@EnableGlobalMethodSecurity`？**
> 启用方法级别的权限控制，这样我们可以在 Controller 的方法上使用 `@PreAuthorize` 注解来实现细粒度的权限控制。

---

## 三、完整工作流程

### 请求处理全流程

```
用户发起 HTTP 请求（例如：GET /api/v1/books/search）
  │
  ▼
┌─────────────────────────────────────────────────────┐
│              Spring Security Filter Chain            │
│  ┌─────────────────────────────────────────────┐    │
│  │ 1. JwtAuthenticationFilter (自定义过滤器)   │    │
│  │    - 从请求头提取 Authorization: Bearer xxx │    │
│  │    - 解析 JWT token，验证签名和过期时间      │    │
│  │    - 提取 readerId 和 role                  │    │
│  │    - 创建 UserPrincipal 对象                │    │
│  │    - 将认证信息注入 SecurityContext         │    │
│  └─────────────────────────────────────────────┘    │
│                        │                             │
│                        ▼                             │
│  ┌─────────────────────────────────────────────┐    │
│  │ 2. SecurityFilterChain (配置的规则)          │    │
│  │    - 检查路径 /api/v1/books/search           │    │
│  │    - 匹配规则 .antMatchers("/api/v1/books/**").hasRole("READER") │
│  │    - 检查用户是否具有 ROLE_READER 角色       │    │
│  │    - 如果有，放行；如果没有，返回 403 拒绝    │    │
│  └─────────────────────────────────────────────┘    │
│                        │                             │
│                        ▼                             │
│  ┌─────────────────────────────────────────────┐    │
│  │ 3. @PreAuthorize 注解检查（方法级别）        │    │
│  │    - 在 Controller 方法上检查权限            │    │
│  │    - 双重保障：路径级别 + 方法级别            │    │
│  └─────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
                        │
                        ▼
              请求到达 Controller
                        │
                        ▼
              执行业务逻辑并返回响应
```

### 登录流程

```
用户发起登录请求（POST /api/v1/auth/login）
  │
  ▼
┌─────────────────────────────────────────────────────┐
│              SecurityFilterChain 检查              │
│  - 路径 /api/v1/auth/login 匹配 .permitAll()       │
│  - 直接放行，不需要认证                              │
└─────────────────────────────────────────────────────┘
                        │
                        ▼
              AuthController.login()
                        │
                        ▼
              AuthServiceImpl.login()
                        │
                        ├─→ 查询读者信息
                        ├─→ 密码校验（PasswordEncoder.matches()）
                        ├─→ 生成 JWT token（JwtTokenProvider.generateToken()）
                        └─→ 返回 {token, role, name}
                        │
                        ▼
              用户拿到 token，后续请求都带上
              Authorization: Bearer {token}
```

---

## 四、关键技术点

### 4.1 核心组件说明

| 组件 | 是什么 | 作用 | 谁来使用 |
| --- | --- | --- | --- |
| **SecurityConfig** | Spring Security 的配置类 | 定义安全规则、密码加密器、认证管理器 | Spring 框架在启动时自动加载 |
| **SecurityFilterChain** | 安全过滤链 Bean | 配置路径权限规则、会话管理、CSRF 等 | Spring Security 在处理请求时使用 |
| **PasswordEncoder** | 密码加密器接口 | 加密和校验密码（BCrypt） | `AuthServiceImpl.login()` 校验密码时使用 |
| **AuthenticationManager** | 认证管理器接口 | 管理认证流程 | Spring Security 内部使用 |
| **JwtAuthenticationFilter** | 自定义过滤器 | 拦截请求，解析 JWT token | Spring Security 在请求处理前调用 |
| **JwtTokenProvider** | JWT 工具类 | 生成、解析、验证 JWT token | `AuthServiceImpl` 和 `JwtAuthenticationFilter` 使用 |

### 4.2 注解详解

| 注解 | 作用 | 使用位置 |
| --- | --- | --- |
| `@Configuration` | 声明这是一个配置类 | 类级别 |
| `@EnableWebSecurity` | 启用 Web 安全功能 | 类级别 |
| `@EnableGlobalMethodSecurity(prePostEnabled = true)` | 启用方法级权限控制 | 类级别 |
| `@RequiredArgsConstructor` | 自动生成构造器注入 | 类级别（Lombok） |
| `@Bean` | 声明一个 Bean，注册到 Spring 容器 | 方法级别 |
| `@PreAuthorize` | 方法级权限检查 | Controller 方法级别 |

### 4.3 路径匹配规则

| 规则 | 含义 | 示例 |
| --- | --- | --- |
| `.antMatchers("/").permitAll()` | 根路径公开访问 | 访问 `/` 不需要认证 |
| `.antMatchers("/api/v1/auth/login").permitAll()` | 登录接口公开 | 访问 `/api/v1/auth/login` 不需要认证 |
| `.antMatchers("/api/v1/books/**").hasRole("READER")` | 需要 READER 角色 | 访问 `/api/v1/books/xxx` 需要 ROLE_READER 角色 |
| `.anyRequest().authenticated()` | 其他请求必须认证 | 没有匹配到上面规则的请求都需要登录 |

> **重要**：`.antMatchers()` 的顺序很重要！Spring Security 会**从上到下**匹配规则，一旦匹配到就不再检查后面的规则。所以 `permitAll()` 的规则要放在前面，`.anyRequest()` 要放在最后。

### 4.4 角色前缀规则

```java
.hasRole("READER")  // ← 实际上检查的是 ROLE_READER
.hasAuthority("ROLE_READER")  // ← 直接检查完整的权限名
```

**区别**：
- `hasRole("READER")`：自动添加 `ROLE_` 前缀，检查的是 `ROLE_READER`
- `hasAuthority("ROLE_READER")`：直接检查，不会添加前缀

所以在数据库中存储角色时，应该存储 `ROLE_READER`、`ROLE_CIRCULATION`、`ROLE_ADMIN`。

### 4.5 Session 管理策略

```java
.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
```

| 策略 | 含义 | 使用场景 |
| --- | --- | --- |
| `STATELESS` | 无状态，不创建 Session | JWT 认证（前后端分离） |
| `STATEFUL` | 有状态，创建 Session | 传统 Web 应用 |
| `ALWAYS` | 总是创建 Session | 强制使用 Session |
| `NEVER` | 从不创建 Session，但如果已有则使用 | 混合模式 |

**为什么我们用 STATELESS？**
- JWT 是无状态的，服务器不需要保存用户登录状态
- 前后端分离项目，前端通过 token 维持登录状态
- 便于水平扩展，多个服务器实例共享同一个 JWT 密钥即可

### 4.6 CSRF 禁用

```java
.csrf().disable()
```

**什么是 CSRF？**
- Cross-Site Request Forgery（跨站请求伪造）
- 攻击者诱导用户点击恶意链接，利用用户已登录的身份执行非预期操作

**为什么禁用？**
- 前后端分离项目中，前端通常是 SPA（单页应用）
- JWT 认证本身就防止了 CSRF（因为每个请求都需要携带 token）
- 如果启用 CSRF，需要在前端每次请求都携带 CSRF token，增加复杂度

---

## 五、前后端联动

```
前端发起请求（带 JWT token）
  │
  ├─→ 请求头：Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  │
  ▼
后端接收请求
  │
  ├─→ JwtAuthenticationFilter 拦截
  │     ├─→ 提取 token
  │     ├─→ 验证签名
  │     ├─→ 解析出 readerId 和 role
  │     └─→ 注入 SecurityContext
  │
  ├─→ SecurityFilterChain 检查
  │     ├─→ 路径匹配规则
  │     └─→ 角色权限检查
  │
  ├─→ @PreAuthorize 检查（方法级别）
  │
  ├─→ Controller 处理业务
  │
  └─→ 返回响应
```

### 登录后的请求示例

```bash
# 登录获取 token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"readerId": "R001", "password": "123456"}'

# 响应：{"code":0,"data":{"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...","role":"ROLE_READER","name":"张三"}}

# 使用 token 访问受保护的接口
curl http://localhost:8080/api/v1/books/search \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 六、常见问题与解决方案

| 问题 | 原因 | 解决方案 |
| --- | --- | --- |
| 所有请求都被拒绝 | `SecurityFilterChain` 配置了 `.anyRequest().authenticated()`，但没有配置 `permitAll()` 的路径 | 在 `.anyRequest()` 之前添加 `.antMatchers("/api/v1/auth/login").permitAll()` |
| 登录后仍然 403 | `hasRole()` 会自动添加 `ROLE_` 前缀，数据库中存储的角色没有 `ROLE_` 前缀 | 数据库中存储角色时添加 `ROLE_` 前缀，如 `ROLE_READER` |
| 密码校验失败 | 使用了错误的 `PasswordEncoder`，或者密码没有加密存储 | 确保使用 `BCryptPasswordEncoder`，并在注册时加密密码 |
| CSRF 报错 | 启用了 CSRF，但前端没有携带 CSRF token | 前后端分离项目建议禁用 CSRF：`.csrf().disable()` |
| Session 不一致 | 配置了 `STATEFUL` 会话管理，但部署在多台服务器上 | 使用 `STATELESS` + JWT，或配置 Redis 共享 Session |

---

## 七、扩展知识

### 7.1 多层权限控制

项目中使用了**两层权限控制**：

1. **路径级别**：在 `SecurityFilterChain` 中配置路径规则
2. **方法级别**：在 Controller 方法上使用 `@PreAuthorize` 注解

```java
// Controller 中的方法级别权限控制
@GetMapping("/search")
@PreAuthorize("hasRole('ROLE_READER')")  // ← 方法级别检查
public Result<IPage<BookDTO>> search(@RequestBody BookSearchRequest request) {
    // ...
}
```

### 7.2 自定义权限表达式

可以在 `@PreAuthorize` 中使用 SpEL 表达式实现更复杂的权限控制：

```java
// 检查用户是否拥有某个权限
@PreAuthorize("hasAuthority('BOOK_CREATE')")

// 检查用户是否是资源的所有者
@PreAuthorize("#readerId == authentication.name")

// 组合多个条件
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CIRCULATION')")
```

### 7.3 安全配置的最佳实践

1. **最小权限原则**：只授予用户需要的最小权限
2. **默认拒绝**：`.anyRequest().authenticated()` 作为兜底规则
3. **安全的密码存储**：使用 BCrypt 加密，绝不存储明文密码
4. **无状态设计**：使用 JWT + STATELESS，便于水平扩展
5. **HTTPS**：生产环境必须使用 HTTPS，防止 token 被劫持
6. **定期轮换密钥**：JWT 密钥定期更换，避免密钥泄露导致的安全问题