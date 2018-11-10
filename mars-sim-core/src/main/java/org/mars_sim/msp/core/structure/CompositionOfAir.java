/**
 * Mars Simulation Project
 * CompositionOfAir.java
 * @version 3.1.0 2017-08-22
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * The CompositionOfAir class accounts for the composition of air of each
 * building in a settlement..
 */
public class CompositionOfAir implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(CompositionOfAir.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	private static final String GREENHOUSE = "greenhouse";

	public static final double C_TO_K = 273.15;

	private static final double AIRLOCK_VOLUME_IN_LITER = BuildingAirlock.AIRLOCK_VOLUME_IN_CM * 1000D; // [in liters]
																										// 12 m^3

	private static final double LOWER_THRESHOLD_GAS_COMPOSITION = -.05;

	private static final double UPPER_THRESHOLD_GAS_COMPOSITION = .05;

	private static final double GAS_CAPTURE_EFFICIENCY = .95D;

	// Astronauts aboard the International Space Station preparing for
	// extra-vehicular activity (EVA)
	// "camp out" at low atmospheric pressure, 10.2 psi (0.70 bar), spending eight
	// sleeping hours
	// in the Quest airlock chamber before their spacewalk. During the EVA they
	// breathe 100% oxygen
	// in their spacesuits, which operate at 4.3 psi (0.30 bar),[71] although
	// research has examined
	// the possibility of using 100% O2 at 9.5 psi (0.66 bar) in the suits to lessen
	// the pressure
	// reduction, and hence the risk of DCS.[72]
	// see https://en.wikipedia.org/wiki/Decompression_sickness

	// private static final double[] EARTH_AIR_COMPOSITION_IN_PERCENT = new double[]
	// {.0407, .934, 77.0043, 21.021, 1};
	// public static final double EARTH_AIR_PRESSURE_IN_KPA = 101.325;

	// Assume having a 1% of water moisture

	/**
	 * The % of air composition used by US Skylab Hab Modules. 5 psi or 340 mb is
	 * the overall pressure rating.
	 */
	private static final double[] SKYLAB_AIR_COMPOSITION_IN_MB = new double[] { 0.5, 0.1, 120, 200, 19.4 }; // {CO2, Ar, N2, O2, H2O
	// see http://www.collectspace.com/ubb/Forum29/HTML/001309.html

	public static final double SKYLAB_TOTAL_AIR_PRESSURE_IN_ATM = 340D;
	public static final double SKYLAB_TOTAL_AIR_PRESSURE_IN_MB = 340D;
	public static final double SKYLAB_TOTAL_AIR_PRESSURE_kPA = 34D; // 1 mb to .1 kPa

	public static final double PSI_PER_ATM = 14.7;
	public static final double MMHG_PER_ATM = 760;
	public static final double KPA_PER_ATM = 101.32501;
	public static final double MB_PER_ATM = 1013.2501;

	// The standard atmosphere (i.e. 1 atm) = 101325 Pa or 1 kPa = 0.00986923267 atm
	// Mars has only 0.13% of O2

	// The partial pressures of each gas are in atm
	private static final double CO2_PARTIAL_PRESSURE = SKYLAB_AIR_COMPOSITION_IN_MB[0] / MB_PER_ATM;// EARTH_AIR_COMPOSITION_IN_PERCENT[0]/100;
																									// // [in atm]
	private static final double ARGON_PARTIAL_PRESSURE = SKYLAB_AIR_COMPOSITION_IN_MB[1] / MB_PER_ATM;// EARTH_AIR_COMPOSITION_IN_PERCENT[1]/100;
																										// // [in atm]
	private static final double N2_PARTIAL_PRESSURE = SKYLAB_AIR_COMPOSITION_IN_MB[2] / MB_PER_ATM;// EARTH_AIR_COMPOSITION_IN_PERCENT[2]/100;
																									// // [in atm]
	public static final double O2_PARTIAL_PRESSURE = SKYLAB_AIR_COMPOSITION_IN_MB[3] / MB_PER_ATM;// EARTH_AIR_COMPOSITION_IN_PERCENT[3]/100;
																									// // [in atm]
	private static final double H2O_PARTIAL_PRESSURE = SKYLAB_AIR_COMPOSITION_IN_MB[4] / MB_PER_ATM;// EARTH_AIR_COMPOSITION_IN_PERCENT[4]/100;
																									// // [in atm]
	// https://en.wikipedia.org/wiki/Vapour_pressure_of_water

	private static final double[] PARTIAL_PRESSURES = new double[] { CO2_PARTIAL_PRESSURE, ARGON_PARTIAL_PRESSURE,
			N2_PARTIAL_PRESSURE, O2_PARTIAL_PRESSURE, H2O_PARTIAL_PRESSURE };

	/** The upper safe limit of the partial pressure [in atm] of O2 */
	// private static final double O2_PRESSURE_UPPER_LIMIT = 1.5;// [in atm]
	/** The lower safe limit of the partial pressure [in atm] of O2 */
	// private static final double O2_PRESSURE_LOWER_LIMIT = 0.15;// [in atm]

	public static final double CO2_MOLAR_MASS = 44.0095 / 1000;; // [in kg/mol]
	public static final double ARGON_MOLAR_MASS = 39.948 / 1000;; // [in kg/mol]
	public static final double N2_MOLAR_MASS = 28.02 / 1000;; // [in kg/mol]
	public static final double O2_MOLAR_MASS = 32.00 / 1000;; // [in kg/mol]
	public static final double H2O_MOLAR_MASS = 18.02 / 1000;; // [in kg/mol]

	public static final double CH4_MOLAR_MASS = 16.04276 / 1000;; // [in kg/mol]
	public static final double H2_MOLAR_MASS = 2.016 / 1000;; // [in kg/mol]

	private static final int MILLISOLS_PER_UPDATE = 2;

	private static final double R_GAS_CONSTANT = 0.082057338; // [ in L atm K^−1 mol^−1 ]
	// alternatively, R_GAS_CONSTANT = 8.3144598 m^3 Pa K^−1 mol^−1
	// see https://en.wikipedia.org/wiki/Gas_constant

	public static final int numGases = 5;
	/** The cache for msols */
	private int msolCache;

	private boolean isDone = false;

	/** Oxygen consumed by a person [kg/millisol] */
	private double o2Consumed;
	/** CO2 expelled by a person [kg/millisol] */
	private double cO2Expelled;
	/** Moisture expelled by a person [kg/millisol] */
	private double moistureExpelled;
	/** Water consumed by a person [kg/millisol] */
	// private double h2oConsumed;

	// private double dryAirDensity = 1.275D; // breath-able air in [kg/m3]

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

	// Data members
	private int numIDsCache;
	// private int solCache = 0;

	// Note : Gas volumes are additive. If you mix some volumes of oxygen and
	// nitrogen, final volume will equal sum of
	// volumes, also final mass will equal sum of masses.
	private double[] fixedVolume; // [in liter]; Note: 1 Cubic Meter = 1,000 Liters
	private double[] totalPressure; // in atm
	private double[] totalMoles;
	private double[] totalMass; // in kg
	// private double [] totalPercent;
	// private double [] buildingTemperature;

	private double[][] percent;
	private double[][] partialPressure;
	private double[][] temperature;
	private double[][] numMoles;
	private double[][] mass;

	private double[][] standardMoles;

	private Settlement settlement;
	private BuildingManager buildingManager;

	private static MasterClock masterClock;
	private static MarsClock clock;
	// private static SurfaceFeatures surfaceFeatures;
	private static PersonConfig personConfig;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public CompositionOfAir(Settlement settlement) {
		this.settlement = settlement;
		this.buildingManager = settlement.getBuildingManager();

		masterClock = Simulation.instance().getMasterClock();
		clock = masterClock.getMarsClock();
		// weather = Simulation.instance().getMars().getWeather();
		personConfig = SimulationConfig.instance().getPersonConfiguration();

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

		// see
		// https://micpohling.wordpress.com/2007/03/27/math-how-much-co2-is-emitted-by-human-on-earth-annually/
		// https://www.quora.com/How-much-water-does-a-person-lose-in-a-day-through-breathing
		// Every day, we breath in about 14000L of air.
		// Assuming that the humidity of exhaled air is 100% and inhaled air is 20%,
		// Use the carrying capacity of 1kg of air to be 20g of water vapour,
		// This estimate gives 400ml of water lost per day
		// Thus, a person loses about 800ml of water per day, half through the skin
		// and half through respiration.

		int numIDs = buildingManager.getLargestInhabitableID() + 1;
		numIDsCache = numIDs;

		// CO2, H2O, N2, O2, Ar2, He, CH4...
		// numGases = 5;

		percent = new double[numGases][numIDs];
		partialPressure = new double[numGases][numIDs];
		temperature = new double[numGases][numIDs];
		numMoles = new double[numGases][numIDs];
		mass = new double[numGases][numIDs];
		standardMoles = new double[numGases][numIDs];

		fixedVolume = new double[numIDs];
		totalPressure = new double[numIDs];
		totalMoles = new double[numIDs];
		totalMass = new double[numIDs];
		// buildingTemperature = new double[numIDs];

		// Part 1 : set up initial conditions at the start of sim
		for (int id = 0; id < numIDs; id++) {

			partialPressure[0][id] = CO2_PARTIAL_PRESSURE;
			partialPressure[1][id] = ARGON_PARTIAL_PRESSURE;
			partialPressure[2][id] = N2_PARTIAL_PRESSURE;
			partialPressure[3][id] = O2_PARTIAL_PRESSURE;
			partialPressure[4][id] = H2O_PARTIAL_PRESSURE;
			/*
			 * double total_pressure = 0; for (int i = 0; i < numGases; i++) total_pressure
			 * += partialPressure [i][id];
			 * 
			 * for (int j = 0; j < numGases; j++) percent [j][id] += partialPressure
			 * [j][id]/total_pressure;
			 */
			/*
			 * percent [0][id] = CO2_PARTIAL_PRESSURE/total_pressure; percent [1][id] =
			 * ARGON_PARTIAL_PRESSURE/total_pressure; percent [2][id] =
			 * N2_PARTIAL_PRESSURE/total_pressure; percent [3][id] =
			 * O2_PARTIAL_PRESSURE/total_pressure; percent [4][id] =
			 * H2O_PARTIAL_PRESSURE/total_pressure;
			 */
		}

		// Part 2 : calculate total # of moles, total mass and total pressure

		// double t = 22.5 + C_TO_K ;

		List<Building> buildings = buildingManager.getBuildingsWithLifeSupport();
		for (Building b : buildings) {

			int id = b.getInhabitableID();
			double t = C_TO_K + b.getCurrentTemperature();
			double vol = b.getVolumeInLiter(); // 1 Cubic Meter = 1,000 Liters

			fixedVolume[id] = vol;

			double sum1 = 0, sum2 = 0, sum3 = 0;// , sum4 = 0;

			for (int gas = 0; gas < numGases; gas++) {

				double molecularMass = getMolecularMass(gas);

				double p = partialPressure[gas][id];
				double nm = p * vol / R_GAS_CONSTANT / t;
				double m = molecularMass * nm;

				temperature[gas][id] = t;
				numMoles[gas][id] = nm;
				standardMoles[gas][id] = nm;
				mass[gas][id] = m;

				sum1 += nm;
				sum2 += m;
				sum3 += p;
				// sum4 += t;

			}

			totalMoles[id] = sum1;
			totalMass[id] = sum2;
			totalPressure[id] = sum3;
			// buildingTemperature[id] = sum4/numGases;

			// System.out.println(b.getNickName() + " has a total " +
			// Math.round(totalMass[id]*100D)/100D + " kg of gas");
		}

		// Part 3 : calculate for each building the percent composition
		for (int id = 0; id < numIDs; id++) {
			// calculate for each gas the % composition
			for (int gas = 0; gas < numGases; gas++) {
				percent[gas][id] = partialPressure[gas][id] / totalPressure[id] * 100D;

			}
		}
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {
		List<Building> newList = buildingManager.getBuildingsWithLifeSupport();
		int num = buildingManager.getLargestInhabitableID() + 1;

		// if adding or subtracting a building in the settlement
		addAirNew(newList, num);

		// For each time interval
		calculateGasExchange(time, newList, num);

		int msol = clock.getMillisolInt();

		if (msolCache != msol && msol % MILLISOLS_PER_UPDATE == 0) {
			msolCache = msol;

			monitorAir(newList, num);
		}

	}

	public double getMolecularMass(int gas) {
		if (gas == 0)
			return CO2_MOLAR_MASS;
		else if (gas == 1)
			return ARGON_MOLAR_MASS;
		else if (gas == 2)
			return N2_MOLAR_MASS;
		else if (gas == 3)
			return O2_MOLAR_MASS;
		else if (gas == 4)
			return H2O_MOLAR_MASS;
		else
			return 0;
	}

	/**
	 * Calculate the gas exchange that happens in an given interval of time
	 * 
	 * @param time      interval in millisols
	 * @param buildings a list of buildings
	 * @param num       numbers of buildings
	 */
	public void calculateGasExchange(double time, List<Building> buildings, int num) {

		double o2 = o2Consumed * time;
		double cO2 = cO2Expelled * time;
		double moisture = moistureExpelled * time;
		// double h2o = (h2oConsumed - moistureExpelled) * time;

		// Part 1 : calculate for each gas the partial pressure and # of moles
		for (Building b : buildings) {
			
			double tt = b.getCurrentTemperature();
			
			if (tt > -40 && tt < 40) {
				
				int id = b.getInhabitableID();
				int numPeople = b.getInhabitants().size();
	
				double t = C_TO_K + tt;
	
				o2 = numPeople * -o2; // consumed
				cO2 = numPeople * cO2; // generated
				moisture = numPeople * moisture; // generated
				// h2o = numPeople * h2o;
	
				// Extract the air moisture generated, O2 generated and CO2 consumed if it's a
				// greenhouse
//				if (b.getBuildingType().toLowerCase().contains(GREENHOUSE)) {
//					double _m = b.getFarming().getMoisture();
//					if (_m > 0)
//						moisture = moisture + b.getFarming().retrieveMoisture(_m); // generated by crops
//					double _o2 = b.getFarming().getO2();
//					if (_o2 > 0)
//						o2 = o2 + b.getFarming().retrieveO2(_o2); // generated by crops
//					double _cO2 = b.getFarming().getCO2();
//					if (_cO2 > 0)
//						cO2 = cO2 - b.getFarming().retrieveCO2(_cO2); // consumed by crops
//				}
	
				for (int gas = 0; gas < numGases; gas++) {
	
					double molecularMass = getMolecularMass(gas);
	
					double m = mass[gas][id];
					double nm = numMoles[gas][id];
					double p = 0;
					
					if (gas == 0) {
						m += cO2;
					} else if (gas == 3) {
						m += o2;
					} else if (gas == 4) {
						m += moisture;
					}
	
					// Divide by molecular mass to convert mass to # of moles
					// note the kg/mole are as indicated as each gas have different amu
	
					nm = m / molecularMass;
					p = nm * R_GAS_CONSTANT * t / fixedVolume[id];
					
					if (p < 0)
						p = 0;
					if (nm < 0)
						nm = 0;
					if (m < 0)
						m = 0;
					
					temperature[gas][id] = t;
					partialPressure[gas][id] = p;
					mass[gas][id] = m;
					numMoles[gas][id] = nm;
	
				}
			}
		}

		// Part 2
		// calculate for each building the total pressure, total # of moles and
		// percentage of composition
		for (int id = 0; id < num; id++) {

			double sum_p = 0, sum_nm = 0, sum_m = 0;// , sum_t = 0;
			// calculate for each gas the total pressure and moles
			for (int gas = 0; gas < numGases; gas++) {

				sum_p += partialPressure[gas][id];
				sum_nm += numMoles[gas][id];
				sum_m += mass[gas][id];
				// sum_t += temperature[gas][id];

			}

			totalPressure[id] = sum_p;
			totalMoles[id] = sum_nm;
			totalMass[id] = sum_m;
			// buildingTemperature[id] = sum_t/numGases;

			// System.out.println(buildingManager.getBuilding(id).getNickName() + " has a
			// total " + Math.round(totalMass[id]*100D)/100D + " kg of gas");
		}

		// Part 3
		// calculate for each building the percent composition
		for (int id = 0; id < num; id++) {
			// calculate for each gas the % composition
			for (int gas = 0; gas < numGases; gas++) {
				percent[gas][id] = partialPressure[gas][id] / totalPressure[id] * 100D;
				// if (percent [gas][id] < 0)
				// System.out.println("gas " + gas
				// + "'s percent : " + Math.round(percent [gas][id]*100.0)/100.0);
			}
		}
	}

	/**
	 * Monitors air and add mass of gases below the threshold
	 * 
	 * @param buildings a list of buildings
	 * @param num       numbers of buildings
	 */
	public void monitorAir(List<Building> buildings, int num) {
		// PART 1 :
		// check % of gas in each building
		// find the delta mass needed for each gas to go within the threshold
		// calculate for each gas the new partial pressure, the mass and # of moles

		for (Building b : buildings) {
			int id = b.getInhabitableID();
			
			double tt = b.getCurrentTemperature();

			if (tt > -40 && tt < 40) {

				double t = C_TO_K + tt;
				
				for (int gas = 0; gas < numGases; gas++) {

					// [0] = CO2
					// [1] = ARGON
					// [2] = N2
					// [3] = O2
					// [4] = H2O

					// double diff = delta/standard_moles;
					double PP = PARTIAL_PRESSURES[gas];
					double p = partialPressure[gas][id];
//					double diff = (PP - p) / PP;
					double tolerance = p / PP;
					
					// if this gas has BELOW 95% or ABOVE 105% the standard percentage of air
					// composition
//					if (Math.abs(diff) > UPPER_THRESHOLD_GAS_COMPOSITION || 
					// if this gas has BELOW 90% or ABOVE 110% the standard percentage of air
					// composition					
					if (tolerance > 1.1 || tolerance < .9) {

						double d_new_moles = standardMoles[gas][id] - numMoles[gas][id];;
						double molecularMass = getMolecularMass(gas);
						double d_mass = d_new_moles * molecularMass; // d_mass can be -ve;
						// if (d_mass >= 0) d_mass = d_mass * 1.1D; //add or extract a little more to
						// save the future effort
						int ar = getGasID(gas);

						if (d_mass > 0)
							Storage.retrieveAnResource(d_mass, ar, b.getInventory(), true);
						else { // too much gas, need to recapture it; d_mass is less than 0
							double recaptured = -d_mass * GAS_CAPTURE_EFFICIENCY;
							if (recaptured > 0)
								Storage.storeAnResource(recaptured, ar, b.getInventory(), sourceName + "::monitorAir");
						}

						double new_m = mass[gas][id] + d_mass;
						double new_moles = 0;
						
						if (new_m < 0) {
//							logger.info("[" + settlement + "] no more " + ResourceUtil.findAmountResource(ar).getName()
//									+ " in ");
							new_m = 0;
						}
						else {
							new_moles = new_m / molecularMass;
						}

						temperature[gas][id] = t;
						partialPressure[gas][id] = new_moles * R_GAS_CONSTANT * t / fixedVolume[id];
						mass[gas][id] = new_m;
						numMoles[gas][id] = new_moles;
					}
				}
			}
		}

		// Part 2
		// calculate for each building the total pressure, total # of moles and
		// percentage of composition
		for (int id = 0; id < num; id++) {

			double p = 0, nm = 0, m = 0;
			// calculate for each gas the total pressure and moles
			for (int gas = 0; gas < numGases; gas++) {

				p += partialPressure[gas][id];
				nm += numMoles[gas][id];
				m += mass[gas][id];

			}

			totalPressure[id] = p;
			totalMoles[id] = nm;
			totalMass[id] = m;

		}

		// Part 3
		// calculate for each building the percent composition
		for (int id = 0; id < num; id++) {
			// calculate for each gas the % composition
			for (int gas = 0; gas < numGases; gas++) {
				percent[gas][id] = partialPressure[gas][id] / totalPressure[id] * 100D;

			}
		}
	}

	/**
	 * Obtain the Amount Resource id of a given gas
	 * 
	 * @param gas id
	 * @return AmountResource id of this gas
	 */
	public int getGasID(int gas) {
		if (gas == 0)
			return ResourceUtil.co2ID;
		else if (gas == 1)
			return ResourceUtil.argonID;
		else if (gas == 2)
			return ResourceUtil.nitrogenID;
		else if (gas == 3)
			return ResourceUtil.oxygenID;
		else if (gas == 4)
			return ResourceUtil.waterID;
		return -1;
	}

	/**
	 * Expands the array to keep track of the gases in the newly added buildings
	 * 
	 * @param buildings a list of {@link Building}
	 * @param numID     numbers of buildings
	 */
	public void addAirNew(List<Building> buildings, int numID) {

		int diff = numID - numIDsCache;

		if (numID != numIDsCache && diff > 0) {
			// if a building is added from a settlement

			numIDsCache = numID;
			// System.out.println("numBuildings : " + numBuildings + " numBuildingsCache : "
			// + numBuildingsCache);
			// System.out.println("percentComposition.length : " +
			// percentComposition.length);
			// System.out.println("partialPressure[0].length : " +
			// partialPressure[0].length);
			// increase the size of the vectors...
			// initialize the new building with default values;

			double[] new_volume = Arrays.copyOf(fixedVolume, fixedVolume.length + diff);

			double[] new_totalPressure = Arrays.copyOf(totalPressure, totalPressure.length + diff);
			double[] new_totalMoles = Arrays.copyOf(totalMoles, totalMoles.length + diff);
			double[] new_totalMass = Arrays.copyOf(totalMass, totalMass.length + diff);
			// double [] new_buildingTemperature = Arrays.copyOf(buildingTemperature,
			// buildingTemperature.length + diff);

			double[][] new_temperature = createGasArray(temperature, numID);
			double[][] new_percent = createGasArray(percent, numID);

			double[][] new_partialPressure = createGasArray(partialPressure, numID);
			double[][] new_numMoles = createGasArray(numMoles, numID);
			double[][] new_standard_moles = createGasArray(standardMoles, numID);
			double[][] new_mass = createGasArray(mass, numID);

			// double [][] new_percent = Arrays.copyOf(percentComposition,
			// percentComposition[0].length + diff);
			// double [][] new_partialPressure = Arrays.copyOf(partialPressure,
			// partialPressure[0].length + diff);
			// double [][] new_temperature = Arrays.copyOf(temperature,
			// temperature[0].length + diff);
			// double [][] new_numMoles = Arrays.copyOf(numMoles, numMoles[0].length +
			// diff);
			/*
			 * // Use Java 8 stream to create new 2D arrays with more columns. double [][]
			 * new_percent = Arrays.stream(percentComposition) .map((double[] row) ->
			 * row.clone()) .toArray((int length) -> new double[length + diff][]);
			 */

			// System.out.println("new_partialPressure[0].length : " +
			// new_partialPressure[0].length);

			for (int id = numID; id < numID; id++) {
				// System.out.println("j : " + j);
				new_totalPressure[id] = 1.0;

				new_partialPressure[0][id] = CO2_PARTIAL_PRESSURE;
				new_partialPressure[1][id] = ARGON_PARTIAL_PRESSURE;
				new_partialPressure[2][id] = N2_PARTIAL_PRESSURE;
				new_partialPressure[3][id] = O2_PARTIAL_PRESSURE;
				new_partialPressure[4][id] = H2O_PARTIAL_PRESSURE;

			}

			List<Building> newList = new ArrayList<>();

			// Part 2 : calculate # of moles and mass
			// Assembled a list of new buildings
			for (Building b : buildings) {
				int id = b.getInhabitableID();
				if (id >= numIDsCache)
					newList.add(b);
			}

			// for (int id = 0; id< num; id++) {
			for (Building b : newList) {
				int id = b.getInhabitableID();

				double t = C_TO_K + b.getCurrentTemperature();
				double sum_nm = 0, sum_p = 0, sum_mass = 0;// , sum_t = 0;
				double vol = b.getVolumeInLiter(); // 1 Cubic Meter = 1,000 Liters

				new_volume[id] = vol;

				// calculate for each gas the new volume, # of moles and total # of moles
				for (int gas = 0; gas < numGases; gas++) {

					double molecularMass = getMolecularMass(gas);

					double p = new_partialPressure[gas][id];
					double nm = p * vol / R_GAS_CONSTANT / t;
					double m = molecularMass * nm;

					if (p < 0)
						p = 0;
					if (nm < 0)
						nm = 0;
					if (m < 0)
						m = 0;
					
					new_temperature[gas][id] = t;
					new_numMoles[gas][id] = nm;
					new_standard_moles[gas][id] = nm;
					new_mass[gas][id] = m;
					new_partialPressure[gas][id] = p;

					sum_nm += nm;
					sum_p += p;
					sum_mass += m;
					// sum_t += t;

				}

				new_totalMoles[id] = sum_nm;
				new_totalPressure[id] = sum_p;
				new_totalMass[id] = sum_mass;
				// new_buildingTemperature [id] = sum_t/numGases;

			}

			// Part 3 : calculate for each building the percent composition
			for (int id = 0; id < numID; id++) {
				// calculate for each gas the % composition
				for (int gas = 0; gas < numGases; gas++) {
					new_percent[gas][id] = new_partialPressure[gas][id] / new_totalPressure[id] * 100D;

				}
			}

			percent = new_percent;
			fixedVolume = new_volume;
			temperature = new_temperature;

			partialPressure = new_partialPressure;

			numMoles = new_numMoles;
			standardMoles = new_standard_moles;
			mass = new_mass;

			totalPressure = new_totalPressure;
			totalMoles = new_totalMoles;
			totalMass = new_totalMass;
			// buildingTemperature = new_buildingTemperature;

			/*
			 * double [][] new_percentByVolume = new double[numGases][numBuildings]; double
			 * [] new_volume = new double[numBuildings]; double [][] new_partialPressure =
			 * new double[numGases][numBuildings]; double [][] new_temperature = new
			 * double[numGases][numBuildings]; double [][] new_numMoles = new
			 * double[numGases][numBuildings]; double [] new_totalPressure = new
			 * double[numBuildings];
			 */

			numIDsCache = numID;
		}

	}

	/**
	 * Creates a new array for gases and pad it with zero for the new building
	 * 
	 * @param oldArray
	 * @param numBuildings
	 * @return new array
	 */
	public double[][] createGasArray(double[][] oldArray, int numBuildings) {
		double[][] newArray = new double[numGases][numBuildings];
		int size = oldArray[0].length;
		for (int j = 0; j < size; j++) {
			for (int i = 0; i < numGases; i++) {
				if (j < numIDsCache) {
					newArray[i][j] = oldArray[i][j];
				} else
					newArray[i][j] = 0;
			}
		}
		return newArray;
	}

	/**
	 * Release or recapture air from a given building
	 * 
	 * @param id       inhabitable id of a building
	 * @param isReleasing positive if releasing, negative if recapturing
	 * @param b        the building
	 */
	public void releaseOrRecaptureAir(int id, boolean isReleasing, Building b) {
		double d_moles[] = new double[numGases];
		// double t = b.getCurrentTemperature();

		for (int gas = 0; gas < numGases; gas++) {
			// double pressure = getPartialPressure()[gas][id];
			// double t = getTemperature()[gas][id];
			// calculate moles on each gas
			d_moles[gas] = numMoles[gas][id] * AIRLOCK_VOLUME_IN_LITER / fixedVolume[id]; // pressure / R_GAS_CONSTANT /
																							// t *
																							// AIRLOCK_VOLUME_IN_LITER;

			releaseOrRecaptureGas(gas, id, d_moles[gas], isReleasing, b);
		}
	}

	/**
	 * Release or recapture numbers of moles of a certain gas to a given building
	 * 
	 * @param gas      the type of gas
	 * @param id       inhabitable id of a building
	 * @param d_moles  numbers of moles
	 * @param isReleasing positive if releasing, negative if recapturing
	 * @param b        the building
	 */
	public void releaseOrRecaptureGas(int gas, int id, double d_moles, boolean isReleasing, Building b) {
		double old_moles = numMoles[gas][id];
		double old_mass = mass[gas][id];
		double new_moles = 0;
		double new_mass = 0;
		double molecularMass = getMolecularMass(gas);

		double d_mass = molecularMass * d_moles;

		int ar = getGasID(gas);

		// System.out.println(" # moles of [" + gas + "] to pump or recapture : " +
		// d_moles);
		// System.out.println(" mass of [" + gas + "] to pump or recapture : " +
		// d_mass);
		if (isReleasing) {
			new_moles = old_moles + d_moles;
			new_mass = old_mass + d_mass;
			if (d_mass > 0)
				Storage.retrieveAnResource(d_mass, ar, b.getInventory(), true);
		} else { // recapture
			new_moles = old_moles - d_moles;
			new_mass = old_mass - d_mass;
			if (d_mass > 0)
				Storage.storeAnResource(d_mass * GAS_CAPTURE_EFFICIENCY, ar, b.getInventory(),
						sourceName + "::pumpOrRecaptureGas");
		}

		if (new_moles < 0)
			new_moles = 0;
		if (new_mass < 0)
			new_mass = 0;
		
		numMoles[gas][id] = new_moles;
		mass[gas][id] = new_mass;

	}

	public double[][] getPercentComposition() {
		return percent;
	}

	public double[][] getPartialPressure() {
		return partialPressure;
	}

	public double[][] getTemperature() {
		return temperature;
	}

	public double[][] getNumMoles() {
		return numMoles;
	}

	public double[][] getMass() {
		return mass;
	}

	public double[] getTotalMass() {
		return totalMass;
	}

	public double[] getTotalPressure() {
		return totalPressure;
	}

	public double[] getTotalMoles() {
		return totalMoles;
	}

	/**
	 * Calculates the partial pressure of water vapor at a given temperature using
	 * Buck equation
	 * 
	 * @param t_C temperature in deg celsius
	 * @return partial pressure in kPa Note : see
	 *         https://en.wikipedia.org/wiki/Vapour_pressure_of_water
	 */
	public double calculateWaterVaporPressure(double t_C) {
		return 0.61121 * Math.exp((18.678 - t_C / 234.5) * (t_C / (257.14 + t_C)));
	}

	public void destroy() {
		buildingManager = null;
		settlement = null;
		masterClock = null;
		clock = null;
		personConfig = null;
	}

}