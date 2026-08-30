/*
 * Mars Simulation Project
 * DataRecorderTest.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.test.MarsSimUnitTest;

class DataRecorderTest extends MarsSimUnitTest {

    @Test
    void testDataRecorder() {
    	var s = buildSettlement("Test");

        var p = buildPerson("Recorder", s);
        var recorder00 = EquipmentFactory.createEquipment(EquipmentType.DATA_RECORDER, s);
        var recorder01 = EquipmentFactory.createEquipment(EquipmentType.DATA_RECORDER, s);
        int id = EquipmentType.convertName2ID(DataRecorder.TYPE);
        EquipmentType type = EquipmentType.convertID2Type(id);
        
        assertEquals(type, EquipmentType.DATA_RECORDER, "Data Recorder equipment type");

        boolean success = recorder01.transfer(p);
		if (success) {
			// Set the person as the owner
			recorder01.setRegisteredOwner(p);
		}
        
		Person pp = recorder01.getRegisteredOwner();
		
		assertEquals(pp, p, "Person registered with the data recorder successfully.");

        double mass1 = EquipmentFactory.getEquipmentMass(EquipmentType.DATA_RECORDER);
    
        assertEquals(recorder00.getBaseMass(), mass1, "The base mass1 of a data recorder");
    }
}
