/*
 * Mars Simulation Project
 * VehicleGood.java
 * @date 2024-06-29
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufactureUtil;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleSpec;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * This represents a type of Vehicle that can be traded.
 */
class VehicleGood extends Good {
	
	private static final long serialVersionUID = 1L;

	private static final double LUV_FACTOR = 2;
	private static final double DRONE_FACTOR = 2;
    private static final double TRANSPORT_VEHICLE_FACTOR = 5;
	private static final double CARGO_VEHICLE_FACTOR = 4;
	private static final double EXPLORER_VEHICLE_FACTOR = 3;
	private static final double LUV_VEHICLE_FACTOR = 4;
	private static final double DRONE_VEHICLE_FACTOR = 5;

    private static final double INITIAL_VEHICLE_DEMAND = 0;
	private static final double INITIAL_VEHICLE_SUPPLY = 0;
	private static final int VEHICLE_VALUE = 20;
	private static final int LUV_VALUE = 10;
	private static final int DRONE_VALUE = 50;

    private static final double SPEED_TO_DISTANCE = 2D / 60D / 60D / MarsTime.convertSecondsToMillisols(1D) * 1000D;
	private static final double VEHICLE_FLATTENING_FACTOR = 2;

	/** The fixed flatten demand for this resource. */
	private double flattenDemand;
	/** The projected demand of each refresh cycle. */
	private double projectedDemand;
	/** The trade demand for this resource of each refresh cycle. */
	private double tradeDemand;
	/** The repair demand for this resource of each refresh cycle. */
	private double repairDemand;
	
	private double theoreticalRange;

	private VehicleSpec vs;
	
    private GoodType goodType;
    
    private VehicleType vehicleType;

    private List<ManufactureProcessInfo> manufactureProcessInfos;
    
	// Suitability of this vehicle type for different Missions
	private transient Map<MissionType, Double> missionCapacities = new EnumMap<>(MissionType.class);

    public VehicleGood(VehicleSpec vs) {
        super(vs.getName(), VehicleType.getVehicleID(vs.getType()));

        this.vs = vs;
        this.vehicleType = vs.getType();
        this.goodType = switch(vehicleType) {
			case DELIVERY_DRONE, CARGO_DRONE, LUV -> GoodType.VEHICLE_SMALL;
			case EXPLORER_ROVER -> GoodType.VEHICLE_MEDIUM;
			case TRANSPORT_ROVER, CARGO_ROVER -> GoodType.VEHICLE_HEAVY;
        };

		this.theoreticalRange = getVehicleRange(vs);

        // Pre-calculate manufactureProcessInfos
		manufactureProcessInfos = ManufactureUtil.getAllManufactureProcesses().stream()
					.filter(p -> p.isOutput(vs.getName()))
					.toList();

        // Calculate fixed values
     	flattenDemand = calculateFlattenDemand(vehicleType);
    }

    /**
	 * Calculates the flatten demand based on the vehicle type.
	 * 
	 * @param vehicleType
	 * @return
	 */
	private double calculateFlattenDemand(VehicleType vehicleType) {
		return VEHICLE_FLATTENING_FACTOR; 
	}
	
    /**
     * Gets the flattened demand.
     * 
     * @return
     */
    @Override
    public double getFlattenDemand() {
    	return flattenDemand;
    }
    
    /**
     * Gets the projected demand of this resource.
     * 
     * @return
     */
	@Override
    public double getProjectedDemand() {
    	return projectedDemand;
    }
	
    /**
     * Gets the trade demand of this resource.
     * 
     * @return
     */
	@Override
    public double getTradeDemand() {
    	return tradeDemand;
    }
	
    /**
     * Gets the repair demand of this resource.
     * 
     * @return
     */
	@Override
    public double getRepairDemand() {
    	return repairDemand;
    }
	
    @Override
    public GoodCategory getCategory() {
        return GoodCategory.VEHICLE;
    }

    @Override
    public double getMassPerItem() {
        return vehicleConfig.getVehicleSpec(getName()).getEmptyMass();
    }

    @Override
    public GoodType getGoodType() {
        return goodType;
    }

    @Override
    protected double computeCostModifier() {
        switch(vehicleType) {
            case LUV:
                return LUV_VALUE;
            case DELIVERY_DRONE, CARGO_DRONE:
                return DRONE_VALUE;
            default:
                return VEHICLE_VALUE;
        }
    }

    @Override
    public double getNumberForSettlement(Settlement settlement) {
		double number = settlement.getAllAssociatedVehicles().stream()
			                    .filter(v -> vehicleType == v.getVehicleType())
                                .count();
 
		// Get the number of vehicles that will be produced by ongoing manufacturing
		// processes.
		number += getManufacturingProcessOutput(settlement);

		return number;
    }

    @Override
    double calculatePrice(Settlement settlement, double value) {
        double mass = CollectionUtils.getVehicleTypeBaseMass(vehicleType);
        double quantity = settlement.findNumVehiclesOfType(vehicleType);
        double factor = Math.log(mass/1600.0 + 1) / (5 + Math.log(quantity + 1));
        return getCostOutput() * (1 + 2 * factor * Math.log(value + 1));  
    }

    @Override
    double getDefaultDemandValue() {
        return INITIAL_VEHICLE_DEMAND;
    }

    @Override
    double getDefaultSupplyValue() {
        return INITIAL_VEHICLE_SUPPLY;
    }

    @Override
    void refreshSupplyDemandValue(GoodsManager owner) {
		
		double previousDemand = owner.getDemandValue(this);
        Settlement settlement = owner.getSettlement();

		// Calculate total supply
		double totalSupply = getAverageVehicleSupply(getNumberForSettlement(settlement));
		
		owner.setSupplyValue(this, totalSupply);
			
		double projectedDemand = determineVehicleProjectedDemand(owner, settlement);
				
		projectedDemand = Math.min(HIGHEST_PROJECTED_VALUE, projectedDemand);
		
		this.projectedDemand = projectedDemand;
		
		double projected = projectedDemand * flattenDemand;

		double average = computeVehiclePartsCost(owner);
		
		tradeDemand = determineTradeVehicleValue(owner, settlement);
		
		// Gets the repair demand
		// Note: need to look into parts and vehicle reliability in MalfunctionManager 
		// to derive the repair value. 
		// Look at each part in vehicleType
		repairDemand = (owner.getMaintenanceLevel() + owner.getRepairLevel())/2.0 
				* owner.getDemandValue(this);
	
		double totalDemand;
		if (previousDemand == 0) {
			totalDemand = .5 * average 
						+ .1 * repairDemand
						+ .2 * projected 
						+ .2 * tradeDemand;
		}

		else {
			// Intentionally lose some values over time
			totalDemand = .97 * previousDemand 
					+ .003 * average 
					+ .001 * repairDemand
					+ .003 * projected 
					+ .003 * tradeDemand;
		}
				
		owner.setDemandValue(this, totalDemand);
	}

   	/**
	 * Computes vehicle parts cost.
	 * 
	 * @param good
	 * @return
	 */
	private double computeVehiclePartsCost(GoodsManager owner) {
		double result = 0;
	
		Iterator<ManufactureProcessInfo> ii = manufactureProcessInfos.iterator();
		while (ii.hasNext()) {
			ManufactureProcessInfo process = ii.next();

			for (var pi : process.getInputList()) {
				String iName = pi.getName();
				int id = GoodsUtil.getGoodID(iName);
				double value = owner.getGoodValuePoint(id);
				result += value;
			}
		}

		return result;
	}

	/**
	 * Gets the total supply for the vehicle.
	 *
	 * @param resource`
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	private static double getAverageVehicleSupply(double supplyStored) {
		return Math.sqrt(1 + supplyStored);
	}

	/**
	 * Determines the vehicle projected demand.
	 *
	 * @param vehicleGood
	 * @return the vehicle demand
	 */
	private double determineVehicleProjectedDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0D;

		boolean buy = false;

		if (vehicleType == VehicleType.LUV) {
			demand = determineLUVValue(settlement, buy);
		}

		else if (VehicleType.isDrone(vehicleType)) {
			double tradeMissionValue = determineMissionVehicleDemand(owner, settlement, MissionType.TRADE, buy);
			if (tradeMissionValue > demand) {
				demand = tradeMissionValue;
			}
			demand += determineDroneValue(settlement, buy);
		}

		else {
			// Check all missions and take highest demand
			for (MissionType missionType : MissionType.values()) {
				double missionDemand = determineMissionVehicleDemand(owner, settlement, missionType, buy);
				if (missionDemand > demand) {
					demand = missionDemand;
				}
			}
		}

        double typeModifier = switch (vehicleType) {
            case CARGO_ROVER -> CARGO_VEHICLE_FACTOR;		
            case TRANSPORT_ROVER -> TRANSPORT_VEHICLE_FACTOR;
            case EXPLORER_ROVER -> EXPLORER_VEHICLE_FACTOR;
		    case DELIVERY_DRONE, CARGO_DRONE -> DRONE_VEHICLE_FACTOR;
		    case LUV -> LUV_VEHICLE_FACTOR;
        };
		return demand * (.5 + owner.getCommerceFactor(CommerceType.TRADE)) * typeModifier;
	}

	/**
	 * Determines the trade vehicle value.
	 *
	 * @param owner GoodsManager
	 * @param settlement
	 * @return the trade vehicle value
	 */
	private double determineTradeVehicleValue(GoodsManager owner, Settlement settlement) {
		double tradeDemand = owner.determineTradeDemand(this);
		double supply = getNumberForSettlement(settlement);
		return tradeDemand / (supply + 1D);
	}

	/**
	 * Determine the value of a drone.
	 *
	 * @param settlement
	 * @param buy true if vehicles can be bought.
	 * @return value (VP)
	 */
	private double determineDroneValue(Settlement settlement, boolean buy) {

		double demand = 1D;

		// Add demand for construction missions by architects.
		demand += Math.min(7, JobUtil.numJobs(JobType.PILOT, settlement) * 1.1);

		// Add demand for mining missions by engineers.
		demand += Math.min(8, JobUtil.numJobs(JobType.TRADER, settlement) * 1.2);

		double supply = getNumberForSettlement(settlement);
		if (!buy)
			supply--;
		if (supply < 0D)
			supply = 0D;

		return demand / Math.log(supply + 2) * DRONE_FACTOR * Math.log(Math.min(48, settlement.getNumCitizens()));
	}

	/**
	 * Determines the value of a light utility vehicle.
	 *
	 * @param settlement
	 * @param buy true if vehicles can be bought.
	 * @return value (VP)
	 */
	private double determineLUVValue(Settlement settlement, boolean buy) {

		double demand = 1;

		// Add demand for mining missions by areologists.
		demand += Math.min(10, JobUtil.numJobs(JobType.AREOLOGIST, settlement) * 1.3);

		// Add demand for construction missions by architects.
		demand += Math.min(8, JobUtil.numJobs(JobType.ARCHITECT, settlement) * 1.2);

		// Add demand for mining missions by engineers.
		demand += Math.min(6, JobUtil.numJobs(JobType.ENGINEER, settlement) * 1.1);

		double supply = getNumberForSettlement(settlement);
		if (!buy)
			supply--;
		if (supply < 0D)
			supply = 0D;

		return demand / Math.log(supply + 2) * LUV_FACTOR * Math.log(Math.min(24, settlement.getNumCitizens()));
	}

	/**
	 * Determines the mission vehicle demand based on vehicle type and mission type.
	 * 
	 * @param owner GoodsManager
	 * @param settlement
	 * @param missionType
	 * @param buy true if vehicles can be bought.
	 * @return
	 */
	private double determineMissionVehicleDemand(GoodsManager owner, Settlement settlement, MissionType missionType, boolean buy) {

		double demand = determineMissionJob(owner, settlement, missionType);
		
		double partDemand = 0;
		
		Set<Integer> setIDs = vs.getParts();
		if (setIDs != null && !setIDs.isEmpty()) {
			for (int id : setIDs) {
				partDemand += owner.getDemandValueWithID(id);
			}
		}
		
		int potentialVehicle = 0;
		boolean soldFlag = false;
		Iterator<Vehicle> i = settlement.getAllAssociatedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle v = i.next();
			if (!buy && !soldFlag && (v.getVehicleType() == vehicleType))
				soldFlag = true;
			else
				potentialVehicle++;
		}

		double vehicleCapacity = determineMissionVehicleCapacity(missionType);

		double baseValue = ((demand + partDemand)/ ((potentialVehicle * vehicleCapacity) + 1D)) * vehicleCapacity;

		return baseValue;
	}

	/**
	 * Determines the mission vehicle demand based on mission type and job numbers.
	 * 
	 * @param owner GoodsManager
	 * @param settlement
	 * @param missionType
	 * @return
	 */
	private double determineMissionJob(GoodsManager owner, Settlement settlement, MissionType missionType) {
		// TODO should come from MissionMeta classes
		switch(missionType) {
		case CONSTRUCTION, SALVAGE:
				return JobUtil.numJobs(JobType.ARCHITECT, settlement);
		
		case TRAVEL_TO_SETTLEMENT, RESCUE_SALVAGE_VEHICLE:
			return JobUtil.numJobs(JobType.PILOT, settlement)
					* ((double) settlement.getNumCitizens() 
					/ (double) settlement.getPopulationCapacity());

		case COLLECT_ICE:
			return Math.min(owner.getDemandValue(GoodsUtil.getGood(ResourceUtil.ICE_ID)), 100);
		
		case TRADE, DELIVERY:
			return JobUtil.numJobs(JobType.TRADER, settlement);
		
		case COLLECT_REGOLITH:
			return Math.min(owner.getDemandValue(GoodsUtil.getGood(ResourceUtil.REGOLITH_ID)), 100);
		
		case MINING, AREOLOGY, EXPLORATION:
			return JobUtil.numJobs(JobType.AREOLOGIST, settlement);
		
		case BIOLOGY:
			return JobUtil.numJobs(JobType.BIOLOGIST, settlement);
		
		case METEOROLOGY:
			return JobUtil.numJobs(JobType.METEOROLOGIST, settlement);
		
		case EMERGENCY_SUPPLY:
            return 1D; // Simplify code as a temp measure
		}

		return 0;
	}

	/**
	 * Determines the mission vehicle capacity.
	 * 
	 * @param missionType
	 * @return
	 */
	private double determineMissionVehicleCapacity(MissionType missionType) {
		if (missionCapacities.containsKey(missionType)) {
			return missionCapacities.get(missionType);
		}
		return calculateMissionVehicleCapacity(missionType);
	}

	/**
	 * Calculates the capacity for this vehicle type to perform a Mission and update the cache.
	 * 
	 * @param missionType
	 * @return
	 */
	private synchronized double calculateMissionVehicleCapacity(MissionType missionType) {
		if (missionCapacities.containsKey(missionType)) {
			return missionCapacities.get(missionType);
		}
		double capacity = 0D;

		VehicleSpec v = vehicleConfig.getVehicleSpec(vehicleType.getName());
		int crewCapacity = v.getCrewSize();

		// TODO This logic should be pushed into the MissionMeta to remove knowledge of different Mission types.
		switch (missionType) {
		case TRAVEL_TO_SETTLEMENT: {
				if (crewCapacity >= 2)
					capacity = 1D;
				capacity *= crewCapacity / 8D;

				capacity *= theoreticalRange / 2000D;
			} break;

		case EXPLORATION: {
				if (crewCapacity >= 2)
					capacity = 1D;

				double cargoCapacity = v.getTotalCapacity();
				if (cargoCapacity < 500D)
					capacity = 0D;

				boolean hasAreologyLab = v.hasLab() && v.getLabTechSpecialties().contains(ScienceType.AREOLOGY);
				if (!hasAreologyLab)
					capacity /= 2D;

				if (theoreticalRange == 0D)
					capacity = 0D;
			} break;

		case COLLECT_ICE, COLLECT_REGOLITH: {
				if (crewCapacity >= 2)
					capacity = 1D;

				double cargoCapacity = v.getTotalCapacity();
				if (cargoCapacity < 1250D)
					capacity = 0D;

				if (theoreticalRange == 0D)
					capacity = 0D;
			} break;

		case RESCUE_SALVAGE_VEHICLE: {
				if (crewCapacity >= 2)
					capacity = 1D;

				capacity *= theoreticalRange / 2000D;
			}
			break;

		case TRADE, EMERGENCY_SUPPLY: {
				if (crewCapacity >= 2)
					capacity = 1D;

				double cargoCapacity = v.getTotalCapacity();
				capacity *= cargoCapacity / 10000D;

				capacity *= theoreticalRange / 2000D;
			} break;

		case DELIVERY: {
				capacity = 1D;

				double cargoCapacity = v.getTotalCapacity();
				capacity *= cargoCapacity / 10000D;

				capacity *= theoreticalRange / 2000D;			
			} break;

		case MINING: {
				if (crewCapacity >= 2)
					capacity = 1D;

				double cargoCapacity = v.getTotalCapacity();
				if (cargoCapacity < 1000D)
					capacity = 0D;

				if (theoreticalRange == 0D)
					capacity = 0D;
			} break;

		case AREOLOGY: {
				if (crewCapacity >= 2)
					capacity = 1D;

				if (v.hasLab()) {
					if (v.getLabTechSpecialties().contains(ScienceType.AREOLOGY)) {
						capacity += v.getLabTechLevel();
					} else {
						capacity /= 2D;
					}
				}

				if (theoreticalRange == 0D)
					capacity = 0D;
			} break;

		case BIOLOGY: {
				if (crewCapacity >= 2)
					capacity = 1D;

				if (v.hasLab()) {
					if (v.getLabTechSpecialties().contains(ScienceType.BIOLOGY)) {
						capacity += v.getLabTechLevel();
					} else {
						capacity /= 2D;
					}
				}

				if (theoreticalRange == 0D)
					capacity = 0D;
			} break;

		case METEOROLOGY: {
				if (crewCapacity >= 2)
					capacity = 1D;

				if (v.hasLab()) {
					if (v.getLabTechSpecialties().contains(ScienceType.METEOROLOGY)) {
						capacity += v.getLabTechLevel();
					} else {
						capacity /= 2D;
					}
				}

				if (theoreticalRange == 0D)
					capacity = 0D;
			} break;
		
			default:
				capacity = 1D;
				break;
		}

		missionCapacities.put(missionType, capacity);
		return capacity;
	}

	/**
	 * Gets the range of the vehicle type.
	 * TODO Surely this logic should be elsewhere
	 * 
	 * @param v {@link VehicleSpec}.
	 * @return range (km)
	 */
	private static double getVehicleRange(VehicleSpec v) {
		double range = v.getBaseRange();

        int crewSize = v.getCrewSize();
        if (crewSize > 0) {
            double baseSpeed = v.getBaseSpeed();
            double distancePerSol = baseSpeed / SPEED_TO_DISTANCE;
            
            if (personConfig == null) 
            	personConfig = SimulationConfig.instance().getPersonConfig();
            
            // Check food capacity as range limit.       
            double foodConsumptionRate = personConfig.getFoodConsumptionRate();
            double foodCapacity = v.getCargoCapacity(ResourceUtil.FOOD_ID);
            double foodSols = foodCapacity / (foodConsumptionRate * crewSize);
            double foodRange = distancePerSol * foodSols / 3D;
            if (foodRange < range)
                range = foodRange;

            // Check water capacity as range limit.
            double waterConsumptionRate = personConfig.getWaterConsumptionRate();
            double waterCapacity = v.getCargoCapacity(ResourceUtil.WATER_ID);
            double waterSols = waterCapacity / (waterConsumptionRate * crewSize);
            double waterRange = distancePerSol * waterSols / 3D;
            if (waterRange < range)
                range = waterRange;

            // Check oxygen capacity as range limit.
            double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
            double oxygenCapacity = v.getCargoCapacity(ResourceUtil.OXYGEN_ID);
            double oxygenSols = oxygenCapacity / (oxygenConsumptionRate * crewSize);
            double oxygenRange = distancePerSol * oxygenSols / 3D;
            if (oxygenRange < range)
                range = oxygenRange;
        }

		return range;
	}
}
