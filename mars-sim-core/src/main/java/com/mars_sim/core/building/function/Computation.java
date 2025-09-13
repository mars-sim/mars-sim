/*
 * Mars Simulation Project
 * Computation.java
 * @date 2025-08-07
 * @author Manny Kung
 */
package com.mars_sim.core.building.function;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.config.FunctionSpec;
import com.mars_sim.core.building.config.SourceSpec;
import com.mars_sim.core.building.config.GenerationSpec;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;

/**
 * The Computation class is a building function for generating computational power.
 */
public class Computation extends Function {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	// May add back private static final SimLogger logger = SimLogger.getLogger(Computation.class.getName());
	
	// Configuration properties
	public static final double ENTROPY_FACTOR = .001;
	
	/** 
	 * The average overall efficiency of power usage. e.g. 30% power is for computation 
	 * and data processing. 70% of power generates for cooling and ventilation.
	 */
	private static final double AVERAGE_POWER_EFFICIENCY = .3;
	
	/**
	 * The power demand fraction for each non-load CU [in kW/CU] out of the full load power demand. 
	 */
	private static final double NON_LOAD_POWER_USAGE = .15;
	
	/**
	 * The percent of cooling power load for each non-load CU [in kW/CU]. 
	 */
	private static final double COOLING_PERCENT = 20;
	
	/**
	 * The fraction of cooling demand to be dissipated as heat [kW]. 
	 */
	private static final double WASTE_HEAT_FRACTION = .3;
	
	private static final String COMPUTING_UNIT = "computing-unit";
	private static final String POWER_DEMAND = "power-demand";

	/** The amount of entropy in the system. */
	private final double maxEntropy;
	/** The highest possible available amount of computing resources [in CUs]. */
	private final double peakCU;
	/** The initial power load in kW for each running CU [in kW/CU]. */
	private final double initialPowerDemand;
	
	/** The previous msol. */
	private int previousMSol;
	/** The instant cooling load in the system. */
	private double instantCoolingLoad;
	/** The amount of heat generated in the system. */
	private double instantHeatGen;
	/** The amount of entropy in the system. */
	private double entropy;
	/** The amount of computing resources capacity currently freely available [in CUs]. */
	private double freeCU;
	/** 
	 * The current overall efficiency of power usage for data center. 
	 * Notes: Use AVERAGE_POWER_EFFICIENCY as the starting figure. 
	 * As improvements are made, this overall power efficiency will go higher. 
	 */
	private double powerEfficiency;
	/** The power load in kW for each running CU [in kW/CU]. */
	private double powerDemand;
	/** The power load in kW needed for cooling each running CU [in kW/CU]. */
	private double coolingDemand;
	/** The combined power demand for each running CU [in kW/CU]. */
	private double combinedLoadkW;
	/** The power demand for each non-load CU [in kW/CU] - Assume 10% of full load. */
	private double nonLoadkW;
	
	/** The schedule demand [in CUs] for each integer msol. */
	private Map<Integer, Double> msolDemand;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @param spec Specification of the Computing Function
	 * @throws BuildingException if error in constructing function.
	 */
	public Computation(Building bldg, FunctionSpec spec) {
		// Call Function constructor.
		super(FunctionType.COMPUTATION, spec, bldg);
		
		peakCU = spec.getDoubleProperty(COMPUTING_UNIT);
		
		maxEntropy = peakCU;
		
		freeCU = peakCU; 
		
		powerEfficiency = AVERAGE_POWER_EFFICIENCY;
		
		powerDemand = spec.getDoubleProperty(POWER_DEMAND) * powerEfficiency / AVERAGE_POWER_EFFICIENCY;
		
		initialPowerDemand = powerDemand;
		
		coolingDemand = powerDemand * COOLING_PERCENT / 100;	
	
		combinedLoadkW = coolingDemand + powerDemand;
		// Assume 15% of full load
		nonLoadkW = NON_LOAD_POWER_USAGE * combinedLoadkW;
		
		msolDemand = new HashMap<>();
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param type the building type.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String type, boolean newBuilding, Settlement settlement) {

		double demand = settlement.getPowerGrid().getRequiredPower();

		double supply = 0D;
		boolean removedBuilding = false;
		for (Building building : settlement.getBuildingManager().getComNodes()) {
			if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
				removedBuilding = true;
			} else {
				Computation com = building.getComputation();
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
				supply += com.getCurrentCU() * wearModifier;
			}
		}

		double existingPowerValue = demand / (supply + 1D);

		var spec = buildingConfig.getFunctionSpec(type, FunctionType.THERMAL_GENERATION);
		if (spec instanceof GenerationSpec ss) {
			double powerSupply = ss.getSources().stream()
									.mapToDouble(SourceSpec::getCapacity).sum();

			return powerSupply * existingPowerValue;
		}
		return 0;
	}

	/**
	 * Sets the power efficiency
	 * 
	 * @param value
	 */
	public void setPowerEfficiency(double value) {
		this.powerEfficiency = value;
		// Recompute the power demand and cooling demand
		resetDemands();
	}
	
	/**
	 * Resets the power and cooling demand.
	 * 
	 * @param value
	 */
	public void resetDemands() {
		// Recompute the power demand
		powerDemand = initialPowerDemand * powerEfficiency / AVERAGE_POWER_EFFICIENCY;
		// Recompute the cooling demand
		coolingDemand = powerDemand * COOLING_PERCENT / 100;
		
		combinedLoadkW = coolingDemand + powerDemand;
		// Assume 15% of full load
		nonLoadkW = NON_LOAD_POWER_USAGE * combinedLoadkW;
	}
	
	/**
	 * Gets the computing unit capacity [in CU].
	 * 
	 * @return
	 */
	public double getCurrentCU() {
		return freeCU;
	}

	/**
	 * Gets the peak CU available computing units [in CU].
	 * 
	 * @return
	 */
	public double getPeakCU() {
		return peakCU;
	}
	
	/**
	 * Sets the free CU available computing units [in CU].
	 * 
	 * @param value
	 */
	public void setFreeCU(double value) {
		freeCU = value;
	}
	
	/**
	 * Dumps the excess heat from server equipment.
	 * 
	 * @param heatGenerated
	 */
	public void dumpExcessHeat(double heatGenerated) {
		building.dumpExcessHeat(heatGenerated);
	}
	
	/**
	 * Gets the power demand [in kW].
	 * 
	 * @return
	 */
	public double getPowerDemand() {
		return powerDemand;
	}

	/**
	 * Gets the cooling demand [in kW].
	 * 
	 * @return
	 */
	public double getCoolingDemand() {
		return coolingDemand;
	}
	
	/**
	 * Schedules for a computing task.
	 * 
	 * @param needed the CUs needed per msol
	 * @param beginningMSol the start msol
	 * @param endMSol the end msol
	 * @return
	 */
	public boolean scheduleTask(double needed, int beginningMSol, int endMSol) {
		double existing = 0;
		
		int duration = endMSol - beginningMSol;
		if (duration < 0)
			duration = endMSol + 1000 - beginningMSol;

		// Test to see if the assigned duration has enough resources on each integer msol
		for (int i = 0; i < duration; i++) {
			int msolInt = i + beginningMSol;
			if (msolInt > 999) {
				msolInt = msolInt - 1000;
			}
			if (msolDemand.containsKey(msolInt)) {
				existing = msolDemand.get(msolInt);
			}
			
			double peak105Percent = 1.05 * peakCU;
			// Need to make sure each msol has enough resources
			double newLoad = existing + needed / duration;
			
			double over105 = peak105Percent - newLoad;
			
			double overZero = peakCU - newLoad;
			
			// May allow the load to go above 100%
			if (over105 < 0) {
				/*
				 *  Do NOT delete. For debugging.
				 */  
//				 	logger.info(getBuilding(), 30_000, "2. Over 105%, peakCU: " + Math.round(peakCU * 100.0)/100.0
//				 			+ "  exist: " + Math.round(existing * 1000.0)/1000.0
//				 			+ "  need: " + Math.round(needed * 1000.0)/1000.0
//				 			+ "  delta: " + Math.round(over105 * 1000.0)/1000.0);
//				 
				
				return false;
			} 
			
			else if (overZero < 0) {	
				/*
				 *  Do NOT delete. For debugging.
				 */ 
//				 	logger.info(getBuilding(), 30_000, "2. Over 100%, peakCU: " + Math.round(peakCU * 100.0)/100.0
//				 			+ "  exist: " + Math.round(existing * 1000.0)/1000.0
//				 			+ "  need: " + Math.round(needed * 1000.0)/1000.0
//				 			+ "  delta: " + Math.round(overZero * 1000.0)/1000.0);
				 
				// It is allowed to go beyond within 5%
			}
		}

		// Now the actual scheduling
		for (int i = 0; i < duration; i++) {
			int msolInt = i + beginningMSol;
			if (msolInt > 999) {
				msolInt = msolInt - 1000;
			}
			if (msolDemand.containsKey(msolInt)) {
				existing = msolDemand.get(msolInt);
			}
			msolDemand.put(msolInt, existing + needed / duration);
		}

		return true;
	}
	
	/**
	 * Returns the evaluation score if scheduling for a computing task for a prescribed period of time. 
	 * 
	 * @param needed CU(s) per millisol
	 * @param beginningMSol
	 * @param endMSol
	 * @return
	 */
	public double evaluateScheduleTask(double needed, int beginningMSol, int endMSol) {
		double score = 0;
		double existing = 0;
		
		int duration = endMSol - beginningMSol;
		if (duration < 0)
			duration = endMSol + 1000 - beginningMSol;
		
		// Test to see if the assigned duration has enough resources on each integer msol
		for (int i = 0; i < duration; i++) {
			int msolInt = i + beginningMSol;
			if (msolInt > 999) {
				msolInt = msolInt - 1000;
			}
			if (msolDemand.containsKey(msolInt)) {
				existing = msolDemand.get(msolInt);
			}
			
			double peak105Percent = 1.05 * peakCU;
			// Need to make sure each msol has enough resources
			double newLoad = existing + needed / duration;
			
			double over105 = peak105Percent - newLoad;
			
			double overZero = peakCU - newLoad;
			
			// May allow the load to go above 100%
			if (over105 < 0) {

			/*
			 *  Do NOT delete. For debugging.
			 */  
//			 	logger.info(getBuilding(), 30_000, "1. Over 105%, peakCU: " + Math.round(peakCU * 100.0)/100.0
//			 			+ "  exist: " + Math.round(existing * 1000.0)/1000.0
//			 			+ "  need: " + Math.round(needed * 1000.0)/1000.0
//			 			+ "  delta: " + Math.round(over105 * 1000.0)/1000.0);
			 
				return 0;
			} 
			
			else if (overZero < 0) {
			/*
			 *  Do NOT delete. For debugging.
			 */ 
//			 	logger.info(getBuilding(), 30_000, "1. Over 100%, peakCU: " + Math.round(peakCU * 100.0)/100.0
//			 			+ "  exist: " + Math.round(existing * 1000.0)/1000.0
//						+ "  need: " + Math.round(needed * 1000.0)/1000.0
//			 			+ "  delta: " + Math.round(overZero * 1000.0)/1000.0);
			 
				// It is allowed to go beyond within 5%
			}
				
			score += overZero;
		}
		
		score = score * getEntropyPenalty();
		
//		logger.info(getBuilding(), 30_000, "score: " + score);
				
		return score;
	}
	
	/**
	 * Sets the computing units or resources to a new value and fires the unit event type alert.
	 * 
	 * @param value
	 */
	public void setCU(double value) {
		double cu = Math.round(value * 100_000.0) / 100_000.0;
		if (freeCU != cu) {
			freeCU = cu;
			building.getSettlement().fireUnitUpdate(UnitEventType.CONSUMING_COMPUTING_EVENT);
		}
	}
	
	/**
	 * Clears the CUs demand on previous integer msol.
	 * 
	 * @param msol
	 */
	public void clearOldDemand(int previous, int now) {
		
		// Future: give players the choice to keep the demand log or to clear it

		for (int i = now - 1; i >= previous; i--) {
			// Delete past demand on previous msol
			if (msolDemand.containsKey(i)) {
				msolDemand.remove(i);
			}
		}
	}
	
	/**
	 * Time passing for the building.
	 * 
	 * @param deltaTime amount of time passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
	
			if (pulse.isNewIntMillisol()) {
				
				increaseEntropy(ENTROPY_FACTOR * (1 + pulse.getElapsed() * (peakCU - freeCU) / 50));
	
				double newDemand = 0;
				int msol = pulse.getMarsTime().getMillisolInt();
		
				if (msolDemand.containsKey(msol)) {
					newDemand = msolDemand.get(msol);
				}
				
				// Clear the old load demand in this center
				clearOldDemand(previousMSol, msol);
				// Update previousMSol  
				previousMSol = msol;
				
				if (newDemand > 0) {
					// Updates the CUs
					setCU(peakCU - newDemand); 
				}
				else {
					setCU(peakCU);
				}
				
				// e.g. at 30% eff, if power = 0.3 kW, cooling = 0.7 kW. total power req = 1 kW.
				
				double instantPower = getCombinedPowerLoad();
				
				instantCoolingLoad = instantPower * COOLING_PERCENT / 100;
				
				instantHeatGen = instantCoolingLoad * WASTE_HEAT_FRACTION;
				// Dump the generated heat into the building to raise the room temperature
				dumpExcessHeat(instantHeatGen);
			}
			
			if (pulse.isNewHalfSol()) {
				// Auto optimization
				reduceEntropy(entropy/5);
			}
		}
		return valid;
	}
	
	/**
	 * Returns the heat generated.
	 * 
	 * @return
	 */
	public double getInstantHeatGenerated() {
		return instantHeatGen;
	}
	
	/**
	 * Returns the instant cooling load.
	 * 
	 * @return
	 */
	public double getInstantCoolingLoad() {
		return instantCoolingLoad;
	}
	
	/**
	 * Returns the percent of usage of computing resources.
	 * 
	 * @return
	 */
	public double getUsagePercent() {
		return (peakCU - freeCU)/peakCU * 100.0;
	}	
	
	/**
	 * Gets the minimum entropy (a negative number).
	 * 
	 * @return
	 */
	public double getMinEntropy() {
		return -0.5 * maxEntropy;
	}
	
	/**
	 * Reduces the entropy.
	 * 
	 * @param the suggested value of entropy to be reduced
	 * @return the final value of entropy being reduced
	 */
	public double reduceEntropy(double value) {
		double oldEntropy = entropy;
		double diff = entropy - value;
		
		if (diff < getMinEntropy()) {
			// Note that entropy can become negative
			// This means that the system has been tuned up
			// to perform very well
			diff = getMinEntropy();
			entropy = diff + value;

		}
		else
			entropy -= value;
		
		return oldEntropy - entropy;
	}
	
	/**
	 * Increases the entropy.
	 * 
	 * @param value
	 */
	public void increaseEntropy(double value) {
		entropy += value;
	}
	
	/**
	 * Gets the penalty factor due to entropy.
	 * Note: it's bad if negative
	 * 
	 * @return
	 */
	public double getEntropyPenalty() {
		return 1 - entropy / maxEntropy;
	}
	
	/**
	 * Gets the current entropy.
	 * 
	 * @return
	 */
	public double getEntropy() {
		return entropy;
	}
	
	/**
	 * Gets the entropy per CU in this node.
	 * 
	 * @return
	 */
	public double getEntropyPerCU() {
		return entropy / peakCU;
	}

	/**
	 * Gets the amount of power required, based on the current load.
	 *
	 * @return power (kW) default zero
	 */
	@Override
	public double getCombinedPowerLoad() {
		double loadFraction = (peakCU - freeCU) / peakCU;
		double nonLoadFraction = 1 - loadFraction;
		
		// Note: Should entropy also increase the power required to run the node ?
		// When entropy is negative, it should reduce or save power
		
		return loadFraction * combinedLoadkW + nonLoadFraction * nonLoadkW;
	}
	
	/**
	 * Gets the amount of power usage on both power load and non-load.
	 *
	 * @return power 
	 */
	public double[] getSeparatePowerLoadNonLoad() {
		double loadFraction = (peakCU - freeCU) / peakCU;
		double nonLoadFraction = 1 - loadFraction;
		
		// Note: Should entropy also increase the power required to run the node ?
		// When entropy is negative, it should reduce or save power
		
		return new double[] {loadFraction * combinedLoadkW, nonLoadFraction * nonLoadkW};
	}
	
	@Override
	public void destroy() {
		msolDemand.clear();
		msolDemand = null;
		super.destroy();
	}

}
