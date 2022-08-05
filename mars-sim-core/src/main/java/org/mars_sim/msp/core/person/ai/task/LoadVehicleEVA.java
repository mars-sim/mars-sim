/*
 * Mars Simulation Project
 * LoadVehicleEVA.java
 * @date 2021-08-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;


import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public LoadVehicleEVA(Person person) {
		// Use Task constructor
		super(NAME, person, true, 20D + RandomUtil.getRandomInt(5) - RandomUtil.getRandomInt(5), null);
		
		if (!person.isFit()) {
			checkLocation();
        	return;
		}
		
		settlement = CollectionUtils.findSettlement(person.getCoordinates());
		if (settlement == null) {
			checkLocation();
        	return;
		}
		
		if (settlement.getBuildingManager().isInGarage(vehicle)) {
			checkLocation();
        	return;
		}
		
        if (!anyVehiclesNeedEVA(settlement)) {
        	checkLocation();
        	return;
        }

		vehicleMission = LoadVehicleGarage.getMissionNeedingLoading(settlement,
																	false);
		if (vehicleMission == null) {
			checkLocation();
			return;
		}		
			
		initLoad(person);
	}
	
	private void initLoad(Person starter) {	
		vehicle = vehicleMission.getVehicle();
		if (vehicle != null) {
			
			setDescription(Msg.getString("Task.description.loadVehicleEVA.detail", vehicle.getName())); // $NON-NLS-1$

			// Add the rover to a garage if possible.
			if (settlement.getBuildingManager().addToGarage(vehicle)) {
				// no need of doing EVA
				checkLocation();
	        	return;
			}

			loadingPlan = vehicleMission.prepareLoadingPlan(starter.getAssociatedSettlement());
			
			// Determine location for loading.
			setOutsideLocation(vehicle);

			// Initialize task phase
			addPhase(LOADING);
		}
		else {
			// no need of doing EVA
			checkLocation();
		}
	}

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
		
		if (!person.isFit()) {
			checkLocation();
        	return;
		}
		
		settlement = CollectionUtils.findSettlement(person.getCoordinates());
		if (settlement == null) {
			endTask();
			return;
		}

		if (settlement.getBuildingManager().isInGarage(vehicle)) {
			checkLocation();
        	return;
		}
		
		initLoad(person);
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
	
		boolean stopLoading = false;
		
		// Check for radiation exposure during the EVA operation.
		if (isDone() || isRadiationDetected(time)) {
			stopLoading = true;
		}

		stopLoading |= (shouldEndEVAOperation() || addTimeOnSite(time));
		
		// NOTE: if a person is not at a settlement or near its vicinity,  
		stopLoading |= (settlement == null || vehicle == null); 
		stopLoading |= settlement.getBuildingManager().isInGarage(vehicle);
		stopLoading |= !person.isFit();
		
		// Do the load
		if (!stopLoading) {
			stopLoading = loadingPlan.load(worker, time);
		}

		else {
			checkLocation();
			return time;
		}
		
        // Add experience points
        addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);
		
		return 0;
	}

	/**
	 * Checks if any vehicle need EVA operation
	 * 
	 * @param settlement
	 * @return
	 */
	private static boolean anyVehiclesNeedEVA(Settlement settlement) {

		for(Vehicle vehicle : settlement.getParkedVehicles()) {
			if (vehicle.isReservedForMission()
					&& !settlement.getBuildingManager().isInGarage(vehicle)) {
				return true;
			}
		}
		return false;
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
