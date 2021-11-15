/*
 * Mars Simulation Project
 * EVASuit.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.SystemType;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

/**
 * The EVASuit class represents an EVA suit which provides life support for a
 * person during a EVA operation.
 *
 * According to https://en.wikipedia.org/wiki/Space_suit,
 *
 * Generally speaking, in order to supply enough oxygen for respiration, a space suit
 * using pure oxygen should have a pressure of about
 *
 * (A) 32.4 kPa (240 Torr; 4.7 psi),
 *
 *     which is equal to the 20.7 kPa (160 Torr; 3.0 psi) partial pressure of oxygen
 *     in the Earth's atmosphere at sea level,
 *
 * (B) plus 5.31 kPa (40 Torr; 0.77 psi) CO2 and 6.28 kPa (47 Torr; 0.91 psi) water vapor
 *     pressure,
 *
 * both of which must be subtracted from the alveolar pressure to get alveolar oxygen
 * partial pressure in 100% oxygen atmospheres, by the alveolar gas equation.
 *
 * According to https://en.wikipedia.org/wiki/Mars_suit#Breathing for a Mars suit, the
 * absolute minimum safe O2 requirement is a partial pressure of 11.94 kPa (1.732 psi)
 *
 * In contrast, The Russian Orlan spacesuit system operates at 40.0 kPa (5.8 psia).
 * On the other hand, the U.S. EMU system operates at 29.6 kPa (4.3 psia) of oxygen,
 * with traces of CO2 and water vapor.
 *
 * The Russian EVA preparation protocol includes a 30-minute oxygen pre-breathe in
 * the Orlan spacesuit at a pressure of 73 kPa (10.6 psia) to partially wash out
 * nitrogen from crew members’ blood and tissues (Barer and Filipenkov, 1994)
 *
 * See https://msis.jsc.nasa.gov/sections/section14.htm for more design and
 * operational considerations.
 *
 */
public class EVASuit extends Equipment
	implements LifeSupportInterface, Serializable, Malfunctionable, ResourceHolder, ItemHolder,
				Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(EVASuit.class.getName());

	// Static members
	public static String TYPE = SystemType.EVA_SUIT.getName();

	public static String GOOD_TYPE = "eva";

	/** Total gas tank volume of EVA suit (Liter). */
	public static final double TOTAL_VOLUME = 3.9D;
	/** Oxygen capacity (kg.). */
	public static final double OXYGEN_CAPACITY = 1D;
	/** CO2 capacity (kg.). */
	private static final double CO2_CAPACITY = 1D;
	/** Water capacity (kg.). */
	public static final double WATER_CAPACITY = 4D;
	/** capacity (kg). */
	public static final double CAPACITY = OXYGEN_CAPACITY + CO2_CAPACITY + WATER_CAPACITY;
	/** Typical O2 air pressure (Pa) inside EVA suit is set to be 20.7 kPa. */
	private static final double NORMAL_AIR_PRESSURE = 17;// 20.7; // CompositionOfAir.SKYLAB_TOTAL_AIR_PRESSURE_kPA; // 101325D;
	/** Normal temperature (celsius). */
	private static final double NORMAL_TEMP = 25D;
	/** The wear lifetime value of 334 Sols (1/2 orbit). */
	private static final double WEAR_LIFETIME = 334_000;
	/** The maintenance time of 20 millisols. */
	private static final double MAINTENANCE_TIME = 20D;

	private static double min_o2_pressure;
	/** The full O2 partial pressure if at full tank. */
	private static double fullO2PartialPressure;
	/** The nominal mass of O2 required to maintain the nominal partial pressure of 20.7 kPa (3.003 psi)  */
	private static double massO2NominalLimit;
	/** The minimum mass of O2 required to maintain right above the safety limit of 11.94 kPa (1.732 psi)  */
	private static double massO2MinimumLimit;
	/** Unloaded mass of EVA suit (kg.). The combined total of the mass of all parts. */
	public static double emptyMass;
	/** The average amount of carbon dioxide expelled by a person on each millisol for high metabolic activities. */
	private static double co2Expelled;
	/** The average amount of oxygen breathed in by a person on each millisol for high metabolic activities. */
	private static double o2Consumed;
	/** The ratio of CO2 expelled to O2 breathed in. */
	private static double ratioCO2ToO2;

	// Data members
	/** The equipment's malfunction manager. */
	private MalfunctionManager malfunctionManager;
	/** The MicroInventory instance. */
	private MicroInventory microInventory;

	static {

		 for (String p: ItemResourceUtil.EVASUIT_PARTS) {
			 emptyMass += ItemResourceUtil.findItemResource(p).getMassPerItem();
		 }

		 PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
		 o2Consumed = personConfig.getHighO2ConsumptionRate() / 1000D;
		 co2Expelled = personConfig.getCO2ExpelledRate() / 1000D;
		 ratioCO2ToO2 = co2Expelled / o2Consumed;

		 min_o2_pressure = personConfig.getMinSuitO2Pressure();

		 fullO2PartialPressure = CompositionOfAir.KPA_PER_ATM * OXYGEN_CAPACITY
				 / CompositionOfAir.O2_MOLAR_MASS * CompositionOfAir.R_GAS_CONSTANT / TOTAL_VOLUME;

		 massO2MinimumLimit = min_o2_pressure / fullO2PartialPressure * OXYGEN_CAPACITY;

		 massO2NominalLimit = NORMAL_AIR_PRESSURE / min_o2_pressure * massO2MinimumLimit;

		 logger.config(" EVA suit's unloaded weight : " + Math.round(emptyMass*1_000.0)/1_000.0 + " kg");
		 logger.config("      Total gas tank volume : " + Math.round(TOTAL_VOLUME*100.0)/100.0 + "L");
		 logger.config("               Full Tank O2 : " + Math.round(fullO2PartialPressure*100.0)/100.0 + " kPa -> "
				 		+ OXYGEN_CAPACITY + "    kg - Maximum tank pressure");
		 logger.config("                 Nomimal O2 : " + NORMAL_AIR_PRESSURE + "  kPa -> "
				 		+ Math.round(massO2NominalLimit*10_000.0)/10_000.0  + " kg - Suit target pressure");
		 logger.config("                 Minimum O2 : " + Math.round(min_o2_pressure*100.0)/100.0 + " kPa -> "
				 		+ Math.round(massO2MinimumLimit*10_000.0)/10_000.0  + " kg - Safety limit");

			// 66.61 kPa -> 1      kg (full tank O2 pressure)
			// 20.7  kPa -> 0.3107 kg
			// 17    kPa -> 0.2552 kg (target O2 pressure)
			// 11.94 kPa -> 0.1792 kg (min O2 pressure)
	}

	/**
	 * Constructor.
	 * @param name
	 *
	 * @param settlement the location of the EVA suit.
	 * @throws Exception if error creating EVASuit.
	 */
	public EVASuit(String name, Settlement settlement) {

		// Use Equipment constructor.
		super(name, TYPE, settlement);

		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		malfunctionManager.addScopeString(TYPE);
		malfunctionManager.addScopeString(FunctionType.LIFE_SUPPORT.getName());

		// Create MicroInventory instance
		microInventory = new MicroInventory(this);

		// Set the empty mass of the EVA suit in kg.
		setBaseMass(emptyMass);

		// Set capacity for each resource
		microInventory.setCapacity(OXYGEN_ID, OXYGEN_CAPACITY);
		microInventory.setCapacity(WATER_ID, WATER_CAPACITY);
		microInventory.setCapacity(CO2_ID, CO2_CAPACITY);
	}

	/**
     * Gets the total capacity of resource that this container can hold.
     * @return total capacity (kg).
     */
	@Override
    public double getCargoCapacity() {
        return CAPACITY;
    }

	/**
	 * Is this resource supported ?
	 *
	 * @param resource
	 * @return true if this resource is supported
	 */
	public boolean isResourceSupported(int resource) {
		return microInventory.isResourceSupported(resource);
	}

	/**
	 * Stores the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public double storeAmountResource(int resource, double quantity) {
		// Note: this method is different from
		// Equipment's storeAmountResource
		if (isResourceSupported(resource)) {
			return microInventory.storeAmountResource(resource, quantity);
		}
		else {
			String name = ResourceUtil.findAmountResourceName(resource);
			logger.warning(this, name + "Not allowed to be stored in "
					+ this + ".");
			return quantity;
		}
	}


	/**
	 * Gets the capacity of a particular amount resource
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		// Note: this method is different from
		// Equipment's getAmountResourceCapacity
		return microInventory.getCapacity(resource);
	}

	/**
	 * Gets the unit's malfunction manager.
	 *
	 * @return malfunction manager
	 */
	public MalfunctionManager getMalfunctionManager() {
		return malfunctionManager;
	}

	/**
	 * Returns true if life support is working properly and is not out of oxygen or
	 * water.
	 *
	 * @return true if life support is OK
	 * @throws Exception if error checking life support.
	 */
	public boolean lifeSupportCheck() {
		// boolean result = true;
		try {
			// With the minimum required O2 partial pressure of 11.94 kPa (1.732 psi), the minimum mass of O2 is 0.1792 kg
//			String name = getOwner().getName();
			if (getAmountResourceStored(OXYGEN_ID) <= massO2MinimumLimit) {
				logger.log(getOwner(), Level.WARNING, 30_000,
						"Less than 0.1792 kg oxygen (below the safety limit).");
				return false;
			}

			if (getAmountResourceStored(WATER_ID) <= 0D) {
				logger.log(getOwner(), Level.WARNING, 30_000,
						"Ran out of water.");
//				return false;
			}

			if (malfunctionManager.getOxygenFlowModifier() < 100D) {
				logger.log(getOwner(), Level.WARNING, 30_000,
						"Oxygen flow sensor malfunction.", null);
				return false;
			}
//			if (malfunctionManager.getWaterFlowModifier() < 100D) {
//				LogConsolidated.log(Level.INFO, 5000, sourceName,
//						"[" + this.getLocationTag().getLocale() + "] "
//								+ person.getName() + "'s " + this.getName() + "'s water flow sensor detected malfunction.", null);
//				return false;
//			}

			double p = getAirPressure();
			if (p > PhysicalCondition.MAXIMUM_AIR_PRESSURE || p <= min_o2_pressure) {
				logger.log(getOwner(), Level.WARNING, 30_000,
						"Detected improper o2 pressure at " + Math.round(p * 100.0D) / 100.0D + " kPa.");
				return false;
			}
			double t = getTemperature();
			if (t > NORMAL_TEMP + 15 || t < NORMAL_TEMP - 20) {
				logger.log(getOwner(), Level.WARNING, 30_000,
						"Detected improper temperature at " + Math.round(t * 100.0D) / 100.0D + " deg C");
				return false;
			}
		} catch (Exception e) {
          	logger.log(Level.SEVERE, "Cannot finish life support check: "+ e.getMessage());
		}

		return true;
	}

	/**
	 * Gets the number of people the life support can provide for.
	 *
	 * @return the capacity of the life support system.
	 */
	public int getLifeSupportCapacity() {
		return 1;
	}

	/**
	 * Gets oxygen from system.
	 *
	 * @param oxygenTaken the amount of oxygen requested from system (kg)
	 * @return the amount of oxygen actually received from system (kg)
	 * @throws Exception if error providing oxygen.
	 */
	public double provideOxygen(double oxygenTaken) {
		double oxygenLacking = 0;

		// NOTE: Should we assume breathing in pure oxygen or trimix and heliox
		// http://www.proscubadiver.net/padi-course-director-joey-ridge/helium-and-diving/
		// May pressurize the suit to 1/3 of atmospheric pressure, per NASA aboard on
		// the ISS

		oxygenLacking = retrieveAmountResource(OXYGEN_ID, oxygenTaken);

		double carbonDioxideProvided = ratioCO2ToO2 * (oxygenTaken - oxygenLacking);
		storeAmountResource(CO2_ID, carbonDioxideProvided);

		return oxygenTaken - oxygenLacking;// * (malfunctionManager.getOxygenFlowModifier() / 100D);
	}

	/**
	 * Gets water from the system.
	 *
	 * @param waterTaken the amount of water requested from system (kg)
	 * @return the amount of water actually received from system (kg)
	 * @throws Exception if error providing water.
	 */
	public double provideWater(double waterTaken) {
//		double waterLeft = getAmountResourceStored(WATER_ID);
		double lacking = 0;

//		if (waterTaken > waterLeft) {
//			waterTaken = waterLeft;
//		}
//		if (waterTaken > 0)
			lacking = retrieveAmountResource(WATER_ID, waterTaken);

		return waterTaken - lacking;// * (malfunctionManager.getWaterFlowModifier() / 100D);
	}

	/**
	 * Gets the air pressure of the life support system.
	 *
	 * @return air pressure (Pa)
	 */
	public double getAirPressure() {
		// Based on some pre-calculation,
		// In a 3.9 liter system, 1 kg of O2 can create 66.61118 kPa partial pressure
		// e.g. To supply a partial oxygen pressure of 20.7 kPa, one needs at least 0.3107 kg O2
		// With the minimum required O2 partial pressure of 11.94 kPa (1.732 psi), the minimum mass of O2 is 0.1792 kg
		// Note : our target o2 partial pressure is 17 kPa (not 20.7 kPa), the targeted mass of O2 is 0.2552 kg

		// 66.61 kPa -> 1      kg (full tank O2 pressure)
		// 20.7  kPa -> 0.3107 kg
		// 17    kPa -> 0.2552 kg (target O2 pressure)
		// 11.94 kPa -> 0.1792 kg (min O2 pressure)

		double oxygenLeft = getAmountResourceStored(OXYGEN_ID);
		// Assuming that we can maintain a constant oxygen partial pressure unless it falls below massO2NominalLimit
		if (oxygenLeft < massO2NominalLimit) {
			double remainingMass = oxygenLeft;
			double pp = CompositionOfAir.KPA_PER_ATM * remainingMass / CompositionOfAir.O2_MOLAR_MASS
					* CompositionOfAir.R_GAS_CONSTANT / TOTAL_VOLUME;
			logger.log(this, getOwner(), Level.WARNING, 30_000,
					" got " + Math.round(oxygenLeft*100.0)/100.0
						+ " kg O2 left at partial pressure of " + Math.round(pp*100.0)/100.0 + " kPa.");
			return pp;
		}
//		Note: the outside ambient air pressure is weather.getAirPressure(getCoordinates());
		return NORMAL_AIR_PRESSURE;// * (malfunctionManager.getAirPressureModifier() / 100D);
	}

	/**
	 * Gets the temperature of the life support system.
	 *
	 * @return temperature (degrees C)
	 */
	public double getTemperature() {
		return NORMAL_TEMP;// * (malfunctionManager.getTemperatureModifier() / 100D);
//		double ambient = weather.getTemperature(getCoordinates());

//		if (result < ambient) {
			// if outside temperature is higher than the EVA normally allowed temp
			// Note: Add codes to simulate the use of cooling coil to turn on cooler to
			// reduce the temperature inside the EVA suit.
			// if cooling coil malfunction, then return ambient only
//			return result;
//		}
	}

	/**
	 * Time passing for EVA suit.
	 *
	 * @param pulse the amount of clock pulse passing (in millisols)
	 * @throws Exception if error during time.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// EVA Suit doesn't check the pulse value like other units
		// because it is not called consistently every pulse. It is only
		// called when in use by a Person.
		Unit container = getContainerUnit();
		if (container.getUnitType() == UnitType.PERSON
			&&!((Person) container).getPhysicalCondition().isDead()) {
				malfunctionManager.activeTimePassing(pulse.getElapsed());
		}

		malfunctionManager.timePassing(pulse);

		return true;
	}

	/**
	 * Gets a list of people affected by this equipment
	 * @return Collection<Person>
	 */
	@Override
	public Collection<Person> getAffectedPeople() {
		return super.getAffectedPeople();
	}

	@Override
	public String getNickName() {
		return getName();
	}

	@Override
	public String getImmediateLocation() {
		return getLocationTag().getImmediateLocation();
	}

	@Override
	public String getLocale() {
		return getLocationTag().getLocale();
	}

	@Override
	public Building getBuildingLocation() {
		return getContainerUnit().getBuildingLocation();
	}

	@Override
	public Settlement getAssociatedSettlement() {
		Settlement s = getContainerUnit().getAssociatedSettlement();
		if (s == null) s = super.getAssociatedSettlement();
		return s;
	}

	/**
	 * Gets the owner of this suit
	 *
	 * @return owner
	 */
	private Person getOwner() {
		return (Person)getLastOwner();
	}

	/**
	 * Return the parts that mau normally fail on a EVA Suit
	 * @return
	 */
	public static Map<Integer, Double> getNormalRepairPart() {
		return MalfunctionFactory.getRepairPartProbabilities(Set.of(TYPE));
	}

	/**
	 * Load the resources need from a source. Also unload any waste
	 * @param source Source of resources
	 * @return The %age full of the suit
	 */
	public double loadResources(EquipmentOwner source) {
		unloadWaste(source);
		loadResource(source, OXYGEN_ID);
		loadResource(source, WATER_ID);

		return getFullness();
	}

	/**
	 * Fully load a resource into the EVASuit
	 * @param source
	 * @param resourceId
	 * @return Suit is fully loaded with resource
	 */
	private boolean loadResource(ResourceHolder source, int resourceId) {
		double needed = getAmountResourceRemainingCapacity(resourceId);
		if (needed > 0D) {
			double shortfall = source.retrieveAmountResource(resourceId, needed);
			double taken = needed - shortfall;
			if (taken > 0) {
				storeAmountResource(resourceId, taken);
			}
		}
		return needed <= 0D;
	}

	/**
	 * Unload any waste products to the holder
	 * @param newSuitOwner
	 */
	public void unloadWaste(EquipmentOwner holder) {
		double co2 = getAmountResourceStored(CO2_ID);
		if (co2 > 0) {
			retrieveAmountResource(CO2_ID, co2);
			holder.storeAmountResource(CO2_ID, co2);
		}
	}

	/**
	 * How fully loaded is the Suit; lowest of water and oxygen
	 * @return Percentage of lowest resource
	 */
	public double getFullness() {
		double o2Loaded = getAmountResourceStored(OXYGEN_ID)/OXYGEN_CAPACITY;
		double waterLoaded = getAmountResourceStored(WATER_ID)/WATER_CAPACITY;

		return Math.min(o2Loaded, waterLoaded);
	}


	@Override
	public int getItemResourceStored(int resource) {
		return microInventory.getItemResourceStored(resource);
	}

	@Override
	public int getItemResourceRemainingQuantity(int resource) {
		return microInventory.getItemResourceRemainingQuantity(resource);
	}

	@Override
	public int storeItemResource(int resource, int quantity) {
		return microInventory.storeItemResource(resource, quantity);
	}

	@Override
	public int retrieveItemResource(int resource, int quantity) {
		return microInventory.retrieveItemResource(resource, quantity);
	}

	@Override
	public Unit getHolder() {
		return this;
	}

	/**
	 * Gets a list of all stored item resources
	 *
	 * @return a list of resource ids
	 */
	public Set<Integer> getItemResourceIDs() {
		return microInventory.getItemsStored();
	}

	@Override
	public double getAmountResourceStored(int resource) {
		return microInventory.getAmountResourceStored(resource);
	}

	/**
	 * Retrieves the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public double retrieveAmountResource(int resource, double quantity) {
		if (isResourceSupported(resource)) {
			return microInventory.retrieveAmountResource(resource, quantity);
		}

		else {
			String name = ResourceUtil.findAmountResourceName(resource);
			logger.warning(this, "No such resource. Cannot retrieve "
					+ Math.round(quantity* 1_000.0)/1_000.0 + " kg "+ name + ".");
			return quantity;
		}
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceRemainingCapacity(int resource) {
		return microInventory.getAmountResourceRemainingCapacity(resource);
	}

	/**
	 * Gets a list of all stored amount resources
	 *
	 * @return a list of resource ids
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return microInventory.getResourcesStored();
	}

	/**
	 * Is this equipment empty ?
	 *
	 * @param brandNew true if it needs to be brand new
	 * @return
	 */
	public boolean isEmpty(boolean brandNew) {
		if (brandNew) {
			return (getLastOwnerID() == -1);
		}

		return microInventory.isEmpty();
	}

	/**
	 * Gets the total weight of the stored resources
	 *
	 * @return
	 */
	public double getStoredMass() {
		if (microInventory == null)
			// Note: needed when starting up
			return 0;
		return microInventory.getStoredMass();
	}

//	/**
//	 * Does this unit have this resource ?
//	 *
//	 * @param resource
//	 * @return
//	 */
//	public boolean hasResource(int resource) {
//		for (int id: getAmountResourceIDs()) {
//			if (id == resource)
//				return true;
//		}
//		return false;
//	}

	public void destroy() {
		malfunctionManager = null;
		microInventory = null;
	}

}