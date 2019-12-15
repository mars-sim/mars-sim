/**
 * Mars Simulation Project
 * LocationTag.java
* @version 3.1.0 2017-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class LocationTag implements LocationState, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String OUTSIDE_ON_MARS = Conversion.capitalize(LocationStateType.OUTSIDE_ON_THE_SURFACE_OF_MARS.getName());

	public static final String VICINITY = " vicinity";

	private static final String UNKNOWN = LocationStateType.UNKNOWN.getName();

	private static final String IN = " in ";

	private Unit unit;

	private Person p = null;
	private Robot r = null;
	private Equipment e = null;
	private Building b = null;
	private Vehicle v = null;
	private Settlement s = null;
	private MarsSurface ms = null;
	

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
		else if (unit instanceof MarsSurface)
			ms = (MarsSurface) unit;
	}

	/**
	 * Gets the name of the settlement the unit is at
	 * 
	 * @return
	 */
	public String getSettlementName() {
		if (p != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == p.getLocationStateType())
				return p.getSettlement().getName();
			else
				return p.getCoordinates().getFormattedString();
		} else if (e != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == e.getLocationStateType())
				return e.getSettlement().getName();
			else
				return e.getCoordinates().getFormattedString();
		} else if (r != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == r.getLocationStateType())
				return r.getSettlement().getName();
			else
				return r.getCoordinates().getFormattedString();// OUTSIDE_ON_MARS;
		} else if (b != null) {
			return b.getSettlement().getName();
		}

		else if (v != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType())
				return v.getSettlement().getName();
			else
				return v.getCoordinates().getFormattedString();// OUTSIDE_ON_MARS;
		}

		return UNKNOWN;
	}

	/**
	 * Obtains the quick location name (either settlement, buried settlement,
	 * vehicle or coordinates)
	 * 
	 * @return the name string of the location the unit is at
	 */
	public String getQuickLocation() {
		if (p != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == p.getLocationStateType())
				return p.getSettlement().getName();
			else if (LocationStateType.INSIDE_VEHICLE == p.getLocationStateType())
				return p.getVehicle().getName();
			else if (p.isDeclaredDead()) {
				if (p.getAssociatedSettlement() != null)
					return p.getAssociatedSettlement().getName(); 
				else if (p.isBuried() && p.getBuriedSettlement() != null)
					return p.getBuriedSettlement().getName();
				else
					return p.getCoordinates().getFormattedString();
			}	
			else
				return p.getCoordinates().getFormattedString();
		}

		else if (e != null) {
			if (e.getContainerID() != 0)
				return e.getContainerUnit().getName();
			else if (e.getTopContainerID() != 0)
				return e.getTopContainerUnit().getName();
			else
				return e.getCoordinates().getFormattedString();
		}

		else if (r != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == r.getLocationStateType())
				return r.getSettlement().getName();
			else if (LocationStateType.INSIDE_VEHICLE == r.getLocationStateType())
				return r.getVehicle().getName();
			else
				return r.getCoordinates().getFormattedString();

		} else if (b != null) {
			return b.getNickName() + " in " + b.getSettlement().getName();
		}

		else if (v != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType())
				return v.getSettlement().getName();
			else if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType())
				return v.getBuildingLocation().getNickName();
			else
				return v.getCoordinates().getFormattedString();
		}

		return UNKNOWN;
	}

	/**
	 * Obtains the general locale (settlement or coordinates)
	 * 
	 * @return the exact or nearby settlement/vehicle
	 */
	public String getLocale() {
		if (p != null) {
			if (p.getPhysicalCondition() != null && p.getPhysicalCondition().isDead())
				return p.getPhysicalCondition().getDeathDetails().getPlaceOfDeath();
			else if (LocationStateType.INSIDE_SETTLEMENT == p.getLocationStateType())
				return p.getSettlement().getName();
			else if (LocationStateType.INSIDE_VEHICLE == p.getLocationStateType())
				return p.getVehicle().getName();
			else if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == p.getLocationStateType())
				return findSettlementVicinity().getName();
			else if (LocationStateType.OUTSIDE_ON_THE_SURFACE_OF_MARS == p.getLocationStateType()) {			
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s.getName();	
				Vehicle v = findNearbyVehicleVicinity();
				if (v != null)
					return v.getName();				
			}
			else
				return p.getCoordinates().getFormattedString();
		}

		else if (e != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == e.getLocationStateType())
				return e.getSettlement().getName();
			else
				return e.getCoordinates().getFormattedString();
		}

		else if (r != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == r.getLocationStateType())
				return r.getSettlement().getName();
			else
				return r.getCoordinates().getFormattedString();

		} 
		
		else if (b != null) {
			return b.getSettlement().getName();
		}

		else if (v != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType())
				return v.getSettlement().getName();		
			else
				return v.getCoordinates().getFormattedString();
		}

		return UNKNOWN;
	}

	/**
	 * Obtains the extended location details
	 * e.g. Lander Hab 1 in New Pompeii
	 * @return the name string of the location the unit is at
	 */
	public String getExtendedLocations() {
		String immediate = getImmediateLocation();
		if (immediate.equals(OUTSIDE_ON_MARS))
			return OUTSIDE_ON_MARS + " of " + getLocale();
		if (immediate.equalsIgnoreCase(getLocale()))
			return immediate;
		
		return immediate + IN + getLocale();
	}

	/**
	 * Obtains the immediate location (either building, vehicle, a settlement's
	 * vicinity or outside on Mars)
	 * 
	 * @return the name string of the location the unit is at
	 */
	public String getImmediateLocation() {
		if (p != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == p.getLocationStateType()) {
				if (p.getBuildingLocation() != null) {
					return p.getBuildingLocation().getNickName();
				}
				else {
					return p.getLocationStateType().getName();
				}
			} else if (LocationStateType.INSIDE_VEHICLE == p.getLocationStateType()) {
				Vehicle v = p.getVehicle();
				if (v.getBuildingLocation() == null) {
					return v.getNickName();
				} else {
					return v.getBuildingLocation().getNickName();
				}
			} 
			else if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == p.getLocationStateType() || p.isRightOutsideSettlement())
				return findSettlementVicinity().getName() + VICINITY;
			// TODO: check if it works in case of a trader arrives at any settlements for
			// trades.
			else
				return OUTSIDE_ON_MARS;
		}

		else if (e != null) {
			if (LocationStateType.ON_A_PERSON_OR_ROBOT == e.getLocationStateType())
				return e.getContainerUnit().getLocationTag().getImmediateLocation();
			else if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == e.getLocationStateType() || e.isRightOutsideSettlement())
				return findSettlementVicinity().getName() + VICINITY;
			else if (e.isInside()) //!(e.getContainerUnit() instanceof MarsSurface))
				return e.getContainerUnit().getName();
			else
				return OUTSIDE_ON_MARS;
		}

		else if (r != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == r.getLocationStateType()) {
				if (r.getBuildingLocation() != null) {
					return r.getBuildingLocation().getNickName();
				} else {
					return OUTSIDE_ON_MARS;
				}
			} else if (r.getVehicle() != null) {
				Vehicle v = r.getVehicle();
				if (v.getBuildingLocation() == null) {
					return v.getNickName();
				} else {
					return v.getBuildingLocation().getNickName();
				}
			} 
			else if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == r.getLocationStateType() || r.isRightOutsideSettlement())
				return findSettlementVicinity().getName() + VICINITY;
			else
				return OUTSIDE_ON_MARS;

		} else if (b != null) {
			return b.getNickName();
		}

		else if (v != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType()) {
				if (v.getBuildingLocation() != null) {
					return v.getBuildingLocation().getNickName();
				} else {
					return OUTSIDE_ON_MARS;
				}
			} 
			else if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == v.getLocationStateType() || v.isRightOutsideSettlement())
				return findSettlementVicinity().getName() + VICINITY;
			else
				return OUTSIDE_ON_MARS;
		}

		return UNKNOWN;
	}

	/**
	 * Finds the settlement that a person/robot is in its vicinity
	 * 
	 * @return {@link Settlement}
	 */
	public Settlement findSettlementVicinity() {
		Coordinates c = unit.getCoordinates();

		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
				
		Collection<Settlement> ss = unitManager.getSettlements();
		for (Settlement s : ss) {
			if (s.getCoordinates().equals(c) || s.getCoordinates() == c)
				return s;
		}

//		if (unit instanceof Person && ((Person) unit).getPhysicalCondition().isDead())
//			return ((Person) unit).getBuriedSettlement();
		
		return unit.getAssociatedSettlement(); 
	}

	/**
	 * Finds the vehicle that drops off a person/robot outside on Mars
	 * 
	 * @return {@link Vehicle}
	 */
	public Vehicle findNearbyVehicleVicinity() {
		Coordinates c = unit.getCoordinates();

		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
				
		Collection<Vehicle> list = unitManager.getVehicles();
		for (Vehicle v : list) {
			if (v.getCoordinates().equals(c) || v.getCoordinates() == c)
				return v;
		}

		return null; 
	}
	
	/**
	 * Checks if an unit is in the vicinity of a settlement
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
	 * Gets the unit's location state type
	 */
	public LocationStateType getType() {
		return unit.getLocationStateType();
	}

//	public LocationSituation getLocationSituation() {
//		if (p != null) {
//			if (p.getLocationSituation() != null)
//				return p.getLocationSituation();
//		} else if (e != null) {
//			if (e.getLocationSituation() != null)
//				return e.getLocationSituation();
//		} else if (r != null) {
//			if (r.getLocationSituation() != null)
//				return r.getLocationSituation();
//		}
//		return LocationSituation.UNKNOWN;
//	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
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