/**
 * Mars Simulation Project
 * ArrivingSettlement.java
 * @version 3.06 2013-10-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.settlement;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.interplanetary.transport.TransportEvent;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
    
    /**
     * Commits a set of modifications for the arriving settlement.
     */
    public void commitModification() {
        HistoricalEvent newEvent = new TransportEvent(this, TransportEvent.TRANSPORT_ITEM_MODIFIED, 
                "Arriving settlement modified");
        Simulation.instance().getEventManager().registerNewEvent(newEvent);  
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
        
        // Create new equipment.
        createNewEquipment(newSettlement);
        
        // Create new parts.
        createNewParts(newSettlement);
        
        // Create new resources.
        createNewResources(newSettlement);
        
        // Create new vehicles.
        createNewVehicles(newSettlement);
    }

    /**
     * Create the new arriving settlement.
     */
    private Settlement createNewSettlement() {

        // Create new settlement with unit manager.
        UnitManager unitManager = Simulation.instance().getUnitManager();
        Settlement newSettlement = new Settlement(name, template, landingLocation, populationNum);
        unitManager.addUnit(newSettlement);
        
        // Add new settlement to credit manager.
        Simulation.instance().getCreditManager().addSettlement(newSettlement);

        return newSettlement;
    }

    /**
     * Create the new immigrants arriving with the settlement.
     * @param newSettlement the new settlement.
     */
    private void createNewImmigrants(Settlement newSettlement) {

        Collection<Person> immigrants = new ConcurrentLinkedQueue<Person>();
        UnitManager unitManager = Simulation.instance().getUnitManager();
        RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
        for (int x = 0; x < populationNum; x++) {
            PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
            String gender = Person.FEMALE;
            if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) gender = Person.MALE;
            String birthplace = "Earth"; //TODO: randomize from list of countries/federations
            String immigrantName = unitManager.getNewName(UnitManager.PERSON, null, gender);
            Person immigrant = new Person(immigrantName, gender, birthplace, newSettlement);
            unitManager.addUnit(immigrant);
            relationshipManager.addNewImmigrant(immigrant, immigrants);
            immigrants.add(immigrant);
            logger.info(immigrantName + " arrives on Mars at " + newSettlement.getName());
        }
    }
    
    /**
     * Create the new settlement's equipment.
     * @param newSettlement the new settlement.
     */
    private void createNewEquipment(Settlement newSettlement) {
        
        SettlementTemplate template = SimulationConfig.instance().getSettlementConfiguration().
                getSettlementTemplate(getTemplate());
        UnitManager unitManager = Simulation.instance().getUnitManager();
        Iterator<String> equipmentI = template.getEquipment().keySet().iterator();
        while (equipmentI.hasNext()) {
            String equipmentType = equipmentI.next();
            int number = template.getEquipment().get(equipmentType);
            for (int x=0; x < number; x++) {
                Equipment equipment = EquipmentFactory.getEquipment(equipmentType, 
                        newSettlement.getCoordinates(), false);
                equipment.setName(unitManager.getNewName(UnitManager.EQUIPMENT, equipmentType, null));
                newSettlement.getInventory().storeUnit(equipment);
            }
        }
    }
    
    /**
     * Create the new settlement's parts.
     * @param newSettlement the new settlement.
     */
    private void createNewParts(Settlement newSettlement) {
        
        SettlementTemplate template = SimulationConfig.instance().getSettlementConfiguration().
                getSettlementTemplate(getTemplate());
        Iterator<Part> partsI = template.getParts().keySet().iterator();
        while (partsI.hasNext()) {
            Part part = partsI.next();
            int number = template.getParts().get(part);
            newSettlement.getInventory().storeItemResources(part, number);
        }
    }
    
    /**
     * Create the new settlement's resources.
     * @param newSettlement the new settlement.
     */
    private void createNewResources(Settlement newSettlement) {
        
        SettlementTemplate template = SimulationConfig.instance().getSettlementConfiguration().
                getSettlementTemplate(getTemplate());
        Iterator<AmountResource> resourcesI = template.getResources().keySet().iterator();
        while (resourcesI.hasNext()) {
            AmountResource resource = resourcesI.next();
            double amount = template.getResources().get(resource);
            double capacity = newSettlement.getInventory().getAmountResourceRemainingCapacity(
                    resource, true, false);
            if (amount > capacity) amount = capacity;
            newSettlement.getInventory().storeAmountResource(resource, amount, true);
        }
    }
    
    /**
     * Create the new settlement's vehicles.
     * @param newSettlement the new settlement.
     */
    private void createNewVehicles(Settlement newSettlement) {
        
        SettlementTemplate template = SimulationConfig.instance().getSettlementConfiguration().
                getSettlementTemplate(getTemplate());
        UnitManager unitManager = Simulation.instance().getUnitManager();
        Iterator<String> vehicleI = template.getVehicles().keySet().iterator();
        while (vehicleI.hasNext()) {
            String vehicleType = vehicleI.next();
            int number = template.getVehicles().get(vehicleType);
            for (int x = 0; x < number; x++) {
                Vehicle vehicle = null;
                if (LightUtilityVehicle.NAME.equals(vehicleType)) {
                    String name = unitManager.getNewName(UnitManager.VEHICLE, "LUV", null);
                    vehicle = new LightUtilityVehicle(name, vehicleType, newSettlement);
                } else {
                    String name = unitManager.getNewName(UnitManager.VEHICLE, null, null);
                    vehicle = new Rover(name, vehicleType, newSettlement);
                }
                unitManager.addUnit(vehicle);
            }
        }
    }
}