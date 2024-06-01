/*
 * Mars Simulation Project
 * ConnectOnline.java
 * @date 2023-08-31
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import java.util.Collections;
import java.util.List;

import com.mars_sim.core.computing.ComputingJob;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Connection;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
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
	private static final ExperienceImpact IMPACT = new ExperienceImpact(1D, null, false, -0.5, Collections.emptySet());
	
	private Connection connection;

	private ComputingJob compute;
	
	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public ConnectOnline(Person person) {
		// Use Task constructor.
		super(NAME, person, false, IMPACT, RandomUtil.getRandomDouble(5, 25));

		connection = person.getPreference().getRandomConnection();

		if (person.isInSettlement()) {
			// set the boolean to true so that it won't be done again today			
			List<FunctionType> types = List.of(FunctionType.COMMUNICATION, 
											FunctionType.ADMINISTRATION,
											FunctionType.MANAGEMENT,
											FunctionType.RECREATION,
											FunctionType.DINING);
			
			Building selected = null;			
			for (var type : types) {
				// Find a facility.
				var bldg = BuildingManager.getAvailableFunctionTypeBuilding(person, type);
				if (bldg != null) {
					// Walk to the facility
					walkToTaskSpecificActivitySpotInBuilding(bldg, type, false);
					selected = bldg;
					break;
				} 
			}
			
			// Go back to his bed
			if (selected == null) {
				if (person.hasBed()) {
					// Walk to the bed
					walkToBed(person, true);
				}
				else {
					endTask();
					return;
				}	
			}
		}
		else if (person.isInVehicle() && (person.getVehicle() instanceof Rover r)) {
			// Walk to the passenger spot
			walkToPassengerActivitySpotInRover(r, true);
		}
		else {
			endTask();
			return;
		}

		compute = new ComputingJob(person.getAssociatedSettlement(), getDuration(), NAME);
		
		// Note: this task can be done in principle anywhere using tablets and handheld device
		// but preferably it will look for a suitable location first
		setDescription(connection.getName());
		// Initialize phase
		addPhase(CONNECTING_ONLINE);
		setPhase(CONNECTING_ONLINE);
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
		
		if (isDone() || getTimeCompleted() + time > getDuration() || compute.isCompleted()) {
        	// this task has ended
	  		logger.fine(person, 30_000L, NAME + " - " 
    				+ Math.round(compute.getConsumed() * 100.0)/100.0 
    				+ " CUs Used.");
			endTask();
			return time;
		}
		
		compute.consumeProcessing(time, getMarsTime());

        // Add experience
        addExperience(time);
        
		return remainingTime;
	}
}
