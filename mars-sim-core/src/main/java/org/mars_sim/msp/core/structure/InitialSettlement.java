/**
 * Mars Simulation Project
 * InitialSettlement.java
 * @version 3.2.0 2021-07-10
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;

/**
 * POJO class for holding a initial settlement info.
 */
public class InitialSettlement implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private String name;
	private ReportingAuthorityType sponsor = ReportingAuthorityType.MS; 
	private String template;
	private int populationNumber;
	private int numOfRobots;
	private Coordinates location;
	
	public InitialSettlement(String name, ReportingAuthorityType sponsor, String template, int populationNumber,
			int numOfRobots, Coordinates location) {
		super();
		this.name = name;
		this.sponsor = sponsor;
		this.template = template;
		this.populationNumber = populationNumber;
		this.numOfRobots = numOfRobots;
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public String getSettlementTemplate() {
		return template;
	}

	public ReportingAuthorityType getSponsor() {
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
}