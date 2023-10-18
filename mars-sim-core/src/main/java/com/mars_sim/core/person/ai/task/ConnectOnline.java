/*
 * Mars Simulation Project
 * ConnectOnline.java
 * @date 2023-08-31
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import java.util.List;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Connection;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The ConnectOnline class is a task of connecting online.
 */
public class ConnectOnline extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(ConnectOnline.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.connectOnline"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase CONNECTING_ONLINE = new TaskPhase(Msg.getString("Task.phase.connectingOnline")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.5D;

	// Data members
	private boolean proceed = false;
    /** Computing Units needed per millisol. */		
	private double computingNeeded;
	/** The seed value. */
    private final double seed = RandomUtil.getRandomDouble(.005, 0.025);
    
	private final double TOTAL_COMPUTING_NEEDED;

	private final Connection connection = person.getPreference().getRandomConnection();
	
	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public ConnectOnline(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, RandomUtil.getRandomDouble(5, 25));

		TOTAL_COMPUTING_NEEDED = getDuration() * seed;
		computingNeeded = TOTAL_COMPUTING_NEEDED;
		
		if (person.isInSettlement()) {
			// set the boolean to true so that it won't be done again today
//			person.getPreference().setTaskDue(this, true);
			
			List<FunctionType> types = List.of(FunctionType.COMMUNICATION, 
											FunctionType.ADMINISTRATION,
											FunctionType.MANAGEMENT,
											FunctionType.RECREATION,
											FunctionType.DINING);
			
			Building bldg = null;
			int size = types.size();
			
			for (int i = 0; i < size; i++) {
				// Find a facility.
				bldg = BuildingManager.getAvailableFunctionTypeBuilding(person, types.get(i));
				if (bldg != null) {
					// Walk to the facility
					walkToTaskSpecificActivitySpotInBuilding(bldg, types.get(i), false);
					proceed = true;
					break;
				} 
			}
			
			if (bldg != null) {
				// Go back to his quarters
				Building quarters = person.getQuarters();
				if (quarters != null) {
					// Walk to the bed
					walkToBed(quarters, person, true);	
					proceed = true;
				}
			}
		}
		
		else if (person.isInVehicle()) {
			if (person.getVehicle() instanceof Rover r) {
				// Walk to the passenger spot
				walkToPassengerActivitySpotInRover(r, true);
				proceed = true;
			}
		}
		
		// Note: this task can be done in principle anywhere using tablets and handheld device
		// but preferably it will look for a suitable location first
//		proceed = true;

		if (proceed) {
			setDescription(connection.getName());
			// Initialize phase
			addPhase(CONNECTING_ONLINE);
			setPhase(CONNECTING_ONLINE);
		}
		else {
			endTask();
		}
	}

	@Override
	public double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (CONNECTING_ONLINE.equals(getPhase())) {
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
		double remainingTime = 0;
		
		if (isDone() || getTimeCompleted() + time > getDuration() || computingNeeded <= 0) {
        	// this task has ended
	  		logger.fine(person, 30_000L, NAME + " - " 
    				+ Math.round((TOTAL_COMPUTING_NEEDED - computingNeeded) * 100.0)/100.0 
    				+ " CUs Used.");
			endTask();
			return time;
		}
		
		int msol = getMarsTime().getMillisolInt(); 
              
        computingNeeded = person.getAssociatedSettlement().getBuildingManager().
            	accessNode(person, computingNeeded, time, seed, 
            			msol, getDuration(), NAME);

        // Add experience
        addExperience(time);
        
		return remainingTime;
	}
}
