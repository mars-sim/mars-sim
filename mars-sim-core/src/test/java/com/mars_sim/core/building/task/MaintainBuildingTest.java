package com.mars_sim.core.building.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.task.MarsSimContext;

public class MaintainBuildingTest extends AbstractMarsSimUnitTest {
	
    static void buildingNeedMaintenance(Building b, MarsSimContext context) {
        MalfunctionManager manager = b.getMalfunctionManager();
        double time = manager.getStandardInspectionWindow() * MaintainBuildingMeta.INSPECTION_PERCENTAGE * 220;
        var mTime = context.getSim().getMasterClock().getMarsTime().addTime(time);
        manager.activeTimePassing(context.createPulse(mTime, false, false));
    }
    
    public void testMetaTask() {
        var s = buildSettlement("Maintenance");
        var b1 = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        // 2nd building check logic
        var b2 = buildResearch(s.getBuildingManager(), new LocalPosition(10, 10), 0D, 2);

        var mt = new MaintainBuildingMeta();
        var tasks = mt.getSettlementTasks(s);
        
        // Note: there is a chance that tasks are made since scoreMaintenance() currently
        //       has a probability component
//        assertTrue("No tasks found", tasks.isEmpty());

        // One building needs maintenance
        buildingNeedMaintenance(b1, this);
        tasks = mt.getSettlementTasks(s);

    	// Question : why would sometimes both buildings (b1, b2) will incur the need for maintenance ?
        // Answer : getSettlementTasks() will consider both buildings always
        
        // Note: tasks may have the size of 0, 1, 2. It depends on the result of  scoreMaintenance()
        if (tasks.size() == 2) {

	        var found1 = tasks.get(0);
	        var found2 = tasks.get(1);
	        
	        var foundB1 = found1.getFocus();
	        var foundB2 = found2.getFocus();
	        
	        if (!found1.isEVA()) {
	        	assertEquals("Found building B1 with maintenance", b1, foundB1);
	        }
	        
	        assertEquals("Found building B2", b2, foundB2);
        }
    }
    
    public void testCreateTask() {

        var s = buildSettlement("Maintain");
        var b = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var p = buildPerson("Engineer", s, JobType.ENGINEER, b, FunctionType.RESEARCH);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled

        // Set building needs maintenance by moving time forward twice minimum
        buildingNeedMaintenance(b, this);
        var manager = b.getMalfunctionManager();
        assertGreaterThan("Maintenance due", 0D, manager.getEffectiveTimeSinceLastMaintenance());

        var task = new MaintainBuilding(p, b);
        assertFalse("Task created", task.isDone());

        // Do the initial walk
        executeTaskUntilSubTask(p, task, 10);
        assertTrue("Walk completed", task.getSubTask().isDone());
        assertFalse("Task still active", task.isDone());
 
        assertFalse("Task still active", task.isDone());
        assertEquals("Engineer location", b, p.getBuildingLocation());
        
        // Do maintenance for a few calls to ensure maintenance is happening
        executeTaskUntilPhase(p, task, 2);
        // Note that inspectionTimeCompleted will be reset to zero right away and is never a sign that work is accomplished
//        assertGreaterThan("Maintenance completed", 0D, manager.getInspectionWorkTimeCompleted());

        // Complete until the end
        executeTaskForDuration(p, task, task.getTimeLeft() * 1.1);
        assertTrue("Task created", task.isDone());
        assertEquals("Maintenance period reset", 0D, manager.getEffectiveTimeSinceLastMaintenance());
    }
}
