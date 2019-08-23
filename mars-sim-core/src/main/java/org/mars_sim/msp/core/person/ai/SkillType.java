/**
 * Mars Simulation Project
 * SkillType.java
 * @version 3.1.0 2018-10-14
 * @author stpa
 */

package org.mars_sim.msp.core.person.ai;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Msg;

public enum SkillType {

//	AREOLOGY			(Msg.getString("SkillType.areology")), //$NON-NLS-1$
//	ASTRONOMY			(Msg.getString("SkillType.astronomy")), //$NON-NLS-1$
//	BIOLOGY				(Msg.getString("SkillType.biology")), //$NON-NLS-1$
//	BOTANY				(Msg.getString("SkillType.botany")), //$NON-NLS-1$
//	CHEMISTRY			(Msg.getString("SkillType.chemistry")), //$NON-NLS-1$
//	
//	CONSTRUCTION		(Msg.getString("SkillType.construction")), //$NON-NLS-1$
//	COOKING				(Msg.getString("SkillType.cooking")), //$NON-NLS-1$
//	EVA_OPERATIONS		(Msg.getString("SkillType.evaOps")), //$NON-NLS-1$
//	MANAGEMENT			(Msg.getString("SkillType.management")), //$NON-NLS-1$
//	MATERIALS_SCIENCE	(Msg.getString("SkillType.materials")), //$NON-NLS-1$
//	
//	MATHEMATICS			(Msg.getString("SkillType.mathematics")), //$NON-NLS-1$
//	MECHANICS			(Msg.getString("SkillType.mechanics")), //$NON-NLS-1$
//	MEDICINE			(Msg.getString("SkillType.medicine")), //$NON-NLS-1$
//	METEOROLOGY			(Msg.getString("SkillType.meteorology")), //$NON-NLS-1$
//	PHYSICS				(Msg.getString("SkillType.physics")), //$NON-NLS-1$
//	
//	PILOTING			(Msg.getString("SkillType.piloting")), //$NON-NLS-1$
//	REPORTING			(Msg.getString("SkillType.reporting")), //$NON-NLS-1$
//	TRADING				(Msg.getString("SkillType.trading")), //$NON-NLS-1$
	
// If organizing the enum accordance to the subject matter affinity 
	TRADING				(Msg.getString("SkillType.trading")), //$NON-NLS-1$
	MATHEMATICS			(Msg.getString("SkillType.mathematics")), //$NON-NLS-1$
	CONSTRUCTION		(Msg.getString("SkillType.construction")), //$NON-NLS-1$
	MECHANICS			(Msg.getString("SkillType.mechanics")), //$NON-NLS-1$
	EVA_OPERATIONS		(Msg.getString("SkillType.evaOps")), //$NON-NLS-1$

	PILOTING			(Msg.getString("SkillType.piloting")), //$NON-NLS-1$
	ASTRONOMY			(Msg.getString("SkillType.astronomy")), //$NON-NLS-1$
	PHYSICS				(Msg.getString("SkillType.physics")), //$NON-NLS-1$
	MATERIALS_SCIENCE	(Msg.getString("SkillType.materials")), //$NON-NLS-1$
	METEOROLOGY			(Msg.getString("SkillType.meteorology")), //$NON-NLS-1$

	AREOLOGY			(Msg.getString("SkillType.areology")), //$NON-NLS-1$
	CHEMISTRY			(Msg.getString("SkillType.chemistry")), //$NON-NLS-1$
	COOKING				(Msg.getString("SkillType.cooking")), //$NON-NLS-1$
	BOTANY				(Msg.getString("SkillType.botany")), //$NON-NLS-1$
	BIOLOGY				(Msg.getString("SkillType.biology")), //$NON-NLS-1$

	MEDICINE			(Msg.getString("SkillType.medicine")), //$NON-NLS-1$
	MANAGEMENT			(Msg.getString("SkillType.management")), //$NON-NLS-1$
	REPORTING			(Msg.getString("SkillType.reporting")) //$NON-NLS-1$
	;

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

	public static SkillType valueOfIgnoreCase(String skillName) {
		return SkillType.valueOf(skillName.toUpperCase().replace(' ','_'));
	}
	

    public static SkillType lookup(int ordinal) {
        // Could just run through the array of values but I will us a Map.
        if (lookup == null) {
            // Late construction - not thread-safe.
            lookup = Arrays.stream(SkillType.values())
                    .collect(Collectors.toMap(s -> s.ordinal(), s -> s));
        }
        return lookup.get(ordinal);
    }
}
