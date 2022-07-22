/*
 * Mars Simulation Project
 * BuildingAirlock.java
 * @date 2021-09-25
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.AirlockType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.ClockPulse;

/**
 * The BuildingAirlock class represents an airlock for a building.
 */
public class BuildingAirlock extends Airlock {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private static SimLogger logger = SimLogger.getLogger(BuildingAirlock.class.getName());

	/** Pressurize/depressurize time (millisols). */
	public static final double CYCLE_TIME = 10D; 
	/** The maximum number of space in the chamber. */
	public static final int MAX_SLOTS = 4;
	/** Assume an uniform height of 2 meters in all airlocks. */
	public static final double HEIGHT = 2; 
	/** The volume of an airlock [in cubic meters]. */
	public static final double AIRLOCK_VOLUME_IN_CM = 12D; //3 * 2 * 2;
	/** The volume of an airlock [in liter]. */	
	private static final double AIRLOCK_VOLUME_IN_LITER = AIRLOCK_VOLUME_IN_CM * 1000D; // 12 m^3

    // Data members.
	/** True if airlock's state is in transition of change. */
	private boolean transitioning;
	/** True if airlock is activated (may elect an operator or may change the airlock state). */
	private boolean activated;
	/** Amount of remaining time for the airlock cycle. (in millisols) */
	private double remainingCycleTime;
	
	/** The building this airlock is for. */
    private Building building;

    private LocalPosition airlockInsidePos;
    private LocalPosition airlockInteriorPos;
    private LocalPosition airlockExteriorPos;

    private List<LocalPosition> EVASpots;

    private Map<LocalPosition, Integer> outsideInteriorDoorMap;
    private Map<LocalPosition, Integer> outsideExteriorDoorMap;

    private Map<LocalPosition, Integer> insideInteriorDoorMap;
    private Map<LocalPosition, Integer> insideExteriorDoorMap;

    private Map<LocalPosition, Integer> activitySpotMap;

    /**
     * Constructor
     * @param building the building this airlock of for.
     * @param capacity number of people airlock can hold.
     */
    public BuildingAirlock(Building building, int capacity, LocalPosition position,
    		LocalPosition interiorPos, LocalPosition exteriorPos) {
        // User Airlock constructor
        super(capacity);

        this.building = building;
        
		activated = false;
		remainingCycleTime = CYCLE_TIME;
		
        activitySpotMap  = new HashMap<>();

        // Determine airlock inner/interior door position.
        airlockInteriorPos = LocalAreaUtil.getLocalRelativePosition(interiorPos, building);
        insideInteriorDoorMap = buildDoorMap(interiorPos, building, 0.3, 0.6, 0.5);
        outsideInteriorDoorMap = buildDoorMap(interiorPos, building, -0.3, -0.6, 0.5);

        // Determine airlock outer/exterior door position.
        airlockExteriorPos = LocalAreaUtil.getLocalRelativePosition(exteriorPos, building);
        insideExteriorDoorMap = buildDoorMap(exteriorPos, building, -0.5, -1.0, 0.5);
        outsideExteriorDoorMap = buildDoorMap(exteriorPos, building, 0.5, 1.0, 0.5);

        // Determine airlock inside position.
        airlockInsidePos = LocalAreaUtil.getLocalRelativePosition(position, building);
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
    private static Map<LocalPosition, Integer> buildDoorMap(LocalPosition center, Building building,
			double x1, double x2, double y) {
        Map<LocalPosition, Integer> result = new HashMap<>();

        result.put(LocalAreaUtil.getLocalRelativePosition(new LocalPosition(center.getX() + x1, center.getY() + y), building), -1);
        result.put(LocalAreaUtil.getLocalRelativePosition(new LocalPosition(center.getX() + x1, center.getY() - y), building), -1);
        result.put(LocalAreaUtil.getLocalRelativePosition(new LocalPosition(center.getX()+ x2, center.getY() + y), building), -1);
        result.put(LocalAreaUtil.getLocalRelativePosition(new LocalPosition(center.getX() + x2, center.getY() - y), building), -1);

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
     * Steps inside of a settlement
     *
     * @param person
     */
    public boolean stepInside(Person person) {
    	boolean successful = false;

        if (person.isOutside()) {

			Settlement settlement = building.getSettlement();

            // 1.0. Pump air into the airlock to make it breathable
			building.getLifeSupport().getAir().releaseOrRecaptureAir(AIRLOCK_VOLUME_IN_LITER, true, building);

            // 1.1. Transfer a person from the surface of Mars to the building inventory
			successful = person.transfer(settlement);

			if (successful) {
	            // 1.2 Add the person to the building
	            BuildingManager.addPersonOrRobotToBuilding(person, building);

	   			logger.log(person, Level.FINER, 0,
		  				"Stepped inside "
	        			+ settlement.getName() + ".");

				// 1.3 Set the person's coordinates to that of the settlement's
				person.setCoordinates(settlement.getCoordinates());
			}

			else
				logger.log(person, Level.SEVERE, 0,
						"Could not step inside "
						+ settlement.getName() + ".");
        }

        else if (!person.isBuried() || !person.isDeclaredDead()) {

        	logger.log(person, Level.SEVERE, 0,
        			"Could not step inside " + getEntityName() + ".");
        }

    	return successful;
    }

    /**
     * Gets outside of the airlock and step into the surface of Mars
     *
     * @param person
     */
    public boolean stepOnMars(Person person) {
    	boolean successful = false;
    	logger.log(person, Level.FINER, 0,
    			"Just stepped onto the surface of Mars.");

    	if (person.isInSettlement()) {

			Settlement settlement = building.getSettlement();

            // Upon depressurization, there is heat loss to the Martian air in Heating class
  			building.getThermalGeneration().getHeating().flagHeatLostViaAirlockOuterDoor(true);

            // 5.0. Recapture air from the airlock before depressurizing it
			building.getLifeSupport().getAir().releaseOrRecaptureAir(AIRLOCK_VOLUME_IN_LITER, false, building);

            // 5.1. Transfer a person from the building to the surface of Mars to the vehicle
            successful = person.transfer(marsSurface);

			if (successful) {
				// 5.3. Set the person's coordinates to that of the settlement's
				person.setCoordinates(settlement.getCoordinates());

				logger.log(person, Level.FINER, 0,
        			"Left "
        			+ settlement
        			+ " and stepped outside.");
			}
			else
				logger.log(person, Level.SEVERE, 0,
					"Could not step outside " + settlement.getName() + ".");
        }

        else if (!person.isBuried() || !person.isDeclaredDead()) {

        	logger.log(person, Level.SEVERE, 0,
        			"Could not step outside " + getEntityName() + ".");
        }

    	return successful;
    }

    @Override
    public String getEntityName() {
        return building.getName();
    }

    @Override
    public Object getEntity() {
        return building;
    }

    @Override
    public LocalPosition getAvailableInteriorPosition() {
        return airlockInteriorPos;
    }

    @Override
    public LocalPosition getAvailableInteriorPosition(boolean inside) {
    	if (inside) {
    		for (Entry<LocalPosition, Integer> i : insideInteriorDoorMap.entrySet()) {
    			if (i.getValue() == -1) {
    				return i.getKey();
    			}
    		}
    	}

    	else {
    		for (Entry<LocalPosition, Integer> i : outsideInteriorDoorMap.entrySet()) {
    			if (i.getValue() == -1) {
    				return i.getKey();
    			}
    		}
    	}

        return null;
    }

    @Override
    public LocalPosition getAvailableExteriorPosition() {
        return airlockExteriorPos;
    }

    @Override
    public LocalPosition getAvailableExteriorPosition(boolean inside) {
    	if (inside) {
    		for(Entry<LocalPosition, Integer> i : insideExteriorDoorMap.entrySet()) {
    			if (i.getValue() == -1) {
    				return i.getKey();
    			}
    		}
    	}

    	else {
    		for( Entry<LocalPosition, Integer> i : outsideExteriorDoorMap.entrySet()) {
    			if (i.getValue() == -1) {
    				return i.getKey();
    			}
    		}
    	}
        return null;
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
    		if (outsideInteriorDoorMap.values().contains(id))
    			return false;
    		
    		// If someone is at that position, do not allow to occupy it
    		if (outsideInteriorDoorMap.get(p) != -1)
    			return false;

    		outsideInteriorDoorMap.put(p, id);
    		return true;
    	}

    	else if (zone == 1) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (insideInteriorDoorMap.values().contains(id))
    			return false;

    		// If someone is at that position, do not allow to occupy it
    		if (insideInteriorDoorMap.get(p) != -1)
    			return false;
    		
    		insideInteriorDoorMap.put(p, id);
    		return true;
    	}
    	
    	else if (zone == 2) {
    		if (activitySpotMap.values().contains(id))
    			return false;
    		
    		// If someone is at that position, do not allow to occupy it
    		if (activitySpotMap.get(p) != -1)
    			return false;
    		
    		activitySpotMap.put(p, id);
    			return true;
    	}

    	else if (zone == 3) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (insideExteriorDoorMap.values().contains(id))
    			return false;

    		// If someone is at that position, do not allow to occupy it
    		if (insideExteriorDoorMap.get(p) != -1)
    			return false;
    		
    		insideExteriorDoorMap.put(p, id);
    		return true;

    	}

    	else if (zone == 4) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (outsideExteriorDoorMap.values().contains(id))
    			return false;

    		// If someone is at that position, do not allow to occupy it
    		if (outsideExteriorDoorMap.get(p) != -1)
    			return false;
    		
    		outsideExteriorDoorMap.put(p, id);
    		return true;

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
     * Vacates the person from a particular zone.
     *
     * @param zone the zone of interest
     * @param id the person's id
	 * @return true if the person has been successfully vacated
     */
	@Override
    public boolean vacate(int zone, Integer id) {
    	if (zone == 0) {
    		LocalPosition oldPos = getOldPos(outsideInteriorDoorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			LocalPosition pp = outsideInteriorList.get(i);
    			if (outsideInteriorDoorMap.get(oldPos).equals(id)) {
    				outsideInteriorDoorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}

    	else if (zone == 1) {
    		LocalPosition oldPos = getOldPos(insideInteriorDoorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			LocalPosition pp = insideInteriorList.get(i);
    			if (insideInteriorDoorMap.get(oldPos).equals(id)) {
    				insideInteriorDoorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}

    	else if (zone == 2) {
    		LocalPosition oldPos = getOldPos(activitySpotMap, id);
    		if (oldPos == null)
    			return false;
			if (activitySpotMap.get(oldPos).equals(id)) {
				activitySpotMap.put(oldPos, -1);
				return true;
			}
//    		return true;
    	}

    	else if (zone == 3) {
    		LocalPosition oldPos = getOldPos(insideExteriorDoorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			LocalPosition pp = insideExteriorList.get(i);
    			if (insideExteriorDoorMap.get(oldPos).equals(id)) {
    				insideExteriorDoorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}

    	else if (zone == 4) {
    		LocalPosition oldPos = getOldPos(outsideExteriorDoorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			LocalPosition pp = outsideExteriorList.get(i);
    			if (outsideExteriorDoorMap.get(oldPos).equals(id)) {
    				outsideExteriorDoorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}
    	return false;
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
    		LocalPosition p0 = getOldPos(outsideInteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(LocalPosition pt : outsideInteriorDoorMap.keySet()) {
    			if (p0.isClose(pt)) {
    				return true;
    			}
    		}
    	}

    	else if (zone == 1) {
    		LocalPosition p0 = getOldPos(insideInteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(LocalPosition pt : insideInteriorDoorMap.keySet()) {
    			if (p0.isClose(pt)) {
    				return true;
    			}
    		}
    	}

    	else if (zone == 2) {
    		loadEVAActivitySpots();
    		LocalPosition p0 = getOldPos(activitySpotMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(LocalPosition pt : EVASpots) {
    			if (p0.isClose(pt)) {
    				return true;
    			}
    		}
    	}

    	else if (zone == 3) {
    		LocalPosition p0 = getOldPos(insideExteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(LocalPosition pt : insideExteriorDoorMap.keySet()) {
    			if (p0.isClose(pt)) {
    				return true;
    			}
    		}
    	}

    	else if (zone == 4) {
    		LocalPosition p0 = getOldPos(outsideExteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(LocalPosition pt : outsideExteriorDoorMap.keySet()) {
    			if (p0.isClose(pt)) {
    				return true;
    			}
    		}
    	}

		return false;
	}

	/**
	 * Gets the total number of people occupying the chamber in zone 2
	 *
	 * @return a list of occupants inside zone 2
	 */
	public int getInsideChamberNum() {
		int result = 0;
		loadEVAActivitySpots();
		for (LocalPosition p : activitySpotMap.keySet()) {
			if (!activitySpotMap.get(p).equals(-1))
				result++;
		}
		return result;
	}

	/**
	 * Gets the total number of people occupying zone 1, 2, and 3,
	 * excluding zone 0 and 4.
	 * 
	 * @return number of occupants
	 */
	public int getNumOccupants() {
		int result = 0;
		for (Integer p : insideExteriorDoorMap.values()) {
			if (!p.equals(-1))
				result++;
		}
		loadEVAActivitySpots();
		for (Integer p : activitySpotMap.values()) {
			if (!p.equals(-1))
				result++;
		}
		for (Integer p : insideInteriorDoorMap.values()) {
			if (!p.equals(-1))
				result++;
		}
		return result;
	}

	/**
	 * Gets a set of ids of the occupants inside zone 1, 2, 3
	 *
	 * @return a set of ids
	 */
	@Override
	public Set<Integer> getAllInsideOccupants() {
		Set<Integer> list = new HashSet<>();
		for (Integer p : insideExteriorDoorMap.values()) {
			if (!p.equals(-1))
				list.add(p);
		}
		loadEVAActivitySpots();
		for (Integer p : activitySpotMap.values()) {
			if (!p.equals(-1))
				list.add(p);
		}
		for (Integer p : insideInteriorDoorMap.values()) {
			if (!p.equals(-1))
				list.add(p);
		}
		return list;
	}

	/**
     * Gets a set of occupants from a particular zone
     *
     * @param zone the zone of interest
	 * @return a set of occupants in the zone of the interest
     */
	@Override
    public Set<Integer> getZoneOccupants(int zone) {
    	Set<Integer> list = new HashSet<>();
    	if (zone == 0) {
    		for (int i: outsideInteriorDoorMap.values()) {
    			if (i != -1) {
    				list.add(i);
    			}
    		}
    	}

    	else if (zone == 1) {
    		for (int i: insideInteriorDoorMap.values()) {
    			if (i != -1) {
    				list.add(i);
    			}
    		}
    	}

    	else if (zone == 2) {
    		for (int i: activitySpotMap.values()) {
    			if (i != -1) {
    				list.add(i);
    			}
    		}
    	}

    	else if (zone == 3) {
    		for (int i: insideExteriorDoorMap.values()) {
    			if (i != -1) {
    				list.add(i);
    			}
    		}
    	}

    	else if (zone == 4) {
    		for (int i: outsideExteriorDoorMap.values()) {
    			if (i != -1) {
    				list.add(i);
    			}
    		}
    	}
    	return list;
    }

	/**
	 * Gets the number of people occupying a zone
	 *
	 * @param zone
	 * @return
	 */
	public int getInZoneNum(int zone) {
		Collection<Integer> occupants = null;
    	if (zone == 0) {
    		occupants = outsideInteriorDoorMap.values();
		}

    	else if (zone == 1) {
			occupants = insideInteriorDoorMap.values();
    	}

    	else if (zone == 2) {
    		loadEVAActivitySpots();
			occupants = activitySpotMap.values();
    	}

    	else if (zone == 3) {
    		occupants = insideExteriorDoorMap.values();
		}

    	else if (zone == 4) {
    		occupants = outsideExteriorDoorMap.values();
    	}

    	int result = 0;
    	
    	// Count them up
    	if (occupants != null) {
			for (Integer p : occupants) {
				if (!p.equals(-1))
					result++;
			}
    	}
		return result;
	}


	/**
	 * Loads up and converts the native EVA activity spots into the settlement coordinates
	 */
	@Override
	public void loadEVAActivitySpots() {
		if (EVASpots == null) {
			EVASpots = new ArrayList<>();
			for (int i=0; i<MAX_SLOTS; i++) {
    			LocalPosition p0 = building.getEVA().getActivitySpotsList().get(i);
    			LocalPosition p1 = LocalAreaUtil.getLocalRelativePosition(p0, building);
    			EVASpots.add(p1);
    			activitySpotMap.put(p1, -1);
    		}
		}
	}


    /**
     * Gets the exact number of occupants who are within the chamber
     * 
     * @return
     */
    public int getNumInChamber() {
    	loadEVAActivitySpots();
    	return getInsideChamberNum();
    }
    
    @Override
    public LocalPosition getAvailableAirlockPosition() {
    	for (Entry<LocalPosition, Integer> i : activitySpotMap.entrySet()) {
			if (i.getValue() == -1) {
				return i.getKey();
			}
		}
    	
    	return null;
//        return airlockInsidePos;
    }

	public boolean removePosition(int zone, LocalPosition p, int id) {
		if (zone == 0 && outsideInteriorDoorMap.containsKey(p)) {
			outsideInteriorDoorMap.put(p, -1);
			return true;
		}

		if (zone == 1 && insideInteriorDoorMap.containsKey(p)) {
			insideInteriorDoorMap.put(p, -1);
			return true;
		}

		if (zone == 2 && activitySpotMap.containsKey(p)) {
			activitySpotMap.put(p, -1);
			return true;
		}

		if (zone == 3 && insideExteriorDoorMap.containsKey(p)) {
			insideExteriorDoorMap.put(p, -1);
			return true;
		}

		if (zone == 4 && outsideExteriorDoorMap.containsKey(p)) {
			outsideExteriorDoorMap.put(p, -1);
			return true;
		}
		return false;
	}

	/**
	 * Checks if the chamber is full
	 *
	 * @return
	 */
	@Override
	public boolean isChamberFull() {
		return getNumOccupants() >= MAX_SLOTS;
	}

	/**
	 * Gets the type of airlock
	 *
	 * @return AirlockType
	 */
	@Override
	public AirlockType getAirlockType() {
		return AirlockType.BUILDING_AIRLOCK;
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
	    building = null;
	    airlockInsidePos = null;
	    airlockInteriorPos = null;
	    airlockExteriorPos = null;

	    activitySpotMap.clear();

	    outsideInteriorDoorMap.clear();
	    outsideExteriorDoorMap.clear();

	    insideInteriorDoorMap.clear();
	    insideExteriorDoorMap.clear();

	    activitySpotMap = null;

	    outsideInteriorDoorMap = null;
	    outsideExteriorDoorMap = null;

	    insideInteriorDoorMap = null;
	    insideExteriorDoorMap = null;
	}

}
