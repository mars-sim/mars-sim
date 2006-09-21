package org.mars_sim.msp.simulation.resource;

import java.util.Set;

import junit.framework.TestCase;

public class TestItemResource extends TestCase {

	public TestItemResource() {
		super();
	}
	
	public void testResourceMass() {
		double hammerMass = ItemResource.HAMMER.getMassPerItem();
		assertEquals("Hammer mass is correct.", 1.4D, hammerMass, 0D);
	}
	
	public void testResourceName() {
		String hammerName = ItemResource.HAMMER.getName();
		assertEquals("Hammer name is correct", "hammer", hammerName);
	}
	
	public void testFindItemResourcePositive() {
		try {
			ItemResource hammerResource = ItemResource.findItemResource("hammer");
			assertEquals("Hammer found in resource types.", ItemResource.HAMMER, hammerResource);
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
	
	public void testGetItemResourcesSize() {
		assertEquals("Correct number of item resource types.", 3, ItemResource.getItemResources().size());
	}
	
	public void testGetItemResourcesContents() {
		Set resources = ItemResource.getItemResources();
		assertTrue("Contains hammer.", (resources.contains(ItemResource.HAMMER)));
		assertTrue("Contains socket wrench.", (resources.contains(ItemResource.SOCKET_WRENCH)));
		assertTrue("Contains pipe wrench.", (resources.contains(ItemResource.PIPE_WRENCH)));
	}
}