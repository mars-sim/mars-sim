/*
 * Mars Simulation Project
 * Resupply.java
 * @date 2025-09-02
 * @author Scott Davis
 */
package com.mars_sim.core.interplanetary.transport.resupply;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.activities.GroupActivity;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingConfig;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.BuildingSpec;
import com.mars_sim.core.building.BuildingTemplate;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.events.ScheduledEventManager;
import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.GroupActivityType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.SettlementBuilder;
import com.mars_sim.core.structure.SettlementSupplies;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Resupply mission from Earth for a settlement.
 */
public class Resupply extends Transportable implements SettlementSupplies {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Resupply.class.getName());
	
	private static final String POSITIONING = "Positioning ";
	
	// Default separation distance between the outer wall of buildings .
	private static final int MAX_HABITABLE_BUILDING_DISTANCE = 6;
	private static final int MIN_HABITABLE_BUILDING_DISTANCE = 2;

	private static final int MAX_INHABITABLE_BUILDING_DISTANCE = 6;
	private static final int MIN_INHABITABLE_BUILDING_DISTANCE = 2;

	private static final int MAX_OBSERVATORY_BUILDING_DISTANCE = 48;
	private static final int MIN_OBSERVATORY_BUILDING_DISTANCE = 36;

	private static final int BUILDING_CENTER_SEPARATION = 11; // why 11?

	public static final int MAX_COUNTDOWN = 20;

	// Default width and length for variable size buildings if not otherwise
	// determined.
	private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = 9D;
	private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = 9D;

	/** Minimum length of a building connector (meters). */
	private static final double MINIMUM_CONNECTOR_LENGTH = 1D;

	// Data members
	private int newImmigrantNum;
	private int newBotNum;
	private int settlementID;
	private transient Settlement settlement;
	
	private ResupplySchedule template;
	
	private List<BuildingTemplate> newBuildings;
	private Map<String, Integer> newVehicles;
	private Map<String, Integer> newEquipment;
	private Map<String, Integer> newBins;
	private Map<AmountResource, Double> newResources;
	private Map<Part, Integer> newParts;

	private int cycle;
	
	/**
	 * Constructor based of a Supply schedule.
	 * 
	 * @param template The schedule of the resupply
	 * @param arrivalDate the arrival date of the supplies.
	 * @param settlement  the settlement receiving the supplies.
	 */
	public Resupply(ResupplySchedule template, int cycle, MarsTime arrivalDate, Settlement settlement) {
		this(template.getName() + " #" + cycle, arrivalDate, settlement);

		// Initialize data members.
		this.template = template;
		this.cycle = cycle;

		// Load up the respply according to the manifest
		ResupplyManifest manifest = template.getManifest();
		var supplies = manifest.getSupplies();
		setBuildings(supplies.getBuildings());
		setVehicles(supplies.getVehicles());
		setEquipment(supplies.getEquipment());
		setBins(supplies.getBins());
		setNewImmigrantNum(manifest.getPeople());
		setResources(supplies.getResources());
		setParts(supplies.getParts());
	}

	/**
	 * Bespoke resupply mission.
	 */
	public Resupply(String name, MarsTime arrivalDate, Settlement destination) {
		super(name, destination.getCoordinates());

		this.settlement = destination;
		settlementID = destination.getIdentifier();

		// Schedule the launch
		setArrivalDate(arrivalDate);
	}

	/**
	 * The parent settlement is controlling the events for the resupply mission.
	 * 
	 * @return Settlement's scheduled events
	 */
	@Override
	protected ScheduledEventManager getOwningManager() {
		return settlement.getFutureManager();
	}

	/**
	 * Gets the schedule that defines this resupply.
	 * 
	 * @return Potentially can return null.
	 */
	public ResupplySchedule getTemplate() {
		return template;
	}

	/**
	 * Generates the delivery event.
	 */
	@Override
	public synchronized void performArrival(SimulationConfig sc, Simulation sim) {
		// Deliver buildings to the destination settlement.
		logger.info(this, "Preparing for the arrival of a resupply mission.");

		// Deliver buildings to the destination settlement.
		boolean hasBuildings = deliverBuildings(sc.getBuildingConfiguration());
		
		if (hasBuildings) {
			// Interrupts everyone's task (Walking tasks can cause issues) 
			settlement.endAllIndoorTasks();
		}
		
		// Deliver the rest of the supplies and add people.
		deliverOthers(sim, sc);

		// If there are new immigrients then have a welcome meeting
		if (newImmigrantNum > 0) {
			GroupActivity.createPersonActivity("Welcome new arrivals of " + getName(),
									GroupActivityType.ANNOUNCEMENT, settlement, null, 1, 
									sim.getMasterClock().getMarsTime());
		}

		// If a schedule then create the next one
		int frequency = template.getSchedule().getFrequency();
		if (frequency > 0) {
			// Scheduled the follow on
			MarsTime newArrival = getArrivalDate().addTime(template.getActiveMissions() * frequency * 1000.0);
			Resupply followOn = new Resupply(this.getTemplate(), cycle + template.getActiveMissions(),
												newArrival, settlement);
			tm.addNewTransportItem(followOn);
		}
	}

	/**
	 * Delivers new buildings to the settlement.
	 * 
	 * @return
	 */
	private boolean deliverBuildings(BuildingConfig buildingConfig) {
		List<BuildingTemplate> orderedBuildings = orderNewBuildings(buildingConfig);

		if (!orderedBuildings.isEmpty()) {

			BuildingManager buildingManager = settlement.getBuildingManager();

			settlement.fireUnitUpdate(UnitEventType.START_BUILDING_PLACEMENT_EVENT, buildingManager.getABuilding());

			Iterator<BuildingTemplate> buildingI = orderedBuildings.iterator();

			while (buildingI.hasNext()) {
				BuildingTemplate btemplate = buildingI.next();

				// Correct length and width in building template.
				// Replace width and length defaults to deal with variable width and length
				// buildings.
				BuildingSpec spec = buildingConfig.getBuildingSpec(btemplate.getBuildingType());
				BoundedObject correctedBounds = getCorrectedBounds(spec, btemplate.getBounds());

				String buildingID = "" + buildingManager.getNextTemplateID();
				
				int zone = btemplate.getZone();
				
				String uniqueName = buildingManager.getUniqueName(btemplate.getBuildingType());

				BuildingTemplate correctedTemplate = new BuildingTemplate(buildingID, zone,
						btemplate.getBuildingType(), uniqueName, correctedBounds);

				checkTemplateAddBuilding(spec, correctedTemplate, buildingManager);
			}
			
			return true;
		}
		
		return false;
	}

	/**
	 * Corrects the width and length of a bounded object.
	 * Note: it doesn't correct its xloc and yloc.
	 * 
	 * @param spec
	 * @param bounds
	 * @return
	 */
	private static BoundedObject getCorrectedBounds(BuildingSpec spec, BoundedObject bounds) {
		double width = spec.getWidth();

		if (bounds.getWidth() > 0D) {
			width = bounds.getWidth();
		}
		if (width <= 0D) {
			width = DEFAULT_VARIABLE_BUILDING_WIDTH;
		}

		double length = spec.getLength();
		if (bounds.getLength() > 0D) {
			length = bounds.getLength();
		}
		if (length <= 0D) {
			length = DEFAULT_VARIABLE_BUILDING_LENGTH;
		}
		
		return new BoundedObject(bounds.getPosition(), width, length, bounds.getFacing());
	}

	/**
	 * Checks for collision with existing buildings/vehicles/construction sites and
	 * creates the building based on the template to the settlement
	 * 
	 * @param bt a building template
	 * @param bc 
	 */
	public static void checkTemplateAddBuilding(BuildingSpec spec, BuildingTemplate bt, BuildingManager buildingManager) {
		// Check if building template position/facing collides with any existing
		// buildings/vehicles/construction sites.
		if (!isTemplatePositionClear(spec, bt, buildingManager)) {
			bt = clearCollision(spec, bt, MAX_COUNTDOWN, buildingManager);
		}

		if (bt != null) {
			buildingManager.addBuilding(
					Building.createBuilding(bt, buildingManager.getSettlement()), bt, true);
		}
	}

	/**
	 * Identifies the type of collision and gets new template if the collision is
	 * immovable.
	 * 
	 * @param bt    a building template
	 * @param count number of counts
	 * @param bc 
	 * @param spec 
	 * @param buildingManager
	 * @return corrected building template
	 */
	public static BuildingTemplate clearCollision(BuildingSpec spec, BuildingTemplate bt, int count, BuildingManager buildingManager) {
		count--;
		logger.config("#" + (Resupply.MAX_COUNTDOWN - count) + " : Calling clearCollision() for " + bt.getBuildingName());
	
		boolean noVehicle = true;
		boolean noImmovable = true;
		boolean noConflictResupply = true;
		boolean inZone = true;
		if (count < 1) {
			return null;
		} else {
			// check if a vehicle is the obstacle and move it
			noVehicle = isCollisionFreeVehicle(bt, buildingManager.getSettlement());

			if (noVehicle) {
				noImmovable = isCollisionFreeImmovable(bt, buildingManager.getSettlement());
			}

			if (noConflictResupply) {
				inZone = isWithinZone(spec, bt, buildingManager);
			}

			if (!noImmovable || !noConflictResupply || !inZone) {// if there are obstacles
				// get a new template
				BuildingTemplate repositioned = positionNewResupplyBuilding(spec, buildingManager);

				// Call again recursively to check for any collision
				bt = clearCollision(spec, repositioned, count, buildingManager);
			}
		}

		return bt;
	}

	/**
	 * Checks for collision and relocate any vehicles if found.
	 * 
	 * @param xLoc
	 * @param yLoc
	 * @param coordinates
	 * @return true if the location is clear of collision
	 */
	private static boolean isCollisionFreeVehicle(BuildingTemplate t, Settlement settlement) {
		return !LocalAreaUtil.isVehicleBoundedOjectIntersected(t.getBounds(),
				settlement.getCoordinates(), true);

	}

	/**
	 * Checks for collision for an immovable object.
	 * 
	 * @param t a building template
	 * @return true if no collision.
	 */
	private static boolean isCollisionFreeImmovable(BuildingTemplate t, Settlement settlement) {

		return !LocalAreaUtil.isImmovableBoundedOjectIntersected(t.getBounds(), 
				settlement.getCoordinates());
	}

	/**
	 * Checks if the building template is outside min radius and within max radius.
	 * 
	 * @param bt the building template
	 * @return true if it's within the prescribed zone
	 */
	private static boolean isWithinZone(BuildingSpec spec, BuildingTemplate bt, BuildingManager mgr) {

		boolean withinRadius = true;
		int leastDistance = 0;
		// TOD: also check if
		Set<FunctionType> supported = spec.getFunctionSupported();
		boolean hasLifeSupport = !spec.isInhabitable();
		if (hasLifeSupport) {

			if (supported.contains(FunctionType.ASTRONOMICAL_OBSERVATION)) {
				leastDistance = MIN_OBSERVATORY_BUILDING_DISTANCE;
			} else {
				leastDistance = MIN_INHABITABLE_BUILDING_DISTANCE;
			}
		}

		else {
			leastDistance = MIN_INHABITABLE_BUILDING_DISTANCE;
		}

		List<Building> list = mgr.getBuildings(FunctionType.LIFE_SUPPORT);
		Collections.shuffle(list);

		Iterator<Building> i = list.iterator();
		while (i.hasNext()) {
			Building startingBuilding = i.next();
			double distance = startingBuilding.getPosition().getDistanceTo(bt.getBounds().getPosition());

			if (distance < leastDistance) {
				withinRadius = false;
				break;
			}
		}

		return withinRadius;
	}

	/**
	 * Delivers vehicles, resources, bots and immigrants to a settlement on a resupply
	 * mission.
	 * 
	 * @param timings 
	 */
	private void deliverOthers(Simulation sim, SimulationConfig sc) {
		SettlementBuilder builder = new SettlementBuilder(sim, sc);
		
		builder.createSupplies(this, settlement);

		builder.createRobots(settlement, settlement.getAllAssociatedRobots().size() + getNewBotNum());
		
		builder.createPeople(settlement, settlement.getNumCitizens() + getNewImmigrantNum(), true);

	}

	/**
	 * Orders the new buildings with non-connector buildings first and connector
	 * buildings last.
	 * 
	 * @param buildingConfig 
	 * 
	 * @return list of new buildings.
	 */
	private List<BuildingTemplate> orderNewBuildings(BuildingConfig buildingConfig) {
		List<BuildingTemplate> result = new ArrayList<>();
		
		List<BuildingTemplate> list = getBuildings().stream()
				.sorted(Comparator.comparing(bt -> bt.getID()))
				.collect(Collectors.toList());
		
		Iterator<BuildingTemplate> i = list.iterator();
		while (i.hasNext()) {
			BuildingTemplate b = i.next();
			boolean isBuildingConnector = buildingConfig.hasFunction(b.getBuildingType(),
																	 FunctionType.CONNECTION);
			if (isBuildingConnector) {
				// Add hallway and tunnel to end of new building list.
				result.add(b);
			} else {
				// Add non-connector to beginning of new building list.
				result.add(0, b);
			}
		}

		return result;
	}

	/**
	 * Checks if a building template's position is clear of collisions with any
	 * existing structures.
	 * 
	 * @param template the building template.
	 * @param buildingConfig 
	 * @return true if building template position is clear.
	 */
	public static boolean isTemplatePositionClear(BuildingSpec spec, BuildingTemplate template, 
			BuildingManager buildingManager) {

		boolean result = true;
		
		// Replace width and length defaults to deal with variable width and length
		// buildings.
		BoundedObject correctedBounds = getCorrectedBounds(spec, template.getBounds());

		result = buildingManager.isBuildingLocationOpen(correctedBounds);

		return result;
	}

	/**
	 * Determines and sets the position of a new resupply building.
	 * @param buildingConfig 
	 * 
	 * @param building type the new building type.
	 * @return the repositioned building template.
	 */
	private static BuildingTemplate positionNewResupplyBuilding(BuildingSpec spec, BuildingManager buildingManager) {

		BuildingTemplate newPosition = null;
		
		// Note : only hallway and tunnel has "connection" function
		Set<FunctionType> supported = spec.getFunctionSupported();
		boolean isBuildingConnector = supported.contains(FunctionType.CONNECTION);
		boolean hasLifeSupport = !spec.isInhabitable();

		if (isBuildingConnector) {
			// Case 0 : a hallway or tunnel
			// Try to find best location to connect between the two buildings.
			newPosition = positionNewConnector(spec, buildingManager);
			if (newPosition != null) {
				logger.config("Case 0: it is a hallway or tunnel.");
				return newPosition;
			}
		}

		else if (hasLifeSupport) {
			// Case 1 : same type and with life support
			newPosition = positionSameType(spec, true, buildingManager);

			if (newPosition != null) {
				logger.config("Case 1: " + POSITIONING + " near the same building type ("
							+ newPosition.getBuildingName()
							+ ") having life support.");
				return newPosition;
			} else {
				// Case 2 : not the same type but with life support
	
				// Put this habitable building next to another inhabitable building (e.g.
				// greenhouse, lander hab, research hab...)
				List<Building> inhabitableBuildings = buildingManager
						.getBuildings(FunctionType.LIFE_SUPPORT);
				Collections.shuffle(inhabitableBuildings);
				Iterator<Building> i = inhabitableBuildings.iterator();
				while (i.hasNext()) {
					Building building = i.next();
					// Note: Don't want to place any building next to the observatory
					double dist1 = 0;
					if (supported.contains(FunctionType.ASTRONOMICAL_OBSERVATION)) {
						dist1 = RandomUtil.getRandomRegressionInteger(MIN_OBSERVATORY_BUILDING_DISTANCE * 2,
								MAX_OBSERVATORY_BUILDING_DISTANCE * 2) / 2D;
					} else {
						dist1 = RandomUtil.getRandomRegressionInteger(MIN_INHABITABLE_BUILDING_DISTANCE * 2,
								MAX_INHABITABLE_BUILDING_DISTANCE * 2) / 2D;
					}

					newPosition = positionNextToBuilding(spec, buildingManager, building, Math.round(dist1), false);
					if (newPosition != null) {
						logger.config("Case 2: " + POSITIONING 
								+ " near a different building type (" 
								+ building.getBuildingType() + ") having life support.");
						return newPosition;
					}
				}
			}
		} else {
			// Case 3 : the same type but having no life support
			newPosition = positionSameType(spec, false, buildingManager);
			if (newPosition != null) {
				logger.config("Case 3: " + POSITIONING + newPosition.getBuildingName()
						+ " near the same building type with no life support");
				return newPosition;
			}
		}

		if (newPosition == null) {
			// Case 4 : no life support
			// Put this non-habitable building next to a different type building.
			// If not successful, try again 10m from each building and continue out at 10m
			// increments
			// until a location is found.
			if (buildingManager.getNumBuildings() > 0) {
				for (int x = BUILDING_CENTER_SEPARATION; newPosition == null; x = x + 2) {
					Iterator<Building> i = buildingManager.getBuildingSet().iterator();
					while (i.hasNext()) {
						Building building = i.next();
						newPosition = positionNextToBuilding(spec, buildingManager, building, x, false);
						if (newPosition != null) {
							logger.config("Case 4: " + POSITIONING + newPosition.getBuildingName() + " at " + x
									+ " meters away near a different building type with no life support");
							return newPosition;
						}
					}
				}
			} else {
				// Case 5 :
				// Replace width and length defaults to deal with variable width and length
				// buildings.
				double width = spec.getWidth();
				if (width <= 0D) {
					width = DEFAULT_VARIABLE_BUILDING_WIDTH;
				}
				double length = spec.getLength();
				if (length <= 0D) {
					length = DEFAULT_VARIABLE_BUILDING_LENGTH;
				}

				// If no buildings at settlement, position new building at (0, 0) with random
				// facing.
				// Note: check to make sure it does not overlap another building.
				String buildingID = "" + buildingManager.getNextTemplateID();
				
				int zone = 0;
				
				String uniqueName = buildingManager.getUniqueName(spec.getName());
				// Note : ask for user to define the location for the new building as well
				newPosition = new BuildingTemplate(buildingID, zone,
						spec.getName(), uniqueName, new BoundedObject(0,  0, width, length, 0));

				logger.config("Case 5: " + POSITIONING + uniqueName + " at (0, 0)");
				return newPosition;
			}
		}

		return newPosition;
	}

	/**
	 * Positions a building template near a building of the same type.
	 * 
	 * @param buildingType
	 * @param lifeSupport
	 * @return
	 */
	private static BuildingTemplate positionSameType(BuildingSpec buildingSpec , boolean lifeSupport, BuildingManager buildingManager) {
		BuildingTemplate newPosition = null;

		// Put this non-habitable building next to the same building type.
		List<Building> sameTypeBuildings = buildingManager
				.getBuildingsOfSameType(buildingSpec.getName());
		
		BuildingCategory buildingCategory = buildingSpec.getCategory();
		
		Set<Building> sameCategoryBuildings = buildingManager
				.getBuildingsOfSameCategory(buildingCategory);
		
		sameTypeBuildings.addAll(sameCategoryBuildings);
		   
		sameTypeBuildings = sameTypeBuildings.stream()
				// Do not look for EVA Airlock building type since putting 
			    // any new building next to it will block ingress/egress.
				.filter(b -> b.getCategory() != BuildingCategory.EVA
				// Do not look for Garage building type as it may block the entry
				// and placement of vehicle
				&& b.getCategory() != BuildingCategory.VEHICLE)
				.collect(Collectors.toList());
		
		Collections.shuffle(sameTypeBuildings);
		Iterator<Building> j = sameTypeBuildings.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			// Note: Don't want to place any building next to the observatory
			double dist2 = 0;
			if (lifeSupport) {
				dist2 = RandomUtil.getRandomRegressionInteger(MIN_HABITABLE_BUILDING_DISTANCE * 2,
						MAX_HABITABLE_BUILDING_DISTANCE * 2) / 2D;
			}
			else
				dist2 = RandomUtil.getRandomRegressionInteger(MIN_INHABITABLE_BUILDING_DISTANCE * 2,
						MAX_INHABITABLE_BUILDING_DISTANCE * 2) / 2D;
			
			newPosition = positionNextToBuilding(buildingSpec, buildingManager, building, Math.round(dist2), false);
			if (newPosition != null) {
				break;
			}
		}
		return newPosition;
	}

	/**
	 * Determines the position and length (for variable length) of a new building
	 * connector building.
	 * 
	 * @param newBuildingType the new building type.
	 * @return new building template with position/length, or null if none found.
	 */
	private static BuildingTemplate positionNewConnector(BuildingSpec spec, BuildingManager buildingManager) {
		
		BuildingTemplate newTemplate = null;

		int baseLevel = spec.getBaseLevel();
		List<Building> inhabitableBuildings = buildingManager.getBuildings(FunctionType.LIFE_SUPPORT);
		Collections.shuffle(inhabitableBuildings);

		// Case 1
		// Try to find a connection between an inhabitable building without access to
		// airlock and
		// another inhabitable building with access to an airlock.
		if (buildingManager.getSettlement().getAirlockNum() > 0) {
			logger.config("Case 1 in positionNewConnector()");
			Building closestStartingBuilding = null;
			Building closestEndingBuilding = null;
			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> i = inhabitableBuildings.iterator();
			while (i.hasNext()) {
				Building startingBuilding = i.next();
				if (!buildingManager.getSettlement().hasWalkableAvailableAirlock(startingBuilding)) {
					// Find a different inhabitable building that has walkable access to an airlock.
					Iterator<Building> k = inhabitableBuildings.iterator();
					while (k.hasNext()) {
						Building building = k.next();
						if (!building.equals(startingBuilding)) {
							// Check if connector base level matches either building.
							boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
									|| (baseLevel == building.getBaseLevel());

							if (buildingManager.getSettlement().hasWalkableAvailableAirlock(building) && matchingBaseLevel) {
								double distance = Point2D.distance(startingBuilding.getXLocation(),
										startingBuilding.getYLocation(), building.getXLocation(),
										building.getYLocation());
								if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {
									// Check that new building can be placed between the two buildings.
									if (positionConnectorBetweenTwoBuildings(spec, buildingManager, startingBuilding,
											building) != null) {
										closestStartingBuilding = startingBuilding;
										closestEndingBuilding = building;
										leastDistance = distance;
									}
								}
							}
						}
					}
				}

				if ((closestStartingBuilding != null) && (closestEndingBuilding != null)) {
					// Determine new location/length between the two buildings.
					newTemplate = positionConnectorBetweenTwoBuildings(spec, buildingManager, closestStartingBuilding,
							closestEndingBuilding);

				}
			}
		}

		// Case 2
		// Try to find valid connection location between two inhabitable buildings with
		// no joining walking path.
		if (newTemplate == null) {
			logger.config("Case 2 in positionNewConnector()");
			Building closestStartingBuilding = null;
			Building closestEndingBuilding = null;
			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> j = inhabitableBuildings.iterator();
			while (j.hasNext()) {
				Building startingBuilding = j.next();

				// Find a different inhabitable building.
				Iterator<Building> k = inhabitableBuildings.iterator();
				while (k.hasNext()) {
					Building building = k.next();
					boolean hasWalkingPath = buildingManager.getSettlement().getBuildingConnectorManager()
							.hasValidPath(startingBuilding, building);

					// Check if connector base level matches either building.
					boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
							|| (baseLevel == building.getBaseLevel());

					if (!building.equals(startingBuilding) && !hasWalkingPath && matchingBaseLevel) {
						double distance = Point2D.distance(startingBuilding.getXLocation(),
								startingBuilding.getYLocation(), building.getXLocation(), building.getYLocation());
						if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {
							// Check that new building can be placed between the two buildings.
							if (positionConnectorBetweenTwoBuildings(spec, buildingManager, startingBuilding,
									building) != null) {
								closestStartingBuilding = startingBuilding;
								closestEndingBuilding = building;
								leastDistance = distance;
							}
						}
					}
				}
			}

			if ((closestStartingBuilding != null) && (closestEndingBuilding != null)) {
				// Determine new location/length between the two buildings.
				newTemplate = positionConnectorBetweenTwoBuildings(spec, buildingManager, closestStartingBuilding,
						closestEndingBuilding);
			}
		}

		// Case 3
		// Try to find valid connection location between two inhabitable buildings that
		// are not directly connected.
		if (newTemplate == null) {
			logger.config("Case 3 in positionNewConnector()");
			Building closestStartingBuilding = null;
			Building closestEndingBuilding = null;
			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> j = inhabitableBuildings.iterator();
			while (j.hasNext()) {
				Building startingBuilding = j.next();

				// Find a different inhabitable building.
				Iterator<Building> k = inhabitableBuildings.iterator();
				while (k.hasNext()) {
					Building building = k.next();
					boolean directlyConnected = (!buildingManager.getSettlement().getBuildingConnectorManager()
							.getBuildingConnections(startingBuilding, building).isEmpty());

					// Check if connector base level matches either building.
					boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
							|| (baseLevel == building.getBaseLevel());

					if (!building.equals(startingBuilding) && !directlyConnected && matchingBaseLevel) {
						//  Case 3A
						double distance = Point2D.distance(startingBuilding.getXLocation(),
								startingBuilding.getYLocation(), building.getXLocation(), building.getYLocation());
						if ((distance < leastDistance) && (distance >= 5D)) { 
							// Check that new building can be placed between the two buildings.
							if (positionConnectorBetweenTwoBuildings(spec, buildingManager, startingBuilding,
									building) != null) {
								closestStartingBuilding = startingBuilding;
								closestEndingBuilding = building;
								leastDistance = distance;
							}
						}
					}
				}
			}

			if ((closestStartingBuilding != null) && (closestEndingBuilding != null)) {
				// Determine new location/length between the two buildings.
				newTemplate = positionConnectorBetweenTwoBuildings(spec, buildingManager, closestStartingBuilding,
						closestEndingBuilding);
			}
		}

		// Case 4
		// Try to find connection to existing inhabitable building.
		if (newTemplate == null) {
			logger.config("Case 4 in positionNewConnector()");
			Iterator<Building> l = inhabitableBuildings.iterator();
			while (l.hasNext()) {
				Building building = l.next();
				// Make connector building face away from building.
				newTemplate = positionNextToBuilding(spec, buildingManager, building, 0D, true);
			}
		}

		return newTemplate;
	}

	/**
	 * Positions a new building near an existing building.
	 * 
	 * @param newBuildingType    the new building type.
	 * @param building           the existing building.
	 * @param separationDistance the separation distance (meters) from the building.
	 * @param faceAway           true if new building should face away from other
	 *                           building.
	 * @return new building template with determined position, or null if none
	 *         found.
	 */
	private static BuildingTemplate positionNextToBuilding(BuildingSpec spec, BuildingManager buildingManager, Building building, 
			double separationDistance, boolean faceAway) {
		BuildingTemplate newPosition = null;

		// Replace width and length defaults to deal with variable width and length
		// buildings.
		double width = spec.getWidth();
		if (width <= 0D) {
			width = DEFAULT_VARIABLE_BUILDING_WIDTH;
		}
		double length = spec.getLength();
		if (length <= 0D) {
			length = DEFAULT_VARIABLE_BUILDING_LENGTH;
		}

		final int front = 0;
		final int back = 1;
		final int right = 2;
		final int left = 3;

		List<Integer> directions = new ArrayList<>();
		directions.add(front);
		directions.add(back);
		directions.add(right);
		directions.add(left);
		Collections.shuffle(directions);

		double direction = 0D;
		double structureDistance = 0D;
		double rectRotation = building.getFacing();

		for (int x = 0; x < directions.size(); x++) {

			switch (directions.get(x)) {
			case front:
				direction = building.getFacing();
				structureDistance = (building.getLength() / 2D) + (length / 2D);
				break;
			case back:
				direction = building.getFacing() + 180D;
				structureDistance = (building.getLength() / 2D) + (length / 2D);
				if (faceAway) {
					rectRotation = building.getFacing() + 180D;
				}
				break;
			case right:
				direction = building.getFacing() + 90D;
				structureDistance = (building.getWidth() / 2D) + (width / 2D);
				if (faceAway) {
					structureDistance = (building.getWidth() / 2D) + (length / 2D);
					rectRotation = building.getFacing() + 90D;
				}
				break;
			case left:
				direction = building.getFacing() + 270D;
				structureDistance = (building.getWidth() / 2D) + (width / 2D);
				if (faceAway) {
					structureDistance = (building.getWidth() / 2D) + (length / 2D);
					rectRotation = building.getFacing() + 270D;
				}
			}

			if (rectRotation > 360D) {
				rectRotation -= 360D;
			}

			double distance = structureDistance + separationDistance;
			double radianDirection = Math.toRadians(direction);
			double rectCenterX = building.getXLocation() - (distance * Math.sin(radianDirection));
			double rectCenterY = building.getYLocation() + (distance * Math.cos(radianDirection));

			// Check to see if proposed new building position intersects with any existing
			// buildings
			// or construction sites.
			BoundedObject position =  new BoundedObject(rectCenterX, rectCenterY, width, length, rectRotation);
			if (buildingManager.isBuildingLocationOpen(position)) {
				// Set the new building here.
				String buildingID = "" + buildingManager.getNextTemplateID();
			
				int zone = 0;
				
				String uniqueName = buildingManager.getUniqueName(spec.getName());

				logger.config(POSITIONING + "at (" + Math.round(rectCenterX * 10D) / 10D + ", "
						+ Math.round(rectCenterY * 10D) / 10D + ") at " + Math.round(rectRotation) + " deg");

				newPosition = new BuildingTemplate(buildingID, zone,
						spec.getName(), uniqueName, position);
				break;
			}
		}

		return newPosition;
	}

	/**
	 * Determines the position and length (for variable length) for a connector
	 * building between two existing buildings.
	 * 
	 * @param newBuildingType the new connector building type.
	 * @param firstBuilding   the first of the two existing buildings.
	 * @param secondBuilding  the second of the two existing buildings.
	 * @return new building template with determined position, or null if none
	 *         found.
	 */
	private static BuildingTemplate positionConnectorBetweenTwoBuildings(BuildingSpec spec, BuildingManager buildingManager,
			Building firstBuilding,
			Building secondBuilding) {

		BuildingTemplate newPosition = null;

		// Determine valid placement lines for connector building.
		List<Line2D> validLines = new ArrayList<>();

		// Check each building side for the two buildings for a valid line unblocked by
		// obstacles.
		double width = spec.getWidth();
		List<Point2D> firstBuildingPositions = getFourPositionsSurroundingBuilding(firstBuilding, .1D);
		List<Point2D> secondBuildingPositions = getFourPositionsSurroundingBuilding(secondBuilding, .1D);
		for (int x = 0; x < firstBuildingPositions.size(); x++) {
			for (int y = 0; y < secondBuildingPositions.size(); y++) {

				Point2D firstBuildingPos = firstBuildingPositions.get(x);
				Point2D secondBuildingPos = secondBuildingPositions.get(y);

				double distance = Point2D.distance(firstBuildingPos.getX(), firstBuildingPos.getY(),
						secondBuildingPos.getX(), secondBuildingPos.getY());

				if (distance >= MINIMUM_CONNECTOR_LENGTH) {
					// Check line rect between positions for obstacle collision.
					Line2D line = new Line2D.Double(firstBuildingPos.getX(), firstBuildingPos.getY(),
							secondBuildingPos.getX(), secondBuildingPos.getY());
					boolean clearPath = LocalAreaUtil.isLinePathCollisionFree(line, buildingManager.getSettlement().getCoordinates(), false);
					if (clearPath) {
						validLines.add(new Line2D.Double(firstBuildingPos, secondBuildingPos));
					}
				}
			}
		}

		if (!validLines.isEmpty()) {

			// Find shortest valid line.
			double shortestLineLength = Double.MAX_VALUE;
			Line2D shortestLine = null;
			Iterator<Line2D> i = validLines.iterator();
			while (i.hasNext()) {
				Line2D line = i.next();
				double length = Point2D.distance(line.getX1(), line.getY1(), line.getX2(), line.getY2());
				if (length < shortestLineLength) {
					shortestLine = line;
					shortestLineLength = length;
				}
			}
			
			// Below is added to satisfy sonarcloud bug only. It's not needed.
			if (shortestLine == null)
				shortestLine = validLines.get(0);

			// Create building template with position, facing, width and length for the
			// connector building.
			double shortestLineFacingDegrees = LocalAreaUtil.getDirection(shortestLine.getP1(), shortestLine.getP2());
			Point2D p1 = adjustConnectorEndPoint(shortestLine.getP1(), shortestLineFacingDegrees, firstBuilding, width);
			Point2D p2 = adjustConnectorEndPoint(shortestLine.getP2(), shortestLineFacingDegrees, secondBuilding,
					width);
			double centerX = (p1.getX() + p2.getX()) / 2D;
			double centerY = (p1.getY() + p2.getY()) / 2D;
			double newLength = p1.distance(p2);
			double facingDegrees = LocalAreaUtil.getDirection(p1, p2);
		
			String buildingID = "" + buildingManager.getNextTemplateID();
		
			int zone = 0;
			
			String uniqueName = buildingManager.getUniqueName(spec.getName());

			newPosition = new BuildingTemplate(buildingID, zone, spec.getName(), uniqueName, 
					new BoundedObject(centerX, centerY, width, newLength, facingDegrees));
		}

		return newPosition;
	}

	/**
	 * Adjusts the connector end point based on relative angle of the connection.
	 * 
	 * @param point          the initial connector location.
	 * @param lineFacing     the facing of the connector line (degrees).
	 * @param building       the existing building being connected to.
	 * @param connectorWidth the width of the new connector.
	 * @return point adjusted location for connector end point.
	 */
	private static Point2D adjustConnectorEndPoint(Point2D point, double lineFacing, Building building,
			double connectorWidth) {

		double lineFacingRad = Math.toRadians(lineFacing);
		double angleFromBuildingCenterDegrees = LocalAreaUtil
				.getDirection(new Point2D.Double(building.getXLocation(), building.getYLocation()), point);
		double angleFromBuildingCenterRad = Math.toRadians(angleFromBuildingCenterDegrees);
		double offsetAngle = angleFromBuildingCenterRad - lineFacingRad;
		double offsetDistance = Math.abs(Math.sin(offsetAngle)) * (connectorWidth / 2D);

		double newXLoc = (-1D * Math.sin(angleFromBuildingCenterRad) * offsetDistance) + point.getX();
		double newYLoc = (Math.cos(angleFromBuildingCenterRad) * offsetDistance) + point.getY();

		return new Point2D.Double(newXLoc, newYLoc);
	}

	/**
	 * Gets four positions surrounding a building with a given distance from its
	 * edge.
	 * 
	 * @param building         the building.
	 * @param distanceFromSide distance (distance) for positions from the edge of
	 *                         the building.
	 * @return list of four positions.
	 */
	private static List<Point2D> getFourPositionsSurroundingBuilding(Building building, double distanceFromSide) {

		List<Point2D> result = new ArrayList<>();

		final int front = 0;
		final int back = 1;
		final int right = 2;
		final int left = 3;

		for (int x = 0; x < 4; x++) {
			double xPos = 0D;
			double yPos = 0D;

			switch (x) {
			case front:
				xPos = 0D;
				yPos = (building.getLength() / 2D) + distanceFromSide;
				break;
			case back:
				xPos = 0D;
				yPos = 0D - (building.getLength() / 2D) - distanceFromSide;
				break;
			case right:
				xPos = 0D - (building.getWidth() / 2D) - distanceFromSide;
				yPos = 0D;
				break;
			case left:
				xPos = (building.getWidth() / 2D) + distanceFromSide;
				yPos = 0D;
				break;
			}

			Point2D position = LocalAreaUtil.convert2SettlementPos(xPos, yPos, building);
			result.add(position);
		}

		return result;
	}

	/**
	 * Gets a list of the resupply buildings.
	 * 
	 * @return list of building types.
	 */
	@Override
	public List<BuildingTemplate> getBuildings() {
		return newBuildings;
	}

	/**
	 * Sets the list of resupply buildings.
	 * 
	 * @param newBuildings list of building types.
	 */
	public void setBuildings(List<BuildingTemplate> newBuildings) {
		this.newBuildings = newBuildings;
	}

	/**
	 * Gets a list of the resupply vehicles.
	 * 
	 * @return list of vehicle types.
	 */
	@Override
	public Map<String, Integer> getVehicles() {
		return newVehicles;
	}

	/**
	 * Sets the list of resupply vehicles.
	 * 
	 * @param newVehicles list of vehicle types.
	 */
	public void setVehicles(Map<String, Integer> newVehicles) {
		this.newVehicles = newVehicles;
	}

	/**
	 * Gets a map of the resupply equipment.
	 * 
	 * @return map of equipment type and number.
	 */
	@Override
	public Map<String, Integer> getEquipment() {
		return newEquipment;
	}

	/**
	 * Sets the map of resupply equipment.
	 * 
	 * @param newEquipment map of equipment type and number.
	 */
	public void setEquipment(Map<String, Integer> newEquipment) {
		this.newEquipment = newEquipment;
	}

	/**
	 * Gets a map of the resupply bins.
	 * 
	 * @return map of equipment type and number.
	 */
	@Override
	public Map<String, Integer> getBins() {
		return newBins;
	}
	
	/**
	 * Sets the map of resupply bins.
	 * 
	 * @param newBin map of bin type and number.
	 */
	public void setBins(Map<String, Integer> newBins) {
		this.newBins = newBins;
	}
	
	/**
	 * Gets the number of immigrants in the resupply mission.
	 * 
	 * @return the number of immigrants.
	 */
	public int getNewImmigrantNum() {
		return newImmigrantNum;
	}

	/**
	 * Sets the number of immigrants in the resupply mission.
	 * 
	 * @param newImmigrantNum the number of immigrants.
	 */
	public void setNewImmigrantNum(int newImmigrantNum) {
		this.newImmigrantNum = newImmigrantNum;
	}

	/**
	 * Gets the number of bots in the resupply mission.
	 * 
	 * @return the number of bots.
	 */
	public int getNewBotNum() {
		return newBotNum;
	}
	
	/**
	 * Sets the number of bots in the resupply mission.
	 * 
	 * @param newBotNum the number of bots.
	 */
	public void setNewBotNum(int newBotNum) {
		this.newBotNum = newBotNum;
	}
	
	
	/**
	 * Gets a map of the resupply resources.
	 * 
	 * @return map of resource and amount (kg).
	 */
	@Override
	public Map<AmountResource, Double> getResources() {
		return newResources;
	}

	/**
	 * Sets the map of resupply resources.
	 * 
	 * @param newResources map of resource and amount (kg).
	 */
	public void setResources(Map<AmountResource, Double> newResources) {
		this.newResources = newResources;
	}

	/**
	 * Gets a map of resupply parts.
	 * 
	 * @return map of part and number.
	 */
	@Override
	public Map<Part, Integer> getParts() {
		return newParts;
	}

	/**
	 * Sets the map of resupply parts.
	 * 
	 * @param newParts map of part and number.
	 */
	public void setParts(Map<Part, Integer> newParts) {
		this.newParts = newParts;
	}


	/**
	 * Gets the destination settlement.
	 * 
	 * @return destination settlement.
	 */
	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Reattact a loading item to the active UnitManager
	 * @param um
	 */
	@Override
	public void reinit(UnitManager um) {
		settlement = um.getSettlementByID(settlementID);
	}

	/**
	 * Sets the destination settlement.
	 * 
	 * @param settlement the destination settlement.
	 */
	public void setSettlement(Settlement settlement) {
		this.settlement = settlement;
		settlementID = settlement.getIdentifier();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(getArrivalDate().getDateTimeStamp());
		buff.append(" : ");
		buff.append(getSettlement().getName());
		return buff.toString();
	}


	@Override
	public String getSettlementName() {
		return getSettlement().getName();
	}
}
