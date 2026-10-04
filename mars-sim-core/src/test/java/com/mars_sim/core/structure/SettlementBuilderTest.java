package com.mars_sim.core.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.data.HistoryTracable;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.tool.MsgContext;
import com.mars_sim.core.vehicle.Vehicle;

class SettlementBuilderTest extends MarsSimUnitTest{
    /**
     * This test takes a few seconds to run as it creates a lot of objects for a full settlement.
     */
    @Test
    void testCreateFullSettlement() {

        var templateName = "Alpha Base 1";
        var expectedAuthority = "NASA";
        var expectedPop = 20;
        var expectedName = "Test Settlement";
        Coordinates expectedLocn = new Coordinates(10, 20);
        var template = getConfig().getSettlementTemplateConfiguration().getItem(templateName);

        SettlementBuilder builder = new SettlementBuilder(getSim(), getConfig(), null);

        var historyEntry = new MsgContext("test", "builder");
        // Build the settlement 
        InitialSettlement initialSettlement = new InitialSettlement(expectedName, expectedAuthority, templateName, expectedPop, expectedLocn, null);
        Settlement settlement = builder.createFullSettlement(initialSettlement, historyEntry);

        assertNotNull(settlement, "Settlement");
        assertEquals(expectedName, settlement.getName(), "Settlement name");
        assertEquals(expectedLocn, settlement.getLocation(), "Settlement location");
        assertEquals(expectedPop, settlement.getCitizens().size(), "Settlement population");
        assertEquals(expectedAuthority, settlement.getReportingAuthority().getName(), "Settlement sponsor");

        assertHistory(settlement, historyEntry);

        // Must force to Integer not Long
        var robots = settlement.getAllAssociatedRobots();
        Map<String,Integer> actualRobots = robots.stream()
                .map(Robot::getModel)
                .collect(Collectors.groupingBy(name -> name, Collectors.reducing(0, e -> 1, (a,b) -> a+b)));
        assertEquals(template.getSupplies().getRobots(), actualRobots, "Robot count");
        assertHistory((new ArrayList<>(robots)).get(0), historyEntry);

        var vehicles = settlement.getAllAssociatedVehicles();
        Map<String,Integer> actualVehicles = vehicles.stream()
                .map(Vehicle::getSpecName)
                .collect(Collectors.groupingBy(name -> name, Collectors.reducing(0, e -> 1, (a,b) -> a+b)));
        assertEquals(template.getSupplies().getVehicles(), actualVehicles, "Vehicle count");
        assertHistory((new ArrayList<>(vehicles)).get(0), historyEntry);

        Map<String,Integer> actualEquipment = settlement.getEquipmentInventory().getEquipmentSet().stream()
                .map(e -> e.getEquipmentType().getName().toLowerCase())
                .collect(Collectors.groupingBy(name -> name, Collectors.reducing(0, e -> 1, (a,b) -> a+b)));
        assertEquals(template.getSupplies().getEquipment(), actualEquipment, "Equipment count");
    }

    private void assertHistory(HistoryTracable source, MsgContext historyEntry) {
        var h = source.getHistory().getChanges();
        assertEquals(1, h.size(), "History contains one entry");
        var he = h.get(0);
        assertEquals(historyEntry, he.getWhat(), "History first entry matches the expected entry");
    }
}
