/*
 * Mars Simulation Project
 * NationSpec.java
 * @date 2023-07-23
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.configuration.UserConfigurable;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class defines the attribute of a country. It also store commonly 
 * used names for creating settlers.
 */
public class NationSpec implements UserConfigurable {
	
	private static final String PERSON_NO = "Person #";
	
	private boolean bundled;
	
	private double gdp;
	private double ppp;
	private double pop;
	private double growth;
	
	private String country;
	
	private List<String> firstMale = new ArrayList<>();
	private List<String> firstFemale = new ArrayList<>();
	private List<String> lastNames = new ArrayList<>();

	private PopulationCharacteristics population = null;

	
	public NationSpec(String country, boolean bundled) {
		this.country = country;
		this.bundled = bundled;
	}

	double getGDP() {
		return gdp;
	}
	
	double getPPP() {
		return ppp;
	}
	
	double getPop() {
		return pop;
	}
	
	double getGrowth() {
		return growth;
	}
	
	void addData(double gdp, double ppp, double pop, double growth) {
		this.gdp = gdp;
		this.ppp = ppp;
		this.pop = pop;
		this.growth = growth;
	}
	
	void setPeople(PopulationCharacteristics population) {
		this.population = population;
	}

	void addLastName(String name) {
		lastNames.add(name);
	}
	
	void addMaleName(String name) {
		firstMale.add(name);	
	}
	
	void addFemaleName(String name) {
		firstFemale.add(name);
	}

	@Override
	public String getDescription() {
		return getName();
	}

	@Override
	public String getName() {
		return country;
	}

	@Override
	public boolean isBundled() {
		return bundled;
	}

	/**
	 * Gets the population characteristics of this nation.
	 * 
	 * @return
	 */
	public PopulationCharacteristics getPopulation() {
		return population;
	}

	/**
	 * Generates a unique name using this naming scheme.
	 * 
	 * @param gender Gender of the name
	 * @param existingNames Existing names to avoid
	 * @return
	 */
	public String generateName(GenderType gender, Set<String> existingNames) {
		// Set up name ranges
		List<String> firstList = null;
		if (gender == GenderType.MALE) {
			firstList = firstMale;
		} else {
			firstList = firstFemale;
		}

		int attemptCount = 100;
		// Attempt to find a unique combination
		while (attemptCount-- > 0) {
			int rand0 = RandomUtil.getRandomInt(lastNames.size() - 1);
			int rand1 = RandomUtil.getRandomInt(firstList.size() - 1);

			String firstStr = firstList.get(rand1);
			String lastStr = lastNames.get(rand0);
			
			if (!firstStr.equalsIgnoreCase(lastStr)) {
	
				String fullname = firstStr + " " + lastStr;
	
				// Double check if this name has already been in use
				if (!existingNames.contains(fullname)) {
					return fullname;
				}
			}
		}

		return PERSON_NO + existingNames.size();
	}
}