/**
 * Mars Simulation Project
 * PhysicalCondition.java
 * @version 3.1.0 2017-01-19
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person;

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
import org.mars_sim.msp.core.person.medical.Complaint;
import org.mars_sim.msp.core.person.medical.ComplaintType;
import org.mars_sim.msp.core.person.medical.DeathInfo;
import org.mars_sim.msp.core.person.medical.HealthProblem;
import org.mars_sim.msp.core.person.medical.MedicalEvent;
import org.mars_sim.msp.core.person.medical.MedicalManager;
import org.mars_sim.msp.core.person.medical.Medication;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class represents the Physical Condition of a Person.
 * It models a person's health and physical characteristics.
 */
public class PhysicalCondition
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    public static final String WELL = "Well";
    public static final String DEAD = "Dead : ";
    public static final String ILL = "Ill : ";

	public static final String OXYGEN = "oxygen";
	public static final String WATER = "water";
	public static final String FOOD = "food";
	public static final String CO2 = "carbon dioxide";

    /** default logger. */
    private static Logger logger = Logger.getLogger(PhysicalCondition.class.getName());
    /** Sleep Habit maximum value. */
    private static int MAX_WEIGHT = 30;
    /** Sleep Habit Map resolution. */
    private static int SLEEP_MAP_RESOLUTION = 20;
    /** Life support minimum value. */
    private static int MIN_VALUE = 0;
    /** Life support maximum value. */
    private static int MAX_VALUE = 1;

    /** Stress jump resulting from being in an accident. */
    public static final double ACCIDENT_STRESS = 10D;

    public static final double FOOD_RESERVE_FACTOR = 1.5D;

    public static final double MENTAL_BREAKDOWN = 100D;

    private static final double COLLAPSE_IMMINENT = 5000D;
    /** Performance modifier for hunger. */
    private static final double HUNGER_PERFORMANCE_MODIFIER = .0001D;
    /** Performance modifier for fatigue. */
    private static final double FATIGUE_PERFORMANCE_MODIFIER = .0001D;
    /** Performance modifier for stress. */
    private static final double STRESS_PERFORMANCE_MODIFIER = .005D;

    /** TODO The anxiety attack health complaint should be an enum or smth. */
    //private static final String PANIC_ATTACK = "Panic Attack";
    //private static final String DEPRESSION = "Depression";
    //private static final String HIGH_FATIGUE_COLLAPSE = "High Fatigue Collapse";

    /** Period of time (millisols) over which random ailments may happen. */
    private static double RANDOM_AILMENT_PROBABILITY_TIME = 100000D;
    // Each meal has 0.1550 kg and has 2525 kJ. Thus each 1 kg has 16290.323 kJ
    public static double FOOD_COMPOSITION_ENERGY_RATIO = 16290.323;
    //public static int MAX_KJ = 16290; //  1kg of food has ~16290 kJ (see notes on people.xml under <food-consumption-rate value="0.62" />)
    public static double ENERGY_FACTOR = 0.8D;

    private static double INFLATION = 1.15;

	private static double o2_consumption;
	private static double h2o_consumption;
	private static double minimum_air_pressure;
	private static double min_temperature;
	private static double max_temperature;
	private static double food_consumption;
	private static double dessert_consumption;
	private static double highFatigueCollapseChance;
    private static double stressBreakdownChance;

    // Data members

	private int solCache = 0;
	private int numSleep = 0;
	private int suppressHabit = 0;
	private int spaceOut = 0;

    /** Person's fatigue level. 0 to infinity */
    private double fatigue;
    /** Person's hunger level. */
    private double hunger;
    /** Person's stress level (0.0 - 100.0). */
    private double stress;
    /** Performance factor 0.0 to 1.0. */
    private double performance;

    private double inclination_factor;

    private double personStarvationTime;
    // 2015-02-23 Added hygiene
    private double hygiene; /** Person's hygiene factor (0.0 - 100.0 */
    // 2015-01-12 Person's energy level
    private double kJoules;

    private double foodDryMassPerServing;

    /** True if person is alive. */
    private boolean alive;

    private boolean isStarving;

    private boolean isStressedOut = false, isCollapsed = false;

    private String name;

    /** List of medication affecting the person. */
    private List<Medication> medicationList;
    /** Injury/Illness effecting person. */
    private HashMap<Complaint, HealthProblem> problems;
    /** Person owning this physical. */
    private Person person;
    /** Details of persons death. */
    private DeathInfo deathDetails;
    /** Most serious problem. */
    private HealthProblem serious;
    // 2015-04-29 Added RadiationExposure
    private RadiationExposure radiation;

	private MarsClock marsClock;

	private static AmountResource foodAR;// needed by Farming
	//private static AmountResource oxygenAR;
	//private static AmountResource waterAR;
	//private static AmountResource carbonDioxideAR;

    // 2015-12-05 Added sleepHabitMap
    private Map<Integer, Integer> sleepCycleMap = new HashMap<>(); // set weight = 0 to MAX_WEIGHT


    /**
     * Constructor 1.
     * @param newPerson The person requiring a physical presence.
     */
    // 2015-04-29 Added RadiationExposure();
    public PhysicalCondition(Person newPerson) {
        person = newPerson;
    	name = newPerson.getName();

        alive = true;

    	if (Simulation.instance().getMasterClock() != null) // for passing maven test
    		marsClock = Simulation.instance().getMasterClock().getMarsClock();

		foodAR = AmountResource.findAmountResource(FOOD);			// 1
		//waterAR = AmountResource.findAmountResource(WATER);		// 2
		//oxygenAR = AmountResource.findAmountResource(OXYGEN);		// 3
		//carbonDioxideAR = AmountResource.findAmountResource(CO2);	// 4

    	radiation = new RadiationExposure(this);
        radiation.initializeWithRandomDose();

        deathDetails = null;

        problems = new HashMap<Complaint, HealthProblem>();
        performance = 1.0D;

        fatigue = RandomUtil.getRandomRegressionInteger(1000) * .5;
        stress = RandomUtil.getRandomRegressionInteger(100) * .2;
        hunger = RandomUtil.getRandomRegressionInteger(1000) * .5;
        kJoules = 500D + RandomUtil.getRandomRegressionInteger(2000);

        // 2015-02-23 Added hygiene (not used yet)
        hygiene = RandomUtil.getRandomDouble(100D);

        medicationList = new ArrayList<Medication>();

        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();

        // 2017-03-08 Add rates
        o2_consumption = personConfig.getNominalO2ConsumptionRate();
        h2o_consumption = personConfig.getWaterConsumptionRate();
        minimum_air_pressure = personConfig.getMinAirPressure();
        min_temperature = personConfig.getMinTemperature();
        max_temperature = personConfig.getMaxTemperature();
        food_consumption = personConfig.getFoodConsumptionRate();
        dessert_consumption = personConfig.getDessertConsumptionRate();

        stressBreakdownChance = personConfig.getStressBreakdownChance();
        highFatigueCollapseChance = personConfig.getHighFatigueCollapseChance();

        foodDryMassPerServing = food_consumption / (double) Cooking.NUMBER_OF_MEAL_PER_SOL;

        try {
        	personStarvationTime =  1000D * (personConfig.getStarvationStartTime()
        			+ RandomUtil.getRandomDouble(1.0) - RandomUtil.getRandomDouble(1.0));
            //System.out.println("personStarvationTime : "+ Math.round(personStarvationTime*10.0)/10.0);
        }
        catch (Exception e) {
        	//logger.severe("");
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
     * The Physical condition should be updated to reflect a passing of time.
     * This method has to check the recover or degradation of any current
     * illness. The progression of this time period may result in the illness
     * turning fatal.
     * It also updated the hunger and fatigue status
     *
     * @param time amount of time passing (in millisols)
     * @param support life support system.
     * @return True still alive.
     */
    public void timePassing(double time, LifeSupportType support) {

    	if (alive) {
	      	// 2015-12-05 check for the passing of each day
	    	int solElapsed = marsClock.getSolElapsedFromStart();
	    	if (solCache != solElapsed) {
	    		// 2015-12-05 reset numSleep back to zero at the beginning of each sol
	    		numSleep = 0;
	    		suppressHabit = 0;
	    		solCache = solElapsed;
	    	}

	        boolean illnessEvent = false;

	        radiation.timePassing(time);

	        // Check the existing problems
	        if (!problems.isEmpty()) {
	            // Throw illness event if any problems already exist.
	            illnessEvent = true;

	            List<Complaint> newProblems = new ArrayList<Complaint>();
	            List<HealthProblem> currentProblems = new ArrayList<HealthProblem>(problems.values());

	            Iterator<HealthProblem> iter = currentProblems.iterator();
	            while(iter.hasNext()) {
	                HealthProblem problem = iter.next();

	                // Advance each problem, they may change into a worse problem.
	                // If the current is completed or a new problem exists then
	                // remove this one.
	                Complaint next = problem.timePassing(time, this);

	                if (problem.getCured() || (next != null)) {
	                	Complaint c = problem.getIllness();
	                    problems.remove(c);

	                    // 2017-01-19 Added resetting isCollapsed and isStressedOut
	                    if (c.getType() == ComplaintType.HIGH_FATIGUE_COLLAPSE)
	                    	isCollapsed = false;

	                    else if (c.getType() == ComplaintType.PANIC_ATTACK)
	                    	isStressedOut = false;

	                    else if (c.getType() == ComplaintType.DEPRESSION)
	                    	isStressedOut = false;
	                }


	                // If a new problem, check it doesn't exist already
	                if (next != null) {
	                    newProblems.add(next);
	                }
	            }

	            // Add the new problems
	            Iterator<Complaint> newIter = newProblems.iterator();
	            while(newIter.hasNext()) {
	                addMedicalComplaint(newIter.next());
	                illnessEvent = true;
	            }
	        }

	        // Has the person died ?
	        //if (isDead()) return false;

	        // See if any random illnesses happen.
	        List<Complaint> randomAilments = checkForRandomAilments(time);
	        if (randomAilments.size() > 0) {
	            illnessEvent = true;
	        }

	        if (illnessEvent) {
	            person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
	        }

	    	if (alive) {
		        // Check life support system
		        try {
		        	//System.out.println("o2_consumption : " + o2_consumption * time / 1000D);
		            if (consumeOxygen(support, o2_consumption * (time / 1000D)))
		                logger.log(Level.SEVERE, name + " has insufficient oxygen.");
		            if (consumeWater(support, h2o_consumption * (time / 1000D)))
		                logger.log(Level.SEVERE, name + " has insufficient water.");

		            if (requireAirPressure(support, minimum_air_pressure))
		                logger.log(Level.SEVERE, name + " is under insufficient air pressure.");
		            if (requireTemperature(support, min_temperature, max_temperature))
		                logger.log(Level.SEVERE, name + " cannot survive long at this extreme temperature.");
		        }
		        catch (Exception e) {
	                e.printStackTrace();
		            logger.log(Level.SEVERE, name + "'s life support system is failing !");// + e.getMessage());
		        }
	    	}

	        // Build up fatigue & hunger for given time passing.
	        setFatigue(fatigue + time);
	        setHunger(hunger + time);

	        // normal bodily function consume a minute amount of energy
	        // even if a person does not perform any tasks
	        // Note: removing this as reduce energy is already handled
	        // in the TaskManager and people are always performing tasks
	        // unless dead. - Scott
	        //reduceEnergy(time);

	        checkStarvation(hunger);
	        //System.out.println("PhysicalCondition : hunger : "+ Math.round(hunger*10.0)/10.0);

	        // Add time to all medications affecting the person.
	        Iterator<Medication> i = medicationList.iterator();
	        while (i.hasNext()) {
	            Medication med = i.next();
	            if (!med.isMedicated()) {
	                i.remove();
	            }
	        }

	        // If person is at high stress, check for mental breakdown.
	        if (!isStressedOut)
		        if (stress > MENTAL_BREAKDOWN)
		            checkForStressBreakdown(time);

	        // 2016-03-01 check if person is at very high fatigue may collapse.
	        if (!isCollapsed)
	        	if (fatigue > COLLAPSE_IMMINENT)
	        		checkForHighFatigueCollapse(time);

	        // Calculate performance and most serious illness.
	        recalculate();

    	}

    }


    /**
     * Check for any random ailments that a person comes down with over a period of time.
     * @param time the time period (millisols).
     * @return list of ailments occurring.  May be empty.
     */
    private List<Complaint> checkForRandomAilments(double time) {

    	// TODO: create a history of past ailments a person suffers from.
    	// TODO: create a history of potential ailments a person is likely to suffer from.


        List<Complaint> result = new ArrayList<Complaint>(0);

        // Check each possible medical complaint.
        Iterator<Complaint> i = getMedicalManager().getAllMedicalComplaints().iterator();
        while (i.hasNext()) {
            Complaint complaint = i.next();
            double probability = complaint.getProbability();
            // TODO: need to be more task-based or location-based ?

            // Check that medical complaint has a probability > zero.
            if (probability > 0D) {

                // Check that person does not already have a health problem with this complaint.
                if (!problems.containsKey(complaint)) {

                    // Randomly determine if person suffers from ailment.
                    double chance = RandomUtil.getRandomDouble(100D);
                    double timeModifier = time / RANDOM_AILMENT_PROBABILITY_TIME;
                    if (chance <= (probability) * timeModifier) {
                    	String ailment = complaint.toString();
                    	if (Conversion.checkVowel(ailment))
                    		ailment = "an " + ailment.toLowerCase();
                    	else
                    		ailment = "a " + ailment.toLowerCase();
                        logger.info(person + " comes down with " + ailment);
                        addMedicalComplaint(complaint);
                        result.add(complaint);
                    }
                }
            }
        }

        return result;
    }

    /** Adds a new medical complaint to the person.
     *  @param complaint the new medical complaint
     */
    public void addMedicalComplaint(Complaint complaint) {
        if ((complaint != null) && !problems.containsKey(complaint)) {
            HealthProblem problem = new HealthProblem(complaint, person);
            problems.put(complaint, problem);
            recalculate();
        }
    }


    /**
     * Person consumes given amount of food
     * @param amount amount of food to consume (in kg).
     * @param container unit to get food from
     * @throws Exception if error consuming food.
     */
    public void consumeFood(double amount, Unit container) {
        if (container == null) throw new IllegalArgumentException("container is null");
		consumePackedFood(amount, container);//, LifeSupportType.FOOD);

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
     * Person consumes given amount of packed food
     * @param amount amount of food to consume (in kg).
     * @param container unit to get food from
     * @throws Exception if error consuming food.
     */
	// 2014-11-07 Added consumePackedFood()
    @SuppressWarnings("unused")
	public void consumePackedFood(double amount, Unit container) {//, String foodType) {
    	Inventory inv = container.getInventory();

    	if (container == null) throw new IllegalArgumentException("container is null");

    	//AmountResource foodAR = AmountResource.findAmountResource(foodType);
        double foodEaten = amount;
        double foodAvailable = inv.getAmountResourceStored(foodAR, false);

        // 2015-01-09 Added addDemandTotalRequest()
        inv.addAmountDemandTotalRequest(foodAR);

        if (foodAvailable < 0.01D) {
           throw new IllegalStateException("Warning: less than 0.01 kg dried food remaining!");
        }
        // if container has less than enough food, finish up all food in the container
        else {

            if (foodEaten > foodAvailable)
            	foodEaten = foodAvailable;

            foodEaten = Math.round(foodEaten * 1000000.0) / 1000000.0;
            // subtract food from container
            inv.retrieveAmountResource(foodAR, foodEaten);

    		// 2015-01-09 addDemandRealUsage()
    		inv.addAmountDemand(foodAR, foodEaten);
        }
    }

    /**
     * Person consumes a given amount of food not taken from local container.
     * @param amount the amount of food to consume (in kg).

    public void consumeFood(double amount) {
        //System.out.println("PhysicalCondition.java : just called consumeFood(double amount) : food NOT taken from local container. amount is " + amount);
        // if (checkResourceConsumption(amount, amount, MIN_VALUE, getMedicalManager().getStarvation()))
        // 	recalculate();
    }
*/
    /**
     * Person consumes given amount of oxygen
     * @param support Life support system providing oxygen.
     * @param amount amount of oxygen to consume (in kg)
     * @return new problem added.
     * @throws Exception if error consuming oxygen.
     */
    private boolean consumeOxygen(LifeSupportType support, double amount) {
        double amountRecieved = support.provideOxygen(amount);
        //if (support == null) System.out.println("support : "+ support);
        //boolean check = checkResourceConsumption(amountRecieved, amount / 2D,
        //        MIN_VALUE, getMedicalManager().getSuffocation());
        //System.out.println("O2 : " + amountRecieved + " : " + check);
        //return check;
        return checkResourceConsumption(amountRecieved, amount / 2D,
                MIN_VALUE, getMedicalManager().getSuffocation());
    }

    /**
     * Person consumes given amount of water
     * @param support Life support system providing water.
     * @param amount amount of water to consume (in kg)
     * @return new problem added.
     * @throws Exception if error consuming water.
     */
    private boolean consumeWater(LifeSupportType support, double amount) {
        double amountReceived = support.provideWater(amount);

        return checkResourceConsumption(amountReceived, amount / 2D,
                MIN_VALUE, getMedicalManager().getDehydration());
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
            addMedicalComplaint(complaint);
            person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
        }
        else {
            //Is the person suffering from the illness, if so recovery
            // as the amount has been provided
            HealthProblem illness = problems.get(complaint);
            if (illness != null) {
                illness.startRecovery();
                person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
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

    /** Gets the person's fatigue level
     *  @return person's fatigue
     */
    public double getFatigue() {
        return fatigue;
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
    public void setPerformanceFactor(double newPerformance) {
        if (newPerformance != performance) {
            performance = newPerformance;
			if (person != null)
	            person.fireUnitUpdate(UnitEventType.PERFORMANCE_EVENT);
        }
    }

    /**
     * Gets the person with this physical condition
     * @return
     */
    Person getPerson() {
        return person;
    }

    /**
     * Define the fatigue setting for this person
     * @param newFatigue New fatigue.
     */
    public void setFatigue(double newFatigue) {
        //if (fatigue != newFatigue) {
            fatigue = newFatigue;
			//if (person != null)
	            person.fireUnitUpdate(UnitEventType.FATIGUE_EVENT);
        //}
    }

    /** Gets the person's hunger level
     *  @return person's hunger
     */
    public double getHunger() {
        return hunger;
    }


    public void checkStarvation(double hunger) {

    	if (person != null) {

                Complaint starvation = getMedicalManager().getStarvation();

//                if (hunger > personStarvationTime
//                		|| ((hunger > 1000D) && (kJoules < 500D))) {
                if (hunger > personStarvationTime) {
                    if (!problems.containsKey(starvation)) {
                        addMedicalComplaint(starvation);
                        //System.out.println("PhysicalCondition : checkStarvation() : hunger is " + Math.round(hunger*10.0)/10.0 + " ");
                        person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
                    }
                }
//                else if (hunger < 1000D
//                		|| kJoules > 500D ) {
                else if (hunger < 200D) {
                    HealthProblem illness = problems.get(starvation);
                    if (illness != null) {
                        illness.startRecovery();
                        //person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
                    }
                }

                //person.fireUnitUpdate(UnitEventType.HUNGER_EVENT);
        	}
    }


    /**
     * Define the hunger setting for this person
     * @param newHunger New hunger.
     */
    public void setHunger(double newHunger) {
        if (hunger != newHunger) {
            hunger = newHunger;
        }
    }


    /**
     * Gets the person's stress level
     * @return stress (0.0 to 100.0)
     */
    public double getStress() {
        return stress;
    }

    /**
     * Sets the person's stress level.
     * @param newStress the new stress level (0.0 to 100.0)
     */
    public void setStress(double newStress) {
        if (stress != newStress) {
            stress = newStress;
            if (stress > 100D) stress = 100D;
            else if (stress < 0D) stress = 0D;
            else if (Double.isNaN(stress)) stress = 0D;
            person.fireUnitUpdate(UnitEventType.STRESS_EVENT);
        }
    }

    /**
     * Checks if person has an anxiety attack due to too much stress.
     * @param time the time passing (millisols)
     */
    // 2016-06-15 Expanded Anxiety Attack into either Panic Attack or Depression
    private void checkForStressBreakdown(double time) {

    	Complaint depression = getMedicalManager().getComplaintByName(ComplaintType.DEPRESSION);
    	Complaint panicAttack = getMedicalManager().getComplaintByName(ComplaintType.PANIC_ATTACK);
    	// a person is limited to have only one of them at a time
        if (!problems.containsKey(panicAttack) && !problems.containsKey(depression)) {

            // Determine stress resilience modifier (0D - 2D).
            int resilience = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.STRESS_RESILIENCE);
            int emotStability = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.EMOTIONAL_STABILITY);

            // 0 (strong) to 1 (weak)
            double resilienceModifier = (double) (100.0 - resilience *.6 - emotStability *.4) / 100D;
            //System.out.println("checkForStressBreakdown()'s resilienceModifier : " + resilienceModifier);
/*
            double chance = 0;
            try {
            	// 0 to 100
            	chance = stressBreakdownChance;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Could not read 'stress-breakdown-chance' element in 'conf/people.xml': " + e.getMessage());
            }
*/
            double value = stressBreakdownChance / 10D * resilienceModifier;
            //System.out.println("checkForStressBreakdown()'s value : " + value);
            //if (RandomUtil.getRandomInt(100) < chance * resilienceModifier) {
            if (RandomUtil.lessThanRandPercent(value)) {

            	isStressedOut = true;
/*
               	if (fatigue < 200)
            		fatigue = fatigue * 4;
            	else if (fatigue < 400)
            		fatigue = fatigue * 2;
            	else if (fatigue < 600)
            		fatigue = fatigue * 1.2;
*/
            	double rand = RandomUtil.getRandomDouble(1.0) + inclination_factor;

            	if (rand < 0.5) {

            		//Complaint panicAttack = getMedicalManager().getComplaintByName(ComplaintType.PANIC_ATTACK);
                    if (panicAttack != null) {
                    	if (inclination_factor > -.5)
                    		inclination_factor = inclination_factor - .05;

                    	addMedicalComplaint(panicAttack);
                        //illnessEvent = true;
                        person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
                        logger.info(name + " suffers from a panic attack.");
                        //System.out.println(name + " has a panic attack.");

                    }
                    else
                    	logger.log(Level.SEVERE, "Could not find 'Panic Attack' medical complaint in 'conf/medical.xml'");

            	} else {

                    //Complaint depression = getMedicalManager().getComplaintByName(ComplaintType.DEPRESSION);
                    if (depression != null) {
                    	if (inclination_factor < .5)
                    		inclination_factor = inclination_factor + .05;
                    	addMedicalComplaint(depression);
                        //illnessEvent = true;
                        person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
                        logger.info(name + " has an episode of depression.");
                        //System.out.println(name + " has an episode of depression.");
                    }
                    else
                    	logger.log(Level.SEVERE,"Could not find 'Depression' medical complaint in 'conf/medical.xml'");
            	}


            } else {
/*
               	if (fatigue < 200)
            		fatigue = fatigue * 2;
            	else if (fatigue < 400)
            		fatigue = fatigue * 1.5;
*/
            }
        }
    }

    /**
     * Checks if person has very high fatigue.
     * @param time the time passing (millisols)
     */
    // 2016-03-01 checkForHighFatigue
    private void checkForHighFatigueCollapse(double time) {
    	Complaint highFatigue = getMedicalManager().getComplaintByName(ComplaintType.HIGH_FATIGUE_COLLAPSE);
        if (!problems.containsKey(highFatigue)) {

            // Calculate the modifier (from 10D to 0D) Note that the base high-fatigue-collapse-chance is 5%
            int endurance = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.ENDURANCE);
            int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.STRENGTH);

            // a person with high endurance will be less likely to be collapse
            double modifier = (double) (100 - endurance * .6 - strength *.4) / 100D;
            //System.out.println("checkForHighFatigueCollapse()'s modifier :" + modifier);
/*
            double chance = 0;

            try {
            	chance = personConfig.getHighFatigueCollapseChance();
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Could not read 'high-fatigue-collapse-chance' element in 'conf/people.xml': " + e.getMessage());
            }
*/
            double value = highFatigueCollapseChance /5D * modifier;

            if (RandomUtil.lessThanRandPercent(value)) {
                //System.out.println("checkForHighFatigueCollapse()'s value :" + value);
            	isCollapsed = true;
            	//Complaint highFatigue = getMedicalManager().getComplaintByName(ComplaintType.HIGH_FATIGUE_COLLAPSE);
/*
             	if (stress < 10)
             		stress = stress * 1.8;
            	else if (stress < 30)
            		stress = stress * 1.5;
            	else if (stress < 50)
            		stress = stress * 1.2;
*/

                if (highFatigue != null) {
                    addMedicalComplaint(highFatigue);
                    //illnessEvent = true;
                    person.fireUnitUpdate(UnitEventType.ILLNESS_EVENT);
                    logger.info(name + " collapses because of high fatigue exhaustion.");
                }
                else
                	logger.log(Level.SEVERE,"Could not find 'High Fatigue Collapse' medical complaint in 'conf/medical.xml'");
            }
            else {

/*
            	if (stress < 10)
             		stress = stress * 1.5;
            	else if (stress < 30)
            		stress = stress * 1.3;
            	else if (stress < 50)
            		stress = stress * 1.1;
*/
            }
        }
    }

    /**
     * This Person is now dead.
     * @param illness The compliant that makes person dead.
     */
    public void setDead(HealthProblem illness, Boolean causedByUser) {
        setFatigue(0D);
        setHunger(0D);
        setPerformanceFactor(0D);
        setStress(0D);
        alive = false;

        if (causedByUser) {
        	person.setDead();

            this.serious = illness;
            illness.setState(HealthProblem.DEAD);
            logger.severe(person + " committed suicide as instructed.");
        }

        deathDetails = new DeathInfo(person, illness);

		person.getMind().setInactive();

    	if (deathDetails.getBodyRetrieved())
    		person.buryBody();

    	logger.log(Level.SEVERE, "A post-mortem examination was ordered on " + person + ". The cause of death : " + illness.toString().toLowerCase());

        if (!causedByUser) {
            // Create medical event for death.
            MedicalEvent event = new MedicalEvent(person, illness, EventType.MEDICAL_DEATH);
            Simulation.instance().getEventManager().registerNewEvent(event);
        }


        // Throw unit event.
        person.fireUnitUpdate(UnitEventType.DEATH_EVENT);
    }

    /**
     * Checks if the person is dead.
     *
     * @return true if dead
     */
    public boolean isDead() {
        return !alive;
    }

    /**
     * Checks if the person is starving.
     *
     * @return true if starving
     */
    public boolean isStarving() {
        return isStarving;
    }

    /**
     * Get a string description of the most serious health situation.
     * @return A string containing the current illness if any.
     */
    public String getHealthSituation() {
        String situation = WELL;
        if (serious != null) {
            if (isDead()) {
                situation = DEAD + serious.getIllness().getType().toString();
            }
            else {
                situation = ILL + serious.getSituation();
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

        if (person != null) {

            serious = null;

            // Check the existing problems. find most serious & performance
            // effecting
            Iterator<HealthProblem> iter = problems.values().iterator();
            while(iter.hasNext()) {
                HealthProblem problem = iter.next();
                double factor = problem.getPerformanceFactor();
                if (factor < tempPerformance) {
                    tempPerformance = factor;
                }

                if ((serious == null) || (serious.getIllness().getSeriousness() <
                        problem.getIllness().getSeriousness())) {
                    serious = problem;
                }
            }

            // High hunger reduces performance.
            if (hunger > 1200D) {
                tempPerformance -= (hunger - 1200D) * HUNGER_PERFORMANCE_MODIFIER /2;
            }
            else if (hunger > 800D) {
                tempPerformance -= (hunger - 800D) * HUNGER_PERFORMANCE_MODIFIER /4;
            }

            // High fatigue reduces performance.
            if (fatigue > 1400D) {
                tempPerformance -= (fatigue - 1400D) * FATIGUE_PERFORMANCE_MODIFIER /2;
            }
            else if (fatigue > 800D) {
                tempPerformance -= (fatigue - 800D) * FATIGUE_PERFORMANCE_MODIFIER /4;
                // e.g. f = 1000, p = 1.0 - 500 * .0001/4 = 1.0 - 0.05/4 = 1.0 - .0125 -> reduces by 1.25% on each frame
            }

            // High stress reduces performance.
            if (stress > 90D) {
                tempPerformance -= (stress - 90D) * STRESS_PERFORMANCE_MODIFIER/2;
            }
            else if (stress > 70D) {
                tempPerformance -= (stress - 70D) * STRESS_PERFORMANCE_MODIFIER/4;
                //e.g. p = 100 - 10 * .005 /3 = 1 - .05/4 -> reduces by .0125  or  1.25%  on each frame
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
     */
    public boolean hasSeriousMedicalProblems() {
        boolean result = false;
        Iterator<HealthProblem> meds = getProblems().iterator();
        while (meds.hasNext()) {
            if (meds.next().getIllness().getSeriousness() >= 50) result = true;
        }
        return result;
    }

    /**
     * Gets the oxygen consumption rate per Sol.
     * @return oxygen consumed (kg/Sol)
     * @throws Exception if error in configuration.
     */
    public static double getOxygenConsumptionRate() {
    	//if (personConfig == null)
    	//	personConfig = SimulationConfig.instance().getPersonConfiguration();
        return o2_consumption;//personConfig.getNominalO2ConsumptionRate();
    }

    /**
     * Gets the water consumption rate per Sol.
     * @return water consumed (kg/Sol)
     * @throws Exception if error in configuration.
     */
    public static double getWaterConsumptionRate() {
    	//if (personConfig == null)
    	//	personConfig = SimulationConfig.instance().getPersonConfiguration();;
        return h2o_consumption; //personConfig.getWaterConsumptionRate();
    }

    /**
     * Gets the food consumption rate per Sol.
     * @return food consumed (kg/Sol)
     * @throws Exception if error in configuration.
     */
    public static double getFoodConsumptionRate() {
    	//if (personConfig == null)
    	//	personConfig = SimulationConfig.instance().getPersonConfiguration();
        return food_consumption;//personConfig.getFoodConsumptionRate();
    }

    /**
     * Gets the dessert consumption rate per Sol.
     * @return dessert consumed (kg/Sol)
     * @throws Exception if error in configuration.
     */
    public static double getDessertConsumptionRate() {
    	//if (personConfig == null)
    	//	personConfig = SimulationConfig.instance().getPersonConfiguration();
        return dessert_consumption;//personConfig.getDessertConsumptionRate();
    }

    /**
     * Gets a list of medication affecting the person.
     * @return list of medication.
     */
    public List<Medication> getMedicationList() {
        return new ArrayList<Medication>(medicationList);
    }

    /**
     * Checks if the person is affected by the given medication.
     * @param medicationName the name of the medication.
     * @return true if person is affected by it.
     */
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

    /**
     * Adds a medication that affects the person.
     * @param medication the medication to add.
     */
    public void addMedication(Medication medication) {
        if (medication == null)
            throw new IllegalArgumentException("medication is null");
        medicationList.add(medication);
    }

    /**
     * Gets the key of the Sleep Cycle Map with the highest weight
     * @return int[] the two best times in integer
     */
    // 2015-12-05 Added getBestKeySleepCycle()
    public int[] getBestKeySleepCycle() {
    	int largest[] = {0,0};

    	Iterator<Integer> i = sleepCycleMap.keySet().iterator();
    	while (i.hasNext()) {
            int key = i.next();
    		int value = sleepCycleMap.get(key);
            if (value > largest[0]) {
            	largest[1] = largest[0];
            	largest[0] = key;
            }
            else if (value > largest[1])
            	largest[1] = key;

        }

    	return largest;
    }

    /**
     * Updates the weight of the Sleep Cycle Map
     * @param millisols the time
     * @param updateType increase or reduce
     */
    // 2015-12-05 Added updateValueSleepCycle()
    public void updateValueSleepCycle(int millisols, boolean updateType) {
    	// set HEAT_MAP_RESOLUTION of discrete sleep periods
    	millisols = (millisols / SLEEP_MAP_RESOLUTION )* SLEEP_MAP_RESOLUTION;
    	int currentValue = 0;

		int d = millisols-SLEEP_MAP_RESOLUTION;
		int d2 = millisols - 2*SLEEP_MAP_RESOLUTION;

		if (d <= 0)
			d = 1000 + millisols - SLEEP_MAP_RESOLUTION;

		if (d2 <= 0)
			d2 = 1000 + millisols - 2*SLEEP_MAP_RESOLUTION;


		int a = millisols + SLEEP_MAP_RESOLUTION;
		int a2 = millisols + 2*SLEEP_MAP_RESOLUTION;

		if (a > 1000)
			a = millisols + SLEEP_MAP_RESOLUTION - 1000;

		if (a2 > 1000)
			a2 = millisols + 2*SLEEP_MAP_RESOLUTION - 1000;

    	if (sleepCycleMap.containsKey(millisols)) {
    		currentValue = sleepCycleMap.get(millisols);

    		if (updateType) {
        		// Increase the central weight value by 30%
    			sleepCycleMap.put(millisols, (int) (currentValue *.7 + MAX_WEIGHT * .3));

    			int c2 = (int) (currentValue *.95 + MAX_WEIGHT * .075);
    			int c = (int) (currentValue *.85 + MAX_WEIGHT * .15);

   				sleepCycleMap.put(d2, c2);
    			sleepCycleMap.put(d, c);
    			sleepCycleMap.put(a, c);
       			sleepCycleMap.put(a2, c2);

    		}
    		else {

        		// Reduce the central weight value by 10%
    			sleepCycleMap.put(millisols, (int) (currentValue/1.1));

    			int b = (int) (currentValue/1.05);
    			int b2 = (int) (currentValue/1.025);

    			sleepCycleMap.put(d2, b2);
    			sleepCycleMap.put(d, b);
    			sleepCycleMap.put(a, b);
    			sleepCycleMap.put(a2, b2);

    		}
    	}
    	else {
    		// For the first time, create the central weight value with 10% of MAX_WEIGHT
			sleepCycleMap.put(millisols, (int) (MAX_WEIGHT * .1));

			int e = (int) (MAX_WEIGHT * .05);
			int e2 = (int) (MAX_WEIGHT * .025);

    		sleepCycleMap.put(d2, e2);
    		sleepCycleMap.put(d, e);
			sleepCycleMap.put(a, e);
			sleepCycleMap.put(a2, e2);
    	}

		//System.out.println(person + "'s sleepHabitMap : " + sleepHabitMap);
    }

    /**
     * Scales down the weight of the Sleep Habit Map
     */
    // 2015-12-05 Added inflateSleepHabit()
    public void inflateSleepHabit() {
    	Iterator<Integer> i = sleepCycleMap.keySet().iterator();
    	while (i.hasNext()) {
            int key = i.next();
    		int value = sleepCycleMap.get(key);

    		if (value > MAX_WEIGHT) {
    			value = MAX_WEIGHT;
    			// need to scale down all values, just in case
    			Iterator<Integer> j = sleepCycleMap.keySet().iterator();
    	    	while (j.hasNext()) {
    	            int key1 = j.next();
    	    		int value1 = sleepCycleMap.get(key1);
    	    		value1 = (int) (value1/INFLATION/INFLATION);
    	            sleepCycleMap.put(key1, value1);
    	        }
    		}

    		value = (int) (value/INFLATION);
            sleepCycleMap.put(key, value);
        }
    }

	public int getNumSleep() {
		return numSleep;
	}

	public int getSuppressHabit() {
		return suppressHabit;
	}

	public int getSpaceOut() {
		return spaceOut;
	}

	public void setNumSleep(int value) {
		numSleep = value;
	}

	public void setSuppressHabit(int value) {
		suppressHabit = value;
	}

	public void setSpaceOut(int value) {
		spaceOut = value;
	}

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        deathDetails = null;
        //problems.clear();
        problems = null;
        serious = null;
        person = null;
        //personConfig = null;
        //if (medicationList != null) medicationList.clear();
        medicationList = null;
    }
}
