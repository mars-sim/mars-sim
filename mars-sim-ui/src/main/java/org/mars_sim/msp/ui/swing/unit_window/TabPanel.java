/*
 * Mars Simulation Project
 * TabPanel.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;

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

		// Initialize data members
		this.tabTitle = tabTitle;
		this.description = description;
		this.tabIcon = tabIcon;
		this.tabToolTip = tabToolTip;
		this.desktop = desktop;
		
		// Create the view panel
		JPanel viewPanel = new JPanel(new BorderLayout(0, 0));
		createViewport();
		setViewportView(viewPanel);
		createVerticalScrollBar();
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);

		// Ideally yes
		//setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);


		JScrollBar vertical = getVerticalScrollBar();
		vertical.setValue(0);
		
		// Create top content panel
		topContentPanel = new JPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		topContentPanel.setBorder(MainDesktopPane.newEmptyBorder());
		viewPanel.add(topContentPanel, BorderLayout.NORTH);

		Border margin = new EmptyBorder(5,5,5,5);
		
		// Create center content panel
		centerContentPanel = new JPanel(new BorderLayout(0, 10));
		centerContentPanel.setBorder(margin);
		viewPanel.add(centerContentPanel, BorderLayout.CENTER);
	}

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
	protected void buildUI(JPanel centerContentPanel) {
		throw new UnsupportedOperationException("Build UI not implemented yet");
	}

	/**
	 * Adds a standard titled border.
	 * 
	 * @param panel
	 * @param title The title to display
	 */
	protected void addBorder(JComponent panel, String title) {
		panel.setBorder(StyleManager.createLabelBorder(title));
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

}
