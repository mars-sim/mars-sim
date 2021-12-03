/*
 * Mars Simulation Project
 * BuildingAirlock.java
 * @date 2021-09-25
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.awt.geom.Point2D;
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

/**
 * The BuildingAirlock class represents an airlock for a building.
 */
public class BuildingAirlock extends Airlock {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private static SimLogger logger = SimLogger.getLogger(BuildingAirlock.class.getName());

	public static final double HEIGHT = 2; // assume an uniform height of 2 meters in all airlocks

	/** The volume of an airlock in cubic meters. */
	public static final double AIRLOCK_VOLUME_IN_CM = 12D; //3 * 2 * 2; //in m^3

    // Data members.
	/** The building this airlock is for. */
    private Building building;

    private Point2D airlockInsidePos;
    private Point2D airlockInteriorPos;
    private Point2D airlockExteriorPos;

    private List<Point2D> EVASpots;

    private Map<Point2D, Integer> outsideInteriorDoorMap;
    private Map<Point2D, Integer> outsideExteriorDoorMap;

    private Map<Point2D, Integer> insideInteriorDoorMap;
    private Map<Point2D, Integer> insideExteriorDoorMap;

    private Map<Point2D, Integer> activitySpotMap;

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

        activitySpotMap  = new HashMap<>();

        // Determine airlock interior position.
        airlockInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorPos.getX(), interiorPos.getY(), building);
        insideInteriorDoorMap = buildDoorMap(interiorPos, building, 0.3, 0.6, 0.5);
        outsideInteriorDoorMap = buildDoorMap(interiorPos, building, -0.3, -0.6, 0.5);

        // Determine airlock exterior position.
        airlockExteriorPos = LocalAreaUtil.getLocalRelativeLocation(exteriorPos.getX(), exteriorPos.getY(), building);
        insideInteriorDoorMap = buildDoorMap(exteriorPos, building, -0.5, -1.0, 0.5);
        outsideExteriorDoorMap = buildDoorMap(exteriorPos, building, 0.5, 1.0, 0.5);

        // Determine airlock inside position.
        airlockInsidePos = LocalAreaUtil.getLocalRelativeLocation(position.getX(), position.getY(), building);
    }

    /**
     * Builds a map for the door positions. Creates four positioned around the center offset by the x and y values.
     * @param center Position of the center of the positions.
     * @param building Building hosting the door
     * @param x1 First x value
     * @param x2 Second x value
     * @param y y values; uses +/- of this.
     * @return
     */
    private static Map<Point2D, Integer> buildDoorMap(LocalPosition center, Building building,
			double x1, double x2, double y) {
        Map<Point2D, Integer> result = new HashMap<>();

        result.put(LocalAreaUtil.getLocalRelativeLocation(center.getX() + x1, center.getY() + y, building), -1);
        result.put(LocalAreaUtil.getLocalRelativeLocation(center.getX() + x1, center.getY() - y, building), -1);
        result.put(LocalAreaUtil.getLocalRelativeLocation(center.getX()+ x2, center.getY() + y, building), -1);
        result.put(LocalAreaUtil.getLocalRelativeLocation(center.getX() + x2, center.getY() - y, building), -1);

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
			settlement.getCompositionOfAir().releaseOrRecaptureAir(building.getInhabitableID(), true, building);

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
  			settlement.getCompositionOfAir().releaseOrRecaptureAir(building.getInhabitableID(), false, building);

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
        return building.getNickName();
    }

    @Override
    public Object getEntity() {
        return building;
    }

    @Override
    public Point2D getAvailableInteriorPosition() {
        return airlockInteriorPos;
    }

    @Override
    public Point2D getAvailableInteriorPosition(boolean inside) {
    	if (inside) {
    		for(Entry<Point2D, Integer> i : insideInteriorDoorMap.entrySet()) {
    			if (i.getValue() == -1) {
    				return i.getKey();
    			}
    		}
    	}

    	else {
    		for( Entry<Point2D, Integer> i : outsideInteriorDoorMap.entrySet()) {
    			if (i.getValue() == -1) {
    				return i.getKey();
    			}
    		}
    	}

        return null;
    }

    @Override
    public Point2D getAvailableExteriorPosition() {
        return airlockExteriorPos;
    }

    @Override
    public Point2D getAvailableExteriorPosition(boolean inside) {
    	if (inside) {
    		for(Entry<Point2D, Integer> i : insideExteriorDoorMap.entrySet()) {
    			if (i.getValue() == -1) {
    				return i.getKey();
    			}
    		}
    	}

    	else {
    		for( Entry<Point2D, Integer> i : outsideExteriorDoorMap.entrySet()) {
    			if (i.getValue() == -1) {
    				return i.getKey();
    			}
    		}
    	}
        return null;
    }

//    @Override
    public boolean occupy(int zone, Point2D p, Integer id) {
    	if (zone == 0) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (outsideInteriorDoorMap.values().contains(id))
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = outsideInteriorList.get(i);
//    			if (pp == p) {
    				outsideInteriorDoorMap.put(p, id);
    				return true;
//    			}
//    		}
    	}

    	else if (zone == 1) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (insideInteriorDoorMap.values().contains(id))
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = insideInteriorList.get(i);
//    			if (pp == p) {
    				insideInteriorDoorMap.put(p, id);
    				return true;
//    			}
//    		}

    	}
    	else if (zone == 2) {
    		if (activitySpotMap.values().contains(id))
    			return false;
    		activitySpotMap.put(p, id);
    			return true;
    	}

    	else if (zone == 3) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (insideExteriorDoorMap.values().contains(id))
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = insideExteriorList.get(i);
//    			if (pp == p) {
    				insideExteriorDoorMap.put(p, id);
    				return true;
//    			}
//    		}
    	}

    	else if (zone == 4) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (outsideExteriorDoorMap.values().contains(id))
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = outsideExteriorList.get(i);
//    			if (pp == p) {
    				outsideExteriorDoorMap.put(p, id);
    				return true;
//    			}
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
     * Vacate the person from a particular zone
     *
     * @param zone the zone of interest
     * @param id the person's id
	 * @return true if the person has been successfully vacated
     */
    public boolean vacate(int zone, Integer id) {
    	if (zone == 0) {
    		Point2D oldPos = getOldPos(outsideInteriorDoorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = outsideInteriorList.get(i);
    			if (outsideInteriorDoorMap.get(oldPos).equals(id)) {
    				outsideInteriorDoorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}

    	else if (zone == 1) {
    		Point2D oldPos = getOldPos(insideInteriorDoorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = insideInteriorList.get(i);
    			if (insideInteriorDoorMap.get(oldPos).equals(id)) {
    				insideInteriorDoorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}

    	else if (zone == 2) {
    		Point2D oldPos = getOldPos(activitySpotMap, id);
    		if (oldPos == null)
    			return false;
			if (activitySpotMap.get(oldPos).equals(id)) {
				activitySpotMap.put(oldPos, -1);
				return true;
			}
//    		return true;
    	}

    	else if (zone == 3) {
    		Point2D oldPos = getOldPos(insideExteriorDoorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = insideExteriorList.get(i);
    			if (insideExteriorDoorMap.get(oldPos).equals(id)) {
    				insideExteriorDoorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}

    	else if (zone == 4) {
    		Point2D oldPos = getOldPos(outsideExteriorDoorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = outsideExteriorList.get(i);
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
	public boolean isInZone(Person p, int zone) {
    	if (zone == 0) {
    		Point2D p0 = getOldPos(outsideInteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(Point2D pt : outsideInteriorDoorMap.keySet()) {
    			if (LocalAreaUtil.areLocationsClose(p0, pt)) {
    				return true;
    			}
    		}
    	}

    	else if (zone == 1) {
    		Point2D p0 = getOldPos(insideInteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(Point2D pt : insideInteriorDoorMap.keySet()) {
    			if (LocalAreaUtil.areLocationsClose(p0, pt)) {
    				return true;
    			}
    		}
    	}

    	else if (zone == 2) {
    		loadEVAActivitySpots();
    		Point2D p0 = getOldPos(activitySpotMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(Point2D pt : EVASpots) {
    			if (LocalAreaUtil.areLocationsClose(p0, pt)) {
    				return true;
    			}
    		}
    	}

    	else if (zone == 3) {
    		Point2D p0 = getOldPos(insideExteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(Point2D pt : insideExteriorDoorMap.keySet()) {
    			if (LocalAreaUtil.areLocationsClose(p0, pt)) {
    				return true;
    			}
    		}
    	}

    	else if (zone == 4) {
    		Point2D p0 = getOldPos(outsideExteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for(Point2D pt : outsideExteriorDoorMap.keySet()) {
    			if (LocalAreaUtil.areLocationsClose(p0, pt)) {
    				return true;
    			}
    		}
    	}

		return false;
	}

	/**
	 * Gets the total number of people occupying the chamber
	 *
	 * @return a list of occupants inside the chamber
	 */
	public int getInsideChamberNum() {
		int result = 0;
		loadEVAActivitySpots();
		for (Point2D p : activitySpotMap.keySet()) {
			if (!activitySpotMap.get(p).equals(-1))
				result++;
		}
		return result;
	}

	/**
	 * Gets the total number of people occupying the area between the inner and outer door (namely, zone 1, 2, and 3)
	 *
	 * @return a list of occupants
	 */
	public int getInsideTotalNum() {
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
	 * Gets a set of ids of the occupants inside
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
	public void loadEVAActivitySpots() {
		if (EVASpots == null) {
			EVASpots = new ArrayList<>();
			for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D p0 = building.getEVA().getActivitySpotsList().get(i);
    			Point2D p1 = LocalAreaUtil.getLocalRelativeLocation(p0.getX(), p0.getY(), building);
    			EVASpots.add(p1);
    			activitySpotMap.put(p1, -1);
    		}
		}
	}

    @Override
    public Point2D getAvailableAirlockPosition() {
        return airlockInsidePos;
    }

	public boolean removePosition(int zone, Point2D p, int id) {
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

//		if (zone == 2) {
//			return true;
//		}

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
	 * Gets the number of occupants currently inside the airlock zone 1, 2, and 3
	 *
	 * @return the number of occupants
	 */
	@Override
	public int getNumOccupants() {
		return getInsideTotalNum();
	}

	/**
	 * Checks if the chamber is full
	 *
	 * @return
	 */
	@Override
	public boolean isChamberFull() {
		return getInsideChamberNum() >= MAX_SLOTS;
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
