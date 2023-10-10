/*
 * Mars Simulation Project
 * Kit.java
 * @date 2023-10-09
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

/**
 * The kit class represents an equipment package.
 */
public class Kit extends Equipment
	implements Malfunctionable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Static members
	public static final String TYPE = "Kit";
	/** Unloaded mass of the kit [kg]. */
//	public static final double EMPTY_MASS = 0;
	/** Capacity [kg]. */
//	public static final double CAPACITY = 0;
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
    public double getCargoCapacity() {
        return 0; // CAPACITY;
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
	public Settlement getAssociatedSettlement() {
		return getContainerUnit().getAssociatedSettlement();
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
	public double getAmountResourceCapacity(int resource) {
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

	@Override
	public String getNickName() {
		return getName();
	}

	@Override
	public void destroy() {
		super.destroy();
		malfunctionManager = null;
	}
	
}
