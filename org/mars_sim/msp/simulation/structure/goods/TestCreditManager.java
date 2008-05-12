package org.mars_sim.msp.simulation.structure.goods;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.TestCase;

import org.mars_sim.msp.simulation.structure.MockSettlement;
import org.mars_sim.msp.simulation.structure.Settlement;

public class TestCreditManager extends TestCase {

	public void testSetCredit() {
		try {
			Collection<Settlement> settlements = new ConcurrentLinkedQueue<Settlement>();
			Settlement settlement1 = new MockSettlement();
			settlements.add(settlement1);
			Settlement settlement2 = new MockSettlement();
			settlements.add(settlement2);
			CreditManager manager = new CreditManager(settlements);
			
			manager.setCredit(settlement1, settlement2, 100D);
			assertEquals("credit amount is correct.", 100D, manager.getCredit(settlement1, settlement2));
			
			manager.setCredit(settlement1, settlement2, -100D);
			assertEquals("credit amount is correct.", -100D, manager.getCredit(settlement1, settlement2));
			
			manager.setCredit(settlement2, settlement1, 100D);
			assertEquals("credit amount is correct.", -100D, manager.getCredit(settlement1, settlement2));
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}
}