/*
 * Mars Simulation Project
 * Nation.java
 * @date 2023-10-08
 * @author Manny Kung
 */

package com.mars_sim.core.authority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.Colonist;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;

public class Nation implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Nation.class.getName());

	// See https://www.investopedia.com/insights/worlds-top-economies/
	
	/**
	 * Gross Domestic Product (GDP) is the standard measure of the value 
	 * added created through the production of goods and services 
	 * in a country during a certain period. 
	 * 
	 * As such, it also measures the income earned from that production, 
	 * or the total amount spent on final goods and services 
	 * (less imports).
	 */
	private double GDP;
	/**
	 * The Purchasing Power Parity (PPP) is the adjusted GDP in 
	 * Current International Dollars. 
	 * 
	 * It's an alternative way of comparing nominal GDP among countries, 
	 * adjusting currencies based on what basket of goods they could 
	 * buy in those countries rather than currency exchange rates. 
	 * 
	 * This is a way to adjust for the difference in the cost of living 
	 * among countries.
	 */
	private double PPP;
	/**
	 * The annual percentage growth rate of nominal GDP in local prices and currencies, which estimates how fast a country’s economy is growing.
	 */
	private double annualGDPGrowth;
	/**
	 * Nominal GDP divided by the number of people in a country. 
	 */
	private double GDPPerCapita;
	/**
	 * Population in millions
	 */
	private double population;
	/**
	 * Space expenditure in millions
	 */
	private double spaceExpenditure;
	
	private String name;
	
	private Set<Colonist> colonists = new HashSet<>();
	
	public Colonist getOneColonist() {
		List<Colonist> list = new ArrayList<>(colonists);
		if (!list.isEmpty()) {
			int rand = RandomUtil.getRandomInt(list.size() - 1);
			return list.get(rand);
		}
		return null;
	}
	
	public Nation(String name) {
		this.name = name;
	}
	
	public void addColonist(Colonist colonist) {
		colonists.add(colonist);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {

		return false;
	}


}
