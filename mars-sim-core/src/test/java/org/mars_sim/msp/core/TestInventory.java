package org.mars_sim.msp.core;

import java.util.Collection;
import java.util.Set;

import junit.framework.TestCase;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.PhaseType;
import org.mars_sim.msp.core.resource.ResourceUtil;

public class TestInventory extends TestCase {

    private static final String CARBON_DIOXIDE = "carbon dioxide";
    private static final String HYDROGEN = "hydrogen";
    private static final String METHANE = "methane";
    private static final String FOOD = LifeSupportInterface.FOOD;

    @Override
    public void setUp() throws Exception {
        SimulationConfig.instance().loadConfig();
    }

    public void testInventoryAmountResourceTypeCapacityGood() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        inventory.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        double amountCO2 = inventory.getAmountResourceCapacity(carbonDioxide, false);
        assertEquals(100D, amountCO2, 0D);
    }

//    public void testInventoryAmountResourceTypeCapacityNegativeCapacity() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
//        try {
//            inventory.addAmountResourceTypeCapacity(carbonDioxide, -100D);
//            fail("Should have thrown exception, cannot add negative");
//        } catch (Exception e) {
//            //expected
//        }
//    }
    
//    /**
//     * Test the removeAmountResourceTypeCapacity method.
//     */
//    public void testRemoveAmountResourceTypeCapacity() {
//        Inventory inventory = new MockUnit1().getInventory();
//        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
//        
//        inventory.addAmountResourceTypeCapacity(carbonDioxide, 100D);
//        double amountCarbonDioxide1 = inventory.getAmountResourceCapacity(carbonDioxide, false);
//        assertEquals(100D, amountCarbonDioxide1);
//        
//        // Test removing 50 kg of CO2 capacity.
//        inventory.removeAmountResourceTypeCapacity(carbonDioxide, 50D);
//        double amountCarbonDioxide2 = inventory.getAmountResourceCapacity(carbonDioxide, false);
//        assertEquals(50D, amountCarbonDioxide2);
//        
//        // Test removing another 50 kg of CO2 capacity.
//        inventory.removeAmountResourceTypeCapacity(carbonDioxide, 50D);
//        double amountCarbonDioxide3 = inventory.getAmountResourceCapacity(carbonDioxide, false);
//        assertEquals(0D, amountCarbonDioxide3);
//        
//        // Test removing another 50 kg of CO2 capacity (should throw IllegalStateException).
//        try {
//            inventory.removeAmountResourceTypeCapacity(carbonDioxide, 50D);
//            fail("Should have thrown an IllegalStateException, no capacity left.");
//        }
//        catch (IllegalStateException e) {
//            // Expected.
//        }
//        double amountCarbonDioxide4 = inventory.getAmountResourceCapacity(carbonDioxide, false);
//        assertEquals(0D, amountCarbonDioxide4);
//    }

    public void testInventoryAmountResourcePhaseCapacityGood() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
        double amountCO2 = inventory.getAmountResourceCapacity(carbonDioxide, false);
        assertEquals(100D, amountCO2, 0D);
    }

//    public void testInventoryAmountResourcePhaseCapacityNegativeCapacity() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        try {
//            inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, -100D);
//            fail("Should have thrown exception, cannot add negative");
//        } catch (Exception e) {
//            //expected
//        }
//    }

    public void testInventoryAmountResourceComboCapacityGood() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, 50D);
        inventory.addAmountResourceTypeCapacity(carbonDioxide, 50D);
        double amountCO2 = inventory.getAmountResourceCapacity(carbonDioxide, false);
        assertEquals(100D, amountCO2, 0D);
    }

    public void testInventoryAmountResourceCapacityNotSet() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        double amountCO2 = inventory.getAmountResourceCapacity(carbonDioxide, false);
        assertEquals(0D, amountCO2, 0D);
    }

    public void testInventoryAmountResourceTypeStoreGood() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        inventory.addAmountResourceTypeCapacity(carbonDioxide, 100D);
        inventory.storeAmountResource(carbonDioxide, 100D, true);
        double amountTypeStored = inventory.getAmountResourceStored(carbonDioxide, false);
        assertEquals(100D, amountTypeStored, 0D);
    }

//    public void testInventoryAmountResourceTypeStoreOverload() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
//        inventory.addAmountResourceTypeCapacity(carbonDioxide, 100D);
//        try {
//            inventory.storeAmountResource(carbonDioxide, 101D, true);
//            fail("Should have thrown exception");
//        } catch (Exception e) {
//            //expected
//        }
//    }

    public void testInventoryAmountResourcePhaseStoreGood() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        AmountResource hydrogen = ResourceUtil.findAmountResource(HYDROGEN);
        inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
        inventory.storeAmountResource(hydrogen, 100D, true);
        double amountPhaseStored = inventory.getAmountResourceStored(hydrogen, false);
        assertEquals(100D, amountPhaseStored, 0D);
    }

//    public void testInventoryAmountResourcePhaseStoreOverload() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        AmountResource hydrogen = ResourceUtil.findAmountResource(HYDROGEN);
//        inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
//        try {
//            inventory.storeAmountResource(hydrogen, 101D, true);
//            fail("Throws exception if overloaded");
//        } catch (Exception e) {
//            //expected
//        }
//    }

//    public void testInventoryAmountResourceStoreNegativeAmount() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
//        inventory.addAmountResourceTypeCapacity(carbonDioxide, 100D);
//        try {
//            inventory.storeAmountResource(carbonDioxide, -1D, true);
//            fail("Throws exception if negative amount");
//        } catch (Exception e) {
//            //expected
//        }
//    }

//    public void testInventoryAmountResourceStoreNoCapacity() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
//        try {
//            inventory.storeAmountResource(carbonDioxide, 100D, true);
//            fail("Throws exception if capacity not set (overloaded)");
//        } catch (Exception e) {
//            //expected
//        }
//    }

//    public void testInventoryAmountResourcePhaseStoreDeep() throws Exception {
//    	Simulation.instance().testRun();
//    	MockUnit1 m1 = new MockUnit1();
//        Inventory inventory = m1.getInventory();
//        AmountResource hydrogen = ResourceUtil.findAmountResource(HYDROGEN);
//        inventory.addGeneralCapacity(130D);
////        System.out.println("Gen Cap " + inventory.getGeneralCapacity());
//        Unit testUnit = new MockUnit3(PhaseType.GAS);
//        inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
//        inventory.storeUnit(testUnit);
//        inventory.storeAmountResource(hydrogen, 100D, true);
////        System.out.println("AR stored " + inventory.getAmountResourceStored(hydrogen, false));
//        double amountPhaseStored = inventory.getAmountResourceStored(hydrogen, false);
//        assertEquals(100D, amountPhaseStored, 0D);
//    }

//    public void testInventoryAmountResourceTypeStoreDeep() throws Exception {
//    	Unit m1 = new MockUnit1();
//        Inventory inventory = m1.getInventory();
//        AmountResource hydrogen = ResourceUtil.findAmountResource(HYDROGEN);
//        System.out.println("Adding " + hydrogen.getName());
//        inventory.addGeneralCapacity(130D);
//        Unit testUnit = new MockUnit3(PhaseType.GAS);
//        testUnit.getInventory().addAmountResourceTypeCapacity(hydrogen, 100D);
//        inventory.storeUnit(testUnit);
//        inventory.storeAmountResource(hydrogen, 100D, true);
//        double amountPhaseStored = inventory.getAmountResourceStored(hydrogen, false);
//        assertEquals(100D, amountPhaseStored, 0D);
//    }

//    public void testInventoryAmountResourceTypeStoreDeepOverload() throws Exception {
//        Unit testUnit1 = new MockUnit1();
//        testUnit1.getInventory().addGeneralCapacity(20D);
//        Unit testUnit2 = new MockUnit1();
//        AmountResource hydrogen = ResourceUtil.findAmountResource(HYDROGEN);
//        testUnit2.getInventory().addAmountResourceTypeCapacity(hydrogen, 100D);
//        testUnit1.getInventory().storeUnit(testUnit2);
//        try {
//            testUnit2.getInventory().storeAmountResource(hydrogen, 100D, true);
//            fail("Fails properly when parent unit's general capacity is overloaded.");
//        } catch (Exception e) {
//            //expected
//        }
//    }

    public void testInventoryAmountResourceRemainingCapacityGood() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        inventory.addAmountResourceTypeCapacity(carbonDioxide, 50D);
        inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, 50D);
        inventory.storeAmountResource(carbonDioxide, 60D, true);
        double remainingCapacity = inventory.getAmountResourceRemainingCapacity(
                ResourceUtil.findAmountResource(CARBON_DIOXIDE), true, false);
        assertEquals(40D, remainingCapacity, 0D);
    }

    public void testInventoryAmountResourceRemainingCapacityMultiple() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        AmountResource methane = ResourceUtil.findAmountResource(METHANE);
        inventory.addAmountResourceTypeCapacity(carbonDioxide, 40D);
        inventory.addAmountResourceTypeCapacity(methane, 20D);
        inventory.getAmountResourceRemainingCapacity(methane, true, false);
        double remainingCapacity = inventory.getAmountResourceRemainingCapacity(
                ResourceUtil.findAmountResource(CARBON_DIOXIDE), true, false);
        assertEquals(40D, remainingCapacity, 0D);
    }
    
//    public void testInventoryAmountResourceRemainingCapacityDeepLimitedGeneral() {
//    	Simulation.instance().testRun();
//        MockUnit1 unit1 = new MockUnit1();
//        unit1.getInventory().addGeneralCapacity(80D);
//        MockUnit3 unit2 = new MockUnit3(PhaseType.SOLID);
//        unit2.getInventory().addAmountResourcePhaseCapacity(PhaseType.SOLID, 100D);
//        unit1.getInventory().storeUnit(unit2);
//        AmountResource food = ResourceUtil.findAmountResource(FOOD);
//        double remainingCapacity = unit1.getInventory().getAmountResourceRemainingCapacity(
//                food, true, false);
//        assertEquals(50D, remainingCapacity);
//    }

    public void testInventoryAmountResourceTypeRemainingCapacityNoCapacity() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        double remainingCapacity = inventory.getAmountResourceRemainingCapacity(carbonDioxide, true, false);
        assertEquals(0D, remainingCapacity, 0D);
    }

    public void testInventoryAmountResourceRetrieveGood() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
        inventory.addAmountResourceTypeCapacity(carbonDioxide, 50D);
        inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, 50D);
        inventory.storeAmountResource(carbonDioxide, 100D, true);
        inventory.retrieveAmountResource(carbonDioxide, 50D);
        double remainingCapacity = inventory.getAmountResourceRemainingCapacity(
                ResourceUtil.findAmountResource(CARBON_DIOXIDE), true, false);
        assertEquals(50D, remainingCapacity, 0D);
    }

//    public void testInventoryAmountResourceRetrieveTooMuch() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
//        inventory.addAmountResourceTypeCapacity(carbonDioxide, 50D);
//        inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, 50D);
//        inventory.storeAmountResource(carbonDioxide, 100D, true);
//        try {
//            inventory.retrieveAmountResource(carbonDioxide, 101D);
//            fail("Amount type retrieved fails correctly.");
//        } catch (Exception e) {
//            //expected
//        }
//    }
//
//    public void testInventoryAmountResourceRetrieveNegative() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
//        inventory.addAmountResourceTypeCapacity(carbonDioxide, 50D);
//        inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, 50D);
//        inventory.storeAmountResource(carbonDioxide, 100D, true);
//        try {
//            inventory.retrieveAmountResource(carbonDioxide, -100D);
//            fail("Amount type retrieved fails correctly.");
//        } catch (Exception e) {
//            //expected
//        }
//    }
//
//    public void testInventoryAmountResourceRetrieveNoCapacity() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        AmountResource carbonDioxide = ResourceUtil.findAmountResource(CARBON_DIOXIDE);
//        try {
//            inventory.retrieveAmountResource(carbonDioxide, 100D);
//            fail("Amount type retrieved fails correctly.");
//        } catch (Exception e) {
//            //expected
//        }
//    }

//    public void testInventoryAmountResourcePhaseRetrieveDeep() throws Exception {
//    	Unit m0 = new MockUnit1();
//        Inventory inventory = m0.getInventory();
//        AmountResource hydrogen = ResourceUtil.findAmountResource(HYDROGEN);
//        System.out.println("Adding " + hydrogen.getName());
//        inventory.addGeneralCapacity(130D);
//        Unit testUnit = new MockUnit3(PhaseType.GAS);
//        testUnit.getInventory().addAmountResourcePhaseCapacity(PhaseType.GAS, 100D);
//        inventory.storeUnit(testUnit);
//        inventory.storeAmountResource(hydrogen, 100D, true);
//        double remainingCapacity1 = inventory.getAmountResourceRemainingCapacity(hydrogen, true, false);
//        System.out.println("remainingCapacity1 : " + remainingCapacity1);
//        assertEquals(0D, remainingCapacity1);
//        inventory.retrieveAmountResource(hydrogen, 50D);
//        double remainingCapacity = inventory.getAmountResourceRemainingCapacity(hydrogen, true, false);
//        System.out.println("remainingCapacity : " + remainingCapacity);
//        assertEquals(50D, remainingCapacity, 0D);
//    }

//    public void testInventoryAmountResourceTypeRetrieveDeep() throws Exception {
//    	Unit m0 = new MockUnit1();
//        Inventory inventory = m0.getInventory();
//        AmountResource hydrogen = ResourceUtil.findAmountResource(HYDROGEN);
//        System.out.println("Adding " + hydrogen.getName());
//        inventory.addGeneralCapacity(130D);
//        Unit testUnit = new MockUnit3(PhaseType.GAS);
//        testUnit.getInventory().addAmountResourceTypeCapacity(hydrogen, 100D);
//        inventory.storeUnit(testUnit);
//        inventory.storeAmountResource(hydrogen, 100D, true);
//        inventory.retrieveAmountResource(hydrogen, 50D);
//        double remainingCapacity = inventory.getAmountResourceRemainingCapacity(hydrogen, true, false);
//        System.out.println("remainingCapacity : " + remainingCapacity);
//        assertEquals(50D, remainingCapacity, 0D);
//    }

//    public void testInventoryAmountResourceAllResources() throws Exception {
//    	Simulation.instance().testRun();
//    	Unit m0 = new MockUnit1();
//        Inventory inventory = m0.getInventory();
//        AmountResource hydrogen = ResourceUtil.findAmountResource(HYDROGEN);
//        AmountResource food = ResourceUtil.findAmountResource(FOOD);
//        inventory.addGeneralCapacity(130D);
//        inventory.addAmountResourcePhaseCapacity(PhaseType.GAS, 20D);
//        inventory.addAmountResourceTypeCapacity(food, 30D);
//        Unit testUnit = new MockUnit3(PhaseType.GAS);
//        testUnit.getInventory().addAmountResourceTypeCapacity(hydrogen, 100D);
//        inventory.storeUnit(testUnit);
//        inventory.storeAmountResource(hydrogen, 120D, true);
//        inventory.storeAmountResource(food, 30D, true);
//        Set<AmountResource> resources = inventory.getAllAmountResourcesStored(false);
//        assertEquals(2, resources.size());
//        assertTrue(resources.contains(hydrogen));
//        assertTrue(resources.contains(food));
//    }

    public void testInventoryAddGeneralCapacity() throws Exception {
    	Unit m0 = new MockUnit1();
        Inventory inventory = m0.getInventory();
        inventory.addGeneralCapacity(100D);
        assertEquals(100D, inventory.getGeneralCapacity(), 0D);
    }

    public void testInventoryItemResourceStoreGood() throws Exception {
        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
        int id = pipeWrench.getID();
      	MockUnit1 m0 = new MockUnit1();
        Inventory inventory = m0.getInventory();
        inventory.addGeneralCapacity(50D);
        inventory.storeItemResources(id, 20);
        int storedResource = inventory.getItemResourceNum(id);
        assertEquals(20, storedResource);
        double storedMass = inventory.getGeneralStoredMass(false);
        assertEquals(50D, storedMass, 0D);
    }

//    public void testInventoryItemResourceStoreDeep() throws Exception {
//        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
//        int id = pipeWrench.getID();
//       	Unit testUnit0 = new MockUnit1();
////    	testUnit0.setContainerID(5); 
//    	Inventory inventory = testUnit0.getInventory();
//        inventory.addGeneralCapacity(60D);
//        Unit testUnit = new MockUnit1();
//        testUnit.getInventory().addGeneralCapacity(50D);
//        inventory.storeUnit(testUnit);
//        testUnit.getInventory().storeItemResources(id, 20);
//        int storedResource = inventory.getItemResourceNum(id);
//        assertEquals(0, storedResource);
//        double storedMass = inventory.getGeneralStoredMass(false);
//        assertEquals(60D, storedMass, 0D);
//    }

////    public void testInventoryItemResourceStoreOverload() throws Exception {
////        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
////        int id = pipeWrench.getID();
////        Inventory inventory = new MockUnit1().getInventory();
////        inventory.addGeneralCapacity(50D);
////        try {
////            inventory.storeItemResources(id, 21);
////            fail("Throws exception if overloaded");
////        } catch (Exception e) {
////            //expected
////        }
////    }
////
////    public void testInventoryItemResourceStoreNegativeNumber() throws Exception {
////        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
////        int id = pipeWrench.getID();
////        Inventory inventory = new MockUnit1().getInventory();
////        inventory.addGeneralCapacity(50D);
////        try {
////            inventory.storeItemResources(id, -1);
////            fail("Throws exception if negative number");
////        } catch (Exception e) {
////            //expected
////        }
////    }
////
////    public void testInventoryItemResourceStoreNoCapacity() throws Exception {
////        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
////        int id = pipeWrench.getID();
////        Inventory inventory = new MockUnit1().getInventory();
////        try {
////            inventory.storeItemResources(id, 1);
////            fail("Throws exception if capacity not set (overloaded)");
////        } catch (Exception e) {
////            //expected
////        }
////    }
//
////    public void testInventoryItemResourceStoreDeepOverload() throws Exception {
////        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
////        int id = pipeWrench.getID();
////        Unit testUnit1 = new MockUnit1();
////        testUnit1.getInventory().addGeneralCapacity(50D);
////        Unit testUnit2 = new MockUnit2();
////        testUnit2.getInventory().addGeneralCapacity(100D);
////        testUnit1.getInventory().storeUnit(testUnit2);
////        try {
////            testUnit2.getInventory().storeItemResources(id, 21);
////            fail("Fails properly when parent unit's general capacity is overloaded.");
////        } catch (Exception e) {
////            //expected
////        }
////    }

    public void testInventoryItemResourceRemainingCapacityGood() throws Exception {
        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
        int id = pipeWrench.getID();
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(50D);
        inventory.storeItemResources(id, 10);
        double remainingCapacity = inventory.getRemainingGeneralCapacity(false);
        assertEquals(25D, remainingCapacity, 0D);
    }

    public void testInventoryItemResourceRemainingCapacityNoCapacity() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        double remainingCapacity = inventory.getRemainingGeneralCapacity(false);
        assertEquals(0D, remainingCapacity, 0D);
    }

    public void testInventoryItemResourceRetrieveGood() throws Exception {
        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
        int id = pipeWrench.getID();
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(50D);
        inventory.storeItemResources(id, 10);
        inventory.retrieveItemResources(id, 5);
        int remainingNum = inventory.getItemResourceNum(id);
        assertEquals(5, remainingNum);
    }

    public void testInventoryItemResourceRetrieveDeep() throws Exception {
        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
        int id = pipeWrench.getID();
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(60D);
        Unit testUnit = new MockUnit1();
        inventory.storeUnit(testUnit);
        testUnit.getInventory().addGeneralCapacity(50D);
        testUnit.getInventory().storeItemResources(id, 10);
        try {
            inventory.retrieveItemResources(id, 5);
            fail("pipewrenches should not be retrievable");
        } catch (Exception e) {
            //expected
        }
        int remainingNum = inventory.getItemResourceNum(id);
        assertEquals(0, remainingNum);
    }

    public void testInventoryItemResourceRetrieveTooMuch() throws Exception {
        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
        int id = pipeWrench.getID();
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(50D);
        inventory.storeItemResources(id, 10);
        try {
            inventory.retrieveItemResources(id, 11);
            fail("Item resource retrieved fails correctly.");
        } catch (Exception e) {
            //expected
        }
    }

    public void testInventoryItemResourceRetrieveNegative() throws Exception {
        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
        int id = pipeWrench.getID();
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(50D);
        inventory.storeItemResources(id, 10);
        try {
            inventory.retrieveItemResources(id, -1);
            fail("Item resource retrieved fails correctly.");
        } catch (Exception e) {
            //expected
        }
    }

    public void testInventoryItemResourceRetrieveNoItem() throws Exception {
        ItemResource pipeWrench = ItemResourceUtil.createItemResource("pipe wrench", 1, "a tool", 2.5D, 1);
        int id = pipeWrench.getID();
        Inventory inventory = new MockUnit1().getInventory();
        try {
            inventory.retrieveItemResources(id, 1);
            fail("Item resource retrieved fails correctly.");
        } catch (Exception e) {
            //expected
        }
    }

    public void testInventoryUnitStoreGood() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(10D);
        Unit testUnit = new MockUnit1();
        inventory.storeUnit(testUnit);
    }

    public void testInventoryUnitStoredDuplicate() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(20D);
        Unit testUnit = new MockUnit1();
//    	testUnit.setContainerID(5);
        inventory.storeUnit(testUnit);
        try {
            inventory.storeUnit(testUnit);
            fail("Duplicate unit stored fails correctly.");
        } catch (Exception e) {
            //expected
        }
    }

    public void testInventoryUnitStoredNoCapacity() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        Unit testUnit = new MockUnit1();
        try {
            inventory.storeUnit(testUnit);
            fail("Unit stored with insufficient capacity fails correctly.");
        } catch (Exception e) {
            //expected
        }
    }

    public void testInventoryUnitStoredUnitContains() throws Exception {
        Unit testUnit1 = new MockUnit1();
//    	testUnit1.setContainerID(5); 
        testUnit1.getInventory().addGeneralCapacity(10D);
        Unit testUnit2 = new MockUnit1();
        testUnit2.getInventory().addGeneralCapacity(10D);
        testUnit1.getInventory().storeUnit(testUnit2);
        try {
            testUnit2.getInventory().storeUnit(testUnit1);
            fail("Unit cannot store another unit that stores it.");
        } catch (Exception e) {
            //expected
        }
    }

//    public void testInventoryUnitStoredItself() throws Exception {
//        Unit testUnit1 = new MockUnit1();
//        testUnit1.getInventory().addGeneralCapacity(10D);
//        try {
//            testUnit1.getInventory().storeUnit(testUnit1);
//            fail("Unit cannot store itself.");
//        } catch (Exception e) {
//            //expected
//        }
//    }

//    public void testInventoryUnitStoredNull() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        inventory.addGeneralCapacity(10D);
//        try {
//            inventory.storeUnit(null);
//            fail("Unit cannot store null unit.");
//        } catch (Exception e) {
//            //expected
//        }
//    }

//    public void testInventoryUnitStoreDeepOverload() throws Exception {
//        Unit testUnit1 = new MockUnit1();
//        testUnit1.getInventory().addGeneralCapacity(30D);
//        Unit testUnit2 = new MockUnit2();
//        testUnit2.getInventory().addGeneralCapacity(100D);
//        testUnit1.getInventory().storeUnit(testUnit2);
//        Unit testUnit3 = new MockUnit2();
//        try {
//            testUnit2.getInventory().storeUnit(testUnit3);
//            fail("Fails properly when parent unit's general capacity is overloaded.");
//        } catch (Exception e) {
//            //expected
//        }
//    }

//    public void testInventoryGetTotalUnitMassStored() throws Exception {
//    	Simulation.instance().testRun();
//       	Unit testUnit0 = new MockUnit1();
////    	testUnit0.setContainerID(5); 
//    	Inventory inventory = testUnit0.getInventory();
//        inventory.addGeneralCapacity(50D);
//        Unit testUnit1 = new MockUnit1();
//        testUnit1.getInventory().addGeneralCapacity(20D);
//        Unit testUnit2 = new MockUnit2();
//        testUnit1.getInventory().storeUnit(testUnit2);
//        inventory.storeUnit(testUnit1);
//        double totalMass = inventory.getUnitTotalMass(false);
//        assertEquals(30D, totalMass, 0D);
//    }

//    public void testInventoryGetContainedUnits() throws Exception {
//    	Unit testUnit0 = new MockUnit1();
//    	testUnit0.setContainerID(5); 
//    	Inventory inventory = testUnit0.getInventory();
//        inventory.addGeneralCapacity(30D);
//        Unit testUnit1 = new MockUnit1();
//        inventory.storeUnit(testUnit1);
//        Unit testUnit2 = new MockUnit2();
//        inventory.storeUnit(testUnit2);
//        int numUnits = inventory.getContainedUnits().size();
//        assertEquals(2, numUnits);
//    }

    public void testInventoryContainsUnitGood() throws Exception {
    	Simulation.instance().testRun();
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(10D);
        Unit testUnit = new MockUnit1();
        inventory.storeUnit(testUnit);
        assertTrue(inventory.containsUnit(testUnit));
    }

    public void testInventoryContainsUnitFail() throws Exception {
    	Simulation.instance().testRun();
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(10D);
        Unit testUnit = new MockUnit1();
        assertTrue(!inventory.containsUnit(testUnit));
    }

//    public void testInventoryContainsUnitClassGood() throws Exception {
//    	Simulation.instance().testRun();
//        Inventory inventory = new MockUnit1().getInventory();
//        inventory.addGeneralCapacity(10D);
//        Unit testUnit = new MockUnit1();
//        inventory.storeUnit(testUnit);
//        assertTrue(inventory.containsUnitClass(MockUnit1.class));
//    }

    public void testInventoryContainsUnitClassFail() throws Exception {
    	Simulation.instance().testRun();
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(10D);
        assertTrue(!inventory.containsUnitClass(MockUnit1.class));
    }

//    public void testInventoryFindUnitGood() throws Exception {
//    	Simulation.instance().testRun();
//        Inventory inventory = new MockUnit1().getInventory();
//        inventory.addGeneralCapacity(10D);
//        Unit testUnit = new MockUnit1();
//        inventory.storeUnit(testUnit);
//        Unit found = inventory.findUnitOfClass(MockUnit1.class);
//        assertEquals(testUnit, found);
//    }

    public void testInventoryFindUnitFail() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(10D);
        Unit found = inventory.findUnitOfClass(MockUnit1.class);
        assertEquals(null, found);
    }

//    public void testInventoryFindAllUnitsGood() throws Exception {
//    	SimulationConfig.instance().loadConfig();
//    	Simulation.instance().testRun();
//    	Unit testUnit0 = new MockUnit1(); // mass of 10
//    	Inventory inventory = testUnit0.getInventory();
//        inventory.addGeneralCapacity(20D);
//        
//        Unit testUnit1 = new MockUnit4("MockUnit 4 1"); // mass of 10
//        Unit testUnit2 = new MockUnit4("MockUnit 4 2"); // mass of 10
//        inventory.storeUnit(testUnit1);
//        inventory.storeUnit(testUnit2);
//        Collection<Unit> units = inventory.findAllUnitsOfClass(MockUnit4.class);
//        assertEquals(2, units.size());
//        assertTrue(units.contains(testUnit1));
//        assertTrue(units.contains(testUnit2));
//    }

    public void testInventoryFindAllUnitsFail() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(20D);
        Collection<Unit> units = inventory.findAllUnitsOfClass(MockUnit1.class);
        assertEquals(0, units.size());
    }

//    public void testInventoryFindNumUnitsGood() throws Exception {
//    	Simulation.instance().testRun();
//        Inventory inventory = new MockUnit1().getInventory();
//        inventory.addGeneralCapacity(20D);
//        Unit testUnit1 = new MockUnit4("MockUnit 4 1");
//        Unit testUnit2 = new MockUnit4("MockUnit 4 2");
//        inventory.storeUnit(testUnit1);
//        inventory.storeUnit(testUnit2);
//        int numUnits = inventory.findNumUnitsOfClass(MockUnit4.class);
//        assertEquals(2, numUnits);
//    }

    public void testInventoryFindNumUnitsFail() throws Exception {
        Inventory inventory = new MockUnit1().getInventory();
        inventory.addGeneralCapacity(20D);
        int numUnits = inventory.findNumUnitsOfClass(MockUnit1.class);
        assertEquals(0, numUnits);
    }

//    public void testInventoryRetrieveUnitGood() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        inventory.addGeneralCapacity(10D);
//        Unit testUnit = new MockUnit1();
//        inventory.storeUnit(testUnit);
//        inventory.retrieveUnit(testUnit);
//    }

//    public void testInventoryRetrieveUnitBad() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        inventory.addGeneralCapacity(10D);
//        Unit testUnit = new MockUnit1();
//        try {
//            inventory.retrieveUnit(testUnit);
//            fail("testUnit not found.");
//        } catch (Exception e) {
//            //expected
//        }
//    }

//    public void testInventoryRetrieveUnitDouble() throws Exception {
//        Inventory inventory = new MockUnit1().getInventory();
//        inventory.addGeneralCapacity(10D);
//        Unit testUnit = new MockUnit1();
//        inventory.storeUnit(testUnit);
//        inventory.retrieveUnit(testUnit);
//        try {
//            inventory.retrieveUnit(testUnit);
//            fail("testUnit retrieved twice.");
//        } catch (Exception e) {
//            //expected
//        }
//    }
}