FROM openjdk:8-alpine

VOLUME /tmp
EXPOSE 8080

ADD app.jar app.jar
RUN sh -c 'touch /app.jar'

# Install prereqs
RUN apk --no-cache update \
 && apk add jq ca-certificates wget bash bc \
 && update-ca-certificates

# Copy agent files
COPY agents/ /agents/
COPY startup.sh startup.sh

# Add the new relic agent
RUN wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip -O /tmp/newrelic.zip \
  && unzip -d /agents /tmp/newrelic.zip newrelic/newrelic.jar \
  && rm /tmp/newrelic.zip

ENTRYPOINT ["./startup.sh"]
