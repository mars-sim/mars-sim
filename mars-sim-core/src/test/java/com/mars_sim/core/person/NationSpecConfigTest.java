package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.tool.RandomUtil;

public class NationSpecConfigTest {
    	
    private NationSpecConfig config;
    
    @BeforeEach
    public void setUp() {
        var simConfig = SimulationConfig.loadConfig();
        config = new NationSpecConfig(simConfig);
    }

    @Test
    void testGenerateName() {
        for (String c : config.getCountries()) {
            generateForCountry(c, RandomUtil.getRandomInt(20, 30));
        }
    }

    private void generateForCountry(String country, int names) {
        Set<String> existing = new HashSet<>();
        NationSpec ns = config.getItem(country);
        assertNotNull(ns, "Name spec for " + country);
        for (int i = 0; i < names; i++) {
            String newName = ns.generateName(GenderType.MALE, existing);
            assertFalse(existing.contains(newName), "New name does not exist");
            assertFalse(newName.contains("#"), "Generated name");
            existing.add(newName);
        }
    }

    @Test
    void testGetNorway() {
        testNationSpec("Norway", true);
    }

    @Test
    void testGetSpain() {
        testNationSpec("Spain", true);
    }

    private void testNationSpec(String name, boolean hasCharacterisitcs) {
        var n = config.getItem(name);

        assertNotNull(n, name + " found");
        assertEquals(name, n.getName(), "Name");
        if (hasCharacterisitcs) {
            assertNotNull(n.getPopulation(), " characteristics present");
        }
        else {
            assertNull(n.getPopulation(), " characteristics not present");
        }

        assertTrue(n.getGDP() > 0, name + " GDP");
    }
}
