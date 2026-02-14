#!/bin/bash

# Define JAVA_HOME para Java 17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using Java version:"
java -version
echo ""

# Executa o comando Maven passado como argumento
if [ $# -eq 0 ]; then
    echo "Starting Spring Boot application..."
    mvn spring-boot:run
else
    echo "Running: mvn $@"
    mvn "$@"
fi

