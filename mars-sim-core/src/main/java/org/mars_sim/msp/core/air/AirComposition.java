/*
 * Mars Simulation Project
 * AirComposition.java
 * @date 2022-03-06
 * @author Barry Evans
 */
package org.mars_sim.msp.core.air;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * Models the composition of auir within a containment. Holds the specific gas composition and pressure.
 */
public class AirComposition implements Serializable {
	// Note : Gas volumes are additive. If you mix some volumes of oxygen and
	// nitrogen, final volume will equal sum of
	// volumes, also final mass will equal sum of masses.

	/**
     * Details of a specific Gas
     */
    static final public class GasDetails implements Serializable {
    	private double percent;
    	private double partialPressure;
    	private double numMoles;
    	private double mass;
    	private double standardMoles;
    
    	public double getPercent() {
    		return percent;
    	}
    
        public double getPartialPressure() {
            return partialPressure;
        }
    
        public double getMass() {
            return mass;
        }
    }

    private static double o2Consumed;
	private static double cO2Expelled;
	private static double moistureExpelled;

	private double fixedVolume; // [in liter]; Note: 1 Cubic Meter = 1,000 Liters
	private double totalPressure; // in atm
	private double totalMass; // in kg

	private Map<Integer, AirComposition.GasDetails> gases = new HashMap<>();
    /**
     * The % of air composition used by US Skylab Hab Modules. 5 psi or 340 mb is
     * the overall pressure rating.
     */
    // see http://www.collectspace.com/ubb/Forum29/HTML/001309.html
    // The partial pressures of each gas are in atm
    private static final double CO2_PARTIAL_PRESSURE = 0.5 / CompositionOfAir.MB_PER_ATM;
    private static final double ARGON_PARTIAL_PRESSURE = 0.1 / CompositionOfAir.MB_PER_ATM;
    private static final double N2_PARTIAL_PRESSURE = 120 / CompositionOfAir.MB_PER_ATM;
    private static final double O2_PARTIAL_PRESSURE = 200 / CompositionOfAir.MB_PER_ATM;
    private static final double H2O_PARTIAL_PRESSURE = 19.4 / CompositionOfAir.MB_PER_ATM;
    private static final double H2O_MOLAR_MASS = 18.02 / 1000;
    private static final double O2_MOLAR_MASS = 32.00 / 1000;
    private static final double N2_MOLAR_MASS = 28.02 / 1000;
    private static final double ARGON_MOLAR_MASS = 39.948 / 1000;
    private static final double CO2_MOLAR_MASS = 44.0095 / 1000;

	public AirComposition(double t, double vol) {

		// Part 1 : set up initial conditions at the start of sim
		initialiseGas(ResourceUtil.co2ID, AirComposition.CO2_PARTIAL_PRESSURE);
		initialiseGas(ResourceUtil.argonID, AirComposition.ARGON_PARTIAL_PRESSURE);
		initialiseGas(ResourceUtil.nitrogenID, AirComposition.N2_PARTIAL_PRESSURE);
		initialiseGas(ResourceUtil.oxygenID, AirComposition.O2_PARTIAL_PRESSURE);
		initialiseGas(ResourceUtil.waterID, AirComposition.H2O_PARTIAL_PRESSURE);
		
		// Part 2 : calculate total # of moles, total mass and total pressure
		fixedVolume = vol;
		for(Entry<Integer, AirComposition.GasDetails> g : gases.entrySet()) {
			int gasId = g.getKey();
			double molecularMass = getMolecularMass(gasId);
			AirComposition.GasDetails gas = g.getValue();

			double p = gas.partialPressure;
			double nm = p * vol / CompositionOfAir.R_GAS_CONSTANT / t;
			double m = molecularMass * nm;

			gas.numMoles = nm;
			gas.standardMoles = nm;
			gas.mass = m;

			totalMass += m;
			totalPressure += p;
		}
		
		// Part 3 : calculate for each building the percent composition
		updateGasPercentage();
	}

	public static void initializeInstances(PersonConfig personConfig) {
		o2Consumed = personConfig.getHighO2ConsumptionRate() / 1000D; // divide by 1000 to convert to [kg/millisol]

		cO2Expelled = personConfig.getCO2ExpelledRate() / 1000D; // [in kg/millisol] 1.0433 kg or 2.3 pounds CO2 per day
																	// for high metabolic activity.

		// If we are breathing regular air, at about ~20-21% 02, we use about 5% of that
		// O2 and exhale the by product of
		// glucose utilization CO2 and the balance of the O2, so exhaled breath is about
		// 16% oxygen, and about 4.75 % CO2.

		moistureExpelled = .8 / 1000D; // ~800 ml through breathing, sweat and skin per sol, divide by 1000 to convert								// to [kg/millisol]
	}

	private static final double getMolecularMass(int gasId) {
		// Can't use a switch because ResourceUtil ids are not constant, e.g. not final static.
		if (gasId == ResourceUtil.co2ID)
			return AirComposition.CO2_MOLAR_MASS;
		else if (gasId == ResourceUtil.argonID)
			return AirComposition.ARGON_MOLAR_MASS;
		else if (gasId == ResourceUtil.nitrogenID)
			return AirComposition.N2_MOLAR_MASS;
		else if (gasId == ResourceUtil.oxygenID)
			return AirComposition.O2_MOLAR_MASS;
		else if (gasId == ResourceUtil.waterID)
				return AirComposition.H2O_MOLAR_MASS;
		else
			throw new IllegalArgumentException("Unknown gas id=" + gasId);
	}

	private static final double getIdealPressure(int gasId) {
		// Can't use a switch because ResourceUtil ids are not constant, e.g. not final static.
		if (gasId == ResourceUtil.co2ID)
			return AirComposition.CO2_PARTIAL_PRESSURE;
		else if (gasId == ResourceUtil.argonID)
			return AirComposition.ARGON_PARTIAL_PRESSURE;
		else if (gasId == ResourceUtil.nitrogenID)
			return AirComposition.N2_PARTIAL_PRESSURE;
		else if (gasId == ResourceUtil.oxygenID)
			return AirComposition.O2_PARTIAL_PRESSURE;
		else if (gasId == ResourceUtil.waterID)
			 return AirComposition.H2O_PARTIAL_PRESSURE;
		else
			throw new IllegalArgumentException("Unknown gas id=" + gasId);
	}

	private void initialiseGas(int gasId, double initialPressure) {
		AirComposition.GasDetails gas = new AirComposition.GasDetails();
		gas.partialPressure = getIdealPressure(gasId);

		gases.put(gasId, gas);
	}

	private void updateGasPercentage() {
		for (AirComposition.GasDetails gd : gases.values()) {
			// calculate for each gas the % composition
			gd.percent = gd.partialPressure / totalPressure * 100D;
		}
	}

	/**
	 * Update gasses for occupants
	 * @param t Current temperature
	 * @param numPeople Number of people in using air
	 * @param time The time span of the gas consumption
	 */
	public void calcPersonImpact(double t, int numPeople, double time) {
		
		totalPressure = 0;
		totalMass = 0;
		
		for(Entry<Integer, AirComposition.GasDetails> g : gases.entrySet()) {
			int gasId = g.getKey();
			AirComposition.GasDetails gas = g.getValue();

			// Part 1 : calculate for each gas the partial pressure and # of moles
			double m = gas.mass;
			if (gasId == ResourceUtil.co2ID) {
				m += numPeople * cO2Expelled * time;
			} else if (gasId == ResourceUtil.oxygenID) {
				m -= numPeople * o2Consumed * time;
			} else if (gasId == ResourceUtil.waterID) {
				m += numPeople * moistureExpelled * time;
			}

			// Divide by molecular mass to convert mass to # of moles
			// note the kg/mole are as indicated as each gas have different amu
			double nm = m / getMolecularMass(gasId);
			double p = nm * CompositionOfAir.R_GAS_CONSTANT * t / fixedVolume;

			if (p < 0)
				p = 0;
			if (nm < 0)
				nm = 0;
			if (m < 0)
				m = 0;

			gas.partialPressure = p;
			gas.mass = m;
			gas.numMoles = nm;

			// Part 2
			// calculate for each building the total pressure, total # of moles and
			// percentage of composition
			totalPressure += gas.partialPressure;
			totalMass += gas.mass;
		}

		// Part 3
		// calculate for each building the percent composition
		updateGasPercentage();
	}

	/**
	 * Monitor the gases exchanges to a Resource Holder. 
	 * @param rh Source or destination of excess gasses.
	 * @param t Current temperature
	 */
	public void monitorGases(ResourceHolder rh, double t) {
		totalPressure = 0;
		totalMass = 0;

		for(Entry<Integer,AirComposition.GasDetails> g : gases.entrySet()) {
			int gasId = g.getKey();
			AirComposition.GasDetails gas = g.getValue();

			// double diff = delta/standard_moles;
			double PP = getIdealPressure(gasId);
			double p = gas.partialPressure;
			double tolerance = p / PP;

			// if this gas has BELOW 95% or ABOVE 105% the standard percentage of air
			// composition
			// if this gas has BELOW 90% or ABOVE 110% the standard percentage of air
			// composition
			if (tolerance > 1.1 || tolerance < .9) {

				double d_new_moles = gas.standardMoles - gas.numMoles;
				double molecularMass = getMolecularMass(gasId);
				double d_mass = d_new_moles * molecularMass; // d_mass can be -ve;
				// if (d_mass >= 0) d_mass = d_mass * 1.1D; //add or extract a little more to
				// save the future effort

				if (d_mass > 0) {
					rh.retrieveAmountResource(gasId, d_mass);
				}
				else { // too much gas, need to recapture it; d_mass is less than 0
					double recaptured = -d_mass * CompositionOfAir.GAS_CAPTURE_EFFICIENCY;
					if (recaptured > 0) {
						rh.storeAmountResource(gasId, recaptured);								
					}						
				}

				double new_m = gas.mass + d_mass;
				double new_moles = 0;

				if (new_m < 0) {
					new_m = 0;
				}
				else {
					new_moles = new_m / molecularMass;
				}

				gas.partialPressure = new_moles * CompositionOfAir.R_GAS_CONSTANT * t / fixedVolume;
				gas.mass = new_m;
				gas.numMoles = new_moles;
            }

            // Update total
            totalPressure += gas.partialPressure;
            totalMass += gas.mass;
		}

		updateGasPercentage();
	}

	/**
 	 * Release or recapture numbers of moles of a certain gas to a given building
 	 *
 	 * @param volume   volume change in the building
 	 * @param isReleasing positive if releasing, negative if recapturing
 	 * @param rh        local store of gases
 	*/
	public void releaseOrRecaptureAir(double volume, boolean isReleasing, ResourceHolder rh) {
		for(Entry<Integer,AirComposition.GasDetails> g : gases.entrySet()) {
			int gasId = g.getKey();
			AirComposition.GasDetails gas = g.getValue();

			double d_moles = gas.numMoles * volume / fixedVolume;

			double molecularMass = getMolecularMass(gasId);
			double d_mass = molecularMass * d_moles;

			if (isReleasing) {
				gas.numMoles = gas.numMoles + d_moles;
				gas.mass  = gas.mass + d_mass;
				if (d_mass > 0) {
					rh.retrieveAmountResource(gasId, d_mass);
				}
			}
			else { // recapture
				gas.numMoles = gas.numMoles - d_moles;
				gas.mass  = gas.mass - d_mass;
				if (d_mass > 0) {
					rh.storeAmountResource(gasId, d_mass * CompositionOfAir.GAS_CAPTURE_EFFICIENCY);	
				}
			}

			if (gas.numMoles < 0)
				gas.numMoles = 0;
			if (gas.mass< 0)
				gas.mass = 0;
		}
	}

    public double getTotalPressure() {
        return totalPressure;
    }

    public double getTotalMass() {
        return totalMass;
    }

    public GasDetails getGas(int gasId) {
        return gases.get(gasId);
    }

    /**
     * Calculate the O2 pressure for a quantity in a fixed volume.
     * @param gasVol Amount of O2 present
     * @param totalVol Total volume of the container 
     */
    public final static double getOxygenPressure(double gasVol, double totalVol) {
    	return CompositionOfAir.KPA_PER_ATM * gasVol / O2_MOLAR_MASS * CompositionOfAir.R_GAS_CONSTANT / totalVol;
    }
}