cd ../

# Create simulation jar.
jar -cvf jars/msp-simulation.jar `find ./org/mars_sim/msp/simulation -name \*.class -printf "%p "`
# Create standard UI jar.
jar -cvf jars/msp-standard-ui.jar `find ./org/mars_sim/msp/ui/standard -name \*.class -printf "%p "` ./images/*.*

# Create main Mars Project jar.
jar -cvmf scripts/main-manifest.txt MarsProject.jar org/mars_sim/msp/MarsProject.class

