/*
 * Mars Simulation Project
 * FunctionType.java
 * @date 2024-07-04
 * @author stpa				
 */
package com.mars_sim.core.structure.building.function;

import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.tools.Msg;

public enum FunctionType {

    ADMINISTRATION              (BuildingCategory.COMMAND, Msg.getString("FunctionType.administration")), //$NON-NLS=1$
	ALGAE_FARMING				(BuildingCategory.FARMING, Msg.getString("FunctionType.algaeFarming")), //$NON-NLS-1$
    ASTRONOMICAL_OBSERVATION	(BuildingCategory.ASTRONOMY, Msg.getString("FunctionType.astronomicalObservation")), //$NON-NLS-1$
	CONNECTION					(BuildingCategory.CONNECTION, Msg.getString("FunctionType.connection")), //$NON-NLS-1$
	COMMUNICATION				(BuildingCategory.COMMUNICATION,Msg.getString("FunctionType.communication")), //$NON-NLS-1$
	COMPUTATION					(BuildingCategory.LABORATORY, Msg.getString("FunctionType.computation")), //$NON-NLS-1$
	COOKING						(BuildingCategory.LIVING, Msg.getString("FunctionType.cooking")), //$NON-NLS-1$
	DINING						(BuildingCategory.LIVING, Msg.getString("FunctionType.dining")), //$NON-NLS-1$
	EARTH_RETURN				(BuildingCategory.ERV, Msg.getString("FunctionType.earthReturn")), //$NON-NLS-1$
	EVA							(BuildingCategory.EVA, Msg.getString("FunctionType.eva")), //$NON-NLS-1$
	EXERCISE					(BuildingCategory.LIVING, Msg.getString("FunctionType.exercise")), //$NON-NLS-1$
	FARMING						(BuildingCategory.FARMING, Msg.getString("FunctionType.farming")), //$NON-NLS-1$
	FISHERY						(BuildingCategory.FARMING, Msg.getString("FunctionType.fishery")), //$NON-NLS-1$
	FOOD_PRODUCTION  			(BuildingCategory.LIVING, Msg.getString("FunctionType.foodProduction")), //$NON-NLS-1$	
	VEHICLE_MAINTENANCE			(BuildingCategory.VEHICLE, Msg.getString("FunctionType.vehicleMaintenance")), //$NON-NLS-1$
	// Life support is everywhere so has no category on it's own
	LIFE_SUPPORT				(null, Msg.getString("FunctionType.lifeSupport")), //$NON-NLS-1$
	LIVING_ACCOMMODATION		(BuildingCategory.LIVING, Msg.getString("FunctionType.livingAccommodation")), //$NON-NLS-1$
	MANAGEMENT					(BuildingCategory.COMMAND, Msg.getString("FunctionType.management")), //$NON-NLS-1$
	MANUFACTURE					(BuildingCategory.WORKSHOP, Msg.getString("FunctionType.manufacture")), //$NON-NLS-1$
	MEDICAL_CARE				(BuildingCategory.MEDICAL, Msg.getString("FunctionType.medicalCare")), //$NON-NLS-1$
	POWER_GENERATION			(BuildingCategory.POWER, Msg.getString("FunctionType.powerGeneration")), //$NON-NLS-1$
	POWER_STORAGE				(BuildingCategory.POWER, Msg.getString("FunctionType.powerStorage")), //$NON-NLS-1$
	PREPARING_DESSERT			(BuildingCategory.LIVING, Msg.getString("FunctionType.preparingDessert")),  //$NON-NLS-1$
	RECREATION					(BuildingCategory.LIVING, Msg.getString("FunctionType.recreation")), //$NON-NLS-1$
	RESEARCH					(BuildingCategory.LABORATORY, Msg.getString("FunctionType.research")), //$NON-NLS-1$
	RESOURCE_PROCESSING			(BuildingCategory.PROCESSING, Msg.getString("FunctionType.resourceProcessing")), //$NON-NLS-1$
	// Like LifeSupport this is a secondary, supporting Function
	ROBOTIC_STATION				(null, Msg.getString("FunctionType.roboticStation")), //$NON-NLS-1$
	STORAGE						(BuildingCategory.STORAGE, Msg.getString("FunctionType.storage")),  //$NON-NLS-1$
	THERMAL_GENERATION			(BuildingCategory.POWER, Msg.getString("FunctionType.thermalGeneration")), //$NON-NLS-1$
	WASTE_PROCESSING			(BuildingCategory.PROCESSING, Msg.getString("FunctionType.wasteProcessing")), //$NON-NLS-1$
	
	// Not implemented yet
	FIELD_STUDY					(BuildingCategory.LABORATORY, Msg.getString("FunctionType.fieldStudy")), //$NON-NLS-1$
	UNKNOWN						(BuildingCategory.LIVING, Msg.getString("FunctionType.unknown")) //$NON-NLS-1$
	;

	private String name;
	private BuildingCategory category;


	/** hidden constructor. */
	private FunctionType(BuildingCategory category, String name) {
		this.name = name;
		this.category = category;
	}

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
}
