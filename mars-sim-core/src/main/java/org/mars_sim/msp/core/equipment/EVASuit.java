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
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.SystemType;

/**
 * The EVASuit class represents an EVA suit which provides life support for a
 * person during a EVA operation.
 */
public class EVASuit extends Equipment implements LifeSupportType, Serializable, Malfunctionable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EVASuit.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	// Static members
	public static String TYPE = SystemType.EVA_SUIT.getName();

	/** Unloaded mass of EVA suit (kg.). */
	public static final double EMPTY_MASS = 45D;
	/** Oxygen capacity (kg.). */
	private static final double OXYGEN_CAPACITY = 1D;
	/** CO2 capacity (kg.). */
	private static final double CO2_CAPACITY = 1D;
	/** Water capacity (kg.). */
	private static final double WATER_CAPACITY = 4D;
	/** Normal air pressure (Pa) inside EVA suit is around 20 kPa. */
	private static final double NORMAL_AIR_PRESSURE = CompositionOfAir.SKYLAB_TOTAL_AIR_PRESSURE_kPA; // 101325D;
	// Ref : https://en.wikipedia.org/wiki/Space_suit
	// Generally, to supply enough oxygen for respiration, a space suit using pure
	// oxygen must have a pressure of about
	// 32.4 kPa (240 Torr; 4.7 psi), equal to the 20.7 kPa (160 Torr; 3.0 psi)
	// partial pressure of oxygen in the Earth's
	// atmosphere at sea level, plus 5.3 kPa (40 Torr; 0.77 psi) CO2 and 6.3 kPa (47
	// Torr; 0.91 psi) water vapor
	// pressure, both of which must be subtracted from the alveolar pressure to get
	// alveolar oxygen partial pressure
	// in 100% oxygen atmospheres, by the alveolar gas equation.
	/** Normal temperature (celsius). */
	private static final double NORMAL_TEMP = 25D;
	/** 334 Sols (1/2 orbit). */
	private static final double WEAR_LIFETIME = 334000D;
	/** 100 millisols. */
	private static final double MAINTENANCE_TIME = 20D;
	private static double minimum_air_pressure;

	// Data members
	private Person person;

	/** The equipment's malfunction manager. */
	private MalfunctionManager malfunctionManager;

	private static Weather weather;

	/**
	 * Constructor.
	 * 
	 * @param location the location of the EVA suit.
	 * @throws Exception if error creating EVASuit.
	 */
	public EVASuit(Coordinates location) {

		// Use Equipment constructor.
		super(TYPE, location);

		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		malfunctionManager.addScopeString(TYPE);
		malfunctionManager.addScopeString(FunctionType.LIFE_SUPPORT.getName());

		// Set the empty mass of the EVA suit in kg.
		setBaseMass(EMPTY_MASS);

		// Set the resource capacities of the EVA suit.
		getInventory().addARTypeCapacity(ResourceUtil.oxygenID, OXYGEN_CAPACITY);
		getInventory().addARTypeCapacity(ResourceUtil.waterID, WATER_CAPACITY);
		getInventory().addARTypeCapacity(ResourceUtil.co2ID, CO2_CAPACITY);

//		PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
		minimum_air_pressure = SimulationConfig.instance().getPersonConfiguration().getMinAirPressure();

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
			if (getInventory().getARStored(ResourceUtil.oxygenID, false) <= 0D) {
				LogConsolidated.log(logger, Level.INFO, 5000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ person.getName() + "'s " + this.getName() + " ran out of oxygen.", null);
				return false;
			}
			if (getInventory().getARStored(ResourceUtil.waterID, false) <= 0D) {
				LogConsolidated.log(logger, Level.INFO, 5000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ person.getName() + "'s " + this.getName() + " ran out of water.", null);
				return false;
			}
			if (malfunctionManager.getOxygenFlowModifier() < 100D) {
				LogConsolidated.log(logger, Level.INFO, 5000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ person.getName() + "'s " + this.getName() + "'s oxygen flow sensor detected malfunction.", null);
				return false;
			}
			if (malfunctionManager.getWaterFlowModifier() < 100D) {
				LogConsolidated.log(logger, Level.INFO, 5000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ person.getName() + "'s " + this.getName() + "'s water flow sensor detected malfunction.", null);
				return false;
			}

			double p = getAirPressure();
			if (p > PhysicalCondition.MAXIMUM_AIR_PRESSURE || p <= minimum_air_pressure) {
				LogConsolidated.log(logger, Level.INFO, 5000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ person.getName() + "'s " + this.getName() + " detected improper air pressure at " + Math.round(p * 10D) / 10D, null);
				return false;
			}
			double t = getTemperature();
			if (t > NORMAL_TEMP + 15 || t < NORMAL_TEMP - 20) {
				LogConsolidated.log(logger, Level.INFO, 5000, sourceName,
						"[" + this.getLocationTag().getLocale() + "] " 
								+ person.getName() + "'s " + this.getName() + " detected improper temperature at " + Math.round(t * 10D) / 10D, null);
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
		// Should we assume breathing in pure oxygen or trimix and heliox
		// http://www.proscubadiver.net/padi-course-director-joey-ridge/helium-and-diving/

		// May pressurize the suit to 1/3 of atmospheric pressure, per NASA aboard on
		// the ISS
		double oxygenTaken = amountRequested;
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
			getInventory().retrieveAmountResource(ResourceUtil.oxygenID, oxygenTaken);
			getInventory().addAmountDemandTotalRequest(ResourceUtil.oxygenID);
			getInventory().addAmountDemand(ResourceUtil.oxygenID, oxygenTaken);

			// Assume the EVA Suit has pump system to vent out all CO2 to prevent the
			// built-up
			// Since the breath rate is 12 to 25 per minute. Size of breath is 500 mL.
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
		return oxygenTaken * (malfunctionManager.getOxygenFlowModifier() / 100D);
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
		getInventory().addAmountDemandTotalRequest(ResourceUtil.waterID);
		getInventory().addAmountDemand(ResourceUtil.waterID, waterTaken);

		return waterTaken * (malfunctionManager.getWaterFlowModifier() / 100D);
	}

	/**
	 * Gets the air pressure of the life support system.
	 * 
	 * @return air pressure (Pa)
	 */
	public double getAirPressure() {
		double result = NORMAL_AIR_PRESSURE * (malfunctionManager.getAirPressureModifier() / 100D);
		if (weather == null)
			weather = Simulation.instance().getMars().getWeather();
		double ambient = weather.getAirPressure(getCoordinates());
		if (result < ambient) {
			return ambient;
		} else {
			return result;
		}
	}

	/**
	 * Gets the temperature of the life support system.
	 * 
	 * @return temperature (degrees C)
	 */
	public double getTemperature() {
		double result = NORMAL_TEMP * (malfunctionManager.getTemperatureModifier() / 100D);
		double ambient = 0;
		if (weather == null) {
			weather = Simulation.instance().getMars().getWeather();
			// For the first time calling, use calculateTemperature()
			ambient = weather.calculateTemperature(getCoordinates());
		} else
			ambient = weather.getTemperature(getCoordinates());

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
				this.person = person;
				malfunctionManager.activeTimePassing(time);	
			}
			
			malfunctionManager.timePassing(time);
		}
		else
			this.person = null;
	}

	@Override
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = super.getAffectedPeople();
		if (getContainerUnit() instanceof Person) {
			if (!people.contains(getContainerUnit())) {
				people.add((Person) getContainerUnit());
			}
		}
		return people;
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

	public void destroy() {
		malfunctionManager = null;
		weather = null;
	}

	@Override
	public Building getBuildingLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return this.getAssociatedSettlement();
	}

	@Override
	public Settlement getBuriedSettlement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Unit getUnit() {
		return this;
	}

}