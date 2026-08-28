#!/bin/bash
# 从 flyswing/custom 源码构建 Docker 镜像
set -euo pipefail

PROJECT_PATH="$(cd "$(dirname "$0")/.." && pwd)"
IMAGE_NAME="${IMAGE_NAME:-flyswing/sonovel:custom}"
GHCR_IMAGE="${GHCR_IMAGE:-ghcr.io/flyswing/so-novel:custom}"
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
if [ "${PUSH_GHCR:-0}" = "1" ]; then
  echo "📤 推送到 GHCR: ${GHCR_IMAGE}"
  if docker info >/dev/null 2>&1; then
    DOCKER_CMD=docker
  else
    DOCKER_CMD="sudo docker"
  fi
  echo "$(gh auth token)" | $DOCKER_CMD login ghcr.io -u "$(gh api user -q .login)" --password-stdin
  $DOCKER_CMD tag "${IMAGE_NAME}" "${GHCR_IMAGE}"
  $DOCKER_CMD push "${GHCR_IMAGE}"
  echo "✅ 已推送: ${GHCR_IMAGE}"
fi
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
echo "镜像: ghcr.io/flyswing/so-novel:custom"
echo "登录: http://localhost:7765/login.html  (admin / admin)"
