#!/bin/bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
echo "Using Java version:"
java -version
echo ""
echo "Building project..."
mvn clean package -DskipTests
