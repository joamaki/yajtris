#!/bin/sh
test -z "$JAVA" && JAVA=java
test -n "$JAVA_HOME" && JAVA=$JAVA_HOME/bin/java

$JAVA -jar yajtris.jar
