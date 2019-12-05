/**
 * Mars Simulation Project
 * BuildingAirlock.java
 * @version 3.1.0 2017-03-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.awt.geom.Point2D;
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

        // Determine airlock interior position.
        airlockInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc, interiorYLoc, building);

        // Determine airlock exterior position.
        airlockExteriorPos = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc, exteriorYLoc, building);

        // Determine airlock inside position.
        airlockInsidePos = LocalAreaUtil.getLocalRelativeLocation(xLoc, yLoc, building);
    }
       
    @Override
    protected boolean exitAirlock(Person person) {
    	boolean successful = false;
    	
        if (inAirlock(person)) {
            if (AirlockState.PRESSURIZED == getState()) {
            	// check if the airlock has been sealed from outside and pressurized, ready to 
            	// open the inner door to release the person into the settlement
            	successful = stepInside(person);
            }
            
            else if (AirlockState.DEPRESSURIZED == getState()) {
            	// check if the airlock has been de-pressurized, ready to open the outer door to 
            	// get exposed to the outside air and release the person
            	successful = stepIntoMarsSurface(person);
            }
            else {
                logger.severe("Building airlock in incorrect state for exiting: " + getState());
            }
        }
        else {
            throw new IllegalStateException(person.getName() + " not in airlock of " + getEntityName());
        }
        
        return successful;
    }

    /**
     * Steps inside of a settlement
     * 
     * @param person
     */
    public boolean stepInside(Person person) {
    	boolean successful = false;
      	LogConsolidated.log(Level.FINER, 0, sourceName,
	  				"[" + person.getLocationTag().getLocale() 
	  				+ "] The airlock had been pressurized and is ready to open the inner door to release " + person + ".");
        	
        if (person.isOutside()) {
        	
			Settlement settlement = building.getSettlement();
			
			LogConsolidated.log(Level.FINER, 0, sourceName,
	  				"[" + person.getLocationTag().getLocale() + "] "
					+ person + " was about to leave the airlock in " + building + " to go inside " 
        			+ settlement
        			+ ".");
			
            // 1.0. Pump air into the airlock to make it breathable
			settlement.getCompositionOfAir().releaseOrRecaptureAir(building.getInhabitableID(), true, building);

            // 1.1. Transfer a person from the surface of Mars to the building inventory
			successful = person.transfer(marsSurface, settlement);
            
			if (successful) {
	            // 1.2 Add the person to the building
	            BuildingManager.addPersonOrRobotToBuilding(person, building);
	            
				// 1.3 Set the person's coordinates to that of the settlement's
				person.setCoordinates(settlement.getCoordinates());
				
	   			LogConsolidated.log(Level.FINER, 0, sourceName,
		  				"[" + person.getLocationTag().getLocale() + "] "
						+ person + " doffed the EVA suit, came through the inner door of the airlock at " 
		  				+ building + " and went inside " 
	        			+ settlement
	        			+ ".");
			}
			else
				LogConsolidated.log(Level.SEVERE, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] "
						+ person.getName() + " could not step inside " + settlement.getName());
        }
        
        else if (!person.isBuried() || !person.isDeclaredDead()) {
          	LogConsolidated.log(Level.SEVERE, 0, sourceName,	
          		"[" + person.getLocationTag().getLocale() + "] "
          		 + person +  " was supposed to be entering " + getEntityName() 
          		 + "'s airlock but already in " + person.getLocationTag().getImmediateLocation());
        }
    	
    	return successful;
    }
 
    /**
     * Gets outside of the airlock and step into the surface of Mars
     * 
     * @param person
     */
    public boolean stepIntoMarsSurface(Person person) {
    	boolean successful = false;
    	LogConsolidated.log(Level.FINER, 0, sourceName,
  				"[" + person.getLocationTag().getLocale() 
  				+ "] The airlock had been depressurized and is ready to open the outer door to release " + person + ".");
    	
    	if (person.isInSettlement()) {
    		
			Settlement settlement = building.getSettlement();
			
  			LogConsolidated.log(Level.FINER, 0, sourceName,
	  				"[" + person.getLocationTag().getLocale() + "] "
					+ person
        			+ " was about to leave the airlock at " + building + " in " 
        			+ building.getSettlement()
        			+ " to step outside.");
  					
            // Upon depressurization, there is heat loss to the Martian air in Heating class
  			building.getThermalGeneration().getHeating().flagHeatLostViaAirlockOuterDoor(true);
            			
            // 5.0. Recapture air from the airlock before depressurizing it
  			settlement.getCompositionOfAir().releaseOrRecaptureAir(building.getInhabitableID(), false, building);
                        
            // 5.2. Transfer a person from the building to the surface of Mars to the vehicle
            successful = person.transfer(settlement, marsSurface);
            
			if (successful) {
				// 5.1 Remove the person from the building
	            BuildingManager.removePersonOrRobotFromBuilding(person, building);
	         
				// 5.3. Set the person's coordinates to that of the settlement's
				person.setCoordinates(settlement.getCoordinates());
				
	  			LogConsolidated.log(Level.FINER, 0, sourceName,
	  				"[" + person.getLocationTag().getLocale() + "] "
					+ person
        			+ " donned the EVA suit, came through the outer door of the airlock at " 
					+ building + " in " 
        			+ settlement
        			+ " and stepped outside.");
			}
			else
				LogConsolidated.log(Level.SEVERE, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] "
						+ person.getName() + " could not step outside " + settlement.getName());
        }
    	
        else if (!person.isBuried() || !person.isDeclaredDead()) {
            	LogConsolidated.log(Level.SEVERE, 0, sourceName,	
                  	"[" + person.getLocationTag().getLocale() + "] "	
            		+ person +  " was supposed to be exiting " + getEntityName()
                    + "'s airlock but already " + person.getLocationTag().getImmediateLocation());
        }
    	
    	return successful;
    }
    
    @Override
    public String getEntityName() {
        return building.getNickName() + " in " + building.getSettlement().getName();

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
    public Point2D getAvailableInteriorPosition() {
        return airlockInteriorPos;
    }

    @Override
    public Point2D getAvailableExteriorPosition() {
        return airlockExteriorPos;
    }

    @Override
    public Point2D getAvailableAirlockPosition() {
        return airlockInsidePos;
    }

	public void destroy() {
	    building = null;
	    airlockInsidePos = null;
	    airlockInteriorPos = null;
	    airlockExteriorPos = null;
	}
	
}