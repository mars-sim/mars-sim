/**
 * Mars Simulation Project
 * GoodsManager.java
 * @version 2.81 2007-04-16
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.goods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/**
 * A manager for goods values at a settlement.
 */
public class GoodsManager implements Serializable {

	// Unit update events.
	public static final String GOODS_VALUE_EVENT = "goods values";
	
	// Data members
	private Settlement settlement;
	private Map goodsValues;
	
	/**
	 * Constructor
	 * @param settlement the settlement this manager is for.
	 * @throws Exception if errors constructing instance.
	 */
	public GoodsManager(Settlement settlement) throws Exception {
		this.settlement = settlement;
		
		populateGoodsValues();
		
		updateGoodsValues();
	}
	
	/**
	 * Populates the goods map with empty values.
	 */
	private void populateGoodsValues() {
		List goods = GoodsUtil.getGoodsList();
		goodsValues = new HashMap(goods.size());
		Iterator i = goods.iterator();
		while (i.hasNext()) goodsValues.put(i.next(), new Double(0D));
	}
	
	/**
	 * Gets the value of a good.
	 * @param good the good to check value for.
	 * @return the value (value points).
	 */
	public double getGoodValue(Good good) {
		if (goodsValues.containsKey(good)) 
			return ((Double) goodsValues.get(good)).doubleValue();
		else throw new IllegalArgumentException("Good: " + good + " not valid.");
	}
	
	/**
	 * Updates the values for all the goods at the settlement.
	 * @throws Exception if error updating goods values.
	 */
	private void updateGoodsValues() throws Exception {
		Iterator i = goodsValues.keySet().iterator();
		while (i.hasNext()) updateGoodValue((Good) i.next());
		settlement.fireUnitUpdate(GOODS_VALUE_EVENT);
	}
	
	/**
	 * Time passing
	 * @param time the amount of time passing (millisols).
	 * @throws Exception if error during time.
	 */
	public void timePassing(double time) throws Exception {
		updateGoodsValues();
	}

	/**
	 * Updates the value of a good at the settlement.
	 * @param good the good to update.
	 * @throws Exception if error updating good value.
	 */
	private void updateGoodValue(Good good) throws Exception {
		if (good != null) {
			double value = 0D;
			
			// Determine all amount resource good values.
			if (good.getClassType() == AmountResource.class) {
				if (good.getObject().equals(AmountResource.OXYGEN)) value = determineOxygenValue();
				else if (good.getObject().equals(AmountResource.WATER)) value = determineWaterValue();
				else if (good.getObject().equals(AmountResource.FOOD)) value = determineFoodValue();
				// TODO: determine all amount resource values.
			}
			
			// TODO: determine all item resource values.
			
			// TODO: determine all equipment values.
			
			// TODO: determine all vehicle values.
			
			goodsValues.put(good, new Double(value));
		}
		else throw new IllegalArgumentException("Good is null.");
	}
	
	/**
	 * Determine oxygen good value.
	 * @return value (value points)
	 * @throws Exception if error determining oxygen good value.
	 */
	private double determineOxygenValue() throws Exception {
		double supply = getAmountOfResourceForSettlement(AmountResource.OXYGEN) + 1D;
		int numPeople = settlement.getAllAssociatedPeople().size();
		double amountNeededSol = SimulationConfig.instance().getPersonConfiguration().getOxygenConsumptionRate();
		double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
		double demand = numPeople * amountNeededOrbit;
		
		return demand / supply;
	}
	
	/**
	 * Determine water good value.
	 * @return value (value points)
	 * @throws Exception if error determining water good value.
	 */
	private double determineWaterValue() throws Exception {
		double supply = getAmountOfResourceForSettlement(AmountResource.WATER) + 1D;
		int numPeople = settlement.getAllAssociatedPeople().size();
		double amountNeededSol = SimulationConfig.instance().getPersonConfiguration().getWaterConsumptionRate();
		double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
		double demand = numPeople * amountNeededOrbit;
		
		return demand / supply;
	}
	
	/**
	 * Determine food good value.
	 * @return value (value points)
	 * @throws Exception if error determining food good value.
	 */
	private double determineFoodValue() throws Exception {
		double supply = getAmountOfResourceForSettlement(AmountResource.FOOD) + 1D;
		int numPeople = settlement.getAllAssociatedPeople().size();
		double amountNeededSol = SimulationConfig.instance().getPersonConfiguration().getFoodConsumptionRate();
		double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
		double demand = numPeople * amountNeededOrbit;
		
		return demand / supply;
	}
	
	private double getAmountOfResourceForSettlement(AmountResource resource) throws InventoryException {
		double amount = 0D;
		
		// Get amount of resource in settlement storage.
		amount += settlement.getInventory().getAmountResourceStored(resource);
		
		// Get amount of resource out on mission vehicles.
		Iterator i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement())) {
					amount += vehicle.getInventory().getAmountResourceStored(AmountResource.OXYGEN);
				}
			}
		}
		
		// Get amount of resource carried by people on EVA.
		PersonIterator j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.getLocationSituation().equals(Person.OUTSIDE)) {
				amount += person.getInventory().getAmountResourceStored(AmountResource.OXYGEN);
			}
		}
		
		return amount;
	}
}