# deploy — 部署与交付

## 目标

定义 EvoLib MVP 从「代码」到「可运行 JAR」到「线上运行」的完整交付流程。

---

## Phase 1 部署拓扑

```
┌──────────────────────────────────────────┐
│  服务器（内网 / 云主机）                     │
│                                          │
│  ┌──────────────────────────────────┐   │
│  │  evolib.jar  (java -jar)         │   │
│  │  Spring Boot 内嵌 Tomcat :8080    │   │
│  │                                  │   │
│  │  ├── /api/v1/*     REST API     │   │
│  │  └── /*            Vue SPA 静态  │   │
│  └─────────────┬────────────────────┘   │
│                │ JDBC :5432              │
│                ▼                         │
│  ┌──────────────────────────────────┐   │
│  │  PostgreSQL 14+                  │   │
│  │  数据库：evolib                    │   │
│  └──────────────────────────────────┘   │
│                                          │
│  备份：pg_dump 每日凌晨 3 点 → 保留 7 天    │
└──────────────────────────────────────────┘
```

---

## 构建流程

### 一键构建脚本

```bash
#!/bin/bash
# build.sh — 构建 EvoLib MVP

set -e  # 任何命令失败则立即退出

echo "=== 第 1 步：构建前端 ==="
cd evolib-frontend
npm install                  # 安装依赖
npm run build                # Vite 构建 → 输出到 ../evolib-backend/src/main/resources/static/

echo "=== 第 2 步：构建后端 ==="
cd ../evolib-backend
mvn clean package -DskipTests  # Maven 打包 → target/evolib-1.0.0.jar

echo "=== 构建完成 ==="
echo "产物：evolib-backend/target/evolib-1.0.0.jar"
echo ""
echo "=== 第 3 步：启动 ==="
echo "java -jar target/evolib-1.0.0.jar"
```

### 开发模式启动

```bash
#!/bin/bash
# dev.sh — 开发环境启动（前后端分离 + HMR）

# 终端 1：启动后端
cd evolib-backend
mvn spring-boot:run &

# 终端 2：启动前端（Vite HMR，修改即刷新浏览器）
cd evolib-frontend
npm run dev
# 浏览器打开 http://localhost:3000
# 前端 /api 请求自动代理到后端 localhost:8080
```

---

## 环境变量

生产环境启动前设置以下环境变量：

| 变量名 | 说明 | 示例 |
| --- | --- | --- |
| `DB_PASSWORD` | PostgreSQL 密码 | `java -jar evolib.jar --spring.datasource.password=xxx` 或环境变量 |
| `JWT_SECRET` | JWT 签名密钥 | 至少 256 位随机字符串 |
| `SPRING_PROFILES_ACTIVE` | 激活的配置 profile | `prod` |

---

## Docker PostgreSQL 快速启动

```bash
# 启动 PostgreSQL 容器（首次使用）
docker run -d \
  --name evolib-db \
  -e POSTGRES_USER=evolib \
  -e POSTGRES_PASSWORD=yourpassword \
  -e POSTGRES_DB=evolib \
  -p 5432:5432 \
  postgres:14

# 导入 DDL（首次启动后执行一次）
psql -h localhost -U evolib -d evolib -f docs/evolib-skills/templates/db/create-table.sql.tmpl
```

---

## 数据库备份

```bash
#!/bin/bash
# backup-db.sh — PostgreSQL 每日备份
# crontab: 0 3 * * * /path/to/backup-db.sh

BACKUP_DIR="/backup/evolib"
DB_NAME="evolib"
DB_USER="evolib"
RETENTION_DAYS=7                          # 保留最近 7 天备份

mkdir -p "$BACKUP_DIR"

# pg_dump 导出
pg_dump -U "$DB_USER" -d "$DB_NAME" -F c \
  -f "$BACKUP_DIR/evolib_$(date +%Y%m%d).dump"

# 清理 7 天前的旧备份文件
find "$BACKUP_DIR" -name "*.dump" -mtime +"$RETENTION_DAYS" -delete

echo "[$(date)] 数据库备份完成"
```

---

## 启动验证

部署后执行以下命令验证系统正常：

```bash
# 1. 健康检查
curl http://localhost:8080/api/v1/books/search?keyword=test
# 预期返回：{"code":0,"data":{"total":0,"list":[]}}

# 2. 登录测试
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"readerId":"R001","password":"123456"}'
# 预期返回：{"code":0,"data":{"token":"eyJ...","role":"ROLE_READER"}}
```

---

## 交付物清单

| 文件 | 说明 |
| --- | --- |
| `evolib-backend/target/evolib-1.0.0.jar` | 可运行 JAR 包 |
| `evolib-backend/src/main/resources/application-prod.yml` | 生产环境配置模板 |
| `docs/evolib-skills/scripts/build.sh` | 一键构建脚本 |
| `docs/evolib-skills/scripts/dev.sh` | 开发环境启动脚本 |
| `docs/evolib-skills/scripts/backup-db.sh` | 数据库备份脚本 |
| `docs/evolib-skills/assets/postman-collection.json` | Postman 接口集合 |
