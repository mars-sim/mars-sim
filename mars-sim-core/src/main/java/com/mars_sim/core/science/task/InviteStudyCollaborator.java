/*
 * Mars Simulation Project
 * InviteStudyCollaborator.java
 * @date 2022-06-11
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.social.RelationshipType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * A task for inviting a researcher to collaborate on a scientific study.
 */
public class InviteStudyCollaborator extends Task {

	private record PersonScore(double score, Person invitee)  {}

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(InviteStudyCollaborator.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.inviteStudyCollaborator"); //$NON-NLS-1$

    /** Duration (millisols) of task. */
    private static final double DURATION = 25D;

    /** Task phases. */
    private static final TaskPhase WRITING_INVITATION = new TaskPhase(Msg.getString(
            "Task.phase.writingInvitation")); //$NON-NLS-1$

    /** The scientific study. */
    private ScientificStudy study;
    /** The collaborative researcher to invite. */
    private List<Person> invitees;

    private static final ExperienceImpact IMPACT = new ExperienceImpact(25D,
                                        NaturalAttributeType.ACADEMIC_APTITUDE, false,
                                        0D, SkillType.ORGANISATION);
    
    /**
     * Constructor.
     * 
     * @param person the person performing the task.
     */
    public InviteStudyCollaborator(Person person) {
    	// Skill determined by Study
        super(NAME, person, true, IMPACT, DURATION);

        study = person.getResearchStudy().getStudy();
        if (study == null) {
            logger.severe(person, "Does not have a primary scientific study.");
            endTask();
            return;
        }

        // Determine best invitee.
        int open = study.getMaxCollaborators() - study.getNumOpenResearchInvitations();
        invitees = determineBestInvitee(open);
        if (invitees.isEmpty()) {
            logger.severe(person, "No available collaborative researchers available for invitation.");
            endTask();
            return;
        }

        // If person is in a settlement, try to find an administration building.
        boolean adminWalk = false;
        if (person.isInSettlement()) {
            Building b = BuildingManager.getAvailableBuilding(study.getScience(), person);
            if (b != null) {
                // Walk to that building.
                walkToResearchSpotInBuilding(b, false);
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

        // Initialize phase
        setPhase(WRITING_INVITATION);
    }

    /**
     * Determines the best available researcher to invite for collaboration on a study.
     * 
     * @return best collaborative invitee or null if none.
     */
    private List<Person> determineBestInvitee(int required) {

        List<PersonScore> potentials = new ArrayList<>();

        for(Person invitee : ScientificStudyUtil.getAvailableCollaboratorsForInvite(study)) {
            double inviteeValue = 0D;

            ScienceType jobScience = ScienceType.getJobScience(invitee.getMind().getJob());

            // Modify based on invitee level in job science.
            SkillType skill = jobScience.getSkill();
            int skillLevel = invitee.getSkillManager().getEffectiveSkillLevel(skill);
            inviteeValue += skillLevel;

            // Modify based on invitee achievement in job science.
            double achievement = invitee.getResearchStudy().getScientificAchievement(jobScience);
            inviteeValue += achievement;

            // Modify based on if invitee's job science is not study primary science.
            if (!jobScience.equals(study.getScience())) inviteeValue /= 2D;

            // Modify based on invitee total scientific achievement.
            double totalAchievement = invitee.getResearchStudy().getTotalScientificAchievement();
            inviteeValue += (totalAchievement / 10D);

            // Modify based on study researcher's personal opinion of invitee.
            double opinion = RelationshipUtil.getOpinionOfPerson(study.getPrimaryResearcher(), invitee);
            inviteeValue *= (opinion / 100D);

            // Modify based on current number of studies researcher is currently collaborating on.
            int numCollaborativeStudies = invitee.getResearchStudy().getCollabStudies().size();
            inviteeValue /= (numCollaborativeStudies + 1D);

            // Modify based on if researcher and primary researcher are at same settlement.
            Settlement researcherSettlement = person.getAssociatedSettlement();
            Settlement primarySettlement = study.getPrimaryResearcher().getAssociatedSettlement();
            if ((researcherSettlement != null) && researcherSettlement.equals(primarySettlement))
                inviteeValue *= 2D;

            PersonScore score = new PersonScore(inviteeValue, invitee);
			potentials.add(score);
        }

        return potentials.stream()
        		.sorted(Comparator.comparingDouble(PersonScore::score).reversed())
        		.limit(required)
        		.map(PersonScore::invitee)
        		.collect(Collectors.toList());
    }

    /**
     * Performs the writing invitation phase.
     * 
     * @param time the time (millisols) to perform the phase.
     * @return the remaining time (millisols) after performing the phase.
     */
    private double writingInvitationPhase(double time) {

		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.log(person, Level.FINE, 10_000, "Ended inviting study collaborator. Not feeling well.");
			endTask();
		}

        if (isDone()) {
			endTask();
            return time;
        }

        // If invitees left then invite
        if (!invitees.isEmpty()) {
            // Pop next off the queue and add invitation to study.
        	Person invitee = invitees.remove(0);
            study.addInvitedResearcher(invitee);

            RelationshipUtil.changeOpinion(person, invitee, RelationshipType.REMOTE_COMMUNICATION, RandomUtil.getRandomDouble(5));
            
            logger.log(worker, Level.FINE, 0, "Inviting " + invitee.getName() +
                    " to collaborate in " + study.getName() + ".");
        }
        else {
            endTask();
        }

        return 0D;
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
}
