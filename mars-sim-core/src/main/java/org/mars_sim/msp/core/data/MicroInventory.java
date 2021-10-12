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
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * The MicroInventory class represents a simple resource storage solution.
 */
public class MicroInventory implements Serializable {

	static final class ResourceStored implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		double capacity;
		double stored = 0;
		
		ResourceStored(double capacity) {
			super();
			this.capacity = capacity;
		}
	}
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MicroInventory.class.getName());

	
	private Loggable owner;
	/** A map of resources with quantity. */
	private Map<Integer, ResourceStored> quantityMap = new HashMap<>();
	private double totalMass = 0D;
	
	public MicroInventory(Loggable owner) {
		this.owner = owner;
	}
	
	/**
     * Gets the capacity of this resource that this container can hold.
     * @return capacity (kg).
     */
    public double getCapacity(int resource) {
		ResourceStored s = quantityMap.get(resource);
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
		quantityMap.put(resource, new ResourceStored(capacity));
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
		ResourceStored s = quantityMap.get(resource);
		return (s == null) || (s.stored == 0D);
	}
	
	/**
	 * Stores the resource
	 * 
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public double storeAmountResource(int resource, double quantity) {
		ResourceStored s = quantityMap.get(resource);
		if (s == null) {
			return quantity;
		}
		double remaining = s.capacity - s.stored;
		double excess = 0D;
		if (remaining < quantity) {
			excess = quantity - remaining;
			quantity = remaining;
			// THis generates warnings during collect ice & regolith because the Task do not check the capacity before digging
//			String name = ResourceUtil.findAmountResourceName(resource);
//			logger.warning(owner, "Storage is full. Excess " + Math.round(excess * 1_000.0)/1_000.0 + " kg " + name + ".");
		}

		s.stored += quantity;

		updateTotal();
		
		return excess;
	}
	
	/**
	 * Recalculate the total mass.
	 */
	private void updateTotal() {
		totalMass = quantityMap.values().stream().mapToDouble(r -> r.stored).sum();
	}

	/**
	 * Retrieves the resource 
	 * 
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public double retrieveAmountResource(int resource, double quantity) {
		ResourceStored s = quantityMap.get(resource);
		if (s == null) {
			return quantity;
		}

		double shortfall = 0D;
		double remaining = s.stored - quantity;
		if (remaining < 0) {
			String name = ((Unit)owner).findAmountResourceName(resource);
			logger.warning(owner, 10_000L, "Just retrieved all " + quantity + " kg of " 
					+ name + ". Lacking " + Math.round(-remaining * 10.0)/10.0 + " kg.");
			shortfall = -remaining;
			remaining = 0;
		}

		s.stored = remaining;
		updateTotal();
		return shortfall;
	}
	
	/**
	 * What resources are stored ?
	 * @return
	 */
	public Set<Integer> getResourcesStored() {
		return Collections.unmodifiableSet(quantityMap.keySet());
	}
	
	/**
	 * Obtains the remaining storage space of a particular amount resource
	 * 
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceRemainingCapacity(int resource) {
		ResourceStored s = quantityMap.get(resource);
		if (s != null) {
			return s.capacity - s.stored;
		}
		return 0;
	}
	
	/**
	 * Gets the amount resource stored
	 * 
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceStored(int resource) {
		ResourceStored s = quantityMap.get(resource);
		if (s != null) {
			return s.stored;
		}
		return 0;
	}

	/**
	 * Clean this container for future use
	 */
	public void clean() {
		quantityMap.clear();
	}
	
	public void destroy() {
		quantityMap = null;
	}

	public boolean isResourceSupported(int resource) {
		return quantityMap.containsKey(resource);
	}

}
