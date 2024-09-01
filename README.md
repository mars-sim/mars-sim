[![Release version](https://img.shields.io/github/v/release/mars-sim/mars-sim?sort=semver&color=blue&label=release&style=flat-square)](https://github.com/mars-sim/mars-sim/releases/latest)
[![Repo Size](https://img.shields.io/github/repo-size/mars-sim/mars-sim?style=flat-square)](https://github.com/mars-sim/mars-sim/releases/latest)
[![Commits Since](https://img.shields.io/github/commits-since/mars-sim/mars-sim/v3.7.2?sort=semver)](https://github.com/mars-sim/mars-sim/commits/v3.7.2)
[![Commits Since](https://img.shields.io/github/commits-since/mars-sim/mars-sim/v3.7.1?sort=semver)](https://github.com/mars-sim/mars-sim/commits/v3.7.1)
[![Last Commit](https://img.shields.io/github/last-commit/mars-sim/mars-sim?style=flat-square)](https://github.com/mars-sim/mars-sim/commits)
[![GitHub Downloads](https://img.shields.io/github/downloads/mars-sim/mars-sim/total?label=gitHub%20downloads&style=flat-square&color=blue)](https://github.com/mars-sim/mars-sim/releases)

[![Gitter](https://badges.gitter.im/mokun/mars-sim.svg)](https://gitter.im/mokun/mars-sim?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.mars-sim%3Amars-sim&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=com.mars-sim%3Amars-sim)
[![License](https://img.shields.io/badge/license-GPL%203.0-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![Language](http://img.shields.io/badge/language-java-brightgreen.svg)](https://www.java.com/)
[![SF Monthly Download](https://img.shields.io/sourceforge/dm/mars-sim.svg?label=sf%20download&style=flat-square)](https://sourceforge.net/projects/mars-sim/files/mars-sim/)


# mars-sim 

## Table of Contents
- [Introduction](#introduction)  
   - [Simulation](#simulation)
   - [Mars Direct Mission Plan](#mars-direct-mission-plan) 
   - [Settlement Development](#settlement-development)
   - [Economics](#economics)
   - [Sponsors](#sponsors)   
- [Operation Modeling](#operation-modeling)
   - [Timekeeping](#timekeeping)
   - [Indoor Atmosphere](#indoor-atmosphere)
   - [EVA](#eva)
   - [Radiation](#radiation)
   - [Job](#job)
   - [Role](#role)
   - [Task](#task)
   - [Work Shift](#work-shift)
   - [Mission](#mission)
   - [Weather](#weather)
   - [Maintenance and Malfunction](#maintenance-and-malfunction)
- [Creating the Future](#creating-the-future)
- [Conclusion](#conclusion)
- [Development](#development)
   - [Set up](#set-up)
      - [Prerequisites](#prerequisites)
      - [JDK and JavaFX](#jdk-and-javafx)
      - [OS Platforms](#os-platforms)
      - [Remote Console Connection](#remote-console-connection)
   - [Discussions](#discussions)
   - [Issues and Tickets](#issues-and-tickets)
   - [Contribution](#contribution)
   - [Website](#website)
   - [Wiki](#wiki)
   - [Outreach](#outreach)
   - [Official Codebase](#official-codebase)
   - [Download](#download)
   - [License](#license)

---

# Introduction
*The Mars Simulation Project* (mars-sim) is a Java-based [open source](https://opensource.dev/) project that
simulates mission planning, operations and activities of initial settlers on Mars with high 
computer modeling details. It is designed to be a **general purpose** simulator depicting early development of 
human settlements on Mars.

## Simulation

According to *Encyclopædia Britannica*, a computer simulation is the use of a computer to represent
dynamic responses of one system by the behavior of another system modeled after it. In essence,
a simulation is a mathematical description, or model, of a real world system in the form of a 
computer program.

mars-sim aims to integrate and incorporate as many subject disciplines (such as engineering, physics, 
chemistry, biology, economics, psychology, and social science) as possible to simulate dynamics 
and behaviors of people, social structures, physical and mechanical systems, and environments in the 
context of developing human settlements on Mars.

mars-sim models each settler as an intelligent agent possessing varying degrees of autonomy and mobility.
It simulates how a conscious agent, given a capacity to learn from past experiences, may 
cooperate with others under a prescribed set of circumstances and make weighted decisions as he interacts 
with one another to produce unexpected results in an open world.

In particular, a settler would be assigned with a job, a role, having technical
[skills](https://github.com/mars-sim/mars-sim/wiki/Skills), [personality traits](https://github.com/mars-sim/mars-sim/wiki/Personality), natural
[attributes](https://github.com/mars-sim/mars-sim/wiki/Attributes), and preferences of tasks.
Given their [relationship](https://github.com/mars-sim/mars-sim/wiki/Relationship) with one another, 
they live, dream, explore, and settle down on Mars.

## Mars Direct Mission Plan 

mars-sim loosely follows the *Mars Direct Mission Plan* (by Robert Zubrin) and has crafted 7 basic
settlement templates. In general, a 4-settler initial base is called a *Mars Direct Plan (MDP) Phase 1* base.
An 8-settler base follows a *MDP Phase 2* template. A 12-settler base is *MDP Phase 3*. A 24-settler base
is designated as *Alpha Base*, whereas a 36-settler base is called *Hub Base*. Besides, players may 
build a *Trading Outpost* or a *Mining Depot* near sites with high mineral concentrations. 

Each country of origin and sponsor has unique template that house various types of buildings. 
Altogether, there is a total of 30 unique [settlement templates](https://github.com/mars-sim/mars-sim/wiki/Settlement-Templates) 
to choose from.

## Settlement Development

The selection of a landing site is determined by a variety of factors. 
The goals in mars-sim include populating Mars with human settlements, outposts, weather station, 
communication towers, and, in future, road/train network.

The key to achieve this is developing a network of viable and self-sustainable settlements. 
Initially, the sponsor would guide the development of her settlements. Eventually, each settlement
will evolve and develop her own laws and governance, going beyond its former [command structure](https://github.com/mars-sim/mars-sim/wiki/Role), 
and it previously defined mission agenda and [objective](https://github.com/mars-sim/mars-sim/wiki/Settlement-Objective).

## Economics

In terms of [economics](https://github.com/mars-sim/mars-sim/wiki/Economics) modeling, mars-sim 
implements the **Value Point (VP)** system, which keeps track of the supply and demand on each 
good and resource. As there is no standard currency established on Mars yet, settlers barter 
trades with neighboring settlements by keeping track of the credits and deficit based on the 
VPs of the exchange of resources in a Trading mission or Delivery mission.

## Sponsors

In mars-sim, we witness an incredible undertaking in that 33 [countries](https://github.com/mars-sim/mars-sim/wiki/Countries) 
from Earth are participating in this dream of making Mars as the second home for humanity. 

Altogether, there's a total of 2 blocs, 12 space agencies, 1 organization, and 2 corporations 
to choose from when funding a settlement. 

European Space Agency (ESA) is a bloc that consists of 22 member nations united in shaping 
the development of Europe's space capability. 

Meanwhile, International Space Research Alliance (ISRA) comprises China, Russia, Taiwan and United Arab Emirates.

Notably, the Mars Society is an international organization that has chapters in all 33 member nations. 

Two corporations, namely, SpaceX and Blue Origin, have joined in to build settlements on Mars.

> [!NOTE]
> See [Sponsorship](https://github.com/mars-sim/mars-sim/wiki/Sponsorship) for further discussions on 
this topic.

---

# Operation Modeling

mars-sim depicts near-term human exploration and settlement on Mars. It speaks of a rich 
scientific language selected from research journal and paper in defining operation paradigms 
and programming models conceived from known present-day technological progress.

## Timekeeping

Without a doubt, settlers need a timekeeping standard system for tracking the passage of time. 
That's because living on Mars does require a functioning Martian calendar in which settlers may keep track 
of days (or sols) that have elapsed. At the same time, astronomers would prefer to come up with a calendar that 
is handy and intuitive in predicting the orbit of Mars around the sun. 

The difficulties arises when each sol on Mars has slightly more than 24 earth hours and there are 669 earth days 
on one Martian orbit (or year). Therefore, it is not a straight-foward exercise in converting the time and day 
between Mars and Earth by merely a simple equation.

> [!NOTE]
> See [Timekeeping](https://github.com/mars-sim/mars-sim/wiki/Timekeeping) for further discussions on 
this topic.

## Indoor Atmosphere

While at the Earth's sea level, the atmospheric pressure is **101.35 kPa** (14.7 psi) and has 20.9% oxygen,
in mars-sim, a low pressure atmosphere of **34 kPa** (4.93 psi) is chosen for the settlement living with
the composition of oxygen at 58.8%. 

However, [EVA suit](https://github.com/mars-sim/mars-sim/wiki/EVA-Suit) or inside rovers could
adopt an even lower pressurized environment of 17 kPa (2.47 psi) for more optimal use of resources 
and design specifications. In comparison, Apollo Lunar Module (LM) atmosphere of 100% oxygen at 33 kPa
(4.8 psi). The National Aeronautics and Space Administration (NASA)'s Shuttle airlock has an oxygen 
concentration of 30% at 70.3 kPa (10.2 psi). NASA's Extravehicular Mobility Units (EMU) has the 
operating pressure of 29.6 kPa (4.3 psi). The upcoming Artemis program's lunar lander will have an atmosphere 
of 342% oxygen at a pressure of 56.5 kPa (8.2 psi).

> [!NOTE]
> See [Atmosphere](https://github.com/mars-sim/mars-sim/wiki/Atmosphere) wiki for more design details.

In mars-sim, each habitable building has a life-support system with various 
[functions](https://github.com/mars-sim/mars-sim/wiki/Building-Function) built-in
that continuously monitor and periodically replenish oxygen, carbon dioxide, and water moisture.
These gases are produced via chemical systems such as **Sabatier Reverse Water Gas (SRWG)**, and
**Oxygen Generation System (OGS)**, etc.

Structurally speaking, a low-pressure environment reduces the need for a rigid structure that supports
various load requirements for a building. It also facilitates occupants' Extra-Vehicular Activity (EVA)
with outside world.

## EVA

In order to walk onto the surface of Mars, a settler must come through an intermediate 
chamber called the *airlock* to exit the settlement. The airlock allows the passage 
of people between a pressure vessel and its surroundings while minimizing the change of 
pressure in the vessel and loss of air from it. 

mars-sim attempts to do an in-depth operation modeling of the sequence of steps 
involving the ingress and egress of airlocks. There are two types of airlocks, a building 
airlock and a vehicle airlock. A building airlcok may join to any *Hab* building 
(which stands for cylindrical *habitation module*) such as *Lander Hab*, or *Outpost Hub*, 
*Astronomy Observatory*, etc. All rovers have a vehicular airlock built-in.

To perform a team EVA, one of the members will be selected as the *airlock operator*, who 
will ensure that proper procedures be followed before going out for an EVA or after 
coming back from an EVA.

In case of an egress operation, 
(1) The airlock would have to be *pressurized*. 
(2) The air would be heated.
so that the atmospheric pressure and temperature are equalized. 
(3) Next, the airlock operator would unlock and open the inner door. 
(4) The whole team would enter into the airlock. 
(5) After all have donned EVA suits, the operator will depressurize the chamber and gases 
would be re-captured to match the outside air pressure. 
(6) At last, he/she would unlock and open the outer door and the whole team will exit 
to the outside surface of Mars.

> [!NOTE]
> See [Airlock](https://github.com/mars-sim/mars-sim/wiki/Airlock) for details on this topic.

## Radiation

Another example is [Radiation Modeling](https://github.com/mars-sim/mars-sim/wiki/Radiation-Exposure),
which account for how often the **Galactic Cosmic Ray (GCR)** and 
**Solar Energetic Particles (SEP)** would occur during EVA. The cumulative dose is 
closely monitored in 3 specific exposure interval, namely, the 30-day, the annual 
and the career lifetime of a settler. It would affect 3 different regions of our body, 
namely, the *Blood Forming Organs (BFO)*, the *Ocular Lens*, and the *Skin*. The dose 
limits are measured in *milli-Severt*.

## Job

Each settler is initially assigned a meaningful [job](https://github.com/mars-sim/mars-sim/wiki/Jobs) 
that fit one's attributes and career profile. Player may also designate the job of 
a predefined settler in the crew xml file.

## Role

Each settlement has a command structure that brings each settler a [role](https://github.com/mars-sim/mars-sim/wiki/Role) 
to play. 

## Task

Settlers spend much of their time learning to *live off the land* and engage in various 
[tasks](https://github.com/mars-sim/mars-sim/wiki/Tasks) such as maintenance, 
ensuring life support resources are plentifully supplied, growing food crops in
[greenhouses](https://github.com/mars-sim/mars-sim/wiki/Greenhouse-Operation), making secondary
[food products](https://github.com/mars-sim/mars-sim/wiki/Food-Production), and manufacturing needed parts
and equipment in workshops, all of which are vital to the health of the economy of the settlements. 

## Work Shift

Each settler is assigned a [work shift](https://github.com/mars-sim/mars-sim/wiki/Work-Shift) during each sol.
The duration of a work shift may be one third of a sol or a quarter of a sol.

## Mission

Settlers also go out on field [Missions](https://github.com/mars-sim/mars-sim/wiki/Missions) to explore and
study the surrounding landscapes, to prospect and mine minerals, and to trade with neighboring settlements, etc.
They may even decide to migrate from one settlement to another.

## Weather

The perils of living on Mars are very real. Even though we do not have a complete surface weather model for Mars,
we do currently simulate a total of 9 outside [weather metrics](https://github.com/mars-sim/mars-sim/wiki/Weather)
in mars-sim. 

## Maintenance and Malfunction

The perils of living on Mars are very real. There is a total of 39 types of 
[Malfunctions](https://github.com/mars-sim/mars-sim/wiki/Malfunctions) 
that can occur at a given moment. 

There are 3 metrics for tracking how reliable a Part is. The 
[Reliability](https://github.com/mars-sim/mars-sim/wiki/Reliability)
is shown in terms of Percentage, Failure Rate, Mean Time Between Failure 
(MTBF), which are dynamically updated in light of any incidents that occur 
during the simulation. Besides malfunction, workshops and machinery factories 
are to produce parts for replenishing parts to be used during regular 
[maintenance](https://github.com/mars-sim/mars-sim/wiki/Maintenance) tasks.

## Creating the Future

History is shaped by pioneers. The exploration of Mars started in the 1960s with sending robotic 
spacecraft to orbit the planet. The apex of this spirit of exploration will be culminated by the 
first human landing in near future. Early explorers of Mars will come face-to-face with 
insurmountable challenges. 

In the first decade immediately after the initial human landing, the major theme would be 
the exploration of Mars. Initially, human explorers would not explore great distances away 
from their habitats until rover technologies for long excursions are proven to be reliable 
enough. They would travel within a region called an Exploration Zone (EZ), say, within 
100 km of their landing. On top of that, mission planner would designate many interesting
Region of Interests (ROIs) for both scientific investigations and resource extractions. 

In the second decade, if the human aspiration to become multi-planetary remains unwavering, 
we must fully engage the following four major areas : 

(1) Risk
How will we as a society perceive success and failure in the human exploration of the planet Mars ? 
Does a spiral engineering development approach for Mars exploration reduce the program risk ?
How do we analyze and reduce risk in maturing hardware and software responsible for 
exploring Mars ? What is the likelihood that it would increase or decrease the success 
and failure in the human exploration of Mars ? 

(2) Collaboration
What kind of partnership and alliances between governmental entities and corporations should 
be established in the next phase of the exploration of Mars ? 

(3) Cost
How do we contain the expense of developing flight proven hardware and software for Mars mission ?
What kind of public support would be needed to continue exploration missions ? How will the mission
planners pay off the capital and mission costs in the long run ? 

(4) Sustainment
What effort does it take to make the construction of long-term, sustainable settlements possible ? 
How do we make humans more adaptable to Mars so that it may one day become the second home for humanity ? 

## Conclusion

Mars is a harsh world but is certainly not as unforgiving than Earth's barren Moon. 
Still, settlers come face-to-face with accidents, equipment malfunctions, illnesses, 
injuries, and even death. Survival depends on how well they work together as they 
hone their survival skills and balance individual versus settlement needs.

Players will get a taste of what it's like to be settlers who survive hardship and 
build up their settlements. There's is no greater reward than knowing that the
engagment in this grand human experiment of the century will pay off in the long run
in creating a new branch of human society on another planetary surface. 

---

# Development

## Set Up

Below is a summary of how player may set up one's machine to evaluate and develop mars-sim

### Prerequisites

Currently, mars-sim supports Java 17 and is evaluating the feasibility of supporting Java 21 
(the latest long-term support (LTS) release) in near future.

* Requires only JRE 17 for running mars-sim
* Requires only JDK 17 (or OpenJDK 17) for compiling binary

### JDK and JavaFX

Beginning Java 11, the JRE/JDK package is being decoupled from the graphic
JavaFX API package.

For the open source community, the OpenJDK is also being decoupled from the OpenJFX.

Currently, mars-sim does not require JavaFX.

> [!IMPORTANT]
> Specifically, the official release of mars-sim (from v3.1.0 up to now) do not
utilize JavaFX / OpenJFX. 

Therefore, it's NOT a requirement to install it for running mars-sim.

Some unofficial releases of mars-sim in the past may have required JavaFX.

However, if you want to run any other JavaFX apps, make sure you download and
configure the OpenJFX or JavaFX package on top of the JDK. See ticket #156 on how 
to set up JavaFX to run it under Java 11.

Obtain the latest JRE/JDK for your platform. 

Check out [Java Version Almanac](https://javaalmanac.io/jdk/17/) for a quick view of the 
new features of Java 17 as well as most (if not all) of the OpenJDK out there.

See some of the popular OpenJDK packages out there in the following :

* [Amazon Cornetto](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)

* [Microsoft](https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-17)

* [Liberica](https://bell-sw.com/pages/downloads/#jdk-17-lts)

* [OpenLogic](https://www.openlogic.com/openjdk-downloads)

If you need JavaFX, we recommend downloading the `Full JDK` 64-bits package.

> [!NOTE]
> In case of Liberica, the `Full JDK` includes LibericaFX, which is based on OpenJFX, for
running other apps that requires JavaFX.


### OS Platforms

mars-sim jar binary currently works on Windows, Linux or macOS based systems. 

Below are the peculiaries of setting up Java in each OS platform.

#### Linux

1. There are shell script command in the bin directory for the console & swing versions.

2. To manage multiple versions of java with the use of a tool called `SDKMan`,
see this [DZone article](https://dzone.com/articles/how-to-install-multiple-versions-of-java-on-the-sa).

#### macOS

1.  Check if the directory of JDK is at `Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home`.
See [DZone](https://dzone.com/articles/installing-openjdk-11-on-macos) for more instructions.

2. Enter `/usr/libexec/java_home -V` to find out which version of Java you have installed.

#### Windows

Choose MSI version that will automatically set up the environment path correctly.

However, there are cases that the `path` variable and `JAVE_HOME` variable are not being configured properly.

See this [page](https://www.baeldung.com/java-home-vs-path-env-var) for explanation.

In the command prompt, try `java -version` to see what version of Java is first being found in your specific cases. 

Follow the steps below : 

1. Locate the folder with Java installation. For instance, "C:\Program Files\Java\jdk-17" may be your JDK's location.

2. Under System variable, ensure `JAVA_HOME` has been added and set up correct as follows:

> set JAVA_HOME=C:\Program Files\Java\jdk-17

2a. Alternatively, one may start a command prompt and type this `set JAVA_HOME="C:\Program Files\Java\jdk-17"` 

3. Under both User and the System variable, set the `PATH` variable to include the JDK folder. For instance,

> set PATH=C:\Program Files\Java\jdk-17\bin

or 

> set PATH="%JAVA_HOME%\bin";%PATH%

> [!IMPORTANT]
> The order of precedence inside `PATH` variable is crucial. The first available folder having Java
executable inside will be the one to be loaded by Windows OS.

> [!NOTE]
> The `\bin` may be crucial. When running `java -jar xxxx.jar`, mars-sim will look for the
presence of the `java.exe` in Windows OS. If `\bin` is missing in the `JAVA_HOME` variable,
the Windows OS may not be able to locate the `java.exe` executable and may continue to go 
down the `PATH` variable to look for a valid JDK folder. If java executable is not found, 
mars-sim cannot be started.

> [!NOTE]
> The BEST approach is to enable only one Java build (such as Java 17.0.8)
inside `PATH` variable and remove all other folders referencing other java versions/builds.

4. Remove any path similar to `C:\ProgramData\Oracle\Java\javapath;` in `PATH` variable. It can
interfere with the correct version of Java that you would like to use.

> [!WARNING]
> If the designated folder `C:\ProgramData\Oracle\Java\javapath` is before other folders 
in `Path` variable, then Windows will first look for a jre/jdk in this folder. Most of the time,
a shortcut that will point to a Java executable in another folder is in this folder.

5. To test the version of Java that your machine is using, type "java -version"
in a command prompt window.

6. It's possible for a machine to have multiple versions of Java installed.
To check if a particular Oracle version of Java is being *enabled*,
start [Java Control Panel (JCP)](https://www.java.com/en/download/help/win_controlpanel.html)
from the Control Panel as follows :

* Move your mouse to the magnifier icon (the 2nd icon from the left) on win 10 task bar.
* Type `Configure Java`.
* Hover your mouse over the `Configure Java` and click to start the `Java Control Panel`.
* Click on `Java` tab on top.
* Click on `View` button to open up another panel window.
* Click on the checkbox on the `Enable` column to enable or disable any installed versions of Java.

> [!NOTE]
> In JCP, each row represents a version of Java. Unfortunately, this panel
only tracks the official Oracle versions. If you install any openJDK's on
your machine, JCP won't be able to recognize them.

7. To track what versions of OpenJDK have been installed on your machine, you may try using 
[JDKMon](https://harmoniccode.blogspot.com/2021/04/friday-fun-lxiii-jdkmon.html).

## Remote Console Connection
To set up true headless mode in your platform, follow the steps in this
[wiki](https://github.com/mars-sim/mars-sim/wiki/Remote-Console-Connection).

## Outreach
Feel free to use our [Facebook community](https://www.facebook.com/groups/125541663548/)
to discuss relevant topics with regard to the development of mars-sim. See also
old/archived [SF discussions](https://sourceforge.net/p/mars-sim/discussion/).

## Discussions
Feel free to start a thread on a particular topic at our GitHub
[Discussion](https://github.com/mars-sim/mars-sim/discussions) page.

## Issues and Tickets
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

**Specifications (please complete)**
 - OS version : [e.g. Windows 11, macOS 14.2, Ubuntu 23.10]
 - Java version : [e.g. Oracle JDK 17.0.8, AdoptOpenJDK 17.0.8, OpenJFX 17]
 - Major version and build : [e.g. 3.7.1 build 9227]

**Additional context**
 - Add any other context about the problem here. By providing more info above when filing it, 
   you help expedite the handling of the issues you submit.

> [!NOTE]
> If you double-click the jar file in an attempt to start mars-sim but nothing shows up, 
it's possible that an old instance of JVM has already been running in the background. 
To see if it's indeed the case, in Windows OS, you may hit <kbd>CTRL</kbd>+<kbd>ESC</kbd> 
to bring up the Task Manager and scroll down to find any *orphaned* instances of 
`Java(TM) Platform SE binary` running in the background. Be sure you first clear them off 
the memory by right-clicking on each of them and choosing `End Task`.


## Contribution
We welcome anyone to contribute to mars-sim in terms of ideas, concepts and coding. 
If you would like to contribute to coding, see this 
[wiki](https://github.com/mars-sim/mars-sim/wiki/Development-Environment) 
for developers. Also, we will answer your questions in our 
[Gitter chatroom](https://gitter.im/mokun/mars-sim).


## Website
For a more detail description of this project, see our 
[project website](https://mars-sim.github.io/) or go to
our [domain](https://www.mars-sim.com/) directly.


## Wiki
Check out our [wikis](https://github.com/mars-sim/mars-sim/wiki) at GitHub.

## Official Codebase
* https://github.com/mars-sim/mars-sim


## Download
Check out the most recent release or pre-release build in GitHub 
[Releases](https://github.com/mars-sim/mars-sim/releases) page.

Alternatively, see all our previous and current official release versions at SourceForge
[Repo](https://sourceforge.net/projects/mars-sim/files/mars-sim/3.6.2/).

> [!NOTE]
> If you prefer, click SF's button below to automatically sense the correct OS platform to download.

[![Download Mars Simulation Project](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/mars-sim/files/latest/download)


## License
This project is licensed under the terms of the GPL v3.0 license.
