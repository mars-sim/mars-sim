/**
 * Mars Simulation Project
 * MissionType.java
 * @version 3.1.0 2019-10-22
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Msg;

public enum MissionType {

	AREOLOGY				(Msg.getString("Mission.description.areologyFieldStudy")), //$NON-NLS-1$
	BIOLOGY					(Msg.getString("Mission.description.biologyFieldStudy")), //$NON-NLS-1$
	COLLECT_ICE				(Msg.getString("Mission.description.collectIce")), //$NON-NLS-1$
	COLLECT_REGOLITH		(Msg.getString("Mission.description.collectRegolith")), //$NON-NLS-1$
	EMERGENCY_SUPPLY		(Msg.getString("Mission.description.emergencySupply")), //$NON-NLS-1$
	
	EXPLORATION				(Msg.getString("Mission.description.exploration")), //$NON-NLS-1$
	METEOROLOGY				(Msg.getString("Mission.description.meteorologyFieldStudy")), //$NON-NLS-1$
	MINING					(Msg.getString("Mission.description.mining")), //$NON-NLS-1$
	RESCUE_SALVAGE_VEHICLE	(Msg.getString("Mission.description.rescueSalvageVehicle")), //$NON-NLS-1$
	TRADE					(Msg.getString("Mission.description.trade")), //$NON-NLS-1$
	
	TRAVEL_TO_SETTLEMENT	(Msg.getString("Mission.description.travelToSettlement")), //$NON-NLS-1$

	BUILDING_CONSTRUCTION	(Msg.getString("Mission.description.buildingConstructionMission")), //$NON-NLS-1$
	BUILDING_SALVAGE		(Msg.getString("Mission.description.buildingSalvageMission")), //$NON-NLS-1$
	;

	private String name;

	static Map<Integer, MissionType> lookup = null;

	/** hidden constructor. */
	private MissionType(String name) {
		this.name = name;
	}

	/** gives the internationalized name of this skill for display in user interface. */
	public String getName() {
		return this.name;
	}

//	public static MissionType valueOfIgnoreCase(String name) {
//		return MissionType.valueOf(name);
//	}
	

    public static MissionType lookup(int ordinal) {
        if (lookup == null) {
            // Late construction - not thread-safe.
            lookup = Arrays.stream(MissionType.values())
                    .collect(Collectors.toMap(s -> s.ordinal(), s -> s));
        }
        return lookup.get(ordinal);
    }
    
    public static MissionType lookup(String name) {
    	for (MissionType t : MissionType.values()) {
    		if (t.getName().equalsIgnoreCase(name))
    			return t;
    	}
    	return null;
    }
}