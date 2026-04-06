/*
 * Mars Simulation Project
 * TransportableWindow.java
 * @date 2026-03-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.transport;

import java.util.Properties;

import com.mars_sim.core.Entity;
import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;

/**
 * Window for displaying information about a Transportable entity.
 * Handles both Resupply missions and Arriving Settlements.
 */
public class TransportableWindow extends EntityContentPanel<Transportable> {

    public TransportableWindow(Transportable entity, UIContext context, Properties props) {
        super(entity, context);

        // Create a different heading based on the type of transportable
        Entity parent = null;
        String typeName = null;
        if (entity instanceof Resupply r) {
            parent = r.getSettlement();
            typeName = "Resupply";
        } else if (entity instanceof ArrivingSettlement as) {
            var authCode = as.getSponsorCode();
            parent = context.getSimulation().getConfig().getReportingAuthorityFactory().getItem(authCode);
            typeName = "Arriving Settlement";
        }

        if (parent != null) {
            setHeading(parent, "specs", "Type", typeName);
        }
        
        addDefaultTabPanel(new TabPanelGeneral(entity, context));
        addTabPanel(new TabPanelSupplies(entity, context));

        applyProps(props);
    }
}
