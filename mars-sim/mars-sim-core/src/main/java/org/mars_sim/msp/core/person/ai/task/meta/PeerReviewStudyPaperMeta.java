/**
 * Mars Simulation Project
 * PeerReviewStudyPaperMeta.java
 * @version 3.07 2014-08-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PeerReviewStudyPaper;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;

/**
 * Meta task for the PeerReviewStudyPaper task.
 */
public class PeerReviewStudyPaperMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Peer Review Compiled Study Paper";
    
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
                            result += 50D;
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
        
        return result;
    }
}