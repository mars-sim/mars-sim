/**
 * Mars Simulation Project
 * BuildingAirlock.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Airlock;
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
    
    private List<Point2D> outsideInteriorDoorList;
    private List<Point2D> insideInteriorDoorList;

    private List<Point2D> insideExteriorDoorList;
    private List<Point2D> outsideExteriorDoorList;
    
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
    public BuildingAirlock(Building building, int capacity, double xLoc, double yLoc,
            double interiorXLoc, double interiorYLoc, double exteriorXLoc, double exteriorYLoc) {
        // User Airlock constructor
        super(capacity);

        this.building = building;
        
        activitySpotMap  = new HashMap<>();
        
        // Determine airlock interior position.
        airlockInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc, interiorYLoc, building);
        Point2D insideInteriorDoor0 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc + 0.3, interiorYLoc + 0.5, building);
        Point2D insideInteriorDoor1 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc + 0.3, interiorYLoc - 0.5, building);
        Point2D insideInteriorDoor2 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc + 0.6, interiorYLoc + 0.5, building);
        Point2D insideInteriorDoor3 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc + 0.6, interiorYLoc - 0.5, building);
        insideInteriorDoorList = new ArrayList<>();
        insideInteriorDoorList.add(insideInteriorDoor0);
        insideInteriorDoorList.add(insideInteriorDoor1);
        insideInteriorDoorList.add(insideInteriorDoor2);
        insideInteriorDoorList.add(insideInteriorDoor3);
        insideInteriorDoorMap = new HashMap<>();
        for (Point2D p : insideInteriorDoorList) {
        	insideInteriorDoorMap.put(p, -1);
        }
       
        Point2D outsideInteriorDoor0 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc - 0.3, interiorYLoc + 0.5, building);
        Point2D outsideInteriorDoor1 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc - 0.3, interiorYLoc - 0.5, building);       
        Point2D outsideInteriorDoor2 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc - 0.6, interiorYLoc + 0.5, building);       
        Point2D outsideInteriorDoor3 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc - 0.6, interiorYLoc - 0.5, building);
        outsideInteriorDoorList = new ArrayList<>();
        outsideInteriorDoorList.add(outsideInteriorDoor0);
        outsideInteriorDoorList.add(outsideInteriorDoor1);
        outsideInteriorDoorList.add(outsideInteriorDoor2);
        outsideInteriorDoorList.add(outsideInteriorDoor3);
        outsideInteriorDoorMap = new HashMap<>();
        for (Point2D p : outsideInteriorDoorList) {
        	outsideInteriorDoorMap.put(p, -1);
        }
      
        // Determine airlock exterior position.
        airlockExteriorPos = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc, exteriorYLoc, building);
        Point2D insideExteriorDoor0 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc - 0.5, exteriorYLoc + 0.5, building);
        Point2D insideExteriorDoor1 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc - 0.5, exteriorYLoc - 0.5, building);       
        Point2D insideExteriorDoor2 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc - 1.0, exteriorYLoc + 0.5, building);       
        Point2D insideExteriorDoor3 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc - 1.0, exteriorYLoc - 0.5, building);
        insideExteriorDoorList = new ArrayList<>();
        insideExteriorDoorList.add(insideExteriorDoor0);
        insideExteriorDoorList.add(insideExteriorDoor1);
        insideExteriorDoorList.add(insideExteriorDoor2);
        insideExteriorDoorList.add(insideExteriorDoor3);
        insideExteriorDoorMap = new HashMap<>();
        for (Point2D p : insideExteriorDoorList) {
        	insideExteriorDoorMap.put(p, -1);
        }
       
        Point2D outsideExteriorDoor0 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc + 0.5, exteriorYLoc + 0.5, building);
        Point2D outsideExteriorDoor1 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc + 0.5, exteriorYLoc - 0.5, building);       
        Point2D outsideExteriorDoor2 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc + 1.0, exteriorYLoc + 0.5, building);       
        Point2D outsideExteriorDoor3 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc + 1.0, exteriorYLoc - 0.5, building);
        outsideExteriorDoorList = new ArrayList<>();
        outsideExteriorDoorList.add(outsideExteriorDoor0);
        outsideExteriorDoorList.add(outsideExteriorDoor1);
        outsideExteriorDoorList.add(outsideExteriorDoor2);
        outsideExteriorDoorList.add(outsideExteriorDoor3);
        outsideExteriorDoorMap = new HashMap<>();
        for (Point2D p : outsideExteriorDoorList) {
        	outsideExteriorDoorMap.put(p, -1);
        }
      
        // Determine airlock inside position.
        airlockInsidePos = LocalAreaUtil.getLocalRelativeLocation(xLoc, yLoc, building);
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
			successful = person.transfer(marsSurface, settlement);
            
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
            successful = person.transfer(settlement, marsSurface);
            
			if (successful) {
				// 5.2 Remove the person from the building
	            BuildingManager.removePersonFromBuilding(person, building);
	         
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
    public Inventory getEntityInventory() {
        return building.getInventory();
    }

    @Override
    public Object getEntity() {
        return building;
    }

    @Override
    public String getLocale() {
        return building.getLocale();
    }
    
    @Override
    public Point2D getAvailableInteriorPosition() {
        return airlockInteriorPos;
    }

    @Override
    public Point2D getAvailableInteriorPosition(boolean inside) {
    	if (inside) {
    		for (int i=0; i<4; i++) {
    			Point2D p = insideInteriorDoorList.get(i);
    			if (insideInteriorDoorMap.get(p) == -1)
    				return p;
    		}
    	}
    	
    	else {
    		for (int i=0; i<4; i++) {
    			Point2D p = outsideInteriorDoorList.get(i);
    			if (outsideInteriorDoorMap.get(p) == -1)
    				return p;
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
    		for (int i=0; i<4; i++) {
    			Point2D p = insideExteriorDoorList.get(i);
    			if (insideExteriorDoorMap.get(p) == -1)
    				return p;
    		}
    	}
    	
    	else {
    		for (int i=0; i<4; i++) {
    			Point2D p = outsideExteriorDoorList.get(i);
    			if (outsideExteriorDoorMap.get(p) == -1)
    				return p;
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
    
	public <K, V> K getOldPos(Map<K, V> map, V value) {
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
     * @param p the person's id
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
    		for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D pt = outsideInteriorDoorList.get(i);
    			if (LocalAreaUtil.areLocationsClose(p0, pt)) {
    				return true;
    			}
    		}
    	}
    	
    	else if (zone == 1) {
    		Point2D p0 = getOldPos(insideInteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D pt = insideInteriorDoorList.get(i);
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
    		for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D pt = EVASpots.get(i);
    			if (LocalAreaUtil.areLocationsClose(p0, pt)) {
    				return true;
    			}
    		}
    	}
    	
    	else if (zone == 3) {
    		Point2D p0 = getOldPos(insideExteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D pp = insideExteriorDoorList.get(i);
    			if (LocalAreaUtil.areLocationsClose(p0, pp)) {
    				return true;
    			}
    		}
    	}
    	
    	else if (zone == 4) {
    		Point2D p0 = getOldPos(outsideExteriorDoorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D pp = outsideExteriorDoorList.get(i);
    			if (LocalAreaUtil.areLocationsClose(p0, pp)) {
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
		for (Point2D p : insideExteriorDoorList) {
			if (!insideExteriorDoorMap.get(p).equals(-1))
				result++;		
		}
		loadEVAActivitySpots();
		for (Point2D p : activitySpotMap.keySet()) {
			if (!activitySpotMap.get(p).equals(-1))
				result++;
		}
		for (Point2D p : insideInteriorDoorList) {
			if (!insideInteriorDoorMap.get(p).equals(-1))
				result++;
		}
		return result;
	}

	/**
	 * Gets a list of ids of the occupants inside
	 * 
	 * @return a list of ids
	 */
	public List<Integer> getAllInsideOccupants() {
		List<Integer> list = new ArrayList<>();
		for (Point2D p : insideExteriorDoorList) {
			if (!insideExteriorDoorMap.get(p).equals(-1))
				list.add(insideExteriorDoorMap.get(p));		
		}
		loadEVAActivitySpots();
		for (Point2D p : activitySpotMap.keySet()) {
			if (!activitySpotMap.get(p).equals(-1))
				list.add(activitySpotMap.get(p));	
		}
		for (Point2D p : insideInteriorDoorList) {
			if (!insideInteriorDoorMap.get(p).equals(-1))
				list.add(insideInteriorDoorMap.get(p));	
		}
		return list;
	}
	
	/**
     * Gets a set of occupants from a particular zone
     * 
     * @param zone the zone of interest
	 * @return a set of occupants in the zone of the interest
     */
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
		int result = 0;
    	if (zone == 0) {
			for (Point2D p : outsideInteriorDoorList) {
				if (!outsideInteriorDoorMap.get(p).equals(-1))
					result++;
			}
		}
			
    	else if (zone == 1) {
			for (Point2D p : insideInteriorDoorList) {
				if (!insideInteriorDoorMap.get(p).equals(-1))
					result++;
			}
    	}
    	
    	else if (zone == 2) {
    		loadEVAActivitySpots();
			for (Point2D p : activitySpotMap.keySet()) {
				if (!activitySpotMap.get(p).equals(-1))
					result++;
			}
    	}
    	
    	else if (zone == 3) {
			for (Point2D p : insideExteriorDoorList) {
				if (!insideExteriorDoorMap.get(p).equals(-1))
					result++;
			}
		}
			
    	else if (zone == 4) {
			for (Point2D p : outsideExteriorDoorList) {
				if (!outsideExteriorDoorMap.get(p).equals(-1))
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
	
	
	public void destroy() {
	    building = null;
	    airlockInsidePos = null;
	    airlockInteriorPos = null;
	    airlockExteriorPos = null;
	    
	    activitySpotMap.clear();
	    
	    outsideInteriorDoorList.clear();
	    insideInteriorDoorList.clear();

	    insideExteriorDoorList.clear();
	    outsideExteriorDoorList.clear();

	    outsideInteriorDoorMap.clear();
	    outsideExteriorDoorMap.clear();
	    
	    insideInteriorDoorMap.clear();
	    insideExteriorDoorMap.clear();
	    
	    activitySpotMap = null;
	    
	    outsideInteriorDoorList = null;
	    insideInteriorDoorList = null;

	    insideExteriorDoorList = null;
	    outsideExteriorDoorList = null;

	    outsideInteriorDoorMap = null;
	    outsideExteriorDoorMap = null;
	    
	    insideInteriorDoorMap = null;
	    insideExteriorDoorMap = null;
	}
	
}
