/**
 * Mars Simulation Project
 * PhysicalCondition.java
 * @version 2.74 2002-05-18
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.medical.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class represents the Physical Condition of a Person. It is models the
 * Persons health and physical charactertics.
 */
public class PhysicalCondition implements Serializable {

    // Static values
    private static int MIN_VALUE = 0;
    private static int MAX_VALUE = 1;

    // Data members
    private DeathInfo deathDetails;     // Details of persons death
    private HashMap problems;           // Injury/Illness effecting person
    private HealthProblem serious;      // Mosr serious problem
    private double fatigue;             // Person's fatigue level
    private double hunger;              // Person's hunger level
    private double performance;         // Performance factor
    private MedicalManager medic;       // Simulation Medical manager
    private Person person;              // Person's of this physical

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
    }

    /**
     * Can any of the existing problems be healed by this FirstAidUnit
     * @param unit FirstAidUnit that can heal.
     */
    void canStartTreatment(MedicalAid unit) {
        Iterator iter = problems.values().iterator();
        while(iter.hasNext()) {
            HealthProblem prob = (HealthProblem)iter.next();
            prob.canStartTreatment(unit);
        }
    }

    /**
     * The Physical condition should be updated to reflect a passing of time.
     * This method has to check the recover or degradation of any current
     * illness. The progression of this time period may result in the illness
     * turning fatal.
     * It also updated the hunder and fatigue status
     *
     * @param time amount of time passing (in millisols)
     * @param support Life support system.
     * @param props Simulation properties.
     * @return True still alive.
     */
    boolean timePassing(double time, LifeSupport support,
                        SimulationProperties props) {
        boolean recalculate = false;

        // Check the existing problems
        if (!problems.isEmpty()) {
            ArrayList newProblems = new ArrayList();

            Iterator iter = problems.values().iterator();
            while((deathDetails == null) && iter.hasNext()) {
                HealthProblem problem = (HealthProblem)iter.next();

                // Advance each problem, they may change into a worse problem.
                // If the current is completed or a new problem exists then
                // remove this one.
                Complaint next = problem.timePassing(time, this);
                if (problem.getCured() || (next != null)) {
                    iter.remove();
                    recalculate = true;
                }

                // If a new problem, check it doesn't exist already
                if (next != null) {
                    newProblems.add(next);

                    recalculate = true;
                }
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
        if (deathDetails != null) {
            return false;
        }

        // See if a random illness catches this Person out if nothing new
        if (!recalculate) {
            Complaint randomComplaint = medic.getProbableComplaint(person);

            // New complaint must not exist already
            if ((randomComplaint != null) &&
                        !problems.containsKey(randomComplaint)) {
                problems.put(randomComplaint,
                                new HealthProblem(randomComplaint, person,
                                                  person.getAccessibleAid()));
                recalculate = true;
            }
        }

        // Consume necessary oxygen and water.
        recalculate |= consumeOxygen(support, props.getPersonOxygenConsumption() * (time / 1000D));
        recalculate |= consumeWater(support, props.getPersonWaterConsumption() * (time / 1000D));
        recalculate |= requireAirPressure(support, props.getPersonMinAirPressure());
        recalculate |= requireTemperature(support, props.getPersonMinTemperature(),
                props.getPersonMaxTemperature());

        // Build up fatigue & hunger for given time passing.
        fatigue += time;
        hunger += time;

        if (recalculate) {
            recalculate();
        }
        return (deathDetails == null);
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
                container.getInventory().removeResource(Inventory.FOOD, amount);

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
    public void setDead(Complaint illness) {
        fatigue = 0;
        hunger = 0;

        deathDetails = new DeathInfo(person);
    }

    /**
     * Get a string description of the most serious health situation.
     * @return A string containing the current illness if any.
     */
    public String getHealthSituation() {
        String situation = "Well";
        if (serious != null) {
            if (deathDetails != null) {
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
    }
}
