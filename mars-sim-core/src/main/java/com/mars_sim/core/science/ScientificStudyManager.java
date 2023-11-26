/**
 * Mars Simulation Project
 * ScientificStudyManager.java
 * @date 2023-11-14
 * @author Scott Davis
 */
package com.mars_sim.core.science;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;

/**
 * A class that keeps track of all scientific studies in the simulation.
 */
public class ScientificStudyManager
		implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ScientificStudyManager.class.getName());
	
	// Data members
	/** The mission identifier. */
	private int identifier;
	/** The sol cache. */	
	private int solCache;
	/** The list of scientific study. */
	private List<ScientificStudy> studies = new ArrayList<>();

	
	/**
	 * Constructor.
	 */
	public ScientificStudyManager() {
		// Initialize data members
		identifier = 1;
		solCache = 1;
		// Methods are threadsafe
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
		
		ScientificStudy study = null;

		// Gets the scientific study string. Must be synchronised to prevent duplicate identifiers 
		// being assigned via different threads
		synchronized (studies) {
			int missionSol = Simulation.instance().getMasterClock().getMarsTime().getMissionSol();
			int id = 1;
			if (solCache != missionSol) {
				solCache = missionSol;
				identifier = 1;
			}
			else
				id = identifier++;
			String numString = missionSol + "-" + String.format("%03d", id);
			String name = science.getCode() + "-" + researcher.getAssociatedSettlement().getSettlementCode()
					+ "-" + numString;
			study = new ScientificStudy(id, name, researcher, science, difficultyLevel);
			studies.add(study);
		}

		logger.fine(researcher, "Began writing proposal for " + study.getName());

		return study;
	}

	/**
	 * Gets all ongoing scientific studies where researcher was a collaborative
	 * researcher in a particular settlement.
	 * 
	 * @param settlement
	 * @return list of studies.
	 */
	private List<ScientificStudy> getOngoingCollaborativeStudies(Settlement settlement, ScienceType type) {
		boolean allSubject = type == null;
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
	 * Gets the number of all completed scientific studies where researcher was the primary
	 * researcher.
	 * 
	 * @param researcher the primary researcher.
	 * @return the number of studies.
	 */
	public int getNumCompletedPrimaryStudies(Person researcher) {
		synchronized(studies) {
			return (int) studies.stream().filter(s -> s.isCompleted()
					&& s.getPrimaryResearcher().equals(researcher)).count();
		}	
	}

	/**
	 * Gets the number of all completed scientific studies where researcher was a collaborative
	 * researcher.
	 * 
	 * @param researcher the collaborative researcher.
	 * @return a number
	 */
	public int getNumCompletedCollaborativeStudies(Person researcher) {
		synchronized(studies) {
			return (int) studies.stream().filter(s -> s.isCompleted()
					&& s.getCollaborativeResearchers().contains(researcher)).count();
		}			
	}
	
	/**
	 * Gets all studies that have open invitations for collaboration for a
	 * researcher.
	 * 
	 * @param collaborativeResearcher the collaborative researcher.
	 * @return list of studies.
	 */
	public List<ScientificStudy> getOpenInvitationStudies(Person collaborativeResearcher) {
		synchronized (studies) {
			return studies.stream()
						.filter(s -> (s.getPhase().equals(ScientificStudy.INVITATION_PHASE)
								&& s.getInvitedResearchers().contains(collaborativeResearcher)
								&& !s.hasInvitedResearcherResponded(collaborativeResearcher)))
						.toList();	
		}
	}

	/**
	 * Gets studies according to a completed filter
	 * @param completed Is the Study completed
	 * @return list of studies.
	 */
	public List<ScientificStudy> getAllStudies(boolean completed) {
		synchronized (studies) {
			return studies.stream().filter(s -> s.isCompleted() == completed).toList();
		}
	}

	/**
	 * Gets a list of all studies a researcher is involved with.
	 * 
	 * @param researcher the researcher.
	 * @return list of scientific studies.
	 */
	public List<ScientificStudy> getAllStudies(Person researcher) {
		List<ScientificStudy> results = new ArrayList<>();
		results.addAll(researcher.getCollabStudies());
		if (researcher.getStudy() != null) {
			results.add(researcher.getStudy());
		}
		return results;
	}

	/**
	 * Gets a list of all studies a settlement is primary for.
	 * 
	 * @param settlement the settlement.
	 * @return list of scientific studies.
	 */
	public List<ScientificStudy> getAllStudies(Settlement settlement) {
		synchronized(studies) {
			return studies.stream().filter(s -> settlement.equals(s.getPrimarySettlement()))
					.toList();	
		}	
	}

	private static double getPhaseScore(ScientificStudy ss) {
		return switch (ss.getPhase()) {
			case ScientificStudy.PROPOSAL_PHASE -> .5;
			case ScientificStudy.INVITATION_PHASE -> 1.0;
			case ScientificStudy.RESEARCH_PHASE -> 1.5;
			case ScientificStudy.PAPER_PHASE -> 2.0;
			case ScientificStudy.PEER_REVIEW_PHASE -> 2.5;
			default -> 0;
		};
	}
	
	private static int getPhaseType(ScientificStudy ss) {
		return switch(ss.getPhase()) {
			case ScientificStudy.PROPOSAL_PHASE -> 0;
			case ScientificStudy.INVITATION_PHASE -> 1;
			case ScientificStudy.RESEARCH_PHASE -> 2;
			case ScientificStudy.PAPER_PHASE -> 3;
			case ScientificStudy.PEER_REVIEW_PHASE -> 4;
			default -> 5;
		};
	}
	
	/**
	 * Computes the overall relationship score of a settlement
	 * 
	 * @param s Settlement
	 * @param type {@link ScienceType} if null, query all science types
	 * @return the score
	 */
	public double getScienceScore(Settlement s, ScienceType type) {
		boolean allSubject = (type == null);

        double score = 0;
		double succeed = 3;	
		double failed = 1;
		double canceled = 0.5;
		
		for(ScientificStudy study : getAllStudies(s)) {
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
		
		for (ScientificStudy ss : getOngoingCollaborativeStudies(s, type)) {
			if (allSubject || type == ss.getScience()) {
				score += getPhaseScore(ss);
			}
		}

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

		boolean allSubject = type == null;
		for(ScientificStudy study : getAllStudies(s)) {
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
		for (ScientificStudy ss : getOngoingCollaborativeStudies(s, type)) {
			if (allSubject || type == ss.getScience()) {
				int phase = getPhaseType(ss);
				if (phase != 5)		
					array[4]++;
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
