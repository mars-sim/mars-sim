package com.mars_sim.core.structure.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.EVAOperationTest;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

public class DigLocalTest extends AbstractMarsSimUnitTest {
    public void testCreateRegolithTask() {
        var s = buildSettlement("Dig base");

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.AREOLOGY, 10); // Skilled
        var eva = EVAOperationTest.prepareForEva(this, p);
        
        // DigLocal uses the Settlement airlock tracking logic.... it shouldn't
        s.checkAvailableAirlocks();

        EquipmentFactory.createEquipment(DigLocalRegolith.CONTAINER_TYPE, s);

        var task = new DigLocalRegolith(p);
        assertFalse("Task created", task.isDone()); 

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);
        assertEquals("Task completed collection", DigLocalRegolith.COLLECT_REGOLITH, task.getPhase());

        // Do collection
        executeTaskUntilPhase(p, task, 2000);
        assertEquals("Task completed collection", DigLocal.WALK_TO_BIN, task.getPhase());

        // Walk to bin
        executeTaskUntilPhase(p, task, 1000);
        assertEquals("Walk to drop completed", DigLocal.DROP_OFF_RESOURCE, task.getPhase());

        // Drop off resources
        executeTaskUntilPhase(p, task, 1000);

        // Add up reg resources
        double collected = 0D;
        for(int i : ResourceUtil.REGOLITH_TYPES) {
            collected += s.getAmountResourceStored(i);
        }
        assertGreaterThan("Collected Regolith", 0D, collected);
    }

    public void testCreateIceTask() {
        var s = buildSettlement("Dig base");

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.AREOLOGY, 10); // Skilled
        var eva = EVAOperationTest.prepareForEva(this, p);
        
        // DigLocal uses the Settlement airlock tracking logic.... it shouldn't
        s.checkAvailableAirlocks();

        EquipmentFactory.createEquipment(DigLocalIce.CONTAINER_TYPE, s);

        var task = new DigLocalIce(p);
        assertFalse("Task created", task.isDone()); 

        // Move onsite
        EVAOperationTest.executeEVAWalk(this, eva, task);
        assertEquals("Task completed collection", DigLocalIce.COLLECT_ICE, task.getPhase());

        // Do collection
        executeTaskUntilPhase(p, task, 2000);
        assertEquals("Task completed collection", DigLocal.WALK_TO_BIN, task.getPhase());

        // Walk to bin
        executeTaskUntilPhase(p, task, 1000);
        assertEquals("Walk to drop completed", DigLocal.DROP_OFF_RESOURCE, task.getPhase());

        // Drop off resources
        executeTaskUntilPhase(p, task, 1000);

        // Add up reg resources
        double collected = s.getAmountResourceStored(ResourceUtil.iceID);
        assertGreaterThan("Collected Ice", 0D, collected);
    }


    public void testCreateTaskWithBin() {
        var s = buildSettlement("Dig base");
        var st = buildRegolithStorage(s.getBuildingManager(), new LocalPosition(100D, 100D), 0D, 2);

        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled
        EVAOperationTest.prepareForEva(this, p);
        
        // DigLocal uses the Settlement airlock tracking logic.... it shouldn't
        s.checkAvailableAirlocks();

        EquipmentFactory.createEquipment(DigLocalRegolith.CONTAINER_TYPE, s);

        var task = new DigLocalRegolith(p);
        assertFalse("Task created", task.isDone());

        assertLessThan("Drop off is Storage bin", DigLocal.MAX_DROPOFF_DISTANCE * 2.5D,
                                        st.getPosition().getDistanceTo(task.getDropOffLocation()));
    }

    private Building buildRegolithStorage(BuildingManager buildingManager, LocalPosition pos,
                            double facing, int id) {
        return buildFunction(buildingManager, "Regolith Storage", BuildingCategory.STORAGE,
                FunctionType.STORAGE,  pos, facing, id);
    }

    public void testIceMetaTask() {
        var s = buildSettlement("Ice");
        var mt = new DigLocalIceMeta();
        var p = buildPerson("Mechanic", s, JobType.TECHNICIAN);
        p.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled
        EVAOperationTest.prepareForEva(this, p);


        var tasks = mt.getSettlementTasks(s);
        assertTrue("No Tasks when no container", tasks.isEmpty());
        EquipmentFactory.createEquipment(DigLocalIce.CONTAINER_TYPE, s);

        // Everything ready
        tasks = mt.getSettlementTasks(s);
        assertFalse("Tasks found", tasks.isEmpty());
        var task = tasks.get(0);
        assertTrue("Task is EVA", task.isEVA());

        // Fill capacity
        s.storeAmountResource(ResourceUtil.iceID, s.getAmountResourceCapacity(ResourceUtil.iceID));
        tasks = mt.getSettlementTasks(s);
        assertTrue("No Tasks when no capacity", tasks.isEmpty());
    }
}
