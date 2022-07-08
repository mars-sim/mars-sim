/*
 * Mars Simulation Project
 * BuildingSpec.java
 * @date 2021-10-01
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * The specification of a certain Building Type.
 *
 */
public class BuildingSpec {

	/**
	 * The thickness of the Aluminum wall of a building in meter. Typically between
	 * 10^-5 (.00001) and 10^-2 (.01) [in m]
	 */
	public static final double WALL_THICKNESS_ALUMINUM = 0.0000254;
	
	// Empty list constants
	private static final List<SourceSpec> EMPTY_SOURCE = new ArrayList<>();
	private static final List<ScienceType> EMPTY_SCIENCE = new ArrayList<>();
	private static final List<ResourceProcessSpec> EMPTY_RESOURCE = new ArrayList<>();
	private static final List<WasteProcessSpec> EMPTY_WASTE_RESOURCE = new ArrayList<>();
	
	private int baseLevel;
	private int maintenanceTime;
	private int wearLifeTime;
	
	private double basePowerRequirement;
	private double basePowerDownPowerRequirement;
	private double roomTemperature;

	private double length;
	private double width;
	private double thickness = WALL_THICKNESS_ALUMINUM;
	
	private double baseMass;
	
	private String buildingType;
	private String description;
	
	private Map<FunctionType, FunctionSpec> supportedFunctions;
	
	// Optional Function details
	private Map<Integer, Double> storageMap = null;
	private Map<Integer, Double> initialMap = null;

	private List<SourceSpec> heatSourceList = EMPTY_SOURCE;
	private List<SourceSpec> powerSource = EMPTY_SOURCE;
	
	private List<ScienceType> scienceType = EMPTY_SCIENCE;
	
	private List<ResourceProcessSpec> resourceProcess = EMPTY_RESOURCE;
	private List<WasteProcessSpec> wasteProcess = EMPTY_WASTE_RESOURCE;

	private List<LocalPosition> beds;
	private List<LocalPosition> parking;

	private BuildingCategory category;

	
	BuildingSpec(String buildingType, String description, BuildingCategory category, double width, double length, int baseLevel,
			double roomTemperature, int maintenanceTime,
			int wearLifeTime, double basePowerRequirement, double basePowerDownPowerRequirement,
			Map<FunctionType, FunctionSpec> supportedFunctions) {
		
		super();
		
		this.buildingType = buildingType;
		this.description = description;
		this.category = category;
		this.width = width;
		this.length = length;
		this.baseLevel = baseLevel;
		this.roomTemperature = roomTemperature;
		this.maintenanceTime = maintenanceTime;
		this.wearLifeTime = wearLifeTime;
		this.basePowerRequirement = basePowerRequirement;
		this.basePowerDownPowerRequirement = basePowerDownPowerRequirement;
		this.supportedFunctions = supportedFunctions;
	}

	/**
	 * What functions are supported by this building type.
	 * @return
	 */
	public Set<FunctionType> getFunctionSupported() {
		return supportedFunctions.keySet();	
	}

	/**
	 * Get the function details for this building type.
	 * @param function
	 * @return
	 */
	public FunctionSpec getFunctionSpec(FunctionType function) {
		return supportedFunctions.get(function);
	}
	
	public double getWallThickness() {
		return thickness;
	}

	public void setWallThickness(double thickness) {
		this.thickness = thickness;
	}
	
	public int getBaseLevel() {
		return baseLevel;
	}
	
	public String getBuildingType() {
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

	public double getRoomTemperature() {
		return roomTemperature;
	}

	public double getLength() {
		return length;
	}

	public double getWidth() {
		return width;
	}
	
	public double getBaseMass() {
		return baseMass;
	}
	
	public void setBaseMass(double value) {
		baseMass = value;
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

	public void setResourceProcess(List<ResourceProcessSpec> resourceProcess) {
		this.resourceProcess = resourceProcess;
	}

	public List<ResourceProcessSpec> getResourceProcess() {
		return resourceProcess;
	}
	
	public void setWasteProcess(List<WasteProcessSpec> wasteProcess) {
		this.wasteProcess = wasteProcess;
	}

	public List<WasteProcessSpec> getWasteProcess() {
		return wasteProcess;
	}
	
	public String toString() {
		return buildingType;
	}
	
	public List<LocalPosition> getBeds() {
		return beds;
	}

	void setBeds(List<LocalPosition> beds) {
		this.beds = beds;
	}
	
	public List<LocalPosition> getParking() {
		return parking;
	}

	void setParking(List<LocalPosition> parking) {
		this.parking = Collections.unmodifiableList(parking);
	}
}
