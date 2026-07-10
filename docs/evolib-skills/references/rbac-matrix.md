# EvoLib RBAC 权限矩阵

> 参考：`docs/ARCHITECTURE_DESIGN.md` §8.2

## 三角色定义

| 角色 | Spring Security 标识 | 职责 |
| --- | --- | --- |
| 普通读者 | `ROLE_READER` | 检索图书、查看详情 |
| 流通馆员 | `ROLE_CIRCULATION` | 借书、还书、注册读者、修改手机号、查看在借清单 |
| 系统管理员 | `ROLE_ADMIN` | 上架图书、下架图书、重置读者密码、配置参数 |

## 后端 API 权限矩阵

| 路径模式 | HTTP 方法 | 权限要求 | 对应 SecurityConfig 配置 |
| --- | --- | --- | --- |
| `/api/v1/auth/login` | POST | 所有人（`permitAll()`） | `.antMatchers("/api/v1/auth/login").permitAll()` |
| `/api/v1/books/**` | GET | `ROLE_READER` 及以上 | `.hasAnyRole("READER","CIRCULATION","ADMIN")` |
| `/api/v1/borrow-records/**` | POST, PUT | `ROLE_CIRCULATION` | `.hasRole("CIRCULATION")` |
| `/api/v1/readers` | POST | `ROLE_CIRCULATION` | `.hasRole("CIRCULATION")` |
| `/api/v1/readers/*/phone` | PUT | `ROLE_CIRCULATION` | `.hasRole("CIRCULATION")` |
| `/api/v1/readers/*/borrows` | GET | `ROLE_CIRCULATION` | `.hasRole("CIRCULATION")` |
| `/api/v1/admin/**` | POST, PUT, DELETE | `ROLE_ADMIN` | `.hasRole("ADMIN")` |

## 前端页面权限矩阵

| 页面路径 | 权限要求 | 路由 `meta.role` | 读者能否访问 | 馆员能否访问 | 管理员能否访问 |
| --- | --- | --- | --- | --- | --- |
| `/login` | 无 | `auth: false` | ✅ | ✅ | ✅ |
| `/reader/search` | `ROLE_READER` | `role: 'ROLE_READER'` | ✅ | ❌ | ❌ |
| `/reader/detail/:isbn` | `ROLE_READER` | `role: 'ROLE_READER'` | ✅ | ❌ | ❌ |
| `/circulation/borrow` | `ROLE_CIRCULATION` | `role: 'ROLE_CIRCULATION'` | ❌ | ✅ | ❌ |
| `/circulation/return` | `ROLE_CIRCULATION` | `role: 'ROLE_CIRCULATION'` | ❌ | ✅ | ❌ |
| `/circulation/register` | `ROLE_CIRCULATION` | `role: 'ROLE_CIRCULATION'` | ❌ | ✅ | ❌ |
| `/circulation/borrows` | `ROLE_CIRCULATION` | `role: 'ROLE_CIRCULATION'` | ❌ | ✅ | ❌ |
| `/admin/add-book` | `ROLE_ADMIN` | `role: 'ROLE_ADMIN'` | ❌ | ❌ | ✅ |
| `/admin/remove-book` | `ROLE_ADMIN` | `role: 'ROLE_ADMIN'` | ❌ | ❌ | ✅ |
| `/admin/reset-password` | `ROLE_ADMIN` | `role: 'ROLE_ADMIN'` | ❌ | ❌ | ✅ |
