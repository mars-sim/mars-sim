/**
 * Mars Simulation Project
 * RepairEVAMalfunction.java
 * @version 3.07 2014-12-27
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
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.vehicle.Vehicle;

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

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.repairEVAMalfunction"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase REPAIRING = new TaskPhase(Msg.getString(
            "Task.phase.repairing")); //$NON-NLS-1$

    // Data members
    /** The malfunctionable entity being repaired. */
    private Malfunctionable entity;
    
    /** The malfunction to be repaired. */
    private Malfunction malfunction;
    
    /** True if repairing the EVA part of the malfunction. */
    private boolean isEVAMalfunction;
    
    /** The container unit the person started the mission in. */
    private Unit containerUnit;

    /**
     * Constructs a RepairEVAMalfunction object.
     * @param person the person to perform the task
     */
    public RepairEVAMalfunction(Person person) {
        super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D);

        containerUnit = person.getTopContainerUnit();

        // Get the malfunctioning entity.
        entity = getEVAMalfunctionEntity(person);
        if (entity != null) {
            malfunction = getMalfunction(person, entity);
            isEVAMalfunction = canRepairEVA(malfunction);
            
            setDescription(Msg.getString("Task.description.repairEVAMalfunction.detail", 
                  malfunction.getName(), entity.getName())); //$NON-NLS-1$
            
            // Determine location for repairing malfunction.
            Point2D malfunctionLoc = determineMalfunctionLocation();
            setOutsideSiteLocation(malfunctionLoc.getX(), malfunctionLoc.getY());
        }
        else {
            endTask();
        }

        // Initialize phase
        addPhase(REPAIRING);

        logger.fine(person.getName() + " has started the RepairEVAMalfunction task.");
    }

    /**
     * Gets a malfunctionable entity with an EVA-required malfunction.
     * @param person the person.
     * @return malfunctionable entity.
     */
    private Malfunctionable getEVAMalfunctionEntity(Person person) {
        Malfunctionable result = null;

        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext() && (result == null)) {
            Malfunctionable entity = i.next();
            if (getMalfunction(person, entity) != null) {
                result = entity;
            }
            MalfunctionManager manager = entity.getMalfunctionManager();
            
            // Check if entity has any EVA malfunctions.
            Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
            while (j.hasNext() && (result == null)) {
                Malfunction malfunction = j.next();
                try {
                    if (RepairEVAMalfunction.hasRepairPartsForMalfunction(person, person.getTopContainerUnit(), 
                            malfunction)) {
                        result = entity;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            
            // Check if entity requires an EVA and has any normal malfunctions.
            if ((result == null) && requiresEVA(person, entity)) {
                Iterator<Malfunction> k = manager.getNormalMalfunctions().iterator();
                while (k.hasNext() && (result == null)) {
                    Malfunction malfunction = k.next();
                    try {
                        if (RepairMalfunction.hasRepairPartsForMalfunction(person, malfunction)) {
                            result = entity;
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            }
        }

        return result;
    }
    
    /**
     * Check if a malfunctionable entity requires an EVA to repair.
     * @param person the person doing the repair.
     * @param entity the entity with a malfunction.
     * @return true if entity requires an EVA repair.
     */
    public static boolean requiresEVA(Person person, Malfunctionable entity) {
        
        boolean result = false;
        
        if (entity instanceof Vehicle) {
            // Requires EVA repair on outside vehicles that the person isn't inside.
            Vehicle vehicle = (Vehicle) entity;
            boolean outsideVehicle = BuildingManager.getBuilding(vehicle) == null;
            boolean personNotInVehicle = !vehicle.getInventory().containsUnit(person);
            if (outsideVehicle && personNotInVehicle) {
                result = true;
            }
        }
        else if (entity instanceof Building) {
            // Requires EVA repair on uninhabitable buildings.
            Building building = (Building) entity;
            if (!building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
                result = true;
            }
        }
        
        return result;
    }
    
    /**
     * Gets a reparable malfunction requiring an EVA for a given entity.
     * @param person the person to repair.
     * @param entity the entity with a malfunction.
     * @return malfunction requiring an EVA repair or null if none found.
     */
    private Malfunction getMalfunction(Person person, Malfunctionable entity) {
        
        Malfunction result = null;
        
        MalfunctionManager manager = entity.getMalfunctionManager();
        
        // Check if entity has any EVA malfunctions.
        Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
        while (j.hasNext() && (result == null)) {
            Malfunction malfunction = j.next();
            try {
                if (RepairEVAMalfunction.hasRepairPartsForMalfunction(person, person.getTopContainerUnit(), 
                        malfunction)) {
                    result = malfunction;
                }
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        
        // Check if entity requires an EVA and has any normal malfunctions.
        if ((result == null) && requiresEVA(person, entity)) {
            Iterator<Malfunction> k = manager.getNormalMalfunctions().iterator();
            while (k.hasNext() && (result == null)) {
                Malfunction malfunction = k.next();
                try {
                    if (RepairMalfunction.hasRepairPartsForMalfunction(person, malfunction)) {
                        result = malfunction;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a malfunction requires EVA repair.
     * @param malfunction the malfunction.
     * @return true if malfunction requires EVA repair.
     */
    private boolean canRepairEVA(Malfunction malfunction) {
        
        boolean result = false;
        
        if ((malfunction.getEVAWorkTime() - malfunction.getCompletedEVAWorkTime()) > 0D) {
            result = true;
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
    public static boolean hasRepairPartsForMalfunction(Person person, Unit containerUnit, 
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
    protected TaskPhase getOutsideSitePhase() {
        return REPAIRING;
    }

    @Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);

        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (REPAIRING.equals(getPhase())) {
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
        int experienceAptitude = nManager.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);

        // If phase is repair malfunction, add experience to mechanics skill.
        if (REPAIRING.equals(getPhase())) {
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

        boolean finishedRepair = false;
        if (isEVAMalfunction) {
            if ((malfunction.getEVAWorkTime() - malfunction.getCompletedEVAWorkTime()) <= 0D) {
                finishedRepair = true;
            }
        }
        else {
            if ((malfunction.getWorkTime() - malfunction.getCompletedWorkTime() <= 0D)) {
                finishedRepair = true;
            }
        }
        
        if (finishedRepair || shouldEndEVAOperation() || addTimeOnSite(time)) {
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

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
        double workTimeLeft = 0D;
        if (isEVAMalfunction) {
            workTimeLeft = malfunction.addEVAWorkTime(workTime);
        }
        else {
            workTimeLeft = malfunction.addWorkTime(workTime);
        }

        // Add experience points
        addExperience(time);

        // Check if an accident happens during repair.
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