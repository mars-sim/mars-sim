/**
 * Mars Simulation Project
 * MissionProjectTest.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;


import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;

public class MissionProjectTest extends AbstractMarsSimUnitTest {
    /**
     *
     */
    private static final String MISSION_NAME = "test-mission";
    private static final double OXYGEN_VALUE = 10D;
    private static final double FOOD_VALUE = 22D;

    class TestStep extends MissionStep {

        private Map<Integer, Number> resources;

        public TestStep(TestMission testMission, int stepId, Number oxygen, Number food) {
            super(testMission, Stage.ACTIVE, Integer.toString(stepId));

            resources = new HashMap<>();
            resources.put(ResourceUtil.oxygenID, oxygen);
            resources.put(ResourceUtil.foodID, food);
        }

        /**
         * Test mission step that completes after a single executon
         */
        @Override
        protected boolean execute(Worker worker) {
             return true;
        }

        /**
         * Get the resources
         * @return
         */
        @Override
        Map<Integer, Number> getRequiredResources() {
            return resources;
        }
    }

    class TestMission extends MissionProject {

        public TestMission(String name, Person leader) {
            super(name, MissionType.AREOLOGY, 1, 1, leader);
        }

        public void addTestStep(int stepId, Number oxygen, Number food) {
            addMissionStep(new TestStep(this, stepId, oxygen, food));
        }

    }

    public void testCreation() {
        Settlement home = buildSettlement();
        Person leader = buildPerson("Leader", home);
        MissionProject mp = new TestMission(MISSION_NAME, leader);

        assertEquals("Mission name", MISSION_NAME, mp.getName());
        assertEquals("Mission settlement", home, mp.getAssociatedSettlement());
        assertEquals("Mission leader", leader, mp.getStartingPerson());
    }

    public void testResources() {
        Settlement home = buildSettlement();
        Person leader = buildPerson("Leader", home);
        TestMission mp = new TestMission(MISSION_NAME, leader);

        int steps = 3;
        for(int i = 0; i < steps; i++) {
            mp.addTestStep(i, OXYGEN_VALUE, FOOD_VALUE);
        }

        Map<Integer,Number> needed = mp.getResources();
        assertEquals("Oxygen needed", steps * OXYGEN_VALUE, needed.get(ResourceUtil.oxygenID));
        assertEquals("Food needed", steps * FOOD_VALUE, needed.get(ResourceUtil.foodID));
        assertEquals("Number of resources needed", 2, needed.size());
    }
}
