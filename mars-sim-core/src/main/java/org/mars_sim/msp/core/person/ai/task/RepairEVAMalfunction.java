/**
 * Mars Simulation Project
 * RepairEVAMalfunction.java
 * @version 3.06 2014-03-04
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The RepairEVAMalfunction class is a task to repair a malfunction requiring an EVA.
 */
public class RepairEVAMalfunction
extends EVAOperation
implements Repair, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(RepairEVAMalfunction.class.getName());

	// TODO Phase names should be an enum.
	private static final String REPAIR_MALFUNCTION = "Repair Malfunction";

	// Data members
	/** The malfunctionable entity being repaired. */
	private Malfunctionable entity;
	/** The container unit the person started the mission in. */
	private Unit containerUnit;

	/**
	 * Constructs a RepairEVAMalfunction object.
	 * @param person the person to perform the task
	 */
    public RepairEVAMalfunction(Person person) {
        super("Repairing EVA Malfunction", person, true, RandomUtil.getRandomDouble(50D) + 10D);

        containerUnit = person.getTopContainerUnit();
        
        // Get the malfunctioning entity.
        entity = getEVAMalfunctionEntity(person, containerUnit);
        if (entity == null) {
            endTask();
            return;
        }

        // Determine location for repairing malfunction.
        Point2D malfunctionLoc = determineMalfunctionLocation();
        setOutsideSiteLocation(malfunctionLoc.getX(), malfunctionLoc.getY());

        // Initialize phase
        addPhase(REPAIR_MALFUNCTION);

        logger.fine(person.getName() + " has started the RepairEVAMalfunction task.");
    }

    /**
     * Checks if the malfunctionable entity has a local EVA malfunction.
     * @param containerUnit the unit the person is doing an EVA from.
     * @return true if malfunction, false if none.
     */
    private static boolean hasEVAMalfunction(Person person, Unit containerUnit, 
            Malfunctionable entity) {

        boolean result = false;

        MalfunctionManager manager = entity.getMalfunctionManager();
        Iterator<Malfunction> i = manager.getEVAMalfunctions().iterator();
        while (i.hasNext() && !result) {
            if (hasRepairPartsForMalfunction(person, containerUnit, i.next())) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Gets a malfunctionable entity with an EVA malfunction for a user.
     * @param person the person.
     * @param containerUnit the unit the person is doing an EVA from.
     * @return malfunctionable entity.
     */
    private static Malfunctionable getEVAMalfunctionEntity(Person person, Unit containerUnit) {
        Malfunctionable result = null;

        Collection<Malfunctionable> malfunctionables = null;
        if (containerUnit instanceof Malfunctionable) {
            malfunctionables = MalfunctionFactory.getMalfunctionables((Malfunctionable) containerUnit);
        }
        else if (containerUnit instanceof Settlement) {
            malfunctionables = MalfunctionFactory.getMalfunctionables((Settlement) containerUnit);
        }

        if (malfunctionables != null) {
            Iterator<Malfunctionable> i = malfunctionables.iterator();
            while (i.hasNext() && (result == null)) {
                Malfunctionable entity = i.next();
                if (hasEVAMalfunction(person, containerUnit, entity)) {
                    result = entity;
                }
            }
        }

        return result;
    }

    /**
     * Checks if there are enough repair parts at person's location to fix the malfunction.
     * @param person the person checking.
     * @param containerUnit the unit the person is doing an EVA from.
     * @param malfunction the malfunction.
     * @return true if enough repair parts to fix malfunction.
     */
    private static boolean hasRepairPartsForMalfunction(Person person, Unit containerUnit, 
            Malfunction malfunction) {
        if (person == null) {
            throw new IllegalArgumentException("person is null");
        }
        if (containerUnit == null) {
            throw new IllegalArgumentException("containerUnit is null");
        }
        if (malfunction == null) {
            throw new IllegalArgumentException("malfunction is null");
        }

        boolean result = true;    	
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

        return result;
    }

    /** Returns the weighted probability that a person might perform this task.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        // Total probabilities for all malfunctionable entities in person's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            MalfunctionManager manager = i.next().getMalfunctionManager();
            Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
            while (j.hasNext()) {
                Malfunction malfunction = j.next();
                try {
                    if (hasRepairPartsForMalfunction(person, person.getTopContainerUnit(), 
                            malfunction)) {
                        result += 100D;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        // Check if an airlock is available
        if (getWalkableAvailableAirlock(person) == null) {
            result = 0D;
        }

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
            if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                result = 0D;
            }
        } 

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Check if person is in vehicle.
        boolean inVehicle = LocationSituation.IN_VEHICLE == person.getLocationSituation();

        // Job modifier if not in vehicle.
        Job job = person.getMind().getJob();
        if ((job != null) && !inVehicle) {
            result *= job.getStartTaskProbabilityModifier(RepairEVAMalfunction.class);        
        }

        return result;
    }

    /**
     * Determine location to repair malfunction.
     * @return location.
     */
    private Point2D determineMalfunctionLocation() {

        Point2D.Double newLocation = new Point2D.Double(0D, 0D);

        if (entity instanceof LocalBoundedObject) {
            LocalBoundedObject bounds = (LocalBoundedObject) entity;
            boolean goodLocation = false;
            for (int x = 0; (x < 50) && !goodLocation; x++) {
                Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(
                        bounds, 1D);
                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), bounds);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), 
                        newLocation.getY(), person.getCoordinates());
            }
        }

        return newLocation;
    }

    @Override
    protected String getOutsideSitePhase() {
        return REPAIR_MALFUNCTION;
    }

    @Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);

        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (REPAIR_MALFUNCTION.equals(getPhase())) {
            return repairMalfunctionPhase(time);
        }
        else {
            return time;
        }
    }

    @Override
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
		person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);

        // If phase is repair malfunction, add experience to mechanics skill.
        if (REPAIR_MALFUNCTION.equals(getPhase())) {
            // 1 base experience point per 20 millisols of collection time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double mechanicsExperience = time / 20D;
            mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
			person.getMind().getSkillManager().addExperience(SkillType.MECHANICS, mechanicsExperience);
		}
	}

    /**
     * Perform the repair malfunction phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double repairMalfunctionPhase(double time) {

        if (!hasEVAMalfunction(person, containerUnit, entity) || shouldEndEVAOperation() || 
                addTimeOnSite(time)) {
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
		int mechanicSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Get a local malfunction.
        Malfunction malfunction = null;
        Iterator<Malfunction> i = entity.getMalfunctionManager().getEVAMalfunctions().iterator();
        while (i.hasNext() && (malfunction == null)) {
            Malfunction tempMalfunction = i.next();
            if (hasRepairPartsForMalfunction(person, containerUnit, tempMalfunction)) {
                malfunction = tempMalfunction;
                setDescription("Repairing " + malfunction.getName() + " on " + entity.getName());
            }
        }

        // Add repair parts if necessary.
        Inventory inv = containerUnit.getInventory();
        if (hasRepairPartsForMalfunction(person, containerUnit, malfunction)) {
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
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Add EVA work to malfunction.
        double workTimeLeft = malfunction.addEVAWorkTime(workTime);

        // Add experience points
        addExperience(time);

        // Check if there are no more malfunctions. 
        if (!hasEVAMalfunction(person, containerUnit, entity)) setPhase(WALK_BACK_INSIDE);

        // Check if an accident happens during maintenance.
        checkForAccident(time);

        return (workTimeLeft / workTime) * time;
    }

    @Override
    public Malfunctionable getEntity() {
        return entity;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
		int mechanicsSkill = manager.getEffectiveSkillLevel(SkillType.MECHANICS);
        return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D); 
    }

    @Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(2);
		results.add(SkillType.EVA_OPERATIONS);
		results.add(SkillType.MECHANICS);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        entity = null;
    }
}