/**
 * Mars Simulation Project
 * ReadyMealMenu.java
 * @version 3.07 2014-12-02
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.cooking.ReadyMeal;

// 2014-12-01 Created ReadyMealMenu class
public class ReadyMealMenu implements Serializable {

	    /** default serial id. */
	private static final long serialVersionUID = 1L;

 	List<ReadyMeal> readyMealList = new ArrayList<ReadyMeal>();

 	//String nameOfMeal;
 	
	public void add(String name, int numServings, int quality, MarsClock clock) {
			readyMealList.add(new ReadyMeal(name, numServings, quality, clock));
		}
	public int size() {
			return readyMealList.size();
		}

	public void change(String name, int nowServings, int quality) {
		Iterator<ReadyMeal> i = readyMealList.iterator();
		while (i.hasNext()) {
			ReadyMeal existingMeal = i.next();
			String existingName = existingMeal.getName();
			
			if (name == existingName) {
				//existingMeal.setNumServings(nowServings + existingMeal.getNumServings());
				existingMeal.addNumServings(nowServings);
			}
		} 
	}

	
	public String getMealName(int row) {
		return readyMealList.get(row).getName();
	}
	
	public int getNumServings(int row) {
		return readyMealList.get(row).getNumServings();
	}
	
	public int getBestQuality(int row) {
		return readyMealList.get(row).getBestQuality();
	}

	public int getWorstQuality(int row) {
		return readyMealList.get(row).getWorstQuality();
	}
	
	public List<ReadyMeal> getReadyMealList() {
			return readyMealList;
		}
	}
