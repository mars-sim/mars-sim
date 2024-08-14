/*
 * Mars Simulation Project
 * LocationTag.java
 * @date 2024-07-17
 * @author Manny Kung
 */
package com.mars_sim.core.location;

import java.io.Serializable;
import java.util.Collection;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The LocationTag class stores the location information of an object.
 */
public class LocationTag implements LocationState, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String MARS_SURFACE = Conversion.capitalize(LocationStateType.MARS_SURFACE.getName());

	public static final String VICINITY = " Vicinity";

	private static final String UNKNOWN = Conversion.capitalize(LocationStateType.UNKNOWN.getName());

	private static final String IN = " in ";

	private Unit unit;

	private Person p = null;
	private Robot r = null;
	private Equipment e = null;
	private Building b = null;
	private Vehicle v = null;
	private Settlement s = null;

	private static UnitManager unitManager = Simulation.instance().getUnitManager();

	public LocationTag(Unit unit) {
		this.unit = unit;
		if (unit instanceof Person)
			p = (Person) unit;
		else if (unit instanceof Robot)
			r = (Robot) unit;
		else if (unit instanceof Equipment)
			e = (Equipment) unit;
		else if (unit instanceof Building)
			b = (Building) unit;
		else if (unit instanceof Vehicle)
			v = (Vehicle) unit;
		else if (unit instanceof Settlement)
			s = (Settlement) unit;
	}

	/**
	 * Prints the quick location name of the unit.
	 *
	 * @return the name string of the quick location
	 */
	public String getQuickLocation() {
		if (p != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == p.getLocationStateType()) {
				Building bb = p.getBuildingLocation();
				if (bb != null) {
					return bb.getName();
				}
				return p.getCoordinates().getFormattedString();
			}

			else if (LocationStateType.INSIDE_VEHICLE == p.getLocationStateType())
				return p.getVehicle().getName();

			else if (p.isDeclaredDead()) {
				if (p.getAssociatedSettlement() != null)
					return p.getAssociatedSettlement().getName();
				else if (p.isBuried() && p.getBuriedSettlement() != null)
					return p.getBuriedSettlement().getName();
			}

			Settlement settlement = findSettlementVicinity();
			if (settlement != null) {
				return settlement.getName() + VICINITY;
			}
			
			Vehicle vehicle = findVehicleVicinity();
			if (vehicle != null) {
				return vehicle.getName() + VICINITY;
			}
			 
			return p.getCoordinates().getFormattedString();
		}

		return UNKNOWN;
	}

	/**
	 * Prints the locale (settlement, vehicle or coordinates) of the unit.
	 *
	 * @return the general (nearby) location
	 */
	public String getLocale() {
		if (p != null) {
			if (p.getPhysicalCondition().isDead()
				&& p.getPhysicalCondition().getDeathDetails() != null) {
				return p.getPhysicalCondition().getDeathDetails().getPlaceOfDeath();
			}
			
			if (LocationStateType.INSIDE_SETTLEMENT == p.getLocationStateType())
				return p.getSettlement().getName();
			
			if (LocationStateType.INSIDE_VEHICLE == p.getLocationStateType())
				return p.getVehicle().getName();
			
			if (LocationStateType.MARS_SURFACE == p.getLocationStateType()) {
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s.getName() + VICINITY;
				Vehicle v = findVehicleVicinity();
				if (v != null)
					return v.getName() + VICINITY;
			}
			
			if (LocationStateType.VEHICLE_VICINITY == p.getLocationStateType()) {
				Vehicle v = findVehicleVicinity();
				if (v != null)
					return v + VICINITY;			
			}
			
			if (LocationStateType.SETTLEMENT_VICINITY == p.getLocationStateType()) {
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s + VICINITY;			
			}
			
			return MARS_SURFACE;
		}

		else if (r != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == r.getLocationStateType())
				return r.getSettlement().getName();
			
			if (LocationStateType.INSIDE_VEHICLE == r.getLocationStateType())
				return r.getVehicle().getName();
				
			if (LocationStateType.MARS_SURFACE == r.getLocationStateType()) {
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s.getName() + VICINITY;
				Vehicle v = findVehicleVicinity();
				if (v != null)
					return v.getName() + VICINITY;
			}
			
			if (LocationStateType.VEHICLE_VICINITY == r.getLocationStateType()) {
				Vehicle v = findVehicleVicinity();
				if (v != null)
					return v + VICINITY;			
			}
			
			if (LocationStateType.SETTLEMENT_VICINITY == r.getLocationStateType()) {
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s + VICINITY;			
			}
			
			return MARS_SURFACE;
		}

		else if (e != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == e.getLocationStateType())
				return e.getSettlement().getName();
			
			if (LocationStateType.INSIDE_VEHICLE == e.getLocationStateType())
				return e.getVehicle().getName();
			
			if (LocationStateType.ON_PERSON_OR_ROBOT == e.getLocationStateType())
				return e.getContainerUnit().getLocationTag().getImmediateLocation();
		}

		else if (b != null) {
			return b.getSettlement().getName();
		}

		else if (v != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType())
				return v.getSettlement().getName();
			
			if (LocationStateType.SETTLEMENT_VICINITY == v.getLocationStateType()) {
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s + VICINITY;			
			}
			
			if (LocationStateType.MARS_SURFACE == v.getLocationStateType()) {
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s.getName() + VICINITY;
				Vehicle v = findVehicleVicinity();
				if (v != null)
					return v.getName() + VICINITY;
			}
			
			return MARS_SURFACE;
		}

		else if (s != null) {
			return MARS_SURFACE;
		}

		return unit.getCoordinates().getFormattedString();
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
		if (p != null) {
			if (p.getPhysicalCondition().isDead()
					&& p.getPhysicalCondition().getDeathDetails() != null) {
					return p.getPhysicalCondition().getDeathDetails().getPlaceOfDeath();
				}

			if (LocationStateType.INSIDE_SETTLEMENT == p.getLocationStateType()) {
				if (p.getBuildingLocation() != null) {
					return p.getBuildingLocation().getName();
				}

				return UNKNOWN;
			}

			if (LocationStateType.INSIDE_VEHICLE == p.getLocationStateType()) {
				return p.getVehicle().getName();
			}

			
			if (LocationStateType.MARS_SURFACE == p.getLocationStateType()) {
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s.getName() + VICINITY;
				Vehicle v = findVehicleVicinity();
				if (v != null)
					return v.getName() + VICINITY;
			}

			return MARS_SURFACE;
		}

		else if (r != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == r.getLocationStateType()) {
				if (r.getBuildingLocation() != null) {
					return r.getBuildingLocation().getName();
				}
				return UNKNOWN;
			}

			if (LocationStateType.INSIDE_VEHICLE == r.getLocationStateType()) {
				return r.getVehicle().getName();
			}
			
			if (LocationStateType.MARS_SURFACE == r.getLocationStateType()) {
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s.getName() + VICINITY;
				Vehicle v = findVehicleVicinity();
				if (v != null)
					return v.getName() + VICINITY;
			}

			return MARS_SURFACE;
		}

		else if (e != null) {
			if (LocationStateType.ON_PERSON_OR_ROBOT == e.getLocationStateType())
				return e.getContainerUnit().getLocationTag().getImmediateLocation();

			if (e.isInside())
				return e.getContainerUnit().getName();
		
			if (LocationStateType.VEHICLE_VICINITY == e.getLocationStateType()) {
				Vehicle v = findVehicleVicinity();
				if (v != null)
					return v + VICINITY;			
			}
			
			if (LocationStateType.SETTLEMENT_VICINITY == e.getLocationStateType()) {
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s + VICINITY;			
			}
			
			return MARS_SURFACE;
		}

		else if (b != null) {
			return b.getSettlement().getName();
		}

		else if (v != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType()) {
				if (v.getBuildingLocation() != null) {
					return v.getBuildingLocation().getName();
				}

				return UNKNOWN;
			}
			if (v.isRightOutsideSettlement()) {
				Settlement settlement = findSettlementVicinity();
				if (settlement != null)
					return settlement.getName() + VICINITY;					
			}

			return MARS_SURFACE;
		}

		else if (s != null) {
			return MARS_SURFACE;
		}

		return UNKNOWN;
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

		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		return unitManager.findSettlement(c);		
	}

	/**
	 * Finds the nearby vehicle that drops off a person/robot in the vicinity.
	 *
	 * @return {@link Vehicle}
	 */
	public Vehicle findVehicleVicinity() {
		Coordinates c = unit.getCoordinates();
		Settlement settlement = unit.getAssociatedSettlement();
		
		Collection<Vehicle> list = settlement.getMissionVehicles();
		for (Vehicle v : list) {
			if (v.getCoordinates().equals(c) || v.getCoordinates() == c)
				return v;
		}
		
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		Collection<Vehicle> list2 = unitManager.getVehicles();
		for (Vehicle v : list2) {
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

		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		Collection<Settlement> ss = unitManager.getSettlements();
		for (Settlement s : ss) {
			if (s.getCoordinates() == c || s.getCoordinates().equals(c))
				return true;
		}

		return false;
	}

	/*
	 * Gets the unit's location state type.
	 */
	public LocationStateType getType() {
		return unit.getLocationStateType();
	}


	@Override
	public String getName() {
		return null;
	}

	public void destroy() {
		unit = null;
		p = null;
		r = null;
		e = null;
		b = null;
		v = null;
	}

}
