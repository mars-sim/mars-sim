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
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.SystemType;

/**
 * The EVASuit class represents an EVA suit which provides life support
 * for a person during a EVA operation.
 */
public class EVASuit
extends Equipment
implements LifeSupportType, Serializable, Malfunctionable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EVASuit.class.getName());

    private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());

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
	/** Normal air pressure (Pa). */
	private static final double NORMAL_AIR_PRESSURE = 101325D;
	/** Normal temperature (celsius). */
	private static final double NORMAL_TEMP = 25D;
	/** 334 Sols (1/2 orbit). */
	private static final double WEAR_LIFETIME = 334000D;
	/** 100 millisols. */
	private static final double MAINTENANCE_TIME = 100D;

	// Data members
	/** The equipment's malfunction manager. */
	protected MalfunctionManager malfunctionManager;
	private Weather weather ;

	// 2017-04-10 WARNING: cannot use static or result in null
	public AmountResource waterAR = ResourceUtil.waterAR;//findAmountResource(WATER);		// 2
	public AmountResource oxygenAR = ResourceUtil.oxygenAR;//findAmountResource(OXYGEN);		// 3
	public AmountResource carbonDioxideAR = ResourceUtil.carbonDioxideAR;//findAmountResource(CO2);	// 4


	/**
	 * Constructor.
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

		//oxygenAR = ResourceUtil.findAmountResource(LifeSupportType.OXYGEN);
		//waterAR = ResourceUtil.findAmountResource(LifeSupportType.WATER);
		//carbonDioxideAR = ResourceUtil.findAmountResource(LifeSupportType.CO2); //carbonDioxideAR;//

		// Set the resource capacities of the EVA suit.
		getInventory().addAmountResourceTypeCapacity(oxygenAR, OXYGEN_CAPACITY);
		getInventory().addAmountResourceTypeCapacity(waterAR, WATER_CAPACITY);
		getInventory().addAmountResourceTypeCapacity(carbonDioxideAR, CO2_CAPACITY);
	}

	/**
	 * Gets the unit's malfunction manager.
	 * @return malfunction manager
	 */
	public MalfunctionManager getMalfunctionManager() {
		return malfunctionManager;
	}

	/**
	 * Returns true if life support is working properly and is not out
	 * of oxygen or water.
	 * @return true if life support is OK
	 * @throws Exception if error checking life support.
	 */
	public boolean lifeSupportCheck() {
		//boolean result = true;
        try {
			if (getInventory().getAmountResourceStored(oxygenAR, false) <= 0D) {
				LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName, 
						this.getName() + " ran out of oxygen in EVASuit", null);
				return false;
			}
			if (getInventory().getAmountResourceStored(waterAR, false) <= 0D) {
				LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
						this.getName() + " ran out of water in EVASuit.", null);
				return false;
			}
			if (malfunctionManager.getOxygenFlowModifier() < 100D) {
				LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName, 
						this.getName() + "'s oxygen flow sensor detected malfunction.", null);
				return false;
			}
			if (malfunctionManager.getWaterFlowModifier() < 100D) {
				LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
						this.getName() + "'s water flow sensor detected malfunction.", null);
				return false;
			}

			double p = getAirPressure();
			if (p > NORMAL_AIR_PRESSURE * 1.5 || p < NORMAL_AIR_PRESSURE * .5) {
				LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName, 
						this.getName() + " detected improper air pressure at " + Math.round(p *10D)/10D, null);
				return false;
			}
			double t = getTemperature();
			if (t > NORMAL_TEMP + 15 || t < NORMAL_TEMP - 40) {
				LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName,
						this.getName() + " detected improper temperature at " + Math.round(t *10D)/10D, null);
				return false;
			}
        }
        catch (Exception e) {
            e.printStackTrace();
        }

		return true;
	}

	/**
	 * Gets the number of people the life support can provide for.
	 * @return the capacity of the life support system.
	 */
	public int getLifeSupportCapacity() {
		return 1;
	}

	/**
	 * Gets oxygen from system.
	 * @param oxygenTaken the amount of oxygen requested from system (kg)
	 * @return the amount of oxygen actually received from system (kg)
	 * @throws Exception if error providing oxygen.
	 */
	public double provideOxygen(double amountRequested) {
/*
		//double oxygenTaken = oxygenTaken;
		//AmountResource oxygen = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
		double oxygenLeft = getInventory().getAmountResourceStored(oxygenAR, false);

		if (oxygenTaken > oxygenLeft) {
			oxygenTaken = oxygenLeft;
		}

		getInventory().retrieveAmountResource(oxygenAR, oxygenTaken);
		// 2015-01-09 Added addDemandTotalRequest()
		getInventory().addAmountDemandTotalRequest(oxygenAR);
		// 2015-01-09 addDemandRealUsage()
		getInventory().addAmountDemand(oxygenAR, oxygenTaken);
*/
		double oxygenTaken = amountRequested;
		try {
			//AmountResource oxygen = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
			//if (oxygenAR == null) System.out.println("o2");
			double oxygenLeft = getInventory().getAmountResourceStored(oxygenAR, false);
			//System.out.println("oxygenLeft : " + oxygenLeft);
			if (oxygenTaken > oxygenLeft)
				oxygenTaken = oxygenLeft;
			getInventory().retrieveAmountResource(oxygenAR, oxygenTaken);
			// 2015-01-09 Added addDemandTotalRequest()
			getInventory().addAmountDemandTotalRequest(oxygenAR);
			// 2015-01-09 addDemandRealUsage()
			getInventory().addAmountDemand(oxygenAR, oxygenTaken);

			//AmountResource carbonDioxide = AmountResource.findAmountResource("carbon dioxide");
			double carbonDioxideProvided = oxygenTaken;
			double carbonDioxideCapacity = getInventory().getAmountResourceRemainingCapacity(carbonDioxideAR, true, false);
			if (carbonDioxideProvided > carbonDioxideCapacity)
				carbonDioxideProvided = carbonDioxideCapacity;

			getInventory().storeAmountResource(carbonDioxideAR, carbonDioxideProvided, true);
			// 2015-01-15 Add addSupplyAmount()
			getInventory().addAmountSupplyAmount(carbonDioxideAR, carbonDioxideProvided);
		}
        catch (Exception e) {
            logger.log(Level.SEVERE, this.getName() + " - Error in providing O2 needs: " + e.getMessage());
        }
		return oxygenTaken * (malfunctionManager.getOxygenFlowModifier() / 100D);
	}

	/**
	 * Gets water from the system.
	 * @param waterTaken the amount of water requested from system (kg)
	 * @return the amount of water actually received from system (kg)
	 * @throws Exception if error providing water.
	 */
	public double provideWater(double waterTaken)  {
		//double waterTaken = waterTaken;
		//AmountResource waterResource = AmountResource.findAmountResource(LifeSupportType.WATER);
		double waterLeft = getInventory().getAmountResourceStored(waterAR, false);

		if (waterTaken > waterLeft) {
			waterTaken = waterLeft;
		}

		getInventory().retrieveAmountResource(waterAR, waterTaken);
		// 2015-01-09 Added addDemandTotalRequest()
		getInventory().addAmountDemandTotalRequest(waterAR);
		// 2015-01-09 addDemandRealUsage()
		getInventory().addAmountDemand(waterAR, waterTaken);

		return waterTaken * (malfunctionManager.getWaterFlowModifier() / 100D);
	}

	/**
	 * Gets the air pressure of the life support system.
	 * @return air pressure (Pa)
	 */
	public double getAirPressure() {
		double result = NORMAL_AIR_PRESSURE
				* (malfunctionManager.getAirPressureModifier() / 100D);
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
	 * @return temperature (degrees C)
	 */
	public double getTemperature() {
		double result = NORMAL_TEMP
				* (malfunctionManager.getTemperatureModifier() / 100D);
		double ambient = 0;
		if (weather == null) {
			weather = Simulation.instance().getMars().getWeather();
			// For the first time calling, use calculateTemperature()
			ambient = weather.calculateTemperature(getCoordinates());
		}
		else
			ambient = weather.getTemperature(getCoordinates());

		// the temperature of the suit will not be lower than the ambient temperature
		if (result < ambient) {
			// TODO: add codes to simulate the use of cooling coil to turn on cooler to reduce the temperature inside the EVA suit.

			// calculate new temperature

			// return newTemperature

			// if cooling coil malfunction, then return ambient only
			return result;
			// NOTE: for now, turn off returning ambient until new codes are added.
			//return ambient;
		} else {
			return result;
		}
	}

	/**
	 * Checks to see if the inventory is at full capacity with oxygen and water.
	 * @return true if oxygen and water stores at full capacity
	 * @throws Exception if error checking inventory.
	 */
	public boolean isFullyLoaded() {
		boolean result = true;

		//AmountResource oxygenResource = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
		double oxygen = getInventory().getAmountResourceStored(oxygenAR, false);
		if (oxygen != OXYGEN_CAPACITY) {
			result = false;
		}

		//AmountResource waterResource = AmountResource.findAmountResource(LifeSupportType.WATER);
		double water = getInventory().getAmountResourceStored(waterAR, false);
		if (water != WATER_CAPACITY) {
			result = false;
		}
		return result;
	}

	/**
	 * Time passing for EVA suit.
	 * @param time the amount of time passing (millisols)
	 * @throws Exception if error during time.
	 */
	public void timePassing(double time) {

		Unit container = getContainerUnit();
		if (container instanceof Person) {
			Person person = (Person) container;
			if (!person.isDead()) {
				malfunctionManager.activeTimePassing(time);
			}
		}
		malfunctionManager.timePassing(time);
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
		// TODO Auto-generated method stub
		return null;
	}

	public void destroy() {
		malfunctionManager = null;
		weather = null;
	}
}