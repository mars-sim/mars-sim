/**
 * Mars Simulation Project
 * BuildingAirlock.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.awt.geom.Point2D;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
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

    // Data members.
    private Building building; // The building this airlock is for.
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
        
        if (building == null) {
            throw new IllegalArgumentException("building is null.");
        }
        
        // Determine airlock interior position.
        airlockInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc, interiorYLoc, building);
        
        // Determine airlock exterior position.
        airlockExteriorPos = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc, exteriorYLoc, building);
        
        // Determine airlock inside position.
        airlockInsidePos = LocalAreaUtil.getLocalRelativeLocation(xLoc, yLoc, building); 
    }   

    protected void exitAirlock(Person person) {
        Inventory inv = building.getInventory();

        if (inAirlock(person)) {

            if (PRESSURIZED.equals(getState())) {
                if (LocationSituation.OUTSIDE == person.getLocationSituation()) {
                    // Exit person to inside building.
                    //logger.fine(person + " entering " + building + " via airlock.");
                    BuildingManager.addPersonOrRobotToBuildingSameLocation(person, building);
                    inv.storeUnit(person);
                    logger.fine(person + " is about to exit airlock and officially belongs into " + building);
                }
                else if (LocationSituation.BURIED != person.getLocationSituation()) {
                    throw new IllegalStateException(person + " is entering " + getEntityName() + 
                            " from an airlock but is not outside.");
                }
            }
            else if (DEPRESSURIZED.equals(getState())) {
                if (LocationSituation.IN_SETTLEMENT == person.getLocationSituation()) {
                    // Exit person to outside building.
                    //logger.fine(person + " exiting " + building + " via airlock.");
                    BuildingManager.removePersonOrRobotFromBuilding(person, building);
                    logger.fine(person + " is about to exit the airlock and leave " + building + " and going outside");
                    inv.retrieveUnit(person);
                }
                else if (LocationSituation.BURIED != person.getLocationSituation()) {
                    throw new IllegalStateException(person + " is exiting " + getEntityName() + 
                            " from an airlock but is not inside.");
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

    protected void exitAirlock(Robot robot) {
        Inventory inv = building.getInventory();

        if (inAirlock(robot)) {

            if (PRESSURIZED.equals(getState())) {
                if (LocationSituation.OUTSIDE == robot.getLocationSituation()) {
                    // Exit robot to inside building.
                    //logger.fine(robot + " exiting " + building + " via airlock.");
                    BuildingManager.addPersonOrRobotToBuildingSameLocation(robot, building);
                    logger.info(robot + " is about to exit the airlock door and officially belongs into " + building);         
                    inv.storeUnit(robot);
                }
                else if (LocationSituation.BURIED != robot.getLocationSituation()) {
                    throw new IllegalStateException(robot + " is entering " + getEntityName() + 
                            " from an airlock but is not outside.");
                }
            }
            else if (DEPRESSURIZED.equals(getState())) {
                if (LocationSituation.IN_SETTLEMENT == robot.getLocationSituation()) {
                    // Exit robot to outside building.
                    //logger.fine(robot + " exiting " + building + " via airlock.");
                    BuildingManager.removePersonOrRobotFromBuilding(robot, building);
                    logger.info(robot + " is about to exit the airlock door and leave " + building + " and going outside");
                    inv.retrieveUnit(robot);
                }
                else if (LocationSituation.BURIED != robot.getLocationSituation()) {
                    throw new IllegalStateException(robot + " is exiting " + getEntityName() + 
                            " from an airlock but is not inside.");
                }
            }
            else {
                logger.severe("Building airlock in incorrect state for exiting: " + getState());
            }
        }
        else {
            throw new IllegalStateException(robot.getName() + " not in airlock of " + getEntityName());
        }
    }
    @Override
    public String getEntityName() {
        Settlement settlement = building.getBuildingManager().getSettlement();
        //return settlement.getName() + ": " + building.getNickName();
        return building.getNickName() + " in " + settlement.getName();

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

	@Override
	protected void exitAirlock(Unit occupant) {
	    
        Person person = null;
        Robot robot = null;
        
        if (occupant instanceof Person) {
         	person = (Person) occupant;
         	exitAirlock(person);
        
        }
        else if (occupant instanceof Robot) {
        	robot = (Robot) occupant;
        	exitAirlock(robot);
		
        }
		
	}
}