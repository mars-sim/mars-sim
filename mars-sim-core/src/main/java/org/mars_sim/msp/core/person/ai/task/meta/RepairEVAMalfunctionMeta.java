/**
 * Mars Simulation Project
 * RepairEVAMalfunctionMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.RepairEVAMalfunction;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;

public class RepairEVAMalfunctionMeta extends MetaTask {

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairEVAMalfunction"); //$NON-NLS-1$

	private static final double WEIGHT = 300D;
	
    public RepairEVAMalfunctionMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANIICS);
	}

	@Override
	public Task constructInstance(Person person) {
		return new RepairEVAMalfunction(person);
	}

	@Override
	public double getProbability(Person person) {
				
		double result = 0D;

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 75, 1000))
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
						
				if ((malfunction == null)
						|| (malfunction.numRepairerSlotsEmpty(MalfunctionRepairWork.EVA) == 0)) {
					return 0;
				}
				else if (malfunction.isWorkDone(MalfunctionRepairWork.EVA)) {
					result += WEIGHT * malfunction.numRepairerSlotsEmpty(MalfunctionRepairWork.EVA);
				}
			}
			else {
				return 0;
			}
        }
        
        else if (person.isInSettlement()) {
			
    		//Settlement settlement = CollectionUtils.findSettlement(person.getCoordinates());
    		Settlement settlement = person.getSettlement();
        	
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
			result = applyPersonModifier(result, person);
        }

		return result;
	}

	private double getSettlementProbability(Settlement settlement) {
		double result = 0D;

		// Add probability for all malfunctionable entities in person's local.
		Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(settlement).iterator();
		while (i.hasNext()) {
			Malfunctionable entity = i.next();
			// Check if entity has any EVA malfunctions.
			Iterator<Malfunction> j = entity.getMalfunctionManager().getEVAMalfunctions().iterator();
			while (j.hasNext()) {
				double score = 0;
				Malfunction malfunction = j.next();
				if (!malfunction.isWorkDone(MalfunctionRepairWork.EVA)) {
					score = WEIGHT;
					if (RepairEVAMalfunction.hasRepairPartsForMalfunction(settlement, malfunction)) {
						score += WEIGHT;
					}
				}
				result += score;
			}
		}

		return result;
	}
}
