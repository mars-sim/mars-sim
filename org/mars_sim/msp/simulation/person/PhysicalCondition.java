/**
 * Mars Simulation Project
 * PhysicalCondition.java
 * @version 2.75 2004-03-10
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.medical.*;

/**
 * This class represents the Physical Condition of a Person. It is models the
 * Persons health and physical charactertics.
 */
public class PhysicalCondition implements Serializable {

    // Static values
    private static int MIN_VALUE = 0;
    private static int MAX_VALUE = 1;
    private static final String START_MEDICAL = "Has Illness";
    private static final String STOP_MEDICAL = "Cured";

    // Data members
    private DeathInfo deathDetails;     // Details of persons death
    private HashMap problems;           // Injury/Illness effecting person
    private HealthProblem serious;      // Mosr serious problem
    private double fatigue;             // Person's fatigue level
    private double hunger;              // Person's hunger level
    private double performance;         // Performance factor
    private MedicalManager medic;       // Simulation Medical manager
    private Person person;              // Person's of this physical
    private boolean alive;              // True if person is alive.

    /**
     * Construct a Physical Condition instance.
     *
     * @param person The person requiring a physical presence.
     * @param mars main simulation control.
     *
     */
    public PhysicalCondition(Person newPerson, Mars mars) {
        deathDetails = null;
        person = newPerson;
        problems = new HashMap();
        performance = 1.0D;

        medic = mars.getMedicalManager();
        fatigue = RandomUtil.getRandomDouble(1000D);
        hunger = RandomUtil.getRandomDouble(1000D);
        alive = true;
    }

    /**
     * Can any of the existing problems be healed by this FirstAidUnit
     * @param unit FirstAidUnit that can heal.
     */
    boolean canTreatProblems(MedicalAid unit) {
        boolean result = false;
        
        Iterator iter = problems.values().iterator();
        while(iter.hasNext()) {
            HealthProblem prob = (HealthProblem) iter.next();
            if (unit.canTreatProblem(prob)) result = true;
        }
        
        return result;
    }
    
    /**
     * Request treatment at the medical aid for all the person's health
     * problems that can be treated there.
     *
     * @param aid the medical aid the person is using.
     */
    void requestAllTreatments(MedicalAid aid) {
        Iterator iter = problems.values().iterator();
        while(iter.hasNext()) {
            HealthProblem prob = (HealthProblem) iter.next();
            if (aid.canTreatProblem(prob)) {
                try {
                    aid.requestTreatment(prob);
                }
                catch (Exception e) {}
            }
        }
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
     * @param config person configuration.
     * @return True still alive.
     */
    boolean timePassing(double time, LifeSupport support,
                        PersonConfig config) {
        
        // Check the existing problems
        if (!problems.isEmpty()) {
            List newProblems = new ArrayList();

            Iterator iter = problems.values().iterator();
            while(!isDead() && iter.hasNext()) {
                HealthProblem problem = (HealthProblem) iter.next();

                // Advance each problem, they may change into a worse problem.
                // If the current is completed or a new problem exists then
                // remove this one.
                Complaint next = problem.timePassing(time, this);

                if (problem.getCured() || (next != null)) iter.remove();

                // If a new problem, check it doesn't exist already
                if (next != null) newProblems.add(next);
            }

            // Add the new problems
            Iterator newIter = newProblems.iterator();
            while(newIter.hasNext()) {
                Complaint illness = (Complaint)newIter.next();
                if (!problems.containsKey(illness)) {
                    problems.put(illness, new HealthProblem(illness, person,
                                                            person.getAccessibleAid()));
                }
            }
        }

        // Has the person died ?
        if (isDead()) return false;

        // See if a random illness happens.
        Complaint randomComplaint = medic.getProbableComplaint(person);

        // New complaint must not exist already
        if ((randomComplaint != null) && !problems.containsKey(randomComplaint)) {
            problems.put(randomComplaint, new HealthProblem(randomComplaint, person,
            	person.getAccessibleAid()));
        }

        // Consume necessary oxygen and water.
        try {
        	consumeOxygen(support, config.getOxygenConsumptionRate() * (time / 1000D));
        	consumeWater(support, config.getWaterConsumptionRate() * (time / 1000D));
        	requireAirPressure(support, config.getMinAirPressure());
        	requireTemperature(support, config.getMinTemperature(), config.getMaxTemperature());
        }
        catch (Exception e) {
        	System.err.println(person.getName() + " - Error in lifesupport needs: " + e.getMessage());
        }

        // Build up fatigue & hunger for given time passing.
        fatigue += time;
        hunger += time;

		// Calculate performance and most serious illness.
        recalculate();

        return (!isDead());
    }

    /** Adds a new medical complaint to the person.
     *  @param complaint the new medical complaint
     */
    public void addMedicalComplaint(Complaint complaint) {

        if ((complaint != null) && !problems.containsKey(complaint)) {
	        HealthProblem problem = new HealthProblem(complaint, person, person.getAccessibleAid());
	        problems.put(complaint, problem);
	        recalculate();
	    }
    }

    /** Person consumes given amount of food
     *  @param amount amount of food to consume (in kg).
     *  @param holder unit to get food from
     *  @param props Simulation proerties.
     */
    public void consumeFood(double amount, Unit container,
                            SimulationProperties props) {
        double amountRecieved =
                container.getInventory().removeResource(Resource.FOOD, amount);

        if (checkResourceConsumption(amountRecieved, amount,
                                     MIN_VALUE, medic.getStarvation())) {
            recalculate();
        }
    }

    /** Person consumes given amount of oxygen
     *  @param support Life support system providing oxygen.
     *  @param amount amount of oxygen to consume (in kg)
     *  @return new problem added.
     */
    private boolean consumeOxygen(LifeSupport support, double amount) {
        double amountRecieved = support.provideOxygen(amount);

        return checkResourceConsumption(amountRecieved, amount / 2D,
                                        MIN_VALUE, medic.getSuffocation());
    }

    /** Person consumes given amount of water
     *  @param support Life support system providing water.
     *  @param amount amount of water to consume (in kg)
     *  @return new problem added.
     */
    private boolean consumeWater(LifeSupport support, double amount) {
        double amountReceived = support.provideWater(amount);

        return checkResourceConsumption(amountReceived, amount / 2D,
                                        MIN_VALUE, medic.getDehydration());
    }

    /**
     * This method checks the consume values of a resource. If the
     * actual is less than the required then a HealthProblem is
     * generated. If the required amount is statisfied, then any problem
     * is recovered.
     *
     * @param actual The amount of resource provided.
     * @param require The amount of resouce required.
     * @param complaint Problem assocoiated to this resource.
     * @return Has a new problem been added.
     */
    private boolean checkResourceConsumption(double actual, double required,
            int bounds, Complaint complaint) {
                
        boolean newProblem = false;
        if ((bounds == MIN_VALUE) && (actual < required)) newProblem = true;
        if ((bounds == MAX_VALUE) && (actual > required)) newProblem = true;

        if (newProblem) {
            if (!problems.containsKey(complaint)) {
                problems.put(complaint, new HealthProblem(complaint, person,
                    person.getAccessibleAid()));
            }
        }
        else {
            //Is the person suffering from the illness, if so recovery
            // as the amount has been provided
            HealthProblem illness = (HealthProblem)problems.get(complaint);
            if (illness != null) illness.startRecovery();
        }
        return newProblem;
    }

    /**
     * Person requires minimum air pressure.
     * @param support Life support system providing air pressure.
     * @param pressure minimum air pressure person requires (in atm)
     * @return new problem added.
     */
    private boolean requireAirPressure(LifeSupport support, double pressure) {
        return checkResourceConsumption(support.getAirPressure(), pressure,
                                        MIN_VALUE, medic.getDecompression());
    }

    /**
     * Person requires minimum temperature.
     * @param support Life support system providing temperature.
     * @param temperature minimum temperature person requires (in degrees Celsius)
     * @return new problem added.
     */
    private boolean requireTemperature(LifeSupport support, double minTemperature,
            double maxTemperature) {
                
        boolean freeze = checkResourceConsumption(support.getTemperature(),
                minTemperature, MIN_VALUE, medic.getFreezing());
        boolean hot = checkResourceConsumption(support.getTemperature(),
	        maxTemperature, MAX_VALUE, medic.getHeatStroke());
        return freeze || hot;
    }

    /**
     * Get the details of this Person's death.
     * @return Deatial of the death, will be null if person os still alive.
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
     * Get the performance factor that effect Person with the complaint.
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceFactor() {
        //return performance * ((1000 - fatigue)/1000);
        return performance;
    }

    Person getPerson() {
        return person;
    }

    /**
     * Define the fatigue setting for this person
     * @param fatigue New fatigue.
     */
    void setFatigue(double fatigue) {
        this.fatigue = fatigue;
    }

    /** Gets the person's hunger level
     *  @return person's hunger
     */
    public double getHunger() {
        return hunger;
    }

    /**
     * Define the hunger setting for this person
     * @param hunger New hunger.
     */
    void setHunger(double hunger) {
        this.hunger = hunger;
    }

    /**
     * This Person is now dead.
     * @param illness THe Compliant that makes person dead.
     */
    public void setDead(HealthProblem illness) {
        fatigue = 0;
        hunger = 0;
        alive = false;

        deathDetails = new DeathInfo(person);

		// Create medical event for death.
		MedicalEvent event = new MedicalEvent(person, illness, MedicalEvent.DEATH);
		person.getMars().getEventManager().registerNewEvent(event);
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
     * Get a string description of the most serious health situation.
     * @return A string containing the current illness if any.
     */
    public String getHealthSituation() {
        String situation = "Well";
        if (serious != null) {
            if (isDead()) {
                situation = "Dead, " + serious.getIllness().getName();
            }
            else {
                situation = serious.getSituation();
            }
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
    public Collection getProblems() {
        return problems.values();
    }

    /**
     * Calculate the most serious problem and the most performanc effecting
     */
    private void recalculate() {

        performance = 1.0D;
        serious = null;

        // Check the existing problems. find most serious & performance
        // effecting
        Iterator iter = problems.values().iterator();
        while(iter.hasNext()) {
            HealthProblem problem = (HealthProblem)iter.next();
            double factor = problem.getPerformanceFactor();
            if (factor < performance) {
                performance = factor;
            }

            if ((serious == null) || (serious.getIllness().getSeriousness() <
                                      problem.getIllness().getSeriousness())) {
                serious = problem;
            }
        }
        
        // High fatigue reduces performance.
        if (fatigue > 1000D) performance -= (fatigue - 1000D) * .0003D;
        
        // High hunger reduces performance.
        if (hunger > 1000D) performance -= (hunger - 1000D) * .0003D;
        
        if (performance < 0D) performance = 0D;
    }
}