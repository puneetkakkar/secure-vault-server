#!/bin/sh

# Exit on any error
set -e

# Function to handle cleanup on exit
cleanup() {
    echo "Shutting down development server..."
    if [ ! -z "$WATCH_PID" ]; then
        kill $WATCH_PID 2>/dev/null || true
    fi
    if [ ! -z "$MAVEN_PID" ]; then
        kill $MAVEN_PID 2>/dev/null || true
    fi
    exit 0
}

# Set up signal handlers
trap cleanup TERM INT

# Check if inotify-tools is available
if ! command -v inotifywait >/dev/null 2>&1; then
    echo "Warning: inotify-tools not found. Hot reload will not work."
    echo "Starting Spring Boot application without file watching..."
    exec mvn spring-boot:run -B
fi

# Start file watcher in background for hot reload
echo "Starting file watcher for hot reload..."
(
    while inotifywait -r -e modify,create,delete,move /app/src/main/ 2>/dev/null; do
        echo "Changes detected, recompiling..."
        mvn compile -B -o -DskipTests >/dev/null 2>&1 || echo "Compilation failed, continuing..."
    done
) &
WATCH_PID=$!

# Start Spring Boot application
echo "Starting Spring Boot application..."
mvn spring-boot:run -B &
MAVEN_PID=$!

# Wait for either process to exit
wait $MAVEN_PID