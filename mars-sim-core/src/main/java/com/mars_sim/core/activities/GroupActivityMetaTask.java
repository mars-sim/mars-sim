/*
 * Mars Simulation Project
 * GroupActivityMetaTask.java
 * @date 2023-03-17
 * @author Barry Evans
 */
package com.mars_sim.core.activities;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.structure.Settlement;

/**
 * This converts any active GroupActivities into a Schedueld Task that allow sPersons to join the
 * activity.
 */
public class GroupActivityMetaTask extends MetaTask implements SettlementMetaTask {

    private class GroupActivitySettlementTask extends SettlementTask {

        private GroupActivity activity;

        protected GroupActivitySettlementTask(SettlementMetaTask parent, GroupActivity activity,
                                    RatingScore score, int attendees) {
            super(parent, activity.getDefinition().name(), activity.getMeetingPlace(), score);
            this.activity = activity;
            setDemand(attendees);
            setScope(activity.getDefinition().scope());
        }

        @Override
        public Task createTask(Person person) {
            return new GroupActivityTask(activity, person);
        }

    }

    private static final String NAME = "GroupActivity";

    public GroupActivityMetaTask() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
    }

    /**
     * Create a Scheduled Task for any active Activity. The assigned Building will have been selected
     * as part of the Activity.
     * @return List of tasks associated with the active Group Activities
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
       var active = settlement.getFutureManager().getEvents().stream()
                .filter(e -> e.getHandler() instanceof GroupActivity)
                .map(e -> (GroupActivity)e.getHandler())
                .filter(GroupActivity::isActive)
                .toList();
        
         List<SettlementTask> results = new ArrayList<>();
         for(var a : active) {
            var score = new RatingScore(a.getDefinition().score());
            int expected = (int) (settlement.getNumCitizens() * a.getDefinition().percentagePop());
            results.add(new GroupActivitySettlementTask(this, a, score, expected));
         }

         return results;
    }

}
