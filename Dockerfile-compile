# Build stage
# the Maven project
FROM maven:3.6.3-openjdk-11-slim AS build
WORKDIR /app/src

# Ideally the source code shouo dbe under a src subdirectory in GitHub repo
COPY pom.xml .
COPY mars-sim-core mars-sim-core/
COPY mars-sim-console mars-sim-console/
COPY mars-sim-headless mars-sim-headless/
COPY mars-sim-main mars-sim-main/
COPY mars-sim-mapdata mars-sim-mapdata/
COPY mars-sim-ui mars-sim-ui/

RUN mvn -DskipTests=true package

# Package stage
FROM openjdk:11.0.9.1-jre
WORKDIR /app
# Override when building
COPY --from=build /app/src/mars-sim-headless/target/mars-sim-headless.jar mars-sim-headless.jar

# The folder /app/data/mars-sim build be a bind volume if the simulation state is persistent

# Cannot pass coommandline argument when using the JAR launcher
# Attempt to load if saved simulation is present or new if not. Use a different data directory that is mapped to a 
# Docker volume
ENTRYPOINT [ "java", "-cp", "/app/mars-sim-headless.jar", "org.mars_sim.headless.MarsProjectHeadless", "-load", "-new", "-timeratio", "1024", "-remote", "18080", "-datadir", "/app/data/mars-sim" ]
EXPOSE 18080