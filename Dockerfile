FROM openjdk:11.0.9.1-jre
WORKDIR /app
# Override when building
ARG mars_version=3.1.2
COPY mars-sim-headless/target/mars-sim-headless-${mars_version}-jar-with-dependencies.jar mars-sim-headless.jar

# Cannot pass coommandline argument when using the JAR launcher
ENTRYPOINT [ "java", "-cp", "/app/mars-sim-headless.jar", "org.mars_sim.headless.MarsProjectHeadless", "-remote", "18080", "-new" ]
EXPOSE 18080