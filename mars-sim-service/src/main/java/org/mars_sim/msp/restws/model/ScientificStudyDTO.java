package org.mars_sim.msp.restws.model;

/**
 * DTO to flatten and present the most important properties of the ScientificStudy entity.
 */
public class ScientificStudyDTO {
	private String science;
	private String phase;
	private int difficultyLevel;
	private String completionState;
	private EntityReference primaryResearcher;
	
	public String getScience() {
		return science;
	}
	public void setScience(String science) {
		this.science = science;
	}
	public String getPhase() {
		return phase;
	}
	public void setPhase(String phase) {
		this.phase = phase;
	}
	public int getDifficultyLevel() {
		return difficultyLevel;
	}
	public void setDifficultyLevel(int difficultyLevel) {
		this.difficultyLevel = difficultyLevel;
	}
	public String getCompletionState() {
		return completionState;
	}
	public void setCompletionState(String completionState) {
		this.completionState = completionState;
	}
	public EntityReference getPrimaryResearcher() {
		return primaryResearcher;
	}
	public void setPrimaryResearcher(EntityReference primaryResearcher) {
		this.primaryResearcher = primaryResearcher;
	}
}
