/**
 * Mars Simulation Project
 * MedicalXmlReader.java
 * @version 2.74 2002-01-28
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person;

import org.mars_sim.msp.simulation.MspXmlReader;
import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/**
 * This class reads an XML file that defines the Medical Complaints used by
 * the MedicalManager. The file
 *
 * @see MedicalManager
 */
public class MedicalXmlReader extends MspXmlReader {

    // XML element types
    private static final int COMPLAINT_LIST = 0;
    private static final int COMPLAINT = 1;
    private static final int NAME = 2;
    private static final int RECOVERY = 3;
    private static final int DEGRADE = 4;
    private static final int SERIOUS = 5;
    private static final int PROBABILITY = 6;
    private static final int PERFORMANCE = 7;
    private static final int NEXT = 8;


    private MedicalManager manager = null;  // The manager to load.
    private int elementType;                // The current element type being parsed
    private String currentName;             // The current person name parsed
    private MedicalComplaint nextComplaint; // Next complaint
    private int currentProbability;         // Current probability
    private int currentSerious;             // Currrent seriousness
    private int currentPerformance;         // Current performance
    private double currentDegrade;          // Currect degrade time
    private double currentRecovery;         // Current recoevery time

    /**
     * Construct a reader of the conf/medical.xml file that will load the
     * specified Medical Manager.
     *
     * @param manager Medical manager to load.
     */
    public MedicalXmlReader(MedicalManager manager) {
        super("conf/medical.xml");

        this.manager = manager;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("COMPLAINT_LIST")) {
            elementType = COMPLAINT_LIST;
        }
        if (name.equals("COMPLAINT")) {
            elementType = COMPLAINT;
            currentName = "";
        }
        if (name.equals("NAME")) {
            elementType = NAME;
        }
        if (name.equals("SERIOUS")) {
            elementType = SERIOUS;
        }
        if (name.equals("PERFORMANCE")) {
            elementType = PERFORMANCE;
        }
        if (name.equals("PROBABILITY")) {
            elementType = PROBABILITY;
        }
        if (name.equals("RECOVERY")) {
            elementType = RECOVERY;
        }
        if (name.equals("DEGRADE")) {
            elementType = DEGRADE;
            currentDegrade = 0D;
        }
        if (name.equals("NEXT")) {
            elementType = NEXT;
            nextComplaint = null;
        }
    }

    /** Handle the end of an element by printing an event.
     *  @param name the name of the ending element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) throws Exception {
        super.endElement(name);

        switch (elementType) {
            case NAME:
            case SERIOUS:
            case PERFORMANCE:
            case PROBABILITY:
            case DEGRADE:
            case RECOVERY:
            case NEXT:
                elementType = COMPLAINT;
                break;

            case COMPLAINT:
                manager.createComplaint(currentName, currentSerious,
                                    currentDegrade, currentRecovery,
                                    currentProbability, currentPerformance,
                                    nextComplaint);
                elementType = COMPLAINT_LIST;
                break;
        }
    }

    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();
try {
        switch (elementType) {
            case NAME:
                currentName = data;
                break;
            case SERIOUS:
                currentSerious = Integer.parseInt(data);
                break;
            case PERFORMANCE:
                currentPerformance = Integer.parseInt(data);
                break;
            case PROBABILITY:
                currentProbability = Integer.parseInt(data);
                break;
            case RECOVERY:
                currentRecovery = Double.parseDouble(data) *
                                        MedicalManager.MINSPERDAY;
                break;
            case DEGRADE:
                currentDegrade = Double.parseDouble(data) *
                                        MedicalManager.MINSPERDAY;
                break;
            case NEXT:
                nextComplaint = manager.getByName(data);
                if (nextComplaint == null) {
                    System.err.println("Problem finding Medical complaint:" +
                                        data);
                }
                break;
        }
        }
        catch(NumberFormatException e) {
            System.out.println("Error " + e);
            System.out.println("Type " + elementType + " data " + data);
        }
    }
}