/**
 * Mars Simulation Project
 * SettlementNamesXmlReader.java
 * @version 2.75 2003-01-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/** 
 * The SettlementNamesXmlReader class parses the settlement_names.xml XML file and
 * creates a list of possible settlement names.
 */
public class SettlementNamesXmlReader extends MspXmlReader {

    // XML element types
    private static final int SETTLEMENT_NAME_LIST = 0;
    private static final int SETTLEMENT_NAME = 1;

    // Data members
    private int elementType; // The current element type being parsed.
    private ArrayList settlementNames; // The collection of settlement names.

    /** 
     * Constructor
     */
    public SettlementNamesXmlReader() {
        super("settlement_names");
    }

    /**
     * Returns the collection of settlement names.
     */
    public ArrayList getSettlementNames() {
        return settlementNames;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("SETTLEMENT_NAME_LIST")) {
            elementType = SETTLEMENT_NAME_LIST;
            settlementNames = new ArrayList();
        }
        else if (name.equals("SETTLEMENT_NAME")) {
            elementType = SETTLEMENT_NAME;
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
            case SETTLEMENT_NAME_LIST:
                elementType = -1;
                break;
            case SETTLEMENT_NAME:
                elementType = SETTLEMENT_NAME_LIST;
        }
    }

    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();
     
        if (elementType == SETTLEMENT_NAME) settlementNames.add(data);
    }
}
