/*
 * Mars Simulation Project
 * BuildingSpec.java
 * @date 2024-07-12
 * @author Barry Evans
 */
package com.mars_sim.core.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.SystemType;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.resourceprocess.ResourceProcessEngine;
import com.mars_sim.core.science.ScienceType;

/**
 * The specification of a certain Building Type.
 *
 */
public class BuildingSpec {
	
	
	public static final String HABITABLE = "habitable";
	public static final String METALLIC_ELEMENT = "metallic element";
	
	// Empty list constants
	private static final List<SourceSpec> EMPTY_SOURCE = new ArrayList<>();
	private static final List<ScienceType> EMPTY_SCIENCE = new ArrayList<>();
	private static final List<ResourceProcessEngine> EMPTY_RESOURCE = new ArrayList<>();
	
	/** is the building non-habitable. */
	private boolean isInhabitable = true;
	/** The flag for tracking if the system scope has been set up. */
	boolean systemScopeDone = false;
	
	private int baseLevel;
	private int maintenanceTime;
	private int wearLifeTime;
	
	private double basePowerRequirement;
	private double basePowerDownPowerRequirement;
	private double presetTemperature;

	private double length;
	private double width;
	
	private double baseMass;
	
	private String alignment;
	private String buildingType;
	private String description;
	
	/** A set of system scopes affected by malfunction incidents. */
	private Set<String> systemScopes = new HashSet<>();;

	/**
	 * The type of material use for the construction of the wall of a building.
	 * Solid by default 
	 */
	private ConstructionType constructionType = ConstructionType.PRE_FABRICATED;

	private Map<FunctionType, FunctionSpec> supportedFunctions;

	// Optional Function details
	private Map<Integer, Double> storageMap = null;
	private Map<Integer, Double> initialMap = null;

	private List<SourceSpec> heatSourceList = EMPTY_SOURCE;
	private List<SourceSpec> powerSource = EMPTY_SOURCE;
	
	private List<ScienceType> scienceType = EMPTY_SCIENCE;
	
	private List<ResourceProcessEngine> resourceProcess = EMPTY_RESOURCE;
	private List<ResourceProcessEngine> wasteProcess = EMPTY_RESOURCE;

	private Set<LocalPosition> beds;
	private Set<LocalPosition> roverParking;
	private Set<LocalPosition> utilityParking;
	private Set<LocalPosition> flyerParking;

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
	 * @param baseLevel
	 * @param presetTemperature
	 * @param maintenanceTime
	 * @param wearLifeTime
	 * @param basePowerRequirement
	 * @param basePowerDownPowerRequirement
	 * @param supportedFunctions
	 */
	BuildingSpec(BuildingConfig buildingConfig, String buildingType, String description, BuildingCategory category, 
			double width, double length, String alignment, int baseLevel,
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
		this.baseLevel = baseLevel;
		this.presetTemperature = presetTemperature;
		this.maintenanceTime = maintenanceTime;
		this.wearLifeTime = wearLifeTime;
		this.basePowerRequirement = basePowerRequirement;
		this.basePowerDownPowerRequirement = basePowerDownPowerRequirement;
		this.supportedFunctions = supportedFunctions;
		
		if (supportedFunctions.containsKey(FunctionType.LIFE_SUPPORT)) {
			isInhabitable = false;
			addSystemScope(HABITABLE);
		}
		
		// Add 'building' as a scope name
		addSystemScope(SystemType.BUILDING.getName());
		// Add the building type as a scope name
		addSystemScope(buildingType);
		// Add all the system scopes pre-defined in buildings.xml for a particular building type
		Set<String> scopes = buildingConfig.getBuildingScopes().get(buildingType);
		if (scopes != null)
			addSystemScope(scopes);
	}

	/**
	 * Sets the flag for the system scope.
	 * 
	 * @param value
	 */
	protected void setScopeDone(boolean value) {
		systemScopeDone = value;
	}
	
	/**
	 * Gets the flag for the system scope.
	 * 
	 * @return
	 */
	protected boolean getScopeDone() {
		return systemScopeDone;
	}
	
	/**
	 * Gets the system scopes
	 */
	protected Set<String> getSystemScopes() {	
		return systemScopes;
	}
	
	/**
	 * Adds a system scope.
	 * 
	 * @param newScope
	 */
	protected void addSystemScope(String newScope) {
		systemScopes.add(newScope);
	}
	
	/**
	 * 
	 * Adds a set of scopes.
	 *
	 * @param scope
	 */
	public void addSystemScope(Set<String> newScopes) {
		for (String aScope: newScopes) {
			String scopeString = aScope.toLowerCase().replace("_", " ");
			if ((scopeString != null) && !systemScopes.contains(scopeString))
				systemScopes.add(scopeString);
		}
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
	
	public double getBaseMass() {
		return baseMass;
	}
	
	public void setBaseMass(double value) {
		baseMass = value;
	}
	
	public ConstructionType getConstruction() {
		return constructionType;
	}

	public void setConstruction(ConstructionType type) {
		this.constructionType = type;
	}

	public Map<Integer, Double> getStorage() {
		return storageMap;
	}
	
	public Map<Integer, Double> getInitialResources() {
		return initialMap;
	}

	void setStorage(Map<Integer, Double> storageMap, Map<Integer, Double> initialMap) {
		this.storageMap = storageMap;
		this.initialMap = initialMap;
	}

	public void setHeatSource(List<SourceSpec> heatSourceList) {
		this.heatSourceList = heatSourceList;
	}
	
	public List<SourceSpec> getHeatSource() {
		return heatSourceList;
	}
	
	public void setPowerSource(List<SourceSpec> powerSource) {
		this.powerSource = powerSource;
	}
	
	public List<SourceSpec> getPowerSource() {
		return powerSource;
	}

	public void setScienceType(List<ScienceType> scienceType) {
		this.scienceType = scienceType;
	}
	
	public List<ScienceType> getScienceType() {
		return scienceType;
	}

	public void setResourceProcess(List<ResourceProcessEngine> resourceProcesses) {
		this.resourceProcess = resourceProcesses;
	}

	public List<ResourceProcessEngine> getResourceProcess() {
		return resourceProcess;
	}
	
	public void setWasteProcess(List<ResourceProcessEngine> list) {
		this.wasteProcess = list;
	}

	public List<ResourceProcessEngine> getWasteProcess() {
		return wasteProcess;
	}
	
	public String toString() {
		return buildingType;
	}
	
	public Set<LocalPosition> getBeds() {
		return beds;
	}

	void setBeds(Set<LocalPosition> beds) {
		this.beds = beds;
	}
	
	public Set<LocalPosition> getRoverParking() {
		return roverParking;
	}

	void setRoverParking(Set<LocalPosition> parking) {
		this.roverParking = Collections.unmodifiableSet(parking);
	}
	
	public Set<LocalPosition> getUtilityParking() {
		return utilityParking;
	}

	void setUtilityParking(Set<LocalPosition> parking) {
		this.utilityParking = Collections.unmodifiableSet(parking);
	}
	
	
	public Set<LocalPosition> getFlyerParking() {
		return flyerParking;
	}

	void setFlyerParking(Set<LocalPosition> parking) {
		this.flyerParking = Collections.unmodifiableSet(parking);
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
