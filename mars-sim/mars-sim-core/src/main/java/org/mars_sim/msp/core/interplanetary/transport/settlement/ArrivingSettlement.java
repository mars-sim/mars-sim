/**
 * Mars Simulation Project
 * ArrivingSettlement.java
 * @version 3.04 2013-04-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.settlement;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * A new arriving settlement from Earth.
 */
public class ArrivingSettlement implements Transportable, Serializable {

    private static Logger logger = Logger.getLogger(ArrivingSettlement.class.getName());

    // Data members.
    private String name;
    private String template;
    private String transitState;
    private MarsClock launchDate;
    private MarsClock arrivalDate;
    private Coordinates landingLocation;
    private int populationNum;

    /**
     * Constructor
     * @param name the name of the arriving settlement.
     * @param template the design template for the settlement.
     * @param arrivalDate the arrival date.
     * @param landingLocation the landing location.
     * @param populationNum the population of new immigrants arriving with the settlement.
     */
    public ArrivingSettlement(String name, String template, 
            MarsClock arrivalDate, Coordinates landingLocation, int populationNum) {

        this.name = name;
        this.template = template;
        this.arrivalDate = arrivalDate;
        this.landingLocation = landingLocation;
        this.populationNum = populationNum;
    }

    /**
     * Gets the name of the arriving settlement.
     * @return settlement name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the arriving settlement.
     * @param name settlement name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the design template of the arriving settlement.
     * @return the settlement template string.
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the design template of the arriving settlement.
     * @param template the settlement template string.
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Gets the transit state of the settlement.
     * @return transit state string.
     */
    public String getTransitState() {
        return transitState;
    }

    /**
     * Sets the transit state of the settlement.
     * @param transitState the transit state string.
     */
    public void setTransitState(String transitState) {
        this.transitState = transitState;
    }

    /**
     * Gets the launch date of the settlement.
     * @return the launch date.
     */
    public MarsClock getLaunchDate() {
        return launchDate;
    }

    /**
     * Sets the launch date of the settlement.
     * @param launchDate the launch date.
     */
    public void setLaunchDate(MarsClock launchDate) {
        this.launchDate = launchDate;
    }

    /**
     * Gets the arrival date of the settlement.
     * @return the arrival date.
     */
    public MarsClock getArrivalDate() {
        return arrivalDate;
    }

    /**
     * Sets the arrival date of the settlement.
     * @param arrivalDate the arrival date.
     */
    public void setArrivalDate(MarsClock arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    /**
     * Gets the landing location for the arriving settlement.
     * @return landing location coordinates.
     */
    public Coordinates getLandingLocation() {
        return landingLocation;
    }

    /**
     * Sets the landing location for the arriving settlement.
     * @param landingLocation the landing location coordinates.
     */
    public void setLandingLocation(Coordinates landingLocation) {
        this.landingLocation = landingLocation;
    }

    /**
     * Gets the population of the arriving settlement.
     * @return population number.
     */
    public int getPopulationNum() {
        return populationNum;
    }

    /**
     * Sets the population of the arriving settlement.
     * @param populationNum the population number.
     */
    public void setPopulationNum(int populationNum) {
        this.populationNum = populationNum;
    }

    @Override
    public void destroy() {
        name = null;
        template = null;
        transitState = null;
        launchDate = null;
        arrivalDate = null;
        landingLocation = null;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(getName());
        buff.append(": ");
        buff.append(getArrivalDate().getDateString());
        return buff.toString();
    }

    @Override
    public int compareTo(Transportable o) {
        int result = 0;

        double arrivalTimeDiff = MarsClock.getTimeDiff(arrivalDate, o.getArrivalDate());
        if (arrivalTimeDiff < 0D) {
            result = -1;
        }
        else if (arrivalTimeDiff > 0D) {
            result = 1;
        }
        else {
            // If arrival time is the same, compare by settlement name alphabetically.
            result = name.compareTo(o.getName());
        }

        return result;
    }

    @Override
    public void performArrival() {
        // Create new settlement.
        Settlement newSettlement = createNewSettlement();

        // Create new immigrants with arriving settlement.
        createNewImmigrants(newSettlement);
    }

    /**
     * Create the new arriving settlement.
     */
    private Settlement createNewSettlement() {

        // Create new settlement with unit manager.
        UnitManager unitManager = Simulation.instance().getUnitManager();
        Settlement newSettlement = new Settlement(name, template, landingLocation, populationNum);
        unitManager.addUnit(newSettlement);

        return newSettlement;
    }

    /**
     * Create the new immigrants arriving with the settlement.
     */
    private void createNewImmigrants(Settlement newSettlement) {

        Collection<Person> immigrants = new ConcurrentLinkedQueue<Person>();
        UnitManager unitManager = Simulation.instance().getUnitManager();
        RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
        for (int x = 0; x < populationNum; x++) {
            PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
            String gender = Person.FEMALE;
            if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) gender = Person.MALE;
            String immigrantName = unitManager.getNewName(UnitManager.PERSON, null, gender);
            Person immigrant = new Person(immigrantName, gender, newSettlement);
            unitManager.addUnit(immigrant);
            relationshipManager.addNewImmigrant(immigrant, immigrants);
            immigrants.add(immigrant);
            logger.info(immigrantName + " arrives on Mars at " + newSettlement.getName());
        }
    }
}