package com.mars_sim.core.preferences;

import java.util.List;

import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterManager;
import com.mars_sim.core.person.ai.mission.MissionWeightParameters;
import com.mars_sim.core.person.ai.task.meta.ScienceParameters;

import junit.framework.TestCase;

public class ParameterManagerTest extends TestCase {
    /**
     *
     */
    private static final ParameterCategory CATEGORY = ScienceParameters.INSTANCE;
    private static final ParameterCategory CATEGORY2 = MissionWeightParameters.INSTANCE;

    private static final String KEY1 = "Bool";
    private static final String KEY2 = "Bool2";
    private static final String KEY3 = "Key3";
    private static final String KEY4 = "Key4";
    private static final int LOW_INT = 59;
    private static final int HIGH_INT = 89;
    private static final double HIGH_DOUBLE = 78.99;
    private static final double LOW_DOUBLE = 12.34D;



    public void testGetBooleanValue() {
        var mgr = new ParameterManager();

        mgr.putValue(CATEGORY, KEY1, Boolean.TRUE);

        assertTrue("Boolean value", mgr.getBooleanValue(CATEGORY, KEY1, false));
    }

    public void testGetDoubleValue() {
        var mgr = new ParameterManager();

        mgr.putValue(CATEGORY, KEY1, LOW_DOUBLE);

        assertEquals("Double value", LOW_DOUBLE, mgr.getDoubleValue(CATEGORY, KEY1, 0));
    }

    public void testGetIntValue() {
       var mgr = new ParameterManager();

        mgr.putValue(CATEGORY, KEY1, LOW_INT);

        assertEquals("Integer value", LOW_INT, mgr.getIntValue(CATEGORY, KEY1, 0));
    }

    public void testUpdateIntValue() {
        var mgr = new ParameterManager();
 
         mgr.putValue(CATEGORY, KEY1, LOW_INT);
         assertEquals("Integer value", LOW_INT, mgr.getIntValue(CATEGORY, KEY1, 0));

         mgr.putValue(CATEGORY, KEY1, HIGH_INT);
         assertEquals("Integer after update", HIGH_INT, mgr.getIntValue(CATEGORY, KEY1, 0));
     }

     public void testRemoveIntValue() {
        var mgr = new ParameterManager();
 
         mgr.putValue(CATEGORY, KEY1, LOW_INT);
        mgr.removeValue(CATEGORY, KEY1);
         assertEquals("Integer after removal", 0, mgr.getIntValue(CATEGORY, KEY1, 0));
     }
     
    public void testPurCategories() {
        var mgr = new ParameterManager();

        mgr.putValue(CATEGORY, KEY1, Boolean.TRUE);
        mgr.putValue(CATEGORY2, KEY1, Boolean.FALSE);
        assertTrue("Boolean value 1", mgr.getBooleanValue(CATEGORY, KEY1, false));
        assertFalse("Boolean value 2", mgr.getBooleanValue(CATEGORY2, KEY1, true));

        assertEquals("Preference value", 2, mgr.getValues().size());
    }

    public void testGetValues() {
        var mgr = new ParameterManager();

        mgr.putValue(CATEGORY, KEY1, Boolean.TRUE);
        mgr.putValue(CATEGORY, KEY2, Boolean.FALSE);

        assertEquals("Preference value", 2, mgr.getValues().size());
    }

    public void testResetAll() {
        var mgr = new ParameterManager();

        mgr.putValue(CATEGORY, KEY1, LOW_DOUBLE);

        var mgr2 = new ParameterManager();
        mgr2.putValue(CATEGORY, KEY1, Boolean.TRUE);
        mgr2.putValue(CATEGORY, KEY2, Boolean.FALSE);

        mgr.resetValues(mgr2);
        assertEquals("Preference values", mgr2.getValues(), mgr.getValues());
    }

    public void testCombine() {
        var mgr = new ParameterManager();
        mgr.putValue(CATEGORY, KEY1, LOW_DOUBLE);
        mgr.putValue(CATEGORY, KEY2, HIGH_INT);
        mgr.putValue(CATEGORY, KEY3, Boolean.TRUE);


        var mgr2 = new ParameterManager();
        mgr2.putValue(CATEGORY, KEY1, HIGH_DOUBLE);
        mgr2.putValue(CATEGORY, KEY2, LOW_INT);
        mgr.putValue(CATEGORY, KEY4, HIGH_INT);


        // Combine the two managers so all values will be the higher value
        var mgr3 = new ParameterManager(List.of(mgr, mgr2));
        assertEquals("Preference count", 4, mgr3.getValues().size());
        assertEquals("Combined double value", HIGH_DOUBLE + LOW_DOUBLE, mgr3.getDoubleValue(CATEGORY, KEY1, 0));
        assertEquals("Combined int value", HIGH_INT + LOW_INT, mgr3.getIntValue(CATEGORY, KEY2, 0));
        assertTrue("Boolean value", mgr3.getBooleanValue(CATEGORY, KEY3, false));
        assertEquals("Integer value", HIGH_INT, mgr3.getIntValue(CATEGORY, KEY4, 0));

    }

}
