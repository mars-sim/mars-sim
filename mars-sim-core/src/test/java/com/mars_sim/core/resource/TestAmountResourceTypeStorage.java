package com.mars_sim.core.resource;

import java.util.Set;

import com.mars_sim.core.SimulationConfig;

import junit.framework.TestCase;

public class TestAmountResourceTypeStorage extends TestCase {

    @Override
    public void setUp() {
        SimulationConfig.loadConfig();
    }

    public void testInventoryAmountResourceTypeCapacityGood() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        double amountCO2 = storage.getAmountResourceTypeCapacity(carbonDioxide);
        assertEquals("Amount resource type capacity set correctly.", 100D, amountCO2, 0D);
    }

    public void testInventoryAmountResourceTypeCapacityNotSet() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        double amountCO2 = storage.getAmountResourceTypeCapacity(carbonDioxide);
        assertEquals("Amount resource type capacity set correctly.", 0D, amountCO2, 0D);
    }

    public void testInventoryAmountResourceTypeCapacityNegativeCapacity() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        try {
            storage.addAmountResourceTypeCapacity(carbonDioxide, -100D);
            fail("Cannot add negative capacity for a type.");
        }
        catch (Exception e) {}
    }

    /**
     * Test the removeAmountResourceTypeCapacity method.
     */
    public void testRemoveAmountResourceTypeCapacity() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);

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

    public void testInventoryAmountResourceTypeStoreGood() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.storeAmountResourceType(carbonDioxide, 100D);
        double amountTypeStored = storage.getAmountResourceTypeStored(carbonDioxide);
        assertEquals("Amount resource type stored is correct.", 100D, amountTypeStored, 0D);
    }

    public void testInventoryAmountResourceTypeStoreOverload() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        try {
            storage.storeAmountResourceType(carbonDioxide, 101D);
            fail("Throws exception if overloaded");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeStoreNegativeAmount() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        try {
            storage.storeAmountResourceType(carbonDioxide, -1D);
            fail("Throws exception if negative amount");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeStoreNoCapacity() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        try {
            storage.storeAmountResourceType(carbonDioxide, 100D);
            fail("Throws exception if capacity not set (overloaded)");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeRemainingCapacityGood() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.storeAmountResourceType(carbonDioxide, 50D);
        double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
        assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
    }

    public void testInventoryAmountResourceTypeRemainingCapacityNoCapacity() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
        assertEquals("Amount type capacity remaining is correct amount.", 0D, remainingCapacity, 0D);
    }

    public void testInventoryAmountResourceTypeRetrieveGood() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.storeAmountResourceType(carbonDioxide, 100D);
        storage.retrieveAmountResourceType(carbonDioxide, 50D);
        double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
        assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
    }

    public void testInventoryAmountResourceTypeRetrieveTooMuch() {
        try {
            AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
            AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
            storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
            storage.storeAmountResourceType(carbonDioxide, 100D);
            storage.retrieveAmountResourceType(carbonDioxide, 101D);
            fail("Amount type retrieved fails correctly.");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeRetrieveNegative() {
        try {
            AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
            AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
            storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
            storage.storeAmountResourceType(carbonDioxide, 100D);
            storage.retrieveAmountResourceType(carbonDioxide, -100D);
            fail("Amount type retrieved fails correctly.");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeRetrieveNoCapacity() {
        try {
            AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
            AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
            storage.retrieveAmountResourceType(carbonDioxide, 100D);
            fail("Amount type retrieved fails correctly.");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeTotalAmount() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        AmountResource oxygen = ResourceUtil.findAmountResource(ResourceUtil.oxygenID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.addAmountResourceTypeCapacity(oxygen, 100D);
        storage.storeAmountResourceType(carbonDioxide, 10D);
        storage.storeAmountResourceType(oxygen, 20D);
        double totalStored = storage.getTotalAmountResourceTypesStored(false);
        assertEquals("Amount total stored is correct.", 30D, totalStored, 0D);
    }

    public void testInventoryAmountResourceTypeAllStored() {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(ResourceUtil.co2ID);
        AmountResource oxygen = ResourceUtil.findAmountResource(ResourceUtil.oxygenID);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.addAmountResourceTypeCapacity(oxygen, 100D);
        storage.storeAmountResourceType(carbonDioxide, 10D);
        storage.storeAmountResourceType(oxygen, 20D);
        Set<AmountResource> allResources = storage.getAllAmountResourcesStored();
        assertTrue("All resources contains carbon dioxide.", allResources.contains(carbonDioxide));
        assertTrue("All resources contains oxygen.", allResources.contains(oxygen));
    }
}