/*
 * Mars Simulation Project
 * BinGood.java
 * @date 2023-07-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.goods;

import java.util.Collection;

import org.mars_sim.msp.core.equipment.BinType;
import org.mars_sim.msp.core.equipment.BinFactory;
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
 * This represents the attributes of how a bin can be traded.
 */
public class BinGood extends Good {
	
	private static final long serialVersionUID = 1L;
	
	private static final int PROJECTED_GAS_CANISTERS = 10;
	private static final int PROJECTED_THERMAL_BOTTLE = 1;
	private static final double PROJECTED_WHEELBARROW = 1;
	
	private static final double GAS_CANISTER_DEMAND = 1;
	
	private static final double INITIAL_DEMAND = 0;
	private static final double INITIAL_SUPPLY = 0;
	
	private static final double CONTAINER_VALUE = .1;
	
    private BinType binType;

    BinGood(BinType type) {
        super(type.getName(), BinType.getResourceID(type));
        this.binType = type;
    }

    @Override
    public GoodCategory getCategory() {
        return GoodCategory.BIN;
    }

	public BinType getBinType() {
		return binType;
	}

    @Override
    public double getMassPerItem() {
        return BinFactory.getBinMass(binType);
    }

    @Override
    public GoodType getGoodType() {
        return GoodType.BIN;
    }

    @Override
    protected double computeCostModifier() {
        return CONTAINER_VALUE;
    }

    @Override
    public double getNumberForSettlement(Settlement settlement) {
		double number = 0D;
//
//		// Get number of the bin in settlement storage.
//		number += settlement.findNumEmptyContainersOfType(binType, false);
//
//		// Get number of bin out on mission vehicles.
//		for (Mission mission : missionManager.getMissionsForSettlement(settlement)) {
//			if (mission instanceof VehicleMission vehicleMission) {
//				Vehicle vehicle = vehicleMission.getVehicle();
//				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement()))
//					number += vehicle.findNumEmptyContainersOfType(binType, false);
//			}
//		}
//
//		// Get number of bin carried by people on EVA.
//		for(Person person : settlement.getAllAssociatedPeople()) {
//			if (person.isOutside())
//				number += person.findNumEmptyContainersOfType(binType, false);
//		}
//
//		// Get the number of bin that will be produced by ongoing manufacturing
//		// processes.
//		number += getManufacturingProcessOutput(settlement);
//
		return number;
    }

    @Override
    double getPrice(Settlement settlement, double value) {
		// For Bin   		
    	double mass = 0;
		double quantity = 0;
    	double factor = 0;
//        if (binType == BinType.EVA_SUIT) {
//    		mass = BinFactory.getBinMass(binType);
//    		quantity = settlement.getNumEVASuit();
//    		
//            // Need to increase the value for EVA
//    		factor = 1.2 * Math.log(mass/50.0 + 1) / (.1 + Math.log(quantity + 1));
//    	}
//    	else {
//    		// For containers
//    		mass = BinFactory.getBinMass(binType);
//    		quantity = settlement.findNumContainersOfType(binType);
//    		factor = 1.2 * Math.log(mass + 1) / (.1 + Math.log(quantity + 1));
//    	}
        return getCostOutput() * (1 + 2 * factor * Math.log(value + 1));   
    }

	@Override
	double getDefaultDemandValue() {
		return INITIAL_DEMAND;
	}

	@Override
	double getDefaultSupplyValue() {
		return INITIAL_SUPPLY;
	}

	@Override
	void refreshSupplyDemandValue(GoodsManager owner) {
		Settlement settlement = owner.getSettlement();
		
		double previousDemand = owner.getDemandValue(this);

		double totalDemand = 0;
		// Determine average demand.
		double average = determineBinDemand(owner, settlement);

		double totalSupply = getAverageBinSupply(settlement.findNumBinsOfType(binType));
				
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
	 * Determines the bin for a type of bin.
	 *
	 * @param settlement the location of this demand
	 * @return demand (# of bin).
	 */
	private double determineBinDemand(GoodsManager owner, Settlement settlement) {
		double baseDemand = 1;

		double areologistFactor = (1 + JobUtil.numJobs(JobType.AREOLOGIST, settlement)) / 3.0;

		// Determine the number of containers that are needed.
//		double containerCapacity = ContainerUtil.getContainerCapacity(binType);
//		double totalPhaseOverfill = 0D;
//
//		// Scan resources that can be held in this Container
//		for (AmountResource resource : ResourceUtil.getAmountResources()) {
//			if (ContainerUtil.getBinTypeForContainer(resource.getID()) == binType) {
//				double settlementCapacity = settlement.getAmountResourceCapacity(resource.getID());
//
//				double resourceDemand = owner.getDemandValueWithID(resource.getID());
//
//				if (resourceDemand > settlementCapacity) {
//					double resourceOverfill = resourceDemand - settlementCapacity;
//					totalPhaseOverfill += resourceOverfill;
//				}
//			}
//		}
//
//		baseDemand += totalPhaseOverfill * containerCapacity;

		double ratio = computeUsageFactor(settlement);

		switch (binType) {
			case BASKET:
				return 1; //Math.max(baseDemand * ratio *settlement.getRegolithCollectionRate() / 1_000, 1000) * areologistFactor * BASKET_DEMAND;

			case CRATE:
				return 1; //Math.max(baseDemand * ratio * CollectRegolith.REQUIRED_LARGE_BAGS, 1000) * CRATE_DEMAND;

			case POT:
				return 1; //Math.max(baseDemand * ratio * CollectIce.REQUIRED_BARRELS, 1000) * areologistFactor * POT_DEMAND;

			default:
				throw new IllegalArgumentException("Do not know how to calculate demand for " + binType + ".");
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

//		Collection<Container> binList = settlement.findContainersOfType(binType);
//
//		for (Mission mission : missionManager.getMissionsForSettlement(settlement)) {
//			if (mission instanceof VehicleMission vehicleMission) {
//				Vehicle vehicle = vehicleMission.getVehicle();
//				if ((vehicle != null) && (vehicle.getSettlement() == null)) {
//					binList.addAll(vehicle.findContainersOfType(binType));
//				}
//			}
//		}
//
		double total = 0; //binList.size();
//		for(Container c: binList) {
//			if (c.getStoredMass() > 0)
//				numUsed++;
//		}

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
	 * Gets the total supply for the bin.
	 *
	 * @param resource`
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	private static double getAverageBinSupply(double supplyStored) {
		return Math.sqrt(1 + supplyStored);
	}
	
	public void destroy() {
		binType = null;
	}
}
