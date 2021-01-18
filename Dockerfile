FROM openjdk:11.0.9.1-jre
WORKDIR /app
# Override when building
ARG mars_version=3.1.2
COPY mars-sim-headless/target/mars-sim-headless-${mars_version}-jar-with-dependencies.jar mars-sim-headless.jar

# The folder /app/data/mars-sim build be a bind volume if the simulation state is persistent

# Cannot pass coommandline argument when using the JAR launcher
# Attempt to load if saved simulation is present or new if not. Use a different data directory that is mapped to a 
# Docker volume
ENTRYPOINT [ "java", "-cp", "/app/mars-sim-headless.jar", "org.mars_sim.headless.MarsProjectHeadless", "-load", "-new", "-timeratio", "1024", "-remote", "18080", "-datadir", "/app/data/mars-sim" ]
EXPOSE 18080