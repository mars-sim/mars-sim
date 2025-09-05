/*
 * Mars Simulation Project
 * EVAOperation.java
 * @date 2025-07-30
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.cooking.task.CookMeal;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalConditionFormat;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.MissionHistoricalEvent;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.SkillWeight;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalEvent;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Airlockable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The EVAOperation class is an abstract task that involves an extra vehicular
 * activity.
 */
public abstract class EVAOperation extends Task {
	/**
	 * Defines the level of light needed for an EVA
	 */
	public enum LightLevel {
		NONE, LOW, HIGH
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default serial id. */
	private static SimLogger logger = SimLogger.getLogger(EVAOperation.class.getName());

	// Experience impact when not doing the on site activity
	private static final ExperienceImpact IMPACT = createPhaseImpact();
	
	/** Task phases. */
	public static final TaskPhase WALK_TO_OUTSIDE_SITE = new TaskPhase(
			Msg.getString("Task.phase.walkToOutsideSite")); //$NON-NLS-1$
	protected static final TaskPhase WALK_BACK_INSIDE = new TaskPhase(
			Msg.getString("Task.phase.walkBackInside")); //$NON-NLS-1$


	// Static members
	/** The average time for egress and ingress of EVA airlock. */
	public static final int AVERAGE_EVA_TIME = 60;
	
	/** The base chance of an accident per millisol. */
	public static final double BASE_ACCIDENT_CHANCE = .01;

	// Data members
	private boolean endEVARequested;
	private double timeOnSiteRemaining;
	private LocalPosition outsideSitePos;

	private LocalBoundedObject interiorObject;
	private LocalPosition returnInsideLoc;

	private LightLevel minEVASunlight;

	private TaskPhase outsidePhase;

	/**
	 * Constructor.
	 *
	 * @param name   the name of the task
	 * @param person the person to perform the task
	 * @param siteDuration How long is the onsite work; zero means there is not a fixed duration
	 * @param onSitePhase The TaskPhase for the actual onsite work
	 */
	protected EVAOperation(String name, Person person, double siteDuration, TaskPhase onSitePhase) {
		super(name, person, true, IMPACT, 0D);

		// Initialize data members
		this.minEVASunlight = LightLevel.LOW;
		this.timeOnSiteRemaining = (siteDuration > 0 ? siteDuration : 1000D);
		this.outsidePhase = onSitePhase;

		// Check if person is in a settlement or a rover.
		if (person.isInSettlement()) {
			interiorObject = BuildingManager.getBuilding(person);
			if (interiorObject == null) {
				logger.warning(person, "Supposed to be inside a building but interiorObject is null.");
				endTask();
			}
			else {
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

			// Set initial phase.
			setPhase(WALK_TO_OUTSIDE_SITE);
		}
	}

	/** 
	 * What phase is executed when the Person is onsite?
	 * 
	 * @return onsite phase
	 */
	public TaskPhase getOutsidePhase() {
		return outsidePhase;
	}

	/**
	 * Helper method to create an impact specific for a TaskPhase.
	 * 
	 * @param extraSkills
	 * @return
	 */
	protected static ExperienceImpact createPhaseImpact(SkillType... extraSkills) {
		Set<SkillWeight> skills = ExperienceImpact.toSkillWeights(extraSkills);
		skills.add(new SkillWeight(SkillType.EVA_OPERATIONS, 1));
		return new ExperienceImpact(100D, NaturalAttributeType.EXPERIENCE_APTITUDE, true, 0.04D, 
					skills);
	}
	
	/**
	 * Helper method to create an impact specific for a TaskPhase.
	 * 
	 * @param effort
	 * @param extraSkills
	 * @return
	 */
	protected static ExperienceImpact createPhaseImpact(PhysicalEffort effort, SkillType... extraSkills) {
		Set<SkillWeight> skills = ExperienceImpact.toSkillWeights(extraSkills);
		skills.add(new SkillWeight(SkillType.EVA_OPERATIONS, 1));
		return new ExperienceImpact(100D, NaturalAttributeType.EXPERIENCE_APTITUDE, effort, 0.05D, 
					skills);
	}

	/**
	 * Sets the minimum sunlight for this EVA operation.
	 * 
	 * @param minSunlight
	 */
	protected void setMinimumSunlight(LightLevel minSunlight) {
		minEVASunlight = minSunlight;
	}

	/**
	 * Is the sunlight at a location above a minimum level?
	 * 
	 * @param locn
	 * @param level
	 * @return
	 */
	public static boolean isSunlightAboveLevel(Coordinates locn, LightLevel level) {
		double solar = switch(level) {
			case NONE -> 0D;
			case LOW -> 0.1D * SurfaceFeatures.MAX_SOLAR_IRRADIANCE;
			case HIGH -> 0.5D * SurfaceFeatures.MAX_SOLAR_IRRADIANCE;
		};

		return getSolarIrradiance(locn) >= solar;
	}

	/**
	 * Is this Task interruptible? EVAs can not be interrupted.
	 * 
	 * @return Returns false by default
	 */
	@Override
	public boolean isInterruptable() {
        return false;
    }

	/**
	 * Request any current EVA is ended at the next opportunity
	 */
	public void requestEndEVA() {
		endEVARequested = true;
	}

	/**
	 * Adds time at EVA site.
	 *
	 * @param time the time to add (millisols).
	 * @return true if site phase should end.
	 */
	protected boolean addTimeOnSite(double time) {
		timeOnSiteRemaining -= time;
		return (timeOnSiteRemaining <= 0);
	}

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
			if (isSuperUnfit()) {
				walkBackInsidePhase();
			}
			else
				person.addEVATime(getTaskSimpleName(), time);
		}

		if (getPhase() == null) {
			throw new IllegalArgumentException("EVAOoperation's task phase is null");
		} else if (WALK_TO_OUTSIDE_SITE.equals(getPhase())) {
			return walkToOutsideSitePhase();
		} else if (WALK_BACK_INSIDE.equals(getPhase())) {
			return walkBackInsidePhase();
		}

		return time;
	}

	/**
	 * Gets a list of the skills associated with the outside phase of the EVA.
	 * 
	 * @return list of skills
	 */
	@Override
	public Set<SkillType> getAssociatedSkills() {
		var i = outsidePhase.getImpact();
		if (i != null) {
			return	i.getImpactedSkills();
		}
		return super.getAssociatedSkills();
	}

	/**
	 * Performs the walk to outside site phase.
	 *
	 * @return remaining time after performing the phase.
	 */
	private double walkToOutsideSitePhase() {
	    // If not at field work site location, create walk outside subtask.
        if (person.isInside()) {
        	// A person is walking toward an airlock or inside an airlock
            Walk walkingTask = Walk.createWalkingTask(person, outsideSitePos, null, true);
            if (walkingTask != null) {

				boolean canAdd = addSubTask(walkingTask);
				if (!canAdd) {
					logger.log(person, Level.WARNING, 4_000,
							". Unable to add subtask Walk.createWalkingTask.");
					// Note: may call below many times
					endTask();
				}
            }
            else {
                endTask();
            }
        }
        else {
        	if (!outsideSitePos.isNear(person.getPosition())) {	
        		// FUTURE: how to get the walk time and return the remaining time ?
        		  
        		// Note that addSubTask() will internally check if the task is a duplicate
        		addSubTask(new WalkOutside(person, person.getPosition(),
            				outsideSitePos, false));   		
        	}
        	else {
                // Set to collectionPhase
        		setPhase(outsidePhase);
        	}
        }

        return 0;
    }

	/**
	 * Performs the walk back inside phase.
	 *
	 * @return remaining time after performing the phase.
	 */
	private double walkBackInsidePhase() {

		if (person.isOutside()) {

			if (interiorObject == null) {
				// Get closest airlock building at settlement.
				Settlement s = unitManager.findSettlement(person.getCoordinates());
				if (s != null) {
					Airlock airlock = s.getClosestIngressAirlock(person);
					if (airlock != null)
						interiorObject = (LocalBoundedObject)(airlock).getEntity();
				}
				else {
					// near a vehicle
					Rover r = (Rover)person.getVehicle();
					if (r != null) {
						interiorObject = (LocalBoundedObject) (r.getAirlock()).getEntity();
					}
				}
			}

			if (interiorObject == null) {
				logger.log(person, Level.SEVERE, 30_000, "Trying to walk somewhere. interiorObject is null.");

				boolean canAdd = addSubTask(new Walk(person));
				if (!canAdd) { 
					logger.log(person, Level.WARNING, 4_000,
							". Unable to add subtask Walk.");
					// Note: may call below many times
					endTask();
				}
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

				Walk walkingTask = Walk.createWalkingTask(person, returnInsideLoc, interiorObject, true);
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
							+ person.getTaskDescription() + "'.");
			endTask();
		}


		return 0;
	}
	
	/**
	 * Checks if situation requires the EVA operation to end prematurely and the
	 * person should return to the airlock.
	 * 
	 * @return true if EVA operation should end
	 */
	public static boolean shouldEndEVAOperation(Person person) {

		boolean result = false;

		// Check for any EVA problems.
		if (hasEVASuitProblem(person)) {
//			May add back for testing : logger.info(worker, 10_000L, getName() + "': EVA problems.");
			return true;
		}

		// Check if it is at meal time and the person is doubly hungry
		if (isHungryAtMealTime(person, 0)) {
//			May add back for future testing : logger.info(worker, 10_000L, "Ending '" + getName() + "': Doubly hungry at meal time.");
			return true;
		}

        // Checks if the person is physically drained
		if (isExhausted(person)) {
//			May add back for future testing : logger.info(worker, 10_000L, "Ending '" + getName() + "': Exhausted.");
			return true;
		}
		
		return result;
	}
	
	/**
	 * Checks if situation requires the EVA operation to end prematurely and the
	 * person should return to the airlock.
	 * 
	 * @return true if EVA operation should end
	 */
	protected boolean shouldEndEVAOperation() {

		// Check end EVA flag.
		if (endEVARequested)
			return true;

		// Check for sunlight
		if (!isSunlightAboveLevel(person.getCoordinates(), minEVASunlight)) {
//			May add back for future testing : logger.info(worker, 10_000L, getName() + "': too dark already.");
			return true;
		}

		return shouldEndEVAOperation(person);
	}

	/**
	 * Checks if the sky light is dimming.
	 *
	 * @param active The surface POI checking the daylight
	 * @return
	 */
	public static boolean isGettingDark(SurfacePOI active) {	
		return orbitInfo.isSunSetting(
        		active.getCoordinates(), false);
    }

	/**
	 * Gets the solar irradiance.
	 * 
	 * @param locn
	 * @return
	 */
	protected static double getSolarIrradiance(Coordinates locn) {
		return surfaceFeatures.getSolarIrradiance(locn);
	}
	
	
	/**
	 * Checks if this EVA operation is ready for prime time.
	 * 
	 * @param time
	 * @return
	 */
	public double checkReadiness(double time) {
		// Task duration has expired
		if (isDone()) {
			endEVA("Task duration ended.");
			return time;
		}
		
		// Check for radiation exposure during the EVA operation.
		if (isRadiationDetected(time)) {
			endEVA("Radiation detected.");
			return time;
		}
		
		// Check fitness only if it's not in the state of emergency
		if (isSuperUnfit()) {
			endEVA("Super Unfit.");
			return time;
		}	

        // Check if there is a reason to cut short and return.
		if (shouldEndEVAOperation()) {
			endEVA("EVA ended prematurely.");
			return time;
		}
		
        // Check time on site
		if (addTimeOnSite(time)) {
			endEVA("Time on site expired.");
			return time;
		}		
		
		return 0;
	}
	
	/**
	 * Checks to see if the person is supposed to be outside. This is used to abort an EVA.
	 * Any call to this method that relates to a problem should be replaced with {@link #endEVA(String)}
	 * 
	 * @param reason
	 * @deprecated Use {@link #endEVA(String)}
	 */
	protected void checkLocation(String reason) {
		endEVA(reason);
	}

	/**
	 * Aborts an EVA, if the Person is outside get them to return otherwise end the Task.
	 * 
	 * @param reason Reason for ending.
	 */
	protected void endEVA(String reason) {
		if (reason != null) {
// 			May add back for future testing :logger.info(worker, 20_000, "Ending EVA '" + getName() + "': " + reason);
		}
		
		if (person.isOutside()) {
            setPhase(WALK_BACK_INSIDE);
		}
    	else
        	endTask();
	}

	/**
	 * This is a safe check method; it intercepts end task calls when the Worker is still outside.
	 * In this case the endEVA method is called.
	 */
	@Override
	public void endTask() {		
		if (person.isOutside()) {
			logger.warning(worker, "Prematurely ending a task during an EVA.");
            endEVA("Premature endTask");
		}
    	else
        	super.endTask();
	}
	
	/**
	 * Checks if there is an EVA suit problem for a person.
	 *
	 * @param person the person.
	 * @return false if having EVA suit problem.
	 */
	public static boolean hasEVASuitProblem(Person person) {
		boolean result = false;
		EVASuit suit = person.getSuit();
		if (suit == null) {
			return true;
		}

		try {
			// Check if EVA suit is at 15% of its oxygen capacity.
			double oxygenCap = suit.getSpecificCapacity(ResourceUtil.OXYGEN_ID);
			double oxygen = suit.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
			if (oxygen <= (oxygenCap * .2D)) {
				logger.log(person, Level.WARNING, 20_000,
						suit.getName() + " reported less than 20% O2 left when "
								+ person.getTaskDescription() + ".");
				result = true;
			}

			// Check if EVA suit is at 15% of its water capacity.
			double waterCap = suit.getSpecificCapacity(ResourceUtil.WATER_ID);
			double water = suit.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
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
		
		return result;
	}

	/**
	 * Checks if the person's settlement is at meal time and is doubly hungry.
	 *
	 * @param person
	 * @param prepTime
	 * @return
	 */
	public static boolean isHungryAtMealTime(Person person, int prepTime) {
		if (isInEmergency(person)) {
			return false;
		}
		
        return CookMeal.isMealTime(person.getAssociatedSettlement(), prepTime) 
        		&& person.getPhysicalCondition().isDoubleHungry();
    }

	/**
	 * Checks if the person's settlement is physically drained.
	 *
	 * @param person
	 * @return
	 */
	public static boolean isExhausted(Person person) {
		if (isInEmergency(person)) {
			return false;
		}	
		
        return person.getPhysicalCondition().isDoubleHungry() && person.getPhysicalCondition().isDoubleThirsty()
                && person.getPhysicalCondition().isSleepy() && person.getPhysicalCondition().isStressed();
    }

	/**
	 * Checks if the person is physically fit for heavy EVA tasks.
	 * Used by DigLocalMeta.
	 *
	 * @param person
	 * @return
	 */
	public static boolean isEVAFit(Person person) {
		if (isInEmergency(person)) {
			return true;
		}	
		
		return person.getPhysicalCondition().isEVAFit() 
				|| person.getPhysicalCondition().computeHealthScore() > 80;
	} 
	
	/**
	 * Checks if the person is physically fit for heavy EVA tasks.
	 *
	 * @param person
	 * @return
	 */
	public static boolean isSuperUnfit(Person person) {
		if (isInEmergency(person)) {
			return false;
		}	
		
		return person.isSuperUnfit();
	} 
	
	/**
	 * Checks if the person is physically fit for heavy EVA tasks.
	 *
	 * @return
	 */
	public boolean isSuperUnfit() {
		return isSuperUnfit(person);
	} 
	
	/**
	 * Is the person's settlement of interest in emergency ?
	 * 
	 * @return
	 */
	static boolean isInEmergency(Person person) {
		boolean isEmergency = false;
		// Note: in future, a person may travel to another settlement that's not his home town.
		//       Not correct calling getAssociatedSettlement() in that use case.
		Settlement s = person.getSettlement();
		if (s == null) {
			s = CollectionUtils.findSettlement(person.getCoordinates());
			if (s != null)
				isEmergency = s.getRationing().isAtEmergency();
		}
		else {
			isEmergency = s.getRationing().isAtEmergency();
		}
		return isEmergency;
	}
	
	/**
	 * Is the person's settlement of interest in emergency ?
	 * 
	 * @return
	 */
	private boolean isInEmergency() {
		return isInEmergency(person);
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
				result = s.getClosestWalkableEgressAirlock(worker);
			}
		}

		return result;
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
	 * Unloads any held equipment and resource back to a vehicle.
	 * 
	 * @param destination
	 */
	protected void returnEquipmentToVehicle(Vehicle destination) {
		// Return containers in rover Take a copy as the original will change.
		List<Equipment> held = new ArrayList<>(person.getEquipmentSet());
		for (Equipment e : held) {
			// Place this equipment within a rover outside on Mars
			if (e != null) {
				boolean done = e.transfer(destination);
				if (done) {
					logger.info(person, 5000, "Transferring " + e.getName() + " from person back to rover.");
					if (e instanceof Container c) {
						for (int resource: c.getAllAmountResourceStoredIDs()) {
							double amount = c.getSpecificAmountResourceStored(resource);
							if (amount > 0) {
								// Retrieve this amount from the container
								c.retrieveAmountResource(resource, amount);
								destination.storeAmountResource(resource, amount);
								logger.info(person, 5000, "Done unloading all resources from person back to rover.");
							}
						}
					}
				}
				else
					logger.warning(person, 5000, "Unable to transfer " + e.getName() + " from person back to rover.");
			}	
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
		BuildingManager.addPatientToMedicalBed(p, s);

		HealthProblem problem = p.getPhysicalCondition().getMostSerious();
		
		HistoricalEvent rescueEvent = null;
		
		if (problem == null) {
			rescueEvent = new MissionHistoricalEvent(EventType.MISSION_RESCUE_PERSON,
				p.getMission(),
				PhysicalConditionFormat.getHealthSituation(p.getPhysicalCondition()),
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
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		outsideSitePos = null;
		interiorObject = null;
		returnInsideLoc = null;
		
		super.destroy();
	}
}
