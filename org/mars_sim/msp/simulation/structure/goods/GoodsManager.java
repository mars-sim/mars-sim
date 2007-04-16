/**
 * Mars Simulation Project
 * GoodsManager.java
 * @version 2.81 2007-04-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.goods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * A manager for goods values at a settlement.
 */
public class GoodsManager implements Serializable {

	// Data members
	private Settlement settlement;
	private Map goodsValues;
	
	/**
	 * Constructor
	 * @param settlement the settlement this manager is for.
	 */
	public GoodsManager(Settlement settlement) {
		this.settlement = settlement;
		
		populateGoodsValues();
		
		updateGoodsValues();
	}
	
	/**
	 * Populates the goods map with empty values.
	 */
	private void populateGoodsValues() {
		Set goods = GoodsUtil.getGoodsSet();
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
			return ((Double) goodsValues.get(good)).intValue();
		else throw new IllegalArgumentException("Good: " + good + " not valid.");
	}
	
	/**
	 * Updates the values for all the goods at the settlement.
	 */
	private void updateGoodsValues() {
		Iterator i = goodsValues.keySet().iterator();
		while (i.hasNext()) {
			updateGoodValue((Good) i.next());
		}
	}
	
	/**
	 * Updates the value of a good at the settlement.
	 * @param good the good to update.
	 */
	private void updateGoodValue(Good good) {
		if (good != null) {
			// TODO: Determine value of good.
			double value = 0D;
			
			goodsValues.put(good, new Double(value));
		}
		else throw new IllegalArgumentException("Good is null.");
	}
	
	/**
	 * Time passing
	 * @param time the amount of time passing (millisols).
	 * @throws Exception if error during time.
	 */
	public void timePassing(double time) throws Exception {
		updateGoodsValues();
	}
}