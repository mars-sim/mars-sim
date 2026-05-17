package com.mars_sim.core.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.ai.task.util.MetaTaskUtil;
import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.vehicle.Vehicle;

class SettlementBuilderTest extends MarsSimUnitTest{
    /**
     * This test takes a few seconds to run as it creates a lot of objects for a full settlement.
     */
    @Test
    void testCreateFullSettlement() {

        MetaTaskUtil.initializeMetaTasks();

        var templateName = "Alpha Base 1";
        var expectedAuthority = "NASA";
        var expectedPop = 20;
        var expectedName = "Test Settlement";
        Coordinates expectedLocn = new Coordinates(10, 20);
        var template = getConfig().getSettlementTemplateConfiguration().getItem(templateName);

        SettlementBuilder builder = new SettlementBuilder(getSim(), getConfig(), null);

        // Build the settlement 
        InitialSettlement initialSettlement = new InitialSettlement(expectedName, expectedAuthority, templateName, expectedPop, 0, expectedLocn, null);
        Settlement settlement = builder.createFullSettlement(initialSettlement);

        assertNotNull(settlement, "Settlement");
        assertEquals(expectedName, settlement.getName(), "Settlement name");
        assertEquals(expectedLocn, settlement.getLocation(), "Settlement location");
        assertEquals(expectedPop, settlement.getCitizens().size(), "Settlement population");
        assertEquals(expectedAuthority, settlement.getReportingAuthority().getName(), "Settlement sponsor");

        // Must force to Interger not Long
        Map<String,Integer> actualRobots = settlement.getAllAssociatedRobots().stream()
                .map(r -> r.getRobotType().getName() + "-" + r.getModel())
                .collect(Collectors.groupingBy(name -> name, Collectors.reducing(0, e -> 1, (a,b) -> a+b)));
        assertEquals(template.getSupplies().getRobots(), actualRobots, "Robot count");

        Map<String,Integer> actualVehicles = settlement.getAllAssociatedVehicles().stream()
                .map(Vehicle::getSpecName)
                .collect(Collectors.groupingBy(name -> name, Collectors.reducing(0, e -> 1, (a,b) -> a+b)));
        assertEquals(template.getSupplies().getVehicles(), actualVehicles, "Vehicle count");

        Map<String,Integer> actualEquipment = settlement.getEquipmentSet().stream()
                .map(e -> e.getEquipmentType().getName().toLowerCase())
                .collect(Collectors.groupingBy(name -> name, Collectors.reducing(0, e -> 1, (a,b) -> a+b)));
        assertEquals(template.getSupplies().getEquipment(), actualEquipment, "Equipment count");
    }
}
