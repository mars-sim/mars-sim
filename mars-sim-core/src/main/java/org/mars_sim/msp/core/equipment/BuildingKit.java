/**
 * Mars Simulation Project
 * BuildingKit.java
 * @version 3.1.0 2017-09-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

public class BuildingKit extends Equipment implements Serializable, Malfunctionable, Salvagable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Static members
	public static final String TYPE = "Building Kit";
	/** Unloaded mass of EVA suit (kg.). */
	public static final double EMPTY_MASS = 30D;

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
	public BuildingKit(Coordinates location) {
		super(TYPE, TYPE, location);

		// Initialize data members.
		isSalvaged = false;
		salvageInfo = null;

		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
//		malfunctionManager.addScopeString(TYPE);
//		malfunctionManager.addScopeString("Life Support");
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
	public void timePassing(double time) {

//		Unit container = getContainerUnit();
//		if (container instanceof Building) {
//			Building building = (Building) container;
//			 if (!person.getPhysicalCondition().isDead()) {
//			 malfunctionManager.activeTimePassing(time);
//			 }
//		}
		malfunctionManager.timePassing(time);
	}

//	/**
//	 * Obtains the immediate location (either building, vehicle, a settlement's vicinity or outside on Mars)
//	 * @return the name string of the location the unit is at
//	 */
//	public String getImmediateLocation() {
//			if (getContainerUnit() != null)
//				return getContainerUnit().getName();
////			else if (e.getTopContainerUnit() != null)
////				return e.getTopContainerUnit().getName();
//			else if (isRightOutsideSettlement())
//				return findSettlementVicinity().getName() + VICINITY;  
//			else
//				return OUTSIDE_ON_MARS;
//
//	}

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return getContainerUnit().getAssociatedSettlement();
	}

	@Override
	public Unit getUnit() {
		return this;
	}
}
