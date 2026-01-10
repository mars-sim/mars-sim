/*
 * Mars Simulation Project
 * TabPanel.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.Unit;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.SwingHelper;

@SuppressWarnings("serial")
public abstract class TabPanel extends JScrollPane {

	// Default values for any top level Spring panel holding values
	protected static final int INITY_DEFAULT = 5;
	protected static final int INITX_DEFAULT = 75;
	protected static final int INITX_DEFAULT_1 = 135;
	protected static final int YPAD_DEFAULT = 1;
	protected static final int XPAD_DEFAULT = 5;

	private boolean isUIDone = false;
	
	private String description;
	private String tabTitle;
	private String tabToolTip;
	
	private transient Icon tabIcon;
	
	// These can be made private once all tabs converted
	private JPanel topContentPanel;
	private JPanel centerContentPanel;
	
	private Unit unit;
	private MainDesktopPane desktop;
	
	/**
	 * Constructor.
	 *
	 * @param tabTitle   the title to be displayed in the tab (may be null).
	 * @param tabIcon    the icon to be displayed in the tab (may be null).
	 * @param tabToolTip the tool tip to be displayed in the icon (may be null).
	 * @param unit       the unit to display.
	 * @param desktop    the main desktop.
	 */
	protected TabPanel(String tabTitle, Icon tabIcon, String tabToolTip, Unit unit, MainDesktopPane desktop) {
		this((tabTitle != null) ? tabTitle : tabToolTip, tabTitle, tabIcon, tabToolTip, desktop);

		this.unit = unit;
	}
	
	
	/**
	 * Constructor.
	 *
	 * @param tabTitle   the title to be displayed in the tab (may be null).
	 * @param tabIcon    the icon to be displayed in the tab (may be null).
	 * @param tabToolTip the tool tip to be displayed in the icon (may be null).
	 * @param desktop    the main desktop.
	 */
	protected TabPanel(String tabTitle, Icon tabIcon, String tabToolTip, MainDesktopPane desktop) {
		this((tabTitle != null) ? tabTitle : tabToolTip, tabTitle, tabIcon, tabToolTip, desktop);
	}

	/**
	 * Constructor.
	 *
	 * @param tabTitle   the title to be displayed in the tab (may be null).
	 * @param description A longer descriptive title displayed at the top of the panel.
	 * @param tabIcon    the icon to be displayed in the tab (may be null).
	 * @param tabToolTip the tool tip to be displayed in the icon (may be null).
	 * @param desktop    the main desktop.
	 */
	protected TabPanel(String tabTitle, String description, Icon tabIcon, String tabToolTip, MainDesktopPane desktop) {
		// Use JScrollPane constructor
		super();
		this.desktop = desktop;

		// Eventually tabTitle MUST be mandatory once all have been converted to UIContext
		if (tabTitle == null && tabToolTip == null) {
			throw new IllegalArgumentException("TabPanel must have either a title or a tool tip");
		}
		// Initialize data members
		this.tabTitle = (tabTitle != null) ? tabTitle : tabToolTip;
		this.description = description;
		this.tabIcon = tabIcon;
		this.tabToolTip = (tabToolTip != null) ? tabToolTip : tabTitle;
		
		// Create the view panel
		JPanel viewPanel = new JPanel();
		viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.Y_AXIS));
		createViewport();
		setViewportView(viewPanel);
		createVerticalScrollBar();
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);

		JScrollBar vertical = getVerticalScrollBar();
		vertical.setValue(0);
		
		// Create top content panel
		topContentPanel = new JPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		topContentPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		viewPanel.add(topContentPanel, BorderLayout.NORTH);

		Box.createVerticalGlue();
		
		// Create center content panel
		centerContentPanel = new JPanel(new BorderLayout(0, 10));
		centerContentPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		viewPanel.add(centerContentPanel, BorderLayout.CENTER);
	}

	/**
	 * Are all the UI element in place ?
	 * 
	 * @return
	 */
 	public boolean isUIDone() {
		return isUIDone;
	}
	
	public void initializeUI() {
		if (!isUIDone) {
			// Create label in top panel
			String topLabel = (description != null ? description : getTabTitle());
			JLabel titleLabel = new JLabel(topLabel, SwingConstants.CENTER);
			StyleManager.applyHeading(titleLabel);
			
			JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			labelPanel.add(titleLabel);
			topContentPanel.add(labelPanel);
			
			buildUI(centerContentPanel);
			
			isUIDone = true;
		}	
	}
	
	/**
	 * Builds the UI element using the 3 components.
	 * 
	 * @param centerContentPanel
	 */
	protected abstract void buildUI(JPanel centerContentPanel);

	/**
	 * Adds a standard titled border.
	 * 
	 * @param panel
	 * @param title The title to display
	 */
	protected void addBorder(JComponent panel, String title) {
		panel.setBorder(SwingHelper.createLabelBorder(title));
	}
	
	/**
	 * Adds a standard titled border.
	 * 
	 * @param panel
	 * @param title The title to display
	 */
	protected void addBorder(JComponent panel, String title, String tooltip) {
		panel.setBorder(SwingHelper.createLabelBorder(title));
		panel.setToolTipText(tooltip);
	}
	
	/**
	 * Gets the tab title.
	 *
	 * @return tab title or null.
	 */
	public String getTabTitle() {
		return tabTitle;
	}

	/**
	 * Gets the tab icon.
	 *
	 * @return tab icon or null.
	 */
	public Icon getTabIcon() {
		return tabIcon;
	}

	/**
	 * Gets the tab tool tip.
	 *
	 * @return tab tool tip.
	 */
	public String getTabToolTip() {
		return tabToolTip;
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		// No updated required
	}

	/**
	 * Gets the main desktop.
	 * 
	 * @return desktop.
	 */
	protected MainDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * Update the Unit.
	 * @param unit
	 */
	protected void setUnit(Unit unit) {
		this.unit = unit;
	}

	/**
	 * Gets the unit.
	 * 
	 * @return unit.
	 */
	protected Unit getUnit() {
		return unit;
	}
	
	/**
	 * Gets the simulation being monitored.
	 * 
	 * @return
	 */
	protected Simulation getSimulation() {
		return desktop.getSimulation();
	}
	
	@Override
	public  String toString() {
		return tabTitle;
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		tabIcon = null;
		topContentPanel = null;
		centerContentPanel = null;
		unit = null;
		desktop = null;
	}

	/**
	 * Refresh the UI elements of this tab. Commonly called when the tab is selected.
	 * Can be overridden by subclasses. but should be rarely needed.
	 */
    public void refreshUI() {
        // Default does nothing
    }
}
