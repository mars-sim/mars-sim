/**
 * Mars Simulation Project
 * PropertiesXmlReader.java
 * @version 2.73 2001-11-28
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/** The PropertiesXmlReader class parses the properties.xml XML file and
 *  reads simulation properties.
 */
class PropertiesXmlReader extends MspXmlReader {

    // XML element types
    private static int PROPERTY_LIST = 0;
    private static int TIME_RATIO = 1;

    // Data members
    private int elementType; // The current element type being parsed
    private double timeRatio; // The time ratio property

    /** Constructor */
    public PropertiesXmlReader() {
        super("conf/properties.xml");
    }

    /** Gets the time ratio property. 
     *  Value must be > 0.
     *  Default value is 1000.
     *  @return the ration between simulation and real time 
     */
    public double getTimeRatio() {
        if (timeRatio <= 0) timeRatio = 1000D;
        return timeRatio;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("PROPERTY_LIST")) {
            elementType = PROPERTY_LIST;
            System.out.println("Start PROPERTY_LIST");
        }
        if (name.equals("TIME_RATIO")) {
            elementType = TIME_RATIO;
            System.out.println("Start TIME_RATIO");
        }
    }

    /** Handle the end of an element by printing an event.
     *  @param name the name of the ending element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) throws Exception {
        super.endElement(name);
      
        if (elementType == TIME_RATIO) {
            System.out.println("End TIME_RATIO");
            elementType = PROPERTY_LIST;
            return;
        }
        if (elementType == PROPERTY_LIST) {
            System.out.println("End PROPERTY_LIST");
        }
    }

    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();

        if (elementType == TIME_RATIO) {
            timeRatio = Double.parseDouble(data);
            System.out.println("timeRatio: " + timeRatio);
        }
    }
}
