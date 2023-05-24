/*
 * Mars Simulation Project
 * RelationshipType.java
 * @date 2023-05-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.social;

/** Types of relationship. */
public enum RelationshipType {
	/** First impression if for meeting a new person. */
	FIRST_IMPRESSION, 
	/** For communicating with someone face-to-face locally. */
	FACE_TO_FACE_COMMUNICATION, 
	/** For communicating with someone remotely (voice chat, email, etc). */
	REMOTE_COMMUNICATION
}
