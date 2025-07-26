/*
 * Mars Simulation Project
 * MarketManager.java
 * @date 2025-07-23
 * @author Manny Kung
 */
package com.mars_sim.core.goods;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
	// May add back later : private static SimLogger logger = SimLogger.getLogger(MarketManager.class.getName());

	/** A map of Goods and the global market data. */
	private Map<Good, MarketData> globalMarketBook;

	private UnitManager unitManager;
	
	/**
	 * Constructor.
	 */
	public MarketManager(Simulation sim) {
		this.unitManager = sim.getUnitManager();
		// Initialize data members
	}

	/**
	 * Time passing.
	 *
	 * @param pulse Pulse of the simulation
	 * @throws Exception if error.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
	
		if (pulse.isNewHalfSol()) {

			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Good> goods = GoodsUtil.getGoodsList();

			if (globalMarketBook == null) {
				globalMarketBook = new HashMap<>();
				
				for (Good g: goods) {
					MarketData marketData = new MarketData();
					globalMarketBook.put(g, marketData);
				}
			}
		
			for (Settlement s: settlements) {
					
				for (Good g: goods) {
					MarketData data = globalMarketBook.get(g);
					double oneDemand = s.getGoodsManager().getMarketMap().get(g).getDemand(); 
					double oneGoodValue = s.getGoodsManager().getMarketMap().get(g).getGoodValue();
		
					data.updateDemand(oneDemand);
					data.updateGoodValue(oneGoodValue);				
					globalMarketBook.put(g, data);			
				}	
			}
		}
		
		return true;
	}
	
	/**
	 * Gets the global market average demand score.
	 * 
	 * @param g
	 * @return
	 */
	public double getGlobalMarketDemand(Good g) {
		if (globalMarketBook == null)
			return 0;
		if (globalMarketBook.isEmpty())
			return 0;
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
			return 0;
		if (globalMarketBook.isEmpty())
			return 0;
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
