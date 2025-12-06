/*
 * Mars Simulation Project
 * NameGenerator.java
 * @date 2025-12-06
 * @author Barry Evans
 */
package com.mars_sim.core.authority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mars_sim.core.tool.RandomUtil;

/**
 * Generates names from a list of potential names.
 * It allows names to be excluded from the potential list.
 */
public class NameGenerator {
    private List<String> potentials;
    
    NameGenerator(List<String> potentials) {
        this.potentials = potentials;
    }

    /**
     * Get the list of potential names.
     * @return
     */
    public List<String> getPotentials() {
        return potentials;
    }

    /**
     * Generate a random name from the list of potential names.
     * Some namaes are excluded.
     * @param excluded Names excluded.
     * @return
     */
    public String generateName(Collection<String> excluded) {

		List<String> candidateNames = new ArrayList<>(potentials);
		candidateNames.removeAll(excluded);

        return RandomUtil.getRandomElement(candidateNames);
    }
}
