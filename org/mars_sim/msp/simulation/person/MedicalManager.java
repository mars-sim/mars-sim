/**
 * Mars Simulation Project
 * @author Barry Evans
 * @version 2.74
 */

package org.mars_sim.msp.simulation.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.mars_sim.msp.simulation.SimulationProperties;
import org.mars_sim.msp.simulation.RandomUtil;

/**
 * This class provides a Factory for the MedicalComplaint class. It is
 * constructed with a set of Simulation Properties. Some of the Medical
 * Complaints are pre-defined.
 * Instances are accessed via a factory method since the properties of the
 * individual complaints are loaded from the XML.
 */
public class MedicalManager implements Serializable {

    private final static int MINSPERDAY = (24 * 60);

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
        starvation = createComplaint(STARVATION, 70,
                                     props.getPersonLackOfFoodPeriod(),
                                     0D, 0, 80, null);

        // Most serious complaint, 100, and has a '0' performance factor, i.e.
        // Person can be nothing. Zero recovery as results in death.
        lackOfOxygen = createComplaint(LACKOFOXYGEN, 100,
                                       props.getPersonLackOfOxygenPeriod(),
                                       0D, 0, 0, null);

        // Very serious complaint, 70, and a 70% performance effect. Zero
        // recovery as death will result
        dehydration = createComplaint(DEHYDRATION, 60,
                                      props.getPersonLackOfWaterPeriod(),
                                      0D, 0, 70, null);
        complaints.put(DEHYDRATION, dehydration);

        // The following should be loaded from an XML file, later work
        // These are illness/injuries that happen at random

        createComplaint("Cold", 10, 0, (7 * MINSPERDAY), 30, 70, null);
        createComplaint("Pulled Tendon/Muscle", 30, 0, (14 * MINSPERDAY),
                        20, 60, null);
        MedicalComplaint next = createComplaint("Gangrene", 80,
                                        (7 * MINSPERDAY), (14 * MINSPERDAY),
                                        1, 0, null);
        createComplaint("Broken bone", 60, (7 * MINSPERDAY), (14 * MINSPERDAY),
                        10, 0, next);
        createComplaint("Laceration", 20, (7 * MINSPERDAY), (2 * MINSPERDAY),
                        10, 70, next);
        createComplaint("Meningitis", 70, (4 * MINSPERDAY), (14 * MINSPERDAY),
                        5, 0, null);
    }

    /**
     * Private factory method.
     */
    private MedicalComplaint createComplaint(String name, int seriousness,
                             double degrade, double recovery,
                             int probability,
                             int performance, MedicalComplaint next) {
        MedicalComplaint complaint = new MedicalComplaint(name, seriousness,
                                                        degrade, recovery,
                                                        probability, performance,
                                                        next);
        // Add an entry keyed on name.
        complaints.put(name, complaint);

        return complaint;
    }

    /**
     * Select a probable complaint to strike the Person down. This uses
     * a random factor to select the complaint based on the probability
     * rating. The physical characteristics of the Person are taken into account.
     *
     * @param person The person that may have a complaint.
     * @return Possible MedicalComplaint, this maybe null.
     */
    MedicalComplaint getProbableComplaint(Person person) {
        MedicalComplaint complaint = null;

        // Get a random number from 0 to the total probability weight.
        int r = RandomUtil.getRandomInt(MedicalComplaint.MAXPROBABILITY);

        // Get the list of possible Complaints, find all Medical complaints
        // that have a probability higher that the calculated, i.e.
        // possible complaints.
        // THis need improving.
        ArrayList possibles = null;
        Iterator items = complaints.values().iterator();
        while(items.hasNext()) {
            MedicalComplaint next = (MedicalComplaint)items.next();

            // Found a match
            if (next.getProbability() > r) {
                if (possibles == null) {
                    possibles = new ArrayList();
                }
                possibles.add(next);
            }
        }

        // Found any possibles complaint that have a lower probability
        // than the random value
        if (possibles != null) {
            // Just take one of the possibles at random
            System.out.println("Calc. prob = " + r + ", found = " + possibles);
            int index = RandomUtil.getRandomInt(possibles.size() - 1);
            complaint = (MedicalComplaint)possibles.get(index);
        }
        return complaint;
    }

    /**
     * This is a finder method that returns a Meidcal Complaint matching
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