/*
 * Mars Simulation Project
 * AmountResourceStorage.java
 * @date 2021-08-28
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;

/**
 * Storage for amount resources.
 */
public class AmountResourceStorage implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(AmountResourceStorage.class.getName());

	// Domain members
	private Unit owner;
	private AmountResourceTypeStorage typeStorage = null;
	private AmountResourcePhaseStorage phaseStorage = null;

	// Cache values
	private transient Set<Integer> allStoredARCache = null;
	private transient boolean allStoredResourcesCacheDirty = true;
	private transient double totalResourcesStored = 0D;
	private transient boolean totalResourcesStoredDirty = true;

	public AmountResourceStorage(Unit owner) {
		this.owner = owner;
	}

	public boolean isEmpty() {
		if (allStoredARCache == null)
			return true;
		return allStoredARCache.isEmpty();
	}

	/**
	 * Adds capacity for a resource type.
	 *
	 * @param resource the resource.
	 * @param capacity the extra capacity amount (kg).
	 */
	public void addAmountResourceTypeCapacity(AmountResource resource, double capacity) {
		addAmountResourceTypeCapacity(resource.getID(), capacity);
	}

	/**
	 * Adds capacity for a resource type.
	 *
	 * @param resource the resource.
	 * @param capacity the extra capacity amount (kg).
	 */
	public void addAmountResourceTypeCapacity(int resource, double capacity) {

		if (typeStorage == null) {
			typeStorage = new AmountResourceTypeStorage();
		}

		typeStorage.addAmountResourceTypeCapacity(resource, capacity);
	}

	/**
	 * Removes capacity for a resource type.
	 *
	 * @param resource the resource
	 * @param capacity capacity the capacity amount (kg).
	 */
	public void removeAmountResourceTypeCapacity(AmountResource resource, double capacity) {
		removeAmountResourceTypeCapacity(resource.getID(), capacity);
	}

	/**
	 * Removes capacity for a resource type.
	 *
	 * @param resource the resource
	 * @param capacity capacity the capacity amount (kg).
	 */
	public void removeAmountResourceTypeCapacity(int resource, double capacity) {

		if (typeStorage == null) {
			return;
//			typeStorage = new AmountResourceTypeStorage();
		}

		typeStorage.removeTypeCapacity(resource, capacity);
	}

	/**
	 * Gets the amount resources and the type capacity for them.
	 *
	 * @return map of all amount resources that have type capacity.
	 */
	public Map<Integer, Double> getAmountResourceTypeCapacities() {

		Map<Integer, Double> typeCapacities = new ConcurrentHashMap<Integer, Double>();

		if (typeStorage != null) {
			Iterator<Integer> i = ResourceUtil.getIDs().iterator();
			while (i.hasNext()) {
				Integer resource = i.next();
				double capacity = typeStorage.getAmountResourceTypeCapacity(resource);
				if (capacity > 0D) {
					typeCapacities.put(resource, capacity);
				}
			}
		}

		return typeCapacities;
	}

	/**
	 * Adds capacity for a resource phase.
	 *
	 * @param phase    the phase
	 * @param capacity the capacity amount (kg).
	 * @throws ResourceException if error adding capacity.
	 */
	public void addAmountResourcePhaseCapacity(PhaseType phase, double capacity) {

		if (phaseStorage == null) {
			phaseStorage = new AmountResourcePhaseStorage();
		}

		phaseStorage.addAmountResourcePhaseCapacity(phase, capacity);
	}

	/**
	 * Gets the phase capacities in storage.
	 *
	 * @return map of phases with capacities.
	 */
	public Map<PhaseType, Double> getAmountResourcePhaseCapacities() {

		Map<PhaseType, Double> phaseCapacities = new ConcurrentHashMap<PhaseType, Double>();

		if (phaseStorage != null) {
			for (PhaseType phase : PhaseType.values()) {
				double capacity = phaseStorage.getAmountResourcePhaseCapacity(phase);
				if (capacity > 0D) {
					phaseCapacities.put(phase, capacity);
				}
			}
		}

		return phaseCapacities;
	}

	/**
	 * Checks if storage has capacity for a resource.
	 *
	 * @param resource the resource.
	 * @return true if storage capacity.
	 */
	public boolean hasAmountResourceCapacity(AmountResource resource) {

		boolean result = false;

		if ((typeStorage != null) && typeStorage.hasAmountResourceTypeCapacity(resource)) {
			result = true;
		} else if ((phaseStorage != null) && phaseStorage.hasAmountResourcePhaseCapacity(resource.getPhase())) {
			result = true;
		}

		return result;
	}

	/**
	 * Checks if storage has capacity for a resource.
	 *
	 * @param resource the resource.
	 * @return true if storage capacity.
	 */
	public boolean hasARCapacity(int resource) {

		boolean result = false;

		if ((typeStorage != null) && typeStorage.hasARTypeCapacity(resource)) {
			result = true;
		} else if ((phaseStorage != null)
				&& phaseStorage.hasAmountResourcePhaseCapacity(ResourceUtil.findAmountResource(resource).getPhase())) {
			result = true;
		}

		return result;
	}

	/**
	 * Gets the storage capacity for a resource.
	 *
	 * @param resource the resource.
	 * @return capacity amount (kg).
	 */
	public double getAmountResourceCapacity(AmountResource resource) {

		double result = 0D;

		if ((typeStorage != null) && typeStorage.hasAmountResourceTypeCapacity(resource)) {
			result = typeStorage.getAmountResourceTypeCapacity(resource);
		}
		if ((phaseStorage != null) && phaseStorage.hasAmountResourcePhaseCapacity(resource.getPhase())) {
			if ((phaseStorage.getAmountResourcePhaseType(resource.getPhase()) == null)
					|| phaseStorage.getAmountResourcePhaseType(resource.getPhase()).equals(resource)) {
				result += phaseStorage.getAmountResourcePhaseCapacity(resource.getPhase());
			}
		}

		return result;
	}

	/**
	 * Gets the storage capacity for a resource.
	 *
	 * @param resource the resource.
	 * @return capacity amount (kg).
	 */
	public double getAmountResourceCapacity(int resource) {
		AmountResource ar = ResourceUtil.findAmountResource(resource);
//		logger.info(40_000L, ar + " " +
//				resource + " getAmountResourceCapacity");
		PhaseType pt = ar.getPhase();
		double result = 0D;

		if ((typeStorage != null) && typeStorage.hasARTypeCapacity(resource)) {
			result = typeStorage.getAmountResourceTypeCapacity(resource);
		}
		if ((phaseStorage != null) && phaseStorage.hasAmountResourcePhaseCapacity(pt)) {
			if ((phaseStorage.getAmountResourcePhaseType(pt) == null)
					|| phaseStorage.getAmountResourcePhaseType(pt).equals(ar)) {
				result += phaseStorage.getAmountResourcePhaseCapacity(pt);
			}
		}

		return result;
	}

	/**
	 * Gets the amount of a resource stored.
	 *
	 * @param resource the resource.
	 * @return stored amount (kg).
	 */
	public double getAmountResourceStored(AmountResource resource) {

		double result = 0D;

		if (typeStorage != null) {
			result = typeStorage.getAmountResourceTypeStored(resource);
		}

		if ((phaseStorage != null) && resource.equals(phaseStorage.getAmountResourcePhaseType(resource.getPhase()))) {
			result += phaseStorage.getAmountResourcePhaseStored(resource.getPhase());
		}

		return result;
	}

	/**
	 * Gets the amount of a resource stored.
	 *
	 * @param resource the resource.
	 * @return stored amount (kg).
	 */
	public double getAmountResourceStored(int resource) {
		AmountResource ar = ResourceUtil.findAmountResource(resource);
		PhaseType pt = ar.getPhase();

		double result = 0D;

		if (typeStorage != null) {
			result = typeStorage.getAmountResourceTypeStored(resource);
		}

		if (phaseStorage != null && ar.equals(phaseStorage.getAmountResourcePhaseType(pt))) {
			result += phaseStorage.getAmountResourcePhaseStored(pt);
		}

		return result;
	}

	/**
	 * Gets all of the amount resources stored.
	 *
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return set of amount resources.
	 */
	public Set<AmountResource> getAllAmountResourcesStored(boolean allowDirty) {
		Set<AmountResource> set = ConcurrentHashMap.newKeySet();
		for (int ar : getAllARStored(allowDirty)) {
			set.add(ResourceUtil.findAmountResource(ar));
		}

		return set;
	}

	/**
	 * Gets all of the amount resources stored.
	 *
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return set of amount resources.
	 */
	public Set<Integer> getAllARStored(boolean allowDirty) {

		if (allStoredARCache == null) {
			allStoredARCache = ConcurrentHashMap.newKeySet();
		}

		if (allStoredResourcesCacheDirty && !allowDirty) {
			updateAllAmountResourcesStored();
		}

		Set<Integer> s = ConcurrentHashMap.newKeySet();
		s.addAll(allStoredARCache);
		return s;
	}

	/**
	 * Update the all stored resources values.
	 */
	private void updateAllAmountResourcesStored() {
		Set<Integer> tempResources = ConcurrentHashMap.newKeySet();

		// Add type storage resources.
		if (typeStorage != null) {
			tempResources.addAll(typeStorage.getAllARStored());
		}

		// Add phase storage resources.
		if (phaseStorage != null) {
			for (PhaseType phase : PhaseType.values()) {
				if (phaseStorage.getAmountResourcePhaseStored(phase) > 0D) {
					tempResources.add(phaseStorage.getAmountResourcePhaseType(phase).getID());
				}
			}
		}

		allStoredARCache = tempResources;
		allStoredResourcesCacheDirty = false;
	}

	/**
	 * Gets the total amount of resources stored.
	 *
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return stored amount (kg).
	 */
	public double getTotalAmountResourcesStored(boolean allowDirty) {

		if (totalResourcesStoredDirty && !allowDirty) {
			updateTotalResourcesStored();
		}

		return totalResourcesStored;
	}

	/**
	 * Update the total resources stored value.
	 */
	private void updateTotalResourcesStored() {

		double tempTotalResources = 0D;

		// Add type resources stored.
		if (typeStorage != null) {
			tempTotalResources += typeStorage.getTotalTypesStored(false);// getTotalAmountResourceTypesStored(false);
		}

		// Add phase resources stored.
		if (phaseStorage != null) {
			tempTotalResources += phaseStorage.getTotalAmountResourcePhasesStored(false);
		}

		totalResourcesStored = tempTotalResources;
		totalResourcesStoredDirty = false;
	}

	/**
	 * Gets the remaining capacity available for a resource.
	 *
	 * @param resource the resource.
	 * @return remaining capacity amount (kg).
	 */
	public double getAmountResourceRemainingCapacity(AmountResource resource) {

		double result = 0D;

		if (hasAmountResourceCapacity(resource)) {
			result = getAmountResourceCapacity(resource) - getAmountResourceStored(resource);
		}

		return result;
	}

	/**
	 * Gets the remaining capacity available for a resource.
	 *
	 * @param resource the resource.
	 * @return remaining capacity amount (kg).
	 */
	public double getARRemainingCapacity(int resource) {

		double result = 0D;

		if (hasARCapacity(resource)) {
			result = getAmountResourceCapacity(resource) - getAmountResourceStored(resource);
		}

		return result;
	}

	/**
	 * Store an amount of a resource.
	 *
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 * @throws ResourceException if error storing resource.
	 */
	public void storeAmountResource(AmountResource resource, double amount) {

		if (amount < 0D) {
			throw new IllegalStateException("Cannot store negative amount of resource: " + amount);
		}

		if (amount > 0D) {
			boolean storable = false;

			if (hasAmountResourceCapacity(resource)) {
				if (getAmountResourceRemainingCapacity(resource) >= amount) {

					double remainingAmount = amount;

					// Store resource in type storage.
					if (typeStorage != null) {

						double remainingTypeCapacity = typeStorage.getAmountResourceTypeRemainingCapacity(resource);
						if (remainingTypeCapacity > 0D) {

							double typeStore = remainingAmount;
							if (typeStore > remainingTypeCapacity) {
								typeStore = remainingTypeCapacity;
							}
							typeStorage.storeAmountResourceType(resource, typeStore);
							remainingAmount -= typeStore;
						}
					}

					// Store resource in phase storage.
					if ((phaseStorage != null) && (remainingAmount > 0D)) {

						double remainingPhaseCapacity = phaseStorage
								.getAmountResourcePhaseRemainingCapacity(resource.getPhase());
						if (remainingPhaseCapacity >= remainingAmount) {

							AmountResource resourceTypeStored = phaseStorage
									.getAmountResourcePhaseType(resource.getPhase());
							if ((resourceTypeStored == null) || resource.equals(resourceTypeStored)) {
								phaseStorage.storeAmountResourcePhase(resource, remainingAmount);
							}
							remainingAmount = 0D;
						}
					}

					if (remainingAmount == 0D) {
						storable = true;
						allStoredResourcesCacheDirty = true;
						totalResourcesStoredDirty = true;
					} else {
						logger.severe(owner, 30_000, amount + " kg " + resource
							+ " could not be stored.  Remaining capacity: " + getAmountResourceRemainingCapacity(resource)
							+ " remaining: " + remainingAmount);
					}
				}
			}

			if (!storable)
				logger.severe(owner, 30_000, amount + " kg " + resource
					+ " could not be stored.  Remaining capacity: " + getAmountResourceRemainingCapacity(resource));
		}
	}

	/**
	 * Store an amount of a resource.
	 *
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 * @throws ResourceException if error storing resource.
	 */
	public void storeAmountResource(int resource, double amount) {

		if (amount < 0D) {
			throw new IllegalStateException("Cannot store negative amount of resource: " + amount);
		}

		if (amount > 0D) {
			boolean storable = false;

			if (hasARCapacity(resource)) {
				if (getARRemainingCapacity(resource) >= amount) {

					double remainingAmount = amount;

					// Store resource in type storage.
					if (typeStorage != null) {

						double remainingTypeCapacity = typeStorage.getARTypeRemainingCapacity(resource);
						if (remainingTypeCapacity > 0D) {

							double typeStore = remainingAmount;
							if (typeStore > remainingTypeCapacity) {
								typeStore = remainingTypeCapacity;
							}
							typeStorage.storeARType(resource, typeStore);
							remainingAmount -= typeStore;
						}
					}

					AmountResource ar = ResourceUtil.findAmountResource(resource);
					PhaseType phase = ar.getPhase();

					// Store resource in phase storage.
					if ((phaseStorage != null) && (remainingAmount > 0D)) {

						double remainingPhaseCapacity = phaseStorage.getAmountResourcePhaseRemainingCapacity(phase);
						if (remainingPhaseCapacity >= remainingAmount) {

							AmountResource resourceTypeStored = phaseStorage.getAmountResourcePhaseType(phase);
							if ((resourceTypeStored == null) || ar.equals(resourceTypeStored)) {
								phaseStorage.storeAmountResourcePhase(ar, remainingAmount);
							}
							remainingAmount = 0D;
						}
					}

					if (remainingAmount == 0D) {
						storable = true;
						allStoredResourcesCacheDirty = true;
						totalResourcesStoredDirty = true;
					}

					else if (phaseStorage != null) {
						double cap = Math.round(10_000.0 * phaseStorage.getAmountResourcePhaseCapacity(phase))/10_000.0;
						double stored = Math.round(10_000.0 * phaseStorage.getTotalAmountResourcePhasesStored(true))/10_000.0;
						logger.severe(owner, 30_000,
							"Cannot store "
							+ Math.round(10_000.0 * amount)/10_000.0 + " kg " + ResourceUtil.findAmountResource(resource)
							+ ". Cap: " + cap
							+ " Stored: " + stored);
					}
				}
			}

			if (!storable)
				logger.severe(owner, 30_000, Math.round(10_000.0 * amount)/10_000.0 + " kg " + ResourceUtil.findAmountResource(resource)
					+ " could not be stored.  Remaining cap: " + getARRemainingCapacity(resource));
		}
	}

	/**
	 * Retrieves an amount of a resource from storage.
	 *
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 * @throws ResourceException if error retrieving resource.
	 */
	public void retrieveAmountResource(AmountResource resource, double amount) {

		if (amount < 0D) {
			throw new IllegalStateException("Cannot retrieve negative amount of resource: " + amount);
		}

		boolean retrievable = false;
		double amountStored = getAmountResourceStored(resource);

		if (amountStored >= amount) {

			// Set caches to dirty because values are changing.
			allStoredResourcesCacheDirty = true;
			totalResourcesStoredDirty = true;

			double remainingAmount = amount;

			// Retrieve resource from phase storage.
			if (phaseStorage != null) {
				double phaseStored = phaseStorage.getAmountResourcePhaseStored(resource.getPhase());
				double retrieveAmount = remainingAmount;
				if (retrieveAmount > phaseStored) {
					retrieveAmount = phaseStored;
				}
				AmountResource resourceTypeStored = phaseStorage.getAmountResourcePhaseType(resource.getPhase());
				if (resource.equals(resourceTypeStored)) {
					phaseStorage.retrieveAmountResourcePhase(resource.getPhase(), retrieveAmount);
				}
				remainingAmount -= retrieveAmount;
			}

			// Retrieve resource from type storage.
			if ((typeStorage != null) && (remainingAmount > 0D)) {
				double remainingTypeStored = typeStorage.getAmountResourceTypeStored(resource);
				if (remainingTypeStored >= remainingAmount) {
					typeStorage.retrieveAmountResourceType(resource, remainingAmount);
					remainingAmount = 0D;
				}
			}

			if (remainingAmount == 0D) {
				retrievable = true;
			} else {
				logger.severe(owner, 30_000, amount + " kg " + resource
						+ " could not be retrieved.  Amount stored: " + amountStored + " remaining: " + remainingAmount);
			}
		} else {
			logger.severe(owner, 30_000, amount + " kg " + resource
					+ " could not be retrieved.  Amount stored: " + amountStored);
		}

		if (!retrievable) {
			logger.severe(owner, 30_000, amount + " kg " + resource
					+ " could not be retrieved from inventory.");
		}
	}

	/**
	 * Retrieves an amount of a resource from storage.
	 *
	 * @param resource the resource.
	 * @param amount   the amount (kg).
	 * @throws ResourceException if error retrieving resource.
	 */
	public void retrieveAmountResource(int resource, double amount) {
		AmountResource ar = ResourceUtil.findAmountResource(resource);
		PhaseType pt = ar.getPhase();

		if (amount < 0D) {
			throw new IllegalStateException("Cannot retrieve negative amount of resource: " + amount);
		}

		boolean retrievable = false;
		double amountStored = getAmountResourceStored(resource);

		if (amountStored >= amount) {

			// Set caches to dirty because values are changing.
			allStoredResourcesCacheDirty = true;
			totalResourcesStoredDirty = true;

			double remainingAmount = amount;

			// Retrieve resource from phase storage.
			if (phaseStorage != null) {
				double phaseStored = phaseStorage.getAmountResourcePhaseStored(pt);
				double retrieveAmount = remainingAmount;
				if (retrieveAmount > phaseStored) {
					retrieveAmount = phaseStored;
				}
				AmountResource resourceTypeStored = phaseStorage.getAmountResourcePhaseType(pt);
				if (ar.equals(resourceTypeStored)) {
					phaseStorage.retrieveAmountResourcePhase(pt, retrieveAmount);
				}
				remainingAmount -= retrieveAmount;
			}

			// Retrieve resource from type storage.
			if ((typeStorage != null) && (remainingAmount > 0D)) {
				double remainingTypeStored = typeStorage.getAmountResourceTypeStored(resource);
				if (remainingTypeStored >= remainingAmount) {
					typeStorage.retrieveAmountResourceType(resource, remainingAmount);
					remainingAmount = 0D;
				}
			}

			if (remainingAmount == 0D) {
				retrievable = true;
			} else {
				logger.severe("Amount resource " + ar + " of amount: " + amount
						+ " needed to retrieve.  Amount stored: " + amountStored + " remaining: " + remainingAmount);
			}
		} else {
			logger.severe("Amount resource " + ar + " of amount: " + amount + " needed to retrieve.  Amount stored: "
					+ amountStored);
		}

		if (!retrievable) {
			throw new IllegalStateException(
					"Amount resource: " + ar + " of amount: " + amount + " could not be retrieved from inventory.");
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (typeStorage != null)
			typeStorage.destroy();
		typeStorage = null;
		if (phaseStorage != null)
			phaseStorage.destroy();
		phaseStorage = null;

		allStoredARCache = null;
	}

	/**
	 * Implementing readObject method for serialization.
	 *
	 * @param in the input stream.
	 * @throws IOException            if error reading from input stream.
	 * @throws ClassNotFoundException if error creating class.
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

		in.defaultReadObject();

		// Initialize transient variables that need it.
		allStoredResourcesCacheDirty = true;
		totalResourcesStoredDirty = true;
	}
}
