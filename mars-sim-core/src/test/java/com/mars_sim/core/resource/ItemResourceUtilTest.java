package com.mars_sim.core.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.goods.GoodType;

public class ItemResourceUtilTest {

	private static final double MASS = 7.8;
    @BeforeEach
    void setUp() {
        SimulationConfig.loadConfig();
    }

    @Test
    void testFindByName() {
        var sheetName = "Steel sheet";
        var sheet = ItemResourceUtil.findItemResource(sheetName);

        assertNotNull(sheet);
        assertEquals(MASS, sheet.getMassPerItem(), 0D);
        assertEquals(sheetName, sheet.getName());
        assertEquals(GoodType.CONSTRUCTION, sheet.getGoodType());
    }
    
    @Test
    void testFindItemResourceNegative() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
        	ItemResourceUtil.findItemResource("test");
        });

        assertEquals("Part 'test' not found.", e.getMessage());
    }

    @Test
    void testGetById() {
        
        // initialize
       var found = ItemResourceUtil.findItemResource(ItemResourceUtil.BACKHOE_ID);
       
       assertNotNull(found);
       assertEquals(ItemResourceUtil.BACKHOE_ID, found.getID());

    }

    @Test
    void testGetEVASuitParts() {
        assertNotNull(ItemResourceUtil.getEVASuitParts());
        assertFalse(ItemResourceUtil.getEVASuitParts().isEmpty(), "EVA suit parts should be discovered");
    }

    @Test
    void testGetDataRecordParts() {
        assertNotNull(ItemResourceUtil.getDataRecordParts());
        assertFalse(ItemResourceUtil.getDataRecordParts().isEmpty(), "Data recorder parts should be discovered");
    }

    @Test
    void testGetBotParts() {
        assertNotNull(ItemResourceUtil.getBotParts());
        assertFalse(ItemResourceUtil.getBotParts().isEmpty(), "Repair bot parts should be discovered");
    }
}