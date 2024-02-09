/**
 * Mars Simulation Project
 * RespondToStudyInvitationMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.RespondToStudyInvitation;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the RespondToStudyInvitation task.
 */
public class RespondToStudyInvitationMeta extends FactoryMetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.respondToStudyInvitation"); //$NON-NLS-1$

    public RespondToStudyInvitationMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST,
				RoleType.CREW_SCIENTIST);
	}


    @Override
    public Task constructInstance(Person person) {
        return new RespondToStudyInvitation(person);
    }

    /**
     * Assess a Person responding to a research invitation. Assessment is based on if an
     * invite is pending.
     * 
     * @param person Being assessed
     * @return Potential task jobs
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
            || !person.isInside()) {
        	return EMPTY_TASKLIST;
        }

        // Check if person has been invited to collaborate on any scientific studies.
        ScientificStudyManager sm = Simulation.instance().getScientificStudyManager();
        List<ScientificStudy> invitedStudies = sm.getOpenInvitationStudies(person);
        if (invitedStudies.isEmpty()) {
            return EMPTY_TASKLIST;
        }
	    
        var result = new RatingScore(invitedStudies.size() * 200D);

        Building adminBuilding = RespondToStudyInvitation.getAvailableAdministrationBuilding(person);
        assessBuildingSuitability(result, adminBuilding, person);
        assessPersonSuitability(result, person);        

        result = applyCommerceFactor(result, person.getAssociatedSettlement(), CommerceType.RESEARCH);

        return createTaskJobs(result);
    }
}
