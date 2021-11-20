/*
 * Mars Simulation Project
 * RepairEVAMalfunction.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.malfunction.RepairHelper;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;

/**
 * The RepairEVAMalfunction class is a task to repair a malfunction requiring an
 * EVA.
 */
public class RepairEVAMalfunction extends EVAOperation implements Repair, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(RepairEVAMalfunction.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairEVAMalfunction"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REPAIRING = new TaskPhase(Msg.getString("Task.phase.repairing")); //$NON-NLS-1$

	// Data members
	/** The malfunctionable entity being repaired. */
	private Malfunctionable entity;

	/** The malfunction to be repaired. */
	private Malfunction malfunction;

	/** The container unit the person started the mission in. */
	private Unit containerUnit;

	public RepairEVAMalfunction(Person person) {
		super(NAME, person, true, 25, SkillType.MECHANICS);

		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}
		
		containerUnit = person.getTopContainerUnit();

		if (!(containerUnit instanceof MarsSurface)) {
			// Get the malfunctioning entity.
			for (Malfunctionable next : MalfunctionFactory.getLocalMalfunctionables(person)) {
				Malfunction potential = next.getMalfunctionManager().getMostSeriousMalfunctionInNeed(MalfunctionRepairWork.EVA);
				if (potential != null) {
					entity = next;
					malfunction = potential;
					break; // Stop searching
				}
			}
		}

			
		// Start if found
		if (entity != null) {
			setDescription(Msg.getString("Task.description.repairEVAMalfunction.detail", malfunction.getName(),
					entity.getNickName())); // $NON-NLS-1$

			// Determine location for repairing malfunction.
			setOutsideLocation((LocalBoundedObject) entity);
			
			// Can fail to get a path and Task will be Done
			if (!isDone()) {
	            if (person.isInside()) {
	            	setPhase(WALK_TO_OUTSIDE_SITE);
	            }				
	
				RepairHelper.startRepair(malfunction, person, MalfunctionRepairWork.EVA, entity);
				
				// Initialize phase
				addPhase(REPAIRING);
			}
		}
		else {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}
	}


	public static Malfunctionable getEVAMalfunctionEntity(Person person) {
		Malfunctionable result = null;

		for(Malfunctionable entity : MalfunctionFactory.getLocalMalfunctionables(person)) {
			if (getMalfunction(person, entity) != null) {
				return entity;
			}
			MalfunctionManager manager = entity.getMalfunctionManager();
			Unit container = person.getTopContainerUnit();
			
			// Check if entity has any EVA malfunctions.
			for(Malfunction malfunction : manager.getAllEVAMalfunctions()) {
				try {
					if (RepairHelper.hasRepairParts(container, malfunction)) {
						return entity;
					}
				} catch (Exception e) {
		          	logger.severe("Problems calling RepairEVAMalfunction's hasRepairPartsForMalfunction(): "+ e.getMessage());
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets a reparable malfunction requiring an EVA for a given entity.
	 * 
	 * @param person the person to repair.
	 * @param entity the entity with a malfunction.
	 * @return malfunction requiring an EVA repair or null if none found.
	 */
	public static Malfunction getMalfunction(Person person, Malfunctionable entity) {
		MalfunctionManager manager = entity.getMalfunctionManager();

		// Check if entity has any EVA malfunctions.
		for(Malfunction malfunction : manager.getAllEVAMalfunctions()) {
			try {
				if (RepairHelper.hasRepairParts(person.getTopContainerUnit(),
						malfunction)) {
					return malfunction;
				}
			} catch (Exception e) {
	          	logger.log(Level.SEVERE, "Problems calling RepairEVAMalfunction's hasRepairPartsForMalfunction(): "+ e.getMessage());
			}
		}
		
		if (manager.hasMalfunction()) {
			logger.log(entity, Level.WARNING, 2000, "No parts available for any malfunction");
		}
		return null;
	}


	@Override
	protected TaskPhase getOutsideSitePhase() {
		return REPAIRING;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
			}
			else if (REPAIRING.equals(getPhase())) {
				time = repairMalfunctionPhase(time);
			}
		}
		return time;
	}

	/**
	 * Perform the repair malfunction phase of the task.
	 * 
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 */
	private double repairMalfunctionPhase(double time) {
		boolean endTask = isDone();

		// Check for radiation exposure during the EVA operation.
		endTask |= (isRadiationDetected(time)
					|| malfunction.isWorkDone(MalfunctionRepairWork.EVA));	
		endTask |= malfunction.isWorkDone(MalfunctionRepairWork.EVA);
		endTask |= (shouldEndEVAOperation() || addTimeOnSite(time));
		endTask |= (!person.isFit());
		if (endTask) {
			// Return all the time
	        if (worker.isOutside()) {
	        	setPhase(WALK_BACK_INSIDE);
	        }
	        else if (person.isInside()) {
	    		endTask();
	        }
    		return time;
        }
	        
		double workTime = 0;
		if (person != null) {
			workTime = time;
		} else if (robot != null) {
			// A robot moves slower than a person and incurs penalty on workTime
			workTime = time / 2;
		}

		// Determine effective work time based on "Mechanic" skill.
		int mechanicSkill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);

		if (mechanicSkill == 0)
			workTime /= 2;
		if (mechanicSkill > 1)
			workTime += workTime * (.2D * mechanicSkill);
		
		if (person != null) {
			if (RepairHelper.hasRepairParts(containerUnit, malfunction)) {
				RepairHelper.claimRepairParts(containerUnit, malfunction);
			} else {
	            if (person.isOutside())
	            	setPhase(WALK_BACK_INSIDE);
	            else //if (person.isInside())
	        		endTask();
	            return time;
			}

		}

		// Add EVA work to malfunction.
		double workTimeLeft = 0D;
		if (!malfunction.isWorkDone(MalfunctionRepairWork.EVA)) {
			workTimeLeft = malfunction.addWorkTime(MalfunctionRepairWork.EVA, workTime, worker.getName());
		}
		
		// Add experience points
		addExperience(time);

		// Check if an accident happens during repair.
		checkForAccident(time);

		// Check if there are no more malfunctions.
		if (malfunction.isWorkDone(MalfunctionRepairWork.EVA)) {
			logger.info(person, "Wrapped up the EVA of " + malfunction.getName() 
					+ " in "+ entity + " ("
					+ Math.round(malfunction.getCompletedWorkTime(MalfunctionRepairWork.EVA)*10.0)/10.0 + " millisols spent).");
            if (worker.isOutside()) {
            	setPhase(WALK_BACK_INSIDE);
            }
            else if (person.isInside()) {
        		endTask();
            }
		}

		return workTimeLeft;
	}

	@Override
	public Malfunctionable getEntity() {
		return entity;
	}


	/**
	 * Worker leaves the malfunction effort
	 */
	@Override
	protected void clearDown() {
		// Leaving the repair effort
		if (malfunction != null) {
			malfunction.leaveWork(MalfunctionRepairWork.EVA, worker.getName());
		}
	}
}
