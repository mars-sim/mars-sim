package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ThirstLevelTest {
    @Test
    void testFromValue() {
        assertEquals(ThirstLevel.ISOTONIC, ThirstLevel.fromValue(0));
        assertEquals(ThirstLevel.WANT_A_SIP, ThirstLevel.fromValue(200));
        assertEquals(ThirstLevel.DRY, ThirstLevel.fromValue(800));
        assertEquals(ThirstLevel.BONE_DRY, ThirstLevel.fromValue(1500));
        assertEquals(ThirstLevel.DESICCATED, ThirstLevel.fromValue(2000));
    }
}
