/**
 * Mars Simulation Project
 * PersonNamesXmlReader.java
 * @version 2.75 2003-01-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person;

import org.mars_sim.msp.simulation.*;
import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/** 
 * The PersonNamesXmlReader class parses the person_names.xml XML file and
 * creates a list of possible person names.
 */
public class PersonNamesXmlReader extends MspXmlReader {

    // XML element types
    private static final int PERSON_NAME_LIST = 0;
    private static final int PERSON_NAME = 1;

    // Data members
    private int elementType; // The current element type being parsed.
    private ArrayList personNames; // The collection of person names.

    /** 
     * Constructor
     */
    public PersonNamesXmlReader() {
        super("person_names");
    }

    /**
     * Returns the collection of person names.
     */
    public ArrayList getPersonNames() {
        return personNames;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("PERSON_NAME_LIST")) {
            elementType = PERSON_NAME_LIST;
            personNames = new ArrayList();
        }
        else if (name.equals("PERSON_NAME")) {
            elementType = PERSON_NAME;
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
            case PERSON_NAME_LIST:
                elementType = -1;
                break;
            case PERSON_NAME:
                elementType = PERSON_NAME_LIST;
        }
    }

    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();
     
        if (elementType == PERSON_NAME) personNames.add(data);
    }
}
