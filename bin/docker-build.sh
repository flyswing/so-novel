#!/bin/bash
# 从 flyswing/custom 源码构建 Docker 镜像
set -euo pipefail

PROJECT_PATH="$(cd "$(dirname "$0")/.." && pwd)"
IMAGE_NAME="${IMAGE_NAME:-flyswing/sonovel:custom}"
ARCH="${ARCH:-$(uname -m)}"

cd "$PROJECT_PATH"

echo "🏗️  Maven 打包..."
mvn -q clean package -Dmaven.test.skip=true

echo "📦 准备 Docker 构建上下文..."
rm -rf .docker-build
mkdir -p .docker-build/rules
cp target/app-jar-with-dependencies.jar .docker-build/app.jar
cp docker/config.ini .docker-build/config.ini
cp -r bundle/rules/. .docker-build/rules/

echo "🐳 构建镜像: ${IMAGE_NAME}"
if docker info >/dev/null 2>&1; then
  docker build -f Dockerfile -t "${IMAGE_NAME}" .docker-build
else
  sudo docker build -f Dockerfile -t "${IMAGE_NAME}" .docker-build
fi

rm -rf .docker-build
echo "✅ 完成: ${IMAGE_NAME}"
echo ""
echo "📁 初始化数据目录..."
mkdir -p docker/data/downloads docker/data/rules
if [ ! -f docker/data/config.ini ]; then
  cp docker/config.ini docker/data/config.ini
fi
if [ ! -f docker/data/rules/main.json ]; then
  cp -r bundle/rules/. docker/data/rules/
fi
echo ""
echo "启动: docker compose up -d"
echo "登录: http://localhost:7765/login.html  (admin / admin)"
