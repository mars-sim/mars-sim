package com.mars_sim.core.manufacture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.process.ProcessInfoTest;
import com.mars_sim.core.science.task.MarsSimContext;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

public class ManufacturingManagerTest extends MarsSimUnitTest {

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

    @Test
    public void testSalvageClaim() {
        var s = buildSettlement("factory", true, MockSettlement.DEFAULT_COORDINATES);
        var mgr = new ManufacturingManager(s);

        // Add 2 processes with the correct input resources at Settlement
        var select2 = getSalvageAtTechLevel(2);
        mgr.addProcessToQueue(select2);
        var select1 = getSalvageAtTechLevel(1);
        mgr.addProcessToQueue(select1);
        assertEquals(2, mgr.getQueue().size(), "Queue at start");
        Set<Tooling> tools = Collections.emptySet();

        int skill = Math.min(select1.getSkillLevelRequired(), select2.getSkillLevelRequired());
        var claimed = mgr.claimNextProcess(0, 0, tools);
        assertNull(claimed, "Claim skill 0, tech 0");

        claimed = mgr.claimNextProcess(0, skill, tools);
        assertNull(claimed, "Claim tech 0 required skill");

        claimed = mgr.claimNextProcess(1, skill-1, tools);
        assertNull(claimed, "Claim tech 2 skill 0");

        // Do the fully matched request
        claimed = mgr.claimNextProcess(select1.getTechLevelRequired(), select1.getSkillLevelRequired(), tools);
        assertNotNull(claimed, "Claim matched");
        assertEquals(1, mgr.getQueue().size(), "Queue matched");
        assertEquals(select1, claimed.getInfo(), "Claim is correct");
    }

    @Test
    public void testManuClaim() {
        var s = buildSettlement("factory", true, MockSettlement.DEFAULT_COORDINATES);
        var mgr = new ManufacturingManager(s);

        // Add 2 processes with the correct input resoruces at Settlement
        var select2 = getManuAtTechLevel(2);
        mgr.addProcessToQueue(select2);
        var select1 = getManuAtTechLevel(1);
        mgr.addProcessToQueue(select1);
        assertEquals(2, mgr.getQueue().size(), "Queue at start");

        Set<Tooling> tools = new HashSet<>();
        tools.add(select1.getTooling());

        int skill = Math.min(select1.getSkillLevelRequired(), select2.getSkillLevelRequired());
        var claimed = mgr.claimNextProcess(0, 0, tools);
        assertNull(claimed, "Claim skill 0, tech 0");

        claimed = mgr.claimNextProcess(0, skill, tools);
        assertNull(claimed, "Claim tech 0 required skill");

        claimed = mgr.claimNextProcess(1, skill-1, tools);
        assertNull(claimed, "Claim tech 2 skill 0");

        // Valid but no resources
        claimed = mgr.claimNextProcess(select1.getTechLevelRequired(), select1.getSkillLevelRequired(), tools);
        assertNull(claimed, "Claim match but no resources");

        // Load resources into settlement
        ProcessInfoTest.loadSettlement(s, select1);

        // Valid but no tools
        claimed = mgr.claimNextProcess(select1.getTechLevelRequired(), select1.getSkillLevelRequired(), Collections.emptySet());
        assertNull(claimed, "Claim match but no tools");

        // Do the fully matched request
        claimed = mgr.claimNextProcess(select1.getTechLevelRequired(), select1.getSkillLevelRequired(), tools);
        assertNotNull(claimed, "Claim matched");
        assertEquals(1, mgr.getQueue().size(), "Queue matched");
        assertEquals(select1, claimed.getInfo(), "Claim is correct");
    }

    @Test
    public void testManuOutputs() {
        var s = buildSettlement("manu");

        // Build workshop before manager
        var b = buildWorkshop(this.getContext(), s.getBuildingManager());
        var mgr = new ManufacturingManager(s);

        int techLevel = b.getManufacture().getTechLevel();
        assertEquals(techLevel, mgr.getMaxTechLevel(), "Mgr tech level");

        // Nothign without an engineer
        var o = mgr.getPossibleOutputs();
        assertTrue(o.isEmpty(), "Outputs without engineer");

        // Try agains with an engineer
        buildEngineer(s, 1);
        o = mgr.getPossibleOutputs();
        assertFalse(o.isEmpty(), "Outputs found");
    }

    @Test
    public void testQueueUpdate() {
        var s = buildSettlement("factory", true, MockSettlement.DEFAULT_COORDINATES);
        var mgr = new ManufacturingManager(s);
        buildWorkshop(this.getContext(), s.getBuildingManager());
        buildEngineer(s, 1);

        var p = mgr.getQueuableManuProcesses(null);
        assertFalse(p.isEmpty(), "Queuable processes");

        // No resources
        mgr.updateQueue();
        assertTrue(mgr.getQueue().isEmpty(), "Queue without resources");

        // Add resources and configure paameters
        var pMgr = s.getPreferences();
        pMgr.putValue(ManufacturingParameters.NEW_MANU_LIMIT, 0);
        var first = p.get(0);
        ProcessInfoTest.loadSettlement(s, first);
        var second = p.get(1);
        ProcessInfoTest.loadSettlement(s, second);

        // Resources, no limit
        mgr.updateQueue();
        assertTrue(mgr.getQueue().isEmpty(), "Queue with zero limit");

        // Resources
        pMgr.putValue(ManufacturingParameters.NEW_MANU_LIMIT, 1);
        mgr.updateQueue();
        assertEquals(1, mgr.getQueue().size(), "Queue contains single item");
    }


    @Test
    public void testManuQueuable() {
        var s = buildSettlement("queue");

        var mgr = new ManufacturingManager(s);

        // No workshop
        var p = mgr.getQueuableManuProcesses(null);
        assertTrue(p.isEmpty(), "No Workshop; no queuable");

        var b = buildWorkshop(this.getContext(), s.getBuildingManager());
        int techLevel = b.getManufacture().getTechLevel();
        mgr.updateTechLevel();

        // No queuable because no engineers
        p = mgr.getQueuableManuProcesses(null);
        assertTrue(p.isEmpty(), "No Engineers; no queuable");

        // Match to return potentials only
        buildEngineer(s, 1);
        p = mgr.getQueuableManuProcesses(null);
        assertFalse(p.isEmpty(), "Queuable");

        // Find any offered processes that are beyond the scope
        long tooHard = p.stream()
                        .filter(q -> (q.getSkillLevelRequired() > 1)
                                        || (q.getTechLevelRequired() > techLevel))
                        .count();
        assertEquals(0L, tooHard, "Too hard processes offered");

        // Filter by an output; take output from first process
        var outputResource = p.get(0).getOutputList().get(0).getName();
        p = mgr.getQueuableManuProcesses(outputResource);
        assertFalse(p.isEmpty(), "Output filtered " + outputResource);
        for(var po : p) {
            assertTrue(po.isOutput(outputResource), "Process " + po.getName() + " has " + outputResource);
        }

        // Check filtering with not found output
        p = mgr.getQueuableManuProcesses("non-existent resource");
        assertTrue(p.isEmpty(), "Not output");
    }

    @Test
    public void testManuQueue() {
        var s = buildSettlement("factory", true, MockSettlement.DEFAULT_COORDINATES);

        var mgr = new ManufacturingManager(s);

        // Add 
        var select2 = getManuAtTechLevel(2);
        mgr.addProcessToQueue(select2);
        assertEquals(1, mgr.getQueue().size(), "One item in queue");
        assertEquals(select2, mgr.getQueue().get(0).getInfo(), "Select2 is queued");
        assertEquals(2, mgr.getLowestOnQueue(), "Lowest on queue");

        // Add second lwest process
        var select1 = getManuAtTechLevel(1);
        mgr.addProcessToQueue(select1);
        assertEquals(2, mgr.getQueue().size(), "Items in queue");
        assertEquals(select1, mgr.getQueue().get(1).getInfo(), "Select2 is queued");
        assertEquals(1, mgr.getLowestOnQueue(), "Lowest on queue");
    }
}
