package com.mars_sim.core.person.ai.task.util;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.structure.Settlement;

public class SettlementTaskManagerTest extends AbstractMarsSimUnitTest {
    // Test settlement task that just record tinme
    private static class TestTask extends SettlementTask {
        private static final RatingScore DEFAULT_SCORE = new RatingScore(10);

        private static int counter = 0;

        private long when;

        protected TestTask(SettlementMetaTask parent, String name, TaskScope scope) {
            super(parent, name, null, DEFAULT_SCORE);
            setScope(scope);
            when = counter++;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + (int) (when ^ (when >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (getClass() != obj.getClass())
                return false;
            TestTask other = (TestTask) obj;
            return (when == other.when);
        }
        
    }

    // Test Meta that create testTasks 
    private static class TestMetaTask extends MetaTask implements SettlementMetaTask  {
        private boolean reject;

        protected TestMetaTask(TaskScope scope, boolean personReject) {
            super(scope.name(), WorkerType.BOTH, scope);
            this.reject = personReject;
        }

        @Override
        public List<SettlementTask> getSettlementTasks(Settlement settlement) {
            List<SettlementTask> tasks = new ArrayList<>();
            for(int i = 0; i < TASKS_PER_META; i++) {
                tasks.add(new TestTask(this, getName() + i, getScope()));
            }
            return tasks;
        }

        /**
         * Simulate Person not being suitable
         */
        @Override
        public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
            if (reject) {
                return RatingScore.ZERO_RATING;
            }
            return t.getScore();
        }
    };

    private static final List<SettlementMetaTask> SCOPE_METATTASKS = List.of(
                            new TestMetaTask(TaskScope.ANY_HOUR, false),
                            new TestMetaTask(TaskScope.WORK_HOUR, false),
                            new TestMetaTask(TaskScope.NONWORK_HOUR, false));

    private static final int TASKS_PER_META = 2;

    /**
     * Build a SettlementTaskManager that uses a number of testMetaTask
     */
    private SettlementTaskManager buildManager(Settlement s, List<SettlementMetaTask> tasks) {
        return new SettlementTaskManager(s) {
            @Override
            protected List<SettlementMetaTask> getMetaTasks() {
                return tasks;
            }
        };
    }

    public void testGetTasks() {
        var s = buildSettlement();
        var manager = buildManager(s, SCOPE_METATTASKS);
        Person p = buildPerson("Worker", s);

        // Triggers the creation of the internal task cache
        manager.getTasks(p);
        var available1 = manager.getAvailableTasks();
        assertEquals("Number of Settlement Tasks", SCOPE_METATTASKS.size() * TASKS_PER_META, available1.size());

        // No refresh so same list
        manager.getTasks(p);
        var available2 = manager.getAvailableTasks();
        assertEquals("Settlement Tasks same on second call", available1, available2);

        // Do a refresh by simulating time passing
        manager.timePassing();
        manager.getTasks(p);
        var available3 = manager.getAvailableTasks();
        assertFalse("Settlement Tasks change after timepassing", available1.equals(available3));

    }

    public void testGetNonWorkTasks() {
        var s = buildSettlement();
        var manager = buildManager(s, SCOPE_METATTASKS);
        Person p = buildPerson("OffDuty", s);
        assertEquals("Person on leave", WorkStatus.OFF_DUTY, p.getShiftSlot().getStatus());


        // Should only be 2 set of tasks, ALL & NonWork
        var available1 = manager.getTasks(p);
        assertEquals("Number of OffDuty Tasks", 2 * TASKS_PER_META, available1.size());
        List<String> scopes = available1.stream().map(TaskJob::getName).toList();
        for(int i = 0; i < TASKS_PER_META; i++) {
            String name = TaskScope.ANY_HOUR.name() + i;
            assertTrue("Task present " + name, scopes.contains(name));
            name = TaskScope.NONWORK_HOUR.name() + i;
            assertTrue("Task present " + name, scopes.contains(name));
        }
    }

    public void testGetWorkTasks() {
        var s = buildSettlement();
        var manager = buildManager(s, SCOPE_METATTASKS);
        Person p = buildPerson("OnDuty", s);
        p.getShiftSlot().setOnCall(true);
        assertEquals("Person on duty", WorkStatus.ON_CALL, p.getShiftSlot().getStatus());

        // Should only be 2 set of tasks, ALL & NonWork
        var available1 = manager.getTasks(p);
        assertEquals("Number of OffDuty Tasks", 2 * TASKS_PER_META, available1.size());
        List<String> scopes = available1.stream().map(TaskJob::getName).toList();
        for(int i = 0; i < TASKS_PER_META; i++) {
            String name = TaskScope.ANY_HOUR.name() + i;
            assertTrue("Task present " + name, scopes.contains(name));
            name = TaskScope.WORK_HOUR.name() + i;
            assertTrue("Task present " + name, scopes.contains(name));
        }
    }

    /**
     * Check that a Peron can reject tasks
     */
    public void testPersonScoring() {
        var s = buildSettlement();

        // One meta rejects and one accepts
        var manager = buildManager(s, List.of(
            new TestMetaTask(TaskScope.ANY_HOUR, true),
            new TestMetaTask(TaskScope.ANY_HOUR, false)));
        Person p = buildPerson("Worker", s);

        // Triggers the creation of the internal task cache
        var selected = manager.getTasks(p);
        var available1 = manager.getAvailableTasks(); 

        assertEquals("Number of Suitable Settlement Tasks", TASKS_PER_META, selected.size());
        assertEquals("Number of Total Settlement Tasks", 2 * TASKS_PER_META, available1.size());
    }
}
