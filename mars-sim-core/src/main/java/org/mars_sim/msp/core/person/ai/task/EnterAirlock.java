/**
 * Mars Simulation Project
 * EnterAirlock.java
 * @version 3.1.1 2020-07-22
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
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The EnterAirlock class is a Task for EVA ingress, namely, entering an airlock of a settlement or 
 * vehicle after an EVA operation outside have been accomplished.
 */
public class EnterAirlock extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EnterAirlock.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
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
	/** The inside airlock position. */
	private Point2D insideAirlockPos = null;
	/** The interior airlock position. */
	private Point2D interiorDoorPos = null;
	
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
		
		// Initialize data members
		setDescription(Msg.getString("Task.description.enterAirlock.detail", airlock.getEntityName())); // $NON-NLS-1$
		// Initialize task phase
		addPhase(WAITING_TO_ENTER_AIRLOCK);
		addPhase(ENTERING_AIRLOCK);
		addPhase(WAITING_INSIDE_AIRLOCK);
		addPhase(EXITING_AIRLOCK);
		addPhase(STORING_EVA_SUIT);

		setPhase(WAITING_TO_ENTER_AIRLOCK);
		
		LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
				+ " was starting the EVA ingress procedure in " + airlock.getEntityName() + ".");
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
		
		String loc = person.getLocationTag().getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		// If person is already inside, don't need the ingress. End the task.
		if (person.isInside()) {
			LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
					" was in the 'waiting to enter' phase for EVA ingress"
					+ " but was reportedly inside. End the Task.");
			
//			endTask();
			setPhase(EXITING_AIRLOCK);
			return remainingTime;
		}

		else {
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " " 
				+ loc + " was in the 'waiting to enter' phase for EVA ingress.");
		
		}
		 if ((Airlock.AirlockState.DEPRESSURIZED.equals(airlock.getState()) && !airlock.isOuterDoorLocked()) || 
	                airlock.inAirlock(person)) {
		// If airlock is depressurized and outer door unlocked, enter airlock.
	
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName,
				"[" + person.getLocationTag().getLocale() + "] The airlock chamber"// in " 
//				 + ((Building)(airlock.getEntity())).getNickName() 
				 + " had just been DEPRESSURIZED with the exterior door UNLOCKED for entry.");
			
			// Add experience
			addExperience(time - remainingTime);
			
			setPhase(ENTERING_AIRLOCK);
		}
		
		else { // The airlock is not being fully prepared for ingress yet.
			
			// Add person to queue awaiting airlock at inner door if not already.
			airlock.addAwaitingAirlockOuterDoor(person);

			loc = person.getLocationTag().getImmediateLocation();
			loc = loc == null ? "[N/A]" : loc;
			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
			
			// If airlock has not been activated, activate it.
			if (!airlock.isActivated()) {
				if (airlock.activateAirlock(person)) {
					
					LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
							"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
							+ " " + loc + 
							" pressed a button asking for activating the airlock in 'waiting to enter' phase.");

				}
				
				else {
					LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
							"[" + person.getLocationTag().getLocale() + "] " + person.getName()
							+ " " + loc + " failed to activate the airlock for EVA ingress.");
					
					endTask();
				}
			}
			
			else { // the airlock has activated
				LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName()
						+ " " + loc + " observed the airlock just got activated for ingress.");			
			}
			
			// Check if person is the airlock operator.
            if (person.equals(airlock.getOperator())) {
                // If person is airlock operator, add cycle time to airlock.
                double activationTime = remainingTime;
                if (airlock.getRemainingCycleTime() < remainingTime) {
                    remainingTime -= airlock.getRemainingCycleTime();
                }
                else {
                    remainingTime = 0D;
                }
                boolean activationSuccessful = airlock.addCycleTime(activationTime);
                if (!activationSuccessful) {
//                    logger.severe("Problem with airlock activation: " + person.getName());
					LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
							"[" + person.getLocationTag().getLocale() + "] "
							+ person.getName() + " " + loc 
							+ " had problems with airlock activation in the 'waiting to enter' phase.");
                }
            }
            else {
                // If person is not airlock operator, just wait.
    			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
    					"[" + person.getLocationTag().getLocale() + "] "
    					+ person.getName() + " " + loc 
    					+ " was not the operator in the 'waiting to enter' phase. Standby.");				
                remainingTime = 0D;
            }
		}
		 
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
		
		String loc = person.getLocationTag().getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
				+ " " + loc + " was in the 'entering the airlock' phase for EVA ingress.");
		
		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
		
		// If person is already inside, don't need the ingress. End the task.
		if (person.isInside()) {
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " already went inside " + loc + ". Ready to to 'waiting inside airlock' phase.");
			
			// Note: Should not end the task
//			endTask();
			
			setPhase(WAITING_INSIDE_AIRLOCK);
		}

		else if (airlock.inAirlock(person)) {
			
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() + 
					" just went inside the airlock chamber. Ready to go to 'waiting inside airlock' phase.");
			
			// Add experience
			addExperience(time - remainingTime);
			
			setPhase(WAITING_INSIDE_AIRLOCK);
		}
		
		else if (LocalAreaUtil.areLocationsClose(personLocation, insideAirlockPos)) {
			
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName()
					+ " came close enough to the reference point inside of the airlock chamber.");

			// Enter airlock from outside
			// Note: make sure the param inside is false
			if (airlock.enterAirlock(person, false)) {
				
				loc = person.getLocationTag().getImmediateLocation();
				loc = loc == null ? "[N/A]" : loc;
				loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
				
				LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] "
						+ person.getName() 
						+ " " + loc + " just entered the airlock chamber.");
				
				// If airlock has not been activated, activate it.
                if (!airlock.isActivated()) {
                    airlock.activateAirlock(person);
                }

				// Add experience
				addExperience(time - remainingTime);
				
				setPhase(WAITING_INSIDE_AIRLOCK);

			}
	        else {
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
	                }
	                else {
	                    remainingTime = 0D;
	                }
	                boolean activationSuccessful = airlock.addCycleTime(activationTime);
	                if (!activationSuccessful) {
//	                    logger.severe("Problem with airlock activation: " + person.getName());
	                    LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
								"[" + person.getLocationTag().getLocale() + "] "
								+ person.getName() + " " + loc 
								+ " had problems with airlock activation in the 'entering airlock' phase.");	                    
	                }
	                
	                else {
	        			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
	        					"[" + person.getLocationTag().getLocale() + "] "
	        					+ person.getName() + " " + loc 
	        					+ " completed the airlock activation in the 'entering airlock' phase. Standby.");
	                }
	            }
	            
	            else {
	                // If person is not airlock operator, just wait.
	    			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
	    					"[" + person.getLocationTag().getLocale() + "] "
	    					+ person.getName() + " " + loc 
	    					+ " was not the operator in the 'entering airlock' phase. Standby.");
	            			
	                remainingTime = 0D;
	            }
	        }
		}
		
		else {
			
			if (person.isOutside()) {
				// Walk to inside airlock position.
				addSubTask(new WalkOutside(person, person.getXLocation(), 
					person.getYLocation(), insideAirlockPos.getX(),
					insideAirlockPos.getY(), true));
				
				LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName()
						+ loc + " attempted to come closer to the reference point inside of the airlock.");
				
				setPhase(WAITING_TO_ENTER_AIRLOCK);
			
			}
		}

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

		String loc = person.getLocationTag().getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		if (airlock.inAirlock(person)) {

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
                }
                else {
                    remainingTime = 0D;
                }
                
                boolean activationSuccessful = airlock.addCycleTime(activationTime);
                if (!activationSuccessful) {
//                    logger.severe("Problem with airlock activation: " + person.getName());
					LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
							"[" + person.getLocationTag().getLocale() + "] "
							+ person.getName() + " " + loc 
							+ " had problems with airlock activation in the 'waiting inside airlock' phase.");
//					// Note: Calling endTask is needed or else this person would get stranded in the airlock 
//					// since this person fails to change his location state and is still inside
					endTask();
					// Note: go back to entering airlock phase won't work
//					setPhase(ENTERING_AIRLOCK);
//					return remainingTime;					
                }
                
                else {
        			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
        					"[" + person.getLocationTag().getLocale() + "] "
        					+ person.getName() + " " + loc 
        					+ " completed the airlock activation in the 'waiting inside airlock' phase. Standby.");
                }
            }
            
            else {
                // If person is not airlock operator, just wait.
    			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
    					"[" + person.getLocationTag().getLocale() + "] "
    					+ person.getName() + " " + loc 
    					+ " was not the operator in the 'waiting inside airlock' phase. Standby.");
                remainingTime = 0D;
            }        
        }
		
        else {
//            logger.finer(person + " is already internal during waiting inside airlock phase.");
			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] "
					+ person.getName() + " " + loc 
					+ " was stepping out of the airlock chamber. Ready to go to 'exiting airlock' phase.");
				
	        // Add experience
	        addExperience(time - remainingTime);
	        
            setPhase(EXITING_AIRLOCK);
        }
	      
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

		if (interiorDoorPos == null) {
			interiorDoorPos = airlock.getAvailableInteriorPosition();
		}

		if (airlock.inAirlock(person)) {

            // If airlock has not been activated, activate it.
            if (!airlock.isActivated()) {
                airlock.activateAirlock(person);
            }
		}
		
		// logger.finer(person + " exiting airlock inside.");
		LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
				+ " was about to come through the interior door to complete the ingress.");

		Point2D personLocation = new Point2D.Double(person.getXLocation(), person.getYLocation());
		
		if (LocalAreaUtil.areLocationsClose(personLocation, interiorDoorPos)) {
			
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " came close enough to the interior door. Ready to store the EVA suit next.");

			// Add experience
			addExperience(time - remainingTime);
			
			// This completes the task of ingress through the airlock
			setPhase(STORING_EVA_SUIT);	
		}
		
		else {// the person is NOT close enough to the interior door position
			String loc = person.getLocationTag().getImmediateLocation();
			loc = loc == null ? "[N/A]" : loc;
			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
						
	           // Walk to interior airlock position.
            if (airlock.getEntity() instanceof Building) {

                Building airlockBuilding = (Building) airlock.getEntity()
                		;
//                logger.finest(person + " exiting airlock inside " + airlockBuilding);
    			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
    					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
    					+ " " + loc + " Level.FINE,");
    			
                addSubTask(new WalkSettlementInterior(person, airlockBuilding, 
                        interiorDoorPos.getX(), interiorDoorPos.getY(), 0));
            }
            else if (airlock.getEntity() instanceof Rover) {

                Rover airlockRover = (Rover) airlock.getEntity();
                
//                logger.finest(person + " exiting airlock inside " + airlockRover);
    			LogConsolidated.log(logger, Level.FINE, 4000, sourceName, 
    					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
    					+ " " + loc + " will walk close to the interior door.");
    			
                addSubTask(new WalkRoverInterior(person, airlockRover, 
                        interiorDoorPos.getX(), interiorDoorPos.getY()));
            }
            

			// This completes the task of ingress through the airlock
			setPhase(STORING_EVA_SUIT);	
			
//			// Walk to inside airlock position.
//			addSubTask(new WalkOutside(person, person.getXLocation(), 
//				person.getYLocation(), interiorAirlockPos.getX(),
//				interiorAirlockPos.getY(), true));

			// Walk toward the reference point inside the airlock
//			walkToReference(loc);
		}

		return remainingTime;
	}

//	/**
//	 * Walks toward the reference inside of the airlock
//	 * 
//	 * @param loc
//	 */
//	private void walkToReference(String loc) {
//		if (airlock.getEntity() instanceof Building) {
//			
//			Building airlockBuilding = (Building) airlock.getEntity();
//
//			if (airlockBuilding != null) {
//
//				Building startBuilding = BuildingManager.getBuilding(person);
//				
//				if (startBuilding != null) {
//					
//					LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
//							"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
//							+ " attempted to step closer to the interior door of the airlock.");
//					
//					// Walk to interior airlock position.
//					addSubTask(new WalkSettlementInterior(person, airlockBuilding, interiorAirlockPos.getX(),
//							interiorAirlockPos.getY(), 0));
//				}
//				
//				else {
//					LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
//							"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
//							+ " was not inside a building. Ending the task.");
//					endTask();
//				}
//			} 
//			
//			else {
//				LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
//						"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
//						+ " was waiting to enter airlock but airlockBuilding is null.");
//				endTask();
//			}
//
//		} else if (airlock.getEntity() instanceof Rover) {
//
//			Rover airlockRover = (Rover) airlock.getEntity();
//			
//			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
//					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
//					+ " attempted to walk toward the internal airlock in " + airlockRover);
//			
//			addSubTask(new WalkRoverInterior(person, airlockRover, interiorAirlockPos.getX(),
//					interiorAirlockPos.getY()));
//		}
//	}
	
	/**
	 * Performs the storing EVA suit phase of the task.
	 * 
	 * @param time the amount of time to perform the task phase.
	 * @return the remaining time after performing the task phase.
	 */
	private double storingEVASuitPhase(double time) {

		double remainingTime = time;

//		EVASuit suit = (EVASuit) person.getInventory().findUnitOfClass(EVASuit.class); ////(EVASuit) person.getContainerUnit();
		// 5.1. Gets the suit instance
		EVASuit suit = person.getSuit(); 
		// 5.2 deregister the suit the person will take into the airlock to don
		person.registerSuit(null);		

		String loc = person.getLocationTag().getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		
		if (suit != null) {
			// 5.3 set the person as the owner
			suit.setLastOwner(person);
			
			Inventory suitInv = suit.getInventory();
		
			if (person.getContainerUnit() instanceof MarsSurface) {
				LogConsolidated.log(logger, Level.WARNING, 4000, sourceName,
						"[" + person.getLocationTag().getLocale() + "] "  
						+ person + " " + loc + " still had MarsSurface as the container unit.");
			}
			
			else {
				// Empty the EVA suit
				LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
						+ " was going to retrieve the O2 and H2O in " + suit.getName() + ".");
	
				Inventory entityInv = airlock.getEntityInventory();
				
				if (entityInv != null && suitInv != null) {
	
					// 5.4 Unloads the resources from the EVA suit to the entityEnv
					
					try {
						// Unload oxygen from suit.
						double oxygenAmount = suitInv.getAmountResourceStored(oxygenID, false);
						double oxygenCapacity = entityInv.getAmountResourceRemainingCapacity(oxygenID, true, false);
						if (oxygenAmount > oxygenCapacity)
							oxygenAmount = oxygenCapacity;
						
						suitInv.retrieveAmountResource(oxygenID, oxygenAmount);
						entityInv.storeAmountResource(oxygenID, oxygenAmount, true);
						entityInv.addAmountSupply(oxygenID, oxygenAmount);
		
					} catch (Exception e) {

						LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
								"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
								+ " " + loc
								+ " but was unable to retrieve/store oxygen : ", e);
//						endTask();
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

						LogConsolidated.log(logger, Level.WARNING, 4000, sourceName, 
								"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
								+ " " + loc
								+ " but was unable to retrieve/store water : ", e);
//						endTask();
					}
		
					// 5.5 Transfer the EVA suit from person to entityInv 
					suit.transfer(person, entityInv);	
			
					// Return suit to entity's inventory.
					LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
							"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
							+ " " + loc 
							+ " had just stowed away "  + suit.getName() + ".");
					
					// Add experience
					addExperience(remainingTime);

				}
			}
		}
		
		else { // the person doesn't have the suit
			
			LogConsolidated.log(logger, Level.WARNING, 4000, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " 
					+ person.getName() + " " + loc 
					+ " was supposed to put away an EVA suit but somehow did not have one.");
		}

		endTask();
		// NOTE: endTask() above is absolutely needed, or else it will call storingEVASuitPhase() again and again
		// for some unknown reasons, in eclipse, the workbench will crash without endTask() here.
		
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
		person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);

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
			LogConsolidated.log(logger, Level.FINER, 4000, sourceName, 
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
			LogConsolidated.log(logger, Level.SEVERE, 4000, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " concluded the airlock operator task.");
//					+  person.getLocationTag().getImmediateLocation() + ".");
			airlock.clearOperator();
		}

	}

	@Override
	public int getEffectiveSkillLevel() {
		return person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
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
