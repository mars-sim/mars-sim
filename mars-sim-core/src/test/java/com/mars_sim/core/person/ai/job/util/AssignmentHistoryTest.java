/*
 * Mars Simulation Project
 * AssignmentHistoryTest.java
 * @date 2023-06-27
 * @author Barry Evans
 */

package com.mars_sim.core.person.ai.job.util;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.data.History.HistoryItem;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;


public class AssignmentHistoryTest extends MarsSimUnitTest {

    @Test
    public void testGetCummulativeJobRating() {
        Settlement home = buildSettlement("mock");
        Person p = buildPerson("Job", home);

        MasterClock clock = getSim().getMasterClock();
        MarsTime base = clock.getMarsTime();

        int j = 1;
        AssignmentHistory jh = p.getJobHistory();
        JobType [] jobs = {JobType.ARCHITECT, JobType.ASTROBIOLOGIST, JobType.MATHEMATICIAN};
        for(JobType job : jobs) {
            clock.setMarsTime(base.addTime(j * 100));  
            jh.saveJob(job, "Name" + j, AssignmentType.APPROVED, "Case");
            j++;
        }

        List<HistoryItem<Assignment>> history = jh.getJobAssignmentList();
        // Add 1 for the first job
        assertEquals(jobs.length + 1, history.size(), "History size");

        double total = 0;
        j = 0;
        for(HistoryItem<Assignment> i : history) {
            Assignment a = i.getWhat();
            assertEquals(base.addTime(j * 100), i.getWhen(), "History item time #" +j);
            assertEquals(AssignmentType.APPROVED, a.getStatus(), "History item State #" +j);
            if (j > 0) {
                // Skip checking the initial job
                assertEquals("Name" + j, a.getInitiator(), "History item initiator #" + j);
                assertEquals(jobs[j-1], a.getType(), "History item Job #" +j);
            }
            a.setJobRating(j, j);
            int newRating = (int)(Assignment.INITIAL_RATING * Assignment.OLD_RATING_WEIGHT
                                            + Assignment.NEW_RATING_WEIGHT * j);
            assertEquals(j, newRating, a.getJobRating(), "New job rating #");
            total += newRating;
            j++;
        }

        assertEquals(total/history.size(), jh.getCummulativeJobRating(), "Cumulative Score");

    }


    @Test
    public void testGetCummulativePendingJobRating() {
        Settlement home = buildSettlement("mock");
        Person p = buildPerson("Job", home);

        MasterClock master = getSim().getMasterClock();
        master.setMarsTime(master.getMarsTime().addTime(10));

        AssignmentHistory jh = p.getJobHistory();  
        jh.saveJob(JobType.ARCHITECT, "Name", AssignmentType.APPROVED, "Case");

        master.setMarsTime(master.getMarsTime().addTime(10));
        jh.saveJob(JobType.MATHEMATICIAN, "Name", AssignmentType.PENDING, "Case");

        List<HistoryItem<Assignment>> history = jh.getJobAssignmentList();
        // Set the rating high on Pending to check it is ignored
        history.get(history.size()-1).getWhat().setJobRating(10, 1);

        // Note Person has 1 JobAssignment as part of the constructor
        assertEquals(3, history.size(), "Hstory with pending");
        assertEquals((double)Assignment.INITIAL_RATING, jh.getCummulativeJobRating(), "Pending cummlative");

        Assignment approved = jh.getLastApproved();
        assertEquals(JobType.ARCHITECT, approved.getType(), "Approved Job");
        assertEquals(AssignmentType.APPROVED, approved.getStatus(), "Approved status");


    }
}
