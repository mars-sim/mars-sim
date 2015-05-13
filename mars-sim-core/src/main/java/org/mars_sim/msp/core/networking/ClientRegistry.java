package org.mars_sim.msp.core.networking;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientRegistry {

	private static List<SettlementRegistry> settlementList;

	public ClientRegistry() {
		settlementList = new CopyOnWriteArrayList<>();
	}

	public static List<SettlementRegistry> getSettlementList() {
		return settlementList;
	}

	public static void clearSettlementList() {
		settlementList.clear();
	}
}
