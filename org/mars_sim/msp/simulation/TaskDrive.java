/**
 * Mars Simulation Project
 * TaskDrive.java
 * @version 2.71 2001-1-9
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The TaskDrive class is a task for driving a ground vehicle.
 *  It has the phases, "Embarking", "Driving" and "Disembarking".
 *  If the task is called as part of a larger task, embarking and/or
 *  disembarking may be ignored by setting the proper constructor parameters.
 *  By default, the task randomly chooses a settlement to drive to, with
 *  the closest three settlements being the most likely.
 */
class TaskDrive extends Task {

    // Data members
    private GroundVehicle vehicle; // Vehicle that person is driving
    private Settlement embarkingSettlement; // The settlement which the person is embarking from.
    private Settlement destinationSettlement; // The destination settlement (if there is one)
    private Coordinates destinationCoordinates; // The destination coordinates
    private String destinationType; // Type of destination ("None", "Settlement" or "Coordinates")
    private boolean embark; // True if driver and crew are to automatically embark before leaving.
    private boolean disembark; // True if driver and crew are to automatically disembark when vehicle is parked.

    private double closestDistance; // Closest distance to destination vehicle has been so far.
    private int obstacleTimeCount; // Number of turns driver has not been any closer to destination.
    private double backingUpDistance; // Distance vehicle has backed up to avoid an obstacle.
    private Coordinates startingLocation; // Current location of vehicle.
    private int destinationSettlementAvailablePop; // Population openings at destination settlement.

    /** Constructs a new TaskDrive object
     *  @param person the person performing the task
     *  @param mars the virtual Mars
     */
    public TaskDrive(Person person, VirtualMars mars) {
        super("Driving Ground Vehicle", person, mars);
        destinationType = new String("None");
        if (person.getLocationSituation().equals("In Settlement"))
            embarkingSettlement = person.getSettlement();
        embark = true;
        disembark = true;
        backingUpDistance = 0D;
        obstacleTimeCount = 0;
    }

    /** Constructor to drive to a specific settlement (called by another task) 
     *  @param person the person performing the task
     *  @param mars the virtual Mars
     *  @param destinationSettlement the driving destination settlement
     *  @param embark true if embarking proceedures should be used
     *  @param disembark true if disembarking proceedures should be used
     */
    TaskDrive(Person person, VirtualMars mars, Settlement destinationSettlement, boolean embark,
            boolean disembark) {
        this(person, mars);
        this.destinationSettlement = destinationSettlement;
        this.destinationCoordinates = destinationSettlement.getCoordinates();
        destinationType = new String("Settlement");
        this.embark = embark;
        this.disembark = disembark;
    }

    /** Constructor to drive to a specific location (called by another task) 
     *  @param person the person performing the task
     *  @param mars the virtual Mars
     *  @param destinationCoordinates the driving destination location
     *  @param embark true if embarking proceedures should be used
     *  @param disembark true if disembarking proceedures should be used
     */
    TaskDrive(Person person, VirtualMars mars, Coordinates destinationCoordinates, boolean embark,
            boolean disembark) {
        this(person, mars);
        this.destinationCoordinates = new Coordinates(destinationCoordinates);
        destinationType = new String("Coordinates");
        this.embark = embark;
        this.disembark = disembark;
    }

    /** Constructor to drive a specific vehicle to a specific settlement (called by another task) 
     *  @param person the person performing the task
     *  @param mars the virtual Mars
     *  @param destinationSettlement the driving destination settlement
     *  @param embark true if embarking proceedures should be used
     *  @param disembark true if disembarking proceedures should be used
     *  @param vehicle vehicle to be driven
     */
    TaskDrive(Person person, VirtualMars mars, Settlement destinationSettlement, boolean embark,
            boolean disembark, GroundVehicle vehicle) {
        this(person, mars, destinationSettlement, embark, disembark);
        this.vehicle = vehicle;
    }

    /** Constructor to drive a specific vehicle to a specific location (called by another task) 
     *  @param person the person performing the task
     *  @param mars the virtual Mars
     *  @param destinationCoordinates the driving destination location
     *  @param embark true if embarking proceedures should be used
     *  @param disembark true if disembarking proceedures should be used
     *  @param vehicle vehicle to be driven
     */
    TaskDrive(Person person, VirtualMars mars, Coordinates destinationCoordinates, boolean embark,
            boolean disembark, GroundVehicle vehicle) {
        this(person, mars, destinationCoordinates, embark, disembark);
        this.vehicle = vehicle;
    }

    /** Returns the weighted probability that a person might perform this task.
     *  Returns a 5 probability if person is at a settlement with an available vehicle.
     *  Returns a 0 if not.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static int getProbability(Person person, VirtualMars mars) {
        int result = 0;

        if (person.getLocationSituation().equals("In Settlement")) {
            Settlement currentSettlement = person.getSettlement();
            int vehicleNum = currentSettlement.getVehicleNum();
            if (vehicleNum > 0) {
                for (int x = 0; x < vehicleNum; x++)
                    if (!currentSettlement.getVehicle(x).isReserved())
                        result = 5;
            }
        }

        return result;
    }

    /** Performs the driving task for a given number of seconds. 
     *  @param seconds the amount of time the task is performed (in seconds)
     */

    void doTask(int seconds) {
        super.doTask(seconds);
        if (subTask != null)
            return;

        if (phase.equals("")) {
            if (embark)
                phase = new String("Embarking");
            else
                phase = new String("Driving");
        }

        // Perform phases of task
        if (phase.equals("Embarking") && embark)
            seconds -= embark(seconds);
        if (phase.equals("Driving"))
            seconds -= drive(seconds);
        if (phase.equals("Disembarking")) {
            if (disembark)
                seconds -= disembark(seconds);
            else
                isDone = true;
        }
    }

    /** Prepare vehicle for travel
     *  Returns any remaining seconds in action turn
     *  @return any remaining seconds in action turn
     */
    private int embark(int seconds) {
        // If task is finished, return
        if (isDone)
            return 0;

        // Initialize subPhase if necessary
        if (subPhase.equals(""))
            subPhase = new String("Reserve Vehicle");

        // Reserve a vehicle
        if (subPhase.equals("Reserve Vehicle"))
            seconds -= reserveVehicle(seconds);

        // Top off fuel and supplies
        if (subPhase.equals("Prepare Vehicle"))
            seconds -= prepareVehicle(seconds);    
            
        // Determine the destination
        if (subPhase.equals("Determine Destination"))
            seconds -= determineDestination(seconds);    
            
        // Invite passengers
        if (subPhase.equals("Invite Passengers"))
            seconds -= invitePassengers(seconds);

        // Get in vehicle
        if (subPhase.equals("Get In Vehicle"))
            seconds -= getInVehicle(seconds);

        // Set up for Driving phase
        if ((seconds > 0) && !isDone) {
            phase = new String("Driving");
            vehicle.setStatus("Moving");
            subPhase = "";
        }

        return seconds;
    }

    /** Reserve a vehicle so that no one else can take it (10 seconds).
     *  Get unreserved vehicle if at settlement.
     *  If settlement has no free vehicles, end task.
     *  If task already defines a vehicle, move on.
     *  @param seconds the seconds to perform this sub-phase
     *  @return the seconds remaining for task 
     */
    private int reserveVehicle(int seconds) {
        if (vehicle != null) {
            subPhase = new String("Prepare Vehicle");
            return seconds;
        } else if (doSubPhase(seconds, 10)) {
            boolean foundVehicle = false;

            if (embarkingSettlement != null) {
                for (int x = 0; x < embarkingSettlement.getVehicleNum(); x++) {
                    Vehicle tempVehicle = embarkingSettlement.getVehicle(x);
                    MaintenanceGarageFacility garage = (MaintenanceGarageFacility)
                            embarkingSettlement.getFacilityManager().getFacility("Maintenance Garage");
                    if (!tempVehicle.isReserved() && !garage.vehicleInGarage(tempVehicle)) {
                        if (!foundVehicle && GroundVehicle.class.isInstance(tempVehicle)) {
                            vehicle = (GroundVehicle) tempVehicle;
                            vehicle.setReserved(true);
                            foundVehicle = true;
                        }
                    }
                }

                if (foundVehicle)
                    subPhase = new String("Prepare Vehicle");
                else
                    isDone = true;
            } else
                isDone = true;
            return 10;
        } else
            return seconds;
    }

    /** Vehicle preparation checks here. (100 seconds)
     *  Fill vehicle with fuel and supplies from settlement 
     *  storage if applicable.
     *  @param seconds the seconds to perform this sub-phase
     *  @return the seconds remaining for task
     */
    private int prepareVehicle(int seconds) {
        if (doSubPhase(seconds, 100)) {

            // Fill vehicle with fuel and supplies if at a settlement.
            if (embarkingSettlement != null) {
                StoreroomFacility stores =
                        (StoreroomFacility) embarkingSettlement.getFacilityManager().getFacility("Storerooms");
                boolean resourcesAvailable = true;
                
                // Top off fuel
                double neededFuel = vehicle.getFuelCapacity() - vehicle.getFuel();
                if (neededFuel < stores.getFuelStores() - 50D) {
                    stores.removeFuel(neededFuel);
                    vehicle.addFuel(neededFuel);
                }
                else resourcesAvailable = false;
                
                // Top off oxygen
                double neededOxygen = vehicle.getOxygenCapacity() - vehicle.getOxygen();
                if (neededOxygen < stores.getOxygenStores() - 50D) {
                    stores.removeOxygen(neededOxygen);
                    vehicle.addOxygen(neededOxygen);
                }
                else resourcesAvailable = false;
                
                // Top off water
                double neededWater = vehicle.getWaterCapacity() - vehicle.getWater();
                if (neededWater < stores.getWaterStores() - 50D) {
                    stores.removeWater(neededWater);
                    vehicle.addWater(neededWater);
                }
                else resourcesAvailable = false;
                
                // Top off food
                double neededFood = vehicle.getFoodCapacity() - vehicle.getFood();
                if (neededFood < stores.getFoodStores() - 100D) {
                    stores.removeFood(neededFood);
                    vehicle.addFood(neededFood);
                }
                else resourcesAvailable = false;
                
                // If not enough resources in storage, cancel trip.
                if (!resourcesAvailable) {
                    vehicle.setReserved(false);
                    isDone = true;
                    return seconds;
                }
            }

            subPhase = new String("Determine Destination");
            return 100;
        } else
            return seconds;
    }
    
    /** Determine destination if there isn't already one. (0 seconds)
     *  Set the destination of the vehicle.
     *  This phase only chooses settlements as destinations.
     *  Use specific destination constructors to choose a specific settlement or non-settlement location.
     *  @param seconds the seconds to perform this sub-phase
     *  @return the seconds remaining for task
     */
    private int determineDestination(int seconds) {
        if (destinationType.equals("None")) {
            destinationType = new String("Settlement");

            UnitManager manager = person.getUnitManager();

            // Choose a random settlement other than current one.
            // 75% chance of selecting one of the closest three settlements.
            if (embarkingSettlement != null) {
                if (RandomUtil.lessThanRandPercent(75)) {
                    destinationSettlement =
                            manager.getRandomOfThreeClosestSettlements(embarkingSettlement);
                    if (destinationSettlement == null)
                        System.out.println("destinationSettlement is NULL");
                } else
                    destinationSettlement = manager.getRandomSettlement(embarkingSettlement);
            } else {
                if (RandomUtil.lessThanRandPercent(75)) {
                    Coordinates currentLocation = person.getCoordinates();
                    destinationSettlement = manager.getRandomOfThreeClosestSettlements(currentLocation);
                } else
                    destinationSettlement = manager.getRandomSettlement();
            }
        }

        if (destinationType.equals("Settlement")) {
            destinationCoordinates = destinationSettlement.getCoordinates();
            vehicle.setDestinationSettlement(destinationSettlement);
        }
        else if (destinationType.equals("Coordinates")) {
            vehicle.setDestination(destinationCoordinates);
        }

        // Set description
        if (destinationSettlement != null)
            description = "Drive " + vehicle.getName() + " to " + destinationSettlement.getName() + ".";
        else description = "Drive " + vehicle.getName() + " to location.";
        
        // Check how many new residents the settlement can hold
        if (destinationSettlement != null) {
            FacilityManager manager = destinationSettlement.getFacilityManager();
            LivingQuartersFacility quarters = (LivingQuartersFacility) manager.getFacility("Living Quarters");
            int maxPopCapacity = quarters.getMaximumCapacity();
            int currentPop = quarters.getCurrentPopulation();
            destinationSettlementAvailablePop = maxPopCapacity - currentPop;
            
            UnitManager unitManager = person.getUnitManager();
            Vehicle[] vehicles = unitManager.getVehicles();
            for (int x=0; x < vehicles.length; x++) {
                if (vehicles[x] != vehicle) {
                    if (vehicles[x].getDestinationSettlement() == destinationSettlement)
                        destinationSettlementAvailablePop -= vehicles[x].getPassengerNum();
                }
            }
             
            if (destinationSettlementAvailablePop < 1) {
                vehicle.setReserved(false);
                isDone = true;
                return seconds;
            }
        }
        
        subPhase = new String("Invite Passengers");

        return 0;
    }
    
    /** Invite other people in current settlement to come along on the trip. (0 seconds)
     *  If not currently at a settlement, go to next phase;
     *  @param seconds the seconds to perform this sub-phase
     *  @return the seconds remaining for task
     */
    private int invitePassengers(int seconds) {
        if (embarkingSettlement != null) {
            for (int x = 0; x < embarkingSettlement.getPeopleNum(); x++) {
                Person tempPerson = embarkingSettlement.getPerson(x);
                if (tempPerson != person) {
                    Task tempTask = tempPerson.getTaskManager().getCurrentTask();
                    if ((tempTask == null) || tempTask.getName().equals("Relaxing")) {
                        if (RandomUtil.lessThanRandPercent(50)) {
                            if (vehicle.getPassengerNum() < vehicle.getMaxPassengers()) {
                                if (vehicle.getPassengerNum() < destinationSettlementAvailablePop) {
                                    vehicle.addPassenger(tempPerson);
                                    embarkingSettlement.personLeave(tempPerson);
                                    tempPerson.setVehicle(vehicle);
                                    tempPerson.setLocationSituation("In Vehicle");
                                }
                            }
                        }
                    }
                }
            }
        }
        subPhase = new String("Get In Vehicle");

        return 0;
    }

    /** Get in vehicle. (100 seconds)
     *  Last sub-phase of embark phase.
     *  Strap yourself in!
     *  @param seconds the seconds to perform this sub-phase
     *  @return the seconds remaining for task
     */
    private int getInVehicle(int seconds) {
        if (doSubPhase(seconds, 100)) {
            if (!vehicle.isPassenger(person))
                vehicle.addPassenger(person);
            vehicle.setDriver(person);
            if (embarkingSettlement != null) {
                embarkingSettlement.personLeave(person);
                embarkingSettlement.vehicleLeave(vehicle);
            }
            person.setVehicle(vehicle);
            person.setLocationSituation("In Vehicle");

            vehicle.setStatus("Moving");
            vehicle.setSettlement(null);
            vehicle.setReserved(false);
            if (destinationCoordinates == null)
                System.out.println("destinationCoordinates = NULL");
            vehicle.setDestination(destinationCoordinates);
            if (destinationType.equals("Settlement")) {
                vehicle.setDestinationSettlement(destinationSettlement);
                vehicle.setDestinationType("Settlement");
            } else
                vehicle.setDestinationType("Coordinates");

            subPhase = "";
            return 100;
        } else
            return seconds;
    }

    /** Driving phase 
     *  @param seconds the seconds to perform this phase
     *  @return the seconds remaining for task
     */
    private int drive(int seconds) {
        // Initialize if first time driving
        if (subPhase.equals("")) {
            vehicle.setStatus("Moving");
            subPhase = new String("Normal Driving");
            closestDistance = 10000000000D;
            obstacleTimeCount = 0;
            backingUpDistance = 0;
        }

        // If driver has failed to get around an obstacle after 20 turns, mark vehicle as stuck and end task
        if ((obstacleTimeCount >= 20) && !vehicle.isStuck()) {
            vehicle.setStuck(true);
            subPhase = "Winching Stuck Vehicle";
            obstacleTimeCount = 0;
        }

        // Find starting location of vehicle
        startingLocation = vehicle.getCoordinates();

        // Find current direction and update vehicle
        Direction direction = new Direction(0);

        if (subPhase.equals("Normal Driving") || subPhase.equals("Winching Stuck Vehicle"))
            direction = startingLocation.getDirectionToPoint(destinationCoordinates);
        else if (subPhase.equals("Avoiding Obstacle"))
            direction = avoidObstacle();
        else if (subPhase.equals("Backing Up"))
            direction = new Direction(vehicle.getDirection().getDirection() + Math.PI);

        vehicle.setDirection(direction);

        // Determine current elevation and update vehicle
        double elevation = mars.getSurfaceFeatures().getSurfaceTerrain().getElevation(startingLocation);
        vehicle.setElevation(elevation);

        // Determine vehicle's speed in given direction
        double speed = getSpeed(direction);

        // If vehicle is stuck and terrain speed is better than 1kph, return to normal driving.
        // Otherwise, set speed to .2kph as vehicle is being winched along.
        if (subPhase.equals("Winching Stuck Vehicle")) {
            if (speed > 1) {
                subPhase = "Normal Driving";
                vehicle.setStuck(false);
            } else
                speed = .2;
        }

        // Set the vehicle's speed
        vehicle.setSpeed(speed);

        // If speed is less than 1kph and vehicle isn't being winched, go into obstacle avoidance subphase
        if ((speed < 1D) && !subPhase.equals("Winching Stuck Vehicle"))
            subPhase = new String("Avoiding Obstacle");

        // Find starting distance to destination
        double distanceToDestination = startingLocation.getDistance(destinationCoordinates);

        // Determine distance traveled in time given
        double distanceTraveled = seconds * ((speed / 60D) / 60D);

        // Consume fuel for distance traveled.
        vehicle.consumeFuel(distanceTraveled / 100D);

        // Add distance traveled to vehicle's odometer
        vehicle.addTotalDistanceTraveled(distanceTraveled);
        vehicle.addDistanceLastMaintenance(distanceTraveled);

        // If backing up, stop backing up if over 10km.
        if (subPhase.equals("Backing Up")) {
            backingUpDistance += distanceTraveled;
            if (backingUpDistance >= 10D) {
                subPhase = new String("Normal Driving");
                backingUpDistance = 0D;
            }
        }

        // If starting distance to destination is less than distance traveled, stop at destination
        Coordinates endingLocation = null;
        if (distanceToDestination < distanceTraveled) {
            vehicle.setDistanceToDestination(0D);
            endingLocation = destinationCoordinates;
        } else {
            // Determine new position
            double newY = -1D * direction.getCosDirection() * (distanceTraveled / 7.4D);
            double newX = direction.getSinDirection() * (distanceTraveled / 7.4D);
            endingLocation = new Coordinates(startingLocation.convertRectToSpherical(newX, newY));

            // Find ending distance to destination
            distanceToDestination = endingLocation.getDistance(destinationCoordinates);
        }

        // Update vehicle's location
        vehicle.setCoordinates(endingLocation);

        // Update vehicle's distance to destination
        vehicle.setDistanceToDestination(distanceToDestination);

        // Update closest distance to destination
        if (distanceToDestination < closestDistance) {
            closestDistance = distanceToDestination;
            obstacleTimeCount = 0;
        } else
            obstacleTimeCount++;

        // Update every passenger's location
        for (int x = 0; x < vehicle.getPassengerNum(); x++)
            vehicle.getPassenger(x).setCoordinates(endingLocation);

        // Add experience points for driver's 'Driving' skill
        double newPoints = (seconds / 60D) / 60D;
        if (subPhase.equals("Avoiding Obstacle") || subPhase.equals("Backing Up"))
            newPoints *= 4D;
        newPoints += newPoints *
                (((double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude") -
                50D) / 100D);
        person.getSkillManager().addExperience("Driving", newPoints);

        // If vehicle is at destination, end driving phase and prepare for disembarking.
        // Otherwise check for vehicle breakdown.
        if (endingLocation.equals(destinationCoordinates)) {
            phase = new String("Disembarking");
            vehicle.setStatus("Parked");
            vehicle.setSpeed(0D);
            vehicle.setDestinationType("None");
        } else
            checkMechanicalBreakdown();

        return 0;
    }

    /** Determine direction for obstacle avoidance. 
     *  @return the direction for obstacle avoidance (in radians)
     */
    private Direction avoidObstacle() {
        boolean foundGoodPath = false;

        String sideCheck = new String("left");
        if (RandomUtil.lessThanRandPercent(50))
            sideCheck = new String("right");

        Direction resultDirection = vehicle.getDirection();

        for (int x = 0; (x < 5) && !foundGoodPath; x++) {
            double modAngle = (double) x * (Math.PI / 6D);

            if (sideCheck.equals("left"))
                resultDirection.setDirection(resultDirection.getDirection() - modAngle);
            else
                resultDirection.setDirection(resultDirection.getDirection() + modAngle);

            if (getSpeed(resultDirection) > 1D)
                foundGoodPath = true;
        }

        if (foundGoodPath)
            subPhase = new String("Normal Driving");
        else
            subPhase = new String("Backing Up");

        return resultDirection;
    }

    /** Determine vehicle speed for given direction 
     *  @param direction the direction the vehice is traveling in
     *  @return the speed of the vehicle (in km/hr)
     */
    private double getSpeed(Direction direction) {

        // Determine the terrain grade in the vehicle's current direction
        double terrainGrade =
                mars.getSurfaceFeatures().getSurfaceTerrain().determineTerrainDifficulty(startingLocation, direction);
        vehicle.setTerrainGrade(terrainGrade);

        // Get the driver's driving skill
        int skillLevel = 0;
        if (person.getSkillManager().hasSkill("Driving"))
            skillLevel = person.getSkillManager().getSkillLevel("Driving");

        // Get vehicle's terrain handling capability
        double terrainHandlingCapability = vehicle.getTerrainHandlingCapability();

        // Adjust the vehicle's real speed based on terrain grade, vehicle terrain handling capability and the driver's driving skill
        double angleModifier = terrainHandlingCapability + skillLevel;
        if (angleModifier == 0D)
            angleModifier = 1D;
        else if (angleModifier < 0D)
            angleModifier = Math.abs(1D / angleModifier);

        double tempAngle = terrainGrade * (1D / angleModifier) * 15D;
        if (tempAngle > (Math.PI / 2D))
            tempAngle = Math.PI / 2D;

        double speed = vehicle.getBaseSpeed() * Math.cos(tempAngle);
        if (speed < 0D)
            speed = 0D;

        return speed;
    }

    /** Checks for vehicle breakdown to mechanical failure. */
    private void checkMechanicalBreakdown() {
        // Base .1% of breakdown average 1 turn of driving.
        double percentChance = .1D;

        // Modify by total distance vehicle has traveled.
        // Taken out until vehicles can be scrapped and created.
        // Otherwise running the sim for more than a few hours will wear the
        // vehicles down too much. [Scott]
        // double distanceModifier = .1D * (vehicle.getTotalDistanceTraveled() / 10000D);

        // Modify by distance since last maintenance if over 5,000 km.
        double maintenanceModifier = 0D;
        if (vehicle.getDistanceLastMaintenance() > 5000D) {
            maintenanceModifier = .3D * (vehicle.getDistanceLastMaintenance() / 5000D);
        }

        // Modify by driver's skill level.
        int skillLevel = 0;
        if (person.getSkillManager().hasSkill("Driving"))
            skillLevel = person.getSkillManager().getSkillLevel("Driving");
        double skillModifier = .1D * (double) skillLevel;

        // Modify by terrain.
        double terrainModifier = .5D * Math.sin(vehicle.getTerrainGrade());

        // Modify by terrain handling capability of vehicle.
        double terrainHandlingModifier = .1D * vehicle.getTerrainHandlingCapability();

        // Modify by what phase the driving task is in.
        double subPhaseModifier = 0;
        if (subPhase.equals("Avoiding Obstacle"))
            subPhaseModifier = .1D;
        if (subPhase.equals("Backing Up"))
            subPhaseModifier = .2D;
        if (subPhase.equals("Winching Stuck Vehicle"))
            subPhaseModifier = .3D;

        // Total up modifications with base chance of failure.
        //percentChance += distanceModifier + maintenanceModifier - skillModifier + terrainModifier -
        //        terrainHandlingModifier + subPhaseModifier;
        percentChance += maintenanceModifier - skillModifier + terrainModifier - terrainHandlingModifier + 
                subPhaseModifier;

        // Determine if failure happens and, if so, have the crew fix the failure.
        if ((Math.random() * 100D) <= percentChance) {
            vehicle.setStatus("Broken Down");
            vehicle.setSpeed(0);
            vehicle.newMechanicalFailure();

            for (int x = 0; x < vehicle.getPassengerNum(); x++) {
                Person crewmember = vehicle.getPassenger(x);
                crewmember.getTaskManager().addSubTask(
                        new TaskMechanic(crewmember, mars, vehicle.getMechanicalFailure()));
            }
            // System.out.println(vehicle.getName() + " failure chance: " + percentChance);
        }
    }

    /** This subphase allows all the passengers to exit and returns the vehicle to parked status. (100 seconds) 
     *  @param seconds the seconds to perform this sub-phase
     *  @return the seconds remaining for task
     */
    private int disembark(int seconds) {
        subPhase = "";
        if (doSubPhase(seconds, 100)) {
            while (vehicle.getPassengerNum() > 0) {
                Person tempPassenger = vehicle.getPassenger(0);
                vehicle.removePassenger(tempPassenger);
                if (destinationType.equals("Settlement")) {
                    tempPassenger.setLocationSituation("In Settlement");
                    tempPassenger.setSettlement(destinationSettlement);
                } else
                    tempPassenger.setLocationSituation("Outside");
            }

            if (destinationType.equals("Settlement")) {
                vehicle.setSettlement(destinationSettlement);
                vehicle.setDestinationSettlement(null);
            }

            isDone = true;
            return 100;
        } else
            return seconds;
    }
}
