package com.mars_sim.core.structure.goods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.goods.CreditEvent;
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
	    Settlement settlement3 = new MockSettlement("S3", false,
	                    MockSettlement.DEFAULT_COORDINATES, authority1);

	    authority1.getCreditManager().setCredit(authority2, 100D);
	    assertEquals(100D, authority1.getCreditManager().getCredit(authority2));
	    assertEquals(100D, authority1.getCreditManager().getCreditMap().get(authority2));
	    assertEquals(authority1.getCreditManager(),
	                    settlement1.getReportingAuthority().getCreditManager());
	    assertEquals(authority1.getCreditManager(),
	                    settlement3.getReportingAuthority().getCreditManager());

	    authority1.getCreditManager().setCredit(authority2, -100D);
	    assertEquals(-100D, authority1.getCreditManager().getCredit(authority2));

	    authority2.getCreditManager().setCredit(authority1, 100D);
	    assertEquals(-100D, authority1.getCreditManager().getCredit(authority2));
	}

	@Test
	void testCreditEventAndSameAuthority() {
	    Authority owner = new Authority("OWNER", "Owner", false, false, 0.5, null, List.of());
	    Authority equalOwner = new Authority("OWNER", "Owner", false, false, 0.5, null, List.of());
	    Authority other = new Authority("OTHER", "Other", false, false, 0.5, null, List.of());
	    var manager = owner.getCreditManager();
	    List<CreditEvent> events = new ArrayList<>();
	    manager.addListener(events::add);

	    manager.setCredit(equalOwner, 10D);
	    assertTrue(manager.getCreditMap().isEmpty());
	    assertTrue(events.isEmpty());

	    manager.setCredit(other, 25D);
	    assertEquals(1, events.size());
	    CreditEvent event = events.get(0);
	    assertSame(owner, event.getAuthority1());
	    assertSame(owner, event.getSource());
	    assertSame(other, event.getAuthority2());
	    assertEquals(25D, event.getCredit());
	}

	@Test
	void testSerializedCreditManagerRetainsOwner() throws Exception {
	    Authority owner = new Authority("OWNER", "Owner", false, false, 0.5, null, List.of());
	    Authority other = new Authority("OTHER", "Other", false, false, 0.5, null, List.of());
	    owner.getCreditManager().setCredit(other, 10D);
	    var bytes = new ByteArrayOutputStream();
	    try (var output = new ObjectOutputStream(bytes)) {
	        output.writeObject(owner);
	    }

	    Authority restored;
	    try (var input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
	        restored = (Authority) input.readObject();
	    }
	    List<CreditEvent> events = new ArrayList<>();
	    restored.getCreditManager().addListener(events::add);
	    assertEquals(10D, restored.getCreditManager().getCredit(other));
	    restored.getCreditManager().setCredit(restored, 20D);
	    assertTrue(events.isEmpty());
	    restored.getCreditManager().setCredit(other, 30D);
	    assertSame(restored, events.get(0).getAuthority1());
	}
}