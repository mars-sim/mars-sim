** Mars Simulation Project **
Copyright (C) 2001 Scott Davis
http://mars-sim.sourceforge.net/

----------------------------------------------------------------

For questions or comments on this project, contact the Mars
Simulation Project user mailing list:
mars-sim-users@lists.sourceforge.net 

If you'd like to join in development visit:
https://sourceforge.net/projects/mars-sim/

----------------------------------------------------------------

** License **

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA

----------------------------------------------------------------

** Installation **

This Java application requires a Java 2 virtual machine.  The latest
JVM can be downloaded from Sun at:
    http://www.javasoft.com/products/jdk/1.4/jre/

1) Unzip MarsProject_X.XX.zip (where XXX is the version number) into a 
   directory.  ie. "unzip MarsProject_2.74.zip"

2) Start the Mars Simulation Project by running the "MarsProject.bat"
   batch/script file. 

   You will need to make sure the Java virtual machine is in your system
   path.  

   You will need to set the classpath to include the JAR libraries the
   program needs.  For Windows environments, see MarsProject.bat.  For 
   UNIX and Linux environments, see MarsProject.sh.

Command line arguments: java MarsProject [-new | -load file]

   -new    Forces the simulation to start with a new simulation.
           Default behavior is to load the default saved simulation.

   -load file    Loads a saved simulation from the given file path.

------------------------------------------------------------------

** Configuration **

Several XML files are available in the "conf" directory for the user to 
configure units and modify properties in the simulation.

people.xml - Adding/configuring people
settlements.xml - Adding/configuring settlements
vehicles.xml - Adding/configuring vehicles
properties.xml - Configuring simulation properties
medical.xml - Adding/configuring medical complaints
malfunction.xml - Adding/configuring malfunctions


