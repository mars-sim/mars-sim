/*
 * Mars Simulation Project
 * RobotWindow.java
 * @date 2025-08-11
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.robot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.core.robot.Robot;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.MaintenanceTabPanel;
import com.mars_sim.ui.swing.unit_window.MalfunctionTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitWindow;
import com.mars_sim.ui.swing.unit_window.person.TabPanelActivity;
import com.mars_sim.ui.swing.unit_window.person.TabPanelAttribute;
import com.mars_sim.ui.swing.unit_window.person.TabPanelSchedule;
import com.mars_sim.ui.swing.unit_window.person.TabPanelSkill;

/**
 * The RobotWindow is the window for displaying a robot.
 */
@SuppressWarnings("serial")
public class RobotUnitWindow extends UnitWindow {
	
	private static final String TOWN = "settlement";
	private static final String JOB = "career";
//	private static final String MADE = "made";
//	private static final String SHIFT = "shift";
	
	private static final String TWO_SPACES = "  ";
	private static final String SIX_SPACES = "      ";
	
	private static final Font font = StyleManager.getSmallLabelFont();

	private JLabel townLabel = new JLabel();
	private JLabel typeLabel = new JLabel();
//	private JLabel roleLabel;
//	private JLabel shiftLabel;

	private JPanel statusPanel;
	
	private Robot robot;

	/**
	 * Constructor.
	 * 
	 * @param desktop the main desktop panel.
	 * @param robot   the robot for this window.
	 */
	public RobotUnitWindow(MainDesktopPane desktop, Robot robot) {
		// Use UnitWindow constructor
		super(desktop, robot, robot.getName()
				+ ((robot.getAssociatedSettlement() != null) ? (" of " + robot.getAssociatedSettlement()) : "")
				+ " (" + (robot.getLocationStateType().getName()) + ")"
				, true);
		this.robot = robot;
		
		initTopPanel();
			
		initTabPanel(robot);
	}

	/**
	 * Initializes the top panel.
	 */
	private void initTopPanel() {
		
		// Create status panel
		statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		statusPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		getContentPane().add(statusPanel, BorderLayout.NORTH);	
		
		statusPanel.setPreferredSize(new Dimension(-1, UnitWindow.STATUS_HEIGHT));

		// Create name label
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		String name = SIX_SPACES + getShortenedName(unit.getName()) + SIX_SPACES;

		JLabel nameLabel = new JLabel(name, displayInfo.getButtonIcon(unit), SwingConstants.CENTER);
		nameLabel.setMinimumSize(new Dimension(120, UnitWindow.STATUS_HEIGHT));
		
		JPanel namePane = new JPanel(new BorderLayout(0, 0));
		namePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		namePane.setAlignmentY(Component.CENTER_ALIGNMENT);
		namePane.add(nameLabel, BorderLayout.CENTER);
		
		nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		nameLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		nameLabel.setFont(font);
		nameLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		nameLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		statusPanel.add(namePane, BorderLayout.WEST);

		JPanel gridPanel = new JPanel(new GridLayout(1, 2, 5, 1));		
		gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		gridPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		gridPanel.add(createTile(TOWN, "Hometown", townLabel));
		gridPanel.add(createTile(JOB, "type", typeLabel));
//		gridPanel.add(createTile(ROLE, "Role", roleLabel));
//		gridPanel.add(createTile(SHIFT, "Shift", shiftLabel));

		statusPanel.add(gridPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Creates a tile panel.
	 * 
	 * @param title
	 * @param tooltip
	 * @param label
	 * @return
	 */
	public JPanel createTile(String title, String tooltip, JLabel label) {
		JPanel tilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		JLabel iconLabel = new JLabel();
		iconLabel.setToolTipText(tooltip);
		setImage(title, iconLabel);

		label.setFont(font);
		
		tilePanel.add(iconLabel);
		tilePanel.add(label);
		tilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		return tilePanel;
	}
	
	/**
	 * Initializes the tab panels.
	 * 
	 * @param robot
	 */
	public void initTabPanel(Robot robot) {
		// Add tab panels
		addTabPanel(new TabPanelActivity(robot, desktop));
		addTabPanel(new TabPanelAttribute(robot, desktop));
		addTabPanel(new MaintenanceTabPanel(robot, desktop));
		addTabPanel(new MalfunctionTabPanel(robot, desktop));

		addTabPanel(new InventoryTabPanel(robot, desktop));
		addTabPanel(new LocationTabPanel(robot, desktop));
		addTabPanel(new NotesTabPanel(robot, desktop));
		addTabPanel(new TabPanelSchedule(robot, desktop));
		addTabPanel(new TabPanelSkill(robot, desktop));
		
		// Add tab sorting
		sortTabPanels();
		
		addFirstPanel(new TabPanelGeneralRobot(robot, desktop));
		
		// Add to tab panels. 
		addTabIconPanels();
	}

	/**
	 * Updates this window.
	 */
	@Override
	public void update() {
		super.update();

		String title = robot.getName()
				+ ((robot.getAssociatedSettlement() != null) ? (" of " + robot.getAssociatedSettlement()) : "")
				+ " (" + (robot.getLocationStateType().getName()) + ")";
		super.setTitle(title);
		
		townLabel.setText(TWO_SPACES + robot.getAssociatedSettlement().getName());
		typeLabel.setText(TWO_SPACES + robot.getRobotType().getName());
//		roleLabel.setText(TWO_SPACES + roleString);
//		shiftLabel.setText(TWO_SPACES + newShift);
	
	}
}
