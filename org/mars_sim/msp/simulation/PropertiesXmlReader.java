/**
 * Mars Simulation Project
 * PropertiesXmlReader.java
 * @version 2.73 2001-12-09
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/** The PropertiesXmlReader class parses the properties.xml XML file and
 *  reads simulation properties.
 */
class PropertiesXmlReader extends MspXmlReader {

    // XML element types
    private static int PROPERTY_LIST = 0;
    private static int TIME_RATIO = 1;
    private static int PERSON_PROPERTIES = 2;
    private static int OXYGEN_CONSUMPTION = 3;
    private static int WATER_CONSUMPTION = 4;
    private static int FOOD_CONSUMPTION = 5;
    private static int ROVER_PROPERTIES = 6;
    private static int OXYGEN_STORAGE_CAPACITY = 7;
    private static int WATER_STORAGE_CAPACITY = 8;
    private static int FOOD_STORAGE_CAPACITY = 9;
    private static int FUEL_STORAGE_CAPACITY = 10;
    private static int FUEL_EFFICIENCY = 11;
    private static int SETTLEMENT_PROPERTIES = 12;
    private static int RANGE = 13;
    private static int GREENHOUSE_FULL_HARVEST = 14;
    private static int GREENHOUSE_GROWING_CYCLE = 15;

    // Data members
    private int elementType; // The current element type being parsed
    private int propertyCatagory; // The property catagory
    private double timeRatio; // The time ratio property
    private double personOxygenConsumption; // The person oxygen consumption property
    private double personWaterConsumption; // The person water consumption property
    private double personFoodConsumption; // The person food consumption property
    private double roverOxygenStorageCapacity; // The rover oxygen storage capacity property
    private double roverWaterStorageCapacity; // The rover water storage capacity property
    private double roverFoodStorageCapacity; // The rover food storage capacity property
    private double roverFuelStorageCapacity; // The rover fuel storage capacity property
    private double roverFuelEfficiency; // The rover fuel efficiency property
    private double roverRange; // The rover range property
    private double settlementOxygenStorageCapacity; // The settlement oxygen storage capacity property
    private double settlementWaterStorageCapacity; // The settlement water storage capacity property
    private double settlementFoodStorageCapacity; // The settlement food storage capacity property
    private double settlementFuelStorageCapacity; // The settlement fuel storage capacity property 
    private double greenhouseFullHarvest; // The greenhouse full harvest property
    private double greenhouseGrowingCycle; // The greenhouse growing cycle property

    /** Constructor */
    public PropertiesXmlReader() {
        super("conf/properties.xml");
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("PROPERTY_LIST")) {
            elementType = PROPERTY_LIST;
        }
        if (name.equals("TIME_RATIO")) {
            elementType = TIME_RATIO;
        }
        if (name.equals("PERSON_PROPERTIES")) {
            elementType = PERSON_PROPERTIES;
            propertyCatagory = PERSON_PROPERTIES;
        }
        if (name.equals("OXYGEN_CONSUMPTION")) {
            elementType = OXYGEN_CONSUMPTION;
        }
        if (name.equals("WATER_CONSUMPTION")) {
            elementType = WATER_CONSUMPTION;
        }
        if (name.equals("FOOD_CONSUMPTION")) {
            elementType = FOOD_CONSUMPTION;
        }
        if (name.equals("ROVER_PROPERTIES")) {
            elementType = ROVER_PROPERTIES;
            propertyCatagory = ROVER_PROPERTIES;
        }
        if (name.equals("OXYGEN_STORAGE_CAPACITY")) {
            elementType = OXYGEN_STORAGE_CAPACITY;
        }
        if (name.equals("WATER_STORAGE_CAPACITY")) {
            elementType = WATER_STORAGE_CAPACITY;
        }
        if (name.equals("FOOD_STORAGE_CAPACITY")) {
            elementType = FOOD_STORAGE_CAPACITY;
        }
        if (name.equals("FUEL_STORAGE_CAPACITY")) {
            elementType = FUEL_STORAGE_CAPACITY;
        }
        if (name.equals("FUEL_EFFICIENCY")) {
            elementType = FUEL_EFFICIENCY;
        }
        if (name.equals("RANGE")) {
            elementType = RANGE;
        }
        if (name.equals("SETTLEMENT_PROPERTIES")) {
            elementType = SETTLEMENT_PROPERTIES;
            propertyCatagory = SETTLEMENT_PROPERTIES;
        }
        if (name.equals("GREENHOUSE_GROWING_CYCLE")) {
            elementType = GREENHOUSE_GROWING_CYCLE;
        }
        if (name.equals("GREENHOUSE_FULL_HARVEST")) {
            elementType = GREENHOUSE_FULL_HARVEST;
        }
    }

    /** Handle the end of an element by printing an event.
     *  @param name the name of the ending element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) throws Exception {
        super.endElement(name);
      
        if (elementType == TIME_RATIO) {
            elementType = PROPERTY_LIST;
            return;
        }
        if (elementType == PERSON_PROPERTIES) {
            elementType = PROPERTY_LIST;
            propertyCatagory = -1;
            return;
        }        
        if (elementType == OXYGEN_CONSUMPTION) {
            elementType = PERSON_PROPERTIES;
            return;
        }
        if (elementType == WATER_CONSUMPTION) {
            elementType = PERSON_PROPERTIES;
            return;
        }
        if (elementType == FOOD_CONSUMPTION) {
            elementType = PERSON_PROPERTIES;
            return;
        }
        if (elementType == ROVER_PROPERTIES) {
            elementType = PROPERTY_LIST;
            propertyCatagory = -1;
            return;
        }
        if (elementType == OXYGEN_STORAGE_CAPACITY) {
            elementType = propertyCatagory;
            return;
        }
        if (elementType == WATER_STORAGE_CAPACITY) {
            elementType = propertyCatagory;
            return;
        }
        if (elementType == FOOD_STORAGE_CAPACITY) {
            elementType = propertyCatagory;
            return;
        }
        if (elementType == FUEL_STORAGE_CAPACITY) {
            elementType = propertyCatagory;
            return;
        }
        if (elementType == FUEL_EFFICIENCY) {
            elementType = ROVER_PROPERTIES;
            return;
        }
        if (elementType == RANGE) {
            elementType = ROVER_PROPERTIES;
            return;
        }
        if (elementType == SETTLEMENT_PROPERTIES) {
            elementType = PROPERTY_LIST;
            propertyCatagory = -1;
            return;
        }
        if (elementType == GREENHOUSE_FULL_HARVEST) {
            elementType = SETTLEMENT_PROPERTIES;
            return;
        }
        if (elementType == GREENHOUSE_GROWING_CYCLE) {
            elementType = SETTLEMENT_PROPERTIES;
            return;
        }
    }

    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();

        if (elementType == TIME_RATIO) {
            timeRatio = Double.parseDouble(data);
        }
        if (elementType == OXYGEN_CONSUMPTION) {
            personOxygenConsumption = Double.parseDouble(data);
        }
        if (elementType == WATER_CONSUMPTION) {
            personWaterConsumption = Double.parseDouble(data);
        }
        if (elementType == FOOD_CONSUMPTION) {
            personFoodConsumption = Double.parseDouble(data);
        }
        if (elementType == OXYGEN_STORAGE_CAPACITY) {
            double value = Double.parseDouble(data);
            if (propertyCatagory == ROVER_PROPERTIES) roverOxygenStorageCapacity = value;
            if (propertyCatagory == SETTLEMENT_PROPERTIES) settlementOxygenStorageCapacity = value;
        }
        if (elementType == WATER_STORAGE_CAPACITY) {
            double value = Double.parseDouble(data);
            if (propertyCatagory == ROVER_PROPERTIES) roverWaterStorageCapacity = value;
            if (propertyCatagory == SETTLEMENT_PROPERTIES) settlementWaterStorageCapacity = value;
        }
        if (elementType == FOOD_STORAGE_CAPACITY) {
            double value = Double.parseDouble(data);
            if (propertyCatagory == ROVER_PROPERTIES) roverFoodStorageCapacity = value;
            if (propertyCatagory == SETTLEMENT_PROPERTIES) settlementFoodStorageCapacity = value;
        }
        if (elementType == FUEL_STORAGE_CAPACITY) {
            double value = Double.parseDouble(data);
            if (propertyCatagory == ROVER_PROPERTIES) roverFuelStorageCapacity = value;
            if (propertyCatagory == SETTLEMENT_PROPERTIES) settlementFuelStorageCapacity = value;
        }
        if (elementType == FUEL_EFFICIENCY) {
            roverFuelEfficiency = Double.parseDouble(data);
        }
        if (elementType == RANGE) {
            roverRange = Double.parseDouble(data);
        }
        if (elementType == GREENHOUSE_FULL_HARVEST) {
            greenhouseFullHarvest = Double.parseDouble(data);
        }
        if (elementType == GREENHOUSE_GROWING_CYCLE) {
            greenhouseGrowingCycle = Double.parseDouble(data);
        }
    }

    /** Gets the time ratio property. 
     *  Value must be > 0.
     *  Default value is 1000.
     *  @return the ratio between simulation and real time 
     */
    public double getTimeRatio() {
        if (timeRatio <= 0) timeRatio = 1000D;
        return timeRatio;
    }

    /** Gets the person oxygen consumption property.
     *  Value must be >= 0.
     *  Default value is 1.0.
     *  @return the person oxygen consumption property
     */
    public double getPersonOxygenConsumption() {
        if (personOxygenConsumption < 0) personOxygenConsumption = 1D;
        return personOxygenConsumption;
    }

    /** Gets the person water consumption property.
     *  Value must be >= 0.
     *  Default value is 4.0.
     *  @return the person water consumption property
     */
    public double getPersonWaterConsumption() {
        if (personWaterConsumption < 0) personWaterConsumption = 4D;
        return personWaterConsumption;
    }

    /** Gets the person food consumption property.
     *  Value must be >= 0.
     *  Default value is 1.5.
     *  @return the person food consumption property
     */
    public double getPersonFoodConsumption() {
        if (personFoodConsumption < 0) personFoodConsumption = 1.5D;
        return personFoodConsumption;
    }

    /** Gets the rover oxygen storage capacity property.
     *  Value must be >= 0.
     *  Default value is 350.0.
     *  @return the rover oxygen storage capacity property
     */
    public double getRoverOxygenStorageCapacity() {
        if (roverOxygenStorageCapacity < 0) roverOxygenStorageCapacity = 350D;
        return roverOxygenStorageCapacity;
    }

    /** Gets the rover water storage capacity property.
     *  Value must be >= 0.
     *  Default value is 1400.0.
     *  @return the rover water storage capacity property
     */
    public double getRoverWaterStorageCapacity() {
        if (roverWaterStorageCapacity < 0) roverWaterStorageCapacity = 1400D;
        return roverWaterStorageCapacity;
    }

    /** Gets the rover food storage capacity property.
     *  Value must be >= 0.
     *  Default value is 525.0.
     *  @return the rover food storage capacity property
     */
    public double getRoverFoodStorageCapacity() {
        if (roverFoodStorageCapacity < 0) roverFoodStorageCapacity = 525D;
        return roverFoodStorageCapacity;
    }

    /** Gets the rover fuel storage capacity property.
     *  Value must be >= 0.
     *  Default value is 2500.0.
     *  @return the rover fuel storage capacity property
     */
    public double getRoverFuelStorageCapacity() {
        if (roverFuelStorageCapacity < 0) roverFuelStorageCapacity = 2500D;
        return roverFuelStorageCapacity;
    }

    /** Gets the rover fuel efficiency property.
     *  Value must be > 0.
     *  Default value is 2.0.
     *  @return the rover fuel efficiency property
     */
    public double getRoverFuelEfficiency() {
        if (roverFuelEfficiency <= 0) roverFuelEfficiency = 2D;
        return roverFuelEfficiency;
    }

    /** Gets the rover range property.
     *  Value must be >= 0.
     *  Default value is 4000.0.
     *  @return the rover range property
     */
    public double getRoverRange() {
        if (roverRange < 0) roverRange = 4000D;
        return roverRange;
    }

    /** Gets the settlement oxygen storage capacity property.
     *  Value must be >= 0.
     *  Default value is 10000.0.
     *  @return the settlement oxygen storage capacity property
     */
    public double getSettlementOxygenStorageCapacity() {
        if (settlementOxygenStorageCapacity < 0) settlementOxygenStorageCapacity = 10000D;
        return settlementOxygenStorageCapacity;
    }

    /** Gets the settlement water storage capacity property.
     *  Value must be >= 0.
     *  Default value is 10000.0.
     *  @return the settlement water storage capacity property
     */
    public double getSettlementWaterStorageCapacity() {
        if (settlementWaterStorageCapacity < 0) settlementWaterStorageCapacity = 10000D;
        return settlementWaterStorageCapacity;
    }

    /** Gets the settlement food storage capacity property.
     *  Value must be >= 0.
     *  Default value is 10000.0.
     *  @return the settlement food storage capacity property
     */
    public double getSettlementFoodStorageCapacity() {
        if (settlementFoodStorageCapacity < 0) settlementFoodStorageCapacity = 10000D;
        return settlementFoodStorageCapacity;
    }
 
    /** Gets the settlement fuel storage capacity property.
     *  Value must be >= 0.
     *  Default value is 10000.0.
     *  @return the settlement fuel storage capacity property
     */
    public double getSettlementFuelStorageCapacity() {
        if (settlementFuelStorageCapacity < 0) settlementFuelStorageCapacity = 10000D;
        return settlementFuelStorageCapacity;
    }

    /** Gets the greenhouse full harvest property.
     *  Value must be >= 0.
     *  Default value is 200.0.
     *  @return the greenhouse full harvest property
     */
    public double getGreenhouseFullHarvest() {
        if (greenhouseFullHarvest < 0) greenhouseFullHarvest = 200D;
        return greenhouseFullHarvest;
    }

    /** Gets the greenhouse growing cycle property.
     *  Value must be >= 0.
     *  Default value is 10000.0.
     *  @return the greenhouse growing cycle property
     */
    public double getGreenhouseGrowingCycle() {
        if (greenhouseGrowingCycle < 0) greenhouseGrowingCycle = 10000D;
        return greenhouseGrowingCycle;
    }
}
