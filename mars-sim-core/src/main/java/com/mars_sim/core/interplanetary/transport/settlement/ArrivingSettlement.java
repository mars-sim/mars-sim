/*
 * Mars Simulation Project
 * ArrivingSettlement.java
 * @date 2023-05-01
 * @author Scott Davis
 */
package com.mars_sim.core.interplanetary.transport.settlement;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.events.ScheduledEventManager;
import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.InitialSettlement;
import com.mars_sim.core.structure.SettlementBuilder;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A new arriving settlement from Earth.
 */
public class ArrivingSettlement extends Transportable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private int populationNum;
	private int numOfRobots;
	private int templateID;
	
	private String template;
	
	private String sponsorCode;

	private int arrivalSols;
	
	/**
	 * Constructor.
	 * 
	 * @param name            the name of the arriving settlement.
	 * @param template        the design template for the settlement.
	 * @param sponsor 
	 * @param arrivalSols     the arrival in terms of Sols in the future.
	 * @param landingLocation the landing location.
	 * @param populationNum   the population of new immigrants arriving with the
	 *                        settlement.
	 * @param numOfRobots     the number of new robots.
	 */
	public ArrivingSettlement(String name, String template, String sponsorCode,
			int arrivalSols, Coordinates landingLocation,
			int populationNum, int numOfRobots) {
		super(name, landingLocation);
		this.template = template;
		this.sponsorCode = sponsorCode;
		this.arrivalSols = arrivalSols;
		this.populationNum = populationNum;
		this.numOfRobots = numOfRobots;
	}

	/**
	 * The shared Transport manager handles arriving settlements
	 * @return Settlement's scheduled events
	 */
	@Override
	protected ScheduledEventManager getOwningManager() {
		return tm.getFutureEvents();
	}

	/**
	 * Gets the templateID of the arriving settlement.
	 * 
	 * @return settlement templateID
	 */
	public int getTemplateID() {
		return templateID;
	}

	/**
	 * Sets the templateID of the arriving settlement.
	 * 
	 * @param settlement id
	 */
	public void setTemplateID(int id) {
		templateID = id;
	}

	/**
	 * Gets the design template of the arriving settlement.
	 * 
	 * @return the settlement template string.
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * Sets the design template of the arriving settlement.
	 * 
	 * @param template the settlement template string.
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * Gets the Sponsor of the settlement.
	 * 
	 * @return sponsor
	 */
	public String getSponsorCode() {
		return sponsorCode;
	}
	
	/**
	 * Sets the sponsor of the Arriving Settlement
	 * @param sponsor
	 */
	public void setSponsorCode(String sponsor) {
		this.sponsorCode = sponsor;
	}

	/**
	 * The original arrival delay
	 * @return
	 */
	public int getArrivalSols() {
		return arrivalSols;
	}


	/**
	 * Gets the population of the arriving settlement.
	 * 
	 * @return population number.
	 */
	public int getPopulationNum() {
		return populationNum;
	}

	/**
	 * Sets the population of the arriving settlement.
	 * 
	 * @param populationNum the population number.
	 */
	public void setPopulationNum(int populationNum) {
		this.populationNum = populationNum;
	}

	/**
	 * Gets the number of robots of the arriving settlement.
	 * 
	 * @return numOfRobots.
	 */
	public int getNumOfRobots() {
		return numOfRobots;
	}

	/**
	 * Sets the number of robots of the arriving settlement.
	 * 
	 * @param numOfRobots.
	 */
	public void setNumOfRobots(int numOfRobots) {
		this.numOfRobots = numOfRobots;
	}

	@Override
	public String getSettlementName() {
		return getName();
	}

	@Override
	public String toString() {
		var buff = new StringBuilder();
		buff.append(getName());
		buff.append(": ");
		buff.append(getArrivalDate().getDateTimeStamp());
		return buff.toString();
	}

	@Override
	protected synchronized void performArrival(SimulationConfig sc, Simulation sim) {
		SettlementBuilder build = new SettlementBuilder(sim, sc, null);
		InitialSettlement spec = new InitialSettlement(getName(), sponsorCode,
													   template, populationNum, numOfRobots,
													   getLandingLocation(), null);
		build.createFullSettlement(spec);
	}

	/**
	 * Schedule the launch for a future date.
	 */
	public void scheduleLaunch() {
		// Determine the arrival date
		MarsTime proposedArrival = tm.getMarsTime().addTime(
					(arrivalSols - 1) * 1000D
					+ 100 
					+ RandomUtil.getRandomDouble(890));
		setArrivalDate(proposedArrival);
	}
	
	
	@Override
	public void reinit(UnitManager um) {
		// This is not needed
	}

    public void setLandingLocation(Coordinates landingLocation) {
		updateLandingLocation(landingLocation);
    }
}
