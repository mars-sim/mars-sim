# Mars Simulation Project
Copyright (C) 2018 Scott Davis
[Project Website](https://mars-sim.github.io)
[GitHub Page](https://github.com/mars-sim/mars-sim)

---------------------------------------------------------------------

## Minimum Requirements 

- Dual Core Pentium/Celeron 1.5GHz or higher

- 1 GB free RAM

- 220 MB free disk space

- JRE/JDK 8 (u77 above) for binaries compiled under Java 8

- JRE/JDK 9/10 for binaries compiled under Java 9

---------------------------------------------------------------------

## Installation

- Install the latest Java JRE or JDK on your machine. 
  See `Prerequisites` at [README.md](https://github.com/mars-sim/mars-sim/blob/master/README.md) 

- Download a binary mars-sim package of your choice.

---------------------------------------------------------------------

## Starting a new sim

Your mars-sim package may or may not come with JavaFX components.

1. Batch Edition

- In Windows OS, double click on "mars-sim.bat" to begin.
- In Linux/MacOS, type "./mars-sim" to begin.

2. Debian Edition

- If you have installed the debian version of mars-sim, type 
`./mars-sim` to begin.

3a. Jar Edition

- Double-click on `mars-sim-[$VERSION].jar` to begin
a new simulation in GUI mode as the jar file is executable
in most operating systems.

- Choose 'New Sim' to start a new simulation in the Main Menu.

Alternatively, one may start mars-sim from the command line. 

- Go to the directory containing the jar file and type :

> java -jar mars-sim-main-[$VERSION].jar

	OR

> java -jar mars-sim-main-[$VERSION].jar new

This gives users the advantage of seeing mars-sim's internal logging
statements while running mars-sim.

Note a: replacing [$VERSION] with the current version or build.

Note b: the argument 'new' is optional. If the argument 'load'
is not provided for, it will assume that the user is interested in
starting a new simulation and 'new' will be appended automatically.

Note c: by default, the simulation will be saved automatically
with a new filename with a date/time stamp, the # of sol and
the build # once every 15 minutes. It's located in the
`\[$HOME]\.mars-sim\autosave\` directory, as dictated by the
following attribute value in simulation.xml :

	<autosave-interval value="15.0" />

3b. Headless Jar

mars-sim is designed with a high level of autonomy. It's capable
of running the entire simulation on its own. Therefore, one may 
prefer to run it in a terminal for hours/days without GUI and in the 
least intrusive manner utilizing minimal CPU resources. Type :

> java -jar mars-sim-main-[$VERSION].jar 2 headless new

	OR

> java -jar mars-sim-main-[$VERSION].jar 2 headless

Note a: the '2' numeral argument is for setting up mars-sim to use
a memory configuration (see below) different from the default.
It is optional.

Note b: the 'new' argument is optional.

Note c: by default, the simulation will be saved automatically
(as default.sim in the \.mars-sim\saved\ directory) once every
15 minutes, as dictated by the following attribute value in
simulation.xml :

	<autosave-interval value="15.0" />

- One may alter the default time ratio for faster simulation
  by adding `512x` or `1024x` as follows when starting a new
  sim or loading from a saved sim :

> java -jar mars-sim-main-[$VERSION].jar 2 headless 512x

	OR

> java -jar mars-sim-main-[$VERSION].jar 2 headless 1024x

Note d: the time ratio argument is optional and is by default
        `256x` as defined in Simulations.xml.
        
        
---------------------------------------------------------------------

## Command-Line Arguments Summary

> java -jar mars-sim-[$VERSION].jar
                    (Note : start a new sim)
   or

> java -jar jarfile [args...]
>                   (Note : start mars-sim with arguments)
>
>  where args include :
>
>    new             start a new sim (by default)
>                    (Note : Whenever arg 'load' is not provided for,
>                            'new' will be automatically appended)
>    headless        run in console mode without an user interface (UI)
>    0               256MB Min, 1024MB Max (by default)
>    1               256MB Min, 512MB Max
>    2               256MB Min, 768MB Max
>    3               256MB Min, 1024MB Max
>    4               256MB Min, 1536MB Max
>    5               256MB Min, 2048MB Max
>    load            open the File Chooser at the \.mars-sim\saved\ 
>                    and wait for user to choose a saved sim
>    load 123.sim    load the sim with filename '123.sim'
>                    (Note : '123.sim' must be located at the same 
>                            folder as the jarfile)
>    noaudio         disable background music and sound effect\n"
>    512x            set time ratio to 512x (for headless edition only)			
>    1024x           set time ratio to 1024x (for headless edition only)                     		

---------------------------------------------------------------------

## Memory Allocation

- The maximum memory allocation for mars-sim is adjustable prior to the
start of the simulation.

- By default, mars-sim uses up to 1 GB maximum memory.

- If your machine has less than 1 GB or more than 1 GB, you may
customize mars-sim to run at one of the configuration by adding a
numeral argument after the jar file as shown below :

> java -jar mars-sim-main-[$VERSION].jar 4

- Below are options :
// no numerical --> 256MB Min, 1024MB Max (by default)
// 0 --> 256MB Min, 1024MB Max
// 1 --> 256MB Min, 512MB Max
// 2 --> 256MB Min, 768MB Max
// 3 --> 256MB Min, 1024MB Max
// 4 --> 256MB Min, 1536MB Max
// 5 --> 256MB Min, 2048MB Max


---------------------------------------------------------------------

## Loading Saved Simulation

A. GUI Mode

If you want to load a previously saved simulation in graphic mode,

- Start the mars-sim

- Choose `Load Sim` in the Main Menu

- Select the `default.sim` or a *.sim in the FileChooser


Alternatively, you may type in a command line :

> java -jar mars-sim-[$VERSION].jar 3 load

This will load the FileChooser and open up 
`[$HOME]\.mars-sim\saved\` in a window panel.

Note 1: the order of the arguments is NOT important

Note 2: by default, it is assumed the user is interested in
loading the default saved sim file. You do NOT need to type 
`default.sim` as an argument. `default.sim` is located 
at `[$HOME]\.mars-sim\saved\`

  OR

> java -jar mars-sim-[$VERSION].jar 3 load 123.sim

This will load `123.sim`, instead of the default saved sim.

Note 3: the order of the arguments is NOT important

Note 4: `123.sim` must be present in the same directory
where the jar file is at


B. Headless Mode

To load the default saved sim without the graphic interface, type :

> java -jar mars-sim-[$VERSION].jar 3 headless load

Note 1: the order of the arguments is not important

Note 2: by default, it is assumed the user is interested in
loading the default saved sim file. You do NOT need to type 
`default.sim` as an argument. `default.sim` is located 
at `[$HOME]\.mars-sim\saved\`

  OR

> java -jar mars-sim-[$VERSION].jar 3 headless load 123.sim

This will load `123.sim`, instead of the default saved sim.

Note 3: the order of the arguments is NOT important

Note 4: `123.sim` must be present in the same directory
where the jar file is at


---------------------------------------------------------------------

## Questions

- For general questions regarding mars-sim, discuss them to our [facebook](https://www.facebook.com/groups/125541663548/) community

- For technical questions/comments or submitting a ticket, post it at
our GitHub [issue tracker](https://github.com/mars-sim/mars-sim/issues)

- If you'd like to join in development, post what you would liek to 
contribute in [GitHub](https://github.com/mars-sim/mars-sim/issues). 
Another way to contact us is via developer [mailing list](mars-sim-developers@lists.sourceforge.net) 

---------------------------------------------------------------------
## Credits

- A full list of contributors is available from the Help menu within nthe application. The credits can also be viewed by opening the file e/docs/help/about.html in mars-sim's built-in help browser.

---------------------------------------------------------------------

## License

- This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by ythe Free Software Foundation; either version 3 of the License, or (at tyour option) any later version..
- This program is distributed in the hope that it will be useful, buttWITHOUT ANY WARRANTY; without even the implied warranty offMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
General Public License 3 for more details.

- You should have received a copy of the GNU General Public License ealong with this program; if not, write to the Free SoftwareeFoundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307USA