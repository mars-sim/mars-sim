/**
 * Mars Simulation Project
 * MalfunctionXmlReader.java
 * @version 2.74 2002-04-21
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation.malfunction;

import org.mars_sim.msp.simulation.*;
import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/**
 * This class reads an XML file that defines the malfunctions used by
 * the MalfunctionManager. 
 */
class MalfunctionXmlReader extends MspXmlReader {

    // XML element types
    private static final int MALFUNCTION_LIST = 0;
    private static final int MALFUNCTION = 1;
    private static final int NAME = 2;
    private static final int SEVERITY = 3;
    private static final int PROBABILITY = 4;
    private static final int EMERGENCY_WORK_TIME = 5;
    private static final int WORK_TIME = 6;
    private static final int EVA_WORK_TIME = 7;
    private static final int SCOPE = 8;
    private static final int ENTITY = 9;
    private static final int EFFECTS = 10;
    private static final int RESOURCE = 11;
    private static final int CHANGE_RATE = 12;
    private static final int LIFE_SUPPORT = 13;
    private static final int MEDICAL_COMPLAINT = 14;

    private MalfunctionFactory factory = null;  // The manager to load.
    private int elementType;                    // The current element type being parsed
    private int entityType;                     // The current entity type being parsed
    private String currentName;                 // The current malfunction name parsed
    private int currentSeverity;                // The current malfunction severity 
    private double currentProbability;          // The current malfunction probability
    private double currentEmergencyWorkTime;    // The current emergency work time
    private double currentWorkTime;             // The current work time
    private double currentEVAWorkTime;          // The current EVA work time
    private Collection scope;                   // The scope of current malfunction
    private Map resourceEffects;                // The resource effects of current malfunction
    private String currentResourceName;         // The name of the current resource
    private double currentResourceChangeRate;   // The current resource's change rate
    private Map lifeSupportEffects;             // The life support effects of current malfunction
    private String currentLifeSupportName;      // The current life support's name
    private double currentLifeSupportChangeRate;// The current life support's change rate
    private Map medicalComplaints;              // The medical complaints associated
    private String currentComplaintName;        // The current medical complaint name
    private double currentComplaintProbability; // The current medical complaint probability

    /**
     * Construct a reader of the conf/malfunction.xml file that will load the
     * specified malfunction manager.
     *
     * @param manager malfunction factory to load
     */
    MalfunctionXmlReader(MalfunctionFactory factory) {
        super("malfunctions");

        this.factory = factory;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("MALFUNCTION_LIST")) {
            elementType = MALFUNCTION_LIST;
        }
	else if (name.equals("MALFUNCTION")) {
            elementType = MALFUNCTION;
	    entityType = MALFUNCTION;
            currentName = "";
	    currentSeverity = 0;
	    currentProbability = 0D;
	    currentEmergencyWorkTime = 0D;
	    currentWorkTime = 0D;
	    currentEVAWorkTime = 0D;
	    scope = new ArrayList();
	    resourceEffects = new HashMap();
	    lifeSupportEffects = new HashMap();
	    medicalComplaints = new HashMap();
        }
        else if (name.equals("NAME")) {
            elementType = NAME;
        }
        else if (name.equals("SEVERITY")) {
            elementType = SEVERITY;
        }
        else if (name.equals("PROBABILITY")) {
            elementType = PROBABILITY;
        }
        else if (name.equals("EMERGENCY_WORK_TIME")) {
            elementType = EMERGENCY_WORK_TIME;
        }
        else if (name.equals("WORK_TIME")) {
            elementType = WORK_TIME;
        }
        else if (name.equals("EVA_WORK_TIME")) {
            elementType = EVA_WORK_TIME;
        }
        else if (name.equals("SCOPE")) {
            elementType = SCOPE;
        }
        else if (name.equals("ENTITY")) {
            elementType = ENTITY;
        }
        else if (name.equals("EFFECTS")) {
            elementType = EFFECTS;
        }
        else if (name.equals("RESOURCE")) {
            elementType = RESOURCE;
	    entityType = RESOURCE;
        }
        else if (name.equals("CHANGE_RATE")) {
            elementType = CHANGE_RATE;
        }
        else if (name.equals("LIFE_SUPPORT")) {
            elementType = LIFE_SUPPORT;
	    entityType = LIFE_SUPPORT;
        }
	else if (name.equals("MEDICAL_COMPLAINT")) {
	    elementType = MEDICAL_COMPLAINT;
	    entityType = MEDICAL_COMPLAINT;
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
            case SEVERITY:
            case EMERGENCY_WORK_TIME:
            case WORK_TIME:
	    case EVA_WORK_TIME:
	    case SCOPE:
                elementType = MALFUNCTION;
                break;
            case EFFECTS:
		elementType = MALFUNCTION;
		entityType = MALFUNCTION;
		break;
	    case ENTITY:
	        elementType = SCOPE;
		break;
	    case RESOURCE:
                resourceEffects.put(currentResourceName, new Double(currentResourceChangeRate));
		elementType = EFFECTS;
		break;
	    case LIFE_SUPPORT:
		lifeSupportEffects.put(currentLifeSupportName, new Double(currentLifeSupportChangeRate));
	        elementType = EFFECTS;
		break;
	    case MEDICAL_COMPLAINT:
	        medicalComplaints.put(currentComplaintName, new Double(currentComplaintProbability));
		elementType = MALFUNCTION;
		entityType = MALFUNCTION;
		break;
	    case NAME:
	    case CHANGE_RATE:
	    case PROBABILITY:
		elementType = entityType;
		break;
	    case MALFUNCTION:
	        factory.addMalfunction(new Malfunction(currentName, currentSeverity, currentProbability,
		        currentEmergencyWorkTime, currentWorkTime, currentEVAWorkTime, scope, 
			resourceEffects, lifeSupportEffects, medicalComplaints));
		elementType = MALFUNCTION_LIST;
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
		    if (entityType == MALFUNCTION) currentName = data;
		    if (entityType == RESOURCE) currentResourceName = data;
		    if (entityType == LIFE_SUPPORT) currentLifeSupportName = data;
		    if (entityType == MEDICAL_COMPLAINT) currentComplaintName = data;
                    break;
                case SEVERITY:
                    currentSeverity = Integer.parseInt(data);
                    break;
                case PROBABILITY:
                    if (entityType == MALFUNCTION) 
	                currentProbability = Double.parseDouble(data);
		    if (entityType == MEDICAL_COMPLAINT) 
		        currentComplaintProbability = Double.parseDouble(data);
                    break;
                case EMERGENCY_WORK_TIME:
                    currentEmergencyWorkTime = Double.parseDouble(data);
                    break;
                case WORK_TIME:
                    currentWorkTime = Double.parseDouble(data);
                    break;
                case EVA_WORK_TIME:
                    currentEVAWorkTime = Double.parseDouble(data);
                    break;
                case ENTITY:
                    scope.add(data);
                    break;
                case CHANGE_RATE:
		    if (entityType == RESOURCE) 
		        currentResourceChangeRate = Double.parseDouble(data);
		    if (entityType == LIFE_SUPPORT) 
	                currentLifeSupportChangeRate = Double.parseDouble(data);
                    break;
            }
        }
        catch(NumberFormatException e) {
            System.out.println("Error " + e);
            System.out.println("Type " + elementType + " data " + data);
        }
    }
}
