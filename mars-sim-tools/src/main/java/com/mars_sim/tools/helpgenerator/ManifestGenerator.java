/*
 * Mars Simulation Project
 * ManifestGenerator.java
 * @date 2024-12-01
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.interplanetary.transport.resupply.ResupplyManifest;

/**
 * Generates content for a ReSupplyManifest
 */
public class ManifestGenerator extends TypeGenerator<ResupplyManifest> {
    public static final String TYPE_NAME = "manifest";

    protected ManifestGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Resupply Manifest",
                "Resupply Manifests that are used to resupply a Settlement templates");
    }

    @Override
    protected List<ResupplyManifest> getEntities() {
        return getParent().getConfig().getSettlementTemplateConfiguration().getSupplyManifests();

    }
	/**
	 * Add properties for the initial suppliers for this Settlement
	 * @param v Template being rendered.
     * @param scope Properties
	 * @throws IOException
	 */
    @Override
    protected void addEntityProperties(ResupplyManifest st, Map<String,Object> scope) {
        SettlementGenerator.addSupplies(getParent(), st.getSupplies(), scope);
    }

    @Override
    protected String getEntityName(ResupplyManifest v) {
        return v.getName();
    }
}
