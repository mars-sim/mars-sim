/*
 * Mars Simulation Project
 * TabPanelSchedule.java
 * @date 2023-07-03
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.data.History;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager.OneActivity;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Shift;
import org.mars_sim.msp.core.structure.ShiftSlot;
import org.mars_sim.msp.core.structure.ShiftSlot.WorkStatus;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.JHistoryPanel;

/**
 * The TabPanelSchedule is a tab panel showing the daily schedule a person.
 */
@SuppressWarnings("serial")
public class TabPanelSchedule extends TabPanel {

	private static final String SCH_ICON = "schedule";

	private String shiftDescCache; 
	
	private JTextField shiftTF;
	private JLabel shiftLabel;
	
	private ShiftSlot shiftSlot;
	private TaskManager taskManager;

	private ActivityPanel activityPanel;

	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelSchedule(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(SCH_ICON),
			Msg.getString("TabPanelSchedule.title"), //$NON-NLS-1$
			unit, desktop
		);

		if (unit instanceof Person person) {
			shiftSlot = person.getShiftSlot();
			taskManager = person.getTaskManager();
		} 
		else if (unit instanceof Robot robot) {
			taskManager = robot.getTaskManager();
		}
	}

	@Override
	protected void buildUI(JPanel content) {

		// Create the shift panel.
		JPanel shiftPane = new JPanel(new FlowLayout(FlowLayout.LEFT));

		if (shiftSlot != null) {

			shiftLabel = new JLabel(Msg.getString("TabPanelSchedule.shift.label"), JLabel.CENTER); //$NON-NLS-1$
			shiftLabel.setToolTipText(Msg.getString("TabPanelSchedule.shift.toolTip")); //$NON-NLS-1$
			shiftPane.add(shiftLabel);

			shiftDescCache = getShiftDescription(shiftSlot);	
			
			shiftTF = new JTextField();
			shiftTF.setText(shiftDescCache);
			
			shiftTF.setEditable(false);
			shiftTF.setColumns(28);
			shiftTF.setHorizontalAlignment(JTextField.CENTER);
			
			shiftPane.add(shiftTF);
		}
		content.add(shiftPane, BorderLayout.NORTH);

		activityPanel = new ActivityPanel(taskManager.getAllActivities());
		activityPanel.setPreferredSize(new Dimension(225, 100));

		content.add(activityPanel, BorderLayout.CENTER);

		update();
	}

	/**
	 * Gets the shift description.
	 * 
	 * @param shift
	 * @return
	 */
	public static String getShiftDescription(ShiftSlot shift) {
		WorkStatus status = shift.getStatus();
		
		Shift s = shift.getShift();
		int start = s.getStart();
		int end = s.getEnd();
		String shiftName = s.getName();
		
		switch(status) {
			case ON_CALL:
				return "On Call";
			case ON_DUTY:
				return shiftName + " : On Duty ends @ " + end + " mols (" + start + " - " + end + ")";
			case OFF_DUTY:
				return shiftName + " : Off Duty starts @ " + start + " mols (" + start + " - " + end + ")";
			case ON_LEAVE:
				return shiftName + " : On Leave";
		}

		
		return "";
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		if (shiftSlot != null) {
			
			String shiftDesc = getShiftDescription(shiftSlot);
			
			if (shiftDescCache.equalsIgnoreCase(shiftDesc))
				shiftTF.setText(shiftDesc);
		}
		activityPanel.refresh();
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private class ActivityPanel extends JHistoryPanel<OneActivity> {
		private final static String[] NAMES = {Msg.getString("TabPanelSchedule.column.description"),
		 										Msg.getString("TabPanelSchedule.column.phase"),
												Msg.getString("TabPanelSchedule.column.missionName")};
		private final static Class<?>[] TYPES = {String.class, String.class, String.class};


		ActivityPanel(History<OneActivity> source) {
			super(source, NAMES, TYPES);
		}

		@Override
		protected Object getValueFrom(OneActivity value, int columnIndex) {
			return switch(columnIndex) {
				case 0 -> value.getDescription();
				case 1 -> value.getPhase();
				case 2 -> value.getMission();
				default -> null;
			};
		}
	}
}
