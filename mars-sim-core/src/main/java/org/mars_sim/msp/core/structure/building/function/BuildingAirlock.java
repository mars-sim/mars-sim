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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
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

    private static Logger logger = Logger.getLogger(BuildingAirlock.class.getName());
	
    private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());

	public static final double HEIGHT = 2; // assume an uniform height of 2 meters in all airlocks
	
	/** The volume of an airlock in cubic meters. */
	public static final double AIRLOCK_VOLUME_IN_CM = 12D; //3 * 2 * 2; //in m^3
	
    // Data members.
	/** The building this airlock is for. */
    private Building building;
  
    private Point2D airlockInsidePos;
    private Point2D airlockInteriorPos;
    private Point2D airlockExteriorPos;
    
    private List<Point2D> outsideInteriorList;
    private List<Point2D> insideInteriorList;

    private List<Point2D> insideExteriorList;
    private List<Point2D> outsideExteriorList;
    
    private List<Point2D> EVASpots;

    private Map<Point2D, Integer> outsideInteriorMap;
    private Map<Point2D, Integer> outsideExteriorMap;
    
    private Map<Point2D, Integer> insideInteriorMap;
    private Map<Point2D, Integer> insideExteriorMap;

    private Map<Point2D, Integer> activitySpotMap;
    
    /**
     * Constructor
     * @param building the building this airlock of for.
     * @param capacity number of people airlock can hold.
     */
    public BuildingAirlock(Building building, int capacity, double xLoc, double yLoc,
            double interiorXLoc, double interiorYLoc, double exteriorXLoc, double exteriorYLoc) {
        // User Airlock constructor
        super(capacity);//, building);

        this.building = building;
        
        activitySpotMap  = new HashMap<>();
        
        // Determine airlock interior position.
        airlockInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc, interiorYLoc, building);
        Point2D insideInteriorDoor0 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc + 0.5, interiorYLoc + 0.5, building);
        Point2D insideInteriorDoor1 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc + 0.5, interiorYLoc - 0.5, building);
        Point2D insideInteriorDoor2 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc + 1.0, interiorYLoc + 0.5, building);
        Point2D insideInteriorDoor3 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc + 1.0, interiorYLoc - 0.5, building);
        insideInteriorList = new ArrayList<>();
        insideInteriorList.add(insideInteriorDoor0);
        insideInteriorList.add(insideInteriorDoor1);
        insideInteriorList.add(insideInteriorDoor2);
        insideInteriorList.add(insideInteriorDoor3);
        insideInteriorMap = new HashMap<>();
        for (Point2D p : insideInteriorList) {
        	insideInteriorMap.put(p, -1);
        }
        
        Point2D outsideInteriorDoor0 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc - 0.5, interiorYLoc + 0.5, building);
        Point2D outsideInteriorDoor1 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc - 0.5, interiorYLoc - 0.5, building);       
        Point2D outsideInteriorDoor2 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc - 1.0, interiorYLoc + 0.5, building);       
        Point2D outsideInteriorDoor3 = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc - 1.0, interiorYLoc - 0.5, building);
        outsideInteriorList = new ArrayList<>();
        outsideInteriorList.add(outsideInteriorDoor0);
        outsideInteriorList.add(outsideInteriorDoor1);
        outsideInteriorList.add(outsideInteriorDoor2);
        outsideInteriorList.add(outsideInteriorDoor3);
        outsideInteriorMap = new HashMap<>();
        for (Point2D p : outsideInteriorList) {
        	outsideInteriorMap.put(p, -1);
        }
        
        // Determine airlock exterior position.
        airlockExteriorPos = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc, exteriorYLoc, building);
        Point2D insideExteriorDoor0 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc - 0.5, exteriorYLoc + 0.5, building);
        Point2D insideExteriorDoor1 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc - 0.5, exteriorYLoc - 0.5, building);       
        Point2D insideExteriorDoor2 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc - 1.0, exteriorYLoc + 0.5, building);       
        Point2D insideExteriorDoor3 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc - 1.0, exteriorYLoc - 0.5, building);
        insideExteriorList = new ArrayList<>();
        insideExteriorList.add(insideExteriorDoor0);
        insideExteriorList.add(insideExteriorDoor1);
        insideExteriorList.add(insideExteriorDoor2);
        insideExteriorList.add(insideExteriorDoor3);
        insideExteriorMap = new HashMap<>();
        for (Point2D p : insideExteriorList) {
        	insideExteriorMap.put(p, -1);
        }
        
        Point2D outsideExteriorDoor0 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc + 0.5, exteriorYLoc + 0.5, building);
        Point2D outsideExteriorDoor1 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc + 0.5, exteriorYLoc - 0.5, building);       
        Point2D outsideExteriorDoor2 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc + 1.0, exteriorYLoc + 0.5, building);       
        Point2D outsideExteriorDoor3 = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc + 1.0, exteriorYLoc - 0.5, building);
        outsideExteriorList = new ArrayList<>();
        outsideExteriorList.add(outsideExteriorDoor0);
        outsideExteriorList.add(outsideExteriorDoor1);
        outsideExteriorList.add(outsideExteriorDoor2);
        outsideExteriorList.add(outsideExteriorDoor3);
        outsideExteriorMap = new HashMap<>();
        for (Point2D p : outsideExteriorList) {
        	outsideExteriorMap.put(p, -1);
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
//      	LogConsolidated.log(logger, Level.INFO, 0, sourceName,
//	  				"[" + person.getLocale() 
//	  				+ "] " + person + " called stepInside()");
        	
        if (person.isOutside()) {
        	
			Settlement settlement = building.getSettlement();
			
//			LogConsolidated.log(logger, Level.INFO, 0, sourceName,
//	  				"[" + person.getLocale() + "] "
//					+ person + " was about to leave the airlock in " + building + " to go inside " 
//        			+ settlement
//        			+ ".");
			
            // 1.0. Pump air into the airlock to make it breathable
			settlement.getCompositionOfAir().releaseOrRecaptureAir(building.getInhabitableID(), true, building);

            // 1.1. Transfer a person from the surface of Mars to the building inventory
			successful = person.transfer(marsSurface, settlement);
            
			if (successful) {
	            // 1.2 Add the person to the building
	            BuildingManager.addPersonOrRobotToBuilding(person, building);
	            
				// 1.3 Set the person's coordinates to that of the settlement's
				person.setCoordinates(settlement.getCoordinates());
				
	   			LogConsolidated.log(logger, Level.INFO, 0, sourceName,
		  				"[" + person.getLocale() + "] "
						+ person 
//						+ " came through the inner door of " 
//		  				+ building 
//		  				+ " and "
		  				+ " stepped inside " 
	        			+ settlement
	        			+ ".");
			}
			else
				LogConsolidated.log(logger, Level.SEVERE, 0, sourceName, 
						"[" + person.getLocale() + "] "
						+ person.getName() + " could not step inside " + settlement.getName());
        }
        
        else if (!person.isBuried() || !person.isDeclaredDead()) {
			String loc = person.getLocationTag().getImmediateLocation();
			loc = loc == null ? "[N/A]" : loc;
			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
			
          	LogConsolidated.log(logger, Level.SEVERE, 0, sourceName,	
          		"[" + person.getLocale() + "] "
          		 + person +  " was supposed to be stepping into " + getEntityName() 
          		 + " but already " + loc + " (" + person.getLocationStateType() + ").");
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
    	LogConsolidated.log(logger, Level.FINER, 0, sourceName,
  				"[" + person.getLocale() 
  				+ "] " + person + " called stepOnMars().");
    	
    	if (person.isInSettlement()) {
    		
			Settlement settlement = building.getSettlement();
			
//  			LogConsolidated.log(logger, Level.FINER, 0, sourceName,
//	  				"[" + person.getLocale() + "] "
//					+ person
//        			+ " was about to leave the airlock at " + building + " in " 
//        			+ building.getSettlement()
//        			+ " to step outside.");
  					
            // Upon depressurization, there is heat loss to the Martian air in Heating class
  			building.getThermalGeneration().getHeating().flagHeatLostViaAirlockOuterDoor(true);
            			
            // 5.0. Recapture air from the airlock before depressurizing it
  			settlement.getCompositionOfAir().releaseOrRecaptureAir(building.getInhabitableID(), false, building);
                        
            // 5.2. Transfer a person from the building to the surface of Mars to the vehicle
            successful = person.transfer(settlement, marsSurface);
            
			if (successful) {
				// 5.1 Remove the person from the building
	            BuildingManager.removePersonFromBuilding(person, building);
	         
				// 5.3. Set the person's coordinates to that of the settlement's
				person.setCoordinates(settlement.getCoordinates());
				
	  			LogConsolidated.log(logger, Level.INFO, 0, sourceName,
	  				"[" + person.getLocale() + "] "
					+ person
        			+ " left " 
					+ building + " in " 
        			+ settlement
        			+ " and stepped outside.");
			}
			else
				LogConsolidated.log(logger, Level.SEVERE, 0, sourceName, 
						"[" + person.getLocale() + "] "
						+ person.getName() + " could not step outside " + settlement.getName());
        }
    	
        else if (!person.isBuried() || !person.isDeclaredDead()) {
			String loc = person.getLocationTag().getImmediateLocation();
			loc = loc == null ? "[N/A]" : loc;
			loc = loc.equalsIgnoreCase("Outside") ? loc.toLowerCase() : "in " + loc;
			
            LogConsolidated.log(logger, Level.SEVERE, 0, sourceName,	
                  	"[" + person.getLocale() + "] "	
            		+ person +  " was supposed to be exiting " + getEntityName()
                    + "'s airlock but already " + loc + ".");
        }
    	
    	return successful;
    }
    
    @Override
    public String getEntityName() {
        return building.getNickName();// + " in " + building.getSettlement().getName();
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
    			Point2D p = insideInteriorList.get(i);
    			if (insideInteriorMap.get(p) == -1)
    				return p;
    		}
    	}
    	
    	else {
    		for (int i=0; i<4; i++) {
    			Point2D p = outsideInteriorList.get(i);
    			if (outsideInteriorMap.get(p) == -1)
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
    			Point2D p = insideExteriorList.get(i);
    			if (insideExteriorMap.get(p) == -1)
    				return p;
    		}
    	}
    	
    	else {
    		for (int i=0; i<4; i++) {
    			Point2D p = outsideExteriorList.get(i);
    			if (outsideExteriorMap.get(p) == -1)
    				return p;
    		}
    	}
    	
        return null;
    }

//    @Override
    public boolean occupy(int zone, Point2D p, Integer id) {
    	if (zone == 0) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (outsideInteriorMap.values().contains(id))
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = outsideInteriorList.get(i);
//    			if (pp == p) {
    				outsideInteriorMap.put(p, id);
    				return true;
//    			}
//    		}
    	}
    	
    	else if (zone == 1) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (insideInteriorMap.values().contains(id))
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = insideInteriorList.get(i);
//    			if (pp == p) {
    				insideInteriorMap.put(p, id);
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
    		if (insideExteriorMap.values().contains(id))
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = insideExteriorList.get(i);
//    			if (pp == p) {
    				insideExteriorMap.put(p, id);
    				return true;
//    			}
//    		}
    	}
    	
    	else if (zone == 4) {
    		// Do not allow the same person who has already occupied a position to take another position
    		if (outsideExteriorMap.values().contains(id))
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = outsideExteriorList.get(i);
//    			if (pp == p) {
    				outsideExteriorMap.put(p, id);
    				return true;
//    			}
//    		}
    	}
    	return false;
    }
    
	public <K, V> K getKey(Map<K, V> map, V value) {
	    for (Entry<K, V> entry : map.entrySet()) {
	        if (entry.getValue().equals(value)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
    public boolean vacate(int zone, Integer id) {
    	if (zone == 0) {
    		Point2D oldPos = getKey(outsideInteriorMap, id);
    		if (oldPos == null)
    			return false;
//    		System.out.println("id : " + id);
//    		System.out.println("outsideInteriorMap : " + outsideInteriorMap);
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = outsideInteriorList.get(i);
    			if (outsideInteriorMap.get(oldPos).equals(id)) {
    				outsideInteriorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}
    	
    	else if (zone == 1) {
    		Point2D oldPos = getKey(insideInteriorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = insideInteriorList.get(i);
    			if (insideInteriorMap.get(oldPos).equals(id)) {
    				insideInteriorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}
    	
    	else if (zone == 2) {
    		Point2D oldPos = getKey(activitySpotMap, id);
    		if (oldPos == null)
    			return false;
			if (activitySpotMap.get(oldPos).equals(id)) {
				activitySpotMap.put(oldPos, -1);
				return true;
			}
//    		return true;
    	}
    	
    	else if (zone == 3) {
    		Point2D oldPos = getKey(insideExteriorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = insideExteriorList.get(i);
    			if (insideExteriorMap.get(oldPos).equals(id)) {
    				insideExteriorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}
    	
    	else if (zone == 4) {
    		Point2D oldPos = getKey(outsideExteriorMap, id);
    		if (oldPos == null)
    			return false;
//    		for (int i=0; i<4; i++) {
//    			Point2D pp = outsideExteriorList.get(i);
    			if (outsideExteriorMap.get(oldPos).equals(id)) {
    				outsideExteriorMap.put(oldPos, -1);
    				return true;
    			}
//    		}
    	}
    	return false;
    }
    
	public boolean isInZone(Person p, int zone) {
//		System.out.println(p + " at " + p0);
    	if (zone == 0) {
    		Point2D p0 = getKey(outsideInteriorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D pt = outsideInteriorList.get(i);
    			if (LocalAreaUtil.areLocationsClose(p0, pt)) {
//        			System.out.println(p0 + " ~ " + pt);
    				return true;
    			}
    		}
    	}
    	
    	else if (zone == 1) {
    		Point2D p0 = getKey(insideInteriorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D pt = insideInteriorList.get(i);
    			if (LocalAreaUtil.areLocationsClose(p0, pt)) {
//        			System.out.println(p0 + " ~ " + pt);
    				return true;
    			}
    		}
    	}
    	
    	else if (zone == 2) {
    		loadEVAActivitySpots();
    		Point2D p0 = getKey(activitySpotMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D pt = EVASpots.get(i);
//    			System.out.println("pt : " + pt);
    			if (LocalAreaUtil.areLocationsClose(p0, pt)) {
//        			System.out.println(p0 + " ~ " + pt);
    				return true;
    			}
    		}		
//    		for (int i=0; i<MAX_SLOTS; i++) {
//    			Point2D s = EVASpots.get(i);
////    			System.out.println(p + " at " + p0 + "   Spot at " + s);
//    			if (LocalAreaUtil.areLocationsClose(p.getXLocation(), p.getYLocation(), s.getX(), s.getY())) {
////        			System.out.println(p0 + " ~ " + s);
//    				return true;
//    			}
//    		}
    	}
    	
    	else if (zone == 3) {
    		Point2D p0 = getKey(insideExteriorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D pp = insideExteriorList.get(i);
    			if (LocalAreaUtil.areLocationsClose(p0, pp)) {
    				return true;
    			}
    		}
    	}
    	
    	else if (zone == 4) {
    		Point2D p0 = getKey(outsideExteriorMap, p.getIdentifier());
    		if (p0 == null)
    			return false;
    		for (int i=0; i<MAX_SLOTS; i++) {
    			Point2D pp = outsideExteriorList.get(i);
    			if (LocalAreaUtil.areLocationsClose(p0, pp)) {
    				return true;
    			}
    		}
    	}
		
		return false;
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
		if (zone == 0 && outsideInteriorMap.containsKey(p)) {
			outsideInteriorMap.put(p, -1);
			return true;
		}
		
		if (zone == 1 && insideInteriorMap.containsKey(p)) {
			insideInteriorMap.put(p, -1);
			return true;
		}
		
		if (zone == 2 && activitySpotMap.containsKey(p)) {
			activitySpotMap.put(p, -1);
			return true;
		}
		
//		if (zone == 2) {
//			return true;
//		}
		
		if (zone == 3 && insideExteriorMap.containsKey(p)) {
			insideExteriorMap.put(p, -1);
			return true;
		}

		if (zone == 4 && outsideExteriorMap.containsKey(p)) {
			outsideExteriorMap.put(p, -1);
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
	    
	    outsideInteriorList.clear();
	    insideInteriorList.clear();

	    insideExteriorList.clear();
	    outsideExteriorList.clear();

	    outsideInteriorMap.clear();
	    outsideExteriorMap.clear();
	    
	    insideInteriorMap.clear();
	    insideExteriorMap.clear();
	    
	    activitySpotMap = null;
	    
	    outsideInteriorList = null;
	    insideInteriorList = null;

	    insideExteriorList = null;
	    outsideExteriorList = null;

	    outsideInteriorMap = null;
	    outsideExteriorMap = null;
	    
	    insideInteriorMap = null;
	    insideExteriorMap = null;
	}
	
}
