/*
 * Mars Simulation Project
 * LocationTag.java
 * @vdate 2021-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;
import java.util.Collection;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
				return r.getCoordinates().getFormattedString();
		} else if (b != null) {
			return b.getSettlement().getName();
		} else if (v != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType())
				return v.getSettlement().getName();
			else
				return v.getCoordinates().getFormattedString();
		}
		return UNKNOWN;
	}

	/**
	 * Prints the quick location name of the unit
	 * 
	 * @apiNote e.g. settlement, buried settlement, building,
	 * vehicle or coordinates 
	 * 
	 * @return the name string of the quick location 
	 */
	public String getQuickLocation() {
		if (p != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == p.getLocationStateType()) {
				if (p.getBuildingLocation() != null) {
					return p.getBuildingLocation().getNickName();
				}
			}
			
			else if (LocationStateType.INSIDE_VEHICLE == p.getLocationStateType())
				return p.getVehicle().getName();
			
			else if (p.isDeclaredDead()) {
				if (p.getAssociatedSettlement() != null)
					return p.getAssociatedSettlement().getName(); 
				else if (p.isBuried() && p.getBuriedSettlement() != null)
					return p.getBuriedSettlement().getName();
			}
			
			else if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == p.getLocationStateType())
				return findSettlementVicinity().getName() + VICINITY;
			
			return p.getCoordinates().getFormattedString();
		}

		else if (r != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == r.getLocationStateType()) {
				if (r.getBuildingLocation() != null) {
					return r.getBuildingLocation().getNickName();
				}
			}
			
			else if (LocationStateType.INSIDE_VEHICLE == r.getLocationStateType())
				return r.getVehicle().getName();
						
			else if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == r.getLocationStateType())
				return findSettlementVicinity().getName() + VICINITY;
			
			return r.getCoordinates().getFormattedString();
		}
		
		else if (e != null) {
			if (e.getContainerID() != 0)
				return e.getContainerUnit().getName();
			else if (e.getTopContainerID() != 0)
				return e.getTopContainerUnit().getName();
			
			return e.getCoordinates().getFormattedString();
		}
		
		else if (b != null) {
			return b.getSettlement().getName();
		}

		else if (v != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType())
				return v.getBuildingLocation().getNickName();
			else if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == v.getLocationStateType())
				return findSettlementVicinity().getName();
			else
				return v.getCoordinates().getFormattedString();
		}
		
		else if (s != null) {
			return MARS_SURFACE;
		}

		return UNKNOWN;
	}

	/**
	 * Prints the locale (settlement, vehicle or coordinates) of the unit
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
			
			if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == p.getLocationStateType())
				return findSettlementVicinity().getName() + VICINITY;
			
			if (LocationStateType.MARS_SURFACE == p.getLocationStateType()) {			
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s.getName() + VICINITY;	
				Vehicle v = findNearbyVehicleVicinity();
				if (v != null)
					return v.getName() + VICINITY;				
			}
		}

		else if (r != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == r.getLocationStateType())
				return r.getSettlement().getName();
			if (p != null && LocationStateType.INSIDE_VEHICLE == p.getLocationStateType())
				return p.getVehicle().getName();
		} 
		
		else if (e != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == e.getLocationStateType())
				return e.getSettlement().getName();

		}
		
		else if (b != null) {
			return b.getSettlement().getName();
		}

		else if (v != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType())
				return v.getSettlement().getName();
			if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == v.getLocationStateType())
				return findSettlementVicinity().getName() + VICINITY;
			if (LocationStateType.MARS_SURFACE == v.getLocationStateType()) {			
				Settlement s = findSettlementVicinity();
				if (s != null)
					return s.getName() + VICINITY;	
				Vehicle v = findNearbyVehicleVicinity();
				if (v != null)
					return v.getName() + VICINITY;				
			}
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
	 * Prints the immediate location of the unit
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
					return p.getBuildingLocation().getNickName();
				}
				
				return UNKNOWN;
			} 
			
			if (LocationStateType.INSIDE_VEHICLE == p.getLocationStateType()) {
				return p.getVehicle().getNickName();
			} 
			
			if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == p.getLocationStateType() 
					|| p.isRightOutsideSettlement())
				return findSettlementVicinity().getName() + VICINITY;
			
			return MARS_SURFACE;
		}

		else if (r != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == r.getLocationStateType()) {
				if (r.getBuildingLocation() != null) {
					return r.getBuildingLocation().getNickName();
				}
				return UNKNOWN;
			} 
			
			if (LocationStateType.INSIDE_VEHICLE == r.getLocationStateType()) {
				return r.getVehicle().getNickName();
			} 
			
			if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == p.getLocationStateType() 
					|| r.isRightOutsideSettlement())
				return findSettlementVicinity().getName() + VICINITY;

			return MARS_SURFACE;
		}
		
		else if (e != null) {
			if (LocationStateType.ON_PERSON_OR_ROBOT == e.getLocationStateType())
				return e.getContainerUnit().getLocationTag().getImmediateLocation();
			if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == e.getLocationStateType() || e.isRightOutsideSettlement())
				return findSettlementVicinity().getName() + VICINITY;
			if (e.isInside()) 
				return e.getContainerUnit().getName();
			
			return MARS_SURFACE;
		}
		
		else if (b != null) {
			return b.getSettlement().getName();
		}

		else if (v != null) {
			if (LocationStateType.INSIDE_SETTLEMENT == v.getLocationStateType()) {
				if (v.getBuildingLocation() != null) {
					return v.getBuildingLocation().getNickName();
				} 
				
				return UNKNOWN;
			} 
			if (LocationStateType.WITHIN_SETTLEMENT_VICINITY == v.getLocationStateType())
				return findSettlementVicinity().getName() + VICINITY;
			
			return MARS_SURFACE;
		}
		
		else if (s != null) {
			return MARS_SURFACE;
		}

		return UNKNOWN;
	}

	/**
	 * Prints the modified immediate location 
	 * 
	 * @return the name string of the location the unit is at
	 */
	public String getModifiedLoc() {
		String loc = getImmediateLocation();
		loc = loc == null ? "[N/A]" : loc;
		loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
		return loc;
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

		if (unit instanceof Person && ((Person) unit).isBuried())
			return ((Person) unit).getBuriedSettlement();
		
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
