/*
 * Mars Simulation Project
 * UnitWindow.java
 * @date 2023-06-04
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MainWindow;
import com.mars_sim.ui.swing.ModalInternalFrame;
import com.mars_sim.ui.swing.tool.svg.SVGIcon;


/**
 * The UnitWindow is the base window for displaying units.
 */
@SuppressWarnings("serial")
public abstract class UnitWindow extends ModalInternalFrame
			implements ConfigurableWindow {

	private static final String AGENCY_FOLDER = "agency/";	
	private static final String UNIT_TYPE = "unittype";
	private static final String UNIT_NAME = "unitname";
	private static final String SELECTED_TAB = "selected_tab";

	public static final int WIDTH = 550;
	public static final int HEIGHT = 620;
	public static final int STATUS_HEIGHT = 55;
	
	private String unitTitle;
	
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
	 * Constructor.
	 *
	 * @param desktop        the main desktop panel.
	 * @param unit           the unit for this window.
	 * @param hasDescription true if unit description is to be displayed.
	 */
	protected UnitWindow(MainDesktopPane desktop, Unit unit, String title, boolean hasDescription) {
		// Use JInternalFrame constructor
		super(title, false, true, false, true);

		// Initialize data members
		this.desktop = desktop;
		this.unit = unit;
		this.unitTitle = title;
		
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

	/**
	 * Initializes the UI elements.
	 */
	private void initializeUI() {

		tabPanels = new ArrayList<>();

		// Create main panel
		JPanel mainPane = new JPanel(new BorderLayout());
		setContentPane(mainPane);

		tabPane = new JTabbedPane(SwingConstants.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
		tabPane.setPreferredSize(new Dimension(WIDTH - 25, HEIGHT - 120));

		// Add a listener for the tab changes
		tabPane.addChangeListener(e -> {
			TabPanel newTab = getSelected();
			if (!newTab.isUIDone() && (oldTab == null || newTab != oldTab)) {
				oldTab = newTab;
				newTab.initializeUI();
				setTitle(unitTitle + " - " + newTab.getTabTitle());
			}
		});


		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		centerPanel.add(tabPane);

		mainPane.add(centerPanel, BorderLayout.CENTER);
	}

	/**
	 * Sets the image on the label.
	 *
	 * @param imageLocation
	 * @param label
	 */
	protected static void setImage(String imageLocation, JLabel label) {
		Icon imageIcon = ImageLoader.getIconByName(imageLocation);
		label.setIcon(imageIcon);
	}

	/**
	 * Removes a tab panel to the center panel.
	 *
	 * @param panel the tab panel to remove.
	 */
	protected final void removeTabPanel(TabPanel panel) {
		if (tabPanels.contains(panel)) {
			tabPanels.remove(panel);
		}
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
		tabPanels.forEach(panel ->
			// Note: if adding panel.getTabTitle() as the first param, it would take up 
			// too much space on the left for displaying the name of each tab
			tabPane.addTab(null, panel.getTabIcon(), panel, panel.getTabToolTip())
		);
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
				// calling directly removes the change of ConcurrentModifications
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
	 * Returns the currently selected tab.
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

	@Override
	public Properties getUIProps() {
		Properties result = new Properties();
		result.setProperty(UNIT_NAME, unit.getName());
		result.setProperty(UNIT_TYPE, unit.getUnitType().name());
		result.setProperty(SELECTED_TAB, getSelected().getTabTitle());

		return result;
	}

	/**
	 * Opens the tab.
	 * 
	 * @param name
	 * @return
	 */
	public TabPanel openTab(String name) {
		for (TabPanel tb : tabPanels) {
			if (tb.getTabTitle().equalsIgnoreCase(name)) {
				tabPane.setSelectedComponent(tb);
				return tb;
			}
		}
		return null;
	}
	
	/**
	 * Applies the preciously saved UI props to a window.
	 * 
	 * @param props
	 */	
    public void setUIProps(Properties props) {
		String previousSelection = props.getProperty(SELECTED_TAB);
		if (previousSelection != null) {
			for (TabPanel tb : tabPanels) {
				if (tb.getTabTitle().equals(previousSelection)) {
					tabPane.setSelectedComponent(tb);
					break;
				}
			}
		}
    }

	/**
	 * Finds a Unit from a previously generated UI Settings instance.
	 * 
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
	
    /**
     * Creates and returns space agency label.
     * 
     * @return
     */
    public JLabel getAgencyLabel() {
		// Add space agency img
		String agencyStr = null;
		
		if (unit.getUnitType() == UnitType.SETTLEMENT) {
			agencyStr = ((Settlement)unit).getReportingAuthority().getName();
		}
		else
			agencyStr = unit.getAssociatedSettlement().getReportingAuthority().getName();
		
		Icon icon = ImageLoader.getIconByName(AGENCY_FOLDER + agencyStr);
		JLabel agencyLabel = null;
		
		if (icon instanceof SVGIcon) {
			agencyLabel = new JLabel(icon);
		}
		else {
			Image img = (ImageLoader.getImage(AGENCY_FOLDER + agencyStr))
					.getScaledInstance(UnitWindow.STATUS_HEIGHT - 5, 
						UnitWindow.STATUS_HEIGHT - 5, Image.SCALE_SMOOTH);
			agencyLabel = new JLabel(new ImageIcon(img));
		}
		
		agencyLabel.setPreferredSize(new Dimension(-1, UnitWindow.STATUS_HEIGHT - 5));
	
		return agencyLabel;
    }
    
	/**
	 * Create a shortened version of a Unit name.
	 * @param name
	 * @return
	 */
	protected static String getShortenedName(String name) {
		name = name.trim();
		int num = name.length();

		boolean hasSpace = name.matches("^\\s*$");

		if (hasSpace) {
			int space = name.indexOf(" ");

			String oldFirst = name.substring(0, space);
			String oldLast = name.substring(space + 1, num);
			String newFirst = oldFirst;
			String newLast = oldLast;
			String newName = name;

			if (num > 20) {

				if (oldFirst.length() > 10) {
					newFirst = oldFirst.substring(0, 10);
				} else if (oldLast.length() > 10) {
					newLast = oldLast.substring(0, 10);
				}
				newName = newFirst + " " + newLast;

			}

			return newName;
		}

		else
			return name;
	}
    
	/**
	 * Prepares unit window for deletion. Close all tabs
	 */
	public void destroy() {
		tabPanels.forEach(t -> t.destroy());	
		tabPanels.clear();
		tabPanels = null;
	}
}
