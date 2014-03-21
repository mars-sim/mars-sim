/**
 * Mars Simulation Project
 * RepairEmergencyMalfunction.java
 * @version 3.06 2014-02-26
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LifeSupport;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * The RepairEmergencyMalfunction class is a task to repair an emergency malfunction.
 */
public class RepairEmergencyMalfunction
extends Task
implements Repair, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(RepairEmergencyMalfunction.class.getName());

	// TODO Task phase should be an enum.
	private static final String REPAIRING = "Repairing";

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = 2D;

	// Data members
	/** The entity being repaired. */
	private Malfunctionable entity;
	/** Problem being fixed. */
	private Malfunction malfunction;

	/**
	 * Constructs a RepairEmergencyMalfunction object.
	 * @param person the person to perform the task
	 */
    public RepairEmergencyMalfunction(Person person) {
        super("Repairing Emergency Malfunction", person, true, true, STRESS_MODIFIER, false, 0D);

        claimMalfunction();

		// Add person to malfunctioning building if necessary.
		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	        if (entity != null) {
	            // Add person to building if malfunctionable is a building with life support.
	            addPersonToMalfunctionableBuilding(entity); 
	        }
	        else {
	            endTask();
			}
		}

		// Create starting task event if needed.
		if (getCreateEvents()) {
			TaskEvent startingEvent = new TaskEvent(person, this, EventType.TASK_START, "");
			Simulation.instance().getEventManager().registerNewEvent(startingEvent);
		}
		
		// Initialize task phase
		addPhase(REPAIRING);
		setPhase(REPAIRING);

        if (malfunction != null) {
            logger.fine(person.getName() + " starting work on emergency malfunction: " + 
                    malfunction.getName() + "@" + Integer.toHexString(malfunction.hashCode()));
        }
    }
    
    @Override
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) {
    	    throw new IllegalArgumentException("Task phase is null");
    	}
    	else if (REPAIRING.equals(getPhase())) {
    	    return repairingPhase(time);
    	}
    	else {
    	    return time;
    	}
    }
    
    /**
     * Performs the repairing phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left after performing the phase.
     */
    private double repairingPhase(double time) {
    	
        // Check if there emergency malfunction work is fixed.
        double workTimeLeft = malfunction.getEmergencyWorkTime() -
             malfunction.getCompletedEmergencyWorkTime();
        if (workTimeLeft == 0) {
            endTask();
        }

        if (isDone()) {
            return time;
        }

        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = getEffectiveSkillLevel();
        if (mechanicSkill == 0) {
            workTime /= 2;
        }
        else if (mechanicSkill > 1) {
            workTime += (workTime * (.2D * mechanicSkill));
        }

        // Add work to emergency malfunction.
        double remainingWorkTime = malfunction.addEmergencyWorkTime(workTime);
        if (remainingWorkTime > 0D) {
            endTask();
        }

        // Add experience
        addExperience(time);

        return (time * (remainingWorkTime / workTime));
    }
    
	@Override
	protected void addExperience(double time) {
		// Add experience to "Mechanics" skill
		// (1 base experience point per 20 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 20D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
        	NaturalAttribute.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(SkillType.MECHANICS, newPoints);
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
            if (manager.hasEmergencyMalfunction()) {
                result = true;
            }
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
     * Adds the person to building if malfunctionable is a building with life support.
     * Otherwise does nothing.
     * @param malfunctionable the malfunctionable the person is repairing.
     */
    private void addPersonToMalfunctionableBuilding(Malfunctionable malfunctionable) {
        
        if (malfunctionable instanceof Building) {
            Building building = (Building) malfunctionable;
            if (building instanceof LifeSupport) {
                
                // Walk to malfunctioning building.
                walkToMalfunctioningBuilding(building);
            }
        }
    }
    
    /**
     * Walk to malfunctioning building.
     * @param malfunctioningBuilding the malfunctioning building.
     */
    private void walkToMalfunctioningBuilding(Building malfunctioningBuilding) {
        
        // Determine location within malfunctioning building.
        // TODO: Use action point rather than random internal location.
        Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(malfunctioningBuilding);
        Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
                buildingLoc.getY(), malfunctioningBuilding);
        
        if (Walk.canWalkAllSteps(person, settlementLoc.getX(), settlementLoc.getY(), 
                malfunctioningBuilding)) {
            
            // Add subtask for walking to malfunctioning building.
            addSubTask(new Walk(person, settlementLoc.getX(), settlementLoc.getY(), 
                    malfunctioningBuilding));
        }
        else {
            logger.fine(person.getName() + " unable to walk to malfunctioning building " + 
                    malfunctioningBuilding.getName());
            endTask();
        }
    }
    
	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(SkillType.MECHANICS);
	}  
	
	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.MECHANICS);
		return results;
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    entity = null;
	    malfunction = null;
	}
}