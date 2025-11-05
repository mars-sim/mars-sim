package com.mars_sim.core.person.health;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.task.SelfTreatHealthProblemTest;

public class HealthProblemTest extends MarsSimUnitTest {

    /**
     * 
     */
    @Test
    public void testCured() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(getContext(), s);
        var p = buildPerson("Ill", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        var pc = p.getPhysicalCondition();

        var startedOn = getSim().getMasterClock().getMarsTime();
        var hp = SelfTreatHealthProblemTest.addComplaint(getContext(), p, ComplaintType.BROKEN_BONE);
        assertEquals(1, pc.getProblems().size(), "Problems registered");
        assertEquals(0, pc.getHealthHistory().size(), "Problems history");

        // Set teh problem cured and run a pulse to update
        hp.setCured();
        
        var curedOn = startedOn.addTime(1500);
        var pulse = createPulse(curedOn, false, false);
        pc.timePassing(pulse, s);

        assertEquals(0, pc.getProblems().size(), "Health problem cured");
        assertEquals(1, pc.getHealthHistory().size(), "Medical history");

        var h = pc.getHealthHistory().get(0);
        assertEquals(startedOn, h.start(), "Problem start On");
        assertEquals(curedOn, h.cured(), "Problem cured On");
        assertEquals(hp.getComplaint(), h.complaint(), "Problem cured");
    }
}
