/**
 * Mars Simulation Project
 * VehiclesXmlReader.java
 * @version 2.73 2001-10-30
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.*;
import com.microstar.xml.*;

class VehiclesXmlReader extends MspXmlReader {

    private static int VEHICLES_LIST = 0;
    private static int ROVER = 1;
    private static int NAME = 2;
    private static int SETTLEMENT = 3;

    private int elementType;
    private int vehicleType;

    private Vector vehicles;
    private UnitManager unitManager;
    private VirtualMars mars;
    private String currentName;
    private String currentSettlement;

    public VehiclesXmlReader(UnitManager unitManager, VirtualMars mars) {
        super("conf/vehicles.xml");

        this.unitManager = unitManager;
        this.mars = mars;
    }

    public Vector getVehicles() {
        return vehicles;
    }

    /**
     * Handle the start of an element by printing an event.
     * @see com.microstar.xml.XmlHandler#startElement
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
            currentSettlement = "";
        }
        if (name.equals("NAME")) elementType = NAME;
        if (name.equals("SETTLEMENT")) elementType = SETTLEMENT;
    }

    /**
     * Handle the end of an element by printing an event.
     * @see com.microstar.xml.XmlHandler#endElement
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
        // Will need to modify this to support more types of vehicles.
        if (elementType == ROVER) {
            elementType = VEHICLES_LIST;
            Settlement settlement = unitManager.getSettlement(currentSettlement);
            Vehicle currentVehicle = new Rover(currentName, settlement.getCoordinates(), mars, unitManager);
            currentVehicle.setSettlement(settlement);
            vehicles.addElement(currentVehicle);
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
        if (elementType == SETTLEMENT) currentSettlement = data;
    }
}

