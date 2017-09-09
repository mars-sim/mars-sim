/**
 * Mars Simulation Project
 * CompositionOfAir.java
 * @version 3.1.0 2017-08-22
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The CompositionOfAir class accounts for the composition of air of each building in a settlement..
 */
public class CompositionOfAir implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(CompositionOfAir.class.getName());
	
	public static final double C_TO_K = 273.15;
	
	public static final int numGases = 5;
	
	private static final double HEIGHT = 2.5; // assume an uniform height of 2.5m in all buildings
	
	private static final double LOW_ATM_FACTOR = 0.6463D; // 9.5 psi / 14.7 psi = 0.6463

	private static final double AIRLOCK_VOLUME_IN_LITER = Building.AIRLOCK_VOLUME_IN_CM / 1000D; // [in liters] 12 cm^3 -> .012 L
	
	private static final double LOWER_THRESHOLD_GAS_COMPOSITION = .95;
	
	private static final double UPPER_THRESHOLD_GAS_COMPOSITION = 1.05;
	
	private static final double GAS_CAPTURE_EFFICIENCY = .75D;
	
	// Astronauts aboard the International Space Station preparing for extra-vehicular activity (EVA) 
	// "camp out" at low atmospheric pressure, 10.2 psi (0.70 bar), spending eight sleeping hours 
	// in the Quest airlock chamber before their spacewalk. During the EVA they breathe 100% oxygen 
	// in their spacesuits, which operate at 4.3 psi (0.30 bar),[71] although research has examined 
	// the possibility of using 100% O2 at 9.5 psi (0.66 bar) in the suits to lessen the pressure 
	// reduction, and hence the risk of DCS.[72]
	// see https://en.wikipedia.org/wiki/Decompression_sickness

	
	//private static final double CO2_PERCENT = 0;//0.0407;
	//private static final double ARGON_PERCENT = 0;//0.9340;
	//private static final double N2_PERCENT = 0;//78.084;
	//private static final double O2_PERCENT = 0;//20.946;
	//private static final double H2O_PERCENT = 0;//0.0047;

	private static final double[] STANDARD_GAS_PERCENT = new double[] {.0407, .934, 78.0043, 20.021, 1}; // assuming having 1% of moisture
	
	
	private static final double CO2_PARTIAL_PRESSURE = 0.000407;
	private static final double ARGON_PARTIAL_PRESSURE = 0.00934;
	private static final double N2_PARTIAL_PRESSURE = .780043;
	private static final double O2_PARTIAL_PRESSURE = .20021;
	private static final double H2O_PARTIAL_PRESSURE = 0.01;
	
	public static final double CO2_MOLAR_MASS = 44.0095 /1000D; // [in kg/mol]
	public static final double ARGON_MOLAR_MASS = 39.948 /1000D; // [in kg/mol]
	public static final double N2_MOLAR_MASS = 28.02 /1000D; // [in kg/mol]
	public static final double O2_MOLAR_MASS = 32.00 /1000D; // [in kg/mol]
	public static final double H2O_MOLAR_MASS = 18.02 /1000D; // [in kg/mol] 
	
	public static final double CH4_MOLAR_MASS = 16.04276; // [in g/mol] 
	public static final double H2_MOLAR_MASS = 2.016; // [in g/mol] 
	
	private static final int MILLISOLS_PER_UPDATE = 50 ;
	
    public static final double kPASCAL_PER_ATM = 1D/0.00986923267 ; 
    // 1 kilopascal = 0.00986923267 atm
    // The standard atmosphere (i.e. 1 atm) = 101325 Pa
    private static final double R_GAS_CONSTANT = 0.082057338; // [ in L atm K^−1 mol^−1 ]
    // alternatively, R_GAS_CONSTANT = 8.3144598 m^3 Pa K^−1 mol^−1
    // see https://en.wikipedia.org/wiki/Gas_constant

    /** Oxygen consumed by a person [kg/millisol] */
    private double o2Consumed;
    /** CO2 expelled by a person [kg/millisol] */    
    private double cO2Expelled;
    /** Moisture expelled by a person [kg/millisol] */
    private double moistureExpelled;

	private double dryAirDensity = 1.275D; // breath-able air in [kg/m3]

	// Assume using Earth's atmospheric pressure at sea level, 14.7 psi, or ~ 1 bar, for the habitat

	// Note : Mars' outside atmosphere is ~6 to 10 millibars (or .0882 to 0.147 psi) , < 1% that of Earth's. 
	
	// 1 cubic ft = L * 0.035315
	// Molar mass of CO2 = 44.0095 g/mol

	// The density of dry air at atmospheric pressure 101.325 kPa (101325 Pa) and 22.5 C 
	// is 101325 Pa / 286.9 J/kgK / (273K + 22.5K) = 1.1952 kg/m3
	
	// one mole of an ideal gas unders standard conditions (273 K and 1 atm) occupies 22.4 L

    // A full scale pressurized Mars rover prototype may have an airlock volume of 5.7 m^3
	
	// in Martian atmosphere, nitrogen (~2.7%) , argon (~1.6%) ,  carbon dioxide (~95.3%)
	
	// Data members
	private int numIDsCache;
	//private int solCache = 0;

	private double [] fixedVolume; // [in liter] note: // 1 Cubic Meter = 1,000 Liters
	private double [] totalPressure;
	private double [] totalMoles;
	private double [] totalMass;
	//private double [] totalPercent;
	
	private double [][] percent;
	private double [][] partialPressure;
	private double [][] temperature;
	private double [][] numMoles;
	private double [][] mass;
	
	// Note : Gas volumes are additive. If you mix some volumes of oxygen and nitrogen, final volume will equal sum of volumes, also final mass will equal sum of masses. 

	private Map<Integer, Double> emissivityMap;

	private Settlement settlement;
 	private ThermalSystem thermalSystem;
 	private BuildingManager buildingManager;
	private Weather weather;
	private Coordinates location;
	private MasterClock masterClock;
	private MarsClock clock;
	private SurfaceFeatures surfaceFeatures;
	private PersonConfig personConfig;
	
	private List<Building> buildings;

	/**
	 * Constructor.
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public CompositionOfAir(Settlement settlement) {
		this.settlement = settlement;
		this.buildingManager = settlement.getBuildingManager();

		masterClock = Simulation.instance().getMasterClock();
		clock = masterClock.getMarsClock();
		weather = Simulation.instance().getMars().getWeather();
		personConfig = SimulationConfig.instance().getPersonConfiguration();
		
		o2Consumed = personConfig.getNominalO2ConsumptionRate() /1000D; // divide by 1000 to convert to [kg/millisol] 
		
		cO2Expelled = 1.0433/1000D; // [in kg/millisol]  1.0433 kg or 2.3 pounds CO2 per day 
		
		// If we are breathing regular air, at about ~20-21% 02, we use about 5% of that O2 and exhale the by product of 
		// glucose utilization CO2 and the balance of the O2, so exhaled breath is about 16% oxygen, and about 4.75 % CO2. 
		
		moistureExpelled = .8/1000D; // ~800 ml through breathing, sweat and skin per sol, divide by 1000 to convert to [kg/millisol] 
		
		// see https://micpohling.wordpress.com/2007/03/27/math-how-much-co2-is-emitted-by-human-on-earth-annually/	
		// https://www.quora.com/How-much-water-does-a-person-lose-in-a-day-through-breathing
		// Every day, we breath in about 14000L of air.
		// Assuming that the humidity of exhaled air is 100% and inhaled air is 20%,
		// Use the carrying capacity of 1kg of air to be 20g of water vapour,
		// This estimate gives 400ml of water lost per day
		// Thus, a person loses about 800ml of water per day, half through the skin
		// and half through respiration.

		buildings = buildingManager.getBuildingsWithLifeSupport();

		int numIDs = buildingManager.getLargestInhabitableID() + 1;
		numIDsCache = numIDs;

		// CO2, H2O, N2, O2, Ar2, He, CH4...
		// numGases = 5;

		percent = new double[numGases][numIDs];
		partialPressure = new double[numGases][numIDs];
		temperature = new double[numGases][numIDs];
		numMoles = new double[numGases][numIDs];
		mass = new double[numGases][numIDs];
		
		fixedVolume = new double[numIDs];
		totalPressure = new double[numIDs];
		totalMoles = new double[numIDs];
		totalMass = new double[numIDs];

		// Part 1 : set up initial conditions at the start of sim
		for (int id = 0; id < numIDs; id++) {

			partialPressure [0][id] = CO2_PARTIAL_PRESSURE ;
			partialPressure [1][id] = ARGON_PARTIAL_PRESSURE;
			partialPressure [2][id] = N2_PARTIAL_PRESSURE;
			partialPressure [3][id] = O2_PARTIAL_PRESSURE;
			partialPressure [4][id] = H2O_PARTIAL_PRESSURE;

			//percentComposition [0][id] = CO2_PERCENT;
			//percentComposition [1][id] = ARGON_PERCENT;
			//percentComposition [2][id] = N2_PERCENT;
			//percentComposition [3][id] = O2_PERCENT;
			//percentComposition [4][id] = H2O_PERCENT;
		}


		
		// Part 2 : calculate total # of moles, total mass and total pressure
		
		//double t =  22.5 + C_TO_K ;
	
		for (Building b: buildings) {
						
			int id = b.getInhabitableID();
			double t = C_TO_K  + b.getCurrentTemperature();

			double sum1 = 0, sum2 = 0, sum3 = 0;
			
			for (int gas = 0; gas < numGases; gas++) {
						
				double molecularMass = getMolecularMass(gas);
				double vol = b.getWidth() * b.getLength() * HEIGHT * 1000D; // 1 Cubic Meter = 1,000 Liters

				double p = partialPressure [gas][id];
				double nm = p * vol / R_GAS_CONSTANT / t;
				double m = molecularMass * nm;
				
				fixedVolume [id] = vol;
				temperature [gas][id] = t;
				
				numMoles [gas][id] = nm;
				mass [gas][id] = m;

				sum1 += nm;
				sum2 += m;
				sum3 += p;
				
			}
			
			totalMoles [id] = sum1;
			totalMass [id] = sum2;
			totalPressure [id] = sum3;

			//System.out.println(b.getNickName() + " has a total " + Math.round(totalMass[id]*100D)/100D + " kg of gas");
		}
		
		// Part 3 : calculate for each building the percent composition
		for (int id = 0; id< numIDs; id++) {
			// calculate for each gas the % composition
			for (int gas= 0; gas< numGases; gas++) {
				percent [gas][id] = partialPressure [gas][id] / totalPressure[id] * 100D;

			}
		}
	}

	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {
		List<Building> newList = buildingManager.getBuildingsWithLifeSupport();
		int num = buildingManager.getLargestInhabitableID() + 1;
		
		// if adding or subtracting a building form the settlement
		addAirNew(newList, num);
		
		// For each time interval
		calculateGasExchange(time, newList, num);
		
		
		int checkTime = (int) Math.round(clock.getMillisol());
		// Note : sometimes getMillisol() skips, does Math.round() help ?
		if (checkTime % MILLISOLS_PER_UPDATE == 0) {
			//System.out.println("at " + checkTime + " remainder is " + checkTime % MILLISOLS_PER_UPDATE);
			monitorAir(newList, num);
		}
		
		// check for the passing of each day
		//int solElapsed = clock.getSolElapsedFromStart();
		//if (solElapsed != solCache) {
		//	monitorAir(newList, num);
		//}
		
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
	 * @param time interval in millisols
	 * @param buildings a list of buildings
	 * @param num numbers of buildings
	 */
	public void calculateGasExchange(double time, List<Building> buildings, int num) {

		double o2 = o2Consumed * time;
		double cO2 = cO2Expelled * time;
		double moisture = moistureExpelled * time;
		
		// Part 1 : calculate for each gas the partial pressure and # of moles
		for (Building b: buildings) {
			int id = b.getInhabitableID();
			int numPeople = b.getInhabitants().size();
			
			double t = C_TO_K  + b.getCurrentTemperature();
			
			o2 = numPeople * o2;
			cO2 = numPeople * cO2;
			moisture = numPeople * moisture;
			
			for (int gas = 0; gas< numGases; gas++) {

				double molecularMass = getMolecularMass(gas);

				double m = mass [gas][id];
				double nm = numMoles [gas][id];

				if (gas == 0) {
					m += cO2;
				}
				else if (gas == 3) {
					m -= o2;
				}
				else if (gas == 4) {
					m += moisture;
				}
				
				// Divide by molecular mass to convert mass to # of moles 
				// note the kg/mole are as indicated as each gas have different amu
				
				nm = m / molecularMass;
				
				temperature [gas][id] = t;	
				
				partialPressure [gas][id] = nm * R_GAS_CONSTANT * t / fixedVolume [id];
				mass [gas][id] = m ;
				numMoles [gas][id] = nm;
				
			}
		}

		// Part 2
		// calculate for each building the total pressure, total # of moles and percentage of composition
		for (int id = 0; id< num; id++) {
			
			double p = 0, nm = 0, m = 0;
			// calculate for each gas the total pressure and moles
			for (int gas = 0; gas < numGases; gas++) {
				
				p += partialPressure [gas][id];
				nm += numMoles [gas][id];
				m += mass [gas][id];

			}

			totalPressure [id] = p;
			totalMoles [id] = nm;
			totalMass [id] = m;
			
			//System.out.println(buildingManager.getBuilding(id).getNickName() + " has a total " + Math.round(totalMass[id]*100D)/100D + " kg of gas");
		}

		
				
		// Part 3
		// calculate for each building the percent composition
		for (int id = 0; id < num; id++) {
			// calculate for each gas the % composition
			for (int gas = 0; gas < numGases; gas++) {
				percent [gas][id] = partialPressure [gas][id] / totalPressure [id] * 100D;
				
			}
		}

/*
		// if time < 1 millisols, may skip calling adjustThermalControl() for several cycle to reduce CPU utilization.
		if (masterClock == null)
			masterClock = Simulation.instance().getMasterClock();
		if (clock == null)
			clock = masterClock.getMarsClock();
		int oneTenthmillisols =  (int) (clock.getMillisol() * 10);
		//System.out.println(" oneTenthmillisols : " + oneTenthmillisols);
		if (oneTenthmillisols % ONE_TENTH_MILLISOLS_PER_UPDATE == 0) {
			//emissivity = emissivityMap.get( (int) (oneTenthmillisols/10D) );
		}
*/
		
	}
	
	/**
	 * Monitors air and add mass of gases below the threshold
	 * @param buildings a list of buildings
	 * @param num numbers of buildings
	 */
	public void monitorAir(List<Building> buildings, int num) {

		// PART 1 : 
		// check % of gas in each building
		// find the delta mass needed for each gas to go within the threshold	
		// calculate for each gas the new partial pressure, the mass and # of moles

		for (Building b: buildings) {
			int id = b.getInhabitableID();	
			double t = C_TO_K  + b.getCurrentTemperature();
			
			for (int gas = 0; gas < numGases; gas++) {
				double pt = percent [gas][id] ;
				
				double molecularMass = getMolecularMass(gas);

				//[0] = CO2
				//[1] = ARGON
				//[2] = N2
				//[3] = O2
				//[4] = H2O
				
				double d_percent = STANDARD_GAS_PERCENT[gas] - pt; // d_percent is +ve if not enough gas ; d_percent is -ve if too much gas is present 
				double part = d_percent/STANDARD_GAS_PERCENT[gas];
				// if this gas has BELOW 95% or ABOVE 105% the standard percentage of air composition
				if (part < LOWER_THRESHOLD_GAS_COMPOSITION
						|| part > UPPER_THRESHOLD_GAS_COMPOSITION) {
					//double d_pressure = totalPressure[id] * d_percent/100D; // d_pressure can be -ve
					//double d_nm = d_pressure /R_GAS_CONSTANT /t * fixedVolume [id]; // d_nm can be -ve
					double d_nm = totalMoles[id] * d_percent/100D;
					double d_mass = d_nm * molecularMass; // d_mass can be -ve; 
					
					//if (d_mass >= 0)
					//	d_mass = d_mass * 1.1D; //add or extract a little more to save the future effort

					AmountResource ar = getGasAR(gas);
					
					if (d_percent > 0)
						Storage.retrieveAnResource(d_mass, ar , b.getInventory(), true); 
					else {
						double amount = Math.abs(d_mass)*GAS_CAPTURE_EFFICIENCY;
						if (amount > 0)		
							Storage.storeAnResource(amount, ar , b.getInventory(), "::monitorAir"); 
					}
						
					double new_m = 0;
					double new_nm = 0;
					
					new_m = mass [gas][id] + d_mass;
					new_nm = new_m / molecularMass;
					
					temperature [gas][id] = t;	
					
					partialPressure [gas][id] = new_nm * R_GAS_CONSTANT * t / fixedVolume [id];
					mass [gas][id] = new_m ;
					numMoles [gas][id] = new_nm;
				}
			}
		}
		

		// Part 2
		// calculate for each building the total pressure, total # of moles and percentage of composition
		for (int id = 0; id< num; id++) {
			
			double p = 0, nm = 0, m = 0;
			// calculate for each gas the total pressure and moles
			for (int gas = 0; gas < numGases; gas++) {
				
				p += partialPressure [gas][id];
				nm += numMoles [gas][id];
				m += mass [gas][id];

			}

			totalPressure [id] = p;
			totalMoles [id] = nm;
			totalMass [id] = m;
			
		}
		
		// Part 3
		// calculate for each building the percent composition
		for (int id = 0; id < num; id++) {
			// calculate for each gas the % composition
			for (int gas = 0; gas < numGases; gas++) {
				percent [gas][id] = partialPressure [gas][id] / totalPressure [id] * 100D;
				
			}
		}
	}
	
	/**
	 * Obtain the Amount Resource instance of a given gas
	 * @param gas
	 * @return
	 */
	public AmountResource getGasAR(int gas) {
		AmountResource ar = null;
		if (gas == 0)
			ar = ResourceUtil.carbonDioxideAR;
		else if (gas == 1)
			ar = ResourceUtil.argonAR;
		else if (gas == 2)
			ar = ResourceUtil.nitrogenAR;
		else if (gas == 3)
			ar = ResourceUtil.oxygenAR;
		else if (gas == 4)
			ar = ResourceUtil.waterAR;

		return ar;
	}
	
	/**
	 * Expands the array to keep track of the gases in the newly added buildings
	 * @param buildings a list of buildings
	 * @param numID numbers of buildings
	 */
	public void addAirNew(List<Building> buildings, int numID) {
		
		int diff = 	numID - numIDsCache; 		
	
		if (numID != numIDsCache && diff > 0) {
			//if a building is added from a settlement	

			numIDsCache = numID;
			//System.out.println("numBuildings : " + numBuildings + "   numBuildingsCache : " + numBuildingsCache);
			//System.out.println("percentComposition.length : " + percentComposition.length);
			//System.out.println("partialPressure[0].length : " + partialPressure[0].length);
			// increase the size of the vectors...
			// initialize the new building with default values;
	
			double [] new_volume = Arrays.copyOf(fixedVolume, fixedVolume.length + diff);
			
			double [] new_totalPressure = Arrays.copyOf(totalPressure, totalPressure.length + diff);
			double [] new_totalMoles = Arrays.copyOf(totalMoles, totalMoles.length + diff);
			double [] new_totalMass = Arrays.copyOf(totalMass, totalMass.length + diff);
			
			double [][] new_temperature = createGasArray(temperature, numID);
			double [][] new_percent = createGasArray(percent, numID);

			double [][] new_partialPressure = createGasArray(partialPressure, numID);
			double [][] new_numMoles = createGasArray(numMoles, numID);
			double [][] new_mass = createGasArray(mass, numID);

			//double [][] new_percent = Arrays.copyOf(percentComposition, percentComposition[0].length + diff);
			//double [][] new_partialPressure = Arrays.copyOf(partialPressure, partialPressure[0].length + diff);
			//double [][] new_temperature = Arrays.copyOf(temperature, temperature[0].length + diff);
			//double [][] new_numMoles = Arrays.copyOf(numMoles, numMoles[0].length + diff);
/*			// Use Java 8 stream to create new 2D arrays with more columns.
			double [][] new_percent = Arrays.stream(percentComposition)
		             .map((double[] row) -> row.clone())
		             .toArray((int length) -> new double[length + diff][]);
*/

			//System.out.println("new_partialPressure[0].length : " + new_partialPressure[0].length);

			for (int id = numID ; id< numID; id++) {
				//System.out.println("j : " + j);
				new_totalPressure [id] = 1.0;

				new_partialPressure [0][id] = CO2_PARTIAL_PRESSURE;
				new_partialPressure [1][id] = ARGON_PARTIAL_PRESSURE;
				new_partialPressure [2][id] = N2_PARTIAL_PRESSURE;
				new_partialPressure [3][id] = O2_PARTIAL_PRESSURE;
				new_partialPressure [4][id] = H2O_PARTIAL_PRESSURE;
				
				//new_percent [0][id] = CO2_PERCENT;
				//new_percent [1][id] = ARGON_PERCENT;
				//new_percent [2][id] = N2_PERCENT;
				//new_percent [3][id] = O2_PERCENT;
				//new_percent [4][id] = H2O_PERCENT;
			}

			List<Building> newList = new ArrayList<>();

			// Part 2 : calculate # of moles and mass
			// Assembled a list of new buildings
			for (Building b: buildings) {
				int id = b.getInhabitableID();
				if (id >= numIDsCache)
					newList.add(b);
			}
			

			//for (int id = 0; id< num; id++) {
			for (Building b: newList) {
				int id = b.getInhabitableID();
				
				double t = C_TO_K  + b.getCurrentTemperature();
				double sum_nm = 0, sum_p = 0, sum_mass = 0;
				
				// calculate for each gas the new volume, # of moles and total # of moles
				for (int gas = 0; gas < numGases; gas++) {
					
					double molecularMass = getMolecularMass(gas);
					
					double vol = b.getWidth() * b.getLength() * HEIGHT * 1000D;
					double p = new_partialPressure [gas][id];
					double nm = p * vol / R_GAS_CONSTANT / t;
					double m = molecularMass * nm;
					
					new_volume [id] = vol;
					new_temperature [gas][id] = t ;
					
					new_numMoles [gas][id] = nm;
					new_mass [gas][id] = m;
					new_partialPressure [gas][id] = p;
									
					sum_nm += nm;
					sum_p += p;
					sum_mass += m;
					
				}

				new_totalMoles [id] = sum_nm;
				new_totalPressure [id] = sum_p;
				new_totalMass [id] = sum_mass;
				
			}
			
			// Part 3 : calculate for each building the percent composition
			for (int id = 0; id< numID; id++) {
				// calculate for each gas the % composition
				for (int gas= 0; gas< numGases; gas++) {
					new_percent [gas][id] = new_partialPressure [gas][id] / new_totalPressure [id] * 100D;

				}
			}
			
			percent = new_percent;			
			fixedVolume = new_volume;
			temperature = new_temperature;
			
			partialPressure = new_partialPressure;

			numMoles = new_numMoles;
			mass = new_mass;
			
			totalPressure = new_totalPressure;
			totalMoles = new_totalMoles;
			totalMass = new_totalMass;
			
			/*
			double [][] new_percentByVolume = new double[numGases][numBuildings];
			double [] new_volume = new double[numBuildings];
			double [][] new_partialPressure = new double[numGases][numBuildings];
			double [][] new_temperature = new double[numGases][numBuildings];
			double [][] new_numMoles = new double[numGases][numBuildings];
			double [] new_totalPressure = new double[numBuildings];
*/

			numIDsCache = numID;
		}


	}

	/**
	 * Creates a new array for gases and pad it with zero for the new building
	 * @param oldArray
	 * @param numBuildings
	 * @return new array
	 */
	public double [][] createGasArray(double [][] oldArray, int numBuildings) {
		double [][] newArray = new double[numGases][numBuildings];
		int size = oldArray[0].length;
		for (int j = 0; j< size; j++) {
			for (int i= 0; i< numGases; i++) {
			if (j < numIDsCache) {
				newArray[i][j] = oldArray[i][j];
				}
			else
				newArray[i][j] = 0;
			}
		}
		return newArray;
	}

	/**
	 * Pumps air to or extract air from a building with this id
	 * @param id inhabitable id of a building
	 * @param pump positive if pumped, negative if extracted
	 * @param b the building
	 */
	public void pumpOrRecaptureAir(int id, boolean pump, Building b) {
		double d_moles[] = new double[numGases];
		//double t = b.getCurrentTemperature();
		
		for (int gas = 0; gas < numGases; gas++) {
			//double pressure = getPartialPressure()[gas][id];
			//double t = getTemperature()[gas][id];
			// calculate moles on each gas
			d_moles[gas] = numMoles[gas][id] * AIRLOCK_VOLUME_IN_LITER / fixedVolume[id]; //pressure /  R_GAS_CONSTANT / t * AIRLOCK_VOLUME_IN_LITER;

			pumpOrExtractMoles(gas, id, d_moles[gas], pump, b);
		}
	}
	
	/**
	 * Pumps or extract numbers of moles of a certain gas to a building with this id
	 * @param gas the type of gas
	 * @param id inhabitable id of a building
	 * @param d_moles numbers of moles
	 * @param pump positive if pumped, negative if extracted
	 * @param b the building
	 */
	public void pumpOrExtractMoles(int gas, int id, double d_moles, boolean pump, Building b) {
		double old_moles = numMoles[gas][id];
		double old_mass = mass[gas][id];		
		double new_moles = 0;
		double new_mass = 0;
		double molecularMass = getMolecularMass(gas);
		
		double d_mass = molecularMass * d_moles;
		
		AmountResource ar = getGasAR(gas);

		//System.out.println(" # moles of [" + gas + "] to pump or recapture : " + d_moles);
		//System.out.println(" mass of [" + gas + "] to pump or recapture : " + d_mass);
		if (pump) {
			new_moles = old_moles + d_moles;
			new_mass = old_mass + d_mass;
			Storage.retrieveAnResource(d_mass, ar , b.getInventory(), true); 
		}
		else { // recapture
			new_moles = old_moles - d_moles;
			new_mass = old_mass - d_mass;
			if (d_mass > 0)
				Storage.storeAnResource(d_mass * GAS_CAPTURE_EFFICIENCY, ar , b.getInventory(), "::pumpOrExtractMoles"); 
			if (new_moles < 0)
				new_moles = 0;
			if (new_mass < 0)
				new_mass = 0;
			
		}
			
		numMoles[gas][id] = new_moles;
		mass[gas][id] = new_mass;
		
	}
	
	public double [][] getPercentComposition() {
		return percent;
	}

	public double [][] getPartialPressure() {
		return partialPressure;
	}
	
	public double [][] getTemperature() {
		return temperature;
	}
	
	public double [][] getNumMoles() {
		return numMoles;
	}

	public double [][] getMass() {
		return mass;
	}

	public double [] getTotalMass() {
		return totalMass;
	}
	
	public double [] getTotalPressure() {
		return totalPressure;
	}

	public double [] getTotalMoles() {
		return totalMoles;
	}

	public double [] getTotalVolume() {
		return fixedVolume;
	}

	
	public void destroy() {
		buildingManager = null;
	 	thermalSystem = null;
		weather = null;
		location = null;
		emissivityMap.clear();
		emissivityMap = null;
		settlement = null;
		masterClock = null;
		clock = null;
		surfaceFeatures = null;
		personConfig = null;
		buildings = null;
		//fmt = null;
	}

}