/**
 * Mars Simulation Project
 * CompileScientificStudyResults.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for compiling research data for a scientific study.
 */
public class CompileScientificStudyResults
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(CompileScientificStudyResults.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			 logger.getName().length());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.compileScientificStudyResults"); //$NON-NLS-1$

    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = 0D;

    /** Task phases. */
    private static final TaskPhase COMPILING_PHASE = new TaskPhase(Msg.getString(
            "Task.phase.compilingPhase")); //$NON-NLS-1$

    // Data members
    /** The scientific study to compile. */
    private ScientificStudy study;

    /**
     * Constructor.
     * @param person the person performing the task.
     * @throws Exception if error constructing the class.
     */
    public CompileScientificStudyResults(Person person) {
        // Use task constructor.
        super(NAME, person, true, false,
                STRESS_MODIFIER, true, RandomUtil.getRandomDouble(50D));

        // Determine study.
        study = determineStudy();
        if (study != null) {
            setDescription(Msg.getString("Task.description.compileScientificStudyResults.detail",
                    study.toString())); //$NON-NLS-1$

            // If person is in a settlement, try to find an administration building.
            boolean adminWalk = false;
            if (person.isInSettlement()) {
                Building adminBuilding = getAvailableAdministrationBuilding(person);
                if (adminBuilding != null) {
                    // Walk to administration building.
                    walkToActivitySpotInBuilding(adminBuilding, true);
                    adminWalk = true;
                }
            }

            if (!adminWalk) {

                if (person.isInVehicle()) {
                    // If person is in rover, walk to passenger activity spot.
                    if (person.getVehicle() instanceof Rover) {
                        walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
                    }
                }
                else {
                    // Walk to random location.
                    walkToRandomLocation(true);
                }
            }
        }
        else {
            logger.severe("Study could not be determined");
            endTask();
        }

        // Initialize phase
        addPhase(COMPILING_PHASE);
        setPhase(COMPILING_PHASE);
    }

    /**
     * Gets an available administration building that the person can use.
     * @param person the person
     * @return available administration building or null if none.
     */
    public static Building getAvailableAdministrationBuilding(Person person) {

        Building result = null;

        if (person.isInSettlement()) {
            BuildingManager manager = person.getSettlement().getBuildingManager();
            List<Building> administrationBuildings = manager.getBuildings(FunctionType.ADMINISTRATION);
            administrationBuildings = BuildingManager.getNonMalfunctioningBuildings(administrationBuildings);
            administrationBuildings = BuildingManager.getLeastCrowdedBuildings(administrationBuildings);

            if (administrationBuildings.size() > 0) {
                Map<Building, Double> administrationBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                        person, administrationBuildings);
                result = RandomUtil.getWeightedRandomObject(administrationBuildingProbs);
            }
        }

        return result;
    }

    @Override
	public FunctionType getLivingFunction() {
        return FunctionType.ADMINISTRATION;
    }

    /**
     * Determines the scientific study that will be compiled.
     * @return study or null if none available.
     */
    public ScientificStudy determineStudy() {
        ScientificStudy result = null;

        List<ScientificStudy> possibleStudies = new ArrayList<ScientificStudy>();

        // Add primary study if in paper phase.
//        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager(); ?
        ScientificStudy primaryStudy = scientificStudyManager.getOngoingPrimaryStudy(person);
        if (primaryStudy != null) {
            if (ScientificStudy.PAPER_PHASE.equals(primaryStudy.getPhase()) &&
                    !primaryStudy.isPrimaryPaperCompleted()) {
                // Primary study added twice to double chance of random selection.
                possibleStudies.add(primaryStudy);
                possibleStudies.add(primaryStudy);
            }
        }

        // Add all collaborative studies in research phase.
        Iterator<ScientificStudy> i = scientificStudyManager.getOngoingCollaborativeStudies(person).iterator();
        while (i.hasNext()) {
            ScientificStudy collabStudy = i.next();
            if (ScientificStudy.PAPER_PHASE.equals(collabStudy.getPhase()) &&
                    !collabStudy.isCollaborativePaperCompleted(person))
                possibleStudies.add(collabStudy);
        }

        // Randomly select study.
        if (possibleStudies.size() > 0) {
            int selected = RandomUtil.getRandomInt(possibleStudies.size() - 1);
            result = possibleStudies.get(selected);
        }

        return result;
    }

    /**
     * Gets the field of science that the researcher is involved with in a study.
     * @return the field of science or null if researcher is not involved with study.
     */
    public ScienceType getScience() {
        ScienceType result = null;
        if (study == null)
        	return null;
        
        if (study.getPrimaryResearcher().equals(person)) {
            result = study.getScience();
        }
        else if (study.getCollaborativeResearchers().containsKey(person.getIdentifier())) {
            result = study.getCollaborativeResearchers().get(person.getIdentifier());
        }

        return result;
    }

    @Override
    public void addExperience(double time) {
        // Add experience to relevant science skill
        // (1 base experience point per 25 millisols of research time)
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        double newPoints = time / 25D;
        int academicAptitude = person.getNaturalAttributeManager().getAttribute(
            NaturalAttributeType.ACADEMIC_APTITUDE);
        newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        SkillType scienceSkill = getScience().getSkill();
        person.getSkillManager().addExperience(scienceSkill, newPoints, time);
    }

    /**
     * Gets the effective compilation time based on the person's science skill.
     * @param time the real amount of time (millisol) for result data compilation.
     * @return the effective amount of time (millisol) for result data compilation.
     */
    public double getEffectiveCompilationTime(double time) {
        // Determine effective compilation time based on the science skill.
        double compilationTime = time;
        int scienceSkill = getEffectiveSkillLevel();
        if (scienceSkill == 0) {
            compilationTime /= 2D;
        }
        if (scienceSkill > 1) {
            compilationTime += compilationTime * (.2D * scienceSkill);
        }

        return compilationTime;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(1);
        SkillType scienceSkill = getScience().getSkill();
        results.add(scienceSkill);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
    	SkillType scienceSkill = getScience().getSkill();
        SkillManager manager = person.getSkillManager();
        return manager.getEffectiveSkillLevel(scienceSkill);
    }

    @Override
    public double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (COMPILING_PHASE.equals(getPhase())) {
            return compilingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the data results compilation phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    public double compilingPhase(double time) {

        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) {
            endTask();
        }

        // Check if data results compilation in study is completed.
        boolean isPrimary = study.getPrimaryResearcher().equals(person);

        if (isDone()) {
            return time;
        }

        // Add paper work time to study.
        double compilingTime = getEffectiveCompilationTime(time);
        if (isPrimary) {
            study.addPrimaryPaperWorkTime(compilingTime);
        }
        else {
            study.addCollaborativePaperWorkTime(person, compilingTime);
        }

        if (isPrimary) {
            if (study.isPrimaryPaperCompleted()) {
    			LogConsolidated.log(Level.INFO, 0, sourceName, "[" + person.getLocationTag().getLocale() + "] "
    					+ person.getName() + " just spent " 
    					+ Math.round(study.getPrimaryPaperWorkTimeCompleted() *10.0)/10.0
    					+ " millisols in compiling data" 
    					+ " for a primary research study in " + study.getScience().getName() 
    					+ " in " + person.getLocationTag().getImmediateLocation());	
            	endTask();
            }
        }
        else {
            if (study.isCollaborativePaperCompleted(person)) {
    			LogConsolidated.log(Level.INFO, 0, sourceName, "[" + person.getLocationTag().getLocale() + "] "
    					+ person.getName() + " just spent " 
    					+ Math.round(study.getCollaborativePaperWorkTimeCompleted(person) *10.0)/10.0
    					+ " millisols in performing lab experiments" 
    					+ " for a collaborative research study in " + study.getScience().getName() 
    					+ " in " + person.getLocationTag().getImmediateLocation());	
            	endTask();
            }
        }

        // Add experience
        addExperience(time);

        return 0D;
    }

    @Override
    public void destroy() {
        super.destroy();

        study = null;
    }
}