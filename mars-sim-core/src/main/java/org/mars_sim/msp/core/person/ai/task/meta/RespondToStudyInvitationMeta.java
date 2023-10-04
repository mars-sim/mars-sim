/**
 * Mars Simulation Project
 * RespondToStudyInvitationMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.RespondToStudyInvitation;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the RespondToStudyInvitation task.
 */
public class RespondToStudyInvitationMeta extends FactoryMetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.respondToStudyInvitation"); //$NON-NLS-1$

    public RespondToStudyInvitationMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.ACADEMICS);
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
     * @return Assessment
     */
    @Override
    protected RatingScore getRating(Person person) {
        
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
            || !person.isInside()) {
        	return RatingScore.ZERO_RATING;
        }

        // Check if person has been invited to collaborate on any scientific studies.
        ScientificStudyManager sm = Simulation.instance().getScientificStudyManager();
        List<ScientificStudy> invitedStudies = sm.getOpenInvitationStudies(person);
        if (invitedStudies.isEmpty()) {
            return RatingScore.ZERO_RATING;
        }
	    
        var result = new RatingScore(invitedStudies.size() * 200D);

        Building adminBuilding = RespondToStudyInvitation.getAvailableAdministrationBuilding(person);
        assessBuildingSuitability(result, adminBuilding, person);
        assessPersonSuitability(result, person);        

        result.addModifier(GOODS_MODIFIER,
                    person.getAssociatedSettlement().getGoodsManager().getResearchFactor());

        return result;
    }
}
