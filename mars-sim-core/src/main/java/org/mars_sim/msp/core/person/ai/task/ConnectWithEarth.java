/*
 * Mars Simulation Project
 * ConnectWithEarth.java
 * @date 2022-07-13
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Connection;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The ConnectWithEarth class is a task of connecting with Earth's family,
 * relatives and friends
 */
public class ConnectWithEarth extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(ConnectWithEarth.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.connectWithEarth"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase CONNECTING_EARTH = new TaskPhase(Msg.getString("Task.phase.connectingWithEarth")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.5D;

	// Data members
	private boolean proceed = false;
    /** Computing Units needed per millisol. */		
	private double computingNeeded;
	/** The seed value. */
    private double seed = RandomUtil.getRandomDouble(.005, 0.025);
    
	private final double TOTAL_COMPUTING_NEEDED;

	private Connection connection;
	
	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public ConnectWithEarth(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, RandomUtil.getRandomDouble(5, 15));

		TOTAL_COMPUTING_NEEDED = getDuration() * seed;
		computingNeeded = TOTAL_COMPUTING_NEEDED;
		
		if (person.isInSettlement()) {
			// set the boolean to true so that it won't be done again today
//			person.getPreference().setTaskDue(this, true);
			
			// If person is in a settlement, try to find an comm facility.
			Building bldg = BuildingManager.getAvailableCommBuilding(person);
			if (bldg != null) {
				// Walk to the facility.
				walkToTaskSpecificActivitySpotInBuilding(bldg, FunctionType.COMMUNICATION, false);
			} 
			
			else {
				// Find an admin facility.
				bldg = BuildingManager.getAvailableAdminBuilding(person);
				if (bldg != null) {
					// Walk to the facility.
					walkToTaskSpecificActivitySpotInBuilding(bldg, FunctionType.ADMINISTRATION, false);
				} 
				
				else {
					// Go back to his quarters
					Building quarters = person.getQuarters();
					if (quarters != null) {
						walkToBed(quarters, person, true);
					}
				}
			}
			
			proceed = true;
			
		} 
		
		else if (person.isInVehicle()) {
			if (person.getVehicle() instanceof Rover) {
				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);

				// set the boolean to true so that it won't be done again today
//				person.getPreference().setTaskDue(this, true);
			}
			
			proceed = true;
		} 

		if (proceed) {
			connection = person.getPreference().getRandomConnection();
			setDescription(connection.getName());
			// Initialize phase
			addPhase(CONNECTING_EARTH);
			setPhase(CONNECTING_EARTH);
		}
	}

	@Override
	public double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (CONNECTING_EARTH.equals(getPhase())) {
			return connectingEarth(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the connecting with earth phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double connectingEarth(double time) {
		double remainingTime = time - standardPulseTime;
		
		if (isDone()) {
        	// this task has ended
    		logger.info(person, 30_000L, connection.getName() + " - "
    				+ Math.round((TOTAL_COMPUTING_NEEDED - computingNeeded) * 100.0)/100.0 
    				+ " CUs Used.");
			endTask();
			return time;
		}
		
		int msol = marsClock.getMillisolInt();       
        boolean successful = false; 

        if (computingNeeded > 0) {
        	double workPerMillisol = 0; 
 
        	if (computingNeeded <= seed) {
        		workPerMillisol = standardPulseTime * computingNeeded;
        	}
        	else {
        		workPerMillisol = standardPulseTime * seed * RandomUtil.getRandomDouble(.9, 1.1);
        	}

        	// Submit request for computing resources
        	Computation center = person.getAssociatedSettlement().getBuildingManager()
        			.getMostFreeComputingNode(workPerMillisol, msol + 1, (int)(msol + getDuration()));
        	if (center != null) {
        		if (computingNeeded <= seed)
        			successful = center.scheduleTask(workPerMillisol, msol + 1, msol + 2);
        		else
        			successful = center.scheduleTask(workPerMillisol, msol + 1, (int)(msol + getDuration()));
        	}
	    	else
	    		logger.info(person, 30_000L, "No computing centers available for " + connection.getName() + ".");
        	
        	if (successful) {
        		if (computingNeeded <= seed)
        			computingNeeded = computingNeeded - workPerMillisol;
        		else
        			computingNeeded = computingNeeded - workPerMillisol * getDuration();
        		if (computingNeeded < 0) {
        			computingNeeded = 0; 
        		}
          	}
	    	else {
	    		logger.info(person, 30_000L, "No computing resources for " + connection.getName() + ".");
	    	}
        }
        else if (computingNeeded <= 0) {
        	// this task has ended
    		logger.info(person, 30_000L, connection.getName() + " - "
    				+ Math.round(TOTAL_COMPUTING_NEEDED * 100.0)/100.0 
    				+ " CUs Used.");
        	endTask();
        }
        
		return remainingTime;
	}
}
