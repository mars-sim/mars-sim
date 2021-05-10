/**
 * Mars Simulation Project
 * MaintainGroundVehicleEVAMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the MaintainGroundVehicleEVA task.
 */
public class MaintainGroundVehicleEVAMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainGroundVehicleEVA"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
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
       		
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 500 || stress > 50 || hunger > 500)
            	return 0;
            
        	Settlement settlement = person.getAssociatedSettlement();
			// Determine if settlement has available space in garage.
			boolean garageSpace = false;
			
			List<Building> garages = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE);
			
			Iterator<Building> j = garages.iterator();
			while (j.hasNext() && !garageSpace) {
				try {
					Building building = j.next();
					VehicleMaintenance garage = building.getGroundVehicleMaintenance();
					if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity()) {
						garageSpace = true;
					}
					
				} catch (Exception e) {
				}
			}
			
			if (garageSpace) {
				return 0D;
			}
			
       		if (garages.size() == 0) {
	
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
				
	            if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity())
	                result *= 2D;
	
	            // Get all vehicles needing maintenance.
                Iterator<Vehicle> i = MaintainGroundVehicleEVA.getAllVehicleCandidates(person).iterator();
                while (i.hasNext()) {
                    MalfunctionManager manager = i.next().getMalfunctionManager();
                    double entityProb = (manager.getEffectiveTimeSinceLastMaintenance() / 50D);
                    if (entityProb > 100D) {
                        entityProb = 100D;
                    }
                    result += entityProb;
                }
	
	            // Effort-driven task modifier.
	            result *= person.getPerformanceRating();
	
	            // Job modifier.
	            Job job = person.getMind().getJob();
	            if (job != null) {
	                result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleEVA.class)
	                		* settlement.getGoodsManager().getTransportationFactor();
	            }
	
	            // Modify if tinkering is the person's favorite activity.
	            if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
	                result += RandomUtil.getRandomInt(1, 20);
	            }
	
	            // Add Preference modifier
	            if (result > 0D) {
	                result = result + result * person.getPreference().getPreferenceScore(this)/5D;
	            }
	
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

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}
