/*
 * Mars Simulation Project
 * BuildingSpec.java
 * @date 2024-07-12
 * @author Barry Evans
 */
package com.mars_sim.core.building.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.ConstructionType;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.BoundedObject;

/**
 * The specification of a certain Building Type.
 *
 */
public class BuildingSpec {
	
	
	public static final String HABITABLE = "habitable";
	public static final String METALLIC_ELEMENT = "metallic element";
	
	/** is the building non-habitable. */
	private boolean isInhabitable = true;
	
	private int baseLevel;
	private int maintenanceTime;
	private int wearLifeTime;
	
	private double basePowerRequirement;
	private double basePowerDownPowerRequirement;
	private double presetTemperature;

	private double length;
	private double width;
	
	private String alignment;
	private String buildingType;
	private String description;
	
	/** A set of system scopes affected by malfunction incidents. */
	private Set<String> systemScopes = new HashSet<>();

	/**
	 * The type of material use for the construction of the wall of a building.
	 * Solid by default 
	 */
	private ConstructionType constructionType;

	private Map<FunctionType, FunctionSpec> supportedFunctions;

	private BuildingCategory category;
	
	/**
	 * The constructor for BuildingSpecs.
	 * 
	 * @param buildingType
	 * @param description
	 * @param category
	 * @param width
	 * @param length
	 * @param alignment
	 * @param scopeNames 
	 * @param constructionType2 
	 * @param baseLevel
	 * @param presetTemperature
	 * @param maintenanceTime
	 * @param wearLifeTime
	 * @param basePowerRequirement
	 * @param basePowerDownPowerRequirement
	 * @param supportedFunctions
	 */
	BuildingSpec(String buildingType, String description, BuildingCategory category, 
			double width, double length, String alignment, ConstructionType constructionType, Set<String> scopeNames, int baseLevel,
			double presetTemperature, int maintenanceTime,
			int wearLifeTime, double basePowerRequirement, double basePowerDownPowerRequirement,
			Map<FunctionType, FunctionSpec> supportedFunctions) {
		
		super();
		
		this.buildingType = buildingType;
		this.description = description;
		this.category = category;
		this.width = width;
		this.length = length;
		this.alignment = alignment;
		this.constructionType = constructionType;
		this.baseLevel = baseLevel;
		this.presetTemperature = presetTemperature;
		this.maintenanceTime = maintenanceTime;
		this.wearLifeTime = wearLifeTime;
		this.basePowerRequirement = basePowerRequirement;
		this.basePowerDownPowerRequirement = basePowerDownPowerRequirement;
		this.supportedFunctions = supportedFunctions;
		
		if (supportedFunctions.containsKey(FunctionType.LIFE_SUPPORT)) {
			isInhabitable = false;
			systemScopes.add(HABITABLE);
		}
		
		// Add 'building' as a scope name
		systemScopes.add("building");
		// Add the building type as a scope name
		systemScopes.add(buildingType);

		// Add all the system scopes pre-defined in buildings.xml for a particular building type
		systemScopes.addAll(scopeNames);
	}

	/**
	 * Gets the system scopes
	 */
	public Set<String> getSystemScopes() {	
		return systemScopes;
	}

	/**
	 * Checks if this building is isInhabitable.
	 * 
	 * @return
	 */
	public boolean isInhabitable() {
		return isInhabitable;
	}
	
	/**
	 * What functions are supported by this building type.
	 * 
	 * @return
	 */
	public Set<FunctionType> getFunctionSupported() {
		return supportedFunctions.keySet();	
	}
	
	/**
	 * Gets the function details for this building type.
	 * 
	 * @param function
	 * @return
	 */
	public FunctionSpec getFunctionSpec(FunctionType function) {
		return supportedFunctions.get(function);
	}
	
	public int getBaseLevel() {
		return baseLevel;
	}
	
	public String getName() {
		return buildingType;
	}

	public String getDescription() {
		return description;
	}

	public BuildingCategory getCategory() {
		return category;
	}

	public double getBasePowerRequirement() {
		return basePowerRequirement;
	}

	public double getBasePowerDownPowerRequirement() {
		return basePowerDownPowerRequirement;
	}
	
	public int getWearLifeTime() {
		return wearLifeTime;
	}

	public int getMaintenanceTime() {
		return maintenanceTime;
	}

	public double getPresetTemperature() {
		return presetTemperature;
	}

	public double getLength() {
		return length;
	}

	public double getWidth() {
		return width;
	}
	
    /**
     * Gets the north-south alignment info.
     * 
     * @return
     */
	public String getAlignment() {
		return alignment;
	}
	
	public ConstructionType getConstruction() {
		return constructionType;
	}

	public String toString() {
		return buildingType;
	}
	
	/**
	 * Combines a custom bounds definition with what is validate for this building.
	 * 
	 * @param bounds Request bounds
	 * @return Combined valid bounds
	 */
    public BoundedObject getValidBounds(BoundedObject bounds) {
		var newWidth = width;
		if (newWidth < 0) {
			newWidth = bounds.getWidth();
		}
		var newLength = length;
		if (newLength < 0) {
			newLength = bounds.getLength();
		}
        return new BoundedObject(bounds.getPosition(), newWidth, newLength, bounds.getFacing());
    }
}
