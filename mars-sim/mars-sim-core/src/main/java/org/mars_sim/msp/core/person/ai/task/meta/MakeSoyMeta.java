/**
 * Mars Simulation Project
 * MakeSoyMeta.java
 * @version 3.07 2014-11-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.MakeSoy;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the MakeSoy task.
 */
public class MakeSoyMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.makeSoy"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(MakeSoyMeta.class.getName());
    
    public MakeSoyMeta() {
        logger.info("just called MakeSoyMeta's constructor");
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new MakeSoy(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        if (MakeSoy.isSoyTime(person)) {

            try {
                // See if there is an available kitchen.
                Building kitchenBuilding = MakeSoy.getAvailableKitchen(person);
                if (kitchenBuilding != null) {
                    result = 200D;

                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, kitchenBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, kitchenBuilding);

                    // Check if there is enough food available to cook.
                    PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
                    double soybeansRequired = config.getFoodConsumptionRate() * (1D / 5D);
                    AmountResource soybeans = AmountResource.findAmountResource("Soybeans");
                    double soybeansAvailable = person.getSettlement().getInventory().getAmountResourceStored(
                    		soybeans, false);
                    if (soybeansAvailable < soybeansRequired) result = 0D;
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"MakeSoyMeta.getProbability()" ,e);
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) result *= job.getStartTaskProbabilityModifier(MakeSoy.class);
        }

        return result;
    }
}