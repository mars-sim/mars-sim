package com.mars_sim.core.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterKey;
import com.mars_sim.core.parameter.ParameterManager;
import com.mars_sim.core.parameter.ParameterValueType;

class ParameterManagerTest {

    private static class TestCategory extends ParameterCategory {
        private static final String DOUBLE1 = "Double1";
        private static final String INT1 = "Int1";
        private static final String BOOL2 = "Bool2";
        private static final String BOOL1 = "Bool1";

        protected ParameterKey bool1;
        protected ParameterKey bool2;
        protected ParameterKey int1;
        protected ParameterKey double1;

		public TestCategory() {
            super("Test");
            bool1 = addParameter(BOOL1, "Boolean 1", ParameterValueType.BOOLEAN);
            bool2 = addParameter(BOOL2, "Boolean 2", ParameterValueType.BOOLEAN);
            int1 = addParameter(INT1, "Int 1", ParameterValueType.INTEGER);
            double1 = addParameter(DOUBLE1, "Double 1", ParameterValueType.INTEGER);
        }
    }

    private static final TestCategory CATEGORY = new TestCategory();

    private static final int LOW_INT = 59;
    private static final int HIGH_INT = 89;
    private static final double HIGH_DOUBLE = 78.99;
    private static final double LOW_DOUBLE = 12.34D;

    @Test
    void testGetBooleanValue() {
        var mgr = new ParameterManager();

        mgr.putValue(CATEGORY.bool1, Boolean.TRUE);

        assertTrue(mgr.getBooleanValue(CATEGORY.bool1, false), "Boolean value");
    }

    @Test
    void testGetDoubleValue() {
        var mgr = new ParameterManager();

        mgr.putValue(CATEGORY.double1, LOW_DOUBLE);

        assertEquals(LOW_DOUBLE, mgr.getDoubleValue(CATEGORY.double1, 0), "Double value");
    }

    @Test
    void testGetIntValue() {
       var mgr = new ParameterManager();

        mgr.putValue(CATEGORY.int1, LOW_INT);

        assertEquals(LOW_INT, mgr.getIntValue(CATEGORY.int1, 0), "Integer value");
    }

    @Test
    void testUpdateIntValue() {
        var mgr = new ParameterManager();
 
         mgr.putValue(CATEGORY.int1, LOW_INT);
         assertEquals(LOW_INT, mgr.getIntValue(CATEGORY.int1, 0), "Integer value");

         mgr.putValue(CATEGORY.int1, HIGH_INT);
         assertEquals(HIGH_INT, mgr.getIntValue(CATEGORY.int1, 0), "Integer after update");
     }

    @Test
    void testRemoveIntValue() {
        var mgr = new ParameterManager();
 
        mgr.putValue(CATEGORY.int1, LOW_INT);
        mgr.removeValue(CATEGORY.int1);
        assertEquals(0, mgr.getIntValue(CATEGORY.int1, 0), "Integer after removal");
    }

    private static final TestCategory CATEGORY2 = new TestCategory();

    @Test
    void testPurCategories() {
        var mgr = new ParameterManager();
        
        mgr.putValue(CATEGORY.bool1, Boolean.TRUE);
        mgr.putValue(CATEGORY2.bool2, Boolean.FALSE);
        assertTrue(mgr.getBooleanValue(CATEGORY.bool1, false), "Boolean value 1");
        assertFalse(mgr.getBooleanValue(CATEGORY2.bool2, true), "Boolean value 2");
        
        assertEquals(2, mgr.getValues().size(), "Preference value");
    }
    
    @Test
    void testGetValues() {
        var mgr = new ParameterManager();

        mgr.putValue(CATEGORY.bool1, Boolean.TRUE);
        mgr.putValue(CATEGORY.bool2, Boolean.FALSE);

        assertEquals(2, mgr.getValues().size(), "Preference value");
    }

    @Test
    void testResetAll() {
        var mgr = new ParameterManager();

        mgr.putValue(CATEGORY.double1, LOW_DOUBLE);

        var mgr2 = new ParameterManager();
        mgr2.putValue(CATEGORY.bool1, Boolean.TRUE);
        mgr2.putValue(CATEGORY.bool2, Boolean.FALSE);

        mgr.resetValues(mgr2);
        assertEquals(mgr2.getValues(), mgr.getValues(), "Preference values");
    }

    @Test
    void testCombine() {
        var mgr = new ParameterManager();
        mgr.putValue(CATEGORY.double1, LOW_DOUBLE);
        mgr.putValue(CATEGORY.int1, HIGH_INT);
        mgr.putValue(CATEGORY.bool1, Boolean.TRUE);

        var mgr2 = new ParameterManager();
        mgr2.putValue(CATEGORY.double1, HIGH_DOUBLE);
        mgr2.putValue(CATEGORY.int1, LOW_INT);
        mgr2.putValue(CATEGORY.bool2, Boolean.FALSE);

        // Combine the two managers so all values will be the higher value
        var mgr3 = new ParameterManager(List.of(mgr, mgr2));
        assertEquals(4, mgr3.getValues().size(), "Preference count");
        assertEquals(HIGH_DOUBLE + LOW_DOUBLE, (Double) mgr3.getDoubleValue(CATEGORY.double1, 0), 0.01, "Combined double value");
        assertEquals(HIGH_INT + LOW_INT, mgr3.getIntValue(CATEGORY.int1, 0), "Combined int value");
        assertTrue(mgr3.getBooleanValue(CATEGORY.bool1, false), "Boolean value");
        assertFalse(mgr3.getBooleanValue(CATEGORY.bool2, true), "Boolean value");

    }

}
