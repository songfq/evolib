#!/bin/bash
# build.sh — EvoLib MVP 一键构建脚本
# 用法：bash build.sh
# 说明：先构建前端（Vite），产物自动落入后端 static/，再构建后端（Maven），输出一个可运行 JAR

set -e  # 任何命令失败则立即退出，避免带着半成品继续

echo "================================="
echo "  EvoLib MVP 构建脚本"
echo "================================="

# ---- 第 1 步：构建前端 ----
echo ""
echo ">>> 第 1 步：构建前端 (Vue 3 + Vite)..."
cd evolib-frontend

if [ ! -d "node_modules" ]; then
    echo "  安装依赖..."
    npm install
fi

npm run build
echo "  ✅ 前端构建完成 → evolib-backend/src/main/resources/static/"

# ---- 第 2 步：构建后端 ----
echo ""
echo ">>> 第 2 步：构建后端 (Spring Boot + Maven)..."
cd ../evolib-backend

mvn clean package -DskipTests

# ---- 第 3 步：输出产物信息 ----
echo ""
echo "================================="
echo "  ✅ 构建完成！"
echo "================================="
echo "  产物: evolib-backend/target/evolib-1.0.0.jar"
echo ""
echo "  启动命令:"
echo "    cd evolib-backend"
echo "    java -jar target/evolib-1.0.0.jar"
echo ""
echo "  访问地址: http://localhost:8080"
echo "================================="
