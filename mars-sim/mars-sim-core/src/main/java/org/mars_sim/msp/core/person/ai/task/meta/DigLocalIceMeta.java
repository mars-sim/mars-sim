/**
 * Mars Simulation Project
 * DigLocalIceMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
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
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

/**
 * Meta task for the DigLocalIce task.
 */
public class DigLocalIceMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(DigLocalIceMeta.class.getName());
    
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
            Settlement settlement = person.getSettlement();
            Inventory inv = settlement.getInventory();
            
            try {
                // Factor the value of ice at the settlement.
                GoodsManager manager = settlement.getGoodsManager();
                AmountResource iceResource = AmountResource.findAmountResource("ice");
                double value = manager.getGoodValuePerItem(GoodsUtil.getResourceGood(iceResource));
                result = value;
                if (result > 100D) {
                    result = 100D;
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error checking good value of ice.");
            }
            
            // Check at least one EVA suit at settlement.
            int numSuits = inv.findNumUnitsOfClass(EVASuit.class);
            if (numSuits == 0) {
                result = 0D;
            }
            
            // Check if at least one empty bag at settlement.
            int numEmptyBags = inv.findNumEmptyUnitsOfClass(Bag.class, false);
            if (numEmptyBags == 0) {
                result = 0D;
            }

            // Check if an airlock is available
            if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
                result = 0D;
            }

            // Check if it is night time.
            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
            if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
                if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                    result = 0D;
                }
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
        }
    
        return result;
    }
}