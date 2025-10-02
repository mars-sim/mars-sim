/*
 * Mars Simulation Project
 * BinGood.java
 * @date 2023-07-30
 * @author Manny Kung
 */
package com.mars_sim.core.goods;

import java.util.Collection;

import com.mars_sim.core.equipment.Bin;
import com.mars_sim.core.equipment.BinFactory;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;

/**
 * This represents the attributes of how a bin can be traded.
 */
public class BinGood extends Good {
	
	private static final long serialVersionUID = 1L;
	
	private static final double PROJECTED_CRATE = 0;
	private static final double PROJECTED_BASKET = 0;
	private static final double PROJECTED_POT = 0;
	
	private static final double INITIAL_DEMAND = 0;
	private static final double INITIAL_SUPPLY = 0;
	
	private static final double CONTAINER_VALUE = 0;
	
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

		// Get number of the bin in settlement storage.
//		number += settlement.findENumEmptyBins(binType, false);
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

		// Get the number of bin that will be produced by ongoing manufacturing
		// processes.
//		number += getManufacturingProcessOutput(settlement);

		return number;
    }

    @Override
    double calculatePrice(Settlement settlement, double value) {
		// For Bin   		
    	double mass = 0; // BinFactory.getBinMass(binType);
		double quantity = 0; //settlement.findNumContainersOfType(binType);
    	double factor = 1.2 * Math.log(mass + 1) / (.1 + Math.log(quantity + 1));
        double price = getCostOutput() * factor;  
        setPrice(price);
	    return price;
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
	void refreshSupplyDemandScore(GoodsManager owner) {
		Settlement settlement = owner.getSettlement();
		
		double previousDemand = owner.getDemandScore(this);

		double totalDemand = previousDemand;
		
		// Determine average demand.
		double average = determineBinDemand(owner, settlement);

		double totalSupply = getAverageBinSupply(settlement.findNumBinsOfType(binType));
		
		owner.setSupplyScore(this, totalSupply);
		
		// This method is not using cache
		double tradeDemand = owner.determineTradeDemand(this);
		
		double ceiling = average + tradeDemand;
		
		if (previousDemand == INITIAL_DEMAND) {
			totalDemand = .5 * average + .5 * tradeDemand;
		}
//		else {
//			// Intentionally lose some of its value
//			totalDemand = .99 * previousDemand 
//						+ .003 * average 
//						+ .005 * trade;
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
	 * Determines the bin for a type of bin.
	 *
	 * @param settlement the location of this demand
	 * @return demand (# of bin).
	 */
	private double determineBinDemand(GoodsManager owner, Settlement settlement) {
		double baseDemand = 1;

		double botanistFactor = (1 + JobUtil.numJobs(JobType.BOTANIST, settlement)) / 3.0;	
		double cookFactor = (1 + JobUtil.numJobs(JobType.CHEF, settlement)) / 3.0;	
		double traderFactor = (1 + JobUtil.numJobs(JobType.TRADER, settlement)) / 3.0;
		
		double numAvailable = settlement.findNumBinsOfType(binType);
		
		// Determine the number of bins that are needed.
		double binCapacity = BinFactory.getBinCapacity(binType);
		double totalPhaseOverfill = 0D;

		// Scan resources that can be held in this Container
		for (AmountResource resource : ResourceUtil.getAmountResources()) {
			if (BinFactory.getBinTypeForResource(resource.getID()) == binType) {
				double settlementCapacity = settlement.getSpecificCapacity(resource.getID());

				double resourceDemand = owner.getDemandScoreWithID(resource.getID());

				if (resourceDemand > settlementCapacity) {
					double resourceOverfill = resourceDemand - settlementCapacity;
					totalPhaseOverfill += resourceOverfill;
				}
			}
		}

		baseDemand += totalPhaseOverfill * binCapacity - numAvailable;

		double ratio = computeUsageFactor(settlement);

		switch (binType) {
			case BASKET:
				return Math.max(baseDemand * ratio, 1000) * cookFactor * PROJECTED_BASKET;

			case CRATE:
				return Math.max(baseDemand * ratio, 1000) * traderFactor * PROJECTED_CRATE;

			case POT:
				return Math.max(baseDemand * ratio, 1000) * botanistFactor * PROJECTED_POT;

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
		double totalAmount = 0;

		Collection<Bin> binList = settlement.findBinsOfType(binType);
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
		double total = binList.size();
		for(Bin b: binList) {
			totalAmount += b.getAmount();
		}

		return totalAmount / (1 + total);
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
