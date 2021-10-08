/*
 * Mars Simulation Project
 * EVAOperation.java
 * @date 2021-09-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalEvent;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.Conversion;
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
	private static SimLogger logger = SimLogger.getLogger(EVAOperation.class.getName());

	/** Task phases. */
	protected static final TaskPhase WALK_TO_OUTSIDE_SITE = new TaskPhase(
			Msg.getString("Task.phase.walkToOutsideSite")); //$NON-NLS-1$
	protected static final TaskPhase WALK_BACK_INSIDE = new TaskPhase(
			Msg.getString("Task.phase.walkBackInside")); //$NON-NLS-1$
	protected static final TaskPhase DROP_OFF_RESOURCE = new TaskPhase(
			Msg.getString("Task.phase.dropOffResource")); //$NON-NLS-1$
	protected static final TaskPhase WALK_TO_BIN = new TaskPhase(
			Msg.getString("Task.phase.walkToBin")); //$NON-NLS-1$
	
	private static final String WALK_BACK_INSIDE_DESCRIPTION = 
			Msg.getString("Task.description.walk.backInside");  //$NON-NLS-1$
	
	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;
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
	private double binXLoc;
	private double binYLoc;

	private LocalBoundedObject interiorObject;
	private Point2D returnInsideLoc;

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
				logger.warning(person, "Is supposed to be in a building but interiorObject is null.");
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
				logger.warning(person, "Is supposed to be in a vehicle inside a garage but interiorObject is null.");
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

		else if (person.isInVehicle()) {
			if (person.getVehicle() instanceof Rover) {
				interiorObject = (Rover) person.getVehicle();
				if (interiorObject == null) {
					logger.warning(person, "Is supposed to be in a vehicle but interiorObject is null.");
				}
				// Add task phases.
				addPhase(WALK_TO_OUTSIDE_SITE);
				addPhase(WALK_BACK_INSIDE);

				// Set initial phase.
				setPhase(WALK_TO_OUTSIDE_SITE);
			} else {
				logger.severe(person, "Not in a rover vehicle: " + person.getVehicle());
			}
		}
	}

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
	
	/**
	 * Set the outside side local location.
	 * 
	 * @param xLoc the X location.
	 * @param yLoc the Y location.
	 */
	protected void setBinLocation(double xLoc, double yLoc) {
		binXLoc = xLoc;
		binYLoc = yLoc;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (person.isOutside()) {
			if (!person.isFit()) {
				setPhase(WALK_BACK_INSIDE);
			}
			else
				person.addEVATime(getTaskName(), time);
		}
			
		if (getPhase() == null) {
			throw new IllegalArgumentException("EVAOoperation's task phase is null");
		} else if (WALK_TO_OUTSIDE_SITE.equals(getPhase())) {
			return walkToOutsideSitePhase(time);
		} else if (WALK_BACK_INSIDE.equals(getPhase())) {
			return walkBackInsidePhase(time);
//		} else if (WALK_TO_BIN.equals(getPhase())) {
//			return walkToBin(time);
//		} else if (DROP_OFF_RESOURCE.equals(getPhase())) {
//			return dropOffResource(time);
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
	      // If not at field work site location, create walk outside subtask.
//        Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
//        Point2D outsideLocation = new Point2D.Double(outsideSiteXLoc, outsideSiteYLoc);
//        boolean closeToLocation = LocalAreaUtil.areLocationsClose(personLocation, outsideLocation); 
       
        if (person.isInside()) {// || !closeToLocation) {
        	// A person is walking toward an airlock or inside an airlock
//        	System.out.println(person + " is still inside " + person.getBuildingLocation() + ".");
            if (Walk.canWalkAllSteps(person, outsideSiteXLoc, outsideSiteYLoc, 0, null)) {
                Task walkingTask = new Walk(person, outsideSiteXLoc, outsideSiteYLoc, 0, null);
//                System.out.println(person + " added a walking task.");
                addSubTask(walkingTask);
            }
            else {
				logger.severe(person, "Cannot walk to outside site.");			
                endTask();
            }
        }
        else {
//        	System.out.println(person + " is outside.");
        	// In case of DigLocalRegolith, set to task phase COLLECT_REGOLITH
            setPhase(getOutsideSitePhase());
        }
        
        return time;
    }		

    private double walkToBin(double time) {
    	// Go to the drop off location
        if (!person.isOutside()) {
            if (Walk.canWalkAllSteps(person, binXLoc, binYLoc, 0, null)) {
                Task walkingTask = new Walk(person, binXLoc, binYLoc, 0, null);
                addSubTask(walkingTask);
            }
            else {
				logger.severe(person, "Cannot walk to the storage bin location.");			
                endTask();
            }
        }
        else {
            setPhase(WALK_TO_BIN);
        }
        
        return time;
    }
    
    private double dropOffResource(double time) {
    	// Go to the drop off location
    	return 0;
    }
    
	/**
	 * Perform the walk back inside phase.
	 * 
	 * @param time the time to perform the phase.
	 * @return remaining time after performing the phase.
	 */
	private double walkBackInsidePhase(double time) {
		    
		if (person.isOutside()) {
			
			setDescription(WALK_BACK_INSIDE_DESCRIPTION);
			
			if (interiorObject == null) {
			// Get closest airlock building at settlement.
				Settlement s = CollectionUtils.findSettlement(person.getCoordinates());
				if (s != null) {
					interiorObject = (Building)(s.getClosestAvailableAirlock(person).getEntity()); 
//					System.out.println("interiorObject is " + interiorObject);
					if (interiorObject == null)
						interiorObject = (LocalBoundedObject)(s.getClosestAvailableAirlock(person).getEntity());
//					System.out.println("interiorObject is " + interiorObject);
					logger.log(person, Level.FINE, 0,
//							"In " + person.getImmediateLocation()
							"Found " + ((Building)interiorObject).getNickName()
							+ " as the closet building with an airlock to enter.");
				}
				else {
					// near a vehicle
					Rover r = (Rover)person.getVehicle();
					interiorObject = (LocalBoundedObject) (r.getAirlock()).getEntity();
					logger.log(person, Level.INFO, 0,
							"Near " + r.getName()
							+ ". Had to walk back inside the vehicle.");
				}
			}
			
			if (interiorObject == null) {
				logger.log(person, Level.SEVERE, 0, "Trying to walk somewhere. interiorObject is null.");
				addSubTask(new Walk(person));
//				logger.log(person, Level.WARNING, 0,
////					"Near " + person.getImmediateLocation()
////					"At (" + Math.round(returnInsideLoc.getX()*10.0)/10.0 + ", " 
////					+ Math.round(returnInsideLoc.getY()*10.0)/10.0 + ") "
//					"InteriorObject is null.");
//				endTask();
			}
			
			else {
				// Set return location.
				Point2D rawReturnInsideLoc = LocalAreaUtil.getRandomInteriorLocation(interiorObject);
				returnInsideLoc = LocalAreaUtil.getLocalRelativeLocation(rawReturnInsideLoc.getX(),
						rawReturnInsideLoc.getY(), interiorObject);
				
				if (returnInsideLoc != null && 
						!LocalAreaUtil.isLocationWithinLocalBoundedObject(
								returnInsideLoc.getX(),	returnInsideLoc.getY(), interiorObject)) {
					
					logger.log(person, Level.SEVERE, 0, "Trying to walk somewhere. returnInsideLoc failed.");
					addSubTask(new Walk(person));
					
//					logger.log(person, Level.WARNING, 0,
//							"Near " + ((Building)interiorObject).getNickName() //person.getImmediateLocation()
//							+ " at (" + Math.round(returnInsideLoc.getX()*10.0)/10.0 + ", " 
//							+ Math.round(returnInsideLoc.getY()*10.0)/10.0 + ") "
//							+ ". Could not get inside " + interiorObject + ".");
//					endTask();
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
						
				logger.log(person, Level.FINE, 10_000, 
							"Near " +  name 
							+ " at (" + Math.round(returnInsideLoc.getX()*10.0)/10.0 + ", " 
							+ Math.round(returnInsideLoc.getY()*10.0)/10.0 
							+ "). Attempting to enter the airlock.");
				
				if (Walk.canWalkAllSteps(person, returnInsideLoc.getX(), returnInsideLoc.getY(), 0, interiorObject)) {
					Task walkingTask = new Walk(person, returnInsideLoc.getX(), returnInsideLoc.getY(), 0, interiorObject);
					addSubTask(walkingTask);
				} 
				
				else {
					logger.log(person, Level.SEVERE, 0, "Trying to walk somewhere. cannot walk all steps.");
					addSubTask(new Walk(person));
//					logger.log(person, Level.SEVERE, 0, 
//							Conversion.capitalize(person.getTaskDescription().toLowerCase()) 
//							+ ". Cannot find a valid path to enter airlock.");
//					endTask();
				}
			}
			
			else {
				logger.log(person, Level.SEVERE, 0, "Trying to walk somewhere. interiorObject is null or close to returnInsideLoc.");
				addSubTask(new Walk(person));
//				logger.log(person, Level.SEVERE, 0, 
//						Conversion.capitalize(person.getTaskDescription().toLowerCase() )
//						+ " and cannot find the building airlock to walk back inside. Will see what to do.");
//				endTask();
			}
		}
		
		else { // if a person is already inside, end the task gracefully here
			logger.log(person, Level.FINE, 4_000, 
					"Walked back inside. Ended '" + Conversion.capitalize(person.getTaskDescription().toLowerCase()) + "'.");	
			endTask();
		}
		

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
		if (endEVA)
			return true;

		// Check for sunlight
		if (isGettingDark(person))
			return true;
				
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
	 * Checks if the sky is dimming and is at dusk
	 * 
	 * @param person
	 * @return
	 */
	public static boolean isGettingDark(Person person) {

        return surfaceFeatures.getTrend(person.getCoordinates()) < 0 &&
                hasLittleSunlight(person);
    }
	
	
	/**
	 * Checks if there is any sunlight
	 * 
	 * @param person
	 * @return
	 */
	public static boolean hasLittleSunlight(Person person) {

		// Check if it is night time.
        return !(surfaceFeatures.getSolarIrradiance(person.getCoordinates()) < 12D)
                || surfaceFeatures.inDarkPolarRegion(person.getCoordinates());
    }
	
	/**
	 * Checks if there is an EVA problem for a person.
	 * 
	 * @param person the person.
	 * @return false if having EVA problem.
	 */
	public static boolean hasEVAProblem(Person person) {
		boolean result = false;
		EVASuit suit = person.getSuit();//(EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
		if (suit == null) {
//			logger.log(person, Level.WARNING, 20_000,
//					"Ended " + person.getTaskDescription() + " : no EVA suit is available.");
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
//				return true;
			}

			// Check if life support system in suit is working properly.
			if (!suit.lifeSupportCheck()) {
				logger.log(person, Level.WARNING, 20_000,
						person.getTaskDescription() + " ended : " + suit.getName()
								+ " failed the life support check.");
				result = true;
			}
		} catch (Exception e) {
			logger.log(person, Level.WARNING, 20_000,
					person.getTaskDescription() + " ended : " + suit.getName() + " failed the system check.", e);
		}

		// Check if suit has any malfunctions.
		if (suit.getMalfunctionManager().hasMalfunction()) {
			logger.log(person, Level.WARNING, 20_000, 
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

	public static boolean hasEVAProblem(Robot robot) {
		return true;
	}

	/**
	 * Checks if the person's settlement is at meal time and is hungry
	 * 
	 * @param person
	 * @return
	 */
	public static boolean isHungryAtMealTime(Person person) {

        return CookMeal.isLocalMealTime(person.getCoordinates(), 15) && person.getPhysicalCondition().isHungry();
    }
	
	/**
	 * Checks if the person's settlement is physically drained
	 * 
	 * @param person
	 * @return
	 */
	public static boolean isExhausted(Person person) {

        return person.getPhysicalCondition().isHungry() || person.getPhysicalCondition().isThirsty()
                || person.getPhysicalCondition().isSleepy() || person.getPhysicalCondition().isStressed();
    }
	
	/**
	 * Add experience for this EVA task. The EVA_OPERATIONS skill is updated.
	 * If the {@link #getPhase()} matches the value of {@link #getOutsideSitePhase()} then experience is also added
	 * to the outsideSkill property defined for this task. 
	 * If the phase is not outside; then only EVA_OPERATIONS is updated.
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
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
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
	 * Check for accident with EVA suit.
	 * 
	 * @param time the amount of time on EVA (in millisols)
	 */
	protected void checkForAccident(double time) {

		if (person != null) {
			EVASuit suit = person.getSuit();
			if (suit != null) {

				// EVA operations skill modification.
				int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
				checkForAccident(suit, time, BASE_ACCIDENT_CHANCE, skill, "EVA operation");
			}
		}
	}

	/**
	 * Check for radiation exposure of the person performing this EVA.
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

		if (person.isInSettlement()) {
			result = person.getSettlement().getClosestWalkableAvailableAirlock(person, xLocation, yLocation);
		} 
		
		else if (person.isInVehicle()) {
			Vehicle vehicle = person.getVehicle();
			if (vehicle instanceof Airlockable) {
				result = ((Airlockable) vehicle).getAirlock();
			}
		}
		
		return result;
	}

	public static Airlock getClosestWalkableAvailableAirlock(Robot robot, double xLocation, double yLocation) {
		Airlock result = null;

		if (robot.isInSettlement()) {
			result = robot.getSettlement().getClosestWalkableAvailableAirlock(robot, xLocation, yLocation);
		} 
		
		else if (robot.isInVehicle()) {
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

	/**
	 * Gets an available airlock to a given location that has a walkable path from
	 * the robot's current location.
	 * 
	 * @param robot the robot.
	 * @return airlock or null if none available
	 */
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
	public static void rescueOperation(Rover r, Person p, Settlement s) {
		
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
//		Complaint complaint = p.getPhysicalCondition().getMostSerious();
		HealthProblem problem = null;
		for (HealthProblem hp : problems) {
			if (problem.equals(hp))
				problem = hp;
		}
		
		// Register the historical event
		HistoricalEvent rescueEvent = new MedicalEvent(p, problem, EventType.MEDICAL_RESCUE);
		registerNewEvent(rescueEvent);
	}
}
