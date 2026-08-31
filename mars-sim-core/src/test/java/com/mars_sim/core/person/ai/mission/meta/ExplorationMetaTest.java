package com.mars_sim.core.person.ai.mission.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.MarsSimContext;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.mineral.RandomMineralFactory;
import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.MetaMission.Roster;
import com.mars_sim.core.mission.MissionCreationException;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

class ExplorationMetaTest extends MarsSimUnitTest {

    @Test
    void testConstructFullySpecified() {
        var mt = new ExplorationMeta();

        var s = buildSettlement("Test");

        var crew = buildEVACrew(getContext(), s, EquipmentType.SPECIMEN_BOX, 10, EXPLORER_ROVER);

        List<MineralSite> sites = buildSites(s, 2);
        var m = (VehicleMission) mt.constructInstance(crew, false, sites);

        assertNotNull(m, "Exploration mission instance should not be null");
        assertFalse(m.isDone(), "Exploration mission should not be done upon construction");
        var waypoints = m.getNavpoints();
        assertEquals(sites.size() + 1, waypoints.size(), "Exploration mission should have waypoints for each site plus the settlement");
   
        for(int idx = 0; idx < sites.size(); idx++) {
            assertEquals(sites.get(idx).getLocation(), waypoints.get(idx).getCoordinates(), "Waypoint #" + idx + " should match the mineral site coordinates");
        }

        // Back hone
        assertEquals(s.getCoordinates(), waypoints.get(waypoints.size() - 1).getCoordinates(), "Last waypoint should be the settlement coordinates");
    }

    
    @Test
    void testConstructSearch() throws MissionCreationException {
        var mt = new ExplorationMeta();

        var s = buildSettlement("Test");

        var crew = buildEVACrew(getContext(), s, EquipmentType.SPECIMEN_BOX, 10, EXPLORER_ROVER);

        var sites = buildSites(s, mt.getExpectedSites(s) + 1);

        var m = (VehicleMission) mt.constructInstance(crew, false);

        assertNotNull(m, "Exploration mission instance should not be null");
        assertFalse(m.isDone(), "Exploration mission should not be done upon construction");
        var waypoints = m.getNavpoints();
        assertEquals(mt.getExpectedSites(s) + 1, waypoints.size(), "Exploration mission should have waypoints for each site plus the settlement");
   
        assertEquals(s.getCoordinates(), waypoints.get(waypoints.size() - 1).getCoordinates(), "Last waypoint should be the settlement coordinates");

        var claimedLocns = sites.stream().map(MineralSite::getCoordinates).toList();
        for(var st = 0; st < mt.getExpectedSites(s); st++) {
            assertTrue(claimedLocns.contains(waypoints.get(st).getCoordinates()),
                            "Waypoint #" + st + " out of " + sites.size() + " should match the mineral site coordinates");
        }
    }

    private List<MineralSite> buildSites(Settlement s, int i) {
        var eMgr = s.getExplorations();

        var mineralMap = getSim().getSurfaceFeatures().getMineralMap();
        RandomMineralFactory.createLocalConcentration(mineralMap, s.getCoordinates());

        List<MineralSite> sites = new ArrayList<>();
        for(int j = 0; j < i; j++) {
            var found = mineralMap.findRandomMineralLocation(s.getCoordinates(), 20, List.of());
            assertNotNull(found, "Expected to find a nearby mineral location");

            MineralSite site = eMgr.createROI(found.getKey(), 100);
            assertNotNull(site, "Exploration manager should create a mineral site ROI");
            sites.add(site);
        }
        
        return sites;
    }


    /**
     * This builds a Roster crew for an EVA mission needing an optional set of Containers.
     * @param context The test context to build the crew.
     * @param s The settlement to build the crew in.
     * @param containterType The type of container to build for the mission; optional if containerCount is 0.
     * @param containerCount The number of containers to build for the mission.
     * @param roverType The type of rover to build for the mission.
     * @return A Roster of crew members for the EVA mission.
     */
    public static MetaMission.Roster buildEVACrew(MarsSimContext context, Settlement s, EquipmentType containterType,
        int containerCount, String roverType) {
  
        var l = context.buildPerson("Leader", s);
        var rover = context.buildRover(s, "Test", LocalPosition.DEFAULT_POSITION, roverType);

        var w = context.buildPerson("Worker1", s);
        var members = List.of(w);
        Roster crew = new Roster(l, members, rover);

        // Build EVA suite to support mission
        for(int i = 0; i < members.size() + 1; i++) {
            EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
        }

        var resources = Map.of(ResourceUtil.OXYGEN_ID, 200D,
                ResourceUtil.WATER_ID, 200D,
                ResourceUtil.FOOD_ID, 200D,
                ResourceUtil.METHANOL_ID, 200D);
        loadSettlementAmounts(s, resources);

        for(int c = 0; c < containerCount; c++) {
            EquipmentFactory.createEquipment(containterType, s);
        }

        return crew;
    }
}
