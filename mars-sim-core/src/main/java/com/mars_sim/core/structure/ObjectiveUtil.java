/*
 * Mars Simulation Project
 * ObjectiveUTIL.java
 * @date 2024-02-09
 * @author Scott Davis
 */
package com.mars_sim.core.structure;

import com.mars_sim.core.goods.GoodsManager.CommerceType;

/**
 * Helper class for ObjectiveType.
 * Ideally this should be configured externally?
 */
public final  class ObjectiveUtil {
    
    /**
     * Prevent construction
     */
    private ObjectiveUtil() {}

    /**
     * What is the best Commerce type for this Objective
     * @param oType
     * @return
     */
	public static CommerceType toCommerce(ObjectiveType oType) {
		return switch(oType) {
			case BUILDERS_HAVEN -> CommerceType.BUILDING;
			case CROP_FARM -> CommerceType.CROP;
			case MANUFACTURING_DEPOT -> CommerceType.MANUFACTURING;
			case RESEARCH_CAMPUS -> CommerceType.RESEARCH;
			case TRANSPORTATION_HUB -> CommerceType.TRANSPORT;
			case TRADE_CENTER -> CommerceType.TRADE;
			case TOURISM -> CommerceType.TOURISM;
			default -> null;
		};
	}

    
	/**
	 * Gets the building type related to the settlement objective.
	 * @param oType Objective beign checked
	 * @return
	 */
	public static String getBuildingType(ObjectiveType oType) {
		return switch (oType) {
            case CROP_FARM -> "Inflatable Greenhouse";
		    // alternatives : "Fish Farm", "Large Greenhouse", "Inground Greenhouse"
		    case MANUFACTURING_DEPOT -> "Workshop"; 
		    // alternatives : "Manufacturing Shed", MD1, MD4
		    case RESEARCH_CAMPUS -> "Laboratory"; 
		    // alternatives : "Mining Lab", "Astronomy Observatory"
		    case TRANSPORTATION_HUB -> "Garage";
		    // alternatives : "Loading Dock Garage"
		    case TRADE_CENTER -> "Garage"; 
		    // alternatives : "Storage Shed", Future: "Markets" 
		    case TOURISM -> "Residential Quarters";
            default -> null;
        };
	}
}
