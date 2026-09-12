package com.mars_sim.core.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.goods.GoodType;

public class TestItemResource {

	private static final double MASS = 7.8;
    @BeforeEach
    void setUp() {
        SimulationConfig.loadConfig();
    }

    @Test
    public void testFindByName() {
        var sheetName = "Steel sheet";
        var sheet = ItemResourceUtil.findItemResource(sheetName);

        assertNotNull(sheet);
        assertEquals(MASS, sheet.getMassPerItem(), 0D);
        assertEquals(sheetName, sheet.getName());
        assertEquals(GoodType.CONSTRUCTION, sheet.getGoodType());
    }
    
    @Test
    public void testFindItemResourceNegative() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
        	ItemResourceUtil.findItemResource("test");
        });

        assertEquals("Part 'test' not found.", e.getMessage());
    }

    @Test
    public void testGetById() {
        
        // initialize
       var found = ItemResourceUtil.findItemResource(ItemResourceUtil.BACKHOE_ID);
       
       assertNotNull(found);
       assertEquals(ItemResourceUtil.BACKHOE_ID, found.getID());

    }
}