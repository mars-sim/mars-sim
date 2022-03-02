/*
 * Mars Simulation Project
 * CompositionOfAir.java
 * @date 2021-12-15
 * @author Manny Kung
 */
package org.mars_sim.msp.core.air;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The CompositionOfAir class accounts for the composition of air of each
 * building in a settlement.
 */
public class CompositionOfAir implements Serializable, Temporal {

	static final public class GasDetails implements Serializable {
		double percent;
		double partialPressure;
		double numMoles;
		double mass;
		double standardMoles;

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

	static final class AirComposition implements Serializable {
		// Note : Gas volumes are additive. If you mix some volumes of oxygen and
		// nitrogen, final volume will equal sum of
		// volumes, also final mass will equal sum of masses.

		double fixedVolume; // [in liter]; Note: 1 Cubic Meter = 1,000 Liters
		double totalPressure; // in atm
		double totalMoles;
		double totalMass; // in kg
		double temperature; // in C
	
		Map<Integer, GasDetails> gases = new HashMap<>();

		public AirComposition(double t, double vol) {
	
			// Part 1 : set up initial conditions at the start of sim
			initialiseGas(ResourceUtil.co2ID, CO2_PARTIAL_PRESSURE);
			initialiseGas(ResourceUtil.argonID, ARGON_PARTIAL_PRESSURE);
			initialiseGas(ResourceUtil.nitrogenID, N2_PARTIAL_PRESSURE);
			initialiseGas(ResourceUtil.oxygenID, O2_PARTIAL_PRESSURE);
			initialiseGas(ResourceUtil.waterID, H2O_PARTIAL_PRESSURE);
			
			// Part 2 : calculate total # of moles, total mass and total pressure
			fixedVolume = vol;
			temperature = t;
			for(Entry<Integer, GasDetails> g : gases.entrySet()) {
				int gasId = g.getKey();
				double molecularMass = getMolecularMass(gasId);
				GasDetails gas = g.getValue();
	
				double p = gas.partialPressure;
				double nm = p * vol / R_GAS_CONSTANT / t;
				double m = molecularMass * nm;
	
				gas.numMoles = nm;
				gas.standardMoles = nm;
				gas.mass = m;
	
				totalMoles += nm;
				totalMass += m;
				totalPressure += p;
			}
			
			// Part 3 : calculate for each building the percent composition
			updateGasPercentage();
		}

		private static final double getMolecularMass(int gasId) {
			// Can't use a switch because ResourceUtil ids are not constant, e.g. not final static.
			if (gasId == ResourceUtil.co2ID)
				return CO2_MOLAR_MASS;
			else if (gasId == ResourceUtil.argonID)
				return ARGON_MOLAR_MASS;
			else if (gasId == ResourceUtil.nitrogenID)
				return N2_MOLAR_MASS;
			else if (gasId == ResourceUtil.oxygenID)
				return O2_MOLAR_MASS;
			else if (gasId == ResourceUtil.waterID)
					return H2O_MOLAR_MASS;
			else
				throw new IllegalArgumentException("Unknown gas id=" + gasId);
		}

		private static final double getPartialPressure(int gasId) {
			// Can't use a switch because ResourceUtil ids are not constant, e.g. not final static.
			if (gasId == ResourceUtil.co2ID)
				return CO2_PARTIAL_PRESSURE;
			else if (gasId == ResourceUtil.argonID)
				return ARGON_PARTIAL_PRESSURE;
			else if (gasId == ResourceUtil.nitrogenID)
				return N2_PARTIAL_PRESSURE;
			else if (gasId == ResourceUtil.oxygenID)
				return O2_PARTIAL_PRESSURE;
			else if (gasId == ResourceUtil.waterID)
				 return H2O_PARTIAL_PRESSURE;
			else
				throw new IllegalArgumentException("Unknown gas id=" + gasId);
		}

		private void initialiseGas(int gasId, double initialPressure) {
			GasDetails gas = new GasDetails();
			gas.partialPressure = getPartialPressure(gasId);

			gases.put(gasId, gas);
		}

		private void updateGasPercentage() {
			for (GasDetails gd : gases.values()) {
				// calculate for each gas the % composition
				gd.percent = gd.partialPressure / totalPressure * 100D;
			}
		}

		public void updateGases(double t, double o2, double cO2, double moisture) {
			
			totalPressure = 0;
			totalMoles = 0;
			totalMass = 0;
			temperature = t;

			for(Entry<Integer, GasDetails> g : gases.entrySet()) {
				int gasId = g.getKey();
				double molecularMass = getMolecularMass(gasId);
				GasDetails gas = g.getValue();

				// Part 1 : calculate for each gas the partial pressure and # of moles
				double m = gas.mass;
				double nm = gas.numMoles;
				double p = 0;

				if (gasId == ResourceUtil.co2ID) {
					m += cO2;
				} else if (gasId == ResourceUtil.oxygenID) {
					m += o2;
				} else if (gasId == ResourceUtil.waterID) {
					m += moisture;
				}

				// Divide by molecular mass to convert mass to # of moles
				// note the kg/mole are as indicated as each gas have different amu

				nm = m / molecularMass;
				p = nm * R_GAS_CONSTANT * t / fixedVolume;

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
				totalMoles += gas.numMoles;
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
			totalMoles = 0;
			totalMass = 0;
			temperature = t;

			for(Entry<Integer,GasDetails> g : gases.entrySet()) {
				int gasId = g.getKey();
				GasDetails gas = g.getValue();

				// double diff = delta/standard_moles;
				double PP = getPartialPressure(gasId);
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
						double recaptured = -d_mass * GAS_CAPTURE_EFFICIENCY;
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

					gas.partialPressure = new_moles * R_GAS_CONSTANT * t / fixedVolume;
					gas.mass = new_m;
					gas.numMoles = new_moles;

					// Update total
					totalPressure = gas.partialPressure;
					totalMoles = gas.numMoles;
					totalMass = gas.mass;
				}
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
			for(Entry<Integer,GasDetails> g : gases.entrySet()) {
				int gasId = g.getKey();
				GasDetails gas = g.getValue();

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
						rh.storeAmountResource(gasId, d_mass * GAS_CAPTURE_EFFICIENCY);	
					}
				}

				if (gas.numMoles < 0)
					gas.numMoles = 0;
				if (gas.mass< 0)
					gas.mass = 0;
			}
		}
	}

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final double C_TO_K = 273.15;

	private static final double GAS_CAPTURE_EFFICIENCY = .95D;

	// Astronauts aboard the International Space Station preparing for
	// extra-vehicular activity (EVA) "camp out" at low atmospheric pressure,
	// 10.2 psi (0.70 bar), spending 8 sleeping hours in the Quest airlock
	// chamber before their spacewalk.
	//
	// During the EVA, they breathe 100% oxygen in their spacesuits,
	// which operate at 4.3 psi (0.30 bar), although
	// research has examined the possibility of using 100% O2 at
	// 9.5 psi (0.66 bar) in the suits to lessen the pressure
	// reduction, and hence the risk of DCS.
	//
	// see https://en.wikipedia.org/wiki/Decompression_sickness

	// private static final double[] EARTH_AIR_COMPOSITION_IN_PERCENT = new double[]
	// {.0407, .934, 77.0043, 21.021, 1};
	// public static final double EARTH_AIR_PRESSURE_IN_KPA = 101.325;

	// Assume having a 1% of water moisture

	public static final double PSI_PER_ATM = 14.696;
	private static final double MMHG_PER_ATM = 760;
	public static final double KPA_PER_ATM = 101.32501;
	public static final double MB_PER_ATM = 1013.2501;

	// The standard atmosphere (i.e. 1 atm) = 101325 Pa or 1 kPa = 0.00986923267 atm
	// 1 mbar = 0.0145037738 psi
	// 1 mbar =	0.1 kPa
	// Mars has only 0.13% of O2

	/**
	 * The % of air composition used by US Skylab Hab Modules. 5 psi or 340 mb is
	 * the overall pressure rating.
	 */
	// see http://www.collectspace.com/ubb/Forum29/HTML/001309.html
	// The partial pressures of each gas are in atm
	private static final double CO2_PARTIAL_PRESSURE = 0.5 / MB_PER_ATM;
	private static final double ARGON_PARTIAL_PRESSURE = 0.1 / MB_PER_ATM;
	private static final double N2_PARTIAL_PRESSURE = 120 / MB_PER_ATM;
	private static final double O2_PARTIAL_PRESSURE = 200 / MB_PER_ATM;
	private static final double H2O_PARTIAL_PRESSURE = 19.4 / MB_PER_ATM;

	private static final double CO2_MOLAR_MASS = 44.0095 / 1000;; // [in kg/mol]
	private static final double ARGON_MOLAR_MASS = 39.948 / 1000;; // [in kg/mol]
	private static final double N2_MOLAR_MASS = 28.02 / 1000;; // [in kg/mol]
	private static final double O2_MOLAR_MASS = 32.00 / 1000;; // [in kg/mol]
	private static final double H2O_MOLAR_MASS = 18.02 / 1000;; // [in kg/mol]

	private static final int MILLISOLS_PER_UPDATE = 2;
	private static final double CALCULATE_FREQUENCY = 2D;

	private static final double R_GAS_CONSTANT = 0.082057338; // [ in L atm K^−1 mol^−1 ]
	// alternatively, R_GAS_CONSTANT = 8.3144598 m^3 Pa K^−1 mol^−1
	// see https://en.wikipedia.org/wiki/Gas_constant


	// Data members
	private Map<Integer, AirComposition> indoorBuildings = new HashMap<>();

	/** The settlement ID */
	private int settlementID;

	/** The time accumulated [in millisols] for each crop update call. */
	private double accumulatedTime = RandomUtil.getRandomDouble(0, 1.0);
	/** Oxygen consumed by a person [kg/millisol] */
	private double o2Consumed;
	/** CO2 expelled by a person [kg/millisol] */
	private double cO2Expelled;
	/** Moisture expelled by a person [kg/millisol] */
	private double moistureExpelled;

	// Assume using Earth's atmospheric pressure at sea level, 14.7 psi, or ~ 1 bar,
	// for the habitat

	// Note : Mars' outside atmosphere is ~6 to 10 millibars (or .0882 to 0.147 psi)
	// , < 1% that of Earth's.

	// 1 cubic ft = L * 0.035315
	// Molar mass of CO2 = 44.0095 g/mol

	// The density of dry air at atmospheric pressure 101.325 kPa (101325 Pa) and
	// 22.5 C
	// is 101325 Pa / 286.9 J/kgK / (273K + 22.5K) = 1.1952 kg/m3

	// one mole of an ideal gas unders standard conditions (273 K and 1 atm)
	// occupies 22.4 L

	// A full scale pressurized Mars rover prototype may have an airlock volume of
	// 5.7 m^3

	// in Martian atmosphere, nitrogen (~2.7%) , argon (~1.6%) , carbon dioxide
	// (~95.3%)
	private static Simulation sim = Simulation.instance();
	private static PersonConfig personConfig;
	private static UnitManager unitManager = sim.getUnitManager();

	/**
	 * Constructor.
	 *
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public CompositionOfAir(Settlement settlement) {

		settlementID = settlement.getIdentifier();

		personConfig = SimulationConfig.instance().getPersonConfig();

		o2Consumed = personConfig.getHighO2ConsumptionRate() / 1000D; // divide by 1000 to convert to [kg/millisol]

		cO2Expelled = personConfig.getCO2ExpelledRate() / 1000D; // [in kg/millisol] 1.0433 kg or 2.3 pounds CO2 per day
																	// for high metabolic activity.

		// If we are breathing regular air, at about ~20-21% 02, we use about 5% of that
		// O2 and exhale the by product of
		// glucose utilization CO2 and the balance of the O2, so exhaled breath is about
		// 16% oxygen, and about 4.75 % CO2.

		moistureExpelled = .8 / 1000D; // ~800 ml through breathing, sweat and skin per sol, divide by 1000 to convert
										// to [kg/millisol]

		// h2oConsumed = personConfig.getWaterConsumptionRate() / 1000D;

		// https://micpohling.wordpress.com/2007/03/27/math-how-much-co2-is-emitted-by-human-on-earth-annually/
		// https://www.quora.com/How-much-water-does-a-person-lose-in-a-day-through-breathing
		//
		// Every day, we breath in about 14000L of air.
		// Assuming that the humidity of exhaled air is 100% and inhaled air is 20%,
		// Use the carrying capacity of 1kg of air to be 20g of water vapour,
		// This estimate gives 400ml of water lost per day
		// Thus, a person loses about 800ml of water per day, half through the skin
		// and half through respiration.

		List<Building> buildings = settlement.getBuildingManager().getBuildingsWithLifeSupport();

		for (Building b : buildings) {
			double t = C_TO_K + b.getCurrentTemperature();
			double vol = b.getVolumeInLiter(); // 1 Cubic Meter = 1,000 Liters

			
			AirComposition air = new AirComposition(t, vol);
			indoorBuildings.put(b.getIdentifier(), air);
		}
	}


	/**
	 * Time passing for the building.
	 *
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {

		// NOTE: use a boolean to check if the building configuration have changed
		List<Building> newList = unitManager.getSettlementByID(settlementID).getBuildingManager().getBuildingsWithLifeSupport();

		// For each time interval
		calculateGasExchange(pulse, newList);

		int msol = pulse.getMarsTime().getMillisolInt();

		if (msol % MILLISOLS_PER_UPDATE == 0) {
			monitorAir(newList);
		}

		return true;
	}

	/**
	 * Calculate the gas exchange that happens in an given interval of time
	 *
	 * @param pulse      ClockPulse
	 * @param buildings a list of buildings
	 */
	private void calculateGasExchange(ClockPulse pulse, List<Building> buildings) {
		double time = pulse.getElapsed();
		accumulatedTime += time;

		if (accumulatedTime >= CALCULATE_FREQUENCY) {
//			logger.info(settlement, 30_000, name + "  pulse width: " + Math.round(time * 10000.0)/10000.0 
//					+ "  accumulatedTime: " + Math.round(accumulatedTime * 100.0)/100.0 
//					+ "  processInterval: " + processInterval);

			accumulatedTime = accumulatedTime - CALCULATE_FREQUENCY;

			double o2 = o2Consumed * time;
			double cO2 = cO2Expelled * time;
			double moisture = moistureExpelled * time;
			// double h2o = (h2oConsumed - moistureExpelled) * time;
	
			for (Building b : buildings) {
	
				double tt = b.getCurrentTemperature();
				AirComposition air = indoorBuildings.get(b.getIdentifier());

				if (tt > -40 && tt < 40 && air != null) {

					int numPeople = b.getNumPeople();

					double t = C_TO_K + tt;

					o2 = numPeople * -o2; // consumed
					cO2 = numPeople * cO2; // generated
					moisture = numPeople * moisture; // generated

					air.updateGases(t, o2, cO2, moisture);
				}
			}
		}
	}

	/**
	 * Monitors air and add mass of gases below the threshold
	 *
	 * @param buildings a list of buildings
	 */
	private void monitorAir(List<Building> buildings) {
		// PART 1 :
		// check % of gas in each building
		// find the delta mass needed for each gas to go within the threshold
		// calculate for each gas the new partial pressure, the mass and # of moles
		ResourceHolder rh = unitManager.getSettlementByID(settlementID);

		for (Building b : buildings) {
			AirComposition air = indoorBuildings.get(b.getIdentifier());

			if (air != null) {
				double tt = b.getCurrentTemperature();

				if (tt > -40 && tt < 40) {

					double t = C_TO_K + tt;
					air.monitorGases(rh, t);
				}
			}
		}
	}

	/**
	 * Expands the array to keep track of the gases in the newly added buildings
	 *
	 * @param buildings a list of {@link Building}
	 * @param numID     numbers of buildings
	 */
	public void addAirNew(Building building) {
		Building b = building;

		double t = C_TO_K + b.getCurrentTemperature();
		double vol = b.getVolumeInLiter(); // 1 Cubic Meter = 1,000 Liters

		AirComposition air = new AirComposition(t, vol);
		indoorBuildings.put(b.getIdentifier(), air);
	}

	/**
	 * Release or recapture air from a given building
	 *
	 * @param isReleasing positive if releasing, negative if recapturing
	 * @param b        the building
	 * @param volLitres Gas volume in litres being exchanged
	 */
	public void releaseOrRecaptureAir(boolean isReleasing, Building b, double volLitres) {
		AirComposition air = indoorBuildings.get(b.getIdentifier());
		air.releaseOrRecaptureAir(volLitres, isReleasing, b);
	}

	/**
	 * Calculate the O2 pressure for a quantity in a fixed volume.
	 * @param gasVol Amount of O2 present
	 * @param totalVol Total volume of the container 
	 */
	public final static double getOxygenPressure(double gasVol, double totalVol) {
		return KPA_PER_ATM * gasVol / O2_MOLAR_MASS * R_GAS_CONSTANT / totalVol;
	}

	/**
	 * Reloads instances after loading from a saved sim
	 *
	 * @param c0 {@link MasterClock}
	 * @param c1 {@link MarsClock}
	 * @param pc {@link PersonConfig}
	 * @param u {@link UnitManager}
	 */
	public static void initializeInstances(PersonConfig pc, UnitManager u) {
		personConfig = pc;
		unitManager = u;
	}

	public void destroy() {
		personConfig = null;
	}

	/**
	 * Get the total air mass for a building
	 */
    public double getTotalMass(Building b) {
        return indoorBuildings.get(b.getIdentifier()).totalMass;
    }


    public double getTotalPressure(Building b) {
        return indoorBuildings.get(b.getIdentifier()).totalPressure;
    }

    public GasDetails getGasDetails(Building b, int gasId) {
        return indoorBuildings.get(b.getIdentifier()).gases.get(gasId);
    }
}
