set PACKAGES = org.mars_sim.msp
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.equipment
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.events
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.malfunction
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.person
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.person.ai
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.aimulation.person.ai.mission
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.aimulation.person.ai.task
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.structure
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.structure.building
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.structure.building.function
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.structure.building.function.impl
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.structure.template
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.simulation.vehicle
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.tool
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.tool.navigator
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.tool.monitor
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.tool.search
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.tool.time
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.unit_window
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.unit_window.equipment
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.unit_window.person
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.unit_window.vehicle
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.unit_window.structure
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.unit_window.structure.building
set PACKAGES := ${PACKAGES}: org.mars_sim.msp.ui.standard.unit_display.info

set CLASSPATH = .:jars/jcommon.jar:jars/jfreechart.jar
set VERSION = 2.76


cd ../

javadoc -classpath ${CLASSPATH} -d docs/javadoc -overview overview.html -use -author -windowtitle "Mars Simulation Project v ${VERSION} API Specification" -doctitle "Mars Simulation Project v ${VERSION}<BR>API Specification" -header "<b>Mars Simulation Project</b><br><font size=\"-1\">v${VERSION}</font>" ${PACKAGES}

