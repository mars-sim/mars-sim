/**
 * Mars Simulation Project
 * ArrivingSettlement.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.settlement;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.interplanetary.transport.TransitState;
import org.mars_sim.msp.core.interplanetary.transport.TransportEvent;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.structure.InitialSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementBuilder;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * A new arriving settlement from Earth.
 */
public class ArrivingSettlement implements Transportable, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private int populationNum;
	private int numOfRobots;
	private int templateID;
	
	private String name;
	private String template;
	
	private TransitState transitState;
	private MarsClock launchDate;
	private MarsClock arrivalDate;
	private Coordinates landingLocation;

	private ReportingAuthorityType sponsor;
	
	/**
	 * Constructor.
	 * 
	 * @param name            the name of the arriving settlement.
	 * @param template        the design template for the settlement.
	 * @param sponsor 
	 * @param arrivalDate     the arrival date.
	 * @param landingLocation the landing location.
	 * @param populationNum   the population of new immigrants arriving with the
	 *                        settlement.
	 * @param numOfRobots     the number of new robots.
	 */
	public ArrivingSettlement(String name, String template, ReportingAuthorityType sponsor,
			MarsClock arrivalDate, Coordinates landingLocation,
			int populationNum, int numOfRobots) {
		this.name = name;
		this.template = template;
		this.sponsor = sponsor;
		this.arrivalDate = arrivalDate;
		this.landingLocation = landingLocation;
		this.populationNum = populationNum;
		this.numOfRobots = numOfRobots;
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
	 * Gets the name of the arriving settlement.
	 * 
	 * @return settlement name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the arriving settlement.
	 * 
	 * @param name settlement name.
	 */
	public void setName(String name) {
		this.name = name;
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
	public ReportingAuthorityType getSponsor() {
		return sponsor;
	}
	
	/**
	 * Sets the sponsor of the Arriving Settlement
	 * @param sponsor
	 */
	public void setSponsor(ReportingAuthorityType sponsor) {
		this.sponsor = sponsor;
	}
	
	/**
	 * Gets the transit state of the settlement.
	 * 
	 * @return transit state string.
	 */
	public TransitState getTransitState() {
		return transitState;
	}

	/**
	 * Sets the transit state of the settlement.
	 * 
	 * @param transitState {@link TransitState} the transit state
	 */
	public void setTransitState(TransitState transitState) {
		this.transitState = transitState;
	}

	/**
	 * Gets the launch date of the settlement.
	 * 
	 * @return the launch date.
	 */
	public MarsClock getLaunchDate() {
		return launchDate;
	}

	/**
	 * Sets the launch date of the settlement.
	 * 
	 * @param launchDate the launch date.
	 */
	public void setLaunchDate(MarsClock launchDate) {
		this.launchDate = launchDate;
	}

	/**
	 * Gets the arrival date of the settlement.
	 * 
	 * @return the arrival date.
	 */
	public MarsClock getArrivalDate() {
		return arrivalDate;
	}

	/**
	 * Sets the arrival date of the settlement.
	 * 
	 * @param arrivalDate the arrival date.
	 */
	public void setArrivalDate(MarsClock arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	/**
	 * Gets the landing location for the arriving settlement.
	 * 
	 * @return landing location coordinates.
	 */
	public Coordinates getLandingLocation() {
		return landingLocation;
	}

	/**
	 * Sets the landing location for the arriving settlement.
	 * 
	 * @param landingLocation the landing location coordinates.
	 */
	public void setLandingLocation(Coordinates landingLocation) {
		this.landingLocation = landingLocation;
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

	/**
	 * Commits a set of modifications for the arriving settlement.
	 */
	public void commitModification() {
		HistoricalEvent newEvent = new TransportEvent(this, EventType.TRANSPORT_ITEM_MODIFIED,
				"Arriving settlement mission modded", landingLocation.toString());
		Simulation.instance().getEventManager().registerNewEvent(newEvent);
	}

	@Override
	public String getSettlementName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(getName());
		buff.append(": ");
		buff.append(getArrivalDate().getDateString());
		return buff.toString();
	}

	@Override
	public int compareTo(Transportable o) {
		int result = 0;

		double arrivalTimeDiff = MarsClock.getTimeDiff(arrivalDate, o.getArrivalDate());
		if (arrivalTimeDiff < 0D) {
			result = -1;
		} else if (arrivalTimeDiff > 0D) {
			result = 1;
		} else {
			// If arrival time is the same, compare by settlement name alphabetically.
			result = name.compareTo(o.getName());
		}

		return result;
	}

	@Override
	public synchronized void performArrival() {
		Simulation sim = Simulation.instance();
		UnitManager unitManager = sim.getUnitManager();
		
		SettlementBuilder build = new SettlementBuilder(sim,
												SimulationConfig.instance());
		
		InitialSettlement spec = new InitialSettlement(name, sponsor,
													   template, populationNum, numOfRobots,
													   landingLocation);
		Settlement newSettlement = build.createFullSettlement(spec);
		
		// Sim is already running so add to the active queue
		unitManager.activateSettlement(newSettlement);
	}

	@Override
	public void destroy() {
		name = null;
		template = null;
		transitState = null;
		launchDate = null;
		arrivalDate = null;
		landingLocation = null;
	}

}
