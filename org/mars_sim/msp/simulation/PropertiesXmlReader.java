/**
 * Mars Simulation Project
 * PropertiesXmlReader.java
 * @version 2.74 2002-05-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The PropertiesXmlReader class parses the properties.xml XML file and
 *  reads simulation properties.
 */
class PropertiesXmlReader extends MspXmlReader {

    // XML element types
    private static final int PROPERTY_LIST = 0;
    private static final int TIME_RATIO = 1;
    private static final int PERSON_PROPERTIES = 2;
    private static final int OXYGEN_CONSUMPTION = 3;
    private static final int WATER_CONSUMPTION = 4;
    private static final int FOOD_CONSUMPTION = 5;
    private static final int ROVER_PROPERTIES = 6;
    private static final int OXYGEN_STORAGE_CAPACITY = 7;
    private static final int WATER_STORAGE_CAPACITY = 8;
    private static final int FOOD_STORAGE_CAPACITY = 9;
    private static final int FUEL_STORAGE_CAPACITY = 10;
    private static final int FUEL_EFFICIENCY = 11;
    private static final int SETTLEMENT_PROPERTIES = 12;
    private static final int RANGE = 13;
    private static final int GREENHOUSE_FULL_HARVEST = 14;
    private static final int GREENHOUSE_GROWING_CYCLE = 15;
    private static final int LACK_OF_OXYGEN = 16;
    private static final int LACK_OF_WATER = 17;
    private static final int LACK_OF_FOOD = 18;
    private static final int MIN_AIR_PRESSURE = 19;
    private static final int DECOMPRESSION = 20;
    private static final int MIN_TEMPERATURE = 21;
    private static final int FREEZING_TIME = 22;
    private static final int AIRLOCK_CYCLE_TIME = 23;
    private static final int MAX_TEMPERATURE = 24;
    
    private static final int INIT_PROPERTIES = 100;
    private static final int INIT_SETTLEMENTS = 101;

    // Data members
    private int elementType; // The current element type being parsed
    private int propertyCatagory; // The property catagory
    private double timeRatio; // The time ratio property
    private double airlockCycleTime; // The time required for airlock cycling
    private double personOxygenConsumption; // The person oxygen consumption property
    private double personLackOfOxygen; // The period a persion surives without Oxygen
    private double personWaterConsumption; // The person water consumption property
    private double personLackOfWater; // The period a person can survive without water
    private double personFoodConsumption; // The person food consumption property
    private double personLackOfFood; // The period a person can survive without food
    private double personMinAirPressure; // The minimum air pressure property
    private double personDecompression; // The decompression property
    private double personMinTemperature; // The minimum temperature property
    private double personMaxTemperature; // The maximum temperature property
    private double personFreezingTime; // The freezing time property
    private double roverFuelEfficiency; // The rover fuel efficiency property
    private double roverRange; // The rover range property
    private double settlementOxygenStorageCapacity; // The settlement oxygen storage capacity property
    private double settlementWaterStorageCapacity; // The settlement water storage capacity property
    private double settlementFoodStorageCapacity; // The settlement food storage capacity property
    private double settlementFuelStorageCapacity; // The settlement fuel storage capacity property
    private double greenhouseFullHarvest; // The greenhouse full harvest property
    private double greenhouseGrowingCycle; // The greenhouse growing cycle property

    private int initSettlements; // settlements at the start of the sim

    /** Constructor */
    public PropertiesXmlReader() {
        super("properties");
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
	if (name.equals("AIRLOCK_CYCLE_TIME")) {
	    elementType = AIRLOCK_CYCLE_TIME;
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
        if (name.equals("LACK_OF_OXYGEN")) {
            elementType = LACK_OF_OXYGEN;
        }
        if (name.equals("LACK_OF_FOOD")) {
            elementType = LACK_OF_FOOD;
        }
        if (name.equals("LACK_OF_WATER")) {
            elementType = LACK_OF_WATER;
        }
	if (name.equals("MIN_AIR_PRESSURE")) {
	    elementType = MIN_AIR_PRESSURE;
	}
	if (name.equals("DECOMPRESSION")) {
            elementType = DECOMPRESSION;
	}
	if (name.equals("MIN_TEMPERATURE")) {
            elementType = MIN_TEMPERATURE;
	}
	if (name.equals("MAX_TEMPERATURE")) {
	    elementType = MAX_TEMPERATURE;
	}
	if (name.equals("FREEZING_TIME")) {
	    elementType = FREEZING_TIME;
	}
	if (name.equals("INIT_PROPERTIES")) {
	    elementType = INIT_PROPERTIES;
	}
	if (name.equals("INIT_SETTLEMENTS")) {
	    elementType = INIT_SETTLEMENTS;
	}
    }

    /** Handle the end of an element by printing an event.
     *  @param name the name of the ending element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) throws Exception {
        super.endElement(name);

        switch (elementType) {
            case TIME_RATIO:
                elementType = PROPERTY_LIST;
                break;
	    case AIRLOCK_CYCLE_TIME:
		elementType = PROPERTY_LIST;
		break;
            case PERSON_PROPERTIES:
            case ROVER_PROPERTIES:
            case SETTLEMENT_PROPERTIES:
                elementType = PROPERTY_LIST;
                propertyCatagory = -1;
                break;
            case OXYGEN_CONSUMPTION:
            case WATER_CONSUMPTION:
            case FOOD_CONSUMPTION:
            case LACK_OF_FOOD:
            case LACK_OF_OXYGEN:
            case LACK_OF_WATER:
	    case MIN_AIR_PRESSURE:
	    case DECOMPRESSION:
	    case MIN_TEMPERATURE:
	    case MAX_TEMPERATURE:
	    case FREEZING_TIME:
                elementType = PERSON_PROPERTIES;
                break;
            case FUEL_EFFICIENCY:
            case RANGE:
                elementType = ROVER_PROPERTIES;
                break;
            case GREENHOUSE_FULL_HARVEST:
            case GREENHOUSE_GROWING_CYCLE:
                elementType = SETTLEMENT_PROPERTIES;
                break;
            case OXYGEN_STORAGE_CAPACITY:
            case WATER_STORAGE_CAPACITY:
            case FOOD_STORAGE_CAPACITY:
            case FUEL_STORAGE_CAPACITY:
                elementType = propertyCatagory;
                break;
	    case INIT_SETTLEMENTS:
		elementType = INIT_PROPERTIES;
		break;
        }
    }

    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();

        switch (elementType) {
            case TIME_RATIO:
                timeRatio = Double.parseDouble(data);
                break;
	    case AIRLOCK_CYCLE_TIME:
		airlockCycleTime = Double.parseDouble(data);
		break;
            case OXYGEN_CONSUMPTION:
                personOxygenConsumption = Double.parseDouble(data);
                break;
            case WATER_CONSUMPTION:
                personWaterConsumption = Double.parseDouble(data);
                break;
            case FOOD_CONSUMPTION:
                personFoodConsumption = Double.parseDouble(data);
                break;
            case LACK_OF_FOOD:
                personLackOfFood = Double.parseDouble(data);
                break;
            case LACK_OF_OXYGEN:
                personLackOfOxygen = Double.parseDouble(data);
                break;
            case LACK_OF_WATER:
                personLackOfWater = Double.parseDouble(data);
                break;
	    case MIN_AIR_PRESSURE:
		personMinAirPressure = Double.parseDouble(data);
		break;
	    case DECOMPRESSION:
		personDecompression = Double.parseDouble(data);
		break;
	    case MIN_TEMPERATURE:
		personMinTemperature = Double.parseDouble(data);
		break;
	    case MAX_TEMPERATURE:
	        personMaxTemperature = Double.parseDouble(data);
		break;
	    case FREEZING_TIME:
		personFreezingTime = Double.parseDouble(data);
		break;
            case OXYGEN_STORAGE_CAPACITY:
                settlementOxygenStorageCapacity = Double.parseDouble(data);
		break;
            case WATER_STORAGE_CAPACITY:
                settlementWaterStorageCapacity = Double.parseDouble(data);
		break;
            case FOOD_STORAGE_CAPACITY:
                settlementFoodStorageCapacity = Double.parseDouble(data);
		break;
            case FUEL_STORAGE_CAPACITY:
                settlementFuelStorageCapacity = Double.parseDouble(data);
		break;
            case FUEL_EFFICIENCY:
                roverFuelEfficiency = Double.parseDouble(data);
                break;
            case RANGE:
                roverRange = Double.parseDouble(data);
                break;
            case GREENHOUSE_FULL_HARVEST:
                greenhouseFullHarvest = Double.parseDouble(data);
                break;
            case GREENHOUSE_GROWING_CYCLE:
                greenhouseGrowingCycle = Double.parseDouble(data);
                break;
  	    case INIT_SETTLEMENTS:
                initSettlements = Integer.parseInt(data);
                break;
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

    /**
     * Gets the airlock pressurization/depressurization time.
     * Value must be > 0.
     * Default value is 10 millisols.
     * @return airlock cycle time in millisols
     */
    public double getAirlockCycleTime() {
        if (airlockCycleTime <= 0D) airlockCycleTime = 10D;
	return airlockCycleTime;
    }

    /** Gets the number of Earth minutes a person can survive without oxygen.
     *  Value must be > 0.
     *  Default value is 5 Earth minutes
     *  @return the person locak of oxygen property
     */
    public double getPersonLackOfOxygenPeriod() {
        if (personLackOfOxygen <= 0) personLackOfOxygen = 5D;
        return personLackOfOxygen;
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

    /** Gets the number of minutues an person can survive without water
     *  Value must be > 0.
     *  Default value is 6 Earth days
     *  @return the person water consumption property
     */
    public double getPersonLackOfWaterPeriod() {
        if (personLackOfWater <= 0) personLackOfWater = (6 * 24 * 60);
        return personLackOfWater;
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

    /** Gets the number of minutes an person can survive without food.
     *  Value must be > 0.
     *  Default value is 21 days.
     *  @return the person lack of food property
     */
    public double getPersonLackOfFoodPeriod() {
        if (personLackOfFood <= 0) personLackOfFood = (21 * 24 * 60);
        return personLackOfFood;
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

    /**
     * Gets the person minimum air pressure property.
     * Default value is .25 atm.
     * @return the person minimum air pressure property
     */
    public double getPersonMinAirPressure() {
        return personMinAirPressure;
    }

    /**
     * Gets the person decompression time property.
     * Value must be >= 0.
     * Default value is 90.0 seconds.
     * @return person decompression time property
     */
    public double getPersonDecompressionTime() {
        if (personDecompression < 0D) personDecompression = 90D;
	return personDecompression;
    }

    /**
     * Gets the person minimum temperature property.
     * Default value is 0 degrees Celsius
     * @return person minimum temperature property
     */
    public double getPersonMinTemperature() {
        return personMinTemperature;
    }

    /**
     * Gets the person maximum temperature property.
     * Default value is 48 degrees Celsius
     * @return person maximum temperature property
     */
    public double getPersonMaxTemperature() {
        return personMaxTemperature;
    }

    /**
     * Gets the person freezing time property.
     * Value must be >= 0.
     * Default value is 240.0 minutes.
     * @return person freezing time property
     */
    public double getPersonFreezingTime() {
        if (personFreezingTime < 0D) personFreezingTime = 240D;
	return personFreezingTime;
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

    /** Gets the number of settlements when starting
     *  Value must be >= 0.
     *  Default value is 5.
     *  @return the number of settlements when starting
     */
    public int getInitSettlements() {
        if (initSettlements < 0) initSettlements = 5;
        return initSettlements;
    }
}
