** Mars Simulation Project **
Copyright (C) 2016 Scott Davis
http://mars-sim.sourceforge.net/

---------------------------------------------------------------------

** Installation **

This Java application requires a Java Runtime Environment (JRE) or 
Java SE Development Kit (JDK) of version 8. 

Note that mars-sim currently does not work with OpenJDK 8 in linux. 
Install oracle-java8 instead. 

The latest JVM can be freely downloaded from Oracle at:
http://www.oracle.com/technetwork/java/javase/downloads/index.html

---------------------------------------------------------------------

** Running **

1. For most operating systems you can simply double click on the 
mars-sim-main-[version number].jar file since it is an executable jar.

2. Alternatively, you can also start mars-sim manually from a command 
console or command line which will allow users to see mars-sim's 
internal logging statements while running mars-sim.

First, go to the directory containing the jar file in the command line,

Second, type in:

java -jar mars-sim-main-[version/build number].jar 

(replacing the [version number] with jar file's version/build number).

---------------------------------------------------------------------

** Memory Allocation **

The maximum memory allocation for mars-sim is tunnable prior to the 
start of the simulation. 

By default, mars-sim uses up to 1 GB maximum memory.

If your machine has less than 1 GB or more than 1 GB, you may 
customize mars-sim to run at one of the configuration by adding a 
numeral after the jar file as shown below :

java -jar mars-sim-main-[version number].jar 4

// no numerical --> 256 Min, 1024MB Max (by default)
// 0 --> 256 Min, 1024MB Max

// 1 --> 256 Min, 512MB Max
// 2 --> 256 Min, 768MB Max
// 3 --> 256 Min, 1024MB Max
// 4 --> 256 Min, 1536MB Max
// 5 --> 256 Min, 2048MB Max


---------------------------------------------------------------------

** Headless Mode **

mars-sim is designed with a level of A.I. capable of running the 
entire simulation on its own. 

If you simply want it to run in headless mode for hours without 
your input and/or want to save some precious CPU resources from 
creating the GUI, start in headless mode by typing :

java -jar mars-sim.jar 1 headless new

OR 

java -jar mars-sim.jar 0 headless

Note 1: '0' or a numeral is for setting up mars-sim to use a
 memory configuration different from the default. It is optional.

Note 2: if the 'new' switch is not provided, it will be added 
automatically.

Note 3: by default, the simulation will be saved automatically
(as default.sim) once every 15 minutes, as dictated by the 
following attribute value found in the simulation.xml :  

<autosave-interval value="15.0" /> 


---------------------------------------------------------------------

** Load Saved Simulation **

If you want to load a previously saved simulation in GUI mode, type :

java -jar mars-sim.jar 3 load

for headless mode, type :

java -jar mars-sim.jar 3 headless load

---------------------------------------------------------------------

** Questions **

For questions or comments or submitting a ticket, contact the Mars
Simulation Project user mailing list:
mars-sim-users@lists.sourceforge.net 

If you want to discuss mars-sim in our facebook community, go to 
https://www.facebook.com/groups/125541663548/

If you'd like to join in development, contact the Mars Simulation 
Project developer mailing list:
mars-sim-developers@lists.sourceforge.net

---------------------------------------------------------------------

** Credits **

A full list of contributors is available from the Help menu within 
the application. The credits can also be viewed by opening the file 
source/mars-sim-ui/src/main/resources/docs/help/about.html in a web 
browser.

---------------------------------------------------------------------

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