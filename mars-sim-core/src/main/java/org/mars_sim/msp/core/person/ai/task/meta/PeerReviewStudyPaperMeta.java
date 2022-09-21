/*
 * Mars Simulation Project
 * PeerReviewStudyPaperMeta.java
 * @date 2022-07-26
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.PeerReviewStudyPaper;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the PeerReviewStudyPaper task.
 */
public class PeerReviewStudyPaperMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.peerReviewStudyPaper"); //$NON-NLS-1$

    public PeerReviewStudyPaperMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC, TaskTrait.TEACHING);
		setPreferredJob(JobType.ACADEMICS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new PeerReviewStudyPaper(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInside()) {

            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(800, 80, 800))
            	return 0;

	        // Get all studies in the peer review phase.
            ScientificStudyManager sm = Simulation.instance().getScientificStudyManager();
	        Iterator<ScientificStudy> i = sm.getOngoingStudies().iterator();
	        while (i.hasNext()) {
	            ScientificStudy study = i.next();
	            if (ScientificStudy.PEER_REVIEW_PHASE.equals(study.getPhase())) {

	                // Check that person isn't a researcher in the study.
	                if (!person.equals(study.getPrimaryResearcher()) &&
	                        !study.getCollaborativeResearchers().contains(person)) {

	                    // If person's current job is related to study primary science,
	                    // add chance to review.
	                    JobType job = person.getMind().getJob();
	                    if (job != null) {
	                        //ScienceType jobScience = ScienceType.getJobScience(job);
	                        if (study.getScience() == ScienceType.getJobScience(job)) {
	                            result += 50D * person.getAssociatedSettlement().getGoodsManager().getResearchFactor();;
	                        }
	                    }
	                }
	            }
	        }

	        if (result == 0) return 0;

            if (person.isInVehicle()) {
    	        // Check if person is in a moving rover.
    	        if (Vehicle.inMovingRover(person)) {
    	        	result += -10D;
    	        }
    	        else
    	        	result += 10D;
            }

	        result = applyPersonModifier(result, person);
        }

        return result;
    }
}
