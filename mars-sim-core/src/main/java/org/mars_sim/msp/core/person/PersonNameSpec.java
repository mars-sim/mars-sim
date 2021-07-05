package org.mars_sim.msp.core.person;

import java.util.ArrayList;
import java.util.List;

public final class PersonNameSpec {
	private List<String> firstMale = new ArrayList<>();
	private List<String> firstFemale = new ArrayList<>();
	private List<String> last = new ArrayList<>();
	
	public void addLastName(String name) {
		last.add(name);
	}
	
	public void addMaleName(String name) {
		firstMale.add(name);	
	}
	
	public void addFemaleName(String name) {
		firstFemale.add(name);
	}

	public List<String> getLastNames() {
		return last;
	}

	public List<String> getFemaleNames() {
		return firstFemale;
	}

	public List<String> getMaleNames() {
		return firstMale;
	}
}