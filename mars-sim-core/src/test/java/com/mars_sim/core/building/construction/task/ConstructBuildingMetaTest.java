package com.mars_sim.core.building.construction.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.building.construction.ConstructionManager;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionSite.ConstructionPhase;
import com.mars_sim.core.building.construction.MockMission;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

public class ConstructBuildingMetaTest extends MarsSimUnitTest {

    private ConstructionSite buildSite(Settlement s, String buildingName) {

        var phases = ConstructionManager.getConstructionStages(buildingName)
                .stream()
                .map(stageInfo -> new ConstructionPhase(stageInfo, true))
                .toList();

        return s.getConstructionManager().createNewConstructionSite(buildingName,
                    new BoundedObject(1,1,10,10, 0), phases);
    }

    
    @Test
    void testGetWithoutMission() {
        var s = buildSettlement("Construct");

        buildSite(s, "Garage");

        var meta = new ConstructBuildingMeta();
        var tasks = meta.getSettlementTasks(s);
        assertTrue(tasks.isEmpty(), "No Tasks found");
    }


    private static void loadMaterials(Settlement s, ConstructionSite site) {

        var stage = site.getCurrentConstructionStage();

        for(var r : stage.getResources().entrySet()) {
            s.storeAmountResource(r.getKey(), r.getValue().getRequired() * 1.2);
        }
        for(var p :stage.getParts().entrySet()) {
            s.storeItemResource(p.getKey(), (int)(p.getValue().getRequired() * 1.2));
        }

        stage.loadAvailableConstructionMaterials(s);
    }

    @Test
    void testGetSettlementTasks() {
        var s = buildSettlement("Construct");

        var site = buildSite(s, "Garage");
        site.setWorkOnSite(new MockMission());

        var meta = new ConstructBuildingMeta();
        var tasks = meta.getSettlementTasks(s);
        assertTrue(tasks.isEmpty(), "No tasks found without resources");

        loadMaterials(s, site);
        tasks = meta.getSettlementTasks(s);
        assertEquals(1, tasks.size(), "One task found with resources");

        var t = tasks.get(0);
        assertEquals(site, t.getFocus(), "Site");
        assertTrue(t.getScore().getScore() > 0, "+ve score");
    }
}
