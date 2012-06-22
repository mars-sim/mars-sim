/**
 * Mars Simulation Project
 * ConstructionTabPanel.java
 * @version 3.03 2012-06-22
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConstructionTabPanel extends TabPanel {

    // Data members
    private Settlement settlement;
    private ConstructionSitesPanel sitesPanel;
    private ConstructedBuildingsPanel buildingsPanel;
    private JCheckBox overrideCheckbox;
    
    /**
     * Constructor
     * @param unit the unit the tab panel is for.
     * @param desktop the desktop.
     */
    public ConstructionTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super("Const", null, "Construction", unit, desktop);
        
        settlement = (Settlement) unit;
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
        
        // Create bottom panel.
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Create override check box.
        overrideCheckbox = new JCheckBox("Override construction & salvage");
        overrideCheckbox.setToolTipText("Prevents settlement inhabitants from starting new " + 
                "construction/salvage building missions.");
        overrideCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setConstructionOverride(overrideCheckbox.isSelected());
            }
        });
        overrideCheckbox.setSelected(settlement.getConstructionOverride());
        bottomPanel.add(overrideCheckbox);
    }
    
    /**
     * Sets the settlement construction override.
     * @param constructionOverride true if construction/salvage building missions are overridden.
     */
    private void setConstructionOverride(boolean constructionOverride) {
       settlement.setConstructionOverride(constructionOverride);
    }
    
    @Override
    public void update() {
        sitesPanel.update();
        buildingsPanel.update();
        
        // Update construction override check box if necessary.
        if (settlement.getConstructionOverride() != overrideCheckbox.isSelected()) 
            overrideCheckbox.setSelected(settlement.getConstructionOverride());
    }
}