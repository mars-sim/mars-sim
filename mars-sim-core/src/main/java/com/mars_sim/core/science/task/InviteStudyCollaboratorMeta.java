/**
 * Mars Simulation Project
 * InviteStudyCollaboratorMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyUtil;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the InviteStudyCollaborator task.
 */
public class InviteStudyCollaboratorMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.inviteStudyCollaborator"); //$NON-NLS-1$
    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(InviteStudyCollaboratorMeta.class.getName());
    
    public InviteStudyCollaboratorMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.LEADERSHIP);
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST,
				RoleType.CREW_SCIENTIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return new InviteStudyCollaborator(person);
    }

    /**
     * Assess suitability of a Person to raise invites to a study. This is based on whether
     * the person is leading a study and it's phase.
     * @param person Being assessed
     * @return List of potential tasks
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        
        if (!person.isInside()
            || !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
            return EMPTY_TASKLIST;
        }

        // Check if study is in invitation phase.
        ScientificStudy study = person.getResearchStudy().getStudy();
        if ((study == null)
                || (study.getPhase() != StudyStatus.INVITATION_PHASE)) {
            return EMPTY_TASKLIST;
        }

        // Check that there isn't a full set of open invitations already sent out.
        int collabNum = study.getCollaborativeResearchers().size();
        int openInvites = study.getNumOpenResearchInvitations();
        if ((openInvites + collabNum) >= study.getMaxCollaborators()) {
            return EMPTY_TASKLIST;
        }
        if (ScientificStudyUtil.getAvailableCollaboratorsForInvite(study).isEmpty()) {
            logger.warning(person, 30_000L, "Can not find anyone to invite for " + study.getName());
            return EMPTY_TASKLIST;
        }

        // Once a proposal is finished get the invites out quickly
        var result = new RatingScore(100D);
		
        // In a moving vehicle?
		result = assessMoving(result, person);
        
        // Crowding modifier
        Building adminBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.ADMINISTRATION);
        result = assessBuildingSuitability(result, adminBuilding, person);

        // Increase probability if person's current job is related to study's science.
        JobType job = person.getMind().getJob();
        ScienceType science = study.getScience();
        if (science == ScienceType.getJobScience(job)) {
            result.addModifier(SCIENCE_MODIFIER, 2D);
        }
        result = applyCommerceFactor(result, person.getAssociatedSettlement(), CommerceType.RESEARCH);
        result = assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }
}
