[![Release version](https://img.shields.io/github/v/release/mars-sim/mars-sim?sort=semver&color=blue&label=release&style=flat-square)](https://github.com/mars-sim/mars-sim/releases/latest)
[![Repo Size](https://img.shields.io/github/repo-size/mars-sim/mars-sim?style=flat-square)](https://github.com/mars-sim/mars-sim/releases/latest)
[![Commits Since](https://img.shields.io/github/commits-since/mars-sim/mars-sim/latest?style=flat-square
)](https://github.com/mars-sim/mars-sim/commits)
[![Last Commit](https://img.shields.io/github/last-commit/mars-sim/mars-sim?style=flat-square)](https://github.com/mars-sim/mars-sim/commits)
[![GitHub Downloads](https://img.shields.io/github/downloads/mars-sim/mars-sim/latest/total?style=flat-square&label=Latest%20Downloads&color=blue)](https://github.com/mars-sim/mars-sim/releases)

[![Gitter](https://badges.gitter.im/mokun/mars-sim.svg)](https://gitter.im/mokun/mars-sim?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.mars-sim%3Amars-sim&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=com.mars-sim%3Amars-sim)
[![Language](http://img.shields.io/badge/language-java-brightgreen.svg)](https://www.java.com/)


# mars-sim 

## Table of Contents
- [Introduction](#introduction)  
   - [Simulation](#simulation)
   - [Mars Direct Mission Plan](#mars-direct-mission-plan) 
   - [Settlement Development](#settlement-development)
   - [Economics](#economics)
   - [Authorities](#authorities)   
- [Operation Modeling](#operation-modeling)
   - [Timekeeping](#timekeeping)
   - [Indoor Atmosphere](#indoor-atmosphere)
   - [EVA](#eva)
   - [Radiation](#radiation)
   - [Task](#task)
   - [Work Shift](#work-shift)
   - [Mission](#mission)
   - [Weather](#weather)
   - [Maintenance and Malfunction](#maintenance-and-malfunction)
- [Creating the Future](#creating-the-future)
- [Conclusion](#conclusion)
- [Development](#development)
   - [Outreach](#outreach)
   - [Discussions](#discussions)
   - [Issues and Tickets](#issues-and-tickets)
   - [Contribution](#contribution)
   - [Website](#website)
   - [Wiki](#wiki)
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

Each settler is modeled as an intelligent agent possessing a reasonably high degree of autonomy and mobility.
Given a set of rules to live by and a capacity to learn from its past history, each agent acquires experiences
and survives the conditions upon it. A settler is assigned with a [job](https://github.com/mars-sim/mars-sim/wiki/Jobs), 
a [role](https://github.com/mars-sim/mars-sim/wiki/Role), having a set of technical [skills](https://github.com/mars-sim/mars-sim/wiki/Skills), 
owning certain [personality traits](https://github.com/mars-sim/mars-sim/wiki/Personality) and natural
[attributes](https://github.com/mars-sim/mars-sim/wiki/Attributes), and preferences on what tasks to take
and what missions to participate. Given their [relationship](https://github.com/mars-sim/mars-sim/wiki/Relationship) with one another, 
they live, dream, explore, and settle down on Mars.

## Mars Direct Mission Plan 

mars-sim loosely follows the *Mars Direct Mission Plan* (by Robert Zubrin) and has 8 basic
settlement templates to choose from. The first template is a *Mars Direct Plan (MDP) Phase 1* base
with 4 settlers. The next template is a *MDP Phase 2* base with 8 settlers. Then a *MDP Phase 3* base
has 12 settlers. An *Alpha Base* has 36 settlers. A *Hub Base* has 56 settlers. A *Sector Base* has
136 settlers. Besides, players may build a *Trading Outpost* or a *Mining Depot* 
near sites with high mineral concentrations. 

Each sponsoring agency or country of origin has unique templates that house various types of buildings. 
Altogether, there is a total of 37 unique [settlement templates](https://github.com/mars-sim/mars-sim/wiki/Settlement-Templates) 
to choose from.

## Settlement Development

The goals in mars-sim include populating Mars with human settlements, outposts, weather stations, 
communication towers, and, in future, transportation corridors such as road/train/plane network. 
Initially, a sponsor decides the location of a landing site and provides specific guidance over 
the development of its settlements such as mission agendas and [objectives](https://github.com/mars-sim/mars-sim/wiki/Settlement-Objective).
. Eventually, settlements will outgrow these goals and develop its own laws and governance 
and evolve at their own pace. 

## Economics

In terms of [economics](https://github.com/mars-sim/mars-sim/wiki/Economics) modeling, mars-sim 
implements the **Value Point** (VP) system, which keeps track of the supply and demand on each 
good and resource. As there is no standard currency established on Mars yet, settlers barter 
trades and do deliveries with neighboring settlements by keeping track of credits and deficits 
based upon the VPs due to the exchange of resources from trading or delivery missions.

## Authorities

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
of 34.2% oxygen at a pressure of 56.5 kPa (8.2 psi).

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

(1) Risk: 
How will we as a society perceive success and failure in the human exploration of the planet Mars ? 
Does a spiral engineering development approach for Mars exploration reduce the program risk ?
How do we analyze and reduce risk in maturing hardware and software responsible for 
exploring Mars ? What is the likelihood that it would increase or decrease the success 
and failure in the human exploration of Mars ? 

(2) Collaboration: 
What kind of partnership and alliances between governmental entities and corporations should 
be established in the next phase of the exploration of Mars ? 

(3) Cost: 
How do we contain the expense of developing flight proven hardware and software for Mars mission ?
What kind of public support would be needed to continue exploration missions ? How will the mission
planners pay off the capital and mission costs in the long run ? 

(4) Sustainment: 
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

Currently, mars-sim requires Java 25. It also uses Apache Maven as the build platform.

* Requires only JRE 25 for running mars-sim
* Requires only JDK 25 (or OpenJDK 25) for compiling binary

## Outreach
Feel free to use our [Facebook community](https://www.facebook.com/groups/125541663548/)
to discuss relevant topics with regard to the development of mars-sim.

## Discussions
Feel free to start a thread on a particular topic at our GitHub
[Discussion](https://github.com/mars-sim/mars-sim/discussions) page.

## Issues and Tickets
* Current : [GH Issues](https://github.com/mars-sim/mars-sim/issues)

Help us by filling in the info below when submitting an issue :

**Describe the bug**
 - A clear and concise description of what the bug is.

**Affected Area**
 - What area(s) are we dealing with ? [e.g. Construction, Mission, Resupply,
 Settlement Map, Mars Navigator, Saving/Loading Sim, System Exceptions in Command
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
 - OS version : [e.g. Windows 11, macOS 26.1, Ubuntu 25.10]
 - Java version : [e.g. Oracle JDK 25, Azul Zulu 25, Amazon Corretto 25, BellSoft Liberica JDK 25]
 - Major version and build : [e.g. post 4.1.0]

**Additional context**
 - Add any other context about the problem here. By providing more info above when filing it, 
   you help expedite the handling of the issues you submit.


## Contribution
We welcome anyone to contribute to mars-sim in terms of ideas, concepts and coding. 
If you would like to contribute to coding, see 
[Contributing](https://github.com/mars-sim/mars-sim?tab=contributing-ov-file) 
for developers. Also, we will answer your questions in our 
[Gitter chatroom](https://gitter.im/mokun/mars-sim).


## Website
For a more detail description of this project, see our 
[project website](https://mars-sim.github.io/) or go to
our [domain](https://marssim.space/) directly.


## Wiki
See our general [wiki](https://github.com/mars-sim/mars-sim/wiki).
For technical detail, check out [DeepWiki](https://deepwiki.com/mars-sim/mars-sim).


## Download
Find the past and recent [releases](https://github.com/mars-sim/mars-sim/releases).

## License
This project is licensed under the terms of the GPL v3.0 license.


