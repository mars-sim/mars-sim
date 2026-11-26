/**
 * Mars Simulation Project
 * MBTIType.java
 * @date 2026-11-17
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai;

import com.mars_sim.core.Named;
import com.mars_sim.core.tool.Msg;

/**
 * The Myers-Briggs Type Indicator (MBTI) personality type.
 * Each type is represented by a four-letter code indicating preferences in:
 * - Introversion (I) vs. Extraversion (E)
 * - Sensing (S) vs. Intuition (N)
 * - Thinking (T) vs. Feeling (F)
 * - Judging (J) vs. Perceiving (P)
 * This enum provides methods to check each preference.
 */
public enum MBTIType implements Named {

	ISTP,
	ISTJ,
	ISFP,
	ISFJ, 
	
	INTP,
	INTJ,
	INFP,
	INFJ,
	
	ESTP,
	ESTJ,
	ESFP,
	ESFJ,
	
	ENTP,
	ENTJ,
	ENFP,
	ENFJ;

	private final String name;
	
	private MBTIType() {
		this.name = Msg.getStringOptional("MBTIType", name());
	}

	@Override
	public String getName() {
		return name;
	}
	
	public boolean isIntrovert() {
		return name().charAt(0) == 'I';
	}

	public boolean isExtrovert() {
		return name().charAt(0) == 'E';
	}

	public boolean isSensor() {
		return name().charAt(1) == 'S';
	}

	public boolean isIntuitive() {
		return name().charAt(1) == 'N';
	}

	public boolean isThinker() {
		return name().charAt(2) == 'T';
	}

	public boolean isFeeler() {
		return name().charAt(2) == 'F';
	}

	public boolean isJudger() {
		return name().charAt(3) == 'J';
	}

	public boolean isPerceiver() {
		return name().charAt(3) == 'P';
	}
}