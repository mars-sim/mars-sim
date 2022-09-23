/*
 * Mars Simulation Project
 * MaintainEVAVehicleMeta.java
 * @date 2022-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.MaintainEVAVehicle;
import org.mars_sim.msp.core.person.ai.task.MaintainGarageVehicle;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the MaintainEVAVehicle task.
 */
public class MaintainEVAVehicleMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainEVAVehicle"); //$NON-NLS-1$
    
	private static final int CAP = 3_000;
	
    public MaintainEVAVehicleMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setPreferredJob(JobType.MECHANICS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new MaintainEVAVehicle(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        // Determine if settlement has a garage.
       	if (person.isInSettlement() || person.isRightOutsideSettlement()) {
            
        	Settlement settlement = person.getAssociatedSettlement();
        	
        	// Check for radiation events
        	boolean[] exposed = settlement.getExposed();

    		if (exposed[2]) {// SEP can give lethal dose of radiation
    			return 0;
    		}

            // Check if an airlock is available
            if (EVAOperation.getWalkableAvailableAirlock(person, false) == null)
	    		return 0;

            // Check if it is night time.
			if (EVAOperation.isGettingDark(person))
				return 0;
			
            // Checks if the person's settlement is at meal time and is hungry
            if (EVAOperation.isHungryAtMealTime(person))
            	return 0;
            
            // Checks if the person is physically fit for heavy EVA tasks
    		if (!EVAOperation.isEVAFit(person))
    			return 0;	
        	
			// Determine if settlement has available space in garage.
			int available = 0;
			int totalCap = 0;
			
			List<Building> garages = settlement.getBuildingManager().getBuildings(FunctionType.VEHICLE_MAINTENANCE);
			
			Iterator<Building> j = garages.iterator();
			while (j.hasNext()) {
				try {
					Building building = j.next();
					VehicleMaintenance garage = building.getVehicleParking();
					totalCap += garage.getVehicleCapacity() + garage.getFlyerCapacity();
					available += garage.getAvailableCapacity() + garage.getAvailableFlyerCapacity();
				} catch (Exception e) {
				}
			}
			
			if (available > 0) {
				// The garage space is still available, 
				// no need to do EVA to maintain vehicle.
				return 0D;
			}
			
			int total = settlement.getOwnedVehicleNum(); 
			int onMission = settlement.getMissionVehicleNum();
			
       		if (total - onMission - totalCap > 0) {
				
       			double score = person.getPhysicalCondition().computeHealthScore();
       			
	            // Get all vehicles needing maintenance.
                Iterator<Vehicle> i = MaintainGarageVehicle.getAllVehicleCandidates(person, true).iterator();
                while (i.hasNext()) {
                    double entityProb = i.next().getMalfunctionManager().getEffectiveTimeSinceLastMaintenance() / 50D;
                    if (entityProb > 1000) {
                        entityProb = 1000;
                    }
                    result += entityProb * score / 50;
                }
                		
	            int num = settlement.getIndoorPeopleCount();
                result = result 
                		+ result * num / settlement.getPopulationCapacity() / 4D
                		+ result * (total - onMission - totalCap) / 2.5;

                result *= settlement.getGoodsManager().getTransportationFactor();

                double shiftBonus = person.getTaskSchedule().obtainScoreAtStartOfShift();
                
                // Encourage to get this task done early in a work shift
                result *= shiftBonus / 10;
                
	            result = applyPersonModifier(result, person);
	            
	        	if (exposed[0]) {
	    			result = result/2D;// Baseline can give a fair amount dose of radiation
	    		}
	
	        	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
	    			result = result/4D;
	    		}
	
	            if (result < 0D) {
	                result = 0D;
	            }         
       		}
        }
 
        if (result > CAP)
        	result = CAP;
        
        return result;
    }
}
