/**
 * Mars Simulation Project
 * DigLocalIceMeta.java
 * @version 3.08 2015-06-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

/**
 * Meta task for the DigLocalIce task.
 */
public class DigLocalIceMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(DigLocalIceMeta.class.getName());

    /** Ice value probability modifier. */
    private static double ICE_VALUE_MODIFIER = 10D;

    private SurfaceFeatures surface;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new DigLocalIce(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
        
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
        	
            // Check if an airlock is available
            if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
                result = 0D;
                return 0;
            }

            // Check if it is night time.
            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
            if (surface.getSolarIrradiance(person.getCoordinates()) == 0D) {
                if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                    result = 0D;
                    return 0;
                }
            }
            
            
            Settlement settlement = person.getSettlement();
            Inventory inv = settlement.getInventory();

            // Check at least one EVA suit at settlement.
            int numSuits = inv.findNumUnitsOfClass(EVASuit.class);
            if (numSuits == 0) {
                result = 0D;
                return 0;
            }
            
            // Check if at least one empty bag at settlement.
            int numEmptyBags = inv.findNumEmptyUnitsOfClass(Bag.class, false);
            if (numEmptyBags == 0) {
                result = 0D;
                return 0;
            }
            
            try {
                // Factor the value of ice at the settlement.
                GoodsManager manager = settlement.getGoodsManager();
                AmountResource iceResource = AmountResource.findAmountResource("ice");
                AmountResource waterResource =AmountResource.findAmountResource(LifeSupportType.WATER);
                double ice_value = manager.getGoodValuePerItem(GoodsUtil.getResourceGood(iceResource));
                ice_value = ice_value * ICE_VALUE_MODIFIER;
            	if (ice_value > 300)
            		ice_value = 300;

                double water_value = manager.getGoodValuePerItem(GoodsUtil.getResourceGood(iceResource));
                water_value = water_value * ICE_VALUE_MODIFIER;
                if (water_value > 300)
            		water_value = 300;
                
                // 2016-10-14 Compare the available amount of water and ice reserve
                double ice_available = inv.getAmountResourceStored(iceResource, false);
                double water_available = inv.getAmountResourceStored(waterResource, false);
                
                int size = settlement.getAllAssociatedPeople().size();
                
                // TODO: create a task to find local ice and simulate the probability of finding local ice and its quantity
                
                if (water_available < 100D*size && ice_available < 50D*size ) {
                	result = water_value + ice_value;
                }
                else {
                    result = 0;
                    return 0;
                }
                
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error checking good value of ice.");
            }


            // Crowded settlement modifier
            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
                result *= 2D;
            }


            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(DigLocalIce.class);
            }

            if (result > 0)
            	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

            
            if (result < 0D) {
                result = 0D;
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