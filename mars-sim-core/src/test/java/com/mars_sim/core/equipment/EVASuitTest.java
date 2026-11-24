/*
 * Mars Simulation Project
 * EVASuitTest.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;

class EVASuitTest extends MarsSimUnitTest {

    @Test
    void testEVASuitMass() {
    	var s = buildSettlement("Test");

        var p = buildPerson("Fixed", s);
        var suit00 = EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
        var suit01 = EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
        int id = EquipmentType.convertName2ID(EVASuit.TYPE);
        EquipmentType suitType = EquipmentType.convertID2Type(id);
        
        assertEquals(EquipmentType.EVA_SUIT, suitType, "EVA Suit equipment type");
        
        EVASuit suit02 = EVASuitUtil.findRegisteredOrGoodEVASuit(p);

        boolean same = suit00.equals(suit02) || suit01.equals(suit02);
        		
        assertTrue(same, "Found a good EVA Suit in the settlement. The person has not registered with it yet.");

        boolean success = suit01.transfer(p);
		if (success) {
			// Set the person as the owner
			suit01.setRegisteredOwner(p);
		}
        
		EVASuit suit03 = p.getSuit();
 
        assertEquals(suit03, suit01, "Found the EVA Suit that the person has registered with.");
        
        double usualMass = EVASuit.getUsualMass();
        assertNotEquals(0D, usualMass, "Usual mass should not be zero.");
        
        // Note: for now, do not use base mass
		double baseMass = Math.round(suit00.getBaseMass() * 1000D)/1000D;

		System.out.println("base mass: " + baseMass);
        assertEquals(baseMass, usualMass, "The base mass equals EVA suit's usual mass");
        
        double equipmentUsualMass = EquipmentFactory.getUsualMass(EquipmentType.EVA_SUIT);
        
        assertEquals(usualMass, equipmentUsualMass, "EVA suit's usual mass equals equipment usual mass.");
    }
}
