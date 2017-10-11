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

public class LocationTag implements LocationState, Serializable {

	private static final long serialVersionUID = 1L;

	public static String OUTSIDE_ON_MARS = "Outside on Mars";

	private String UNKNOWN = "Unknown";
	
	private Unit unit;

	private Person p = null;
	private Robot r = null;
	private Equipment e = null;
	private Building b = null;

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
		
	}
	
	public String getSettlementName() {
		if (p != null) {
			if (p.getSettlement() != null)
				return p.getSettlement().getName();
			else
				return OUTSIDE_ON_MARS;		
		}
		else if (e != null) {
			if (e.getSettlement() != null)
				return e.getSettlement().getName();
			else
				return OUTSIDE_ON_MARS;		
		}
		else if (r != null) {
			if (r.getSettlement() != null)
				return r.getSettlement().getName();
			else
				return OUTSIDE_ON_MARS;		
		}
		else if (b != null) {
			return b.getSettlement().getName();
		}
		return UNKNOWN;
	}

	public String getLocationName() {
		if (p != null) {
			if (p.getVehicle() != null)
				return p.getVehicle().getName();
			else {
				if (p.getSettlement() != null) {
					if (p.getBuildingLocation() != null) {
						return p.getBuildingLocation().getNickName() + " in " + p.getSettlement().getName();
					}
					else {
						return p.getSettlement().getName();
					}
				}
				else
					return OUTSIDE_ON_MARS;	
			}
		}
		
		else if (e != null) {
			if (e.getContainerUnit() != null)
				return e.getContainerUnit().getName();
			else if (e.getTopContainerUnit() != null)
				return e.getTopContainerUnit().getName();
			//if (e.getSettlement() != null)
			//	return e.getSettlement().getName();
			else
				return OUTSIDE_ON_MARS;		
		}
		
		else if (r != null) {
			if (r.getVehicle() != null)
				return r.getVehicle().getName();
			else {
				if (r.getSettlement() != null) {
					if (r.getBuildingLocation() != null) {
						return r.getBuildingLocation().getNickName() + " in " + r.getSettlement().getName();
					}
					else {
						return r.getSettlement().getName();
					}
				}
				else
					return OUTSIDE_ON_MARS;	
			}
		}
		else if (b != null) {
			return b.getNickName() + " in " + b.getSettlement().getName();
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
	


}