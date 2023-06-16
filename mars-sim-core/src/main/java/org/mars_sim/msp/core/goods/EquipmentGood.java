/*
 * Mars Simulation Project
 * EquipmentGood.java
 * @date 2023-05-16
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import java.util.Collection;

import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.job.util.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * THis represents the attributes of how an Equipment can be traded
 */
public class EquipmentGood extends Good {
	
	private static final long serialVersionUID = 1L;
	
	private static final int PROJECTED_GAS_CANISTERS = 10;
	private static final int PROJECTED_THERMAL_BOTTLE = 1;
	private static final double PROJECTED_WHEELBARROW = 1;
	
	private static final double GAS_CANISTER_DEMAND = 1;
	private static final double SPECIMEN_BOX_DEMAND = 1.2;
	private static final double LARGE_BAG_DEMAND = .5;
	private static final double BAG_DEMAND = .1;
	private static final double BARREL_DEMAND = .2;
	private static final double THERMAL_BOTTLE_DEMAND = 0.25;
	private static final double WHEELBARROW_DEMAND = .5;
	
	private static final double INITIAL_EQUIPMENT_DEMAND = 0;
	private static final double INITIAL_EQUIPMENT_SUPPLY = 0;
	private static final double EVA_SUIT_VALUE = 50;
	private static final double CONTAINER_VALUE = .1;

    private EquipmentType equipmentType;

    EquipmentGood(EquipmentType type) {
        super(type.getName(), EquipmentType.getResourceID(type));
        this.equipmentType = type;
    }

    @Override
    public GoodCategory getCategory() {
        if (equipmentType == EquipmentType.EVA_SUIT) {
            return GoodCategory.EQUIPMENT;
        }
        
        return GoodCategory.CONTAINER;
    }

	public EquipmentType getEquipmentType() {
		return equipmentType;
	}

    @Override
    public double getMassPerItem() {
        return EquipmentFactory.getEquipmentMass(equipmentType);
    }

    @Override
    public GoodType getGoodType() {
        if (equipmentType == EquipmentType.EVA_SUIT) {
            return GoodType.EVA;
        }
        
        return GoodType.CONTAINER;
    }

    @Override
    protected double computeCostModifier() {
        if (equipmentType == EquipmentType.EVA_SUIT) {
			return EVA_SUIT_VALUE;
		}
        return CONTAINER_VALUE;
    }

    @Override
    public double getNumberForSettlement(Settlement settlement) {
		double number = 0D;

		// Get number of the equipment in settlement storage.
		number += settlement.findNumEmptyContainersOfType(equipmentType, false);

		// Get number of equipment out on mission vehicles.
		for(Mission mission : missionManager.getMissionsForSettlement(settlement)) {
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement()))
					number += vehicle.findNumEmptyContainersOfType(equipmentType, false);
			}
		}

		// Get number of equipment carried by people on EVA.
		for(Person person : settlement.getAllAssociatedPeople()) {
			if (person.isOutside())
				number += person.findNumEmptyContainersOfType(equipmentType, false);
		}

		// Get the number of equipment that will be produced by ongoing manufacturing
		// processes.
		number += getManufacturingProcessOutput(settlement);

		return number;
    }

    @Override
    double getPrice(Settlement settlement, double value) {
		// For Equipment   		
    	double mass = 0;
		double quantity = 0;
    	double factor = 0;
        if (equipmentType == EquipmentType.EVA_SUIT) {
    		mass = EquipmentFactory.getEquipmentMass(equipmentType);
    		quantity = settlement.getNumEVASuit();
    		
            // Need to increase the value for EVA
    		factor = 1.2 * Math.log(mass/50.0 + 1) / (.1 + Math.log(quantity + 1));
    	}
    	else {
    		// For containers
    		mass = EquipmentFactory.getEquipmentMass(equipmentType);
    		quantity = settlement.findNumContainersOfType(equipmentType);
    		factor = 1.2 * Math.log(mass + 1) / (.1 + Math.log(quantity + 1));
    	}
        return getCostOutput() * (1 + 2 * factor * Math.log(value + 1));   
    }

	@Override
	double getDefaultDemandValue() {
		return INITIAL_EQUIPMENT_DEMAND;
	}

	@Override
	double getDefaultSupplyValue() {
		return INITIAL_EQUIPMENT_SUPPLY;
	}

	@Override
	void refreshSupplyDemandValue(GoodsManager owner) {
		Settlement settlement = owner.getSettlement();
		double previousDemand = owner.getDemandValue(this);

		double totalDemand = 0;
		
		// Determine average demand.
		double average = determineEquipmentDemand(owner, settlement);

		double totalSupply = getAverageEquipmentSupply(settlement.findNumContainersOfType(equipmentType));
				
		owner.setSupplyValue(this, totalSupply);
		
		// This method is not using cache
		double trade = owner.determineTradeDemand(this);
		if (previousDemand == 0) {
			totalDemand = .5 * average + .5 * trade;
		}
		else {
			// Intentionally lose 2% of its value
			totalDemand = .97 * previousDemand + .005 * average + .005 * trade;
		}
				
		owner.setDemandValue(this, totalDemand);
	}

	/**
	 * Determines the demand for a type of equipment.
	 *
	 * @param settlement the locaiton of this demand
	 * @return demand (# of equipment).
	 */
	private double determineEquipmentDemand(GoodsManager owner, Settlement settlement) {
		double baseDemand = 1;

		double areologistFactor = (1 + JobUtil.numJobs(JobType.AREOLOGIST, settlement)) / 3.0;

		// Determine number of EVA suits that are needed
		if (equipmentType == EquipmentType.EVA_SUIT) {
			// Add the whole EVA Suit demand.
			baseDemand += getWholeEVASuitDemand(owner);

			return baseDemand + owner.getEVASuitMod() * EVA_SUIT_VALUE;
		}

		// Determine the number of containers that are needed.
		double containerCapacity = ContainerUtil.getContainerCapacity(equipmentType);
		double totalPhaseOverfill = 0D;

		// Scan resources that can be held in this Container
		for(AmountResource resource : ResourceUtil.getAmountResources()) {
			if (ContainerUtil.getContainerClassToHoldResource(resource.getID()) == equipmentType) {
				double settlementCapacity = settlement.getAmountResourceCapacity(resource.getID());

				double resourceDemand = owner.getDemandValueWithID(resource.getID());

				if (resourceDemand > settlementCapacity) {
					double resourceOverfill = resourceDemand - settlementCapacity;
					totalPhaseOverfill += resourceOverfill;
				}
			}
		}

		baseDemand += totalPhaseOverfill * containerCapacity;

		double ratio = computeUsageFactor(settlement);

		switch (equipmentType) {
			case BAG:
				return Math.max(baseDemand * ratio *settlement.getRegolithCollectionRate() / 1_000, 1000) * areologistFactor * BAG_DEMAND;

			case LARGE_BAG:
				return Math.max(baseDemand * ratio * CollectRegolith.REQUIRED_LARGE_BAGS, 1000) * LARGE_BAG_DEMAND;

			case BARREL:
				return Math.max(baseDemand * ratio * CollectIce.REQUIRED_BARRELS, 1000) * areologistFactor * BARREL_DEMAND;

			case SPECIMEN_BOX:
				return Math.max(baseDemand * ratio * Exploration.REQUIRED_SPECIMEN_CONTAINERS, 1000) * areologistFactor * SPECIMEN_BOX_DEMAND;

			case GAS_CANISTER:
				return Math.max(baseDemand * ratio * PROJECTED_GAS_CANISTERS, 1000) * areologistFactor * GAS_CANISTER_DEMAND;

			case THERMAL_BOTTLE:
				return Math.max(baseDemand * ratio * PROJECTED_THERMAL_BOTTLE, 1000) * areologistFactor * THERMAL_BOTTLE_DEMAND;

			case WHEELBARROW:
				return Math.max(baseDemand * ratio * PROJECTED_WHEELBARROW, 1000) * areologistFactor *WHEELBARROW_DEMAND;
							
			default:
				throw new IllegalArgumentException("Do not know how to calculate demand for " + equipmentType + ".");
		}
	}

	
	/**
	 * Computes the usage factor (the used number of container / the total number)
	 * of a type of container.
	 *
	 * @param containerType
	 * @return the usage factor
	 */
	private double computeUsageFactor(Settlement settlement) {
		int numUsed = 0;

		Collection<Container> equipmentList = settlement.findContainersOfType(equipmentType);

		for(Mission mission : missionManager.getMissionsForSettlement(settlement)) {
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && (vehicle.getSettlement() == null)) {
					equipmentList.addAll(vehicle.findContainersOfType(equipmentType));
				}
			}
		}

		double total = equipmentList.size();
		for(Container c: equipmentList) {
			if (c.getStoredMass() > 0)
				numUsed++;
		}

		return  (1 + numUsed) / (1 + total);
	}

	/**
	 * Gets the EVA suit demand from its part.
	 *
	 * @param owner Owner of Goods
	 * @return demand
	 */
	private static double getWholeEVASuitDemand(GoodsManager owner) {
		double demand = 0;
	
		if (ItemResourceUtil.evaSuitPartIDs != null && !ItemResourceUtil.evaSuitPartIDs.isEmpty()) {
			for (int id : ItemResourceUtil.evaSuitPartIDs) {
				demand += owner.getDemandValueWithID(id);
			}
		}
		return demand;
	}

	/**
	 * Gets the total supply for the equipment.
	 *
	 * @param resource`
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	private static double getAverageEquipmentSupply(double supplyStored) {
		return Math.sqrt(1 + supplyStored);
	}
	
}
