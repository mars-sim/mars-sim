/*
 * Mars Simulation Project
 * PersonNameSpec.java
 * @date 2023-07-23
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.configuration.UserConfigurable;

/**
 * Represents a naming schem for persons. It contains a set of gender neutral surnames and 
 * gender specific forenames.
 * Usually these represent a counry/language bias.
 */
public class PersonNameSpec implements UserConfigurable {
	private List<String> firstMale = new ArrayList<>();
	private List<String> firstFemale = new ArrayList<>();
	private List<String> last = new ArrayList<>();
	private String scheme;
	private boolean bundled;

	public PersonNameSpec(String scheme, boolean bundled) {
		this.scheme = scheme;
		this.bundled = bundled;
	}

	void addLastName(String name) {
		last.add(name);
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
	 * Generate a unqiue name using this naming scheme
	 * @param gender Gener of the name
	 * @param existingNames Existign names to avoid
	 * @return
	 */
	public String generateName(GenderType gender, Set<String> existingNames) {
				// Setup name ranges
		List<String> firstList = null;
		if (gender == GenderType.MALE) {
			firstList = firstMale;
		} else {
			firstList = firstFemale;
		}

		int attemptCount = 100;
		// Attempt to find a unique combination
		while (attemptCount-- > 0) {
			int rand0 = RandomUtil.getRandomInt(last.size() - 1);
			int rand1 = RandomUtil.getRandomInt(firstList.size() - 1);

			String fullname = firstList.get(rand1) + " " + last.get(rand0);

			// double checking if this name has already been in use
			if (!existingNames.contains(fullname)) {
				return fullname;
			}
		}

		return "Person #" + existingNames.size();
	}
}