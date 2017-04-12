#!/bin/bash
set -e

# Set basic java options
export JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

# Load agent support if required
source ./agents/newrelic.sh

# load the shared key
export JAVA_OPTS="${JAVA_OPTS} -Djwt.sharedSecret=`cat /opt/hs256-key/key`"

# debug logging
#export JAVA_OPTS="${JAVA_OPTS} -Dlogging.level.auth=DEBUG"

echo "Starting with Java Options ${JAVA_OPTS}"

# Start the application
exec java ${JAVA_OPTS} -jar /app.jar

