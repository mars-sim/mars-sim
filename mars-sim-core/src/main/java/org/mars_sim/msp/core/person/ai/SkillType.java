/*
 * Mars Simulation Project
 * SkillType.java
 * @date 2022-07-16
 * @author stpa
 */

package org.mars_sim.msp.core.person.ai;

import java.util.Map;

import org.mars.sim.tools.Msg;

/**
 * The skill of a settler. A total of 19 types is available
 */
public enum SkillType {

// The following is the alphabetical listing of skills : 
//	
//	AREOLOGY			(Msg.getString("SkillType.areology")), //$NON-NLS-1$
//	ASTRONOMY			(Msg.getString("SkillType.astronomy")), //$NON-NLS-1$
//	BIOLOGY				(Msg.getString("SkillType.biology")), //$NON-NLS-1$
//	BOTANY				(Msg.getString("SkillType.botany")), //$NON-NLS-1$
//	CHEMISTRY			(Msg.getString("SkillType.chemistry")), //$NON-NLS-1$
//
//	COMPUTING			(Msg.getString("SkillType.computing")), //$NON-NLS-1$
//	CONSTRUCTION		(Msg.getString("SkillType.construction")), //$NON-NLS-1$
//	COOKING				(Msg.getString("SkillType.cooking")), //$NON-NLS-1$
//	EVA_OPERATIONS		(Msg.getString("SkillType.evaOps")), //$NON-NLS-1$
//	MANAGEMENT			(Msg.getString("SkillType.management")), //$NON-NLS-1$
//	
//	MATERIALS_SCIENCE	(Msg.getString("SkillType.materials")), //$NON-NLS-1$
//	MATHEMATICS			(Msg.getString("SkillType.mathematics")), //$NON-NLS-1$
//	MECHANICS			(Msg.getString("SkillType.mechanics")), //$NON-NLS-1$
//	MEDICINE			(Msg.getString("SkillType.medicine")), //$NON-NLS-1$
//	METEOROLOGY			(Msg.getString("SkillType.meteorology")), //$NON-NLS-1$
//
//	PHYSICS				(Msg.getString("SkillType.physics")), //$NON-NLS-1$
//	PILOTING			(Msg.getString("SkillType.piloting")), //$NON-NLS-1$
//  PSYCHOLOGY			(Msg.getString("SkillType.psychology")), //$NON-NLS-1$
//	REPORTING			(Msg.getString("SkillType.reporting")), //$NON-NLS-1$
//	TRADING				(Msg.getString("SkillType.trading")), //$NON-NLS-1$
	
	
// The following is organized in accordance with the subject matter affinity : 
	
	TRADING				(Msg.getString("SkillType.trading")), //$NON-NLS-1$
	CONSTRUCTION		(Msg.getString("SkillType.construction")), //$NON-NLS-1$
	MECHANICS			(Msg.getString("SkillType.mechanics")), //$NON-NLS-1$
	PILOTING			(Msg.getString("SkillType.piloting")), //$NON-NLS-1$
	EVA_OPERATIONS		(Msg.getString("SkillType.evaOps")), //$NON-NLS-1$

	PROSPECTING			(Msg.getString("SkillType.prospecting")), //$NON-NLS-1$
	METEOROLOGY			(Msg.getString("SkillType.meteorology")), //$NON-NLS-1$
	AREOLOGY			(Msg.getString("SkillType.areology")), //$NON-NLS-1$
	ASTRONOMY			(Msg.getString("SkillType.astronomy")), //$NON-NLS-1$
	CHEMISTRY			(Msg.getString("SkillType.chemistry")), //$NON-NLS-1$

	MATHEMATICS			(Msg.getString("SkillType.mathematics")), //$NON-NLS-1$
	COMPUTING			(Msg.getString("SkillType.computing")), //$NON-NLS-1$
	PHYSICS				(Msg.getString("SkillType.physics")), //$NON-NLS-1$
	MATERIALS_SCIENCE	(Msg.getString("SkillType.materials")), //$NON-NLS-1$

	COOKING				(Msg.getString("SkillType.cooking")), //$NON-NLS-1$
	BOTANY				(Msg.getString("SkillType.botany")), //$NON-NLS-1$
	BIOLOGY				(Msg.getString("SkillType.biology")), //$NON-NLS-1$
	MEDICINE			(Msg.getString("SkillType.medicine")), //$NON-NLS-1$
	PSYCHOLOGY			(Msg.getString("SkillType.psychology")), //$NON-NLS-1$	
	
	MANAGEMENT			(Msg.getString("SkillType.management")), //$NON-NLS-1$
	REPORTING			(Msg.getString("SkillType.reporting")) //$NON-NLS-1$
	;

	// Future skils:
	
	// See https://en.wikipedia.org/wiki/Areography 

	
	private String name;

	static Map<Integer, SkillType> lookup = null;

	/** hidden constructor. */
	private SkillType(String name) {
		this.name = name;
	}

	/** gives the internationalized name of this skill for display in user interface. */
	public String getName() {
		return this.name;
	}
}
