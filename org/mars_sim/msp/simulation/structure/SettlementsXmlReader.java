/**
 * Mars Simulation Project
 * SettlementsXmlReader.java
 * @version 2.75 2003-01-28
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.template.*;
import java.util.*;

/** The SettlementsXmlReader class parses the settlements.xml XML file and
 *  creates settlement unit objects.
 */
public class SettlementsXmlReader extends MspXmlReader {

    // XML element types
    private static final int SETTLEMENT_PROPERTIES = 0;
    private static final int SETTLEMENT_TEMPLATE_LIST = 1;
    private static final int SETTLEMENT_TEMPLATE = 2;
    private static final int BUILDING = 3;
    private static final int VEHICLE = 4;
    private static final int INITIAL_SETTLEMENT_LIST = 5;
    private static final int SETTLEMENT = 6;
    private static final int LOCATION = 7;

    // Data members
    private int elementType; // The current element type being parsed.
    private Mars mars; // The virtual Mars instance.
    private Collection settlementTemplates; // The collection of settlement templates.
    private SettlementTemplate currentSettlementTemplate; // The current settlement template.
    private String currentName; // The current name attribute for an element.
    private String currentType; // The current type attribute for an element.
    private int currentNumber; // The current number attribute for an element.
    private SettlementCollection initialSettlements; // The collection of created initial settlements.
    private String currentTemplate; // The current template attribute for an element.
    private String currentLatitude; // The current latitude string parsed.
    private String currentLongitude; // The current longitude string parsed.
    private Coordinates currentLocation; // The current settlement location created.
    private Random rand = new Random();

    /** Constructor
     *  @param mars the virtual Mars instance
     */
    public SettlementsXmlReader(Mars mars) {
        super("settlements");
        this.mars = mars;
    }
    
    /**
     * Returns the collection of settlement templates created from the XML file.
     * @return collection of settlement templates
     */
    public Collection getSettlementTemplates() {
        return settlementTemplates;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);
        
        if (name.equals("SETTLEMENT_PROPERTIES")) {
            elementType = SETTLEMENT_PROPERTIES;
        }
        else if (name.equals("SETTLEMENT_TEMPLATE_LIST")) {
            elementType = SETTLEMENT_TEMPLATE_LIST;
            settlementTemplates = new ArrayList();
        }
        else if (name.equals("SETTLEMENT_TEMPLATE")) {
            elementType = SETTLEMENT_TEMPLATE;
            currentSettlementTemplate = new SettlementTemplate(currentName);
            settlementTemplates.add(currentSettlementTemplate);
        }
        else if (name.equals("BUILDING")) {
            elementType = BUILDING;
        }
        else if (name.equals("VEHICLE")) {
            elementType = VEHICLE;
        }   
        else if (name.equals("INITIAL_SETTLEMENT_LIST")) {
            elementType = INITIAL_SETTLEMENT_LIST;
        }
        else if (name.equals("SETTLEMENT")) {
            elementType = SETTLEMENT;
        }
        else if (name.equals("LOCATION")) elementType = LOCATION;
    }
    
    /** Handle an attribute value assignment by printing an event.
     *  @see com.microstar.xml.XmlHandler#attribute
     */
    public void attribute (String name, String value, boolean isSpecified) {
        super.attribute(name, value, isSpecified);
        
        if (name.equals("TYPE")) {
            currentType = value;
        }
        else if (name.equals("NAME")) {
            currentName = value;
        }   
        else if (name.equals("NUMBER")) {
            try {
                currentNumber = Integer.parseInt(value);
            }
            catch(NumberFormatException e) {}
        }
        else if (name.equals("TEMPLATE")) {
            currentTemplate = value;
        }
        else if (name.equals("LATITUDE")) {
            currentLatitude = value;
        }
        else if (name.equals("LONGITUDE")) {
            currentLongitude = value;
        }
    }

    /** Handle the end of an element by printing an event.
     *  @param name the name of the ended element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) throws Exception {
        super.endElement(name);
        
        switch (elementType) {
            case SETTLEMENT_PROPERTIES:
                elementType = -1;
                break;
            case SETTLEMENT_TEMPLATE_LIST:
                elementType = SETTLEMENT_PROPERTIES;
                break;
            case SETTLEMENT_TEMPLATE:
                elementType = SETTLEMENT_TEMPLATE_LIST;
                currentName = "";
                break;
            case BUILDING:
                if (currentNumber <= 0) currentNumber = 1;
                for (int x=0; x < currentNumber; x++) 
                    currentSettlementTemplate.addBuilding(currentType);
                elementType = SETTLEMENT_TEMPLATE;
                currentType = "";
                currentNumber = 0;
                break;
            case VEHICLE:
                if (currentNumber <= 0) currentNumber = 1;
                for (int x=0; x < currentNumber; x++) 
                    currentSettlementTemplate.addVehicle(currentType);
                elementType = SETTLEMENT_TEMPLATE;
                currentType = "";
                currentNumber = 0;
                break;
            case INITIAL_SETTLEMENT_LIST:
                elementType = SETTLEMENT_PROPERTIES;
                break;
  	        case SETTLEMENT:
                SettlementTemplate template = null;
                Iterator i = settlementTemplates.iterator();
                while (i.hasNext()) {
                    SettlementTemplate settlementTemplate = (SettlementTemplate) i.next();
                    if (currentTemplate.equals(settlementTemplate.getName())) template = settlementTemplate;
                }
                if (template != null) {
                    Settlement settlement = template.constructSettlement(currentName, currentLocation, mars);
                    mars.getUnitManager().addUnit(settlement);
                }
		        elementType = INITIAL_SETTLEMENT_LIST;
                currentName = "";
                currentTemplate = "";
                currentLocation = null;
                break;
            case LOCATION:
                currentLocation = createLocation();
                elementType = SETTLEMENT;
                currentLatitude = "";
                currentLongitude = "";
        }
    }

    /** Create a coordinates location if parameters are valid.
     *  @return coordinates object based on parsed longitude and latitude
     */
    private Coordinates createLocation() {
        double phi = 0D;
        double theta = 0D;
        
        try {
            if (currentLatitude.equals("random")) phi = getRandomLatitude();
            else phi = parseLatitude(currentLatitude);
            
            if (currentLongitude.equals("random")) theta = getRandomLongitude();
            else theta = parseLongitude(currentLongitude);
        }
        catch(IllegalArgumentException e) {}

        return new Coordinates(phi, theta);
    }
    
    /**
     * Gets a random latitude.
     *
     * @return latitude
     */
    private double getRandomLatitude() {
        // Random latitude should be less likely to be near the poles.
        double phi = (rand.nextGaussian() * (Math.PI / 7D)) + (Math.PI / 2D);
        if (phi > Math.PI) phi = Math.PI;
        if (phi < 0D) phi = 0D;
        return phi;
    }
    
    /**
     * Gets a random longitude.
     *
     * @return longitude
     */
    private double getRandomLongitude() {
        double theta = (double)(Math.random() * (2D * Math.PI)); 
        return theta;
    }

    /** Parse a latitude string into a phi value
     *  ex. "25.344 N"
     *  @param latitude as string
     *  @return phi based on latitude string
     *  @throws java.lang.IllegalArgumentException if bad latitude string
     */
    private double parseLatitude(String latitude) throws IllegalArgumentException {
        boolean badLatitude = false;
        double latValue = 0D;

        if (latitude.trim().equals("")) badLatitude = true;
        try {
            latValue = Double.parseDouble(latitude.substring(0, latitude.length() - 2));
            if ((latValue > 90D) || (latValue < 0)) badLatitude = true;
        }
        catch(NumberFormatException e) { badLatitude = true; }
        char direction = latitude.charAt(latitude.length() - 1);
        if (direction == 'N') latValue = 90D - latValue;
        else if (direction == 'S') latValue += 90D;
        else badLatitude = true;

        if (badLatitude) throw new IllegalArgumentException();

        double phi = Math.PI * (latValue / 180D);
        return phi;
    }

    /** Parse a longitude string into a theta value
     *  ex. "63.5532 W"
     *  @param longitude as string
     *  @return theta based on longitude string
     *  @throws java.lang.IllegalArgumentException if bad longitude string
     */
    private double parseLongitude(String longitude) throws IllegalArgumentException {
        boolean badLongitude = false;
        double longValue = 0D;

        if (longitude.trim().equals("")) badLongitude = true;
        try {
            longValue = Double.parseDouble(longitude.substring(0, longitude.length() - 2));
            if ((longValue > 180D) || (longValue < 0)) badLongitude = true;
        }
        catch(NumberFormatException e) { badLongitude = true; }
        char direction = longitude.charAt(longitude.length() - 1);
        if (direction == 'W') longValue = 360D - longValue;
        else if (direction != 'E') badLongitude = true;

        if (badLongitude) throw new IllegalArgumentException();

        double theta = (2 * Math.PI) * (longValue / 360D);
        return theta;
    }
}
