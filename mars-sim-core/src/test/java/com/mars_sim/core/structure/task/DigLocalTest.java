package com.mars_sim.core.structure.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.resource.ResourceUtil;

public class DigLocalTest extends MarsSimUnitTest {
    @Test
    public void testCreateRegolithTask() {
        var s = buildSettlement("Dig base");

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.AREOLOGY, 10); // Skilled
        var eva = EVAOperationTest.prepareForEva(this, p);
        
        // DigLocal uses the Settlement airlock tracking logic.... it shouldn't
        s.checkAvailableAirlocks();

        EquipmentFactory.createEquipment(DigLocalRegolith.CONTAINER_TYPE, s);

        var task = new DigLocalRegolith(p);
        assertFalse(task.isDone(), "Task created"); 

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);
        assertEquals(DigLocalRegolith.COLLECT_REGOLITH, task.getPhase(), "Task completed collection");

        // Do collection
        executeTaskUntilPhase(p, task, 2000);
        assertEquals(DigLocal.WALK_TO_BIN, task.getPhase(), "Task completed collection");

        // Walk to bin
        executeTaskUntilPhase(p, task, 1000);
        assertEquals(DigLocal.DROP_OFF_RESOURCE, task.getPhase(), "Walk to drop completed");

        // Drop off resources
        executeTaskUntilPhase(p, task, 1000);

        // Add up reg resources
        double collected = 0D;
        for(int i : ResourceUtil.REGOLITH_TYPES_IDS) {
            collected += s.getSpecificAmountResourceStored(i);
        }
        assertGreaterThan("Collected Regolith", 0D, collected);
    }

    @Test
    public void testCreateIceTask() {
        var s = buildSettlement("Dig base");

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.AREOLOGY, 10); // Skilled
        var eva = EVAOperationTest.prepareForEva(this, p);
        
        // DigLocal uses the Settlement airlock tracking logic.... it shouldn't
        s.checkAvailableAirlocks();

        EquipmentFactory.createEquipment(DigLocalIce.CONTAINER_TYPE, s);

        var task = new DigLocalIce(p);
        assertFalse(task.isDone(), "Task created"); 

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);
        assertEquals(DigLocalIce.COLLECT_ICE, task.getPhase(), "Task completed collection");

        // Do collection
        executeTaskUntilPhase(p, task, 2000);
        assertEquals(DigLocal.WALK_TO_BIN, task.getPhase(), "Task completed collection");

        // Walk to bin
        executeTaskUntilPhase(p, task, 1000);
        assertEquals(DigLocal.DROP_OFF_RESOURCE, task.getPhase(), "Walk to drop completed");

        // Drop off resources
        executeTaskUntilPhase(p, task, 1000);

        // Add up reg resources
        double collected = s.getSpecificAmountResourceStored(ResourceUtil.ICE_ID);
        assertGreaterThan("Collected Ice", 0D, collected);
    }


    @Test
    public void testCreateTaskWithBin() {
        var s = buildSettlement("Dig base");
        var st = buildRegolithStorage(s.getBuildingManager(), new LocalPosition(100D, 100D), 0D);

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled
        EVAOperationTest.prepareForEva(this, p);
        
        // DigLocal uses the Settlement airlock tracking logic.... it shouldn't
        s.checkAvailableAirlocks();

        EquipmentFactory.createEquipment(DigLocalRegolith.CONTAINER_TYPE, s);

        var task = new DigLocalRegolith(p);
        assertFalse(task.isDone(), "Task created");

        assertLessThan("Drop off is Storage bin", DigLocal.MAX_DROPOFF_DISTANCE * 2.5D,
                                        st.getPosition().getDistanceTo(task.getDropOffLocation()));
    }

    private Building buildRegolithStorage(BuildingManager buildingManager, LocalPosition pos,
                            double facing) {
        return buildFunction(buildingManager, "Regolith Storage", BuildingCategory.STORAGE,
                FunctionType.STORAGE,  pos, facing, true);
    }

    @Test
    public void testIceMetaTask() {
        var s = buildSettlement("Ice");
        var mt = new DigLocalIceMeta();
        
        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled
        EVAOperationTest.prepareForEva(this, p);

        var tasks = mt.getSettlementTasks(s);
        assertTrue(tasks.isEmpty(), "No Tasks when no container");
        EquipmentFactory.createEquipment(DigLocalIce.CONTAINER_TYPE, s);

        // Everything ready
        tasks = mt.getSettlementTasks(s);
        assertFalse(tasks.isEmpty(), "Tasks found");
        var task = tasks.get(0);
 
        assertTrue(task.isEVA(), "Task is EVA");

        double cap = s.getSpecificCapacity(ResourceUtil.ICE_ID);
        assertTrue(cap == 0.0, "Capacity is zero");
        
        // Fill capacity
        s.storeAmountResource(ResourceUtil.ICE_ID, 5);
        tasks = mt.getSettlementTasks(s);
        assertFalse(tasks.isEmpty(), "Has Tasks even if no capacity");
    }
}
