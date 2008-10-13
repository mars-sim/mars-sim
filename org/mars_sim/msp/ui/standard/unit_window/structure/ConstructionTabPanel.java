/**
 * Mars Simulation Project
 * ConstructionTabPanel.java
 * @version 2.85 2008-10-11
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.construction.ConstructionManager;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

public class ConstructionTabPanel extends TabPanel {

    // Data members
    private ConstructionSitesPanel sitesPanel;
    private ConstructedBuildingsPanel buildingsPanel;
    
    /**
     * Constructor
     * @param unit the unit the tab panel is for.
     * @param desktop the desktop.
     */
    public ConstructionTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super("Const", null, "Construction", unit, desktop);
        
        Settlement settlement = (Settlement) unit;
        ConstructionManager manager = settlement.getConstructionManager();
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(titlePanel);
        
        JLabel constructionLabel = new JLabel("Construction", JLabel.CENTER);
        titlePanel.add(constructionLabel);
        
        JPanel mainContentPanel = new JPanel(new GridLayout(2, 1));
        centerContentPanel.add(mainContentPanel, BorderLayout.CENTER);
        
        sitesPanel = new ConstructionSitesPanel(manager);
        mainContentPanel.add(sitesPanel);
        
        buildingsPanel = new ConstructedBuildingsPanel(manager);
        mainContentPanel.add(buildingsPanel);
    }
    
    @Override
    public void update() {
        sitesPanel.update();
        buildingsPanel.update();
    }
}