/*
 * Mars Simulation Project
 * VehicleGood.java
 * @date 2023-05-16
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.job.util.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleSpec;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * This represents a type of Vehicle that can be traded.
 */
class VehicleGood extends Good {
	
	private static final long serialVersionUID = 1L;
	
//	private static SimLogger logger = SimLogger.getLogger(VehicleGood.class.getName());

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

    private static final double SPEED_TO_DISTANCE = 2D / 60D / 60D / MarsClock.convertSecondsToMillisols(1D) * 1000D;

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
        switch(vehicleType) {
		case DELIVERY_DRONE, LUV:
			goodType = GoodType.VEHICLE_SMALL;
            break;

		case EXPLORER_ROVER, LONG_RANGE_EXPLORER:
            goodType = GoodType.VEHICLE_MEDIUM;
            break;

		case TRANSPORT_ROVER, CARGO_ROVER:
            goodType = GoodType.VEHICLE_HEAVY;
            break;

        default:
            throw new IllegalArgumentException(vs.getName() + " has unknown vehicle type.");
        }

		this.theoreticalRange = getVehicleRange(vs);

        // Pre-calculate manufactureProcessInfos
		List<ManufactureProcessInfo> infos = new ArrayList<>();
		Iterator<ManufactureProcessInfo> i = ManufactureUtil.getAllManufactureProcesses().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			for (String n : process.getOutputNames()) {
				if (getName().equalsIgnoreCase(n)) {
					infos.add(process);
				}
			}
		}
		
		manufactureProcessInfos = infos;
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
            case DELIVERY_DRONE:
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
    double getPrice(Settlement settlement, double value) {
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
			
		// Doesn't use cache value sin this method
		double projected = determineVehicleProjectedDemand(owner, settlement);
		
		double average = computeVehiclePartsCost(owner);
		
		double trade = determineTradeVehicleValue(owner, settlement);
		
		double totalDemand;
		if (previousDemand == 0) {
			totalDemand = .5 * average + .25 * projected + .25 * trade;
		}

		else {
			// Intentionally lose 2% of its value
			totalDemand = .97 * previousDemand 
					+ .003 * average 
					+ .003 * projected 
					+ .003 * trade;
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
			List<ManufactureProcessItem> itemList = process.getInputList();

			for (ManufactureProcessItem pi : itemList) {
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

		else if (vehicleType == VehicleType.DELIVERY_DRONE) {
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

        double transportationFactor = owner.getTransportationFactor();
		switch (vehicleType) {
            case CARGO_ROVER:
			    demand *= (.5 + transportationFactor) * CARGO_VEHICLE_FACTOR;
                break;
		
            case TRANSPORT_ROVER:
			    demand *= (.5 + transportationFactor) * TRANSPORT_VEHICLE_FACTOR;
                break;
            case LONG_RANGE_EXPLORER, EXPLORER_ROVER:
			    demand *= (.5 + transportationFactor) * EXPLORER_VEHICLE_FACTOR;
                break;

		    case DELIVERY_DRONE:
			    demand *= (.5 + transportationFactor) * DRONE_VEHICLE_FACTOR;
                break;

		    case LUV:
			    demand *= (.5 + transportationFactor) * LUV_VEHICLE_FACTOR;
                break;
        }
		return demand;
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

		int typeID = vs.getTypeID();
		
		double partDemand = 0;
		
		Set<Integer> setIDs = ItemResourceUtil.vehiclePartIDs.get(typeID);
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
			case BUILDING_CONSTRUCTION:
			case BUILDING_SALVAGE:
				return JobUtil.numJobs(JobType.ARCHITECT, settlement);
		
		case TRAVEL_TO_SETTLEMENT:
		case RESCUE_SALVAGE_VEHICLE:
			return JobUtil.numJobs(JobType.PILOT, settlement)
					* ((double) settlement.getNumCitizens() 
					/ (double) settlement.getPopulationCapacity());

		case COLLECT_ICE:
			return Math.min(owner.getDemandValue(GoodsUtil.getGood(ResourceUtil.iceID)), 100);
		
		case TRADE:
		case DELIVERY:
			return JobUtil.numJobs(JobType.TRADER, settlement);
		
		case COLLECT_REGOLITH:
			return Math.min(owner.getDemandValue(GoodsUtil.getGood(ResourceUtil.regolithID)), 100);
		
		case MINING:
		case AREOLOGY:
		case EXPLORATION:
			return JobUtil.numJobs(JobType.AREOLOGIST, settlement);
		
		case BIOLOGY:
			return JobUtil.numJobs(JobType.BIOLOGIST, settlement);
		
		case METEOROLOGY:
			return JobUtil.numJobs(JobType.METEOROLOGIST, settlement);
		
		case EMERGENCY_SUPPLY:
            return 1D; // Simplify code as a temp measure
			//return Math.max(unitManager.getSettlementNum() - 1D, 0);
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

		case COLLECT_ICE:
		case COLLECT_REGOLITH: {
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

		case TRADE:
		case EMERGENCY_SUPPLY: {
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
		double range = 0D;

		double fuelCapacity = v.getCargoCapacity(ResourceUtil.methanolID);
		double fuelEfficiency = v.getDriveTrainEff();
		range = fuelCapacity * fuelEfficiency * Vehicle.RMFC_CONVERSION_EFFICIENCY;

        int crewSize = v.getCrewSize();
        if (crewSize > 0) {
            double baseSpeed = v.getBaseSpeed();
            double distancePerSol = baseSpeed / SPEED_TO_DISTANCE;
            
            // Check food capacity as range limit.
//            System.out.println("personConfig is " + personConfig);
            double foodConsumptionRate = personConfig.getFoodConsumptionRate();
            double foodCapacity = v.getCargoCapacity(ResourceUtil.foodID);
            double foodSols = foodCapacity / (foodConsumptionRate * crewSize);
            double foodRange = distancePerSol * foodSols / 3D;
            if (foodRange < range)
                range = foodRange;

            // Check water capacity as range limit.
            double waterConsumptionRate = personConfig.getWaterConsumptionRate();
            double waterCapacity = v.getCargoCapacity(ResourceUtil.waterID);
            double waterSols = waterCapacity / (waterConsumptionRate * crewSize);
            double waterRange = distancePerSol * waterSols / 3D;
            if (waterRange < range)
                range = waterRange;

            // Check oxygen capacity as range limit.
            double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
            double oxygenCapacity = v.getCargoCapacity(ResourceUtil.oxygenID);
            double oxygenSols = oxygenCapacity / (oxygenConsumptionRate * crewSize);
            double oxygenRange = distancePerSol * oxygenSols / 3D;
            if (oxygenRange < range)
                range = oxygenRange;
        }

		return range;
	}
}
