package com.mars_sim.core.manufacture;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.process.ProcessInfoTest;
import com.mars_sim.core.science.task.MarsSimContext;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;

public class ManufacturingManagerTest extends AbstractMarsSimUnitTest {

    private Person buildEngineer(Settlement s, int skill) {
        var p = buildPerson("Engineer =" + skill, s);
        p.getSkillManager().addNewSkill(SkillType.MATERIALS_SCIENCE, skill);

        return p;
    }

    public static Building buildWorkshop(MarsSimContext context, BuildingManager buildingManager) {
        return context.buildFunction(buildingManager, "Lander Hab", BuildingCategory.WORKSHOP,
                        FunctionType.MANUFACTURE,  LocalPosition.DEFAULT_POSITION, 1D, true);
    }

    private ManufactureProcessInfo getManuAtTechLevel(int level) {
        var conf = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(level);
        return conf.stream().filter(q -> q.getTechLevelRequired() == level).findAny().get();
    }

    public void testClaim() {
        var s = buildSettlement();
        var mgr = new ManufacturingManager(s);

        // Add 2 processes with the correct input resoruces at Settlement
        var select2 = getManuAtTechLevel(2);
        mgr.addManufacturing(select2);
        var select1 = getManuAtTechLevel(1);
        mgr.addManufacturing(select1);
        assertEquals("Queue at start", 2, mgr.getQueue().size());

        int skill = Math.min(select1.getSkillLevelRequired(), select2.getSkillLevelRequired());
        var claimed = mgr.claimNextProcess(0, 0);
        assertNull("Claim skill 0, tech 0", claimed);

        claimed = mgr.claimNextProcess(0, skill);
        assertNull("Claim tech 0 required skill", claimed);

        claimed = mgr.claimNextProcess(1, skill-1);
        assertNull("Claim tech 2 skill 0", claimed);

        // Valid but no resources
        claimed = mgr.claimNextProcess(select1.getTechLevelRequired(), select1.getSkillLevelRequired());
        assertNull("Claim match but no resources", claimed);

        // Load resoruces into settlement and try again
        ProcessInfoTest.loadSettlement(s, select1);
        claimed = mgr.claimNextProcess(select1.getTechLevelRequired(), select1.getSkillLevelRequired());
        assertNotNull("Claim matched", claimed);
        assertEquals("Queue matched", 1, mgr.getQueue().size());
        assertEquals("Claim is correct", select1, claimed.getInfo());

    }

    public void testManuQueuable() {
        var s = buildSettlement();
        var mgr = new ManufacturingManager(s);

        var p = mgr.getQueuableManuProcesses();
        assertTrue("No Workshop; no queuable", p.isEmpty());

        var b = buildWorkshop(this, s.getBuildingManager());
        int techLevel = b.getManufacture().getTechLevel();

        p = mgr.getQueuableManuProcesses();
        assertTrue("No Engineers; no queuable", p.isEmpty());

        buildEngineer(s, 1);
        p = mgr.getQueuableManuProcesses();
        assertFalse("Queuable", p.isEmpty());

        // Find any offered processes that are beyond the scope
        long tooHard = p.stream()
                        .filter(q -> (q.getSkillLevelRequired() > 1)
                                        || (q.getTechLevelRequired() > techLevel))
                        .count();
        assertEquals("Too hard processes offered", 0L, tooHard);
    }

    public void testManuQueue() {
        var s = buildSettlement();
        var mgr = new ManufacturingManager(s);

        // Add 
        var select2 = getManuAtTechLevel(2);
        mgr.addManufacturing(select2);
        assertEquals("One item in queue", 1, mgr.getQueue().size());
        assertEquals("Select2 is queued", select2, mgr.getQueue().get(0).getInfo());
        assertEquals("Lowest on queue", 2, mgr.getLowestOnQueue());

        // Add second lwest process
        var select1 = getManuAtTechLevel(1);
        mgr.addManufacturing(select1);
        assertEquals("Items in queue", 2, mgr.getQueue().size());
        assertEquals("Select2 is queued", select1, mgr.getQueue().get(1).getInfo());
        assertEquals("Lowest on queue", 1, mgr.getLowestOnQueue());
    }
}
