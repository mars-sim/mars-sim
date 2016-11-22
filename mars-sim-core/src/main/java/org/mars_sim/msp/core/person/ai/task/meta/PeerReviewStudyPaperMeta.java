/**
 * Mars Simulation Project
 * PeerReviewStudyPaperMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PeerReviewStudyPaper;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;

/**
 * Meta task for the PeerReviewStudyPaper task.
 */
public class PeerReviewStudyPaperMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.peerReviewStudyPaper"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new PeerReviewStudyPaper(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            	//|| person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

	        // Get all studies in the peer review phase.
	        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
	        Iterator<ScientificStudy> i = studyManager.getOngoingStudies().iterator();
	        while (i.hasNext()) {
	            ScientificStudy study = i.next();
	            if (ScientificStudy.PEER_REVIEW_PHASE.equals(study.getPhase())) {

	                // Check that person isn't a researcher in the study.
	                if (!person.equals(study.getPrimaryResearcher()) &&
	                        !study.getCollaborativeResearchers().keySet().contains(person)) {

	                    // If person's current job is related to study primary science,
	                    // add chance to review.
	                    Job job = person.getMind().getJob();
	                    if (job != null) {
	                        ScienceType jobScience = ScienceType.getJobScience(job);
	                        if (study.getScience().equals(jobScience)) {
	                            result += 50D * person.getParkedSettlement().getGoodsManager().getResearchFactor();;
	                        }
	                    }
	                }
	            }
	        }

	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();

	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(PeerReviewStudyPaper.class);
	        }

	        // Modify if research is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Research")) {
	            result *= 2D;
	        }

	        // 2015-06-07 Added Preference modifier
            if (result > 0)
            	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

	        if (result < 0) result = 0;
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}