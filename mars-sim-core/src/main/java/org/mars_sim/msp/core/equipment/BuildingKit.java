/**
 * Mars Simulation Project
 * BuildingKit.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class BuildingKit extends Equipment
	implements Serializable, Malfunctionable, Salvagable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Static members
	public static final String TYPE = "Building Kit";
	/** Unloaded mass of EVA suit (kg.). */
	public static final double EMPTY_MASS = 30D;
	/** capacity (kg). */
	public static final double CAPACITY = 0;
	
	/** 334 Sols (1/2 orbit). */
	private static final double WEAR_LIFETIME = 334_000;
	/** 100 millisols. */
	private static final double MAINTENANCE_TIME = 100D;
	// Data members.
	private boolean isSalvaged;
	private String kitName;

	private SalvageInfo salvageInfo;
	/** The equipment's malfunction manager. */
	protected MalfunctionManager malfunctionManager;

	/**
	 * The BuildingKit class represents a building kit in a building.
	 */
	public BuildingKit(Settlement base) {
		super(TYPE, TYPE, base);

		// Initialize data members.
		isSalvaged = false;
		salvageInfo = null;

		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
	}
	
	/**
     * Gets the total capacity of resource that this container can hold.
     * @return total capacity (kg).
     */
    public double getCargoCapacity() {
        return CAPACITY;
    }
    
	/**
	 * Checks if the item is salvaged.
	 * 
	 * @return true if salvaged.
	 */
	public boolean isSalvaged() {
		return isSalvaged;
	}

	public String getKitName() {
		return kitName;
	}

	/**
	 * Indicate the start of a salvage process on the item.
	 * 
	 * @param info       the salvage process info.
	 * @param settlement the settlement where the salvage is taking place.
	 */
	public void startSalvage(SalvageProcessInfo info, int settlement) {
		salvageInfo = new SalvageInfo(this, info, settlement);
		isSalvaged = true;
	}

	/**
	 * Gets the salvage info.
	 * 
	 * @return salvage info or null if item not salvaged.
	 */
	public SalvageInfo getSalvageInfo() {
		return salvageInfo;
	}

	/**
	 * Gets the unit's malfunction manager.
	 * 
	 * @return malfunction manager
	 */
	public MalfunctionManager getMalfunctionManager() {
		return malfunctionManager;
	}

	/**
	 * Time passing for the Building Kit.
	 * 
	 * @param time the amount of time passing (millisols)
	 * @throws Exception if error during time.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {

		return malfunctionManager.timePassing(pulse);
	}

	public Settlement findSettlementVicinity() {

		Collection<Settlement> ss = unitManager.getSettlements();
		for (Settlement s : ss) {
			if (s.getCoordinates().equals(getCoordinates()))
				return s;
		}

		return null;
	}

	@Override
	public String getNickName() {
		return getName();
	}

	@Override
	public String getImmediateLocation() {
		return getLocationTag().getImmediateLocation();
	}

	@Override
	public String getLocale() {
		return getLocationTag().getLocale();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (salvageInfo != null)
			salvageInfo.destroy();
		salvageInfo = null;
	}

	@Override
	public Building getBuildingLocation() {
		return null;
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return getContainerUnit().getAssociatedSettlement();
	}
}
