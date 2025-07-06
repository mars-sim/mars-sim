/*
 * Mars Simulation Project
 * PopulationStats.java
 * @date 2025-07-05
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.util.Collection;
import java.util.stream.Collectors;

import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;

/**
 * Helper class to prodice statistics on a populatino of Person.
 */
public final class PopulationStats {

    private PopulationStats() {
        // Prevent instantiation
    }

    /**
	 * Get the average age based on years
	 * @param population The population to assess
	 * @return
	 */
	public static double getAverageAge(Collection<Person> population) {
		return population.stream()
				.mapToInt(Person::getAge)
                .average()
                .orElse(0.0);
	}
	
	/**
	 * Gets the statistical string showing the gender ratio.
	 * @param population The population to assess
	 * @return
	 */
	public static String getGenderRatioAsString(Collection<Person> population) {
        var split = population.stream()
            .collect(Collectors.groupingBy(Person::getGender, Collectors.counting()));
		long male = split.getOrDefault(GenderType.MALE, 0L);
		long female = split.getOrDefault(GenderType.FEMALE, 0L);
		
		double ratio = 1D;
		
		if ((male == 0) && (female == 0))
			ratio = 0.5;
		else if ((female >= 0) && (male >= 0)) {
			ratio = Math.round(1.0 * male/female * 100.0)/100.0;
        }
		
		return male + " to " + female + " (" + ratio + ")";
	}
}
