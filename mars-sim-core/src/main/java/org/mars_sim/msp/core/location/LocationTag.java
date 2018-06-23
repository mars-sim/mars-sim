/**
 * Mars Simulation Project
 * LocationTag.java
* @version 3.1.0 2017-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class LocationTag implements LocationState, Serializable {

	private static final long serialVersionUID = 1L;

	public static String OUTSIDE_ON_MARS = "Outside on Mars";

	public static String VICINITY = " vicinity";
	
	private String UNKNOWN = "Unknown";
	
	private Unit unit;

	private Person p = null;
	private Robot r = null;
	private Equipment e = null;
	private Building b = null;
	private Vehicle v = null;

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
		
	}
	
	public String getSettlementName() {
		if (p != null) {
			if (p.getSettlement() != null)
				return p.getSettlement().getName();
			else
				return p.getCoordinates().getFormattedString();	
		}
		else if (e != null) {
			if (e.getSettlement() != null)
				return e.getSettlement().getName();
			else
				return e.getCoordinates().getFormattedString();	
		}
		else if (r != null) {
			if (r.getSettlement() != null)
				return r.getSettlement().getName();
			else
				return r.getCoordinates().getFormattedString();//OUTSIDE_ON_MARS;		
		}
		else if (b != null) {
			return b.getSettlement().getName();
		}
		
		else if (v != null) {
			if (v.getSettlement() != null)
				return v.getSettlement().getName();
			else
				return v.getCoordinates().getFormattedString();//OUTSIDE_ON_MARS;		
		}
		
		return UNKNOWN;
	}

	/**
	 * Obtains the short location name
	 * @return the name string of the location the unit is at
	 */
	public String getShortLocationName() {
		if (p != null) {
			if (p.getSettlement() != null)
				return p.getSettlement().getName();
			else if (p.getVehicle() != null)
				return p.getVehicle().getName();
			else
				return p.getCoordinates().getFormattedString();
			
		}
		
		else if (e != null) {
			if (e.getContainerUnit() != null)
				return e.getContainerUnit().getName();
			else if (e.getTopContainerUnit() != null)
				return e.getTopContainerUnit().getName();
			else
				return e.getCoordinates().getFormattedString();	
		}
		
		else if (r != null) {
			if (r.getSettlement() != null)
				return r.getSettlement().getName();
			else if (r.getVehicle() != null)
				return r.getVehicle().getName();
			else
				return r.getCoordinates().getFormattedString();
	
		}
		else if (b != null) {
			return b.getNickName() + " in " + b.getSettlement().getName();
		}
		
		else if (v != null) {
			if (v.getSettlement() != null) {
				if (v.getBuildingLocation() != null)
					return v.getBuildingLocation().getNickName();
				else
					return v.getSettlement().getName();
			}
			else
				return v.getCoordinates().getFormattedString();	
		}
		
		return UNKNOWN;
	}
	
	/**
	 * Obtains the general locale (settlement or coordinates)
	 * @return the settlement or the coordinates
	 */
	public String getLocale() {
		if (p != null) {
			if (p.getSettlement() != null)
				return p.getSettlement().getName();
			else
				return p.getCoordinates().getFormattedString();		
		}
		
		else if (e != null) {
//			if (e.getContainerUnit() != null)
//				return e.getContainerUnit().getName();
			if (e.getTopContainerUnit() != null)
				return e.getTopContainerUnit().getName();
			else
				return e.getCoordinates().getFormattedString();	
		}
		
		else if (r != null) {
			if (r.getSettlement() != null)
				return r.getSettlement().getName();
//			else if (r.getVehicle() != null)
//				return r.getVehicle().getName();
			else
				return r.getCoordinates().getFormattedString();
	
		}
		else if (b != null) {
			return b.getSettlement().getName();
		}
		
		else if (v != null) {
			if (v.getSettlement() != null) {
//				if (v.getBuildingLocation() != null)
//					return v.getBuildingLocation().getNickName();
//				else
					return v.getSettlement().getName();
			}
			else {
//				if (v.getLocationStateType() == LocationStateType.OUTSIDE_SETTLEMENT_VICINITY)
//					return v.getAssociatedSettlement().getName() + " Vicinity";
//				else
					return v.getCoordinates().getFormattedString();	
			}
		}
		
		return UNKNOWN;
	}
	
	/**
	 * Obtains the long location name
	 * @return the name string of the location the unit is at
	 */
	public String getLongLocationName() {
		if (p != null) {
			if (p.getSettlement() != null) {
				if (p.getBuildingLocation() != null) {
					return p.getBuildingLocation().getNickName() + " in " + p.getSettlement().getName();
				}
				else {
					return p.getSettlement().getName();
				}
			}
			else if (p.getVehicle() != null) {
				if (p.getVehicle().getBuildingLocation() != null) {
					return p.getBuildingLocation().getNickName() + " in " + p.getSettlement().getName();
				}
				else {
					return p.getVehicle().getName() + " at " + p.getCoordinates().getFormattedString();
				}
			}
			else
				return p.getCoordinates().getFormattedString(); 
		}
		
		else if (e != null) {
			if (e.getContainerUnit() != null)
				return e.getContainerUnit().getName();
			else if (e.getTopContainerUnit() != null)
				return e.getTopContainerUnit().getName();
			else
				return e.getCoordinates().getFormattedString();	
		}
		
		else if (r != null) {
			if (r.getSettlement() != null) {
				if (r.getBuildingLocation() != null) {
					return r.getBuildingLocation().getNickName() + " in " + r.getSettlement().getName();
				}
				else {
					return r.getSettlement().getName();
				}
			}
			else if (r.getVehicle() != null)
				return r.getVehicle().getName();
			else
				return p.getCoordinates().getFormattedString(); 
			
		}
		else if (b != null) {
			return b.getNickName() + " in " + b.getSettlement().getName();
		}
		
		else if (v != null) {
			if (v.getSettlement() != null) {
				if (v.getBuildingLocation() != null) {
					return v.getBuildingLocation().getNickName() + " in " + v.getSettlement().getName();
				}
				else {
					return v.getSettlement().getName();
				}
			}
			else
				return v.getCoordinates().getFormattedString();
		}
		
		return UNKNOWN;
	}
	
	
	/**
	 * Obtains the long location name
	 * @return the name string of the location the unit is at
	 */
	public String getImmediateLocation() {
		if (p != null) {
			if (p.getSettlement() != null) {
				if (p.getBuildingLocation() != null) {
					return p.getBuildingLocation().getNickName();
				}
//				else {
//					return OUTSIDE_ON_MARS;
//				}
			}
			else if (p.getVehicle() != null) {
				if (p.getVehicle().getBuildingLocation() != null) {
					return p.getBuildingLocation().getNickName();
				}
				else {
					return p.getVehicle().getName();
				}
			}
			else if (p.isRightOutsideSettlement())
				return p.getAssociatedSettlement() + VICINITY;
			else
				return OUTSIDE_ON_MARS;
		}
		
		else if (e != null) {
			if (e.getContainerUnit() != null)
				return e.getContainerUnit().getName();
			else if (e.getTopContainerUnit() != null)
				return e.getTopContainerUnit().getName();
			else
				return OUTSIDE_ON_MARS;
		}
		
		else if (r != null) {
			if (r.getSettlement() != null) {
				if (r.getBuildingLocation() != null) {
					return r.getBuildingLocation().getNickName();
				}
				else {
					return OUTSIDE_ON_MARS;
				}
			}
			else if (r.getVehicle() != null)
				return r.getVehicle().getName();
			else if (r.isRightOutsideSettlement())
				return r.getAssociatedSettlement() + VICINITY;
			else
				return OUTSIDE_ON_MARS;
			
		}
		else if (b != null) {
			return b.getNickName();
		}
		
		else if (v != null) {
			if (v.getSettlement() != null) {
				if (v.getBuildingLocation() != null) {
					return v.getBuildingLocation().getNickName();
				}
				else {
					return OUTSIDE_ON_MARS;
				}
			}
			else if (v.isRightOutsideSettlement())
				return v.getAssociatedSettlement() + VICINITY;
			else
				return OUTSIDE_ON_MARS;
		}
		
		return UNKNOWN;
	}
	
	public LocationStateType getType() {
		return unit.getLocationStateType();
	}

	public LocationSituation getLocationSituation() {
		if (p != null) {
			if (p.getLocationSituation() != null)
				return p.getLocationSituation();	
		}
		else if (e != null) {
			if (e.getLocationSituation() != null)
				return e.getLocationSituation();	
		}
		else if (r != null) {
			if (r.getLocationSituation() != null)
				return r.getLocationSituation();
		}
		return LocationSituation.UNKNOWN;
	}
	
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