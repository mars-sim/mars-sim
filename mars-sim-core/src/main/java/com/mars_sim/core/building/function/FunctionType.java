/*
 * Mars Simulation Project
 * FunctionType.java
 * @date 2024-07-04
 * @author stpa				
 */
package com.mars_sim.core.building.function;

import com.mars_sim.core.Named;

import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

public enum FunctionType implements Named {

    ADMINISTRATION              (BuildingCategory.COMMAND), 
	ALGAE_FARMING				(BuildingCategory.FARMING), 
    ASTRONOMICAL_OBSERVATION	(BuildingCategory.ASTRONOMY), 
	CONNECTION					(BuildingCategory.CONNECTION), 
	COMMUNICATION				(BuildingCategory.COMMUNICATION), 
	COMPUTATION					(BuildingCategory.LABORATORY), 
	COOKING						(BuildingCategory.LIVING), 
	DINING						(BuildingCategory.LIVING), 
	EARTH_RETURN				(BuildingCategory.ERV), 
	EVA							(BuildingCategory.EVA), 
	EXERCISE					(BuildingCategory.LIVING), 
	FARMING						(BuildingCategory.FARMING), 
	FISHERY						(BuildingCategory.FARMING), 
	FOOD_PRODUCTION  			(BuildingCategory.LIVING), 	
	VEHICLE_MAINTENANCE			(BuildingCategory.VEHICLE), 
	// Life support is everywhere so has no category on it's own
	LIFE_SUPPORT				(null), 
	LIVING_ACCOMMODATION		(BuildingCategory.LIVING), 
	MANAGEMENT					(BuildingCategory.COMMAND), 
	MANUFACTURE					(BuildingCategory.WORKSHOP), 
	MEDICAL_CARE				(BuildingCategory.MEDICAL), 
	POWER_GENERATION			(BuildingCategory.POWER), 
	POWER_STORAGE				(BuildingCategory.POWER), 
	RECREATION					(BuildingCategory.LIVING), 
	RESEARCH					(BuildingCategory.LABORATORY), 
	RESOURCE_PROCESSING			(BuildingCategory.PROCESSING), 
	// Like LifeSupport this is a secondary, supporting Function
	ROBOTIC_STATION				(null), 
	STORAGE						(BuildingCategory.STORAGE),  
	THERMAL_GENERATION			(BuildingCategory.POWER), 
	WASTE_PROCESSING			(BuildingCategory.PROCESSING), 
	
	// Not implemented yet
	FIELD_STUDY					(BuildingCategory.LABORATORY), 
	UNKNOWN						(BuildingCategory.LIVING) 
	;

	private String name;
	private BuildingCategory category;


	/** hidden constructor. */
	private FunctionType(BuildingCategory category) {
		this.name = Msg.getStringOptional("FunctionType", name());
		this.category = category;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	public BuildingCategory getCategory() {
		return this.category;
	}

	/**
	 * Gets the default Function for a Robot Type.
	 * 
	 * @return FunctionType
	 */
	public static FunctionType getDefaultFunction(RobotType type) {
		switch (type) {
		case CHEFBOT:
			return FunctionType.COOKING;
		
		case CONSTRUCTIONBOT:
			return FunctionType.MANUFACTURE;
			
		case DELIVERYBOT:
			return FunctionType.VEHICLE_MAINTENANCE;
			
		case GARDENBOT:
			return FunctionType.FARMING;
			
		case MAKERBOT:
			return FunctionType.MANUFACTURE;
			
		case MEDICBOT:
			return FunctionType.MEDICAL_CARE;
			
		case REPAIRBOT:
			return FunctionType.LIFE_SUPPORT;
			
		default:
			return FunctionType.ROBOTIC_STATION;
		}
	}
	
	/**
	 * Gets the default Function for a Job Type.
	 * 
	 * @return FunctionType
	 */
	public static FunctionType getDefaultFunction(JobType type) {
		switch (type) {
		
		case ARCHITECT, AREOLOGIST :
			return FunctionType.MANUFACTURE;
		
		case ASTRONOMER:
			return FunctionType.ASTRONOMICAL_OBSERVATION;
			
		case ASTROBIOLOGIST:
			return FunctionType.FISHERY;
			
		case BOTANIST:
			return FunctionType.FARMING;
			
		case CHEF:
			return FunctionType.COOKING;
			
		case CHEMIST:
			return FunctionType.ALGAE_FARMING;
			
		case COMPUTER_SCIENTIST:
			return FunctionType.COMPUTATION;

		case DOCTOR, PSYCHOLOGIST:
			return FunctionType.MEDICAL_CARE;
			
		case ENGINEER:
			return FunctionType.COMMUNICATION;
			
		case MATHEMATICIAN, PHYSICIST:
			return FunctionType.RESEARCH;
			
		case METEOROLOGIST, TOURIST:
			return FunctionType.FIELD_STUDY;
			
		case PILOT, TRADER:
			return FunctionType.VEHICLE_MAINTENANCE;
			
		case POLITICIAN, REPORTER:
			return FunctionType.ADMINISTRATION;
			
		case SOCIOLOGIST:
			return FunctionType.RESEARCH;
			
		case TECHNICIAN:
			return FunctionType.RESOURCE_PROCESSING;
			
		default:
			int rand = RandomUtil.getRandomInt(4);
			if (rand == 0)		
				return FunctionType.LIVING_ACCOMMODATION;
			else if (rand == 1)
				return FunctionType.DINING;
			else if (rand == 2)
				return FunctionType.LIFE_SUPPORT;
			else if (rand == 3)
				return FunctionType.EXERCISE;
			else 
				return FunctionType.RECREATION;
			
		}
	}
}
