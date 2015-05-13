package org.mars_sim.msp.core.networking;

public interface Networkable {

	void sendUpdate();

	void receiveUpdate();
}
