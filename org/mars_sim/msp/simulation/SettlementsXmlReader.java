/**
 * Mars Simulation Project
 * SettlementsXmlReader.java
 * @version 2.73 2001-11-22
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/** The SettlementsXmlReader class parses the settlements.xml XML file and 
 *  creates settlement unit objects.
 */
class SettlementsXmlReader extends MspXmlReader {

    // XML element types
    private static int SETTLEMENTS_LIST = 0;
    private static int SETTLEMENT = 1;
    private static int NAME = 2;
    private static int LOCATION = 4;
    private static int LATITUDE = 5;
    private static int LONGITUDE = 6;
    private static int POP_CAPACITY = 7;

    // Data members
    private int elementType; // The current element type being parsed
    private SettlementCollection settlements; // The collection of created settlements
    private VirtualMars mars; // The virtual Mars instance
    private String currentName; // The current settlement name parsed
    private String currentLatitude; // The current latitude string parsed
    private String currentLongitude; // The current longitude string parsed
    private Coordinates currentLocation; // The current settlement location created
    private int currentPopulationCapacity; // The current settlement population capacity

    /** Constructor
     *  @param mars the virtual Mars instance
     */
    SettlementsXmlReader(VirtualMars mars) {
        super("conf/settlements.xml");

        this.mars = mars;
    }

    /** Returns the collection of settlements created from the XML file.
     *  @return the collection of settlements
     */
    public SettlementCollection getSettlements() {
        return settlements; 
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("SETTLEMENTS_LIST")) {
            elementType = SETTLEMENTS_LIST;
            settlements = new SettlementCollection();
        }
        if (name.equals("SETTLEMENT")) {
            elementType = SETTLEMENT;
            currentName = "";
            currentLatitude = "";
            currentLongitude = "";
            currentLocation = null;
            currentPopulationCapacity = 0;
        }
        if (name.equals("NAME")) elementType = NAME;
        if (name.equals("LOCATION")) elementType = LOCATION;
        if (name.equals("LATITUDE")) elementType = LATITUDE;
        if (name.equals("LONGITUDE")) elementType = LONGITUDE;
        if (name.equals("POPULATION_CAPACITY")) elementType = POP_CAPACITY;
    }

    /** Handle the end of an element by printing an event.
     *  @param name the name of the ended element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) throws Exception {
        super.endElement(name);
      
        if (elementType == NAME) {
            elementType = SETTLEMENT;
            return;
        }
        if (elementType == LOCATION) {
            currentLocation = createLocation();
            elementType = SETTLEMENT;
            return;
        }
        if (elementType == LATITUDE) {
            elementType = LOCATION;
            return;
        }
        if (elementType == LONGITUDE) {
            elementType = LOCATION;
            return;
        }
        if (elementType == POP_CAPACITY) {
            elementType = SETTLEMENT;
            return;
        }
        if (elementType == SETTLEMENT) {
            settlements.add(createSettlement());    
            elementType = SETTLEMENTS_LIST;
            return;
        }
    }

    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();

        if (elementType == NAME) currentName = data;
        if (elementType == LATITUDE) currentLatitude = data;
        if (elementType == LONGITUDE) currentLongitude = data;
        if (elementType == POP_CAPACITY) {
            try {
                currentPopulationCapacity = Integer.parseInt(data);
            }
            catch(NumberFormatException e) {}
            if (currentPopulationCapacity < 0) currentPopulationCapacity = 0;
        }
    }

    /** Creates a settlement based on parsed information. 
     *  @return constructed settlment based on parsed information
     */
    private Settlement createSettlement() {
        Settlement settlement = null;
        
        if (currentLocation == null) {
            settlement = new Settlement(currentName, currentPopulationCapacity, mars);
        }
        else {
            settlement = new Settlement(currentName, currentLocation, currentPopulationCapacity, mars);
        }
        
        return settlement;
    }
    
    /** Create a coordinates location if parameters are valid. 
     *  @return coordinates object based on parsed longitude and latitude
     */
    private Coordinates createLocation() {
        double phi = 0D;
        double theta = 0D;
        
        try {
            phi = parseLatitude(currentLatitude);
            theta = parseLongitude(currentLongitude);
            return new Coordinates(phi, theta);
        }
        catch(IllegalArgumentException e) {}
        
        return new Coordinates(phi, theta);
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
