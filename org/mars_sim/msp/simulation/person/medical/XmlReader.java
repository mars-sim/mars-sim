/**
 * Mars Simulation Project
 * XmlReader.java
 * @version 2.74 2002-01-28
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.medical;

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
class XmlReader extends MspXmlReader {

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
    private static final int CURE = 9;
    private static final int TREATMENT_LIST = 10;
    private static final int TREATMENT = 11;
    private static final int DURATION = 12;
    private static final int SKILL = 13;


    private MedicalManager manager = null;  // The manager to load.
    private int elementType;                // The current element type being parsed
    private String currentName;             // The current person name parsed
    private Complaint nextComplaint;        // Next complaint
    private int currentProbability;         // Current probability
    private int currentSerious;             // Currrent seriousness
    private int currentPerformance;         // Current performance
    private double currentDegrade;          // Currect degrade time
    private double currentRecovery;         // Current recoevery time
    private double currentDuration;         // Current treatment length
    private Treatment currentTreatment;     // Current recovery treatment
    private int currentSkill;               // Skill for Treatment

    /**
     * Construct a reader of the conf/medical.xml file that will load the
     * specified Medical Manager.
     *
     * @param manager Medical manager to load
     */
    XmlReader(MedicalManager manager) {
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
        else if (name.equals("TREATMENT_LIST")) {
            elementType = TREATMENT_LIST;
        }
        if (name.equals("COMPLAINT")) {
            elementType = COMPLAINT;
            currentName = "";
            currentDegrade = 0D;
            currentTreatment = null;
            nextComplaint = null;
        }
        else if (name.equals("TREATMENT")) {
            elementType = TREATMENT;
            currentName = "";
            currentSkill = 0;
            currentDuration = -1.0D;
        }
        else if (name.equals("DURATION")) {
            elementType = DURATION;
        }
        else if (name.equals("SKILL")) {
            elementType = SKILL;
        }
        else if (name.equals("NAME")) {
            elementType = NAME;
        }
        else if (name.equals("SERIOUS")) {
            elementType = SERIOUS;
        }
        else if (name.equals("PERFORMANCE")) {
            elementType = PERFORMANCE;
        }
        else if (name.equals("PROBABILITY")) {
            elementType = PROBABILITY;
        }
        else if (name.equals("RECOVERY")) {
            elementType = RECOVERY;
        }
        else if (name.equals("DEGRADE")) {
            elementType = DEGRADE;
        }
        else if (name.equals("CURE")) {
            elementType = CURE;
        }
        else if (name.equals("NEXT")) {
            elementType = NEXT;
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

            case TREATMENT:
                manager.createTreatment(currentName, currentSkill,
                                        currentDuration);
                elementType = TREATMENT_LIST;
                break;

            case COMPLAINT :
                manager.createComplaint(currentName, currentSerious,
                                    currentDegrade, currentRecovery,
                                    currentProbability, currentPerformance,
                                    currentTreatment,
                                    nextComplaint);
                elementType = COMPLAINT_LIST;
                break;

            case COMPLAINT_LIST:
            case TREATMENT_LIST:
                elementType = -1;
                break;

            case NAME:
            case DURATION:
            case SKILL:
                elementType = TREATMENT;
                break;

            case RECOVERY:
            case DEGRADE:
            case SERIOUS:
            case PROBABILITY:
            case PERFORMANCE:
            case NEXT:
            case CURE:
                elementType = COMPLAINT;
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
            case SKILL:
                currentSkill = Integer.parseInt(data);
                break;
            case PROBABILITY:
                currentProbability = Integer.parseInt(data);
                break;
            case DURATION:
                currentDuration = Double.parseDouble(data) * 60;
                break;
            case RECOVERY:
                currentRecovery = Double.parseDouble(data) *
                                        MedicalManager.MINSPERDAY;
                break;
            case DEGRADE:
                currentDegrade = Double.parseDouble(data) *
                                        MedicalManager.MINSPERDAY;
                break;
            case CURE:
                currentTreatment = manager.getTreatmentByName(data);
                if (currentTreatment == null) {
                    System.err.println("Problem finding Treatment:" +
                                        data);
                }
                break;

            case NEXT:
                nextComplaint = manager.getComplaintByName(data);
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
