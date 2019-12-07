/**
 * Mars Simulation Project
 * TravelMission.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * A mission that involves traveling along a series of navpoints. TODO
 * externalize strings
 */
public abstract class TravelMission extends Mission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(TravelMission.class.getName());
//	private static String loggerName = logger.getName();
//	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	// Travel Mission status
	public final static String AT_NAVPOINT = "At a navpoint";
	public final static String TRAVEL_TO_NAVPOINT = "Traveling to navpoint";

	// Data members
	/** List of navpoints for the mission. */
	private List<NavPoint> navPoints = new ArrayList<NavPoint>();
	/** The current navpoint index. */
	private int navIndex = 0;
	
	/** The total distance travelled so far. */
	private double proposedRouteTotalDistance = 0;
	/** The current leg remaining distance so far. */
	private double currentLegRemainingDistance;
	/** The total remaining distance so far. */
	private double totalRemainingDistance;
	
	/** The current traveling status of the mission. */
	private String travelStatus;
	
	/** The last navpoint the mission stopped at. */
	private NavPoint lastStopNavpoint;
	/** The time the last leg of the mission started at. */
	private MarsClock legStartingTime;

	
	/**
	 * Constructor 1
	 * 
	 * @param missionName
	 * @param startingMember
	 * @param minPeople
	 */
	protected TravelMission(String missionName, MissionType missionType, MissionMember startingMember, int minPeople) {
		// Use Mission constructor.
		super(missionName, missionType, startingMember, minPeople);

		NavPoint startingNavPoint = null;
		Coordinates c = getCurrentMissionLocation();

		if (c != null) {
			if (startingMember.getSettlement() != null) {
				startingNavPoint = new NavPoint(c, startingMember.getSettlement(),
						startingMember.getSettlement().getName());
			} else {
				startingNavPoint = new NavPoint(c, "starting location");
			}
		}

		if (startingNavPoint != null) {
			addNavpoint(startingNavPoint);
			lastStopNavpoint = startingNavPoint;

			setTravelStatus(AT_NAVPOINT);
		}
		
		logger.info(getStartingMember() + " had put together the navpoints for the " + missionName);
	}

	/**
	 * Adds a navpoint to the mission.
	 * 
	 * @param navPoint the new nav point location to be added.
	 * @throws IllegalArgumentException if location is null.
	 */
	public final void addNavpoint(NavPoint navPoint) {
		if (navPoint != null) {
			navPoints.add(navPoint);
			fireMissionUpdate(MissionEventType.NAVPOINTS_EVENT);
		} else {
			LogConsolidated.log(Level.SEVERE, 10_000, logger.getName(), "navPoint is null");
			// throw new IllegalArgumentException("navPoint is null");
		}
	}

	/**
	 * Sets a nav point for the mission.
	 * 
	 * @param index    the index in the list of nav points.
	 * @param navPoint the new navpoint
	 * @throws IllegalArgumentException if location is null or index < 0.
	 */
	protected final void setNavpoint(int index, NavPoint navPoint) {
		if ((navPoint != null) && (index >= 0)) {
			navPoints.set(index, navPoint);
			fireMissionUpdate(MissionEventType.NAVPOINTS_EVENT);
		} else {
			LogConsolidated.log(Level.SEVERE, 10_000, logger.getName(), "navPoint is null");
			// throw new IllegalArgumentException("navPoint is null");
		}
	}

	/**
	 * Clears out any unreached nav points.
	 */
	public final void clearRemainingNavpoints() {
		int index = getNextNavpointIndex();
		int numNavpoints = getNumberOfNavpoints();
		for (int x = index; x < numNavpoints; x++) {
			navPoints.remove(index);
			fireMissionUpdate(MissionEventType.NAVPOINTS_EVENT);
		}
	}

	/**
	 * Gets the last navpoint reached.
	 * 
	 * @return navpoint
	 */
	public final NavPoint getPreviousNavpoint() {
		return lastStopNavpoint;
	}

	/**
	 * Gets the mission's next navpoint.
	 * 
	 * @return navpoint or null if no more navpoints.
	 */
	public final NavPoint getNextNavpoint() {
		if (navIndex < navPoints.size())
			return navPoints.get(navIndex);
		else
			return null;
	}

	/**
	 * Gets the mission's next navpoint index.
	 * 
	 * @return navpoint index or -1 if none.
	 */
	public final int getNextNavpointIndex() {
		if (navIndex < navPoints.size())
			return navIndex;
		else
			return -1;
	}

	/**
	 * Set the next navpoint index.
	 * 
	 * @param newNavIndex the next navpoint index.
	 * @throws MissionException if the new navpoint is out of range.
	 */
	public final void setNextNavpointIndex(int newNavIndex) {
		if (newNavIndex < getNumberOfNavpoints()) {
			navIndex = newNavIndex;
		} else
			LogConsolidated.log(Level.SEVERE, 0, logger.getName(),
					getPhase() + "'s newNavIndex " + newNavIndex + " is out of bounds.");
		// throw new IllegalStateException(getPhase() + " : newNavIndex: "
		// + newNavIndex + " is outOfBounds.");
	}

	/**
	 * Gets the navpoint at an index value.
	 * 
	 * @param index the index value
	 * @return navpoint
	 * @throws IllegaArgumentException if no navpoint at that index.
	 */
	public final NavPoint getNavpoint(int index) {
		if ((index >= 0) && (index < getNumberOfNavpoints()))
			return navPoints.get(index);
		else {
//			LogConsolidated.log(Level.SEVERE, 0, logger.getName(),
//					// getPhase() + " index " + index + " out of bounds."
//					this.getDescription() 
////					+ " at " + getPhase() 
//					+ "  Index is " + index + ". # of navpoints is " + getNumberOfNavpoints());
			// throw new IllegalArgumentException("index: " + index
			// + " out of bounds.");

			return null;
		}
	}

	/**
	 * Gets the index of a navpoint.
	 * 
	 * @param navpoint the navpoint
	 * @return index or -1 if navpoint isn't in the trip.
	 */
	public final int getNavpointIndex(NavPoint navpoint) {
		if (navpoint == null)
			throw new IllegalArgumentException("navpoint is null");
		if (navPoints.contains(navpoint))
			return navPoints.indexOf(navpoint);
		else
			return -1;
	}

	/**
	 * Gets the number of navpoints on the trip.
	 * 
	 * @return number of navpoints
	 */
	public final int getNumberOfNavpoints() {
		return navPoints.size();
	}

	/**
	 * Gets the current navpoint the mission is stopped at.
	 * 
	 * @return navpoint or null if mission is not stopped at a navpoint.
	 */
	public final NavPoint getCurrentNavpoint() {
		if (travelStatus != null && AT_NAVPOINT.equals(travelStatus)) {
			if (navIndex < navPoints.size())
				return navPoints.get(navIndex);
			else
				return null;
		} else
			return null;
	}

	/**
	 * Gets the index of the current navpoint the mission is stopped at.
	 * 
	 * @return index of current navpoint or -1 if mission is not stopped at a
	 *         navpoint.
	 */
	public final int getCurrentNavpointIndex() {
		if (travelStatus != null && AT_NAVPOINT.equals(travelStatus))
			return navIndex;
		else
			return -1;
	}

	/**
	 * Get the travel mission's current status.
	 * 
	 * @return travel status as a String.
	 */
	public final String getTravelStatus() {
		return travelStatus;
	}

	/**
	 * Set the travel mission's current status.
	 * 
	 * @param newTravelStatus the mission travel status.
	 */
	private void setTravelStatus(String newTravelStatus) {
		travelStatus = newTravelStatus;
		fireMissionUpdate(MissionEventType.TRAVEL_STATUS_EVENT);
	}

	/**
	 * Starts travel to the next navpoint in the mission.
	 * 
	 * @throws MissionException if no more navpoints.
	 */
	protected final void startTravelToNextNode() {
		setNextNavpointIndex(navIndex + 1);
		setTravelStatus(TRAVEL_TO_NAVPOINT);
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();
		legStartingTime = (MarsClock) marsClock.clone();
	}

	/**
	 * The mission has reached the next navpoint.
	 * 
	 * @throws MisisonException if error determining mission location.
	 */
	protected final void reachedNextNode() {
		setTravelStatus(AT_NAVPOINT);
		lastStopNavpoint = getCurrentNavpoint();
	}

	/**
	 * Performs the travel phase of the mission.
	 * 
	 * @param member the mission member currently performing the mission.
	 */
	protected abstract void performTravelPhase(MissionMember member);
//    protected abstract void performTravelPhase(Robot robot);

	/**
	 * Gets the starting time of the current leg of the mission.
	 * 
	 * @return starting time
	 */
	protected final MarsClock getCurrentLegStartingTime() {
		if (legStartingTime != null) {
			return (MarsClock) legStartingTime.clone();
		} else {
			throw new IllegalArgumentException("legStartingTime is null");
		}
	}

	/**
	 * Gets the distance of the current leg of the mission, or 0 if not in the
	 * travelling phase.
	 * 
	 * @return distance (km)
	 */
	public final double getCurrentLegDistance() {
		if (travelStatus != null && TRAVEL_TO_NAVPOINT.equals(travelStatus) && lastStopNavpoint != null
				&& getNextNavpoint() != null) {
			return lastStopNavpoint.getLocation().getDistance(getNextNavpoint().getLocation());
		} else {
			return 0D;
		}
	}

	/**
	 * Gets the remaining distance for the current leg of the mission.
	 * 
	 * @return distance (km) or 0 if not in the travelling phase.
	 * @throws MissionException if error determining distance.
	 */
	public final double getCurrentLegRemainingDistance() {
		if (travelStatus != null && TRAVEL_TO_NAVPOINT.equals(travelStatus)) {

			if (getNextNavpoint() == null) {
				int offset = 2;
				if (getPhase().equals(VehicleMission.TRAVELLING))
					offset = 1;
				setNextNavpointIndex(getNumberOfNavpoints() - offset);
				updateTravelDestination();
			}
			
			Coordinates c1 = null;
			
			if (getNextNavpoint() != null) {
				c1 = getNextNavpoint().getLocation();
			}
			else if (this instanceof TravelToSettlement) {
				c1 = ((TravelToSettlement)this).getDestinationSettlement().getCoordinates();	
			}
			
			double dist = Coordinates.computeDistance(getCurrentMissionLocation(), c1);
			
			if (currentLegRemainingDistance != dist) {
				currentLegRemainingDistance = dist;
				fireMissionUpdate(MissionEventType.DISTANCE_EVENT);
			}
			
//			System.out.println("   c0 : " + c0 + "   c1 : " + c1 + "   dist : " + dist);
			return dist;
		}

		else
			return 0D;
	}

	/**
	 * Computes the proposed route total distance of the trip.
	 * 
	 * @return distance (km)
	 */
	public final void computeProposedRouteTotalDistance() {
		if (proposedRouteTotalDistance == 0) {
			if (navPoints.size() > 1) {
				double result = 0D;
				
				for (int x = 1; x < navPoints.size(); x++) {
					NavPoint prevNav = navPoints.get(x - 1);
					NavPoint currNav = navPoints.get(x);
					double distance = Coordinates.computeDistance(currNav.getLocation(), prevNav.getLocation());
					result += distance;
				}
				
				if (result > proposedRouteTotalDistance) {
					// Record the distance
					proposedRouteTotalDistance = result;
					fireMissionUpdate(MissionEventType.DISTANCE_EVENT);	
				}
			}
		}
	}

	/**
	 * Gets the proposed route total distance of the trip.
	 * 
	 * @return distance (km)
	 */
	public final double getProposedRouteTotalDistance() {
		return proposedRouteTotalDistance;
	}
	
	/**
	 * Gets the total remaining distance to travel in the mission.
	 * 
	 * @return distance (km).
	 * @throws MissionException if error determining distance.
	 */
	public final double getTotalRemainingDistance() {
		// TODO: check for Double.isInfinite() and Double.isNaN()
		double leg = getCurrentLegRemainingDistance();
//		if (getVehicle().getName().equalsIgnoreCase("Opportunity"))
//			System.out.print("leg : " + leg);//Math.round(leg*10.0)/10.0);
		int index = 0;
		double navDist = 0;
		if (AT_NAVPOINT.equals(travelStatus))
			index = getCurrentNavpointIndex();
		else if (TRAVEL_TO_NAVPOINT.equals(travelStatus))
			index = getNextNavpointIndex();

		for (int x = index + 1; x < getNumberOfNavpoints(); x++) {
			navDist += Coordinates.computeDistance(getNavpoint(x - 1).getLocation(), getNavpoint(x).getLocation());
//			if (getVehicle().getName().equalsIgnoreCase("Opportunity")) {
//				System.out.print("     index = " + index + "     x = " + x);
//				System.out.println("     Nav Distance from " + (x-1) + " to " + x + " : " + Math.round(navDist*10.0)/10.0);
//			}
		}
//		if (getVehicle().getName().equalsIgnoreCase("Opportunity")) {
//			System.out.print("    Nav : " + navDist);//Math.round(navDist*10.0)/10.0);
//			System.out.println("    Total : " + (leg + navDist));//Math.round((leg + navDist)*10.0)/10.0);
//		}
		double total = leg + navDist;
		
		if (total > totalRemainingDistance) {
			totalRemainingDistance = total;
			fireMissionUpdate(MissionEventType.DISTANCE_EVENT);	
		}
			
		return total;
	}

	/**
	 * Gets the actual total distance travelled during the mission so far.
	 * 
	 * @return distance (km)
	 */
	public abstract double getActualTotalDistanceTravelled();

	/**
	 * Gets the estimated time of arrival (ETA) for the current leg of the mission.
	 * 
	 * @return time (MarsClock) or null if not applicable.
	 */
	public abstract MarsClock getLegETA();

	/**
	 * Gets the estimated time remaining for the mission.
	 * 
	 * @param useBuffer use a time buffer in estimation if true.
	 * @return time (millisols)
	 * @throws MissionException
	 */
	public abstract double getEstimatedRemainingMissionTime(boolean useBuffer);

	/**
	 * Gets the estimated time for a trip.
	 * 
	 * @param useBuffer use time buffers in estimation if true.
	 * @param distance  the distance of the trip.
	 * @return time (millisols)
	 * @throws MissionException
	 */
	public abstract double getEstimatedTripTime(boolean useBuffer, double distance);

	/**
	 * Update mission to the next navpoint destination.
	 */
	public abstract void updateTravelDestination();

	@Override
	public void endMission() {
		super.endMission();
	}

	@Override
	public void destroy() {
		super.destroy();

		if (navPoints != null)
			navPoints.clear();
		navPoints = null;
		travelStatus = null;
		lastStopNavpoint = null;
		legStartingTime = null;
	}
}