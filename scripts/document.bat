echo off

set PACKAGES=org.mars_sim.msp
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.equipment
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.events
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.malfunction
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.person
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.person.ai
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.person.ai.mission
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.person.ai.task
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.structure
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.structure.building
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.structure.building.function
set PACKAGES=%PACKAGES% org.mars_sim.msp.simulation.vehicle
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.tool
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.tool.navigator
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.tool.monitor
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.tool.search
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.tool.time
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.unit_window
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.unit_window.equipment
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.unit_window.person
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.unit_window.vehicle
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.unit_window.structure
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.unit_window.structure.building
set PACKAGES=%PACKAGES% org.mars_sim.msp.ui.standard.unit_display_info

echo on

cd ../
javadoc -classpath .;jars\jfreechart.jar;jars\jcommon.jar;jars\junit.jar;jars\plexus-core.jar;jars\commons-collections-3.1.jar;jars\log4j-1.2.8.jar -d docs/javadoc -overview overview.html -use -author -windowtitle "Mars Simulation Project v 2.77 API Specification" -doctitle "Mars Simulation Project v 2.77<BR>API Specification" -header "<b>Mars Simulation Project</b><br><font size=\"-1\">v2.77</font>" %PACKAGES%
cd scripts
