package com.mars_sim.core.person;

import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.tool.RandomUtil;

import junit.framework.TestCase;

public class NationSpecConfigTest extends TestCase {
    	
    private NationSpecConfig config;
    
    @Override
    public void setUp() {
        var simConfig = SimulationConfig.instance();
        simConfig.loadConfig();
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

    public void testGetNorway() {
        testNationSpec("Norway", true);
    }

    public void testGetSpain() {
        testNationSpec("Spain", true);
    }

    private void testNationSpec(String name, boolean hasCharacterisitcs) {
        var n = config.getItem(name);

        assertNotNull(name + " found", n);
        assertEquals("Name", name, n.getName());
        if (hasCharacterisitcs) {
            assertNotNull(" characteristics present", n.getPopulation());
        }
        else {
            assertNull(" characteristics not present", n.getPopulation());
        }

        assertTrue(name + " GDP", n.getGDP() > 0);
    }
}
