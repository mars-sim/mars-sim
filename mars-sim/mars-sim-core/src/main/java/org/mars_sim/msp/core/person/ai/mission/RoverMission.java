/**
 * Mars Simulation Project
 * RoverMission.java
 * @version 3.06 2014-03-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.mission;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.DriveGroundVehicle;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A mission that involves driving a rover vehicle along a series of navpoints.
 */
public abstract class RoverMission
extends VehicleMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(RoverMission.class.getName());

	// Static members
	protected static final int MIN_PEOPLE = 2;
	protected static final double MIN_STARTING_SETTLEMENT_METHANE = 1000D;

	// Data members
	private Settlement startingSettlement;

	/**
	 * Constructor.
	 * @param name the name of the mission.
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error constructing mission.
	 */
	protected RoverMission(String name, Person startingPerson) {
		// Use VehicleMission constructor.
		super(name, startingPerson, MIN_PEOPLE);
	}

    /**
     * Constructor with min people.
     * @param name the name of the mission.
     * @param startingPerson the person starting the mission.
     * @param minPeople the minimum number of people required for mission.
     * @throws MissionException if error constructing mission.
     */
    protected RoverMission(String name, Person startingPerson, int minPeople) {
        // Use VehicleMission constructor.
        super(name, startingPerson, minPeople);
    }

    /**
     * Constructor with min people and rover.
     * @param name the name of the mission.
     * @param startingPerson the person starting the mission.
     * @param minPeople the minimum number of people required for mission.
     * @param rover the rover to use on the mission.
     * @throws MissionException if error constructing mission.
     */
    protected RoverMission(String name, Person startingPerson, int minPeople,
            Rover rover) {
        // Use VehicleMission constructor.
        super(name, startingPerson, minPeople, rover);
    }

    /**
     * Gets the mission's rover if there is one.
     * @return vehicle or null if none.
     */
    public final Rover getRover() {
        return (Rover) getVehicle();
    }

    /**
     * Sets the starting settlement.
     * @param startingSettlement the new starting settlement
     */
    protected final void setStartingSettlement(Settlement startingSettlement) {
        this.startingSettlement = startingSettlement;
        fireMissionUpdate(MissionEventType.STARTING_SETTLEMENT_EVENT);
    }

    /**
     * Gets the starting settlement.
     * @return starting settlement
     */
    public final Settlement getStartingSettlement() {
        return startingSettlement;
    }

    /**
     * The person performs the current phase of the mission.
     * @param person the person performing the phase.
     * @throws MissionException if problem performing the phase.
     */
    protected void performPhase(Person person) {
        // if (hasEmergency()) setEmergencyDestination(true);
        super.performPhase(person);
    }

    /**
     * Gets the available vehicle at the settlement with the greatest range.
     * @param settlement the settlement to check.
     * @param allowMaintReserved allow vehicles that are reserved for maintenance.
     * @return vehicle or null if none available.
     * @throws Exception if error finding vehicles.
     */
    protected static Vehicle getVehicleWithGreatestRange(Settlement settlement,
            boolean allowMaintReserved) {
        Vehicle result = null;

        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();

            boolean usable = true;
            if (vehicle.isReservedForMission())
                usable = false;
            if (!allowMaintReserved && vehicle.isReserved())
                usable = false;
            if (!vehicle.getStatus().equals(Vehicle.PARKED))
                usable = false;
            if (vehicle.getInventory().getTotalInventoryMass(false) > 0D)
                usable = false;
            if (!(vehicle instanceof Rover))
                usable = false;

            if (usable) {
                if (result == null)
                    result = vehicle;
                else if (vehicle.getRange() > result.getRange())
                    result = vehicle;
            }
        }

        return result;
    }

    /**
     * Checks to see if any vehicles are available at a settlement.
     * @param settlement the settlement to check.
     * @param allowMaintReserved allow vehicles that are reserved for maintenance.
     * @return true if vehicles are available.
     */
    protected static boolean areVehiclesAvailable(Settlement settlement,
            boolean allowMaintReserved) {

        boolean result = false;

        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();

            boolean usable = true;
            if (vehicle.isReservedForMission())
                usable = false;
            if (!allowMaintReserved && vehicle.isReserved())
                usable = false;
            if (!vehicle.getStatus().equals(Vehicle.PARKED))
                usable = false;
            if (!(vehicle instanceof Rover))
                usable = false;

            if (vehicle.getInventory().getTotalInventoryMass(false) > 0D)
                usable = false;

            if (usable)
                result = true;
        }

        return result;
    }

    /**
     * Checks if vehicle is usable for this mission. (This method should be overridden by children)
     * @param newVehicle the vehicle to check
     * @return true if vehicle is usable.
     * @throws MissionException if problem determining if vehicle is usable.
     */
    protected boolean isUsableVehicle(Vehicle newVehicle) {
        boolean usable = super.isUsableVehicle(newVehicle);
        if (!(newVehicle instanceof Rover))
            usable = false;
        return usable;
    }

    /**
     * Checks that everyone in the mission is aboard the rover.
     * @return true if everyone is aboard
     */
    protected final boolean isEveryoneInRover() {
        boolean result = true;
        Iterator<Person> i = getPeople().iterator();
        while (i.hasNext()) {
            if (i.next().getLocationSituation() != LocationSituation.IN_VEHICLE)
                result = false;
        }
        return result;
    }

    /**
     * Checks that no one in the mission is aboard the rover.
     * @return true if no one is aboard
     */
    protected final boolean isNoOneInRover() {
        boolean result = true;
        Iterator<Person> i = getPeople().iterator();
        while (i.hasNext()) {
            if (i.next().getLocationSituation() == LocationSituation.IN_VEHICLE)
                result = false;
        }
        return result;
    }

    /**
     * Checks if the rover is currently in a garage or not.
     * @return true if rover is in a garage.
     */
    protected boolean isRoverInAGarage() {
        return (BuildingManager.getBuilding(getVehicle()) != null);
    }

    /**
     * Performs the embark from settlement phase of the mission.
     * @param person the person currently performing the mission
     * @throws MissionException if error performing phase.
     */
    protected void performEmbarkFromSettlementPhase(Person person) {

        Settlement settlement = getVehicle().getSettlement();
        if (settlement == null)
            throw new IllegalStateException(getPhase()
                    + " : Vehicle is not at a settlement.");

        // Add the rover to a garage if possible.
        if (BuildingManager.getBuilding(getVehicle()) == null) {
            BuildingManager.addToRandomBuilding((Rover) getVehicle(),
                    getVehicle().getSettlement());
        }

        // Load vehicle if not fully loaded.
        if (!loadedFlag) {
            if (isVehicleLoaded()) {
                loadedFlag = true;
            }
            else {
                // Check if vehicle can hold enough supplies for mission.
                if (isVehicleLoadable()) {
                    // Load rover
                    // Random chance of having person load (this allows person to do other things sometimes)
                    if (RandomUtil.lessThanRandPercent(75)) {
                        if (BuildingManager.getBuilding(getVehicle()) != null) {
                            assignTask(person, new LoadVehicleGarage(person, getVehicle(), 
                                    getRequiredResourcesToLoad(), getOptionalResourcesToLoad(), 
                                    getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
                        }
                        else {
                            // Check if it is day time.
                            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
                            if ((surface.getSurfaceSunlight(person.getCoordinates()) > 0D) || 
                                    surface.inDarkPolarRegion(person.getCoordinates())) {
                                assignTask(person, new LoadVehicleEVA(person, getVehicle(), 
                                        getRequiredResourcesToLoad(), getOptionalResourcesToLoad(), 
                                        getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
                            }
                        }
                    }
                } else {
                    endMission("Vehicle is not loadable (RoverMission).");
                    return;
                }
            }
        } else {
            // If person is not aboard the rover, board rover.
            if (person.getLocationSituation() != LocationSituation.IN_VEHICLE
                    && person.getLocationSituation() != LocationSituation.BURIED) {

                // Move person to random location within rover.
                Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(getVehicle());
                Point2D.Double adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), 
                        vehicleLoc.getY(), getVehicle());
                if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle())) {
                    assignTask(person, new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle()));
                }
                else {
                    logger.severe(person.getName() + " unable to enter rover " + getVehicle());
                    endMission(person.getName() + " unable to enter rover " + getVehicle());
                }
                
                if (isRoverInAGarage()) {
                    
                    // Store one EVA suit for person (if possible).
                    if (settlement.getInventory().findNumUnitsOfClass(EVASuit.class) > 0) {
                        EVASuit suit = (EVASuit) settlement.getInventory().findUnitOfClass(EVASuit.class);
                        if (getVehicle().getInventory().canStoreUnit(suit, false)) {
                            settlement.getInventory().retrieveUnit(suit);
                            getVehicle().getInventory().storeUnit(suit);
                        }
                        else {
                            endMission("Equipment " + suit + " cannot be loaded in rover " + getVehicle());
                            return;
                        }
                    }
                }
            }

            // If rover is loaded and everyone is aboard, embark from settlement.
            if (!isDone() && loadedFlag && isEveryoneInRover()) {

                // Remove from garage if in garage.
                Building garageBuilding = BuildingManager
                        .getBuilding(getVehicle());
                if (garageBuilding != null) {
					VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
                    garage.removeVehicle(getVehicle());
                }

                // Embark from settlement
                settlement.getInventory().retrieveUnit(getVehicle());
                setPhaseEnded(true);
            }
        }
    }

    /**
     * Performs the disembark to settlement phase of the mission.
     * @param person the person currently performing the mission.
     * @param disembarkSettlement the settlement to be disembarked to.
     * @throws MissionException if error performing phase.
     */
    protected void performDisembarkToSettlementPhase(Person person,
            Settlement disembarkSettlement) {

        Building garageBuilding = null;
        VehicleMaintenance garage = null;

        // If rover is not parked at settlement, park it.
        if ((getVehicle() != null) && (getVehicle().getSettlement() == null)) {
            disembarkSettlement.getInventory().storeUnit(getVehicle());
            getVehicle().determinedSettlementParkedLocationAndFacing();

            // Add vehicle to a garage if available.
            BuildingManager.addToRandomBuilding((GroundVehicle) getVehicle(),
                    disembarkSettlement);
            garageBuilding = BuildingManager.getBuilding(getVehicle());
            if (garageBuilding != null)
                garage = (VehicleMaintenance) garageBuilding
				.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
        }

        // Have person exit rover if necessary.
        if (person.getLocationSituation() != LocationSituation.IN_SETTLEMENT) {
            
            // Get random inhabitable building at settlement.
            Building destinationBuilding = disembarkSettlement.getBuildingManager().
                    getRandomAirlockBuilding();
            if (destinationBuilding != null) {
                Point2D destinationLoc = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
                Point2D adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(destinationLoc.getX(), 
                        destinationLoc.getY(), destinationBuilding);
                
                if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding)) {
                    assignTask(person, new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding));
                }
                else {
                    logger.severe(person + " unable to walk to building " + destinationBuilding);
                }
            }
            else {
                logger.severe("No inhabitable buildings at " + destinationBuilding);
                endMission("No inhabitable buildings at " + destinationBuilding);
            }
        }

        Rover rover = (Rover) getVehicle();
        if (rover != null) {
            
            // If any people are aboard the rover who aren't mission members, carry them into the settlement.
            if (isNoOneInRover() && (rover.getCrewNum() > 0)) {
                Iterator<Person> i = rover.getCrew().iterator();
                while (i.hasNext()) {
                    Person crewmember = i.next();
                    rover.getInventory().retrieveUnit(crewmember);
                    disembarkSettlement.getInventory().storeUnit(crewmember);
                    BuildingManager.addToRandomBuilding(crewmember,
                            disembarkSettlement);
                }
            }

            // If no one is in the rover, unload it and end phase.
            if (isNoOneInRover()) {
                
                // Unload rover if necessary.
                boolean roverUnloaded = rover.getInventory().getTotalInventoryMass(false) == 0D;
                if (!roverUnloaded) {
                    // Random chance of having person unload (this allows person to do other things sometimes)
                    if (RandomUtil.lessThanRandPercent(50)) {
                        if (BuildingManager.getBuilding(rover) != null) {
                            assignTask(person, new UnloadVehicleGarage(person, rover));
                        }
                        else {
                            // Check if it is day time.
                            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
                            if ((surface.getSurfaceSunlight(person.getCoordinates()) > 0D) || 
                                    surface.inDarkPolarRegion(person.getCoordinates())) {
                                assignTask(person, new UnloadVehicleEVA(person, rover));
                            }
                        }
                    
                        return;
                    }
                }
                else {
                    // End the phase.
                    
                    // If the rover is in a garage, put the rover outside.
                    if (isRoverInAGarage()) {
                        garageBuilding = BuildingManager.getBuilding(getVehicle());
                        garage = (VehicleMaintenance) garageBuilding
								.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
                        garage.removeVehicle(getVehicle());
                    }

                    // Leave the vehicle.
                    leaveVehicle();
                    setPhaseEnded(true);
                }
            }
        } 
        else {
            setPhaseEnded(true);
        }
    }

    /**
     * Gets a new instance of an OperateVehicle task for the person.
     * @param person the person operating the vehicle.
     * @return an OperateVehicle task for the person.
     * @throws MissionException if error creating OperateVehicle task.
     */
    protected OperateVehicle getOperateVehicleTask(Person person,
            String lastOperateVehicleTaskPhase) {
        OperateVehicle result = null;
        if (lastOperateVehicleTaskPhase != null) {
            result = new DriveGroundVehicle(person, getRover(),
                    getNextNavpoint().getLocation(),
                    getCurrentLegStartingTime(), getCurrentLegDistance(),
                    lastOperateVehicleTaskPhase);
        } else {
            result = new DriveGroundVehicle(person, getRover(),
                    getNextNavpoint().getLocation(),
                    getCurrentLegStartingTime(), getCurrentLegDistance());
        }

        return result;
    }

    /**
     * Checks to see if at least one inhabitant a settlement is remaining there.
     * @param settlement the settlement to check.
     * @param person the person checking
     * @return true if at least one person left at settlement.
     */
    protected static boolean atLeastOnePersonRemainingAtSettlement(
            Settlement settlement, Person person) {
        boolean result = false;

        if (settlement != null) {
            Iterator<Person> i = settlement.getInhabitants().iterator();
            while (i.hasNext()) {
                Person inhabitant = i.next();
                if ((inhabitant != person)
                        && !inhabitant.getMind().hasActiveMission())
                    result = true;
            }
        }

        return result;
    }

    /**
     * Checks to see if at least a minimum number of people are available for a mission at a settlement.
     * @param settlement the settlement to check.
     * @param minNum minimum number of people required.
     * @return true if minimum people available.
     */
    protected static boolean minAvailablePeopleAtSettlement(
            Settlement settlement, int minNum) {
        boolean result = false;

        if (settlement != null) {
            int numAvailable = 0;
            Iterator<Person> i = settlement.getInhabitants().iterator();
            while (i.hasNext()) {
                Person inhabitant = i.next();
                if (!inhabitant.getMind().hasActiveMission())
                    numAvailable++;
            }
            if (numAvailable >= minNum)
                result = true;
        }

        return result;
    }

    /**
     * Checks if there is only one person at the associated settlement and he/she has a serious medical problem.
     * @return true if serious medical problem
     */
    protected final boolean hasDangerousMedicalProblemAtAssociatedSettlement() {
        boolean result = false;
        if (getAssociatedSettlement() != null) {
            if (getAssociatedSettlement().getCurrentPopulationNum() == 1) {
                Person person = (Person) getAssociatedSettlement()
                        .getInhabitants().toArray()[0];
                if (person.getPhysicalCondition().hasSeriousMedicalProblems())
                    result = true;
            }
        }
        return result;
    }

    /**
     * Checks if the mission has an emergency situation.
     * @return true if emergency.
     */
    protected final boolean hasEmergency() {
        boolean result = super.hasEmergency();
        if (hasDangerousMedicalProblemAtAssociatedSettlement())
            result = true;
        return result;
    }

    @Override
    public Map<Resource, Number> getResourcesNeededForTrip(boolean useBuffer,
            double distance) {
        Map<Resource, Number> result = super.getResourcesNeededForTrip(
                useBuffer, distance);

        // Determine estimate time for trip.
        double time = getEstimatedTripTime(useBuffer, distance);
        double timeSols = time / 1000D;

        int crewNum = getPeopleNumber();

        // Determine life support supplies needed for trip.
        double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate()
                * timeSols * crewNum;
        if (useBuffer)
            oxygenAmount *= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
        AmountResource oxygen = AmountResource.findAmountResource("oxygen");
        result.put(oxygen, oxygenAmount);

        double waterAmount = PhysicalCondition.getWaterConsumptionRate()
                * timeSols * crewNum;
        if (useBuffer)
            waterAmount *= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
        AmountResource water = AmountResource.findAmountResource("water");
        result.put(water, waterAmount);

        double foodAmount = PhysicalCondition.getFoodConsumptionRate()
                * timeSols * crewNum;
        if (useBuffer)
            foodAmount *= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
        AmountResource food = AmountResource.findAmountResource("food");
        result.put(food, foodAmount);

        return result;
    }

    @Override
    public void endMission(String reason) {
        // If at a settlement, associate all members with the settlement.
        Iterator<Person> i = getPeople().iterator();
        while (i.hasNext()) {
            Person person = i.next();
            if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT)
                person.setAssociatedSettlement(person.getSettlement());
        }

        super.endMission(reason);
    }

    /**
     * Checks if there is an available backup rover at the settlement for the mission.
     * @param settlement the settlement to check.
     * @return true if available backup rover.
     */
    protected static boolean hasBackupRover(Settlement settlement) {
        int availableVehicleNum = 0;
        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            if ((vehicle instanceof Rover) && !vehicle.isReservedForMission())
                availableVehicleNum++;
        }
        return (availableVehicleNum >= 2);
    }

    /**
     * Checks if there are enough basic mission resources at the settlement to start mission.
     * @param settlement the starting settlement.
     * @return true if enough resources.
     */
    protected static boolean hasEnoughBasicResources(Settlement settlement) {
        boolean hasBasicResources = true;
        
        Inventory inv = settlement.getInventory();
        try {
            AmountResource oxygen = AmountResource.findAmountResource("oxygen");
            if (inv.getAmountResourceStored(oxygen, false) < 50D) {
                hasBasicResources = false;
            }
            AmountResource water = AmountResource.findAmountResource("water");
            if (inv.getAmountResourceStored(water, false) < 50D) {
                hasBasicResources = false;
            }
            AmountResource food = AmountResource.findAmountResource("food");
            if (inv.getAmountResourceStored(food, false) < 50D) {
                hasBasicResources = false;
            }
            AmountResource methane = AmountResource
                    .findAmountResource("methane");
            if (inv.getAmountResourceStored(methane, false) < 100D) {
                hasBasicResources = false;
            }
        } 
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return hasBasicResources;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        startingSettlement = null;
    }
}