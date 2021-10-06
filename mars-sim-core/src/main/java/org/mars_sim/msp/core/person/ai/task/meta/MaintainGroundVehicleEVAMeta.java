/**
 * Mars Simulation Project
 * MaintainGroundVehicleEVAMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the MaintainGroundVehicleEVA task.
 */
public class MaintainGroundVehicleEVAMeta extends MetaTask {


    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainGroundVehicleEVA"); //$NON-NLS-1$
    
    public MaintainGroundVehicleEVAMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setPreferredJob(JobType.MECHANIICS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new MaintainGroundVehicleEVA(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        // Determine if settlement has a garage.
       	if (person.isInSettlement() || person.isRightOutsideSettlement()) {

            if (!person.getPhysicalCondition().isFitByLevel(500, 50, 500))
            	return 0;
            
        	Settlement settlement = person.getAssociatedSettlement();
        	
        	// Check for radiation events
        	boolean[] exposed = settlement.getExposed();

    		if (exposed[2]) {// SEP can give lethal dose of radiation
    			return 0;
    		}

            // Check if an airlock is available
            if (EVAOperation.getWalkableAvailableAirlock(person) == null)
	    		return 0;

            // Check if it is night time.
			if (EVAOperation.isGettingDark(person))
				return 0;
			
            // Checks if the person's settlement is at meal time and is hungry
            if (EVAOperation.isHungryAtMealTime(person))
            	return 0;
            
            // Checks if the person is physically drained
			if (EVAOperation.isExhausted(person))
				return 0;     	
        	
			// Determine if settlement has available space in garage.
			int available = 0;
			int totalCap = 0;
			
			List<Building> garages = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE);
			
			Iterator<Building> j = garages.iterator();
			while (j.hasNext()) {
				try {
					Building building = j.next();
					VehicleMaintenance garage = building.getGroundVehicleMaintenance();
					totalCap += garage.getVehicleCapacity();
					available += (garage.getVehicleCapacity() - garage.getCurrentVehicleNumber());
				} catch (Exception e) {
				}
			}
			
			if (available > 0) {
				// The garage space is still available, 
				// no need to do EVA to maintain vehicle.
				return 0D;
			}
			
			int total = settlement.getVehicleNum(); 
			int onMission = settlement.getMissionVehicles().size();
			
       		if (total - onMission - totalCap > 0) {
				
	            // Get all vehicles needing maintenance.
                Iterator<Vehicle> i = MaintainGroundVehicleGarage.getAllVehicleCandidates(person, true).iterator();
                while (i.hasNext()) {
                    MalfunctionManager manager = i.next().getMalfunctionManager();
                    double entityProb = (manager.getEffectiveTimeSinceLastMaintenance() / 50D);
                    if (entityProb > 100D) {
                        entityProb = 100D;
                    }
                    result += entityProb;
                }
                
	            int num = settlement.getIndoorPeopleCount();
                result = result 
                		+ result * num / settlement.getPopulationCapacity() / 4D
                		+ result * (total - onMission - totalCap) / 2.5;

                result *= settlement.getGoodsManager().getTransportationFactor();

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
 
        return result;
    }
}
