#!/usr/bin/env bash
set -euo pipefail

# Maven build tool (JDK 21 is already present in the base image; the maven
# package also pulls a JDK as a dependency if one is missing).
if ! command -v mvn >/dev/null 2>&1; then
  sudo apt-get update
  sudo apt-get install -y --no-install-recommends maven
fi

# Dev-mode config file. It is gitignored (see .gitignore), so recreate it from
# the bundled template when absent. So Novel reads config-dev.ini when ENV=dev.
if [ ! -f config-dev.ini ]; then
  cp bundle/config.ini config-dev.ini
fi

# Warm the dependency cache and verify the sources compile.
mvn -q -DskipTests compile
