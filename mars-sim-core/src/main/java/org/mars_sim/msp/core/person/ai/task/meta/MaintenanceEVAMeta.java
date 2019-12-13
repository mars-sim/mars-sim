/**
 * Mars Simulation Project
 * MaintenanceEVAMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.MaintenanceEVA;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.Structure;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the MaintenanceEVA task.
 */
public class MaintenanceEVAMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(MaintenanceEVAMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintenanceEVA"); //$NON-NLS-1$

	private static final double FACTOR = 1D;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new MaintenanceEVA(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;
  
        if (person.isInSettlement()) {
        	
            // Check if an airlock is available
            if (EVAOperation.getWalkableAvailableAirlock(person) == null)
	    		return 0;

            // Check if it is night time.
            if (EVAOperation.isGettingDark(person))
            	return 0;

            // Checks if the person's settlement is at meal time and is hungry
            if (EVAOperation.isHungryAtMealTime(person))
            	return 0;
            
            // Checks if a EVA suit is good
            if (EVAOperation.noEVAProblem(person))
            	return 0;	
            
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
        	Settlement settlement = person.getSettlement();
    	
        	// Check for radiation events
        	boolean[] exposed = settlement.getExposed();

    		if (exposed[2]) {// SEP can give lethal dose of radiation
    			return 0;
    		}

            try {
                // Total probabilities for all malfunctionable entities in person's local.
                Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();

                while (i.hasNext()) {
                    Malfunctionable entity = i.next();
                    boolean isStructure = (entity instanceof Structure);
                    boolean uninhabitableBuilding = false;
                    if (entity instanceof Building)
                        uninhabitableBuilding = !((Building) entity).hasFunction(FunctionType.LIFE_SUPPORT);

                    MalfunctionManager manager = entity.getMalfunctionManager();
                    boolean hasMalfunction = manager.hasMalfunction();
                    boolean hasParts = Maintenance.hasMaintenanceParts(person, entity);
                    double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
                    boolean minTime = (effectiveTime >= 1000D);
                    if ((isStructure || uninhabitableBuilding) && !hasMalfunction && minTime && hasParts) {
                        double entityProb = manager.getEffectiveTimeSinceLastMaintenance() / 1000D;
                        if (entityProb > 100D) {
                            entityProb = 100D;
                        }
                        result += entityProb * FACTOR;
                    }
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"getProbability()",e);
            }

            
            if (settlement.getIndoorPeopleCount() > settlement.getPopulationCapacity())
                result *= 2D;
            
            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(MaintenanceEVA.class);
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Modify if tinkering is the person's favorite activity.
            if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
                result += RandomUtil.getRandomInt(1, 20);
            }

            // Added Preference modifier
            if (result > 0D) {
                result = result + result * person.getPreference().getPreferenceScore(this)/5D;
            }

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
            Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(settlement).iterator();

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
	
	@Override
	public Task constructInstance(Robot robot) {
        return null;
	}

	@Override
	public double getProbability(Robot robot) {
        return 0;
	}
}