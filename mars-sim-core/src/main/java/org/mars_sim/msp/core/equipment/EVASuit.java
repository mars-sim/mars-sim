/**
 * Mars Simulation Project
 * EVASuit.java
 * @version 3.1.0 2016-10-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.SystemType;

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
 */
public class EVASuit extends Equipment implements LifeSupportInterface, Serializable, Malfunctionable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EVASuit.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	// Static members
	public static String TYPE = SystemType.EVA_SUIT.getName();

	private static String[] parts;
	
	/** Total gas tank volume of EVA suit (Liter). */
	public static final double TOTAL_VOLUME = 3.9D;
	/** Oxygen capacity (kg.). */
	private static final double OXYGEN_CAPACITY = 1D;
	/** CO2 capacity (kg.). */
	private static final double CO2_CAPACITY = 1D;
	/** Water capacity (kg.). */
	private static final double WATER_CAPACITY = 4D;
	/** Typical O2 air pressure (Pa) inside EVA suit is set to be 20.7 kPa. */
	private static final double NORMAL_AIR_PRESSURE = 17;// 20.7; // CompositionOfAir.SKYLAB_TOTAL_AIR_PRESSURE_kPA; // 101325D;
	/** Normal temperature (celsius). */
	private static final double NORMAL_TEMP = 25D;
	/** The wear lifetime value of 334 Sols (1/2 orbit). */
	private static final double WEAR_LIFETIME = 334_000;
	/** The maintenance time of 20 millisols. */
	private static final double MAINTENANCE_TIME = 20D;
	
	/** The minimum required O2 partial pressure. At 11.94 kPa (1.732 psi)  */
	private static double min_o2_pressure;
	/** The full O2 partial pressure if at full tank. */
	private static double fullO2PartialPressure;
	/** The nominal mass of O2 required to maintain the nominal partial pressure of 20.7 kPa (3.003 psi)  */
	private static double massO2NominalLimit;
	/** The minimum mass of O2 required to maintain right above the safety limit of 11.94 kPa (1.732 psi)  */
	private static double massO2MinimumLimit;
	/** Unloaded mass of EVA suit (kg.). The combined total of the mass of all parts. */
	public static double emptyMass;
	
	// Data members
	/** The equipment's malfunction manager. */
	private MalfunctionManager malfunctionManager;

	static {
		 parts = new String[] {
					"eva helmet",
					"helmet visor",
					"counter pressure suit",
					"coveralls",
					"suit heating unit",
					"eva gloves",
					"eva boots",
					"eva pads",
					"eva backpack",
					"eva antenna",
					"eva battery",
					"eva radio",
			};
		 
		 for (String p: parts) {
			 emptyMass += ItemResourceUtil.findItemResource(p).getMassPerItem();
		 }
		 
		 logger.config("Each EVA suit has an unloaded weight of " + emptyMass + " kg");
		 
		 min_o2_pressure = personConfig.getMinSuitO2Pressure();
		 
		 fullO2PartialPressure = CompositionOfAir.KPA_PER_ATM * OXYGEN_CAPACITY / CompositionOfAir.O2_MOLAR_MASS * CompositionOfAir.R_GAS_CONSTANT / TOTAL_VOLUME;

		 massO2MinimumLimit = min_o2_pressure / fullO2PartialPressure * OXYGEN_CAPACITY;
		 
		 massO2NominalLimit = NORMAL_AIR_PRESSURE / min_o2_pressure * massO2MinimumLimit;
		 
		 logger.info("The full tank O2 partial pressure is " + Math.round(fullO2PartialPressure*1_000.0)/1_000.0 + " kPa");
		 logger.info("The minimum mass limit of O2 (above the safety limit) is " + Math.round(massO2MinimumLimit*10_000.0)/10_000.0  + " kg");
		 logger.info("The nomimal mass limit of O2 is " + Math.round(massO2NominalLimit*10_000.0)/10_000.0  + " kg");

	}
	
	/**
	 * Constructor.
	 * 
	 * @param location the location of the EVA suit.
	 * @throws Exception if error creating EVASuit.
	 */
	public EVASuit(Coordinates location) {

		// Use Equipment constructor.
		super(TYPE, TYPE, location);
	
		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		malfunctionManager.addScopeString(TYPE);
		malfunctionManager.addScopeString(FunctionType.LIFE_SUPPORT.getName());

		// Set the empty mass of the EVA suit in kg.
		setBaseMass(emptyMass);

		// Set the resource capacities of the EVA suit.
		getInventory().addAmountResourceTypeCapacity(ResourceUtil.oxygenID, OXYGEN_CAPACITY);
		getInventory().addAmountResourceTypeCapacity(ResourceUtil.waterID, WATER_CAPACITY);
		getInventory().addAmountResourceTypeCapacity(ResourceUtil.co2ID, CO2_CAPACITY);
		
		// Set the load carrying capacity of the EVA suit to an arbitrary value of 250 kg.
		getInventory().addGeneralCapacity(250);
		
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
			
			if (getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false) <= massO2MinimumLimit) {
				String name = ((Person)(super.getLastOwner())).getName();
				
				LogConsolidated.log(Level.WARNING, 10_000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ getName() + " worned by " + name
								+ " had less than 0.1792 kg oxygen (below the safety limit).");
				return false;
			}
			
			if (getInventory().getAmountResourceStored(ResourceUtil.waterID, false) <= 0D) {
				String name = ((Person)(super.getLastOwner())).getName();
				
				LogConsolidated.log(Level.WARNING, 10_000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ getName() + " worned by " + name + " ran out of water.");
//				return false;
			}
			
			if (malfunctionManager.getOxygenFlowModifier() < 100D) {
				String name = ((Person)(super.getLastOwner())).getName();
				LogConsolidated.log(Level.WARNING, 10_000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ getName() + " worned by " + name + "had oxygen flow sensor malfunction.", null);
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
				LogConsolidated.log(Level.WARNING, 5000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ this.getName() + " detected improper o2 pressure at " + Math.round(p * 100.0D) / 100.0D);
				return false;
			}
			double t = getTemperature();
			if (t > NORMAL_TEMP + 15 || t < NORMAL_TEMP - 20) {
				LogConsolidated.log(Level.WARNING, 5000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ this.getName() + " detected improper temperature at " + Math.round(t * 100.0D) / 100.0D);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	public double provideOxygen(double amountRequested) {
		double oxygenTaken = amountRequested;
		// NOTE: Should we assume breathing in pure oxygen or trimix and heliox
		// http://www.proscubadiver.net/padi-course-director-joey-ridge/helium-and-diving/
		// May pressurize the suit to 1/3 of atmospheric pressure, per NASA aboard on
		// the ISS
		if (amountRequested > 0) {
			try {
				double oxygenLeft = getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false);
	//			if (oxygenTaken * 100 > oxygenLeft) {
	//				// O2 is running out soon
	//				// Walk back to the building or vehicle
	//				person.getMind().getTaskManager().clearTask();//
	//				person.getMind().getTaskManager().addTask(new Relax(person));
	//			}
				
				if (oxygenTaken > oxygenLeft)
					oxygenTaken = oxygenLeft;
				if (oxygenTaken > 0) {
					getInventory().retrieveAmountResource(ResourceUtil.oxygenID, oxygenTaken);
//					getInventory().addAmountDemandTotalRequest(ResourceUtil.oxygenID);
//					getInventory().addAmountDemand(ResourceUtil.oxygenID, oxygenTaken);
				}
				// NOTE: Assume the EVA Suit has pump system to vent out all CO2 to prevent the
				// built-up. Since the breath rate is 12 to 25 per minute. Size of breath is 500 mL.
				// Percent CO2 exhaled is 4% so CO2 per breath is approx 0.04g ( 2g/L x .04 x
				// .5l).
	
	//			double carbonDioxideProvided = .04 * .04 * oxygenTaken;
	//			double carbonDioxideCapacity = getInventory().getAmountResourceRemainingCapacity(carbonDioxideAR, true, false);
	//			if (carbonDioxideProvided > carbonDioxideCapacity)
	//				carbonDioxideProvided = carbonDioxideCapacity;
	//
	//			getInventory().storeAmountResource(carbonDioxideAR, carbonDioxideProvided, true);
	//			getInventory().addAmountSupplyAmount(carbonDioxideAR, carbonDioxideProvided);
	
			} catch (Exception e) {
				logger.log(Level.SEVERE, this.getName() + " - Error in providing O2 needs: " + e.getMessage());
			}
		}
		
		return oxygenTaken;// * (malfunctionManager.getOxygenFlowModifier() / 100D);
	}

	/**
	 * Gets water from the system.
	 * 
	 * @param waterTaken the amount of water requested from system (kg)
	 * @return the amount of water actually received from system (kg)
	 * @throws Exception if error providing water.
	 */
	public double provideWater(double waterTaken) {
		double waterLeft = getInventory().getAmountResourceStored(ResourceUtil.waterID, false);

		if (waterTaken > waterLeft) {
			waterTaken = waterLeft;
		}

		getInventory().retrieveAmountResource(ResourceUtil.waterID, waterTaken);
//		getInventory().addAmountDemandTotalRequest(ResourceUtil.waterID);
//		getInventory().addAmountDemand(ResourceUtil.waterID, waterTaken);

		return waterTaken;// * (malfunctionManager.getWaterFlowModifier() / 100D);
	}

	/**
	 * Gets the air pressure of the life support system.
	 * 
	 * @return air pressure (Pa)
	 */
	public double getAirPressure() {
		// Based on some pre-calculation, 
		// In a 3.9 liter system, 1 kg of O2 can create 66.61118 kPa partial pressure 
		// To supply a partial oxygen pressure of 20.7 kPa, one needs at least 0.3107 kg O2
		
		// With the minimum required O2 partial pressure of 11.94 kPa (1.732 psi), the minimum mass of O2 is 0.1792 kg 
		
		// Note : our target o2 partial pressure is now 17 kPa (not 20.7 kPa)
		
		double oxygenLeft = getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false);
		// Assuming that we can maintain a constant oxygen partial pressure unless it falls below massO2NominalLimit 
		if (oxygenLeft < massO2NominalLimit) {
			double remainingMass = oxygenLeft;
			double pp = CompositionOfAir.KPA_PER_ATM * remainingMass / CompositionOfAir.O2_MOLAR_MASS * CompositionOfAir.R_GAS_CONSTANT / TOTAL_VOLUME;
			LogConsolidated.log(Level.WARNING, 10_000, sourceName,
					"[" + this.getLocationTag().getLocale() + "] " 
						+ this.getName() + " has " + Math.round(oxygenLeft*100.0)/100.0
						+ " kg O2 left at partial pressure of " + Math.round(pp*100.0)/100.0);
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
		double result = NORMAL_TEMP;// * (malfunctionManager.getTemperatureModifier() / 100D);
		double ambient = weather.getTemperature(getCoordinates());

		// the temperature of the suit will not be lower than the ambient temperature
		if (result < ambient) {
			// TODO: add codes to simulate the use of cooling coil to turn on cooler to
			// reduce the temperature inside the EVA suit.
			// if cooling coil malfunction, then return ambient only
			return result;
			// NOTE: for now, turn off returning ambient until new codes are added.
			// return ambient;
		} else {
			return result;
		}
	}

//	/**
//	 * Checks to see if the inventory is at full capacity with oxygen and water.
//	 * 
//	 * @return true if oxygen and water stores at full capacity
//	 * @throws Exception if error checking inventory.
//	 */
//	public boolean isFullyLoaded() {
//		boolean result = true;
//
//		double oxygen = getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false);
//		if (oxygen != OXYGEN_CAPACITY) {
//			result = false;
//		}
//
//		double water = getInventory().getAmountResourceStored(ResourceUtil.waterID, false);
//		if (water != WATER_CAPACITY) {
//			result = false;
//		}
//		
//		return result;
//	}

	/**
	 * Time passing for EVA suit.
	 * 
	 * @param time the amount of time passing (millisols)
	 * @throws Exception if error during time.
	 */
	public void timePassing(double time) {

		Unit container = getContainerUnit();
		if (container instanceof Person) {
			Person person = (Person) container;
			if (!person.getPhysicalCondition().isDead()) {
//				setLastOwner(person);
				malfunctionManager.activeTimePassing(time);	
			}
		}

		malfunctionManager.timePassing(time);
	}

	/**
	 * Gets a list of people affected by this equipment
	 * @return Collection<Person>
	 */
	@Override
	public Collection<Person> getAffectedPeople() {
		return super.getAffectedPeople();
//		return getInventory().getContainedPeople();
		
//		Collection<Person> people = super.getAffectedPeople();
//		if (getContainerUnit() instanceof Person) {
//			if (!people.contains(getContainerUnit())) {
//				people.add((Person) getContainerUnit());
//			}
//		}
		
//		Person p = (Person) getInventory().findUnitOfClass(Person.class);
//		people.add(p);
//		for (Unit u : getInventory().getContainedUnits()) {
//		}
			
//		return getInventory().getAllContainedUnits()
//				.stream()
//				.filter(u -> u instanceof Person)
//				.map(p -> (Person) p)
//				.collect(Collectors.toList());
		
//		people.addAll(pp);
//		return people;
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
		// TODO Auto-generated method stub
		return getContainerUnit().getBuildingLocation();
	}

	@Override
	public Settlement getAssociatedSettlement() {
		Settlement s = getContainerUnit().getAssociatedSettlement();
		if (s == null) s = super.getAssociatedSettlement();
		return s;
	}

	@Override
	public Unit getUnit() {
		return this;
	}
	
//	public boolean equals(Object obj) {
//		if (this == obj) return true;
//		if (obj == null) return false;
//		if (this.getClass() != obj.getClass()) return false;
//		EVASuit e = (EVASuit) obj;
//		return this.getNickName().equals(e.getNickName());
//	}
	

	public void destroy() {
		malfunctionManager = null;
		parts = null;
	}

	public static String[] getParts() {
		return parts;
	}
}