/**
 * Mars Simulation Project
 * LoadVehicleEVAMeta.java
 * @version 3.1.0 2017-10-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the LoadVehicleEVA task.
 */
public class LoadVehicleEVAMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicleEVA"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(LoadVehicleEVAMeta.class.getName());

    private static SurfaceFeatures surface;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new LoadVehicleEVA(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

    	if (person.isInSettlement()) {
 
        	Settlement settlement = person.getSettlement();
            
	    	// Check for radiation events
	    	boolean[] exposed = settlement.getExposed();
	
			if (exposed[2]) {// SEP can give lethal dose of radiation
	            return 0;
			}
	
	        // Check if an airlock is available
	        if (EVAOperation.getWalkableAvailableAirlock(person) == null)
	    		return 0;
	
	        // Check if it is night time.
	        surface = Simulation.instance().getMars().getSurfaceFeatures();
	        if (surface.getSolarIrradiance(person.getCoordinates()) == 0D)
	            if (!surface.inDarkPolarRegion(person.getCoordinates()))
	                return 0;
	
	        // Check all vehicle missions occurring at the settlement.
	        try {
	            List<Mission> missions = LoadVehicleEVA.getAllMissionsNeedingLoading(settlement);
	            result += 100D * missions.size();
	        }
	        catch (Exception e) {
	            logger.log(Level.SEVERE, "Error finding loading missions.", e);
	        }
	
	        // Check if any rovers are in need of EVA suits to allow occupants to exit.
	        if (LoadVehicleEVA.getRoversNeedingEVASuits(settlement).size() > 0) {
	            int numEVASuits = settlement.getInventory().findNumEmptyUnitsOfClass(EVASuit.class, false);
	            if (numEVASuits >= 2) {
	                result += 100D;
	            }
	        }
	
	        // Crowded settlement modifier
	        if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity())
	            result *= 2D;
	
	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null)
	            result *= job.getStartTaskProbabilityModifier(LoadVehicleEVA.class)
	            		* settlement.getGoodsManager().getTransportationFactor();
	
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	
	        // Modify if operations is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.OPERATION)
	            result *= 1.5D;
	
	        // 2015-06-07 Added Preference modifier
	        if (result > 0D) {
	            result = result + result * person.getPreference().getPreferenceScore(this)/5D;
	        }
	
	    	if (exposed[0]) {
				result = result/2D;// Baseline can give a fair amount dose of radiation
			}
	
	    	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
				result = result/4D;
			}

    	}
    	
        if (result < 0)
            result = 0;

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		return null;//new LoadVehicleEVA(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

/*
        if (robot.getBotMind().getRobotJob() instanceof Deliverybot)  {

            if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

                // Check all vehicle missions occurring at the settlement.
                try {
                    List<Mission> missions = LoadVehicleEVA.getAllMissionsNeedingLoading(robot.getSettlement());
                    result += 100D * missions.size();
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "Error finding loading missions.", e);
                }

                if (LoadVehicleEVA.getRoversNeedingEVASuits(robot.getSettlement()).size() > 0) {
                    int numEVASuits = robot.getSettlement().getSettlementInventory().findNumEmptyUnitsOfClass(EVASuit.class, false);
                    if (numEVASuits >= 2) {
                        result += 100D;
                    }
                }
            }

            // Effort-driven task modifier.
            result *= robot.getPerformanceRating();

            // Check if an airlock is available
            if (EVAOperation.getWalkableAvailableAirlock(robot) == null) {
                result = 0D;
            }

            // Check if it is night time.
            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
            if (surface.getSolarIrradiance(robot.getCoordinates()) == 0) {
                if (!surface.inDarkPolarRegion(robot.getCoordinates())) {
                    result = 0D;
                }
            }

        }
*/
        return result;
    }
}