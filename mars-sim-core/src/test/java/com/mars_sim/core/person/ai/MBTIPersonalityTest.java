package com.mars_sim.core.person.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;

public class MBTIPersonalityTest {

    @BeforeAll 
    static void setUp() {
        SimulationConfig.loadConfig();
    }

    @Test
    void testGetName() {
        var seenNames = new HashSet<String>();

        for (MBTIType type : MBTIType.values()) {
            var name = type.getName();
            assertNotNull(name, "Name null for " + type.name());
            assertNotEquals(type.name(), name.toUpperCase(), "Name should not match enum constant");
            seenNames.add(name);
        }
        assertEquals(MBTIType.values().length, seenNames.size(), "All MBTI type unique names should be seen");
    }

    @Test
    void testSetRandomMBTI() {
        var personality = new MBTIPersonality();

        // Attempt to see all MBTI types by repeatedly setting random MBTI types
        // Make 20 per type
        int attempts = MBTIType.values().length * 20;
        Set<MBTIType> seenTypes = new HashSet<>();
        for (int i = 0; i < attempts; i++) {
            personality.setRandomMBTI();
            seenTypes.add(personality.getType());
        }
        assertEquals(MBTIType.values().length, seenTypes.size(), "All MBTI types should be seen after enough attempts");
    }

    @Test
    void testMBTIType() {
        assertTrue(MBTIType.ISTP.isIntrovert(), "Is Introvert");
        assertTrue(MBTIType.ESTP.isExtrovert(), "Is Extrovert");
        assertTrue(MBTIType.ISFP.isSensor(), "Is Sensor");
        assertTrue(MBTIType.INFP.isIntuitive(), "Is Intuitive");
        assertTrue(MBTIType.ISTJ.isThinker(), "Is Thinker");
        assertTrue(MBTIType.ISFJ.isFeeler(), "Is Feeler");
        assertTrue(MBTIType.INTJ.isJudger(), "Is Judger");
        assertTrue(MBTIType.INFP.isPerceiver(), "Is Perceiver");

        assertFalse(MBTIType.ISTP.isExtrovert(), "Is not Extrovert");
        assertFalse(MBTIType.ESTP.isIntrovert(), "Is not Introvert");
        assertFalse(MBTIType.INFP.isSensor(), "Is not Sensor");
        assertFalse(MBTIType.ISFP.isIntuitive(), "Is not Intuitive");
        assertFalse(MBTIType.ISFJ.isThinker(), "Is not Thinker");
        assertFalse(MBTIType.ISTJ.isFeeler(), "Is not Feeler");
        assertFalse(MBTIType.INTP.isJudger(), "Is not Judger");
        assertFalse(MBTIType.ISTJ.isPerceiver(), "Is not Perceiver");
    }

    @Test
    void testGetPersonalityDifference() {
        var source = new MBTIPersonality();
        source.setType(MBTIType.ISTP);
        assertEquals(MBTIType.ISTP, source.getType(), "The type should match the set type");

        assertEquals(0, source.getPersonalityDifference(MBTIType.ISTP), "Personality difference with itself should be 0");
        assertEquals(4, source.getPersonalityDifference(MBTIType.ENFJ), "Personality difference with opposite type should be 4");
        assertEquals(2, source.getPersonalityDifference(MBTIType.ISFJ), "Personality difference with partially different type should be 2");
        assertEquals(3, source.getPersonalityDifference(MBTIType.INFJ), "Personality difference with partially different type should be 3");
    }
}
