/*
 * Mars Simulation Project
 * StudyStatus.java
 * @date 2024-04-27
 * @author Barry Eavns
 */
 package com.mars_sim.core.science;

import com.mars_sim.core.tool.Msg;

/**
  * Represents the different states of a Scientific Study
  */
public enum StudyStatus {
    PROPOSAL_PHASE, INVITATION_PHASE,
    RESEARCH_PHASE, PAPER_PHASE,
    PEER_REVIEW_PHASE, CANCELLED,
    SUCCESSFUL_COMPLETION, FAILED_COMPLETION;
	
	private String name;

	/** hidden constructor. */
	private StudyStatus() {
        this.name = Msg.getStringOptional("StudyStatus", name());
    }

    /**
     * Get localised label
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Is the specified status an end/completed status?
     * @param status
     * @return
     */
    public static boolean isCompleted(StudyStatus status) {
        return ((status == StudyStatus.CANCELLED)
            || (status == StudyStatus.SUCCESSFUL_COMPLETION)
            || (status == StudyStatus.FAILED_COMPLETION));
    }
}
