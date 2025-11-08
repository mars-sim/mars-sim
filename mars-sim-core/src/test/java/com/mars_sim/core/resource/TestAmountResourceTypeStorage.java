package com.mars_sim.core.resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


import java.util.Set;

import com.mars_sim.core.SimulationConfig;



public class TestAmountResourceTypeStorage {

    @BeforeEach





    public void setUp() {
        SimulationConfig.loadConfig();
    }

    @Test
    void testInventoryAmountResourceTypeCapacityGood() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        double amountCO2 = storage.getAmountResourceTypeCapacity(carbonDioxide);
        assertEquals(100D, amountCO2, 0D, "Amount resource type capacity set correctly.");
    }

    @Test
    void testInventoryAmountResourceTypeCapacityNotSet() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        double amountCO2 = storage.getAmountResourceTypeCapacity(carbonDioxide);
        assertEquals(0D, amountCO2, 0D, "Amount resource type capacity set correctly.");
    }

    @Test
    void testInventoryAmountResourceTypeCapacityNegativeCapacity() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        try {
            storage.addAmountResourceTypeCapacity(carbonDioxide, -100D);
            fail("Cannot add negative capacity for a type.");
        }
        catch (Exception e) {}
    }

    /**
     * Test the removeAmountResourceTypeCapacity method.
     */
    @Test
    void testRemoveAmountResourceTypeCapacity() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);

        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        double amountCarbonDioxide1 = storage.getAmountResourceTypeCapacity(carbonDioxide);
        assertEquals(100D, amountCarbonDioxide1);

        // Test removing 50 kg of CO2 capacity.
        storage.removeAmountResourceTypeCapacity(carbonDioxide, 50D);
        double amountCarbonDioxide2 = storage.getAmountResourceTypeCapacity(carbonDioxide);
        assertEquals(50D, amountCarbonDioxide2);

        // Test removing another 50 kg of CO2 capacity.
        storage.removeAmountResourceTypeCapacity(carbonDioxide, 50D);
        double amountCarbonDioxide3 = storage.getAmountResourceTypeCapacity(carbonDioxide);
        assertEquals(0D, amountCarbonDioxide3);

        // Test removing another 50 kg of CO2 capacity (should throw IllegalStateException).
        try {
            storage.removeAmountResourceTypeCapacity(carbonDioxide, 50D);
            fail("Should have thrown an IllegalStateException, no capacity left.");
        }
        catch (IllegalStateException e) {
            // Expected.
        }
        double amountCarbonDioxide4 = storage.getAmountResourceTypeCapacity(carbonDioxide);
        assertEquals(0D, amountCarbonDioxide4);
    }

    @Test
    void testInventoryAmountResourceTypeStoreGood() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.storeAmountResourceType(carbonDioxide, 100D);
        double amountTypeStored = storage.getAmountResourceTypeStored(carbonDioxide);
        assertEquals(100D, amountTypeStored, 0D, "Amount resource type stored is correct.");
    }

    @Test
    void testInventoryAmountResourceTypeStoreOverload() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        try {
            storage.storeAmountResourceType(carbonDioxide, 101D);
            fail("Throws exception if overloaded");
        }
        catch (Exception e) {}
    }

    @Test
    void testInventoryAmountResourceTypeStoreNegativeAmount() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        try {
            storage.storeAmountResourceType(carbonDioxide, -1D);
            fail("Throws exception if negative amount");
        }
        catch (Exception e) {}
    }

    @Test
    void testInventoryAmountResourceTypeStoreNoCapacity() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        try {
            storage.storeAmountResourceType(carbonDioxide, 100D);
            fail("Throws exception if capacity not set (overloaded)");
        }
        catch (Exception e) {}
    }

    @Test
    void testInventoryAmountResourceTypeRemainingCapacityGood() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.storeAmountResourceType(carbonDioxide, 50D);
        double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
        assertEquals(50D, remainingCapacity, 0D, "Amount type capacity remaining is correct amount.");
    }

    @Test
    void testInventoryAmountResourceTypeRemainingCapacityNoCapacity() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
        assertEquals(0D, remainingCapacity, 0D, "Amount type capacity remaining is correct amount.");
    }

    @Test
    void testInventoryAmountResourceTypeRetrieveGood() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.storeAmountResourceType(carbonDioxide, 100D);
        storage.retrieveAmountResourceType(carbonDioxide, 50D);
        double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
        assertEquals(50D, remainingCapacity, 0D, "Amount type capacity remaining is correct amount.");
    }

    @Test
    void testInventoryAmountResourceTypeRetrieveTooMuch() {
        try {
            AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
            AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
            storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
            storage.storeAmountResourceType(carbonDioxide, 100D);
            storage.retrieveAmountResourceType(carbonDioxide, 101D);
            fail("Amount type retrieved fails correctly.");
        }
        catch (Exception e) {}
    }

    @Test
    void testInventoryAmountResourceTypeRetrieveNegative() {
        try {
            AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
            AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
            storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
            storage.storeAmountResourceType(carbonDioxide, 100D);
            storage.retrieveAmountResourceType(carbonDioxide, -100D);
            fail("Amount type retrieved fails correctly.");
        }
        catch (Exception e) {}
    }

    @Test
    void testInventoryAmountResourceTypeRetrieveNoCapacity() {
        try {
            AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
            AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
            storage.retrieveAmountResourceType(carbonDioxide, 100D);
            fail("Amount type retrieved fails correctly.");
        }
        catch (Exception e) {}
    }

    @Test
    void testInventoryAmountResourceTypeTotalAmount() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        AmountResource oxygen = ResourceUtil.findAmountResource(ResourceUtil.OXYGEN_ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.addAmountResourceTypeCapacity(oxygen, 100D);
        storage.storeAmountResourceType(carbonDioxide, 10D);
        storage.storeAmountResourceType(oxygen, 20D);
        double totalStored = storage.getTotalAmountResourceTypesStored(false);
        assertEquals(30D, totalStored, 0D, "Amount total stored is correct.");
    }

    @Test
    void testInventoryAmountResourceTypeAllStored() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.CO2_ID);
        AmountResource oxygen = ResourceUtil.findAmountResource(ResourceUtil.OXYGEN_ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.addAmountResourceTypeCapacity(oxygen, 100D);
        storage.storeAmountResourceType(carbonDioxide, 10D);
        storage.storeAmountResourceType(oxygen, 20D);
        Set<AmountResource> allResources = storage.getAllAmountResourcesStored();
        assertTrue(allResources.contains(carbonDioxide), "All resources contains carbon dioxide.");
        assertTrue(allResources.contains(oxygen), "All resources contains oxygen.");
    }
}