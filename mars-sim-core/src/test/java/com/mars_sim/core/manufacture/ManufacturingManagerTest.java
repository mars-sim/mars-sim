package com.mars_sim.core.manufacture;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.process.ProcessInfoTest;
import com.mars_sim.core.science.task.MarsSimContext;
import com.mars_sim.core.structure.Settlement;

public class ManufacturingManagerTest extends AbstractMarsSimUnitTest {

    private Person buildEngineer(Settlement s, int skill) {
        var p = buildPerson("Engineer =" + skill, s);
        p.getSkillManager().addNewSkill(SkillType.MATERIALS_SCIENCE, skill);

        return p;
    }

    public static Building buildWorkshop(MarsSimContext context, BuildingManager buildingManager) {
        return context.buildFunction(buildingManager, "Workshop", BuildingCategory.WORKSHOP,
                        FunctionType.MANUFACTURE,  LocalPosition.DEFAULT_POSITION, 1D, true);
    }

    private ManufactureProcessInfo getManuAtTechLevel(int level) {
        var conf = getConfig().getManufactureConfiguration().getManufactureProcessesForTechLevel(level);
        return conf.stream().filter(q -> q.getTechLevelRequired() == level).findAny().get();
    }

    private SalvageProcessInfo getSalvageAtTechLevel(int level) {
        var conf = getConfig().getManufactureConfiguration().getSalvageProcessesForTechLevel(level);
        return conf.stream().filter(q -> q.getTechLevelRequired() == level).findAny().get();
    }

    public void testSalvageClaim() {
        var s = buildSettlement("factory", true);
        var mgr = new ManufacturingManager(s);

        // Add 2 processes with the correct input resources at Settlement
        var select2 = getSalvageAtTechLevel(2);
        mgr.addProcessToQueue(select2);
        var select1 = getSalvageAtTechLevel(1);
        mgr.addProcessToQueue(select1);
        assertEquals("Queue at start", 2, mgr.getQueue().size());
        Set<Tooling> tools = Collections.emptySet();

        int skill = Math.min(select1.getSkillLevelRequired(), select2.getSkillLevelRequired());
        var claimed = mgr.claimNextProcess(0, 0, tools);
        assertNull("Claim skill 0, tech 0", claimed);

        claimed = mgr.claimNextProcess(0, skill, tools);
        assertNull("Claim tech 0 required skill", claimed);

        claimed = mgr.claimNextProcess(1, skill-1, tools);
        assertNull("Claim tech 2 skill 0", claimed);

        // Do the fully matched request
        claimed = mgr.claimNextProcess(select1.getTechLevelRequired(), select1.getSkillLevelRequired(), tools);
        assertNotNull("Claim matched", claimed);
        assertEquals("Queue matched", 1, mgr.getQueue().size());
        assertEquals("Claim is correct", select1, claimed.getInfo());
    }

    public void testManuClaim() {
        var s = buildSettlement("factory", true);
        var mgr = new ManufacturingManager(s);

        // Add 2 processes with the correct input resoruces at Settlement
        var select2 = getManuAtTechLevel(2);
        mgr.addProcessToQueue(select2);
        var select1 = getManuAtTechLevel(1);
        mgr.addProcessToQueue(select1);
        assertEquals("Queue at start", 2, mgr.getQueue().size());

        Set<Tooling> tools = new HashSet<>();
        tools.add(select1.getTooling());

        int skill = Math.min(select1.getSkillLevelRequired(), select2.getSkillLevelRequired());
        var claimed = mgr.claimNextProcess(0, 0, tools);
        assertNull("Claim skill 0, tech 0", claimed);

        claimed = mgr.claimNextProcess(0, skill, tools);
        assertNull("Claim tech 0 required skill", claimed);

        claimed = mgr.claimNextProcess(1, skill-1, tools);
        assertNull("Claim tech 2 skill 0", claimed);

        // Valid but no resources
        claimed = mgr.claimNextProcess(select1.getTechLevelRequired(), select1.getSkillLevelRequired(), tools);
        assertNull("Claim match but no resources", claimed);

        // Load resources into settlement
        ProcessInfoTest.loadSettlement(s, select1);

        // Valid but no tools
        claimed = mgr.claimNextProcess(select1.getTechLevelRequired(), select1.getSkillLevelRequired(), Collections.emptySet());
        assertNull("Claim match but no tools", claimed);

        // Do the fully matched request
        claimed = mgr.claimNextProcess(select1.getTechLevelRequired(), select1.getSkillLevelRequired(), tools);
        assertNotNull("Claim matched", claimed);
        assertEquals("Queue matched", 1, mgr.getQueue().size());
        assertEquals("Claim is correct", select1, claimed.getInfo());
    }

    public void testManuOutputs() {
        var s = buildSettlement();

        // Build workshop before manager
        var b = buildWorkshop(this, s.getBuildingManager());
        var mgr = new ManufacturingManager(s);

        int techLevel = b.getManufacture().getTechLevel();
        assertEquals("Mgr tech level", techLevel, mgr.getMaxTechLevel());

        // Nothign without an engineer
        var o = mgr.getPossibleOutputs();
        assertTrue("Outputs without engineer", o.isEmpty());

        // Try agains with an engineer
        buildEngineer(s, 1);
        o = mgr.getPossibleOutputs();
        assertFalse("Outputs found", o.isEmpty());
    }

    public void testQueueUpdate() {
        var s = buildSettlement("factory", true);
        var mgr = new ManufacturingManager(s);
        buildWorkshop(this, s.getBuildingManager());
        buildEngineer(s, 1);

        var p = mgr.getQueuableManuProcesses(null);
        assertFalse("Queuable processes", p.isEmpty());

        // No resources
        mgr.updateQueue();
        assertTrue("Queue without resources", mgr.getQueue().isEmpty());

        // Add resources and configure paameters
        var pMgr = s.getPreferences();
        pMgr.putValue(ManufacturingParameters.NEW_MANU_LIMIT, 0);
        var first = p.get(0);
        ProcessInfoTest.loadSettlement(s, first);
        var second = p.get(1);
        ProcessInfoTest.loadSettlement(s, second);

        // Resources, no limit
        mgr.updateQueue();
        assertTrue("Queue with zero limit", mgr.getQueue().isEmpty());

        // Resources
        pMgr.putValue(ManufacturingParameters.NEW_MANU_LIMIT, 1);
        mgr.updateQueue();
        assertEquals("Queue contains single item", 1, mgr.getQueue().size());
    }


    public void testManuQueuable() {
        var s = buildSettlement();
        var mgr = new ManufacturingManager(s);

        // No workshop
        var p = mgr.getQueuableManuProcesses(null);
        assertTrue("No Workshop; no queuable", p.isEmpty());

        var b = buildWorkshop(this, s.getBuildingManager());
        int techLevel = b.getManufacture().getTechLevel();
        mgr.updateTechLevel();

        // No queuable because no engineers
        p = mgr.getQueuableManuProcesses(null);
        assertTrue("No Engineers; no queuable", p.isEmpty());

        // Match to return potentials only
        buildEngineer(s, 1);
        p = mgr.getQueuableManuProcesses(null);
        assertFalse("Queuable", p.isEmpty());

        // Find any offered processes that are beyond the scope
        long tooHard = p.stream()
                        .filter(q -> (q.getSkillLevelRequired() > 1)
                                        || (q.getTechLevelRequired() > techLevel))
                        .count();
        assertEquals("Too hard processes offered", 0L, tooHard);

        // Filter by an output; take output from first process
        var outputResource = p.get(0).getOutputList().get(0).getName();
        p = mgr.getQueuableManuProcesses(outputResource);
        assertFalse("Output filtered " + outputResource, p.isEmpty());
        for(var po : p) {
            assertTrue("Process " + po.getName() + " has " + outputResource,
                        po.isOutput(outputResource));
        }

        // Check filtering with not found output
        p = mgr.getQueuableManuProcesses("non-existent resource");
        assertTrue("Not output", p.isEmpty());
    }

    public void testManuQueue() {
        var s = buildSettlement("factory", true);

        var mgr = new ManufacturingManager(s);

        // Add 
        var select2 = getManuAtTechLevel(2);
        mgr.addProcessToQueue(select2);
        assertEquals("One item in queue", 1, mgr.getQueue().size());
        assertEquals("Select2 is queued", select2, mgr.getQueue().get(0).getInfo());
        assertEquals("Lowest on queue", 2, mgr.getLowestOnQueue());

        // Add second lwest process
        var select1 = getManuAtTechLevel(1);
        mgr.addProcessToQueue(select1);
        assertEquals("Items in queue", 2, mgr.getQueue().size());
        assertEquals("Select2 is queued", select1, mgr.getQueue().get(1).getInfo());
        assertEquals("Lowest on queue", 1, mgr.getLowestOnQueue());
    }
}
