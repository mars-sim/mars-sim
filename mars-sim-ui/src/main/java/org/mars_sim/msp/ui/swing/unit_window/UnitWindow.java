/*
 * Mars Simulation Project
 * UnitWindow.java
 * @date 2022-10-24
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.ui.swing.ConfigurableWindow;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;


/**
 * The UnitWindow is the base window for displaying units.
 */
@SuppressWarnings("serial")
public abstract class UnitWindow extends ModalInternalFrame
			implements ConfigurableWindow {

	public static final int WIDTH = 530;
	public static final int HEIGHT = 620;

	public static final int STATUS_HEIGHT = 60;
	private static final String UNIT_TYPE = "unittype";
	private static final String UNIT_NAME = "unitname";
	
	/** The tab panels. */
	private List<TabPanel> tabPanels;
	/** The center panel. */
	private JTabbedPane tabPane;

	/** The cache for the currently selected TabPanel. */
	private TabPanel oldTab;

	/** Main window. */
	protected MainDesktopPane desktop;
	/** Unit for this window. */
	protected Unit unit;

	/**
	 * Constructor
	 *
	 * @param desktop        the main desktop panel.
	 * @param unit           the unit for this window.
	 * @param hasDescription true if unit description is to be displayed.
	 */
	public UnitWindow(MainDesktopPane desktop, Unit unit, String title, boolean hasDescription) {
		// Use JInternalFrame constructor
		super(title, false, true, false, true);

		// Initialize data members
		this.desktop = desktop;
		this.unit = unit;

		setFrameIcon(MainWindow.getLanderIcon());

		Dimension windowSize;
		if (unit.getUnitType() == UnitType.PERSON 
				|| unit.getUnitType() == UnitType.SETTLEMENT) {
			
			windowSize = new Dimension(WIDTH, HEIGHT);
		}
		else { // for robot, equipment and vehicle
			windowSize = new Dimension(WIDTH, HEIGHT - STATUS_HEIGHT);
		}

		setMaximumSize(windowSize);
		setPreferredSize(windowSize);
		this.setIconifiable(false);

		initializeUI();
	}

	private void initializeUI() {

		tabPanels = new ArrayList<>();

		// Create main panel
		JPanel mainPane = new JPanel(new BorderLayout());
		setContentPane(mainPane);

		tabPane = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
		tabPane.setPreferredSize(new Dimension(WIDTH - 25, HEIGHT - 120));

		// Add a listener for the tab changes
		tabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				TabPanel newTab = getSelected();
				if (!newTab.isUIDone()) {
					if (oldTab == null || newTab != oldTab) {
						oldTab = newTab;
						newTab.initializeUI();
					}
				}
			}
		});


		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		centerPanel.add(tabPane);

		mainPane.add(centerPanel, BorderLayout.CENTER);
	}

	/**
	 * Sets the image on the label
	 *
	 * @param imageLocation
	 * @param label
	 */
	protected static void setImage(String imageLocation, JLabel label) {
		Icon imageIcon = ImageLoader.getIconByName(imageLocation);
		label.setIcon(imageIcon);
	}

	/**
	 * Adds a tab panel to the center panel.
	 *
	 * @param panel the tab panel to add.
	 */
	protected final void addTabPanel(TabPanel panel) {
		if (!tabPanels.contains(panel)) {
			tabPanels.add(panel);
		}
	}
	
	/**
	 * Adds this tab panel as the first tab panel.
	 *
	 * @param panel the tab panel to add.
	 */
	protected final void addFirstPanel(TabPanel panel) {
		if (!tabPanels.contains(panel)) {
			tabPanels.add(0, panel);
		}
	}

	/**
	 * Sorts tab panels.
	 */
	protected void sortTabPanels() {
		Collections.sort(tabPanels, (t1, t2) -> t1.getTabTitle().compareTo(t2.getTabTitle()));
	}
	
	/**
	 * Adds tab panels with icons.
	 */
	protected void addTabIconPanels() {
		tabPanels.forEach(panel -> {
			tabPane.addTab(null, panel.getTabIcon(), panel, panel.getTabToolTip());
		});
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
			if (tabPanel.isVisible() && tabPanel.isShowing() && tabPanel.isUIDone()) { 
				// Instead of using SwingUtilities.invokeLater, 
				// calling directly removes the change of ConcurrentMidifications
				tabPanel.update();
			}
		}
	}

	@Override
    public String getName() {
		if (unit != null && unit.getName() != null)
			return unit.getName() +"'s unit window";
		return null;
    }

	/**
	 * Return the currently selected tab.
	 *
	 * @return Monitor tab being displayed.
	 */
	public TabPanel getSelected() {
		// Not using SwingUtilities.updateComponentTreeUI(this)
		TabPanel selected = null;
		int selectedIdx = tabPane.getSelectedIndex();
		if ((selectedIdx != -1) && (selectedIdx < tabPanels.size()))
			selected = tabPanels.get(selectedIdx);
		return selected;
	}

	/**
	 * Prepares unit window for deletion.
	 */
	public void destroy() {		
		if (tabPanels != null)
			tabPanels.clear();
		tabPanels = null;
		tabPane = null;
		desktop = null;
		unit = null;
	}

	@Override
	public Properties getUIProps() {
		Properties result = new Properties();
		result.setProperty(UNIT_NAME, unit.getName());
		result.setProperty(UNIT_TYPE, unit.getUnitType().name());

		return result;
	}

	/**
	 * Find a Unit from a previously generated UI Settings instance.
	 * @see #getUIProps()
	 * @param uMgr
	 * @param settings
	 * @return
	 */
	public static Unit getUnit(UnitManager uMgr, Properties settings) {
		String type = settings.getProperty(UNIT_TYPE);
		String name = settings.getProperty(UNIT_NAME);

		if ((type != null) && (name != null)) {
			UnitType uType = UnitType.valueOf(type);
			return uMgr.getUnitByName(uType, name);
		}
		return null;
	}
}
