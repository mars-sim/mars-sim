/**
 * Mars Simulation Project
 * ScientificStudyManager.java
 * @version 3.1.0 2017-09-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.science;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * A class that keeps track of all scientific studies in the simulation.
 */
public class ScientificStudyManager //extends Thread
implements Serializable {

    /** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ScientificStudyManager.class.getName());
    
    // Data members
    private List<ScientificStudy> studies;
    
	private static Simulation sim = Simulation.instance();
	private static MarsClock marsClock = sim.getMasterClock().getMarsClock();
	private static UnitManager unitManager = sim.getUnitManager();
	
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
    public ScientificStudy createScientificStudy(Person researcher, ScienceType science, 
            int difficultyLevel) {
        if (researcher == null) throw new IllegalArgumentException("Researcher cannot be null");
        if (science == null) throw new IllegalArgumentException("Science cannot be null");
        if (difficultyLevel < 0) throw new IllegalArgumentException("difficultyLevel must be positive value");
        
        ScientificStudy study = new ScientificStudy(researcher, science, difficultyLevel);
        studies.add(study);
        
        logger.fine(researcher.getName() + " begins writing proposal for new " + study.toString());
        
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
     * Gets all canceled scientific studies.
     * @return list of studies.
     */
    public List<ScientificStudy> getCanceledStudies() {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (study.isCompleted() && 
                    study.getCompletionState().equals(ScientificStudy.CANCELED)) 
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
            if (!study.isCompleted() && (study.getCollaborativeResearchers().containsKey(researcher.getIdentifier())))
                result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all ongoing scientific studies where researcher was a collaborative researcher in a particular settlement.
     * @param settlement
     * @return list of studies.
     */
    public List<ScientificStudy> getOngoingCollaborativeStudies(Settlement settlement) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        
		List<Person> pList = new ArrayList<>(settlement.getAllAssociatedPeople());

		for (Person p : pList) {
	        Iterator<ScientificStudy> i = studies.iterator();
	        while (i.hasNext()) {
	            ScientificStudy study = i.next();
	            if (!study.isCompleted() && (study.getCollaborativeResearchers().containsKey(p.getIdentifier())))
	                result.add(study);
	        }
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
            if (study.isCompleted() && (study.getCollaborativeResearchers().containsKey(researcher.getIdentifier())))
                result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all completed scientific studies where researcher was a collaborative researcher in a particular settlement.
     * @param settlement
     * @return list of studies.
     */
    public List<ScientificStudy> getCompletedCollaborativeStudies(Settlement settlement) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        
		List<Person> pList = new ArrayList<>(settlement.getAllAssociatedPeople());

		for (Person p : pList) {
	        Iterator<ScientificStudy> i = studies.iterator();
	        while (i.hasNext()) {
	            ScientificStudy study = i.next();
	            if (study.isCompleted() && (study.getCollaborativeResearchers().containsKey(p.getIdentifier())))
	                result.add(study);
	        }
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
     * Gets all failed scientific studies at a primary research settlement.
     * @param settlement the primary research settlement.
     * @return list of studies.
     */
    public List<ScientificStudy> getAllFailedStudies(Settlement settlement) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (study.isCompleted() 
            		&& study.getCompletionState().equals(ScientificStudy.FAILED_COMPLETION) 
            		&& settlement.equals(study.getPrimarySettlement()))
                result.add(study);
        }
        return result;
    }
    
    /**
     * Gets all studies that have open invitations for collaboration for a researcher.
     * @param collaborativeResearcher the collaborative researcher.
     * @return list of studies.
     */
    public List<ScientificStudy> getOpenInvitationStudies(Person collaborativeResearcher) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        Iterator<ScientificStudy> i = studies.iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (!study.isCompleted() && study.getPhase().equals(ScientificStudy.INVITATION_PHASE)) {
                if (study.hasResearcherBeenInvited(collaborativeResearcher)) {
                    if (!study.hasInvitedResearcherResponded(collaborativeResearcher)) 
                        result.add(study);
                }
            }
        }
        return result;
    }
    
    /**
     * Gets a list of all studies a researcher is involved with.
     * @param researcher the researcher.
     * @return list of scientific studies.
     */
    public List<ScientificStudy> getAllStudies(Person researcher) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        
        // Add ongoing primary study.
        ScientificStudy primaryStudy = getOngoingPrimaryStudy(researcher);
        if (primaryStudy != null) result.add(primaryStudy);
        
        // Add any ongoing collaborative studies.
        List<ScientificStudy> collaborativeStudies = getOngoingCollaborativeStudies(researcher);
        result.addAll(collaborativeStudies);
        
        // Add completed primary studies.
        List<ScientificStudy> completedPrimaryStudies = getCompletedPrimaryStudies(researcher);
        result.addAll(completedPrimaryStudies);
        
        // Add completed collaborative studies.
        List<ScientificStudy> completedCollaborativeStudies = getCompletedCollaborativeStudies(researcher);
        result.addAll(completedCollaborativeStudies);
        
        return result;
    }
    
    /**
     * Gets a list of all studies a settlement is primary for.
     * @param settlement the settlement.
     * @return list of scientific studies.
     */
    public List<ScientificStudy> getAllStudies(Settlement settlement) {
        List<ScientificStudy> result = new ArrayList<ScientificStudy>();
        
        // Add any ongoing primary studies.
        List<ScientificStudy> primaryStudies = getOngoingPrimaryStudies(settlement);
        result.addAll(primaryStudies);
        
        // Add any completed primary studies.
        List<ScientificStudy> completedPrimaryStudies = getCompletedPrimaryStudies(settlement);
        result.addAll(completedPrimaryStudies);
        
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
                
                // Check if primary researcher has died.
                if (isPrimaryResearcherDead(study)) {
                    study.setCompleted(ScientificStudy.CANCELED);
                    logger.fine(study.toString() + " canceled due to primary researcher death.");
                    continue;
                }
                
                // Check if collaborators have died.
                Iterator<Integer> j = study.getCollaborativeResearchers().keySet().iterator();
                while (j.hasNext()) {
                	int id = j.next();
                    Person collaborator = unitManager.getPersonByID(id);
                    if (collaborator.getPhysicalCondition().isDead()) {
                        study.removeCollaborativeResearcher(collaborator);
                        logger.fine(collaborator.getName() + " removed as collaborator in " + study.toString() + 
                                " due to death.");
                    }
                }
                
                if (study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
                    // Check if proposal work time is completed, then move to invitation phase.
                    if (study.getProposalWorkTimeCompleted() >= 
                            study.getTotalProposalWorkTimeRequired()) {
                        logger.fine(study.getPrimaryResearcher().getName() + " finishes writing proposal for " 
                                + study.toString() + " and is starting to invite collaborative researchers");
                        
                        study.setPhase(ScientificStudy.INVITATION_PHASE);
                        continue;
                    }
                }
                else if (study.getPhase().equals(ScientificStudy.INVITATION_PHASE)) {
                    // Clean out any dead research invitees.
                    study.cleanResearchInvitations();
                    
                    boolean phaseEnded = false;
                    if (study.getCollaborativeResearchers().size() < ScientificStudy.MAX_NUM_COLLABORATORS) {
                        int availableInvitees = ScientificStudyUtil.getAvailableCollaboratorsForInvite(study).size();
                        int openResearchInvitations = study.getNumOpenResearchInvitations();
                        if ((availableInvitees + openResearchInvitations) == 0)
                            phaseEnded = true;
                    }
                    else phaseEnded = true;
                    
                    if (phaseEnded) {
                        logger.fine(study.toString() + " ending invitation phase with " + 
                                study.getCollaborativeResearchers().size() + " collaborative researchers.");
                        logger.fine(study.toString() + " starting research.");
                        study.setPhase(ScientificStudy.RESEARCH_PHASE);
                        
                        // Set initial research work time for primary and all collaborative researchers.
                        study.addPrimaryResearchWorkTime(0D);
                        Iterator<Integer> k = study.getCollaborativeResearchers().keySet().iterator();
                        while (k.hasNext()) study.addCollaborativeResearchWorkTime(unitManager.getPersonByID(k.next()), 0D);
                        
                        continue;
                    }
                }
                else if (study.getPhase().equals(ScientificStudy.RESEARCH_PHASE)) {
                    
                    if (study.isAllResearchCompleted()) {
                        study.setPhase(ScientificStudy.PAPER_PHASE);
                        logger.info(study.toString() + " finished research and is starting data results compiling.");
                        continue;
                    }
                    else {
                        
                        // Check primary researcher downtime.
                        if (!study.isPrimaryResearchCompleted()) {
                            MarsClock lastPrimaryWork = study.getLastPrimaryResearchWorkTime();
                            if ((lastPrimaryWork != null) && MarsClock.getTimeDiff(marsClock, lastPrimaryWork) > 
                                    ScientificStudy.PRIMARY_WORK_DOWNTIME_ALLOWED) {
                                study.setCompleted(ScientificStudy.CANCELED);
                                logger.fine(study.toString() + " canceled due to lack of primary researcher participation.");
                                continue;
                            }
                        }
                                
                        // Check each collaborator for downtime.
                        Iterator<Integer> l = study.getCollaborativeResearchers().keySet().iterator();
                        while (l.hasNext()) {
                            Person researcher = unitManager.getPersonByID(l.next());
                            if (!study.isCollaborativeResearchCompleted(researcher)) {
                                MarsClock lastCollaborativeWork = study.getLastCollaborativeResearchWorkTime(researcher);
                                if ((lastCollaborativeWork != null) && MarsClock.getTimeDiff(marsClock, lastCollaborativeWork) 
                                        > ScientificStudy.COLLABORATIVE_WORK_DOWNTIME_ALLOWED) {
                                    study.removeCollaborativeResearcher(researcher);
                                    logger.fine(researcher.getName() + " removed as collaborator in " + study.toString() + 
                                            " due to lack of participation.");
                                }
                            }
                        }
                    }
                }
                else if (study.getPhase().equals(ScientificStudy.PAPER_PHASE)) {
                    
                    if (study.isAllPaperWritingCompleted()) {
                        study.setPhase(ScientificStudy.PEER_REVIEW_PHASE);
                        study.startingPeerReview();
                        logger.info(study.toString() + " has compiled data results and is starting peer review.");
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
                            logger.info(study.toString() + " is completed with a successful peer review.");
                        }
                        else {
                            study.setCompleted(ScientificStudy.FAILED_COMPLETION);
                            logger.info(study.toString() + " is completed with a failed peer review.");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Checks if a study's primary researcher is dead.
     * @param study the scientific study.
     * @return true if primary researcher dead.
     */
    private boolean isPrimaryResearcherDead(ScientificStudy study) {
        Person primaryResearcher = study.getPrimaryResearcher();
        return primaryResearcher.getPhysicalCondition().isDead();
    }
    
	/**
	 * Computes the overall relationship score of a settlement
	 * 
	 * @param s Settlement
	 * @return the score
	 */
	public double getScienceScore(Settlement s, ScienceType type) {
		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		
		double score = 0;
		double completed = 10;
		double ongoing = 7.5;
		double failed = 2.5;

		List<ScientificStudy> list0 = getCompletedPrimaryStudies(s);
		if (!list0.isEmpty()) {
			for (ScientificStudy ss : list0) {
				if (allSubject || type == ss.getScience()) {
					score += completed;
				}
			}
		}
		

		List<ScientificStudy> list1 = getOngoingPrimaryStudies(s);
		if (!list1.isEmpty()) {
			for (ScientificStudy ss : list1) {
				if (allSubject || type == ss.getScience()) {
					score += ongoing;
				}
			}
		}
		

		List<ScientificStudy> list2 = getAllFailedStudies(s);
		if (!list2.isEmpty()) {
			for (ScientificStudy ss : list2) {
				if (allSubject || type == ss.getScience()) {
					score += failed;
				}
			}
		}
		
		
		List<ScientificStudy> list3 = getCompletedCollaborativeStudies(s);
		if (!list1.isEmpty()) {
			for (ScientificStudy ss : list3) {
				if (allSubject || type == ss.getScience()) {
					score += ongoing;
				}
			}
		}

		List<ScientificStudy> list4 = getOngoingCollaborativeStudies(s);
		if (!list1.isEmpty()) {
			for (ScientificStudy ss : list4) {
				if (allSubject || type == ss.getScience()) {
					score += ongoing;
				}
			}
		}
		
		score = Math.round(score *100.0)/100.0;
		
		return score;
	}
	
	/**
	 * initializes instances after loading from a saved sim
	 * 
	 * @param {{@link MarsClock}
	 */
	public static void initializeInstances(MarsClock c, UnitManager u) {
		unitManager = u;		
		marsClock = c;
	}
	
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
       Iterator<ScientificStudy> i = studies.iterator();
       while (i.hasNext()) {
           i.next().destroy();
       }
       studies.clear();
       studies = null;
    }
}