/*
 * Mars Simulation Project
 * EVASuit.java
 * @date 2023-05-16
 * @author Scott Davis
 */
package com.mars_sim.core.equipment;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.LifeSupportInterface;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.air.AirComposition;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.SystemType;
import com.mars_sim.core.data.History;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionFactory;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.PartConfig;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.unit.UnitHolder;

/**
 * 
 * The EVASuit class represents an EVA suit which provides life support for a
 * person during a EVA operation.
 *
 * <p>According to https://en.wikipedia.org/wiki/Space_suit,
 *
 * <p>Generally speaking, in order to supply enough oxygen for respiration, a space suit
 * using pure oxygen should have a pressure of about a total of 32.4 kPa (240 Torr; 4.7 psi),
 *
 * <p> (A) which is equal to the 20.7 kPa (160 Torr; 3.0 psi) partial pressure of oxygen
 *     in the Earth's atmosphere at sea level,
 *
 * <p> (B) plus 5.31 kPa (40 Torr; 0.77 psi) CO2 and 
 * O
 * <p> (C) 6.28 kPa (47 Torr; 0.91 psi) water vapor pressure,
 * 
 * <p> both of which must be subtracted from the alveolar pressure to get alveolar oxygen
 * partial pressure in 100% oxygen atmospheres, by the alveolar gas equation.
 *
 * <p> According to https://en.wikipedia.org/wiki/Mars_suit#Breathing for a Mars suit, the
 * absolute minimum safe O2 requirement is a partial pressure of 11.94 kPa (1.732 psi)
 *
 * <p> In contrast, the Russian Orlan spacesuit system operates at 40.0 kPa (5.8 psia).
 * 
 * <p> On the other hand, the U.S. EMU system operates at 29.6 kPa (4.3 psia) of oxygen,
 * with traces of CO2 and water vapor.
 *
 * <p> The Russian EVA preparation protocol includes a 30-minute oxygen pre-breathe in
 * the Orlan spacesuit at a pressure of 73 kPa (10.6 psia) to partially wash out
 * nitrogen from crew members’ blood and tissues (Barer and Filipenkov, 1994)
 *
 * <p>See https://msis.jsc.nasa.gov/sections/section14.htm for more design and
 * operational considerations.
 *
 * <p>For an intro to Extravehicular Mobility Unit (EMU), see 
 * https://www.lpi.usra.edu/publications/reports/CB-979/cornell.pdf
 * `Extravehicular Activity Suit Systems Design How to Walk, Talk, and Breathe on Mars` 
 */
public class EVASuit extends Equipment
	implements LifeSupportInterface, Malfunctionable, ResourceHolder, ItemHolder,
				Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(EVASuit.class.getName());

	// Static members
	public static final String EVA = "EVA";
	
	/** String name of an EVA suit. */	
	public static final String TYPE = SystemType.EVA_SUIT.getName();

	public static final String DASHES = " -----------------------------------------------------------------------";
	
	/** Total gas tank volume of EVA suit (Liter). */
	public static final double TOTAL_VOLUME = 3.9D;
	/** Oxygen capacity (kg.). */
	public static final double OXYGEN_CAPACITY = 1D;
	/** CO2 capacity (kg.). */
	private static final double CO2_CAPACITY = 1D;
	/** Water capacity (kg.). */
	public static final double WATER_CAPACITY = 1D;
	/** capacity (kg). */
	public static final double CAPACITY = OXYGEN_CAPACITY + CO2_CAPACITY + WATER_CAPACITY;
	/** Nominal O2 air pressure (Pa) inside EVA suit is set to be 20.7 kPa. */
	private static final double NOMINAL_O2_PRESSURE = 20.7;
	/** Target O2 air pressure (Pa) inside EVA suit is set to be 17 kPa. */
	private static final double TARGET_O2_PRESSURE = 17;
	/** Normal temperature (celsius). */
	private static final double NORMAL_TEMP = 25D;
	/** The wear lifetime value is 1 orbit. */
	private static final double WEAR_LIFETIME = 668_000;
	/** The maintenance time in millisols. */
	private static final double MAINTENANCE_TIME = 200D;
	/** The ratio of CO2 expelled to O2 breathed in. */
	private static double gasRatio;
	/** The minimum required O2 partial pressure. At 11.94 kPa (1.732 psi)  */
	private static double minO2Pressure;
	/** The full O2 partial pressure if at full tank. */
	private static double fullO2PartialPressure;
	/** The nominal mass of O2 required to maintain the nominal partial pressure of 20.7 kPa (3.003 psi)  */
	private static double massO2NominalLimit;
	/** The target mass of O2 required to maintain the nominal partial pressure of 17 kPa (? psi)  */
	private static double massO2TargetLimit;
	/** The minimum mass of O2 required to maintain right above the safety limit of 11.94 kPa (1.732 psi)  */
	private static double massO2MinimumLimit;

	private static double usualMass = -1;

	// Data members
	/** The equipment's malfunction manager. */
	private MalfunctionManager malfunctionManager;
	/** The MicroInventory instance. */
	private MicroInventory microInventory;
	
	private History<UnitHolder> locnHistory;
	
	static {

		// Initialize the parts
		ItemResourceUtil.initEVASuit();
		
		PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
		
		double o2Consumed = personConfig.getHighO2ConsumptionRate();
		double co2Expelled = personConfig.getCO2ExpelledRate();
		
		gasRatio = co2Expelled / o2Consumed;
				
		minO2Pressure = personConfig.getMinSuitO2Pressure();
		
		fullO2PartialPressure = AirComposition.getOxygenPressure(OXYGEN_CAPACITY, TOTAL_VOLUME);
		
		massO2MinimumLimit = minO2Pressure / fullO2PartialPressure * OXYGEN_CAPACITY;
		
		massO2NominalLimit = NOMINAL_O2_PRESSURE / fullO2PartialPressure * OXYGEN_CAPACITY;
		
		massO2TargetLimit = TARGET_O2_PRESSURE / fullO2PartialPressure * OXYGEN_CAPACITY;
				
		logger.config(DASHES);
		
		logger.config("  Total Gas Tank Volume : " + Math.round(TOTAL_VOLUME * 100.0)/100.0 + " L");
		
		logger.config("           Full Tank O2 : " + Math.round(fullO2PartialPressure * 1000.0)/1000.0 
					+ " kPa -> " + OXYGEN_CAPACITY + "   kg - Maximum Tank Pressure");
		
		logger.config("             Nominal O2 : " + NOMINAL_O2_PRESSURE + "   kPa -> "
					+ Math.round(massO2NominalLimit * 1000.0)/1000.0  
					+ " kg - Suit Nominal Pressure");
		
		logger.config("              Target O2 : " + TARGET_O2_PRESSURE + "   kPa -> "
					+ Math.round(massO2TargetLimit * 1000.0)/1000.0  
					+ " kg - Suit Target Pressure");
		
		logger.config("             Minimum O2 : " + Math.round(minO2Pressure * 1000.0)/1000.0 + "  kPa -> "
					+ Math.round(massO2MinimumLimit * 1000.0)/1000.0  
					+ " kg - Safety Limit");
		
		logger.config(DASHES);
			
//		01-Adir-01:000.000(Config) EVASuit :  -----------------------------------------------------------------------
//		01-Adir-01:000.000(Config) EVASuit :   Total Gas Tank Volume : 3.9 L
//		01-Adir-01:000.000(Config) EVASuit :            Full Tank O2 : 66.622 kPa -> 1.0   kg - Maximum Tank Pressure
//		01-Adir-01:000.000(Config) EVASuit :              Nominal O2 : 20.7   kPa -> 0.311 kg - Suit Nominal Pressure
//		01-Adir-01:000.000(Config) EVASuit :               Target O2 : 17.0   kPa -> 0.255 kg - Suit Target Pressure
//		01-Adir-01:000.000(Config) EVASuit :              Minimum O2 : 11.94  kPa -> 0.179 kg - Safety Limit
//		01-Adir-01:000.000(Config) EVASuit :  -----------------------------------------------------------------------
		
		
		// Currently, the full settlement indoor air composition consists of 58.79% oxygen.
		// The full indoor air pressure is at 34.0 kPa (0.3 atm, 4.9 psi).
		// The O2 partial pressure is kept at 19.9 kPa (0.2 atm, 2.9 psi).
		
		// For an EVA suit,
		// one may opt for a full pressure at 56.54 kPa (8.2 psi),
		// With 34% of O2 in air composition, 
		// the O2 partial pressure is kept at 19.22 kPa.
		
		// Currently for simplicity, an EVA suit has only one gas, namely O2,
		//                    Target O2 is at 17.0  kPa -> 0.255 kg 
	}
		
	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param settlement the location of the EVA suit.
	 * @throws Exception if error creating EVASuit.
	 */
	EVASuit(String name, Settlement settlement) {
		// Use Equipment constructor.
		super(name, TYPE, settlement);
		
		setDescription("A standard EVA suit for Mars surface operation.");

		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		
		PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();
		
		// Add "EVA" to the part scope
		partConfig.addScopes(EVA);

		// Add TYPE to the part scope
		partConfig.addScopes(TYPE);

		// Add "EVA" to malfunction manager scope
		malfunctionManager.addScopeString(EVA);
		
		// Add TYPE to malfunction manager scope
		malfunctionManager.addScopeString(TYPE);
		
		// Add life support function type
		malfunctionManager.addScopeString(FunctionType.LIFE_SUPPORT.getName());
	
		// Initialize the scope map.
		malfunctionManager.initScopes();
		
		// Create MicroInventory instance
		microInventory = new MicroInventory(this, 1);

		// Set capacity for each resource
		microInventory.setSpecificCapacity(ResourceUtil.OXYGEN_ID, OXYGEN_CAPACITY);
		microInventory.setSpecificCapacity(ResourceUtil.WATER_ID, WATER_CAPACITY);
		microInventory.setSpecificCapacity(ResourceUtil.CO2_ID, CO2_CAPACITY);
		
		// Sets the base mass of the bag.
		setBaseMass(getEmptyMass());
	}

	/**
	 * Gets the usual mass of an empty EVASuit.
	 * 
	 * @return
	 */
	public static double getEmptyMass() {
		if (usualMass < 0) {
			usualMass = EquipmentFactory.getEquipmentMass(EquipmentType.EVA_SUIT);
		}
		return usualMass;
	}
	
	/**
     * Gets the total capacity of resource that this container can hold.
     * 
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
	 * Stores the resource.
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
	 * Gets the specific capacity of a particular amount resource.
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getSpecificCapacity(int resource) {
		// Note: this method is different from Equipment's getAmountResourceCapacity
		return microInventory.getSpecificCapacity(resource);
	}

	/**
	 * Gets the unit's malfunction manager.
	 *
	 * @return malfunction manager
	 */
	@Override
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
	@Override
	public boolean lifeSupportCheck() {
		try {

			if (getSpecificAmountResourceStored(ResourceUtil.WATER_ID) <= 0D) {
				logger.log(this, Level.WARNING, 30_000,
						"Ran out of water.");
			}

			if (malfunctionManager.getOxygenFlowModifier() < 100D) {
				logger.warning(this, 30_000, "Oxygen flow sensor malfunction.");
				return false;
			}

			double p = getAirPressure();
			if (p > PhysicalCondition.MAXIMUM_AIR_PRESSURE) {
				logger.log(this, Level.WARNING, 30_000,
						"Detected improper oxygen partial pressure at " + Math.round(p * 100.0D) / 100.0D + " kPa.");
				return false;
			}
			else if (p <= minO2Pressure) {
				logger.log(this, Level.WARNING, 30_000,
						"Dwindling amount of oxygen at " + Math.round(p * 100.0D) / 100.0D
						+ " kPa, already below the minimum safety partial pressure of " + minO2Pressure + " kPa.");
			return false;
			}
			else if (p <= (minO2Pressure + TARGET_O2_PRESSURE) / 2) {
				logger.log(this, Level.WARNING, 30_000,
						"Dwindling amount of oxygen at " + Math.round(p * 100.0D) / 100.0D 
						+ " kPa, already below the target partial pressure of " + TARGET_O2_PRESSURE + " kPa.");
				return false;
			}
			
			double t = getTemperature();
			if (t > NORMAL_TEMP + 15 || t < NORMAL_TEMP - 20) {
				logger.log(this, Level.WARNING, 30_000,
						"Detected improper temperature at " + Math.round(t * 100.0D) / 100.0D + " deg C");
				return false;
			}
		} catch (Exception e) {
          	logger.log(Level.SEVERE, "Cannot finish life support check: "+ e.getMessage());
		}

		return true;
	}

	/**
	 * Gets the air pressure of the life support system.
	 *
	 * @return air pressure (Pa)
	 */
	@Override
	public double getAirPressure() {
		// Based on some pre-calculation,
		// In a 3.9 liter system, 1 kg of O2 can create 66.61118 kPa partial pressure.
		// To supply a partial oxygen pressure of 20.7 kPa, one needs 0.3107 kg of O2.
		// With the minimum required O2 partial pressure of 11.94 kPa (1.732 psi), the minimum mass of O2 is 0.1792 kg
		// Note : our target o2 partial pressure is 17 kPa (not 20.7 kPa), the targeted mass of O2 is 0.2552 kg

		// 66.61 kPa -> 1      kg (full tank O2 pressure)
		// 20.7  kPa -> 0.3107 kg
		// 17    kPa -> 0.2552 kg (target O2 pressure)
		// 11.94 kPa -> 0.1792 kg (min O2 pressure)

		double oxygenLeft = getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
		
		double pp = AirComposition.getOxygenPressure(oxygenLeft, TOTAL_VOLUME);
		// Assuming that we can maintain a constant oxygen partial pressure unless it falls below massO2NominalLimit
		if (oxygenLeft < massO2TargetLimit) {
			
			logger.log(this, Level.WARNING, 30_000,
					"<Alert> " + Math.round(oxygenLeft * 1000.0)/1000.0
						+ " kg O2 left at " + Math.round(pp * 1000.0)/1000.0 + " kPa, going below the target partial pressure of " + TARGET_O2_PRESSURE + " kPa.");
			return pp;
		}		
		else if (oxygenLeft < massO2NominalLimit) {
			
			logger.log(this, Level.WARNING, 30_000,
					"<Alert> " + Math.round(oxygenLeft * 1000.0)/1000.0
					+ " kg O2 left at " + Math.round(pp * 1000.0)/1000.0 + " kPa, going below the nominal partial pressure of " + NOMINAL_O2_PRESSURE + " kPa.");
			return pp;
		}
		
		return pp;
	}

	/**
	 * Gets oxygen partial pressure.
	 * 
	 * @return
	 */
	private double getCurrentOxygenPartialPressure() {
		double oxygenLeft = getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
		return AirComposition.getOxygenPressure(oxygenLeft, TOTAL_VOLUME);
	}
	
	/**
	 * Gets the number of people the life support can provide for.
	 *
	 * @return the capacity of the life support system.
	 */
	@Override
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
	@Override
	public double provideOxygen(double oxygenTaken) {
		double oxygenLacking = 0;

		// NOTE: Should we assume breathing in pure oxygen or trimix and heliox
		// http://www.proscubadiver.net/padi-course-director-joey-ridge/helium-and-diving/
		// May pressurize the suit to 1/3 of atmospheric pressure, per NASA aboard on
		// the ISS

		oxygenLacking = retrieveAmountResource(ResourceUtil.OXYGEN_ID, oxygenTaken);

		double carbonDioxideProvided = gasRatio * (oxygenTaken - oxygenLacking);
		storeAmountResource(ResourceUtil.CO2_ID, carbonDioxideProvided);

		return oxygenTaken - oxygenLacking;
	}

	/**
	 * Gets water from the system.
	 *
	 * @param waterTaken the amount of water requested from system (kg)
	 * @return the amount of water actually received from system (kg)
	 * @throws Exception if error providing water.
	 */
	@Override
	public double provideWater(double waterTaken) {
		double lacking = retrieveAmountResource(ResourceUtil.WATER_ID, waterTaken);

		return waterTaken - lacking;
	}

	
	/**
	 * Gets the temperature of the life support system.
	 *
	 * @return temperature (degrees C)
	 */
	@Override
	public double getTemperature() {
		// Future: Will implement suit internal temperature regulation 
		return NORMAL_TEMP;
	}

	/**
	 * Records usage of this suit.
	 *
	 * @param pulse the amount of clock pulse passing (in millisols)
	 * @throws Exception if error during time.
	 */
	public void recordUsageTime(ClockPulse pulse) {
		var container = getContainerUnit();
		if ((container instanceof Person p)
			&& p.isOutside() && !p.getPhysicalCondition().isDead()) {
				malfunctionManager.activeTimePassing(pulse);
		}
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
		malfunctionManager.timePassing(pulse);

		return true;
	}

	/**
	 * Sets the unit's container unit.
	 *
	 * @param newContainer the unit to contain this unit.
	 */
	@Override
	public void setContainer(UnitHolder newContainer) {
		
		var cu = getContainerUnit();
		if (newContainer != cu) {
			// Add new parent to owner history
			if (locnHistory == null) {
				locnHistory = new History<>(10);
			}
			locnHistory.add(newContainer);
		}
		
		if (newContainer != null) {

			// Note: need to decide what to set for a deceased person
			
			// Call AbstractMobileUnit's setContainer
			super.setContainer(newContainer);
			
			updateStates();
		}
	}
	
	
	/**
	 * Gets the history of the EVASuit.
	 * 
	 * @return
	 */
	public History<UnitHolder> getHistory() {
		return locnHistory;
	}
	
	/**
	 * Returns the parts that normally fail on a EVA Suit.
	 * 
	 * @return
	 */
	public static Map<Integer, Double> getNormalRepairPart() {
		return MalfunctionFactory.getRepairPartProbabilities(Set.of(TYPE));
	}

	/**
	 * Loads the resources need from a source. Also unload any waste.
	 * 
	 * @param source Source of resources
	 * @return The %age full of the suit
	 */
	public double loadResources(EquipmentOwner source) {
		unloadWaste(source);
		loadResource(source, ResourceUtil.OXYGEN_ID);
		loadResource(source, ResourceUtil.WATER_ID);

		return getFullness();
	}

	/**
	 * Fully loads a resource into the EVASuit.
	 * 
	 * @param source
	 * @param resourceId
	 * @return Suit is fully loaded with resource
	 */
	private boolean loadResource(ResourceHolder source, int resourceId) {
		double needed = getRemainingSpecificCapacity(resourceId);
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
	 * Unloads any waste products to the holder.
	 * 
	 * @param newSuitOwner
	 */
	public void unloadWaste(EquipmentOwner holder) {
		double co2 = getSpecificAmountResourceStored(ResourceUtil.CO2_ID);
		if (co2 > 0) {
			retrieveAmountResource(ResourceUtil.CO2_ID, co2);
			holder.storeAmountResource(ResourceUtil.CO2_ID, co2);
		}
	}

	/**
	 * Gets how fully loaded. Gets the lowest of water and oxygen.
	 * 
	 * @return Percentage of lowest resource
	 */
	public double getFullness() {
		double o2Loaded = getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID)/OXYGEN_CAPACITY;
		double waterLoaded = getSpecificAmountResourceStored(ResourceUtil.WATER_ID)/WATER_CAPACITY;

		return Math.min(o2Loaded, waterLoaded);
	}


	@Override
	public int getItemResourceStored(int resource) {
		return microInventory.getItemResourceStored(resource);
	}

	/**
	 * NOTE: EVASuit doesn't have any items/parts yet.
	 */
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

	/**
	 * Gets a list of all stored item resources.
	 *
	 * @return a list of resource ids
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
		return microInventory.getItemResourceIDs();
	}

	@Override
	public double getSpecificAmountResourceStored(int resource) {
		return microInventory.getSpecificAmountResourceStored(resource);
	}

	/**
	 * Retrieves the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
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
	 * Obtains the remaining combined capacity of storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingCombinedCapacity(int resource) {
		return microInventory.getRemainingCombinedCapacity(resource);
	}

	/**
	 * Obtains the remaining specific capacity of storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingSpecificCapacity(int resource) {
		return microInventory.getRemainingSpecificCapacity(resource);
	}
	
	/**
	 * Gets the quantity of all stock and specific amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllAmountResourceStored(int resource) {
		return microInventory.getAllAmountResourceStored(resource);
	}
	
	/**
	 * Gets a list of all stored specific amount resources.
	 *
	 * @return a list of resource ids
	 */
	@Override
	public Set<Integer> getSpecificResourceStoredIDs() {
		return microInventory.getSpecificResourceStoredIDs();
	}
	
	/**
	 * Gets all stored amount resources in eqmInventory, including inside equipment.
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceStoredIDs() {
		return getSpecificResourceStoredIDs();
	}
	
	/**
	 * Is this equipment empty ?
	 *
	 * @param brandNew true if it needs to be brand new
	 * @return
	 */
	public boolean isEmpty(boolean brandNew) {
		if (brandNew) {
			return (getRegisteredOwnerID() == -1);
		}

		return microInventory.isEmpty();
	}

	/**
	 * Gets the total weight of the stored resources.
	 *
	 * @return
	 */
	public double getStoredMass() {
		if (microInventory == null)
			// Note: needed when starting up
			return 0;
		return microInventory.getStoredMass();
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		return microInventory.hasAmountResourceRemainingCapacity(resource);
	}
	
	@Override
	public UnitType getUnitType() {
		return UnitType.EVA_SUIT;
	}
	
	@Override
	public void destroy() {
		malfunctionManager = null;
		microInventory = null;
		super.destroy();
	}
}