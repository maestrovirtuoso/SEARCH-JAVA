#!/bin/bash

# Ensure Java 17 is used
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Run the tests
mvn test
