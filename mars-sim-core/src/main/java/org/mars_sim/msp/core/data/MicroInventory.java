/*
 * Mars Simulation Project
 * MicroInventory.java
 * @date 2021-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;

/**
 * The MicroInventory class represents a simple resource storage solution.
 */
public class MicroInventory implements Serializable {

	static final class ResourceStored implements Serializable {
		/** default serial id. */
		private static final long serialVersionUID = 1L;
		double capacity;
		double storedAmount = 0;
		int quantity = 0;
		
		ResourceStored(double capacity) {
			super();
			this.capacity = capacity;
		}
	}
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MicroInventory.class.getName());

	/** The owner of this micro inventory. */
	private Loggable owner;
	/** A map of resources. */
	private Map<Integer, ResourceStored> storageMap = new HashMap<>();
	
	private double totalMass = 0D;
	
	public MicroInventory(Loggable owner) {
		this.owner = owner;
	}
	
	/**
     * Gets the capacity of this resource that this container can hold.
     * @return capacity (kg).
     */
    public double getCapacity(int resource) {
		ResourceStored s = storageMap.get(resource);
		if (s != null) {
			return s.capacity;
		}
		return 0;
    }
    
	/**
	 * Sets the capacity of a particular resource
	 * 
	 * @param resource
	 * @param capacity
	 */
	public void setCapacity(int resource, double capacity) {
		storageMap.put(resource, new ResourceStored(capacity));
	}
	 
	/**
	 * Gets the total weight of the stored resources
	 * 
	 * @return mass [kg]
	 */
	public double getStoredMass() {
		return totalMass;
	}
	
	/**
	 * Is this suit empty ?
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return totalMass == 0D;
	}

	/**
	 * Is this suit empty of this resource ?
	 * 
	 * @param resource
	 * @return
	 */
	public boolean isEmpty(int resource) {
		ResourceStored s = storageMap.get(resource);
		return (s == null) || (s.storedAmount == 0D);
	}
	
	/**
	 * Stores the resource
	 * 
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public double storeAmountResource(int resource, double quantity) {
		ResourceStored s = storageMap.get(resource);
		if (s == null) {
			return quantity;
		}
		double remaining = s.capacity - s.storedAmount;
		double excess = 0D;
		if (remaining < quantity) {
			excess = quantity - remaining;
			quantity = remaining;
			// THis generates warnings during collect ice & regolith because the Task do not check the capacity before digging
//			String name = ResourceUtil.findAmountResourceName(resource);
//			logger.warning(owner, "Storage is full. Excess " + Math.round(excess * 1_000.0)/1_000.0 + " kg " + name + ".");
		}

		s.storedAmount += quantity;

		updateAmountResourceTotalMass();
		
		return excess;
	}
	
	/**
	 * Stores the item resource
	 * 
	 * @param item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public int storeItemResource(int resource, int quantity) {
		ResourceStored s = storageMap.get(resource);
		if (s == null) {
			return quantity;
		}
		
		double mass = ItemResourceUtil.findItemResource(resource).getMassPerItem();
//		double excess = 0;
		int num = (int) (s.capacity / (quantity * mass));

		if (num > 0) {
		
			if (quantity != num) {
				String name = ((Unit)owner).findItemResourceName(resource);
				logger.warning(owner, "Can only store " + quantity + "x " + name + " and return the excess " + (quantity - num) + "x " + name + ".");
			}
			
//			excess = s.capacity - num * mass;
			s.quantity = num;		
		}
		
		else {
			double excess = quantity * mass - s.capacity;
			String name = ((Unit)owner).findItemResourceName(resource);
			logger.warning(owner, "Cannot store " + quantity + " " + name + ", lacking " + Math.round(excess * 1_000.0)/1_000.0 + " kg " + name + ".");
		}

		updateItemResourceTotalMass();
		
		return num;
	}
	
	/**
	 * Recalculate the amount resource total mass.
	 */
	private void updateAmountResourceTotalMass() {
		totalMass = storageMap.values().stream().mapToDouble(r -> r.storedAmount).sum();	
	}

	/**
	 * Recalculate the item resource total mass.
	 */
	private void updateItemResourceTotalMass() {
		double result = 0;
		for (int resource: storageMap.keySet()) {
			int q = storageMap.get(resource).quantity;
			result += ItemResourceUtil.findItemResource(resource).getMassPerItem() * q;
		}
		
		totalMass = result;
	}

	/**
	 * Retrieves the resource 
	 * 
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public double retrieveAmountResource(int resource, double quantity) {
		ResourceStored s = storageMap.get(resource);
		if (s == null) {
			return quantity;
		}

		double shortfall = 0D;
		double remaining = s.storedAmount - quantity;
		if (remaining < 0) {
			String name = ((Unit)owner).findAmountResourceName(resource);
			logger.warning(owner, 10_000L, "Just retrieved all " + quantity + " kg of " 
					+ name + ". Lacking " + Math.round(-remaining * 10.0)/10.0 + " kg.");
			shortfall = -remaining;
			remaining = 0;
		}

		s.storedAmount = remaining;
		// Update the total mass
		updateAmountResourceTotalMass();
		return shortfall;
	}
	
	/**
	 * Retrieves the item resource 
	 * 
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public double retrieveItemResource(int resource, int quantity) {
		ResourceStored s = storageMap.get(resource);
		if (s == null) {
			return quantity;
		}

		int shortfall = 0;
		int remaining = s.quantity - quantity;
		if (remaining < 0) {
			String name = ((Unit)owner).findAmountResourceName(resource);
			logger.warning(owner, 10_000L, "Just retrieved all " + quantity + " of " 
					+ name + ". Lacking " + Math.round(-remaining * 10.0)/10.0 + ".");
			shortfall = -remaining;
			remaining = 0;
		}

		s.quantity = remaining;
		// Update the total mass
		updateItemResourceTotalMass();
		return shortfall;
	}
	
	/**
	 * What resources are stored ?
	 * @return
	 */
	public Set<Integer> getResourcesStored() {
		return Collections.unmodifiableSet(storageMap.keySet());
	}
	
	/**
	 * Obtains the remaining storage space of a particular amount resource
	 * 
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceRemainingCapacity(int resource) {
		ResourceStored s = storageMap.get(resource);
		if (s != null) {
			return s.capacity - s.storedAmount;
		}
		return 0;
	}
	
	/**
	 * Gets the quantity of the amount resource stored
	 * 
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceStored(int resource) {
		ResourceStored s = storageMap.get(resource);
		if (s != null) {
			return s.storedAmount;
		}
		return 0;
	}

	/**
	 * Gets the quantity of the item resource stored
	 * 
	 * @param resource
	 * @return quantity
	 */
	public double getItemResourceStored(int resource) {
		ResourceStored s = storageMap.get(resource);
		if (s != null) {
			return s.quantity;
		}
		return 0;
	}
	
	/**
	 * Does this resource exist in storage ?
	 * 
	 * @param resource
	 * @return
	 */
	public boolean isResourceSupported(int resource) {
		return storageMap.containsKey(resource);
	}

	/**
	 * Clean this container for future use
	 */
	public void clean() {
		storageMap.clear();
	}
	
	public void destroy() {
		storageMap = null;
	}
}
