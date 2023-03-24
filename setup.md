# Mars Simulation Project

Copyright (C) 2022 Scott Davis
https://mars-sim.github.io
https://github.com/mars-sim/mars-sim

---------------------------------------------------------------------

## Minimum Requirements 

- Dual Core Pentium/Celeron 1.5GHz or higher

- 1.5 GB free RAM

- 220 MB free disk space

- Java 17 or openjdk 17

---------------------------------------------------------------------

## Installation

- Install the latest Java JRE or JDK on your machine. 
  See `Prerequisites` at [README.md](https://github.com/mars-sim/mars-sim/blob/master/README.md) 

- Download a binary edition of your choice.

---------------------------------------------------------------------

## Starting a new sim

mars-sim may come in under a few flavors as follows :

A. Swing Edition

- Double-click on `[$VERSION]_swing_java17.jar` to begin
a new simulation in GUI mode. The jar file is executable
in most operating systems.

- Choose 'New Sim' to start a new simulation in the console menu.

Alternatively, players may start mars-sim from a terminal / command line.

- Go to the directory containing the jar file and type :

> java -jar [$VERSION]_swing_java17.jar

	OR

> java -jar [$VERSION]_swing_java17.jar new

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

B. Headless Edition

mars-sim is designed with a high level of autonomy. It's capable
of running the entire simulation on its own. Therefore, one may 
prefer to run it in a terminal for hours/days without GUI and in the 
least intrusive manner utilizing minimal CPU resources. Type :

> java -jar [$VERSION]_headless_java17.jar

	OR

> java -jar [$VERSION]_headless_java17.jar new

Note a: the 'new' argument is optional.

Note b: by default, the simulation will be saved automatically
(as default.sim in the \.mars-sim\saved\ directory) once every
15 minutes, as dictated by the following attribute value in
simulation.xml :

	<autosave-interval value="15.0" />

- One may alter the default time ratio for faster simulation
  by adding `512x` or `1024x` as follows when starting a new
  sim or loading from a saved sim :

> java -jar [$VERSION]_headless_java17.jar -timeratio 512

	OR

> java -jar [$VERSION]_headless_java17.jar -timeratio 1024

Note d: the time ratio argument is optional and is by default
        `256` as defined in Simulations.xml.
 
        
C. Debian Edition (either Swing GUI or Headless) 

- If you have installed the debian version of mars-sim, type 
`./mars-sim` to begin.        
 
    
D. Batch Edition

In Windows OS, double click on "mars-sim.bat" to begin.
In Linux/MacOS, type "./mars-sim" to begin.

Note 1: player may have to use a text editor to edit the version 
or build tag within mars-sim.bat so as to match up with the name
of the jarfile.

        
---------------------------------------------------------------------

## Command-Line Arguments Summary

> java -jar [$VERSION]_{$EDITION]_java17.jar
>                    (Note : start a new sim)
>   or
>
> java -jar jarfile [args...]
>                   (Note : start mars-sim with arguments)
>
> usage: [for mars-sim edition]
>  -datadir <path to data directory>   Path to the data directory for
>                                      simulation files (defaults to
>                                      user.home)
>  -help                               Help of the options
>  -lat <latitude>                     Set the latitude of the new template
>                                      Settlement
>  -load <path to simulation file>     Load the a previously saved sim,
>                                      default is used if none specifed
>  -lon <longitude>                    Set the longitude of the new template
>                                      Settlement
>  -new                                Create a new simulation if one is not
>                                      present
>  -noaudio                            Disable the audio
>  -nogui                              Disable the main UI
>  -sponsor <sponsor>                  Set the sponsor for the settlement
>                                      template
>  -template <template>                New simulation from a template
>  -timeratio <Ratio (power of 2)>     Define the time ratio of the
>                                      simulation

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
// no numerical --> 256MB Min, 1536MB Max (by default)
// -1   --> 1024MB Max
// -1.5 --> 1536MB Max
// -2   --> 2048MB Max
// -2.5 --> 2560MB Max 
// -3   --> 3072MB Max


---------------------------------------------------------------------

## Loading Saved Simulation

A. GUI Mode

If you want to load a previously saved simulation in graphic mode,

- Start the mars-sim

- Choose `Load Sim` in the Main Menu

- Select the `default.sim` or a *.sim in the FileChooser


Alternatively, you may type in a command line :

> java -jar mars-sim-[$VERSION].jar -load

This will load the FileChooser and open up 
`[$HOME]\.mars-sim\saved\` in a window panel.

Note 1: the order of the arguments is NOT important

Note 2: by default, it is assumed the user is interested in
loading the default saved sim file. You do NOT need to type 
`default.sim` as an argument. `default.sim` is located 
at `[$HOME]\.mars-sim\saved\`.

  OR

> java -jar mars-sim-[$VERSION].jar -load 123.sim

This will load `123.sim`, instead of the default saved sim.

Note 3: the order of the arguments is NOT important.

Note 4: `123.sim` must be present in the same directory
where the jar file is at.


B. Headless Mode

To load the default saved sim without the graphic interface, 
type in :

> java -jar mars-sim-[$VERSION].jar -nogui -load

Note 1: the order of the arguments is not important.

Note 2: by default, it is assumed the user is interested in
loading the default saved sim file. You do NOT need to type 
`default.sim` as an argument. `default.sim` is located 
at `[$HOME]\.mars-sim\saved\`.

  OR

> java -jar mars-sim-[$VERSION].jar -nogui -load 123.sim

This will load `123.sim`, instead of the default saved sim.

Note 3: the order of the arguments is NOT important.

Note 4: `123.sim` must be present in the same directory
where the jar file is at.


---------------------------------------------------------------------

## Questions

- For general questions regarding mars-sim, discuss them with our 
[Facebook](https://www.facebook.com/groups/125541663548/) community.

- For technical questions/comments, open a post in GitHub at 
[Discussions](https://github.com/mars-sim/mars-sim/discussions) tab.

- For issues or bugs, submit a ticket at GitHub 
[Issues](https://github.com/mars-sim/mars-sim/issues) tab.

- If you'd like to join in development, post what you would like to 
contribute in [GitHub](https://github.com/mars-sim/mars-sim/issues). 
Another way to contact us is via our developer 
[mailing list](mars-sim-developers@lists.sourceforge.net). 

---------------------------------------------------------------------

## Credits

- A full list of contributors is available from the Help menu within 
the application. The credits can also be viewed by opening the file 
/docs/help/about.html in mars-sim's built-in help browser.

---------------------------------------------------------------------

## License

- This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by 
the Free Software Foundation; either version 3 of the License, or (at 
your option) any later version..
- This program is distributed in the hope that it will be useful, but 
WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the General 
Public License 3 for more details.

- You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 