/*
 * Mars Simulation Project
 * GoodType.java
 * @date 2022-06-25
 * @author Manny Kung
 */
package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.Msg;

/**
 * The GoodType enum class is used for distinguishing between various type of goods
 */
public enum GoodType {
	
	// For Amount Resources
	
	// food resources
	ANIMAL				(Msg.getString("FoodType.animal")), //$NON-NLS-1$ 
	CROP				(Msg.getString("FoodType.crop")), //$NON-NLS-1$ 
	DERIVED				(Msg.getString("FoodType.derived")), //$NON-NLS-1$
	INSECT				(Msg.getString("FoodType.insect")), //$NON-NLS-1$ 
	OIL					(Msg.getString("FoodType.oil")), //$NON-NLS-1$ 
	ORGANISM			(Msg.getString("FoodType.organism")), //$NON-NLS-1$ 
	SOY_BASED			(Msg.getString("FoodType.soyBased")), //$NON-NLS-1$ 
	TISSUE				(Msg.getString("FoodType.tissue")), //$NON-NLS-1$ 

	// non-food resources
	GEMSTONE	("gemstone"),
	MEDICAL 	("medical"),
	MINERAL		("mineral"),
	ORE			("ore"),
	ROCK		("rock"),
	REGOLITH	("regolith"),
	WASTE		("waste"),
	
	// For Parts
	ATTACHMENT		("Attachment"),
    COMPOUND		("Compound"),
    CONSTRUCTION	("Construction"),
    ELECTRICAL		("Electrical"),
    ELECTRONIC		("Electronic"),
    KITCHEN			("Kitchen"),
    INSTRUMENT		("Instrument"),
    METALLIC		("Metallic"),
    RAW				("Raw"),
    TOOL			("Tool"),
    UTILITY			("Utility"),
    VEHICLE			("Vehicle"),
    
	// For both Amount Resources or Parts
	CHEMICAL		(Msg.getString("FoodType.chemical")), //$NON-NLS-1$ 
    ELEMENT			("Element"),
    
    // For containers
    CONTAINER		("Container"),
    EVA				("EVA"),
    
    // For vehicles
	VEHICLE_HEAVY		("Vehicle Heavy"),
	VEHICLE_MEDIUM		("Vehicle Medium"),
	VEHICLE_SMALL		("Vehicle Small"),
    
    // For robots
    CHEFBOT				(Msg.getString("RobotType.chefBot")), //$NON-NLS-1$
	CONSTRUCTIONBOT		(Msg.getString("RobotType.constructionBot")), //$NON-NLS-1$
	DELIVERYBOT			(Msg.getString("RobotType.deliveryBot")), //$NON-NLS-1$ )
	GARDENBOT			(Msg.getString("RobotType.gardenBot")), //$NON-NLS-1$
	MAKERBOT			(Msg.getString("RobotType.makerBot")), //$NON-NLS-1$
	MEDICBOT			(Msg.getString("RobotType.medicBot")), //$NON-NLS-1$
	REPAIRBOT			(Msg.getString("RobotType.repairBot")),
    ;
    
	private String name;	
	
	/** hidden constructor. */
	private GoodType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * Obtains the enum type of the equipment with its name.
	 * 
	 * @param name
	 * @return {@link GoodType}
	 */
	public static GoodType convertName2Enum(String name) {
		if (name != null) {
	    	for (GoodType et : GoodType.values()) {
	    		if (name.equalsIgnoreCase(et.name)) {
	    			return et;
	    		}
	    	}
		}
		throw new IllegalArgumentException("No good type called " + name);
	}
}
