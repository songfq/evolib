#!/bin/bash
# dev.sh — EvoLib 开发环境启动脚本
# 用法：bash dev.sh
# 说明：启动后端（Spring Boot）和前端（Vite HMR），前端 /api 请求自动代理到后端

echo "================================="
echo "  EvoLib 开发环境启动"
echo "================================="

# ---- 终端 1：启动后端 ----
echo ""
echo ">>> 启动后端 (Spring Boot :8080)..."
cd evolib-backend
mvn spring-boot:run &
BACKEND_PID=$!
echo "  后端 PID: $BACKEND_PID"

# ---- 终端 2：启动前端 ----
echo ""
echo ">>> 启动前端 (Vite :3000)..."
cd ../evolib-frontend

if [ ! -d "node_modules" ]; then
    echo "  安装依赖..."
    npm install
fi

npm run dev &
FRONTEND_PID=$!
echo "  前端 PID: $FRONTEND_PID"

echo ""
echo "================================="
echo "  ✅ 开发环境已启动"
echo "================================="
echo "  前端地址: http://localhost:3000"
echo "  后端地址: http://localhost:8080"
echo "  API 代理: localhost:3000/api → localhost:8080/api"
echo ""
echo "  停止服务: kill $BACKEND_PID $FRONTEND_PID"
echo "================================="

# 等待用户 Ctrl+C 停止
trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; echo ' 服务已停止'; exit" INT TERM
wait
