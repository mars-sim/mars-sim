/**
 * Mars Simulation Project
 * CompositionOfAir.java
 * @version 3.08 2015-12-29
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

	private static final double AIRLOCK_VOLUME = .012; // in cubic meters
	
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

	
	private static final double CO2_PARTIAL_PRESSURE = 0.000407;
	private static final double ARGON_PARTIAL_PRESSURE = 0.00934;
	private static final double N2_PARTIAL_PRESSURE = .78;
	private static final double O2_PARTIAL_PRESSURE = .20021;
	private static final double H2O_PARTIAL_PRESSURE = 0.01;
	
	public static final double CO2_MOLAR_MASS = 44.0095 /1000D; // [in kg/mol]
	public static final double ARGON_MOLAR_MASS = 39.948 /1000D; // [in kg/mol]
	public static final double N2_MOLAR_MASS = 28.02 /1000D; // [in kg/mol]
	public static final double O2_MOLAR_MASS = 32.00 /1000D; // [in kg/mol]
	public static final double H2O_MOLAR_MASS = 18.02 /1000D; // [in kg/mol] 
	
	public static final double CH4_MOLAR_MASS = 16.04276; // [in g/mol] 
	public static final double H2_MOLAR_MASS = 2.016; // [in g/mol] 
	
	private static final int ONE_TENTH_MILLISOLS_PER_UPDATE = 10 ;
	
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
	private int numCache;

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

	//DecimalFormat fmt = new DecimalFormat("#.####");

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
		
		cO2Expelled = o2Consumed *.9 /1000D; //~.9 kg per sol, divide by 1000 to convert to [kg/millisol] 
		
		moistureExpelled = .4/1000D; // ~400 ml through breathing per sol, divide by 1000 to convert to [kg/millisol] 
		
		// see https://micpohling.wordpress.com/2007/03/27/math-how-much-co2-is-emitted-by-human-on-earth-annually/	
		// https://www.quora.com/How-much-water-does-a-person-lose-in-a-day-through-breathing
		// Every day, we breath in about 14000L of air.
		// Assuming that the humidity of exhaled air is 100% and inhaled air is 20%,
		// Use the carrying capacity of 1kg of air to be 20g of water vapour,
		// This estimate gives 400ml of water lost per day
		// Thus, a person loses about 800ml of water per day, half through the skin
		// and half through respiration.

		buildings = buildingManager.getBuildingsWithLifeSupport();

		int num = buildings.size();
		numCache = num;

		// CO2, H2O, N2, O2, Ar2, He, CH4...
		// numGases = 5;

		percent = new double[numGases][num];
		partialPressure = new double[numGases][num];
		temperature = new double[numGases][num];
		numMoles = new double[numGases][num];
		mass = new double[numGases][num];
		
		fixedVolume = new double[num];
		totalPressure = new double[num];
		totalMoles = new double[num];
		totalMass = new double[num];
		//totalPercent = new double[num];
		
		// Part 1 : set up initial conditions at the start of sim
		for (int id = 0; id< num; id++) {

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
		for (int id = 0; id< num; id++) {
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
		int num = buildings.size();
		
		// if adding or subtracting a building form the settlement
		addSubtractBuilding(newList, num);
		
		// For each time interval
		calculateGasExchange(time, newList, num);
		
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
				//o2Consumed = o2Consumed/31.9988 * 1000D; // [# of moles/millisol]
				//cO2Expelled = cO2Expelled/44.01 * 1000D; // [# of moles/millisol]
				//moistureExpelled = moistureExpelled/18.02 * 1000D; // [# of moles/millisol]
				
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
	
	
	public void addSubtractBuilding(List<Building> buildings, int num) {
		
		int diff = 	num - numCache; 		
	
		if (num != numCache && diff > 0) {
			//if a building is added from a settlement	

			numCache = num;
			//System.out.println("numBuildings : " + numBuildings + "   numBuildingsCache : " + numBuildingsCache);
			//System.out.println("percentComposition.length : " + percentComposition.length);
			//System.out.println("partialPressure[0].length : " + partialPressure[0].length);
			// increase the size of the vectors...
			// initialize the new building with default values;
	
			double [] new_volume = Arrays.copyOf(fixedVolume, fixedVolume.length + diff);
			
			double [] new_totalPressure = Arrays.copyOf(totalPressure, totalPressure.length + diff);
			double [] new_totalMoles = Arrays.copyOf(totalMoles, totalMoles.length + diff);
			double [] new_totalMass = Arrays.copyOf(totalMass, totalMass.length + diff);
			
			double [][] new_temperature = createANewArray(temperature, num);
			double [][] new_percent = createANewArray(percent, num);

			double [][] new_partialPressure = createANewArray(partialPressure, num);
			double [][] new_numMoles = createANewArray(numMoles, num);
			double [][] new_mass = createANewArray(mass, num);

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

			for (int id = numCache ; id< num; id++) {
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
				if (id >= numCache)
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
			for (int id = 0; id< num; id++) {
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

			numCache = num;
		}


	}

	public double [][] createANewArray(double [][] array, int numBuildings) {
		double [][] result = new double[numGases][numBuildings];

		for (int j = 0; j< numBuildings; j++) {
			for (int i= 0; i< numGases; i++) {
			if (j < numCache) {
				result[i][j] = array[i][j];
				}
			else
				result[i][j] = 0;
			}
		}
		return result;
	}

	public void pumpOrExtractAir(int id, boolean pump) {
		double moles_to_extract[] = new double[numGases];
					
		for (int gas = 0; gas < numGases; gas++) {
			double pressure = getPartialPressure()[gas][id];
			double t = getTemperature()[gas][id];
			// calculate moles on each gas
			moles_to_extract[gas] = pressure /  R_GAS_CONSTANT / t * AIRLOCK_VOLUME;
			pumpOrExtractMoles(gas, id, moles_to_extract[gas], pump);
		}

	}
	
	
	public void pumpOrExtractMoles(int gas, int id, double m, boolean pump) {
		double old_moles = getNumMoles()[gas][id];
		double new_moles = 0;
		if (pump)
			new_moles = old_moles + m;
		else {
			new_moles = old_moles - m;
			if (new_moles < 0)
				new_moles = 0;
		}
		numMoles[gas][id] = new_moles;
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