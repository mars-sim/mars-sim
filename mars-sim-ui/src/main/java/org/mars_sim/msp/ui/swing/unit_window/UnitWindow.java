/*
 * Mars Simulation Project
 * UnitWindow.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;


/**
 * The UnitWindow is the base window for displaying units.
 */
@SuppressWarnings("serial")
public abstract class UnitWindow extends ModalInternalFrame implements ChangeListener {

	public static final int WIDTH = 530;
	public static final int HEIGHT = 620;

	// private BufferedImage image;
	public static final String USER = Msg.getString("icon.user");
	private static final String TOWN = Msg.getString("icon.town");
	private static final String JOB = Msg.getString("icon.job");
	private static final String ROLE = Msg.getString("icon.role");
	private static final String SHIFT = Msg.getString("icon.shift");

	private static final String ONE_SPACE = " ";
	private static final String TWO_SPACES = "  ";
	private static final String SHIFT_FROM = " Shift :  (From ";
	private static final String TO = " to ";
	private static final String MILLISOLS = " millisols)";
	private static final String SHIFT_ANYTIME = " Shift :  Anytime";
	private static final String ONE_SPACE_SHIFT = " Shift";

	// Data members

	private boolean hasDescription;

	private String oldRoleString = "";
	private String oldJobString = "";
	private String oldTownString = "";

	private ShiftType oldShiftType = null;

	private WebLabel townLabel;
	private WebLabel jobLabel;
	private WebLabel roleLabel;
	private WebLabel shiftLabel;

	private WebPanel statusPanel;
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
	public UnitWindow(MainDesktopPane desktop, Unit unit, boolean hasDescription) {
		// Use JInternalFrame constructor
		super(unit.getName(), false, true, false, true);

		// Initialize data members
		this.desktop = desktop;
		this.unit = unit;
		this.hasDescription = hasDescription;

		setFrameIcon(MainWindow.getLanderIcon());

		if (unit.getUnitType() == UnitType.PERSON) {
			setMaximumSize(new Dimension(WIDTH, HEIGHT));
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
		}
		else if (unit.getUnitType() == UnitType.SETTLEMENT) {
			setMaximumSize(new Dimension(WIDTH, HEIGHT + 30));
			setPreferredSize(new Dimension(WIDTH, HEIGHT + 30));
		}
		else { //if (unit instanceof Vehicle) {
			setMaximumSize(new Dimension(WIDTH, HEIGHT - 50));
			setPreferredSize(new Dimension(WIDTH, HEIGHT - 50));
		}

		this.setIconifiable(false);

		initializeUI();
	}

	private void initializeUI() {

		tabPanels = new ArrayList<>();

		// Create main panel
		WebPanel mainPane = new WebPanel(new BorderLayout());
//		mainPane.setBorder(new MarsPanelBorder());// setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create status panel
		statusPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));

		mainPane.add(statusPanel, BorderLayout.NORTH);

		// Create name label
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		String name = ONE_SPACE + Conversion.capitalize(unit.getShortenedName()) + ONE_SPACE;

		if (unit.getUnitType() == UnitType.PERSON) {
			statusPanel.setPreferredSize(new Dimension(WIDTH / 8, 60));

			WebLabel nameLabel = new WebLabel(name, displayInfo.getButtonIcon(unit), SwingConstants.CENTER);
			nameLabel.setMinimumSize(new Dimension(80, 60));

			Font font = null;

			if (MainWindow.OS.contains("linux")) {
				new Font("DIALOG", Font.BOLD, 8);
			} else {
				new Font("DIALOG", Font.BOLD, 10);
			}
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);// .CENTER_ALIGNMENT);
			nameLabel.setAlignmentY(Component.TOP_ALIGNMENT);
			nameLabel.setFont(font);
			nameLabel.setVerticalTextPosition(WebLabel.BOTTOM);
			nameLabel.setHorizontalTextPosition(WebLabel.CENTER);

			statusPanel.add(nameLabel);

			// Create description label if necessary.
			if (hasDescription) {

				WebLabel townIconLabel = new WebLabel();
				TooltipManager.setTooltip(townIconLabel, "Hometown", TooltipWay.down);
				setImage(TOWN, townIconLabel);

				WebLabel jobIconLabel = new WebLabel();
				TooltipManager.setTooltip(jobIconLabel, "Job", TooltipWay.down);
				setImage(JOB, jobIconLabel);

				WebLabel roleIconLabel = new WebLabel();
				TooltipManager.setTooltip(roleIconLabel, "Role", TooltipWay.down);
				setImage(ROLE, roleIconLabel);

				WebLabel shiftIconLabel = new WebLabel();
				TooltipManager.setTooltip(shiftIconLabel, "Work Shift", TooltipWay.down);
				setImage(SHIFT, shiftIconLabel);

				WebPanel townPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				WebPanel jobPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				WebPanel rolePanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				WebPanel shiftPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

				townLabel = new WebLabel();
				townLabel.setFont(font);

				jobLabel = new WebLabel();
				jobLabel.setFont(font);

				roleLabel = new WebLabel();
				roleLabel.setFont(font);

				shiftLabel = new WebLabel();
				shiftLabel.setFont(font);

				statusUpdate((Person)unit);

				townPanel.add(townIconLabel);
				townPanel.add(townLabel);
				townPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

				jobPanel.add(jobIconLabel);
				jobPanel.add(jobLabel);
				jobPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

				rolePanel.add(roleIconLabel);
				rolePanel.add(roleLabel);
				rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

				shiftPanel.add(shiftIconLabel);
				shiftPanel.add(shiftLabel);
				shiftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

				WebPanel rowPanel = new WebPanel(new GridLayout(2, 2, 0, 0));
				rowPanel.add(townPanel);
				rowPanel.add(rolePanel);
				rowPanel.add(shiftPanel);
				rowPanel.add(jobPanel);

				statusPanel.add(rowPanel);
				rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
			}
		}


		tabPane = new WebTabbedPane(WebTabbedPane.LEFT, WebTabbedPane.SCROLL_TAB_LAYOUT); // WRAP_TAB_LAYOUT);//
		if (unit.getUnitType() == UnitType.PERSON) {
			tabPane.setPreferredSize(new Dimension(WIDTH - 45, HEIGHT - 120));
		}
		else if (unit.getUnitType() == UnitType.SETTLEMENT) {
			tabPane.setPreferredSize(new Dimension(WIDTH - 45, HEIGHT - 40));
		}
		else  { //if (unit instanceof Vehicle) {
			tabPane.setPreferredSize(new Dimension(WIDTH - 45, HEIGHT - 120));
		}

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


		WebPanel centerPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
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
	public void setImage(String imageLocation, WebLabel label) {
		ImageIcon imageIcon = ImageLoader.getNewIcon(imageLocation);
		label.setIcon(imageIcon);
	}

	/*
	 * Updates the status of an unit
	 */
	public void statusUpdate(Person p) {

		String townString = null;

		if (p.getPhysicalCondition().isDead()) {
			if (p.getAssociatedSettlement() != null)
				townString = Conversion.capitalize(p.getAssociatedSettlement().getName());
			else if (p.getBuriedSettlement() != null)
				townString = Conversion.capitalize(p.getBuriedSettlement().getName());
			else if (p.getPhysicalCondition().getDeathDetails().getPlaceOfDeath() != null)
				townString = Conversion.capitalize(p.getPhysicalCondition().getDeathDetails().getPlaceOfDeath());
		}

		else if (p.getAssociatedSettlement() != null)
			townString = Conversion.capitalize(p.getAssociatedSettlement().getName());

		if (!oldTownString.equals(townString)) {
			oldJobString = townString;
			if (townString.length() > 40)
				townString = townString.substring(0, 40);
			townLabel.setText(TWO_SPACES + townString);
		}

		String jobString = p.getMind().getJob().getName();
		if (!oldJobString.equals(jobString)) {
			oldJobString = jobString;
			jobLabel.setText(TWO_SPACES + jobString);
		}

		String roleString = p.getRole().getType().getName();
		if (!oldRoleString.equals(roleString)) {
			oldRoleString = roleString;
			roleLabel.setText(TWO_SPACES + roleString);
		}

		ShiftType newShiftType = p.getTaskSchedule().getShiftType();
		if (oldShiftType != newShiftType) {
			oldShiftType = newShiftType;
			shiftLabel.setText(TWO_SPACES + newShiftType.getName());
			TooltipManager.setTooltip(shiftLabel, newShiftType.getName() + getTimePeriod(newShiftType),
					TooltipWay.down);
		}
	}

	/**
	 * Gets the time period string
	 *
	 * @param shiftType
	 * @return
	 */
	public String getTimePeriod(ShiftType shiftType) {
		String time = null;
		if (shiftType == ShiftType.A)
			time = SHIFT_FROM + TaskSchedule.A_START + TO + TaskSchedule.A_END + MILLISOLS;
		else if (shiftType == ShiftType.B)
			time = SHIFT_FROM + TaskSchedule.B_START + TO + TaskSchedule.B_END + MILLISOLS;
		else if (shiftType == ShiftType.X)
			time = SHIFT_FROM + TaskSchedule.X_START + TO + TaskSchedule.Y_END + MILLISOLS;
		else if (shiftType == ShiftType.Y)
			time = SHIFT_FROM + TaskSchedule.Y_START + TO + TaskSchedule.Y_END + MILLISOLS;
		else if (shiftType == ShiftType.Z)
			time = SHIFT_FROM + TaskSchedule.Z_START + TO + TaskSchedule.Z_END + MILLISOLS;
		else if (shiftType == ShiftType.ON_CALL)
			time = SHIFT_ANYTIME;
		else
			time = ONE_SPACE_SHIFT;
		return time;
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
	 * Adds a tab panel to the center panel.
	 *
	 * @param panel the tab panel to add.
	 */
	protected final void addTopPanel(TabPanel panel) {
		if (!tabPanels.contains(panel)) {
			tabPanels.add(panel);
			statusPanel.add(panel, BorderLayout.CENTER);
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
	 * Adds tab panels.
	 */
	protected void addTabPanels() {
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

		if (unit.getUnitType() == UnitType.PERSON) {
			statusUpdate((Person)unit);
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
		statusPanel = null;
		if (tabPanels != null)
			tabPanels.clear();
		tabPanels = null;
		tabPane = null;
		oldShiftType = null;
		townLabel = null;
		jobLabel = null;
		roleLabel = null;
		shiftLabel = null;
		desktop = null;
		unit = null;
	}
}
