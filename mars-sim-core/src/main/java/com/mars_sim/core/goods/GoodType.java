/*
 * Mars Simulation Project
 * GoodType.java
 * @date 2023-07-30
 * @author Manny Kung
 */
package com.mars_sim.core.goods;

import com.mars_sim.core.food.FoodType;
import com.mars_sim.core.robot.RobotType;

/**
 * The GoodType enum class is used for distinguishing between various type of goods
 */
public enum GoodType {
	
	// For Amount Resources
	
	// food resources
	ANIMAL				(FoodType.ANIMAL.getName()),
	CROP				(FoodType.CROP.getName()),
	DERIVED				(FoodType.DERIVED.getName()),
	INSECT				(FoodType.INSECT.getName()),
	OIL					(FoodType.OIL.getName()),
	ORGANISM			(FoodType.ORGANISM.getName()),
	SOY_BASED			(FoodType.SOY_BASED.getName()),
	TISSUE				(FoodType.TISSUE.getName()),

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
	CHEMICAL		(FoodType.CHEMICAL.getName()),
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
	ROBOT				("Robot"),
    CHEFBOT				(RobotType.CHEFBOT.getName()),
	CONSTRUCTIONBOT		(RobotType.CONSTRUCTIONBOT.getName()),
	DELIVERYBOT			(RobotType.DELIVERYBOT.getName()),
	GARDENBOT			(RobotType.GARDENBOT.getName()),
	MAKERBOT			(RobotType.MAKERBOT.getName()),
	MEDICBOT			(RobotType.MEDICBOT.getName()),
	REPAIRBOT			(RobotType.REPAIRBOT.getName()),
    ;
    
	private String name;	
	
	/** hidden constructor. */
	private GoodType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
