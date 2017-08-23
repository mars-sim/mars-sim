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

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
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
	
    private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());

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
        Inventory inv = building.getSettlementInventory();

        if (inAirlock(person)) {

            if (PRESSURIZED.equals(getState())) { 
            	// check if the airlock has been sealed from outside and pressurized, ready to 
            	// open the inner door to release the person into the settlement
            	
                // Pump air into the airlock to make it breathable
                building.getSettlement().getCompositionOfAir().pumpOrRecaptureAir(building.getInhabitableID(), true, building);
                  
                if (LocationSituation.OUTSIDE == person.getLocationSituation()) {
                	//logger.fine(
                	LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName, person 
                			+ " has got inside the airlock at " + building + " in " 
                			+ building.getBuildingManager().getSettlement()
                			+ ". The airlock has been pressurized and is ready to open the inner door to release the person. ", null);
                	inv.storeUnit(person);
                    BuildingManager.addPersonOrRobotToBuildingSameLocation(person, building);
                    


                }
                else {
                	//if (LocationSituation.BURIED != person.getLocationSituation()) {
                    throw new IllegalStateException(person + " is entering " + getEntityName() +
                            " from an airlock but is not outside.");
                }
            }
            else if (DEPRESSURIZED.equals(getState())) { 
            	// check if the airlock has been depressurized, ready to open the outer door to 
            	// get exposed to the outside air and release the person
            	
                // Upon depressurization, there is heat loss to the Martian air in Heating class
                building.getThermalGeneration().getHeating().flagHeatLostViaAirlockOuterDoor(true);
                
                // Recapture air from the airlock before depressurizing it
                building.getSettlement().getCompositionOfAir().pumpOrRecaptureAir(building.getInhabitableID(), false, building);
                
                if (LocationSituation.IN_SETTLEMENT == person.getLocationSituation()) {
                   	//logger.fine(
                	LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName, person 
                			+ " has got inside the airlock at " + building + " in " 
                			+ building.getBuildingManager().getSettlement()
                			+ ". The airlock has been depressurized and is ready to open the outer door to release the person. ", null);
                    inv.retrieveUnit(person);
                    BuildingManager.removePersonOrRobotFromBuilding(person, building);

                    // Recapture air from the airlock and store for us
                    building.getSettlement().getCompositionOfAir().pumpOrRecaptureAir(building.getInhabitableID(), true, building);

                }
                else {
                	//if (LocationSituation.BURIED != person.getLocationSituation()) {
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
        Inventory inv = building.getSettlementInventory();

        if (inAirlock(robot)) {
        	
        	// NOTE : robot is currently NOT allowed to leave the settlement
        	//return;
        	
            if (PRESSURIZED.equals(getState())) {
                if (LocationSituation.OUTSIDE == robot.getLocationSituation()) {
                  	//logger.fine(
                	LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName, robot 
                			+ " has got inside the airlock at " + building + " in " 
                			+ building.getBuildingManager().getSettlement()
                			+ ". The airlock has been pressurized and is ready to open the inner door to release the robot. ", null);        	 
                  	inv.storeUnit(robot);
                	BuildingManager.addPersonOrRobotToBuildingSameLocation(robot, building);
               }
                else {
                	//if (LocationSituation.BURIED != robot.getLocationSituation()) {
                    throw new IllegalStateException(robot + " is entering " + getEntityName() +
                            " from an airlock but is not outside.");
                }
            }
            else if (DEPRESSURIZED.equals(getState())) {
                if (LocationSituation.IN_SETTLEMENT == robot.getLocationSituation()) {
                   	//logger.fine(
                   	LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName, robot 
                			+ " has got inside the airlock at " + building + " in " 
                			+ building.getBuildingManager().getSettlement()
                			+ ". The airlock has been depressurized and is ready to open the outer door to release the robot. ", null);
                	inv.retrieveUnit(robot);
                 	BuildingManager.removePersonOrRobotFromBuilding(robot, building);

                }
                else {
                	//if (LocationSituation.BURIED != robot.getLocationSituation()) {
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
        return building.getSettlementInventory();
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