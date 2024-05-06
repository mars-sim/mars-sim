/*
 * Mars Simulation Project
 * MaintainEVAVehicle.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;

/**
 * The MaintainEVAVehicle class is a task for performing
 * preventive maintenance on ground vehicles on the surface of Mars.
 */
public class MaintainEVAVehicle extends EVAOperation {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static final SimLogger logger = SimLogger.getLogger(MaintainEVAVehicle.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainEVAVehicle"); //$NON-NLS-1$

    private static final String DETAIL = Msg.getString(
    		"Task.description.maintainEVAVehicle.detail") + " "; //$NON-NLS-1$
    
    /** Task phases. */
    private static final TaskPhase MAINTAIN_VEHICLE = new TaskPhase(Msg.getString(
            "Task.phase.maintainVehicle"), createPhaseImpact(SkillType.MECHANICS));

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
        super(NAME, person, 25, MAINTAIN_VEHICLE);

		if (!person.isNominallyFit()) {
			checkLocation("Not nominally fit.");
        	return;
		}
        setMinimumSunlight(LightLevel.NONE);

     	settlement = unitManager.findSettlement(person.getCoordinates());
     	if (settlement == null) {
        	return;
     	}
     	
        // Choose an available needy ground vehicle.
        vehicle = target;

        // Add the rover to a garage if possible.
        if (settlement.getBuildingManager().addToGarage(vehicle)) {
            // no need of doing EVA
            checkLocation("Vehicle in garage.");
            return;
        }
        
        vehicle.setReservedForMaintenance(true);
        vehicle.addSecondaryStatus(StatusType.MAINTENANCE);
        
        String des = DETAIL + vehicle.getName();
		setDescription(des);
		logger.info(person, 4_000, des + ".");
		
        // Determine location for maintenance.
        setOutsideLocation(vehicle);
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
				
		if (settlement == null) {
			checkLocation("Settlement is null.");
			return time;
		}

		if (vehicle == null) {
			checkLocation("Vehicle is null.");
			return time;
		}		
	   	
		if (checkReadiness(time) > 0)
			return time;
			
		if (settlement.getBuildingManager().isInGarage(vehicle)) {
			checkLocation("Vehicle in garage.");
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

		// Note: if parts don't exist, it simply means that one can still do the 
		// inspection portion of the maintenance with no need of replacing any parts
		boolean partsPosted = manager.hasMaintenancePartsInStorage(vehicle.getAssociatedSettlement());
		
		if (partsPosted) {
			Unit containerUnit = vehicle.getAssociatedSettlement();

			int shortfall = manager.transferMaintenanceParts((EquipmentOwner) containerUnit);
			
			if (shortfall == -1) {
				logger.warning(worker, 30_000L, "No spare parts available for maintenance on " 
						+ vehicle + ".");
			}
		}

        // Add work to the maintenance
        manager.addInspectionMaintWorkTime(time);

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
