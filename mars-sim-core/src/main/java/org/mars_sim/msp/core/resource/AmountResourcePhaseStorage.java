/**
 * Mars Simulation Project
 * AmountResourcePhaseStorage.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis 
 */

package org.mars_sim.msp.core.resource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Storage for phases of amount resource.
 */
class AmountResourcePhaseStorage implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Comparison to indicate a small but non-zero amount. */
	private static final double SMALL_AMOUNT_COMPARISON = .0000001D;

	// Data members

	/** Capacity for each phase of amount resource. */
	private Map<PhaseType, Double> amountResourcePhaseCapacities = null;

	/** Stored resources by phase. */
	private Map<PhaseType, StoredPhase> amountResourcePhaseStored = null;

	/** The cache value for the total amount phase resources stored. (kg) */
	private transient double totalStoredCache = 0D;
	private transient boolean totalStoredCacheDirty = true;

	/**
	 * Adds capacity for a resource phase.
	 * 
	 * @param phase    the phase
	 * @param capacity the capacity amount (kg).
	 * @throws ResourceException if error adding capacity.
	 */
	void addAmountResourcePhaseCapacity(PhaseType phase, double capacity) {

		if (capacity < 0D) {
			throw new IllegalStateException("Cannot add negative phase capacity: " + capacity);
		}

		if (amountResourcePhaseCapacities == null) {
			amountResourcePhaseCapacities = new HashMap<PhaseType, Double>();
		}

		if (hasAmountResourcePhaseCapacity(phase)) {
			double current = amountResourcePhaseCapacities.get(phase);
			amountResourcePhaseCapacities.put(phase, (current + capacity));
		} else {
			amountResourcePhaseCapacities.put(phase, capacity);
		}
	}

	/**
	 * Checks if storage has capacity for a phase.
	 * 
	 * @param phase the phase.
	 * @return true if capacity in phase.
	 */
	boolean hasAmountResourcePhaseCapacity(PhaseType phase) {

		boolean result = false;

		if (amountResourcePhaseCapacities != null) {
			result = amountResourcePhaseCapacities.containsKey(phase);
		}

		return result;
	}

	/**
	 * Gets the capacity for a phase.
	 * 
	 * @param phase the phase
	 * @return the capacity (kg).
	 */
	double getAmountResourcePhaseCapacity(PhaseType phase) {

		double result = 0D;

		if (hasAmountResourcePhaseCapacity(phase)) {
			result = amountResourcePhaseCapacities.get(phase);
		}

		return result;
	}

	/**
	 * Gets the stored amount of a phase.
	 * 
	 * @param phase the phase
	 * @return amount stored (kg)
	 */
	double getAmountResourcePhaseStored(PhaseType phase) {

		double result = 0D;

		StoredPhase stored = getAmountResourcePhaseStoredObject(phase);
		if (stored != null) {
			result = stored.amount;
		}

		return result;
	}

	/**
	 * Gets the amount of a resource phase stored.
	 * 
	 * @param phase the resource phase.
	 * @return stored amount as StoredPhase object.
	 */
	private StoredPhase getAmountResourcePhaseStoredObject(PhaseType phase) {

		StoredPhase result = null;

		if (amountResourcePhaseStored != null) {
			result = amountResourcePhaseStored.get(phase);
		}

		return result;
	}

	/**
	 * Gets the total amount of phase resources stored.
	 * 
	 * @param allowDirty will allow dirty (possibly out of date) results.
	 * @return amount stored (kg).
	 */
	double getTotalAmountResourcePhasesStored(boolean allowDirty) {

		if (totalStoredCacheDirty && !allowDirty) {
			updateTotalAmountResourcePhasesStored();
		}

		return totalStoredCache;
	}

	/**
	 * Updates the total amount resource phases stored cache value.
	 */
	private void updateTotalAmountResourcePhasesStored() {

		double totalAmount = 0D;

		if (amountResourcePhaseStored != null) {
			Map<PhaseType, StoredPhase> tempMap = Collections.unmodifiableMap(amountResourcePhaseStored);
			Iterator<PhaseType> i = tempMap.keySet().iterator();
			while (i.hasNext()) {
				totalAmount += tempMap.get(i.next()).amount;
			}
		}

		totalStoredCache = totalAmount;
		totalStoredCacheDirty = false;
	}

	/**
	 * Gets the remaining capacity for a phase .
	 * 
	 * @param phase the phase
	 * @return remaining capacity (kg)
	 */
	double getAmountResourcePhaseRemainingCapacity(PhaseType phase) {

		double result = 0D;

		if (hasAmountResourcePhaseCapacity(phase)) {
			result = getAmountResourcePhaseCapacity(phase) - getAmountResourcePhaseStored(phase);
		}

		return result;
	}

	/**
	 * Gets the type of resource that is stored for a phase.
	 * 
	 * @param phase the phase
	 * @return the resource stored.
	 */
	AmountResource getAmountResourcePhaseType(PhaseType phase) {

		AmountResource result = null;

		if (amountResourcePhaseStored != null) {
			StoredPhase stored = amountResourcePhaseStored.get(phase);
			if (stored != null) {
				result = stored.resource;
			}
		}

		return result;
	}

	/**
	 * Stores an amount of a resource.
	 * 
	 * @param resource the resource.
	 * @param amount   the amount to store (kg)
	 * @throws ResourceException if error storing resource.
	 */
	void storeAmountResourcePhase(AmountResource resource, double amount) {

		if (amount < 0D) {
			throw new IllegalStateException("Cannot store negative amount of phase: " + amount);
		}

		if (amount > SMALL_AMOUNT_COMPARISON) {

			PhaseType resourcePhase = resource.getPhase();
			boolean storable = false;

			if (getAmountResourcePhaseRemainingCapacity(resourcePhase) >= amount) {

				if ((getAmountResourcePhaseStored(resourcePhase) == 0D)
						|| (resource.equals(getAmountResourcePhaseType(resourcePhase)))) {
					storable = true;
				}
			}

			if (storable) {

				// Set total stored cache to dirty since value is changing.
				totalStoredCacheDirty = true;

				if (amountResourcePhaseStored == null) {
					amountResourcePhaseStored = new HashMap<PhaseType, StoredPhase>();
				}

				StoredPhase stored = getAmountResourcePhaseStoredObject(resourcePhase);
				if (stored != null) {
					stored.amount += amount;
				} else {
					amountResourcePhaseStored.put(resourcePhase, new StoredPhase(resource, amount));
				}
			} else {
				throw new IllegalStateException("Amount resource could not be added in phase storage.");
			}
		}
	}

	/**
	 * Retrieves an amount of a resource.
	 * 
	 * @param phase  the phase
	 * @param amount the amount to retrieve.
	 * @throws ResourceException if error retrieving amount from phase.
	 */
	void retrieveAmountResourcePhase(PhaseType phase, double amount) {

		if (amount < 0D) {
			throw new IllegalStateException("Cannot retrieve negative amount of phase: " + amount);
		}

		if (amount > 0D) {
			boolean retrievable = false;
			if (getAmountResourcePhaseStored(phase) >= amount) {

				StoredPhase stored = amountResourcePhaseStored.get(phase);
				if (stored != null) {

					// Set total stored cache to dirty since value is changing.
					totalStoredCacheDirty = true;

					stored.amount -= amount;

					retrievable = true;

					if (stored.amount <= SMALL_AMOUNT_COMPARISON) {
						amountResourcePhaseStored.remove(phase);
					}
				}
			}
			if (!retrievable) {
				throw new IllegalStateException("Amount resource (" + phase.getName() + ":" + amount
						+ ") could not be retrieved from phase storage");
			}
		}
	}

	/**
	 * Internal class for a stored phase.
	 */
	private static class StoredPhase implements Serializable {
		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private AmountResource resource;
		private double amount;

		private StoredPhase(AmountResource resource, double amount) {
			this.resource = resource;
			this.amount = amount;
		}
	}

	/**
	 * Prepare object for garbage collection
	 */
	public void destroy() {
		if (amountResourcePhaseCapacities != null)
			amountResourcePhaseCapacities.clear();
		amountResourcePhaseCapacities = null;
		if (amountResourcePhaseStored != null)
			amountResourcePhaseStored.clear();
		amountResourcePhaseStored = null;
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
		totalStoredCacheDirty = true;
	}
}