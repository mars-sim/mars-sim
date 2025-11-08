package com.mars_sim.core.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.goods.GoodType;

public class TestItemResource {

    @BeforeEach
    void setUp() {
        SimulationConfig.loadConfig();
    }

    @Test
    public void testFindByName() {
        var sheetName = "Steel sheet";
        var sheet = ItemResourceUtil.findItemResource(sheetName);

        assertNotNull(sheet);
        assertEquals(7.8D, sheet.getMassPerItem(), 0D);
        assertEquals(sheetName, sheet.getName());
        assertEquals(GoodType.METALLIC, sheet.getGoodType());
    }
    
    @Test
    public void testFindItemResourceNegative() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
        	ItemResourceUtil.findItemResource("test");
        });

        assertEquals(e.getMessage(), "Part 'test' not found.");
    }

    @Test
    public void testGetById() {
        
        // initialize
       var found = ItemResourceUtil.findItemResource(ItemResourceUtil.BACKHOE_ID);
       
       assertNotNull(found);
       assertEquals(ItemResourceUtil.BACKHOE_ID, found.getID());

    }
}
