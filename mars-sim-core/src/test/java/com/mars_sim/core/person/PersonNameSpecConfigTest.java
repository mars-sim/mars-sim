/*
 * Mars Simulation Project
 * PersonNameSpecConfigTest.java
 * @date 2023-07-23
 * @author Barry Evans
 */

package com.mars_sim.core.person;

import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.tool.RandomUtil;

import junit.framework.TestCase;

public class PersonNameSpecConfigTest extends TestCase {
	
    private NationSpecConfig config;
    
    @Override
    public void setUp() {
        var simConfig = SimulationConfig.loadConfig();
        config = new NationSpecConfig(simConfig);
    }

    public void testGenerateName() {
        for (String c : config.getCountries()) {
            generateForCountry(c, RandomUtil.getRandomInt(20, 30));
        }
    }

    private void generateForCountry(String country, int names) {
        Set<String> existing = new HashSet<>();
        NationSpec ns = config.getItem(country);
        assertNotNull("Name spec for " + country, ns);
        for (int i = 0; i < names; i++) {
            String newName = ns.generateName(GenderType.MALE, existing);
            assertFalse("New name does not exist", existing.contains(newName));
            assertFalse("Generated name", newName.contains("#"));
            existing.add(newName);
        }
    }
}
