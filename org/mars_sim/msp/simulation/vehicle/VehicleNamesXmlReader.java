/**
 * Mars Simulation Project
 * VehicleNamesXmlReader.java
 * @version 2.75 2003-01-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import java.util.*;

/** 
 * The VehicleNamesXmlReader class parses the vehicle_names.xml XML file and
 * creates a list of possible vehicle names.
 */
public class VehicleNamesXmlReader extends MspXmlReader {

    // XML element types
    private static final int VEHICLE_NAME_LIST = 0;
    private static final int VEHICLE_NAME = 1;

    // Data members
    private int elementType; // The current element type being parsed.
    private ArrayList vehicleNames; // The collection of vehicle names.

    /** 
     * Constructor
     */
    public VehicleNamesXmlReader() {
        super("vehicle_names");
    }

    /**
     * Returns the collection of vehicle names.
     */
    public ArrayList getVehicleNames() {
        return vehicleNames;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("VEHICLE_NAME_LIST")) {
            elementType = VEHICLE_NAME_LIST;
            vehicleNames = new ArrayList();
        }
        else if (name.equals("VEHICLE_NAME")) {
            elementType = VEHICLE_NAME;
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
            case VEHICLE_NAME_LIST:
                elementType = -1;
                break;
            case VEHICLE_NAME:
                elementType = VEHICLE_NAME_LIST;
        }
    }

    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();
     
        if (elementType == VEHICLE_NAME) vehicleNames.add(data);
    }
}
