/**
 * Mars Simulation Project
 * VehiclesXmlReader.java
 * @version 2.73 2001-11-15
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/** The VehiclesXmlReader class parses the vehicles.xml XML file and 
 *  creates vehicle unit objects.
 */
class VehiclesXmlReader extends MspXmlReader {

    // XML element types
    private static int VEHICLES_LIST = 0;
    private static int ROVER = 1;
    private static int NAME = 2;
    private static int SETTLEMENT = 3;

    // Data members
    private int elementType; // The current element type being parsed
    private int vehicleType; // The current vehicle type being parsed
    private Vector vehicles; // The vector of created settlements
    private VirtualMars mars; // The virtual Mars instance
    private UnitManager manager; // The unit manager
    private String currentName; // The current vehicle's name
    private Settlement currentSettlement; // The current vehicle's settlement

    /** Constructor
     *  @param manager the unit manager
     *  @param mars the virtual Mars instance
     */
    public VehiclesXmlReader(UnitManager manager, VirtualMars mars) {
        super("conf/vehicles.xml");

        this.manager = manager;
        this.mars = mars;
    }

    /** Returns the vector of vehicles created from the XML file.
     *  @return the vector of vehicles
     */
    public Vector getVehicles() {
        return vehicles;
    }

    /** Handle the start of an element by printing an event.
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) {
        super.startElement(name);

        if (name.equals("VEHICLES_LIST")) {
            elementType = VEHICLES_LIST;
            vehicles = new Vector();
        }
        if (name.equals("ROVER")) {
            elementType = ROVER;
            vehicleType = ROVER;
            currentName = "";
            currentSettlement = null;
        }
        if (name.equals("NAME")) elementType = NAME;
        if (name.equals("SETTLEMENT")) elementType = SETTLEMENT;
    }

    /** Handle the end of an element by printing an event.
     *  @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) {
        super.endElement(name);
      
        if (elementType == NAME) {
            elementType = vehicleType;
            return;
        }
        if (elementType == SETTLEMENT) {
            elementType = vehicleType;
            return;
        }
        if (elementType == ROVER) {
            Rover rover = createRover();
            if (rover != null) vehicles.addElement(rover);
            elementType = VEHICLES_LIST;
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
        if (elementType == SETTLEMENT) currentSettlement = manager.getSettlement(data);
    }
    
    /** Creates a rover object 
     *  @return a rover or null if rover could not be constructed
     */
    private Rover createRover() {
        Rover rover = null;
        if (currentSettlement != null) {
            rover = new Rover(currentName, currentSettlement, mars);
        }
        else {
            try {
                rover = new Rover(currentName, mars, manager);
            }
            catch (Exception e) {}
        }
        
        return rover;
    }
}

