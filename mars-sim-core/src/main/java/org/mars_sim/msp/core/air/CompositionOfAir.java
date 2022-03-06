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

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The CompositionOfAir class accounts for the composition of air of each
 * building in a settlement.
 */
public class CompositionOfAir implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final double C_TO_K = 273.15;

	static final double GAS_CAPTURE_EFFICIENCY = .95D;

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

	private static final int MILLISOLS_PER_UPDATE = 2;
	private static final double CALCULATE_FREQUENCY = 2D;

	static final double R_GAS_CONSTANT = 0.082057338; // [ in L atm K^−1 mol^−1 ]
	// alternatively, R_GAS_CONSTANT = 8.3144598 m^3 Pa K^−1 mol^−1
	// see https://en.wikipedia.org/wiki/Gas_constant


	// Data members
	private Map<Integer, AirComposition> indoorBuildings = new HashMap<>();

	/** The settlement ID */
	private int settlementID;

	/** The time accumulated [in millisols] for each crop update call. */
	private double accumulatedTime = RandomUtil.getRandomDouble(0, 1.0);

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
	private static UnitManager unitManager = sim.getUnitManager();

	/**
	 * Constructor.
	 *
	 * @param building the building this function is for.
	 * @throws BuildingException if error in constructing function.
	 */
	public CompositionOfAir(Settlement settlement) {

		settlementID = settlement.getIdentifier();

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
	
			for (Building b : buildings) {
	
				double tt = b.getCurrentTemperature();
				AirComposition air = indoorBuildings.get(b.getIdentifier());

				if (tt > -40 && tt < 40 && air != null) {

					int numPeople = b.getNumPeople();

					double t = C_TO_K + tt;

					air.calcPersonImpact(t, numPeople,  accumulatedTime);
				}
			}

			accumulatedTime = 0;
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
	 * Reloads instances after loading from a saved sim
	 *
	 * @param pc {@link PersonConfig}
	 * @param u {@link UnitManager}
	 */
	public static void initializeInstances(PersonConfig pc, UnitManager u) {
		AirComposition.initializeInstances(pc);
		unitManager = u;
	}


	/**
	 * Get the total air mass for a building
	 */
    public double getTotalMass(Building b) {
        return indoorBuildings.get(b.getIdentifier()).getTotalMass();
    }


    public double getTotalPressure(Building b) {
        return indoorBuildings.get(b.getIdentifier()).getTotalPressure();
    }

    public AirComposition.GasDetails getGasDetails(Building b, int gasId) {
        return indoorBuildings.get(b.getIdentifier()).getGas(gasId);
    }
}
