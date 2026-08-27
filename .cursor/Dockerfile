FROM maven:3.9-eclipse-temurin-21

# Git for repository operations; fontconfig for PDF/EPUB rendering (openhtmltopdf)
RUN apt-get update \
    && apt-get install -y --no-install-recommends git ca-certificates fontconfig \
    && rm -rf /var/lib/apt/lists/*
