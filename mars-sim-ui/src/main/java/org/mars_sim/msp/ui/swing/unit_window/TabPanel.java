/**
 * Mars Simulation Project
 * TabPanel.java
 * @version 3.1.0 2017-03-04
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

public abstract class TabPanel extends WebScrollPane {

	protected String tabTitle;
	protected Icon tabIcon;
	protected String tabToolTip;
	protected WebPanel viewPanel;
	protected WebPanel topContentPanel;
	protected WebPanel centerContentPanel;
	protected Unit unit;
	protected MainDesktopPane desktop;

	/**
	 * Constructor
	 *
	 * @param tabTitle   the title to be displayed in the tab (may be null).
	 * @param tabIcon    the icon to be displayed in the tab (may be null).
	 * @param tabToolTip the tool tip to be displayed in the icon (may be null).
	 * @param unit       the unit to display.
	 * @param desktop    the main desktop.
	 */
	public TabPanel(String tabTitle, Icon tabIcon, String tabToolTip, Unit unit, MainDesktopPane desktop) {

		// Use JScrollPane constructor
		super();

		// Initialize data members
		this.tabTitle = tabTitle;
		this.tabIcon = tabIcon;
		this.tabToolTip = tabToolTip;
		this.unit = unit;
		this.desktop = desktop;

		this.setMaximumSize(new Dimension(UnitWindow.WIDTH - 20, UnitWindow.HEIGHT));
		this.setPreferredSize(new Dimension(UnitWindow.WIDTH - 20, UnitWindow.HEIGHT));
		
		// Create the view panel
		viewPanel = new WebPanel(new BorderLayout(0, 0));
		createViewport();
		setViewportView(viewPanel);
		createVerticalScrollBar();
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);

		// Create top content panel
		topContentPanel = new WebPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		topContentPanel.setBorder(MainDesktopPane.newEmptyBorder());
		viewPanel.add(topContentPanel, BorderLayout.NORTH);

		Border border = new MarsPanelBorder();
		Border margin = new EmptyBorder(5,5,5,5);
		
		// Create center content panel
		centerContentPanel = new WebPanel(new BorderLayout(0, 0));
		centerContentPanel.setBorder(new CompoundBorder(border, margin));
		viewPanel.add(centerContentPanel, BorderLayout.CENTER);

		// setBorder(new DropShadowBorder(Color.BLACK, 0, 11, .2f, 16,false, true, true,
		// true));
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
	public abstract void update();

	/**
	 * Updates the info on this panel.
	 */
	public void updateUI() {
		super.updateUI();
	};
	
	/**
	 * Gets the main desktop.
	 * 
	 * @return desktop.
	 */
	public MainDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * Gets the unit.
	 * 
	 * @return unit.
	 */
	public Unit getUnit() {
		return unit;
	}
}