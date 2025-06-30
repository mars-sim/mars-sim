package com.mars_sim.core.preferences;

import java.util.HashMap;
import java.util.Map;


import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.parameter.ParameterCategory;
import com.mars_sim.core.parameter.ParameterValueType;
import com.mars_sim.core.person.ai.mission.MissionWeightParameters;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.meta.ScienceParameters;
import com.mars_sim.core.person.ai.task.util.MetaTaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskParameters;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.ProcessParameters;

import junit.framework.TestCase;

public class ParameterCategoryTest extends TestCase {
    private static final String CAT_NAME = "Test";
    private static final int VALUE_COUNT = 5;
    private static final String DISPLAY_PREFIX = "Display_";
    private static final String ID_PREFIX = "key_";

	class TestCategory extends ParameterCategory {

        private static final long serialVersionUID = 1L;

		public TestCategory() {
            super(CAT_NAME);
        }

        @Override
        protected Map<String, ParameterSpec> calculateSpecs() {
            var types = ParameterValueType.values();
            Map<String, ParameterSpec> result = new HashMap<>();
            for(int i = 0; i < VALUE_COUNT; i++) {
                var id = ID_PREFIX + i;
                result.put(id, new ParameterSpec(id, DISPLAY_PREFIX + id, types[i % types.length]));
            }
            return result;
        }
    }

    public void testGetRange() {
        ParameterCategory cat = new TestCategory();
        assertEquals("Category name", CAT_NAME, cat.getId());

        var range = cat.getRange();
        assertEquals("Number of values", VALUE_COUNT, range.size());
        for(var v : range) {
            assertEquals("Value name", DISPLAY_PREFIX + v.id(), v.displayName());
        }
    }

    public void testGetSpec() {
        ParameterCategory cat = new TestCategory();
        var types = ParameterValueType.values();

        for(int i = 0; i < VALUE_COUNT; i++) {
            var id = ID_PREFIX + i;
            var s = cat.getSpec(id);
            assertEquals("Id of Value #" + i, id, s.id());
            assertEquals("Type of Value #" + i, types[i % types.length], s.type());
        }
    }

    public void testFixedCategories() {
        assertEquals("Mission values", MissionType.values().length,
                                    MissionWeightParameters.INSTANCE.getRange().size());

        assertEquals("Process values", OverrideType.values().length,
                                    ProcessParameters.INSTANCE.getRange().size());

        assertEquals("Science values", ScienceType.values().length,
                                    ScienceParameters.INSTANCE.getRange().size());

        SimulationConfig.loadConfig();
        MetaTaskUtil.initializeMetaTasks();
        assertEquals("Science values", MetaTaskUtil.getAllMetaTasks().size(),
                                    TaskParameters.INSTANCE.getRange().size());
    }
}
