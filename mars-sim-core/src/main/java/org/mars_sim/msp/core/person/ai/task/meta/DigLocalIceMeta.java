/**
 * Mars Simulation Project
 * DigLocalIceMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;


/**
 * Meta task for the DigLocalIce task.
 */
public class DigLocalIceMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$

    private static final double VALUE = .8;
    
    public DigLocalIceMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);

	}

    @Override
    public Task constructInstance(Person person) {
        return new DigLocalIce(person);
    }

    @Override
    public double getProbability(Person person) {

    	// Will not perform this task if he has a mission
    	if (person.getMission() != null)
    		return 0;

    	Settlement settlement = CollectionUtils.findSettlement(person.getCoordinates());
        
        double result = 0D;
   
    	// If a person is on a mission out there, he/she should not run this task
        if (settlement != null) {
        	 
        	double collectionRate = settlement.getIceCollectionRate();
            
            if (collectionRate <= 0)
            	return 0; 
            
	    	// Check if an airlock is available
	        if (EVAOperation.getWalkableAvailableAirlock(person) == null)
	    		return 0;
	
	        // Checked for radiation events
	    	boolean[] exposed = settlement.getExposed();
	
			if (exposed[2]) {
				// SEP can give lethal dose of radiation
	            return 0;
			}
			
            // Check if it is night time.          
            if (EVAOperation.isGettingDark(person))
            	return 0;
            
            // Checks if the person's settlement is at meal time and is hungry
            if (EVAOperation.isHungryAtMealTime(person))
            	return 0;
            
            // Checks if the person is physically drained
			if (EVAOperation.isExhausted(person))
				return 0;
			
			
            Inventory inv = settlement.getInventory();

            // Check at least one EVA suit at settlement.
            int numSuits = inv.findNumEVASuits(false, true);
            if (numSuits == 0) {
                return 0;
            }

            // Check if at least one empty bag at settlement.
            int numEmptyBags = inv.findNumBags(true, true);
            if (numEmptyBags == 0) {
                return 0;
            }

            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double stress = condition.getStress();
            double fatigue = condition.getFatigue();
            double hunger = condition.getHunger();
            
            if (!condition.isFitByLevel(300, 30, 300))
            	return 0;
       
            result = settlement.getIceProbabilityValue() * VALUE;

	        if (result > 3000)
	        	result = 3000;
	        
            result = result - stress * 3 - fatigue/2 - hunger/2;

            if (result < 0)
            	return 0;

            // Crowded settlement modifier
            if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity())
                result *= 1.5D;

            if (settlement.getIndoorPeopleCount() <= 4)
                result *= 1.5D;

            result = applyPersonModifier(result, person);

            //logger.info("DigLocalIceMeta's probability : " + Math.round(result*100D)/100D);

	    	if (exposed[0]) {
				result = result/5D;// Baseline can give a fair amount dose of radiation
			}

	    	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
				result = result/10D;
			}
	    	
            if (result <= 0D) {
                result = 0D;
            }
        }

        return result;
    }
}
