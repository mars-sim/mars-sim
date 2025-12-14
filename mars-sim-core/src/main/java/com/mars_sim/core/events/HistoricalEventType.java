/*
 * Mars Simulation Project	
 * HistoricalEventType.java
 * @date 2023-07-02
 * @author stpa
 */

package com.mars_sim.core.events;

import com.mars_sim.core.tool.Msg;

public enum HistoricalEventType {

	MALFUNCTION_PARTS_FAILURE	(HistoricalEventCategory.MALFUNCTION),
	MALFUNCTION_HUMAN_FACTORS	(HistoricalEventCategory.MALFUNCTION),
	MALFUNCTION_PROGRAMMING_ERROR	(HistoricalEventCategory.MALFUNCTION),
	MALFUNCTION_FIXED			(HistoricalEventCategory.MALFUNCTION),

	MEDICAL_CURED				(HistoricalEventCategory.MEDICAL),
	MEDICAL_STARTS				(HistoricalEventCategory.MEDICAL),
	MEDICAL_DEGRADES			(HistoricalEventCategory.MEDICAL),
	MEDICAL_RECOVERY			(HistoricalEventCategory.MEDICAL),
	MEDICAL_TREATED				(HistoricalEventCategory.MEDICAL),
	MEDICAL_DEATH				(HistoricalEventCategory.MEDICAL),
	MEDICAL_POSTMORTEM_EXAM		(HistoricalEventCategory.MEDICAL),
	MEDICAL_RESCUE				(HistoricalEventCategory.MEDICAL),
	MEDICAL_RESUSCITATED		(HistoricalEventCategory.MEDICAL),

	MISSION_START					(HistoricalEventCategory.MISSION),
	MISSION_JOINING					(HistoricalEventCategory.MISSION),
	MISSION_FINISH					(HistoricalEventCategory.MISSION),
	MISSION_EMERGENCY_DESTINATION	(HistoricalEventCategory.MISSION),
	MISSION_NOT_ENOUGH_RESOURCES		(HistoricalEventCategory.MISSION),
	MISSION_MEDICAL_EMERGENCY		(HistoricalEventCategory.MISSION),
	MISSION_EMERGENCY_BEACON_ON		(HistoricalEventCategory.MISSION),
	MISSION_EMERGENCY_BEACON_OFF	(HistoricalEventCategory.MISSION),
	MISSION_RENDEZVOUS				(HistoricalEventCategory.MISSION),
	MISSION_SALVAGE_VEHICLE			(HistoricalEventCategory.MISSION),
	MISSION_RESCUE_PERSON			(HistoricalEventCategory.MISSION),
	MISSION_ONLY_ONE_MEMBER			(HistoricalEventCategory.MISSION),
	MISSION_LEAD_NO_SHOW			(HistoricalEventCategory.MISSION),

	TASK_START			(HistoricalEventCategory.TASK),

	TRANSPORT_ITEM_CREATED		(HistoricalEventCategory.TRANSPORT),
	TRANSPORT_ITEM_CANCELLED	(HistoricalEventCategory.TRANSPORT),
	TRANSPORT_ITEM_LAUNCHED		(HistoricalEventCategory.TRANSPORT),
	TRANSPORT_ITEM_ARRIVED		(HistoricalEventCategory.TRANSPORT),
	TRANSPORT_ITEM_MODIFIED		(HistoricalEventCategory.TRANSPORT),

	HAZARD_ACTS_OF_GOD			(HistoricalEventCategory.HAZARD), 
	HAZARD_RADIATION_EXPOSURE	(HistoricalEventCategory.HAZARD);

	private String name;
	private HistoricalEventCategory category;

	/** hidden constructor. */
	private HistoricalEventType(HistoricalEventCategory category) {	
		this.name = Msg.getStringOptional("HistoricalEventType", name());
		this.category = category;
	}

	/**
	 * Language specific name for display in user interface.
	 * @return
	 */
	public final String getName() {
		return this.name;
	}

	public HistoricalEventCategory getCategory() {
		return this.category;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
