/**
 * Mars Simulation Project
 * TravelMission.java
 * @version 2.78 2005-08-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.ArrayList;
import java.util.List;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.time.MarsClock;

/**
 * A mission that involves traveling along a series of navpoints.
 */
public abstract class TravelMission extends Mission {

	// Travel Mission status
	public final static String AT_NAVPOINT = "At a navpoint";
	public final static String TRAVEL_TO_NAVPOINT = "Traveling to navpoint";
	
	// Data members
	private List navPoints = new ArrayList();  // List of navpoints for the mission.
	private int navIndex = 0; // The current navpoint index.
	private String travelStatus; // The current traveling status of the mission. 
	private NavPoint lastStopNavpoint; // The last navpoint the mission stopped at.
	private MarsClock legStartingTime;  // The time the last leg of the mission started at. 
	private boolean emergencyTravelHome; // True if mission needs to travel home next.

	/**
	 * Constructor
	 * (note: The constructor handles setting the initial nav point.)
	 * @param name the name of the mission.
	 * @param startingPerson the person starting the mission.
	 * @param minPeople the minimum number of people required for mission.
	 * @throws MissionException if error constructing mission.
	 */
	protected TravelMission(String name, Person startingPerson, int minPeople) throws MissionException {
		// Use Mission constructor.
		super(name, startingPerson, minPeople);
		
		NavPoint startingNavPoint = null;
		if (startingPerson.getSettlement() != null) 
			startingNavPoint = new NavPoint(startingPerson.getCoordinates(), startingPerson.getSettlement());
		else startingNavPoint = new NavPoint(startingPerson.getCoordinates());
		addNavpoint(startingNavPoint);
		lastStopNavpoint = startingNavPoint;
		setTravelStatus(AT_NAVPOINT);
	}
	
	/**
	 * Adds a navpoint to the mission.
	 * @param navPoint the new nav point location to be added.
	 * @throws IllegalArgumentException if location is null.
	 */
	protected void addNavpoint(NavPoint navPoint) {
		if (navPoint != null) navPoints.add(navPoint);
		else throw new IllegalArgumentException("navPoint is null");
	}
	
	/**
	 * Sets a nav point for the mission.
	 * @param index the index in the list of nav points.
	 * @param navPoint the new navpoint
	 * @throws IllegalArgumentException if location is null or index < 0.
	 */
	protected void setNavpoint(int index, NavPoint navPoint) {
		if ((navPoint != null) && (index >= 0)) navPoints.set(index, navPoint);
		else throw new IllegalArgumentException("navPoint is null");
	}
	
	/**
	 * Gets the last navpoint reached.
	 * @return navpoint
	 */
	public NavPoint getPreviousNavpoint() {
		return lastStopNavpoint;
	}
	
	/**
	 * Gets the mission's next navpoint.
	 * @return navpoint or null if no more navpoints.
	 */
	public NavPoint getNextNavpoint() {
		if (navIndex < navPoints.size()) return (NavPoint) navPoints.get(navIndex);
		else return null;
	}
	
	/**
	 * Set the next navpoint index.
	 * @param newNavIndex the next navpoint index.
	 * @throws MissionException if the new navpoint is out of range.
	 */
	protected void setNextNavpointIndex(int newNavIndex) throws MissionException {
		if (newNavIndex < getNumberOfNavpoints()) {
			navIndex = newNavIndex;
		}
		else throw new MissionException(getPhase(), "newNavIndex: " + newNavIndex + " is outOfBounds.");
	}
	
	/**
	 * Gets the navpoint at an index value.
	 * @param index the index value
	 * @return navpoint
	 * @throws IllegaArgumentException if no navpoint at that index.
	 */
	public NavPoint getNavpoint(int index) {
		if ((index >= 0) && (index < getNumberOfNavpoints())) return (NavPoint) navPoints.get(index);
		else throw new IllegalArgumentException("index: " + index + " out of bounds.");
	}
	
	/**
	 * Gets the number of navpoints on the trip.
	 * @return number of navpoints
	 */
	public int getNumberOfNavpoints() {
		return navPoints.size();
	}
	
	/**
	 * Gets the current navpoint the mission is stopped at.
	 * @return navpoint or null if mission is not stopped at a navpoint.
	 */
	public NavPoint getCurrentNavpoint() {
		if (AT_NAVPOINT.equals(getTravelStatus())) {
			if (navIndex < navPoints.size()) return (NavPoint) navPoints.get(navIndex);
			else return null;
		}
		else return null;
	}
	
	/**
	 * Gets the index of the current navpoint the mission is stopped at.
	 * @return index of current navpoint or -1 if mission is not stopped at a navpoint.
	 */
	public int getCurrentNavpointIndex() {
		if (AT_NAVPOINT.equals(getTravelStatus())) return navIndex;
		else return -1;
	}
	
	/**
	 * Get the travel mission's current status.
	 * @return travel status as a String.
	 */
	public String getTravelStatus() {
		return travelStatus;
	}
	
	/**
	 * Set the travel mission's current status.
	 * @param newTravelStatus the mission travel status.
	 */
	private void setTravelStatus(String newTravelStatus) {
		travelStatus = newTravelStatus;
	}
	
	/**
	 * Starts travel to the next navpoint in the mission.
	 * @param person the person performing the mission
	 * @throws MissionException if no more navpoints.
	 */
	protected void startTravelToNextNode(Person person) throws MissionException {
		
		// If emergency, set to last navpoint.
		if (getEmergencyTravelHome()) setNextNavpointIndex(navPoints.size() - 1);
		else setNextNavpointIndex(navIndex + 1);
		
		setTravelStatus(TRAVEL_TO_NAVPOINT);
		legStartingTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
	}
	
	/**
	 * The mission has reached the next navpoint.
	 * @person the person performing the mission
	 */
	protected void reachedNextNode(Person person) {
		setTravelStatus(AT_NAVPOINT);
		lastStopNavpoint = new NavPoint(person.getCoordinates());
	}
	
	/**
	 * Performs the travel phase of the mission.
	 * @param person the person currently performing the mission.
	 * @throws MissionException if error performing travel phase.
	 */
	protected abstract void performTravelPhase(Person person) throws MissionException;
	
	/**
	 * Gets the starting time of the current leg of the mission.
	 * @return starting time
	 */
	protected MarsClock getCurrentLegStartingTime() {
		if (legStartingTime != null) return (MarsClock) legStartingTime.clone();
		else return null;
	}
	
	/**
	 * Gets the distance of the current leg of the mission, or 0 if not in the travelling phase.
	 * @return distance (km) 
	 */
	public double getCurrentLegDistance() {
		if (TRAVEL_TO_NAVPOINT.equals(travelStatus)) {
			NavPoint prev = getPreviousNavpoint();
			NavPoint next = getNextNavpoint();
			return getPreviousNavpoint().getLocation().getDistance(getNextNavpoint().getLocation());
		}
		else return 0D;
	}
	
	/**
	 * Gets the remaining distance for the current leg of the mission.
	 * @return distance (km) or 0 if not in the travelling phase.
	 */
	public abstract double getCurrentLegRemainingDistance();
	
	/**
	 * Gets the total distance of the trip.
	 * @return total distance (km)
	 */
	public double getTotalDistance() {
		double result = 0D;
		if (navPoints.size() > 1) {
			for (int x = 1; x < navPoints.size(); x++) {
				NavPoint prevNav = (NavPoint) navPoints.get(x - 1);
				NavPoint currNav = (NavPoint) navPoints.get(x);
				double distance = currNav.getLocation().getDistance(prevNav.getLocation());
				result += distance;
			}
		}
		return result;
	}
	
    /**
     * Gets the estimated time of arrival (ETA) for the current leg of the mission.
     * @return time (MarsClock) or null if not applicable.
     */
    public abstract MarsClock getLegETA();
    
    /**
     * Sets the mission emergency travel home mode
     * @param emergencyTravelHome true if emergency home mode.
     */
    public void setEmergencyTravelHome(boolean emergencyTravelHome) {
    	this.emergencyTravelHome = emergencyTravelHome;
    }
    
    /**
     * Checks if the mission is in emergency travel home mode.
     * @return true if emergency travel home mode.
     */
    public boolean getEmergencyTravelHome() {
    	return emergencyTravelHome;
    }
}