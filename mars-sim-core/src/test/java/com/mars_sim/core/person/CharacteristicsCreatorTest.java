package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CharacteristicsCreatorTest {

    private static final String [] BLOOD_TYPES = {"A", "B", "AB", "O"};

    @Test
    void testCalculateBloodType() {

        Set<String> targetSet = new HashSet<String>(Arrays.asList(BLOOD_TYPES));

        for (int i = 0; i < 100; i++) {
            String bloodType = CharacteristicsCreator.calculateBloodType();
        
            var rhType = bloodType.substring(bloodType.length() - 1, bloodType.length());
            assertTrue(rhType.equals("+") || rhType.equals("-"), "Invalid Rh type: " + rhType);

            String type = bloodType.substring(0, bloodType.length() - 1);
            assertTrue(targetSet.contains(type), "Invalid blood type: " + type);
        }
    }
}
