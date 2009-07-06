/**
 * Mars Simulation Project
 * ScientificStudyManager.java
 * @version 2.87 2009-07-05
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.science;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.time.MarsClock;

/**
 * A class that keeps track of all scientific studies in the simulation.
 */
public class ScientificStudyManager implements Serializable {

    // Data members
    private List<ScientificStudy> studies;
    
    /**
     * Constructor.
     */
    public ScientificStudyManager() {
        studies = new ArrayList<ScientificStudy>();
    }
    
    /**
     * Creates a new scientific study.
     * @param researcher the primary researcher.
     * @param science the primary field of science.
     * @param difficultyLevel the difficulty level of the study.
     * @return the created study.
     */
    public ScientificStudy createScientificStudy(Person researcher, Science science, 
            int difficultyLevel) {
        if (researcher == null) throw new IllegalArgumentException("Researcher cannot be null");
        if (science == null) throw new IllegalArgumentException("Science cannot be null");
        if (difficultyLevel < 0) throw new IllegalArgumentException("difficultyLevel must be positive value");
        
        ScientificStudy study = new ScientificStudy(researcher, science, difficultyLevel);
        studies.add(study);
        return study;
    }
    
    /**
     * Gets all ongoing scientific studies.
     * @return list of studies.
     */
    public List<ScientificStudy> getOngoingStudies() {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (!study.isCompleted()) result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all completed scientific studies, regardless of completion state.
     * @return list of studies.
     */
    public List<ScientificStudy> getCompletedStudies() {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (study.isCompleted()) result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all successfully completed scientific studies.
     * @return list of studies.
     */
    public List<ScientificStudy> getSuccessfulStudies() {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (study.isCompleted() && 
                    study.getCompletionState().equals(ScientificStudy.SUCCESSFUL_COMPLETION)) 
                result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all failed completed scientific studies.
     * @return list of studies.
     */
    public List<ScientificStudy> getFailedStudies() {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (study.isCompleted() && 
                    study.getCompletionState().equals(ScientificStudy.FAILED_COMPLETION)) 
                result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all cancelled scientific studies.
     * @return list of studies.
     */
    public List<ScientificStudy> getCancelledStudies() {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (study.isCompleted() && 
                    study.getCompletionState().equals(ScientificStudy.CANCELLED)) 
                result.add(study);
        }
        return result;
    }
    
    /**
     * Gets the researcher's ongoing primary research scientific study, if any.
     * @param researcher the primary researcher.
     * @return primary research scientific study or null if none.
     */
    public ScientificStudy getOngoingPrimaryStudy(Person researcher) {
        ScientificStudy result = null;
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (!study.isCompleted() && (study.getPrimaryResearcher().equals(researcher)))
                result = study;
        }
        return result;
    }
    
    /**
     * Gets all completed scientific studies where researcher was the primary researcher.
     * @param researcher the primary researcher.
     * @return list of studies.
     */
    public List<ScientificStudy> getCompletedPrimaryStudies(Person researcher) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (study.isCompleted() && (study.getPrimaryResearcher().equals(researcher)))
                result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all ongoing scientific studies where researcher is a collaborative researcher.
     * @param researcher the collaborative researcher.
     * @return list of studies.
     */
    public List<ScientificStudy> getOngoingCollaborativeStudies(Person researcher) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (!study.isCompleted() && (study.getCollaborativeResearchers().containsKey(researcher)))
                result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all completed scientific studies where researcher was a collaborative researcher.
     * @param researcher the collaborative researcher.
     * @return list of studies.
     */
    public List<ScientificStudy> getCompletedCollaborativeStudies(Person researcher) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (study.isCompleted() && (study.getCollaborativeResearchers().containsKey(researcher)))
                result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all ongoing scientific studies at a primary research settlement.
     * @param settlement the primary research settlement.
     * @return list of studies.
     */
    public List<ScientificStudy> getOngoingPrimaryStudies(Settlement settlement) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (!study.isCompleted() && settlement.equals(study.getPrimarySettlement()))
                result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all completed scientific studies at a primary research settlement.
     * @param settlement the primary research settlement.
     * @return list of studies.
     */
    public List<ScientificStudy> getCompletedPrimaryStudies(Settlement settlement) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (study.isCompleted() && settlement.equals(study.getPrimarySettlement()))
                result.add(study);
        }
        return result;
    }
    
    /**
     * Update all of the studies.
     */
    public void updateStudies() {
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (!study.isCompleted()) {
                
                if (study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
                    // Check if proposal work time is completed, then move to invitation phase.
                    if (study.getProposalWorkTimeCompleted() >= 
                            study.getTotalProposalWorkTimeRequired()) {
                        study.setPhase(ScientificStudy.INVITATION_PHASE);
                        continue;
                    }
                }
                else if (study.getPhase().equals(ScientificStudy.INVITATION_PHASE)) {
                    boolean phaseEnded = false;
                    if (study.getCollaborativeResearchers().size() < ScientificStudy.MAX_NUM_COLLABORATORS) {
                        if (ScientificStudyUtil.getAvailableCollaboratorsForInvite(study).size() == 0)
                            phaseEnded = true;
                    }
                    else phaseEnded = true;
                    
                    if (phaseEnded) {
                        study.setPhase(ScientificStudy.RESEARCH_PHASE);
                        continue;
                    }
                }
                else if (study.getPhase().equals(ScientificStudy.RESEARCH_PHASE)) {
                    
                    if (study.isAllResearchCompleted()) {
                        study.setPhase(ScientificStudy.PAPER_PHASE);
                        continue;
                    }
                    else {
                        MarsClock currentDate = (MarsClock) Simulation.instance().getMasterClock().
                                getMarsClock().clone();
                        
                        // Check primary researcher downtime.
                        MarsClock lastPrimaryWork = study.getLastPrimaryResearchWorkTime();
                        if ((lastPrimaryWork != null) && MarsClock.getTimeDiff(lastPrimaryWork, currentDate) > 
                                ScientificStudy.PRIMARY_WORK_DOWNTIME_ALLOWED) {
                            study.setCompleted(ScientificStudy.CANCELLED);
                            continue;
                        }
                                
                        // Check each collaborator for downtime.
                        Iterator<Person> j = study.getCollaborativeResearchers().keySet().iterator();
                        while (j.hasNext()) {
                            Person researcher = j.next();
                            MarsClock lastCollaborativeWork = study.getLastCollaborativeResearchWorkTime(researcher);
                            if ((lastCollaborativeWork != null) && MarsClock.getTimeDiff(lastCollaborativeWork, 
                                    currentDate) > ScientificStudy.COLLABORATIVE_WORK_DOWNTIME_ALLOWED) {
                                study.removeCollaborativeResearcher(researcher);
                            }
                        }
                    }
                }
                else if (study.getPhase().equals(ScientificStudy.PAPER_PHASE)) {
                    
                    if (study.isAllPaperWritingCompleted()) {
                        study.setPhase(ScientificStudy.PEER_REVIEW_PHASE);
                        continue;
                    }
                }
                else if (study.getPhase().equals(ScientificStudy.PEER_REVIEW_PHASE)) {
                    
                    if (study.isPeerReviewTimeFinished()) {
                        // Determine results of peer review.
                        if (ScientificStudyUtil.determinePeerReviewResults(study)) { 
                            study.setCompleted(ScientificStudy.SUCCESSFUL_COMPLETION);
                            
                            // Provide scientific achievement to primary and collaborative researchers.
                            ScientificStudyUtil.provideCompletionAchievements(study);
                        }
                        else {
                            study.setCompleted(ScientificStudy.FAILED_COMPLETION);
                        }
                    }
                }
            }
        }
    }
}