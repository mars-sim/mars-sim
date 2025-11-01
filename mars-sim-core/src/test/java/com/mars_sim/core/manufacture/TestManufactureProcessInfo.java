/*
 * Mars Simulation Project
 * TestManufactureProcessInfo
 * @date 2023-05-13
 * @author Barry Evans
 */

package com.mars_sim.core.manufacture;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;

class TestManufactureProcessInfo {


	private static final String ASSEMBLE_EVA_SUIT = "Assemble EVA suit";
	private static final String EVA_SUIT = "EVA suit";
	
	private ManufactureProcessInfo mInfo;
	private ManufactureConfig config;
	
    @BeforeEach
    void setUp() {
		config = SimulationConfig.loadConfig().getManufactureConfiguration();
    }
    
    @Test
    void testEVASuitParts() {
    	for (ManufactureProcessInfo info : config.getManufactureProcessList()) {
	        if (info.getName().equals(ASSEMBLE_EVA_SUIT)) {
	        	mInfo = info;
	        }
		 }

    	var inputList = mInfo.getInputList();
		
		// [eva helmet, helmet visor, pressure suit, coveralls, Liquid Cooling Garment, 
		// eva gloves, eva boots, eva pads, eva backpack, eva antenna, 
		// eva radio, eva battery, suit heating unit, electrical wire, wire connector, 
		// biosensor]

		assertEquals(16, inputList.size());
    }
    
    @Test
    void testEVASuitProcess() {
    	   
		List<ManufactureProcessInfo> list = ManufactureUtil.getManufactureProcessesWithGivenOutput(EVA_SUIT);
		assertEquals(ASSEMBLE_EVA_SUIT, list.get(0).getName());
    }
}