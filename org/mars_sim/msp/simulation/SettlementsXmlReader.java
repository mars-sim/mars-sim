/**
 * Mars Simulation Project
 * SettlementsXmlReader.java
 * @version 2.73 2001-11-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.*;
import com.microstar.xml.*;

class SettlementsXmlReader extends MspXmlReader {

    private static int SETTLEMENTS_LIST = 0;
    private static int SETTLEMENT = 1;
    private static int NAME = 2;
    private static int LOCATION = 4;
    private static int LATITUDE = 5;
    private static int LONGITUDE = 6;
    private static int POP_CAPACITY = 7;

    private int elementType;

    private Vector settlements;
    private VirtualMars mars;
    private String currentName;
    private String currentLatitude;
    private String currentLongitude;
    private String currentPopulationCapacity;

    public SettlementsXmlReader(VirtualMars mars) {
        super("conf/settlements.xml");

        this.mars = mars;
    }

    public Vector getSettlements() {
        return settlements; 
    }

    /**
     * Handle the start of an element by printing an event.
     * @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) {
        super.startElement(name);

        if (name.equals("SETTLEMENTS_LIST")) {
            elementType = SETTLEMENTS_LIST;
            settlements = new Vector();
        }
        if (name.equals("SETTLEMENT")) {
            elementType = SETTLEMENT;
            currentName = "";
            currentLatitude = "";
            currentLongitude = "";
            currentPopulationCapacity = "";
        }
        if (name.equals("NAME")) elementType = NAME;
        if (name.equals("LOCATION")) elementType = LOCATION;
        if (name.equals("LATITUDE")) elementType = LATITUDE;
        if (name.equals("LONGITUDE")) elementType = LONGITUDE;
        if (name.equals("POPULATION_CAPACITY")) elementType = POP_CAPACITY;
    }

    /**
     * Handle the end of an element by printing an event.
     * @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) {
        super.endElement(name);
      
        if (elementType == NAME) {
            elementType = SETTLEMENT;
            return;
        }
        if (elementType == LOCATION) {
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
            elementType = SETTLEMENTS_LIST;
            int popCapacity = Integer.parseInt(currentPopulationCapacity);
            double phi = parseLatitude(currentLatitude);
            double theta = parseLongitude(currentLongitude);
            Coordinates currentLocation = new Coordinates(phi, theta);
            Settlement currentSettlement = new Settlement(currentName, currentLocation, popCapacity, mars);
            settlements.addElement(currentSettlement);
            return;
        }
    }

    /**
     * Handle character data by printing an event.
     * @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();

        if (elementType == NAME) currentName = data;
        if (elementType == LATITUDE) currentLatitude = data;
        if (elementType == LONGITUDE) currentLongitude = data;
        if (elementType == POP_CAPACITY) currentPopulationCapacity = data;
    }

    /**
     * Parse a latitude string into a phi value
     * ex. "25.344 N"
     * @param latitude as string
     * @return phi based on latitude string
     */
    public double parseLatitude(String latitude) {
        char direction = latitude.charAt(latitude.length() - 1);
        double latValue = Double.parseDouble(latitude.substring(0, latitude.length() - 2));
 
        if (direction == 'N') latValue = 90D - latValue;
        else if (direction == 'S') latValue += 90D;
         
        double phi = Math.PI * (latValue / 180D);
     
        return phi;
    }

    /**
     * Parse a longitude string into a theta value
     * ex. "63.5532 W"
     * @param longitude as string
     * @return theta based on longitude string
     */
    public double parseLongitude(String longitude) {
        char direction = longitude.charAt(longitude.length() - 1);
        double longValue = Double.parseDouble(longitude.substring(0, longitude.length() - 2));

        if (direction == 'W') longValue = 360D - longValue;
        
        double theta = (2 * Math.PI) * (longValue / 360D);

        return theta;
    }
}

