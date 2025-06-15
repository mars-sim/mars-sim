/**
 * Mars Simulation Project
 * MissionProjectTest.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.structure.Settlement;

public class MissionProjectTest extends AbstractMarsSimUnitTest {
    /**
     *
     */
    private static final String MISSION_NAME = "test-mission";
    private static final double OXYGEN_VALUE = 10D;
    private static final double FOOD_VALUE = 22D;

    @SuppressWarnings("serial")
	class TestStep extends MissionStep {

        private double oxygen;
        private double food;

        public TestStep(TestMission testMission, int stepId, double oxygen, double food) {
            super(testMission, Stage.ACTIVE, Integer.toString(stepId));

            this.oxygen = oxygen;
            this.food = food;
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
        protected void getRequiredResources(SuppliesManifest manifest, boolean optionl) {
            manifest.addAmount(ResourceUtil.OXYGEN_ID, oxygen, true);
            manifest.addAmount(ResourceUtil.FOOD_ID, food, true);
        }
    }

    @SuppressWarnings("serial")
	class TestMission extends MissionProject {

        public TestMission(String name, Person leader, int numSteps) {
            super(name, MissionType.AREOLOGY, 1, 1, 1, leader);
         
            List<MissionStep> steps = new ArrayList<>();
            for(int i = 0; i < numSteps; i++) {
                steps.add(new TestStep(this, i, OXYGEN_VALUE, FOOD_VALUE));
            }
            setSteps(steps);
        }
    }

    public void testCreation() {
        Settlement home = buildSettlement();
        Person leader = buildPerson("Leader", home);
        MissionProject mp = new TestMission(MISSION_NAME, leader, 0);

        assertEquals("Mission name", MISSION_NAME, mp.getName());
        assertEquals("Mission settlement", home, mp.getAssociatedSettlement());
        assertEquals("Mission leader", leader, mp.getStartingPerson());
    }

    public void testResources() {
        Settlement home = buildSettlement();
        Person leader = buildPerson("Leader", home);
        int numSteps = 3;
        TestMission mp = new TestMission(MISSION_NAME, leader, numSteps);

        SuppliesManifest manifest = mp.getResources(true);
        Map<Integer,Double> needed = manifest.getAmounts(true);
        assertEquals("Oxygen needed", numSteps * OXYGEN_VALUE, needed.get(ResourceUtil.OXYGEN_ID));
        assertEquals("Food needed", numSteps * FOOD_VALUE, needed.get(ResourceUtil.FOOD_ID));
        assertEquals("Number of resources needed", 2, needed.size());
    }
}
