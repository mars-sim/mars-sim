/**
 * Mars Simulation Project
 * CropXmlReader.java
 * @version 2.75 2003-02-22
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.building;

import org.mars_sim.msp.simulation.*;
import java.util.*;

/** 
 * The SettlementNamesXmlReader class parses the settlement_names.xml XML file and
 * creates a list of possible settlement names.
 */
public class CropXmlReader extends MspXmlReader {

    // XML element types
    private static final int CROP_LIST = 0;
    private static final int CROP = 1;
    private static final int NAME = 2;

    // Data members
    private int elementType; // The current element type being parsed.
    private String currentName; // The current crop type name.
    private double currentGrowingTime; // The current crop type growing time.
    private ArrayList cropTypes; // The list of crop types.

    /** 
     * Constructor
     */
    public CropXmlReader() {
        super("crops");
    }

    /**
     * Returns the list of crop types.
     */
    public ArrayList getCropTypes() {
        return cropTypes;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("CROP_LIST")) {
            elementType = CROP_LIST;
            cropTypes = new ArrayList();
        }
        else if (name.equals("CROP")) {
            elementType = CROP;
        }
    }

    /** Handle an attribute value assignment by printing an event.
     *  @see com.microstar.xml.XmlHandler#attribute
     */
    public void attribute (String name, String value, boolean isSpecified) {
        super.attribute(name, value, isSpecified);
        
        if (name.equals("NAME")) currentName = value;
        else if (name.equals("GROWING_TIME")) {
            try {
                // Get growing time in sols and convert to millisols.
                currentGrowingTime = Double.parseDouble(value) * 1000D;
            }
            catch (NumberFormatException e) { 
                System.out.println("Bad crop growing time specified in crops.xml"); 
            }
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
            case CROP_LIST:
                elementType = -1;
                break;
            case CROP:
                cropTypes.add(new CropType(currentName, currentGrowingTime));
                elementType = CROP_LIST;
        }
    }
}
