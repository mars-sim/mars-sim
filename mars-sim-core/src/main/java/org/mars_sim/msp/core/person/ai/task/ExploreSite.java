/**
 * Mars Simulation Project
 * ExploreSite.java
 * @version 3.1.0 2017-03-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.SpecimenContainer;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.MineralMap;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for the EVA operation of exploring a site.
 */
public class ExploreSite
extends EVAOperation
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(ExploreSite.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.exploreSite"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase EXPLORING = new TaskPhase(Msg.getString(
            "Task.phase.exploring")); //$NON-NLS-1$

    // Static members
    private static final double AVERAGE_ROCK_SAMPLES_COLLECTED_SITE = 10D;
    public static final double AVERAGE_ROCK_SAMPLE_MASS = .5D;
    private static final double ESTIMATE_IMPROVEMENT_FACTOR = 5D;

    // Data members
    private ExploredLocation site;
    private Rover rover;

    //static AmountResource rockSamplesAR = EVA.AmountResource.rockSamplesAR;

    /**
     * Constructor.
     * @param person the person performing the task.
     * @param site the site to explore.
     * @param rover the mission rover.
     * @throws exception if error creating task.
     */
    public ExploreSite(Person person, ExploredLocation site, Rover rover) {

        // Use EVAOperation parent constructor.
        super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D);

        // Initialize data members.
        this.site = site;
        this.rover = rover;

        // Determine location for field work.
        Point2D exploreLoc = determineExploreLocation();
        setOutsideSiteLocation(exploreLoc.getX(), exploreLoc.getY());

        // Take specimen containers for rock samples.
        if (!hasSpecimenContainer()) {
            takeSpecimenContainer();

            // If specimen containers are not available, end task.
            if (!hasSpecimenContainer()) {
                logger.fine(person.getName() +
                        " not able to find specimen container to collect rock samples.");
                endTask();
            }
        }

        // Add task phase
        addPhase(EXPLORING);
    }

    /**
     * Determine location to explore.
     * @return field work X and Y location outside rover.
     */
    private Point2D determineExploreLocation() {

        Point2D newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 5) && !goodLocation; x++) {
            for (int y = 0; (y < 10) && !goodLocation; y++) {

                double distance = RandomUtil.getRandomDouble(100D) + (x * 100D) + 50D;
                double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
                double newXLoc = rover.getXLocation() - (distance * Math.sin(radianDirection));
                double newYLoc = rover.getYLocation() + (distance * Math.cos(radianDirection));
                Point2D boundedLocalPoint = new Point2D.Double(newXLoc, newYLoc);

                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(),
                        boundedLocalPoint.getY(), rover);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(),
                        person.getCoordinates());
            }
        }

        return newLocation;
    }

    /**
     * Checks if a person can explore a site.
     * @param member the member
     * @param rover the rover
     * @return true if person can explore a site.
     */
    public static boolean canExploreSite(MissionMember member, Rover rover) {

        if (member instanceof Person) {
            Person person = (Person) member;

            // Check if person can exit the rover.
            if(!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
            	return false;

            Mars mars = Simulation.instance().getMars();
            if (mars.getSurfaceFeatures().getSolarIrradiance(person.getCoordinates()) == 0D) {
                logger.fine(person.getName() + " end exploring site: night time");
                if (!mars.getSurfaceFeatures().inDarkPolarRegion(person.getCoordinates()))
                    return false;
            }

            // Check if person's medical condition will not allow task.
            if (person.getPerformanceRating() < .5D)
            	return false;
        }


        return true;
    }

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return EXPLORING;
    }

    @Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);

        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (EXPLORING.equals(getPhase())) {
            return exploringPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Perform the exploring phase of the task.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     * @throws Exception if error performing phase.
     */
    private double exploringPhase(double time) {

        // Check for an accident during the EVA operation.
        checkForAccident(time);

        // 2015-05-29 Check for radiation exposure during the EVA operation.
        if (isRadiationDetected(time)){
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Check if site duration has ended or there is reason to cut the exploring
        // phase short and return to the rover.
        if (shouldEndEVAOperation() || addTimeOnSite(time)) {
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Collect rock samples.
        collectRockSamples(time);

        // Improve mineral concentration estimates.
        improveMineralConcentrationEstimates(time);

        // TODO: Add other site exploration activities later.

        // Add experience points
        addExperience(time);

        return 0D;
    }

    /**
     * Collect rock samples if chosen.
     * @param time the amount of time available (millisols).
     * @throws Exception if error collecting rock samples.
     */
    private void collectRockSamples(double time) {
        if (hasSpecimenContainer()) {
            double numSamplesCollected = AVERAGE_ROCK_SAMPLES_COLLECTED_SITE / AVERAGE_ROCK_SAMPLE_MASS;
            double probability = (time / Exploration.EXPLORING_SITE_TIME) * (numSamplesCollected);
            if (RandomUtil.getRandomDouble(1.0D) <= probability) {
                Inventory inv = person.getInventory();
                double rockSampleMass = RandomUtil.getRandomDouble(AVERAGE_ROCK_SAMPLE_MASS * 2D);
                //AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
                double rockSampleCapacity = inv.getAmountResourceRemainingCapacity(
                		ResourceUtil.rockSamplesAR, true, false);
                if (rockSampleMass < rockSampleCapacity)
                    inv.storeAmountResource(ResourceUtil.rockSamplesAR, rockSampleMass, true);
   			 		// 2015-01-15 Add addSupplyAmount()
                	inv.addAmountSupplyAmount(ResourceUtil.rockSamplesAR, rockSampleMass);
            }
        }
    }

    /**
     * Improve the mineral concentration estimates of an explored site.
     * @param time the amount of time available (millisols).
     */
    private void improveMineralConcentrationEstimates(double time) {
        double probability = (time / Exploration.EXPLORING_SITE_TIME) * getEffectiveSkillLevel() *
                ESTIMATE_IMPROVEMENT_FACTOR;
        if (RandomUtil.getRandomDouble(1.0D) <= probability) {
            MineralMap mineralMap = Simulation.instance().getMars().getSurfaceFeatures().getMineralMap();
            Map<String, Double> estimatedMineralConcentrations = site.getEstimatedMineralConcentrations();
            Iterator<String> i = estimatedMineralConcentrations.keySet().iterator();
            while (i.hasNext()) {
                String mineralType = i.next();
                double actualConcentration = mineralMap.getMineralConcentration(mineralType, site.getLocation());
                double estimatedConcentration = estimatedMineralConcentrations.get(mineralType);
                double estimationDiff = Math.abs(actualConcentration - estimatedConcentration);
                double estimationImprovement = RandomUtil.getRandomDouble(1D * getEffectiveSkillLevel());
                if (estimationImprovement > estimationDiff) estimationImprovement = estimationDiff;
                if (estimatedConcentration < actualConcentration) estimatedConcentration += estimationImprovement;
                else estimatedConcentration -= estimationImprovement;
                estimatedMineralConcentrations.put(mineralType, estimatedConcentration);
            }

            // Add to site mineral concentration estimation improvement number.
            site.addEstimationImprovement();
            logger.fine("Explored site " + site.getLocation().getFormattedString() + " estimation improvement: " +
                    site.getNumEstimationImprovement() + " from exploring site");
        }
    }

    /**
     * Checks if the person is carrying a specimen container.
     * @return true if carrying container.
     */
    private boolean hasSpecimenContainer() {
        return person.getInventory().containsUnitClass(SpecimenContainer.class);
    }

    /**
     * Takes the least full specimen container from the rover, if any are available.
     * @throws Exception if error taking container.
     */
    private void takeSpecimenContainer() {
        Unit container = findLeastFullContainer(rover);
        if (container != null) {
            if (person.getInventory().canStoreUnit(container, false)) {
                rover.getInventory().retrieveUnit(container);
                person.getInventory().storeUnit(container);
            }
        }
    }

    /**
     * Gets the least full specimen container in the rover.
     * @param rover the rover with the inventory to look in.
     * @return specimen container or null if none.
     */
    private static SpecimenContainer findLeastFullContainer(Rover rover) {
        SpecimenContainer result = null;
        double mostCapacity = 0D;

        Iterator<Unit> i = rover.getInventory().findAllUnitsOfClass(SpecimenContainer.class).iterator();
        while (i.hasNext()) {
            SpecimenContainer container = (SpecimenContainer) i.next();
            try {
                //AmountResource rockSamples = ("rock samples");
                double remainingCapacity = container.getInventory().getAmountResourceRemainingCapacity(
                		ResourceUtil.rockSamplesAR, false, false);

                if (remainingCapacity > mostCapacity) {
                    result = container;
                    mostCapacity = remainingCapacity;
                }
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        return result;
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

        // If phase is exploring, add experience to areology skill.
        if (EXPLORING.equals(getPhase())) {
            // 1 base experience point per 10 millisols of exploration time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double areologyExperience = time / 10D;
            areologyExperience += areologyExperience * experienceAptitudeModifier;
            person.getMind().getSkillManager().addExperience(SkillType.AREOLOGY, areologyExperience);
        }
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(2);
        results.add(SkillType.EVA_OPERATIONS);
        results.add(SkillType.AREOLOGY);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int areologySkill = manager.getEffectiveSkillLevel(SkillType.AREOLOGY);
        return (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D);
    }

    @Override
    public void endTask() {

        // Load specimen container in rover.
        Inventory pInv = person.getInventory();
        if (pInv.containsUnitClass(SpecimenContainer.class)) {
            Unit container = pInv.findUnitOfClass(SpecimenContainer.class);
            pInv.retrieveUnit(container);
            rover.getInventory().storeUnit(container);
        }

        super.endTask();
    }

    @Override
    public void destroy() {
        super.destroy();

        site = null;
        rover = null;
    }
}