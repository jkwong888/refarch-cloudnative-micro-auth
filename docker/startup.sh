#!/bin/bash
set -e

# find the java heap size as 50% of container memory using sysfs
max_heap=`cat /sys/fs/cgroup/memory/memory.limit_in_bytes | xargs -I{} echo "({} / 1024 / 1024) / 2" | bc`
export JAVA_OPTS="${JAVA_OPTS} -Xmx${max_heap}m"

# Set basic java options
export JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom"

# Load agent support if required
source ./agents/newrelic.sh

# load the shared key
export JAVA_OPTS="${JAVA_OPTS} -Djwt.sharedSecret=`cat /var/run/secrets/hs256-key/key`"

# debug logging
#export JAVA_OPTS="${JAVA_OPTS} -Dlogging.level.auth=DEBUG"

echo "Starting with Java Options ${JAVA_OPTS}"

# Start the application
exec java ${JAVA_OPTS} -jar /app.jar

