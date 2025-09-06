/*
 * Mars Simulation Project
 * Malfunction.java
 * @date 2022-06-14
 * @author Scott Davis
 */
package com.mars_sim.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionMeta.EffortSpec;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.resource.MaintenanceScope;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The Malfunction class represents a malfunction in a vehicle, structure or
 * equipment.
 */
public class Malfunction implements Serializable {

	/**
	 * Description of Repairer's effort
	 */
	public static class Repairer {
		private String worker;
		private boolean active;
		private double workTime;

		private Repairer(String worker, boolean active, double workTime) {
			super();
			this.worker = worker;
			this.active = active;
			this.workTime = workTime;
		}

		public String getWorker() {
			return worker;
		}

		public boolean isActive() {
			return active;
		}

		public double getWorkTime() {
			return workTime;
		}
	}

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
		private Map<String, Double> activeWorkers;
		private Map<String, Double> previousWorkers;

		public RepairWork(double actualEffort, int desiredWorkers) {
			workExpected = actualEffort;
			this.desiredWorkers = desiredWorkers;
			activeWorkers = new HashMap<>();
			previousWorkers = new HashMap<>();

		}

		public boolean isCompleted() {
			return (workCompleted >= workExpected);
		}

		public int getAvailableSlots() {
			return desiredWorkers - activeWorkers.size();
		}

		/**
		 * Worker leaves the repair.
		 * 
		 * @param name
		 * @return If the worker was active
		 */
		public boolean leaveWork(String name) {
			if (activeWorkers.containsKey(name)) {
				Double previous = activeWorkers.remove(name);
				// Don't remember worker who never contributed
				if (previous.doubleValue() > 0D) {
					previousWorkers.put(name, previous);
				}
				return true;
			}
			return false;
		}

		/**
		 * Adds some repair time for a worker.
		 * 
		 * @param repairer
		 * @param time
		 */
		public void addTime(String repairer, double time) {
			double previousTime = 0D;
			if (activeWorkers.containsKey(repairer)) {
				previousTime = activeWorkers.get(repairer);
			}
			else if (previousWorkers.containsKey(repairer)) {
				previousTime = previousWorkers.remove(repairer);
			}

			activeWorkers.put(repairer, previousTime + time);
		}

		/**
		 * Gets the repairers effort. For non-active works add a "*".
		 * 
		 * @return
		 */
		public List<Repairer> getEffort() {
			List<Repairer> result = new ArrayList<>();
			for(Entry<String, Double> entry : activeWorkers.entrySet()) {
				result.add(new Repairer(entry.getKey(), true, entry.getValue()));
			}
			for(Entry<String, Double> entry : previousWorkers.entrySet()) {
				result.add(new Repairer(entry.getKey(), false, entry.getValue()));
			}
			return result;
		}
	}

	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Malfunction.class.getName());

	private static final String INCIDENT_NUM = " - Incident #";
	private static final String REPAIR_REQUIRES = " - Repair requires ";
	private static final String QUANTITY = " x";
	private static final String CLOSE_B = ".";

	// Data members
	private int incidentNum;
	/* Owner of the Malfuncion */
	private MalfunctionManager owner;
	/* Name */
	private String name;
	/* The person who are being the most traumatized by this malfunction. */
	private String mostTraumatized = "None";
	/* The definition instance of this malfunction. */
	private MalfunctionMeta definition;
	/* The map of repair part id and part quantity for this malfunction. */
	private Map<MaintenanceScope, Integer> repairParts;
	/** Repair work required */
	private EnumMap<MalfunctionRepairWork, RepairWork> work = new EnumMap<>(MalfunctionRepairWork.class);


	/**
	 * Creates a new Malfunction instance based on a meta definition
	 *
	 * @param instigator The owner of this Malfunction
	 * @param incident the incident id
	 * @param definition the MalfunctionMeta instance
	 * @param supportsInside Does the entity supports inside repairs
	 */
	Malfunction(MalfunctionManager instigator, int incident, MalfunctionMeta definition, boolean supportsInside) {
		repairParts = new HashMap<>();
		owner = instigator;
		incidentNum = incident;
		this.definition = definition;
		StringBuilder builder = new StringBuilder().append(definition.getName()).append(INCIDENT_NUM)
				.append(incidentNum);
		this.name = builder.toString();

		Map<MalfunctionRepairWork, EffortSpec> workEffort = definition.getRepairEffort();
		for (Entry<MalfunctionRepairWork, EffortSpec> effort : workEffort.entrySet()) {
			MalfunctionRepairWork type = effort.getKey();
		
			// If supportsInside is false, skip setting up inside repair
			if (!supportsInside && (type == MalfunctionRepairWork.INSIDE)) {
				continue;
			}
			
			double workTime = effort.getValue().getWorkTime();
			double actualEffort = computeWorkTime(workTime);
			if (actualEffort > (2 * Double.MIN_VALUE)) {
				logger.info(owner.getEntity(), 10_000, name + ". " + type.toString() + " repair triggered. Estimated work time: "
								+ Math.round(actualEffort * 10.0)/10.0 + " millisols.");
				work.put(type, new RepairWork(actualEffort, effort.getValue().getDesiredWorkers()));
			}
		}

		// Determine what parts are needed to repair malfunction
		determineRepairParts();
	}

	/**
	 * Does this malfunction have this particular work type ?
	 * 
	 * @param type
	 * @return
	 */
	public boolean hasWorkType(MalfunctionRepairWork type) {
		Map<MalfunctionRepairWork, EffortSpec> workEffort = definition.getRepairEffort();
		return workEffort.keySet().contains(type);
	}
	
	/**
	 * This find the details of a work type for this malfunction.
	 * 
	 * @param type Requested type
	 * @return the RepairWork instance
	 * @throws IllegalArgumentException If the type is not supported for this Malfunction
	 */
	private RepairWork getWorkType(MalfunctionRepairWork type) {
		RepairWork w = null;
		if (work.containsKey(type)) {
			w = work.get(type);
		}
		else {
			logger.info("'" + getName()
				+ "' does not need " + type.toString() + " repair.");
			return null;
		}
		return w;
	}

	/**
	 * Obtains the name of the chief repairer
	 *
	 * @param type MalfunctionRepairWork type
	 * @return the name of the chief repairer
	 */
	public String getChiefRepairer(MalfunctionRepairWork type) {
		RepairWork w = getWorkType(type);
		if (w != null) {
			return w.chiefRepairer;
		}
		
		return null;
	}

	/**
	 * Sets the name of the deputy repairer of a particular type of repair
	 *
	 * @param type Type of work
	 * @param name the name of the deputy repairer
	 */
	public void setDeputyRepairer(MalfunctionRepairWork type, String name) {
		RepairWork w = getWorkType(type);
		if (w != null) {
			w.deputyRepairer = name;
		}
	}

	/**
	 * Obtains the name of the deputy repairer
	 *
	 * @param type Type of work
	 * @return the name of the deputy repairer
	 */
	public String getDeputyRepairer(MalfunctionRepairWork type) {
		RepairWork w = getWorkType(type);
		if (w != null) {
			return w.deputyRepairer;
		}
		
		return null;
	}

	/**
	 * Checks if all repairer slots are filled
	 *
	 * @param type Type of work
	 * @return number of empty repairer slots
	 */
	public int numRepairerSlotsEmpty(MalfunctionRepairWork type) {
		RepairWork rw = work.get(type);
		return rw.getAvailableSlots();
	}

	/**
	 * Gets the number of repairers desired
	 *
	 * @param type Type of work
	 * @return the # of desired repairs
	 */
	public int getDesiredRepairers(MalfunctionRepairWork type) {
		RepairWork rw = work.get(type);
		if (rw != null)
			return rw.desiredWorkers;
		return 0;
	}

	/**
	 * Sets the name of the chief repairer of a particular type of repair
	 *
	 * @param type Type of work
	 * @param name the name of the chief repairer
	 */
	public void setChiefRepairer(MalfunctionRepairWork type, String name) {
		RepairWork w = getWorkType(type);
		if (w != null) {
			w.chiefRepairer = name;
		}
	}

	/**
	 * Computes the expected work time on a gaussian curve
	 *
	 * @param timeValue
	 * @return the work time
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
			t = RandomUtil.getGaussianPositive(time, time/2D);
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
		return name;
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
	 * Is the work repair done ?
	 * 
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
	 * Returns the probability of failure of the malfunction.
	 *
	 * @return probability in %
	 */
	public double getProbability() {
		return definition.getProbability();
	}

	/**
	 * Returns the MalfunctionMeta definition of the malfunction.
	 *
	 * @return MalfunctionMeta
	 */
	public MalfunctionMeta getMalfunctionMeta() {
		return definition;
	}

	/**
	 * Sets the probability of failure.
	 *
	 * @param p
	 */
	public void setProbability(double p) {
		definition.setProbability(p);
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
			logger.warning("Malfunction " + name + " not expecting work " + workType);
		}
		else {
			// if this malfunction has repair work
			if (!w.isCompleted()) {
				if ((w.workCompleted == 0) && (time > 0D)) {
					logger.info(owner.getEntity(), 10_000, name + " - " + workType.getName() + " repair work initiated by " + repairer + ".");
				}

				// How much can be used
				double workLeft = w.workExpected - w.workCompleted;
				if (workLeft > time) {
					// Use all time
					remaining = 0;
					w.workCompleted += time;
				}
				else {
					// Some left
					remaining = time - workLeft;
					w.workCompleted = w.workExpected;
					time = workLeft;
				}

				// Add effort from the worker
				w.addTime(repairer, time);

				// Check for completed
				if (w.isCompleted() && isFixed()) {
					owner.removeFixedMalfunction(this);
				}
			}
		}

		return remaining;
	}

	/**
	 * Leaves the position as a repair. THis worker is not longer contributing.
	 * 
	 * @param required
	 * @param name
	 */
	public void leaveWork(MalfunctionRepairWork required, String name) {
		RepairWork w = getWorkType(required);
		if (w != null && w.leaveWork(name) && !w.isCompleted()) {
			logger.info(owner.getEntity(), 10_000, "Repairer " + name + " leaving the scene.");
		}
	}

	/**
	 * Gets the name of the person who spent most time repairing this malfunction.
	 *
	 * @return the name of the person
	 */
	public String getMostProductiveRepairer() {
		// Collate all values
		Map<String, Double> totalWork = work.values().stream()
			.flatMap(w -> w.activeWorkers.entrySet().stream())
			.collect(Collectors.toMap(Map.Entry::getKey,
								  Map.Entry::getValue,
								  Double::sum								  
								  ));

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
	public List<Repairer> getRepairersEffort(MalfunctionRepairWork type) {
		RepairWork w = getWorkType(type);
		if (w != null) {
			return w.getEffort();
		}
		return new ArrayList<>();
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
			
		for (MaintenanceScope ms : pickOneScopeHighFatigue()) {

			double prob = ms.getProbability();
			int chance = ms.getMaxNumber();
			for (int i=0; i<chance; i++) {
				int num = 0;
				if (RandomUtil.lessThanRandPercent(prob)) {
					if (repairParts.containsKey(ms)) {
						num = repairParts.get(ms) + 1;
						repairParts.put(ms, num);
					}
					else {
						repairParts.put(ms, ++num);
					}
					Part part = ms.getPart();
					logger.warning(name + REPAIR_REQUIRES + part.getName()
						+ QUANTITY + num + CLOSE_B);
				}
			}
		}
	}

	/**
	 * Randomly picks one of the scopes that has a proportionally high fatigue
	 * 
	 * @return
	 */
	private List<MaintenanceScope> pickOneScopeHighFatigue() {
		double highestFatigue = 0;
		List<MaintenanceScope> selectedScope = Collections.emptyList();
//		May add back for debugging: logger.info("Malfunction: " + definition.getSystems());
		for (String scope: definition.getSystems()) {
			double sumFatigue = 0;
			List<MaintenanceScope> list = owner.getMaintenanceScopeList(scope);
			for (MaintenanceScope ms: list) {
				sumFatigue += ms.getFatigue();
			}
			if (sumFatigue > highestFatigue) {
				selectedScope = list;
				highestFatigue = sumFatigue;
			}
		}
		return selectedScope;
	}
	
	/**
	 * Randomly picks one of the scopes.
	 * 
	 * @return
	 */
	private String pickOneScope() {
		Set<String> scopes = definition.getSystems();

		int rand = RandomUtil.getRandomInt(scopes.size() - 1);
		return scopes.stream()
				     .skip(rand)
				     .findFirst()
				     .orElse(null);
	}

	/**
	 * Gets the parts required to repair this malfunction.
	 *
	 * @return map of parts and their number.
	 */
	public Map<MaintenanceScope, Integer> getRepairParts() {
		return Collections.unmodifiableMap(repairParts);
	}

	/**
	 * Repairs the malfunction with a number of a part.
	 *
	 * @param ms the scope.
	 * @param number the number used for repair.
	 * @param inv the inventory
	 */
	public void repairWithParts(MaintenanceScope ms, int number, EquipmentOwner containerUnit) {
		if (!repairParts.containsKey(ms)) {
			return;
		}

		int numberNeeded = repairParts.get(ms);
		if (number > numberNeeded) {
			return;
		}
		
		numberNeeded -= number;

		//  Add producing solid waste
		double mass = ms.getPart().getMassPerItem();
		if (mass > 0) {
			containerUnit.storeAmountResource(ResourceUtil.SOLID_WASTE_ID, mass);
		}

		if (numberNeeded > 0) {
			repairParts.put(ms, numberNeeded);
		}
		else
			repairParts.remove(ms);
	}

	/**
	 * Get the source of the malfunction.
	 */
	public Malfunctionable getSource() {
		return owner.getEntity();
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

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Malfunction m = (Malfunction) obj;
		return this.incidentNum == m.incidentNum;
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		return (1 + incidentNum) % 32;
	}


	public void destroy() {
		repairParts = null;
	}
}
