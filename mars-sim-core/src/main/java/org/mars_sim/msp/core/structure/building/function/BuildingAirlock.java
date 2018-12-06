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
import org.mars_sim.msp.core.structure.CompositionOfAir;
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
	private Settlement settlement;
    private Building building; // The building this airlock is for.
    private Inventory inv;
    
    private CompositionOfAir air;
    private Heating heating;
    
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
        
        settlement = building.getBuildingManager().getSettlement();

        inv = settlement.getInventory();
        
        // Determine airlock interior position.
        airlockInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc, interiorYLoc, building);

        // Determine airlock exterior position.
        airlockExteriorPos = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc, exteriorYLoc, building);

        // Determine airlock inside position.
        airlockInsidePos = LocalAreaUtil.getLocalRelativeLocation(xLoc, yLoc, building);
    }
       
    @Override
    protected void exitAirlock(Person person) {

        if (inAirlock(person)) {

            if (PRESSURIZED.equals(getState())) {
            	// check if the airlock has been sealed from outside and pressurized, ready to 
            	// open the inner door to release the person into the settlement
            	LogConsolidated.log(Level.FINER, 0, sourceName,
    	  				"[" + person.getLocationTag().getLocale() 
    	  				+ "] The airlock had been pressurized and is ready to open the inner door to release " + person + ".");
            	
                if (person.isOutside()) {
                	
        			LogConsolidated.log(Level.FINER, 0, sourceName,
        	  				"[" + person.getLocationTag().getLocale() + "] "
        					+ person + " was about to leave the airlock in " + building + " to go inside " 
                			+ building.getBuildingManager().getSettlement()
                			+ ".");
        			
        			if (air == null)
        				air = building.getSettlement().getCompositionOfAir();
        	    	
                    // Pump air into the airlock to make it breathable
                    air.releaseOrRecaptureAir(building.getInhabitableID(), true, building);

                    // Enter a settlement
//                    person.enter(LocationCodeType.SETTLEMENT);               
                    // Put the person into the settlement
                	inv.storeUnit(person);
                    BuildingManager.addPersonOrRobotToBuilding(person, building);
                    
           			LogConsolidated.log(Level.FINER, 0, sourceName,
        	  				"[" + person.getLocationTag().getLocale() + "] "
        					+ person + " had just exited the airlock at " + building + " and went inside " 
                			+ building.getBuildingManager().getSettlement()
                			+ ".");

                }
                
                else {
                	//if (LocationSituation.BURIED != person.getLocationSituation()) {
//                    throw new IllegalStateException(person + " was in " + person.getLocationTag().getImmediateLocation() + " and entering " + getEntityName() +
//                            " from an airlock but not from outside.");
                  	LogConsolidated.log(Level.SEVERE, 0, sourceName,		
                  		person +  " was supposed to be entering " + getEntityName() +
                          "'s airlock but now alraedy in " + person.getLocationTag().getImmediateLocation());
                }
            }
            
            else if (DEPRESSURIZED.equals(getState())) { 
            	// check if the airlock has been depressurized, ready to open the outer door to 
            	// get exposed to the outside air and release the person
            	LogConsolidated.log(Level.FINER, 0, sourceName,
    	  				"[" + person.getLocationTag().getLocale() 
    	  				+ "] The airlock had been depressurized and is ready to open the outer door to release " + person + ".");
            	
            	if (person.isInSettlement()) {
          			LogConsolidated.log(Level.FINER, 0, sourceName,
        	  				"[" + person.getLocationTag().getLocale() + "] "
        					+ person
                			+ " was about to leave the airlock at " + building + " in " 
                			+ building.getBuildingManager().getSettlement()
                			+ " to step outside.");
          			
          			
          			if (heating == null) 
          	    		heating = building.getThermalGeneration().getHeating();
          				
                    // Upon depressurization, there is heat loss to the Martian air in Heating class
                    heating.flagHeatLostViaAirlockOuterDoor(true);
                    
        			if (air == null)
        				air = building.getSettlement().getCompositionOfAir();
        			
                    // Recapture air from the airlock before depressurizing it
                    air.releaseOrRecaptureAir(building.getInhabitableID(), false, building);
                    
                    // Exit the settlement into its vicinity
//                    person.exit(LocationCodeType.SETTLEMENT);
                    // Take the person out of the settlement
                    inv.retrieveUnit(person);
                    BuildingManager.removePersonOrRobotFromBuilding(person, building);
                                     
          			LogConsolidated.log(Level.FINER, 0, sourceName,
        	  				"[" + person.getLocationTag().getLocale() + "] "
        					+ person
                			+ " had just left the airlock at " + building + " in " 
                			+ building.getBuildingManager().getSettlement()
                			+ " and stepped outside.");
          			
                }
                else {
                	//if (LocationSituation.BURIED != person.getLocationSituation()) {
//                    throw new IllegalStateException(
                    	LogConsolidated.log(Level.SEVERE, 0, sourceName,		
                    		person +  " was supposed to be exiting " + getEntityName() +
                            "'s airlock but now alraedy in " + person.getLocationTag().getImmediateLocation());
                }
            }
            else {
                logger.severe("Building airlock in incorrect state for exiting: " + getState());
            }
        }
        else {
            throw new IllegalStateException(person.getName() + " not in airlock of " + getEntityName());
        }
    }

 
    @Override
    public String getEntityName() {
        return building.getNickName() + " in " + settlement.getName();

    }

    @Override
    public Inventory getEntityInventory() {
        return inv;
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
		settlement = null;
	    building = null;
	    inv = null;
	    air = null;
	    heating = null; 
	    airlockInsidePos = null;
	    airlockInteriorPos = null;
	    airlockExteriorPos = null;
	}
	
}