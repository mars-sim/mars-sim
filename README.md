[![Download Mars Simulation Project](https://img.shields.io/sourceforge/dm/mars-sim.svg)](https://sourceforge.net/projects/mars-sim/files/latest/download)
[![Gitter](https://badges.gitter.im/mokun/mars-sim.svg)](https://gitter.im/mokun/mars-sim?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/dee6a80651fe420b85adf22c4ca79574)](https://www.codacy.com/app/mokun/mars-sim?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mars-sim/mars-sim&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.org/mars-sim/mars-sim.svg?branch=master)](https://travis-ci.org/mars-sim/mars-sim)
[![codecov](https://codecov.io/gh/mars-sim/mars-sim/branch/master/graph/badge.svg)](https://codecov.io/gh/mars-sim/mars-sim)
[![License](https://img.shields.io/badge/license-GPL%203.0-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![Language](http://img.shields.io/badge/language-java-brightgreen.svg)](https://www.java.com/)


# mars-sim
The Mars Simulation Project is a Java based open source project that simulates the activities of the first generation of settlers on Mars with a higher fidelity of simulation than a typical PC strategy game. 


## Simulation
mars-sim is a "general purpose" simulator depicting the early development of the human settlements on Mars. According to Encyclopædia Britannica, Computer simulation is the use of a computer to represent the dynamic responses of one system by the behavior of another system modeled after it. A simulation uses a mathematical description, or model, of a real system in the form of a computer program. 

mars-sim attempts to incorporate Mathematical modeling of many systems or scientific disciplines (such as in physics, chemistry, biology, economics, psychology, and social science) and simulate the behaviors of these systems under the Object-Oriented programming paradigm in Java. 

As each settler in mars-sim is a computer agent possessing certain properties, based on these properties and the external characteristics imposed on them, they make weight decisions and interact with one another to produce unexpected results in a sandbox world.

For instance, each settler has a well-defined job, a role, a bunch of technical skills, a set of personality, natural attributes, and task preferences (likes and dislikes). They build relationship as they interact and work with one another. They are there to live, dream, explore, and settle Mars. 


## Operation Modeling
mars-sim adopts a rich set of scientific vocabulary from research journal and paper when it comes to defining operation paradigms and programming models  based on the present day technologies that are applicable for the near-term human exploration and settlement on Mars.

For instance, a low pressure atmosphere of 34 kPa (5 psi) is chosen for humans to live in each martian settlement, as opposed to the Earth's 
sea level atmosphere of 101 kPa (14.7 psi). The oxygen (as well as other traces of gas and water moisture) in the air is continuously monitored 
and replenished periodically for each building. These gases are generated via various systems such as Sabatier Reverse Water Gas (SRWG), Oxygen Generation System and through various other processes.

Another example is the modes of operation involved in EVA. Prior to each EVA activity, one of the settlers will be selected as the airlock operator. The airlock would have to be pressurized and depressurized (gases captured and released and reheated) with inner/outer doors opened and closed at a specific sequence in order to allow the ingress/egress of the settlers onto the surface of Mars.

The third example is the radiation modeling and how often the Galactic Cosmic Ray (GCR) and Solar Energetic Particles (SEP) would occur during EVA as the dose is closely monitored and tracked presumably over the career lifetime of a settler. 
 
## Settlement Development 
Player may create numerous settlements spreading across the surface of Mars. Each settlement has a command structure and a developmental objective. They can be as simple as a four-person initial base (loosely following the Mars Direct Mission Plan by Robert Zubrin), a trading outpost, a mining depot near sites with high mineral concentration; or a self-contained colony with 48+ settlers having an elected mayor. 


## Economics
In terms of economic modeling, mars-sim implements a value point (VP) system, which keeps track of the supply and demand on each resource. There is no standard currency established yet. Traders barter trades with neighboring settlements and the surplus/ deficit based on the 
VPs of the resources in exchanged would be computed in each trading session.


## Tasks and Missions
Settlers spend much of their time learning to "live off the land". They engage in maintenance, ensuring life support resources are well balanced, growing crops in greenhouses, making secondary food products, and manufacturing needed parts and equipment in workshops.

Settlers will also go out on field missions to explore and study surrounding landscapes, to prospect and mine minerals, and to trade with neighboring settlements. They may even decide to migrate from one settlment to another.


## Reliability and Malfunctions
The perils of living on Mars are very real. There are close to 30 types of malfunctions that can strike. The failure rate, the Mean Time Between Failure (MTBF) and the reliability of parts are tracked real-time and updated dynamically based on field available data during the simulation.


## Summary
Mars is a harsh world but is certainly less unforgiving than our Moon. Settlers come face-to-face with accidents, equipment malfunctions, illnesses, injuries, and even death. Survival depends on how well they work together, improve their survival skills and balance individual versus settlement needs. The reward of

As the settlers learn how to survive the hardship and build up their settlements, players are rewarded with the pure joy of participating in this grand social experiment of creating a new branch of human society on another planetary surface.


## Webpage
For a more detail description of this project, see our project website at at https://mars-sim.github.io/


## Wiki
* Check out new wiki pages at https://github.com/mars-sim/mars-sim/wiki


## Supported Platforms
* Windows
* MacOS (known bugs in displaying certain fonts of some websites in Help Browser)
* Linux (cannot input text in text fields)


## Getting Started

### Prerequisites
* Require JRE/JDK 8 (u77 or above) for binaries compiled under Java 8 
* Require JRE/JDK 9 or above for binaries compiled under Java 9
* For Windows OS, one should manually set up the following : 
  - Edit the `JAVA_HOME` and `PATH` in the System's "Environment Variables" in Control Panel 
  - Set `JAVA_HOME` to a JRE or JDK's destination such as `C:\Program Files\Java\jre1.8.0_151` or `C:\Program Files\Java\jdk1.8.0_151`
  - Add `%JAVA_HOME%;%JAVA_HOME%\bin;` to `PATH`         
  - Remove any path similar to `C:\ProgramData\Oracle\Java\javapath;`  in `PATH` variable. It interferes with the correct version of 
    Java that should be used. 
  - Check if the correct version of Java is being enable in "Java Control Panel" in Windows's Control Panel. 

> Note 1 The best approach is enabling only one Java build (such as Java 10.0.1) at a time and disable all other versions/builds.

> Note 2 : To test the version of Java that your machine is using, type "java -version" in a terminal/command prompt.

> Note 3 : Remove all Java 9 related executables inside the folder `C:\ProgramData\Oracle\Java\javapath` in order to avoid loading the undesired version of jre/jdk.


## Feedback/Comments
Feel free to provide your comments at our [facebook community]( https://www.facebook.com/groups/125541663548/). See also [old/archived discussions](https://sourceforge.net/p/mars-sim/discussion/)


## Issues/Tickets
* Current : https://github.com/mars-sim/mars-sim/issues
* Past/Archived : https://sourceforge.net/p/mars-sim/tickets/search/?q=status%3Awont-fix+or+status%3Aclosed

Help us by filling in the info below when submitting an issue

**Describe the bug**
 - A clear and concise description of what the bug is.

**Affected Area**
 - What area(s) are we dealing with ? [e.g. Construction, Mission, Resupply, Settlement Map, Mini-map, Saving/Loading Sim, System Exceptions in Command Prompt/Terminal, etc..]

**Expected behaviors**
 - A clear and concise description of what you expected to happen.

**Actual/Observed Behaviors**
 - A clear and concise description of what you have actually seen.

**Reproduction (optional)**
 - Steps to reproduce the problem

**Screenshots**
 - If applicable, add screenshots to help explain your problem.
e.g. Include the followings :
 a. Person Window showing various activity tabs 
 b. Settlement/Vehicle Window 
 c. Monitor Tool's showing People/Vehicle/Mission tabs  
 d. Settlement Map, etc.

**Specifications  (please complete the following information):**
 - OS version : [e.g. Windows 10, macOS 10.13, Ubuntu 14.04, etc.]
 - Java version : [e.g. Oracle Java 8u171, openjdk 1.8.0.171-8.b10 and openjfx 8 etc.]
 - mars-sim build version : [e.g. r4255, v3.1.0-p9_Java9, etc.]

**Additional context**
 - Add any other context about the problem here.

> Note 1 : By providing the info above from the start, you help expedite the handling of the issue you submit.

> Note 2 : if you double-click the jar file to start mars-sim and nothing shows up, it's possible that an instance of a JVM be created but it fails to load MainScene. In Windows OS, you may hit Ctrl+ESC to bring up the Task Manager and scroll down to see any "orphaned" instances of `Java(TM) Platform SE binary` running in the background. Be sure you clear them off the memory by right-clicking on it and choosing `End Task`. 

## How to contribute 
We welcome anyone to contribute to mars-sim in terms of ideas, concepts and coding. If you would like to contribute to coding, see this [wiki](https://github.com/mars-sim/mars-sim/wiki/Development-Environment) for developers. Also, we will answer your questions in our [Gitter chatroom](https://gitter.im/mokun/mars-sim). 


## Official Codebase
* https://github.com/mars-sim/mars-sim


## Download 
Check out the most recent build in the [GitHub's release tab](https://github.com/mars-sim/mars-sim/releases) or in [SourceForge repo](https://sourceforge.net/projects/mars-sim/files/mars-sim/3.1.0/)

Alternatively, you may use SourceForge's button below to automatically sense the correct platform. 
[![Download Mars Simulation Project](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/mars-sim/files/latest/download)


## License
This project is licensed under the terms of the GPL v3.0 license.
