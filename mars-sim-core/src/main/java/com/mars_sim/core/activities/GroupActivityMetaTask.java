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
import com.mars_sim.core.person.ai.social.Relation;
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

        private static final long serialVersionUID = 1L;
		private GroupActivity activity;

        protected GroupActivitySettlementTask(SettlementMetaTask parent, GroupActivity activity,
                                    RatingScore score, int attendees) {
            super(parent, "Host " + activity.getDefinition().name(), activity.getMeetingPlace(), score);
            this.activity = activity;
            setDemand(attendees);
            setScope(activity.getDefinition().scope());
        }

        @Override
        public Task createTask(Person person) {
            return new GroupActivityTask(activity, person);
        }

        public GroupActivity getActivity() {
            return activity;
        }
    }

    private static final String NAME = "GroupActivity";
    private static final RatingScore INSTIGATOR_SCORE = new RatingScore(9000D);

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
        var active = settlement.getGroupActivities(true);
        
         List<SettlementTask> results = new ArrayList<>();
         for(var a : active) {
            var score = new RatingScore(a.getDefinition().score());
            int expected = (int) (settlement.getNumCitizens() * a.getDefinition().percentagePop());
            results.add(new GroupActivitySettlementTask(this, a, score, expected));
         }

         return results;
    }

    /**
     * Score the task based on the relationshio with the instigator, if there is one.
     * If the person is the instigator then give a high score.
     * 
     * @param t Group activity task being evaluated
     * @param p Person evaluating the Task
     */
    @Override
    public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        RatingScore result = super.assessPersonSuitability(t, p);

        // Should always be true
        if ((result.getScore() > 0) && (t instanceof GroupActivitySettlementTask gst)) {
            Person instigator = gst.getActivity().getInstigator();
            if (instigator != null) {
                if (instigator.equals(p)) {
                    return INSTIGATOR_SCORE;
                }
                
                // Use the opinion of the instigator to score
                var opinion = p.getRelation().getOpinion(instigator);
                if (opinion != null) {
                    var modifier = 1 + (opinion.getAverage()/Relation.MAX_OPINION);
                    result = new RatingScore(result);
                    result.addModifier("person", modifier);
                }
            }
        }

        return result;
    }
}
