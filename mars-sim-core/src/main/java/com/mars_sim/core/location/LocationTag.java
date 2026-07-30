/*
 * Mars Simulation Project
 * LocationTag.java
 * @date 2024-07-17
 * @author Manny Kung
 */
package com.mars_sim.core.location;

import java.io.Serializable;
import java.util.Collection;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.unit.AbstractMobileUnit;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The LocationTag class stores the location information of an object.
 */
public class LocationTag implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String MARS_SURFACE = LocationStateType.MARS_SURFACE.getName();

	private static final String VICINITY = " Vicinity";

	private static final String UNKNOWN = "Unknown";

	private static final String OPEN_P = " (";

	private static final String CLOSE_P = ")";

	private AbstractMobileUnit unit;

	public LocationTag(AbstractMobileUnit unit) {
		this.unit = unit;
	}

	/**
	 * Prints the locale (settlement, vehicle or coordinates) of the unit.
	 *
	 * @return the general (nearby) location
	 */
	public String getLocale() {
		String result = UNKNOWN;
		
		var topContainer = unit.getContainerUnit();
		if (topContainer != null)
			result = topContainer.getName();
		
		return result;
	}

	/**
	 * Prints the extended location of the unit in details.
	 *
	 * @apiNote Extended = immediate + locale
	 * (e.g. Lander Hab 1 in New Pompeii;
	 * e.g. Garage 1 in New Pompeii;
	 * e.g. On the Surface of Mars in New Pompeii Vicinity)
	 *
	 * @return the name string of the extended location
	 */
	public String getExtendedLocation() {
		String immediate = getImmediateLocation();
		String locale = getLocale();

		// a special case
		if (immediate.equalsIgnoreCase(locale))
			return immediate;

		if (locale != null && !locale.equals(UNKNOWN) && !locale.equals(""))
			// The general case
			return immediate + OPEN_P + locale + CLOSE_P;
		
		return immediate;
	}


	/**
	 * Prints the immediate location of the unit.
	 *
	 * @apiNote
	 * (e.g. in a container, building, vehicle, settlement vicinity or on surface of Mars)
	 *
	 * @return the name string of the immediate location
	 */
	public String getImmediateLocation() {
		String result = UNKNOWN;
		
		var container = unit.getContainerUnit();
		if (container != null)
			result = container.getName();
		
		return result;
	}


	/**
	 * Finds the settlement in the vicinity of a unit.
	 *
	 * @return {@link Settlement}
	 */
	public Settlement findSettlementVicinity() {
		
		if (unit instanceof Person p) {
			Settlement s = p.getSettlement();
			if (s != null)
				return s;
			
			if (p.isBuried())
				return p.getBuriedSettlement();			
		}
		
		if (unit instanceof Vehicle v) {
			Settlement s = v.getSettlement();
			if (s != null) 
				return s;
		}
		
		if (unit instanceof Robot r) {
			Settlement s = r.getSettlement();
			if (s != null) 
				return s;
		}
		
		if (unit instanceof Equipment e) {
			Settlement s = e.getSettlement();
			if (s != null) 
				return s;
		}
		
		return CollectionUtils.findSettlement(unit.getCoordinates());	
	}

	/**
	 * Is this unit in a settlement vicinity ?
	 *
	 * @return {@link Settlement}
	 */
	public boolean isInSettlementVicinity() {
		
		if (unit instanceof Person p) {
			if (p.getSettlement() != null)
				return true;
			
			if (p.isBuried())
				return true;			
		}
		
		if (unit instanceof Robot r && r.getSettlement() != null) {
			return true;
		}
		
		if (unit instanceof Vehicle v && v.getSettlement() != null) {
			return true;
		}
		
		if (unit instanceof Equipment e && e.getSettlement() != null) {
			return true;
		}
		
		return CollectionUtils.findSettlement(unit.getCoordinates()) instanceof Settlement;	
	}
	
	/**
	 * Finds the nearby vehicle that drops off a person/robot in the vicinity.
	 *
	 * @return {@link Vehicle}
	 */
	public Vehicle findVehicleVicinity() {
		Coordinates c = unit.getCoordinates();
		Settlement settlement = unit.getAssociatedSettlement();
		
		Collection<Vehicle> list = settlement.getAllAssociatedVehicles();
		for (Vehicle v : list) {
			Coordinates coord = v.getCoordinates();
			if (coord.equals(c) || coord == c)
				return v;
		}
		
		return null;
	}
}
