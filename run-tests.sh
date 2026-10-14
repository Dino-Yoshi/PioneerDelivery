#!/bin/sh
set -eu

project_dir=$(CDPATH= cd "$(dirname "$0")" && pwd)
cd "$project_dir"
if [ -n "${JAVA_HOME:-}" ]; then
    exec "$JAVA_HOME/bin/java" tools/TestSuite.java
fi
exec java tools/TestSuite.java
