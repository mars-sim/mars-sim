/**
 * Mars Simulation Project
 * VehiclesXmlReader.java
 * @version 2.74 2002-03-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/** The VehiclesXmlReader class parses the vehicles.xml XML file and 
 *  creates vehicle unit objects.
 */
public class VehiclesXmlReader extends MspXmlReader {

    // XML element types
    private static final int VEHICLES_LIST = 0;
    private static final int EXPLORER_ROVER = 1;
    private static final int TRANSPORT_ROVER = 2;
    private static final int NAME = 3;
    private static final int SETTLEMENT = 4;

    // Data members
    private int vehiclesMax; // Maximum number of vehicles to initialize
    private int elementType; // The current element type being parsed
    private int vehicleType; // The current vehicle type being parsed
    private VehicleCollection vehicles; // The collection of created settlements
    private Mars mars; // The virtual Mars instance
    private UnitManager manager; // The unit manager
    private String currentName; // The current vehicle's name
    private Settlement currentSettlement; // The current vehicle's settlement

    /** Constructor
     *  @param manager the unit manager
     *  @param mars the virtual Mars instance
     */
    public VehiclesXmlReader(UnitManager manager, Mars mars) {
        super("conf/vehicles.xml");
	vehiclesMax = mars.getSimulationProperties().getInitVehicles();
	this.manager = manager;
        this.mars = mars;
    }

    /** Returns the collection of vehicles created from the XML file.
     *  @return the collection of vehicles
     */
    public VehicleCollection getVehicles() {
        return vehicles;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("VEHICLES_LIST")) {
            elementType = VEHICLES_LIST;
            vehicles = new VehicleCollection();
        }
        if (name.equals("EXPLORER_ROVER")) {
            elementType = EXPLORER_ROVER;
            vehicleType = EXPLORER_ROVER;
            currentName = "";
            currentSettlement = null;
        }
	if (name.equals("TRANSPORT_ROVER")) {
	    elementType = TRANSPORT_ROVER;
	    vehicleType = TRANSPORT_ROVER;
	    currentName = "";
	    currentSettlement = null;
	}
        if (name.equals("NAME")) elementType = NAME;
        if (name.equals("SETTLEMENT")) elementType = SETTLEMENT;
    }

    /** Handle the end of an element by printing an event.
     *  @param name the name of the ended element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) throws Exception {
        super.endElement(name);
     
        switch (elementType) {
            case NAME:
            case SETTLEMENT:
                elementType = vehicleType;
                break;
            case EXPLORER_ROVER:
	    case TRANSPORT_ROVER:
		if(vehiclesMax == 0 || vehicles.size() < vehiclesMax) {
		    Rover rover = createRover(elementType);
		    if (rover != null) vehicles.add(rover);
		}
		elementType = VEHICLES_LIST;
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
            case NAME:
                currentName = data;
                break;
            case SETTLEMENT:
                currentSettlement = manager.getSettlements().getSettlement(data);
                break;
        }
    }
    
    /** Creates a rover object
     *  @param roverType type of rover 
     *  @return a rover or null if rover could not be constructed
     */
    private Rover createRover(int roverType) {

        Rover rover = null;
        if (currentSettlement != null) {
	    if (roverType == EXPLORER_ROVER) 
                rover = new ExplorerRover(currentName, currentSettlement, mars);
	    else if (roverType == TRANSPORT_ROVER) 
                rover = new TransportRover(currentName, currentSettlement, mars);
        }
        else {
            try {
		if (roverType == EXPLORER_ROVER)
                    rover = new ExplorerRover(currentName, mars, manager);
		else if (roverType == TRANSPORT_ROVER)
		    rover = new TransportRover(currentName, mars, manager);
            }
            catch (Exception e) {}
        }
        
        return rover;
    }
}

