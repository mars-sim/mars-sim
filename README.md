[![Download Mars Simulation Project](https://img.shields.io/sourceforge/dm/mars-sim.svg)](https://sourceforge.net/projects/mars-sim/files/latest/download)
[![Gitter](https://badges.gitter.im/mokun/mars-sim.svg)](https://gitter.im/mokun/mars-sim?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/dee6a80651fe420b85adf22c4ca79574)](https://www.codacy.com/app/mokun/mars-sim?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mars-sim/mars-sim&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.org/mars-sim/mars-sim.svg?branch=master)](https://travis-ci.org/mars-sim/mars-sim)
[![Dependency Status](https://www.versioneye.com/user/projects/9ffb7e9ead4f58524bc9/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5936417e98442b00398eb873?child=summary#dialog_dependency_badge)
[![License](https://img.shields.io/badge/license-GPL%203.0-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![Language](http://img.shields.io/badge/language-java-brightgreen.svg)](https://www.java.com/)


# mars-sim
The Mars Simulation Project is a Java based open source project that simulates the activities of the first generation of settlers on Mars with a higher fidelity of simulation than a typical PC strategy game. 


## Simulation
mars-sim is a general purpose simulation for the human settlement on Mars. While computer simulation is a particularly useful tool for the mathematical modeling of many systems or branches of science (such as in physics, climatology, chemistry, biology, economics, psychology, and social science), mars-sim attempts to simulate the behaviors of these systems and sub-systems under the Object-Oriented programming paradigm of Java. 

As each settler in mars-sim is a computer agent possessing certain properties, based on these properties and the external characteristics imposed on them, they make weight decisions and interact with one another to produce unexpected results in a sandbox world.

For instance, each settler has a well-defined job, a role, a bunch of technical skills, a set of personality, natural attributes, and task preferences (likes and dislikes). They build relationship as they interact and work with one another. They are there to live, dream, explore, and settle Mars. 


## Operational Modeling
mars-sim leverages the scientific principles found in many research journal and attempts to model and incorporate many operational paradigm based on the present day technologies that are applicable for the near-term human exploration and settlement on Mars.

For instance, a low pressure atmosphere of 34kPa or 5 psi is chosen for humans to live in each settlement on Mars, as opposed to the Earth's 
sea level atmosphere of 101 kPa (14.7 psi). The oxygen (as well as other traces of gas and water moisture) in the air is continuously monitored 
and replenished periodically for each building. These gases are generated via various systems such as Sabatier Reverse Water Gas (SRWG) and Oxygen Generation System as well as in greenhouses.

Another example is the modes of operation involved in EVA. Prior to each EVA activity, one of the settlers will be selected as the airlock operator. The airlock would have to be pressurized and depressurized (gases captured and released and reheated) with inner/outer doors opened and closed at a specific sequence in order to allow the ingress/egress of the settlers onto the surface of Mars.

The third example is radiation modeling and how often the Galactic Cosmic Ray (GCR) and Solar Energetic Particles (SEP) happens during EVA as they are monitored and tracked presumably over the career lifetime of a settler. 
 
## Settlement Development 
Player may create numerous settlements spreading across the surface of Mars. Each settlement has a command structure and a developmental objective. They can be as simple as a four-person initial base (loosely following the Mars Direct Mission Plan by Robert Zubrin), a trading outpost, a mining depot near sites with high mineral concentration; or a self-contained colony with 48+ settlers having an elected mayor. 


## Economics
In terms of economic modeling, mars-sim implements a value point (VP) system, which keeps track of the supply and demand on each resource. There is no standard currency established yet. Traders barter trades with neighboring settlements and the surplus/ deficit based on the 
VPs of the resources in exchanged would be computed in each trading session.


## Tasks and Missions
Settlers spend much of their time learning to "live off the land". They engage in maintenance, ensuring life support resources are well balanced, growing crops in greenhouses, making secondary food products, and manufacturing needed parts and equipment in workshops.

Settlers will also go out on field missions to explore and study surrounding landscapes, to prospect and mine minerals, and to trade with neighboring settlements. They may even decide to migrate from one settlment to another.


## Reliability and Malfunctions
The perils of living on Mars are very real. There are close to 30 types of malfunctions that can strike. The failure rate (such as the Mean Time Between Failure (MTBF)) and reliability of parts are tracked real-time and updated dynamically based on field available data during the simulation.


## Summary
Mars is a harsh world but is certainly less unforgiving than our Moon. Settlers come face-to-face with accidents, equipment malfunctions, illnesses, injuries, and even death. Survival depends on how well they work together, improve their survival skills and balance individual versus settlement needs. The reward of

As the settlers learn how to survive the hardship and build up their settlements, players are rewarded with the pure joy of participating in this grand social experiment of creating a new branch of human society on another planetary surface.


## Webpage
For a more detail description of this project, see our project website at at https://mars-sim.github.io/


## Feedback/Comments
We welcome anyone to contribute to mars-sim in terms of ideas and concepts. Feel free to provide your comments at our facebook community at https://www.facebook.com/groups/125541663548/. See also old/archived discussions at https://sourceforge.net/p/mars-sim/discussion/


## Issues/Tickets
* Current : https://github.com/mars-sim/mars-sim/issues
* Past/Archived : https://sourceforge.net/p/mars-sim/tickets/search/?q=status%3Awont-fix+or+status%3Aclosed


## Wiki
* Check out new wiki pages at https://github.com/mars-sim/mars-sim/wiki


## Supported Platforms
* Windows
* MacOS (known bugs in displaying certain fonts in a website in Help Browser)
* Linux (cannot input text in text fields)


## OS Environment Setup
* Requires Java 8u77 or above
* For Windows OS, may need to manually set up the following : 
  - Edit the `JAVA_HOME` and `PATH` in System's Environment Variables in Control Panel 
  - Set `JAVA_HOME` to a JRE or JDK's destination such as `C:\Program Files\Java\jre1.8.0_144` or `C:\Program Files\Java\jdk1.8.0_144`
  - Add `%JAVA_HOME%;%JAVA_HOME%\bin;` to `PATH`          


## Development Prerequisites
If you would like to contribute to the project, see our [developer wiki](https://github.com/mars-sim/mars-sim/wiki/Development-Environment). 
We will help you and answer your questions in our new [Gitter chatroom](https://gitter.im/mokun/mars-sim). 


## Official Codebase
* https://github.com/mars-sim/mars-sim


## Download 
[![Download Mars Simulation Project](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/mars-sim/files/latest/download)
* Check out the most recent build at https://sourceforge.net/projects/mars-sim/files/mars-sim/3.1.0/


## License
This project is licensed under the terms of the GPL v3.0 license.
