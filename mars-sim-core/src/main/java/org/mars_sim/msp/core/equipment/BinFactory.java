/*
 * Mars Simulation Project
 * BinFactory.java
 * @date 2023-07-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.equipment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.goods.GoodType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.manufacture.ManufactureConfig;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The factory class for bin containers.
 */
public final class BinFactory {

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(BinFactory.class.getName());

	
	private static final String crate = "make crate";
	private static final String basket = "make basket";
	private static final String pot = "make pot";
	
	private static Map<String, Double> weights = new HashMap<>();
	
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
	 * Gets the empty mass of the bin.
	 *
	 * @param type the bin type.
	 * @return empty mass (kg).
	 * @throws Exception if bin mass could not be determined.
	 */
	public static double getBinMass(BinType type) {
		String productName = type.getName();
		
		switch (type) {
					
			case BASKET:
				return calculateMass(basket, productName);
			case CRATE:
				return calculateMass(crate, productName);
			case POT:
				return calculateMass(pot, productName);
			default:
				throw new IllegalStateException("Class for bin '" + type + "' could not be found.");
		}		
	}

	/**
	 * Calculates the mass of the output of a process.
	 * 
	 * @param processName
	 * @return
	 */
    public static double calculateMass(String processName, String productName) {	
		if (weights.isEmpty() || !weights.containsKey(processName)) {
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
	
			// Calculate total mass as the summation of the multiplication of the quantity and mass of each part 
			mass = manufactureProcessInfo.calculateTotalInputMass();
			// Calculate output quantity
			quantity = manufactureProcessInfo.calculateOutputQuantity(productName);			
			// Save the key value pair onto the weights Map
			weights.put(processName, mass/quantity);
			
			logger.info(productName + " - input mass: " + mass + "  output quantity: " 
					+ quantity + "   mass per item: " + Math.round(mass/quantity * 10.0)/10.0);
			
			return mass;
		}

		return weights.get(processName);
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

		Set<AmountResourceBin> binSet = settlement.getEquipmentInventory().getAmountResourceBinSet();
	
		AmountResourceBin binMap = null;
		Bin newBin = null;
		boolean hasIt = false;
		
		for (AmountResourceBin arb : binSet) {
			if (arb.getBinType() == type) {
				hasIt = true;
				binMap = arb;
				break;
			}
		}

		switch (type) {
			case CRATE:
				if (!hasIt) {
					// Create a bin map
					binMap = new Crate(settlement, getBinCapacity(type));
				}	
				break;
	
			case BASKET:
				if (!hasIt) {
					// Create a bin map
					binMap = new Basket(settlement, getBinCapacity(type));
				}	
				break;
	
			case POT:		
				if (!hasIt) {
					// Create a bin map
					binMap = new Pot(settlement, getBinCapacity(type));
				}	
				break;
				
			default:
				throw new IllegalStateException("Bin type '" + type + "' could not be constructed.");
		}

		if (!hasIt) {
			// Set owner
			binMap.setOwner(settlement);
			// Set bin type
			binMap.setBinType(type);
		}
		
		// Add this new bin to the bin map
		int id = binMap.addBin(type, null, 0.0);
		// Get the instance of this new bin
		newBin = binMap.getBin(id);
		
		return newBin;
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
