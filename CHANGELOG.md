# Mars Simulation Project

Copyright &copy; 2025 Scott Davis  
Project: https://marssim.space
GitHub: https://github.com/mars-sim/mars-sim  

------------------------------------------|---------------------

## Version 3.10.0 (25 Oct 2025)

### A. CORE ENGINE IMPROVEMENTS :
<OL>
    <LI>Meal time schedules are flexible and follow the sunrise.</LI>
	<LI>Different toolsets requirements can be defined for Manufacturing Processes.</LI>
    <LI>Manufacture function can provide multiple toolings for processes.</LI>
	<LI>Simplify the start up for the Simulation configuration.</LI>
    <LI>Default configuration directory can be changed by a commandline argument.</LI>
    <LI>File logging is controlled from a commandline argument.</LI>
    <li>Dessert creation merged into normal Dish/Meal configurations.</li>
    <li>Mission outcome/objectives are held seperate from the Mission class. This support future of mixed purpose Missions.</li>
	<LI>Remodel and display a perosn's blood type.</LI>
	<LI>Remodel a person's eating appetite.</LI>
	<LI>Rebalance biomass yield on food crop.</LI>
	<li>Add Sector Base Template with population of 120.</li>
	<li>Replace with efficient pathfinding algorithm for settlement interior walk.</li>
	<li>Rework Loading Dock Garage to support 3 bays for rovers.</li>
	<li>Add 2 LUV bays in both garages.</li>
	<li>Add to garage when a vehicle returns to a settlement.</li>
	<li>Add Alpha Base template variants'.</li>
	<li>Optimize getting a set of buildings.</li>
	<li>Moderate food resource and food production.</li>
	<li>Lower structural heat loss due to proximity of adjacent building.</li>
	<li>Add bunk beds in some buildings for accommodating more settlers.</li>
	<li>Add Parameter Categories for customizing preferences in each settlement. </li>
	<li>Add Brine Water Well building to lessen the need for digging local ice and ice collection mission.</li>	
	<li>Implement construction queue.</li>
	<li>Improve inspection and maintenance. Allow auditing outside buildings remotely.</li>
	<li>Create MarsZone, StorageSpec, Research Spec, GenerationSpect and SourceSpec.</li>
	<li>Unify access to walking methods for persons and robots.</li>	
	<li>Rework how local construction tasks are executed during a construction mission.</li>
	<li>Rework various settlement templates and experiment with new building placements.</li>	
	<li>Create and rework coordinate format to be non-locale based in support of internationalization.</li>	
	<li>Add more German and UK person, settlement and vehicle names.</li>
	<li>Add Agenzia Spaziale Italiana, German Space Agency (DLR) and UK Space Agency (UKSA).</li>	
	<li>Add part fatigues to vehicles, robots and buildings and they may be lowered by inspection and maintenance.</li>
	<li>Limit resource demand with a ceiling imposed by the sum of projected, trade and repair demand.</li>		
	<li>Rebalance resources demand and supply chain.</li>
	<li>Add new bio-based and chemical-based resource processes.</li>
	<li>Allow hallways to have sufficient heat capacity by generating heat per square meter of area.</li>	
	<li>Add Sociology and Architecture as a science subject.</li>
	<li>Switch to supporting JUnit 5 and 6.</li>	
	<li>Rework mission log entries.</li>	
	<li>Add the initial model of generating tourists for large settlements.</li>	
	<li>Add new background music ogg files.</li>	
	<li>Implement new area collision algorithm for walking outside.</li>	
 </OL>

### B. UI IMPROVEMENT :
 <OL>
    <LI>Person Favorite displays the list of dish names.</LI>
    <li>Navigator Window provides filters for teh Exploration, Unit & Landmark layers.</li>
    <li>Redesign of the Navigator Window controls so layers are managed via a Tree control.</li>
    <li>Navigator window is no longer a fixed size and can be resized.</li>
    <li>Mission details panel redesigned.</li>
    <li>New standard way to display Entities with details and location button provided.</li>
	<LI>Display the average age and gender atio of a settlement.</LI>
	<li>Add 3 dark themes and a light theme.</li>
	<li>Enable top menu bar in macOS.</li>
	<li>Enable mars-sim dock icon in macOS.</li>
	<li>Relocate speed buttons to top left of the main window.</li>
	<li>Add 'Life Core A' and 'Half Core A/B' buildings.</li>
	<li>Add Preferences tab in SettlementUnitWindow.</li>
	<li>Add buildings svg images for new Bio-reactor labs and various Core connectors.</li>
	<li>Improve construction tab with queue table.</li>
	<li>Add deep borehole small drilling site.</li>
	<li>Show spot locations within building in Settlement Map's status bar.</li>
	<li>Fast track building maintenance if repair parts are posted and ready.</li>
	<li>Remove memory leak when scrolling up and down Settlement map's zoom slider.</li>
	<li>Update mass/quantity properly in Monitor Tool's Good tab.</li>	
	<li>Add settlement wide construction and manufacturing queue.</li>
	<li>Convert various missions to Objective pattern.</li>	
	<li>Reduce heap memory when zooming in the Settlement local Map.</li>
	<li>Replace console main menu with a swing-based startup chooser menu.</li>
	<li>Allow each tab in Monitor Tool to filter settlements (All, a sponsor or a settlement).</li>
	<li>Add a sound effect toolbar and a music toolbar.</li>	
	<li>Add a startup chooser.</li>	
  </OL>  

### C. FIXES :
<OL>
    <LI>Correct how GoodsManager uses properties when updating the value.</LI>
    <LI>Correct missing main window title.</LI>
	<LI>Correct male and female gender ratio.</LI>
	<LI>Correct the use of Alive/Deceased/Buried checkboxes in Monitor Tool's Citizens tab.</LI>
	<LI>Correct the use of stock capacity.</LI>
	<li>Correct mouse button detection for macOS in Settlement Map.</li>
	<li>Proper artificial lighting for growing crops in greenhouses.</li>
	<li>Correct hatch-facing attribute interpretation for building locations.</li>
	<li>Correct getting a computational node.</li>
	<li>Fix getting a random settlement name.</li>
	<li>Fix calculating local vs. market demand and value points.</li>
	<li>Fix area calculation for hab and hub.</li>
	<li>Fix repairbots getting stuck in EVA airlocks.</li>
	<li>Correct the use of population factor for computing demand and good value.</li>
	<li>Return zero rating for emergency mission.</li>
	<li>Clean up astroart code.</li>	
	<li>Update mass/quantity properly in Monitor Tool's Good tab.</li>		
	<li>Correct the way the ChainOfCommand elect leaders and chiefs and release old roles.</li>		
	<li>Identify and correct teleportation of mission members during mission back to their home settlement.</li>
	<li>Correct the way the power grid would selectively shut down various types of buildings that may generate power to have net position power.</li>		
	<li>Correct inability to automatically starting Field Study missions.</li>		
	<li>Correct collecting rocks in Exploration and various field missions.</li>			
  </OL>

------------------------------------------|---------------------

## Version 3.9.0 (15 Mar 2025)

### A. CORE ENGINE IMPROVEMENTS :

- Help generator expanded to be more flexible.
- Salvage processes & Manufacturing processes consoliddated into single logic.
- Settlements have a single queue to cover all Manufacturing.
- Vehicle Unloading uses a new Status to remove dependency on Vehicle Mission.
- Unit Hierarchy restructured to introduce MobileUnit & FixedUnits.

    
### B. UI IMPROVEMENT :

- Help library expanded to cover all configuration entities.
- New Manufacturing Settlement screen.
- Maps are downloaded asynchronously.
- Reworking of Map Layer logic to increase reuse and reduce computation costs.
  
  
### C. FIXES :

- Resource Process: fix problematic toggle logic.
- Monitor Tool: Displays newly arrived Settlements in Monitor Tool.


------------------------------------------|---------------------
## Version 3.8.0 (Tue 27 Aug 2024)

### A. CORE ENGINE IMPROVEMENTS :

- Agency: add "International Space Research Alliance" (ISRA), a Chinese-led multi-national alliance and Taiwan nation state.
- Computing: schedule resources of a task from multiple nodes. 
- Country: incorporate Taiwan as nation state.  
- Drone: add modeling thrust and potential energy.  
- Fishery: refine feeding, growing and harvesting fish and weed. 
- Goods Manager: rework relationship with objectives.
- Group Activity: add group activities for a settlement. e.g. birthday parties, council announcements, team meetings etc.
- Health: streamline MedicalConfig, health problem and complaints.
- Heating: revamp heat sink & ventilation.  
- Medical: rework prescribing medication, examining bodies, and recovery tasks.
- Preferences: manage settlement preferences in various categories of mission, science and task.
- Process Definitions: standardize approach in defining processes such as salvage, food production and manufacturing.  
- Releases: integrate Gitflow and GitHub Action for building binary releases.
- Resource Process: respond better to toggling resources on-demand. 
- Settlement Tasks: tasks can be selected by On duty & Off duty persons.
- Settlement Template: may define default objective optionally.

  
### B. UI IMPROVEMENT :

- Backlog: add a column showing the task's work scope.
- Building Tab: add tracking heating system parameters.
- Computing: shows heat dissipation, power for load/non-load & cooling demand.  
- Exploration: show types of rocks collected when exploring sites.	
- Health Log: show a person's health history log with complaint types, date started and date cured.
- Monitor Tool: support and filter authorities and settlements in one single combobox.
- Preference: show # active Missions controlled by Preference panel.
- Settlement: add new Process History tab showing all completed Processes.
- Status Bar: correctly show phi and theta of the mouse cursor.
- Vehicle: shows highlighted vehicle box border in settlement map.
  

### C. FIXES :

- Airlock: correctly looks for airlock with least number of occupants.	
- Heating: manage temperature instability via entropy change to relate temperature change and heat transfer. 
- Launch Date: correct the launch day calculation when it's before 01-Adir-01.
- Navigation: rework vehicle movement to consume fuel and battery power correctly.	
- Parking: set limits in looking for vehicle parking locations recursively.
- Power Generation: correct how power grid turns on/off buildings during power surplus/deficit.  
- Resource: correct budgeting essential resource on-demand. 
- Resource: resolve dwindling amount of methane and hydrogen.
- Robot Charging: avoid robot battery depletion. Ensure wireless charging always possible. 
- Salvage Process: Correct problem when scoring the salvage of a vehicle. 

------------------------------------------|---------------------

## Version 3.7.2 (Sun 18 Feb 2024)

### A. CORE ENGINE IMPROVEMENTS :

- Amount Resource: rework fertilizer composition to make use of bacteria.
- Construction: revise frame and foundation template.
- Dust Storm: refines log and show occurrences in settlement banner.
- Part: rework use of aerogel tiles for construction mission.
- Resource: remove mortar in ResourceUtil.

  
### B. UI IMPROVEMENT :

- Construction: add new col "Available Material" during construction mission.
- Settlement: correct building overlapping in Hub Base template.
  

### C. FIXES :

- Airlock: add missing airlock to trading outpost and mining outpost template.
- Building: add checking for building collision at startup.
- Greenhouse: correct experience point calculation when tending crops.
- Heating: revise air and water heat sink buffer.
- Power: add missing power set in settlement templates.


------------------------------------------|---------------------

## Version 3.7.1 (Wed 17 Jan 2024)

### A. CORE ENGINE IMPROVEMENTS :

- Building Alignment: add north-south alignment attribute to each building. 
- Building Package: add standalone building sets for faster settlement template creation.  
- Building: add new Syngas Plant building for synthesizing methanol for vehicles.
- Task: add checking for water level change and resource demand in Budget Resource Task.
- Threading: quit clock thread while loop when paused to save CPU cycles.      
- Walking: simplify colision-related methods.    
- Weather: rework refresh timing with 5 weather params. 

  
### B. UI IMPROVEMENT :

- Airlock: fix failing to ingress.
- Dust Storm: show dust storm status in settlement map banner.
- ERVs: relocate ERV closer to (0,0) and align next to habs.  
- Layout: rework activity spots and add a lab room in Medical Hab.
- Location Tab: show settlement or vehicle vicinity.
  

### C. FIXES :

- Airlock: avoid getting stuck in pre-breathing phase when a person is too exhausted.
- Goods: ensure no same goods be selected in the buying and selling list.
- Heating: prevent temperature instability and correct air heat ink.
- Heat/Power Generation: correct calculation with methane fuel spent and its power output.
- Maintenance: correct computing maintenance meta tasks and work time.
- Sleep: ensure settler can get some sleep in astronomy observatory.
- Time: limit to certain iterations in A* pathfinding algorithm when walking outside.


------------------------------------------|---------------------

## Version 3.7.0 (Thu 28 Dec 2023)

### A. CORE ENGINE IMPROVEMENTS :

- Activity Spot: spots represent names & reservable spot where Tasks can be completed.
- Algae Pond: add building, function, tasks for growing spirulina. 
- Corporation: add Blue Origin as the 3rd corporation. 
- Country: add UAE and Saudi Arabia.   
- Economy: add GDP and PPP to xml for each country. 
- Group id: rename to com.mars-sim and com.mars_sim in package name. 
- Macro Simulation: simulate lunar colonies with influx of researchers and engineers. 
- Maven: change all maven package names to start with `com.mars-sim`. 
- Person: add average weight & height characteristics per country.   
- Rating: implement rating score for Settlement, Person & Robot Tasks and hold the composite parts of the overall score. 
- Sponsor: add sponsor mode for players to choose in console menu. 
- UnitTests: add more Unit Tests. 

  
### B. UI IMPROVEMENT :

- Monitor Tool: optimize Monitor Model.
- Diplomatic: add diplomatic channel in Command Dashboard for viewing live statistics in Lunar Colonies. 
- Orbit Viewer: zoom in between Mars and Earth's orbit.
- Settlement Map: Reworking fo the map layer logic. Activity Spots are now visible on the map.
- Person Monitor Tool: add tooltip to Task column showing Score breakdown. On Duty indicator on Shift column.
- Robot/Person Activity Panel: redesigned to show the stack of Tasks and alternative Tasks not choosen. Tooltip shows Rating breakdown.
  

### C. FIXES :

	<LI>Airlock: avoid getting stuck in pre-breathing phase when a person is too exhausted.
	<LI>Conversation: allow settlers to chat with others as a subtask.
	<LI>Mars Navigator: ensure each map type remember its previous choice of resolution.
	<LI>Mission Table: fix problem with invalid column. 
	<LI>Sleep: ensure correct building when assigning a bed.


------------------------------------------|---------------------

## 3.6.2 (Mon, 18 Sep 2023)

### A. CORE ENGINE IMPROVEMENTS :

- Crop: allow changing growing area per crop in Farming.
- Log: remove excessive CPU loads in generating log statements in PowerGrid and ThermalSystem.
- Pending Task: optimize the use of pending task vs. assigning/replacing task directly. 
- Time Ratio: optimize how time ratio increase/decrease can dynamically affect pulse width and TPS in MasterClock. 



### B. UI IMPROVEMENT :

- Crop: use spinner to change growing area for each farm in Agriculture tab in Dashboard.
- Task: display subTask2 and subTaskPhase2 in Activity Tab.
 

### C. FIXES :

- Computing: rework work time related to consuming computing resources when analyzing map data.
- Digging Local: correct not being able to start digging ice and regolith on its own.
- Disembarking: optimize and correct how a crew member disembarks vehicle from garage. 
- Equipment: correct transferring container back to settlement after digging regolith/ice. 
- Exploration: end EVA if site is not determined.  
- Food Production: correct potato typo.
- Map: correct magnification and zooming issues.
- Settlement: select correctly whether settlers and vehicles are in settlement or its vicinity in Settlement Map.  
- Weather: Correct weather param concurrency during reload.


------------------------------------------|---------------------

## 3.6.1 build 0ac7103 (25 Aug 2023)

### A. FIXES :

- Mineral Map: Avoid NPE after a reload by serializing a list of mineral types 
    - <a href="https://github.com/mars-sim/mars-sim/issues/1005">#1005</a>.


------------------------------------------|---------------------


## 3.6.0 build 8557 (24 Aug 2023)

### A. CORE ENGINE IMPROVEMENTS :

- Building    : Add 2 new buildings, namely, Server Farm and Central Hub A. 
- Computing   : Track entropy in each computing node. 
- Country     : Allow each country's names to be loaded on demand. 
- Crop        : Add a new category of herbs. 
- Earth Time  : Model using the standard Java LocalDateTime class.
- Elevation   : Adopt higher resolution height data.  
- EVA Suit    : Refine the modeling of resources needed to produce the suit parts.
- EVA Time    : Add scheduling EVA time to start right after sunrise.
- Fuel        : Enable the use of methanol as fuel for vehicles.
- History     : Add History class to tally time-dependent events.
- Mars Time   : Model using the standard Java LocalDateTime class.
- Mission     : Retains reference to Vehicle after Mission completed.  
- Power       : Model nuclear reactors with more parameters.
- Reliability : Tweak malfunction probability and rework how various factors relate to one another.	
- Resupply    : Define resupply missions with a repeating schedule.
- Resources   : Allow alternate resources for manufacturing and food production.
- Settlement  : Keep track of settlement preference task/mission/science modifiers. 
- Sites       : Discover & estimate mineral concentration of potential sites.
- Training    : Replace hard-coded training-history-to-role mapping with external xml configuration.
- Vehicle     : Enable regen braking and encapsulate propulsion calculations in motor controller class.


  
### B. UI IMPROVEMENT :

- Connection : Simplify building connection definition with hatch-facing attribute.
- Events     : Events in the Monitor Window are clickable to drill down into the details of the entity.
- Mars Map   : Add zooming capability, add more map types, and allow maps to load various levels of resolutions.
- Mineral    : Optimize mineral layer loading speed in Mars globe Navigator.
- Resupply   : Showcase resupply missions by settlements under a tree.
- Role       : Restore previous role if change is not confirmed.
- Scenario   : Scale up default scenario to including 12 settlements.
- Search     : Add search term history and add equipment list in Search Tool.
- Settlement : Display settlement preference task/mission/science modifiers and allow players to edit scores. 
- Splash     : Add 3 more splash pictures.
- Template   : Add Hub Base settlement template that features a Central Hub at the center of the base. 
- Vehicle    : Retain vehicle reference to completed missions.
- Weather    : Correct weather icon switching in Settlement Map.
  

### C. FIXES :

	<LI>EVA Suits  : Ensure EVA suits retain in vehicle during mission.
	<LI>Meteorites : Rework what reasonable malfunctions an meteorite impact would trigger.	
	<LI>Social     : Avoid concurrency issue by making relationship score updating in one direction.
	<LI>Resources  : Correct displaying amount resources stored in Inventory Tab.


------------------------------------------|---------------------

## 3.5.0 (build 7907) - 5 Apr 2023

### CORE ENGINE IMPROVEMENTS :
-    Battery : Standardize battery capacity. 
-     Events : Supports future scheduled events to better handle periodic actions on the entities.     
-    Mission : Add deadline on rover mission departure. Split embarking phase into loading & departing.  
-      Parts : Add silica aerogel as transparent rooftop tiles.   
-      Robot : Define specifications using RobotSpec class.   
-      Shift : Add shift manager to handle work shift change and add on leave status.  
-  Stock Cap : Increase general/cargo capacities and specific resource storage capacity. 
-    Storage : Allow selection of storage bin in DigLocal task based on building resource capacity. 
-       Task : Define SettlementMetaTask and track a pool of shared tasks.   
      
### UI IMPROVEMENT :
-   Desktop : Remember tools' position and contents. 
-       L&F : Look and Feel can be changed via Settings menu item. 
-   Mission : Organize missions under a tree structure for each settlement.   
-    Panels : Separate tables for Maintenance, Waste and Resource processing. 
-   Styling : Single styling via look and feel applied to all components. 
-       Tab : Add new tab to the Monitor Tool to show task backlog of each settlement. 

### RUNTIME ENVIRONMENT :
- Java : Require Java 17 or above.  
 
### FIXES :
 <LI> EVA Suit : Allow suit repair in a vehicle using repair task. 
 <LI> Jar file : Make settlement template name readable by using lowercase letters. 
 <LI>    Robot : Correct robot battery status to be based on percentage, instead of actual KWh. 
 <LI>  Vehicle : Correct fuel consumption calculation. 
 

------------------------------------------|---------------------

## v3.4.1 (build 3681d3e) - 31 Dec 2022

### ISSUES ADDRESSED :
 
 <LI> Fix #783 that relates to a failure of starting from the JAR file. 
 
------------------------------------------|--------------------- 
 
  
## v3.4.0 (build 7642) - 19 Nov 2022

### ISSUES ADDRESSED :

### CORE ENGINE IMPROVEMENTS :
-   Base Mass : Add base mass to all buildings, in preparation for calculating overall rocket payload mass. 
-    Building : Add building general info such as dimensions and mass. 
-   Computing : Add computing function and skill. Track computing resource usage. 
-      Filter : New Settlement filter and Building tab to the Monitor Tool. 
-   Inventory : Replace with lightweight inventory for all units. Revamp the storage of resources/equipment and transfer of person/vehicle between locales.
- Malfunction : Tidy up malfunction and maintenance logic on vehicles, buildings and equipment.
-     Mission : Restructure and simplify handling of Mission.
-       Parts : Add garment and thermal bottle. 
-      Person : Track exercise. Make a person's carrying capacity to be age-dependent.
  <LI >Processing : Unify resource and waste processes, food production and manufacturing under common super class.  
-       Robot : Track battery capacity and charging.
-     Trading : Compute and relate the cost, profit, and price of trade goods.
-    Scenario : Configure arriving future Settlements from a scenario. Scenarios can be exported and shared. 
-        Site : Explore, claim and track mining sites.
-      Social : Remove global relationship graph and simplify codes.
-    Sunlight : Predict sunrise and sunset at each settlement. Track actual sunrise and sunset time.
-   Meta Task : Implement TaskJob approach and remove duplicate computation of outstanding tasks.
-     Vehicle : Rework vehicle specifications. Refine fuel economy calculation.

  
### UI IMPROVEMENT :
-   Authority : Add editor that customize sponsors/authorities. 
-       Chart : Auto select bar and pie chart.  
-    Economic : Display the cost/profit/price of a good in each settlement for comparison.
-       Icons : Replace word title of each tab with an icon in all Unit Windows.
-    Location : Show areocentric longitude of Mars in Time Tool.   
-       Power : Display generated/load power & stored energy for settlements in Monitor Tool.
-    Scenario : Edit and load basic scenarios.
-      Window : Add new Building Window with tab, replacing old Building tab.
  
### FIXES :
-     Airlock : Fix bugs during EVA egress and ingress. 
-    Delivery : Resolve stalled negotiation. 
- Exploration : Revisit existing sites until they reach an evaluation ready for Mining. 
- Maintenance :	Ground vehicle maintenance no longer stalls when EVA is aborted. 
-     Mission : Fix the mission selection. Correct Navigation tab.
-  Settlement : Correct settlement selection in Monitor Tool. 
-     Trading : Consider loading/unloading edge cases for Trading/Delivery. 
-   Transport : Correct handling of arriving settlement by transport manager.
-     Vehicle : Correct vehicle fuel calculation.


------------------------------------------|---------------------

## v3.3.0 (build 6218) - 2021-09-25

### ISSUES ADDRESSED :

-- remove old releases of junit which are abandoned upstream and add support for junit 5  #81
-- Task & Building classes are not Serialised #334
-- Use unmanned drones to deliver goods between settlements #361
-- MissionTableModel shows invisible rows #374
-- New Idea: Regolith Processing #376
-- Malfunction not fixed - repairers are sick or cannot find malfunction #377
-- Orbit Viewer cannot display from the view point of Mars #387
-- ClassCast exception starting a RescueMission #401
-- Support multiple Crews #410
-- Create a Scenario concept by isolating the initial Settlements #411
-- Relocate Reporting Authority pre-configured names #412
-- Optimize UIConfig #414
-- Equipment constructor can identify the incorrect Settlement #415
-- 3 methods with highest CPU usage #416
-- Removing dangling missions #418
-- Duplicated crew member #434
-- NPE during loading vehicle #439
-- Cannot create new player-defined single settlement in command line #442
-- Cannot load crew_alpha.xml in windows: File separator issue #446
-- NPE due to threading in finding dead people #447

### CORE ENGINE IMPROVEMENTS :

1. Increase # of crops in greenhouses.
2. Move regolith-related processes from manufacturing to resource processing.
3. Keep track of new vs. used EVA suits.
4. Refine vehicle modeling. Give rovers and delivery drones acceleration profile.
5. Sort Parts by type.
6. Define countries and sponsors to xml instead of hard-coding them.
7. Define scenarios.
8. Revamp malfunction repair.
9. Refactor loading mission resources.

### UI IMPROVEMENT :

1. Add back the Orbit Viewer showing a graphical representation of the solar system
2. Add Authority Editor.
3. Add Scenario Editor.

### FIXES :

1. Correct extreme value point fluctuation.
2. Correct no vehicle operator in Delivery drone.
3. Fix drone delivery.
4. Adopt Sonarcloud for improving code quality.


------------------------------------------|---------------------
## v3.2.0 (build 5916) - 2021-07-06

### ISSUES ADDRESSED :

1. Simulation goes into a loop with PlanMission Task improvement
#370 by bevans2000 was closed on Jul 2

2. Time Ratio (TR) Adjustment based on the Tick Per Sec (TPS)
critical improvement
#369 by mokun was closed 6 minutes ago

3. Commander's Profile not loading
#367 by mokun was closed on Jun 21

4. Sponsoring Agencies feature request idea improvement
#363 by mokun was closed on Aug 3

5. MersenneTwisterFast is not Thread safe bug
#362 by bevans2000 was closed on Jun 21

6. Person is involved in too many Scientific studies started,
research agreement (or friendliness index) between settlements
improvement
#359 by bevans2000 was closed on Jul 27

7. Not a collaborator in a scientific study, Sorting in Science
 Tool, Task.endTask bug improvement
#357 by mokun was closed on Aug 2

8. Optimise UnitManager Unit improvement
#355 by bevans2000 was closed on Jul 15

9. Cannot store previous demand Good Value, lightweight Inventory
bug improvement
#348 by mokun was closed 6 days ago

10. Modeling of Airlock State and Airlock Operator's responsibility,
Vehicular Airlock, Console UI freeze improvement
#340 by mokun was closed on Jul 11

11. Limiting the size of the heap space bug improvement
#335 by mokun was closed on Aug 14

12. People stuck trying to enter airlock bug
#305 by bevans2000 was closed on Jun 23

13. MasterClock is reporting timing errors improvement
#287 by bevans2000 was closed on Dec 30, 2020

14. Problem find JFreechart in clean build, Java vs. JSON
serialization, concurrent thread for each settlement improvement
maven test
#283 by bevans2000 was closed on Jun 23

15. having a form to populate crew.xml; add beta_crew.xml feature
request
#251 by shirishag75 was closed on Jun 20

16. How do I begin? Incompatibilies with Java 13 improvement
#236 by Ranged was closed on Jun 23

17. Is there a way to have a crew profile saved as well,
new /xml folder, edit people.xml, new crew.xml, beta_crew.xml
feature request improvement question
#207 by shirishag75 was closed on Jun 20

18. IllegalStateException. not in a valid location situation to
start EVA task OUTSIDE bug
#14 by larsnaesbye was closed on Jun 16, 2017

### CORE ENGINE IMPROVEMENTS :

1. Switch back to supporting Java 11 for better compatibility.
2. Enforce one continuous sleep session as much as possible.
3. Reduce memory footprint by 50% when loading from a saved sim.
4. Remove the duplicated EVA function in garages.
5. Refine ice collection rate. Higher above/ below +/-60 deg
   latitude.
6. Refine/add computing site value/score for collecting ice,
   regolith, mineral exploration, and mining.
7. Add new job 'psychologist' and new skill/science type
   'psychology'.
8. Add ability to choose individual destination for each
   crewman as listed in crew.xml when using Crew Editor.
9. Adjust job prospect and refine job assignment for each
   settlement.
10. Designate Night/Day/Swing shift as XYZ work shift.
    Designate Day/Night shift as AB work shift.
11. Change the start and end time for work shift A
    (Day shift) and work shift B (Night shift).
12. Add teaching reward and learning points on skills when
   performing Teach task.
13. Make a reading task contribute to adding experience points
    to a skill.
14. Add settlement and vehicle names tailored to its
    sponsor/country.
15. Revamp EVA egress and ingress phases to model airlock
    activities in finer details.
16. Incorporate Weblaf's IconManager for caching svg icons.
17. Add "Engineering" as a new science subject.
18. Record the values of solar irradiance at each settlement.
19. Auto-sense user-edited xml files and back them up when
    checksum are mismatched. Allow a list of exception
    xml files.
20. Logically partition the calling of units by settlement.
21. Revamp sending time pulse and clock threading.
22. Rework acquiring new tasks and remove recursive calls inside Mind.
23. Refactor and expand console capability. Enable SSH connection.
24. Activate the fish farm and enable eating fish.
25. Create BuildingSpec to keep track of building type specifications.
26. Add new task of reporting to mission control.
27. Manage the upper limit of the time ratio (TR) internally based on
    most recent average value of tick per second (TPS).
28. Add Delivery mission and unmanned drone for trading resources
    (Experimental only).
29. Meteorite fragments (upon impacting a settlement) can be found
    and stored.


### UI IMPROVEMENT :

1. Provide the exact relationship score and attribute score (in
   addition to its adjective) in Person Window.
2. Add more levels of zooming in the settlement map.
3. Switch to using svg icons for better scaling and visual
   consistency in the settlement map.
4. Add showing the reference coordinates of the settlement map
   of the mouse pointer.
5. Correct the scaling of the dot size and coordinates of
   the person/robot in PersonMapLayer and RobotMapLayer at
   various zoom level.
6. Revamp the design of EVA airlock in its svg image.
7. Add BuildingPanelLiving to show the living accommodation
   aspects of a building.
8. Show the second subtask's description and phase.
9. Change size of person/robot/building/vehicle/site label
   on-the-fly in response to the change of map scale in
   settlement map.
10. Display weather icon and top text banner in settlement map.
11. Display parts in used in each building in Maint tab.
12. Allow players to assign 'task order' to all settlers in
    Command Mode (NOT available in Sandbox Mode).
13. Provide sunrise, sunset, period of daylight, zenith time,
    max sunlight, current sunlight in the settlement map.
14. Replace speed/time ratio (TR) slider bar with increase and
    decrease speed button.


### FIXES :

1. Fix OutOfMemoryError when saving sim.
2. Remove extraneous object references that bloat the saved
   sim file.
3. Fix rover embarking from a settlement.
4. Correct major walking bugs in that person frozen in the EVA
   Airlock building.
5. Account for vehicle emergency while still parking in a
   settlement.
6. Correct the location of inner and outer door/hatch of
   EVA Airlock.
7. Fix retrieving a list of vehicles reserved for mission or
   on mission.
8. Fix loading the alpha crew in the crew editor.
9. Correct the time consumed when reviewing mission plans.
10. Correct the creation of a list of sites to be explored for
    mineral content.
11. Correct the bed assignment.
12. Correctly associate how the availability of 3D printers
    affect concurrent manufacturing processes.
13. Sync up the position of the WebSwitch when pausing or
    resuming the sim.
14. Fix finding a lab supporting certain science types.
15. Fix and refactor the use of static references.
16. Fix music mute and volume control.

------------------------------------------|---------------------

## v3.1.1 (build 5283) - 2020-07-22

### CORE ENGINE IMPROVEMENTS :

1. Switch to supporting Java 14 only

2. Provide a bare basic way of using CLI to start a single settlement.

3. Refine the conversion between the level of effort of an operation and its modifier
   in GoodsManager.

### UI IMPROVEMENT :

1. None

### FIXES :

1. Correct startup issues

2. Correct dashboard's level of effort alignment

-----------------------------------------------------------------------------------------


## v3.1.0 (build 5268) - 2020-01-28

### CORE ENGINE IMPROVEMENTS :

1. Adopt the use of Java 11 SE for running mars-sim.

2. Allow time-ratio switches in full headless mode.

3. Significantly reduce the size of jarfile.

4. Improve mission planning - add mission approval phase, tasks to plan and score a mission plan.

5. Track/record settler's sleep time on each sol.

6. Add a stand-alone console window for querying the health of a settlement.

7. Implement basic emotion for each settler.

8. Add Command Mode vs. Sandbox Mode.

9. Compare settlement achievement using metrics (e.g. social/science score, water/O2 consumption/production)

10. Add options for customizing logging level.

11. Streamline saving and loading simulation.

12. Add beryx terminal for use in headless and non-headless mode.

13. Create a death report on a deceased person.

14. Add 'topics' for each scientific study.

15. Add role prospect scores (like job prospects) on each role.

16. Export xml config files to player's home folder and backup old xml files.

17. Allow playing external ogg music files in player's home folder.

18. Add vehicle status log for tracking changes to a vehicle.

19. Add mission status log for tracking changes to a mission.

21. Incorporate more accurate MOLA elevation data.

20. Link sponsor's mission directives to settlement's mission decisions.

22. Add the price of a good.

23. Add training certification prior to arrival on Mars.

24. Add role prospect score for determining the need of a role in a
    settlement.


### UI IMPROVEMENT :

1. Add play and pause switch on top right of tool bar.

2. Add geological map to Mars Navigator.

3. Add earth calendar on bottom status bar.

4. Add (x, y) coordinates, elevation info on Mars Navigator surface Map (and RGB/HSV values on the topo map)

5. Attach a separate 'EVA Airlock' building to Lander Hab.


### FIXES :

1. Correct the bad path inside executable file for headless edition

2. Solve unix End-Of-Line issue for the executable file.

3. Enable year 0 (orbit 0) in resupply or new arriving settlement mission.

4. Fix gaining mechanical skill in garage.

5. Fix numerous mission bugs.

6. Correct how food crop harvest should end.

7. Fix recovering the simulation from machine's power saving.

8. Fix food & water consumption.

9. Fix loading alpha crew MBTI personality into Crew Editor.

10. Improve vehicle fuel usage modeling.

11. Fix not being able to fix vehicle malfunctions.

12. Fix choosing sponsors (after choosing the country) in Crew Editor.

13. Correct inconsistency in the quantity/amount of input resources in
    construction.xml.

-----------------------------------------------------------------------------------------

## v3.1.0-beta1 (build 4421) - 2018-07-30

### KNOWN LIMITATIONS :

1. In MacOS, a JavaFX WebEngine's bug may create garbled characters on certain
   webpages in Help Browser.

2. In Linux, text fields do not allow text input.

3. A JavaFX WebEngine's crypto issue cripples the full UI loading of certain secure
   websites such as GitHub pages

4. When clicking on a combobox to pick an option, the option list would pop up
   at the far edge of the main window


### CORE ENGINE IMPROVEMENTS :

1. Implement pure headless mode (without the need of installing openjfx package).
   It utilizes minimal cpu resources when running mars-sim.

2. Enable saving and loading past historical events.

3. Improve how a vehicle should respond to "resources not enough" events
   during a mission. Will now travel toward a settlement as close as possible
   before turning on the beacon and asking for help.

4. Players may set limit to the distance a rover may travel in a mission.

5. Enable flexible Earth start date/time in simulation.xml.

6. Add new Reporter Job and new RecordActivity task.

7. Refine and revise mission objectives for each sponsor.

8. Relate the onset of a random ailment to the task a person is performing.

9. Add new resources such as silicon dioxide, quartz, glass, sodium carbonate,
   & sodium oxide and new processes "make quartz from silicon dioxide",
   "make soda from sodium oxide", "make glass from sand" and "extract silicon
   dioxide from sand".

10. Players may set water rationing and emphasis on certain type of tasks.


### UI IMPROVEMENT :

1. Display a lists of settlers according to their associated settlements in
   Dashboard tab.

2. Add a draggable dot matrix news ticker for notifying users of events
   under the category of medical, malfunctions, safety/hazards and mission.

3. Set default height and width to 1366x768 in the main menu and main window.


### FIXES :

1. Correct the missing decimal separator in Configuration Editor.

2. Fix excessive rows showing up in task schedules. Enable mission name to
   show up on separate column.

3. Fix not being able to autosave if running in headless mode.

4. Correct how field reliability data for parts is associated with human
   factor vs. parts fatigue vs. acts of God vs. software quality control issue

5. Fix the formula that restore a person from fatigue while sleeping. Tweak
   it to work well for high fatigue case.

6. Correct plexus graph issue (only after loading from a saved sim) that
   prevent from fully restore credit/debit history between settlement and
   existing relationship between settlers.


-----------------------------------------------------------------------------------------

## v3.1.0-Preview 9 (build 4271) - 2018-05-25

### KNOWN LIMITATIONS :

1. In MacOS, JavaFX WebEngine's bug creates garbled characters on certain
   webpages in Help Browser.

2. In Linux, text fields do not allow text input.


### CORE ENGINE IMPROVEMENTS :

1. Add checkbox for showing non-power/non-heat generating buildings in
   Power/Thermal Tab Panels.

2. Improve grid battery charging/discharging model.

3. Add air monitoring and heat ventilation for each building.

4. Tweak amount of power/heat output from various types of power/heat source.

5. Add tracking/displaying various properties of gases in each building.

6. Improve dust storm model and connect it to wind speed for each settlement.

7. Account for variation of outside air temperature in polar region.

8. Refine range of latitude/longitude input in sim config editor and minimap.

9. Refine grid battery charging/discharging cycle.

10. Consolidate repetitive log statements in console/command prompt window.

11. Convert to metric system in calculating heat gain/loss in each building.

12. Add reliability analysis on top of the malfunction model for tracking
    probability of failure for some item resource.

13. Shrink down 30% the file size of a saved sim.

14. Add thirst indicator and track if a person is dehydrated.

15. Add ability for settlers to tag an EVA suit and stick to using it as
    much as possible.

16. Stabilize the value points (VP) based economy. Add variable inflation to
    prevent abrupt changes.

17. Add new background music tracks.

18. Rework input/outpost of various resource processes.

19. Add noaudio argument if user desires running the simulation with no sound.


### UI IMPROVEMENT :

1. Display build # on the bottom right of the main menu.

2. Add a list of supported resolutions in the main menu. Detect the current
   screen resolution and set it as the default.

3. Add separate volume controls for sound effects and background music in
   main menu and main scene.

4. Add a Dashboard tab and show the basic info of all settlers (experimental).

5. Incorporate certain ui elements from weblaf for improving the looks of
   some swing components.


### FIXES :

1. Reduce building temperature fluctuation.

2. Fix bugs in solar/fuel/electric heating.

3. Fix settlers being under-weight and short-height.

4. Correct issues with storing resources after loading from a saved sim.

5. Redefine stock capacity for each building to avoid space shortage.

6. Fix the tab choice combobox of an unit window from popping near the edge
   of the screen.

7. Correct botanists' low participation of of inspecting/cleaning/
   sampling tissues in TendGreenhouse.

9. Set botanists' chance of sampling crop tissues to 50% if there's time
   remaining after tending a greenhouse.

10. Fix user initiated construction mission.

11. Fix various bugs during emergency rescue missions.


-----------------------------------------------------------------------------------------

## v3.1.0-Preview 8 (build 4082) - 2017-08-10


### KNOWN LIMITATIONS :

1. In MacOS, JavaFX WebEngine's bug creates garbled characters in Help Browser.

2. In Linux, textfields do not allow text input.

3. Not compatible with 32-bit Java 8.


### CORE ENGINE IMPROVEMENTS :

1. Refine phenological/growing stage for each category of crop.

2. Disallow growing corn consecutively in the same greenhouse as it depletes
   nitrogen in the soil more quickly than other crops.

3. Rename Green Onion to Spring Onion. Remove Spices as a Crop Category.


### UI IMPROVEMENT :

1. Switch to using only JavaFX UI elements on Help Browser.

2. Add animated Main Menu items.

3. Create an EXE installation package specifically for installing mars-sim in
   Windows OS with Inno Script Studio.


### FIXES :

1. Correct crop harvesting issues.

2. Fix crash if dragging the globe in the Minimap too fast.

3. Temporarily disable the "Edit Mission" and "Abort Mission" Buttons in Mission
   Tool.


-----------------------------------------------------------------------------------------

## v3.1.0-Preview 7 (build 3973) - 2017-05-08

### MINIMUM REQUIREMENTS :

1. Dual Core Pentium/Celeron 1.5GHz or above

2. 500 MB to 1.5 GB free RAM dedicated for running mars-sim

3. 64-bit Oracle Java 8 (JRE or JDK 8u77 and higher) OR OpenJDK 8u77 with OpenJFX


### KNOWN LIMITATIONS :

1. In MacOS, the Tab bar does NOT work and will freeze up when clicking it--
   recommend using MacOS's style top menu to gain access to each Tool. Known JavaFX
   WebEngine bug in displaying certain fonts in Help Browser.

2. In Linux, the text fields does not allow text input.

3. Not compatible with 32-bit Java 8.


### CORE ENGINE IMPROVEMENTS :

1. Improve meal modeling and add new meals and crops (taro, corn, white mustard).
   see project #5 at https://github.com/mars-sim/mars-sim/projects/5

2. Add arbitrary genetic factor and gender correlated curves for computing weight and
   height of the settlers.

3. Rework the minimum and maximum value of time ratio and ticks per sec.

4. Implement multi-phase water rationing approach if water reserve is below 20% of the
   settlement's projected usage.

5. Initiate the tasks of digging for ice/regolith locally instead of going out on a
   mission of collecting ice/regolith.

6. Increase quantity of resources (O2, H2O, etc.) and # of spare parts to be carried on
   a mission in case of accidents and malfunctions.


### UI IMPROVEMENT :

1. Adopt spinner for selecting the time ratio in Speed Panel.

2. Add showing the Martian season (for Northern/Southern Hemisphere) and solar longitude
   in Mars Calendar Panel.

3. Refine death-related info display.

4. Add fast tooltips for Mars Calendar Panel and Speed panel.

5. Accept the use of comma (in addition to the default case of using dot) as the decimal
   mark when inputting lat/lon in the Config Editor.

6. Switch to using an unobtrusive, transparent pause pane when sim is paused.

7. Add url history in Help Browser.

8. Display autosaving indicator when autosaving timer is triggered.


### FIXES :

1. Fix the suffocation bug loading a saved sim.

2. Fix NullPointerException when starting a sim with a Mining/Trading Outpost.

3. Correct ParseException due to non-US system locale when loading simulation.xml and
   generating settlers' date of birth

4. Further remove calling Repairbots to go outside to fix vehicles. Exclude sending
   Gardenbots to Trading Outpost & Mining Outpost at the start of sim.

5. Remove external fonts that cause startup crash if using openJDK/openJFX in linux.

6. Fix errors when creating brand new Resupply Missions.

7. Fix quality of dessert not showing up correctly.

8. Temporarily disable Transport/Construction Wizard and adopt automated building/site placement.

9. Prevent Mars mini-globe from crashing and provide reset button.

10. Fix the "trapped" vehicle occupants (held up by the finished mission) and release
    them back to their settlement.

11. Remove the shadow artifact on the surface of the spinning Mars Globe in Linux.

-----------------------------------------------------------------------------------------

## v3.1.0-Preview 6 (build 3899) - 2017-03-22

### MINIMUM REQUIREMENTS :

1. Dual Core Pentium/Celeron 1.5GHz or above

2. 500 MB to 1.5 GB free RAM dedicated for running mars-sim

3. 64-bit Oracle Java 8 (JRE or JDK 8u71 and higher) OR OpenJDK 8u71 with OpenJFX


### KNOWN LIMITATIONS :

1. In MacOS, the Tab bar does NOT work and will freeze mars-sim if clicking on it.
   Recommend using MacOS's style top menu to gain access to each Tool.

2. In Linux, the spinning 3D Mars Globe has unwanted black shadow artifact.

3. Not compatible with 32-bit Java 8.


### CORE ENGINE IMPROVEMENTS :

1. Add lists of first & last names for each 22 countries of European Space Agency (ESA).

2. Add combo boxes for choosing country and sponsorship for each member of the Alpha
   Crew in Crew Editor.

3. Add validation and partial correction of the textfield input of settler, bot,
   latitude and longitude in config editor.

4. Reduce memory leak by caching more objects and icons.


### UI IMPROVEMENT :

1. Add sound control panel, calendar panel and speed panel.

2. Add map toggle button and minimap toggle button.

3. Consolidate various tools into the Main tabs. Reduce the # of available tabs to 3.


### FIXES :

1. Fix a bug preventing a new settlement from showing up in the Settlement combo box
   in Settlement Map.

2. Fix displaying the most current activity description/phase and mission description
   /phase in Activity Tab in Person Window.

3. Fix crash in linux due to bugs in nimrod L&F theme by using only nimbus L&F.

4. Fix settler suffocation bug when loading a saved sim.

5. Fix popup display in Settlement Map in linux.

------------------------------------------------------

## v3.1.0 (build 3837) Snapshot Preview 5 - 2017-01-10


### KNOWN LIMITATIONS :

1. The Graphic Mode cannot run under MacOSX due to some unknown bugs when loading jfoenix's JFXTabPane. However, it can still run under Headless Mode in MacOSX.

2. Main Menu's 3D Mars Globe may display shadow artifact when running in linux (due to unknown recent JavaFX changes)

3. User must have 64-bit Java 8 SE or JDK (at least 1.8.0.60 version) installed. Currently, it's not compatible with 32-bit Java 8 or any versions of OpenJDK.



### CORE ENGINE IMPROVEMENTS :

1. Scale cpu utilization according to the available number of threads in the CPU.

- Slower CPUs will have lower pulse per seconds and time ratio.


### UI IMPROVEMENT :

1. Minor rework on the display in Time Tool.


### FIXES :

1. Enable lower clock speed cpu such as Dual Core Pentium/Celeron to run mars-sim.

2. Prevent mars-sim from attempting to run in any java VM version below 1.8.0.60.

------------------------------------------------------

## v3.1.0 (build 3817) Snapshot Preview 4 - 2016-12-05


### KNOWN LIMITATIONS :

1. The Graphic Mode cannot run under MacOSX due to some unknown bugs. However, it can still run under Headless Mode in MacOSX.

2. The Main Menu's 3D Mars Globe has shadow artifact when running in linux (due to unknown recent Java JDK changes)



### NEW FEATURES :


A. New Modeling

1. Improve the depth of personality modeling by implementing the Five Factor Model, alongside with the existing Myer-Brigg Type Indicator (MBTI). Display personality using bullet charts and gauges

2. Add new battery charging/discharging model for the power grid batteries that store unused/excess power for future use.

3. Add wash water rationing. Activate rationing if water stored at a settlement is less than 10% of their yearly drinking water needs.

4. Add the ability to tweak how much fuel and life support consumable to bring for each mission via an element/attribute in xml.


B. New UI elements

1. Add a tab bar on top and segregate most Tools into their own tabs.

2. Add Earth and Mars Date/Time bar anchored near top center.

3. The 'Mars Navigator Tool' is renamed 'Navigator Minimap' and now has two maps stacked up on top of each other.

4. Both the Settlement Map and the Navigator Minimap are now displayed inside Map tab. They can be turned on at the click of their buttons on top right.

5. Add bullet bar and gauges for displaying a person's personalities (for both MBTI and Big Five Models)

6. Add the use of keyboard shortcuts (see /docs/help/shortcuts.html at Help Browser's User Guide under User Interface).


### CORE ENGINE IMPROVEMENTS:

1. Optimize numerous method calls for less cpu utilization.

2. Re-enable doing various scientific research tasks while inside a moving/non-moving vehicle (albeit with penalty).

3. Re-implement validation of all table cells in simulation configuration editor.

4. Mute/unmute the background music when the game is paused/resume.

5. Implement how choosing the settlement's objective would affect the VP (and production) of related goods.

6. Re-implement validation of settlements' name, population and # of bots entries in configuration editor.

7. Re-implement validation of latitude and longitude in the configuration editor.

8. Improve the Good Value Points (VS) updates for waste water, cooking consumables and others.

9. Add supplies of black water, grey water and regolith at the beginning of a new sim.



## UI IMPROVEMENT

1. Sync up UI updates better in each frame and with better multi-threading.

2. Remove top menu bar. Remove status bar.

3. Replace the old swing UI elements with new JavaFX UI in Settlement Map.




### FIXES :

1. Lessen the likelihood of bots moving frequently from one building to another.

2. Remove duplicate calling of building update in each frame.

3. Fix tracking the whereabout of vehicles in the Location tab in the Vehicle Window.

4. Fix the electric lighting calculation for greenhouse operation.

5. Fix width and height determination of the quotation popup under different screen resolution. Add a scrollbar and text autowrapping.

6. Fix the latest URL not being displayed correctly in the address bar of Help Browswer.

7. Resolve a major linux UI issue. Can now switch between the Orange and Blue themes just like in Windows OS.



------------------------------------------------------

## v3.1.0 (build 3743) Snapshot Preview 3 - 2016-10-13

### NEW FEATURES :

1. Added user's option to change the height of the MarsNet chat box with the command '/y1', '/y2', '/y3', and '/y4' to suit the resolution of user's monitor.
/y1 --> 256 pixels (by default)
/y2 --> 512 pixels
/y3 --> 768 pixels
/y4 --> 1024 pixels

2. Added new user's keyword "settlement"/"vehicle"/"rover" in the chat box for reporting # of settlements in total and their names and to query the total # and types of vehicles/rovers (parked and/or on mission).

3. Added animated loading/saving/paused indicators.



### FIXES :

1. Fixed volume control with the ability to mute both the background music and sound effects.

2. Fixed NullPointerException/other bugs when loading a saved sim in either the Main Menu or the command prompt.

3. Enabled inline web browser to load quicker.

4. Fixed tracking the availability and usage of lighting for illuminating the food crop in greenhouses



### IMPROVEMENTS:

1. Refactored the meteorite impact calculation in MeteoriteImpactImpl.java and completed Part I and II below. (Part III still in-work).

Note: any observable impact will increase the stress level of the settlers on that building if his/her natural attribute of courage and emotional stability are not high enough.

Part I : Calculate the probability of impact per square meter per sol on the settlement, assuming the meteorite has an average impact velocity of 1km/s, critical diameter of .0016 cm and average density of 1 g/cm^3, per NASA study.

Part II: Calculate how far the incoming meteorite may penetrate the wall of a building

Part III: Implement equations of the probability distribution of different sizes of the meteorites. This no longer assumes the size and impact speed of the meteorites are homogeneous as in Part I and II.

Source 1: Inflatable Transparent Structures for Mars Greenhouse Applications 2005-01-2846. SAE International.
http://data.spaceappschallenge.org/ICES.pdf

Source 2: 1963 NASA Technical Note D-1463 Meteoroid Hazard
http://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19630002110.pdf


2. Minimized or prevented EVA operations during intense Galactic Cosmic Rays (GCR) and Solar Energetic Particles (SEP) events for reducing radiation exposure. Refactored probability of exposure to conform better to NASA study.

See Curiosity's Radiation Assessment Detector (RAD) http://www.boulder.swri.edu/~hassler/rad/
http://www.swri.org/3pubs/ttoday/Winter13/pdfs/MarsRadiation.pdf
http://www.mars-one.com/faq/health-and-ethics/how-much-radiation-will-the-settlers-be-exposed-to



------------------------------------------------------

## v3.1.0 (build 3729) Snapshot Preview 2 - 2016-09-27

### NEW FEATURES :

1. Added codes to abort an on-going vehicle mission without activating the emergency beacon. Vehicle will drive to the settlement if fuel and consumables are still available.

2. Optimized the stress modifier on a few AI tasks.

3. Added more first and last names (of various national origin) in people.xml for settlers from various sponsoring space agencies.

4. Added new part "heat pipe" and used it throughout the sim.


### FIXES :

1. Temporarily disabled the ability for settlers to start a construction mission on his own (bug fixes still in-work).

2. Removed some case of a NullPointerException during a resupply mission and a building construction mission.

3. Fixed inability to save as default.sim and as other file names.

4. Fixed NullPointerException in Career Tab of a deceased person when opening the Person Window.

5. Fixed NullPointerException in Location Tab of a vehicle in some cases when opening the Vehicle Window.



### IMPROVEMENTS/OTHERS :

1. Added Description, Type and Phase of an on-going mission to the top right of the panel in Mission Tool.

2. Limited the # of Sols activities to be saved to 100 Sols.

3. Improved linux compatibility in displaying UI.

4. Prevented settlers from having duplicated first and last names in a settlement. The chance of this happening in the real world is remotely possible. Therefore, we preclude it from happening in mars-sim.

5. Shrunk the size of the binaries by removing unused contents.

6. Revised files for the debian package, which can be installed via GDebi (Debian Package Installer) in linux.



------------------------------------------------------

## v3.1.0 (build 3713) Snapshot Preview 1 - 2016-08-27


### NEW FEATURES :

1. Added several new crops.

2. Added several new meals.

3. Added new tasks (PlayHoloGame, WriteReport, ReviewJobAssignment, HaveConversation, Read, ListenToMusic)

4. Added new items/resources.

5. Added the use of 3-D printing for manufacturing building.

6. Added preferences and favorites for each settler.

7. Added new crop categories.

8. Added unique crop growth phases for a few crop categories.

9. Added a list of space agencies sponsoring each settlement.

10. Added lists of autogenerated last and first names for each sponsoring space agency.

11. Added job performance rating.

12. Added work shift (either X,Y,Z or A,B).

13. Added history of job changes/reassignment.

14. Added the use of robots for certain repetitive/programmable tasks.

15. Added tracking atmospheric gases inside settlement (i.e. the new "Air" tab in the Settlement Window).

16. Added the new Manager job.

17. Added the new concept of "role" for each settler.

18. Added a hierachical structure in each settlement and assigned a role to each settler.

19. Added radiation exposure tracking for each settler (in the existing "Health" tab).

20. Designated bed/quarters for each settler.

21. Implemented sleep hour habit for each settler.

22. Added the use of artificial lighting in greenhouse operations.

23. Added new medical issues.

24. Added new malfunction events.

25. Added new energy level in kJ (as a health metric) for each settler.



### UI CHANGES :

1. Added detection of the 2nd monitor/LCD.

2. Added a chat box called MarsNet for primarily tracking the location/activity of settlers/bots.

3. Added search capability in the Monitor Tool.

4. Implemented day/night transition in the Settlement Map and the Mars Navigator.

5. Provided two theme skins : blue snow and orange nimrod themes.

6. Added macOSX top menu bar integration.

7. Added a Main Menu screen at the start of the sim with a spinning Mars globe as background.

8. Added floating buttons (one for hiding/unhiding of the pull down menu.

9. Added hiding of the pull down menu.

10. Added Construction Wizard for placement of construction sites.

11. Better readibility of inline help pages (now partially html5 compliant).

12. Better notification popups for malfunctions.

13. Gradual replacement of swing UI components with JavaFX UI.

14. Reworked Simulation Configuration Editor for choosing one sponsoring space agency for each settlement.


### FIXES/IMPROVEMENTS/OTHER CHANGES :

1. Fixed temperature fluctuation with better algorithm for HVAC calculation.

2. Added placement of buildings for Transport Wizard in the Resupply Tool.

3. Can choose settlement in Crew Editor.

4. Supported multiple event notification popups. Can be stacked on top of each other. Effect of fading in and out effect.

5. Further Refined crop modeling algorithm in greenhouse operation.

6. Fixed water consumption and waste water generation bug.

7. Fixed resource name upper/lower case mismatch bug.

8. Converted sound track and effects to ogg format.

9. Fixed various walking bugs.

10. Doubled the size of Mars Globe in Mars Navigator.

11. Refined weather modeling and improved sunlight calculation.

12. More internationalization.

13. Improved multithreading.

14. Applied LZMA2 compression and reduced the size of saved sim by 5-10 times.

