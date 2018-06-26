/**
 * Mars Simulation Project
 * HealthRiskType.java
 * @version 3.1.0 2017-10-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.health;

import org.mars_sim.msp.core.Msg;

public enum HealthRiskType {

	CATARACTS							(Msg.getString("HealthRiskType.cataracts")), //$NON-NLS-1$
	BRAIN_STEM_CELLS_DAMAGE				(Msg.getString("HealthRiskType.brainStemCellsDamage")), //$NON-NLS-1$
	CANCERS								(Msg.getString("HealthRiskType.cancers")), //$NON-NLS-1$
	CENTRAL_NERVOUS_SYSTEM_EFFECTS		(Msg.getString("HealthRiskType.cnsEffects")), //$NON-NLS-1$
	NEUROVESTIBULAR						(Msg.getString("HealthRiskType.neurovestibular")), //$NON-NLS-1$
	INFERTILITY							(Msg.getString("HealthRiskType.infertility")), //$NON-NLS-1$
	MUSCULAR_ATROPHY					(Msg.getString("HealthRiskType.muscularAtrophy")), //$NON-NLS-1$
	BONE_LOSS							(Msg.getString("HealthRiskType.boneLoss")), //$NON-NLS-1$
	CARDIOVASCULAR_STRESS				(Msg.getString("HealthRiskType.cardiovascular")), //$NON-NLS-1$
	IMMUNOLOGICAL_EFFECTS				(Msg.getString("HealthRiskType.immunological")), //$NON-NLS-1$
	;

	private String name;

	/** hidden constructor. */
	private HealthRiskType(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
