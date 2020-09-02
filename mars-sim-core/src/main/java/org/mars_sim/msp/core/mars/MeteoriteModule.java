/**
 * Mars Simulation Project
 * MeteoriteModule.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import com.google.inject.AbstractModule;

public class MeteoriteModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(MeteoriteImpact.class).to(MeteoriteImpactImpl.class);
	}
}
