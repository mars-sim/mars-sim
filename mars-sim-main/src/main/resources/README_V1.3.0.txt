** Mars Simulation Project **
Copyright (C) 2016 Scott Davis
http://mars-sim.sourceforge.net/

---------------------------------------------------------------------

** Installation **

- This Java application requires a 64-bit Java Runtime Environment (JRE) or 
Java SE Development Kit (JDK) of version 8. 

- Note that mars-sim currently does not work with OpenJDK 8. Please
use oracle-java8 instead. 

- The latest JVM can be freely downloaded from Oracle at:
http://www.oracle.com/technetwork/java/javase/downloads/index.html

---------------------------------------------------------------------

** Running **

- For most operating systems you can simply double click on the 
mars-sim-main-[version number].jar file since it is an executable jar.

- Alternatively, you can also start mars-sim manually from a terminal 
or command prompt which will allow users to see mars-sim's 
internal logging statements while running mars-sim.

- Go to the directory containing the jar file and type :

	java -jar mars-sim-main-[version/build].jar 

Replacing the [version/build] with jar file's current version/build #.

---------------------------------------------------------------------

** Memory Allocation **

- The maximum memory allocation for mars-sim is adjustable prior to the 
start of the simulation. 

- By default, mars-sim uses up to 1 GB maximum memory.

- If your machine has less than 1 GB or more than 1 GB, you may 
customize mars-sim to run at one of the configuration by adding a 
numeral argument after the jar file as shown below :

  java -jar mars-sim-main-[version/build].jar 4

// no numerical --> 256 Min, 1024MB Max (by default)
// 0 --> 256 Min, 1024MB Max

// 1 --> 256 Min, 512MB Max
// 2 --> 256 Min, 768MB Max
// 3 --> 256 Min, 1024MB Max
// 4 --> 256 Min, 1536MB Max
// 5 --> 256 Min, 2048MB Max


---------------------------------------------------------------------

** Headless Mode **

- mars-sim is designed with a level of A.I. capable of running the 
entire simulation on its own. 

- If you want it to run in headless mode for hours without 
your input and/or want to save precious CPU resources from 
creating the GUI, type the arguments as follows :

  java -jar mars-sim-main-[version/build].jar 2 headless new

		OR 

  java -jar mars-sim-main-[version/build].jar 2 headless

Note 1: '2' or a numeral argument is for setting up mars-sim to use a
 memory configuration different from the default. It is optional.

Note 2: in this case, the 'new' argument is optional. If 'new' is not 
provided for , it will be added automatically.

Note 3: by default, the simulation will be saved automatically
(as default.sim in the \.mars-sim\saved\ directory) once every 
15 minutes, as dictated by the following attribute value in 
simulation.xml :

<autosave-interval value="15.0" /> 


---------------------------------------------------------------------

** Load Saved Simulation **

1. If you want to load a previously saved simulation in GUI mode, 
type and choose your saved sim file:

 java -jar mars-sim-main-[version/build no].jar 3 load


2. For loading a particular saved sim in headless mode, type the 
arguments in the following order :

  java -jar mars-sim-main-[version/build].jar 3 headless load 123.sim

Note: 'headless' must be in front of 'load' and the user sim file 
must be right after 'load'.

3. For loading the default saved sim (namely default.sim), type :

  java -jar mars-sim-main-[version/build].jar 3 headless load 

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
source/mars-sim-ui/src/main/resources/docs/help/about.html in a web 
browser.

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