/*
 * Mars Simulation Project
 * MaintainEVAVehicle.java
 * @date 2022-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The MaintainEVAVehicle class is a task for performing
 * preventive maintenance on ground vehicles on the surface of Mars.
 */
public class MaintainEVAVehicle
extends EVAOperation
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static final Logger logger = Logger.getLogger(MaintainEVAVehicle.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainEVAVehicle"); //$NON-NLS-1$

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
     * @param target
     */
    public MaintainEVAVehicle(Person person, Vehicle target) {
        super(NAME, person, true, 25, SkillType.MECHANICS);

		if (!person.isNominallyFit()) {
			checkLocation();
        	return;
		}
		
     	settlement = CollectionUtils.findSettlement(person.getCoordinates());
     	if (settlement == null) {
        	return;
     	}
     	
        // Choose an available needy ground vehicle.
        vehicle = target;
        if (vehicle.isReservedForMaintenance()) {
            clearTask(vehicle.getName() + " already reserved for EVA maintenance");
            checkLocation();
            return;
        }

        // Add the rover to a garage if possible.
        if (settlement.getBuildingManager().addToGarage(vehicle)) {
            // no need of doing EVA
            checkLocation();
            return;
        }
        
        vehicle.setReservedForMaintenance(true);
        vehicle.addSecondaryStatus(StatusType.MAINTENANCE);
        setDescription(Msg.getString("Task.description.maintainEVAVehicle.detail", vehicle.getName()));

        // Determine location for maintenance.
        setOutsideLocation(vehicle);
        
        // Initialize phase.
        addPhase(MAINTAIN_VEHICLE);

        logger.finest(person.getName() + " started maintainEVAVehicle task.");
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
    	double remainingTime = 0;
    	
		if (checkReadiness(time, true) > 0)
			return remainingTime;
				
		// NOTE: if a person is not at a settlement or near its vicinity,  
		if (settlement == null || vehicle == null) {
			checkLocation();
			return time;
		}
		
		if (settlement.getBuildingManager().isInGarage(vehicle)) {
			checkLocation();
			return time;
		}
		
        MalfunctionManager manager = vehicle.getMalfunctionManager();
        boolean malfunction = manager.hasMalfunction();
        boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() == 0D);
        
        if (finishedMaintenance || malfunction || shouldEndEVAOperation(true) ||
                addTimeOnSite(time)) {
        	checkLocation();
			return remainingTime;
        }

        // Determine effective work time based on "Mechanic" and "EVA Operations" skills.
        double workTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) workTime /= 2;
        if (skill > 1) workTime += workTime * (.2D * skill);

        // Add repair parts if necessary.
        if (manager.hasMaintenanceParts(settlement)) {
            manager.transferMaintenanceParts(settlement);
        }
        else {
        	checkLocation();
			return remainingTime;
        }

        // Add work to the maintenance
        manager.addMaintenanceWorkTime(time);

        // Add experience points
        addExperience(time);

        // Check if an accident happens during maintenance.
        checkForAccident(time);

        return remainingTime;
    }


    /**
     * Release the vehicle
     */
	@Override
	protected void clearDown() {
        if (vehicle != null) {
        	vehicle.setReservedForMaintenance(false);
            vehicle.removeSecondaryStatus(StatusType.MAINTENANCE);
        }
		super.clearDown();
	}
	
    @Override
    protected void checkForAccident(double time) {

        // Use EVAOperation checkForAccident() method.
        super.checkForAccident(time);

        // Mechanic skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
        checkForAccident(vehicle, time, 0.001D, skill, vehicle.getName());
    }
}
