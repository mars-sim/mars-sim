/*
 * Mars Simulation Project
 * LoadVehicleEVA.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;


import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The LoadVehicleEVA class is a task for loading a vehicle with fuel and
 * supplies when the vehicle is outside.
 */
public class LoadVehicleEVA extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
//	private static SimLogger logger = SimLogger.getLogger(LoadVehicleEVA.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.loadVehicleEVA"); //$NON-NLS-1$

	/** Simple Task name */
	public static final String SIMPLE_NAME = LoadVehicleEVA.class.getSimpleName();
	
	/** Task phases. */
	private static final TaskPhase LOADING = new TaskPhase(Msg.getString("Task.phase.loading")); //$NON-NLS-1$


	// Data members
	/** The vehicle that needs to be loaded. */
	private Vehicle vehicle;
	/** The person's settlement instance. */
	private Settlement settlement;
	/** The vehicle mission instance. */
	private VehicleMission vehicleMission;

	private LoadingController loadingPlan;

	
	/**
	 * Constructor
	 * 
	 * @param person            the person performing the task.
	 * @param vehicle           the vehicle to be loaded.
	 * @param requiredResources a map of required resources to be loaded.
	 * @param optionalResources a map of optional resources to be loaded.
	 * @param requiredEquipment a map of required equipment to be loaded.
	 * @param optionalEquipment a map of optional equipment to be loaded.
	 */
	public LoadVehicleEVA(Person person, VehicleMission mission) {
		// Use Task constructor.
		super(NAME, person, true, 20D + RandomUtil.getRandomInt(5) - RandomUtil.getRandomInt(5), null);

		this.vehicleMission = mission;
		
		if (person.isSuperUnFit()) {
			checkLocation("Person is super unfit.");
        	return;
		}
		
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
		
		settlement = unitManager.findSettlement(person.getCoordinates());
		if (settlement == null) {
			endTask();
			return;
		}
		
		vehicle = vehicleMission.getVehicle();
		if (vehicle == null) {
			// Mission must be done
			checkLocation("Vehicle is null.");
			return;
		}

		setDescription(Msg.getString("Task.description.loadVehicleEVA.detail", vehicle.getName())); // $NON-NLS-1$

		// Add the rover to a garage if possible.
		if (settlement.getBuildingManager().addToGarage(vehicle)) {
			// no need of doing EVA
			checkLocation("Vehicle in garage.");
			return;
		}

		loadingPlan = vehicleMission.getLoadingPlan();
		
		// Determine location for loading.
		setOutsideLocation(vehicle);

		// Initialize task phase
		addPhase(LOADING);
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
			checkLocation("Settlement is null.");
			return time;
		}

		if (vehicle == null) {
			checkLocation("Vehicle is null.");
			return time;
		}
		
		if (checkReadiness(time, false) > 0) {
			return time;
		}
		
		// Check if the vehicle is in a garage
		if (settlement.getBuildingManager().isInGarage(vehicle)) {
			checkLocation("Vehicle in garage.");
			return time;
		}
		
		// Load the resource
		if (loadingPlan.load(worker, time)) {
			checkLocation("Loading plan fully executed.");
			return time;
		}
			
		person.getPhysicalCondition().stressMuscle(time);

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


	@Override
	protected TaskPhase getOutsideSitePhase() {
		return LOADING;
	}
}
