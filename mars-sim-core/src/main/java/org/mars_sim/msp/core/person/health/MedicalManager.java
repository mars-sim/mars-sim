/**
 * Mars Simulation Project
 * MedicalManager.java
 * @version 3.2.0 2021-06-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.health;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This class provides a Factory for the {@link Complaint} class. Some of the
 * Medical Complaints are pre-defined. Instances are accessed via a factory
 * method since the properties of the individual complaints are loaded from the
 * XML.
 */
public class MedicalManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static final Logger logger = Logger.getLogger(MedicalManager.class.getName());

	public final static int MINUTES_PER_DAY = 24 * 60;

	/** Possible Complaints. */
//	private static Map<ComplaintType, Complaint> complaints;// = new HashMap<ComplaintType, Complaint>();
	/** Environmentally Related Complaints. */
	private static Map<ComplaintType, Complaint> environmentalComplaints;

	/** Treatments based on a facility's tech level. */
	private static Map<Integer, List<Treatment>> supportedTreatments;

	/** Settlement's Postmortem Exam waiting list. */
	private Map<Integer, List<DeathInfo>> awaitingPostmortemExam;
	/** Settlement's Death Registry. */
	private Map<Integer, List<DeathInfo>> deathRegistry;

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

	private static MedicalConfig medicalConfig;

	/**
	 * Construct a new {@link MedicalManager}. This also constructs all the
	 * pre-defined Complaints and the user-defined ones in the XML configuration
	 * file.
	 */
	public MedicalManager() {
		if (medicalConfig == null)
			medicalConfig = SimulationConfig.instance().getMedicalConfiguration();
		environmentalComplaints = new ConcurrentHashMap<ComplaintType, Complaint>();
		supportedTreatments = new ConcurrentHashMap<Integer, List<Treatment>>();
		
		awaitingPostmortemExam = new ConcurrentHashMap<>();
		deathRegistry = new ConcurrentHashMap<>();
		
		initializeInstances();
	}

	/**
	 * Initialize the Medical Complaints from the configuration.
	 * 
	 * @throws exception if not able to initialize complaints.
	 */
	public static void initializeInstances() {
		// Maven test requires the full "SimulationConfig.instance()" declaration here, rather than during class declaration.
		medicalConfig = SimulationConfig.instance().getMedicalConfiguration();

		setUpEnvironmentalComplaints();
	}

	/***
	 * Create the pre-defined environmental complaints using person configuration.
	 */
	private static void setUpEnvironmentalComplaints() {
		
		// TODO: Why are these not created via the medical.xml and then lookup by a fixed name ?????
		// Bad case of magic numbers.
		// Maven test requires the full "SimulationConfig.instance()" declaration here, rather than during class declaration.
		PersonConfig personConfig = SimulationConfig.instance().getPersonConfig(); 

		// Most serious complaint
		suffocation = createEnvironmentComplaint(ComplaintType.SUFFOCATION, 80, personConfig.getOxygenDeprivationTime(),
				.5, 20, true);

		// Very serious complaint
		decompression = createEnvironmentComplaint(ComplaintType.DECOMPRESSION, 70, personConfig.getDecompressionTime(),
				.5, 30, true);

		// Somewhat serious complaint
		heatStroke = createEnvironmentComplaint(ComplaintType.HEAT_STROKE, 40, 200D, 1, 60, true);

		// Serious complaint
		freezing = createEnvironmentComplaint(ComplaintType.FREEZING, 50, personConfig.getFreezingTime(), 1, 50, false);

		// degrade = (7 - 3) * 1000 millisols
		double degrade = (personConfig.getWaterDeprivationTime() - personConfig.getDehydrationStartTime()) * 1000D;
		dehydration = createEnvironmentComplaint(ComplaintType.DEHYDRATION, 20,
				degrade, 1, 80,
				false);

		// degrade = (40 - 7) * 1000 millisols
		degrade = (personConfig.getFoodDeprivationTime() - personConfig.getStarvationStartTime()) * 1000D;
		starvation = createEnvironmentComplaint(ComplaintType.STARVATION, 40,
				degrade, 1, 60, false);
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
		Complaint c = new Complaint(type, seriousness, degrade, recovery, 
				0D, performance, needBedRest, null, null);

		environmentalComplaints.put(type, c);
		return c;
	}

	/**
	 * Gets a list of all medical complaints.
	 * 
	 * @return list of complaints.
	 */
	public List<Complaint> getAllMedicalComplaints() {
		return medicalConfig.getComplaintList();
	}

	/**
	 * This is a finder method that returns a Medical Complaint matching the
	 * specified name.
	 * 
	 * @param name Name of the complaintType to retrieve.
	 * @return Matched complaint, if none is found then a null.
	 */
	public Complaint getComplaintByName(ComplaintType type) {
		for (Complaint c : medicalConfig.getComplaintList()) {
			if (type == c.getType())
				return c;
		}
		
		if (environmentalComplaints.containsKey(type))
			return environmentalComplaints.get(type);
		
		return null;
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
		if (!supportedTreatments.isEmpty() && supportedTreatments.get(level) != null)
			return supportedTreatments.get(level);
		
		List<Treatment> results = new ArrayList<>();
		List<Treatment> list = SimulationConfig.instance().getMedicalConfiguration().getTreatmentList();

		Iterator<Treatment> iter = list.iterator();
		while (iter.hasNext()) {
			Treatment next = iter.next();
			if (next.getFacilityLevel() <= level)
				results.add(next);
		}
		Collections.sort(results);
		supportedTreatments.put(level, results);

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
		return environmentalComplaints.containsKey(complaint.getType());
	}

	public void addDeathRegistry(Settlement s, DeathInfo death) {
		int id = s.getIdentifier();
		awaitingPostmortemExam.get(id).remove(death);
		if (deathRegistry.containsKey(id)) {
			deathRegistry.get(id).add(death);
		} else {
			List<DeathInfo> list = new CopyOnWriteArrayList<>();
			list.add(death);
			deathRegistry.put(id, list);
		}
	}

	public List<DeathInfo> getDeathRegistry(Settlement s) {
		return deathRegistry.getOrDefault(s.getIdentifier(), null);
	}

	public void addPostmortemExams(Settlement s, DeathInfo death) {
		int id = s.getIdentifier();
		if (awaitingPostmortemExam.containsKey(id)) {
			awaitingPostmortemExam.get(id).add(death);
		} else {
			List<DeathInfo> list = new CopyOnWriteArrayList<>();
			list.add(death);
			awaitingPostmortemExam.put(id, list);
		}
	}

	public List<DeathInfo> getPostmortemExams(Settlement s) {
		if (awaitingPostmortemExam.containsKey(s.getIdentifier())) {
			return awaitingPostmortemExam.get(s.getIdentifier());
		}
		
		return Collections.emptyList();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {

		environmentalComplaints = null;
		supportedTreatments = null;

		starvation = null;
		suffocation = null;
		dehydration = null;
		decompression = null;
		freezing = null;
		heatStroke = null;
	}
}
