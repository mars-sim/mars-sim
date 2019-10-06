/**
 * Mars Simulation Project
 * SystemCondition.java
 * @version 3.1.0 2018-10-07
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class represents the System Condition of a robot.
 * It models a robot's health.
 */
public class SystemCondition implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(SystemCondition.class.getName());

    /** Sleep Habit maximum value. */
    private static int MAX_WEIGHT = 30;
    /** Sleep Habit Map resolution. */
    //private static int SLEEP_MAP_RESOLUTION = 20;

    private static double INFLATION = 1.15;

	private int solCache = 0;

    /** Life support minimum value. */
    private static int MIN_VALUE = 0;
    /** Life support maximum value. */
    private static int MAX_VALUE = 1;

    /** Stress jump resulting from being in an accident. */
    //public static final double ACCIDENT_STRESS = 40D;

    public static final double FOOD_RESERVE_FACTOR = 1.5D;
    
    // Each meal has 0.1550 kg and has 2525 kJ. Thus each 1 kg has 16290.323 kJ
    public static double FUEL_COMPOSITION_ENERGY_RATIO = 16290.323;
    //public static int MAX_KJ = 16290; //  1kg of food has ~16290 kJ (see notes on people.xml under <food-consumption-rate value="0.62" />)

    public static double ENERGY_FACTOR = 0.8D;

    // Data members
    /** Robot's fatigue level. */
    private double mechanicalFatigue;
    /** Robot's power discharge level. */
    private double powerDischarge;
    /** Robot's stress level (0.0 - 100.0). */
    private double systemLoad;
    /** Performance factor. */
    private double performance;
    // 2015-01-12 Energy level
    private double kJoules;
    private double robotBatteryDrainTime;

    private boolean operable;
    private boolean isLowPowerMode;
    private boolean isBatteryDepleting;

    private Robot robot;
    
	private static MarsClock marsClock;


    /**
     * Constructor 2.
     * @param robot The robot requiring a physical presence.
     */
    public SystemCondition(Robot newRobot) {
    	marsClock = Simulation.instance().getMasterClock().getMarsClock();
        robot = newRobot;
        performance = 1.0D;
        powerDischarge = RandomUtil.getRandomDouble(400D);
        operable = true;

        RobotConfig robotConfig = SimulationConfig.instance().getRobotConfiguration();

        try {
        	robotBatteryDrainTime = robotConfig.getLowPowerModeStartTime() * 1000D;
            //System.out.println("robotBatteryDrainTime : "+ Math.round(robotBatteryDrainTime*10.0)/10.0);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Gets the medical manager.
     * @return medical manager.
     */
    private MedicalManager getMedicalManager() {
        return Simulation.instance().getMedicalManager();
    }

    /**
     * This timePassing method 2 reflect a passing of time for robots.

     * @param time amount of time passing (in millisols)
     * @param support life support system.
     * @param config robot configuration.
     * @return True still alive.
     */
    public boolean timePassing(double time, RobotConfig config) {

    	//1. Check malfunction
        //performSystemCheck();
 
        // 2. Consume a minute amount of energy even if a robot does not perform any tasks
        reduceEnergy(time);
    
        // 3. Calculate performance
        recalculate();
	
        return (operable);
    }


    /**
     * Robot consumes given amount of power
     * @param amount amount of power to consume (in kJ).
     * @param container unit to get power from
     * @throws Exception if error consuming power.
     */
    public void consumePower(double amount, Unit container) {
        if (container == null) throw new IllegalArgumentException("container is null");
    }


    /**
     * Consumes given amount of fuel (e.g. Methane
     * @param support Life support system providing water.
     * @param amount amount of methane to consume (in kg)
     * @return new problem added.
     * @throws Exception if error consuming methane
     */
    private boolean consumeFuel(LifeSupportInterface support, double amount) {
    	return true;
    }


    public void checkDischarged(double hunger) {
        // TODO: need a different method and different terminology to account for the drain on the robot's battery

    }
        
    /**
     * This method checks the consume values of a resource. If the
     * actual is less than the required then a HealthProblem is
     * generated. If the required amount is satisfied, then any problem
     * is recovered.
     *
     * @param actual The amount of resource provided.
     * @param require The amount of resource required.
     * @param complaint Problem associated to this resource.
     * @return Has a new problem been added.
     */
    private boolean checkResourceConsumption(double actual, double required,
            int bounds, Complaint complaint) {

        boolean newProblem = false;
        if ((bounds == MIN_VALUE) && (actual < required)) newProblem = true;
        if ((bounds == MAX_VALUE) && (actual > required)) newProblem = true;

        if (newProblem) {
            //addMedicalComplaint(complaint);
            //person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
        }
        else {
            //Is the person suffering from the illness, if so recovery
            // as the amount has been provided
            //HealthProblem illness = problems.get(complaint);
            //if (illness != null) {
            //    illness.startRecovery();
                //person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
            //}
        }
        return newProblem;
    }

    /**
     * Person requires minimum air pressure.
     * @param support Life support system providing air pressure.
     * @param pressure minimum air pressure person requires (in Pa)
     * @return new problem added.
     */
    private boolean requireAirPressure(LifeSupportInterface support, double pressure) {
        return checkResourceConsumption(support.getAirPressure(), pressure,
                MIN_VALUE, getMedicalManager().getDecompression());
    }

    /**
     * Person requires minimum temperature.
     * @param support Life support system providing temperature.
     * @param temperature minimum temperature person requires (in degrees Celsius)
     * @return new problem added.
     */
    private boolean requireTemperature(LifeSupportInterface support, double minTemperature,
            double maxTemperature) {

        boolean freeze = checkResourceConsumption(support.getTemperature(),
                minTemperature, MIN_VALUE, getMedicalManager().getFreezing());
        boolean hot = checkResourceConsumption(support.getTemperature(),
                maxTemperature, MAX_VALUE, getMedicalManager().getHeatStroke());
        return freeze || hot;
    }

    /**
     * Get the details of this robot's death.
     * @return Detail of the death, will be null if person is still alive.
     
    public DeathInfo getDeathDetails() {
        return deathDetails;
    }
*/
    
    /**
     * Gets the robot's energy level.
     * @return robot's energy level in kilojoules
     */
    public double getEnergy() {
        return kJoules;
    }

    /** Reduces the robot's caloric energy.
     *  @param time the amount of time (millisols).
     */
    public void reduceEnergy(double time) {
        double dailyEnergyIntake = 10100D;

        double xdelta = (time / 1000D) * dailyEnergyIntake;

        kJoules -= xdelta;

        if (kJoules < 100D) {
            // 100 kJ is the lowest possible energy level
        	kJoules = 100D;
        }

        //System.out.println("PhysicalCondition : ReduceEnergy() : kJ is " + Math.round(kJoules*100.0)/100.0);
    }


    /** Sets the robot's energy level
     *  @param kilojoules
     */
    public void setEnergy(double kJ) {
        kJoules = kJ;
        //System.out.println("PhysicalCondition : SetEnergy() : " + Math.round(kJoules*100.0)/100.0 + " kJoules");
    }

    /** Adds to the robot's energy intake by eating
     *  @param robot's energy level in kilojoules
     */
    public void addEnergy(double amount) {
    	// TODO: vary MAX_KJ according to the individual's physical profile strength, endurance, etc..
         double xdelta = amount * FUEL_COMPOSITION_ENERGY_RATIO;
        kJoules += xdelta;

        double dailyEnergyIntake = 10100D;
        if (kJoules > dailyEnergyIntake) {
        	kJoules = dailyEnergyIntake;
        }
        //System.out.println("PhysicalCondition : addEnergy() : " + Math.round(kJoules*100.0)/100.0 + " kJoules");
    }

    /**
     * Get the performance factor that effect Person with the complaint.
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceFactor() {
        return performance;
    }

    /**
     * Sets the performance factor.
     * @param newPerformance new performance (between 0 and 1).
     */
    private void setPerformanceFactor(double newPerformance) {
        if (newPerformance != performance) {
            performance = newPerformance;
			if (robot != null)
				robot.fireUnitUpdate(UnitEventType.PERFORMANCE_EVENT);

        }
    }


    /**
     * Define the fatigue setting for this person
     * @param newFatigue New fatigue.
     */
    public void setFatigue(double newFatigue) {
        if (mechanicalFatigue != newFatigue) {
            mechanicalFatigue = newFatigue;
			if (robot != null)
				robot.fireUnitUpdate(UnitEventType.FATIGUE_EVENT);

        }
    }

    /** Gets the robot's discharge amount 
     *  @return robot's power discharge amount 
     */
    public double getPowerDischarge() {
        return powerDischarge;
    }


    /**
     * Define the robot's power level 
     * @param robot's power level .
     */
    public void setBatteryPower(double value) {
        if (powerDischarge != 100D-value) {
            powerDischarge = 100D-value;
        }
    }


    /**
     * Gets the robot system stress level
     * @return stress (0.0 to 100.0)
     */
    public double getStress() {
        return systemLoad;
    }


    /**
     * This Person is now dead.
     * @param illness The compliant that makes person dead.
     */
    public void setInoperable(HealthProblem illness) {
        setBatteryPower(0D);
        setPerformanceFactor(0D);
        operable = false;
    }

    /**
     * Checks if the robot is inoperable.
     *
     * @return true if inoperable
     */
    public boolean isInoperable() {
        return !operable;
    }

    /**
     * Checks if the robot is on low power mode.
     *
     * @return true if starving
     */
    public boolean isOnLowBatteryMode() {
        return isLowPowerMode;
    }

    /**
     * Checks if the robot's battery is nearly depleted.
     *
     * @return true if nearly depleted
     */
    public boolean isBatteryDepleting() {
        return isBatteryDepleting;
    }

    /**
     * Calculate the most serious problem and the robot's performance.
     */
    private void recalculate() {

        double tempPerformance = 1.0D;
        
        if (kJoules < 200D) {
        	tempPerformance = kJoules / 200D;
        }


        if (tempPerformance < 0D) {
            tempPerformance = 0D;
        }

        setPerformanceFactor(tempPerformance);
    }


    /**
     * Gets the power consumption rate per Sol.
     * @return power consumed (kJ/Sol)
     * @throws Exception if error in configuration.
     */
    public static double getPowerConsumptionRate() {
        RobotConfig config = SimulationConfig.instance().getRobotConfiguration();
        return config.getPowerConsumptionRate();
    }

    /**
     * Gets the fuel consumption rate per Sol.
     * @return fuelconsumed (kJ/Sol)
     * @throws Exception if error in configuration.
     */
    public static double getFuelConsumptionRate() {
        RobotConfig config = SimulationConfig.instance().getRobotConfiguration();
        return config.getFuelConsumptionRate();
    }

    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        robot = null;
    }
}
