/**
 * Mars Simulation Project
 * GoodsUtil.java
 * @version 2.81 2007-04-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.goods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for goods information.
 */
public class GoodsUtil {
	
	private static List goodsList;
	
	/**
	 * Private constructor for utility class.
	 */
	private GoodsUtil() {}
	
	/**
	 * Gets an ordered list of all goods in the simulation.
	 * @return list of goods
	 */
	public static List getGoodsList() {
		
		if (goodsList == null) {
			goodsList = new ArrayList();
			populateGoodsList();
		}
		
		return Collections.unmodifiableList(goodsList);
	}
	
	/**
	 * Populates the goods list with all goods.
	 */
	private static void populateGoodsList() {
		// TODO: Populate goods list.
	}
}