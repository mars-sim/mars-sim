/**
 * Mars Simulation Project
 * Maintenance.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Manufacture;

/**
 * A task for working on a manufacturing process.
 */
public class ManufactureGood extends Task implements Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.ManufactureGood";

    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // Task phase
    private static final String MANUFACTURE = "Manufacture";

    // Static members
    private static final double STRESS_MODIFIER = .1D; // The stress modified
                                                       // per millisol.

    // Data members
    private Manufacture workshop; // The manufacturing workshop the person is
                                  // using.

    /**
     * Constructor
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public ManufactureGood(Person person) throws Exception {
        super("Manufacturing", person, true, false, STRESS_MODIFIER, true, RandomUtil.getRandomDouble(100D));

        // Initialize data members
        if (person.getSettlement() != null)
            setDescription("Manufacturing at " + person.getSettlement().getName());
        else
            endTask();

        // Get available manufacturing workshop if any.
        try {
            Building manufactureBuilding = getAvailableManufacturingBuilding(person);
            if (manufactureBuilding != null) {
                workshop = (Manufacture) manufactureBuilding.getFunction(Manufacture.NAME);
                BuildingManager.addPersonToBuilding(person, manufactureBuilding);
            } else
                endTask();
        } catch (BuildingException e) {
            logger.log(Level.SEVERE, "ManufactureGood", e);
            endTask();
        }

        // Initialize phase
        addPhase(MANUFACTURE);
        setPhase(MANUFACTURE);
    }

    /**
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            try {
                // See if there is an available manufacturing building.
                Building manufacturingBuilding = getAvailableManufacturingBuilding(person);
                if (manufacturingBuilding != null) {
                    result = 1D;

                    // Crowding modifier.
                    result *= Task.getCrowdingProbabilityModifier(person, manufacturingBuilding);
                    result *= Task.getRelationshipModifier(person, manufacturingBuilding);

                    // Manufacturing good value modifier.
                    result *= getHighestManufacturingProcessValue(person, manufacturingBuilding) * 10D;

                    // Add a base chance.
                    if (result > 0D)
                        result += 25D;

                    // If manufacturing building has process requiring work, add
                    // modifier.
                    SkillManager skillManager = person.getMind().getSkillManager();
                    int skill = skillManager.getEffectiveSkillLevel(Skill.MATERIALS_SCIENCE);
                    if (hasProcessRequiringWork(manufacturingBuilding, skill))
                        result += 10D;
                    // If settlement has manufacturing override, no new
                    // manufacturing processes can be created.
                    else if (person.getSettlement().getManufactureOverride())
                        result = 0;
                }
            } catch (BuildingException e) {
                logger.log(Level.SEVERE, "ManufactureGood.getProbability()", e);
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null)
            result *= job.getStartTaskProbabilityModifier(ManufactureGood.class);

        return result;
    }

    /**
     * Gets an available manufacturing building that the person can use. Returns
     * null if no manufacturing building is currently available.
     * @param person the person
     * @return available manufacturing building
     * @throws BuildingException if error finding manufacturing building.
     */
    private static Building getAvailableManufacturingBuilding(Person person) throws BuildingException {

        Building result = null;

        SkillManager skillManager = person.getMind().getSkillManager();
        int skill = skillManager.getEffectiveSkillLevel(Skill.MATERIALS_SCIENCE);

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            BuildingManager manager = person.getSettlement().getBuildingManager();
            List<Building> manufacturingBuildings = manager.getBuildings(Manufacture.NAME);
            manufacturingBuildings = BuildingManager.getNonMalfunctioningBuildings(manufacturingBuildings);
            manufacturingBuildings = getManufacturingBuildingsNeedingWork(manufacturingBuildings, skill);
            manufacturingBuildings = getBuildingsWithProcessesRequiringWork(manufacturingBuildings, skill);
            manufacturingBuildings = getHighestManufacturingTechLevelBuildings(manufacturingBuildings);
            manufacturingBuildings = BuildingManager.getLeastCrowdedBuildings(manufacturingBuildings);
            manufacturingBuildings = BuildingManager.getBestRelationshipBuildings(person, manufacturingBuildings);

            if (manufacturingBuildings.size() > 0)
                result = (Building) manufacturingBuildings.get(0);
        }

        return result;
    }

    /**
     * Gets a list of manufacturing buildings needing work from a list of
     * buildings with the manufacture function.
     * @param buildingList list of buildings with the manufacture function.
     * @param skill the materials science skill level of the person.
     * @return list of manufacture buildings needing work.
     * @throws BuildingException if any buildings in building list don't have
     *         the manufacture function.
     */
    private static List<Building> getManufacturingBuildingsNeedingWork(
            List<Building> buildingList, int skill) throws BuildingException {

        List<Building> result = new ArrayList<Building>();

        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            Manufacture manufacturingFunction = (Manufacture) building.getFunction(Manufacture.NAME);
            if (manufacturingFunction.requiresManufacturingWork(skill))
                result.add(building);
        }

        return result;
    }

    /**
     * Gets a subset list of manufacturing buildings with processes requiring
     * work.
     * @param buildingList the original building list.
     * @param skill the materials science skill level of the person.
     * @return subset list of buildings with processes requiring work, or
     *         original list if none found.
     * @throws BuildingException if error determining building processes.
     */
    private static List<Building> getBuildingsWithProcessesRequiringWork(
            List<Building> buildingList, int skill) throws BuildingException {

        List<Building> result = new ArrayList<Building>();

        // Add all buildings with processes requiring work.
        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (hasProcessRequiringWork(building, skill))
                result.add(building);
        }

        // If no building with processes requiring work, return original list.
        if (result.size() == 0)
            result = buildingList;

        return result;
    }

    /**
     * Checks if manufacturing building has any processes requiring work.
     * @param manufacturingBuilding the manufacturing building.
     * @param skill the materials science skill level of the person.
     * @return true if processes requiring work.
     * @throws BuildingException if building is not manufacturing.
     */
    private static boolean hasProcessRequiringWork(Building manufacturingBuilding, int skill) throws BuildingException {

        boolean result = false;

        Manufacture manufacturingFunction = (Manufacture) manufacturingBuilding.getFunction(Manufacture.NAME);
        Iterator<ManufactureProcess> i = manufacturingFunction.getProcesses().iterator();
        while (i.hasNext()) {
            ManufactureProcess process = i.next();
            boolean workRequired = (process.getWorkTimeRemaining() > 0D);
            boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
            if (workRequired && skillRequired) result = true;
        }

        return result;
    }

    /**
     * Gets a subset list of manufacturing buildings with the highest tech level
     * from a list of buildings with the manufacture function.
     * @param buildingList list of buildings with the manufacture function.
     * @return subset list of highest tech level buildings.
     * @throws BuildingException if any buildings in building list don't have
     *         the manufacture function.
     */
    private static List<Building> getHighestManufacturingTechLevelBuildings(List<Building> buildingList) 
            throws BuildingException {

        List<Building> result = new ArrayList<Building>();

        int highestTechLevel = 0;
        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            Manufacture manufacturingFunction = (Manufacture) building.getFunction(Manufacture.NAME);
            if (manufacturingFunction.getTechLevel() > highestTechLevel)
                highestTechLevel = manufacturingFunction.getTechLevel();
        }

        Iterator<Building> j = buildingList.iterator();
        while (j.hasNext()) {
            Building building = j.next();
            Manufacture manufacturingFunction = (Manufacture) building.getFunction(Manufacture.NAME);
            if (manufacturingFunction.getTechLevel() == highestTechLevel)
                result.add(building);
        }

        return result;
    }

    /**
     * Gets the highest manufacturing process goods value for the person and the
     * manufacturing building.
     * @param person the person to perform manufacturing.
     * @param manufacturingBuilding the manufacturing building.
     * @return highest process good value.
     * @throws BuildingException if error determining process value.
     */
    private static double getHighestManufacturingProcessValue(Person person, 
            Building manufacturingBuilding) throws BuildingException {

        double highestProcessValue = 0D;

        int skillLevel = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MATERIALS_SCIENCE);

        Manufacture manufacturingFunction = (Manufacture) manufacturingBuilding.getFunction(Manufacture.NAME);
        int techLevel = manufacturingFunction.getTechLevel();

        try {
            Iterator<ManufactureProcessInfo> i = ManufactureUtil.getManufactureProcessesForTechSkillLevel(
                    techLevel, skillLevel).iterator();
            while (i.hasNext()) {
                ManufactureProcessInfo process = i.next();
                if (ManufactureUtil.canProcessBeStarted(process, manufacturingFunction) || 
                        isProcessRunning(process, manufacturingFunction)) {
                    Settlement settlement = manufacturingBuilding.getBuildingManager().getSettlement();
                    double processValue = ManufactureUtil.getManufactureProcessValue(process, settlement);
                    if (processValue > highestProcessValue)
                        highestProcessValue = processValue;
                }
            }
        } catch (Exception e) {
            throw new BuildingException("ManufactureGood.getHighestManufacturingProcessValue()", e);
        }

        return highestProcessValue;
    }

    @Override
    protected void addExperience(double time) {
        // Add experience to "Materials Science" skill
        // (1 base experience point per 100 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude"
        // attribute.
        double newPoints = time / 100D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
                NaturalAttributeManager.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(Skill.MATERIALS_SCIENCE, newPoints);
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(1);
        results.add(Skill.MATERIALS_SCIENCE);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        return manager.getEffectiveSkillLevel(Skill.MATERIALS_SCIENCE);
    }

    @Override
    protected double performMappedPhase(double time) throws Exception {
        if (getPhase() == null)
            throw new IllegalArgumentException("Task phase is null");
        if (MANUFACTURE.equals(getPhase()))
            return manufacturePhase(time);
        else
            return time;
    }

    /**
     * Perform the manufacturing phase.
     * @param time the time to perform (millisols)
     * @return remaining time after performing (millisols)
     * @throws Exception if error performing phase.
     */
    private double manufacturePhase(double time) throws Exception {

        // Check if workshop has malfunction.
        if (workshop.getBuilding().getMalfunctionManager().hasMalfunction()) {
            endTask();
            return time;
        }

        // Determine amount of effective work time based on "Materials Science"
        // skill.
        double workTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) workTime /= 2;
        else workTime += workTime * (.2D * (double) skill);

        // Apply work time to manufacturing processes.
        while ((workTime > 0D) && !isDone()) {
            ManufactureProcess process = getRunningManufactureProcess();
            if (process != null) {
                double remainingWorkTime = process.getWorkTimeRemaining();
                double providedWorkTime = workTime;
                if (providedWorkTime > remainingWorkTime)
                    providedWorkTime = remainingWorkTime;
                process.addWorkTime(providedWorkTime);
                workTime -= providedWorkTime;

                if ((process.getWorkTimeRemaining() <= 0D) && (process.getProcessTimeRemaining() <= 0D))
                    workshop.endManufacturingProcess(process, false);
            } else {
                if (!person.getSettlement().getManufactureOverride())
                    process = createNewManufactureProcess();
                if (process == null)
                    endTask();
            }
        }

        // Add experience
        addExperience(time);

        // Check for accident in workshop.
        checkForAccident(time);

        return 0D;
    }

    /**
     * Gets an available running manufacturing process.
     * @return process or null if none.
     */
    private ManufactureProcess getRunningManufactureProcess() {
        ManufactureProcess result = null;

        int skillLevel = getEffectiveSkillLevel();

        Iterator<ManufactureProcess> i = workshop.getProcesses().iterator();
        while (i.hasNext() && (result == null)) {
            ManufactureProcess process = i.next();
            if ((process.getInfo().getSkillLevelRequired() <= skillLevel) && 
                    (process.getWorkTimeRemaining() > 0D)) {
                result = process;
            }
        }

        return result;
    }

    /**
     * Checks if a process type is currently running at a manufacturing
     * building.
     * @param processInfo the process type.
     * @param manufactureBuilding the manufacturing building.
     * @return true if process is running.
     */
    private static boolean isProcessRunning(ManufactureProcessInfo processInfo, 
            Manufacture manufactureBuilding) {
        boolean result = false;

        Iterator<ManufactureProcess> i = manufactureBuilding.getProcesses().iterator();
        while (i.hasNext()) {
            ManufactureProcess process = i.next();
            if (process.getInfo().getName() == processInfo.getName())
                result = true;
        }

        return result;
    }

    /**
     * Creates a new manufacturing process if possible.
     * @return the new manufacturing process or null if none.
     * @throws Exception if error creating manufacturing process.
     */
    private ManufactureProcess createNewManufactureProcess() throws Exception {
        ManufactureProcess result = null;

        if (workshop.getTotalProcessNumber() < workshop.getConcurrentProcesses()) {

            int skillLevel = getEffectiveSkillLevel();
            int techLevel = workshop.getTechLevel();

            double highestValue = 0D;
            ManufactureProcessInfo highestValueProcess = null;
            Iterator<ManufactureProcessInfo> i = ManufactureUtil.getManufactureProcessesForTechSkillLevel(
                    techLevel, skillLevel).iterator();
            while (i.hasNext()) {
                ManufactureProcessInfo processInfo = i.next();

                if (ManufactureUtil.canProcessBeStarted(processInfo, workshop)) {
                    double processValue = ManufactureUtil.getManufactureProcessValue(processInfo, 
                            person.getSettlement());
                    if (processValue > highestValue) {
                        highestValue = processValue;
                        highestValueProcess = processInfo;
                    }
                }
            }

            if (highestValueProcess != null) {
                result = new ManufactureProcess(highestValueProcess, workshop);
                workshop.addProcess(result);
            }
        }

        return result;
    }

    /**
     * Check for accident in manufacturing building.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Materials science skill modification.
        int skill = getEffectiveSkillLevel();
        if (skill <= 3)
            chance *= (4 - skill);
        else
            chance /= (skill - 2);

        // Modify based on the workshop building's wear condition.
        chance *= workshop.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            logger.info(person.getName() + " has accident while manufacturing.");
            workshop.getBuilding().getMalfunctionManager().accident();
        }
    }
}