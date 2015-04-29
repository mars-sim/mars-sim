package org.mars_sim.msp.network;

public interface Networkable {

	void sendUpdate();

	void receiveUpdate();
}
