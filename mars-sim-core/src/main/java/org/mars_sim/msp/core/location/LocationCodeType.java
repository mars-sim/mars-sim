/**
 * Mars Simulation Project
 * LocationCodeType.java
 * @version 3.1.0 2018-11-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.location;

import java.util.HashMap;
import java.util.Map;

public enum LocationCodeType {

	SPACE 				(100_000),
	
	MARS 				(10_000),
	
	SETTLEMENT 			(1_000),
	REMOTE_STATION		(2_000),
	MOBILE_UNIT_4		(3_000),
	HALLWAY_TUNNEL_4	(4_000),
	ROAD_4				(5_000),
	CHANNEL_4			(6_000),
	LANDMARK			(7_000),
	DUST_STORM			(8_000),
	SETTLEMENT_VICINITY	(9_000),
	
	BUILDING 			(100),
	FIXED_STATION		(200),
	MOBILE_UNIT_3		(300),
	HALLWAY_TUNNEL_3	(400),
	ROAD_3 				(500),
	CHANNEL_3			(600),

	AIRLOCK				(10),
//	LAB					(20),
//	X					(30),
//	Y					(40),
//	Z	 				(50),
//	XX					(60),	
	
	EARTH 				(20_000),
	MOON 				(30_000),
	PHOBOS 				(40_000),
	DEIMOS 				(50_000),
	;

	private int code;
	
    private static Map<Integer, LocationCodeType> map = new HashMap<>();

	static {
		for (LocationCodeType type : LocationCodeType.values()) {
			map.put(type.code, type);
		}
	}

	/** hidden constructor. */
	private LocationCodeType(int code) {
		this.code = code;
	}
	
	public final int getCode() {
		return code;
	}

  
    public static LocationCodeType valueOf(int type) {
        return (LocationCodeType) map.get(type);
    }
    
//	@Override
//	public final String toString() {
//		return getName();
//	}
	
//	public static LocationCodeType getType(String s) {
//		return valueOf(s.toUpperCase().replace(" ", "_"));
//	}
}
