# Only a packing stage. JAR must be available
# Package stage
FROM amazoncorretto:26@sha256:4c445efde5dc4feaf37706cc941efe01d715543d49e274ee36aca204c5c8552a
WORKDIR /app

# Copy the MVN generated JAR into a standard name in the image
COPY mars-sim-console.jar mars-sim-console.jar

# The folder /app/data/mars-sim build be a bind volume if the simulation state is persistent
ENV JAVA_OPTS="-Xmx2048m"

# Attempt to load if saved simulation is present or new if not. Use a different data directory that is mapped to a 
# Docker volume
CMD java $JAVA_OPTS -cp /app/mars-sim-console.jar org.mars_sim.headless.MarsProjectHeadless -load -new -timeratio 1024 -remote 18080 -datadir /app/data/mars-sim 

EXPOSE 18080