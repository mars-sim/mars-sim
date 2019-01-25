/**
 * Mars Simulation Project
 * EnterAirlock.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The EnterAirlock class is a task for entering a airlock from an EVA
 * operation.
 */
public class EnterAirlock extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EnterAirlock.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
//    private static final double INGRESS_TIME = .05; // in millisols

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.enterAirlock"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase WAITING_TO_ENTER_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.waitingToEnterAirlock")); //$NON-NLS-1$
	private static final TaskPhase ENTERING_AIRLOCK = new TaskPhase(Msg.getString("Task.phase.enteringAirlock")); //$NON-NLS-1$
	private static final TaskPhase WAITING_INSIDE_AIRLOCK = new TaskPhase(
			Msg.getString("Task.phase.waitingInsideAirlock")); //$NON-NLS-1$
	private static final TaskPhase EXITING_AIRLOCK = new TaskPhase(Msg.getString("Task.phase.exitingAirlock")); //$NON-NLS-1$
	private static final TaskPhase STORING_EVA_SUIT = new TaskPhase(Msg.getString("Task.phase.storingEVASuit")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	// Data members
	/** The airlock to be used. */
	private Airlock airlock;
	private Point2D insideAirlockPos = null;
	private Point2D interiorAirlockPos = null;
	
	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;

	/**
	 * Constructor.
	 * 
	 * @param person  the person to perform the task
	 * @param airlock to be used.
	 */
	public EnterAirlock(Person person, Airlock airlock) {
		super(NAME, person, false, false, STRESS_MODIFIER, false, 0D);
		this.airlock = airlock;
		LogConsolidated.log(Level.FINER, 0, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
				+ " was starting to enter " + airlock.getEntityName());
		// Initialize data members
		setDescription(Msg.getString("Task.description.enterAirlock.detail", airlock.getEntityName())); // $NON-NLS-1$
		// Initialize task phase
		addPhase(WAITING_TO_ENTER_AIRLOCK);
		addPhase(ENTERING_AIRLOCK);
		addPhase(WAITING_INSIDE_AIRLOCK);
		addPhase(EXITING_AIRLOCK);
		addPhase(STORING_EVA_SUIT);

		setPhase(WAITING_TO_ENTER_AIRLOCK);
	}


	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time (millisols) the phase is to be performed.
	 * @return the remaining time (millisols) after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (WAITING_TO_ENTER_AIRLOCK.equals(getPhase())) {
			return waitingToEnterAirlockPhase(time);
		} else if (ENTERING_AIRLOCK.equals(getPhase())) {
			return enteringAirlockPhase(time);
		} else if (WAITING_INSIDE_AIRLOCK.equals(getPhase())) {
			return waitingInsideAirlockPhase(time);
		} else if (EXITING_AIRLOCK.equals(getPhase())) {
			return exitingAirlockPhase(time);
		} else if (STORING_EVA_SUIT.equals(getPhase())) {
			return storingEVASuitPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the waiting to enter airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double waitingToEnterAirlockPhase(double time) {

		double remainingTime = time;

		// Waiting to enter. But not allowed to enter yet.
		LogConsolidated.log(Level.FINER, 0, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
				" was waiting to enter airlock from outside in " + person.getLocationTag().getImmediateLocation());
		
		// If person is already inside, change to exit airlock phase.
		if (person.isInside()) {
			setPhase(EXITING_AIRLOCK);
			return remainingTime;
		}

		// If airlock is depressurized and outer door unlocked, enter airlock.
		if ((Airlock.DEPRESSURIZED.equals(airlock.getState()) && !airlock.isOuterDoorLocked())
				|| airlock.inAirlock(person)) {
			setPhase(ENTERING_AIRLOCK);
		} else {
			// Add person to queue awaiting airlock at inner door if not already.
			airlock.addAwaitingAirlockOuterDoor(person);

			// If airlock has not been activated, activate it.
			if (!airlock.isActivated()) {
				airlock.activateAirlock(person);
			}

			// Check if person is the airlock operator.
			if (person.equals(airlock.getOperator())) {
				// If person is airlock operator, add cycle time to airlock.
				double activationTime = remainingTime;
				if (airlock.getRemainingCycleTime() < remainingTime) {
					remainingTime -= airlock.getRemainingCycleTime();
				} else {
					remainingTime = 0D;
				}
				boolean activationSuccessful = airlock.addCycleTime(activationTime);
				if (!activationSuccessful) {
					LogConsolidated.log(Level.WARNING, 0, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " + person.getName() +
							" had problem with airlock activation.");
				}
			} else {
				// If person is not airlock operator, just wait.
				remainingTime = 0D;
			}
		}

		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the enter airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return remaining time after performing task phase.
	 */
	private double enteringAirlockPhase(double time) {

		double remainingTime = time;

		if (insideAirlockPos == null) {
			insideAirlockPos = airlock.getAvailableAirlockPosition();
		}

		LogConsolidated.log(Level.FINER, 0, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
				" was about to enter the airlock from outside.");
		
		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());

		if (airlock.inAirlock(person)) {
			LogConsolidated.log(Level.FINER, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
					" was about to enter airlock, but found already in the airlock.");
			setPhase(WAITING_INSIDE_AIRLOCK);
		} 
		
		else if (person.isInside()) { 
			// WARNING: calling this incorrectly can potentially halt the simulation
			LogConsolidated.log(Level.WARNING, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
					" was about to enter the airlock from outside, but was already inside the settlement. End task.");
			endTask();
		} 
		
		else if (LocalAreaUtil.areLocationsClose(personLocation, insideAirlockPos)) {

			// Enter airlock.
			if (airlock.enterAirlock(person, false)) {

				// If airlock has not been activated, activate it.
				if (!airlock.isActivated()) {
					airlock.activateAirlock(person);
				}
				LogConsolidated.log(Level.FINER, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
						" had entered the airlock in " + person.getLocationTag().getImmediateLocation());

				setPhase(WAITING_INSIDE_AIRLOCK);
			} else {
				// If airlock has not been activated, activate it.
				if (!airlock.isActivated()) {
					airlock.activateAirlock(person);
				}

				// Check if person is the airlock operator.
				if (person.equals(airlock.getOperator())) {
					// If person is airlock operator, add cycle time to airlock.
					double activationTime = remainingTime;
					if (airlock.getRemainingCycleTime() < remainingTime) {
						remainingTime -= airlock.getRemainingCycleTime();
					} else {
						remainingTime = 0D;
					}
					boolean activationSuccessful = airlock.addCycleTime(activationTime);
					if (!activationSuccessful) {
						LogConsolidated.log(Level.WARNING, 0, sourceName,
								"[" + person.getLocationTag().getLocale() + "] " + person.getName() +
								" had problem with airlock activation.");
					}
				} else {
					// If person is not airlock operator, just wait.
					remainingTime = 0D;
				}
			}
		} else if (person.isOutside()) {
			// Walk to inside airlock position.
			addSubTask(new WalkOutside(person, person.getXLocation(), person.getYLocation(), insideAirlockPos.getX(),
					insideAirlockPos.getY(), true));
		}


		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the waiting inside airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double waitingInsideAirlockPhase(double time) {

		double remainingTime = time;

		// waiting inside
		LogConsolidated.log(Level.FINER, 0, sourceName,
				"[" + person.getLocationTag().getLocale() + "] "
  						+ person + " was waiting inside airlock. ");
		// NOTE: person.getLocationTag().getImmediateLocation() will NOT reported correctly
		// since it doesn't track intermediate state such as in the airlock.
		
		if (airlock.inAirlock(person)) {

			// Check if person is the airlock operator.
			if (person.equals(airlock.getOperator())) {

				// If airlock has not been activated, activate it.
				if (!airlock.isActivated()) {
					airlock.activateAirlock(person);
				}

				// If person is airlock operator, add cycle time to airlock.
				double activationTime = remainingTime;
				if (airlock.getRemainingCycleTime() < remainingTime) {
					remainingTime -= airlock.getRemainingCycleTime();
				} else {
					remainingTime = 0D;
				}
				
				boolean activationSuccessful = airlock.addCycleTime(activationTime);
				if (!activationSuccessful) {
					LogConsolidated.log(Level.WARNING, 0, sourceName, "[" + person.getLocationTag().getLocale() + "] "
							+ person.getName() + " has problems with airlock activation.");
				}
			} 
			
			else {
				// If person is not airlock operator, just wait.
				remainingTime = 0D;
			}
		} 
		
		else {
			LogConsolidated.log(Level.FINER, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " was in " + person.getLocationTag().getImmediateLocation() + 
					" and somehow was not inside its airlock.");
			setPhase(EXITING_AIRLOCK);
		}

		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the exit airlock phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double exitingAirlockPhase(double time) {

		double remainingTime = time;

		if (interiorAirlockPos == null) {
			interiorAirlockPos = airlock.getAvailableInteriorPosition();
		}

		// logger.finer(person + " exiting airlock inside.");
		LogConsolidated.log(Level.FINER, 0, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
				+ " was trying to exit an airlock in exitingAirlockPhase().");

		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
		
		if (LocalAreaUtil.areLocationsClose(personLocation, interiorAirlockPos)) {
			LogConsolidated.log(Level.FINER, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " had exited the airlock and going to store the EVA suit");

			setPhase(STORING_EVA_SUIT);
			
		} else {

			// Walk to interior airlock position.
			if (airlock.getEntity() instanceof Building) {

				Building airlockBuilding = (Building) airlock.getEntity();

				if (airlockBuilding != null) {

					Building startBuilding = BuildingManager.getBuilding(person);
					if (startBuilding != null) {
						LogConsolidated.log(Level.FINER, 0, sourceName, 
								"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
								+ " was walking from " + startBuilding + " to an airlock at " + airlockBuilding);
						addSubTask(new WalkSettlementInterior(person, airlockBuilding, interiorAirlockPos.getX(),
								interiorAirlockPos.getY()));
					} else {
						LogConsolidated.log(Level.FINER, 0, sourceName, 
								"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
								+ " was not inside a building");
						endTask();
					}
				} else {
					LogConsolidated.log(Level.WARNING, 0, sourceName, 
							"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
							+ " was waiting to enter airlock but airlockBuilding is null");
				}

//				 logger.finest(person + " exiting airlock inside " + airlockBuilding);
//				 addSubTask(new WalkSettlementInterior(person, airlockBuilding,
//				 interiorAirlockPos.getX(), interiorAirlockPos.getY()));
			} else if (airlock.getEntity() instanceof Rover) {

				Rover airlockRover = (Rover) airlock.getEntity();
				LogConsolidated.log(Level.FINER, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
						+ " was walking to an airlock in " + airlockRover);
				addSubTask(new WalkRoverInterior(person, airlockRover, interiorAirlockPos.getX(),
						interiorAirlockPos.getY()));
			}
		}

		// Add experience
		addExperience(time - remainingTime);

		return remainingTime;
	}

	/**
	 * Performs the storing EVA suit phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double storingEVASuitPhase(double time) {

		double remainingTime = time;

		EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class);
		if (suit != null) {
			
			Inventory suitInv = suit.getInventory();
			Inventory personInv = person.getInventory();
			
			if (person.getContainerUnit() instanceof MarsSurface) {
				LogConsolidated.log(Level.WARNING, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] "  
									+ person + " had no container. Location state type : " 
									+ person.getLocationStateType());
				endTask();
			}
			else {
				// Empty the EVA suit
				LogConsolidated.log(Level.FINER, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
						+ " was going to retrieve the O2 and H2O in " + suit.getName());
	
				Inventory entityInv = null;
				
				if (person.getContainerUnit().getInventory() != null) {
	
					try {
						entityInv = person.getContainerUnit().getInventory();
						// Warning : if person.getContainerUnit().getInventory() is null, the simulation hang up
						// person.getContainerUnit() instanceof MarsSurface may alleviate this situation
						
						// Unload oxygen from suit.
						double oxygenAmount = suitInv.getAmountResourceStored(oxygenID, false);
						double oxygenCapacity = entityInv.getAmountResourceRemainingCapacity(oxygenID, true, false);
						if (oxygenAmount > oxygenCapacity)
							oxygenAmount = oxygenCapacity;
						
						suitInv.retrieveAmountResource(oxygenID, oxygenAmount);
						entityInv.storeAmountResource(oxygenID, oxygenAmount, true);
						entityInv.addAmountSupply(oxygenID, oxygenAmount);
		
					} catch (Exception e) {
						LogConsolidated.log(Level.WARNING, 0, sourceName, 
								"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
								+ " in " + person.getLocationTag().getImmediateLocation() 
								+ " but was unable to retrieve/store oxygen : ", e);
						endTask();
					}
		
					// Unload water from suit.
					double waterAmount = suitInv.getAmountResourceStored(waterID, false);
					double waterCapacity = entityInv.getAmountResourceRemainingCapacity(waterID, true, false);
					if (waterAmount > waterCapacity)
						waterAmount = waterCapacity;
					
					try {
						suitInv.retrieveAmountResource(waterID, waterAmount);
						entityInv.storeAmountResource(waterID, waterAmount, true);
						entityInv.addAmountSupply(waterID, waterAmount);
		
					} catch (Exception e) {
						LogConsolidated.log(Level.WARNING, 0, sourceName, 
								"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
								+ " in " + person.getLocationTag().getImmediateLocation() 
								+ " but was unable to retrieve/store water : ", e);
						endTask();
					}
		
		//			 logger.finer(person.getName() + " putting away EVA suit into " +
		//			 entity.getName());
					personInv.retrieveUnit(suit);
		//			 suit.setLastOwner(person);
					entityInv.storeUnit(suit);
					// Return suit to entity's inventory.
					LogConsolidated.log(Level.FINER, 0, sourceName, 
							"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
							+ " in " + person.getLocationTag().getImmediateLocation() 
							+ " had just stowed away "  + suit.getName() + ".");
					
					// Add experience
					addExperience(remainingTime);
					
					endTask();
					// NOTE: endTask() above is absolutely needed, or else it will call storingEVASuitPhase() again and again
				}
			}
		}
		
		else {
			LogConsolidated.log(Level.WARNING, 0, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " 
					+ person.getName() + " did NOT have an EVA suit to put away in "
							+ person.getLocationTag().getImmediateLocation());
			endTask();
		}

		return remainingTime;
	}

	/**
	 * Adds experience to the person's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {

		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;

		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);

	}

	/**
	 * Checks if a person can enter an airlock from an EVA.
	 * 
	 * @param person  the person trying to enter
	 * @param airlock the airlock to be used.
	 * @return true if person can enter the airlock
	 */
	public static boolean canEnterAirlock(Person person, Airlock airlock) {

		boolean result = true;

		if (person.isInside()) {
			LogConsolidated.log(Level.FINER, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " could not enter airlock to " + airlock.getEntityName()
					+ " due to not being outside.");
			result = false;
		}

		return result;
	}

	@Override
	public void endTask() {
		super.endTask();

		// Clear the person as the airlock operator if task ended prematurely.
		if ((airlock != null) && person.equals(airlock.getOperator())) {
			LogConsolidated.log(Level.SEVERE, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " was ending entering airlock task prematurely, clearing as airlock operator for "
					+ airlock.getEntityName());
			airlock.clearOperator();
		}

	}

	@Override
	public int getEffectiveSkillLevel() {
		return person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.EVA_OPERATIONS);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();

		airlock = null;
	}
}