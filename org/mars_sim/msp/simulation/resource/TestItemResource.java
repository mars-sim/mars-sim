package org.mars_sim.msp.simulation.resource;

import java.util.Set;

import junit.framework.TestCase;

public class TestItemResource extends TestCase {

	public TestItemResource() {
		super();
	}
	
	public void testResourceMass() {
		double hammerMass = ItemResource.getTestResourceHammer().getMassPerItem();
		assertEquals("Hammer mass is correct.", 1.4D, hammerMass, 0D);
	}
	
	public void testResourceName() {
		String hammerName = ItemResource.getTestResourceHammer().getName();
		assertEquals("Hammer name is correct", "hammer", hammerName);
	}
	
	public void testFindItemResourcePositive() {
		try {
			ItemResource hammer = ItemResource.getTestResourceHammer();
			ItemResource hammerResource = ItemResource.findItemResource("hammer");
			assertEquals("Hammer found in resource types.", hammer, hammerResource);
		}
		catch (Exception e) {
			fail("Hammer found in resource types.");
		}
	}
	
	public void testFindItemResourceNegative() {
		try {
			ItemResource.findItemResource("test");
			fail("Throws exception if unknown item resource name.");
		}
		catch (Exception e) {}
	}
	
	public void testGetItemResourcesContents() {
		ItemResource hammer = ItemResource.getTestResourceHammer();
		ItemResource socketWrench = ItemResource.getTestResourceSocketWrench();
		ItemResource pipeWrench = ItemResource.getTestResourcePipeWrench();
		Set resources = ItemResource.getItemResources();
		assertTrue("Contains hammer.", (resources.contains(hammer)));
		assertTrue("Contains socket wrench.", (resources.contains(socketWrench)));
		assertTrue("Contains pipe wrench.", (resources.contains(pipeWrench)));
	}
}