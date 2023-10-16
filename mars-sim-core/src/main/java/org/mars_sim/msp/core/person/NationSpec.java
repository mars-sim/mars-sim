/*
 * Mars Simulation Project
 * NationSpec.java
 * @date 2023-07-23
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.configuration.UserConfigurable;
import org.mars_sim.tools.util.RandomUtil;

/**
 * This class defines the attribute of a country. It also store commonly 
 * used names for creating settlers.
 */
public class NationSpec implements UserConfigurable {
	
	private final String PERSON_NO = "Person #";
	
	private boolean bundled;
	
	private double gdp;
	private double ppp;
	private double pop;
	private double growth;
	
	private String scheme;
	
	private List<String> firstMale = new ArrayList<>();
	private List<String> firstFemale = new ArrayList<>();
	private List<String> lastNames = new ArrayList<>();
	
	public NationSpec(String scheme, boolean bundled) {
		this.scheme = scheme;
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
		return scheme;
	}

	@Override
	public String getName() {
		return scheme;
	}

	@Override
	public boolean isBundled() {
		return bundled;
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