/**
 * Mars Simulation Project
 * DriveGroundVehicle.java
 * @version 2.72 2001-07-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** 
 *  The Drive Ground Vehicle class is a task for driving a ground vehicle to a destination.
 */
class DriveGroundVehicle extends Task {

    // Data members
    private GroundVehicle vehicle; // Vehicle person is driving.
    private Coordinates destination; // Destination coordinates.
    private double distanceToDestination; // Current distance to destination.
    private double closestDistance; // Closest distance to destination vehicle has been so far. 
    private double obstacleTimeCount; // Amount of time driver has not been any closer to destination. (in millisols)
    private double backingUpDistance; // Distance vehicle has backed up to avoid an obstacle.
    private Coordinates startingLocation; // Current location of vehicle.
    private boolean backingUp; // True if vehicle is backing up to avoid an obstacle.

    /** Constructs a DriveGroundVehicle object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param vehicle the vehicle to be driven
     *  @param destination location to be driven to
     */
    public DriveGroundVehicle(Person person, VirtualMars mars, GroundVehicle vehicle, Coordinates destination) {
        super("Driving " + vehicle.getName(), person, mars);

        // Set initial parameters
        this.vehicle = vehicle; 
        this.destination = new Coordinates(destination);
        closestDistance = Double.MAX_VALUE;
        obstacleTimeCount = 0D;
        backingUpDistance = 0D;
        phase = "Driving";
        vehicle.setStatus("Moving");
        backingUp = false;
    }

    /** Perform the driving task 
     *  @param time amount of time to perform the task (in millisols)
     *  @return time remaining after finishing with task (in millisols
     */
    double doTask(double time) {
        double timeLeft = super.doTask(time);
        if (subTask != null) return timeLeft;
            
        while ((timeLeft > 0D) && !isDone) {
            if (phase.equals("Driving")) timeLeft = drivingPhase(timeLeft);
            else if (phase.equals("Avoiding Obstacle")) timeLeft = obstaclePhase(timeLeft);
            else if (phase.equals("Winching Stuck Vehicle")) timeLeft = winchingPhase(timeLeft);
        }
    
        return timeLeft;
    }

    /** Perform task in normal driving phase.
     *  @param time the amount of time to perform the task (in millisols)
     *  @return time the amount of time remaining after performing phase (in millisols)
     */
    private double drivingPhase(double time) {

        double timeUsed = 0D;

        // Find current direction and update vehicle.
        startingLocation = vehicle.getCoordinates();
        vehicle.setDirection(startingLocation.getDirectionToPoint(destination));
    
        // Update vehicle elevation.
        vehicle.setElevation(getVehicleElevation());
   
        // Update vehicle speed.
        double speed = getSpeed(vehicle.getDirection());
        vehicle.setSpeed(speed);

        // If speed is less the 1 kph, change to avoiding obstacle phase.
        if (speed < 1D) {
            phase = "Avoiding Obstacle";
            return(time);
        }
 
        // Drive vehicle
        timeUsed = time - drive(time);

        // Add experience points for driver's 'Driving' skill.
        // Add one point for every 100 millisols.
        double newPoints = time / 100D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        person.getSkillManager().addExperience("Driving", newPoints);

        // Check for mechanical breakdown.
        checkMechanicalBreakdown(timeUsed);

        return time - timeUsed;
    }
 
    /** Perform task in obstace phase.
     *  @param time the amount of time to perform the task (in millisols) 
     *  @return time remaining after performing phase (in millisols) 
     */
    private double obstaclePhase(double time) {

        double timeUsed = 0D;

        // If driver has failed to get around an obstacle after 100 millisols, 
        // vehicle should be considered stuck and needs to be winched free.
        if ((obstacleTimeCount >= 100D) && !vehicle.isStuck()) {
            vehicle.setStuck(true);
            phase = "Winching Stuck Vehicle";
            obstacleTimeCount = 0D;
            backingUp = false;
            backingUpDistance = 0D;
            return time;
        }

        // If having backup 10 km or more, revert to normal obstacle avoidance.
        if (backingUp && (backingUpDistance >= 10D)) {
            backingUpDistance = 0D;
            backingUp = false;
        }

        // Update vehicle direction.
        if (backingUp) {
            startingLocation = vehicle.getCoordinates();
            double reverseDirection = startingLocation.getDirectionToPoint(destination).getDirection() + Math.PI;
            vehicle.setDirection(new Direction(reverseDirection));
        }
        else vehicle.setDirection(getObstacleAvoidanceDirection());

        // Update vehicle elevation.
        vehicle.setElevation(getVehicleElevation());

        // Update vehicle speed.
        if (backingUp) vehicle.setSpeed(getSpeed(vehicle.getDirection()) / 2D);
        else vehicle.setSpeed(getSpeed(vehicle.getDirection()));

        // Drive in the direction
        timeUsed = time - drive(time);

        // Update closest distance to destination.
        if (distanceToDestination < closestDistance) {
            closestDistance = distanceToDestination;
            obstacleTimeCount = 0;
        }
        else obstacleTimeCount++;

        // Add experience points for driver's 'Driving' skill.
        double newPoints = time / 100D;
        newPoints *= 4D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        person.getSkillManager().addExperience("Driving", newPoints);

        // Check for mechanical breakdown.
        checkMechanicalBreakdown(timeUsed);

        return time - timeUsed; 
    }

    /** Perform task in winching phase.
     *  @param time the amount of time to perform the phase. 
     *  @return time remaining after performing the phase. 
     */
    private double winchingPhase(double time) {

        double timeUsed = 0D;

        // Find current direction and update vehicle.
        startingLocation = vehicle.getCoordinates();
        vehicle.setDirection(startingLocation.getDirectionToPoint(destination));

        // Update vehicle elevation.
        vehicle.setElevation(getVehicleElevation());

        // If speed given the terrain would be better than 1kph, return to normal driving.
        // Otherwise, set speed to .2kph for winching speed.
        if (getSpeed(vehicle.getDirection()) > 1D) {
            phase = "Normal Driving";
            vehicle.setStuck(false);
            return(time);
        } 
        else vehicle.setSpeed(.2D);

        // Drive in the direction
        timeUsed = time - drive(time);

        // Add experience points for driver's 'Driving' skill.
        double newPoints = time / 100D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        person.getSkillManager().addExperience("Driving", newPoints);

        // Check for mechanical breakdown.
        checkMechanicalBreakdown(timeUsed);

        return time - timeUsed;
    }

    /** Drive vehicle in current driving speed.
     *  Stop if reaching destination.
     *
     *  @param time the amount if time vehicle is driven 
     *  @return time remaining after driving complete.
     */
    private double drive(double time) {

        // Find starting distance to destination.
        distanceToDestination = startingLocation.getDistance(destination);

        // Determine distance traveled in time given.
        double secondsTime = MarsClock.convertMillisolsToSeconds(time);
        double distanceTraveled = secondsTime * ((vehicle.getSpeed() / 60D) / 60D);

        // Consume fuel for distance traveled.
        vehicle.consumeFuel(distanceTraveled / 100D);

        // Add distance traveled to vehicle's odometer.
        vehicle.addTotalDistanceTraveled(distanceTraveled);
        vehicle.addDistanceLastMaintenance(distanceTraveled);

        // If backing up, add distanceTraveled to backingUpDistance 
        if (phase.equals("Backing Up")) backingUpDistance += distanceTraveled;

        // If starting distance to destination is less than distance traveled, stop at destination.
        if (distanceToDestination < distanceTraveled) {
            distanceTraveled = vehicle.getDistanceToDestination();
            vehicle.setDistanceToDestination(0D);
            vehicle.setCoordinates(destination);
            isDone = true;
            return time - MarsClock.convertSecondsToMillisols(distanceTraveled / vehicle.getSpeed() * 60D * 60D);
        }
        else {
            // Determine new position.
            double newY = -1D * vehicle.getDirection().getCosDirection() * (distanceTraveled / 7.4D);
            double newX = vehicle.getDirection().getSinDirection() * (distanceTraveled / 7.4D);
            vehicle.setCoordinates(startingLocation.convertRectToSpherical(newX, newY));
            
            // Update distance to destination.
            vehicle.setDistanceToDestination(vehicle.getCoordinates().getDistance(destination));
        }

        // Update every passenger's location.
        for (int x = 0; x < vehicle.getPassengerNum(); x++)
            vehicle.getPassenger(x).setCoordinates(vehicle.getCoordinates());

        return 0D;
    }

    /** Returns the elevation at the vehicle's position. 
     *  @return elevation in km.
     */
    private double getVehicleElevation() {
        return mars.getSurfaceFeatures().getSurfaceTerrain().getElevation(startingLocation);
    }

    /** Determine direction for obstacle avoidance.
     *  @return direction for obstacle avoidance in radians
     */
    private Direction getObstacleAvoidanceDirection() {
        boolean foundGoodPath = false;

        String sideCheck = "left";
        if (RandomUtil.lessThanRandPercent(50)) sideCheck = "right";

        Direction resultDirection = vehicle.getDirection();

        for (int x=0; (x < 5) && !foundGoodPath; x++) {
            double modAngle = (double) x * (Math.PI / 6D);

            if (sideCheck.equals("left"))
                resultDirection.setDirection(resultDirection.getDirection() - modAngle);
            else
                resultDirection.setDirection(resultDirection.getDirection() + modAngle);

            if (getSpeed(resultDirection) > 1D) foundGoodPath = true;
        }

        if (foundGoodPath) phase = "Driving";
        else backingUp = true; 

        return resultDirection;
    }  

    /** Determine vehicle speed for a given direction.
     *  @param direction the direction of travel
     *  @return speed in km/hr
     */
    private double getSpeed(Direction direction) {
        
        // Determine the terrain grade in the vehicle's current direction.
        TerrainElevation terrain = mars.getSurfaceFeatures().getSurfaceTerrain();
        double terrainGrade = terrain.determineTerrainDifficulty(startingLocation, direction);
        vehicle.setTerrainGrade(terrainGrade);

        // Get the driver's driving skill.
        int skillLevel = person.getSkillManager().getSkillLevel("Driving");

        // Get vehicle's terrain handling capability.
        double handling = vehicle.getTerrainHandlingCapability();

        // Determine temp angle.
        double angleModifier = handling + skillLevel - 4D;
        if (angleModifier < 0D) angleModifier = Math.abs(1D / angleModifier);
        double tempAngle = terrainGrade / angleModifier;
        if (tempAngle > (Math.PI / 2D)) tempAngle = Math.PI / 2D;

        // Modify base speed by driver's skill level.
        double skillModifier = 0D;
        double baseSpeed = vehicle.getBaseSpeed();
        if (skillLevel < 5) skillModifier = 0D - ((baseSpeed / 2D) * ((double) skillLevel / 4D));
        else if (skillLevel > 5) {
            double tempSpeed = baseSpeed;
            for (int x=0; x < skillLevel - 5; x++) {
                tempSpeed /= 2D; 
                skillModifier += tempSpeed;
            }
        }   

        double speed = vehicle.getBaseSpeed() * Math.cos(tempAngle);
        if (speed < 0D) speed = 0D;

        return speed;
    }

    /** Checks for vehicle breakdown to mechanical failure. 
     *  @param time the amount of time vehicle is driven
     */
    private void checkMechanicalBreakdown(double time) {
        // Base 1% of breakdown per 100 millisols of driving.
        double percentChance = time / 100D; 

        // Modify by total mileage on vehicle.
        // Taken out until vehicle scrapping is implemented.
        // double mileageModifier - .1D * (vehicle.getTotalDistanceTraveled() / 10000D);

        // Modify by distance since last maintenance if over 5,000 km.
        double maintenanceModifier = 0D;
        if (vehicle.getDistanceLastMaintenance() > 5000D) 
            maintenanceModifier = 3D * (vehicle.getDistanceLastMaintenance() / 5000D);
        
        // Modify by driver's skill level.
        int skillLevel = person.getSkillManager().getSkillLevel("Driving");
        double skillModifier = .1D * (double) skillLevel;

        // Modify by terrain.
        double terrainModifier = .5D * Math.sin(vehicle.getTerrainGrade());

        // Modifier by terrain handling capability of vehicle.
        double handlingModifier = .1D * vehicle.getTerrainHandlingCapability();

        // Modify by the current phase of the driving task.
        double phaseModifier = 0D;
        if (phase.equals("Avoiding Obstacle")) {
            if (backingUp) phaseModifier = .2D;
            else phaseModifier = .1D;
        }
        else if (phase.equals("Winching Stuck Vehicle")) phaseModifier = .3D;

        // Add modifiers to base chance.
        percentChance += maintenanceModifier - skillModifier + terrainModifier - handlingModifier + phaseModifier;

        // Determine if failure happens and, if so, have the crew repair the failure.
        if (RandomUtil.lessThanRandPercent(percentChance)) {
            vehicle.setStatus("Broken Down");
            vehicle.setSpeed(0);
            vehicle.newMechanicalFailure();

            for (int x=0; x < vehicle.getPassengerNum(); x++) {
                Person crewmember = vehicle.getPassenger(x);
                Task mechanicTask = new RepairMechanicalFailure(crewmember, mars, vehicle.getMechanicalFailure());
                crewmember.getTaskManager().addSubTask(mechanicTask);
            }
        }
    }
}
