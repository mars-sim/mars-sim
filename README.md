[![Download Mars Simulation Project](https://img.shields.io/sourceforge/dm/mars-sim.svg)](https://sourceforge.net/projects/mars-sim/files/mars-sim/)
[![Gitter](https://badges.gitter.im/mokun/mars-sim.svg)](https://gitter.im/mokun/mars-sim?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.github.mars-sim%3Amars-sim&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=com.github.mars-sim%3Amars-sim)
[![License](https://img.shields.io/badge/license-GPL%203.0-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![Language](http://img.shields.io/badge/language-java-brightgreen.svg)](https://www.java.com/)


# mars-sim
*The Mars Simulation Project* is a Java-based [open source](https://opensource.dev/) project that 
simulates the activities of the first generation of settlers on Mars with a higher fidelity of 
modeling and simulation details than a typical PC strategy game. 

---

## Simulation
mars-sim is a **general purpose** simulator depicting the early development of human settlements on Mars. 
According to *Encyclopædia Britannica*, a computer simulation is the use of a computer to represent 
the dynamic responses of one system by the behavior of another system modeled after it. In essence, 
a simulation is a mathematical description, or model, of a real system in the form of a computer program. 

mars-sim aims to integrate and  incorporate as many research disciplines (such as physics, chemistry, 
biology, economics, psychology, and social science) as possible to simulate the dynamics and behaviors 
of people, social structure, physical and mechanical systems, and environment in the context of 
developing human settlements on Mars. 

A settler will be a computer agent possessing certain prescribed attributes, dynamic properties and 
the external characteristics imposed on it. Each one makes weighted decisions and interacts with one 
another to produce unexpected results in a sandbox world.

For instance, each settler is assigned with a job, a role, having technical 
[skills](https://github.com/mars-sim/mars-sim/wiki/Skills), [personality traits](https://github.com/mars-sim/mars-sim/wiki/Personality), natural 
[attributes](https://github.com/mars-sim/mars-sim/wiki/Attributes), and preferences of tasks. 
They build [relationship](https://github.com/mars-sim/mars-sim/wiki/Relationship) 
as they interact and work with one another. They are there to live, dream, explore, and settle Mars. 

---

## Operation Modeling
mars-sim adopts a rich set of scientific vocabulary from research journal and paper when it comes to 
defining operation paradigms and programming models based on the present day technologies that are 
applicable for the near-term human exploration and settlement on Mars.

For instance, the simulation assumes a low-pressure living environment that facilitates EVA and reduces 
structural requirements on buildings. 

While at the Earth's sea level, the atmospheric pressure is **101 kPa** (14.7 psi) and has 20.9% oxygen, 
in mars-sim, a low pressure atmosphere of **34 kPa** (4.93 psi) is chosen for the settlement living with
the composition of oxygen at 58.8%. However, inside a rover or an 
[EVA suit](https://github.com/mars-sim/mars-sim/wiki/EVA-Suit), an even lower pressurized 
environment of **17 kPa** (2.47 psi) is adopted for more optimal use of resources and design specifications. 
See [Atmosphere](https://github.com/mars-sim/mars-sim/wiki/Atmosphere) wiki.

Each building has life-support system [functions](https://github.com/mars-sim/mars-sim/wiki/Building-Function)
that continuously monitored and periodically replenished oxygen, carbon dioxide, and water moisture. 
These gases are produced via chemical systems such as **Sabatier Reverse Water Gas (SRWG)**, and 
**Oxygen Generation System (OGS)**, etc.

An example of operation modeling is the sequence of steps involving the ingress and egress of airlocks 
before and after an **Extra-Vehicular Activity (EVA)**. 

To walk onto the surface of Mars, a settler must come through an intermediate chamber 
called the *airlock* to exit the settlement. The airlock allows the passage of people between 
a pressure vessel and its surroundings while minimizing the change of pressure in the vessel and loss of 
air from it. In mars-sim, the airlock is a separate building joined to any *Hab* (which stands for cylindrical
*habitation module*) such as *Lander Hab*, or *Outpost Hub*, *Astronomy Observatory*, etc.
All rovers have vehicular airlock built-in. 

---

## Extra-Vehicular Activity (EVA)
To perform a team EVA, one of will be selected as the *airlock operator*, who will ensure that proper
procedures be followed before going out for an EVA or after coming back from an EVA.

In case of an egress operation, (1) the airlock would have to be *pressurized*. (2) The air would be heated 
so that the atmospheric pressure and temperature are equalized. (3) Then the airlcok operator would unlock 
and open the inner door. (4) The whole team would enter into the airlock. (5) After all have donned 
EVA suits, the operator will depressurize the chamber and the gases would be re-captured to match the 
outside air pressure. (6) At last, he/she would unlock and open the outer door and the whole team will 
exit to the outside surface of Mars. 

Our third example is the [Radiation Modeling](https://github.com/mars-sim/mars-sim/wiki/Radiation-Exposure),
 which account for how often the **Galactic Cosmic Ray (GCR)** and **Solar Energetic Particles (SEP)** 
 would occur during EVA. The cumulative dose is closely monitored in 3 specific exposure interval, 
 namely, the 30-day, the annual and the career lifetime of a settler. It would affect 3 different regions 
 of our body, namely, the *Blood Forming Organs (BFO)*, the *Ocular Lens*, and the *Skin*. The dose limits are
 measured in *milli-Severt*.
 
---
 
## Settlement Development 
Player may build numerous settlements spreading across the surface of Mars. Each settlement has a 
*[command structure](https://github.com/mars-sim/mars-sim/wiki/Role)* and a 
*[development objective](https://github.com/mars-sim/mars-sim/wiki/Settlement-Objective)*.  

As mars-sim loosely follows the *Mars Direct Mission Plan by Robert Zubrin*, there are 6 types of 
[settlement templates](https://github.com/mars-sim/mars-sim/wiki/Settlement-Templates) to choose from. 
A 4-settler initial base is called a *Mars Direct Plan (MDP) Phase 1* template. An 8-settler base is constructed 
under *MDP Phase 2* template. A 12-settler base is *MDP Phase 3*. A 24-settler base is designated as 
*Alpha Base*. Besides, players may build a *Trading Outpost* or a *Mining Depot* near sites with 
high mineral concentration.

---

## Economics
In terms of [economic](https://github.com/mars-sim/mars-sim/wiki/Economics) modeling, mars-sim implements the 
**Value Point (VP)** system, which keeps track of the supply and demand on each good and resource. 
As there is no standard currency established on Mars yet, settlers barter trades with neighboring settlements 
by keeping track of the credits and deficit based on the VPs of the resources in exchange in each trading session.

---

## Jobs and Missions
Settlers spend much of their time learning to *live off the land*. Assigning meaningful 
[jobs](https://github.com/mars-sim/mars-sim/wiki/Jobs) to the settlers are vital to the health of the economy of 
the settlements. Settlers engage in various [tasks](https://github.com/mars-sim/mars-sim/wiki/Tasks) such as 
maintenance, ensuring life support resources are plentifully supplied, growing food crops in 
[greenhouses](https://github.com/mars-sim/mars-sim/wiki/Greenhouse-Operation), making secondary 
[food products](https://github.com/mars-sim/mars-sim/wiki/Food-Production), and manufacturing needed parts 
and equipment in workshops.

Settlers will also go out on field [Missions](https://github.com/mars-sim/mars-sim/wiki/Missions) to explore and 
study the surrounding landscapes, to prospect and mine minerals, and to trade with neighboring settlements, etc. 
They may even decide to migrate from one settlement to another.

---

## Reliability, Maintenance and Malfunctions
The perils of living on Mars are very real. There is a total of 39 types of [Malfunctions](https://github.com/mars-sim/mars-sim/wiki/Malfunctions) 
that can occur at a given moment. There are 3 metrics
for tracking how reliable a part is, namely, [Reliability](https://github.com/mars-sim/mars-sim/wiki/Reliability) 
percentage, Failure Rate, Mean Time Between Failure (MTBF), which are dynamically updated in light of any incidents
that occur during the simulation. Workshops and machinery factories are to produce parts for 
replenishing parts to be used during [Maintenance](https://github.com/mars-sim/mars-sim/wiki/Maintenance) tasks.

---

## Summary
Mars is a harsh world but is certainly less unforgiving than our Moon. Settlers come face-to-face with accidents, 
equipment malfunctions, illnesses, injuries, and even death. Survival depends on how well they work together, 
improve their survival skills and balance individual versus settlement needs.

As the settlers learn how to survive the hardship and build up their settlements, players are rewarded with the 
pure joy of participating in this grand social experiment of creating a new branch of human society on another 
planetary surface.

---

## Website
For a more detail description of this project, see our [project website](https://mars-sim.github.io/).

---

## Wiki
* Check out our [wikis](https://github.com/mars-sim/mars-sim/wiki) at GitHub. 

---

## Supported Platforms
* Windows
* MacOS
* Linux

---

## Download 
Check out the most recent pre-release build in the [GitHub's Release Tab](https://github.com/mars-sim/mars-sim/releases).  

Also, see the previous and official release version at 
[SourceForge Repo](https://sourceforge.net/projects/mars-sim/files/mars-sim/3.1.0/).

If you like, click on the SF's button below to automatically sense the correct OS platform to download. 

[![Download Mars Simulation Project](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/mars-sim/files/latest/download)



---

## Getting Started

### Prerequisites

Currently, mars-sim supports Java 11. We will transition to Java 17 as 
JDK 17 is the next long-term support (LTS) release.

* Requires only JRE 11 for running mars-sim
* Requires only JDK 11 (or openjdk 11) for compiling binary

### JDK and JavaFX

Beginning Java 11, the JRE/JDK package is being decoupled from the graphic 
JavaFX API package. 

For the open source community, the OpenJDK is also being decoupled from the OpenJFX.

Currently, mars-sim does not require JavaFX. 

 ```
Specifically, the official release v3.1.0 to v3.2.0 of mars-sim do not 
utilize JavaFX / OpenJFX. Therefore, it's NOT a requirement to install them.

 ```
 
However, if you want to run any JavaFX apps, make sure you also download and 
configure the OpenJFX or JavaFX package on top of the JDK. 

See ticket #156 to read the discussions on how to set up JavaFX to run it 
under Java 11.



You may go to [Bellsoft site](https://bell-sw.com/pages/downloads/#/java-16-current) to 
obtain the latest Liberica JDK for your platform. 

We recommend downloading the `Full JDK` 64 bits JDK. 

`Full JDK` includes LibericaFX, which is based on OpenJFX, for 
running other apps that requires JavaFX.

For windows platform, choose MSI version that will automatically set up the environment path.


### OS Platforms

Assuming that OpenJDK 11.0.12 is being used,

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
 
1. Start a command prompt and type this `set PATH="C:\Program Files\Java\jre-11.0.12\bin";%PATH%`.

2. Alternatively, pre-set the `JAVA_HOME` and `PATH` in *Environment Variables* in Control Panel. 
   
  a. Add `C:\Program Files\Java\jre-11.0.12\bin` to the `PATH` variable. 
 ```
Note 2 : The order of precedence inside `PATH` is crucial. The first available folder having Java 
executable inside will be the one to be loaded by Windows OS. 
 ```
  b. Set `JAVA_HOME` to a JRE or JDK's destination such as `C:\Program Files\Java\jdk-11.0.12\bin\` or 
`C:\Program Files\Java\jre-11.0.12\bin`. 
 
  ```
Note 2a : The `\bin` is crucial. When running `java -jar xxxx.jar`, mars-sim will look for the 
presence of the `java.exe` in Windows OS. If `\bin` is missing in the `JAVA_HOME` variable, 
the Windows OS may not be able to locate the `java.exe` and may continue to go down the `PATH` 
variable to look for a valid JDK folder. If it's not found, java cannot start mars-sim. 
 ```
 
  c. Add `%JAVA_HOME%;` to `PATH`. Type "path" in a command prompt to double check 
the order of precedence when it comes to searching for the JDK. 

 ```
Note 3 : The BEST approach is to enable only one Java build (such as Java 11.0.12) 
inside `PATH` and remove all other folders referencing other java versions/builds.
 ```
3. Remove any path similar to `C:\ProgramData\Oracle\Java\javapath;`  in `PATH` variable. It can 
interfere with the correct version of Java that you would like to use. 
 ```
Note 4 : Depending on the order of precedence in Path variable, 
`C:\ProgramData\Oracle\Java\javapath` can load the undesired version of jre/jdk,
instead of the java version you prefer.
 ```
4. To test the version of Java that your machine is using, type "java -version"
in a command prompt window.

5. To check if a particular Oracle version of Java is being *enabled*, 
start **Java Control Panel (JCP)** in Windows's Control Panel as follows :  

* Move your mouse to the magnifier icon (the 2nd icon from the left) on win 10 task bar. 
* Type `Configure Java`. 
* Hover your mouse over the `Configure Java` and click to start the `Java Control Panel`.
* Click on `Java` tab on top.
* Click on `View` button to open up another panel window.
* Click on the checkbox on the `Enable` column to enable or disable any installed versions of Java.
 ```
Note 5. In JCP, each row represents a version of Java. It's very typical for a machine to 
have multiple versions of Java installed. Unfortunately, this panel only tracks the 
official Oracle versions. If you install any openJDK's on your machine, JCP won't 
be able to recognize them.
 ```
 ``` 
Note 6. To track what versions of openjdk having been installed on your machine. Use [JDKMon](https://harmoniccode.blogspot.com/2021/04/friday-fun-lxiii-jdkmon.html) 
 ``` 
 
### Remote Console Connection

To set up true headless mode in your platform, follow the steps in this [wiki](https://github.com/mars-sim/mars-sim/wiki/Remote-Console-Connection).

---

## Outreach
Feel free to use our [Facebook community](https://www.facebook.com/groups/125541663548/) to discuss relevant topics with 
regard to the development of mars-sim. See also old/archived [SF discussions](https://sourceforge.net/p/mars-sim/discussion/).

---

## Discussions
Feel free to start a discussion on a particular topic at our GitHub [Discussion](https://github.com/mars-sim/mars-sim/discussions) page. 

---

## Issues/Tickets
* Current : [GitHub Issues](https://github.com/mars-sim/mars-sim/issues)
* Past/Archived : [SF Issues](https://sourceforge.net/p/mars-sim/tickets/search/?q=status%3Awont-fix+or+status%3Aclosed)

Help us by filling in the info below when submitting an issue :

**Describe the bug**
 - A clear and concise description of what the bug is.

**Affected Area**
 - What area(s) are we dealing with ? [e.g. Construction, Mission, Resupply, Settlement Map, Mini-map, 
 Saving/Loading Sim, System Exceptions in Command Prompt/Terminal, etc..]

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
 - Java version : [e.g. Oracle JDK 11.0.12, AdoptOpenJDK 11.0.12, openjfx 11 etc.]
 - mars-sim build version : [e.g. r4255, v3.1.1, etc.]

**Additional context**
 - Add any other context about the problem here.

> Note 1 : By providing the info above from the start, you help expedite the handling of the issue you submit.

> Note 2 : if you double-click the jar file to start mars-sim and nothing shows up, it's possible that an instance of 
a JVM be created but it fails to load MainScene. In Windows OS, you may hit Ctrl+ESC to bring up the Task Manager and 
scroll down to see any "orphaned" instances of `Java(TM) Platform SE binary` running in the background. Be sure you 
clear them off the memory by right-clicking on it and choosing `End Task`. 

---

## How to contribute 
We welcome anyone to contribute to mars-sim in terms of ideas, concepts and coding. If you would like to contribute 
to coding, see this [wiki](https://github.com/mars-sim/mars-sim/wiki/Development-Environment) for developers. 
Also, we will answer your questions in our [Gitter chatroom](https://gitter.im/mokun/mars-sim). 

---

## Official Codebase
* https://github.com/mars-sim/mars-sim

---

## License
This project is licensed under the terms of the GPL v3.0 license.
<!--stackedit_data:
eyJoaXN0b3J5IjpbMjAwMTcyNzM3OCwtODY5ODg0NTgwXX0=
-->
