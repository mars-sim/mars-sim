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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.Conversion;

/**
 * A class that keeps track of all scientific studies in the simulation.
 */
public class ScientificStudyManager // extends Thread
		implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final Logger logger = Logger.getLogger(ScientificStudyManager.class.getName());
	private static final String loggerName = logger.getName();
	private static final String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	// Data members
	private List<ScientificStudy> studies;

	private static Simulation sim = Simulation.instance();
	private static MarsClock marsClock = sim.getMasterClock().getMarsClock();
	private static UnitManager unitManager = sim.getUnitManager();
	private static ScienceConfig scienceConfig = SimulationConfig.instance().getScienceConfig();
	
	/**
	 * Constructor.
	 */
	public ScientificStudyManager() {
		studies = new ArrayList<ScientificStudy>();
	}

	/**
	 * Creates a new scientific study.
	 * 
	 * @param researcher      the primary researcher.
	 * @param science         the primary field of science.
	 * @param difficultyLevel the difficulty level of the study.
	 * @return the created study.
	 */
	public ScientificStudy createScientificStudy(Person researcher, ScienceType science, int difficultyLevel) {
		if (researcher == null)
			throw new IllegalArgumentException("Researcher cannot be null");
		if (science == null)
			throw new IllegalArgumentException("Science cannot be null");
		if (difficultyLevel < 0)
			throw new IllegalArgumentException("difficultyLevel must be positive value");

		ScientificStudy study = new ScientificStudy(researcher, science, difficultyLevel);
		studies.add(study);

		logger.fine(researcher.getName() + " began writing proposal for new " + study.toString());

		return study;
	}

	/**
	 * Gets all ongoing scientific studies.
	 * 
	 * @return list of studies.
	 */
	public List<ScientificStudy> getOngoingStudies() {
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (!study.isCompleted())
				result.add(study);
		}
		return result;
	}

	/**
	 * Gets all completed scientific studies, regardless of completion state.
	 * 
	 * @return list of studies.
	 */
	public List<ScientificStudy> getCompletedStudies() {
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (study.isCompleted())
				result.add(study);
		}
		return result;
	}

	/**
	 * Gets all successfully completed scientific studies.
	 * 
	 * @return list of studies.
	 */
	public List<ScientificStudy> getSuccessfulStudies() {
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (study.isCompleted() && study.getCompletionState().equals(ScientificStudy.SUCCESSFUL_COMPLETION))
				result.add(study);
		}
		return result;
	}

	/**
	 * Gets all failed completed scientific studies.
	 * 
	 * @return list of studies.
	 */
	public List<ScientificStudy> getFailedStudies() {
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (study.isCompleted() && study.getCompletionState().equals(ScientificStudy.FAILED_COMPLETION))
				result.add(study);
		}
		return result;
	}

	/**
	 * Gets all canceled scientific studies.
	 * 
	 * @return list of studies.
	 */
	public List<ScientificStudy> getCanceledStudies() {
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (study.isCompleted() && study.getCompletionState().equals(ScientificStudy.CANCELED))
				result.add(study);
		}
		return result;
	}

	/**
	 * Gets the researcher's ongoing primary research scientific study, if any.
	 * 
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
	 * Gets the number of all completed scientific studies where researcher was the primary
	 * researcher.
	 * 
	 * @param researcher the primary researcher.
	 * @return the number of studies.
	 */
	public int getNumCompletedPrimaryStudies(Person researcher) {
		int result = 0;
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (study.isCompleted() && (study.getPrimaryResearcher().equals(researcher)))
				result++;
		}
		return result;
	}
	
	/**
	 * Gets all completed scientific studies where researcher was the primary
	 * researcher.
	 * 
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
	 * Gets all ongoing scientific studies where researcher is a collaborative
	 * researcher.
	 * 
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
	 * Gets all ongoing scientific studies where researcher was a collaborative
	 * researcher in a particular settlement.
	 * 
	 * @param settlement
	 * @return list of studies.
	 */
	public List<ScientificStudy> getOngoingCollaborativeStudies(Settlement settlement, ScienceType type) {
		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();

		List<Person> pList = new ArrayList<>(settlement.getAllAssociatedPeople());

		for (Person p : pList) {
			Iterator<ScientificStudy> i = studies.iterator();
			while (i.hasNext()) {
				ScientificStudy study = i.next();
				if (allSubject || type == study.getScience()) {
					if (!study.isCompleted() && (study.getCollaborativeResearchers().containsKey(p.getIdentifier())))
						result.add(study);
				}
			}
		}
		return result;
	}

	/**
	 * Gets the number of all completed scientific studies where researcher was a collaborative
	 * researcher.
	 * 
	 * @param researcher the collaborative researcher.
	 * @return a number
	 */
	public int getNumCompletedCollaborativeStudies(Person researcher) {
		int result = 0;
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (study.isCompleted() && (study.getCollaborativeResearchers().containsKey(researcher.getIdentifier())))
				result++;
		}
		return result;
	}
	
	/**
	 * Gets all completed scientific studies where researcher was a collaborative
	 * researcher.
	 * 
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
	 * Gets all completed scientific studies where researcher was a collaborative
	 * researcher in a particular settlement.
	 * 
	 * @param settlement
	 * @return list of studies.
	 */
	public List<ScientificStudy> getCompletedCollaborativeStudies(Settlement settlement, ScienceType type) {
		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();

		List<Person> pList = new ArrayList<>(settlement.getAllAssociatedPeople());

		for (Person p : pList) {
			Iterator<ScientificStudy> i = studies.iterator();
			while (i.hasNext()) {
				ScientificStudy study = i.next();
				if (allSubject || type == study.getScience()) {
					if (study.isCompleted() && (study.getCollaborativeResearchers().containsKey(p.getIdentifier())))
						result.add(study);
				}
			}
		}
		return result;
	}

	/**
	 * Gets all ongoing scientific studies at a primary research settlement.
	 * 
	 * @param settlement the primary research settlement.
	 * @return list of studies.
	 */
	public List<ScientificStudy> getOngoingPrimaryStudies(Settlement settlement, ScienceType type) {
		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (allSubject || type == study.getScience()) {
				if (!study.isCompleted() && settlement.equals(study.getPrimarySettlement()))
					result.add(study);
			}
		}
		return result;
	}

	/**
	 * Gets all completed scientific studies at a primary research settlement.
	 * 
	 * @param settlement the primary research settlement.
	 * @return list of studies.
	 */
	public List<ScientificStudy> getCompletedPrimaryStudies(Settlement settlement, ScienceType type) {
		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (allSubject || type == study.getScience()) {
				if (study.isCompleted() && settlement.equals(study.getPrimarySettlement()))
					result.add(study);
			}
		}
		return result;
	}

	/**
	 * Gets all failed scientific studies at a primary research settlement.
	 * 
	 * @param settlement the primary research settlement.
	 * @return list of studies.
	 */
	public List<ScientificStudy> getAllFailedStudies(Settlement settlement, ScienceType type) {
		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (allSubject || type == study.getScience()) {
				if (study.isCompleted() && study.getCompletionState().equals(ScientificStudy.FAILED_COMPLETION)
						&& settlement.equals(study.getPrimarySettlement()))
					result.add(study);
			}
		}
		return result;
	}

	/**
	 * Gets all successful scientific studies at a primary research settlement.
	 * 
	 * @param settlement the primary research settlement.
	 * @return list of studies.
	 */
	public List<ScientificStudy> getAllSuccessfulStudies(Settlement settlement, ScienceType type) {
		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (allSubject || type == study.getScience()) {
				if (study.isCompleted() && study.getCompletionState().equals(ScientificStudy.SUCCESSFUL_COMPLETION)
						&& settlement.equals(study.getPrimarySettlement()))
					result.add(study);
			}
		}
		return result;
	}

	/**
	 * Gets all canceled scientific studies at a primary research settlement.
	 * 
	 * @param settlement the primary research settlement.
	 * @return list of studies.
	 */
	public List<ScientificStudy> getAllCanceledStudies(Settlement settlement, ScienceType type) {
		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();
		Iterator<ScientificStudy> i = studies.iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (allSubject || type == study.getScience()) {
				if (study.isCompleted() && study.getCompletionState().equals(ScientificStudy.CANCELED)
						&& settlement.equals(study.getPrimarySettlement()))
					result.add(study);
			}
		}
		return result;
	}
	
	/**
	 * Gets all studies that have open invitations for collaboration for a
	 * researcher.
	 * 
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
	 * 
	 * @param researcher the researcher.
	 * @return list of scientific studies.
	 */
	public List<ScientificStudy> getAllStudies(Person researcher) {
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();

		// Add ongoing primary study.
		ScientificStudy primaryStudy = getOngoingPrimaryStudy(researcher);
		if (primaryStudy != null)
			result.add(primaryStudy);

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
	 * 
	 * @param settlement the settlement.
	 * @return list of scientific studies.
	 */
	public List<ScientificStudy> getAllStudies(Settlement settlement) {
		List<ScientificStudy> result = new ArrayList<ScientificStudy>();

		// Add any ongoing primary studies.
		List<ScientificStudy> primaryStudies = getOngoingPrimaryStudies(settlement, null);
		result.addAll(primaryStudies);

		// Add any completed primary studies.
		List<ScientificStudy> completedPrimaryStudies = getCompletedPrimaryStudies(settlement, null);
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
				Person person = study.getPrimaryResearcher();
				String name = person.getName();
				
				// Check if primary researcher has died.
				if (isPrimaryResearcherDead(study)) {
					study.setCompleted(ScientificStudy.CANCELED);
					logger.fine(study.toString() + " was canceled due to primary researcher's death.");
					LogConsolidated.log(Level.INFO, 0, sourceName,
							"[" + person.getLocationTag().getLocale() + "] " 
							+ "Due to " + name + "'s death, the " + study.toString()
							+ " study was abandoned.");
					continue;
				}

				Map<Integer, Person> lookupPerson = unitManager.getLookupPerson();
				if (lookupPerson == null) {
					lookupPerson = unitManager.getLookupPerson();
				}
				
				// Check if collaborators have died.
				Iterator<Integer> j = study.getCollaborativeResearchers().keySet().iterator();
				while (j.hasNext()) {
					Integer id = j.next();
					Person collaborator = lookupPerson.get(id);//unitManager.getPersonByID(id);
					if (collaborator.getPhysicalCondition().isDead()) {
						String genderStr = GenderType.getPossessivePronoun(collaborator.getGender());
						study.removeCollaborativeResearcher(collaborator);
						LogConsolidated.log(Level.INFO, 0, sourceName,
								"[" + collaborator.getLocationTag().getLocale() + "] " 
								+ collaborator.getName() + " (a collaborator) was removed in the " + study.toString()
								+ " study since " + genderStr + " has passed away.");
					}
				}

				if (study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
					// Check if proposal work time is completed, then move to invitation phase.
					if (study.getProposalWorkTimeCompleted() >= study.getTotalProposalWorkTimeRequired()) {
						LogConsolidated.log(Level.INFO, 0, sourceName,
								"[" + person.getLocationTag().getLocale() + "] " 
								+ name  +  " finished writing proposal for the "
								+ study.toString() + " study and was starting to invite collaborative researchers");
						// Picks research topics 
						pickTopics(study);
						study.setPhase(ScientificStudy.INVITATION_PHASE);
						continue;
					}
				} else if (study.getPhase().equals(ScientificStudy.INVITATION_PHASE)) {
					// Clean out any dead research invitees.
					study.cleanResearchInvitations();

					boolean phaseEnded = false;
					if (study.getCollaborativeResearchers().size() < study.getMaxCollaborators()) {
						int availableInvitees = ScientificStudyUtil.getAvailableCollaboratorsForInvite(study).size();
						int openResearchInvitations = study.getNumOpenResearchInvitations();
						if ((availableInvitees + openResearchInvitations) == 0)
							phaseEnded = true;
					} else
						phaseEnded = true;

					if (phaseEnded) {
						LogConsolidated.log(Level.INFO, 0, sourceName,
								"[" + person.getLocationTag().getLocale() + "] " 
								+ name  + " ended the invitation phase on the " + study.toString() + " study with "
								+ study.getCollaborativeResearchers().size() 
								+ " collaborative researchers and started the research work phase.");
						study.setPhase(ScientificStudy.RESEARCH_PHASE);

						// Set initial research work time for primary and all collaborative researchers.
						study.addPrimaryResearchWorkTime(0D);
						Iterator<Integer> k = study.getCollaborativeResearchers().keySet().iterator();
						while (k.hasNext())
							study.addCollaborativeResearchWorkTime(lookupPerson.get(k.next()), 0D);

						continue;
					}
				} else if (study.getPhase().equals(ScientificStudy.RESEARCH_PHASE)) {

					if (study.isAllResearchCompleted()) {
						study.setPhase(ScientificStudy.PAPER_PHASE);
						LogConsolidated.log(Level.INFO, 0, sourceName,
								"[" + person.getLocationTag().getLocale() + "] " 
								+ name + " finished the research work on the " 
								+ study.toString() + " study and was starting to compile data results.");
						continue;
					} else {

						// Check primary researcher downtime.
						if (!study.isPrimaryResearchCompleted()) {
							MarsClock lastPrimaryWork = study.getLastPrimaryResearchWorkTime();
							if ((lastPrimaryWork != null) && MarsClock.getTimeDiff(marsClock,
									lastPrimaryWork) >study.getPrimaryWorkDownTimeAllowed()) {
								study.setCompleted(ScientificStudy.CANCELED);
								LogConsolidated.log(Level.INFO, 0, sourceName,
										"[" + person.getLocationTag().getLocale() + "] " 
										+ name + " abandoned the "
										+ study.toString()
										+ " study due to lack of primary researcher participation.");
								continue;
							}
						}

						// Check each collaborator for downtime.
						Iterator<Integer> l = study.getCollaborativeResearchers().keySet().iterator();
						while (l.hasNext()) {
							Person researcher = lookupPerson.get(l.next());
							if (!study.isCollaborativeResearchCompleted(researcher)) {
								MarsClock lastCollaborativeWork = study
										.getLastCollaborativeResearchWorkTime(researcher);
								if ((lastCollaborativeWork != null) && MarsClock.getTimeDiff(marsClock,
										lastCollaborativeWork) > study.getCollaborativeWorkDownTimeAllowed()) {
									study.removeCollaborativeResearcher(researcher);
									LogConsolidated.log(Level.INFO, 0, sourceName,
											"[" + researcher.getLocationTag().getLocale() + "] " 
											+ researcher.getName() + " (a collaborator) was removed in the " 
											+ study.toString()
											+ " study due to lack of participation.");
								}
							}
						}
					}
				} else if (study.getPhase().equals(ScientificStudy.PAPER_PHASE)) {

					if (study.isAllPaperWritingCompleted()) {
						study.setPhase(ScientificStudy.PEER_REVIEW_PHASE);
						study.startingPeerReview(); 
						LogConsolidated.log(Level.INFO, 0, sourceName,
								"[" + person.getLocationTag().getLocale() + "] " + name 
								+ " had compiled data results for "
								+ Conversion.capitalize(study.toString()) + " and was starting to do the peer review.");
						continue;
					}
				} else if (study.getPhase().equals(ScientificStudy.PEER_REVIEW_PHASE)) {

					if (study.isPeerReviewTimeFinished()) {
						// Determine results of peer review.
						if (ScientificStudyUtil.determinePeerReviewResults(study)) {
							study.setCompleted(ScientificStudy.SUCCESSFUL_COMPLETION);

							// Provide scientific achievement to primary and collaborative researchers.
							ScientificStudyUtil.provideCompletionAchievements(study);
							LogConsolidated.log(Level.INFO, 0, sourceName,
									"[" + person.getLocationTag().getLocale() + "] " 
										+ name + " completed a peer review on the " 
									+ study.toString() + " study successfully.");
						} else {
							study.setCompleted(ScientificStudy.FAILED_COMPLETION);
							LogConsolidated.log(Level.INFO, 0, sourceName,
									"[" + person.getLocationTag().getLocale() + "] " 
									+ name + " failed to complete a peer review on the " 
									+ study.toString() + " study.");
						}
					}
				}
			}
		}
	}

	private void pickTopics(ScientificStudy study) {
		ScienceType type = study.getScience();
		List<String> topics = new ArrayList<>();
		topics.add(getTopic(type));
		study.saveTopics(type, topics);//new ArrayList<String>().add(type.getTopic(type))));
	}
	
	/**
	 * Gets a topic
	 * 
	 * @param type  {@link ScienceType}
	 * @return a string
	 */
	public String getTopic(ScienceType type) {
		return scienceConfig.getATopic(type);
	}
	
	/**
	 * Checks if a study's primary researcher is dead.
	 * 
	 * @param study the scientific study.
	 * @return true if primary researcher dead.
	 */
	private boolean isPrimaryResearcherDead(ScientificStudy study) {
		Person primaryResearcher = study.getPrimaryResearcher();
		return primaryResearcher.getPhysicalCondition().isDead();
	}

	public double getPhaseScore(ScientificStudy ss) {
		if (ss.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
			return .5;
		}
		
		else if (ss.getPhase().equals(ScientificStudy.INVITATION_PHASE)) {
			return 1.0;
		}
		
		if (ss.getPhase().equals(ScientificStudy.RESEARCH_PHASE)) {
			return 1.5;
		}
		
		else if (ss.getPhase().equals(ScientificStudy.PAPER_PHASE)) {
			return 2.0;
		}
		
		else if (ss.getPhase().equals(ScientificStudy.PEER_REVIEW_PHASE)) {
			return 2.5;
		}
		
		return 0;
	}
	
	public int getPhaseType(ScientificStudy ss) {
		if (ss.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
			return 0;
		}
		
		else if (ss.getPhase().equals(ScientificStudy.INVITATION_PHASE)) {
			return 1;
		}
		
		if (ss.getPhase().equals(ScientificStudy.RESEARCH_PHASE)) {
			return 2;
		}
		
		else if (ss.getPhase().equals(ScientificStudy.PAPER_PHASE)) {
			return 3;
		}
		
		else if (ss.getPhase().equals(ScientificStudy.PEER_REVIEW_PHASE)) {
			return 4;
		}
		
		return 5;
	}
	
	/**
	 * Computes the overall relationship score of a settlement
	 * 
	 * @param s Settlement
	 * @param type {@link ScienceType} if null, query all science types
	 * @return the score
	 */
	public double getScienceScore(Settlement s, ScienceType type) {
		boolean allSubject = false;
		if (type == null)
			allSubject = true;

		double score = 0;
		
		double succeed = 3;	
		double failed = 1;
		double canceled = 0.5;
		double oPri ;
		double oCol ;
		
//		List<ScientificStudy> list0 = getCompletedPrimaryStudies(s);
//		if (!list0.isEmpty()) {
//			for (ScientificStudy ss : list0) {
//				if (allSubject || type == ss.getScience()) {				
//					score += priCompleted;
//				}
//			}
//		}

		List<ScientificStudy> list00 = getOngoingCollaborativeStudies(s, type);
		if (!list00.isEmpty()) {
			for (ScientificStudy ss : list00) {
				if (allSubject || type == ss.getScience()) {
					score += getPhaseScore(ss);
				}
			}
		}
		
		List<ScientificStudy> list01 = getOngoingPrimaryStudies(s, type);
		if (!list01.isEmpty()) {
			for (ScientificStudy ss : list01) {
				if (allSubject || type == ss.getScience()) {
					score += getPhaseScore(ss);
				}
			}
		}

		List<ScientificStudy> list02 = getAllFailedStudies(s, type);
		if (!list02.isEmpty()) {
			for (ScientificStudy ss : list02) {
				if (allSubject || type == ss.getScience()) {
					score += failed;
				}
			}
		}

		List<ScientificStudy> list03 = getAllCanceledStudies(s, type);
		if (!list03.isEmpty()) {
			for (ScientificStudy ss : list03) {
				if (allSubject || type == ss.getScience()) {
					score += canceled;
				}
			}
		}
		
		List<ScientificStudy> list04 = this.getAllSuccessfulStudies(s, type);
		if (!list04.isEmpty()) {
			for (ScientificStudy ss : list04) {
				if (allSubject || type == ss.getScience()) {				
					score += succeed;
				}
			}
		}
		
		
//		List<ScientificStudy> list05 = getCompletedCollaborativeStudies(s);
//		if (!list05.isEmpty()) {
//			for (ScientificStudy ss : list05) {
//				if (allSubject || type == ss.getScience()) {
//					score += colCompleted;
//				}
//			}
//		}

		score = Math.round(score * 100.0) / 100.0;

		return score;
	}

	/**
	 * Computes the overall relationship score of a settlement
	 * 
	 * @param s Settlement
	 * @param type {@link ScienceType} if null, query all science types
	 * @return the score
	 */
	public double[] getNumScienceStudy(Settlement s, ScienceType type) {
		double[] array = new double[5];
		
		// 0 = succeed 	
		// 1 = failed
		// 2 = canceled
		// 3 = oPri
		// 4 = oCol

		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		
//		List<ScientificStudy> list0 = getCompletedPrimaryStudies(s);
//		if (!list0.isEmpty()) {
//			for (ScientificStudy ss : list0) {
//				if (allSubject || type == ss.getScience()) {				
//					score += priCompleted;
//				}
//			}
//		}

		List<ScientificStudy> list00 = getOngoingCollaborativeStudies(s, type);
		if (!list00.isEmpty()) {
			for (ScientificStudy ss : list00) {
				if (allSubject || type == ss.getScience()) {
					int phase = getPhaseType(ss);
					if (phase != 5)		
						array[4]++; // getPhaseScore(ss);
				}
			}
		}
		
		List<ScientificStudy> list01 = getOngoingPrimaryStudies(s, type);
		if (!list01.isEmpty()) {
			for (ScientificStudy ss : list01) {
				if (allSubject || type == ss.getScience()) {
					int phase = getPhaseType(ss);
					if (phase != 5)		
						array[3]++; // getPhaseScore(ss);
				}
			}
		}

		List<ScientificStudy> list02 = getAllFailedStudies(s, type);
		if (!list02.isEmpty()) {
			for (ScientificStudy ss : list02) {
				if (allSubject || type == ss.getScience()) {
					array[1]++; //score += failed;
				}
			}
		}

		List<ScientificStudy> list03 = getAllCanceledStudies(s, type);
		if (!list03.isEmpty()) {
			for (ScientificStudy ss : list03) {
				if (allSubject || type == ss.getScience()) {
					array[2]++; //score += canceled;
				}
			}
		}
		
		List<ScientificStudy> list04 = this.getAllSuccessfulStudies(s, type);
		if (!list04.isEmpty()) {
			for (ScientificStudy ss : list04) {
				if (allSubject || type == ss.getScience()) {				
					array[0]++;//score += succeed;
				}
			}
		}
		
		
//		List<ScientificStudy> list05 = getCompletedCollaborativeStudies(s);
//		if (!list05.isEmpty()) {
//			for (ScientificStudy ss : list05) {
//				if (allSubject || type == ss.getScience()) {
//					score += colCompleted;
//				}
//			}
//		}

		return array;
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