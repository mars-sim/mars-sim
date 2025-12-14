package com.mars_sim.core.structure.goods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.goods.CreditManager;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;

public class TestCreditManager {

    @BeforeEach
    public void setUp() throws Exception {
        SimulationConfig.loadConfig();
        Simulation.instance().testRun();
    }

    @Test
    void testSetCredit() {
        Collection<Settlement> settlements = new ConcurrentLinkedQueue<Settlement>();
        Settlement settlement1 = new MockSettlement();
        settlements.add(settlement1);
        Settlement settlement2 = new MockSettlement();
        settlements.add(settlement2);
 
        // Need to manually initialize them
        settlement1.setCreditManager(new CreditManager(settlement1, settlements));
        settlement2.setCreditManager(new CreditManager(settlement2, settlements));
        
        // Sleeping the thread for a short time to allow the credit manager to finish loading.
        try {
            Thread.sleep(100L);
        }
        catch (InterruptedException e) {
            fail();
        }
        
        CreditManager.setCredit(settlement1, settlement2, 100D);
        assertEquals(100D, CreditManager.getCredit(settlement1, settlement2));

        CreditManager.setCredit(settlement1, settlement2, -100D);
        assertEquals(-100D, CreditManager.getCredit(settlement1, settlement2));

        CreditManager.setCredit(settlement2, settlement1, 100D);
        assertEquals(-100D, CreditManager.getCredit(settlement1, settlement2));
	}
}