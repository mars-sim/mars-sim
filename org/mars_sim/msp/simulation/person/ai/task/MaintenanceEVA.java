/**
 * Mars Simulation Project
 * MaintenanceEVA.java
 * @version 2.77 2004-08-23
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.mars.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Structure;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;

/** 
 * The Maintenance class is a task for performing
 * preventive maintenance on malfunctionable entities outdoors.
 */
public class MaintenanceEVA extends EVAOperation implements Serializable {

	private static final String MAINTENANCE = "Maintenance";
	
	private Malfunctionable entity; // Entity to be maintained.
	private Airlock airlock; // Airlock to be used for EVA.
	private double duration; // Duration (in millisols) the person will perform this task.
	
	/** 
	 * Constructor
	 * @param person the person to perform the task
	 */
	public MaintenanceEVA(Person person) {
		super("Performing EVA Maintenance", person);
		
		// Get an available airlock.
		airlock = getAvailableAirlock(person);
		if (airlock == null) endTask();
		
		// Randomly determine duration, from 0 - 500 millisols
		duration = RandomUtil.getRandomDouble(500D);
		
		try {
			entity = getMaintenanceMalfunctionable();
			if (entity == null) endTask();
		}
		catch (Exception e) {
			System.err.println("MaintenanceEVA.constructor(): " + e.getMessage());
			endTask();
		}
		
		// Set initial phase.
		phase = EXIT_AIRLOCK;
		
		// System.out.println(person.getName() + " is starting " + getDescription());
	}
	
	/** 
	 * Returns the weighted probability that a person might perform this task.
	 * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
	 * @param person the person to perform the task
	 * @return the weighted probability that a person might perform this task
	 */
	public static double getProbability(Person person) {
		double result = 0D;

		// Total probabilities for all malfunctionable entities in person's local.
		Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext()) {
			Malfunctionable entity = (Malfunctionable) i.next();
			boolean isStructure = (entity instanceof Structure);
			boolean uninhabitableBuilding = false;
			if (entity instanceof Building) 
				uninhabitableBuilding = !((Building) entity).hasFunction(LifeSupport.NAME);
			MalfunctionManager manager = entity.getMalfunctionManager();
			boolean hasMalfunction = manager.hasMalfunction();
			if ((isStructure || uninhabitableBuilding) && !hasMalfunction) {
				double entityProb = manager.getEffectiveTimeSinceLastMaintenance() / 1000D;
				if (entityProb > 100D) entityProb = 100D;
				result += entityProb;
			}   
		}
		
		// Check if an airlock is available
		if (getAvailableAirlock(person) == null) result = 0D;

		// Check if it is night time.
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
			if (!surface.inDarkPolarRegion(person.getCoordinates()))
				result = 0D;
		} 
	
		// Effort-driven task modifier.
		result *= person.getPerformanceRating();
        
		// Job modifier.
		result *= person.getMind().getJob().getStartTaskProbabilityModifier(MaintenanceEVA.class);        
	
		return result;
	}
	
	/**
	 * Perform the task.
	 * @param time the amount of time (millisols) to perform the task
	 * @return amount of time remaining after performing the task
	 * @throws Exception if error performing task.
	 */
	double performTask(double time) throws Exception {
		double timeLeft = super.performTask(time);
		if (subTask != null) return timeLeft;

		while ((timeLeft > 0D) && !isDone()) {
			if (phase.equals(EXIT_AIRLOCK)) timeLeft = exitEVA(timeLeft); 
			else if (phase.equals(MAINTENANCE)) timeLeft = maintenance(timeLeft);
			else if (phase.equals(ENTER_AIRLOCK)) timeLeft = enterEVA(timeLeft);
		}			            
	
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 20 millisols of time spent)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double experience = time / 50D;
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
		experience *= getTeachingExperienceModifier();
		person.getSkillManager().addExperience(Skill.EVA_OPERATIONS, experience);
        
		return timeLeft;
	}
	
	/**
	 * Perform the exit airlock phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 */
	private double exitEVA(double time) {
		try {
			time = exitAirlock(time, airlock);
		}
		catch (Exception e) { 
			// System.err.println(e.getMessage()); 
		}
        
		if (exitedAirlock) phase = MAINTENANCE;
		return time;
	}
	
	/**
	 * Perform the maintenance phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 */
	private double maintenance(double time) {
        
		MalfunctionManager manager = entity.getMalfunctionManager();
		boolean malfunction = manager.hasMalfunction();
		boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() == 0D);
        
		if (finishedMaintenance || malfunction || shouldEndEVAOperation()) {
			phase = ENTER_AIRLOCK;
			return time;
		}
        
		// Determine effective work time based on "Mechanic" skill.
		double workTime = time;
		int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
		if (mechanicSkill == 0) workTime /= 2;
		if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

		// Add work to the maintenance
		manager.addMaintenanceWorkTime(workTime);

		// Add experience to "Mechanic" skill.
		// (1 base experience point per 100 millisols of time spent)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double experience = time / 100D;
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
		person.getSkillManager().addExperience(Skill.MECHANICS, experience);

		// Keep track of the duration of the task.
		timeCompleted += time;
		if (timeCompleted >= duration) phase = ENTER_AIRLOCK;
	
		// Check if an accident happens during maintenance.
		checkForAccident(time);

		return 0D;
	}  

	/**
	 * Perform the enter airlock phase of the task.
	 * @param time amount of time to perform the phase
	 * @return time remaining after performing the phase
	 */
	private double enterEVA(double time) {
		try {
			time = enterAirlock(time, airlock);
		}
		catch (Exception e) { 
			// System.out.println(e.getMessage()); 
		}
        
		if (enteredAirlock) endTask();
		return time;
	}	
	
	/**
	 * Check for accident with entity during maintenance phase.
	 * @param time the amount of time (in millisols)
	 */
	protected void checkForAccident(double time) {

		// Use EVAOperation checkForAccident() method.
		super.checkForAccident(time);

		double chance = .001D;

		// Mechanic skill modification.
		int skill = person.getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
		if (skill <= 3) chance *= (4 - skill);
		else chance /= (skill - 2);

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			// System.out.println(person.getName() + " has accident while performing maintenance on " + entity.getName() + ".");
			entity.getMalfunctionManager().accident();
		}
	}
	
	/**
	 * Gets a random malfunctionable to perform maintenance on.
	 * @return malfunctionable or null.
	 * @throws Exception if error finding malfunctionable.
	 */
	private Malfunctionable getMaintenanceMalfunctionable() throws Exception {
		Malfunctionable result = null;
    	
		// Determine entity to maintain.
		double totalProbabilityWeight = 0D;
		
		// Total probabilities for all malfunctionable entities in person's local.
		Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext()) {
			Malfunctionable e = (Malfunctionable) i.next();
			boolean isStructure = (e instanceof Structure);
			boolean uninhabitableBuilding = false;
			if (e instanceof Building) 
				uninhabitableBuilding = !((Building) e).hasFunction(LifeSupport.NAME);
			MalfunctionManager manager = e.getMalfunctionManager();
			boolean hasMalfunction = manager.hasMalfunction();
			if ((isStructure || uninhabitableBuilding) && !hasMalfunction) {
				double entityWeight = manager.getEffectiveTimeSinceLastMaintenance();
				totalProbabilityWeight += entityWeight;
			}
		}
		
		// Randomly determine a malfunctionable entity.
		double chance = RandomUtil.getRandomDouble(totalProbabilityWeight);
		
		// Get the malfunctionable entity chosen.
		i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext()) {
			Malfunctionable malfunctionable = (Malfunctionable) i.next();
			boolean isStructure = (malfunctionable instanceof Structure);
			boolean uninhabitableBuilding = false;
			if (malfunctionable instanceof Building) 
				uninhabitableBuilding = !((Building) malfunctionable).hasFunction(LifeSupport.NAME);
			MalfunctionManager manager = malfunctionable.getMalfunctionManager();
			boolean hasMalfunction = manager.hasMalfunction();
			if ((isStructure || uninhabitableBuilding) && !hasMalfunction) {
				double entityWeight = manager.getEffectiveTimeSinceLastMaintenance();
			
				if (chance < entityWeight) {
					result = malfunctionable;
					description = "Performing maintenance on " + result.getName();
					break;
				}
				else chance -= entityWeight;
			}
		}
    	
		return result;
	}

	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int mechanicsSkill = manager.getEffectiveSkillLevel(Skill.MECHANICS);
		return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D); 
	}

	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		results.add(Skill.EVA_OPERATIONS);
		results.add(Skill.MECHANICS);
		return results;
	}
}