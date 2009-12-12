/**
 * Mars Simulation Project
 * UnitWindow.java
 * @version 2.81 2007-08-27
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.ui.swing.*;
import org.mars_sim.msp.ui.swing.unit_display_info.*;

/**
 * The UnitWindow is the base window for displaying units.
 */
public abstract class UnitWindow extends JInternalFrame {
    
    // Data members
    protected MainDesktopPane desktop; // Main window
    protected Unit unit;               // Unit for this window
    private Collection<TabPanel> tabPanels;      // The tab panels
    private JTabbedPane centerPanel;   // The center panel
    
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param unit the unit for this window.
     * @param displayDescription true if unit description is to be displayed.
     */
    public UnitWindow(MainDesktopPane desktop, Unit unit, boolean displayDescription) {
        
        // Use JInternalFrame constructor
        super(unit.getName(), false, true, false, true);

        // Initialize data members
        this.desktop = desktop;
        this.unit = unit;
        tabPanels = new ArrayList<TabPanel>();
        
        // Create main panel
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);
        
        // Create name panel
        JPanel namePanel = new JPanel(new BorderLayout(0, 0));
        mainPane.add(namePanel, BorderLayout.NORTH);

        // Create name label
        UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
        JLabel nameLabel = new JLabel(unit.getName(), displayInfo.getButtonIcon(), JLabel.CENTER);
        nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
        nameLabel.setHorizontalTextPosition(JLabel.CENTER);
        namePanel.add(nameLabel, BorderLayout.NORTH);
        
        // Create description label if necessary.
        if (displayDescription) {
            JLabel descriptionLabel = new JLabel(unit.getDescription(), JLabel.CENTER);
            namePanel.add(descriptionLabel, BorderLayout.SOUTH);
        }
        
        // Create center panel
        centerPanel = new JTabbedPane();
        mainPane.add(centerPanel, BorderLayout.CENTER);
        // add focusListener to play sounds and alert users of critical conditions.
       
        //TODO: disabled in CVS while in development
        //this.addInternalFrameListener(new UniversalUnitWindowListener(UnitInspector.getGlobalInstance()));
        
    }
    
    /**
     * Adds a tab panel to the center panel.
     *
     * @param panel the tab panel to add.
     */
    protected final void addTabPanel(TabPanel panel) {
        if (!tabPanels.contains(panel)) {
            tabPanels.add(panel);
            centerPanel.addTab(panel.getTabTitle(), panel.getTabIcon(), 
                panel, panel.getTabToolTip());
        }
    }
     
    /**
     * Gets the unit for this window.
     *
     * @return unit 
     */
    public Unit getUnit() {
        return unit;
    }
    
    /**
     * Updates this window.
     */
    public void update() {
        
        // Update each of the tab panels.
        Iterator<TabPanel> i = tabPanels.iterator();
        while (i.hasNext()) i.next().update();
    }
    
    /**
     * Prepares unit window for deletion.
     */
    public void destroy() {}
}