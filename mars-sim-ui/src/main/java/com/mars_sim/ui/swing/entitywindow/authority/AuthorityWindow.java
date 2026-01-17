/*
 * Mars Simulation Project
 * AuthorityWindow.java
 * @date 2025-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.authority;

import java.util.Properties;

import com.mars_sim.core.authority.Authority;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;

/**
 * The AuthorityWindow is the window for an authority entity.
 * It contains a number of tabs.
 */
@SuppressWarnings("serial")
public class AuthorityWindow extends EntityContentPanel<Authority> {

    public AuthorityWindow(Authority entity, UIContext context, Properties props) {
        super(entity, context);

        addDefaultTabPanel(new TabPanelGeneral(entity, context));
        addTabPanel(new TabPanelObjective(entity, context));
        addTabPanel(new TabPanelSettlements(entity, context));
        
        applyProps(props);
    }
}
