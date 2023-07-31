/*
 * Mars Simulation Project
 * GoodType.java
 * @date 2023-07-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.goods;

import org.mars.sim.tools.Msg;

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
	GEMSTONE	("Gemstone"),
	MEDICAL 	("Medical"),
	MINERAL		("Mineral"),
	ORE			("Ore"),
	ROCK		("Rock"),
	REGOLITH	("Regolith"),
	WASTE		("Waste"),
	
	// For amount resources
    COMPOUND		("Compound"),
    
	// For Parts
	ATTACHMENT		("Attachment"),
    CONSTRUCTION	("Construction"),
    ELECTRICAL		("Electrical"),
    ELECTRONIC		("Electronic"),
    KITCHEN			("Kitchen"),
    INSTRUMENT		("Instrument"),
    METALLIC		("Metallic"),
    RAW				("Raw"),
    TOOL			("Tool"),
    VEHICLE			("Vehicle"),
    
	// For both Amount Resources or Parts
	CHEMICAL		(Msg.getString("FoodType.chemical")), //$NON-NLS-1$ 
    ELEMENT			("Element"),
    UTILITY			("Utility"),
    
    // For containers
    CONTAINER		("Container"),
    EVA				("EVA"),
    
    // For bins
    BIN				("Bin"),
    
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
	REPAIRBOT			(Msg.getString("RobotType.repairBot")), //$NON-NLS-1$
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
}
