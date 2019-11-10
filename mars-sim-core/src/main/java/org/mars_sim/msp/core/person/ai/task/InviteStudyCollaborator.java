/**
 * Mars Simulation Project
 * InviteStudyCollaborator.java
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
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for inviting a researcher to collaborate on a scientific study.
 */
public class InviteStudyCollaborator
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(InviteStudyCollaborator.class.getName());
    
	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			 logger.getName().length());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.inviteStudyCollaborator"); //$NON-NLS-1$

    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = 0D;

    /** Duration (millisols) of task. */
    private static final double DURATION = 25D;

    /** Task phases. */
    private static final TaskPhase WRITING_INVITATION = new TaskPhase(Msg.getString(
            "Task.phase.writingInvitation")); //$NON-NLS-1$

    /** The scientific study. */
    private ScientificStudy study;
    /** The collaborative researcher to invite. */
    private Person invitee;

    /**
     * Constructor
     * @param person the person performing the task.
     */
    public InviteStudyCollaborator(Person person) {
        super(NAME, person, false, true, STRESS_MODIFIER, true, DURATION);

        study = scientificStudyManager.getOngoingPrimaryStudy(person);
        if (study != null) {

            // Determine best invitee.
            invitee = determineBestInvitee();

            if (invitee != null) {

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
                logger.severe("No available collaborative researchers available for invitation.");
                endTask();
            }
        }
        else {
            logger.severe(person.getName() + " does not have a primary scientific study.");
            endTask();
        }

        // Initialize phase
        addPhase(WRITING_INVITATION);
        setPhase(WRITING_INVITATION);
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
     * Determines the best available researcher to invite for collaboration on a study.
     * @return best collaborative invitee or null if none.
     */
    private Person determineBestInvitee() {
        Person bestInvitee = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        Iterator<Person> i = ScientificStudyUtil.getAvailableCollaboratorsForInvite(study).iterator();
        while (i.hasNext()) {
            Person invitee = i.next();
            double inviteeValue = 0D;

            ScienceType jobScience = ScienceType.getJobScience(invitee.getMind().getJob());

            // Modify based on invitee level in job science.
            SkillType skill = jobScience.getSkill();
            int skillLevel = invitee.getSkillManager().getEffectiveSkillLevel(skill);
            inviteeValue += skillLevel;

            // Modify based on invitee achievement in job science.
            double achievement = invitee.getScientificAchievement(jobScience);
            inviteeValue += achievement;

            // Modify based on if invitee's job science is not study primary science.
            if (!jobScience.equals(study.getScience())) inviteeValue /= 2D;

            // Modify based on invitee total scientific achievement.
            double totalAchievement = invitee.getTotalScientificAchievement();
            inviteeValue += (totalAchievement / 10D);

            // Modify based on study researcher's personal opinion of invitee.
//            RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
            double opinion = relationshipManager.getOpinionOfPerson(study.getPrimaryResearcher(), invitee);
            inviteeValue *= (opinion / 100D);

            // Modify based on current number of studies researcher is currently collaborating on.
//            ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
            int numCollaborativeStudies = scientificStudyManager.getOngoingCollaborativeStudies(invitee).size();
            inviteeValue /= (numCollaborativeStudies + 1D);

            // Modify based on if researcher and primary researcher are at same settlement.
            Settlement researcherSettlement = person.getAssociatedSettlement();
            Settlement primarySettlement = study.getPrimaryResearcher().getAssociatedSettlement();
            if ((researcherSettlement != null) && researcherSettlement.equals(primarySettlement))
                inviteeValue *= 2D;

            if (inviteeValue > bestValue) {
                bestInvitee = invitee;
                bestValue = inviteeValue;
            }
        }

        return bestInvitee;
    }

    /**
     * Performs the writing invitation phase.
     * @param time the time (millisols) to perform the phase.
     * @return the remaining time (millisols) after performing the phase.
     */
    private double writingInvitationPhase(double time) {

        if (isDone()) {
            return time;
        }

        // If duration, send invitation.
        if (getDuration() <= (getTimeCompleted() + time)) {

            // Add invitation to study.
            study.addInvitedResearcher(invitee);

            // Check if existing relationship between primary researcher and invitee.
//            RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
            if (!relationshipManager.hasRelationship(person, invitee)) {
                // Add new communication meeting relationship.
                relationshipManager.addRelationship(person, invitee, Relationship.COMMUNICATION_MEETING);
            }

            // Add 10 points to invitee's opinion of primary researcher due to invitation.
            Relationship relationship = relationshipManager.getRelationship(invitee, person);
            double currentOpinion = relationship.getPersonOpinion(invitee);
            relationship.setPersonOpinion(invitee, currentOpinion + 10D);
            LogConsolidated.log(Level.INFO, 0, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " + person
					+ " was inviting " + invitee.getName() +
                    " to collaborate in " + study.toString());
        }

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

        SkillType skillName = study.getScience().getSkill();
        person.getSkillManager().addExperience(skillName, newPoints, time);
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> skills = new ArrayList<SkillType>(1);
        if (study != null) 
        	skills.add(study.getScience().getSkill());
        return skills;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getSkillManager();
        SkillType skillName = study.getScience().getSkill();
        return manager.getEffectiveSkillLevel(skillName);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (WRITING_INVITATION.equals(getPhase())) {
            return writingInvitationPhase(time);
        }
        else {
            return time;
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        study = null;
        invitee = null;
    }
}