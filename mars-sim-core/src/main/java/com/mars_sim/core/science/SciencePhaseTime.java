/*
 * Mars Simulation Project
 * SciencePhaseTime.java
 * @date 2023-11-25
 * @author Barry Evans
 */
package com.mars_sim.core.science;

/**
 * Represents the different phases of a science project.
 */
public enum SciencePhaseTime {
    PROPOSAL, PRIMARY_RESEARCH, COLLABORATIVE_RESEARCH,
    PRIMARY_RESEARCHER_WRITING, COLLABORATOR_WRITING,
    PEER_REVIEW, PRIMARY_RESEARCHER_IDLE, COLLABORATOR_IDLE;
}
