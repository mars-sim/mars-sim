/**
 * Mars Simulation Project
 * ScientificStudyManager.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.science;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A class that keeps track of all scientific studies in the simulation.
 */
public class ScientificStudyManager // extends Thread
		implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final Logger logger = Logger.getLogger(ScientificStudyManager.class.getName());
	
	// Data members
	private List<ScientificStudy> studies;

	/**
	 * Constructor.
	 */
	public ScientificStudyManager() {
		// Methods are threadsafe
		studies = new ArrayList<>();
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

		String name = science.getName() + " #" + (studies.size() + 1);
		ScientificStudy study = new ScientificStudy(name, researcher, science, difficultyLevel);
		synchronized (studies) {
			studies.add(study);
		}

		logger.fine(researcher.getName() + " began writing proposal for new " + study.toString());

		return study;
	}

	/**
	 * Gets all ongoing scientific studies.
	 * 
	 * @return list of studies.
	 */
	public List<ScientificStudy> getOngoingStudies() {
		synchronized (studies) {
			return studies.stream().filter(s -> s.isCompleted() == false).collect(Collectors.toList());
		}
	}

	/**
	 * Gets all completed scientific studies, regardless of completion state.
	 * 
	 * @return list of studies.
	 */
	public List<ScientificStudy> getCompletedStudies() {
		return studies.stream().filter(ScientificStudy::isCompleted).collect(Collectors.toList());
	}

	/**
	 * Gets the number of all completed scientific studies where researcher was the primary
	 * researcher.
	 * 
	 * @param researcher the primary researcher.
	 * @return the number of studies.
	 */
	public int getNumCompletedPrimaryStudies(Person researcher) {
		return (int) studies.stream().filter(s -> s.isCompleted()
				&& s.getPrimaryResearcher().equals(researcher)).count();		
	}

	/**
	 * Gets all ongoing scientific studies where researcher was a collaborative
	 * researcher in a particular settlement.
	 * 
	 * @param settlement
	 * @return list of studies.
	 */
	private List<ScientificStudy> getOngoingCollaborativeStudies(Settlement settlement, ScienceType type) {
		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		List<ScientificStudy> result = new ArrayList<>();

		List<Person> pList = new ArrayList<>(settlement.getAllAssociatedPeople());

		for (Person p : pList) {
			for(ScientificStudy study : p.getCollabStudies()) {
				if (allSubject || (type == study.getScience())) {
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
		return (int) studies.stream().filter(s -> s.isCompleted()
				&& s.getCollaborativeResearchers().contains(researcher)).count();			
	}
	
	/**
	 * Gets all studies that have open invitations for collaboration for a
	 * researcher.
	 * 
	 * @param collaborativeResearcher the collaborative researcher.
	 * @return list of studies.
	 */
	public List<ScientificStudy> getOpenInvitationStudies(Person collaborativeResearcher) {
		List<ScientificStudy> result = new ArrayList<>();
		synchronized (studies) {
			Iterator<ScientificStudy> i = studies.iterator();
			while (i.hasNext()) {
				ScientificStudy study = i.next();
				if (study.getPhase().equals(ScientificStudy.INVITATION_PHASE)
						&& study.getInvitedResearchers().contains(collaborativeResearcher)
						&& !study.hasInvitedResearcherResponded(collaborativeResearcher)) {
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
		return studies.stream().filter(s -> (s.getPrimaryResearcher().equals(researcher)
				||  s.getCollaborativeResearchers().contains(researcher))).collect(Collectors.toList());	
	}

	/**
	 * Gets a list of all studies a settlement is primary for.
	 * 
	 * @param settlement the settlement.
	 * @return list of scientific studies.
	 */
	public List<ScientificStudy> getAllStudies(Settlement settlement) {
		return studies.stream().filter(s -> s.getPrimarySettlement().equals(settlement))
				.collect(Collectors.toList());		
	}

	private static double getPhaseScore(ScientificStudy ss) {
		switch (ss.getPhase()) {
		case ScientificStudy.PROPOSAL_PHASE:
			return .5;
		
		case ScientificStudy.INVITATION_PHASE:
			return 1.0;
		
		case ScientificStudy.RESEARCH_PHASE:
			return 1.5;
		
		case ScientificStudy.PAPER_PHASE:
			return 2.0;
			
		case ScientificStudy.PEER_REVIEW_PHASE:
			return 2.5;

		default:
			return 0;
		}
	}
	
	private static int getPhaseType(ScientificStudy ss) {
		switch(ss.getPhase()) {
		case ScientificStudy.PROPOSAL_PHASE:
			return 0;
		
		case ScientificStudy.INVITATION_PHASE:
			return 1;
		
		case ScientificStudy.RESEARCH_PHASE:
			return 2;

		case ScientificStudy.PAPER_PHASE:
			return 3;
			
		case ScientificStudy.PEER_REVIEW_PHASE:
			return 4;
		default:
			return 5;
		}
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
		
		if (type == null)
			allSubject = true;
		
		Iterator<ScientificStudy> i = getAllStudies(s).iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (allSubject || (type == study.getScience())) {
				// Study need counting
				if (study.getPhase().equals(ScientificStudy.COMPLETE_PHASE)) {
					// Score on the completion state
					switch(study.getCompletionState()) {
					case ScientificStudy.CANCELED:
						score += canceled;
						break;
					
					case ScientificStudy.FAILED_COMPLETION:
						score += failed;
						break;
						
					case ScientificStudy.SUCCESSFUL_COMPLETION:
						score += succeed;
						break;
					default:				
						break;
					}
				}
				else {
					// On going as primary reasearcher
					score += getPhaseScore(study);
				}
			}
		}
		

		List<ScientificStudy> list00 = getOngoingCollaborativeStudies(s, type);
		if (!list00.isEmpty()) {
			for (ScientificStudy ss : list00) {
				if (allSubject || type == ss.getScience()) {
					score += getPhaseScore(ss);
				}
			}
		}

		score = Math.round(score * 100.0) / 100.0;

		return score;
	}

	/**
	 * Computes the overall relationship score of a settlement.
	 * 		
     * 0 = succeed 	
	 * 1 = failed
     * 2 = canceled
	 * 3 = oPri
     * 4 = oCol
	 * 
	 * @param s Settlement
	 * @param type {@link ScienceType} if null, query all science types
	 * @return the score
	 */
	public int[] getNumScienceStudy(Settlement s, ScienceType type) {
		int[] array = new int[5];
		
		// 0 = succeed 	
		// 1 = failed
		// 2 = canceled
		// 3 = oPri
		// 4 = oCol

		boolean allSubject = false;
		if (type == null)
			allSubject = true;
		
		Iterator<ScientificStudy> i = getAllStudies(s).iterator();
		while (i.hasNext()) {
			ScientificStudy study = i.next();
			if (allSubject || (type == study.getScience())) {
				// Study need counting
				if (study.getPhase().equals(ScientificStudy.COMPLETE_PHASE)) {
					// Score on the completion state
					switch(study.getCompletionState()) {
					case ScientificStudy.CANCELED:
						array[2]++; 
						break;
					
					case ScientificStudy.FAILED_COMPLETION:
						array[1]++; 
						break;
						
					case ScientificStudy.SUCCESSFUL_COMPLETION:
						array[0]++;
						break;
					
					default:
						break;
					}				
					break;
				}
				else {
					// On going as primary reasearcher
					array[3]++;
				}
			}
		}
		
		// Have to search for collab Studies
		List<ScientificStudy> list00 = getOngoingCollaborativeStudies(s, type);
		if (!list00.isEmpty()) {
			for (ScientificStudy ss : list00) {
				if (allSubject || type == ss.getScience()) {
					int phase = getPhaseType(ss);
					if (phase != 5)		
						array[4]++;
				}
			}
		}

		return array;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		studies = null;
	}
}
