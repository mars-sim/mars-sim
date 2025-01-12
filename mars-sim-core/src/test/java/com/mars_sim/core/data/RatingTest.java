package com.mars_sim.core.data;

import java.util.Map;

import junit.framework.TestCase;
 
public class RatingTest extends TestCase {

    private static final double BASE = 100;
    private static final double BASE2 = 50;

    private static final String MOD1 = "Mod1";
    private static final double MOD1_VALUE = 0.1D;
    private static final String MOD2 = "Mod2";
    private static final double MOD2_VALUE = 0.5D;

    public void testAddModifier() {
        RatingScore r = new RatingScore(BASE);
        assertEquals("Only base", BASE, r.getScore());
        var b = r.getBases();
        assertEquals("Bases present", 1, b.size());
        assertEquals("1st Base", BASE, b.get(RatingScore.BASE));


        r.addModifier(MOD1, MOD1_VALUE);
        assertEquals("Apply " + MOD1, BASE * MOD1_VALUE, r.getScore());

        r.addModifier(MOD2, MOD2_VALUE);
        assertEquals("Apply " + MOD2, BASE * MOD1_VALUE * MOD2_VALUE, r.getScore());

        Map<String, Double> mods = r.getModifiers();
        assertEquals("Number of modifiers", 2, mods.size());
        assertEquals("Value of " + MOD1, MOD1_VALUE, mods.get(MOD1));
        assertEquals("Value of " + MOD2, MOD2_VALUE, mods.get(MOD2));
    }

    public void testSetBase() {
        RatingScore r = new RatingScore("test", BASE);
        r.addModifier(MOD1, MOD1_VALUE);

        r.addBase("test", BASE2);
        assertEquals("Set base " + MOD1, BASE2 * MOD1_VALUE, r.getScore());
    }

    
    public void testAddBase() {
        RatingScore r = new RatingScore("test", BASE);
        r.addModifier(MOD1, MOD1_VALUE);

        r.addBase("tests", BASE2);
        assertEquals("Set base " + MOD1, (BASE + BASE2) * MOD1_VALUE, r.getScore());
    }

    public void testCompare() {
        RatingScore r1 = new RatingScore("test", 2);
        RatingScore r2 = new RatingScore("test", 1);

        assertTrue("Compare base only", r1.compareTo(r2) > 0);

        r2.addModifier(MOD1, 4);
        assertTrue("Compare modifiers", r1.compareTo(r2) < 0);

    }
}
