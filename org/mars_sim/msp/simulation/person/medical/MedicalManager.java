/**
 * Mars Simulation Project
 * MedicalManager.java
 * @version 2.74 2002-02-25
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.medical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.mars_sim.msp.simulation.SimulationProperties;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.Person;

/**
 * This class provides a Factory for the Complaint class. It is
 * constructed with a set of Simulation Properties. Some of the Medical
 * Complaints are pre-defined.
 * Instances are accessed via a factory method since the properties of the
 * individual complaints are loaded from the XML.
 */
public class MedicalManager implements Serializable {

    public final static int MINSPERDAY = (24 * 60);

    private HashMap complaints = new HashMap(); // Possible Complaints
    private HashMap treatments = new HashMap(); // Possible Treatments
    private HashMap supported = new HashMap();  // Treatments 2 Facilities
    private Complaint starvation;        // Pre-defined complaint
    private Complaint suffocation;       // Pre-defined complaint
    private Complaint dehydration;       // Pre-defined complaint
    private Complaint decompression;     // Pre-defined complaint
    private Complaint freezing;          // Pre-defined complaint

    /**
     * The name of the suffocation complaint
     */
    public final static String SUFFOCATION = "Suffocation";

    /**
     * The name of the Dehydration complaint
     */
    public final static String DEHYDRATION = "Dehydration";

    /**
     * The name of the starvation complaint
     */
    public final static String STARVATION = "Starvation";

    /**
     * The name of the decompression complaint
     */
    public final static String DECOMPRESSION = "Decompression";

    /**
     * The name of the freezing complaint
     */
    public final static String FREEZING = "Freezing";

    /**
     * Construct a new Medical Manager. This also constructs all the
     * pre-defined Complaints and the user-defined ones in the XML
     * propery file.
     *
     * @param props Simulation properties.
     */
    public MedicalManager(SimulationProperties props) {

        initMedical(props);
    }

    /**
     * Initialise the Medical Complaints from the properties and XML file.
     *
     * @param props Global properties.
     */
    public void initMedical(SimulationProperties props) {
        // Create the pre-defined complaints, using properties.

        // Quite serious, 70, and has a 80% performance factor.
        // Zero recovery as death will result if unchecked.
        starvation = createEnvironmentComplaint(STARVATION, 70,
                                     props.getPersonLackOfFoodPeriod(),
                                     80);

        // Most serious complaint, 100, and has a '0' performance factor, i.e.
        // Person can be nothing.
        suffocation = createEnvironmentComplaint(SUFFOCATION, 100,
                                       props.getPersonLackOfOxygenPeriod(),
                                       0);

        // Very serious complaint, 70, and a 70% performance effect. Zero
        // recovery as death will result
        dehydration = createEnvironmentComplaint(DEHYDRATION, 60,
                                      props.getPersonLackOfWaterPeriod(),
                                      70);

        // Very serious complaint, 100, and has a 0% performance factor. Zero
	    // recovery as death will result
	    decompression = createEnvironmentComplaint(DECOMPRESSION, 100,
                                    props.getPersonDecompressionTime() / 60D,
                                    0);

        // Somewhat serious complaint, 80, and a 40% performance factor. Zero
	    // recovery as death will result
	    freezing = createEnvironmentComplaint(FREEZING, 80,
                                      props.getPersonFreezingTime(),40);

        /** Creates initial complaints from XML config file */
        XmlReader medicalReader = new XmlReader(this);

        medicalReader.parse();
    }

    /**
     * Create an environment related Complaint. These are started
     * by the simulation and not via randonmness. The all result in death
     * hence have no next phase and no recovery period, when the environment
     * changes, the complaint is resolved.
     */
    private Complaint createEnvironmentComplaint(String name, int seriousness,
                             double degrade, int performance) {
        return new Complaint(name, seriousness, degrade, 0D, 0,
                             performance, null, null);
    }

    /**
     * Package friendly factory method.
     */
    void createComplaint(String name, int seriousness,
                             double degrade, double recovery,
                             int probability,
                             int performance, Treatment recoveryTreatment,
                             Complaint next) {

        Complaint complaint = new Complaint(name, seriousness,
                                            degrade, recovery,
                                            probability, performance,
                                            recoveryTreatment, next);
        // Add an entry keyed on name.
        complaints.put(name, complaint);
    }

    /**
     * Package friendly factory method.
     */
    void createTreatment(String name, int skill, double duration) {
        Treatment newTreatment = new Treatment(name, skill, duration);
        treatments.put(name, newTreatment);
    }

    /**
     * Select a probable complaint to strike the Person down. This uses
     * a random factor to select the complaint based on the probability
     * rating. The physical characteristics of the Person are taken into account.
     *
     * @param person The person that may have a complaint.
     * @return Possible Complaint, this maybe null.
     */
    public Complaint getProbableComplaint(Person person) {
        Complaint complaint = null;

        // Get a random number from 0 to the total probability weight.
        int r = RandomUtil.getRandomInt(Complaint.MAXPROBABILITY);

        // Get the list of possible Complaints, find all Medical complaints
        // that have a probability higher that the calculated, i.e.
        // possible complaints.
        // THis need improving.
        ArrayList possibles = null;
        Iterator items = complaints.values().iterator();
        while(items.hasNext()) {
            Complaint next = (Complaint)items.next();

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
            int index = RandomUtil.getRandomInt(possibles.size() - 1);
            complaint = (Complaint)possibles.get(index);
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
    public Complaint getComplaintByName(String name) {
        return (Complaint)complaints.get(name);
    }

    /**
     * This is a finder method that returns a Meidcal Treatment matching
     * the specified name.
     *
     * @param name Name of the treatment to retrieve.
     * @return Matched Treatment, if none is found then a null.
     */
    public Treatment getTreatmentByName(String name) {
        return (Treatment)treatments.get(name);
    }

    /**
     * Return the pre-defined Medical Complaint that signifies a suffocation
     * complaint.
     *
     * @return Medical complaint for shortage of oxygen.
     */
    public Complaint getSuffocation() {
        return suffocation;
    }

    /**
     * Get the supported Treatments for a Medical Facility of a name.
     *
     * @param name Name of Medical facility.
     * @return Collection of Treatments
     */
    public Collection getSupportedTreatments(String name) {
        return (Collection)supported.get(name);
    }

    /**
     * Return the pre-defined Medical Complaint that signifies a dehydration
     * complaint.
     *
     * @return Medical complaint for shortage of water.
     */
    public Complaint getDehydration() {
        return dehydration;
    }

    /**
     * Return the pre-defined Medical Complaint that signifies a Stavation
     * complaint.
     *
     * @return Medical complaint for shortage of oxygen.
     */
    public Complaint getStarvation() {
        return starvation;
    }

    /**
     * Return the pre-defined Medical Complaint that signifies a Decompression
     * conplaint.
     *
     * @return Medical complaint for decompression.
     */
    public Complaint getDecompression() {
        return decompression;
    }

    /**
     * Return the pre-defined Medical Complaint that signifies a Freezing
     * complaint.
     *
     * @return Medical complaint for freezing.
     */
    public Complaint getFreezing() {
        return freezing;
    }
}
