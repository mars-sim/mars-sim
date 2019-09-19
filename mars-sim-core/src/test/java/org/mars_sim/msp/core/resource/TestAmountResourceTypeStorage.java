package org.mars_sim.msp.core.resource;

import java.util.Set;

import junit.framework.TestCase;

import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.SimulationConfig;

public class TestAmountResourceTypeStorage extends TestCase {

    private static final String CARBON_DIOXIDE = "carbon dioxide";
    private static final String OXYGEN = LifeSupportInterface.OXYGEN;

    @Override
    public void setUp() throws Exception {
        SimulationConfig.instance().loadConfig();
    }

    public void testInventoryAmountResourceTypeCapacityGood() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        double amountCO2 = storage.getAmountResourceTypeCapacity(carbonDioxide);
        assertEquals("Amount resource type capacity set correctly.", 100D, amountCO2, 0D);
    }

    public void testInventoryAmountResourceTypeCapacityNotSet() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        double amountCO2 = storage.getAmountResourceTypeCapacity(carbonDioxide);
        assertEquals("Amount resource type capacity set correctly.", 0D, amountCO2, 0D);
    }

    public void testInventoryAmountResourceTypeCapacityNegativeCapacity() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
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
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);

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

    public void testInventoryAmountResourceTypeStoreGood() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.storeAmountResourceType(carbonDioxide, 100D);
        double amountTypeStored = storage.getAmountResourceTypeStored(carbonDioxide);
        assertEquals("Amount resource type stored is correct.", 100D, amountTypeStored, 0D);
    }

    public void testInventoryAmountResourceTypeStoreOverload() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        try {
            storage.storeAmountResourceType(carbonDioxide, 101D);
            fail("Throws exception if overloaded");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeStoreNegativeAmount() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        try {
            storage.storeAmountResourceType(carbonDioxide, -1D);
            fail("Throws exception if negative amount");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeStoreNoCapacity() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        try {
            storage.storeAmountResourceType(carbonDioxide, 100D);
            fail("Throws exception if capacity not set (overloaded)");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeRemainingCapacityGood() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.storeAmountResourceType(carbonDioxide, 50D);
        double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
        assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
    }

    public void testInventoryAmountResourceTypeRemainingCapacityNoCapacity() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
        assertEquals("Amount type capacity remaining is correct amount.", 0D, remainingCapacity, 0D);
    }

    public void testInventoryAmountResourceTypeRetrieveGood() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.storeAmountResourceType(carbonDioxide, 100D);
        storage.retrieveAmountResourceType(carbonDioxide, 50D);
        double remainingCapacity = storage.getAmountResourceTypeRemainingCapacity(carbonDioxide);
        assertEquals("Amount type capacity remaining is correct amount.", 50D, remainingCapacity, 0D);
    }

    public void testInventoryAmountResourceTypeRetrieveTooMuch() throws Exception {
        try {
            AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
            AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
            storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
            storage.storeAmountResourceType(carbonDioxide, 100D);
            storage.retrieveAmountResourceType(carbonDioxide, 101D);
            fail("Amount type retrieved fails correctly.");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeRetrieveNegative() throws Exception {
        try {
            AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
            AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
            storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
            storage.storeAmountResourceType(carbonDioxide, 100D);
            storage.retrieveAmountResourceType(carbonDioxide, -100D);
            fail("Amount type retrieved fails correctly.");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeRetrieveNoCapacity() throws Exception {
        try {
            AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
            AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
            storage.retrieveAmountResourceType(carbonDioxide, 100D);
            fail("Amount type retrieved fails correctly.");
        }
        catch (Exception e) {}
    }

    public void testInventoryAmountResourceTypeTotalAmount() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        AmountResource oxygen = ResourceUtil.findAmountResource(OXYGEN);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.addAmountResourceTypeCapacity(oxygen, 100D);
        storage.storeAmountResourceType(carbonDioxide, 10D);
        storage.storeAmountResourceType(oxygen, 20D);
        double totalStored = storage.getTotalAmountResourceTypesStored(false);
        assertEquals("Amount total stored is correct.", 30D, totalStored, 0D);
    }

    public void testInventoryAmountResourceTypeAllStored() throws Exception {
        AmountResourceTypeStorage storage = new AmountResourceTypeStorage();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        AmountResource oxygen = ResourceUtil.findAmountResource(OXYGEN);
        storage.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        storage.addAmountResourceTypeCapacity(oxygen, 100D);
        storage.storeAmountResourceType(carbonDioxide, 10D);
        storage.storeAmountResourceType(oxygen, 20D);
        Set<AmountResource> allResources = storage.getAllAmountResourcesStored();
        assertTrue("All resources contains carbon dioxide.", allResources.contains(carbonDioxide));
        assertTrue("All resources contains oxygen.", allResources.contains(oxygen));
    }
}