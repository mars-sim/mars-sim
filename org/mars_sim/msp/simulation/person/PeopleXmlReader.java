/**
 * Mars Simulation Project
 * PeopleXmlReader.java
 * @version 2.75 2003-01-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person;

import org.mars_sim.msp.simulation.*;
import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/** The PeopleXmlReader class parses the people.xml XML file and
 *  reads person-related properties.
 */
public class PeopleXmlReader extends MspXmlReader {

    // XML element types

    // Data members
    private Mars mars; // The virtual Mars instance
    private UnitManager manager; // The unit manager
    private int elementType; // The current element type being parsed

    /** Constructor
     *  @param manager the unit manager
     *  @param mars the virtual Mars instance
     */
    public PeopleXmlReader(UnitManager manager, Mars mars) {
        super("people");
        this.manager = manager;
        this.mars = mars;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

    }

    /** Handle the end of an element by printing an event.
     *  @param name the name of the ending element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) throws Exception {
        super.endElement(name);

    }

    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();

    }
}
