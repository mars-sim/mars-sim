package com.mars_sim.core.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.MissionWeightParameters;
import com.mars_sim.core.person.ai.task.meta.ScienceParameters;
import com.mars_sim.core.person.ai.task.util.MetaTaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskParameters;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.ProcessParameters;


class ParameterCategoryTest {
    private static final String CAT_NAME = "Test";
    private static final int VALUE_COUNT = 5;
    private static final String DISPLAY_PREFIX = "Display_";
    private static final String KEY_PREFIX = "key_";

	class TestCategory extends ParameterCategory {

        private static final long serialVersionUID = 1L;

		public TestCategory() {
            super(CAT_NAME);

            var types = ParameterValueType.values();
            for(int i = 0; i < VALUE_COUNT; i++) {
                var id = KEY_PREFIX + i;
                addParameter(id, DISPLAY_PREFIX + id, types[i % types.length]);
            }
        }
    }

    @Test
    void testGetRange() {
        ParameterCategory cat = new TestCategory();
        assertEquals(CAT_NAME, cat.getId(), "Category name");

        var range = cat.getRange();
        assertEquals(VALUE_COUNT, range.size(), "Number of values");
        for(var v : range.entrySet()) {
            assertEquals(DISPLAY_PREFIX + v.getKey().id(), v.getValue().displayName(), "Value name");
        }
    }

    @Test
    void testGetSpec() {
        ParameterCategory cat = new TestCategory();
        var types = ParameterValueType.values();

        for(int i = 0; i < VALUE_COUNT; i++) {
            var id = KEY_PREFIX + i;
            var key = cat.getKey(id);
            assertNotNull(key, "Key of Value #" + i);
            assertEquals(cat, key.category(), "Category of Value #" + i);
            assertEquals(id, key.id(), "Id of Value #" + i);

            var s = cat.getSpec(key);
            assertEquals(types[i % types.length], s.type(), "Type of Value #" + i);
        }
    }

    @Test
    void testFixedCategories() {
        assertEquals(MissionType.values().length, 
                     MissionWeightParameters.INSTANCE.getRange().size(),
                     "Mission values");

        assertEquals(OverrideType.values().length,
                     ProcessParameters.INSTANCE.getRange().size(),
                     "Process values");

        assertEquals(ScienceType.values().length,
                     ScienceParameters.INSTANCE.getRange().size(),
                     "Science values");

        SimulationConfig.loadConfig();
        MetaTaskUtil.initializeMetaTasks();
        assertEquals(MetaTaskUtil.getAllMetaTasks().size(),
                     TaskParameters.INSTANCE.getRange().size(),
                     "Task values");
    }
}
