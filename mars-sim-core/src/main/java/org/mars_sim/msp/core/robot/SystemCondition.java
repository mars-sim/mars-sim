/**
 * Mars Simulation Project
 * SystemCondition.java
 * @version 3.08 2016-06-08
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.RadiationExposure;
import org.mars_sim.msp.core.person.medical.Complaint;
import org.mars_sim.msp.core.person.medical.DeathInfo;
import org.mars_sim.msp.core.person.medical.HealthProblem;
import org.mars_sim.msp.core.person.medical.MedicalEvent;
import org.mars_sim.msp.core.person.medical.MedicalManager;
import org.mars_sim.msp.core.person.medical.Medication;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotConfig;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * This class represents the System Condition of a robot.
 * It models a robot's health.
 */
public class SystemCondition
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(SystemCondition.class.getName());

    /** Sleep Habit maximum value. */
    private static int MAX_WEIGHT = 30;
    /** Sleep Habit Map resolution. */
    private static int SLEEP_MAP_RESOLUTION = 20;

    private static double INFLATION = 1.15;

	private int solCache = 0;

    /** Life support minimum value. */
    private static int MIN_VALUE = 0;
    /** Life support maximum value. */
    private static int MAX_VALUE = 1;

    /** Stress jump resulting from being in an accident. */
    public static final double ACCIDENT_STRESS = 40D;

    public static final double FOOD_RESERVE_FACTOR = 1.5D;
    
    public static final double MENTAL_BREAKDOWN = 80D;
    
    private static final double COLLAPSE_IMMINENT = 2500D;

    /** TODO The anxiety attack health complaint should be an enum or smth. */
    private static final String ANXIETY_ATTACK = "Anxiety Attack";

    private static final String HIGH_FATIGUE_COLLAPSE = "High Fatigue Collapse";

    /** Period of time (millisols) over which random ailments may happen. */
    private static double RANDOM_AILMENT_PROBABILITY_TIME = 100000D;

    // Each meal has 0.1550 kg and has 2525 kJ. Thus each 1 kg has 16290.323 kJ
    public static double FOOD_COMPOSITION_ENERGY_RATIO = 16290.323;
    //public static int MAX_KJ = 16290; //  1kg of food has ~16290 kJ (see notes on people.xml under <food-consumption-rate value="0.62" />)

    public static double ENERGY_FACTOR = 0.8D;

    /** Performance modifier for hunger. */
    private static final double HUNGER_PERFORMANCE_MODIFIER = .001D;

    /** Performance modifier for fatigue. */
    private static final double FATIGUE_PERFORMANCE_MODIFIER = .001D;

    /** Performance modifier for stress. */
    private static final double STRESS_PERFORMANCE_MODIFIER = .02D;


    // Data members
	private int numSleep = 0;
	private int suppressHabit = 0;
	private int spaceOut = 0;

    /** Person's fatigue level. */
    private double fatigue;
    /** Person's hunger level. */
    private double powerDischarge;
    /** Person's stress level (0.0 - 100.0). */
    private double stress;
    /** Performance factor. */
    private double performance;

    private double personStarvationTime;

    // 2015-02-23 Added hygiene
    private double hygiene; /** Person's hygiene factor (0.0 - 100.0 */

    // 2015-01-12 Person's energy level
    private double kJoules;
    private double foodDryMassPerServing;
    private double robotBatteryDrainTime;


    /** True if person is alive. */
    private boolean operable;
    private boolean isLowPowerMode;
    private boolean isBatteryDepleting;

    /** List of medication affecting the person. */
    private List<Medication> medicationList;
    /** Injury/Illness effecting person. */
    private HashMap<Complaint, HealthProblem> problems;

    private Robot robot;
    /** Details of persons death. */
    private DeathInfo deathDetails;
    /** Most serious problem. */
    private HealthProblem serious;

    // 2015-04-29 Added RadiationExposure
    private RadiationExposure radiation;

	private MarsClock clock;// = Simulation.instance().getMasterClock().getMarsClock();

    // 2015-12-05 Added sleepHabitMap
    private Map<Integer, Integer> sleepHabitMap = new HashMap<>(); // set weight = 0 to MAX_WEIGHT


    /**
     * Constructor 2.
     * @param robot The robot requiring a physical presence.
     */
    public SystemCondition(Robot newRobot) {
    	clock = Simulation.instance().getMasterClock().getMarsClock();
    	
        deathDetails = null;
        robot = newRobot;
        problems = new HashMap<Complaint, HealthProblem>();
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

    public RadiationExposure getRadiationExposure() {
    	return radiation;
    }


    public double getMassPerServing() {
        return foodDryMassPerServing;
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
    public boolean timePassing(double time, LifeSupportType support, RobotConfig config) {

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
    private boolean consumeFuel(LifeSupportType support, double amount) {
    	return true;
    }


    public void checkStarvation(double hunger) {

        // TODO: need a different method and different terminology to account for the drain on the robot's battery
        if (robot != null) {

        }
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
            HealthProblem illness = problems.get(complaint);
            if (illness != null) {
                illness.startRecovery();
                //person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
            }
        }
        return newProblem;
    }

    /**
     * Person requires minimum air pressure.
     * @param support Life support system providing air pressure.
     * @param pressure minimum air pressure person requires (in Pa)
     * @return new problem added.
     */
    private boolean requireAirPressure(LifeSupportType support, double pressure) {
        return checkResourceConsumption(support.getAirPressure(), pressure,
                MIN_VALUE, getMedicalManager().getDecompression());
    }

    /**
     * Person requires minimum temperature.
     * @param support Life support system providing temperature.
     * @param temperature minimum temperature person requires (in degrees Celsius)
     * @return new problem added.
     */
    private boolean requireTemperature(LifeSupportType support, double minTemperature,
            double maxTemperature) {

        boolean freeze = checkResourceConsumption(support.getTemperature(),
                minTemperature, MIN_VALUE, getMedicalManager().getFreezing());
        boolean hot = checkResourceConsumption(support.getTemperature(),
                maxTemperature, MAX_VALUE, getMedicalManager().getHeatStroke());
        return freeze || hot;
    }

    /**
     * Get the details of this Person's death.
     * @return Detail of the death, will be null if person is still alive.
     */
    public DeathInfo getDeathDetails() {
        return deathDetails;
    }

    /**
     * Gets the person's caloric energy.
     * @return person's caloric energy in kilojoules
     * Note: one large calorie is about 4.2 kilojoules
     */
    public double getEnergy() {
        return kJoules;
    }

    /** Reduces the person's caloric energy.
     *  @param time the amount of time (millisols).
     */
    public void reduceEnergy(double time) {
        double dailyEnergyIntake = 10100D;
        // Changing this to a more linear reduction of energy.
        // We may want to change it back to exponential. - Scott
        double xdelta = (time / 1000D) * dailyEnergyIntake;
    	// TODO: re-tune the experimental FACTOR to work in most situation
//    	double xdelta =  4 * time / FOOD_COMPOSITION_ENERGY_RATIO;
        //System.out.println("PhysicalCondition : ReduceEnergy() : time is " + Math.round(time*100.0)/100.0);
        //System.out.println("PhysicalCondition : ReduceEnergy() : xdelta is " + Math.round(xdelta*10000.0)/10000.0);
//        kJoules = kJoules / exponential(xdelta);
        kJoules -= xdelta;

        if (kJoules < 100D) {
            // 100 kJ is the lowest possible energy level
        	kJoules = 100D;
        }

        //System.out.println("PhysicalCondition : ReduceEnergy() : kJ is " + Math.round(kJoules*100.0)/100.0);
    }

//    public double exponential(double x) {
//    	  x = 1d + x / 256d;
//    	  x *= x; x *= x; x *= x; x *= x;
//    	  x *= x; x *= x; x *= x; x *= x;
//    	  return x;
//    	}

    /** Sets the person's energy level
     *  @param kilojoules
     */
    public void setEnergy(double kJ) {
        kJoules = kJ;
        //System.out.println("PhysicalCondition : SetEnergy() : " + Math.round(kJoules*100.0)/100.0 + " kJoules");
    }

    /** Adds to the person's energy intake by eating
     *  @param person's energy level in kilojoules
     */
    public void addEnergy(double foodAmount) {
    	// TODO: vary MAX_KJ according to the individual's physical profile strength, endurance, etc..
        // double FOOD_COMPOSITION_ENERGY_RATIO = 16290;  1kg of food has ~16290 kJ (see notes on people.xml under <food-consumption-rate value="0.62" />)
        // double FACTOR = 0.8D;
		// Each meal (.155 kg = .62/4) has an average of 2525 kJ
        // Note: changing this to a more linear addition of energy.
        // We may want to change it back to exponential. - Scott
        double xdelta = foodAmount * FOOD_COMPOSITION_ENERGY_RATIO;
//        kJoules += foodAmount * xdelta * Math.log(FOOD_COMPOSITION_ENERGY_RATIO / kJoules) / ENERGY_FACTOR;
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
        if (fatigue != newFatigue) {
            fatigue = newFatigue;
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
        return stress;
    }


    /**
     * This Person is now dead.
     * @param illness The compliant that makes person dead.
     */
    public void setInoperable(HealthProblem illness) {

        setBatteryPower(0D);
        setPerformanceFactor(0D);
        operable = false;

        //deathDetails = new DeathInfo(person);

        //logger.severe(person + " dies due to " + illness);

        // Create medical event for death.
        //MedicalEvent event = new MedicalEvent(person, illness, EventType.MEDICAL_DEATH);
        //Simulation.instance().getEventManager().registerNewEvent(event);

        // Throw unit event.
        //person.fireUnitUpdate(UnitEventType.DEATH_EVENT);
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
     * Get a string description of the most serious health situation.
     * @return A string containing the current illness if any.
     */
    public String getHealthSituation() {
        String situation = "Operable";
        if (serious != null) {
            if (isInoperable()) {
                situation = "Inoperable : " + serious.getIllness().getType().toString();
            }
            else {
                situation = serious.getSituation();
            }
            //else situation = "Not Well";
        }
        return situation;
    }

    /**
     * Gets the most serious illness.
     * @return most serious illness
     */
    public Complaint getMostSerious() {
        return serious.getIllness();
    }

    /**
     * The collection of known Medical Problems.
     */
    public Collection<HealthProblem> getProblems() {
        return problems.values();
    }

    /**
     * Calculate the most serious problem and the person's performance.
     */
    private void recalculate() {

        double tempPerformance = 1.0D;

        if (robot != null) {

             if (kJoules < 200D) {
                tempPerformance = kJoules / 200D;
            }

        }

        if (tempPerformance < 0D) {
            tempPerformance = 0D;
        }

        setPerformanceFactor(tempPerformance);
    }

    /**
     * Checks if the person has any serious medical problems.
     * @return true if serious medical problems
     
    public boolean hasSeriousMedicalProblems() {
        boolean result = false;
        Iterator<HealthProblem> meds = getProblems().iterator();
        while (meds.hasNext()) {
            if (meds.next().getIllness().getSeriousness() >= 50) result = true;
        }
        return result;
    }
*/
    

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
     * Gets a list of medication affecting the person.
     * @return list of medication.
     
    public List<Medication> getMedicationList() {
        return new ArrayList<Medication>(medicationList);
    }
*/
    /**
     * Checks if the person is affected by the given medication.
     * @param medicationName the name of the medication.
     * @return true if person is affected by it.
     
    public boolean hasMedication(String medicationName) {
        if (medicationName == null)
            throw new IllegalArgumentException("medicationName is null");

        boolean result = false;

        Iterator<Medication> i = medicationList.iterator();
        while (i.hasNext()) {
            if (medicationName.equals(i.next().getName())) result = true;
        }

        return result;
    }
*/
    
    /**
     * Adds a medication that affects the person.
     * @param medication the medication to add.
     
    public void addMedication(Medication medication) {
        if (medication == null)
            throw new IllegalArgumentException("medication is null");
        medicationList.add(medication);
    }
*/
    

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        deathDetails = null;
        problems.clear();
        problems = null;
        serious = null;
        robot = null;
        if (medicationList != null) medicationList.clear();
        medicationList = null;
    }
}
