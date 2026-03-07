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
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.events.ScheduledEventManager;
import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.InitialSettlement;
import com.mars_sim.core.structure.SettlementBuilder;
import com.mars_sim.core.time.MarsTime;

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
	
	/**
	 * Constructor.
	 * 
	 * @param name            the name of the arriving settlement.
	 * @param template        the design template for the settlement.
	 * @param sponsorCode     the sponsor code for the settlement.
	 * @param arrivalDate     the arrival date of the settlement.
	 * @param landingLocation the landing location.
	 * @param populationNum   the population of new immigrants arriving with the
	 *                        settlement.
	 * @param numOfRobots     the number of new robots.
	 */
	public ArrivingSettlement(String name, String template, String sponsorCode,
			MarsTime arrivalDate, Coordinates landingLocation,
			int populationNum, int numOfRobots) {
		super(name, landingLocation);
		this.template = template;
		this.sponsorCode = sponsorCode;
		this.populationNum = populationNum;
		this.numOfRobots = numOfRobots;

		setArrivalDate(arrivalDate);
	}

	/**
	 * Gets the shared Transport manager handles arriving settlement.
	 * 
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
	protected synchronized HistoricalEvent performArrival(SimulationConfig sc, Simulation sim) {
		SettlementBuilder build = new SettlementBuilder(sim, sc, null);
		InitialSettlement spec = new InitialSettlement(getName(), sponsorCode,
													   template, populationNum, numOfRobots,
													   getLandingLocation(), null);
		var s = build.createFullSettlement(spec);

		return new HistoricalEvent(HistoricalEventType.TRANSPORT_ITEM_ARRIVED, this,
						this.getName(), "", "", this, s);
	}


	
	@Override
	public void reinit(UnitManager um) {
		// This is not needed
	}

    public void setLandingLocation(Coordinates landingLocation) {
		updateLandingLocation(landingLocation);
    }
}
