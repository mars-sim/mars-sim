/**
 * Mars Simulation Project
 * MedicalManager.java
 * @version 3.1.0 2017-03-09
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.health;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.structure.Settlement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides a Factory for the {@link Complaint} class. Some of the
 * Medical Complaints are pre-defined. Instances are accessed via a factory
 * method since the properties of the individual complaints are loaded from the
 * XML.
 */
public class MedicalManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static final Logger logger =
	// Logger.getLogger(MedicalManager.class.getName());

	public final static int MINUTES_PER_DAY = 24 * 60;

	/** Possible Complaints. */
	private static Map<ComplaintType, Complaint> complaints;// = new HashMap<ComplaintType, Complaint>();
	/** Environmentally Related Complaints. */
	private static Map<ComplaintType, Complaint> environmentalComplaints;// = new HashMap<ComplaintType, Complaint>();

	/** Possible Treatments. */
	private static Map<String, Treatment> treatments;// = new HashMap<String, Treatment>();
	/** Treatments to Facilities. */
	private static Map<Integer, List<Treatment>> supportedTreatments;// = new HashMap<Integer, List<Treatment>>();

	/** Settlement's Postmortem Exam waiting list. */
	private Map<Integer, List<DeathInfo>> awaitingPostmortemExam;// = new HashMap<Integer, List<Treatment>>();
	/** Settlement's Death Registry. */
	private Map<Integer, List<DeathInfo>> deathRegistry;// = new HashMap<Integer, List<Treatment>>();

//	private List<DeathInfo> awaitingPostmortemExam;
//	private List<DeathInfo> deathRegistry;

	/** Pre-defined complaint. */
	private static Complaint starvation;
	/** Pre-defined complaint. */
	private static Complaint suffocation;
	/** Pre-defined complaint. */
	private static Complaint dehydration;
	/** Pre-defined complaint. */
	private static Complaint decompression;
	/** Pre-defined complaint. */
	private static Complaint freezing;
	/** Pre-defined complaint. */
	private static Complaint heatStroke;

//	private static SimulationConfig simConfig = SimulationConfig.instance();
//	private static MedicalConfig medicalConfig;
//	private static PersonConfig personConfig;

	/**
	 * Construct a new {@link MedicalManager}. This also constructs all the
	 * pre-defined Complaints and the user-defined ones in the XML configuration
	 * file.
	 */
	public MedicalManager() {
		complaints = new HashMap<ComplaintType, Complaint>();
		environmentalComplaints = new HashMap<ComplaintType, Complaint>();
		treatments = new HashMap<String, Treatment>();
		supportedTreatments = new HashMap<Integer, List<Treatment>>();
		
		awaitingPostmortemExam = new HashMap<>();
		deathRegistry = new HashMap<>();
		
		initializeInstances();
	}

	/**
	 * Initialize the Medical Complaints from the configuration.
	 * 
	 * @throws exception if not able to initialize complaints.
	 */
	public static void initializeInstances() {
		// Maven test requires the full "SimulationConfig.instance()" declaration here, rather than during class declaration.
		MedicalConfig medicalConfig = SimulationConfig.instance().getMedicalConfiguration();
//		System.out.println("medicalConfig : " + medicalConfig);
		setUpEnvironmentalComplaints();

		// Create treatments from medical config.
		Iterator<Treatment> i = medicalConfig.getTreatmentList().iterator();
		while (i.hasNext())
			addTreatment(i.next());

		// logger.info("initMedical() : adding Complaints");

		// Create additional complaints from medical config.
		Iterator<Complaint> j = medicalConfig.getComplaintList().iterator();
		while (j.hasNext())
			addComplaint(j.next());
	}

	/***
	 * Create the pre-defined environmental complaints using person configuration.
	 */
	private static void setUpEnvironmentalComplaints() {
		
		// Maven test requires the full "SimulationConfig.instance()" declaration here, rather than during class declaration.
		PersonConfig personConfig = SimulationConfig.instance().getPersonConfig(); 

		// Most serious complaint
//		System.out.println("suffocation : " + suffocation);
		suffocation = createEnvironmentComplaint(ComplaintType.SUFFOCATION, 80, personConfig.getOxygenDeprivationTime(),
				.5, 20, true);
//		System.out.println("suffocation : " + suffocation);
		addEnvComplaint(suffocation);

		// Very serious complaint
		decompression = createEnvironmentComplaint(ComplaintType.DECOMPRESSION, 70, personConfig.getDecompressionTime(),
				.5, 30, true);
		addEnvComplaint(decompression);

		// Somewhat serious complaint
		heatStroke = createEnvironmentComplaint(ComplaintType.HEAT_STROKE, 40, 200D, 1, 60, true);
		addEnvComplaint(heatStroke);

		// Serious complaint
		freezing = createEnvironmentComplaint(ComplaintType.FREEZING, 50, personConfig.getFreezingTime(), 1, 50, false);
		addEnvComplaint(freezing);

		// Somewhat serious complaint
		dehydration = createEnvironmentComplaint(ComplaintType.DEHYDRATION, 20,
				(personConfig.getWaterDeprivationTime() - personConfig.getDehydrationStartTime()) * 1000D, 1, 80,
				false);
		addEnvComplaint(dehydration);

		// Least serious complaint
		starvation = createEnvironmentComplaint(ComplaintType.STARVATION, 40,
				(personConfig.getFoodDeprivationTime() - personConfig.getStarvationStartTime()) * 1000D, 1, 60, false);
		addEnvComplaint(starvation);
	}

	static void addEnvComplaint(Complaint c) {
		environmentalComplaints.put(c.getType(), c);
	}

	/**
	 * Create an environment related Complaint. These are started by the simulation
	 * and not via randomness. The all result in death hence have no next phase and
	 * no recovery period, when the environment changes, the complaint is resolved.
	 */

	/***
	 * Create an environment related Complaint.
	 * 
	 * @param type
	 * @param seriousness
	 * @param degrade
	 * @param recovery
	 * @param performance
	 * @param needBedRest
	 * 
	 * @return {@link Complaint}
	 */
	private static Complaint createEnvironmentComplaint(ComplaintType type, int seriousness, double degrade,
			double recovery, double performance, boolean needBedRest) {
		return new Complaint(type, seriousness, degrade, recovery, 0D, performance, needBedRest, null, null);
	}

//	/**
//	 * Package friendly factory method.
//	 * 
//	 * @param type
//	 * @param seriousness
//	 * @param degrade
//	 * @param recovery
//	 * @param probability
//	 * @param performance
//	 * @param needBedRest
//	 * @param treatment
//	 * @param             {@link Complaint}
//	 */
//	void createComplaint(ComplaintType type, 
//			int seriousness, double degrade,
//			double recovery, double probability, double performance,
//			boolean bedRest, Treatment recoveryTreatment, Complaint next) {
//
//		Complaint complaint = new Complaint(type, seriousness, degrade,
//				recovery, probability, performance, bedRest, recoveryTreatment, next);
//		// Add an entry keyed on name.
//		complaints.put(type, complaint);
//	}

	/**
	 * Adds a new complaint to the map.
	 * 
	 * @param newComplaint the new complaint to add.
	 * @throws Exception if complaint already exists in map.
	 */
	static void addComplaint(Complaint newComplaint) {
		if (!complaints.containsKey(newComplaint.getType()))
			complaints.put(newComplaint.getType(), newComplaint);
		else
			throw new IllegalStateException(Msg.getString("MedicalManager.error.complaint", //$NON-NLS-1$
					newComplaint.getType().toString()));
	}

	/**
	 * Package friendly factory method.
	 */
	void createTreatment(String name, int skill, double duration, boolean selfHeal, boolean retainAid, int level) {
		Treatment newTreatment = new Treatment(name, skill, duration, selfHeal, level);
		treatments.put(name, newTreatment);
	}

	/**
	 * Adds a new treatment to the map.
	 * 
	 * @param newTreatment the new treatment to add.
	 * @throws Exception if treatment already exists in map.
	 */
	static void addTreatment(Treatment newTreatment) {
		if (!treatments.containsKey(newTreatment.getName()))
			treatments.put(newTreatment.getName(), newTreatment);
		else
			throw new IllegalStateException(Msg.getString("MedicalManager.error.treatment", //$NON-NLS-1$
					newTreatment.getName()));
	}

	/**
	 * Gets a list of all medical complaints.
	 * 
	 * @return list of complaints.
	 */
	public List<Complaint> getAllMedicalComplaints() {
		return new ArrayList<Complaint>(complaints.values());
	}

	/**
	 * Gets a list of all environmentally related complaints.
	 * 
	 * @return list of environmental complaints.
	 */
	public List<Complaint> getAllEnvironmentalComplaints() {
		return new ArrayList<Complaint>(environmentalComplaints.values());
	}

	/**
	 * This is a finder method that returns a Medical Complaint matching the
	 * specified name.
	 * 
	 * @param name Name of the complaintType to retrieve.
	 * @return Matched complaint, if none is found then a null.
	 */
	public Complaint getComplaintByName(ComplaintType type) {
		if (complaints.containsKey(type))
			return complaints.get(type);
		else if (environmentalComplaints.containsKey(type))
			return environmentalComplaints.get(type);
		else
			return null;
	}

	/**
	 * This is a finder method that returns a Medical Treatment matching the
	 * specified name.
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
	 * Get the supported Treatments for a Medical Facility of a particular level.
	 * This will be a combination of all the Treatments of the specified level and
	 * all those lower.
	 * 
	 * @param level Level of Medical facility.
	 * @return List of Treatments
	 */
	public List<Treatment> getSupportedTreatments(int level) {
		Integer key = level;
		List<Treatment> results = supportedTreatments.get(key);
		if (results == null) {
			results = new ArrayList<Treatment>();
			Iterator<Treatment> iter = treatments.values().iterator();
			while (iter.hasNext()) {
				Treatment next = iter.next();
				if (next.getFacilityLevel() <= level)
					results.add(next);
			}
			Collections.sort(results);
			supportedTreatments.put(key, results);
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
	 * Return the pre-defined Medical Complaint that signifies a starvation
	 * complaint.
	 * 
	 * @return Medical complaint for shortage of oxygen.
	 */
	public Complaint getStarvation() {
		return starvation;
	}

	/**
	 * Return the pre-defined Medical Complaint that signifies a Decompression
	 * complaint.
	 * 
	 * @return Medical complaint for decompression.
	 */
	public Complaint getDecompression() {
		return decompression;
	}

	/**
	 * Return the pre-defined Medical Complaint that signifies a Freezing complaint.
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

		if (complaint == suffocation)
			result = true;
		else if (complaint == dehydration)
			result = true;
		else if (complaint == starvation)
			result = true;
		else if (complaint == decompression)
			result = true;
		else if (complaint == freezing)
			result = true;
		else if (complaint == heatStroke)
			result = true;

		return result;
	}

	public void addDeathRegistry(Settlement s, DeathInfo death) {
		int id = s.getIdentifier();
		awaitingPostmortemExam.get(id).remove(death);
		if (deathRegistry.containsKey(id)) {
			deathRegistry.get(id).add(death);
		} else {
			List<DeathInfo> list = new ArrayList<>();
			list.add(death);
			deathRegistry.put(id, list);
		}
	}

	public List<DeathInfo> getDeathRegistry(Settlement s) {
		if (deathRegistry.containsKey(s.getIdentifier())) {
			return deathRegistry.get(s.getIdentifier());
		} else {
			return null;
		}
	}

	public void addPostmortemExams(Settlement s, DeathInfo death) {
		int id = s.getIdentifier();
		if (awaitingPostmortemExam.containsKey(id)) {
			awaitingPostmortemExam.get(id).add(death);
		} else {
			List<DeathInfo> list = new ArrayList<>();
			list.add(death);
			awaitingPostmortemExam.put(id, list);
		}
	}

	public List<DeathInfo> getPostmortemExams(Settlement s) {
		if (awaitingPostmortemExam.containsKey(s.getIdentifier())) {
			return awaitingPostmortemExam.get(s.getIdentifier());
		} else {
			List<DeathInfo> list = new ArrayList<>();
			return list;
		}
	}

	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 */
	public static void justReloaded() {
//		initMedical();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {

		environmentalComplaints.clear();
		environmentalComplaints = null;
		complaints.clear();
		complaints = null;
		treatments.clear();
		treatments = null;
		supportedTreatments.clear();
		supportedTreatments = null;

		starvation = null;
		suffocation = null;
		dehydration = null;
		decompression = null;
		freezing = null;
		heatStroke = null;

//		simConfig = null;
	}
}