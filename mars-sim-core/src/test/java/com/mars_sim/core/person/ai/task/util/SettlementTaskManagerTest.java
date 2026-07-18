package com.mars_sim.core.person.ai.task.util;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.Entity;
import com.mars_sim.core.TestEntityListener;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.structure.Settlement;

class SettlementTaskManagerTest extends MarsSimUnitTest {
   
    // Test Meta that create testTasks 
    private static class TestMetaTask extends MetaTask implements SettlementMetaTask  {
        private static final RatingScore DEFAULT_SCORE = new RatingScore(10);

        private boolean reject;
        private Set<Entity> focus;
        private int demand = 1;

        protected TestMetaTask(TaskScope scope, boolean personReject, Set<Entity> focus) {
            super(scope.name(), WorkerType.BOTH, scope);
            this.reject = personReject;
            this.focus = focus;
        }

        void setDemand(int demand) {
            this.demand = demand;
        }

        @Override
        public List<SettlementTask> getSettlementTasks(Settlement settlement) {
            List<SettlementTask> tasks = new ArrayList<>();
            for(var f : focus) {
                var task =new MockSettlementTask(this, settlement, getName(), f, DEFAULT_SCORE, getScope());
                task.updateDemand(demand);

                tasks.add(task);
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
            return new RatingScore(t.getScore());
        }
    }

    /**
     * Build a SettlementTaskManager that uses a number of testMetaTask
     */
    private SettlementTaskManager buildManager(Settlement s, List<SettlementMetaTask> tasks) {
        return new SettlementTaskManager(s) {
            private static final long serialVersionUID = 1L;

			@Override
            protected List<SettlementMetaTask> getMetaTasks() {
                return tasks;
            }
        };
    }

    private SettlementTaskManager buildShiftBasedManager(Settlement s, int numFocus) {
        var focus = new HashSet<Entity>();
        for (int i = 0; i < numFocus; i++) {
            focus.add(buildPerson("Worker" + i, s));
        }

        List<SettlementMetaTask> metas = List.of(
                new TestMetaTask(TaskScope.ANY_HOUR, false, focus),
                new TestMetaTask(TaskScope.WORK_HOUR, false, focus),
                new TestMetaTask(TaskScope.NONWORK_HOUR, false, focus)
        );
        return buildManager(s, metas);
    }

    @Test
    void testGetTasks() {
        var s = buildSettlement("mock");

        var numFocus = 2;
        var manager = buildShiftBasedManager(s, numFocus);
        Person p = buildPerson("Worker", s);

        // Triggers the creation of the internal task cache
        manager.getTasks(p);
        var available1 = manager.getAvailableTasks();
        assertEquals(3 * numFocus, available1.size(), "Number of Settlement Tasks");

        // No refresh so same list
        manager.getTasks(p);
        var available2 = manager.getAvailableTasks();
        assertEquals(available1, available2, "Settlement Tasks same on second call");
    }

    @Test
    void testGetNonWorkTasks() {
        var s = buildSettlement("mock");

        var numFocus = 2;
        var manager = buildShiftBasedManager(s, numFocus);
        
        Person p = buildPerson("OffDuty", s);
        assertEquals(WorkStatus.OFF_DUTY, p.getShiftSlot().getStatus(), "Person on leave");

        // Should only be 2 set of tasks, ALL & NonWork
        var available1 = manager.getTasks(p);
        assertEquals(2 * numFocus, available1.size(), "Number of OffDuty Tasks");
        List<String> scopes = available1.stream().map(TaskJob::getName).toList();
        for(int i = 0; i < numFocus; i++) {
            String name = TaskScope.ANY_HOUR.name() + " @ Worker" + i;
            assertTrue(scopes.contains(name), "Task present " + name);
            name = TaskScope.NONWORK_HOUR.name() + " @ Worker" + i;
            assertTrue(scopes.contains(name), "Task present " + name);
        }
    }

    @Test
    void testGetWorkTasks() {
        var s = buildSettlement("mock");
        var numFocus = 2;
        var manager = buildShiftBasedManager(s, numFocus);

        Person p = buildPerson("OnDuty", s);
        p.getShiftSlot().setOnCall(true);
        assertEquals(WorkStatus.ON_CALL, p.getShiftSlot().getStatus(), "Person on duty");

        // Should only be 2 set of tasks, ALL & NonWork
        var available1 = manager.getTasks(p);
        assertEquals(2 * numFocus, available1.size(), "Number of OffDuty Tasks");
        List<String> scopes = available1.stream().map(TaskJob::getName).toList();
        for(int i = 0; i < numFocus; i++) {
            String name = TaskScope.ANY_HOUR.name() + " @ Worker" + i;
            assertTrue(scopes.contains(name), "Task present " + name);
            name = TaskScope.WORK_HOUR.name() + " @ Worker" + i;
            assertTrue(scopes.contains(name), "Task present " + name);
        }
    }

    /**
     * Check that a Peron can reject tasks
     */
    @Test
    void testPersonScoring() {
        var s = buildSettlement("mock");

        Set<Entity> focus = Set.of(buildPerson("Worker1", s));

        // One meta rejects and one accepts
        var manager = buildManager(s, List.of(
            new TestMetaTask(TaskScope.ANY_HOUR, true, focus),
            new TestMetaTask(TaskScope.ANY_HOUR, false, focus)));
        Person p = buildPerson("Worker", s);

        // Triggers the creation of the internal task cache
        var selected = manager.getTasks(p);
        var available1 = manager.getAvailableTasks(); 

        assertEquals(focus.size(), selected.size(), "Number of Suitable Settlement Tasks");
        assertEquals(2 * focus.size(), available1.size(), "Number of Total Settlement Tasks");
    }

    @Test
    void testTaskOwnerIsSettlement() {
        var s = buildSettlement("owner");
        var manager = buildShiftBasedManager(s, 1);
        var p = buildPerson("Worker", s);

        manager.getTasks(p);
        var available = manager.getAvailableTasks();

        assertTrue(available.stream().allMatch(t -> ((SettlementTask) t).getOwner().equals(s)),
                "All generated SettlementTasks should reference the manager settlement as owner");
    }

    @Test
    void testFireUpdateEvents() {
        var s = buildSettlement("mock");
        var p = buildPerson("Worker", s);

        Set<Entity> focus = new HashSet<>();
        var w1 = buildPerson("Worker1", s);
        focus.add(w1);

        // One meta rejects and one accepts
        var meta = new TestMetaTask(TaskScope.ANY_HOUR, false, focus);
        var manager = buildManager(s, List.of(meta));

        TestEntityListener listener = new TestEntityListener(SettlementTaskManager.NEWTASK_EVENT,
                                                            SettlementTaskManager.UPDATETASK_EVENT,
                                                            SettlementTaskManager.REMOVETASK_EVENT);
        s.addEntityListener(listener);

        // Triggers the creation of the internal task cache
        manager.timePassing();
        manager.getTasks(p);
        assertEquals(1, listener.getEventsReceived(), "One event should be fired for the new task");
        assertEquals(SettlementTaskManager.NEWTASK_EVENT, listener.getLastType(), "Event type should be NEWTASK_EVENT");
        var lastTask = (SettlementTask) listener.getLastTarget();

        meta.setDemand(2);
        var clock = getSim().getMasterClock();
        var newTime = clock.getMarsTime().addTime(500);
        clock.setMarsTime(newTime);
        manager.timePassing();
        manager.getTasks(p);
        assertEquals(2, listener.getEventsReceived(), "Two events should be fired for the new tasks");
        assertEquals(SettlementTaskManager.UPDATETASK_EVENT, listener.getLastType(), "Event type should be NEWTASK_EVENT");
        SettlementTask st = (SettlementTask) listener.getLastTarget();
        assertEquals(lastTask, st, "Update event should be for original task");
        assertEquals(newTime, st.getCreatedOn(), "Update event should be for original task");
        assertEquals(2, st.getDemand(), "Update event should be for task with demand 2");
    }

    @Test
    void testFireEvents() {
        var s = buildSettlement("mock");
        var p = buildPerson("Worker", s);

        Set<Entity> focus = new HashSet<>();
        var w1 = buildPerson("Worker1", s);
        focus.add(w1);

        // One meta rejects and one accepts
        var meta = new TestMetaTask(TaskScope.ANY_HOUR, false, focus);
        var manager = buildManager(s, List.of(meta));

        TestEntityListener listener = new TestEntityListener(SettlementTaskManager.NEWTASK_EVENT,
                                                            SettlementTaskManager.UPDATETASK_EVENT,
                                                            SettlementTaskManager.REMOVETASK_EVENT);
        s.addEntityListener(listener);

        // Triggers the creation of the internal task cache
        manager.timePassing();
        manager.getTasks(p);
        assertEquals(1, listener.getEventsReceived(), "One event should be fired for the new task");
        assertEquals(SettlementTaskManager.NEWTASK_EVENT, listener.getLastType(), "Event type should be NEWTASK_EVENT");

        var w2 = buildPerson("Worker2", s);
        focus.add(w2);
        manager.timePassing();
        manager.getTasks(p);
        assertEquals(2, listener.getEventsReceived(), "Two events should be fired for the new tasks");
        assertEquals(SettlementTaskManager.NEWTASK_EVENT, listener.getLastType(), "Event type should be NEWTASK_EVENT");
        SettlementTask st = (SettlementTask) listener.getLastTarget();
        assertEquals(w2, st.getFocus(), "Last event should be for the new task with focus Worker2");

        // Nothing changed so no events
        manager.timePassing();
        manager.getTasks(p);
        assertEquals(2, listener.getEventsReceived(), "Two events should be from old runs");

        // Remove entity
        focus.remove(w1);
        manager.timePassing();
        manager.getTasks(p);
        assertEquals(3, listener.getEventsReceived(), "Three events should be fired for the removed task");
        assertEquals(SettlementTaskManager.REMOVETASK_EVENT, listener.getLastType(), "Event type should be REMOVETASK_EVENT");
        st = (SettlementTask) listener.getLastTarget();
        assertEquals(w1, st.getFocus(), "Last event should be for the removed task with focus Worker1");
    }
}