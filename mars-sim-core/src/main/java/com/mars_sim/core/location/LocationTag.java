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
import com.mars_sim.core.environment.MarsSurface;
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

	public static final String MARS_SURFACE = MarsSurface.NAME;// LocationStateType.MARS_SURFACE.getName();

	private static final String VICINITY = " Vicinity";

	private static final String UNKNOWN = "Unknown";

	private static final String OPEN_P = " (";

	private static final String CLOSE_P = ")";

	private AbstractMobileUnit unit;

	public LocationTag(AbstractMobileUnit unit) {
		this.unit = unit;
	}

	/**
	 * Prints the settlement, Mars Surface, or Mars Surface's coordinates of the unit.
	 *
	 * @return the general (nearby) location
	 */
	public String getLocale() {
		String result = UNKNOWN;
		
		var container = unit.getContainerUnit();
		
		if (unit instanceof Person || unit instanceof Robot 
				|| unit instanceof Vehicle || unit instanceof Equipment) {
			if (container instanceof Settlement) {
				result = container.getName();
			}
			else if (container instanceof Vehicle v) {
				var topContainer = v.getContainerUnit();
				if (topContainer instanceof Settlement s) {
					result = s.getName();
				}
				else {
					result = MARS_SURFACE;
				}
			}
			else if (container instanceof MarsSurface) {
				// Print out coordinates on MarsSurface
				result = unit.getCoordinates().getFormattedString();
			}
		}

		return result;
	}

	/**
	 * Prints the extended location of the unit in details.
	 *
	 * @apiNote Extended = mobile loc + locale
	 * e.g. Lander Hab 1 (New Pompeii)
	 * e.g. Josh Benson (Starbase)
	 * e.g. Mars Surface (32.2312 N 121.3233 E)
	 * e.g. New Shanghai (Mars Surface)
	 *
	 * @return the name string of the extended location
	 */
	public String getExtendedLocation() {
		String mobileLoc = getMobileContainerUnit();
		String locale = getLocale();

		// a special case
		if (mobileLoc.equalsIgnoreCase(locale))
			return mobileLoc;

		if (locale != null && !locale.equals(UNKNOWN) && !locale.equals(""))
			// The general case
			return mobileLoc + OPEN_P + locale + CLOSE_P;
		
		return mobileLoc;
	}


	/**
	 * Prints the container unit of the unit.
	 *
	 * @apiNote
	 * (e.g. in a container unit such as person, robot, building, vehicle, or Mars Surface)
	 *
	 * @return the name string of the immediate location
	 */
	public String getMobileContainerUnit() {
		String result = UNKNOWN;
		
		var container = unit.getContainerUnit();
		
		if (unit instanceof Person p) {
			if (container instanceof Settlement) {
				result = p.getBuildingLocation().getName();
			}
			else if (container instanceof Vehicle v) {
				result = v.getName();
			}
			else if (container instanceof MarsSurface) {
				// Print out MarsSurface
				result = container.getName();
			}
			else if (container != null) {
				// Print out container
				result = container.getName();
			}
		}
		else if (unit instanceof Robot r) {
			if (container instanceof Settlement) {
				result = r.getBuildingLocation().getName();
			}
			else if (container instanceof Vehicle v) {
				result = v.getName();
			}
			else if (container instanceof MarsSurface) {
				// Print out MarsSurface
				result = container.getName();
			}
			else if (container != null) {
				// Print out container
				result = container.getName();
			}
		}
		else if (unit instanceof Equipment e) {
			if (container instanceof Settlement) {
				result = e.getBuildingLocation().getName();
			}
			else if (container instanceof Vehicle v) {
				result = v.getName();
			}
			else if (container instanceof Person p) {
				result = p.getName();
			}
			else if (container instanceof Robot r) {
				result = r.getName();
			}
			else if (container instanceof MarsSurface) {
				// Print out MarsSurface
				result = container.getName();
			}
			else if (container != null) {
				// Print out container
				result = container.getName();
			}
		}	
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
