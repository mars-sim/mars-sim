/*
 * Mars Simulation Project
 * LoadVehicleEVA.java
 * @date 2021-08-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The LoadVehicleEVA class is a task for loading a vehicle with fuel and
 * supplies when the vehicle is outside.
 */
public class LoadVehicleEVA extends EVAOperation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(LoadVehicleEVA.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.loadVehicleEVA"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase LOADING = new TaskPhase(Msg.getString("Task.phase.loading")); //$NON-NLS-1$

	/**
	 * The amount of resources (kg) one person of average strength can load per
	 * millisol.
	 */
	private static final double WATER_NEED = 10D;
	private static final double OXYGEN_NEED = 10D;

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
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}

		settlement = CollectionUtils.findSettlement(person.getCoordinates());
		if (settlement == null) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}
		
        if (!anyVehiclesNeedEVA(settlement)) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
        }

		vehicleMission = LoadVehicleGarage.getMissionNeedingLoading(person.getSettlement(),
																	false);
		if (vehicleMission == null) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
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
	        	if (starter.isOutside())
	        		setPhase(WALK_BACK_INSIDE);
	        	else
	        		endTask();
	        	return;
			}

			loadingPlan = vehicleMission.getLoadingPlan();
			
			// Determine location for loading.
			Point2D loadingLoc = determineLoadingLocation();
			setOutsideSiteLocation(loadingLoc.getX(), loadingLoc.getY());

			// Initialize task phase
			addPhase(LOADING);
		}
		else {
			// no need of doing EVA
        	if (starter.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
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
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}
		
		settlement = CollectionUtils.findSettlement(person.getCoordinates());
		if (settlement == null) {
			endTask();
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

		stopLoading = stopLoading || (shouldEndEVAOperation() || addTimeOnSite(time));
		
		// NOTE: if a person is not at a settlement or near its vicinity,  
		stopLoading = stopLoading || (settlement == null || vehicle == null); 
		stopLoading = stopLoading || settlement.getBuildingManager().isInGarage(vehicle);
		stopLoading = stopLoading || !person.isFit();
		
		// Do the load
		if (!stopLoading) {
			stopLoading = loadingPlan.load(worker, time);
		
	        // Add experience points
	        addExperience(time);
	
			// Check for an accident during the EVA operation.
			checkForAccident(time);
			
			// Used all time
			time = 0;
		}

		if (stopLoading) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		return 0;
	}

	/**
	 * Checks if any vehicle need EVA operation
	 * 
	 * @param settlement
	 * @return
	 */
	private static boolean anyVehiclesNeedEVA(Settlement settlement) {

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if (vehicle.isReservedForMission()) { 
				if (!settlement.getBuildingManager().isInGarage(vehicle)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Gets a list of rovers with crew who are missing EVA suits.
	 * 
	 * @param settlement the settlement.
	 * @return list of rovers.
	 */
	public static List<Rover> getRoversNeedingEVASuits(Settlement settlement) {

		List<Rover> result = new ArrayList<>();

		for(Vehicle vehicle : settlement.getParkedVehicles()) {
			if (vehicle instanceof Rover) {
				Rover rover = (Rover) vehicle;
				if (!settlement.getBuildingManager().addToGarage(vehicle)) {
					Inventory roverInv = rover.getInventory();
					int peopleOnboard = roverInv.findNumUnitsOfClass(Person.class);
					if ((peopleOnboard > 0)) {
						int numSuits = roverInv.findNumEVASuits(false, false);
						double water = roverInv.getAmountResourceStored(ResourceUtil.waterID, false);
						double oxygen = roverInv.getAmountResourceStored(ResourceUtil.oxygenID, false);
						if ((numSuits == 0) || (water < WATER_NEED) || (oxygen < OXYGEN_NEED)) {
							result.add(rover);
						}
					}
				}
			}
		}

		return result;
	}



	/**
	 * Gets the vehicle being loaded.
	 * 
	 * @return vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Determine location to load the vehicle.
	 * 
	 * @return location.
	 */
	private Point2D determineLoadingLocation() {

		Point2D.Double newLocation = null;
		boolean goodLocation = false;
		for (int x = 0; (x < 50) && !goodLocation; x++) {
			Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(vehicle, 1D);
			newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), boundedLocalPoint.getY(),
					vehicle);
			goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
					worker.getCoordinates());
		}

		return newLocation;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return LOADING;
	}
}
