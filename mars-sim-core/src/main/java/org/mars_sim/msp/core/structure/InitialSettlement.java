/**
 * Mars Simulation Project
 * InitialSettlement.java
 * @version 3.2.0 2021-07-10
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;

import org.mars.sim.mapdata.location.Coordinates;

/**
 * POJO class for holding a initial settlement info.
 */
public class InitialSettlement implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private String name;
	private String sponsor; 
	private String template;
	private int populationNumber;
	private int numOfRobots;
	private Coordinates location;
	private String crew;
	
	public InitialSettlement(String name, String sponsor, String template, int populationNumber,
			int numOfRobots, Coordinates location, String crew) {
		super();
		this.name = name;
		this.sponsor = sponsor;
		this.template = template;
		this.populationNumber = populationNumber;
		this.numOfRobots = numOfRobots;
		this.location = location;
		this.crew = crew;
	}

	public String getName() {
		return name;
	}

	public String getSettlementTemplate() {
		return template;
	}

	public String getSponsor() {
		return sponsor;
	}

	public int getPopulationNumber() {
		return populationNumber;
	}

	public int getNumOfRobots() {
		return numOfRobots;
	}

	public Coordinates getLocation() {
		return location;
	}
	
	public String getCrew() {
		return crew;
	}
}