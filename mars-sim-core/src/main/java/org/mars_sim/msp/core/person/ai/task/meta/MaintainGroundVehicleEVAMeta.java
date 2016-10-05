/**
 * Mars Simulation Project
 * MaintainGroundVehicleEVAMeta.java
 * @version 3.08 2015-06-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
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

    private SurfaceFeatures surface;

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
        boolean noGo = false;

        // Determine if settlement has a garage.
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
        	
            if (person.getSettlement().getBuildingManager().getBuildings(
                    BuildingFunction.GROUND_VEHICLE_MAINTENANCE).size() > 0) {

            	//2016-10-04 Checked for radiation events
            	boolean[] exposed = person.getSettlement().getExposed();

        		if (exposed[2]) {
        			noGo = true;// SEP can give lethal dose of radiation, out won't go outside
        		}
        			
                // Check if an airlock is available
                if (!noGo)
    	    		if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
    	                result = 0D;
    	                noGo = true;	
    	            }

                if (!noGo) {
    	            // Check if it is night time.
    	            if (surface == null)
    	                surface = Simulation.instance().getMars().getSurfaceFeatures();
    	            
    	            if (surface.getSolarIrradiance(person.getCoordinates()) == 0D)
    	                if (!surface.inDarkPolarRegion(person.getCoordinates())) {
    	                    result = 0D;
    	                    noGo = true;
    	                }
                }
            	
                if (!noGo) {
	                
	                Settlement settlement = person.getSettlement();
	                if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity())
	                    result *= 2D;
	
	                // Get all vehicles needing maintenance.
	                if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	                    Iterator<Vehicle> i = MaintainGroundVehicleEVA.getAllVehicleCandidates(person).iterator();
	                    while (i.hasNext()) {
	                        MalfunctionManager manager = i.next().getMalfunctionManager();
	                        double entityProb = (manager.getEffectiveTimeSinceLastMaintenance() / 50D);
	                        if (entityProb > 100D) {
	                            entityProb = 100D;
	                        }
	                        result += entityProb;
	                    }
	                }
	
	                // Effort-driven task modifier.
	                result *= person.getPerformanceRating();
	
	                // Job modifier.
	                Job job = person.getMind().getJob();
	                if (job != null) {
	                    result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleEVA.class);
	                }
	
	                // Modify if tinkering is the person's favorite activity.
	                if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Tinkering")) {
	                    result *= 1.5D;
	                }
	                
	                // 2015-06-07 Added Preference modifier
	                if (result > 0D) {
	                    result = result + result * person.getPreference().getPreferenceScore(this)/4D;
	                }
	                
	               	if (exposed[0]) {
		    			noGo = false;
		    			result = result/1.2;// Baseline can give lethal dose of radiation, out won't go outside
		    		}
		        	
		        	if (exposed[1]) {
		    			noGo = false;// GCR can give lethal dose of radiation, out won't go outside
		    			result = result/2D;
		    		}		        	

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