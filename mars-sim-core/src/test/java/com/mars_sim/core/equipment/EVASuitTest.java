/*
 * Mars Simulation Project
 * EVASuitTest.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;

class EVASuitTest extends MarsSimUnitTest {

    @Test
    void testEVASuitMass() {
    	var s = buildSettlement("Test");
//        var a = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
//        var r = buildRobot("Bunny Bear", s, RobotType.GARDENBOT, a, null);
        var p = buildPerson("Fixed", s);
        var suit00 = EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
        var suit01 = EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
        int id = EquipmentType.convertName2ID(EVASuit.TYPE);
        EquipmentType suitType = EquipmentType.convertID2Type(id);
        
        assertEquals(suitType, EquipmentType.EVA_SUIT, "EVA Suit equipment type");
        
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
        
        double mass0 = EVASuit.getEmptyMass();
        
        assertEquals(suit00.getBaseMass(), mass0, "The base mass0 of a EVA Suit");
        
        double mass1 = EquipmentFactory.getEquipmentMass(EquipmentType.EVA_SUIT);
        
        assertEquals(suit00.getBaseMass(), mass1, "The base mass1 of a EVA Suit");
    }
}
