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
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

@SuppressWarnings("serial")
public abstract class TabPanel extends JScrollPane {

	protected String tabTitle;
	protected String tabToolTip;
	
	protected Icon tabIcon;
	
	protected JPanel viewPanel;
	protected JPanel topContentPanel;
	protected JPanel centerContentPanel;
	
	protected Unit unit;
	protected MainDesktopPane desktop;

	protected static MissionManager missionManager = Simulation.instance().getMissionManager();

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

		if (unit instanceof Person) {
			this.setMaximumSize(new Dimension(UnitWindow.WIDTH - 30, UnitWindow.HEIGHT - 140));
			this.setPreferredSize(new Dimension(UnitWindow.WIDTH - 30, UnitWindow.HEIGHT - 140));
		}
		else {
			this.setMaximumSize(new Dimension(UnitWindow.WIDTH - 30, UnitWindow.HEIGHT - 90));
			this.setPreferredSize(new Dimension(UnitWindow.WIDTH - 30, UnitWindow.HEIGHT - 90));
		}
		
		// Create the view panel
		viewPanel = new JPanel(new BorderLayout(0, 0));
		createViewport();
		setViewportView(viewPanel);
		createVerticalScrollBar();
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);

		JScrollBar vertical = getVerticalScrollBar();
		vertical.setValue(0);//vertical.getMinimum());
		
		// Create top content panel
		topContentPanel = new JPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		topContentPanel.setBorder(MainDesktopPane.newEmptyBorder());
		viewPanel.add(topContentPanel, BorderLayout.NORTH);

//		Border border = new MarsPanelBorder();
		Border margin = new EmptyBorder(5,5,5,5);
		
		// Create center content panel
		centerContentPanel = new JPanel(new BorderLayout(0, 10));
//		centerContentPanel.setBorder(new CompoundBorder(border, margin));
		centerContentPanel.setBorder(margin);
		viewPanel.add(centerContentPanel, BorderLayout.CENTER);

		// setBorder(new DropShadowBorder(Color.BLACK, 0, 11, .2f, 16,false, true, true,
		// true));
	}

//	public abstract void initializeUI();
	
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
	
	public abstract void initializeUI();
	
	public abstract boolean isUIDone();
	
	@Override
	public  String toString() {
		return tabTitle;
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		tabIcon = null;
		viewPanel = null;
		topContentPanel = null;
		centerContentPanel = null;
		unit = null;
		desktop = null;
		missionManager = null;
		
	}
}