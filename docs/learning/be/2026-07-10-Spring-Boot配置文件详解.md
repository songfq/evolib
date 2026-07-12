# 2026-07-10 — Spring Boot 配置文件详解

---

## 一、做了什么

- 功能描述：学习并理解 Spring Boot 的多环境配置机制，包括 application.yml、application-dev.yml、application-prod.yml 的作用和关系
- 涉及文件：
  - `evolib-backend/src/main/resources/application.yml`（主配置文件）
  - `evolib-backend/src/main/resources/application-dev.yml`（开发环境配置）
  - `evolib-backend/src/main/resources/application-prod.yml`（生产环境配置）
- 对应需求：了解项目配置方式，能够正确配置数据库密码等敏感信息

---

## 二、代码逻辑

### 文件 1: `application.yml`

```yaml
server:
  port: 8080                    # 所有环境共用的端口

spring:
  profiles:
    active: dev                 # ← 激活哪个环境配置文件
  datasource:
    url: jdbc:postgresql://localhost:5432/evolib
    username: postgres
    password: ${DB_PASSWORD:evolib123}  # ← 环境变量占位符，找不到用默认值

jwt:
  secret: ${JWT_SECRET:evolib-jwt-secret-key}
  expiration: 28800000

library:
  max:
    borrow:
      days: 30
      count: 3
```

> **为什么有三个配置文件？**
> Spring Boot 支持多环境配置，主配置文件存放所有环境共用的配置，环境特定配置文件存放各环境的差异配置。这样开发、测试、生产可以使用不同的数据库密码、日志级别等，而不需要修改代码。

### 文件 2: `application-dev.yml`

```yaml
spring:
  datasource:
    password: evolib123         # 开发环境直接写死密码，方便调试

logging:
  level:
    com.evolib: DEBUG           # 开发环境日志级别高，方便排查问题
    org.springframework.security: DEBUG
```

> **为什么开发环境密码可以写死？**
> 开发环境是本地运行，数据库也是本地的，风险低。写死密码可以让团队成员开箱即用，无需额外配置。

### 文件 3: `application-prod.yml`

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}    # ← 生产环境必须设置环境变量，无默认值！

logging:
  level:
    com.evolib: INFO            # 生产环境日志级别低，减少磁盘占用
    org.springframework.security: WARN
```

> **为什么生产环境没有默认值？**
> 安全考虑！生产环境的密码必须通过环境变量传入，不能硬编码在代码中，防止密码泄露。

---

## 三、配置加载流程

```
Spring Boot 启动
  → 读取 application.yml（主配置）
    → 发现 spring.profiles.active: dev
      → 读取 application-dev.yml（开发环境配置）
        → 合并配置：环境配置覆盖主配置中同名属性
          → 最终配置生效
```

**配置覆盖示例**：

| 属性 | application.yml | application-dev.yml | 最终生效值 |
| --- | --- | --- | --- |
| `spring.datasource.password` | `${DB_PASSWORD:evolib123}` | `evolib123` | `evolib123` |
| `logging.level.com.evolib` | 未配置 | `DEBUG` | `DEBUG` |
| `server.port` | `8080` | 未配置 | `8080` |

---

## 四、关键技术点

| 技术点 | 是什么 | 为什么用 |
| --- | --- | --- |
| `spring.profiles.active` | 指定激活哪个环境配置 | 切换开发/测试/生产环境，无需修改代码 |
| `application-{profile}.yml` | 环境特定配置文件命名规则 | `{profile}` 必须与 `spring.profiles.active` 的值完全匹配 |
| `${VAR:default}` | 环境变量占位符 | 优先读取环境变量，找不到用默认值，安全且灵活 |
| 配置合并 | 后加载的配置覆盖先加载的 | 环境配置可以覆盖主配置，实现差异化配置 |
| 多 Profile 激活 | `active: dev,mysql` | 同时加载多个配置文件，实现组合配置 |

### 环境变量的配置方式

**开发环境（直接运行）**：
```powershell
# 方式1：直接运行，使用默认密码 evolib123
mvn spring-boot:run

# 方式2：设置环境变量后运行
$env:DB_PASSWORD="my-dev-password"
mvn spring-boot:run
```

**生产环境（安全方式）**：
```powershell
# 设置环境变量（必须）
$env:DB_PASSWORD="your-production-password"
$env:JWT_SECRET="your-secure-jwt-secret"

# 切换到生产环境启动
mvn spring-boot:run -Dspring.profiles.active=prod
```

**打包后运行**：
```powershell
mvn clean package -DskipTests
$env:DB_PASSWORD="your-password"
java -jar target/evolib-backend-1.0.0.jar --spring.profiles.active=prod
```