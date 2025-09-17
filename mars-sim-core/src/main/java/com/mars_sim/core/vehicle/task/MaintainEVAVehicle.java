/*
 * Mars Simulation Project
 * MaintainEVAVehicle.java
 * @date 2025-08-24
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;

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
	/** The modified skill level. */
	private int effectiveSkillLevel;
    /** Vehicle to be maintained. */
    private Vehicle vehicle;

    /**
     * Constructor.
     * 
     * @param person the person to perform the task
     * @param target
     */
    public MaintainEVAVehicle(Person person, Vehicle target) {
        super(NAME, person, AVERAGE_EVA_TIME + RandomUtil.getRandomDouble(80, 120), MAINTAIN_VEHICLE);

		if (isSuperUnfit()) {
			endEVA("Super Unfit.");
			return;
		}
		
        setMinimumSunlight(LightLevel.NONE);

     	var settlement = person.getAssociatedSettlement();
     	
        // Choose an available needy ground vehicle.
        vehicle = target;

        // Add the rover to a garage if possible.
        if (settlement.getBuildingManager().addToGarage(vehicle)) {
            // no need of doing EVA
        	endEVA("Vehicle in garage.");
            return;
        }
        
        vehicle.setReservedForMaintenance(true);
        vehicle.addSecondaryStatus(StatusType.MAINTENANCE);
        
        String des = DETAIL + vehicle.getName();
		setDescription(des);
		logger.info(person, 4_000, des + ".");
		
        // Determine location for maintenance.
        setOutsideLocation(vehicle);
    	
		// Determine the effective skill level
		effectiveSkillLevel = getEffectiveSkillLevel();
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
        var settlement = vehicle.getAssociatedSettlement();
		if (checkReadiness(time) > 0) {
			vehicle.setReservedForMaintenance(false);
            vehicle.removeSecondaryStatus(StatusType.MAINTENANCE);
			return time;	
		}
		
		if (settlement.getBuildingManager().isInGarage(vehicle)) {
			vehicle.setReservedForMaintenance(false);
            vehicle.removeSecondaryStatus(StatusType.MAINTENANCE);
			endEVA("Vehicle in garage.");
			return time;
		}
		
        MalfunctionManager manager = vehicle.getMalfunctionManager();
        boolean malfunction = manager.hasMalfunction();
 
		if (malfunction) {
			endEVA("Vehicle had malfunction.");
			return time * .75;
		}

        // Determine effective work time based on "Mechanic" and "EVA Operations" skills.
        double workTime = time;
        int skill = effectiveSkillLevel;
        if (skill == 0) workTime /= 2;
        if (skill > 1)
        	workTime = workTime * (1 + .25 * skill);
		
        double timeCompleted = getTimeCompleted();
        
        // At the beginning of maintenance, identify if anything needs to be replaced
        if (timeCompleted == 0.0) {
			// Inspect the entity
			manager.inspectEntityTrackParts(timeCompleted);
        }
        
		boolean doneInspection = false;
		
		// Check if maintenance has already been completed.
		boolean finishedMaintenance = manager.getEffectiveTimeSinceLastMaintenance() == 0D;

		if (!finishedMaintenance) {
			doneInspection = !manager.addInspectionMaintWorkTime(workTime);
		}
		
		if (finishedMaintenance || doneInspection || timeCompleted >= getDuration()) {
			// Reduce fatigue
			manager.reduceFatigue(timeCompleted * (1 + .25 * skill));
			
            vehicle.setReservedForMaintenance(false);
            
            vehicle.removeSecondaryStatus(StatusType.MAINTENANCE);
			// No more maintenance is needed
			endEVA("Inspection Done");
		}

        // Add experience points
        addExperience(time);

        // Check if an accident happens during maintenance.
        checkForAccident(time);

		// if work time is greater than time, then less time is spent on this frame
		return MathUtils.between((workTime - time), 0, time) * .5;
		// Note: 1. workTime can be longer or shorter than time
		//       2. the return time may range from zero to as much as half the tick  
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
