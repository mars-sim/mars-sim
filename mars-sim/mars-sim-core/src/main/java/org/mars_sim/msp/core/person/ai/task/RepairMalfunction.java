/**
 * Mars Simulation Project
 * RepairMalfunction.java
 * @version 3.06 2014-02-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupport;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * The RepairMalfunction class is a task to repair a malfunction.
 */
public class RepairMalfunction
extends Task
implements Repair, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(RepairMalfunction.class.getName());

	// TODO Task phase should be an enum.
	private static final String REPAIRING = "Repairing";

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .3D;

	// Data members
	/** Entity being repaired. */
	private Malfunctionable entity;

	/**
	 * Constructor
	 * @param person the person to perform the task
	 */
	public RepairMalfunction(Person person) {
		super("Repairing Malfunction", person, true, false, STRESS_MODIFIER, true, 10D + 
				RandomUtil.getRandomDouble(50D));

		// Get the malfunctioning entity.
		entity = getMalfunctionEntity(person);
        if (entity != null) {
            // Add person to building if malfunctionable is a building with life support.
            addPersonToMalfunctionableBuilding(entity); 
        }
        else {
            endTask();
        }

        // Initialize phase
        addPhase(REPAIRING);
        setPhase(REPAIRING);

        logger.fine(person.getName() + " repairing malfunction.");
    }

    /**
     * Gets a malfunctional entity with a normal malfunction for a user.
     * @param person the person.
     * @return malfunctional entity.
     */
    private static Malfunctionable getMalfunctionEntity(Person person) {
        Malfunctionable result = null;

        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext() && (result == null)) {
            Malfunctionable entity = i.next();
            if (hasMalfunction(person, entity)) {
                result = entity;
            }
        }

        return result;
    }

    /**
     * Gets a malfunctional entity with a normal malfunction for a user.
     * @return malfunctional entity.
     */
    private static boolean hasMalfunction(Person person, Malfunctionable entity) {
        boolean result = false;

        MalfunctionManager manager = entity.getMalfunctionManager();
        Iterator<Malfunction> i = manager.getNormalMalfunctions().iterator();
        while (i.hasNext() && !result) {
            if (hasRepairPartsForMalfunction(person, i.next())) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Checks if there are enough repair parts at person's location to fix the malfunction.
     * @param person the person checking.
     * @param malfunction the malfunction.
     * @return true if enough repair parts to fix malfunction.
     */
    private static boolean hasRepairPartsForMalfunction(Person person, 
            Malfunction malfunction) {
        if (person == null) {
            throw new IllegalArgumentException("person is null");
        }
        if (malfunction == null) {
            throw new IllegalArgumentException("malfunction is null");
        }

        boolean result = false;    	
        Unit containerUnit = person.getTopContainerUnit();

        if (containerUnit != null) {
            result = true;
            Inventory inv = containerUnit.getInventory();

            Map<Part, Integer> repairParts = malfunction.getRepairParts();
            Iterator<Part> i = repairParts.keySet().iterator();
            while (i.hasNext() && result) {
                Part part = i.next();
                int number = repairParts.get(part);
                if (inv.getItemResourceNum(part) < number) {
                    result = false;
                }
            }
        }

        return result;
    }

    /** 
     *  Returns the weighted probability that a person might perform this task.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        // Total probabilities for all malfunctionable entities in person's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
            MalfunctionManager manager = entity.getMalfunctionManager();
            Iterator<Malfunction> j = manager.getNormalMalfunctions().iterator();
            while (j.hasNext()) {
                Malfunction malfunction = j.next();
                try {
                    if (hasRepairPartsForMalfunction(person, malfunction)) {
                        result += 100D;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(RepairMalfunction.class);        
        }

        return result;
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

        // Check if there are no more malfunctions.
        if (!hasMalfunction(person, entity)) {
            endTask();
        }

        if (isDone()) {
            return time;
        }

        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
        if (mechanicSkill == 0) {
            workTime /= 2;
        }
        else if (mechanicSkill > 1) {
            workTime += workTime * (.2D * mechanicSkill);
        }

        // Get a local malfunction.
        Malfunction malfunction = null;
        Iterator<Malfunction> i = entity.getMalfunctionManager().getNormalMalfunctions().iterator();
        while (i.hasNext() && (malfunction == null)) {
            Malfunction tempMalfunction = i.next();
            if (hasRepairPartsForMalfunction(person, tempMalfunction)) {
                malfunction = tempMalfunction;
                setDescription("Repairing " + malfunction.getName() + " on " + entity.getName());
            }
        }

        // Add repair parts if necessary.
        if (hasRepairPartsForMalfunction(person, malfunction)) {
            Inventory inv = person.getTopContainerUnit().getInventory();
            Map<Part, Integer> parts = new HashMap<Part, Integer>(malfunction.getRepairParts());
            Iterator<Part> j = parts.keySet().iterator();
            while (j.hasNext()) {
                Part part = j.next();
                int number = parts.get(part);
                inv.retrieveItemResources(part, number);
                malfunction.repairWithParts(part, number);
            }
        }
        else {
            endTask();
            return time;
        }

        // Add work to malfunction.
        // logger.info(description);
        double workTimeLeft = malfunction.addWorkTime(workTime);

        // Add experience
        addExperience(time);

        // Check if an accident happens during maintenance.
        checkForAccident(time);

        // Check if there are no more malfunctions.
        if (!hasMalfunction(person, entity)) {
            endTask();
        }

        return (workTimeLeft / workTime) / time;
    }

    @Override
    protected void addExperience(double time) {
        // Add experience to "Mechanics" skill
        // (1 base experience point per 20 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 20D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(SkillType.MECHANICS, newPoints);
    }

    /**
     * Check for accident with entity during maintenance phase.
     * @param time the amount of time (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
        if (skill <= 3) {
            chance *= (4 - skill);
        }
        else {
            chance /= (skill - 2);
        }

        // Modify based on the entity's wear condition.
        chance *= entity.getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // logger.info(person.getName() + " has accident while " + description);
            if (entity != null) {
                entity.getMalfunctionManager().accident();
            }
        }
    }

    @Override
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
    }
}