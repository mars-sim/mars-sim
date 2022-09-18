/*
 * Mars Simulation Project
 * MaintenanceEVAMeta.java
 * @date 2022-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.MaintenanceEVA;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.Structure;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * Meta task for the MaintenanceEVA task.
 */
public class MaintenanceEVAMeta extends MetaTask {

    /** default logger. */
    private static final Logger logger = Logger.getLogger(MaintenanceEVAMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintenanceEVA"); //$NON-NLS-1$

	private static final double FACTOR = 1D;

    public MaintenanceEVAMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANICS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new MaintenanceEVA(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;
  
    	Settlement settlement = person.getSettlement();
        
        if (settlement != null) {
        	
            // Check if an airlock is available
            if (EVAOperation.getWalkableAvailableAirlock(person, false) == null)
	    		return 0;

            // Check if it is night time.
            if (EVAOperation.isGettingDark(person))
            	return 0;

            // Checks if the person's settlement is at meal time and is hungry
            if (EVAOperation.isHungryAtMealTime(person))
            	return 0;
            
            // Checks if the person is physically fit for heavy EVA tasks
    		if (!EVAOperation.isEVAFit(person))
    			return 0;
			
//            // Checks if a EVA suit is good
			// don't have one until being donned on
//            if (EVAOperation.hasEVAProblem(person))
//            	return 0;
            	
        	// Check for radiation events
        	boolean[] exposed = settlement.getExposed();

    		if (exposed[2]) {// SEP can give lethal dose of radiation
    			return 0;
    		}

            try {
                // Total probabilities for all malfunctionable entities in person's local.
                Iterator<Malfunctionable> i = MalfunctionFactory.getLocalMalfunctionables(person).iterator();

                while (i.hasNext()) {
                    Malfunctionable entity = i.next();
                    
                    boolean isStructure = (entity instanceof Structure);	
                    boolean uninhabitableBuilding = false;
                    if (entity.getUnitType() == UnitType.BUILDING)
                        uninhabitableBuilding = !((Building) entity).hasFunction(FunctionType.LIFE_SUPPORT);
                    
                    if (!isStructure && !uninhabitableBuilding)
                    	return 0;
                    
                    MalfunctionManager manager = entity.getMalfunctionManager();
					boolean hasMalfunction = manager.hasMalfunction();
					if (hasMalfunction) {
						return 0;
					}
					
					boolean hasParts = Maintenance.hasMaintenanceParts(person, entity);
					if (!hasParts) {
						return 0;
					}
					
					double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
					boolean minTime = (effectiveTime >= 1000D);
					
					double score = person.getPhysicalCondition().computeHealthScore();
					
					if (minTime) {
						double entityProb = effectiveTime / 1000D;
						if (entityProb > score) {
							entityProb = score;
						}
						result += entityProb * FACTOR;
					}         
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"getProbability()", e);
            }

            if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity())
                result *= 2D;
            
            double shiftBonus = person.getTaskSchedule().obtainScoreAtStartOfShift();
            
            // Encourage to get this task done early in a work shift
            result *= shiftBonus / 10;
            
            result = applyPersonModifier(result, person);

        	if (exposed[0]) {
    			result = result/2D;// Baseline can give a fair amount dose of radiation
    		}

        	if (exposed[1]) {// GCR can give nearly lethal dose of radiation
    			result = result/4D;
    		}

            if (result < 0) result = 0;
        }

        return result;
    }

	public double getSettlementProbability(Settlement settlement) {
		double result = 0D;

        try {
            // Total probabilities for all malfunctionable entities in person's local.
            Iterator<Malfunctionable> i = MalfunctionFactory.getBuildingMalfunctionables(settlement).iterator();

            while (i.hasNext()) {
                Malfunctionable entity = i.next();
                boolean isStructure = (entity instanceof Structure);
                boolean uninhabitableBuilding = false;
                if (entity instanceof Building)
                    uninhabitableBuilding = !((Building) entity).hasFunction(FunctionType.LIFE_SUPPORT);

                MalfunctionManager manager = entity.getMalfunctionManager();
                boolean hasMalfunction = manager.hasMalfunction();
                boolean hasParts = Maintenance.hasMaintenanceParts(settlement, entity);
                double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
                boolean minTime = (effectiveTime >= 1000D);
                if ((isStructure || uninhabitableBuilding) && !hasMalfunction && minTime && hasParts) {
                    double entityProb = manager.getEffectiveTimeSinceLastMaintenance() / 1000D;
                    if (entityProb > 100D) {
                        entityProb = 100D;
                    }
                    result += entityProb;
                }
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE,"getProbability()",e);
        }

		return result;
	}
}
