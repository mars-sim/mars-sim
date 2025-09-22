/*
 * Mars Simulation Project
 * LoadVehicleEVA.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The LoadVehicleEVA class is a task for loading a vehicle with fuel and
 * supplies when the vehicle is outside.
 */
public class LoadVehicleEVA extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.loadVehicleEVA"); //$NON-NLS-1$

	/** Simple Task name */
	public static final String SIMPLE_NAME = LoadVehicleEVA.class.getSimpleName();
	
	public static final String DETAIL = Msg.getString("Task.description.loadVehicleEVA.detail") + " ";
	
	/** Task phases. */
	private static final TaskPhase LOADING = 
			new TaskPhase(Msg.getString("Task.phase.loading"), //$NON-NLS-1$
					createPhaseImpact(PhysicalEffort.HIGH));


	// Data members
	/** The vehicle that needs to be loaded. */
	private Vehicle vehicle;
	/** The person's settlement instance. */
	private Settlement settlement;

	private LoadingController loadingPlan;

	
	/**
	 * Constructor
	 * 
	 * @param person            the person performing the task.
	 * @param vehicle           vehicle to load via EVA
	 */
	public LoadVehicleEVA(Person person, Vehicle vehicle) {
		// Use Task constructor.
		super(NAME, person, 20D + RandomUtil.getRandomInt(5) - RandomUtil.getRandomInt(5), LOADING);

		setMinimumSunlight(LightLevel.NONE);

		if (isSuperUnfit()) {
			endEVA("Super Unfit.");
			return;
		}
		
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
		
		settlement = person.getSettlement();
		if (settlement == null) {
			endTask();
			return;
		}

		this.vehicle = vehicle;
		this.loadingPlan = vehicle.getLoadingPlan();
		if (loadingPlan == null) {
			// Mission must be done
			endEVA("Vehicle is null.");
			return;
		}

		setDescription(DETAIL + vehicle.getName());

		// Add the rover to a garage if possible.
		if (settlement.getBuildingManager().addToGarage(vehicle)) {
			// no need of doing EVA
			endEVA("Vehicle in garage.");
			return;
		}
		
		// Determine location for loading.
		setOutsideLocation(vehicle);
	}

	@Override
	protected double performMappedPhase(double time) {
		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
	            throw new IllegalArgumentException("Task phase is null");
			} else if (LOADING.equals(getPhase())) {
				time = loadingPhase(time);
			}
		}
		return time;
	}

	/**
	 * Perform the loading phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) after performing the phase.
	 */
	private double loadingPhase(double time) {
	
		if (settlement == null) {
			endEVA("Settlement is null.");
			return time;
		}

		if (vehicle == null) {
			endEVA("Vehicle is null.");
			return time;
		}
		
		if (checkReadiness(time) > 0) {
			return time;
		}
		
		// Check if the vehicle is in a garage
		if (settlement.getBuildingManager().isInGarage(vehicle)) {
			endEVA("Vehicle in garage.");
			return time;
		}
		
		// Load the resource
		if (loadingPlan.load(worker, time)) {
			endEVA("Loading plan fully executed.");
			return time;
		}

        // Add experience points
        addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);
		
		return 0;
	}

	/**
	 * Gets the vehicle being loaded.
	 * 
	 * @return vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}
}
