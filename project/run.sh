#!/bin/bash

# Ensure Java 17 is used
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Package the application if needed
mvn clean package -DskipTests

# Run the application
java -jar target/search-service-1.0.0.jar
