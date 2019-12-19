/**
 * Mars Simulation Project
 * RepairEVAMalfunctionMeta.java
 * @version 3.1.0 2017-03-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.RepairEVAMalfunction;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

public class RepairEVAMalfunctionMeta implements MetaTask, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairEVAMalfunction"); //$NON-NLS-1$

	private static final double WEIGHT = 300D;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Task constructInstance(Person person) {
		return new RepairEVAMalfunction(person);
	}

	@Override
	public double getProbability(Person person) {
				
		double result = 0D;

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();
        
        if (fatigue > 1000 || stress > 50 || hunger > 500)
        	return 0;
        
        if (person.isInside() && EVAOperation.getWalkableAvailableAirlock(person) == null)
			return 0;
			
//		boolean returnFromMission = false;
//		// TODO: need to analyze if checking the location state this way can properly verify if a person return from a mission
//		if (person.isInVehicle() && person.getVehicle().getLocationStateType() == LocationStateType.WITHIN_SETTLEMENT_VICINITY) {
//			returnFromMission = true;
//		}
        		
        if (person.isInVehicle()) {
        	// Get the malfunctioning entity.
        	Malfunctionable entity = RepairEVAMalfunction.getEVAMalfunctionEntity(person);
			
			if (entity != null) {
				Malfunction malfunction = RepairEVAMalfunction.getMalfunction(person, entity);
						
				if (malfunction != null) {
					if (malfunction.areAllRepairerSlotsFilled()) {
						return 0;
					}
					else if (malfunction.needEVARepair()) {
						result += WEIGHT * malfunction.numRepairerSlotsEmpty(2);
					}
				}
				else {
					return 0;
				}
				
			}
			else {
				return 0;
			}
        }
        
        else if (person.isInSettlement()) {
			
    		Settlement settlement = CollectionUtils.findSettlement(person.getCoordinates());
    		
			// Check for radiation events
			boolean[] exposed = settlement.getExposed();

			if (exposed[2]) {// SEP can give lethal dose of radiation
				return 0;
			}

			// Check if it is night time.
			// Even if it's night time, technicians/engineers are assigned to man that work shift 
			// to take care of the the repair.
			
			result = getSettlementProbability(settlement);


			if (exposed[0]) {
				result = result / 3D;// Baseline can give a fair amount dose of radiation
			}

			if (exposed[1]) {// GCR can give nearly lethal dose of radiation
				result = result / 6D;
			}

			if (result < 0) {
				result = 0;
			}
		}
        
        if (person.isInside()) {
			// Effort-driven task modifier.
			result *= person.getPerformanceRating();

			// Job modifier if not in vehicle.
			Job job = person.getMind().getJob();
			if ((job != null)) {
				result *= job.getStartTaskProbabilityModifier(RepairEVAMalfunction.class);
			}

			// Modify if tinkering is the person's favorite activity.
			if (person.getFavorite().getFavoriteActivity() == FavoriteType.TINKERING) {
				result *= 1.5D;
			}

			// Add Preference modifier
			if (result > 0D) {
				result = result + result * person.getPreference().getPreferenceScore(this) / 5D;
			}
        }

		return result;
	}

	public double getSettlementProbability(Settlement settlement) {
		double result = 0D;

		// Add probability for all malfunctionable entities in person's local.
		Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(settlement).iterator();
		while (i.hasNext()) {
			Malfunctionable entity = i.next();
//			MalfunctionManager manager = entity.getMalfunctionManager();
			if (RepairEVAMalfunction.hasEVA(entity)) {
				// Check if entity has any EVA malfunctions.
				Iterator<Malfunction> j = entity.getMalfunctionManager().getEVAMalfunctions().iterator();
				while (j.hasNext()) {
					double score = 0;
					Malfunction malfunction = j.next();
					if (!malfunction.isEVARepairDone()) {
//						score = WEIGHT;
						try {
							if (RepairEVAMalfunction.hasRepairPartsForMalfunction(settlement, malfunction)) {
								score = WEIGHT * 2;
							}
						} catch (Exception e) {
							e.printStackTrace(System.err);
						}
						result += score;
					}
				}
			}

			// Check if entity requires an EVA and has any normal malfunctions.
//			if (RepairEVAMalfunction.requiresEVA(entity)) {
//				Iterator<Malfunction> k = manager.getEVAMalfunctions().iterator();
//				while (k.hasNext()) {
//					Malfunction malfunction = k.next();
//					try {
//						if (RepairMalfunction.hasRepairPartsForMalfunction(settlement, malfunction)) {
//							result += WEIGHT;
//						}
//					} catch (Exception e) {
//						e.printStackTrace(System.err);
//					}
//				}
//			}
		}

		return result;
	}
	@Override
	public Task constructInstance(Robot robot) {
		return null;// new RepairEVAMalfunction(robot);
	}

	@Override
	public double getProbability(Robot robot) {
		return 0;
	}
}