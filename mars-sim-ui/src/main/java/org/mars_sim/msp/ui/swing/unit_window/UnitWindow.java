/**
 * Mars Simulation Project
 * UnitWindow.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitWindow is the base window for displaying units.
 */
public abstract class UnitWindow extends JInternalFrame {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** Main window. */
	protected MainDesktopPane desktop;
	/** Unit for this window. */
	protected Unit unit;
	protected JPanel namePanel;
	/** The tab panels. */
	private Collection<TabPanel> tabPanels;
	/** The center panel. */
	private JTabbedPane centerPanel;

	//private Color THEME_COLOR = Color.ORANGE;
	
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param unit the unit for this window.
     * @param displayDescription true if unit description is to be displayed.
     */
    public UnitWindow(MainDesktopPane desktop, Unit unit, boolean displayDescription) {
        
        // Use JInternalFrame constructor
        super(unit.getName(), true, true, false, true);

        // Initialize data members
        this.desktop = desktop;
        this.unit = unit;
        tabPanels = new ArrayList<TabPanel>();

        
        // Create main panel
        JPanel mainPane = new JPanel(new BorderLayout());
        // mainPane.setBorder(MainDesktopPane.newEmptyBorder());
        setContentPane(mainPane);
        //getContentPane().setBackground(THEME_COLOR);

        
        // Create name panel
        namePanel = new JPanel(new BorderLayout(0, 0));
        //namePanel.setBackground(THEME_COLOR);
        //namePanel.setBorder(new MarsPanelBorder());
        mainPane.add(namePanel, BorderLayout.NORTH);
        //mainPane.setBackground(THEME_COLOR);

        // Create name label
        UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
        JLabel nameLabel = new JLabel(" " + unit.getName() + " ", displayInfo.getButtonIcon(), SwingConstants.CENTER);
        nameLabel.setOpaque(true);
        nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
        nameLabel.setHorizontalTextPosition(JLabel.CENTER);
        nameLabel.setBorder(new EmptyBorder(5, 5, 5, 5) );
        namePanel.setBorder(new MarsPanelBorder());
        //namePanel.add(nameLabel, BorderLayout.EAST);
        //namePanel.setBorder(new EmptyBorder(5, 5, 5, 5) );
        namePanel.add(nameLabel, BorderLayout.WEST);
        
        // Create description label if necessary.
        if (displayDescription) {
            JLabel descriptionLabel = new JLabel(unit.getDescription(), JLabel.CENTER);
            namePanel.add(descriptionLabel, BorderLayout.SOUTH);
        }
        
        // Create center panel
        centerPanel = new JTabbedPane();
        mainPane.add(namePanel, BorderLayout.NORTH);
        //centerPanel.setBackground(THEME_COLOR);
        mainPane.add(centerPanel, BorderLayout.CENTER);
        // add focusListener to play sounds and alert users of critical conditions.
       
        //TODO: disabled in SVN while in development
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

    protected final void addTopPanel(TabPanel panel) {
        if (!tabPanels.contains(panel)) {
            tabPanels.add(panel);
            namePanel.add(panel,BorderLayout.CENTER);
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
        for (TabPanel tabPanel : tabPanels) {
        	tabPanel.update();
        }
    }
    
    /**
     * Prepares unit window for deletion.
     */
    public void destroy() {}
}