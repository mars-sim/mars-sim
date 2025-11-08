package com.mars_sim.core.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
 
public class RatingTest {

    private static final double BASE = 100;
    private static final double BASE2 = 50;

    private static final String MOD1 = "Mod1";
    private static final double MOD1_VALUE = 0.1D;
    private static final String MOD2 = "Mod2";
    private static final double MOD2_VALUE = 0.5D;

    @Test
    public void testAddModifier() {
        RatingScore r = new RatingScore(BASE);
        assertEquals(BASE, r.getScore(), "Only base");
        var b = r.getBases();
        assertEquals(1, b.size(), "Bases present");
        assertEquals(BASE, b.get(RatingScore.BASE), "1st Base");


        r.addModifier(MOD1, MOD1_VALUE);
        assertEquals(BASE * MOD1_VALUE, r.getScore(), "Apply " + MOD1);

        r.addModifier(MOD2, MOD2_VALUE);
        assertEquals(BASE * MOD1_VALUE * MOD2_VALUE, r.getScore(), "Apply " + MOD2);

        Map<String, Double> mods = r.getModifiers();
        assertEquals(2, mods.size(), "Number of modifiers");
        assertEquals(MOD1_VALUE, mods.get(MOD1), "Value of " + MOD1);
        assertEquals(MOD2_VALUE, mods.get(MOD2), "Value of " + MOD2);
    }

    @Test
    public void testSetBase() {
        RatingScore r = new RatingScore("test", BASE);
        r.addModifier(MOD1, MOD1_VALUE);

        r.addBase("test", BASE2);
        assertEquals(BASE2 * MOD1_VALUE, r.getScore(), "Set base " + MOD1);
    }

    @Test
    public void testAddBase() {
        RatingScore r = new RatingScore("test", BASE);
        r.addModifier(MOD1, MOD1_VALUE);

        r.addBase("tests", BASE2);
        assertEquals((BASE + BASE2) * MOD1_VALUE, r.getScore(), "Set base " + MOD1);
    }

    @Test
    public void testCompare() {
        RatingScore r1 = new RatingScore("test", 2);
        RatingScore r2 = new RatingScore("test", 1);

        assertTrue(r1.compareTo(r2) > 0, "Compare base only");

        r2.addModifier(MOD1, 4);
        assertTrue(r1.compareTo(r2) < 0, "Compare modifiers");

    }
}
