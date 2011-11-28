/**
 * Mars Simulation Project
 * RepairEmergencyMalfunction.java
 * @version 3.02 2011-11-27
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The RepairEmergencyMalfunction class is a task to repair an emergency malfunction.
 */
public class RepairEmergencyMalfunction extends Task implements Repair, Serializable {

	// Task phase
	private static final String REPAIRING = "Repairing";
	
	// Static members
	private static final double STRESS_MODIFIER = 2D; // The stress modified per millisol.

    // Data members
    private Malfunctionable entity; // The entity being repaired.
    private Malfunction malfunction; // Problem being fixed

    /**
     * Constructs a RepairEmergencyMalfunction object.
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public RepairEmergencyMalfunction(Person person) {
        super("Repairing Emergency Malfunction", person, true, true, STRESS_MODIFIER, false, 0D);

        claimMalfunction();

		// Add person to malfunctioning building if necessary.
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			if (entity instanceof Building) {
				BuildingManager.addPersonToBuilding(person, (Building) entity);
			}
		}

		// Create starting task event if needed.
		if (getCreateEvents()) {
			TaskEvent startingEvent = new TaskEvent(person, this, TaskEvent.START, "");
			Simulation.instance().getEventManager().registerNewEvent(startingEvent);
		}
		
		// Initialize task phase
		addPhase(REPAIRING);
		setPhase(REPAIRING);

        // if (malfunction != null) logger.info(person.getName() + " starting work on emergency malfunction: " + malfunction.getName() + "@" + Integer.toHexString(malfunction.hashCode()));
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (REPAIRING.equals(getPhase())) return repairingPhase(time);
    	else return time;
    }
    
    /**
     * Performs the repairing phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double repairingPhase(double time) {
    	
        // Check if there emergency malfunction work is fixed.
        double workTimeLeft = malfunction.getEmergencyWorkTime() -
             malfunction.getCompletedEmergencyWorkTime();
        if (workTimeLeft == 0) endTask();

        if (isDone()) return time;

        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = getEffectiveSkillLevel();
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += (workTime * (.2D * mechanicSkill));

        // Add work to emergency malfunction.
        double remainingWorkTime = malfunction.addEmergencyWorkTime(workTime);
        if (remainingWorkTime > 0D) endTask();

        // Add experience
        addExperience(time);

        return (time * (remainingWorkTime / workTime));
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience to "Mechanics" skill
		// (1 base experience point per 20 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 20D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
        	NaturalAttributeManager.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(Skill.MECHANICS, newPoints);
	}

    /**
     * Checks if the person has a local emergency malfunction.
     * @return true if emergency, false if none.
     */
    public static boolean hasEmergencyMalfunction(Person person) {

        boolean result = false;

        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
            MalfunctionManager manager = entity.getMalfunctionManager();
            if (manager.hasEmergencyMalfunction()) result = true;
        }

        return result;
    }

	/**
	 * Gets a local emergency malfunction.
	 */
    private void claimMalfunction() {
        malfunction = null;
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext() && (malfunction == null)) {
            Malfunctionable e = i.next();
            MalfunctionManager manager = e.getMalfunctionManager();
            if (manager.hasEmergencyMalfunction()) {
                malfunction = manager.getMostSeriousEmergencyMalfunction();
                entity = e;
                setDescription("Emergency repair " + malfunction.getName() + " on " + entity);
            }
        }
    }

    /**
     * Gets the malfunctionable entity the person is currently repairing or null if none.
     * @return entity
     */
    public Malfunctionable getEntity() {
        return entity;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.MECHANICS);
	}  
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(1);
		results.add(Skill.MECHANICS);
		return results;
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    entity = null;
	    malfunction = null;
	}
}