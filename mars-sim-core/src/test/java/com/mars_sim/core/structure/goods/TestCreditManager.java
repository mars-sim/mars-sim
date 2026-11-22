package com.mars_sim.core.structure.goods;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;

class TestCreditManager {

    @BeforeEach
    void setUp(){
        SimulationConfig.loadConfig();
        Simulation.createTestRun();
    }

	@Test
	void testSetCredit() {
	    var authorities = SimulationConfig.instance().getReportingAuthorityFactory()
	                    .getKnownItems().iterator();
	    Authority authority1 = authorities.next();
	    Authority authority2 = authorities.next();

	    Settlement settlement1 = new MockSettlement("S1", false,
	                    MockSettlement.DEFAULT_COORDINATES, authority1);
	    Settlement settlement2 = new MockSettlement("S2", false,
	                    MockSettlement.DEFAULT_COORDINATES, authority2);

	    settlement1.getCreditManager().setCredit(authority2, 100D);
	    assertEquals(100D, settlement1.getCreditManager().getCredit(authority2));

	    settlement1.getCreditManager().setCredit(authority2, -100D);
	    assertEquals(-100D, settlement1.getCreditManager().getCredit(authority2));

	    settlement2.getCreditManager().setCredit(authority1, 100D);
	    assertEquals(-100D, settlement1.getCreditManager().getCredit(authority2));
	}
}