package org.mars_sim.msp.core.person;

import java.util.HashSet;
import java.util.Set;

import org.mars.sim.tools.util.RandomUtil;

import junit.framework.TestCase;

public class PersonNameSpecConfigTest extends TestCase {

    // Need a better way to discover the files bundled
    private String[] COUNTRIES = {"Austria",  "Belgium", "Brazil", "Canada", "China", "Czech Republic",
                                    "Denmark", "Estonia", "Finland", "France", "Germany", "Greece",
                                    "Hungary", "India", "Ireland", "Italy", "Japan", "Luxembourg",
                                    "Norway", "Poland", "Portugal", "Romania", "Russia",
                                    "South Korea", "Spain", "Sweden", "Switzerland", "The Netherlands",
                                    "UK", "USA"};

    private PersonNameSpecConfig config;

    @Override
    public void setUp() {
        config = new PersonNameSpecConfig();
    }

    public void testGenerateName() {
        for(String c : COUNTRIES) {
            generateForCountry(c,RandomUtil.getRandomInt(20,30));
        }
    }

    private void generateForCountry(String country, int names) {
        Set<String> existing = new HashSet<>();
        PersonNameSpec ns = config.getItem(country);
        assertNotNull("Name spec for " + country, ns);
        for(int i = 0; i < names; i++) {
            String newName = ns.generateName(GenderType.MALE, existing);
            assertFalse("New name does not exist", existing.contains(newName));
            assertFalse("Generated name", newName.contains("#"));
            existing.add(newName);
        }
    }
}
