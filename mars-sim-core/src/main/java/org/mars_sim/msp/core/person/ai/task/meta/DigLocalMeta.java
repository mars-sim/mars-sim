/**
 * Mars Simulation Project
 * DigLocalhMeta.java
 * @version 3.3.0 2021-10-13
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the DigLocal task.
 */
public abstract class DigLocalMeta extends MetaTask {

	private static final double VALUE = 2.0;
	private EquipmentType containerType;
    
    
    public DigLocalMeta(String name, EquipmentType containerType) {
		super(name, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);

		this.containerType = containerType;
	}


    protected double getProbability(Settlement settlement, Person person, double collectionProbability) {

    	// Will not perform this task if he has a mission
    	if ((person.getMission() != null) || !person.isInSettlement()) {
    		return 0;
    	}
    	
        double result = 0D;
       	
    	// Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person) == null)
    		return 0;   
        
        //Checked for radiation events
    	boolean[]exposed = settlement.getExposed();

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
        int numEmptyBags = inv.findNumContainers(containerType, true, false);
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
  
	        result = collectionProbability * VALUE;
	    	
	        if (result > 3000)
	        	result = 3000;
	        
            result = result - stress * 3 - fatigue/2 - hunger/2;
            
	        if (result < 0)
	        	return 0;
	
	        // Crowded settlement modifier
        if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity())
            result = result * (settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity());

        if (settlement.getIndoorPeopleCount() <= 4)
            result *= 1.5D;

        result = applyPersonModifier(result, person);

    	if (exposed[0]) {
			result = result/5D;// Baseline can give a fair amount dose of radiation
		}

    	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
			result = result/10D;
		}

        if (result <= 0)
            return 0;

        return result;
    }
}
