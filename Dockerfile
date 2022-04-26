# Only a packing stage. JAR must be available
# Package stage
FROM openjdk:11.0.15-jre-slim
WORKDIR /app

# Version name
ARG version_name

# Copy the MVN generated JAR into a standard name in the image
COPY mars-sim-headless/target/mars-sim-headless-$version_name-jar-with-dependencies.jar mars-sim-headless.jar

# The folder /app/data/mars-sim build be a bind volume if the simulation state is persistent
ENV JAVA_OPTS="-Xmx1536m"

# Attempt to load if saved simulation is present or new if not. Use a different data directory that is mapped to a 
# Docker volume
CMD java $JAVA_OPTS -cp /app/mars-sim-headless.jar org.mars_sim.headless.MarsProjectHeadless -load -new -timeratio 1024 -remote 18080 -datadir /app/data/mars-sim 

EXPOSE 18080