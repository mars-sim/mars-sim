/**
 * Mars Simulation Project
 * PhysicalCondition.java
 * @version 2.74 2002-03-11
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


    private boolean isAlive;            // Is the person alive
    private HashMap problems;           // Injury/Illness effecting person
    private HealthProblem serious;     // Mosr serious problem
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
        isAlive = true;
        person = newPerson;
        problems = new HashMap();
        performance = 1.0D;

        medic = mars.getMedicalManager();
        fatigue = RandomUtil.getRandomDouble(1000D);
        hunger = RandomUtil.getRandomDouble(1000D);
    }

    /**
     * Can any of the existing problem be heeled by this FirstAidUnit
     * @param unit FirstAidUnit that can heal.
     */
    void canStartRecovery(MedicalAid unit) {
        Iterator iter = problems.values().iterator();
        while(iter.hasNext()) {
            HealthProblem prob = (HealthProblem)iter.next();
            prob.canStartRecovery(unit);
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
            while(getAlive() && iter.hasNext()) {
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
	    recalculate |= requireTemperature(support, props.getPersonMinTemperature());

        // Build up fatigue & hunger for given time passing.
        fatigue += time;
        hunger += time;

        if (recalculate) {
            recalculate();
        }
        return isAlive;
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
                                     medic.getStarvation())) {
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

        return checkResourceConsumption(amountRecieved, amount,
                                        medic.getSuffocation());
    }

    /** Person consumes given amount of water
     *
     *  @param support Life support system providing water.
     *  @param amount amount of water to consume (in kg)
     *  @return new problem added.
     */
    private boolean consumeWater(LifeSupport support, double amount) {
        double amountReceived = support.provideWater(amount);

        return checkResourceConsumption(amountReceived, amount,
                                        medic.getDehydration());
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
                                Complaint complaint) {
        boolean newProblem = false;

        if (actual < required) {
            problems.put(complaint, new HealthProblem(complaint, person,
                                                      person.getAccessibleAid()));
            newProblem = true;
        }
        else {
            //Is the person suffering from the illness, if so recovery
            // as the amount has been provided
            HealthProblem illness = (HealthProblem)problems.get(complaint);
            if (illness != null) {
                illness.startRecovery();
            }
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
                                        medic.getDecompression());
    }

    /**
     * Person requires minimum temperature.
     * @param support Life support system providing temperature.
     * @param temperature minimum temperature person requires (in degrees Celsius)
     * @return new problem added.
     */
    private boolean requireTemperature(LifeSupport support, double temperature) {
        return checkResourceConsumption(support.getTemperature(), temperature,
                                        medic.getFreezing());
    }

    /**
     * Predicate to check if the Person is alive.
     * @return Boolean of alive state.
     */
    public boolean getAlive() {
        return isAlive;
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
     */
    public void setDead() {
        fatigue = 0;
        hunger = 0;
        isAlive = false;
    }

    /**
     * Get a string description of the most serious health situation.
     * @return A string containing the current illness if any.
     */
    public String getHealthSituation() {
        String situation = "Well";
        if (serious != null) {
            if (!isAlive) {
                situation = "Dead, " + serious.getIllness().getName();
            }
            else {
                situation = serious.getSituation();
            }
        }
        return situation;
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
            Complaint illness = problem.getIllness();
            if (illness.getPerformanceFactor() < performance) {
                performance = illness.getPerformanceFactor();
            }

            if ((serious == null) || (serious.getIllness().getSeriousness() <
                                      illness.getSeriousness())) {
                serious = problem;
            }
        }
    }
}
