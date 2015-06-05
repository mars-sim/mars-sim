package org.mars_sim.msp.core.mars;

import com.google.inject.AbstractModule;

public class MeteoriteModule extends AbstractModule{
	@Override
	  protected void configure() {
	    bind(MeteoriteImpact.class).to(MeteoriteImpactImpl.class);
	  }
}
