/**
 * Mars Simulation Project
 * Malfunction.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */

package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.malfunction.MalfunctionMeta.EffortSpec;
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

	/**
	 * Records the effort spent of fixing one phase of the malfunction.
	 */
	private static final class RepairWork implements Serializable {
	
		private static final long serialVersionUID = 1L;
		
		String chiefRepairer;
		String deputyRepairer;
		double workExpected;
		double workCompleted = 0D;
		int desiredWorkers;
		
		/** The map for storing how much work time the repairers spent in fixing this malfunction. */
		Map<String, Double> repairersWorkTime;

		public int leavers = 0;
		
		public RepairWork(double actualEffort, int desiredWorkers) {
			workExpected = actualEffort;
			this.desiredWorkers = desiredWorkers;
			repairersWorkTime = new HashMap<>();
		}

		public boolean isCompleted() {
			return (workCompleted >= workExpected);
		}

		public int getAvailableSlots() {
			return Math.max(desiredWorkers - (repairersWorkTime.size() + leavers),
							0);
		}
	}
	
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Malfunction.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	private static final String INCIDENT_NUM = " - Incident #";
	private static final String REPAIR_REQUIRES = " - Repair requires ";
	private static final String QUANTITY = "(x";
	private static final String CLOSE_B = ").";
	
	// Data members
	private int incidentNum;
	
	/* The person who are being the most traumatized by this malfunction */
	private String mostTraumatized = "None";

	private Map<Integer, Integer> repairParts;


	/** Repair work required */
	private EnumMap<MalfunctionRepairWork, RepairWork> work = new EnumMap<>(MalfunctionRepairWork.class);

	private MalfunctionMeta definition;

	/**
	 * Create a new Malfunction instance based on a meta definition
	 * @param choosenMalfunction
	 */
	Malfunction(int incident, MalfunctionMeta defintion) {
		repairParts = new HashMap<>();

		incidentNum = incident;
		definition = defintion;

		String idString = getUniqueIdentifer();

		Map<MalfunctionRepairWork, EffortSpec> workEffort = defintion.getRepairEffort();
		for (Entry<MalfunctionRepairWork, EffortSpec> effort : workEffort.entrySet()) {
			double workTime = effort.getValue().getWorkTime();
			double actualEffort = computeWorkTime(workTime);
			if (actualEffort > (2 * Double.MIN_VALUE)) {
				LogConsolidated.log(logger, Level.INFO, 10_000, sourceName,
						idString + " - Estimated " + effort.getKey() + " work time: "
								+ Math.round(actualEffort*10.0)/10.0);	
				work.put(effort.getKey(), new RepairWork(actualEffort, effort.getValue().getDesiredWorkers()));				
			}
		}

		// What is need to repair malfunction
		determineRepairParts();
	}

	/**
	 * This find the details of a work type for this malfunction.
	 * @param type Requested type
	 * @return
	 * @throws IllegalArgumentException If the type is not supported for this Malfunction
	 */
	private RepairWork getWorkType(MalfunctionRepairWork type) {
		RepairWork w = work.get(type);
		if (w == null) {
			throw new IllegalArgumentException("Malfunction " + getUniqueIdentifer()
							+ " does not need " + type);
		}
		return w;
	}
	
	/**
	 * Obtains the name of the chief repairer
	 * 
	 * @param type 1: general repair; 2: emergency repair; 3: EVA repair
	 * @return
	 */
	public String getChiefRepairer(MalfunctionRepairWork type) {
		return getWorkType(type).chiefRepairer;
	}
	
	/**
	 * Sets the name of the deputy repairer of a particular type of repair
	 * 
	 * @param type Type of work
	 * @param name
	 */
	public void setDeputyRepairer(MalfunctionRepairWork type, String name) {
		getWorkType(type).deputyRepairer = name;
	}
	
	/**
	 * Obtains the name of the deputy repairer
	 * 
	 * @param type
	 * @return
	 */
	public String getDeputyRepairer(MalfunctionRepairWork type) {
		return getWorkType(type).deputyRepairer;
	}
	
	/**
	 * Checks if all repairer slots are filled
	 * 
	 * @return
	 */
	public int numRepairerSlotsEmpty(MalfunctionRepairWork type) {
		RepairWork rw = work.get(type);
		return rw.getAvailableSlots();
	}
	
	/**
	 * How many repairers are desired
	 * 
	 * @return
	 */
	public int getDesiredRepairers(MalfunctionRepairWork type) {
		RepairWork rw = work.get(type);
		return rw.desiredWorkers;
	}
	
	
	/**
	 * Sets the name of the chief repairer of a particular type of repair
	 * 
	 * @param type
	 * @param name
	 */
	public void setChiefRepairer(MalfunctionRepairWork type, String name) {
		getWorkType(type).chiefRepairer = name;
	}

	/**
	 * Computes the expected work time on a gaussian curve
	 * 
	 * @param time
	 * @return
	 */
	private double computeWorkTime(Double timeValue) {
		if (timeValue == null) {
			return 0;
		}
		double time = Math.abs(timeValue.doubleValue());
		
		if (time < 2 * Double.MIN_VALUE)
			return 0;

		double t = 0;
		
		do {
			t = time + RandomUtil.getGaussianDouble() * time/4D;
			if (t < 0)
				t = -t;
		}
		// Limit the expected work time to no more than 5x the average work time.
		while (t == 0 || t > 5 * time);
		
		if (t > 5 * time)
			t = 5 * time;
  
		return t;
	}
	
	/**
	 * Returns the name of the malfunction.
	 * 
	 * @return name of the malfunction
	 */
	public String getName() {
		return definition.getName();
	}

	/**
	 * Returns true if malfunction is fixed.
	 * 
	 * @return true if malfunction is fixed
	 */
	public boolean isFixed() {
		boolean fixed = true;
		for (RepairWork rw : work.values()) {
			fixed = fixed && rw.isCompleted();
		}
		return fixed;
	}

	/**
	 * Does this malfunction require Repair ?
	 * @param type Type of work

	 * @return true if it does
	 */
	public boolean isWorkNeeded(MalfunctionRepairWork type) {
		return work.containsKey(type);
	}

	/**
	 * Is the work repair done ?
	 * @param type Type of work
	 * @return true if work type repair is done
	 */
	public boolean isWorkDone(MalfunctionRepairWork type) {
		RepairWork w = work.get(type);
		return (w == null) || w.isCompleted();
	}
	
	/**
	 * Returns the total percentage fixed 
	 * 
	 * @return the percent
	 */
	public double getPercentageFixed() {
		double totalRequiredWork = 0D;
		double totalCompletedWork = 0D;
		for (RepairWork rw : work.values()) {
			totalRequiredWork += rw.workExpected;
			totalCompletedWork += rw.workCompleted;
		}
		
		int percentComplete = 0;
		if (totalRequiredWork > 0D)
			percentComplete = (int) (100D * (totalCompletedWork / totalRequiredWork));
		return percentComplete;
	}
	
	/**
	 * Returns the severity level of the malfunction.
	 * 
	 * @return severity of malfunction (1 - 100)
	 */
	public int getSeverity() {
		return definition.getSeverity();
	}

	/**
	 * Returns the probability of failure of the malfunction
	 * 
	 * @return probability in %
	 */
	public double getProbability() {
		return definition.getProbability();
	}

	/**
	 * Returns the work time required to repair the malfunction.
	 * 
	 * @return work time (in millisols)
	 */
	public double getWorkTime(MalfunctionRepairWork type) {
		RepairWork rw = work.get(type);
		return (rw != null) ? rw.workExpected : 0D;
	}

	/**
	 * Returns the completed work time required to repair the malfunction.
	 * 
	 * @param workType Type of work
	 * @return work time (in millisols)
	 */
	public double getCompletedWorkTime(MalfunctionRepairWork workType) {
		RepairWork rw = work.get(workType);
		return (rw != null) ? rw.workCompleted : 0D;
	}
	
	
	/**
	 * Adds EVA work time to the malfunction.
	 * 
	 * @param time EVA work time (in millisols)
	 * @return remaining work time not used (in millisols)
	 */
	public double addWorkTime(MalfunctionRepairWork workType, double time, String repairer) {
		RepairWork w = work.get(workType);
		double remaining = time;
		if (w == null) {
			logger.warning("Malfunction " + getUniqueIdentifer() + " not expecting work " + workType);
		}
		else {
			// if this malfunction has repair work
			double t0 = w.workExpected;
			double t = w.workCompleted;
			if (t0 > 0) {
				if (t == 0) {
					LogConsolidated.log(logger, Level.INFO, 10_000, sourceName,
							getUniqueIdentifer() + " - " + workType + " repair work initiated by " + repairer + ".");
				}
				
				t += time;
				
				// Add randomness to the expected work time
				t0 = t0 + (t0 - t) * (RandomUtil.getRandomDouble(.01) - RandomUtil.getRandomDouble(.01));
				
				if (t0 > 0)
					w.workExpected = t0;
				else
					t0 = w.workExpected;
			
				w.workCompleted = t;
				
				// Add effort from the worker
				Map<String, Double> repairersWorkTime = w.repairersWorkTime;
				if (repairersWorkTime.containsKey(repairer)) {
					repairersWorkTime.put(repairer, repairersWorkTime.get(repairer) + time);
				} else {
					repairersWorkTime.put(repairer, time);
				}
				
				if (t > t0) {
					remaining = t - t0;
				}
			}
		}
		
		return remaining;
	}

	/**
	 * A worker is not longer contributing
	 * @param required
	 * @param name
	 */
	public void leaveWork(MalfunctionRepairWork required, String name) {
		RepairWork w = getWorkType(required);
		if (w.repairersWorkTime.containsKey(name)) {
			w.leavers++;
		
			if (!w.isCompleted()) {
				LogConsolidated.log(logger, Level.INFO, 0, sourceName,
					getUniqueIdentifer() + " repairer left:" + name, null);
			}
		}
	}
	
	/**
	 * Gets the name of the person who spent most time repairing this malfunction
	 * 
	 * @return the name of the person
	 */
	public String getMostProductiveRepairer() {
		// Collate all values
		Map<String, Double> totalWork = work.values().stream().flatMap(w -> w.repairersWorkTime.entrySet().stream())
			.collect(Collectors.toMap(Map.Entry::getKey,
								  Map.Entry::getValue,
								  (v1, v2) -> Double.sum(v1, v2)));
				
		// Find the max
		Map.Entry<String, Double> maxEntry = totalWork.entrySet()
				.stream()
				.max(Map.Entry.comparingByValue())
				.orElse(null); // .get()
    	if (maxEntry != null && maxEntry.getKey() != null)
    		return maxEntry.getKey();
    	else
    		return null;
	}

	/**
	 * How has worked on the repair
	 * @return 
	 * @return Work effort by repairer
	 */
	public Map<String, Double> getRepairersEffort(MalfunctionRepairWork type) {
		RepairWork w = getWorkType(type);
		return w.repairersWorkTime;
	}
	
	/**
	 * Gets the resource effects of the malfunction.
	 * 
	 * @return resource effects as name-value pairs in Map
	 */
	public Map<Integer, Double> getResourceEffects() {
		return definition.getResourceEffects();
	}

	/**
	 * Gets the life support effects of the malfunction.
	 * 
	 * @return life support effects as name-value pairs in Map
	 */
	public Map<String, Double> getLifeSupportEffects() {
		return definition.getLifeSupportEffects();
	}

	/**
	 * Gets the medical complaints produced by this malfunction and their
	 * probability of occurrence.
	 * 
	 * @return medical complaints as name-value pairs in Map
	 */
	public Map<ComplaintType, Double> getMedicalComplaints() {
		return definition.getMedicalComplaints();
	}

	/**
	 * Determines the parts that are required to repair this malfunction.
	 * 
	 * @throws Exception if error determining the repair parts.
	 */
	private void determineRepairParts() {
		for (RepairPart part : definition.getParts()) {
			if (RandomUtil.lessThanRandPercent(part.getProbability())) {				
				int id = ItemResourceUtil.findIDbyItemResourceName(part.getName());				
				repairParts.put(id, part.getNumber());
					
				LogConsolidated.log(logger, Level.WARNING, 0, sourceName,
						getUniqueIdentifer() + REPAIR_REQUIRES + part.getName()
						+ QUANTITY + part.getNumber() + CLOSE_B, null);
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
		return getUniqueIdentifer();
	}
	
	private String getUniqueIdentifer() {
		StringBuilder builder = new StringBuilder().append(definition.getName()).append(INCIDENT_NUM)
				.append(incidentNum);
		return builder.toString();
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
				&& this.getName().equals(m.getName());
	}

	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		int hashCode = (1 + incidentNum);
		hashCode *= (1 + definition.getSeverity());
		return hashCode;
	}
	
	
	public void destroy() {
		repairParts = null;
	}

}
