/*
 * Mars Simulation Project
 * BinFactory.java
 * @date 2023-07-30
 * @author Manny Kung
 */
package com.mars_sim.core.equipment;

import java.util.EnumMap;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Unit;
import com.mars_sim.core.goods.GoodType;
import com.mars_sim.core.manufacture.ManufactureConfig;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;

/**
 * The factory class for bin containers.
 */
public final class BinFactory {

	// default logger.
	// May bring back private static finsal SimLogger logger = SimLogger.getLogger(BinFactory.class.getName())
	// Default mass for uncalculated types
	protected static final double DEFAULT_MASS = 0.0001;
	
	private static final EnumMap<BinType, Double> massMap = new EnumMap<>(BinType.class);
	
	private static ManufactureConfig manufactureConfig;
	
	/**
	 * Private constructor.
	 */
	private BinFactory() {
	}
	
	/**
	 * Gets the bin type of the container needed to hold a particular resource.
	 * 
	 * @param resourceID the id of the resource to hold.
	 * @return bin type or null if none found.
	 */
	public static BinType getBinTypeForResource(int resourceID) {
		AmountResource ar = ResourceUtil.findAmountResource(resourceID);
		// Crop, Rock
		GoodType goodType = ar.getGoodType();
		
		boolean isCrop = goodType == GoodType.CROP;
		boolean isRock = goodType == GoodType.ROCK;
		boolean isStone = goodType == GoodType.GEMSTONE;
		if (isRock || isCrop || isStone) {
			return BinType.BASKET;
		}
		
		
		return null;
	}

	/**
	 * Calculates the mass of the output of a process.
	 * 
	 * @param type
	 * @param processName
	 * @param productName
	 * @return
	 */
    public static double calculateUsualMass(BinType type, String processName, String productName) {	
		double massPerItem = 0;
		BinType binType = BinType.convertName2Enum(productName);
		
		if (massMap.isEmpty() || !massMap.containsKey(binType)) {

			double mass = 0;
			double quantity = 0;
	    	ManufactureProcessInfo manufactureProcessInfo = null;

	    	if (manufactureConfig == null) {
	    		manufactureConfig = SimulationConfig.instance().getManufactureConfiguration();
	    	}
	    			
	    	for (ManufactureProcessInfo info : manufactureConfig.getManufactureProcessList()) {
	    		if (info.getName().equalsIgnoreCase(processName)) {
	    			manufactureProcessInfo = info;
	    			break;
		        }
	    	}
			
	    	if (manufactureProcessInfo != null) {
				// Calculate total mass as the summation of the multiplication of the quantity and mass of each part 
				mass = manufactureProcessInfo.calculateTotalInputMass();
				// Calculate output quantity
				quantity = manufactureProcessInfo.calculateOutputQuantity(productName);		
				
				// mass/quantity is mass per unit e.g. need 7.8 kg of polyethylene in order to make 12 baskets 
				massPerItem = mass/quantity;
				// Save the key value pair onto the weights Map
				massMap.put(binType, massPerItem);
	    	}
		}

		return massPerItem;
    }
	
	/**
	 * Creates a new bin container. This may be temporary to be shared.
	 * 
	 * @param name
	 * @param settlement
	 * @param temp
	 * @return
	 */
	public static synchronized Bin createBins(String binName, Settlement settlement) {

		BinType type = BinType.convertName2Enum(binName);

		AmountResourceBin binMap = findBinMap(settlement,
											settlement.getEquipmentInventory().getAmountResourceBinSet(), type);
    	
		double usualMass = 0;
		
		if (massMap.isEmpty() || !massMap.containsKey(type)) {
			usualMass = calculateUsualMass(type, "Make " + type.getName(), type.getName());
    		massMap.put(type, usualMass);
    	}
		else {
    		usualMass = massMap.get(type);
    	}
    	
		// Add this new bin to the bin map
		int id = binMap.addBin(type, null, 0.0);
		// Get the instance of this new bin
		return binMap.getBin(id);		
	}

	/**
	 * Find a BinMap for certain type.It will check existing map and create a new one if needed
	 * @param owner
	 * @param availableMaps
	 * @param type
	 * @return
	 */
	public static AmountResourceBin findBinMap(Unit owner, Set<AmountResourceBin> availableMaps,
			BinType type) {
		
		AmountResourceBin binMap = availableMaps.stream()
									.filter(a -> a.getBinType() == type)
									.findFirst()
									.orElse(null);

		if (binMap == null) {
			var capacity = getBinCapacity(type);
			binMap = new AmountResourceBin(owner, capacity, type);
		}
		return binMap;
	}

	/**
	 * Finds an empty bin container.
	 * 
	 * @param owner	Source of bins to search for
	 * @param binType
	 * @return bin container
	 */
	public static Bin findEmptyBin(BinHolder owner, BinType binType) {
		for(AmountResourceBin arb : owner.getAmountResourceBinSet()) {
			if (arb.getBinType() == binType) {
				for(Bin b : arb.getBinMap().values()) {
					if (b.isEmpty()) {
						return b;
					}
				}
			}
		}

		return null;
	}
	
	/**
	 * Gets the capacity of the bin.
	 * 
	 * @param BinType the bin type.
	 * @return capacity (kg).
	 */
	public static double getBinCapacity(BinType type) {
		switch(type) {
		case BASKET:
			return 100D;
		case CRATE:
			return 100D;
		case POT:
			return 100D;			
		default:
			throw new IllegalArgumentException("'" + type + "' is not a bin.");
		}
	}
	
	/**
	 * Gets the empty/usual mass.
	 * 
	 * @param type
	 * @return
	 */
	public static double getUsualMass(BinType type) {
		if (massMap.containsKey(type))
			return massMap.get(type);
		return DEFAULT_MASS;
	}
	
//	/**
//	 * Gets the least full container..
//	 * 
//	 * @param owner	Source of containers to search
//	 * @param containerType Preferred Type of container to look for
//	 * @param resource  the resource for capacity.
//	 * @return container.
//	 */
//	public static BaseBin findLeastFullBin(BinHolder owner,
//												BinType binType,
//												   int resource) {
//		BaseBin result = null;
//		double mostCapacity = 0D;
//
//		for(AmountResourceBin b : owner.getAmountResourceBinSet()) {
//			if (b.getBinType() == binType) {
//				Container container = (BinType)b ;
//				double remainingCapacity = container.getAmountResourceRemainingCapacity(resource);
//				if (remainingCapacity >= mostCapacity) {
//					result = container;
//					mostCapacity = remainingCapacity;
//				}
//			}
//		}
//
//		return result;
//	}
}
