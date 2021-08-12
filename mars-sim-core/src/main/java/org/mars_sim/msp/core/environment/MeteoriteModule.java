/**
 * Mars Simulation Project
 * MeteoriteModule.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.environment;

import com.google.inject.AbstractModule;

public class MeteoriteModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(MeteoriteImpact.class).to(MeteoriteImpactImpl.class);
	}
}
