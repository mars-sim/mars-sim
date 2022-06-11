/*
 * Mars Simulation Project
 * RelationshipType.java
 * @date 2022-06-10
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.social;

/** Types of relationship. */
public enum RelationshipType {
	/** First impression if for meeting a new person. */
	FIRST_IMPRESSION, 
	/** Existing relationship is for meeting a person who is already known. */
	EXISTING_RELATIONSHIP, 
	/** Communication meeting is for meeting a new person remotely (email, etc). */
	COMMUNICATION_MEETING
}
