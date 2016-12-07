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
	private static final double C_TO_K = 273.15;
	private static final int numGases = 5;
	private static final int HEIGHT = 3; // assume an uniform height of 3m in all buildings
	
	
	private static final int ONE_TENTH_MILLISOLS_PER_UPDATE = 10 ;
    public static final double kPASCAL_PER_ATM = 1D/0.00986923267 ; // 1 kilopascal = 0.00986923267 atm
    // The standard atmosphere (symbol: atm) is a unit of pressure equal to 101325
    private static final double R_GAS_CONSTANT = 0.08206; //R = 0.08206 L atm K−1 mol−1
    //alternatively, R_GAS_CONSTANT = 8.31441  m3 Pa K−1 mol−1

    private double o2Consumed, cO2Expelled, moistureExpelled;

	private double dryAirDensity = 1.275D; // breath-able air in [kg/m3]

	// Assume using Earth's atmospheric pressure at sea level, 14.7 psi, or ~ 1 bar, for the habitat

	// Note : Mars' outside atmosphere is ~6 to 10 millibars (or .0882 to 0.147 psi) , < 1% that of Earth's. 
	
    // 1 kilopascal = 0.00986923267 atm
	// 1 cubic ft = L * 0.035315
    // A full scale pressurized Mars rover prototype may have an airlock volume of 5.7 m^3
	// Molar mass of CO2 = 44.0095 g/mol
    // average density of air : 0.020 kg/m3


	// Data members
	private int numBuildingsCache;

	private double [][] percentComposition;
	private double [] volume;
	private double [] totalPressure;
	private double [] totalMoles;
	private double [][] partialPressure;
	private double [][] temperature;
	private double [][] numMoles;

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

	DecimalFormat fmt = new DecimalFormat("#.####");

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
		o2Consumed = personConfig.getNominalO2ConsumptionRate() /1000D; // [kg/millisol]
		cO2Expelled = o2Consumed *.9 /1000D; //[kg/millisol] or  ~.9 kg per sol
		// see https://micpohling.wordpress.com/2007/03/27/math-how-much-co2-is-emitted-by-human-on-earth-annually/
		moistureExpelled = .4/1000D; //[kg/millisol] or ~400 ml through breathing per sol
		// https://www.quora.com/How-much-water-does-a-person-lose-in-a-day-through-breathing
		// Every day, we breath in about 14000L of air.
		// Assuming that the humidity of exhaled air is 100% and inhaled air is 20%,
		// Use the carrying capacity of 1kg of air to be 20g of water vapour,
		// This estimate gives 400ml of water lost per day
		// Thus, a person loses about 800ml of water per day, half through the skin
		// and half through respiration.

		// Convert kg to # of moles
		// note the kg/mole are as indicated as each gas have different amu
		o2Consumed = o2Consumed/31.9988 * 1000D; // [# of moles/millisol]
		cO2Expelled = cO2Expelled/44.01 * 1000D; // [# of moles/millisol]
		moistureExpelled = moistureExpelled/18.02 * 1000D; // [# of moles/millisol]

		//thermalSystem = building.getBuildingManager().getSettlement().getThermalSystem();
		//if (surfaceFeatures == null)
		//	surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();


		List<Building> buildings = buildingManager.getBuildingsWithLifeSupport();

/*
		while (k.hasNext()) {
			Building b = k.next();
			int id = buildingManager.getNextHabitableID();
			if (b.getHabitable_id() == -1)
				b.setHabitable_id(id);
		}
*/
		int numBuildings = buildings.size();
		numBuildingsCache = numBuildings;

		// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
		// numGases = 5;

		percentComposition = new double[numGases][numBuildings];
		volume = new double[numBuildings];
		partialPressure = new double[numGases][numBuildings];
		temperature = new double[numGases][numBuildings];
		numMoles = new double[numGases][numBuildings];
		totalPressure = new double[numBuildings];
		totalMoles = new double[numBuildings];

		// set up initial conditions at the start of sim
		for (int j = 0; j< numBuildings; j++) {

			totalPressure [j] = 1.0;

			partialPressure [0][j] = 0.0005;
			partialPressure [1][j] = .01;
			partialPressure [2][j] = .78;
			partialPressure [3][j] = .2;
			partialPressure [4][j] = .0095;

			percentComposition [0][j] = 0.05;
			percentComposition [1][j] = 1.0;
			percentComposition [2][j] = 78.0;
			percentComposition [3][j] = 20.0;
			percentComposition [4][j] = 0.95;
		}

		for (int i = 0; i < numGases; i++) {
			for (Building b: buildings) {
			//Iterator<Building> k = buildings.iterator();
			//while (k.hasNext()) {
			//	Building b = k.next();
				int id = b.getInhabitable_id();

				double t =  22.5 + C_TO_K ;
				temperature [i][id] = t;
				double vol = b.getWidth() * b.getLength() * HEIGHT * 1000;
				volume [id] = vol;
				numMoles [i][id] = partialPressure [i][id] * vol / R_GAS_CONSTANT / t;
				totalMoles [id] += numMoles [i][id];
				//System.out.println(" partialPressure [" + i + "][" + id + "] = " + partialPressure [i][id]);
				//System.out.println(" numMoles [" + i + "][" + id + "] = " + fmt.format(numMoles [i][id]));
				//System.out.println(" percentComposition [" + i + "][" + id + "] = " + fmt.format(percentComposition [i][id]));
			}
		}
	}

	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {
		List<Building> buildings = buildingManager.getBuildingsWithLifeSupport();

		int numBuildings = buildings.size();

		if (numBuildings != numBuildingsCache) {
			//System.out.println("numBuildings : " + numBuildings + "   numBuildingsCache : " + numBuildingsCache);
			//System.out.println("percentComposition.length : " + percentComposition.length);
			//System.out.println("partialPressure[0].length : " + partialPressure[0].length);
			// increase the size of the vectors...
			// initialize the new building with default values;
			int diff = 	numBuildings - numBuildingsCache;
			// TODO: should consider negative diff

			double [] new_volume = Arrays.copyOf(volume, volume.length + diff);
			double [] new_totalPressure = Arrays.copyOf(totalPressure, totalPressure.length + diff);
			double [] new_totalMoles = Arrays.copyOf(totalMoles, totalMoles.length + diff);

			double [][] new_percent = makeNewArray(percentComposition, numBuildings);
			double [][] new_partialPressure = makeNewArray(partialPressure, numBuildings);
			double [][] new_temperature = makeNewArray(temperature, numBuildings);
			double [][] new_numMoles = makeNewArray(numMoles, numBuildings);

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

			for (int j = numBuildingsCache ; j< numBuildings; j++) {
				//System.out.println("j : " + j);
				new_totalPressure [j] = 1.0;

				new_partialPressure [0][j] = 0.0005;
				new_partialPressure [1][j] = .01;
				new_partialPressure [2][j] = .78;
				new_partialPressure [3][j] = .2;
				new_partialPressure [4][j] = .0095;

				new_percent [0][j] = 0.05;
				new_percent [1][j] = 1.0;
				new_percent [2][j] = 78.0;
				new_percent [3][j] = 20.0;
				new_percent [4][j] = 0.95;
			}

			List<Building> newBuildings = new ArrayList<>();

			// Assembled a list of new buildings
			for (Building b: buildings) {
			//Iterator<Building> k = buildings.iterator();
			//while (k.hasNext()) {
			//	Building b = k.next();
				int id = b.getInhabitable_id();
				if (id >= numBuildingsCache)
					newBuildings.add(b);
			}

			for (Building b: newBuildings) {
			//Iterator<Building> kk = newBuildings.iterator();
			//while (kk.hasNext()) {
			//	Building b = kk.next();
				int id = b.getInhabitable_id();

				for (int i=0; i<numGases; i++) {
					new_temperature [i][id] = 22.5 + C_TO_K ;
					double vol = b.getWidth() * b.getLength() * 2.5 * 1000;
					new_volume [id] = vol;
					new_numMoles [i][id] = new_partialPressure [i][id] * vol / R_GAS_CONSTANT / new_temperature [i][id];
					new_totalMoles [id] += new_numMoles [i][id];
				}

			}

			percentComposition = new_percent;
			volume = new_volume;
			partialPressure = new_partialPressure;
			temperature = new_temperature;
			numMoles = new_numMoles;
			totalPressure = new_totalPressure;
			totalMoles = new_totalMoles;
/*
			double [][] new_percentByVolume = new double[numGases][numBuildings];
			double [] new_volume = new double[numBuildings];
			double [][] new_partialPressure = new double[numGases][numBuildings];
			double [][] new_temperature = new double[numGases][numBuildings];
			double [][] new_numMoles = new double[numGases][numBuildings];
			double [] new_totalPressure = new double[numBuildings];
*/

			numBuildingsCache = numBuildings;
		}


		for (int i= 0; i< numGases; i++) {
			for (Building b: buildings) {
			//Iterator<Building> k = buildings.iterator();
			//while (k.hasNext()) {
			//	Building b = k.next();
				int id = b.getInhabitable_id();

				double t = C_TO_K  + b.getCurrentTemperature(); //b.getThermalGeneration().getHeating().getCurrentTemperature();
				//ThermalGeneration gen = (ThermalGeneration) b.getFunction(BuildingFunction.THERMAL_GENERATION);
				//double t = C_TO_K  + gen..getCurrentTemperature();

				temperature [i][id] = t;
				//System.out.println("t is " + t);

				int numPeople = b.getInhabitants().size();

				o2Consumed = numPeople * o2Consumed * time;
				cO2Expelled = numPeople * cO2Expelled * time;
				moistureExpelled = numPeople * moistureExpelled * time;

				// CO2, H2O, N2, O2, Others (Ar2, He, CH4...)
				// numGases = 5;

				double moles = numMoles [i][id];
				double delta_moles = 0;

				if (i == 0)
					delta_moles = cO2Expelled;
				else if (i == 1)
					delta_moles = moistureExpelled;
				else if (i == 3)
					delta_moles = -o2Consumed;

				moles = moles + delta_moles;
				partialPressure [i][id] = moles * R_GAS_CONSTANT * t / volume [id];
				numMoles [i][id] = moles;

			}
		}


		for (int j = 0; j< numBuildings; j++) {

			double p = 0, m = 0;

			for (int i= 0; i< numGases; i++) {
				p += partialPressure [i][j];
				totalPressure [j] = p;
				m += numMoles [i][j];
				totalMoles [j] = m;
			}

			//System.out.println(" totalPressure [" + j + "] = " + fmt.format(totalPressure [j]));
			//System.out.println(" totalMoles [" + j + "] = " + fmt.format(totalMoles [j]));
		}

		for (int j = 0; j< numBuildings; j++) {
			for (int i= 0; i< numGases; i++) {
				//percentComposition [i][j] = partialPressure [i][j] / totalPressure [j];
				percentComposition [i][j] = numMoles [i][j] / totalMoles [j] * 100D;
				//System.out.println(" percentComposition [" + i + "][" + j + "] = " + fmt.format(percentComposition [i][j]));
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

	public double [][] makeNewArray(double [][] array, int numBuildings) {
		double [][] result = new double[numGases][numBuildings];

		for (int j = 0; j< numBuildings; j++) {
			for (int i= 0; i< numGases; i++) {
			if (j < numBuildingsCache) {
				result[i][j] = array[i][j];
				}
			else
				result[i][j] = 0;
			}
		}
		return result;
	}

	public double [][] getPercentComposition() {
		return percentComposition;
	}

	public double [] getTotalPressure() {
		return totalPressure;
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
		fmt = null;
	}

}