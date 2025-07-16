/*
 * Mars Simulation Project
 * Kit.java
 * @date 2023-10-09
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

/**
 * The kit class represents an equipment package.
 */
public class Kit extends Equipment
	implements Malfunctionable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Static members
	public static final String TYPE = "Kit";
	/** 334 Sols (1/2 orbit). */
	private static final double WEAR_LIFETIME = 334_000;
	/** 100 millisols. */
	private static final double MAINTENANCE_TIME = 100D;
	
	// Data members.
	private String kitName;

	/** The kit malfunction manager. */
	protected MalfunctionManager malfunctionManager;

	/**
	 * Constructor.
	 */
	public Kit(Settlement base) {
		super(TYPE, TYPE, base);

		// Add scope to malfunction manager
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
	}

	/**
     * Gets the total capacity of resource that this kit can hold.
     * 
     * @return total capacity [kg]
     */
	@Override
    public double getCargoCapacity() {
		// To be modified
        return 0; 
    }


	public String getKitName() {
		return kitName;
	}

	/**
	 * Gets the kit's malfunction manager.
	 *
	 * @return malfunction manager
	 */
	public MalfunctionManager getMalfunctionManager() {
		return malfunctionManager;
	}

	@Override
	public Building getBuildingLocation() {
		return null;
	}

	@Override
	public double storeAmountResource(int resource, double quantity) {
		return 0;
	}

	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		return 0;
	}

	@Override
	public double getSpecificCapacity(int resource) {
		return 0;
	}

	@Override
	public double getAmountResourceStored(int resource) {
		return 0;
	}

	@Override
	public boolean isEmpty(boolean brandNew) {
		return false;
	}

	@Override
	public double getStoredMass() {
		return 0;
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.CONTAINER;
	}
	
	/**
	 * Time passing for the Kit.
	 *
	 * @param time the amount of time passing (millisols)
	 * @throws Exception if error during time.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		return malfunctionManager.timePassing(pulse);
	}

	/**
	 * Compares if an object is the same as this unit.
	 *
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		return this.kitName.equalsIgnoreCase(((Kit)obj).getKitName())
			&& this.getIdentifier() == ((Kit)obj).getIdentifier();
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	@Override
	public int hashCode() {
		int hashCode = getIdentifier();
		return hashCode % 32;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		malfunctionManager = null;
	}
	
}
