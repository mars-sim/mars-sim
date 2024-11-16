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
import com.mars_sim.core.Unit;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
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

	private static final String IN = " in ";

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
		switch(unit.getLocationStateType()) {
			case INSIDE_SETTLEMENT:
				result = unit.getSettlement().getName();
				break;
			case INSIDE_VEHICLE:
				result = unit.getVehicle().getName();
				break;
			case MARS_SURFACE: {
				Settlement s = findSettlementVicinity();
				if (s != null) {
					result = s.getName() + VICINITY;
				}
				else {
					Vehicle v = findVehicleVicinity();
					if (v != null)
						result = v.getName() + VICINITY;
					else
						result = MARS_SURFACE;
				}
			} break;
			case ON_PERSON_OR_ROBOT: {
				Unit container = unit.getContainerUnit();
				if (container instanceof AbstractMobileUnit w) {
					result = w.getLocationTag().getImmediateLocation();
				}
			} break;
			case SETTLEMENT_VICINITY: {
				Settlement s = findSettlementVicinity();
				if (s != null)
					result = s + VICINITY;	
			} break;
			case VEHICLE_VICINITY:
				Vehicle v = findVehicleVicinity();
				if (v != null)
					result = v + VICINITY;	
				break;
			default:
				result = UNKNOWN;
				break;
		}

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

		// The general case
		return immediate + IN + locale;
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
		switch(unit.getLocationStateType()) {
			case INSIDE_SETTLEMENT:
				if (unit.getBuildingLocation() != null) {
					result = unit.getBuildingLocation().getName();
				}			
				break;
			case INSIDE_VEHICLE:
				result = unit.getVehicle().getName();
				break;
			case MARS_SURFACE: {
				Settlement s = findSettlementVicinity();
				if (s != null) {
					result = s.getName() + VICINITY;
				}
				else {
					Vehicle v = findVehicleVicinity();
					if (v != null)
						result = v.getName() + VICINITY;
					else
						result = MARS_SURFACE;
				}
			} break;
			case ON_PERSON_OR_ROBOT: {
				Unit container = unit.getContainerUnit();
				if (container instanceof AbstractMobileUnit w) {
					result = w.getLocationTag().getImmediateLocation();
				}
			} break;
			case SETTLEMENT_VICINITY:
				result = unit.getPosition().getShortFormat();	
				break;
			case VEHICLE_VICINITY:
				result = unit.getPosition().getShortFormat();	
				break;
			default:
				result = UNKNOWN;
				break;
		}

		return result;
	}


	/**
	 * Finds the settlement in the vicinity of a person/robot/vehicle.
	 *
	 * @return {@link Settlement}
	 */
	public Settlement findSettlementVicinity() {
		
		if (unit instanceof Person p && p.isBuried())
			return p.getBuriedSettlement();
		
		if (unit instanceof Vehicle v && v.getSettlement() != null)
			return v.getSettlement();
		
		Coordinates c = unit.getCoordinates();

		return CollectionUtils.findSettlement(c);	
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
			if (v.getCoordinates().equals(c) || v.getCoordinates() == c)
				return v;
		}
		
		return null;
	}

	/**
	 * Checks if an unit is in the vicinity of a settlement.
	 *
	 * @return true if it is
	 */
	public boolean isInSettlementVicinity() {
		Coordinates c = unit.getCoordinates();

		return (CollectionUtils.findSettlement(c) != null);
	}
}
