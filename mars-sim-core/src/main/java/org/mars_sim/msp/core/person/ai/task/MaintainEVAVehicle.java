/*
 * Mars Simulation Project
 * MaintainEVAVehicle.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.logging.Logger;

import org.mars.sim.tools.Msg;
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
public class MaintainEVAVehicle extends EVAOperation {

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
			checkLocation("Person not fit.");
        	return;
		}
		
     	settlement = unitManager.findSettlement(person.getCoordinates());
     	if (settlement == null) {
        	return;
     	}
     	
        // Choose an available needy ground vehicle.
        vehicle = target;
//        if (vehicle.isReservedForMaintenance()) {
//            clearTask(vehicle.getName() + " already reserved for EVA maintenance.");
//            checkLocation("Vehicle reserved for maintenance.");
//            return;
//        }

        // Add the rover to a garage if possible.
        if (settlement.getBuildingManager().addToGarage(vehicle)) {
            // no need of doing EVA
            checkLocation("Vehicle in garage.");
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
				
		if (settlement == null) {
			checkLocation("Settlement is null.");
			return time;
		}

		if (vehicle == null) {
			checkLocation("Vehicle is null.");
			return time;
		}
		
		if (settlement.getBuildingManager().isInGarage(vehicle)) {
			checkLocation("Vehicle in garage.");
			return time;
		}
		
        // Check if there is a reason to cut short and return.
		if (shouldEndEVAOperation(true)) {
			checkLocation("No sunlight.");
			return time;
		}
		
        // Check time on site
		if (addTimeOnSite(time)) {
			checkLocation("Time on site expired.");
			return time;
		}
		
        MalfunctionManager manager = vehicle.getMalfunctionManager();
        boolean malfunction = manager.hasMalfunction();
        boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() == 0D);

		if (malfunction) {
			checkLocation("Vehicle had malfunction. Quit maintenance.");
			return time;
		}
		
		if (finishedMaintenance) {
			checkLocation("Maintenance finished.");
			return time;
		}

        // Determine effective work time based on "Mechanic" and "EVA Operations" skills.
        double workTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) workTime /= 2;
        if (skill > 1) workTime += workTime * (.2D * skill);

		int shortfall = manager.transferMaintenanceParts(settlement);
		if (shortfall == -1) {
        	checkLocation("Part(s) not available.");
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
