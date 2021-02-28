package org.mars_sim.msp.core.structure.building;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	private String name;
	private double roomTemperature;
	private int maintenanceTime;
	private double basePowerDownPowerRequirement;
	private int wearLifeTime;
	private double basePowerRequirement;
	private int baseLevel;
	private String description;
	private Set<FunctionType> supportedFunctions;
	private double length;
	private double width;
	private double thickness = WALL_THICKNESS_ALUMINUM;
	
	// Optional Function details
	private Map<Integer, Double> storageMap = null;
	private Map<Integer, Double> initialMap = null;
	private double stockCapacity = 0;
	private List<SourceSpec> heatSourceList = EMPTY_SOURCE;
	private List<SourceSpec> powerSource = EMPTY_SOURCE;
	private List<ScienceType> scienceType = EMPTY_SCIENCE;
	private List<ResourceProcessSpec> resourceProcess = EMPTY_RESOURCE;
	
	
	public BuildingSpec(String name, String description, double width, double length, int baseLevel,
			double roomTemperature, int maintenanceTime,
			int wearLifeTime, double basePowerRequirement, double basePowerDownPowerRequirement,
			Set<FunctionType> supportedFunctions) {
		super();
		this.name = name;
		this.description = description;
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

	public double getWallThickness() {
		return thickness;
	}

	public void setWallThickness(double thickness) {
		this.thickness = thickness;
	}
	
	public int getBaseLevel() {
		return baseLevel;
	}
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Set<FunctionType> getFunctionSupported() {
		return supportedFunctions;	
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

	public double getStockCapacity() {
		return stockCapacity;
	}
	
	public Map<Integer, Double> getStorage() {
		return storageMap;
	}
	
	public Map<Integer, Double> getInitialResources() {
		return initialMap;
	}

	void setStorage(double stockCapacity, Map<Integer, Double> storageMap, Map<Integer, Double> initialMap) {
		this.stockCapacity = stockCapacity;
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

	
	public String toString() {
		return name;
	}
	
	public void setResourceProcess(List<ResourceProcessSpec> resourceProcess) {
		this.resourceProcess = resourceProcess;
	}

	public List<ResourceProcessSpec> getResourceProcess() {
		return resourceProcess;
	}
}
