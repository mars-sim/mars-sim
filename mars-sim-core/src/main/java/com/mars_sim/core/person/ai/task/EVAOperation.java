/*
 * Mars Simulation Project
 * EVAOperation.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.Unit;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.MissionHistoricalEvent;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.Complaint;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalEvent;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.vehicle.Airlockable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.mapdata.location.LocalBoundedObject;
import com.mars_sim.mapdata.location.LocalPosition;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The EVAOperation class is an abstract task that involves an extra vehicular
 * activity.
 */
public abstract class EVAOperation extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default serial id. */
	private static SimLogger logger = SimLogger.getLogger(EVAOperation.class.getName());

	/** Task phases. */
	protected static final TaskPhase WALK_TO_OUTSIDE_SITE = new TaskPhase(
			Msg.getString("Task.phase.walkToOutsideSite")); //$NON-NLS-1$
	protected static final TaskPhase WALK_BACK_INSIDE = new TaskPhase(
			Msg.getString("Task.phase.walkBackInside")); //$NON-NLS-1$


	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .05;
	/** The base chance of an accident per millisol. */
	public static final double BASE_ACCIDENT_CHANCE = .01;

	/** Minimum sunlight for EVA is 1% of max sunlight */
	private static double minEVASunlight = SurfaceFeatures.MAX_SOLAR_IRRADIANCE * 0.01;

	// Data members
	/** Flag for ending EVA operation externally. */
	private boolean endEVA;
	private boolean hasSiteDuration;

	private double siteDuration;
	private double timeOnSite;
	private LocalPosition outsideSitePos;

	private LocalBoundedObject interiorObject;
	private LocalPosition returnInsideLoc;

	private SkillType outsideSkill;

	/**
	 * Constructor.
	 *
	 * @param name   the name of the task
	 * @param person the person to perform the task
	 */
	protected EVAOperation(String name, Person person, boolean hasSiteDuration, double siteDuration, SkillType outsideSkill) {
		super(name, person, true, false, STRESS_MODIFIER, SkillType.EVA_OPERATIONS, 100D);

		// Initialize data members
		this.hasSiteDuration = hasSiteDuration;
		this.siteDuration = siteDuration;
		this.outsideSkill = outsideSkill;
		if (outsideSkill != null) {
			addAdditionSkill(outsideSkill);
		}
		timeOnSite = 0D;

		// Check if person is in a settlement or a rover.
		if (person.isInSettlement()) {
			interiorObject = BuildingManager.getBuilding(person);
			if (interiorObject == null) {
				logger.warning(person, "Supposed to be inside a building but interiorObject is null.");
				endTask();
			}

			else {
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
				logger.warning(person, "Supposed to be in a vehicle inside a garage but interiorObject is null.");
				endTask();
			}

			else {
				// Add task phases.
				addPhase(WALK_TO_OUTSIDE_SITE);
				addPhase(WALK_BACK_INSIDE);

				// Set initial phase.
				setPhase(WALK_TO_OUTSIDE_SITE);
			}
		}

		else if (person.isInVehicle() &&
			person.getVehicle() instanceof Rover) {
			
			interiorObject = person.getVehicle();
			if (interiorObject == null) {
				logger.warning(person, "Supposed to be in a vehicle but interiorObject is null.");
			}
			// Add task phases.
			addPhase(WALK_TO_OUTSIDE_SITE);
			addPhase(WALK_BACK_INSIDE);

			// Set initial phase.
			setPhase(WALK_TO_OUTSIDE_SITE);
		}
	}

	/**
	 * Constructor 2.
	 * 
	 * @param name
	 * @param robot
	 * @param hasSiteDuration
	 * @param siteDuration
	 * @param outsideSkill
	 */
	protected EVAOperation(String name, Robot robot, boolean hasSiteDuration, double siteDuration, SkillType outsideSkill) {
		super(name, robot, true, false, STRESS_MODIFIER, SkillType.EVA_OPERATIONS, 100D);

		this.hasSiteDuration = hasSiteDuration;
		this.siteDuration = siteDuration;
		this.outsideSkill = outsideSkill;
		if (outsideSkill != null) {
			addAdditionSkill(outsideSkill);
		}
	}

	/**
	 * Is this Task interruptable? EVAs can not be interrupted.
	 * @return Returns false by default
	 */
	@Override
	public boolean isInterruptable() {
        return false;
    }

	/**
	 * Checks if EVA should end.
	 */
	public void endEVA() {
		endEVA = true;
	}

	/**
	 * Adds time at EVA site.
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
	 * Sets the outside side local location.
	 *
	 * @param pos The outside position of the EVA
	 */
	protected void setOutsideSiteLocation(LocalPosition pos) {
		outsideSitePos = pos;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (person.isOutside()) {
			if (person.isSuperUnFit()) {
				setPhase(WALK_BACK_INSIDE);
			}
			else
				person.addEVATime(getTaskSimpleName(), time);
		}

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
	 * Performs the walk to outside site phase.
	 *
	 * @param time the time to perform the phase.
	 * @return remaining time after performing the phase.
	 */
	private double walkToOutsideSitePhase(double time) {
	    // If not at field work site location, create walk outside subtask.
        if (person.isInside()) {
        	// A person is walking toward an airlock or inside an airlock
            Walk walkingTask = Walk.createWalkingTask(person, outsideSitePos, null);
            if (walkingTask != null) {
                addSubTask(walkingTask);
            }
            else {
//				logger.severe(person, 30_000, "Cannot walk to outside site.");
                endTask();
            }
        }
        else {
        	if (!person.getPosition().equals(outsideSitePos)) {
    			Task currentTask = person.getMind().getTaskManager().getTask();
        		Task subTask = person.getMind().getTaskManager().getTask().getSubTask();
        		if ((currentTask != null && !currentTask.getName().equalsIgnoreCase(WalkOutside.NAME))
        			|| (subTask != null && !subTask.getName().equalsIgnoreCase(WalkOutside.NAME))) {	
        			addSubTask(new WalkOutside(person, person.getPosition(),
            				outsideSitePos, true));
        		}
        	}
        	else {
                // In case of DigLocalRegolith,
                // set to getOutsideSitePhase() to COLLECT_REGOLITH
        		setPhase(getOutsideSitePhase());
        	}
        }

        return time * .9;
    }

	/**
	 * Performs the walk back inside phase.
	 *
	 * @param time the time to perform the phase.
	 * @return remaining time after performing the phase.
	 */
	private double walkBackInsidePhase(double time) {

		if (person.isOutside()) {

			if (interiorObject == null) {
				// Get closest airlock building at settlement.
				Settlement s = unitManager.findSettlement(person.getCoordinates());
				if (s != null) {
					interiorObject = (LocalBoundedObject)(s.getClosestIngressAirlock(person)).getEntity();
					if (interiorObject instanceof Building b)
						logger.log(person, Level.INFO, 30_000,
							"Found " + b.getName()
							+ " to enter.");
				}
				else {
					// near a vehicle
					Rover r = (Rover)person.getVehicle();
					if (r != null) {
						interiorObject = (LocalBoundedObject) (r.getAirlock()).getEntity();
						logger.log(person, Level.INFO, 30_000,
								"Found " + ((Airlock)interiorObject).getEntityName()
								+ " to enter.");
					}
				}
			}

			if (interiorObject == null) {
				logger.log(person, Level.SEVERE, 30_000, "Trying to walk somewhere. interiorObject is null.");
				addSubTask(new Walk(person));
			}

			else {
				// Set return location.
				returnInsideLoc = LocalAreaUtil.getRandomLocalPos(interiorObject);

				if (returnInsideLoc != null &&
						!LocalAreaUtil.isPositionWithinLocalBoundedObject(
								returnInsideLoc, interiorObject)) {

					logger.log(person, Level.SEVERE, 30_000, 
							"Trying to walk somewhere. returnInsideLoc failed.");
					addSubTask(new Walk(person));
				}
			}

			// If not at return inside location, create walk inside subtask.
			// If not inside, create walk inside subtask.
			if (interiorObject != null && returnInsideLoc != null && !person.getPosition().isClose(returnInsideLoc)) {
				String name = "";
				if (interiorObject instanceof Building b) {
					name = b.getName();
				}
				else if (interiorObject instanceof Vehicle v) {
					name = v.getName();
				}

				logger.log(person, Level.FINE, 4_000,
							"Near " +  name
							+ " at (" + returnInsideLoc
							+ "). Attempting to enter the airlock.");

				Walk walkingTask = Walk.createWalkingTask(person, returnInsideLoc, interiorObject);
				if (walkingTask != null) {
					addSubTask(walkingTask);
				}
				else {
					logger.log(person, Level.SEVERE, 30_000, 
							"Trying to walk somewhere. cannot walk all steps.");
					addSubTask(new Walk(person));
				}
			}

			else {
				logger.log(person, Level.SEVERE, 30_000, 
						"Trying to walk somewhere. interiorObject is null or close to returnInsideLoc.");
				addSubTask(new Walk(person));
			}
		}

		else { // if a person is already inside, end the task gracefully here
			logger.log(person, Level.FINE, 4_000,
					"Walked back inside. Ended '" 
							+ Conversion.capitalize(person.getTaskDescription().toLowerCase()) + "'.");
			endTask();
		}


		return time * .9;
	}
	
	/**
	 * Checks if situation requires the EVA operation to end prematurely and the
	 * person should return to the airlock.
	 * 
	 * @param checkLight
	 * @return true if EVA operation should end
	 */
	protected boolean shouldEndEVAOperation(boolean checkLight) {

		boolean result = false;

		// Check end EVA flag.
		if (endEVA)
			return true;

		// Check for sunlight
		if (checkLight && isGettingDark(person)) {
			// Added to show issue #509
			logger.warning(worker, 10_000L, "Ended '" + getName() + "': becoming too dark.");
			return true;
		}
		
		if (checkLight && getSolarIrradiance(person.getCoordinates()) <= minEVASunlight) {
			logger.warning(worker, 10_000L, "Ended '" + getName() + "': too dark already.");
			return true;
		}

		// Check for any EVA problems.
		if (hasEVAProblem(person))
			return true;

		// Check if it is at meal time and the person is hungry
		if (isHungryAtMealTime(person))
			return true;

        // Checks if the person is physically drained
		if (isExhausted(person))
			return true;


		return result;
	}

	/**
	 * Checks if the sky light is dimming.
	 *
	 * @param active The Unit checking the daylight
	 * @return
	 */
	public static boolean isGettingDark(Unit active) {	
		return orbitInfo.isSunSetting(
        		active.getCoordinates(), false);
    }

	/**
	 * Gets the solar irradiance.
	 * 
	 * @param locn
	 * @return
	 */
	public static double getSolarIrradiance(Coordinates locn) {
		return surfaceFeatures.getSolarIrradiance(locn);
	}
	
	/**
	 * Is there enough sunlight for an EVA ?
	 * 
	 * @return
	 */
	public static boolean isEnoughSunlightForEVA(Coordinates locn) {
		if (minEVASunlight == 0D) {
			// Don't bother calculating sunlight; EVA valid in whatever conditions
			return true;
		}

		// This logic comes from EVAMission originally
		boolean inDarkPolarRegion = surfaceFeatures.inDarkPolarRegion(locn);
		double sunlight = surfaceFeatures.getSolarIrradiance(locn);

		// This is equivalent of a 1% sun ratio as below
		return (sunlight >= minEVASunlight && !inDarkPolarRegion);

		// This is the old logic used originally in EVAOperation
		// if (surfaceFeatures.inDarkPolarRegion(person.getCoordinates())) {
		// 	return false;
		// }

		// // if it's at night
		// // Note: sunlight ratio cannot be smaller than zero.
		// if (surfaceFeatures.getSunlightRatio(person.getCoordinates()) < .01) {
		// 	return false;
		// }

		// // if the sunlight is getting less
        // return surfaceFeatures.getTrend(person.getCoordinates()) < 0;
	}

	/**
	 * Checks if this EVA operation is ready for prime time.
	 * 
	 * @param time
	 * @return
	 */
	public double checkReadiness(double time, boolean checkSunlight) {
		// Task duration has expired
		if (isDone()) {
			checkLocation("Task duration ended.");
			return time;
		}
		// Check for radiation exposure during the EVA operation.
		if (isRadiationDetected(time)) {
			checkLocation("Radiation detected.");
			return time;
		}
		
        // Check if there is a reason to cut short and return.
		if (checkSunlight && shouldEndEVAOperation(checkSunlight)) {
			checkLocation("No sunlight.");
			return time;
		}

        // Check time on site
		if (addTimeOnSite(time)) {
			checkLocation("Time on site expired.");
			return time;
		}		
		
		if (person.isSuperUnFit()) {
			abortEVA("Super unfit.");
			return time;
		}
		return 0;
	}
	
	/**
	 * Checks to see if the person is supposed to be outside. This is used to abort an EVA.
	 * Any call to this method that relates to a problem should be replaced with {@link #abortEVA(String)}
	 * 
	 * @param reason
	 */
	protected void checkLocation(String reason) {
		abortEVA(reason);
	}

	/**
	 * Aborts an EVA, if the Person is outside get them to return otherwise end the Task.
	 * 
	 * @param reason Reason for ending.
	 */
	protected void abortEVA(String reason) {
		if (reason != null) {
 			logger.info(worker, "EVA " + getName() + " aborted: " + reason);
		}
		
		if (person.isOutside()) {
            setPhase(WALK_BACK_INSIDE);
		}
    	else
        	endTask();
	}
	
	/**
	 * Checks if there is an EVA problem for a person.
	 *
	 * @param person the person.
	 * @return false if having EVA problem.
	 */
	public static boolean hasEVAProblem(Person person) {
		boolean result = false;
		EVASuit suit = person.getSuit();
		if (suit == null) {
			return true;
		}

		try {
			// Check if EVA suit is at 15% of its oxygen capacity.
			double oxygenCap = suit.getAmountResourceCapacity(ResourceUtil.oxygenID);
			double oxygen = suit.getAmountResourceStored(ResourceUtil.oxygenID);
			if (oxygen <= (oxygenCap * .2D)) {
				logger.log(person, Level.WARNING, 20_000,
						suit.getName() + " reported less than 20% O2 left when "
								+ person.getTaskDescription() + ".");
				result = true;
			}

			// Check if EVA suit is at 15% of its water capacity.
			double waterCap = suit.getAmountResourceCapacity(ResourceUtil.waterID);
			double water = suit.getAmountResourceStored(ResourceUtil.waterID);
			if (water <= (waterCap * .10D)) {
				logger.log(person, Level.WARNING, 20_000,
						suit.getName() + " reported less than 10% water left when "
										+ person.getTaskDescription() + ".");
				// Running out of water should not stop a person from doing EVA
			}

			// Check if life support system in suit is working properly.
			if (!suit.lifeSupportCheck()) {
				logger.warning(person, 20_000,
						person.getTaskDescription() + " ended : " + suit.getName()
								+ " failed the life support check.");
				result = true;
			}
		} catch (Exception e) {
			logger.severe(person, 20_000,
					person.getTaskDescription() + " ended : " + suit.getName() + " failed the system check.", e);
		}

		// Check if suit has any malfunctions.
		if (suit.getMalfunctionManager().hasMalfunction()) {
			logger.warning(person, 20_000,
					person.getTaskDescription() + "ended : " + suit.getName() + " has malfunction.");
			result = true;
		}

		double perf = person.getPerformanceRating();
		// Check if person's medical condition is sufficient to continue phase.
		if (perf < .05) {
			// Add back to 10% so that the person can walk
			person.getPhysicalCondition().setPerformanceFactor(0.1);
			logger.log(person, Level.WARNING, 20_000,
					person.getTaskDescription() + " ended : low performance.");
			result = true;
		}

		return result;
	}

	/**
	 * Checks if the person's settlement is at meal time and is hungry.
	 *
	 * @param person
	 * @return
	 */
	public static boolean isHungryAtMealTime(Person person) {
        return CookMeal.isMealTime(person, 0) 
        		&& person.getPhysicalCondition().isHungry();
    }

	/**
	 * Checks if the person's settlement is physically drained.
	 *
	 * @param person
	 * @return
	 */
	public static boolean isExhausted(Person person) {
        return person.getPhysicalCondition().isHungry() && person.getPhysicalCondition().isThirsty()
                && person.getPhysicalCondition().isSleepy() || person.getPhysicalCondition().isStressed();
    }

	/**
	 * Checks if the person is physically fit for heavy EVA tasks.
	 * Used by DigLocalMeta.
	 *
	 * @param person
	 * @return
	 */
	public static boolean isEVAFit(Person person) {
		return person.getPhysicalCondition().isEVAFit() 
				|| person.getPhysicalCondition().computeHealthScore() > 80;
	} 
	
	/**
	 * Adds experience for this EVA task. The EVA_OPERATIONS skill is updated.
	 * 
	 * If the {@link #getPhase()} matches the value of {@link #getOutsideSitePhase()} then experience is also added
	 * to the outsideSkill property defined for this task.
	 * If the phase is not outside; then only EVA_OPERATIONS is updated.
	 * 
	 * @param time
	 */
	@Override
	protected void addExperience(double time) {
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;

		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = worker.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (experienceAptitude - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		worker.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);

		// If phase is outside, add experience to outside skill.
		if (getOutsideSitePhase().equals(getPhase()) && (outsideSkill != null)) {
			// 1 base experience point per 10 millisols of collection time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double outsideExperience = time / 10D;
			outsideExperience += outsideExperience * experienceAptitudeModifier;
			worker.getSkillManager().addExperience(outsideSkill, outsideExperience, time);
		}
	}

	/**
	 * Checks for accident with EVA suit.
	 *
	 * @param time the amount of time on EVA (in millisols)
	 */
	protected void checkForAccident(double time) {

		if (person != null) {
			EVASuit suit = person.getSuit();
			if (suit != null) {

				// EVA operations skill modification.
				int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
				checkForAccident(suit, time, BASE_ACCIDENT_CHANCE, skill, "EVA operation at " + person.getCoordinates().toString());
			}
		}
	}

	/**
	 * Checks for radiation exposure of the person performing this EVA.
	 *
	 * @param time the amount of time on EVA (in millisols)
	 * @result true if detected
	 */
	protected boolean isRadiationDetected(double time) {
		if (person != null)
			return person.getPhysicalCondition().getRadiationExposure().isRadiationDetected(time);
		return false;
	}


	/**
	 * Determines a random location for for working outside a Rover for the
	 * assigned Worker.
	 * <p>If no site is found then the Task is ended.
	 *
	 * @param rover Base rover hosting EVA
	 * @return Was a site found
	 */
	protected boolean setRandomOutsideLocation(Rover rover) {

		LocalPosition sLoc = null;
		boolean goodLocation = false;
		for (int x = 0; (x < 5) && !goodLocation; x++) {
			for (int y = 0; (y < 10) && !goodLocation; y++) {

				double distance = RandomUtil.getRandomDouble(50D) + (x * 100D) + 50D;
				double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);

				LocalPosition boundedLocalPoint = rover.getPosition().getPosition(distance, radianDirection);

				sLoc = LocalAreaUtil.convert2SettlementPos(boundedLocalPoint, rover);
				goodLocation = LocalAreaUtil.isPositionCollisionFree(sLoc, worker.getCoordinates());
			}
		}

		if (goodLocation) {
			setOutsideSiteLocation(sLoc);
		}
		else {
			endTask();
			logger.warning(worker, "Can not find a suitable random EVA location");
		}
		return goodLocation;
	}

	/**
	 * Sets the outside location near a BoundedObject.
	 *
	 * @param basePoint
	 * @return A location has been chosen.
	 */
	protected boolean setOutsideLocation(LocalBoundedObject basePoint) {

		LocalPosition newLocation = LocalAreaUtil.getCollisionFreeRandomPosition(basePoint, worker.getCoordinates(), 1D);
		boolean found = false;
		if (newLocation != null) {
			setOutsideSiteLocation(newLocation);
			found = true;
		}
		else {
			endTask();
			logger.warning(worker, "Can not find a suitable EVA location near " + basePoint);
		}
		return found;
	}

	/**
	 * Gets the closest egress airlock to a given location that has a walkable
	 * path from the person's current location.
	 *
	 * @param worker the worker.
	 * @param        double xLocation the destination's X location.
	 * @param        double yLocation the destination's Y location.
	 * @return airlock or null if none available
	 */
	public static Airlock getClosestWalkableEgressAirlock(Worker worker, LocalPosition pos) {
		Airlock result = null;

		if (worker.isInVehicle()) {
			if (worker.getVehicle() instanceof Airlockable a) {
				result = a.getAirlock();
			}
		}
		
		else {
			Settlement s = worker.getSettlement();
			if (s != null) {
				result = s.getClosestWalkableEgressAirlock(worker, pos);
			}
		}

		return result;
	}

	/**
	 * Gets an available airlock to a given location that has a walkable path from
	 * the person's current location.
	 *
	 * @param worker the worker.
	 * @param ingress is the person ingressing.
	 * @return airlock or null if none available
	 */
	public static Airlock getWalkableEgressAirlock(Worker worker) {
		return getClosestWalkableEgressAirlock(worker, worker.getPosition());
	}

	/**
	 * Gets an available airlock for egress to a given location that has a walkable path from
	 * the person's current location.
	 *
	 * @param worker the worker.
	 * 
	 * @return airlock or null if none available
	 */
	public static Airlock getWalkableAvailableEgressAirlock(Worker worker) {
		return getClosestWalkableEgressAirlock(worker, worker.getPosition());
	}

	/**
	 * Unloads any held Equipment back to a Vehicle.
	 * 
	 * @param destination
	 */
	protected void returnEquipmentToVehicle(Vehicle destination) {
		// Return containers in rover Take a copy as the original will change.
		List<Equipment> held = new ArrayList<>(person.getEquipmentSet());
		for(Equipment e : held) {
			// Place this equipment within a rover outside on Mars
			e.transfer(destination);
		}
	}

	/**
	 * Sends a person to a medical building.
	 * 
	 * Note: this is more like a hack, rather than a legitimate 
	 * way of transferring a person through the airlock into the settlement.			
	 *
	 * @param p the person
	 * @param s the settlement
	 */
	public static void send2Medical(Person p, Settlement s) {

		// Store the person into a medical building
		BuildingManager.addToMedicalBuilding(p, s);

		Collection<HealthProblem> problems = p.getPhysicalCondition().getProblems();
		Complaint complaint = p.getPhysicalCondition().getMostSerious();
		HealthProblem problem = null;
		for (HealthProblem hp : problems) {
			if (complaint.getType() == hp.getType()) {
				problem = hp;
				break;
			}
		}
		
		HistoricalEvent rescueEvent = null;
		
		if (problem == null) {
			rescueEvent = new MissionHistoricalEvent(EventType.MISSION_RESCUE_PERSON,
				p.getMission(),
				p.getPhysicalCondition().getHealthSituation(),
				p.getTaskDescription(),
				p.getName(),
				p
				);
		}
		else {
			rescueEvent = new MedicalEvent(p, problem, EventType.MEDICAL_RESCUE);
		}

		// Register the historical event
		registerNewEvent(rescueEvent);
	}

	/**
	 * Sets the minimum sunlight for any EVA operations.
	 * 
	 * @param minimum
	 */
	public static void setMinSunlight(double minimum) {
		logger.config("Minimum sunlight for EVA = " + minimum);
		minEVASunlight = minimum;
	}
}
