#!/bin/sh
# Standard Gradle wrapper launcher.
# If gradle/wrapper/gradle-wrapper.jar is missing, open this project in
# Android Studio once (it regenerates the jar automatically), or run
# `gradle wrapper --gradle-version 8.9` on a machine with Gradle installed.

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
APP_HOME=$(cd "$(dirname "$0")" && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

exec java $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
