/**
 * Mars Simulation Project
 * EVAOperation.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalEvent;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Airlockable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The EVAOperation class is an abstract task that involves an extra vehicular
 * activity.
 */
public abstract class EVAOperation extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default serial id. */
	private static Logger logger = Logger.getLogger(EVAOperation.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

	/** Task phases. */
	protected static final TaskPhase WALK_TO_OUTSIDE_SITE = new TaskPhase(
			Msg.getString("Task.phase.walkToOutsideSite")); //$NON-NLS-1$
	protected static final TaskPhase WALK_BACK_INSIDE = new TaskPhase(Msg.getString("Task.phase.walkBackInside")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .25D;
	/** The base chance of an accident per millisol. */
	public static final double BASE_ACCIDENT_CHANCE = .01;

	// Data members
	/** Flag for ending EVA operation externally. */
	private boolean endEVA;
	private boolean hasSiteDuration;

	private double siteDuration;
	private double timeOnSite;
	private double outsideSiteXLoc;
	private double outsideSiteYLoc;

	private LocalBoundedObject interiorObject;
	private Point2D returnInsideLoc;
	
	/**
	 * Constructor.
	 * 
	 * @param name   the name of the task
	 * @param person the person to perform the task
	 */
	public EVAOperation(String name, Person person, boolean hasSiteDuration, double siteDuration) {
		super(name, person, true, false, STRESS_MODIFIER, false, 0D);

		// Initialize data members
		this.hasSiteDuration = hasSiteDuration;
		this.siteDuration = siteDuration;
		timeOnSite = 0D;

		sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());

		// Check if person is in a settlement or a rover.
		if (person.isInSettlement()) {
			interiorObject = BuildingManager.getBuilding(person);
			if (interiorObject == null) {
				// throw new IllegalStateException(person.getName() + " is in " +
				// person.getSettlement() + " but not in building : interiorObject is null.");
				LogConsolidated.log(Level.WARNING, 10_000, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
						" in " + person.getLocationTag().getImmediateLocation()
								+ " is supposed to be in a building but interiorObject is null.");
				endTask();
			} else {
				// Add task phases.
				addPhase(WALK_TO_OUTSIDE_SITE);
				addPhase(WALK_BACK_INSIDE);

				// Set initial phase.
				setPhase(WALK_TO_OUTSIDE_SITE);
			}
		}
		
		else if (person.isInVehicleInGarage()) {
			interiorObject = person.getVehicle();
			if (interiorObject == null) {
				// throw new IllegalStateException(person.getName() + " is in " +
				// person.getSettlement() + " but not in building : interiorObject is null.");
				LogConsolidated.log(Level.WARNING, 10_000, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
						" in " + person.getLocationTag().getImmediateLocation()
								+ " is supposed to be in a vehicle hosued inside a garage interiorObject is null.");
				endTask();
			} else {
				// Add task phases.
				addPhase(WALK_TO_OUTSIDE_SITE);
				addPhase(WALK_BACK_INSIDE);

				// Set initial phase.
				setPhase(WALK_TO_OUTSIDE_SITE);
			}
		}

		else if (person.isInVehicle()) {
			if (person.getVehicle() instanceof Rover) {
				interiorObject = (Rover) person.getVehicle();
				if (interiorObject == null) {
					// throw new IllegalStateException(person.getName() + " not in a vehicle and
					// interiorObject is null.");
					LogConsolidated.log(Level.WARNING, 10_000, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " in "
								+ person.getLocationTag().getImmediateLocation() 
								+ " is supposed to be in a vehicle but interiorObject is null.");
				}
				// Add task phases.
				addPhase(WALK_TO_OUTSIDE_SITE);
				addPhase(WALK_BACK_INSIDE);

				// Set initial phase.
				setPhase(WALK_TO_OUTSIDE_SITE);
			} else {
				LogConsolidated.log(Level.SEVERE, 10_000, sourceName,
						"[" + person.getName() + " not in a rover vehicle: " + person.getVehicle());
			}
		}
	}

	public EVAOperation(String name, Robot robot, boolean hasSiteDuration, double siteDuration) {
		super(name, robot, true, false, STRESS_MODIFIER, false, 0D);

		sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());

	}

	/**
	 * Check if EVA should end.
	 */
	public void endEVA() {
		endEVA = true;
	}

	/**
	 * Add time at EVA site.
	 * 
	 * @param time the time to add (millisols).
	 * @return true if site phase should end.
	 */
	protected boolean addTimeOnSite(double time) {

		boolean result = false;

		timeOnSite += time;

		if (hasSiteDuration && (timeOnSite >= siteDuration)) {
			result = true;
		}

		return result;
	}

	/**
	 * Gets the outside site phase.
	 * 
	 * @return task phase.
	 */
	protected abstract TaskPhase getOutsideSitePhase();

	/**
	 * Set the outside side local location.
	 * 
	 * @param xLoc the X location.
	 * @param yLoc the Y location.
	 */
	protected void setOutsideSiteLocation(double xLoc, double yLoc) {
		outsideSiteXLoc = xLoc;
		outsideSiteYLoc = yLoc;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("EVAOoperation's task phase is null");
		} else if (WALK_TO_OUTSIDE_SITE.equals(getPhase())) {
			return walkToOutsideSitePhase(time);
		} else if (WALK_BACK_INSIDE.equals(getPhase())) {
			return walkBackInsidePhase(time);
		} 
		
		return time;
	}

	/**
	 * Perform the walk to outside site phase.
	 * 
	 * @param time the time to perform the phase.
	 * @return remaining time after performing the phase.
	 */
	private double walkToOutsideSitePhase(double time) {
		// If not outside, create walk outside subtask.
		if (person.isOutside()) {
			setPhase(getOutsideSitePhase());
		} 
		
		else {

			if (Walk.canWalkAllSteps(person, outsideSiteXLoc, outsideSiteYLoc, 0, null)) {
				Task walkingTask = new Walk(person, outsideSiteXLoc, outsideSiteYLoc, 0, null);
				addSubTask(walkingTask);
			} else {
				// LogConsolidated.log(logger, Level.WARNING, 3000, sourceName,
				// person.getName() + " cannot walk to outside site.", null);
				endTask();
			}
		}

		return time;
	}

	/**
	 * Perform the walk back inside phase.
	 * 
	 * @param time the time to perform the phase.
	 * @return remaining time after performing the phase.
	 */
	private double walkBackInsidePhase(double time) {
		
		if (person.isOutside()) {
			if (interiorObject == null) {
			// Get closest airlock building at settlement.
				Settlement s = CollectionUtils.findSettlement(person.getCoordinates());
				if (s != null) {
					interiorObject = (Building)(s.getClosestAvailableAirlock(person).getEntity()); 
					System.out.println("interiorObject is " + interiorObject);
					if (interiorObject == null)
						interiorObject = (LocalBoundedObject)(s.getClosestAvailableAirlock(person).getEntity());
					System.out.println("interiorObject is " + interiorObject);
					LogConsolidated.log(Level.WARNING, 0, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " + person.getName()
							+ " in " + person.getLocationTag().getImmediateLocation()
							+ " found " + ((Building)interiorObject).getNickName()
							+ " as the closet building with an airlock to enter.");
				}
				else {
					// near a vehicle
					Rover r = (Rover)person.getVehicle();
					interiorObject = (LocalBoundedObject) (r.getAirlock()).getEntity();
					LogConsolidated.log(Level.WARNING, 0, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " + person.getName()
							+ " was near " + r.getName()
							+ " and had to walk back inside the vehicle.");
				}
			}
			
			if (interiorObject == null) {
				LogConsolidated.log(Level.WARNING, 0, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " + person.getName()
					+ " was near " + person.getLocationTag().getImmediateLocation()
					+ " at (" + Math.round(returnInsideLoc.getX()*10.0)/10.0 + ", " 
					+ Math.round(returnInsideLoc.getY()*10.0)/10.0 + ") "
					+ " but interiorObject is null.");
				endTask();
			}
			else {
				// Set return location.
				Point2D rawReturnInsideLoc = LocalAreaUtil.getRandomInteriorLocation(interiorObject);
				returnInsideLoc = LocalAreaUtil.getLocalRelativeLocation(rawReturnInsideLoc.getX(),
						rawReturnInsideLoc.getY(), interiorObject);
				
				if (returnInsideLoc != null && !LocalAreaUtil.checkLocationWithinLocalBoundedObject(returnInsideLoc.getX(),
							returnInsideLoc.getY(), interiorObject)) {
					LogConsolidated.log(Level.WARNING, 0, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " + person.getName()
							+ " was near " + ((Building)interiorObject).getNickName() //person.getLocationTag().getImmediateLocation()
							+ " at (" + Math.round(returnInsideLoc.getX()*10.0)/10.0 + ", " 
							+ Math.round(returnInsideLoc.getY()*10.0)/10.0 + ") "
							+ " but could not be found inside " + interiorObject);
					endTask();
				}
			}
	
			// If not at return inside location, create walk inside subtask.
	        Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
	        boolean closeToLocation = LocalAreaUtil.areLocationsClose(personLocation, returnInsideLoc);
	        
			// If not inside, create walk inside subtask.
			if (interiorObject != null && !closeToLocation) {
				String name = "";
				if (interiorObject instanceof Building) {
					name = ((Building)interiorObject).getNickName();
				}
				else if (interiorObject instanceof Vehicle) {
					name = ((Vehicle)interiorObject).getNickName();
				}
						
				LogConsolidated.log(Level.FINEST, 10_000, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " + person.getName()
							+ " was near " +  name 
							+ " at (" + Math.round(returnInsideLoc.getX()*10.0)/10.0 + ", " 
							+ Math.round(returnInsideLoc.getY()*10.0)/10.0 
							+ ") and was attempting to enter its airlock.");
				
				if (Walk.canWalkAllSteps(person, returnInsideLoc.getX(), returnInsideLoc.getY(), 0, interiorObject)) {
					Task walkingTask = new Walk(person, returnInsideLoc.getX(), returnInsideLoc.getY(), 0, interiorObject);
					addSubTask(walkingTask);
				} 
				
				else {
					LogConsolidated.log(Level.SEVERE, 0, sourceName,
							person.getName() + " was " + person.getTaskDescription().toLowerCase() 
							+ " and cannot find a valid path to enter an airlock. Will see what to do.");
					endTask();
				}
			} else {
				LogConsolidated.log(Level.SEVERE, 0, sourceName,
						person.getName() + " was " + person.getTaskDescription().toLowerCase() 
						+ " and cannot find the building airlock to walk back inside. Will see what to do.");
				endTask();
			}
		}
		else
			endTask();
		
		return time;
	}

	/**
	 * Checks if situation requires the EVA operation to end prematurely and the
	 * person should return to the airlock.
	 * 
	 * @return true if EVA operation should end
	 */
	protected boolean shouldEndEVAOperation() {

		boolean result = false;

		// Check end EVA flag.
		if (endEVA) {
			return true;
		}

		// Check for sunlight
		if (isGettingDark(person)) {
			return true;
		}
				
		// Check if any EVA problem.
		else if (!noEVAProblem(person)) {
			return true;
		}

		// Check if it is at meal time and the person is hungry
		else if (isHungryAtMealTime(person)) {
			return true;
		}
		
		return result;
	}

	/**
	 * Checks if the sky is dimming and is at dusk
	 * 
	 * @param person
	 * @return
	 */
	public static boolean isGettingDark(Person person) {
	
		if (surfaceFeatures.getTrend(person.getCoordinates()) < 0
				&& hasLittleSunlight(person)) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Checks if there is any sunlight
	 * 
	 * @param person
	 * @return
	 */
	public static boolean hasLittleSunlight(Person person) {

		// Check if it is night time.
		if (surfaceFeatures.getSolarIrradiance(person.getCoordinates()) < 12D
			&& !surfaceFeatures.inDarkPolarRegion(person.getCoordinates())) {
				return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if there is an EVA problem for a person.
	 * 
	 * @param person the person.
	 * @return false if having EVA problem.
	 */
	public static boolean noEVAProblem(Person person) {
		
//		if (isGettingDark(person)) {
//			LogConsolidated.log(Level.FINE, 5000, sourceName,
//					"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " ended "
//					+ person.getTaskDescription() + " : too dark to continue with the EVA outside at " 
//					+ person.getCoordinates().getFormattedString());
//			return false;
//		}
		
		EVASuit suit = person.getSuit();//(EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
		if (suit == null) {
//			LogConsolidated.log(Level.WARNING, 5000, sourceName, 
//					"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " ended " 
//							+ person.getTaskDescription() + " : no EVA suit is available.");
			return false;
		}

		Inventory suitInv = suit.getInventory();

		try {
			// Check if EVA suit is at 15% of its oxygen capacity.
			double oxygenCap = suitInv.getAmountResourceCapacity(ResourceUtil.oxygenID, false);
			double oxygen = suitInv.getAmountResourceStored(ResourceUtil.oxygenID, false);
			if (oxygen <= (oxygenCap * .2D)) {
				LogConsolidated.log(Level.INFO, 5000, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName()
								+ " reported less than 20% O2 level left in "
								+ suit.getName() + " Ending "
								+ person.getTaskDescription() 
								+ " at " + person.getCoordinates().getFormattedString());
				return false;
			}

			// Check if EVA suit is at 15% of its water capacity.
			double waterCap = suitInv.getAmountResourceCapacity(ResourceUtil.waterID, false);
			double water = suitInv.getAmountResourceStored(ResourceUtil.waterID, false);
			if (water <= (waterCap * .10D)) {
				LogConsolidated.log(Level.INFO, 5000, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() + "'s " + suit.getName()
								+ " reported less than 10% water level left when "
										+ person.getTaskDescription());
//				return false;
			}

			// Check if life support system in suit is working properly.
			if (!suit.lifeSupportCheck()) {
				LogConsolidated.log(Level.WARNING, 5000, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " ended '"
								+ person.getTaskDescription() + "' : " + suit.getName() + " failed life support check.");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			LogConsolidated.log(Level.WARNING, 5000, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " ended '"
							+ person.getTaskDescription() + "' : " + suit.getName() + " failed system check.", e);
		}

		// Check if suit has any malfunctions.
		if (suit.getMalfunctionManager().hasMalfunction()) {
			LogConsolidated.log(Level.INFO, 5000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
					+ person.getName() + " ended '" + person.getTaskDescription() + "' : " + suit.getName() + " has malfunction.");
			return false;
		}

		double perf = person.getPerformanceRating();
		// Check if person's medical condition is sufficient to continue phase.
		if (perf < .1D) {
			// Add back to 10% so that the person can walk
			person.getPhysicalCondition().setPerformanceFactor((perf + .01)* 1.1);
			LogConsolidated.log(Level.INFO, 5000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
							+ person.getName() + " ended '" + person.getTaskDescription() + "' : performance is less than 10%.");
			return false;
		}

		return true;
	}

	public static boolean checkEVAProblem(Robot robot) {
		return true;
	}

	/**
	 * Checks if the person's settlement is at meal time and is hungry
	 * 
	 * @param person
	 * @return
	 */
	public static boolean isHungryAtMealTime(Person person) {
	
		if (CookMeal.isLocalMealTime(person.getCoordinates(), 15) && person.getPhysicalCondition().isHungry()) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Check for accident with EVA suit.
	 * 
	 * @param time the amount of time on EVA (in millisols)
	 */
	protected void checkForAccident(double time) {

		if (person != null) {
			EVASuit suit = person.getSuit();//(EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
			if (suit != null) {

				double chance = BASE_ACCIDENT_CHANCE;

				// EVA operations skill modification.
				int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
				if (skill <= 3)
					chance *= (4 - skill);
				else
					chance /= (skill - 2);

				// Modify based on the suit's wear condition.
				chance *= suit.getMalfunctionManager().getWearConditionAccidentModifier();

				if (RandomUtil.lessThanRandPercent(chance * time)) {
					if (person != null) {
//    	    	            logger.info(person.getName() + " has an accident during an EVA operation.");
						suit.getMalfunctionManager().createASeriesOfMalfunctions("EVA operation", person);
					} else if (robot != null) {
						// logger.info(robot.getName() + " has an accident during an EVA operation.");
						suit.getMalfunctionManager().createASeriesOfMalfunctions("EVA operation", robot);
					}
				}
			}
		} else if (robot != null) {

		}
	}

	/**
	 * Check for radiation exposure of the person performing this EVA.
	 * 
	 * @param time the amount of time on EVA (in millisols)
	 * @result true if detected
	 */
	protected boolean isRadiationDetected(double time) {
		if (person != null) {
			return person.getPhysicalCondition().getRadiationExposure().isRadiationDetected(time);

		} else if (robot != null) {
			return false;
		}

		return false;
	}

	/**
	 * Gets the closest available airlock to a given location that has a walkable
	 * path from the person's current location.
	 * 
	 * @param person the person.
	 * @param        double xLocation the destination's X location.
	 * @param        double yLocation the destination's Y location.
	 * @return airlock or null if none available
	 */
	public static Airlock getClosestWalkableAvailableAirlock(Person person, double xLocation, double yLocation) {
		Airlock result = null;

//		Settlement settlement = CollectionUtils.findSettlement(person.getCoordinates());
//		if (settlement != null) {
//			result = settlement.getClosestWalkableAvailableAirlock(person, xLocation, yLocation);
//			// logger.info(person.getName() + " is walking toward an airlock.");
//		} else {
//			Vehicle vehicle = CollectionUtils.findVehicle(person.getCoordinates());	
//			if (vehicle != null && vehicle instanceof Airlockable) {
//				result = ((Airlockable) vehicle).getAirlock();
//			}
//		}

		if (person.isInSettlement()) {
			Settlement settlement = person.getSettlement();
			result = settlement.getClosestWalkableAvailableAirlock(person, xLocation, yLocation);
		} else if (person.isInVehicle()) {
			Vehicle vehicle = person.getVehicle();
			if (vehicle instanceof Airlockable) {
				result = ((Airlockable) vehicle).getAirlock();
			}
		}
		
		return result;
	}

	public static Airlock getClosestWalkableAvailableAirlock(Robot robot, double xLocation, double yLocation) {
		Airlock result = null;

//		Settlement settlement = CollectionUtils.findSettlement(robot.getCoordinates());
//		if (settlement != null) {
//			result = settlement.getClosestWalkableAvailableAirlock(robot, xLocation, yLocation);
//			// logger.info(person.getName() + " is walking toward an airlock.");
//		} else {
//			Vehicle vehicle = CollectionUtils.findVehicle(robot.getCoordinates());	
//			if (vehicle != null && vehicle instanceof Airlockable) {
//				result = ((Airlockable) vehicle).getAirlock();
//			}
//		}
		
		if (robot.isInSettlement()) {
			Settlement settlement = robot.getSettlement();
			result = settlement.getClosestWalkableAvailableAirlock(robot, xLocation, yLocation);
		} else if (robot.isInVehicle()) {
			Vehicle vehicle = robot.getVehicle();
			if (vehicle instanceof Airlockable) {
				result = ((Airlockable) vehicle).getAirlock();
			}
		}

		return result;
	}

	/**
	 * Gets an available airlock to a given location that has a walkable path from
	 * the person's current location.
	 * 
	 * @param person the person.
	 * @return airlock or null if none available
	 */
	public static Airlock getWalkableAvailableAirlock(Person person) {
		return getClosestWalkableAvailableAirlock(person, person.getXLocation(), person.getYLocation());
	}

	public static Airlock getWalkableAvailableAirlock(Robot robot) {
		return getClosestWalkableAvailableAirlock(robot, robot.getXLocation(), robot.getYLocation());
	}

	/**
	 * Set the task's stress modifier. Stress modifier can be positive (increase in
	 * stress) or negative (decrease in stress).
	 * 
	 * @param newStressModifier stress modification per millisol.
	 */
	protected void setStressModifier(double newStressModifier) {
		super.setStressModifier(stressModifier);
	}
	
	/**
	 * Rescue the person from the rover
	 * 
	 * @param r the rover
	 * @param p the person
	 * @param s the settlement
	 */
	static void rescueOperation(Rover r, Person p, Settlement s) {
		
		if (p.isDeclaredDead()) {
			Unit cu = p.getPhysicalCondition().getDeathDetails().getContainerUnit();
//			cu.getInventory().retrieveUnit(p);
			p.transfer(cu, s);
		}
		// Retrieve the person from the rover
		else if (r != null) {
//			r.getInventory().retrieveUnit(p);
			p.transfer(r, s);
		}
		else if (p.isOutside()) {
//			unitManager.getMarsSurface().getInventory().retrieveUnit(p);
			p.transfer(unitManager.getMarsSurface(), s);
		}
		
		// Gets the settlement id
		int id = s.getIdentifier();
		// Store the person into a medical building
		BuildingManager.addToMedicalBuilding(p, id);
		// Register the person
//		p.setAssociatedSettlement(id);
		
		
		Collection<HealthProblem> problems = p.getPhysicalCondition().getProblems();
		Complaint complaint = p.getPhysicalCondition().getMostSerious();
		HealthProblem problem = null;
		for (HealthProblem hp : problems) {
			if (problem.equals(hp))
				problem = hp;
		}
		
		// Register the historical event
		HistoricalEvent rescueEvent = new MedicalEvent(p, problem, EventType.MEDICAL_RESCUE);
		Simulation.instance().getEventManager().registerNewEvent(rescueEvent);
	}
	
	@Override
	public void destroy() {
		super.destroy();

		interiorObject = null;
	}
}