/**
 * Mars Simulation Project
 * ProposeScientificStudy.java
 * @version 3.1.0 2017-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for proposing a new scientific study.
 */
public class ProposeScientificStudy
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ProposeScientificStudy.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.proposeScientificStudy"); //$NON-NLS-1$
	
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;

	/** Task phases. */
    private static final TaskPhase PROPOSAL_PHASE = new TaskPhase(Msg.getString(
            "Task.phase.proposalPhase")); //$NON-NLS-1$

	/** The scientific study to propose. */
	private ScientificStudy study;

    /**
     * Constructor.
     * @param person the person performing the task.
     */
    public ProposeScientificStudy(Person person) {
        super(NAME, person, false, true, STRESS_MODIFIER, 
                true, 10D + RandomUtil.getRandomDouble(50D));
        
        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        study = manager.getOngoingPrimaryStudy(person);
        if (study == null) {
            
            // Create new scientific study.
            Job job = person.getMind().getJob();
            ScienceType science = ScienceType.getJobScience(job);
            if (science != null) {
                SkillType skill = science.getSkill();
                int level = person.getMind().getSkillManager().getSkillLevel(skill);
                study = manager.createScientificStudy(person, science, level);
            }
            else {
                logger.severe("Person's job: " + job.getName(person.getGender()) + " not scientist.");
                endTask();
            }
        }
        
        if (study != null) {
            setDescription(Msg.getString("Task.description.proposeScientificStudy.detail", 
                    study.getScience().getName())); //$NON-NLS-1$
            
            // If person is in a settlement, try to find an administration building.
            boolean adminWalk = false;
            if (person.isInSettlement()) {     
                Building adminBuilding = getAvailableAdministrationBuilding(person);
                if (adminBuilding != null) {
                    // Walk to administration building.
                    walkToActivitySpotInBuilding(adminBuilding, false);
                    adminWalk = true;
                }
            }
            
            if (!adminWalk) {
                
                if (person.isInVehicle()) {
                    // If person is in rover, walk to passenger activity spot.
                    if (person.getVehicle() instanceof Rover) {
                        walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), false);
                    }
                }
                else {
                    // Walk to random location.
                    walkToRandomLocation(true);
                }
            }
        }
        else {
            endTask();
        }
        
        // Initialize phase
        addPhase(PROPOSAL_PHASE);
        setPhase(PROPOSAL_PHASE);
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
    protected FunctionType getLivingFunction() {
        return FunctionType.ADMINISTRATION;
    }
    
    /**
     * Performs the writing study proposal phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double proposingPhase(double time) {
        
        if (!study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
            endTask();
        }
        
        if (isDone()) {
            return time;
        }
        
        // Determine amount of effective work time based on science skill.
        double workTime = time;
        int scienceSkill = getEffectiveSkillLevel();
        if (scienceSkill == 0) {
            workTime /= 2;
        }
        else {
            workTime += workTime * (.2D * (double) scienceSkill);
        }
        
        study.addProposalWorkTime(workTime);
        
        // Add experience
        addExperience(time);
        
        return 0D;
    }
    
    @Override
    protected void addExperience(double time) {
        // Add experience to relevant science skill
        // 1 base experience point per 25 millisols of proposal writing time.
        double newPoints = time / 25D;
        
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        int academicAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
        newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        
        person.getMind().getSkillManager().addExperience(study.getScience().getSkill(), newPoints);
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> skills = new ArrayList<SkillType>(1);
        skills.add(study.getScience().getSkill());
        return skills;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        return manager.getEffectiveSkillLevel(study.getScience().getSkill());
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (PROPOSAL_PHASE.equals(getPhase())) {
            return proposingPhase(time);
        }
        else {
            return time;
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        study = null;
    }
}