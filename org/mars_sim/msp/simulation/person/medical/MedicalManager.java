/**
 * Mars Simulation Project
 * MedicalManager.java
 * @version 2.81 2007-08-27
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.medical;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;

/**
 * This class provides a Factory for the Complaint class. Some of the Medical
 * Complaints are pre-defined.
 * Instances are accessed via a factory method since the properties of the
 * individual complaints are loaded from the XML.
 */
public class MedicalManager implements Serializable {

    public final static int MINSPERDAY = (24 * 60);

    private HashMap<String, Complaint> complaints = new HashMap<String, Complaint>(); // Possible Complaints
    private HashMap<String, Treatment> treatments = new HashMap<String, Treatment>(); // Possible Treatments
    private HashMap<Integer, List<Treatment>> supported = new HashMap<Integer, List<Treatment>>();  // Treatments 2 Facilities
    private Complaint starvation;        // Pre-defined complaint
    private Complaint suffocation;       // Pre-defined complaint
    private Complaint dehydration;       // Pre-defined complaint
    private Complaint decompression;     // Pre-defined complaint
    private Complaint freezing;          // Pre-defined complaint
    private Complaint heatStroke;        // Pre-defined complaint

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
     * The name of the heat stroke complaint
     */
    public final static String HEAT_STROKE = "Heat Stroke";
    
    /**
     * Construct a new Medical Manager. This also constructs all the
     * pre-defined Complaints and the user-defined ones in the XML
     * propery file.
     *
     * @throws Exception if unable to construct.
     */
    public MedicalManager() throws Exception {

        initMedical();
    }

    /**
     * Initialise the Medical Complaints from the configuration.
     * @throws exception if not able to initialize complaints.
     */
    public void initMedical() throws Exception{
        // Create the pre-defined complaints, using person configuration.
        SimulationConfig simConfig = SimulationConfig.instance();
        PersonConfig personConfig = simConfig.getPersonConfiguration();
        MedicalConfig medicalConfig = simConfig.getMedicalConfiguration();

		try {
			
        	// Quite serious, 70, and has a 80% performance factor.
        	// Zero recovery as death will result if unchecked.
        	starvation = createEnvironmentComplaint(STARVATION, 70,
				(personConfig.getFoodDeprivationTime() - 
						personConfig.getStarvationStartTime()) * 1000D, 80);

        	// Most serious complaint, 100, and has a 25% performance factor, i.e.
        	// Person can be nothing.
        	suffocation = createEnvironmentComplaint(SUFFOCATION, 100,
				personConfig.getOxygenDeprivationTime(), 25);

        	// Very serious complaint, 70, and a 70% performance effect. Zero
        	// recovery as death will result
        	dehydration = createEnvironmentComplaint(DEHYDRATION, 60,
				personConfig.getWaterDeprivationTime() * 1000D, 70);

        	// Very serious complaint, 100, and has a 10% performance factor. Zero
        	// recovery as death will result
        	decompression = createEnvironmentComplaint(DECOMPRESSION, 100,
				personConfig.getDecompressionTime(), 10);

        	// Somewhat serious complaint, 80, and a 40% performance factor. Zero
        	// recovery as death will result
        	freezing = createEnvironmentComplaint(FREEZING, 80,
				personConfig.getFreezingTime(), 40);

        	// Somewhat serious complaint, 80, and a 40% performance factor. Zero
        	// recovery as death will result
        	heatStroke = createEnvironmentComplaint(HEAT_STROKE, 80,
                	100D, 40);

			// Create treatments from medical config.
			try {
				Iterator i = medicalConfig.getTreatmentList().iterator();
				while (i.hasNext()) addTreatment((Treatment) i.next());
			}
			catch (Exception e) {
				throw new Exception("Error loading treatments: " + e.getMessage());
			}

			// Create additional complaints from medical config.
			try {
				Iterator j = medicalConfig.getComplaintList().iterator();
				while (j.hasNext()) addComplaint((Complaint) j.next());
			}
			catch (Exception e) {
				throw new Exception("Error loading complaints: " + e.getMessage());
			}
		}
		catch (Exception e) {
			throw new Exception("Medical manager cannot be initialized: " + e.getMessage());
		}
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
                             double probability,
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
     * Adds a new complaint to the map.
     * @param newComplaint the new complaint to add.
     * @throws Exception if complaint already exists in map.
     */
    void addComplaint(Complaint newComplaint) throws Exception {
    	if (!complaints.containsKey(newComplaint.getName())) 
    		complaints.put(newComplaint.getName(), newComplaint);
    	else throw new Exception("Complaint " + newComplaint.getName() + " already exists in map.");
    }

    /**
     * Package friendly factory method.
     */
    void createTreatment(String name, int skill, double duration,
                         boolean selfHeal, boolean retainAid, int level) {
        Treatment newTreatment = new Treatment(name, skill, duration,
                                               selfHeal, retainAid, level);
        treatments.put(name, newTreatment);
    }

	/**
	 * Adds a new treatment to the map.
	 * @param newTreatment the new treatment to add.
	 * @throws Exception if treatment already exists in map.
	 */
	void addTreatment(Treatment newTreatment) throws Exception {
		if (!treatments.containsKey(newTreatment.getName()))
			treatments.put(newTreatment.getName(), newTreatment);
		else throw new Exception("Treatment " + newTreatment.getName() + " already exists in map.");
	}

    /**
     * Select a probable complaint to strike the Person down. This uses
     * a random factor to select the complaint based on the probability
     * rating. The physical characteristics of the Person are taken into account.
     *
     * @param person The person that may have a complaint.
     * @param time the time passing (millisols).
     * @return Possible Complaint, this maybe null.
     */
    public Complaint getProbableComplaint(Person person, double time) {
        Complaint complaint = null;

        // Get a random number from 0 to the total probability weight.
        double r = RandomUtil.getRandomDouble(Complaint.MAXPROBABILITY);
        
        // Take into account the time passing (compared to one sol).
        r *= (1000D / time);

        // Get the list of possible Complaints, find all Medical complaints
        // that have a probability higher that the calculated, i.e.
        // possible complaints.
        // THis need improving.
        ArrayList<Complaint> possibles = null;
        Iterator<Complaint> items = complaints.values().iterator();
        while(items.hasNext()) {
            Complaint next = items.next();

            // Found a match
            if (next.getProbability() > r) {
                if (possibles == null) {
                    possibles = new ArrayList<Complaint>();
                }
                possibles.add(next);
            }
        }

        // Found any possibles complaint that have a lower probability
        // than the random value
        if (possibles != null) {
            // Just take one of the possibles at random
            int index = RandomUtil.getRandomInt(possibles.size() - 1);
            complaint = possibles.get(index);
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
        return complaints.get(name);
    }

    /**
     * This is a finder method that returns a Meidcal Treatment matching
     * the specified name.
     *
     * @param name Name of the treatment to retrieve.
     * @return Matched Treatment, if none is found then a null.
     */
    public Treatment getTreatmentByName(String name) {
        return treatments.get(name);
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
     * Get the supported Treatments for a Medical Facility of a particular
     * level. This will be a combination of all the Treatments of the
     * specified level and all those lower.
     *
     * @param level Level of Medical facility.
     * @return List of Treatments
     */
    public List<Treatment> getSupportedTreatments(int level) {
        Integer key = new Integer(level);
        List<Treatment> results = supported.get(key);
        if (results == null) {
            results = new ArrayList<Treatment>();
            Iterator<Treatment> iter = treatments.values().iterator();
            while(iter.hasNext()) {
                Treatment next = iter.next();
                if (next.getFacilityLevel() <= level) results.add(next);
            }
            Collections.sort(results);
            supported.put(key, results);
        }
        return results;
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

    /**
     * Return the pre-defined Medical Complaint that signifies a Heat Stroke
     * complaint.
     *
     * @return Medical complaint for heat stroke.
     */
    public Complaint getHeatStroke() {
        return heatStroke;
    }
    
    /**
     * Checks if a health complaint is an environmental complaint.
     * 
     * @param complaint the complaint to check.
     * @return true if complaint is environmental complaint.
     */
    public boolean isEnvironmentalComplaint(Complaint complaint) {
        boolean result = false;
        
        if (complaint == suffocation) result = true;
        if (complaint == dehydration) result = true;
        if (complaint == starvation) result = true;
        if (complaint == decompression) result = true;
        if (complaint == freezing) result = true;
        if (complaint == heatStroke) result = true;
        
        return result;
    }
}