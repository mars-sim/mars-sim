/**
 * Mars Simulation Project
 * TrainingType.java
 * @version 3.1.0 2017-10-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Msg;

public enum TrainingType {

	BIOETHICAL							(Msg.getString("TrainingType.bioethical")), //$NON-NLS-1$
	EXTREME_ENV_OPS						(Msg.getString("TrainingType.extremeEnvOps")), //$NON-NLS-1$
	NASA_DESERT_RATS					(Msg.getString("TrainingType.NASADesertRATS")), //$NON-NLS-1$
	SURVIVAL_TRAINING					(Msg.getString("TrainingType.survival")), //$NON-NLS-1$
	SCUBA_DIVING_MASTER					(Msg.getString("TrainingType.scubaDiving")), //$NON-NLS-1$
	
	FLIGHT_SAFETY						(Msg.getString("TrainingType.flightSafety")), //$NON-NLS-1$
	SEARCH_AND_RESCUE					(Msg.getString("TrainingType.searchAndRescue")), //$NON-NLS-1$
	MOUNTAINEERING_MASTER				(Msg.getString("TrainingType.mountaineering")), //$NON-NLS-1$
	AIRBORNE_AND_RANGER_SCHOOL			(Msg.getString("TrainingType.airborneRanger")), //$NON-NLS-1$
	HAUGHTON_MARS_GEOLOGICAL			(Msg.getString("TrainingType.haughtonMarsGeological")), //$NON-NLS-1$
	
	HALO_JUMPMASTER						(Msg.getString("TrainingType.HALOJump")), //$NON-NLS-1$
	MISHAP_INVESTIGATION				(Msg.getString("TrainingType.mishapInvestigation")), //$NON-NLS-1$
	MARS_500_C							(Msg.getString("TrainingType.mars500C")), //$NON-NLS-1$
	MARS_ANALOG_ENVIRONMENT				(Msg.getString("TrainingType.analogEnvironment")), //$NON-NLS-1$
	UNDERSEA_MISSION					(Msg.getString("TrainingType.underseaMission")), //$NON-NLS-1$
	
	MILITIARY_DEPLOYMENT				(Msg.getString("TrainingType.militaryDeployment")), //$NON-NLS-1$
	COMMERCIAL_PILOT					(Msg.getString("TrainingType.commercialPilot")), //$NON-NLS-1$
	ANTARCTICA_RESEARCH					(Msg.getString("TrainingType.antarcticaResearch")), //$NON-NLS-1$
	MARS_TWO_FINAL_100		 			(Msg.getString("TrainingType.marsTwoFinal100")) //$NON-NLS-1$
	;
	
	private String name;

	/** hidden constructor. */
	private TrainingType(String name) {
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
