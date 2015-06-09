/**
 * Mars Simulation Project
 * DigLocalRegolithMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
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
import org.mars_sim.msp.core.person.ai.task.DigLocalRegolith;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

/**
 * Meta task for the DigLocalRegolith task.
 */
public class DigLocalRegolithMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalRegolith"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(DigLocalRegolithMeta.class.getName());

    /** Regolith value probability modifier. */
    private static double REGOLITH_VALUE_MODIFIER = 10D;

    private SurfaceFeatures surface;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new DigLocalRegolith(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
            result = 0D;
        }

        // Check if it is night time.
        if (surface == null)
        	surface = Simulation.instance().getMars().getSurfaceFeatures();

        if (surface.getPreviousSolarIrradiance(person.getCoordinates()) == 0) {
            if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                result = 0D;
            }
        }

        if (result != 0)
	        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	            Settlement settlement = person.getSettlement();
	            Inventory inv = settlement.getInventory();

	            try {
	                // Factor the value of regolith at the settlement.
	                GoodsManager manager = settlement.getGoodsManager();
	                AmountResource regolithResource = AmountResource.findAmountResource("regolith");
	                double value = manager.getGoodValuePerItem(GoodsUtil.getResourceGood(regolithResource));
	                result = value * REGOLITH_VALUE_MODIFIER;

	                if (result > 100D) {
	                    result = 100D;
	                }
	            }
	            catch (Exception e) {
	                logger.log(Level.SEVERE, "Error checking good value of regolith.");
	            }

	            // Crowded settlement modifier
	            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
	                result *= 2D;
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


	            // Effort-driven task modifier.
	            result *= person.getPerformanceRating();

	            // Job modifier.
	            Job job = person.getMind().getJob();
	            if (job != null) {
	                result *= job.getStartTaskProbabilityModifier(DigLocalRegolith.class);
	            }

	            // Modify if field work is the person's favorite activity.
	            if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Field Work")) {
	                result *= 2D;
	            }

                // 2015-06-07 Added Preference modifier
                if (result > 0)
                	result += person.getPreference().getPreferenceScore(this);
                if (result < 0) result = 0;

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