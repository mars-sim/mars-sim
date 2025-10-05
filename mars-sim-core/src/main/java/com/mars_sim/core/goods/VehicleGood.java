/*
 * Mars Simulation Project
 * VehicleGood.java
 * @date 2025-07-26
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleSpec;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * This represents a type of Vehicle that can be traded.
 */
class VehicleGood extends Good {
	
	private static final long serialVersionUID = 1L;

	private static final double LUV_FACTOR = 3;
	private static final double DRONE_FACTOR = 3;
    private static final double TRANSPORT_VEHICLE_FACTOR = .4;
	private static final double CARGO_VEHICLE_FACTOR = .3;
	private static final double EXPLORER_VEHICLE_FACTOR = .3;

    private static final double INITIAL_VEHICLE_DEMAND = 1;
	private static final double INITIAL_VEHICLE_SUPPLY = 1;
	private static final int VEHICLE_VALUE = 10;
	private static final int LUV_VALUE = 7;
	private static final int DRONE_VALUE = 30;

    private static final double SPEED_TO_DISTANCE = 2D / 60D / 60D / MarsTime.convertSecondsToMillisols(1D) * 1000D;
	private static final double VEHICLE_FLATTENING_FACTOR = 1;

	/** The fixed flatten demand for this resource. */
	private double flattenDemand;
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
			case EXPLORER_ROVER, PASSENGER_DRONE -> GoodType.VEHICLE_MEDIUM;
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
        double supply = settlement.getGoodsManager().getSupplyScore(getID());
        double factor = 1.5 / (2 + supply);
        double price = getCostOutput() * (1 + factor * Math.log(Math.sqrt(value + 1)));  
        setPrice(price);
	    return price;
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
    void refreshSupplyDemandScore(GoodsManager owner) {
    	
		double previousDemand = owner.getDemandScore(this);
		
        Settlement settlement = owner.getSettlement();
		
		// Calculate total supply
		double totalSupply = getAverageVehicleSupply(getNumberForSettlement(settlement));
		
		owner.setSupplyScore(this, totalSupply);
			
		double newProjDemand = determineVehicleProjectedDemand(owner, settlement);
				
		newProjDemand = MathUtils.between(newProjDemand, LOWEST_PROJECTED_VALUE, HIGHEST_PROJECTED_VALUE);
		
		double projected = newProjDemand * flattenDemand;
		
		double projectedCache = owner.getProjectedDemandScore(this);
		if (projectedCache == INITIAL_VEHICLE_DEMAND) {
			projectedCache = projected;
		}
		else {
			projectedCache = .01 * projected + .99 * projectedCache;
		}
		
		owner.setProjectedDemandScore(this, projectedCache);
	
		double average = computeVehiclePartsCost(owner);
		
		double tradeDemand = determineTradeVehicleValue(owner, settlement);
		
		// Gets the repair demand
		// Note: need to look into parts and vehicle reliability in MalfunctionManager 
		// to derive the repair value. 
		// Look at each part in vehicleType
		repairDemand = (owner.getMaintenanceLevel() + owner.getRepairLevel())/2.0 
				* owner.getDemandScore(this);
		
		// Note: the ceiling uses projected, not projectedCache
		double ceiling = projected + tradeDemand + repairDemand;
		
		double totalDemand = previousDemand;
		
		if (previousDemand == INITIAL_VEHICLE_DEMAND) {
			totalDemand = .5 * average 
						+ .1 * repairDemand
						+ .2 * projectedCache 
						+ .2 * tradeDemand;
		}

//		else {
//			// Intentionally lose some values over time
//			totalDemand = .993 * previousDemand
//						+ .005  * projectedCache
//						+ .0002 * average 
//						+ .0002 * repairDemand
//						+ .0002 * tradeDemand;
//		}

		// If less than 1, graduating reach toward one 
		if (totalDemand < ceiling || totalDemand < 1) {
			// Increment projectedDemand
			totalDemand *= 1.003;
		}
		// If less than 1, graduating reach toward one 
		else if (totalDemand > ceiling) {
			// Decrement projectedDemand
			totalDemand *= 0.997;
		}		
		
		owner.setDemandScore(this, totalDemand);
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
				var id = GoodsUtil.getGood(iName);
				double value = owner.getGoodValuePoint(id.getID());
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
		return Math.sqrt(0.1 + supplyStored);
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
		    case DELIVERY_DRONE, CARGO_DRONE, PASSENGER_DRONE -> DRONE_FACTOR;
		    case LUV -> LUV_FACTOR;
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
		demand += MathUtils.between(JobUtil.numJobs(JobType.PILOT, settlement) * 1.1, 1, 100);

		// Add demand for mining missions by engineers.
		demand += MathUtils.between(JobUtil.numJobs(JobType.TRADER, settlement) * 1.2, 1, 100);

		double supply = getNumberForSettlement(settlement);
		if (!buy)
			supply--;
		if (supply < 1)
			supply = 1;
		
		return settlement.getPopulationFactor() / demand / supply * DRONE_FACTOR;
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
		demand +=  MathUtils.between(JobUtil.numJobs(JobType.AREOLOGIST, settlement) * 1.3, 1, 100);

		// Add demand for construction missions by architects.
		demand +=  MathUtils.between(JobUtil.numJobs(JobType.ARCHITECT, settlement) * 1.2, 1, 100);

		// Add demand for mining missions by engineers.
		demand +=  MathUtils.between(JobUtil.numJobs(JobType.ENGINEER, settlement) * 1.1, 1, 100);

		double supply = getNumberForSettlement(settlement);
		if (!buy)
			supply--;
		if (supply < 1)
			supply = 1;
	
		return settlement.getPopulationFactor() / demand / supply * LUV_FACTOR;
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
				partDemand += owner.getDemandScoreWithID(id);
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
		
		double demand = settlement.getPopulationFactor();
		
		switch(missionType) {
		case CONSTRUCTION ->
			demand /= 1 + JobUtil.numJobs(JobType.ARCHITECT, settlement);
		
		case TRAVEL_TO_SETTLEMENT, RESCUE_SALVAGE_VEHICLE ->
			demand /= 1 + JobUtil.numJobs(JobType.PILOT, settlement);

		case COLLECT_ICE ->
			demand /= MathUtils.between(owner.getDemandScore(GoodsUtil.getGood(ResourceUtil.ICE_ID)), 1, 100);
		
		case TRADE, DELIVERY ->
			demand /= 1 + JobUtil.numJobs(JobType.TRADER, settlement);
		
		case COLLECT_REGOLITH ->
			demand /= MathUtils.between(owner.getDemandScore(GoodsUtil.getGood(ResourceUtil.REGOLITH_ID)), 1, 100);
		
		case MINING, AREOLOGY, EXPLORATION ->
			demand /= 1 + JobUtil.numJobs(JobType.AREOLOGIST, settlement);
		
		case BIOLOGY ->
			demand /= 1 + JobUtil.numJobs(JobType.ASTROBIOLOGIST, settlement);
		
		case METEOROLOGY ->
			demand /= 1 + JobUtil.numJobs(JobType.METEOROLOGIST, settlement);
			default -> { // Nothing to do 
				}
		}

		return demand;
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
					if (v.getLabTechSpecialties().contains(ScienceType.ASTROBIOLOGY)) {
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
