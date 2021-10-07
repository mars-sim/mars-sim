/*
 * Mars Simulation Project
 * BuildingSpec.java
 * @date 2021-10-01
 * @author Barry Evans
 */

package org.mars_sim.msp.core.structure.building;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * The specification of a certain Building Type.
 *
 */
public class BuildingSpec {

	static class FunctionSpec {
		private Properties props;
		private List<Point2D> spots;

		public FunctionSpec(Properties props, List<Point2D> spots) {
			this.props = props;
			this.spots = spots;
		}

		public List<Point2D> getActivitySpots() {
			return spots;
		}
		
		/**
		 * Get the custom Function property
		 * @param name
		 * @return
		 */
		public String getProperty(String name) {
			return props.getProperty(name);
		}
	}
	
	/**
	 * The thickness of the Aluminum wall of a building in meter. Typically between
	 * 10^-5 (.00001) and 10^-2 (.01) [in m]
	 */
	public static final double WALL_THICKNESS_ALUMINUM = 0.0000254;
	
	// Empty list constants
	private static final List<SourceSpec> EMPTY_SOURCE = new ArrayList<>();
	private static final List<ScienceType> EMPTY_SCIENCE = new ArrayList<>();
	private static final List<ResourceProcessSpec> EMPTY_RESOURCE = new ArrayList<>();
	
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
	
	private double computingUnit;
	private double powerDemand;
	private double coolingDemand;
	
	private double stockCapacity = 0;
	
	private String name;
	private String description;
	
	private Map<FunctionType, FunctionSpec> supportedFunctions;
	
	// Optional Function details
	private Map<Integer, Double> storageMap = null;
	private Map<Integer, Double> initialMap = null;

	private List<SourceSpec> heatSourceList = EMPTY_SOURCE;
	private List<SourceSpec> powerSource = EMPTY_SOURCE;
	private List<ScienceType> scienceType = EMPTY_SCIENCE;
	private List<ResourceProcessSpec> resourceProcess = EMPTY_RESOURCE;

	private List<Point2D> beds;
	private List<Point2D> parking;

	private List<ScienceType> wasteSpecialties;
	
	
	public BuildingSpec(String name, String description, double width, double length, int baseLevel,
			double roomTemperature, int maintenanceTime,
			int wearLifeTime, double basePowerRequirement, double basePowerDownPowerRequirement,
			Map<FunctionType, FunctionSpec> supportedFunctions) {
		
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
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
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

	void setComputation(double computingUnit, double powerDemand, double coolingDemand) {
		this.computingUnit = computingUnit;
		this.powerDemand = powerDemand;
		this.coolingDemand = coolingDemand;
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
	
	public String toString() {
		return name;
	}
	
	public List<Point2D> getBeds() {
		return beds;
	}

	public void setBeds(List<Point2D> beds) {
		this.beds = beds;
	}
	
	public List<Point2D> getParking() {
		return parking;
	}

	public void setParking(List<Point2D> parking) {
		this.parking = parking;
	}

	public List<ScienceType> getWasteSpecialties() {
		return wasteSpecialties;
	}

	public void setWasteSpecialties(List<ScienceType> wasteSpecialties) {
		this.wasteSpecialties = wasteSpecialties;
	}
	
	public double getComputingUnit() {
		return computingUnit;
	}
	
	public double getPowerDemand() {
		return powerDemand;
	}
	
	public double getCoolingDemand() {
		return coolingDemand;
	}
}
