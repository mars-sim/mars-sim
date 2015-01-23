/**
 * Mars Simulation Project
 * MaintenanceEVAMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.MaintenanceEVA;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.Structure;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;

/**
 * Meta task for the MaintenanceEVA task.
 */
public class MaintenanceEVAMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintenanceEVA"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(MaintenanceEVAMeta.class.getName());
    
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

        try {
            // Total probabilities for all malfunctionable entities in person's local.
            Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
            while (i.hasNext()) {
                Malfunctionable entity = i.next();
                boolean isStructure = (entity instanceof Structure);
                boolean uninhabitableBuilding = false;
                if (entity instanceof Building) {
                    uninhabitableBuilding = !((Building) entity).hasFunction(BuildingFunction.LIFE_SUPPORT);
                }
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
                    result += entityProb;
                }
            }   
        }
        catch (Exception e) {
            logger.log(Level.SEVERE,"getProbability()",e);
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
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();
            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
                result *= 2D;
            }
        }
    
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(MaintenanceEVA.class);
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