/*
 * Mars Simulation Project
 * TestManufactureProcessInfo
 * @date 2023-05-13
 * @author Barry Evans
 */

package org.mars_sim.msp.core.manufacture;

import java.util.List;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;

public class TestManufactureProcessInfo extends AbstractMarsSimUnitTest {


	private static final String ASSEMBLE_EVA_SUIT = "Assemble EVA suit";
	private static final String EVA_SUIT = "EVA suit";
	
	private ManufactureProcessInfo mInfo;
	
    @Override
    public void setUp() {
		super.setUp();
		
		ManufactureConfig config = simConfig.getManufactureConfiguration();

		 for (ManufactureProcessInfo info : config.getManufactureProcessList()) {
	        	if (info.getName().equals(ASSEMBLE_EVA_SUIT)) {
	        		mInfo = info;
	        	}
	        }
    }
    
    public void testEVASuitParts() {
    	
    	List<String> inputList = mInfo.getInputNames();
//		System.out.println(inputList);
		assertEquals(14, inputList.size());
    }
    
    public void testEVASuitProcess() {
    	   
		List<ManufactureProcessInfo> list = ManufactureUtil.getManufactureProcessesWithGivenOutput(EVA_SUIT);
		assertEquals(ASSEMBLE_EVA_SUIT, list.get(0).getName());
//		System.out.println(list);
    }
}