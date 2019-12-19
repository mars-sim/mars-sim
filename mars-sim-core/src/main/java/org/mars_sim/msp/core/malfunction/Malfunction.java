/**
 * Mars Simulation Project
 * Malfunction.java
 * @version 3.1.0 2017-09-05
 * @author Scott Davis
 */

package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Malfunction class represents a malfunction in a vehicle, structure or
 * equipment.
 */
public class Malfunction implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Malfunction.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	private static final String INCIDENT_NUM = " - incident #";

	// Data members
	private int severity;
	private int incidentNum;

	private double probability;
	
	// Work time tracking
	private double generalWorkTimeExpected = 0;
	private double generalWorkTimeCompleted = 0;
	
	private double emergencyWorkTimeExpected = 0;
	private double emergencyWorkTimeCompleted = 0;
	
	private double EVAWorkTimeExpected = 0;
	private double EVAWorkTimeCompleted = 0;

	private String name;

	/* The person who are being the most traumatized by this malfunction */
	private String mostTraumatized = "None";

	private Collection<String> systems;
	private Map<Integer, Double> resourceEffects;
	private Map<String, Double> lifeSupportEffects;
	private Map<ComplaintType, Double> medicalComplaints;
	private Map<Integer, Integer> repairParts;

	/** The map for storing how much work time the repairers spent in fixing this malfunction. */
	private Map<String, Double> repairersWorkTime;
	
	/** The chief repairer for each type of repair work */
	private Map<Integer, String> chiefRepairers;
	
	/** The deputy repairer for each type of repair work */
	private Map<Integer, String> deputyRepairers;
	
	private static MalfunctionConfig malfunctionConfig = SimulationConfig.instance().getMalfunctionConfiguration();

	/**
	 * Constructs a Malfunction object
	 * 
	 * @param name name of the malfunction
	 */
	public Malfunction(String name, int incidentNum, int severity, double probability, double emergencyWorkTime,
			double workTime, double EVAWorkTime, Collection<String> entities, Map<Integer, Double> resourceEffects,
			Map<String, Double> lifeSupportEffects, Map<ComplaintType, Double> medicalComplaints) {

		// Initialize data members
		this.name = name;
		this.incidentNum = incidentNum;
		this.severity = severity;
		this.probability = probability;
		this.emergencyWorkTimeExpected = computeWorkTime(emergencyWorkTime);
		this.generalWorkTimeExpected = computeWorkTime(workTime);
		this.EVAWorkTimeExpected = computeWorkTime(EVAWorkTime);
		this.systems = entities;
		this.resourceEffects = resourceEffects;
		this.lifeSupportEffects = lifeSupportEffects;
		this.medicalComplaints = medicalComplaints;

		repairParts = new HashMap<>();
		repairersWorkTime = new HashMap<>();
		chiefRepairers = new HashMap<>();
		deputyRepairers = new HashMap<>();
		
		if (needGeneralRepair()) {
			chiefRepairers.put(0, "");
			deputyRepairers.put(0, "");
		}
		if (needEmergencyRepair()) {
			chiefRepairers.put(1, "");
			deputyRepairers.put(1, "");
		}
		if (needEVARepair()) {
			chiefRepairers.put(2, "");
			deputyRepairers.put(2, "");
		}
	}

	/**
	 * Obtains the name of the chief repairer
	 * 
	 * @param type 1: general repair; 2: emergency repair; 3: EVA repair
	 * @return
	 */
	public String getChiefRepairer(int type) {
		if (chiefRepairers.containsKey(type)) {
			return chiefRepairers.get(type);
		}
		return null;
	}
	
	/**
	 * Sets the name of the deputy repairer of a particular type of repair
	 * 
	 * @param type 1: general repair; 2: emergency repair; 3: EVA repair
	 * @param name
	 */
	public void setDeputyRepairer(int type, String name) {
		deputyRepairers.put(type, name);
	}
	
	/**
	 * Obtains the name of the deputy repairer
	 * 
	 * @param type 1: general repair; 2: emergency repair; 3: EVA repair
	 * @return
	 */
	public String getDeputyRepairer(int type) {
		if (deputyRepairers.containsKey(type)) {
			return deputyRepairers.get(type);
		}
		return null;
	}
	
	/**
	 * Checks if this person is already a repairer or not
	 * 
	 * @param name
	 * @return true if htis person is already a repairer of this malfunction
	 */
	public boolean isARepairer(String name) {
		for (String n: chiefRepairers.values()) {
			if (n.equalsIgnoreCase(name))
				return true;
		}
		for (String n: deputyRepairers.values()) {
			if (n.equalsIgnoreCase(name))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if all repairer slots are filled
	 * 
	 * @return
	 */
	public boolean areAllRepairerSlotsFilled() {
		for (String n: chiefRepairers.values()) {
			if (n.equalsIgnoreCase(""))
				return false;
		}
		for (String n: deputyRepairers.values()) {
			if (n.equalsIgnoreCase(""))
				return false;
		}
		return true;
	}
	
	/**
	 * Checks if all repairer slots are filled
	 * 
	 * @return
	 */
	public int numRepairerSlotsEmpty(int type) {
		int emptySlots = 0;
		if (chiefRepairers.containsKey(type)) {
			if (chiefRepairers.get(type).equalsIgnoreCase(""))
				emptySlots++;
		}
		if (deputyRepairers.containsKey(type)) {
			if (deputyRepairers.get(type).equalsIgnoreCase(""))
				emptySlots++;
		}
		return emptySlots;
	}
	
	/**
	 * Sets the name of the chief repairer of a particular type of repair
	 * 
	 * @param type 1: general repair; 2: emergency repair; 3: EVA repair
	 * @param name
	 */
	public void setChiefRepairer(int type, String name) {
		chiefRepairers.put(type, name);
	}
	
	
	/**
	 * Computes the expected work time on a gaussian curve
	 * 
	 * @param time
	 * @return
	 */
	public double computeWorkTime(double time) {
		if (time < 1)
			return time;
		
		double t = 0;
		
		do {
			t = time + RandomUtil.getGaussianDouble() * time/4D;
			if (t < 0)
				t = -t;
		}
		// Limit the expected work time to no more than 5x the average work time.
		while (t == 0 || t > 5 * time);
			
		return t;
		
//		if (RandomUtil.getRandomInt(3) == 0)
//			return time/8D + RandomUtil.getRandomDouble(time *.875);
//		else
//			// Set it to be at least a quarter of its value to its full value
//			return RandomUtil.getRandomRegressionInteger((int)(time/4D), (int)time);
	}
	
	/**
	 * Returns the name of the malfunction.
	 * 
	 * @return name of the malfunction
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns true if malfunction is fixed.
	 * 
	 * @return true if malfunction is fixed
	 */
	public boolean isFixed() {
		return isGeneralRepairDone() && isEmergencyRepairDone() && isEVARepairDone();
	}

	/**
	 * Is the general repair done ?
	 * 
	 * @return true if general repair is done
	 */
	public boolean isGeneralRepairDone() {
		if (generalWorkTimeExpected > 0 && generalWorkTimeCompleted <= generalWorkTimeExpected)
			// If generalWorkTimeExpected exists for this malfunction
			return false;
		return true;
	}

	/**
	 * Is the emergency repair done ?
	 * 
	 * @return true if emergency repair is done
	 */
	public boolean isEmergencyRepairDone() {
		if (emergencyWorkTimeExpected > 0 && emergencyWorkTimeCompleted <= emergencyWorkTimeExpected)
			// If emergencyWorkTimeExpected exists for this malfunction
			return false;
		return true;
	}

	/**
	 * Is the EVA repair done ?
	 * 
	 * @return true if EVA repair is done
	 */
	public boolean isEVARepairDone() {
		if (EVAWorkTimeExpected > 0 && EVAWorkTimeCompleted <= EVAWorkTimeExpected)
			// If EVAWorkTimeExpected exists for this malfunction
			return false;
		return true;
	}
	
	/**
	 * Returns the total percentage fixed 
	 * 
	 * @return the percent
	 */
	public double getPercentageFixed() {
		double totalRequiredWork = emergencyWorkTimeExpected + generalWorkTimeExpected + EVAWorkTimeExpected;
		double totalCompletedWork = emergencyWorkTimeCompleted + generalWorkTimeCompleted
				+ EVAWorkTimeCompleted;
		int percentComplete = 0;
		if (totalRequiredWork > 0D)
			percentComplete = (int) (100D * (totalCompletedWork / totalRequiredWork));
		return percentComplete;
//		double result = 0;
//		int count = 0;
//
//		if (workTime > 0) {
//			result += workTimeCompleted/workTime;
//			count++;
//		}
//		if (emergencyWorkTime > 0) {
//			result += emergencyWorkTimeCompleted/emergencyWorkTime;
//			count++;
//		}
//		if (EVAWorkTime > 0) {
//			result += EVAWorkTimeCompleted/EVAWorkTime;
//			count++;
//		}
//		
//		return result/(double)count;
	}
	
	/**
	 * Returns the severity level of the malfunction.
	 * 
	 * @return severity of malfunction (1 - 100)
	 */
	public int getSeverity() {
		return severity;
	}

	/**
	 * Returns the probability of failure of the malfunction
	 * 
	 * @return probability in %
	 */
	public double getProbability() {
		return probability;
	}

	public void setProbability(double p) {
		probability = p;
	}

	/**
	 * Returns the work time required to repair the malfunction.
	 * 
	 * @return work time (in millisols)
	 */
	public double getGeneralWorkTime() {
		return generalWorkTimeExpected;
	}

	/**
	 * Does this malfunction require General Repair ?
	 * 
	 * @return true if it does
	 */
	public boolean needGeneralRepair() {
		return generalWorkTimeExpected > 0;
	}
	
	/**
	 * Returns the general completed work time.
	 * 
	 * @return completed general work time (in millisols)
	 */
	public double getCompletedGeneralWorkTime() {
		return generalWorkTimeCompleted;
	}

	/**
	 * Adds general work time to the malfunction.
	 * 
	 * @param time general work time (in millisols)
	 * @return remaining general work time not used (in millisols)
	 */
	public double addGeneralWorkTime(double time, String repairer) {
		// if this malfunction has general repair work
		double t0 = generalWorkTimeExpected;
		double t = generalWorkTimeCompleted;
		if (t0 > 0) {
			if (t == 0) {
				String id_string = INCIDENT_NUM + incidentNum;
				LogConsolidated.log(Level.INFO, 10_000, sourceName,
						name + id_string + " - General repair work initiated by " + repairer + ".");
			}
			
			t += time;
			
			// Since the general repair work time should be just an "estimated" figure, 
			// each time a repair is being worked on, it should get closer to reveal the "actual" repair work time 
			// but not exactly know its "actual" value. Use PRNG to simulate this Stochastic nature due to the 
			// uncertainty and unpredictability of repair
			// TODO: the mechanic skill or "troubleshooting" skill should contribute to reducing the randomness.
			t0 = t0 + (t0 - t) * (RandomUtil.getRandomDouble(.1) - RandomUtil.getRandomDouble(.1));
			
			if (t0 > 0)
				generalWorkTimeExpected = t0;
			else
				t0 = generalWorkTimeExpected;
		
			generalWorkTimeCompleted = t;
			
			if (repairersWorkTime.containsKey(repairer)) {
				repairersWorkTime.put(repairer, repairersWorkTime.get(repairer) + time);
			} else {
				repairersWorkTime.put(repairer, time);
			}
			
			if (t > t0) {
				double remaining = t - t0;
				
				return remaining;
			}
			
		}
		else
			return time;
		return 0D;
	}

	/**
	 * Returns the emergency work time required to repair the malfunction.
	 * 
	 * @return emergency work time (in millisols)
	 */
	public double getEmergencyWorkTime() {
		return emergencyWorkTimeExpected;
	}

	/**
	 * Does this malfunction require Emergency Repair ?
	 * 
	 * @return true if it does
	 */
	public boolean needEmergencyRepair() {
		return emergencyWorkTimeExpected > 0;
	}

	/**
	 * Returns the completed emergency work time.
	 * 
	 * @return completed emergency work time (in millisols)
	 */
	public double getCompletedEmergencyWorkTime() {
		return emergencyWorkTimeCompleted;
	}

	/**
	 * Adds emergency work time to the malfunction.
	 * 
	 * @param time emergency work time (in millisols)
	 * @return remaining work time not used (in millisols)
	 */
	public double addEmergencyWorkTime(double time, String repairer) {
		// if this malfunction has emergency repair work
		double t0 = emergencyWorkTimeExpected;
		double t = emergencyWorkTimeCompleted;
		if (t0 > 0) {
			if (t == 0) {
				String id_string = INCIDENT_NUM + incidentNum;
				LogConsolidated.log(Level.INFO, 10_000, sourceName,
						name + id_string + " - Emergency repair work initiated by " + repairer + ".");
			}
			
			t += time;
			
			// Add randomness to the expected emergency work time
			t0 = t0 + (t0 - t) * (RandomUtil.getRandomDouble(.1) - RandomUtil.getRandomDouble(.1));
			
			if (t0 > 0)
				emergencyWorkTimeExpected = t0;
			else
				t0 = emergencyWorkTimeExpected;
		
			emergencyWorkTimeCompleted = t;
			
			if (repairersWorkTime.containsKey(repairer)) {
				repairersWorkTime.put(repairer, repairersWorkTime.get(repairer) + time);
			} else {
				repairersWorkTime.put(repairer, time);
			}		
			
			if (t > t0) {
				double remaining = t - t0;
				
				return remaining;
			}

		}
		else
			return time;
		return 0D;
	}

	/**
	 * Returns the EVA work time required to repair the malfunction.
	 * 
	 * @return EVA work time (in millisols)
	 */
	public double getEVAWorkTime() {
		return EVAWorkTimeExpected;
	}

	/**
	 * Does this malfunction require EVA Repair ?
	 * 
	 * @return true if it does
	 */
	public boolean needEVARepair() {
		return EVAWorkTimeExpected > 0;
	}

	/**
	 * Returns the completed EVA work time.
	 * 
	 * @return completed EVA work time (in millisols)
	 */
	public double getCompletedEVAWorkTime() {
		return EVAWorkTimeCompleted;
	}
	
	/**
	 * Adds EVA work time to the malfunction.
	 * 
	 * @param time EVA work time (in millisols)
	 * @return remaining work time not used (in millisols)
	 */
	public double addEVAWorkTime(double time, String repairer) {
//		logger.info(repairer + "  time : " + time);
		// if this malfunction has EVA repair work
		double t0 = EVAWorkTimeExpected;
		double t = EVAWorkTimeCompleted;
		if (t0 > 0) {
			if (t == 0) {
				String id_string = INCIDENT_NUM + incidentNum;
				LogConsolidated.log(Level.INFO, 10_000, sourceName,
						name + id_string + " - EVA repair work initiated by " + repairer + ".");
			}
			
			t += time;
			
			// Add randomness to the expected EVA work time
			t0 = t0 + (t0 - t) * (RandomUtil.getRandomDouble(.1) - RandomUtil.getRandomDouble(.1));
				
			t += time;
			
			if (t0 > 0)
				EVAWorkTimeExpected = t0;
			else
				t0 = EVAWorkTimeExpected;
		
			EVAWorkTimeCompleted = t;
			
			if (repairersWorkTime.containsKey(repairer)) {
				repairersWorkTime.put(repairer, repairersWorkTime.get(repairer) + time);
			} else {
				repairersWorkTime.put(repairer, time);
			}
			
			if (t > t0) {
				double remaining = t - t0;
//				logger.info(repairer + "  remaining : " + remaining);
				return remaining;
			}
			
		}
		else
			return time;
		
		return 0D;
	}

	/**
	 * Gets the name of the person who spent most time repairing this malfunction
	 * 
	 * @return the name of the person
	 */
	public String getMostProductiveRepairer() {
		Map.Entry<String, Double> maxEntry = repairersWorkTime.entrySet()
				.stream()
				.max(Map.Entry.comparingByValue())
				.orElse(null); // .get()
    	if (maxEntry != null && maxEntry.getKey() != null)
    		return maxEntry.getKey();
    	else
    		return null;
	}

	/**
	 * Checks if a unit's scope strings have any matches with the malfunction's
	 * scope strings.
	 * 
	 * @return true if any matches
	 */
	public boolean isMatched(Collection<String> scopes) {
//		boolean result = false;

		if ((systems.size() > 0) && (scopes.size() > 0)) {
			for (String s : systems) {
				for (String u : scopes) {
					if (s.equalsIgnoreCase(u))
						return true;//result = true;
				}
			}
		}

		return false;
	}

	/**
	 * Gets the resource effects of the malfunction.
	 * 
	 * @return resource effects as name-value pairs in Map
	 */
	public Map<Integer, Double> getResourceEffects() {
		return resourceEffects;
	}

	/**
	 * Gets the life support effects of the malfunction.
	 * 
	 * @return life support effects as name-value pairs in Map
	 */
	public Map<String, Double> getLifeSupportEffects() {
		return lifeSupportEffects;
	}

	/**
	 * Gets the medical complaints produced by this malfunction and their
	 * probability of occurrence.
	 * 
	 * @return medical complaints as name-value pairs in Map
	 */
	public Map<ComplaintType, Double> getMedicalComplaints() {
		return medicalComplaints;
	}

	/**
	 * Gets a clone of this malfunction.
	 * 
	 * @return clone of this malfunction
	 */
	public Malfunction getClone() {
		int id = Simulation.instance().getMalfunctionFactory().getNewIncidentNum();
		Malfunction clone = new Malfunction(name, id, severity, probability, emergencyWorkTimeExpected, generalWorkTimeExpected, EVAWorkTimeExpected,
				systems, resourceEffects, lifeSupportEffects, medicalComplaints);

		String id_string = INCIDENT_NUM + id;

		if (emergencyWorkTimeExpected > 0D) {
			LogConsolidated.log(logger, Level.WARNING, 0, sourceName,
					name + id_string + " - an Emergency repair work order was requested.", null);
		}

		if (this.EVAWorkTimeExpected > 0) {
			LogConsolidated.log(logger, Level.WARNING, 0, sourceName,
					name + id_string + " - an EVA repair work order was put in place.", null);
		}

		if (this.generalWorkTimeExpected > 0) {
			LogConsolidated.log(logger, Level.WARNING, 0, sourceName,
					name + id_string + " - a General repair work order was set up.", null);
		}

		return clone;
	}

	/**
	 * Determines the parts that are required to repair this malfunction.
	 * 
	 * @throws Exception if error determining the repair parts.
	 */
	void determineRepairParts() {
		String[] partNames = malfunctionConfig.getRepairPartNamesForMalfunction(name);
		for (String partName : partNames) {
			if (RandomUtil.lessThanRandPercent(malfunctionConfig.getRepairPartProbability(name, partName))) {
				int number = RandomUtil.getRandomRegressionInteger(malfunctionConfig.getRepairPartNumber(name, partName));
				// Part part = (Part) ItemResource.findItemResource(partName);
				
				int id = ItemResourceUtil.findIDbyItemResourceName(partName);
				// Add tracking demand
//				inv.addItemDemandTotalRequest(id, number);
//				inv.addItemDemand(id, number);
				
				repairParts.put(id, number);
				String id_string = INCIDENT_NUM + incidentNum;
				

				
				LogConsolidated.log(logger, Level.WARNING, 0, sourceName,
						name + id_string + " - the repair requires " + partName + " (quantity: " + number + ").", null);
			}
		}
	}


	/**
	 * Gets the parts required to repair this malfunction.
	 * 
	 * @return map of parts and their number.
	 */
	public Map<Integer, Integer> getRepairParts() {
		return new HashMap<Integer, Integer>(repairParts);
	}

	/**
	 * Repairs the malfunction with a number of a part.
	 * 
	 * @param part   the part.
	 * @param number the number used for repair.
	 */
	public void repairWithParts(Integer id, int number, Inventory inv) {
//		Part part = (Part) ItemResourceUtil.findItemResource(id);
		// if (part == null) throw new IllegalArgumentException("part is null");
		if (repairParts.containsKey(id)) {

			int numberNeeded = repairParts.get(id);
			if (number > numberNeeded)
				throw new IllegalArgumentException(
						"number " + number + " is greater that number of parts needed: " + numberNeeded);
			else {
				numberNeeded -= number;

				//  Add producing solid waste
				double mass = ItemResourceUtil.findItemResource(id).getMassPerItem();
				if (mass > 0)
					Storage.storeAnResource(mass, ResourceUtil.solidWasteID, inv,
							sourceName + "::repairWithParts");

				if (numberNeeded > 0)
					repairParts.put(id, numberNeeded);
				else
					repairParts.remove(id);
			}
		} else
			throw new IllegalArgumentException("Part " + (Part) ItemResourceUtil.findItemResource(id) + " is not needed for repairs.");
	}

	/**
	 * Gets the string value for the object.
	 */
	public String toString() {
		return name;
	}

	public void setTraumatized(String name) {
		mostTraumatized = name;
	}

	public String getTraumatized() {
		return mostTraumatized;
	}

	public int getIncidentNum() {
		return incidentNum;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Malfunction m = (Malfunction) obj;
		return this.incidentNum == m.getIncidentNum()
				&& this.name == m.getName();
	}

	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		int hashCode = (int)(1 + incidentNum);
		hashCode *= (int)(1 + severity);
		hashCode *= (1 + name.hashCode());
		return hashCode;
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public static void initializeInstances() {
		malfunctionConfig = SimulationConfig.instance().getMalfunctionConfiguration();
	}
	
	public void destroy() {
		systems = null;
		resourceEffects = null;
		lifeSupportEffects = null;
		medicalComplaints = null;
		repairParts = null;
		malfunctionConfig = null;
	}
}