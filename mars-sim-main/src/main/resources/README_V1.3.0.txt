** Mars Simulation Project **
Copyright (C) 2017 Scott Davis
http://mars-sim.sourceforge.net/

---------------------------------------------------------------------

** Minimum Requirements **

- Dual Core Pentium/Celeron 

- 1.5GHz or above

- 1 GB RAM dedicated for running mars-sim

- 64-bit Java 8 (jre1.8.0_91 or above) 

---------------------------------------------------------------------

** Installation **

- This Java application requires the latest 64-bit Java Runtime Environment (JRE) or 
Java SE Development Kit (JDK) version 8. 

- Note that mars-sim currently does not work with OpenJDK 7/8/9 or Java 9
Please use oracle's java 8 instead. 

- The latest JVM can be freely downloaded from Oracle at:
http://www.oracle.com/technetwork/java/javase/downloads/index.html

---------------------------------------------------------------------

** Starting a new sim **

A. Graphic Mode

-  Double-click on 'mars-sim-main-[version/build].jar' to begin 
a new simulation in GUI mode since the jar file is executable 
in most operating systems.

- in the Main Menu, choose 'New Sim' to start a new simulation.

- Alternatively, one may start the simulation manually from a terminal 
or command prompt which will display mars-sim's internal logging
 statements while running mars-sim.

- To start the graphic mode via a terminal, go to the directory 
containing the jar file and type :

> java -jar mars-sim-main-[version/build].jar 

	OR 

> java -jar mars-sim-main-[version/build].jar new

Note a : replacing [version/build] with the current version/build.

Note b : the argument 'new' is optional. If the argument 'load' 
is not provided for, it will assume that the user is interested in 
starting a new simulation and 'new' will be appended automatically.

Note c: by default, the simulation will be saved automatically
with a new filename with a date/time stamp, the # of sol and 
the build # once every 15 minutes. It's located in the 
\[HOME]\.mars-sim\autosave\ directory, as dictated by the 
following attribute value in simulation.xml :

	<autosave-interval value="15.0" /> 

B. Headless Mode

- mars-sim is designed with a level of autonomy capable of running the 
entire simulation on its own. Therefore, you may prefer to run it
in a terminal for hours/days in the least intrusive manner with lowest 
CPU resources in headless mode. Type in the terminal as follows :

> java -jar mars-sim-main-[version/build].jar 2 headless new

	OR 

> java -jar mars-sim-main-[version/build].jar 2 headless

Note a: the '2' numeral argument is for setting up mars-sim to use 
a memory configuration (see below) different from the default. 
It is optional.

Note b: the 'new' argument is optional. 

Note c: by default, the simulation will be saved automatically
(as default.sim in the \.mars-sim\saved\ directory) once every 
15 minutes, as dictated by the following attribute value in 
simulation.xml :

	<autosave-interval value="15.0" /> 

---------------------------------------------------------------------

** Memory Allocation **

- The maximum memory allocation for mars-sim is adjustable prior to the 
start of the simulation. 

- By default, mars-sim uses up to 1 GB maximum memory.

- If your machine has less than 1 GB or more than 1 GB, you may 
customize mars-sim to run at one of the configuration by adding a 
numeral argument after the jar file as shown below :

> java -jar mars-sim-main-[version/build].jar 4

// no numerical --> 256MB Min, 1024MB Max (by default)
// 0 --> 256MB Min, 1024MB Max
// 1 --> 256MB Min, 512MB Max
// 2 --> 256MB Min, 768MB Max
// 3 --> 256MB Min, 1024MB Max
// 4 --> 256MB Min, 1536MB Max
// 5 --> 256MB Min, 2048MB Max


---------------------------------------------------------------------

** Load Saved Simulation **

A. Graphic Mode

- If you want to load a previously saved simulation in graphic mode,

-- Double-click the jar file to start

-- Choose 'Load Sim' in the Main Menu

-- Select the default.sim or or a *.sim in the FileChooser 


- Alternatively, you may use the terminal to type : 

> java -jar mars-sim-main-[version/build].jar 3 load

- This will load the FileChooser and open up \.mars-sim\saved\

Note 1 : the order of the arguments is not important.

Note 2 : do NOT type 'default.sim' as an argument since default.sim
is located at \.mars-sim\saved\

	OR

> java -jar mars-sim-main-[version/build].jar 3 load 123.sim

- This will load '123.sim', instead of the default saved sim.

Note 3 : the order of the arguments is not important.

Note 4 : '123.sim' must be present in the same directory 
where the jar file is at.


B. Headless Mode

- To load the default saved sim without the GUI interface, type :

> java -jar mars-sim-main-[version/build].jar 3 headless load 

Note 1 : the order of the arguments is not important.

Note 2 : by default, it is assumed the user is interested in 
loading the default saved sim file. Do NOT type 'default.sim' 
as an argument since default.sim is located at \.mars-sim\saved\

- To load a particular saved sim without the GUI interface, type :

> java -jar mars-sim-main-[version/build].jar 3 headless load 123.sim

Note 3 : the order of the arguments is not important.

Note 4 : '123.sim' must be present in the same directory 
where the jar file is at.


---------------------------------------------------------------------

** Questions **

- For questions or comments or submitting a ticket, contact the Mars
Simulation Project user mailing list:
mars-sim-users@lists.sourceforge.net 

- If you want to discuss mars-sim in our facebook community, go to 
https://www.facebook.com/groups/125541663548/

- If you'd like to join in development, contact the Mars Simulation 
Project developer mailing list:
mars-sim-developers@lists.sourceforge.net

---------------------------------------------------------------------

** Credits **

- A full list of contributors is available from the Help menu within 
the application. The credits can also be viewed by opening the file 
 /docs/help/about.html in mars-sim's built-in web browser.

---------------------------------------------------------------------

** License **

- This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

- This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

- You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA