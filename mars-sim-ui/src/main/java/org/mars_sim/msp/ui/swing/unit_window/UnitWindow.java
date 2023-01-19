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
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;


/**
 * The UnitWindow is the base window for displaying units.
 */
@SuppressWarnings("serial")
public abstract class UnitWindow extends ModalInternalFrame implements ChangeListener {

	public static final int WIDTH = 530;
	public static final int HEIGHT = 620;

	public static final int STATUS_HEIGHT = 60;
	
	public static final String USER = Msg.getString("icon.user");

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

		if (unit.getUnitType() == UnitType.PERSON 
				|| unit.getUnitType() == UnitType.SETTLEMENT) {
			setMaximumSize(new Dimension(WIDTH, HEIGHT));
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
		}
		else { // for robot, equipment and vehicle
			setMaximumSize(new Dimension(WIDTH, HEIGHT - STATUS_HEIGHT));
			setPreferredSize(new Dimension(WIDTH, HEIGHT - STATUS_HEIGHT));
		}

		this.setIconifiable(false);

		initializeUI();
	}

	private void initializeUI() {

		tabPanels = new ArrayList<>();

		// Create main panel
		JPanel mainPane = new JPanel(new BorderLayout());
		setContentPane(mainPane);

		tabPane = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
		tabPane.setPreferredSize(new Dimension(WIDTH - 45, HEIGHT - 120));

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

		// Add focusListener to play sounds and alert users of critical conditions.
		// Disabled in SVN while in development
		// this.addInternalFrameListener(new
		// UniversalUnitWindowListener(UnitInspector.getGlobalInstance()));

		desktop.getMainWindow().initializeTheme();//initializeWeblaf();
	}

	/**
	 * Sets the image on the label
	 *
	 * @param imageLocation
	 * @param label
	 */
	public void setImage(String imageLocation, JLabel label) {
		ImageIcon imageIcon = ImageLoader.getNewIcon(imageLocation);
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
	 * Adds the death tab panel to the center panel.
	 *
	 * @param panel the death tab panel to add.
	 */
	protected final void addDeathPanel(TabPanel panel) {
		if (!tabPanels.contains(panel)) {
			tabPanels.add(0, panel);
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
		tabPanels = tabPanels.stream()
				.sorted((t1, t2) -> t1.getTabTitle().compareTo(t2.getTabTitle()))
				.collect(Collectors.toList());
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
	 * Adds tab panels with titles.
	 */
	protected void addTabTitlePanels() {
		tabPanels.forEach(panel -> {
			tabPane.addTab(panel.getTabTitle(), null, panel, panel.getTabToolTip());
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

	public void setTitle(String value) {
		super.setTitle(unit.getName());
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
}
