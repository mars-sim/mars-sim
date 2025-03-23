package com.mars_sim.core.person.health;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.task.SelfTreatHealthProblemTest;

public class HealthProblemTest extends AbstractMarsSimUnitTest {

    /**
     * 
     */
    public void testCured() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var p = buildPerson("Ill", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        var pc = p.getPhysicalCondition();

        var startedOn = getSim().getMasterClock().getMarsTime();
        var hp = SelfTreatHealthProblemTest.addComplaint(this, p, ComplaintType.BROKEN_BONE);
        assertEquals("Problems registered", 1, pc.getProblems().size());
        assertEquals("Problems history", 0, pc.getHealthHistory().size());

        // Set teh problem cured and run a pulse to update
        hp.setCured();
        
        var curedOn = startedOn.addTime(1500);
        var pulse = createPulse(curedOn, false, false);
        pc.timePassing(pulse, s);

        assertEquals("Health problem cured", 0, pc.getProblems().size());
        assertEquals("Medical history", 1, pc.getHealthHistory().size());

        var h = pc.getHealthHistory().get(0);
        assertEquals("Problem start On", startedOn, h.start());
        assertEquals("Problem cured On", curedOn, h.cured());
        assertEquals("Problem cured", hp.getComplaint(), h.complaint());
    }
}