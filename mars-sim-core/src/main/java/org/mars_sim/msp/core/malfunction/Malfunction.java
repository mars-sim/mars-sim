/**
 * Mars Simulation Project
 * Malfunction.java
 * @version 3.1.0 2017-09-05
 * @author Scott Davis
 */

package org.mars_sim.msp.core.malfunction;

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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private double generalWorkTime = 0;
	private double generalWorkTimeCompleted = 0;
	private double emergencyWorkTime = 0;
	private double emergencyWorkTimeCompleted = 0;
	private double EVAWorkTime = 0;
	private double EVAWorkTimeCompleted = 0;

	private String name;

	/* The person who are being the most traumatized by this malfunction */
	private String mostTraumatized = "None";

	private Collection<String> systems;
	private Map<Integer, Double> resourceEffects;
	private Map<String, Double> lifeSupportEffects;
	private Map<ComplaintType, Double> medicalComplaints;
	private Map<Integer, Integer> repairParts;

	/*
	 * The map for storing how much worktime the repairers spent in fixing this
	 * malfunction
	 */
	private Map<String, Double> repairersWorkTime;

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
		this.emergencyWorkTime = computeWorkTime(emergencyWorkTime);
		this.generalWorkTime = computeWorkTime(workTime);
		this.EVAWorkTime = computeWorkTime(EVAWorkTime);
		this.systems = entities;
		this.resourceEffects = resourceEffects;
		this.lifeSupportEffects = lifeSupportEffects;
		this.medicalComplaints = medicalComplaints;

		repairParts = new HashMap<>();
		repairersWorkTime = new HashMap<>();

	}

	public double computeWorkTime(double time) {
		if (time < 1)
			return 0;
		if (RandomUtil.getRandomInt(3) == 0)
			return time/8D + RandomUtil.getRandomDouble(time *.875);
		else
			// Set it to be at least a quarter of its value to its full value
			return RandomUtil.getRandomRegressionInteger((int)(time/4D), (int)time);
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
		if (generalWorkTime > 0 && generalWorkTimeCompleted < generalWorkTime)
			return false;
		if (emergencyWorkTime > 0 && emergencyWorkTimeCompleted < emergencyWorkTime)
			return false;
		if (EVAWorkTime > 0 && EVAWorkTimeCompleted < EVAWorkTime)
			return false;

		return true;
	}

	/**
	 * Is the general repair done ?
	 * 
	 * @return true if general repair is done
	 */
	public boolean isGeneralRepairDone() {
		if (generalWorkTime > 0 && generalWorkTimeCompleted < generalWorkTime)
			return false;
		return true;
	}

	/**
	 * Is the emergency repair done ?
	 * 
	 * @return true if emergency repair is done
	 */
	public boolean isEmergencyRepairDone() {
		if (emergencyWorkTime > 0 && emergencyWorkTimeCompleted < emergencyWorkTime)
			return false;
		return true;
	}

	/**
	 * Is the EVA repair done ?
	 * 
	 * @return true if EVA repair is done
	 */
	public boolean isEVARepairDone() {
		if (EVAWorkTime > 0 && EVAWorkTimeCompleted < EVAWorkTime)
			return false;
		return true;
	}
	
	/**
	 * Returns the total percentage fixed 
	 * 
	 * @return the percent
	 */
	public double getPercentageFixed() {
		double totalRequiredWork = emergencyWorkTime + generalWorkTime + EVAWorkTime;
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
		return generalWorkTime;
	}

	/**
	 * Does this malfunction require General Repair ?
	 * 
	 * @return true if it does
	 */
	public boolean needGeneralRepair() {
		return generalWorkTime > 0;
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
		if (generalWorkTime > 0) {
			if (generalWorkTimeCompleted == 0) {
				String id_string = INCIDENT_NUM + incidentNum;
				LogConsolidated.log(Level.INFO, 0, sourceName,
						name + id_string + " - General repair work initiated by " + repairer + ".");
			}
			
			generalWorkTimeCompleted += time;
			
			if (repairersWorkTime.containsKey(repairer)) {
				repairersWorkTime.put(repairer, repairersWorkTime.get(repairer) + time);
			} else {
				repairersWorkTime.put(repairer, time);
			}
			
			if (generalWorkTimeCompleted > generalWorkTime) {
				double remaining = generalWorkTimeCompleted - generalWorkTime;
				generalWorkTimeCompleted = generalWorkTime;
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
		return emergencyWorkTime;
	}

	/**
	 * Does this malfunction require Emergency Repair ?
	 * 
	 * @return true if it does
	 */
	public boolean needEmergencyRepair() {
		return emergencyWorkTime > 0;
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
		if (emergencyWorkTime > 0) {
			if (emergencyWorkTimeCompleted == 0) {
				String id_string = INCIDENT_NUM + incidentNum;
				LogConsolidated.log(Level.INFO, 0, sourceName,
						name + id_string + " - Emergency repair work initiated by " + repairer + ".");
			}
			
			emergencyWorkTimeCompleted += time;
			
			if (repairersWorkTime.containsKey(repairer)) {
				repairersWorkTime.put(repairer, repairersWorkTime.get(repairer) + time);
			} else {
				repairersWorkTime.put(repairer, time);
			}
			
			if (emergencyWorkTimeCompleted > emergencyWorkTime) {
				double remaining = emergencyWorkTimeCompleted - emergencyWorkTime;
				emergencyWorkTimeCompleted = emergencyWorkTime;
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
		return EVAWorkTime;
	}

	/**
	 * Does this malfunction require EVA Repair ?
	 * 
	 * @return true if it does
	 */
	public boolean needEVARepair() {
		return EVAWorkTime > 0;
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
		// if this malfunction has EVA repair work
		if (EVAWorkTime > 0) {
			if (EVAWorkTimeCompleted == 0) {
				String id_string = INCIDENT_NUM + incidentNum;
				LogConsolidated.log(Level.INFO, 0, sourceName,
						name + id_string + " - EVA repair work initiated by " + repairer + ".");
			}
			
			EVAWorkTimeCompleted += time;
			
			if (repairersWorkTime.containsKey(repairer)) {
				repairersWorkTime.put(repairer, repairersWorkTime.get(repairer) + time);
			} else {
				repairersWorkTime.put(repairer, time);
			}
			
			if (EVAWorkTimeCompleted > EVAWorkTime) {
				double remaining = EVAWorkTimeCompleted - EVAWorkTime;
				EVAWorkTimeCompleted = EVAWorkTime;
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
	public String getChiefRepairer() {
		Map.Entry<String, Double> maxEntry = repairersWorkTime.entrySet().stream().max(Map.Entry.comparingByValue())
				.get(); // may get null ?
    	if (maxEntry.getKey() != null)
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
		boolean result = false;

		if ((systems.size() > 0) && (scopes.size() > 0)) {
			for (String s : systems) {
				for (String u : scopes) {
					if (s.equalsIgnoreCase(u))
						result = true;
				}
			}
		}

		return result;
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
		Malfunction clone = new Malfunction(name, id, severity, probability, emergencyWorkTime, generalWorkTime, EVAWorkTime,
				systems, resourceEffects, lifeSupportEffects, medicalComplaints);

		String id_string = INCIDENT_NUM + id;

		if (emergencyWorkTime > 0D) {
			LogConsolidated.log(logger, Level.WARNING, 0, sourceName,
					name + id_string + " - an Emergency repair work order was requested.", null);
		}

		if (this.EVAWorkTime > 0) {
			LogConsolidated.log(logger, Level.WARNING, 0, sourceName,
					name + id_string + " - an EVA repair work order was put in place.", null);
		}

		if (this.generalWorkTime > 0) {
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
		// MalfunctionConfig config =
		// SimulationConfig.instance().getMalfunctionConfiguration();
		String[] partNames = malfunctionConfig.getRepairPartNamesForMalfunction(name);
		for (String partName : partNames) {
			if (RandomUtil.lessThanRandPercent(malfunctionConfig.getRepairPartProbability(name, partName))) {
				int number = RandomUtil.getRandomRegressionInteger(malfunctionConfig.getRepairPartNumber(name, partName));
				// Part part = (Part) ItemResource.findItemResource(partName);
				repairParts.put(ItemResourceUtil.findIDbyItemResourceName(partName), number);
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
		Part part = (Part) ItemResourceUtil.findItemResource(id);
		// if (part == null) throw new IllegalArgumentException("part is null");
		if (repairParts.containsKey(id)) {

			int numberNeeded = repairParts.get(id);
			if (number > numberNeeded)
				throw new IllegalArgumentException(
						"number " + number + " is greater that number of parts needed: " + numberNeeded);
			else {
				numberNeeded -= number;

				//  Add produceSolidWaste()
				if (part.getMassPerItem() > 0)
					Storage.storeAnResource(part.getMassPerItem(), ResourceUtil.solidWasteID, inv,
							sourceName + "::repairWithParts");

				if (numberNeeded > 0)
					repairParts.put(id, numberNeeded);
				else
					repairParts.remove(id);
			}
		} else
			throw new IllegalArgumentException("Part " + part + " is not needed for repairs.");
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

	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public static void setInstances() {
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