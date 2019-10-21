/**
 * Mars Simulation Project
 * EventType.java
 * @version 3.1.0 2017-10-03
 * @author stpa
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.Msg;

public enum EventType {

	MALFUNCTION_PARTS_FAILURE	(Msg.getString("EventType.malfunction.partsFailure")), //$NON-NLS-1$
	MALFUNCTION_HUMAN_FACTORS	(Msg.getString("EventType.malfunction.humanFactors")), //$NON-NLS-1$
	MALFUNCTION_PROGRAMMING_ERROR	(Msg.getString("EventType.malfunction.programmingError")), //$NON-NLS-1$
	MALFUNCTION_ACT_OF_GOD		(Msg.getString("EventType.malfunction.actOfGod")), //$NON-NLS-1$
	MALFUNCTION_FIXED			(Msg.getString("EventType.malfunction.fixed")), //$NON-NLS-1$

	MEDICAL_CURED		(Msg.getString("EventType.illness.cured")), //$NON-NLS-1$
	MEDICAL_STARTS		(Msg.getString("EventType.illness.starts")), //$NON-NLS-1$
	MEDICAL_DEGRADES	(Msg.getString("EventType.illness.degrades")), //$NON-NLS-1$
	MEDICAL_RECOVERY	(Msg.getString("EventType.illness.recovering")), //$NON-NLS-1$
	MEDICAL_TREATED		(Msg.getString("EventType.illness.treated")), //$NON-NLS-1$
	MEDICAL_DEATH		(Msg.getString("EventType.illness.dead")), //$NON-NLS-1$
	MEDICAL_RESCUE		(Msg.getString("EventType.medical.rescue")), //$NON-NLS-1$
	
	MISSION_START					(Msg.getString("EventType.mission.started")), //$NON-NLS-1$
	MISSION_JOINING					(Msg.getString("EventType.mission.joined")), //$NON-NLS-1$
	MISSION_FINISH					(Msg.getString("EventType.mission.finished")), //$NON-NLS-1$
	MISSION_DEVELOPMENT				(Msg.getString("EventType.mission.development")), //$NON-NLS-1$
	MISSION_EMERGENCY_DESTINATION	(Msg.getString("EventType.mission.emergencyDestination")), //$NON-NLS-1$
	MISSION_NOT_ENOUGH_RESOURCES	(Msg.getString("EventType.mission.notEnoughResource")), //$NON-NLS-1$
	MISSION_MEDICAL_EMERGENCY		(Msg.getString("EventType.mission.medicalEmergency")), //$NON-NLS-1$
	MISSION_EMERGENCY_BEACON_ON		(Msg.getString("EventType.mission.emergencyBeaconOn")), //$NON-NLS-1$
	MISSION_EMERGENCY_BEACON_OFF	(Msg.getString("EventType.mission.emergencyBeaconOff")), //$NON-NLS-1$
	MISSION_RENDEZVOUS				(Msg.getString("EventType.mission.rendezvous")), //$NON-NLS-1$
	MISSION_SALVAGE_VEHICLE			(Msg.getString("EventType.mission.salvageVehicle")), //$NON-NLS-1$
	MISSION_RESCUE_PERSON			(Msg.getString("EventType.mission.rescuePerson")), //$NON-NLS-1$

	TASK_START			(Msg.getString("EventType.task.starting")), //$NON-NLS-1$
	TASK_FINISH			(Msg.getString("EventType.task.finished")), //$NON-NLS-1$
	TASK_DEVELOPMENT	(Msg.getString("EventType.task.development")), //$NON-NLS-1$

	TRANSPORT_ITEM_CREATED		(Msg.getString("EventType.transportItem.created")), //$NON-NLS-1$
	TRANSPORT_ITEM_CANCELLED	(Msg.getString("EventType.transportItem.canceled")), //$NON-NLS-1$
	TRANSPORT_ITEM_LAUNCHED		(Msg.getString("EventType.transportItem.launched")), //$NON-NLS-1$
	TRANSPORT_ITEM_ARRIVED		(Msg.getString("EventType.transportItem.arrived")), //$NON-NLS-1$
	TRANSPORT_ITEM_MODIFIED		(Msg.getString("EventType.transportItem.modified")), //$NON-NLS-1$
	
	HAZARD_METEORITE_IMPACT		(Msg.getString("EventType.hazard.meteoriteImpact")), //$NON-NLS-1$ 
	HAZARD_RADIATION_EXPOSURE	(Msg.getString("EventType.hazard.radiationExposure")); //$NON-NLS-1$

	private String name;

	/** hidden constructor. */
	private EventType(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
	
	public static EventType int2enum(int ordinal) {
		return EventType.values()[ordinal];
	}
}
