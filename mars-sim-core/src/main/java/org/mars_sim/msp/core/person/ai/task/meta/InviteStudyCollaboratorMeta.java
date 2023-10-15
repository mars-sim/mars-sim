/**
 * Mars Simulation Project
 * InviteStudyCollaboratorMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

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
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.LEADERSHIP);
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST);
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
        ScientificStudy study = person.getStudy();
        if ((study == null)
                || !study.getPhase().equals(ScientificStudy.INVITATION_PHASE)) {
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
            result.addModifier("science", 2D);
        }
        result.addModifier(GOODS_MODIFIER, person.getAssociatedSettlement().getGoodsManager().getResearchFactor());

        result = assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }
}
