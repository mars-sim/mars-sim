/**
 * Mars Simulation Project
 * @author Barry Evans
 * @version 2.74
 */

package org.mars_sim.msp.simulation.person;

import java.io.Serializable;
import java.util.HashMap;
import org.mars_sim.msp.simulation.SimulationProperties;

/**
 * This class provides a Factory for the MedicalComplaint class. It is
 * constructed with a set of Simulation Properties. Some of the Medical
 * Complaints are pre-defined.
 * Instances are accessed via a factory method since the properties of the
 * individual complaints are loaded from the XML.
 */
public class MedicalManager implements Serializable {

    private HashMap complaints = new HashMap(); // Possible MedicalComplaints
    private MedicalComplaint starvation;        // Starvation problem
    private MedicalComplaint lackOfOxygen;      // Pre-defined complaint
    private MedicalComplaint dehydration;       // Pre-defined complaint

    /**
     * The name of the Lack Of Oxygen complaint
     */
    public final static String LACKOFOXYGEN = "Lack Of Oxygen";

    /**
     * The name of the Dehydration complaint
     */
    public final static String DEHYDRATION = "Dehydration";

    /**
     * The name of the starvation complaint
     */
    public final static String STARVATION = "Starvation";

    /**
     * Construct a new Medical Manager. This also constructs all the
     * pre-defined MedicalComplaints and the user-defined ones in the XML
     * propery file.
     *
     * @param props Simulation properties.
     */
    public MedicalManager(SimulationProperties props) {

        // Create the pre-defined complaints, using properties.

        // Quite serious, 70, and has a 80% performance factor.
        // Zero recovery as death will result if unchecked.
        starvation = new MedicalComplaint(STARVATION, 70,
                                          props.getPersonLackOfFoodPeriod(),
                                          0D, 80, null);
        complaints.put(STARVATION, starvation);

        // Most serious complaint, 100, and has a '0' performance factor, i.e.
        // Person can be nothing. Zero recovery as results in death.
        lackOfOxygen = new MedicalComplaint(LACKOFOXYGEN, 100,
                                            props.getPersonLackOfOxygenPeriod(),
                                            0D, 0, null);
        complaints.put(LACKOFOXYGEN, lackOfOxygen);

        // Very serious complaint, 70, and a 70% performance effect. Zero
        // recovery as death will result
        dehydration = new MedicalComplaint(DEHYDRATION, 80,
                                           props.getPersonLackOfWaterPeriod(),
                                           0D, 70, null);
        complaints.put(DEHYDRATION, dehydration);
    }

    /**
     * This is a factory method that returns a Meidcal Complaint matching
     * the specified name.
     *
     * @param name Name of the complaint to retrieve.
     * @return Matched complaint, if none is found then a null.
     */
    public MedicalComplaint getByName(String name) {
        return (MedicalComplaint)complaints.get(name);
    }

    /**
     * Return the pre-defined Medical Complaint that signifies a Lack of Oxygen
     * complaint.
     *
     * @return Medical complaint for shortage of oxygen.
     */
    public MedicalComplaint getLackOfOxygen() {
        return lackOfOxygen;
    }
    /**
     * Return the pre-defined Medical Complaint that signifies a lack
     * of water complaint.
     *
     * @return Medical complaint for shortage of water.
     */
    public MedicalComplaint getDehydration() {
        return dehydration;
    }

    /**
     * Return the pre-defined Medical Complaint that signifies a Stavation
     * complaint.
     *
     * @return Medical complaint for shortage of oxygen.
     */
    public MedicalComplaint getStarvation() {
        return starvation;
    }
}