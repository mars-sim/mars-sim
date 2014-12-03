/**
 * Mars Simulation Project
 * ReadyMeal.java
 * @version 3.07 2014-12-02
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building.cooking;

import org.mars_sim.msp.core.time.MarsClock;


// 2014-12-01 Created ReadyMeal class
	 public class ReadyMeal {
	 	String name;
	 	int numServings;
		//int numKitchens;
		static int bestQuality;
		static int worstQuality;
		int quality;
		MarsClock expiration;
		
		/** default serial id. */
		private static final long serialVersionUID = 1L;
 	
 	ReadyMeal(String name, int numServings, int quality, MarsClock expiration){
 		this.name = name;
 		this.numServings = numServings;
 		//this.numKitchens = numKitchens;
 		this.quality = quality;
 		this.expiration = expiration;
 		
 		if (quality > bestQuality)
 			ReadyMeal.bestQuality = quality;
 		if (quality < worstQuality)
 			ReadyMeal.worstQuality = quality;
 		//this.expiration = expiration;
 	}
	    //public ReadyMeal() {
			// TODO Auto-generated constructor stub
		//}
		public String getName() {
	    		return name;
	    	}
	    public int getNumServings() {
	    		return numServings;
	    	}
	    public int getBestQuality() {
	    	return bestQuality;
	    }
	    public int getWorstQuality() {
	    	return worstQuality;
	    }
	    //public int getNumKitchens() {
	    //	return numKitchens;
	    //}
	    public MarsClock getExpiration() {
 		return expiration;
 	}
		public void setNumServings(int d) {
			numServings = d + numServings;
		}
 
	}