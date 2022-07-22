/*
 * Mars Simulation Project
 * VehicleAirlock.java
 * @date 2021-09-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.AirlockType;
import org.mars_sim.msp.core.time.ClockPulse;

/**
 * This class represents an airlock for a vehicle.
 */
public class VehicleAirlock
extends Airlock {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(VehicleAirlock.class.getName());

	/** Pressurize/depressurize time (millisols). */
	public static final double CYCLE_TIME = 5D; 
	/** The maximum number of space in the chamber. */
	public static final int MAX_SLOTS = 2;
	
	// Data members.
	/** True if airlock's state is in transition of change. */
	private boolean transitioning;
	/** True if airlock is activated (may elect an operator or may change the airlock state). */
	private boolean activated;
	/** Amount of remaining time for the airlock cycle. (in millisols) */
	private double remainingCycleTime;
	
	/** The vehicle this airlock is for. */
	private Vehicle vehicle;
	
	private LocalPosition airlockInsidePos;
	private LocalPosition airlockInteriorPos;
	private LocalPosition airlockExteriorPos;

    private Map<LocalPosition, Integer> airlockInsidePosMap;
    private Map<LocalPosition, Integer> airlockInteriorPosMap;
    private Map<LocalPosition, Integer> airlockExteriorPosMap;

	/**
	 * Constructor.
	 * @param vehicle the vehicle this airlock of for.
	 * @param capacity number of people airlock can hold.
	 */
	public VehicleAirlock(
		Vehicle vehicle, int capacity, LocalPosition loc, LocalPosition interiorLoc,
		LocalPosition exteriorLoc) {
		// User Airlock constructor
		super(capacity);

		if (vehicle == null) {
			throw new IllegalArgumentException(Msg.getString("VehicleAirlock.error.null")); //$NON-NLS-1$
		}
		else if (!(vehicle instanceof Crewable)) {
			throw new IllegalArgumentException(Msg.getString("VehicleAirlock.error.notCrewable")); //$NON-NLS-1$
		}
		else if (!(vehicle instanceof LifeSupportInterface)) {
			throw new IllegalArgumentException(Msg.getString("VehicleAirlock.error.noLifeSupport")); //$NON-NLS-1$
		}
		else {
			this.vehicle = vehicle;
		}

		activated = false;
		remainingCycleTime = CYCLE_TIME;
		
		// Determine airlock interior position.
		airlockInteriorPos = LocalAreaUtil.getLocalRelativePosition(interiorLoc, vehicle);
		airlockInteriorPosMap = buildDoorMap(interiorLoc, vehicle, 0.3, 0.6, 0.5);
		
		// Determine airlock exterior position.
		airlockExteriorPos = LocalAreaUtil.getLocalRelativePosition(exteriorLoc, vehicle);
		airlockExteriorPosMap = buildDoorMap(exteriorLoc, vehicle, 0.3, 0.6, 0.5);

		// Determine airlock inside position.
		airlockInsidePos = LocalAreaUtil.getLocalRelativePosition(loc, vehicle);
		airlockInsidePosMap = new HashMap<>();
	}

    /**
     * Builds a map for the door positions. Creates four positioned around the center offset by the x and y values.
     * 
     * @param center Position of the center of the positions.
     * @param building Building hosting the door
     * @param x1 First x value
     * @param x2 Second x value
     * @param y y values; uses +/- of this.
     * @return
     */
    private static Map<LocalPosition, Integer> buildDoorMap(LocalPosition center, Vehicle vehicle,
			double x1, double x2, double y) {
        Map<LocalPosition, Integer> result = new HashMap<>();

        result.put(LocalAreaUtil.getLocalRelativePosition(new LocalPosition(center.getX() + x1, center.getY() + y), vehicle), -1);
        result.put(LocalAreaUtil.getLocalRelativePosition(new LocalPosition(center.getX() + x1, center.getY() - y), vehicle), -1);

        return result;
	}
    
    @Override
    protected boolean egress(Person person) {
        return stepOnMars(person);
    }

    @Override
    protected boolean ingress(Person person) {
        return stepInside(person);
    }

    /**
     * Steps back into the airlock of a vehicle
     *
     * @param person
     */
    public boolean stepInside(Person person) {
    	boolean successful = false;
    	if (person.isOutside()) {
            // 1.1. Transfer a person from the surface of Mars to the vehicle
    		successful = person.transfer(vehicle);

			if (successful)
				logger.log(person, Level.FINER, 0,
					"Just stepped inside rover " + vehicle.getName() + ".");
			else
				logger.log(person, Level.SEVERE, 0,
					"Could not step inside rover " + vehicle.getName() + ".");

		}

		else if (person.isInSettlement()) {
			logger.log(person, Level.SEVERE, 0,
					Msg.getString("VehicleAirlock.error.notOutside", person.getName(), getEntityName()) + ".");
		}

		return successful;
    }

    /**
     * Goes outside of the airlock and step into the surface of Mars
     *
     * @param person
     */
    public boolean stepOnMars(Person person) {
    	boolean successful = false;
		if (person.isInVehicle()) {

            // 5.1. Transfer a person from the vehicle to the surface of Mars
			successful = person.transfer(marsSurface);

			if (successful) {
				// 5.2 Set the person's coordinates to that of the settlement's
				person.setCoordinates(vehicle.getCoordinates());

				logger.log(person, Level.FINER, 0,
					"Just stepped outside rover " + vehicle.getName() + ".");
			}
			else
				logger.log(person, Level.SEVERE, 0,
					"Could not step outside rover " + vehicle.getName() + ".");
		}
		else if (person.isOutside()) {
			logger.log(person, Level.SEVERE, 0,
					Msg.getString("VehicleAirlock.error.notInside", person.getName(), getEntityName()) + ".");
		}

		return successful;
    }

	/**
	 * Gets the name of the entity this airlock is attached to.
	 *
	 * @return name {@link String}
	 */
	public String getEntityName() {
		return vehicle.getName();
	}

	@Override
	public Object getEntity() {
		return vehicle;
	}

	@Override
	public LocalPosition getAvailableInteriorPosition(boolean pos) {
		// Note: the param pos is not needed
		return getAvailableInteriorPosition();
	}

	@Override
	public LocalPosition getAvailableExteriorPosition(boolean pos) {
		// Note: the param pos is not needed
		return getAvailableExteriorPosition();
	}

	@Override
	public LocalPosition getAvailableInteriorPosition() {
		for (Entry<LocalPosition, Integer> i : airlockInteriorPosMap.entrySet()) {
			if (i.getValue() == -1) {
				return i.getKey();
			}
		}
		
		return null;
	}

	@Override
	public LocalPosition getAvailableExteriorPosition() {
		for (Entry<LocalPosition, Integer> i : airlockExteriorPosMap.entrySet()) {
			if (i.getValue() == -1) {
				return i.getKey();
			}
		}
		
		return null;
	}

	@Override
	public LocalPosition getAvailableAirlockPosition() {
    	for (Entry<LocalPosition, Integer> i : airlockInsidePosMap.entrySet()) {
			if (i.getValue() == -1) {
				return i.getKey();
			}
		}
    	
    	return null;
//		return airlockInsidePos;
	}

	/**
	 * Gets the person having this particular id
	 *
	 * @param id
	 * @return
	 */
	public Person getAssociatedPerson(int id) {
		return vehicle.getAssociatedSettlement().getAssociatedPerson(id);
	}

	/**
     * Gets a set of occupants from a particular zone
     *
     * @param zone the zone of interest
	 * @return a set of occupants in the zone of the interest
     */
	@Override
    public Set<Integer> getZoneOccupants(int zone) {
		if (zone == 0)
			return getAwaitingInnerDoor();
		else if (zone == 4)
			return getAwaitingOuterDoor();
		else if (zone == 1 || zone == 3)
			return new HashSet<>();

		// if in zone 2
		return getOccupants();
    }

    /**
     * Gets the exact number of occupants who are within the chamber
     * 
     * @return
     */
    public int getNumInChamber() {
    	return getNumOccupants();
    }
    
	/**
	 * Gets the number of occupants currently inside the airlock zone 1, 2, and 3
	 *
	 * @return the number of occupants
	 */
	@Override
	public int getNumOccupants() {
		return getOccupants().size();
	}
    
	/**
	 * Gets a collection of occupants' ids
	 *
	 * @return
	 */
	@Override
	public Set<Integer> getAllInsideOccupants() {
		return getOccupants();
	}

	/**
	 * Checks if the chamber is full
	 *
	 * @return
	 */
	@Override
	public boolean isChamberFull() {
		return getOccupants().size() >= MAX_SLOTS;
	}

	/**
	 * Gets the type of airlock
	 *
	 * @return AirlockType
	 */
	@Override
	public AirlockType getAirlockType() {
		return AirlockType.VEHICLE_AIRLOCK;
	}

    /**
     * Vacate the person from a particular zone
     *
     * @param zone the zone of interest
     * @param id the person's id
	 * @return true if the person has been successfully vacated
     */
	@Override
	public boolean vacate(int zone, Integer id) {
	 	if (zone == 0) {
    		LocalPosition oldPos = getOldPos(airlockInteriorPosMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<2; i++) {
//    			LocalPosition pp = outsideInteriorList.get(i);
    			if (airlockInteriorPosMap.get(oldPos).equals(id)) {
    				airlockInteriorPosMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}

	   	else if (zone == 1 || zone == 3) {
				return true;
	    	}

    	else if (zone == 2) {
    		LocalPosition oldPos = getOldPos(airlockInsidePosMap, id);
    		if (oldPos == null)
    			return false;
			if (airlockInsidePosMap.get(oldPos).equals(id)) {
				airlockInsidePosMap.put(oldPos, -1);
				return true;
			}
//    		return true;
    	}

    	else if (zone == 4) {
    		LocalPosition oldPos = getOldPos(airlockExteriorPosMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<2; i++) {
//    			LocalPosition pp = outsideExteriorList.get(i);
    			if (airlockExteriorPosMap.get(oldPos).equals(id)) {
    				airlockExteriorPosMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}
    	return false;
    }
	
	private <K, V> K getOldPos(Map<K, V> map, V value) {
	    for (Entry<K, V> entry : map.entrySet()) {
	        if (entry.getValue().equals(value)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	/**
     * Check if the person is in a particular zone
     *
     * @param p the person
     * @param zone the zone of interest
	 * @return a list of occupants inside the chamber
     */
	@Override
	public boolean isInZone(Person p, int zone) {
	   	if (zone == 0) {
    		LocalPosition p0 = getOldPos(airlockInteriorPosMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(LocalPosition pt : airlockInteriorPosMap.keySet()) {
    			if (p0.isClose(pt)) {
    				return true;
    			}
    		}
    	}

    	else if (zone == 1 || zone == 3) {
    		return false;
    	}

    	else if (zone == 2) {
//    		loadEVAActivitySpots();
    		LocalPosition p0 = getOldPos(airlockInsidePosMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(LocalPosition pt : airlockInsidePosMap.keySet()) {
    			if (p0.isClose(pt)) {
    				return true;
    			}
    		}
    	}


    	else if (zone == 4) {
    		LocalPosition p0 = getOldPos(airlockExteriorPosMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(LocalPosition pt : airlockExteriorPosMap.keySet()) {
    			if (p0.isClose(pt)) {
    				return true;
    			}
    		}
    	}

		return false;
		
//		Set<Integer> set = getZoneOccupants(zone);
//		if (set.contains(p.getIdentifier())) {
//			return true;
//		}
//
//		return false;
	}
	
	 /**
     * Occupies a position in a zone.
     * 
     * @param zone
     * @param p
     * @param id
     */
    @Override
    public boolean occupy(int zone, LocalPosition p, Integer id) {
    	if (zone == 0) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (airlockInteriorPosMap.values().contains(id))
    			return false;

    		// If someone is at that position, do not allow to occupy it
    		if (airlockInteriorPosMap.get(p) != -1)
    			return false;
    		
    		airlockInteriorPosMap.put(p, id);
    		return true;
    	}
    		
    	else if (zone == 1 || zone == 3) {
			return false;
    	}
    	
    	else if (zone == 2) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (airlockInsidePosMap.values().contains(id))
    			return false;

    		// If someone is at that position, do not allow to occupy it
    		if (airlockInsidePosMap.get(p) != -1)
    			return false;
    		
    		airlockInsidePosMap.put(p, id);
    		return true;
    	}
    	
    	else if (zone == 4) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (airlockExteriorPosMap.values().contains(id))
    			return false;

    		// If someone is at that position, do not allow to occupy it
    		if (airlockExteriorPosMap.get(p) != -1)
    			return false;
    		
    		airlockExteriorPosMap.put(p, id);
    		return true;
    	}
    	
		return false;
   	}
    	
	/**
	 * Loads up and converts the native EVA activity spots
	 */
	@Override
	public void loadEVAActivitySpots() {
		// nothing
	}
	
	/**
	 * Activates the airlock.
	 * 
	 * @param value
	 */
	@Override
	public void setActivated(boolean value) {
		if (value) {
			// Reset the cycle count down timer back to the default
			remainingCycleTime = CYCLE_TIME;
		}
		activated = value;
	}
	
	/**
	 * Cycles the air and consumes the time
	 * 
	 * @param time
	 */
	@Override
	protected void cycleAir(double time) {
		// Ensure not to consume more than is needed
		double consumed = Math.min(remainingCycleTime, time);

		remainingCycleTime -= consumed;
		// if the air cycling has been completed
		if (remainingCycleTime <= 0D) {
			// Reset remainingCycleTime back to max
			remainingCycleTime = CYCLE_TIME;
			// Go to the next steady state
			goToNextSteadyState();			
		}
	}
	
	/**
	 * Checks if the airlock is currently activated.
	 *
	 * @return true if activated.
	 */
	@Override
	public boolean isActivated() {
		return activated;
	}

	/**
	 * Allows or disallows the airlock to be transitioning its state.
	 *
	 * @param value
	 */
	public void setTransitioning(boolean value) {
		transitioning = value;
	}

	/**
	 * Checks if the airlock is allowed to be transitioning its state.
	 *
	 * @param value
	 */
	public boolean isTransitioning() {
		return transitioning;
	}
	
	/**
	 * Gets the remaining airlock cycle time.
	 *
	 * @return time (millisols)
	 */
	public double getRemainingCycleTime() {
		return remainingCycleTime;
	}
	
	/**
	 * Time passing for airlock. Checks for unusual situations and deal with them.
	 * Called from the unit owning the airlock.
	 *
	 * @param pulse
	 */
	@Override
	public void timePassing(ClockPulse pulse) {
		
		if (activated) {

			double time = pulse.getElapsed();
			
			if (transitioning) {
				// Starts the air exchange and state transition
				addTime(time);
			}

			if (pulse.isNewMSol()) {
				// Check occupants
				checkOccupantIDs();
				// Check the airlock operator
				checkOperator();
			}
		}
	}
	
	/**
	 * Adds this unit to the set or zone (for zone 0 and zone 4 only).
	 *
	 * @param set
	 * @param id
	 * @return true if the unit is already inside the set or if the unit can be added into the set
	 */
	@Override
	protected boolean addToZone(Set<Integer> set, Integer id) {
		if (set.contains(id)) {
			// this unit is already in the zone
			return true;
		}
		else {
			// MAX_SLOTS - 1 because it needs to have one vacant spot
			// for the flow of traffic
			if (set.size() < MAX_SLOTS - 1) {
				set.add(id);
				return true;
			}
		}
		return false;
	}
	
	public void destroy() {
	    vehicle = null;
	    airlockInsidePos = null;
	    airlockInteriorPos = null;
	    airlockExteriorPos = null;
	}
}
