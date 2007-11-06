/**
 * Mars Simulation Project
 * MaintenanceEVA.java
 * @version 2.82 2007-11-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.mars.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;

/** 
 * The Maintenance class is a task for performing
 * preventive maintenance on malfunctionable entities outdoors.
 */
public class MaintenanceEVA extends EVAOperation implements Serializable {
	
	// Task phase
	private static final String MAINTAIN = "Maintenance";
	
	private Malfunctionable entity; // Entity to be maintained.
	private Airlock airlock; // Airlock to be used for EVA.
	
	/** 
	 * Constructor
	 * @param person the person to perform the task
	 * @throws Exception if error constructing task.
	 */
	public MaintenanceEVA(Person person) throws Exception {
		super("Performing EVA Maintenance", person);
		
		// Get an available airlock.
		airlock = getAvailableAirlock(person);
		if (airlock == null) endTask();
		
		try {
			entity = getMaintenanceMalfunctionable();
			if (entity == null) endTask();
		}
		catch (Exception e) {
			System.err.println("MaintenanceEVA.constructor(): " + e.getMessage());
			endTask();
		}
		
		// Initialize phase
		addPhase(MAINTAIN);
		
		// Set initial phase.
		setPhase(EXIT_AIRLOCK);
		
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

		try {
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
				boolean hasParts = Maintenance.hasMaintenanceParts(person, entity);
				double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
				boolean minTime = (effectiveTime >= 1000D);
				if ((isStructure || uninhabitableBuilding) && !hasMalfunction && minTime && hasParts) {
					double entityProb = manager.getEffectiveTimeSinceLastMaintenance() / 1000D;
					if (entityProb > 100D) entityProb = 100D;
					result += entityProb;
				}
			}   
		}
		catch (Exception e) {
    		e.printStackTrace(System.err);
    	}
		
		// Check if an airlock is available
		if (getAvailableAirlock(person) == null) result = 0D;

		// Check if it is night time.
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
			if (!surface.inDarkPolarRegion(person.getCoordinates()))
				result = 0D;
		} 
		
		// Crowded settlement modifier
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
			if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) result *= 2D;
		}
	
		// Effort-driven task modifier.
		result *= person.getPerformanceRating();
        
		// Job modifier.
		Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(MaintenanceEVA.class);        
	
		return result;
	}
	
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitEVA(time);
    	if (MAINTAIN.equals(getPhase())) return maintenance(time);
    	if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterEVA(time);
    	else return time;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;
		
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
		
		// If phase is maintenance, add experience to mechanics skill.
		if (MAINTAIN.equals(getPhase())) {
			// 1 base experience point per 100 millisols of time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double mechanicsExperience = time / 100D;
			mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
			person.getMind().getSkillManager().addExperience(Skill.MECHANICS, mechanicsExperience);
		}
	}
	
	/**
	 * Perform the exit airlock phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error exiting the airlock.
	 */
	private double exitEVA(double time) throws Exception {
		
    	try {
    		time = exitAirlock(time, airlock);
        
    		// Add experience points
    		addExperience(time);
    	}
		catch (Exception e) {
			// Person unable to exit airlock.
			endTask();
		}
        
		if (exitedAirlock) setPhase(MAINTAIN);
		return time;
	}
	
	/**
	 * Perform the maintenance phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error during maintenance.
	 */
	private double maintenance(double time) throws Exception {
        
		MalfunctionManager manager = entity.getMalfunctionManager();
		boolean malfunction = manager.hasMalfunction();
		boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() < 1000D);
        
		if (finishedMaintenance || malfunction || shouldEndEVAOperation()) {
			setPhase(ENTER_AIRLOCK);
			return time;
		}
        
		// Determine effective work time based on "Mechanic" skill.
		double workTime = time;
		int mechanicSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
		if (mechanicSkill == 0) workTime /= 2;
		if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

		// Add work to the maintenance
		manager.addMaintenanceWorkTime(workTime);

        // Add repair parts if necessary.
        Inventory inv = containerUnit.getInventory();
        Map<Part, Integer> parts = new HashMap<Part, Integer>(manager.getMaintenanceParts());
        Iterator<Part> j = parts.keySet().iterator();
        while (j.hasNext()) {
          	Part part = j.next();
           	int number = parts.get(part);
           	inv.retrieveItemResources(part, number);
           	manager.maintainWithParts(part, number);
        }
		
        // Add experience points
        addExperience(time);
	
		// Check if an accident happens during maintenance.
		checkForAccident(time);

		return 0D;
	}  

	/**
	 * Perform the enter airlock phase of the task.
	 * @param time amount of time to perform the phase
	 * @return time remaining after performing the phase
	 * @throws Exception if error entering airlock.
	 */
	private double enterEVA(double time) throws Exception {
		time = enterAirlock(time, airlock);
		
        // Add experience points
        addExperience(time);
        
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
		int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
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
		while (i.hasNext()) totalProbabilityWeight += getProbabilityWeight((Malfunctionable) i.next());
		
		// Randomly determine a malfunctionable entity.
		double chance = RandomUtil.getRandomDouble(totalProbabilityWeight);
		
		// Get the malfunctionable entity chosen.
		i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext()) {
			Malfunctionable malfunctionable = (Malfunctionable) i.next();
			double entityWeight = getProbabilityWeight(malfunctionable);
			if (chance < entityWeight) {
				result = malfunctionable;
				setDescription("Performing maintenance on " + result.getName());
				break;
			}
			else chance -= entityWeight;
		}
    	
		return result;
	}
	
	/**
	 * Gets the probability weight for a malfunctionable.
	 * @param malfunctionable the malfunctionable.
	 * @return the probability weight.
	 */
	private double getProbabilityWeight(Malfunctionable malfunctionable) throws Exception {
		double result = 0D;
		boolean isStructure = (malfunctionable instanceof Structure);
		boolean uninhabitableBuilding = false;
		if (malfunctionable instanceof Building) 
			uninhabitableBuilding = !((Building) malfunctionable).hasFunction(LifeSupport.NAME);
		MalfunctionManager manager = malfunctionable.getMalfunctionManager();
		boolean hasMalfunction = manager.hasMalfunction();
		boolean hasParts = Maintenance.hasMaintenanceParts(person, malfunctionable);
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		boolean minTime = (effectiveTime >= 1000D); 
		if ((isStructure || uninhabitableBuilding) && !hasMalfunction && minTime && hasParts) result = effectiveTime;
		return result;
	}

	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int mechanicsSkill = manager.getEffectiveSkillLevel(Skill.MECHANICS);
		return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D); 
	}

	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(2);
		results.add(Skill.EVA_OPERATIONS);
		results.add(Skill.MECHANICS);
		return results;
	}
}