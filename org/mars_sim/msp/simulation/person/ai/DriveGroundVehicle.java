/**
 * Mars Simulation Project
 * DriveGroundVehicle.java
 * @version 2.74 2002-04-30
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.io.Serializable;

/**
 *  The Drive Ground Vehicle class is a task for driving a ground vehicle to a destination.
 */
public class DriveGroundVehicle extends Task implements Serializable {

    /**
     * Driving phase.
     */
    public static final String DRIVING = "Driving";

    /**
     * Avoiding obstacle phase.
     */
    public final static String AVOID_OBSTACLE = "Avoiding Obstacle";

    /**
     * Back up  vehicle phase.
     */
    public final static String BACKUP = "Backup Vehicle";

    /**
     * Winch stuck vehicle phase.
     */
    public final static String WINCH_VEHICLE = "Winching Stuck Vehicle";

    // Data members
    private GroundVehicle vehicle; // Vehicle person is driving.
    private Coordinates destination; // Destination coordinates.
    private double distanceToDestination; // Current distance to destination.
    private double closestDistance; // Closest distance to destination vehicle has been so far.
    private double obstacleTimeCount; // Amount of time driver has not been any closer to destination. (in millisols)
    private double backingUpDistance; // Distance vehicle has backed up to avoid an obstacle.
    private Coordinates startingLocation; // Current location of vehicle.
    private boolean backingUp; // True if vehicle is backing up to avoid an obstacle.
    private double speedSkillModifier;  // Skill modifier to vehicle speed.
    private MarsClock startTime; // Starting date/time of the trip.
    private double startDistance; // Starting distance to destination of the trip.
    private double duration;  // Duration of the driving task.

    /** Constructs a DriveGroundVehicle object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param vehicle the vehicle to be driven
     *  @param destination location to be driven to
     *  @param startTripTime the starting time of the trip
     *  @param startTripDistance the starting distance to destination for the trip
     */
    public DriveGroundVehicle(Person person, Mars mars, GroundVehicle vehicle,
            Coordinates destination, MarsClock startTripTime, double startTripDistance) {
        super("Driving vehicle", person, true, mars);

        // Set initial parameters
	description = DRIVING + " " + vehicle.getName();
        this.vehicle = vehicle;
        this.destination = destination;
        vehicle.setDestination(destination);
        closestDistance = Double.MAX_VALUE;
        obstacleTimeCount = 0D;
        backingUpDistance = 0D;
        phase = DRIVING;
        backingUp = false;
        startTime = startTripTime;
        startDistance = startTripDistance;

        // Determine duration (from 200 to 300 millisols)
        duration = 200D + RandomUtil.getRandomDouble(100D);

        // System.out.println(person.getName() + " is driving " + vehicle.getName());
    }

    /** Perform the driving task
     *  @param time amount of time to perform the task (in millisols)
     *  @return time remaining after finishing with task (in millisols
     */
    double performTask(double time) {

        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // Set person as driver.
	vehicle.setDriver(person);
	
        // If person is incompacitated, end task.
	if (person.getPerformanceRating() == 0D) done = true;
	
        // If night time, end task.
        if (mars.getSurfaceFeatures().getSurfaceSunlight(vehicle.getCoordinates()) == 0D) {
            // System.out.println(person.getName() + " stopped driving due to darkness.");
	    vehicle.setSpeed(0D);
            vehicle.setDriver(null);
	    vehicle.setStuck(false);
            done = true;
            return 0D;
        }

        // If vehicle has malfunction, end task.
	if (vehicle.getMalfunctionManager().hasMalfunction()) done = true;
	
	// Perform phases of task until time is up or task is done.
        while ((timeLeft > 0D) && !done) {
            if (phase.equals(DRIVING)) timeLeft = drivingPhase(timeLeft);
            else if (phase.equals(AVOID_OBSTACLE)) timeLeft = obstaclePhase(timeLeft);
            else if (phase.equals(WINCH_VEHICLE)) timeLeft = winchingPhase(timeLeft);
        }

        // Keep track of the duration of the task.
        timeCompleted += time;
        if (done || (timeCompleted >= duration)) {
	    // System.out.println(person.getName() + " finished driving " + vehicle.getName());
	    vehicle.setSpeed(0D);
            vehicle.setDriver(null);
	    vehicle.setStuck(false);
            done = true;
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
            phase = AVOID_OBSTACLE;
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

        // Check for accident.
        if (!done) checkForAccident(timeUsed);

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
            phase = WINCH_VEHICLE;
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
        else obstacleTimeCount += timeUsed;

        // Add experience points for driver's 'Driving' skill.
        double newPoints = time / 100D;
        newPoints *= 4D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        person.getSkillManager().addExperience("Driving", newPoints);

        // Check for accident.
        if (!done) checkForAccident(timeUsed);

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
            phase = DRIVING;
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

        // Check for accident.
        if (!done) checkForAccident(timeUsed);

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
        SimulationProperties properties = mars.getSimulationProperties();
        double fuelConsumed = distanceTraveled / properties.getRoverFuelEfficiency();
        vehicle.getInventory().removeResource(Inventory.FUEL, fuelConsumed);
	
        // Add distance traveled to vehicle's odometer.
        vehicle.addTotalDistanceTraveled(distanceTraveled);
        vehicle.addDistanceLastMaintenance(distanceTraveled);

        // If backing up, add distanceTraveled to backingUpDistance
        if (phase.equals(BACKUP)) backingUpDistance += distanceTraveled;

        double result = 0;

        // If starting distance to destination is less than distance traveled, stop at destination.
        if (distanceToDestination < distanceTraveled) {
            distanceTraveled = vehicle.getDistanceToDestination();
            distanceToDestination = 0D;
            vehicle.setDistanceToDestination(distanceToDestination);
            vehicle.setCoordinates(destination);
            vehicle.setSpeed(0D);
            vehicle.setDriver(null);
            done = true;
            result = time - MarsClock.convertSecondsToMillisols(distanceTraveled / vehicle.getSpeed() * 60D * 60D);
            // System.out.println(vehicle.getName() + " reached destination.");
        }
        else {
            // Determine new position.
            vehicle.setCoordinates(startingLocation.getNewLocation(vehicle.getDirection(), distanceTraveled));

            // Update distance to destination.
            distanceToDestination = vehicle.getCoordinates().getDistance(destination);
            vehicle.setDistanceToDestination(distanceToDestination);
            result = 0D;
        }

        // Update vehicle's ETA
        vehicle.setETA(getETA());

        return result;
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

        if (foundGoodPath) phase = DRIVING;
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
        int skillLevel = person.getSkillManager().getEffectiveSkillLevel("Driving");

        // Get vehicle's terrain handling capability.
        double handling = vehicle.getTerrainHandlingCapability();

        // Determine temp angle.
        double angleModifier = handling + skillLevel - 10D;
        if (angleModifier < 0D) angleModifier = Math.abs(1D / angleModifier);
        double tempAngle = terrainGrade / angleModifier;
        if (tempAngle > (Math.PI / 2D)) tempAngle = Math.PI / 2D;

        // Determine skill modifier based on driver's skill level.
        speedSkillModifier = 0D;
        double baseSpeed = vehicle.getBaseSpeed();
        if (skillLevel <= 5) speedSkillModifier = 0D - ((baseSpeed / 2D) * ((5D - skillLevel) / 5D));
        else if (skillLevel > 5) {
            double tempSpeed = baseSpeed;
            for (int x=0; x < skillLevel - 5; x++) {
                tempSpeed /= 2D;
                speedSkillModifier += tempSpeed;
            }
        }

        // Determine light condition modifier based on available sunlight
        double lightModifier = mars.getSurfaceFeatures().getSurfaceSunlight(vehicle.getCoordinates());
        lightModifier = ((lightModifier / 127D) * .9D) + .1D;

        double speed = (vehicle.getBaseSpeed() + speedSkillModifier) * Math.cos(tempAngle) * lightModifier;
        if (speed < 0D) speed = 0D;

        return speed;
    }

    /**
     * Check if vehicle has had an accident.
     * @param time the amount of time vehicle is driven (millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

	// Driver skill modification.
	int skill = person.getSkillManager().getEffectiveSkillLevel("Driving");
	if (skill <= 3) chance *= (4 - skill);
	else chance /= (skill - 2);
	
        // Get task phase modification.
        if (phase.equals(AVOID_OBSTACLE)) chance *= 1.2D;
        if (phase.equals(WINCH_VEHICLE)) chance *= 1.3D;

	// Terrain modification.
	chance *= (1D + Math.sin(vehicle.getTerrainGrade()));

        // Vehicle handling modification.
        chance /= (1D + vehicle.getTerrainHandlingCapability());

        // Light condition modification.
        double lightModifier = mars.getSurfaceFeatures().getSurfaceSunlight(vehicle.getCoordinates());
        chance *= ((5D * (127D - lightModifier) / 127D) + 1D);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident driving " + vehicle.getName());
	    vehicle.getMalfunctionManager().accident();
	}
    }
    
    /** Determines the ETA (Estimated Time of Arrival) to the destination.
     *  @return MarsClock instance of date/time for ETA
     */
    private MarsClock getETA() {
        MarsClock currentTime = mars.getMasterClock().getMarsClock();

        // Determine time difference from start of trip in millisols.
        double millisolsDiff = MarsClock.getTimeDiff(currentTime, startTime);
        double hoursDiff = MarsClock.convertMillisolsToSeconds(millisolsDiff) / 60D / 60D;

        // Determine average speed so far in km/hr.
        double avgSpeed = (startDistance - distanceToDestination) / hoursDiff;

        // Determine estimated speed in km/hr.
        double estimatorConstant = .5D;
        double estimatedSpeed = estimatorConstant * (vehicle.getBaseSpeed() + speedSkillModifier);

        // Determine final estimated speed in km/hr.
        double tempAvgSpeed = avgSpeed * ((startDistance - distanceToDestination) / startDistance);
        double tempEstimatedSpeed = estimatedSpeed * (distanceToDestination / startDistance);
        double finalEstimatedSpeed = tempAvgSpeed + tempEstimatedSpeed;

        // Determine time to destination in millisols.
        double hoursToDestination = distanceToDestination / finalEstimatedSpeed;
        double millisolsToDestination = MarsClock.convertSecondsToMillisols(hoursToDestination * 60D * 60D);

        // Determine ETA
        MarsClock eta = (MarsClock) currentTime.clone();
        eta.addTime(millisolsToDestination);

        return eta;
    }
}
