package org.mars_sim.msp.core.person.ai.job.util;

import java.util.List;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.data.History.HistoryItem;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MasterClock;


public class AssignmentHistoryTest extends AbstractMarsSimUnitTest {

    public void testGetCummulativeJobRating() {
        Settlement home = buildSettlement();
        Person p = buildPerson("Job", home);

        MasterClock clock = sim.getMasterClock();
        MarsTime base = clock.getMarsTime();

        int j = 1;
        AssignmentHistory jh = p.getJobHistory();
        JobType [] jobs = {JobType.ARCHITECT, JobType.BIOLOGIST, JobType.MATHEMATICIAN};
        for(JobType job : jobs) {
            clock.setMarsTime(base.addTime(j * 100));  
            jh.saveJob(job, "Name" + j, AssignmentType.APPROVED, "Case");
            j++;
        }

        List<HistoryItem<Assignment>> history = jh.getJobAssignmentList();
        // Add 1 for the first job
        assertEquals("History size", jobs.length + 1, history.size());

        double total = 0;
        j = 0;
        for(HistoryItem<Assignment> i : history) {
            Assignment a = i.getWhat();
            assertEquals("History item time #" +j, base.addTime(j * 100), i.getWhen());
            assertEquals("History item State #" +j, AssignmentType.APPROVED, a.getStatus());
            if (j > 0) {
                // Skip checking the initial job
                assertEquals("History item initiator #" + j, "Name" + j, a.getInitiator());
                assertEquals("History item Job #" +j, jobs[j-1], a.getType());
            }
            a.setJobRating(j, j);
            int newRating = (int)(Assignment.INITIAL_RATING * Assignment.OLD_RATING_WEIGHT
                                            + Assignment.NEW_RATING_WEIGHT * j);
            assertEquals("New job rating #", j, newRating, a.getJobRating());
            total += newRating;
            j++;
        }

        assertEquals("Cumulative Score", total/history.size(), jh.getCummulativeJobRating());

    }


    public void testGetCummulativePendingJobRating() {
        Settlement home = buildSettlement();
        Person p = buildPerson("Job", home);

        MasterClock master = sim.getMasterClock();
        master.setMarsTime(master.getMarsTime().addTime(10));

        AssignmentHistory jh = p.getJobHistory();  
        jh.saveJob(JobType.ARCHITECT, "Name", AssignmentType.APPROVED, "Case");

        master.setMarsTime(master.getMarsTime().addTime(10));
        jh.saveJob(JobType.MATHEMATICIAN, "Name", AssignmentType.PENDING, "Case");

        List<HistoryItem<Assignment>> history = jh.getJobAssignmentList();
        // Set the rating high on Pending to check it is ignored
        history.get(history.size()-1).getWhat().setJobRating(10, 1);

        // Note Person has 1 JobAssignment as part of the constructor
        assertEquals("Hstory with pending", 3, history.size());
        assertEquals("Pending cummlative", (double)Assignment.INITIAL_RATING, jh.getCummulativeJobRating());

        Assignment approved = jh.getLastApproved();
        assertEquals("Approved Job", JobType.ARCHITECT, approved.getType());
        assertEquals("Approved status", AssignmentType.APPROVED, approved.getStatus());


    }
}
