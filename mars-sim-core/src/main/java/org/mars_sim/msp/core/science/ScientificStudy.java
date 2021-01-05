/**
 * Mars Simulation Project
 * ScientificStudy.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.science;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A class representing a scientific study.
 */
public class ScientificStudy implements Serializable, Temporal, Comparable<ScientificStudy> {
	// POJO holding collaborators effort
	private final static class CollaboratorStats implements Serializable {
		private static final long serialVersionUID = 1L;
		
		double acheivementEarned = 0D;
		double paperWorkTime = 0D;
		double reseachWorkTime = 0D;
		MarsClock lastContribution = null;
		ScienceType contribution;
		
		CollaboratorStats(ScienceType contribution) {
			this.contribution = contribution;
		}
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static final Logger logger = Logger.getLogger(ScientificStudy.class.getName());
	private static final String loggerName = logger.getName();
	private static final String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

	// Study Phases
	public static final String PROPOSAL_PHASE = "Study Proposal";
	public static final String INVITATION_PHASE = "Collaborator Invitation";
	public static final String RESEARCH_PHASE = "Research";
	public static final String PAPER_PHASE = "Writing Paper";
	public static final String PEER_REVIEW_PHASE = "Peer Review";
	public static final String COMPLETE_PHASE = "Completed";
	
	// Completion States
	public static final String SUCCESSFUL_COMPLETION = "Successful Completion";
	public static final String FAILED_COMPLETION = "Failed Completion";
	public static final String CANCELED = "Canceled";

	/** The average amount of base work time (millisols) required for proposal phase. */
	private double baseProposalTime;

	/** The average amount of base work time (millisols) required for primary research. */
	private double basePrimaryResearchTime;

	/** The average amount of base work time (millisols) required for collaborative research. */
	private double baseCollaborativeResearchTime;

	/** The average amount of base work time (millisols) required for primary researcher writing study paper. */
	private double basePrimaryWritingPaperTime;

	/** The average amount of base work time (millisols) required for collaborative researcher writing study paper. */
	private double baseCollaborativePaperWritingTime;

	/** The average amount of base time (millisols) for peer review. */
	private double basePeerReviewTime;

	/** The average amount of downtime (millisols) allowed for primary work. */
	private double primaryWorkDownTimeAllowed;

	/** The average amount of downtime (millisols) allowed for collaborative work. */
	private double collaborativeWorkDownTimeAllowed;
	
	/** A list of listeners for this scientific study. */
	private transient List<ScientificStudyListener> listeners; 
	
	// Data members
	/** Maximum number of collaborative researchers. */
	private int maxCollaborators;
	/** The difficulty level of this scientific study. */
	private int difficultyLevel;
	/** The primary researcher */
	private Person primaryResearcher;

	/** The amount of proposal time done so far. */
	private double proposalWorkTime;
	
	private String phase;
	private String completionState;
	
	private String name;
	private ScienceType science;

	private MarsClock peerReviewStartTime;

	private CollaboratorStats primaryStats;
	private Map<Person, CollaboratorStats> collaborators;
	private Map<Person, Boolean> invitedResearchers;

	/** A major topics this scientific study is aiming at. */
	private List<String> topics;

	private static MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();
	private static ScienceConfig scienceConfig = SimulationConfig.instance().getScienceConfig();

	/**
	 * Constructor.
	 * 
	 * @param primaryResearcher the primary researcher for the study.
	 * @param science           {@link ScienceType} the primary field of science in
	 *                          the study.
	 * @param difficultyLevel   the difficulty level of the study.
	 */
	ScientificStudy(String name, Person primaryResearcher, ScienceType science, int difficultyLevel) {
		// Initialize data members.
		this.name = name;
		this.primaryResearcher = primaryResearcher;
		primaryResearcher.setStudy(this);
		this.science = science;
		this.difficultyLevel = difficultyLevel;

		phase = PROPOSAL_PHASE;
		
		// Gets the average number from scientific_study.json
		int aveNum = ScienceConfig.getAveNumCollaborators();
		// Compute the number for this particular scientific study
		maxCollaborators = (int)aveNum + (int)(aveNum/5D * RandomUtil.getGaussianDouble());
		
		// Compute the base proposal study time for this particular scientific study
		baseProposalTime = computeTime(0) * Math.max(1, difficultyLevel);
		
		// Compute the primary research time for this particular scientific study
		basePrimaryResearchTime = computeTime(1) * Math.max(1, difficultyLevel);
		
		// Compute the collaborative research time for this particular scientific study
		baseCollaborativeResearchTime = computeTime(2) * Math.max(1, difficultyLevel);
		
		// Compute the primary research paper writing time for this particular scientific study
		basePrimaryWritingPaperTime = computeTime(3)  * Math.max(1, difficultyLevel);
		
		// Compute the collaborative paper writing time for this particular scientific study
		baseCollaborativePaperWritingTime = computeTime(4) * Math.max(1, difficultyLevel);
		
		// Compute the base peer review time for this particular scientific study
		basePeerReviewTime = computeTime(5);
		
		// Compute the primary work downtime allowed for this particular scientific study
		primaryWorkDownTimeAllowed = computeTime(6);
		
		// Compute the collaborative work downtime allowed for this particular scientific study
		collaborativeWorkDownTimeAllowed = computeTime(7);
		
		
		collaborators = new HashMap<Person, ScientificStudy.CollaboratorStats>();
		invitedResearchers = new HashMap<Person, Boolean>();
		primaryStats = new CollaboratorStats(science);
		proposalWorkTime = 0D;
		peerReviewStartTime = null;
		completionState = null;
		listeners = new ArrayList<ScientificStudyListener>();
		topics = new ArrayList<>();
	}

	/**
	 * Computes the time of interest for this scientific study
	 * 
	 * @param index
	 * @return
	 */
	private double computeTime(int index) {
		// Gets the average time from scientific_study.json
		int mean = ScienceConfig.getAverageTime(index);
		// Modify it with random gaussian (and limit it to not less than 1/4 of the mean) for this particular scientific study
		double mod = RandomUtil.getGaussianDouble();
		if (mod > 10)
			mod = 10;
		return Math.max(mean / 4D, mean + mean * mod / 5D);	
	}
	
	public double getPrimaryWorkDownTimeAllowed() {
		return primaryWorkDownTimeAllowed;
	}
	
	public int getMaxCollaborators() {
		return maxCollaborators;
	}
	
	/**
	 * Get a list of topics
	 * 
	 * @param type
	 * @return {@link List<String>}
	 */
	public List<String> getTopic() {
		return topics;
	}
	
	/**
	 * Gets the study's current phase.
	 * 
	 * @return phase
	 */
	public String getPhase() {
		return phase;
	}

	/**
	 * Sets the study's current phase.
	 * 
	 * @param phase the phase.
	 */
	private void setPhase(String phase) {
		this.phase = phase;

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.PHASE_CHANGE_EVENT);
	}

	/**
	 * Gets the study's primary field of science.
	 * 
	 * @return science
	 */
	public ScienceType getScience() {
		return science;
	}

	/**
	 * Gets the study's difficulty level.
	 * 
	 * @return difficulty level.
	 */
	public int getDifficultyLevel() {
		return difficultyLevel;
	}

	/**
	 * Gets the study's primary researcher.
	 * 
	 * @return primary researcher
	 */
	public Person getPrimaryResearcher() {
		return primaryResearcher;
	}

	/**
	 * Gets the total amount of proposal work time required for the study.
	 * 
	 * @return work time (millisols).
	 */
	public double getTotalProposalWorkTimeRequired() {
		return baseProposalTime;
	}

	/**
	 * Gets the amount of work time completed for the proposal phase.
	 * 
	 * @return work time (millisols).
	 */
	public double getProposalWorkTimeCompleted() {
		return proposalWorkTime;
	}

	/**
	 * Has the proposal been completed
	 * @return
	 */
	public boolean isProposalCompleted() {
		return (proposalWorkTime >= baseProposalTime);
	}
	
	/**
	 * Adds work time to the proposal phase.
	 * 
	 * @param workTime work time (millisols)
	 */
	public void addProposalWorkTime(double workTime) {
		proposalWorkTime += workTime;
		if (proposalWorkTime >= baseProposalTime)
			proposalWorkTime = baseProposalTime;

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.PROPOSAL_WORK_EVENT);
	}

	/**
	 * Gets the study's collaborative researchers.
	 * 
	 * @return map of researchers and their sciences.
	 */
	public Set<Person> getCollaborativeResearchers() {
		return collaborators.keySet();
	}

	/**
	 * Get the contribution of a researcher to this study. Maybe primary researcher or a collaborator
	 * @param researcher
	 * @return
	 */
	public ScienceType getContribution(Person researcher) {
		ScienceType result = null;
		if (researcher.equals(primaryResearcher)) {
			result = science;
		}
		else {
			CollaboratorStats cs = getCollaboratorStats(researcher);
			if (cs != null) {
				result = cs.contribution;
			}
		}
		return result;
	}
	
	/**
	 * Gets the study's collaborative researchers and their fields of science.
	 * 
	 * @return map of researchers and their sciences.
	 */
	public Map<Person, ScienceType> getPersonCollaborativePersons() {
		Map<Person, ScienceType> map =  new HashMap<>();
		for (Entry<Person, CollaboratorStats> c : collaborators.entrySet()) {
			map.put(c.getKey(), c.getValue().contribution);
		}
		return map;
	}

	
	/**
	 * Adds a collaborative researcher to the study.
	 * Must be synchronised as Collaborators come from Settlements outside the primary Settlement and hence
	 * different Threads.
	 * 
	 * @param researcher the collaborative researcher.
	 * @param science    the scientific field to collaborate with.
	 */
	public synchronized void addCollaborativeResearcher(Person researcher, ScienceType science) {		
		collaborators.put(researcher, new CollaboratorStats(science));

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.ADD_COLLABORATOR_EVENT, researcher);
	}

	/**
	 * Removes a collaborative researcher from a study.
	 * Must be synchronised as Collaborators come from Settlements outside the primary Settlement and hence
	 * different Threads.
	 * @param researcher the collaborative researcher.
	 */
	public synchronized void removeCollaborativeResearcher(Person researcher) { 
		collaborators.remove(researcher);

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.REMOVE_COLLABORATOR_EVENT, researcher);
	}

	/**
	 * Checks if an invited researcher has responded to the invitation.
	 * 
	 * @param researcher the invited researcher
	 * @return true if reseacher has responded.
	 */
	public boolean hasInvitedResearcherResponded(Person researcher) {
		boolean result = false;
		if (invitedResearchers.containsKey(researcher))
			result = invitedResearchers.get(researcher);
		return result;
	}

	/**
	 * Get number of research invitations that have not been responded to yet.
	 * 
	 * @return num invitations.
	 */
	public int getNumOpenResearchInvitations() {
		int result = 0;

		Iterator<Person> i = invitedResearchers.keySet().iterator();
		while (i.hasNext()) {
			if (!invitedResearchers.get(i.next()))
				result++;
		}

		return result;
	}

	/** 
	 * Who has been invited?
	 * @return
	 */
	public Set<Person> getInvitedResearchers() {
		return invitedResearchers.keySet();
	}
	
	/**
	 * Cleans out any dead collaboration invitees.
	 */
	private void cleanResearchInvitations() {
		Iterator<Person> i = invitedResearchers.keySet().iterator();
		
		while (i.hasNext()) {
			Person p = i.next();
			if (p.getPhysicalCondition().isDead()) {
				i.remove();
			}
		}
	}

	/**
	 * Adds a researcher to the list of researchers invited to collaborate on this
	 * study.
	 * 
	 * @param researcher the invited researcher.
	 */
	public synchronized void addInvitedResearcher(Person researcher) {
		if (!invitedResearchers.containsKey(researcher))
			invitedResearchers.put(researcher, false);
	}

	/**
	 * Sets that an invited researcher has responded.
	 * Must be synchronised as Collaborators come from Settlements outside the primary Settlement and hence
	 * different Threads.
	 * 
	 * @param researcher the invited researcher.
	 */
	public synchronized void respondingInvitedResearcher(Person researcher) {
		if (invitedResearchers.containsKey(researcher))
			invitedResearchers.put(researcher, true);
	}

	/**
	 * Gets the total work time required for primary research.
	 * 
	 * @return work time (millisols).
	 */
	public double getTotalPrimaryResearchWorkTimeRequired() {
		return basePrimaryResearchTime;
	}

	/**
	 * Gets the work time completed for primary research.
	 * 
	 * @return work time (millisols).
	 */
	public double getPrimaryResearchWorkTimeCompleted() {
		return primaryStats.reseachWorkTime;
	}

	/**
	 * Adds work time for primary research.
	 * 
	 * @param workTime work time (millisols).
	 */
	public void addPrimaryResearchWorkTime(double workTime) {
		primaryStats.reseachWorkTime += workTime;
		double requiredWorkTime = getTotalPrimaryResearchWorkTimeRequired();
		if (primaryStats.reseachWorkTime >= requiredWorkTime) {
			primaryStats.reseachWorkTime = requiredWorkTime;
		}
		
		// Update last primary work time.
		primaryStats.lastContribution = (MarsClock) marsClock.clone();

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.PRIMARY_RESEARCH_WORK_EVENT, getPrimaryResearcher());
	}

	/**
	 * Checks if primary research has been completed.
	 * 
	 * @return true if primary research completed.
	 */
	public boolean isPrimaryResearchCompleted() {
		return (primaryStats.reseachWorkTime >= getTotalPrimaryResearchWorkTimeRequired());
	}

	/**
	 * Gets the total work time required for a collaborative researcher.
	 * 
	 * @return work time (millisols).
	 */
	public double getTotalCollaborativeResearchWorkTimeRequired() {
		return baseCollaborativeResearchTime;
	}

	private CollaboratorStats getCollaboratorStats(Person researcher) {
		CollaboratorStats c = collaborators.get(researcher);
		if (c == null) {
			throw new IllegalArgumentException(researcher + " is not a collaborative researcher in this study.");	
		}
		return c;
	}
	
	/**
	 * Gets the work time completed for a collaborative researcher.
	 * 
	 * @param researcher the collaborative researcher.
	 * @return work time (millisols).
	 */
	public double getCollaborativeResearchWorkTimeCompleted(Person researcher) {
		return getCollaboratorStats(researcher).reseachWorkTime;
	}

	/**
	 * Adds work time for collaborative research.
	 * Must be synchronised as Collaborators come from Settlements outside the primary Settlement and hence
	 * different Threads.
	 * 
	 * @param researcher the collaborative researcher.
	 * @param workTime   the work time (millisols).
	 */
	public synchronized void addCollaborativeResearchWorkTime(Person researcher, double workTime) {
		CollaboratorStats c = getCollaboratorStats(researcher);
		
		c.reseachWorkTime += workTime;
		double requiredWorkTime = getTotalCollaborativeResearchWorkTimeRequired();
		if (c.reseachWorkTime >= requiredWorkTime)
			c.reseachWorkTime = requiredWorkTime;

		// Update last collaborative work time.
		c.lastContribution = (MarsClock) marsClock.clone();

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.COLLABORATION_RESEARCH_WORK_EVENT, researcher);		
	}

	/**
	 * Checks if collaborative research has been completed by a given researcher.
	 * 
	 * @param researcher the collaborative researcher.
	 */
	public boolean isCollaborativeResearchCompleted(Person researcher) {
		CollaboratorStats c = getCollaboratorStats(researcher);		
		return (c.reseachWorkTime >= getTotalCollaborativeResearchWorkTimeRequired());
	}

	/**
	 * Checks if all research in study has been completed.
	 * 
	 * @return true if research completed.
	 */
	private boolean isAllResearchCompleted() {
		boolean result = true;
		double targetTime = getTotalCollaborativeResearchWorkTimeRequired();
		
		for (CollaboratorStats c : collaborators.values()) {
			if (c.reseachWorkTime >= targetTime);
				result = false;
		}
		return (result && isPrimaryResearchCompleted());
	}

	/**
	 * Gets the total work time required for primary researcher writing paper.
	 * 
	 * @return work time (millisols).
	 */
	public double getTotalPrimaryPaperWorkTimeRequired() {
		return basePrimaryWritingPaperTime;
	}

	/**
	 * Gets the work time completed for primary researcher writing paper.
	 * 
	 * @return work time (millisols).
	 */
	public double getPrimaryPaperWorkTimeCompleted() {
		return primaryStats.paperWorkTime;
	}

	/**
	 * Adds work time for primary researcher writing paper.
	 * 
	 * @param workTime work time (millisols).
	 */
	public void addPrimaryPaperWorkTime(double workTime) {
		primaryStats.paperWorkTime += workTime;
		double requiredWorkTime = getTotalPrimaryPaperWorkTimeRequired();
		if (primaryStats.paperWorkTime >= requiredWorkTime) {
			primaryStats.paperWorkTime = requiredWorkTime;
		}
		
		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.PRIMARY_PAPER_WORK_EVENT);
	}

	/**
	 * Checks if primary researcher paper writing has been completed.
	 * 
	 * @return true if primary researcher paper writing completed.
	 */
	public boolean isPrimaryPaperCompleted() {
		return (primaryStats.paperWorkTime >= getTotalPrimaryPaperWorkTimeRequired());
	}

	/**
	 * Gets the total work time required for a collaborative researcher writing
	 * paper.
	 * 
	 * @return work time (millisols).
	 */
	public double getTotalCollaborativePaperWorkTimeRequired() {
		return baseCollaborativePaperWritingTime;
	}

	/**
	 * Gets the work time completed for a collaborative researcher writing paper.
	 * 
	 * @param researcher the collaborative researcher.
	 * @return work time (millisols).
	 */
	public double getCollaborativePaperWorkTimeCompleted(Person researcher) {
		return getCollaboratorStats(researcher).paperWorkTime;
	}

	/**
	 * Adds work time for collaborative researcher writing paper.
	 * Must be synchronised as Collaborators come from Settlements outside the primary Settlement and hence
	 * different Threads.
	 * 
	 * @param researcher the collaborative researcher.
	 * @param workTime   the work time (millisols).
	 */
	public synchronized void addCollaborativePaperWorkTime(Person researcher, double workTime) {
		CollaboratorStats c = getCollaboratorStats(researcher);
		
		c.paperWorkTime += workTime;
		double requiredWorkTime = getTotalCollaborativePaperWorkTimeRequired();
		if (c.paperWorkTime  >= requiredWorkTime)
			c.paperWorkTime  = requiredWorkTime;

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.COLLABORATION_PAPER_WORK_EVENT, researcher);
	}

	/**
	 * Checks if collaborative paper writing has been completed by a given
	 * researcher.
	 * 
	 * @param researcher the collaborative researcher.
	 */
	public boolean isCollaborativePaperCompleted(Person researcher) {
		return (getCollaboratorStats(researcher).paperWorkTime >= getTotalCollaborativePaperWorkTimeRequired());
	}


	/**
	 * Checks if all paper writing in study has been completed.
	 * 
	 * @return true if paper writing completed.
	 */
	private boolean isAllPaperWritingCompleted() {
		boolean result = true;
		double targetTime = getTotalCollaborativePaperWorkTimeRequired();
		for (CollaboratorStats c : collaborators.values()) {
			if (c.paperWorkTime >= targetTime);
				result = false;
		}
		return (result && isPrimaryPaperCompleted());
	}

	/**
	 * Start the peer review phase of the study.
	 */
	private void startingPeerReview() {
		peerReviewStartTime = (MarsClock) marsClock.clone();
	}

	/**
	 * Checks if peer review time has finished.
	 * 
	 * @return true if peer review time finished.
	 */
	private boolean isPeerReviewTimeFinished() {
		boolean result = false;
		if (peerReviewStartTime != null) {
			double peerReviewTime = MarsClock.getTimeDiff(marsClock, peerReviewStartTime);
			if (peerReviewTime >= peerReviewTime)
				result = true;
		}
		return result;
	}

	/**
	 * Gets the amount of peer review time that has been completed so far.
	 * 
	 * @return peer review time completed (millisols)..
	 */
	public double getPeerReviewTimeCompleted() {
		double result = 0D;
		if (peerReviewStartTime != null) {
			result = MarsClock.getTimeDiff(marsClock, peerReviewStartTime);
		}
		return result;
	}

	/**
	 * Gets the total amount of peer review time required for the study.
	 * 
	 * @return the total peer review time (millisols).
	 */
	public double getTotalPeerReviewTimeRequired() {
		return basePeerReviewTime;
	}

	/**
	 * Sets the study as completed.
	 * 
	 * @param completionState the state of completion.
	 */
	private void setCompleted(String completionState) {
		this.phase = COMPLETE_PHASE;
		this.completionState = completionState;
		primaryResearcher.setStudy(null);

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.STUDY_COMPLETION_EVENT);
	}

	/**
	 * Checks if the study is completed.
	 * 
	 * @return true if completed.
	 */
	public boolean isCompleted() {
		return phase.equals(COMPLETE_PHASE);
	}

	/**
	 * Gets the study's completion state.
	 * 
	 * @return completion state or null if not completed.
	 */
	public String getCompletionState() {
		if (isCompleted())
			return completionState;
		else
			return null;
	}

	/**
	 * Gets the settlement where primary research is conducted.
	 * 
	 * @return settlement.
	 */
	public Settlement getPrimarySettlement() {
		return primaryResearcher.getAssociatedSettlement();
	}

	/**
	 * Gets the last time primary research work was done on the study.
	 * 
	 * @return last time or null if none.
	 */
	public MarsClock getLastPrimaryResearchWorkTime() {
		return primaryStats.lastContribution;
	}

	/**
	 * Gets the primary researcher's earned scientific achievement from the study.
	 * 
	 * @return earned scientific achievement.
	 */
	public double getPrimaryResearcherEarnedScientificAchievement() {
		return primaryStats.acheivementEarned;
	}

	/**
	 * Gets a collaborative researcher's earned scientific achievement from the
	 * study.
	 * 
	 * @param researcher the collaborative researcher.
	 * @return earned scientific achievement.
	 */
	public double getCollaborativeResearcherEarnedScientificAchievement(Person researcher) {
		return getCollaboratorStats(researcher).acheivementEarned;
	}


	/**
     * Determine the results of a study's peer review process.
     * @param study the scientific study.
     * @return true if study passes peer review, false if it fails to pass.
     */
    private boolean determinePeerReviewResults() {
        
        double baseChance = 50D;
        
        // Modify based on primary researcher's academic aptitude attribute.
        int academicAptitude = primaryResearcher.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
        double academicAptitudeModifier = (academicAptitude - 50) / 2D;
        baseChance += academicAptitudeModifier;
                
        for (Entry<Person, CollaboratorStats> c : collaborators.entrySet()) {
            Person researcher = c.getKey();
            double collaboratorModifier = 10D;
            CollaboratorStats cs = c.getValue();
            
            // Modify based on collaborative researcher skill in their science.
            ScienceType collaborativeScience = cs.contribution;
            SkillType skill = collaborativeScience.getSkill();
            int skillLevel = researcher.getSkillManager().getSkillLevel(skill);
            collaboratorModifier *= (double) skillLevel / (double) getDifficultyLevel();
            
            // Modify based on researcher's academic aptitude attribute.
            int collaboratorAcademicAptitude = researcher.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
            double collaboratorAcademicAptitudeModifier = (collaboratorAcademicAptitude - 50) / 10D;
            collaboratorModifier += collaboratorAcademicAptitudeModifier;
            
            // Modify based on if collaborative science is different from primary science.
            if (!collaborativeScience.equals(science)) collaboratorModifier /= 2D;
            
            baseChance += collaboratorModifier;
        }
        
        // Randomly determine if study passes peer review.
        return RandomUtil.getRandomDouble(100D) < baseChance;
    }
    
    /**
     * Provide achievements for the completion of a study.
     */
    private void provideCompletionAchievements() {
        
        double baseAchievement = difficultyLevel;
        
        // Add achievement credit to primary researcher.
        primaryResearcher.addScientificAchievement(baseAchievement, science);
        primaryStats.acheivementEarned = baseAchievement;
        ScientificStudyUtil.modifyScientistRelationshipsFromAchievement(primaryResearcher, science, baseAchievement); 
        
        // Add achievement credit to primary settlement.
        Settlement primarySettlement = getPrimarySettlement();
        primarySettlement.addScientificAchievement(baseAchievement, science);
        
        // Add achievement credit to collaborative researchers.
        double collaborativeAchievement = baseAchievement / 3D;
                
        for (Entry<Person, CollaboratorStats> c : collaborators.entrySet()) {
            Person researcher = c.getKey();
            CollaboratorStats cs = c.getValue();
            ScienceType collaborativeScience = cs.contribution;
            researcher.addScientificAchievement(collaborativeAchievement, collaborativeScience);
            cs.acheivementEarned = collaborativeAchievement;
            ScientificStudyUtil.modifyScientistRelationshipsFromAchievement(researcher, collaborativeScience, collaborativeAchievement);
            
            // Add achievement credit to the collaborative researcher's current settlement.
            Settlement collaboratorSettlement = researcher.getAssociatedSettlement();
            if (collaboratorSettlement != null) collaboratorSettlement.addScientificAchievement(
                    collaborativeAchievement, collaborativeScience);
        }
    }
    
	/**
	 * Adds a listener
	 * 
	 * @param newListener the listener to add.
	 */
	public synchronized void addScientificStudyListener(ScientificStudyListener newListener) {
		if (listeners == null)
			listeners = new ArrayList<>();
		if (!listeners.contains(newListener))
			listeners.add(newListener);
	}

	/**
	 * Removes a listener
	 * 
	 * @param oldListener the listener to remove.
	 */
	public synchronized void removeScientificStudyListener(ScientificStudyListener oldListener) {
		if (listeners == null)
			listeners = new ArrayList<>();
		if (listeners.contains(oldListener))
			listeners.remove(oldListener);
	}

	/**
	 * Fire a scientific study update event.
	 * 
	 * @param type the update type.
	 */
	private void fireScientificStudyUpdate(String type) {
		fireScientificStudyUpdate(type, null);
	}

	/**
	 * Fire a scientific study update event.
	 * 
	 * @param buildingType the update type.
	 * @param researcher   the researcher related to the event or null if none.
	 */
	private void fireScientificStudyUpdate(String updateType, Person researcher) {
		if (listeners != null) {
			synchronized (listeners) {
				Iterator<ScientificStudyListener> i = listeners.iterator();
				while (i.hasNext())
					i.next().scientificStudyUpdate(new ScientificStudyEvent(this, researcher, updateType));
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(ScientificStudy o) {
		return getName().compareTo(o.getName());
	}
	
	/**
	 * initializes instances after loading from a saved sim
	 * 
	 * @param {{@link MarsClock}
	 */
	public static void initializeInstances(MarsClock c) {	
		marsClock = c;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		phase = null;
		science = null;
//		primaryResearcher = null;
		collaborators = null;
		invitedResearchers.clear();
		invitedResearchers = null;
		peerReviewStartTime = null;
		completionState = null;
		if (listeners != null) {
			listeners.clear();
		}
		listeners = null;
	}

	/**
	 * Time passes for the study
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		Person person = primaryResearcher;
		String name = person.getName();
		
		// Check if primary researcher has died.
		if (primaryResearcher.getPhysicalCondition().isDead()) {
			setCompleted(ScientificStudy.CANCELED);
			logger.fine(toString() + " was canceled due to primary researcher's death.");
			LogConsolidated.flog(Level.INFO, 0, sourceName,
					"[" + primaryResearcher.getLocationTag().getLocale() + "] " 
					+ "Due to " + name + "'s death, the " + getName()
					+ " study was abandoned.");
			return true;
		}
		
		// Check if collaborators have died. Take a copy as removal is possible
		Set<Person> colls = new HashSet<>(collaborators.keySet());
		for (Person collaborator : colls) {
			if (collaborator.getPhysicalCondition().isDead()) {
				removeCollaborativeResearcher(collaborator);
				LogConsolidated.flog(Level.INFO, 0, sourceName,
						"[" + collaborator.getLocationTag().getLocale() + "] " 
						+ collaborator.getName() + " (a collaborator) was removed in the " + getName()
						+ " study since they passed away.");
			}
		}

		switch (phase) {
		case PROPOSAL_PHASE:
			// Check if proposal work time is completed, then move to invitation phase.
			if (proposalWorkTime >= baseProposalTime) {
				LogConsolidated.flog(Level.INFO, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " 
						+ name  +  " finished writing proposal for the "
						+ getName() + " study and was starting to invite collaborative researchers");
				// Picks research topics 
				topics.add(scienceConfig.getATopic(science));
				setPhase(INVITATION_PHASE);
			}
			break;
		
		case INVITATION_PHASE:
			// Clean out any dead research invitees.
			cleanResearchInvitations();

			boolean phaseEnded = false;
			if (collaborators.size() < maxCollaborators) {
				int availableInvitees = ScientificStudyUtil.getAvailableCollaboratorsForInvite(this).size();
				int openResearchInvitations = getNumOpenResearchInvitations();
				if ((availableInvitees + openResearchInvitations) == 0)
					phaseEnded = true;
			} else
				phaseEnded = true;

			if (phaseEnded) {
				LogConsolidated.flog(Level.INFO, 0, sourceName,
						"[" + primaryResearcher.getLocationTag().getLocale() + "] " 
						+ primaryResearcher.getName()  + " ended the invitation phase on the " + getName() + " study with "
						+ collaborators.size() 
						+ " collaborative researchers and started the research work phase.");
				setPhase(RESEARCH_PHASE);
			}
			break;
			
		case RESEARCH_PHASE:
			if (isAllResearchCompleted()) {
				setPhase(PAPER_PHASE);
				LogConsolidated.flog(Level.INFO, 0, sourceName,
						"[" + primaryResearcher.getLocationTag().getLocale() + "] " 
						+ primaryResearcher.getName() + " finished the research work on the " 
						+ getName() + " study and was starting to compile data results.");
			} else {

				// Check primary researcher downtime.
				if (!isPrimaryResearchCompleted()) {
					MarsClock lastPrimaryWork = getLastPrimaryResearchWorkTime();
					if ((lastPrimaryWork != null) && MarsClock.getTimeDiff(pulse.getMarsTime(),
							lastPrimaryWork) > getPrimaryWorkDownTimeAllowed()) {
						setCompleted(CANCELED);
						LogConsolidated.flog(Level.INFO, 0, sourceName,
								"[" + primaryResearcher.getLocationTag().getLocale() + "] " 
								+ primaryResearcher.getName() + " abandoned the "
								+ getName()
								+ " study due to lack of primary researcher participation.");
					}
				}

				// Check each collaborator for downtime. Take a copy because it may change
				for (Entry<Person, CollaboratorStats> c : new HashSet<>(collaborators.entrySet())) {
					Person researcher = c.getKey();
					if (!isCollaborativeResearchCompleted(researcher)) {
						MarsClock lastCollaborativeWork = c.getValue().lastContribution;
						if ((lastCollaborativeWork != null) && MarsClock.getTimeDiff(pulse.getMarsTime(),
								lastCollaborativeWork) > collaborativeWorkDownTimeAllowed) {
							removeCollaborativeResearcher(researcher);
							LogConsolidated.flog(Level.INFO, 0, sourceName,
									"[" + researcher.getLocationTag().getLocale() + "] " 
									+ researcher.getName() + " (a collaborator) was removed in the " 
									+ getName()
									+ " study due to lack of participation.");
						}
					}
				}
			}
			break;
			
		case PAPER_PHASE:
			if (isAllPaperWritingCompleted()) {
				setPhase(PEER_REVIEW_PHASE);
				startingPeerReview(); 
				LogConsolidated.flog(Level.INFO, 0, sourceName,
						"[" + primaryResearcher.getLocationTag().getLocale() + "] " + primaryResearcher.getName() 
						+ " had compiled data results for "
						+ getName() + " and was starting to do tbreak;"
								+ "he peer review.");
			}
			break;
			
		case PEER_REVIEW_PHASE:
			if (isPeerReviewTimeFinished()) {
				// Determine results of peer review.
				if (determinePeerReviewResults()) {
					setCompleted(SUCCESSFUL_COMPLETION);

					// Provide scientific achievement to primary and collaborative researchers.
					provideCompletionAchievements();
					LogConsolidated.flog(Level.INFO, 0, sourceName,
							"[" + primaryResearcher.getLocationTag().getLocale() + "] " 
								+ primaryResearcher.getName() + " completed a peer review on the " 
							+ getName() + " study successfully.");
				}
				else {
					setCompleted(ScientificStudy.FAILED_COMPLETION);
					LogConsolidated.flog(Level.INFO, 0, sourceName,
							"[" + primaryResearcher.getLocationTag().getLocale() + "] " 
							+ primaryResearcher.getName() + " failed to complete a peer review on the " 
							+ getName() + " study.");
				}
			}
			break;
			
		default: // Nothing to do
				break;
		}
		return true;
	}
}