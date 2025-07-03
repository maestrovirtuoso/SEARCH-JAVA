#!/bin/bash

# Ensure we're using Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

echo "Using Java version:"
java -version

# Run the application JAR with Java 17
java -jar target/search-service-1.0.0.jar
