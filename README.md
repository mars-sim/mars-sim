[![Release version](https://img.shields.io/github/v/release/mars-sim/mars-sim?sort=semver&color=blue&label=release&style=flat-square)](https://github.com/mars-sim/mars-sim/releases/latest)
[![Repo Size](https://img.shields.io/github/repo-size/mars-sim/mars-sim?style=flat-square)](https://github.com/mars-sim/mars-sim/releases/latest)
[![Commits Since](https://img.shields.io/github/commits-since/mars-sim/mars-sim/3.4.0?sort=semver)](https://github.com/mars-sim/mars-sim/commits)
[![Commits Since](https://img.shields.io/github/commits-since/mars-sim/mars-sim/3.3.0?sort=semver)](https://github.com/mars-sim/mars-sim/commits)
[![Last Commit](https://img.shields.io/github/last-commit/mars-sim/mars-sim?style=flat-square)](https://github.com/mars-sim/mars-sim/commits)
[![GitHub Downloads](https://img.shields.io/github/downloads/mars-sim/mars-sim/total?label=gitHub%20downloads&style=flat-square&color=blue)](https://github.com/mars-sim/mars-sim/releases)

[![Gitter](https://badges.gitter.im/mokun/mars-sim.svg)](https://gitter.im/mokun/mars-sim?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.github.mars-sim%3Amars-sim&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=com.github.mars-sim%3Amars-sim)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mars-sim/mars-sim.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mars-sim/mars-sim/alerts/)
[![License](https://img.shields.io/badge/license-GPL%203.0-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![Language](http://img.shields.io/badge/language-java-brightgreen.svg)](https://www.java.com/)
[![SF Monthly Download](https://img.shields.io/sourceforge/dm/mars-sim.svg?label=sf%20download&style=flat-square)](https://sourceforge.net/projects/mars-sim/files/mars-sim/)



# mars-sim
*The Mars Simulation Project* is a Java-based [open source](https://opensource.dev/) project that
simulates daily activities of the first generation of settlers on Mars with a higher fidelity of
modeling and simulation details than a typical PC strategy game.

---

## Simulation
mars-sim is designed to be a **general purpose** simulator depicting the early development of human settlements on Mars.

According to *Encyclopædia Britannica*, a computer simulation is the use of a computer to represent
the dynamic responses of one system by the behavior of another system modeled after it. In essence,
a simulation is a mathematical description, or model, of a real system in the form of a computer program.

mars-sim aims to integrate and incorporate as many research disciplines (such as physics, chemistry,
biology, economics, psychology, and social science) as possible to simulate the dynamics and behaviors
of people, social structure, physical and mechanical systems, and environment in the context of
developing human settlements on Mars.

mars-sim models each settler as an intelligent agent which possess varying degrees of autonomy and mobility.
It is a symbolic model of reality, given a capacity to learn from experiences and an ability to
cooperate with other agents and systems. A settler has prescribed attributes and skills and makes
weighted decisions and interacts with one another to produce unexpected results in a sandbox world.

For instance, each settler is assigned with a job, a role, having technical
[skills](https://github.com/mars-sim/mars-sim/wiki/Skills), [personality traits](https://github.com/mars-sim/mars-sim/wiki/Personality), natural
[attributes](https://github.com/mars-sim/mars-sim/wiki/Attributes), and preferences of tasks.
They build [relationship](https://github.com/mars-sim/mars-sim/wiki/Relationship)
as they interact and work with one another. They are there to live, dream, explore, and settle Mars.

---

## Operation Modeling

mars-sim depicts near-term human exploration and settlement on Mars. It speaks of a rich scientific
language selected from research journal and paper in defining operation paradigms and programming models
that are based on present-day technologies.

### Timekeeping

Without a doubt, settlers need a timekeeping standard system for tracking the passage of time. 
That's because living on Mars does require a functioning Martian calendar in which settlers may keep track 
of sols that have elapsed. At the same time, astronomers would prefer to come up with a calendar that 
is handy and intuitive in predicting the orbit of Mars around the sun. 

The difficulties arises when each day on Mars has slightly more than 24 hours and there are 669 earth days 
on one Martian years. Therefore, it is not a straight-foward exercise in converting the time and day 
between Mars and Earth in the form of a simple equation.

See [timekeeping wiki](https://github.com/mars-sim/mars-sim/wiki/Timekeeping) for further discussions on 
this topic.

### Indoor Atmospheric Modeling

While at the Earth's sea level, the atmospheric pressure is **101.35 kPa** (14.7 psi) and has 20.9% oxygen,
in mars-sim, a low pressure atmosphere of **34 kPa** (4.93 psi) is chosen for the settlement living with
the composition of oxygen at 58.8%. 

However, [EVA suit](https://github.com/mars-sim/mars-sim/wiki/EVA-Suit) or rovers (inside a vehicle) 
adopt an even lower pressurized environment of 17 kPa (2.47 psi) for more optimal use of resources 
and design specifications. In comparison, Apollo Lunar Module (LM) atmosphere of 100% oxygen at 33 kPa
(4.8 psi). The National Aeronautics and Space Administration (NASA)'s Shuttle airlock has an oxygen 
concentration of 30% at 70.3 kPa (10.2 psi). NASA's Extravehicular Mobility Units (EMU) has the 
operating pressure of 29.6 kPa (4.3 psi). The upcoming Artemis program's lunar lander will have an atmosphere 
of 342% oxygen at a pressure of 56.5 kPa (8.2 psi).

See [Atmosphere](https://github.com/mars-sim/mars-sim/wiki/Atmosphere) wiki for more design details.

In mars-sim, each habitable building has a life-support system with various 
[functions](https://github.com/mars-sim/mars-sim/wiki/Building-Function) built-in
that continuously monitor and periodically replenish oxygen, carbon dioxide, and water moisture.
These gases are produced via chemical systems such as **Sabatier Reverse Water Gas (SRWG)**, and
**Oxygen Generation System (OGS)**, etc.

Structurally speaking, a low-pressure environment reduces the need for a rigid structure that supports
various load requirements for a building. It also facilitates occupants' Extra-Vehicular Activity (EVA)
with the outside world.

### EVA Modeling 

An example of operation modeling is the sequence of steps involving the ingress and egress of airlocks.

To walk onto the surface of Mars, a settler must come through an intermediate chamber
called the *airlock* to exit the settlement. The airlock allows the passage of people between
a pressure vessel and its surroundings while minimizing the change of pressure in the vessel and loss of
air from it. 

In mars-sim, the airlock is a separate building joined to any *Hab* (which stands for cylindrical
*habitation module*) such as *Lander Hab*, or *Outpost Hub*, *Astronomy Observatory*, etc.
All rovers have vehicular airlock built-in.

To perform a team EVA, one of the members will be selected as the *airlock operator*, who will ensure that proper
procedures be followed before going out for an EVA or after coming back from an EVA.

In case of an egress operation, (1) the airlock would have to be *pressurized*. (2) The air would be heated
so that the atmospheric pressure and temperature are equalized. (3) Next, the airlock operator would unlock
and open the inner door. (4) The whole team would enter into the airlock. (5) After all have donned
EVA suits, the operator will depressurize the chamber and gases would be re-captured to match the
outside air pressure. (6) At last, he/she would unlock and open the outer door and the whole team will
exit to the outside surface of Mars.

See [Airlock wiki](https://github.com/mars-sim/mars-sim/wiki/Airlock) for details on this topic.

### Radiation Modeling 

Another example is [Radiation Modeling](https://github.com/mars-sim/mars-sim/wiki/Radiation-Exposure),
which account for how often the **Galactic Cosmic Ray (GCR)** and **Solar Energetic Particles (SEP)**
would occur during EVA. The cumulative dose is closely monitored in 3 specific exposure interval,
namely, the 30-day, the annual and the career lifetime of a settler. It would affect 3 different regions
of our body, namely, the *Blood Forming Organs (BFO)*, the *Ocular Lens*, and the *Skin*. The dose limits are
measured in *milli-Severt*.

### Economic Modeling

In terms of [economics](https://github.com/mars-sim/mars-sim/wiki/Economics) modeling, mars-sim implements the
**Value Point (VP)** system, which keeps track of the supply and demand on each good and resource.
As there is no standard currency established on Mars yet, settlers barter trades with neighboring settlements
by keeping track of the credits and deficit based on the VPs of the resources in exchange in each trading session.

### Earth Space Agencies 

A settler may come from any one of 29 major countries as listed in this [countries wiki](https://github.com/mars-sim/mars-sim/wiki/Countries).

Note that the European Space Agency (ESA) consists of 22 member nations who are funding the space development effort.

Altogether, there's also 10 possible space agencies that can be acted as a sponsor to a settlement.


### Job, Work Shift and Task Modeling

Settlers spend much of their time learning to *live off the land*. Assigning a meaningful [role](https://github.com/mars-sim/mars-sim/wiki/Role) 
with interesting [job](https://github.com/mars-sim/mars-sim/wiki/Jobs) to each settler is crucial. 
Each settler is assigned a [work shift](https://github.com/mars-sim/mars-sim/wiki/Work-Shift) and 
engages in various [tasks](https://github.com/mars-sim/mars-sim/wiki/Tasks) such as
maintenance, ensuring life support resources are plentifully supplied, growing food crops in
[greenhouses](https://github.com/mars-sim/mars-sim/wiki/Greenhouse-Operation), making secondary
[food products](https://github.com/mars-sim/mars-sim/wiki/Food-Production), and manufacturing needed parts
and equipment in workshops, all of which are vital to the health of the economy of the settlements. 

### Mission Modeling

Settlers also go out on field [Missions](https://github.com/mars-sim/mars-sim/wiki/Missions) to explore and
study the surrounding landscapes, to prospect and mine minerals, and to trade with neighboring settlements, etc.
They may even decide to migrate from one settlement to another.

### Weather Modeling

The perils of living on Mars are very real. Even though we do not have a complete surface weather model for Mars,
we do currently simulate a total of 9 outside [weather metrics](https://github.com/mars-sim/mars-sim/wiki/Weather)
in mars-sim. 

### Reliability, Maintenance and Malfunction Modeling

The perils of living on Mars are very real. There is a total of 39 types of [Malfunctions](https://github.com/mars-sim/mars-sim/wiki/Malfunctions)
that can occur at a given moment. There are 3 metrics
for tracking how reliable a part is, namely, [Reliability](https://github.com/mars-sim/mars-sim/wiki/Reliability)
percentage, Failure Rate, Mean Time Between Failure (MTBF), which are dynamically updated in light of any incidents
that occur during the simulation. Besides malfunction, workshops and machinery factories are to produce parts for
replenishing parts to be used during regular [maintenance](https://github.com/mars-sim/mars-sim/wiki/Maintenance) tasks.

---

## Settlement Development

One of the goals of mars-sim is to populate the surface of Mars with human settlements.
Each settlement has an initial sponsor to guide its development but will eventually develop
its own *[command structure](https://github.com/mars-sim/mars-sim/wiki/Role)* and
*[development objective](https://github.com/mars-sim/mars-sim/wiki/Settlement-Objective)*.

mars-sim loosely follows the *Mars Direct Mission Plan by Robert Zubrin* and has 6 basic types of
[settlement templates](https://github.com/mars-sim/mars-sim/wiki/Settlement-Templates) to choose from.
A 4-settler initial base is called a *Mars Direct Plan (MDP) Phase 1* template. An 8-settler base
is constructed under *MDP Phase 2* template. A 12-settler base is *MDP Phase 3*. A 24-settler base
is designated as *Alpha Base*. Besides, players may build a *Trading Outpost* or a *Mining Depot*
near sites with high mineral concentrations. Depending on its country or origin and/or sponsor,
each level of template may vary in the numbers and types of building it contains.

---

## Summary
Mars is a harsh world but is certainly less unforgiving than our Moon. Settlers come face-to-face with accidents,
equipment malfunctions, illnesses, injuries, and even death. Survival depends on how well they work together,
improve their survival skills and balance individual versus settlement needs.

As the settlers learn how to survive hardship and build up their settlements, players are rewarded with the
pure joy of participating in this grand social experiment of creating a new branch of human society on another
planetary surface.

---

## Getting Started

### Prerequisites

<a href="https://foojay.io/today/works-with-openjdk">
   <img align="right"
        src="https://github.com/foojayio/badges/raw/main/works_with_openjdk/Works-with-OpenJDK.png"
        width="100">
</a>

Currently, mars-sim supports Java 11. We will transition to Java 17 in near future
as JDK 17 is the latest long-term support (LTS) release.

* Requires only JRE 11 for running mars-sim
* Requires only JDK 11 (or openjdk 11) for compiling binary

### JDK and JavaFX

Beginning Java 11, the JRE/JDK package is being decoupled from the graphic
JavaFX API package.

For the open source community, the OpenJDK is also being decoupled from the OpenJFX.

Currently, mars-sim does not require JavaFX.

> Note 1 : Specifically, the official release of v3.1.0, v3.2.0, v3.3.0 and v3.4.0 of mars-sim do not
utilize JavaFX / OpenJFX. 

Therefore, it's NOT a requirement to install it for running mars-sim.

Some unofficial releases of mars-sim in the past may have required JavaFX.

However, if you want to run any other JavaFX apps, make sure you download and
configure the OpenJFX or JavaFX package on top of the JDK.

See ticket #156 to read the discussions on how to set up JavaFX to run it
under Java 11.

You may go to [Bellsoft](https://bell-sw.com/pages/downloads/) or 
[OpenLogic](https://www.openlogic.com/openjdk-downloads) to
obtain the latest JRE/JDK for your platform.

If you need JavaFX, we recommend downloading the `Full JDK` 64-bits package.

In case of Liberica, the `Full JDK` includes LibericaFX, which is based on OpenJFX, for
running other apps that requires JavaFX.

For windows platform, choose MSI version that will automatically set up the environment path.


### OS Platforms

Assuming that OpenJDK 11 is being used.

#### Linux

1. The debian edition of mars-sim comes with debian installer for quick installation. However,
you will have to install, configure and properly update the JDK or openJDK binary in your linux
 machine in order to run mars-sim. Please google to find out the most updated instructions for your distro.

2. To manage multiple versions of java with the use of a tool called `SDKMan`,
see this [DZone article](https://dzone.com/articles/how-to-install-multiple-versions-of-java-on-the-sa).

#### macOS

1.  Check if the directory of JDK is at `Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home`.
See [DZone](https://dzone.com/articles/installing-openjdk-11-on-macos) for more instructions.

2. Enter `/usr/libexec/java_home -V` to find out which version of Java you have installed.


#### Windows

1. Start a command prompt and type this `set PATH="C:\Program Files\Java\jre-11.0.17\bin";%PATH%`.

2. Alternatively, pre-set the `JAVA_HOME` and `PATH` in *Environment Variables* in Control Panel.

- a. Add `C:\Program Files\Java\jre-11.0.17\bin` to the `PATH` variable.

> Note 2 : The order of precedence inside `PATH` is crucial. The first available folder having Java
executable inside will be the one to be loaded by Windows OS.

- b. Set `JAVA_HOME` to a JRE or JDK's destination such as `C:\Program Files\Java\jdk-11.0.17\bin\` or
`C:\Program Files\Java\jre-11.0.17\bin`.

> Note 2a : The `\bin` is crucial. When running `java -jar xxxx.jar`, mars-sim will look for the
presence of the `java.exe` in Windows OS. If `\bin` is missing in the `JAVA_HOME` variable,
the Windows OS may not be able to locate the `java.exe` and may continue to go down the `PATH`
variable to look for a valid JDK folder. If it's not found, java cannot start mars-sim.

- c. Add `%JAVA_HOME%;` to `PATH`. Type "path" in a command prompt to double check
the order of precedence when it comes to searching for the JDK.

> Note 3 : The BEST approach is to enable only one Java build (such as Java 11.0.17)
inside `PATH` and remove all other folders referencing other java versions/builds.

3. Remove any path similar to `C:\ProgramData\Oracle\Java\javapath;`  in `PATH` variable. It can
interfere with the correct version of Java that you would like to use.

> Note 4 : Depending on the order of precedence in Path variable,
`C:\ProgramData\Oracle\Java\javapath` can load the undesired version of jre/jdk,
instead of the java version you prefer.

4. To test the version of Java that your machine is using, type "java -version"
in a command prompt window.

5. It's very typical for a machine to have multiple versions of Java installed.
To check if a particular Oracle version of Java is being *enabled*,
start [Java Control Panel (JCP)](https://www.java.com/en/download/help/win_controlpanel.html)
from the Control Panel as follows :

* Move your mouse to the magnifier icon (the 2nd icon from the left) on win 10 task bar.
* Type `Configure Java`.
* Hover your mouse over the `Configure Java` and click to start the `Java Control Panel`.
* Click on `Java` tab on top.
* Click on `View` button to open up another panel window.
* Click on the checkbox on the `Enable` column to enable or disable any installed versions of Java.

> Note 5 : In JCP, each row represents a version of Java. Unfortunately, this panel
only tracks the official Oracle versions. If you install any openJDK's on
your machine, JCP won't be able to recognize them.


6. To track what versions of openjdk have been installed on your machine.
Use [JDKMon](https://harmoniccode.blogspot.com/2021/04/friday-fun-lxiii-jdkmon.html).

### Remote Console Connection

To set up true headless mode in your platform, follow the steps in this
[wiki](https://github.com/mars-sim/mars-sim/wiki/Remote-Console-Connection).


### Outreach
Feel free to use our [Facebook community](https://www.facebook.com/groups/125541663548/)
to discuss relevant topics with regard to the development of mars-sim. See also
old/archived [SF discussions](https://sourceforge.net/p/mars-sim/discussion/).


### Discussions
Feel free to start a thread on a particular topic at our GitHub
[Discussion](https://github.com/mars-sim/mars-sim/discussions) page.


### Issues/Tickets
* Current : [GH Issues](https://github.com/mars-sim/mars-sim/issues)
* Past/Archived : [SF Issues](https://sourceforge.net/p/mars-sim/tickets/search/?q=status%3Awont-fix+or+status%3Aclosed)

Help us by filling in the info below when submitting an issue :

**Describe the bug**
 - A clear and concise description of what the bug is.

**Affected Area**
 - What area(s) are we dealing with ? [e.g. Construction, Mission, Resupply,
 Settlement Map, Mini-map, Saving/Loading Sim, System Exceptions in Command
 Prompt/Terminal, etc..]

**Expected behaviors**
 - A clear and concise description of what you expected to happen.

**Actual/Observed Behaviors**
 - A clear and concise description of what you have actually seen.

**Reproduction (optional)**
 - Steps to reproduce the problem

**Screenshots**
 - If applicable, add screenshots to help explain your problem.
e.g. Include the followings :
 - Person Window showing various activity tabs
 - Settlement/Vehicle Window
 - Monitor Tool's showing People/Vehicle/Mission tabs
 - Settlement Map, etc.

**Specifications  (please complete the following information):**
 - OS version : [e.g. Windows 10, macOS 10.13, Ubuntu 14.04, etc.]
 - Java version : [e.g. Oracle JDK 11.0.17, AdoptOpenJDK 11.0.17, openjfx 11 etc.]
 - mars-sim build version : [e.g. r7688, 3.4.0, pre-3.5.0, etc.]

**Additional context**
 - Add any other context about the problem here.

> Note 1 : By providing the info above from the start, you help expedite the handling of the issue you submit.

> Note 2 : if you double-click the jar file to start mars-sim and nothing shows up, it's possible that an instance of
a JVM be created but it fails to load MainScene. In Windows OS, you may hit Ctrl+ESC to bring up the Task Manager and
scroll down to see any "orphaned" instances of `Java(TM) Platform SE binary` running in the background. Be sure you
clear them off the memory by right-clicking on it and choosing `End Task`.


### How to contribute
We welcome anyone to contribute to mars-sim in terms of ideas, concepts and coding. If you would like to contribute
to coding, see this [wiki](https://github.com/mars-sim/mars-sim/wiki/Development-Environment) for developers.
Also, we will answer your questions in our [Gitter chatroom](https://gitter.im/mokun/mars-sim).


### Website
For a more detail description of this project, see our [project website](https://mars-sim.github.io/).


### Wiki
* Check out our [wikis](https://github.com/mars-sim/mars-sim/wiki) at GitHub.


### Supported Platforms
* Windows
* MacOS
* Linux

### Official Codebase
* https://github.com/mars-sim/mars-sim


### Download
Check out the most recent pre-release build in the [GitHub's Release Tab](https://github.com/mars-sim/mars-sim/releases).

Also, see the previous and official release version at
[SourceForge Repo](https://sourceforge.net/projects/mars-sim/files/mars-sim/3.4.0/).

If you like, click on the SF's button below to automatically sense the correct OS platform to download.

[![Download Mars Simulation Project](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/mars-sim/files/latest/download)


### License
This project is licensed under the terms of the GPL v3.0 license.
<!--stackedit_data:
eyJoaXN0b3J5IjpbMjAwMTcyNzM3OCwtODY5ODg0NTgwXX0=
-->
