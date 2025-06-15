/*
 * Mars Simulation Project
 * HealthRiskType.java
 * @date 2023-07-21
 * @author Manny Kung
 */

package com.mars_sim.core.person.health;

import com.mars_sim.core.tool.Msg;

public enum HealthRiskType {

	CATARACTS, BRAIN_STEM_CELLS_DAMAGE,CANCERS,CENTRAL_NERVOUS_SYSTEM_EFFECTS,
	NEUROVESTIBULAR,INFERTILITY,MUSCULAR_ATROPHY,BONE_LOSS,
	CARDIOVASCULAR_STRESS,IMMUNOLOGICAL_EFFECTS;

	private String name;

	/** hidden constructor. */
	private HealthRiskType() {
        this.name = Msg.getStringOptional("HealthRiskType", name());
	}

	public final String getName() {
		return this.name;
	}
}
