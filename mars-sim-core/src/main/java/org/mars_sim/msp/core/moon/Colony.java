/*
 * Mars Simulation Project
 * Colony.java
 * @date 2023-09-25
 * @author Manny Kung
 */

package org.mars_sim.msp.core.moon;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.Loggable;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.reportingAuthority.Organization;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public class Colony implements Serializable, Temporal, Loggable, Comparable<Colony> {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Colony.class.getName());

	private String name;
	
	/** The settlement's ReportingAuthority instance. */
	private ReportingAuthority sponsor;

	private Coordinates location;
	
	private Population population;
	
	private Simulation sim;
	
	Set<Zone> zones = new HashSet<>();
	
	public Colony(String name, ReportingAuthority sponsor, Coordinates location) {
		this.name = name;
		this.sponsor = sponsor;
		this.location = location;
		
		population = new Population();
		
		for (ZoneType type: ZoneType.values()) {
			addZone(new Zone(type));
		}
		
	}

	public void addZone(Zone zone) {
		zones.add(zone);
	}
	
	public Set<Zone> getZones() {
		return zones;
	}
	
	public Organization getOrganization() {
		return sponsor.getOrganization();
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		getOrganization().timePassing(pulse);
		
		population.timePassing(pulse);
		
		for (Zone z: zones) {
			z.timePassing(pulse);
		}
		
		return true;
	}
	
	public Population getPopulation() {
		return population;
	}
	
	public double getTotalArea() {
		double sum = 0;
		for (Zone z: zones) {
			sum += z.getArea();
		}
		return sum;
	}

	/**
	 * Gets the authority.
	 */
	public ReportingAuthority getReportingAuthority() {
		return sponsor;
	}

	/**
	 * Compares this object with the specified object for order.
	 *
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(Colony	o) {
		return name.compareToIgnoreCase(o.name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Coordinates getCoordinates() {
		return location;
	}

	@Override
	public Unit getContainerUnit() {
		if (sim == null) {
			sim = Simulation.instance();
		}
		return sim.getUnitManager().getMoon();
	}
}



