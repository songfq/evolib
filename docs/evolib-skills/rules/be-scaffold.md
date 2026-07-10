# be-scaffold — 后端 Spring Boot 项目搭建

## 目标

一键生成 `evolib-backend/` 工程骨架，含完整 Maven 目录树、全部依赖、application.yml 配置。

---

## 技术栈版本

> **重要：不需要单独安装 Tomcat。** Spring Boot 内嵌 Tomcat（打包在 `spring-boot-starter-web` 依赖中），启动 `java -jar` 时自动拉起，无需额外配置。

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 1.8 | 编译目标 `1.8` |
| Spring Boot | 2.7.18 | 2.x 最终稳定版 |
| Spring Security | 5.7.x | Boot 自带 |
| MyBatis-Plus | 3.5.5 | ORM |
| PostgreSQL 驱动 | 42.6.0 | JDBC 驱动 |
| jjwt | 0.11.5 | JWT 库 |
| Maven | 3.6+ | 构建工具 |

---

## 生成步骤

### 第一步：创建 Maven 项目

执行以下操作生成 `evolib-backend/` 目录，含 `pom.xml`。

> 关键：Maven 项目结构必须包含 `src/main/java/com/evolib/` 路径。

### 第二步：确认 pom.xml 依赖

确保 `pom.xml` 包含以下关键依赖（使用模板 `templates/be/pom.xml.tmpl`）：

- `spring-boot-starter-web`（REST API）
- `spring-boot-starter-security`（认证授权）
- `mybatis-plus-boot-starter` 3.5.5（ORM）
- `postgresql` 42.6.0（数据库驱动）
- `jjwt-api` / `jjwt-impl` / `jjwt-jackson` 0.11.5（JWT）
- `spring-boot-starter-test`（单元测试）

> 警告：不要引入 Spring Boot 3.x 依赖——JDK 8 不支持。所有版本号严格参照上方表格。

### 第三步：创建完整目录树

必须生成以下目录（空目录也要创建，后续编码时直接使用）：

```
evolib-backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/evolib/
│   │   │   ├── EvolibApplication.java        # Spring Boot 启动类
│   │   │   │
│   │   │   ├── config/                        # 配置层
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtProperties.java
│   │   │   │   ├── MyBatisPlusConfig.java
│   │   │   │   └── WebMvcConfig.java
│   │   │   │
│   │   │   ├── common/                        # 公共层
│   │   │   │   ├── Result.java
│   │   │   │   ├── ErrorCode.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   ├── security/                      # 安全层
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── UserPrincipal.java
│   │   │   │
│   │   │   └── module/                        # 业务模块（按域垂直划分）
│   │   │       ├── auth/
│   │   │       │   ├── controller/
│   │   │       │   │   └── AuthController.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── AuthService.java
│   │   │       │   │   └── impl/
│   │   │       │   │       └── AuthServiceImpl.java
│   │   │       │   └── dto/
│   │   │       │       ├── LoginRequest.java
│   │   │       │       └── LoginResponse.java
│   │   │       │
│   │   │       ├── book/
│   │   │       │   ├── controller/
│   │   │       │   │   └── BookController.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── BookService.java
│   │   │       │   │   └── impl/
│   │   │       │   │       └── BookServiceImpl.java
│   │   │       │   ├── mapper/
│   │   │       │   │   └── BookMapper.java
│   │   │       │   ├── entity/
│   │   │       │   │   └── Book.java
│   │   │       │   └── dto/
│   │   │       │       ├── BookDTO.java
│   │   │       │       └── BookSearchRequest.java
│   │   │       │
│   │   │       ├── reader/
│   │   │       │   ├── controller/
│   │   │       │   │   └── ReaderController.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── ReaderService.java
│   │   │       │   │   └── impl/
│   │   │       │   │       └── ReaderServiceImpl.java
│   │   │       │   ├── mapper/
│   │   │       │   │   └── ReaderMapper.java
│   │   │       │   ├── entity/
│   │   │       │   │   └── Reader.java
│   │   │       │   └── dto/
│   │   │       │       ├── ReaderDTO.java
│   │   │       │       └── RegisterRequest.java
│   │   │       │
│   │   │       ├── borrow/
│   │   │       │   ├── controller/
│   │   │       │   │   └── BorrowController.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── BorrowService.java
│   │   │       │   │   └── impl/
│   │   │       │   │       └── BorrowServiceImpl.java
│   │   │       │   ├── mapper/
│   │   │       │   │   └── BorrowRecordMapper.java
│   │   │       │   ├── entity/
│   │   │       │   │   └── BorrowRecord.java
│   │   │       │   └── dto/
│   │   │       │       ├── BorrowRequest.java
│   │   │       │       ├── BorrowResponse.java
│   │   │       │       └── ReturnResponse.java
│   │   │       │
│   │   │       ├── admin/
│   │   │       │   ├── controller/
│   │   │       │   │   └── AdminController.java
│   │   │       │   └── service/
│   │   │       │       └── AdminService.java
│   │   │       │
│   │   │       └── audit/
│   │   │           ├── entity/
│   │   │           │   └── AuditLog.java
│   │   │           ├── mapper/
│   │   │           │   └── AuditLogMapper.java
│   │   │           ├── service/
│   │   │           │   └── AuditService.java
│   │   │           └── annotation/
│   │   │               └── Auditable.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml            # 主配置
│   │       ├── application-dev.yml        # 开发环境
│   │       └── application-prod.yml       # 生产环境模板
│   │
│   └── test/
│       └── java/com/evolib/              # 测试目录（空壳）
```

### 第四步：EvolibApplication.java 启动类

```java
package com.evolib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EvoLib 图书馆管理系统 — 启动入口
 * 
 * @SpringBootApplication 是一个组合注解，等于同时标注：
 *   @Configuration  — 标记此类为配置类
 *   @EnableAutoConfiguration — 让 Spring Boot 自动配置（数据源、Web、Security 等）
 *   @ComponentScan  — 自动扫描 com.evolib 包下所有 @Component/@Service/@Controller
 */
@SpringBootApplication
public class EvolibApplication {
    public static void main(String[] args) {
        SpringApplication.run(EvolibApplication.class, args);
    }
}
```

### 第五步：确认 application.yml

确保配置文件包含以下基本结构：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/evolib
    username: evolib
    password: ${DB_PASSWORD:evolib123}       # ← 优先取环境变量，默认值仅供本地开发
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20                  # 连接池最大连接数
      minimum-idle: 5                        # 最小空闲连接

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true       # 自动映射 book_title → bookTitle
  global-config:
    db-config:
      logic-delete-field: is_active          # 逻辑删除字段名
      logic-delete-value: false              # 已删除的值
      logic-not-delete-value: true           # 未删除的值

jwt:
  secret: ${JWT_SECRET:evolib-jwt-secret-key-change-in-production}
  expiration: 28800000                       # 8 小时，单位毫秒

library:
  max:
    borrow:
      days: 30                               # 借阅天数
      count: 3                               # 每人最多借 3 本
  default:
    password: phone-last-6                   # 默认密码规则：手机号后六位
```

---

## 验收标准

1. `mvn compile` 成功，无报错
2. `mvn spring-boot:run` 启动成功，输出 `Started EvolibApplication`
3. 浏览器访问 `http://localhost:8080` 返回 404（暂无接口，正常）
4. 生成的包结构与上方目录树**完全一致**，无缺漏目录
