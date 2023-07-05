/*
 * Mars Simulation Project
 * InviteStudyCollaborator.java
 * @date 2022-06-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.social.RelationshipType;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for inviting a researcher to collaborate on a scientific study.
 */
public class InviteStudyCollaborator extends Task {

	private final static class PersonScore  {
		double score;
		Person invitee;

		double getScore() {
			return score;
		}

		Person getInvitee() {
			return invitee;
		}
	}

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(InviteStudyCollaborator.class.getName());

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
    private List<Person> invitees;

    /**
     * Constructor
     * @param person the person performing the task.
     */
    public InviteStudyCollaborator(Person person) {
    	// Skill determined by Study
        super(NAME, person, false, true, STRESS_MODIFIER, null, 25D, DURATION);
        setExperienceAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);

        study = person.getStudy();
        if (study != null) {
        	addAdditionSkill(study.getScience().getSkill());

            // Determine best invitee.
        	int open = study.getMaxCollaborators() - study.getNumOpenResearchInvitations();
            invitees = determineBestInvitee(open);

            if (!invitees.isEmpty()) {

                // If person is in a settlement, try to find an administration building.
                boolean adminWalk = false;
                if (person.isInSettlement()) {
                    Building b = BuildingManager.getAvailableBuilding(study, person);
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
            }
            else {
                logger.severe(person, "No available collaborative researchers available for invitation.");
                endTask();
            }
        }
        else {
            logger.severe(person, "Does not have a primary scientific study.");
            endTask();
        }

        // Initialize phase
        addPhase(WRITING_INVITATION);
        setPhase(WRITING_INVITATION);
    }

    /**
     * Determines the best available researcher to invite for collaboration on a study.
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
            double achievement = invitee.getScientificAchievement(jobScience);
            inviteeValue += achievement;

            // Modify based on if invitee's job science is not study primary science.
            if (!jobScience.equals(study.getScience())) inviteeValue /= 2D;

            // Modify based on invitee total scientific achievement.
            double totalAchievement = invitee.getTotalScientificAchievement();
            inviteeValue += (totalAchievement / 10D);

            // Modify based on study researcher's personal opinion of invitee.
            double opinion = RelationshipUtil.getOpinionOfPerson(study.getPrimaryResearcher(), invitee);
            inviteeValue *= (opinion / 100D);

            // Modify based on current number of studies researcher is currently collaborating on.
            int numCollaborativeStudies = invitee.getCollabStudies().size();
            inviteeValue /= (numCollaborativeStudies + 1D);

            // Modify based on if researcher and primary researcher are at same settlement.
            Settlement researcherSettlement = person.getAssociatedSettlement();
            Settlement primarySettlement = study.getPrimaryResearcher().getAssociatedSettlement();
            if ((researcherSettlement != null) && researcherSettlement.equals(primarySettlement))
                inviteeValue *= 2D;

            PersonScore score = new PersonScore();
            score.invitee = invitee;
            score.score = inviteeValue;
			potentials.add(score);
        }

        return potentials.stream()
        		.sorted(Comparator.comparingDouble(PersonScore::getScore).reversed())
        		.limit(required)
        		.map(PersonScore::getInvitee)
        		.collect(Collectors.toList());
    }

    /**
     * Performs the writing invitation phase.
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

        // If duration, send invitation.
        if (getDuration() <= (getTimeCompleted() + time)) {

        	for (Person invitee : invitees) {
	            // Add invitation to study.
	            study.addInvitedResearcher(invitee);

	            RelationshipUtil.changeOpinion(person, invitee, RelationshipType.REMOTE_COMMUNICATION, RandomUtil.getRandomDouble(5));
	            
	            logger.log(worker, Level.FINE, 0, "Inviting " + invitee.getName() +
	                    " to collaborate in " + study.getName() + ".");
	        }
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
