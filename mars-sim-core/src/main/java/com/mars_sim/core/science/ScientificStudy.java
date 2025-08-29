/*
 * Mars Simulation Project
 * ScientificStudy.java
 * @date 2025-08-09 (patched per PR #1679 discussion)
 * @author Scott Davis
 */
package com.mars_sim.core.science;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A class representing a scientific study.
 */
public class ScientificStudy implements Entity, Temporal, Comparable<ScientificStudy> {

	// POJO holding collaborators' effort
	private static final class CollaboratorStats implements Serializable {
		private static final long serialVersionUID = 1L;

		double acheivementEarned = 0D;
		double paperWorkTime = 0D;
		double reseachWorkTime = 0D;
		MarsTime lastContribution = null;
		ScienceType contribution;

		CollaboratorStats(ScienceType contribution) {
			this.contribution = contribution;
		}
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ScientificStudy.class.getName());

	// Data members
	/** The assigned study number. */
	private int id;
	/** Maximum number of collaborative researchers. */
	private int maxCollaborators;
	/** The difficulty level of this scientific study. */
	private int difficultyLevel;

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

	/** The amount of proposal time done so far. */
	private double proposalWorkTime;

	/** The primary researcher. */
	private Person primaryResearcher;

	private StudyStatus phase;
	private String name;
	private ScienceType science;

	private MarsTime peerReviewStartTime;

	private CollaboratorStats primaryStats;

	/** Having these keyed on Person seems to create a problem deserializing a saved sim. */
	private Map<Integer, CollaboratorStats> collaborators;
	/** A map of invited researchers.  */
	private Map<Integer, Boolean> invitedResearchers;
	/** A list of listeners for this scientific study. */
	private transient List<ScientificStudyListener> listeners;
	/** Major topics covered by this research. */
	private List<String> topics;

	private static MasterClock masterClock;

	private static ScienceConfig scienceConfig;

	/**
	 * Constructor.
	 *
	 * @param id                study id
	 * @param name              study name
	 * @param primaryResearcher the primary researcher for the study.
	 * @param science           {@link ScienceType} the primary field of science in the study.
	 * @param difficultyLevel   the difficulty level of the study.
	 */
	ScientificStudy(int id, String name, Person primaryResearcher, ScienceType science, int difficultyLevel) {
		// Initialize data members.
		this.id = id;
		this.name = name;
		this.primaryResearcher = primaryResearcher;
		primaryResearcher.getResearchStudy().setStudy(this);
		this.science = science;
		this.difficultyLevel = difficultyLevel;

		phase = StudyStatus.PROPOSAL_PHASE;

		// Gets the average number from scientific_study.json
		int aveNum = scienceConfig.getAveNumCollaborators();
		// Compute the number for this particular scientific study
		maxCollaborators = (int) RandomUtil.getGaussianPositive(aveNum, aveNum / 5D);

		// Compute base times for this particular scientific study
		baseProposalTime = computeTime(SciencePhaseTime.PROPOSAL) * Math.max(1, difficultyLevel);
		basePrimaryResearchTime = computeTime(SciencePhaseTime.PRIMARY_RESEARCH) * Math.max(1, difficultyLevel);
		baseCollaborativeResearchTime = computeTime(SciencePhaseTime.COLLABORATIVE_RESEARCH) * Math.max(1, difficultyLevel);
		basePrimaryWritingPaperTime = computeTime(SciencePhaseTime.PRIMARY_RESEARCHER_WRITING) * Math.max(1, difficultyLevel);
		baseCollaborativePaperWritingTime = computeTime(SciencePhaseTime.COLLABORATOR_WRITING) * Math.max(1, difficultyLevel);
		basePeerReviewTime = computeTime(SciencePhaseTime.PEER_REVIEW);
		primaryWorkDownTimeAllowed = computeTime(SciencePhaseTime.PRIMARY_RESEARCHER_IDLE);
		collaborativeWorkDownTimeAllowed = computeTime(SciencePhaseTime.COLLABORATOR_IDLE);

		// Concurrent to avoid reload-corruption with multiple threads.
		collaborators = new ConcurrentHashMap<>();
		invitedResearchers = new ConcurrentHashMap<>();
		primaryStats = new CollaboratorStats(science);
		proposalWorkTime = 0D;
		peerReviewStartTime = null;
		listeners = new ArrayList<>();
		topics = new ArrayList<>();
	}

	/**
	 * Computes the time of interest for this scientific study.
	 *
	 * @param index phase time index
	 * @return phase time in millisols
	 */
	private double computeTime(SciencePhaseTime index) {
		// Gets the average time from scientific_study.json
		int mean = scienceConfig.getAverageTime(index);
		// Modify it with random gaussian for this particular scientific study
		double mod = RandomUtil.getGaussianPositive(0, .68);
		if (mod > 10) mod = 10;
		// Limit it to not less than 1/4 of the mean
		return Math.max(mean / 4D, mean + mean * mod / 5D);
	}

	public double getPrimaryWorkDownTimeAllowed() {
		return primaryWorkDownTimeAllowed;
	}

	public int getMaxCollaborators() {
		return maxCollaborators;
	}

	/**
	 * Gets a list of topics.
	 *
	 * @return {@link List<String>} a list of topics
	 */
	public List<String> getTopic() {
		return topics;
	}

	/**
	 * Gets the assigned id of this study.
	 *
	 * @return id
	 */
	public int getID() {
		return id;
	}

	/**
	 * Gets the study's current phase.
	 *
	 * @return phase
	 */
	public StudyStatus getPhase() {
		return phase;
	}

	/**
	 * Sets the study's current phase.
	 *
	 * @param phase the phase.
	 */
	private void setPhase(StudyStatus phase) {
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
	 * Has the proposal been completed ?
	 *
	 * @return true if completed
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
		if (proposalWorkTime >= baseProposalTime) {
			proposalWorkTime = baseProposalTime;
		}

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.PROPOSAL_WORK_EVENT);
	}

	/**
	 * Gets the study's collaborative researchers.
	 *
	 * @return set of researchers.
	 */
	public Set<Person> getCollaborativeResearchers() {
		return getPersons(collaborators.keySet());
	}

	/**
	 * Converts a set of Person IDs into a Set of Person objects.
	 */
	private static Set<Person> getPersons(Set<Integer> ids) {
		UnitManager um = getUnitManager();
		return ids.stream()
		          .map(um::getPersonByID)
		          .filter(p -> p != null)
		          .collect(Collectors.toSet());
	}

	/**
	 * Gets the contribution of a researcher to this study. Maybe primary researcher or a collaborator.
	 *
	 * @param researcher person
	 * @return Can return null if this person does not play a part.
	 */
	public ScienceType getContribution(Person researcher) {
		ScienceType result;
		if (researcher.equals(primaryResearcher)) {
			result = science;
		}
		else {
			CollaboratorStats c = collaborators.get(researcher.getIdentifier());
			result = (c != null ? c.contribution : null);
		}
		return result;
	}

	/**
	 * Get the Science contributions to this study.
	 * @return The collaboration science.
	 */
	public Set<ScienceType> getCollaborationScience() {
		return collaborators.values().stream()
				.map(s -> s.contribution)
				.collect(Collectors.toSet());
	}

	/**
	 * Gets the study's collaborative researchers and their fields of science.
	 *
	 * @return map of researchers and their sciences.
	 */
	public Map<Person, ScienceType> getPersonCollaborativePersons() {
		UnitManager um = getUnitManager();
		Map<Person, ScienceType> map = new HashMap<>();
		for (Entry<Integer, CollaboratorStats> c : collaborators.entrySet()) {
			Person p = um.getPersonByID(c.getKey());
			if (p != null) {
				map.put(p, c.getValue().contribution);
			}
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
	public void addCollaborativeResearcher(Person researcher, ScienceType science) {
		synchronized (collaborators) {
			collaborators.put(researcher.getIdentifier(), new CollaboratorStats(science));
			researcher.getResearchStudy().addCollabStudy(this);
		}

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.ADD_COLLABORATOR_EVENT, researcher);
	}

	/**
	 * Removes a collaborative researcher from a study.
	 * Must be synchronised as Collaborators come from Settlements outside the primary Settlement and hence
	 * different Threads.
	 *
	 * @param researcher the collaborative researcher.
	 */
	private void removeCollaborativeResearcher(Person researcher) {
		synchronized (collaborators) {
			// Remove research first in case they make a call to this Study
			researcher.getResearchStudy().removeCollabStudy(this);
			collaborators.remove(researcher.getIdentifier());
		}

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.REMOVE_COLLABORATOR_EVENT, researcher);
	}

	/**
	 * Checks if an invited researcher has responded to the invitation.
	 *
	 * @param researcher the invited researcher
	 * @return true if researcher has responded.
	 */
	public boolean hasInvitedResearcherResponded(Person researcher) {
		boolean result = false;
		int resId = researcher.getIdentifier();
		if (invitedResearchers.containsKey(resId))
			result = invitedResearchers.get(resId);
		return result;
	}

	/**
	 * Gets number of research invitations that have not been responded to yet.
	 *
	 * @return num invitations.
	 */
	public int getNumOpenResearchInvitations() {
		int result = 0;
		for (Boolean responded : invitedResearchers.values()) {
			if ((responded != null) && !responded.booleanValue()) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Who has been invited?
	 */
	public Set<Person> getInvitedResearchers() {
		return getPersons(invitedResearchers.keySet());
	}

	/**
	 * Cleans out any dead invitees.
	 */
	private void cleanDeadInvitations() {
		Set<Person> dead = findDeadPeople(invitedResearchers.keySet());
		for (Person d : dead) {
			logger.info(this, "Remove dead invitee " + d.getName() + ".");
			invitedResearchers.remove(d.getIdentifier());
		}
	}

	/**
	 * Cleans out any dead collaborators.
	 */
	private void cleanDeadCollaborators() {
		Set<Person> dead = findDeadPeople(collaborators.keySet());
		for (Person d : dead) {
			logger.info(this, "Remove dead collaborator " + d.getName() + ".");
			removeCollaborativeResearcher(d);
		}
	}

	/**
	 * Finds all dead people in a list of IDs.
	 */
	private static Set<Person> findDeadPeople(Set<Integer> ids) {
		Set<Person> dead = new UnitSet<>();
		UnitManager um = getUnitManager();
		for (Integer id : ids) {
			Person p = um.getPersonByID(id);
			if (p == null || p.getPhysicalCondition().isDead()) {
				if (p != null) dead.add(p);
			}
		}
		return dead;
	}

	private static UnitManager getUnitManager() {
		return Simulation.instance().getUnitManager();
	}

	/**
	 * Adds a researcher to the list of researchers invited to collaborate on this study.
	 */
	public synchronized void addInvitedResearcher(Person researcher) {
		invitedResearchers.put(researcher.getIdentifier(), Boolean.FALSE);
	}

	/**
	 * Sets that an invited researcher has responded.
	 * Must be synchronised as Collaborators come from Settlements outside the primary Settlement and hence
	 * different Threads.
	 */
	public synchronized void respondingInvitedResearcher(Person researcher) {
		invitedResearchers.put(researcher.getIdentifier(), Boolean.TRUE);
	}

	/**
	 * Gets the total work time required for primary research.
	 */
	public double getTotalPrimaryResearchWorkTimeRequired() {
		return basePrimaryResearchTime;
	}

	/**
	 * Gets the work time completed for primary research.
	 */
	public double getPrimaryResearchWorkTimeCompleted() {
		return primaryStats.reseachWorkTime;
	}

	/**
	 * Adds work time for primary research.
	 */
	public void addPrimaryResearchWorkTime(double workTime) {
		primaryStats.reseachWorkTime += workTime;
		double requiredWorkTime = getTotalPrimaryResearchWorkTimeRequired();
		if (primaryStats.reseachWorkTime >= requiredWorkTime) {
			primaryStats.reseachWorkTime = requiredWorkTime;
		}

		// Update last primary work time.
		primaryStats.lastContribution = masterClock.getMarsTime();

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.PRIMARY_RESEARCH_WORK_EVENT, getPrimaryResearcher());
	}

	/**
	 * Checks if primary research has been completed.
	 */
	public boolean isPrimaryResearchCompleted() {
		return (primaryStats.reseachWorkTime >= getTotalPrimaryResearchWorkTimeRequired());
	}

	/**
	 * Gets the total work time required for a collaborative researcher.
	 */
	public double getTotalCollaborativeResearchWorkTimeRequired() {
		return baseCollaborativeResearchTime;
	}

	private synchronized CollaboratorStats getCollaboratorStats(Person researcher) {
		CollaboratorStats c = collaborators.get(researcher.getIdentifier());
		if (c == null) {
			throw new IllegalArgumentException(researcher + " is not a collaborative researcher in this study.");
		}
		return c;
	}

	/**
	 * Gets the work time completed for a collaborative researcher.
	 */
	public double getCollaborativeResearchWorkTimeCompleted(Person researcher) {
		return getCollaboratorStats(researcher).reseachWorkTime;
	}

	/**
	 * Adds work time for collaborative research.
	 * Must be synchronised as Collaborators come from Settlements outside the primary Settlement and hence
	 * different Threads.
	 */
	public synchronized void addCollaborativeResearchWorkTime(Person researcher, double workTime) {
		CollaboratorStats c = getCollaboratorStats(researcher);
		c.reseachWorkTime += workTime;
		double requiredWorkTime = getTotalCollaborativeResearchWorkTimeRequired();
		if (c.reseachWorkTime >= requiredWorkTime) {
			c.reseachWorkTime = requiredWorkTime;
		}
		// Update last collaborative work time.
		c.lastContribution = masterClock.getMarsTime();

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.COLLABORATION_RESEARCH_WORK_EVENT, researcher);
	}

	/**
	 * Checks if collaborative research has been completed by a given researcher.
	 *
	 * (Per PR #1679 discussion) Guard against null/non-collaborator and return false instead of throwing.
	 */
	public synchronized boolean isCollaborativeResearchCompleted(Person researcher) {
		if (researcher == null) return false;
		CollaboratorStats c = collaborators.get(researcher.getIdentifier());
		if (c == null) return false;
		return c.reseachWorkTime >= getTotalCollaborativeResearchWorkTimeRequired();
	}

	private boolean isAllResearchCompleted() {
		boolean result = true;
		double targetTime = getTotalCollaborativeResearchWorkTimeRequired();

		for (CollaboratorStats c : collaborators.values()) {
			// If ANY collaborator has NOT reached the target, overall research is not complete.
			if (c.reseachWorkTime < targetTime) result = false;
		}
		return (result && isPrimaryResearchCompleted());
	}

	/**
	 * Gets the total work time required for primary researcher writing paper.
	 */
	public double getTotalPrimaryPaperWorkTimeRequired() {
		return basePrimaryWritingPaperTime;
	}

	/**
	 * Gets the work time completed for primary researcher writing paper.
	 */
	public double getPrimaryPaperWorkTimeCompleted() {
		return primaryStats.paperWorkTime;
	}

	/**
	 * Adds work time for primary researcher writing paper.
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
	 */
	public boolean isPrimaryPaperCompleted() {
		return (primaryStats.paperWorkTime >= getTotalPrimaryPaperWorkTimeRequired());
	}

	/**
	 * Gets the total work time required for a collaborative researcher writing paper.
	 */
	public double getTotalCollaborativePaperWorkTimeRequired() {
		return baseCollaborativePaperWritingTime;
	}

	/**
	 * Gets the work time completed for a collaborative researcher writing paper.
	 */
	public double getCollaborativePaperWorkTimeCompleted(Person researcher) {
		return getCollaboratorStats(researcher).paperWorkTime;
	}

	/**
	 * Adds work time for collaborative researcher writing paper.
	 * Must be synchronised as Collaborators come from Settlements outside the primary Settlement and hence
	 * different Threads.
	 */
	public synchronized void addCollaborativePaperWorkTime(Person researcher, double workTime) {
		CollaboratorStats c = getCollaboratorStats(researcher);

		c.paperWorkTime += workTime;
		double requiredWorkTime = getTotalCollaborativePaperWorkTimeRequired();
		if (c.paperWorkTime >= requiredWorkTime) {
			c.paperWorkTime = requiredWorkTime;
		}

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.COLLABORATION_PAPER_WORK_EVENT, researcher);
	}

	/**
	 * Checks if collaborative paper writing has been completed by a given researcher.
	 */
	public boolean isCollaborativePaperCompleted(Person researcher) {
		return (getCollaboratorStats(researcher).paperWorkTime >= getTotalCollaborativePaperWorkTimeRequired());
	}

	/**
	 * Checks if all paper writing in study has been completed.
	 */
	private boolean isAllPaperWritingCompleted() {
		boolean result = true;
		double targetTime = getTotalCollaborativePaperWorkTimeRequired();
		for (CollaboratorStats c : collaborators.values()) {
			// If ANY collaborator has NOT reached the target, overall paper writing is not complete.
			if (c.paperWorkTime < targetTime) result = false;
		}
		return (result && isPrimaryPaperCompleted());
	}

	/**
	 * Start the peer review phase of the study.
	 */
	private void startingPeerReview() {
		peerReviewStartTime = masterClock.getMarsTime();
	}

	/**
	 * Checks if peer review time has finished.
	 */
	private boolean isPeerReviewTimeFinished() {
		boolean result = false;
		if (peerReviewStartTime != null) {
			double peerReviewTime = masterClock.getMarsTime().getTimeDiff(peerReviewStartTime);
			if (peerReviewTime >= basePeerReviewTime) result = true;
		}
		return result;
	}

	/**
	 * Gets the amount of peer review time that has been completed so far.
	 */
	public double getPeerReviewTimeCompleted() {
		double result = 0D;
		if (peerReviewStartTime != null) {
			result = masterClock.getMarsTime().getTimeDiff(peerReviewStartTime);
		}
		return result;
	}

	/**
	 * Gets the total amount of peer review time required for the study.
	 */
	public double getTotalPeerReviewTimeRequired() {
		return basePeerReviewTime;
	}

	/**
	 * Sets the study as completed.
	 *
	 * @param completionState the state of completion.
	 * @param reason          Reason for completed
	 */
	public void setCompleted(StudyStatus completionState, String reason) {
		if (!StudyStatus.isCompleted(completionState)) {
			throw new IllegalArgumentException("State cannot be used to complete Study:" + completionState.name());
		}

		logger.info(this, "State: " + completionState.getName() + ". Reason: " + reason);

		this.phase = completionState;
		primaryResearcher.getResearchStudy().setStudy(null);

		for (Person p : getCollaborativeResearchers()) {
			p.getResearchStudy().removeCollabStudy(this);
		}

		// Fire scientific study update event.
		fireScientificStudyUpdate(ScientificStudyEvent.STUDY_COMPLETION_EVENT);
	}

	/**
	 * Checks if the study is completed.
	 */
	public boolean isCompleted() {
		return StudyStatus.isCompleted(phase);
	}

	/**
	 * Gets the settlement where primary research is conducted.
	 */
	public Settlement getPrimarySettlement() {
		return primaryResearcher.getAssociatedSettlement();
	}

	/**
	 * Gets the last time primary research work was done on the study.
	 */
	public MarsTime getLastPrimaryResearchWorkTime() {
		return primaryStats.lastContribution;
	}

	/**
	 * Gets the primary researcher's earned scientific achievement from the study.
	 */
	public double getPrimaryResearcherEarnedScientificAchievement() {
		return primaryStats.acheivementEarned;
	}

	/**
	 * Gets a collaborative researcher's earned scientific achievement from the study.
	 */
	public double getCollaborativeResearcherEarnedScientificAchievement(Person researcher) {
		return getCollaboratorStats(researcher).acheivementEarned;
	}

	/**
	 * Determines the results of a study's peer review process.
	 *
	 * @return true if study passes peer review, false if it fails to pass.
	 */
	private boolean determinePeerReviewResults() {

		double baseChance = 50D;

		// Modify based on primary researcher's academic aptitude attribute.
		int academicAptitude = primaryResearcher.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		double academicAptitudeModifier = (academicAptitude - 50) / 2D;
		baseChance += academicAptitudeModifier;

		UnitManager um = getUnitManager();
		for (Entry<Integer, CollaboratorStats> c : collaborators.entrySet()) {
			Person researcher = um.getPersonByID(c.getKey());
			if (researcher == null) continue;

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
	 * Provides achievements for the completion of a study.
	 */
	private void provideCompletionAchievements() {

		double baseAchievement = difficultyLevel;

		// Add achievement credit to primary researcher.
		primaryResearcher.getResearchStudy().addScientificAchievement(baseAchievement, science);
		primaryStats.acheivementEarned = baseAchievement;
		ScientificStudyUtil.modifyScientistRelationshipsFromAchievement(primaryResearcher, science, baseAchievement);

		// Add achievement credit to primary settlement.
		Settlement primarySettlement = getPrimarySettlement();
		primarySettlement.addScientificAchievement(baseAchievement, science);

		// Add achievement credit to collaborative researchers.
		double collaborativeAchievement = baseAchievement / 3D;
		UnitManager um = getUnitManager();
		for (Entry<Integer, CollaboratorStats> c : collaborators.entrySet()) {
			Person researcher = um.getPersonByID(c.getKey());
			if (researcher == null) continue;

			CollaboratorStats cs = c.getValue();
			ScienceType collaborativeScience = cs.contribution;
			researcher.getResearchStudy().addScientificAchievement(collaborativeAchievement, collaborativeScience);
			cs.acheivementEarned = collaborativeAchievement;
			ScientificStudyUtil.modifyScientistRelationshipsFromAchievement(researcher, collaborativeScience, collaborativeAchievement);

			// Add achievement credit to the collaborative researcher's current settlement.
			Settlement collaboratorSettlement = researcher.getAssociatedSettlement();
			if (collaboratorSettlement != null) {
				collaboratorSettlement.addScientificAchievement(collaborativeAchievement, collaborativeScience);
			}
		}
	}

	/**
	 * Adds a listener.
	 */
	public synchronized void addScientificStudyListener(ScientificStudyListener newListener) {
		if (listeners == null) listeners = new ArrayList<>();
		if (!listeners.contains(newListener)) listeners.add(newListener);
	}

	/**
	 * Removes a listener.
	 */
	public synchronized void removeScientificStudyListener(ScientificStudyListener oldListener) {
		if (listeners == null) listeners = new ArrayList<>();
		if (listeners.contains(oldListener)) listeners.remove(oldListener);
	}

	/**
	 * Fires a scientific study update event.
	 */
	private void fireScientificStudyUpdate(String type) {
		fireScientificStudyUpdate(type, null);
	}

	/**
	 * Fires a scientific study update event.
	 *
	 * @param updateType the update type.
	 * @param researcher the researcher related to the event or null if none.
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

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Context for a study is always the Settlement of the lead researcher.
	 */
	@Override
	public String getContext() {
		return primaryResearcher.getAssociatedSettlement().getName();
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Compares this object with the specified object for order.
	 */
	@Override
	public int compareTo(ScientificStudy o) {
		return getName().compareTo(o.getName());
	}

	/**
	 * Initializes instances after loading from a saved sim.
	 */
	public static void initializeInstances(MasterClock c, ScienceConfig sc) {
		masterClock = c;
		scienceConfig = sc;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		phase = null;
		science = null;
		collaborators = null;
		if (invitedResearchers != null) invitedResearchers.clear();
		invitedResearchers = null;
		peerReviewStartTime = null;
		if (listeners != null) listeners.clear();
		listeners = null;
	}

	/**
	 * Time passes for the study.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {

		// Check if collaborators have died. Remove dead & inactive collaborators
		cleanDeadCollaborators();

		switch (phase) {
		case PROPOSAL_PHASE:
			// Check if proposal work time is completed, then move to invitation phase.
			if (proposalWorkTime >= baseProposalTime) {
				logger.info(this, "Finished writing proposal. Starting to invite collaborative researchers.");
				topics.add(scienceConfig.getATopic(science));
				setPhase(StudyStatus.INVITATION_PHASE);
			}
			break;

		case INVITATION_PHASE:
			// Clean out any dead research invitees.
			cleanDeadInvitations();

			boolean phaseEnded = false;
			if (collaborators.size() < maxCollaborators) {
				int availableInvitees = ScientificStudyUtil.getAvailableNumCollaboratorsForInvite(this);
				int openResearchInvitations = getNumOpenResearchInvitations();
				if ((availableInvitees + openResearchInvitations) == 0) phaseEnded = true;
			}
			else phaseEnded = true;

			if (phaseEnded) {
				logger.info(this, "Ended the invitation phase with "
						+ collaborators.size()
						+ " collaborative researchers. Started the research work phase.");
				setPhase(StudyStatus.RESEARCH_PHASE);
			}
			break;

		case RESEARCH_PHASE:
			if (isAllResearchCompleted()) {
				setPhase(StudyStatus.PAPER_PHASE);
				logger.info(this, "Finished the research work. Starting to compile data results.");
			}
			else {
				MarsTime now = masterClock.getMarsTime();

				// Check primary researcher downtime.
				if (!isPrimaryResearchCompleted()) {
					MarsTime lastPrimaryWork = getLastPrimaryResearchWorkTime();
					if ((lastPrimaryWork != null) && (now.getTimeDiff(lastPrimaryWork) > getPrimaryWorkDownTimeAllowed())) {
						setCompleted(StudyStatus.CANCELLED, "Abandoned due to lack of participation from the primary researcher");
					}
				}

				// Check each collaborator for downtime. Take a copy because it may change
				UnitManager um = getUnitManager();
				for (Entry<Integer, CollaboratorStats> e : new HashSet<>(collaborators.entrySet())) {
					CollaboratorStats c = e.getValue();
					// Only remove for inactivity if they have NOT yet completed their required work
					if (c.reseachWorkTime < baseCollaborativeResearchTime) {
						MarsTime lastCollaborativeWork = c.lastContribution;
						if ((lastCollaborativeWork != null)
								&& (now.getTimeDiff(lastCollaborativeWork) > collaborativeWorkDownTimeAllowed)) {
							Person researcher = um.getPersonByID(e.getKey());
							if (researcher != null) {
								removeCollaborativeResearcher(researcher);
								logger.info(this, "Removed " + researcher.getName()
									+ " as a collaborator due to lack of participation");
							}
						}
					}
				}
			}
			break;

		case PAPER_PHASE:
			if (isAllPaperWritingCompleted()) {
				setPhase(StudyStatus.PEER_REVIEW_PHASE);
				startingPeerReview();
				logger.info(this, "Done compiling data results. Starting to do a peer review.");
			}
			break;

		case PEER_REVIEW_PHASE:
			if (isPeerReviewTimeFinished()) {
				// Determine results of peer review.
				if (determinePeerReviewResults()) {
					setCompleted(StudyStatus.SUCCESSFUL_COMPLETION, "Completed a peer review.");

					// Provide scientific achievement to primary and collaborative researchers.
					provideCompletionAchievements();
				}
				else {
					setCompleted(StudyStatus.FAILED_COMPLETION, "Failed to complete a peer review.");
				}
			}
			break;

		default: // Nothing to do
			break;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ScientificStudy other = (ScientificStudy) obj;
		if (name == null) {
			return other.name == null;
		}
		else return name.equals(other.name);
	}

	/**
	 * Get the percentage [0 -> 1.0] of the completion of the current phase.
	 */
	public double getPhaseProgress() {
		switch (phase) {
			case PROPOSAL_PHASE:
				return (baseProposalTime > 0D) ? (proposalWorkTime / baseProposalTime) : 0D;
			case INVITATION_PHASE:
				return (maxCollaborators > 0) ? (double) collaborators.size() / maxCollaborators : 0D;
			case PAPER_PHASE: {
				double total = getTotalPrimaryPaperWorkTimeRequired() + (collaborators.size() * baseCollaborativePaperWritingTime);
				if (total <= 0D) return 0D;
				double completed = getPrimaryPaperWorkTimeCompleted()
						+ collaborators.values().stream().mapToDouble(v -> v.paperWorkTime).sum();
				return completed / total;
			}
			case RESEARCH_PHASE: {
				double total = getTotalPrimaryResearchWorkTimeRequired() + (collaborators.size() * baseCollaborativeResearchTime);
				if (total <= 0D) return 0D;
				double completed = getPrimaryResearchWorkTimeCompleted()
						+ collaborators.values().stream().mapToDouble(v -> v.reseachWorkTime).sum();
				return completed / total;
			}
			case PEER_REVIEW_PHASE:
				return (basePeerReviewTime > 0D) ? (getPeerReviewTimeCompleted() / basePeerReviewTime) : 0D;
			default:
				return 0D;
		}
	}
}
