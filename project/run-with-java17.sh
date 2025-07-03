#!/bin/bash

# Ensure we're using Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

echo "Using Java version:"
java -version

# Run the application with Java 17
mvn clean compile spring-boot:run
