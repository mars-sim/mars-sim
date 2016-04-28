/**
 * Mars Simulation Project
 * MedicalManager.java
 * @version 3.07 2014-11-13
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.medical;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.PersonConfig;

import java.io.Serializable;
import java.util.*;

/**
 * This class provides a Factory for the {@link Complaint} class. Some of the Medical Complaints are pre-defined. Instances are
 * accessed via a factory method since the properties of the individual complaints are loaded from the XML.
 */
public class MedicalManager //extends Thread
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public final static int MINSPERDAY = (24 * 60);

	/** Possible Complaints. */
	private HashMap<String, Complaint> complaints = new HashMap<String, Complaint>();
	/** Possible Treatments. */
	private HashMap<String, Treatment> treatments = new HashMap<String, Treatment>();
	/** Treatments to Facilities. */
	private HashMap<Integer, List<Treatment>> supported = new HashMap<Integer, List<Treatment>>();

	/** Pre-defined complaint. */
	private Complaint starvation;
	/** Pre-defined complaint. */
	private Complaint suffocation;
	/** Pre-defined complaint. */
	private Complaint dehydration;
	/** Pre-defined complaint. */
	private Complaint decompression;
	/** Pre-defined complaint. */
	private Complaint freezing;
	/** Pre-defined complaint. */
	private Complaint heatStroke;

	/** The name of the suffocation complaint. */
	public final static String SUFFOCATION = Msg.getString("MedicalManager.suffocation"); //$NON-NLS-1$
	/** The name of the dehydration complaint. */
	public final static String DEHYDRATION = Msg.getString("MedicalManager.dehydration"); //$NON-NLS-1$
	/** The name of the starvation complaint. */
	public final static String STARVATION = Msg.getString("MedicalManager.starvation"); //$NON-NLS-1$
	/** The name of the decompression complaint. */
	public final static String DECOMPRESSION = Msg.getString("MedicalManager.decompression"); //$NON-NLS-1$
	/** The name of the freezing complaint. */
	public final static String FREEZING = Msg.getString("MedicalManager.freezing"); //$NON-NLS-1$
	/** The name of the heat stroke complaint. */
	public final static String HEAT_STROKE = Msg.getString("MedicalManager.heatStroke"); //$NON-NLS-1$

	/**
	 * Construct a new {@link MedicalManager}. This also constructs all the pre-defined Complaints and the user-defined ones in
	 * the XML configuration file.
	 */
	public MedicalManager() {//}
	// 2015-02-04 Added run()
	//public void run() {
		initMedical();
	}
	
	/**
	 * Initialise the Medical Complaints from the configuration.
	 * @throws exception if not able to initialize complaints.
	 */
	public void initMedical() {
		// Create the pre-defined complaints, using person configuration.
		SimulationConfig simConfig = SimulationConfig.instance();
		PersonConfig personConfig = simConfig.getPersonConfiguration();
		MedicalConfig medicalConfig = simConfig.getMedicalConfiguration();

		// Quite serious, 70, and has a 80% performance factor.
		// Zero recovery as death will result if unchecked.
		starvation = createEnvironmentComplaint(STARVATION, 70, (personConfig
				.getFoodDeprivationTime() - personConfig
				.getStarvationStartTime()) * 1000D, 80);

		// Most serious complaint, 100, and has a 25% performance factor, i.e.
		// Person can be nothing.
		suffocation = createEnvironmentComplaint(SUFFOCATION, 100, personConfig
				.getOxygenDeprivationTime(), 25);

		// Very serious complaint, 70, and a 70% performance effect. Zero
		// recovery as death will result
		dehydration = createEnvironmentComplaint(DEHYDRATION, 60, personConfig
				.getWaterDeprivationTime() * 1000D, 70);

		// Very serious complaint, 100, and has a 10% performance factor. Zero
		// recovery as death will result
		decompression = createEnvironmentComplaint(DECOMPRESSION, 100,
				personConfig.getDecompressionTime(), 10);

		// Somewhat serious complaint, 80, and a 40% performance factor. Zero
		// recovery as death will result
		freezing = createEnvironmentComplaint(FREEZING, 80, personConfig
				.getFreezingTime(), 40);

		// Somewhat serious complaint, 80, and a 40% performance factor. Zero
		// recovery as death will result
		heatStroke = createEnvironmentComplaint(HEAT_STROKE, 80, 100D, 40);

		// Create treatments from medical config.
		Iterator<Treatment> i = medicalConfig.getTreatmentList().iterator();
		while (i.hasNext())
			addTreatment(i.next());

		// Create additional complaints from medical config.
		Iterator<Complaint> j = medicalConfig.getComplaintList().iterator();
		while (j.hasNext())
			addComplaint(j.next());
	}

	/**
	 * Create an environment related Complaint. These are started by the simulation and not via randomness. The all
	 * result in death hence have no next phase and no recovery period, when the environment changes, the complaint is
	 * resolved.
	 */
	private Complaint createEnvironmentComplaint(String name, int seriousness,
			double degrade, double performance) {
		return new Complaint(name, seriousness, degrade, 0D, 0D, performance,
				false, null, null);
	}

	/**
	 * Package friendly factory method.
	 */
	void createComplaint(String name, int seriousness, double degrade,
			double recovery, double probability, double performance,
			boolean bedRest, Treatment recoveryTreatment, Complaint next) {

		Complaint complaint = new Complaint(name, seriousness, degrade,
				recovery, probability, performance, bedRest, recoveryTreatment, next);
		// Add an entry keyed on name.
		complaints.put(name, complaint);
	}

	/**
	 * Adds a new complaint to the map.
	 * @param newComplaint the new complaint to add.
	 * @throws Exception if complaint already exists in map.
	 */
	void addComplaint(Complaint newComplaint) {
		if (!complaints.containsKey(newComplaint.getName()))
			complaints.put(newComplaint.getName(), newComplaint);
		else throw new IllegalStateException(
			Msg.getString(
				"MedicalManager.error.complaint", //$NON-NLS-1$
				newComplaint.getName()
			)
		);
	}

	/**
	 * Package friendly factory method.
	 */
	void createTreatment(String name, int skill, double duration,
			boolean selfHeal, boolean retainAid, int level) {
		Treatment newTreatment = new Treatment(name, skill, duration, selfHeal,
				level);
		treatments.put(name, newTreatment);
	}

	/**
	 * Adds a new treatment to the map.
	 * @param newTreatment the new treatment to add.
	 * @throws Exception if treatment already exists in map.
	 */
	void addTreatment(Treatment newTreatment) {
		if (!treatments.containsKey(newTreatment.getName()))
			treatments.put(newTreatment.getName(), newTreatment);
		else throw new IllegalStateException(
			Msg.getString(
				"MedicalManager.error.treatment", //$NON-NLS-1$
				newTreatment.getName()
			)
		);
	}
	
	/**
	 * Gets a list of all medical complaints.
	 * @return list of complaints.
	 */
	public List<Complaint> getAllMedicalComplaints() {
	    return new ArrayList<Complaint>(complaints.values());
	}

	/**
	 * This is a finder method that returns a Medical Complaint matching the specified name.
	 * @param name Name of the complaint to retrieve.
	 * @return Matched complaint, if none is found then a null.
	 */
	public Complaint getComplaintByName(String name) {
		return complaints.get(name);
	}

	/**
	 * This is a finder method that returns a Medical Treatment matching the specified name.
	 * @param name Name of the treatment to retrieve.
	 * @return Matched Treatment, if none is found then a null.
	 */
	public Treatment getTreatmentByName(String name) {
		return treatments.get(name);
	}

	/**
	 * Return the pre-defined Medical Complaint that signifies a suffocation complaint.
	 * @return Medical complaint for shortage of oxygen.
	 */
	public Complaint getSuffocation() {
		return suffocation;
	}

	/**
	 * Get the supported Treatments for a Medical Facility of a particular level. This will be a combination of all the
	 * Treatments of the specified level and all those lower.
	 * @param level Level of Medical facility.
	 * @return List of Treatments
	 */
	public List<Treatment> getSupportedTreatments(int level) {
		Integer key = level;
		List<Treatment> results = supported.get(key);
		if (results == null) {
			results = new ArrayList<Treatment>();
			Iterator<Treatment> iter = treatments.values().iterator();
			while (iter.hasNext()) {
				Treatment next = iter.next();
				if (next.getFacilityLevel() <= level)
					results.add(next);
			}
			Collections.sort(results);
			supported.put(key, results);
		}
		return results;
	}

	/**
	 * Return the pre-defined Medical Complaint that signifies a dehydration complaint.
	 * @return Medical complaint for shortage of water.
	 */
	public Complaint getDehydration() {
		return dehydration;
	}

	/**
	 * Return the pre-defined Medical Complaint that signifies a starvation complaint.
	 * @return Medical complaint for shortage of oxygen.
	 */
	public Complaint getStarvation() {
		return starvation;
	}

	/**
	 * Return the pre-defined Medical Complaint that signifies a Decompression complaint.
	 * @return Medical complaint for decompression.
	 */
	public Complaint getDecompression() {
		return decompression;
	}

	/**
	 * Return the pre-defined Medical Complaint that signifies a Freezing complaint.
	 * @return Medical complaint for freezing.
	 */
	public Complaint getFreezing() {
		return freezing;
	}

	/**
	 * Return the pre-defined Medical Complaint that signifies a Heat Stroke complaint.
	 * @return Medical complaint for heat stroke.
	 */
	public Complaint getHeatStroke() {
		return heatStroke;
	}

	/**
	 * Checks if a health complaint is an environmental complaint.
	 * @param complaint the complaint to check.
	 * @return true if complaint is environmental complaint.
	 */
	public boolean isEnvironmentalComplaint(Complaint complaint) {
		boolean result = false;

		if (complaint == suffocation)
			result = true;
		if (complaint == dehydration)
			result = true;
		if (complaint == starvation)
			result = true;
		if (complaint == decompression)
			result = true;
		if (complaint == freezing)
			result = true;
		if (complaint == heatStroke)
			result = true;

		return result;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		complaints.clear();
		complaints = null;
		treatments.clear();
		treatments = null;
		supported.clear();
		supported = null;
		starvation = null;
		suffocation = null;
		dehydration = null;
		decompression = null;
		freezing = null;
		heatStroke = null;
	}
}