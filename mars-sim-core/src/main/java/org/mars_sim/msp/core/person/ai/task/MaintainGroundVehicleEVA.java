/**
 * Mars Simulation Project
 * MaintainGroundVehicleEVA.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The MaintainGroundVehicleEVA class is a task for performing
 * preventive maintenance on ground vehicles outside a settlement.
 */
public class MaintainGroundVehicleEVA
extends EVAOperation
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(MaintainGroundVehicleEVA.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainGroundVehicleEVA"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase MAINTAIN_VEHICLE = new TaskPhase(Msg.getString(
            "Task.phase.maintainVehicle")); //$NON-NLS-1$

    // Data members.
    /** Vehicle to be maintained. */
    private Vehicle vehicle;
    /** The settlement where the maintenance takes place. */  
    private Settlement settlement;

    /**
     * Constructor.
     * 
     * @param person the person to perform the task
     */
    public MaintainGroundVehicleEVA(Person person) {
        super(NAME, person, true, 25, SkillType.MECHANICS);

//		if (shouldEndEVAOperation()) {
//        	if (person.isOutside())
//        		setPhase(WALK_BACK_INSIDE);
//        	else
//        		endTask();
//        	return;
//        }
		
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}
		
     	settlement = CollectionUtils.findSettlement(person.getCoordinates());
     	if (settlement == null) {
        	return;
     	}
     	
        // Choose an available needy ground vehicle.
        vehicle = getNeedyGroundVehicle(person);
        if (vehicle != null) {
        	// Add the rover to a garage if possible.
			if (settlement.getBuildingManager().addToGarage(vehicle)) {
				// no need of doing EVA
	        	if (person.isOutside())
	        		setPhase(WALK_BACK_INSIDE);
	        	else
	        		endTask();
	        	return;
			}
			
            vehicle.setReservedForMaintenance(true);
            vehicle.addStatus(StatusType.MAINTENANCE);
            // Determine location for maintenance.
            Point2D maintenanceLoc = determineMaintenanceLocation();
            setOutsideSiteLocation(maintenanceLoc.getX(), maintenanceLoc.getY());
            
            // Initialize phase.
            addPhase(MAINTAIN_VEHICLE);

            logger.finest(person.getName() + " started MaintainGroundVehicleEVA task.");
        }
        else {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
        }
    }

    /**
     * Determine location to perform vehicle maintenance.
     * 
     * @return location.
     */
    private Point2D determineMaintenanceLocation() {

        Point2D.Double newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 50) && !goodLocation; x++) {
            Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(vehicle, 1D);
            newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(),
                    boundedLocalPoint.getY(), vehicle);
            goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
                    person.getCoordinates());
        }

        return newLocation;
    }

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return MAINTAIN_VEHICLE;
    }

    @Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);
        if (!isDone()) {
	        if (getPhase() == null) {
	            throw new IllegalArgumentException("Task phase is null");
	        }
	        else if (MAINTAIN_VEHICLE.equals(getPhase())) {
	            time = maintainVehiclePhase(time);
	        }
        }
        return time;
    }

    /**
     * Perform the maintain vehicle phase of the task.
     * 
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double maintainVehiclePhase(double time) {

        // Check for radiation exposure during the EVA operation.
        if (isDone() || isRadiationDetected(time)){
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        }
        
        // Check if there is a reason to cut short and return.
        if (shouldEndEVAOperation() || addTimeOnSite(time)){
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        }
        
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}
				
		// NOTE: if a person is not at a settlement or near its vicinity,  
		if (settlement == null || vehicle == null) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return 0;
		}
		
		if (settlement.getBuildingManager().isInGarage(vehicle)) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return 0;
		}
		
        MalfunctionManager manager = vehicle.getMalfunctionManager();
        boolean malfunction = manager.hasMalfunction();
        boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() == 0D);
        if (finishedMaintenance) {
        	vehicle.setReservedForMaintenance(false);
            vehicle.removeStatus(StatusType.MAINTENANCE);
        }
        
        if (finishedMaintenance || malfunction || shouldEndEVAOperation() ||
                addTimeOnSite(time)) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return 0;
        }

        // Determine effective work time based on "Mechanic" and "EVA Operations" skills.
        double workTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) workTime /= 2;
        if (skill > 1) workTime += workTime * (.2D * skill);

        // Add repair parts if necessary.
        Inventory inv = settlement.getInventory();
        if (Maintenance.hasMaintenanceParts(inv, vehicle)) {
            Map<Integer, Integer> parts = new HashMap<>(manager.getMaintenanceParts());
            Iterator<Integer> j = parts.keySet().iterator();
            while (j.hasNext()) {
            	Integer part = j.next();
                int number = parts.get(part);
                inv.retrieveItemResources(part, number);
                manager.maintainWithParts(part, number);
                
				// Add item demand
				inv.addItemDemandTotalRequest(part, number);
				inv.addItemDemand(part, number);
            }
        }
        else {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return 0;
        }

        // Add work to the maintenance
        manager.addMaintenanceWorkTime(workTime);

        // Add experience points
        addExperience(time);

        // Check if an accident happens during maintenance.
        checkForAccident(time);

        return 0D;
    }

    @Override
    protected void checkForAccident(double time) {

        // Use EVAOperation checkForAccident() method.
        super.checkForAccident(time);

        // Mechanic skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
        checkForAccident(vehicle, time, 0.001D, skill, vehicle.getName());
    }

    /**
     * Gets the vehicle  the person is maintaining.
     * Returns null if none.
     * 
     * @return entity
     */
    public Malfunctionable getVehicle() {
        return vehicle;
    }

    /**
     * Gets all ground vehicles requiring maintenance that are parked outside the settlement.
     *
     * @param person person checking.
     * @return collection of ground vehicles available for maintenance.
     */
    public static Collection<Vehicle> getAllVehicleCandidates(Person person) {
        Collection<Vehicle> result = new ConcurrentLinkedQueue<Vehicle>();

        Settlement settlement = person.getSettlement();
        if (settlement != null) {
            Iterator<Vehicle> vI = settlement.getParkedVehicles().iterator();
            while (vI.hasNext()) {
                Vehicle vehicle = vI.next();
                if (vehicle instanceof GroundVehicle && !vehicle.isReservedForMission()) {
                    result.add(vehicle);
                }
            }
        }

        return result;
    }

    /**
     * Gets a ground vehicle that requires maintenance in a local garage.
     * Returns null if none available.
     * 
     * @param person person checking.
     * @return ground vehicle
     * @throws Exception if error finding needy vehicle.
     */
    private Vehicle getNeedyGroundVehicle(Person person) {

        Vehicle result = null;

        // Find all vehicles that can be maintained.
        Collection<Vehicle> availableVehicles = getAllVehicleCandidates(person);

        // Populate vehicles and probabilities.
        Map<Vehicle, Double> vehicleProb = new HashMap<Vehicle, Double>(availableVehicles.size());
        Iterator<Vehicle> i = availableVehicles.iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            if (!vehicle.getSettlement().getBuildingManager().addToGarage(vehicle)) {
	            double prob = getProbabilityWeight(vehicle);
	            if (prob > 0D) {
	                vehicleProb.put(vehicle, prob);
	            }
			}
        }

        // Randomly determine needy vehicle.
        if (!vehicleProb.isEmpty()) {
            result = RandomUtil.getWeightedRandomObject(vehicleProb);
                   
            if (result != null) {
            	
	            if (settlement.getBuildingManager().addToGarage(result)) {
	            	result = null;
	            }
	            else {
	                setDescription(Msg.getString("Task.description.maintainGroundVehicleEVA.detail",
	                        result.getName())); //$NON-NLS-1$
	            }
	        }
        }

        return result;
    }

    /**
     * Gets the probability weight for a vehicle.
     * 
     * @param vehicle the vehicle.
     * @return the probability weight.
     * @throws Exception if error determining probability weight.
     */
    private double getProbabilityWeight(Vehicle vehicle) {
		double result = 0D;
		MalfunctionManager manager = vehicle.getMalfunctionManager();
		boolean tethered = vehicle.isBeingTowed() || (vehicle.getTowingVehicle() != null);
		if (tethered)
			return 0;
		
		boolean hasMalfunction = manager.hasMalfunction();
		if (hasMalfunction)
			return 0;

		boolean hasParts = false;
		if (person != null) {
			hasParts = Maintenance.hasMaintenanceParts(person, vehicle);
		} else {
			hasParts = Maintenance.hasMaintenanceParts(robot, vehicle);
		}
		if (!hasParts)
			return 0;
		
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		boolean minTime = (effectiveTime >= 1000D);

		if (minTime) {
			result = effectiveTime;
		}
		return result;
    }
}
