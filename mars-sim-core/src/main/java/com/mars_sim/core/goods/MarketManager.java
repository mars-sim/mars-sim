/*
 * Mars Simulation Project
 * MarketManager.java
 * @date 2025-08-26
 * @author Manny Kung
 */
package com.mars_sim.core.goods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

/**
 * This class keeps track of ongoing global market situation in the simulation.
 */
public class MarketManager implements Serializable, Temporal {

	/** default serial identifier. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	// May add back private static SimLogger logger = SimLogger.getLogger(MarketManager.class.getName());

	/** A map of Goods and the global market data. */
	private Map<Good, MarketData> globalMarketBook;

	private UnitManager unitManager;
	
	/**
	 * Constructor.
	 */
	public MarketManager(Simulation sim) {
		this.unitManager = sim.getUnitManager();
		// Initialize data members

		if (globalMarketBook == null) {
			globalMarketBook = new HashMap<>();
			
			for (Good g: GoodsUtil.getGoodsList()) {
				MarketData marketData = new MarketData();
				globalMarketBook.put(g, marketData);
			}
		}
		
		updateMarket();	
	}

	/**
	 * Time passing.
	 *
	 * @param pulse Pulse of the simulation
	 * @throws Exception if error.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
	
		if (pulse.isNewHalfMillisol()) {
			updateMarket();
		}
		
		return true;
	}
	
	/**
	 * Updates the global demand and vp.  
	 */
	private void updateMarket() {
		for (Settlement s: unitManager.getSettlements()) {
			
			for (Good g: GoodsUtil.getGoodsList()) {
				MarketData data = globalMarketBook.get(g);
				double oneDemand = s.getGoodsManager().getDemandScore(g); 
				double oneGoodValue = s.getGoodsManager().getGoodValuePoint(g.getID());
	
				data.updateDemand(oneDemand);
				data.updateGoodValue(oneGoodValue);	
			}	
		}
	}
	
	/**
	 * Gets the global market map.
	 * 
	 * @return
	 */
	public Map<Good, MarketData> getGlobalMarketBook() {
		return globalMarketBook;
	}
	
	/**
	 * Gets the global market average demand score.
	 * 
	 * @param g
	 * @return
	 */
	public double getGlobalMarketDemand(Good g) {
		if (globalMarketBook == null)
			return 0.0;
		if (globalMarketBook.isEmpty())
			return 0.0;
		return globalMarketBook.get(g).getDemand();
	}
	
	/**
	 * Gets the global market average Good value.
	 * 
	 * @param g
	 * @return
	 */
	public double getGlobalMarketGoodValue(Good g) {
		if (globalMarketBook == null)
			return 0.0;
		if (globalMarketBook.isEmpty())
			return 0.0;
		return globalMarketBook.get(g).getGoodValue();
	}
	
	/**
	 * Resets links to the unit manager classes after a reload.
	 */
	public void reinitalizeInstances(Simulation sim) {
		this.unitManager = sim.getUnitManager();
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions
		if (globalMarketBook != null) {
			globalMarketBook.clear();
			globalMarketBook = null;
		}
	}
}
