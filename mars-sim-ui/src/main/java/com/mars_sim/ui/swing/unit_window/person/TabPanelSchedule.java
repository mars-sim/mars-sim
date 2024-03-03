/*
 * Mars Simulation Project
 * TabPanelSchedule.java
 * @date 2023-08-27
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.mars_sim.core.Unit;
import com.mars_sim.core.data.History;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.shift.Shift;
import com.mars_sim.core.person.ai.shift.ShiftSlot;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.OneActivity;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.JHistoryPanel;

/**
 * The TabPanelSchedule is a tab panel showing the daily schedule a person.
 */
@SuppressWarnings("serial")
public class TabPanelSchedule extends TabPanel {

	private static final String SCH_ICON = "schedule";
	private static final String NOTE = "Note : ";
	
	private String noteCache; 
	private String shiftCache;
	private String timeCache;
	private String statusCache;
	
	private JTextField shiftNoteTF;

	private JLabel shiftLabel;
	private JLabel timeLabel;
	private JLabel statusLabel;
	
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

		// Prepare label panel
		JPanel northPanel = new JPanel(new BorderLayout());
		content.add(northPanel, BorderLayout.NORTH);
				
		AttributePanel attrPanel = new AttributePanel(3);
		northPanel.add(attrPanel, BorderLayout.NORTH);
		
		// Create the shift panel.
		JPanel shiftPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		northPanel.add(shiftPane, BorderLayout.CENTER);
		
		if (shiftSlot != null) {

			shiftCache = getWorkShift(shiftSlot);
			
			shiftLabel = attrPanel.addRow(Msg.getString("TabPanelSchedule.shift.label"), //$NON-NLS-1$
					shiftCache);
			
			timeCache = getWorkPeriod(shiftSlot);
			
			timeLabel = attrPanel.addRow(Msg.getString("TabPanelSchedule.shift.period.label"), //$NON-NLS-1$
					timeCache);

			statusCache = shiftSlot.getStatus().getName();
			
			statusLabel = attrPanel.addRow(Msg.getString("TabPanelSchedule.shift.status.label"), //$NON-NLS-1$
					statusCache);
			
			noteCache = getShiftNote(shiftSlot);	
			
			shiftNoteTF = new JTextField();
			shiftNoteTF.setFont(new Font("Arial", Font.ITALIC | Font.PLAIN, 12));
			shiftNoteTF.setText(NOTE + noteCache);
			
			shiftNoteTF.setEditable(false);
			shiftNoteTF.setColumns(20);
			shiftNoteTF.setHorizontalAlignment(SwingConstants.CENTER);
			
			shiftPane.add(shiftNoteTF);
		}

		activityPanel = new ActivityPanel(taskManager.getAllActivities());
		activityPanel.setPreferredSize(new Dimension(225, 100));

		content.add(activityPanel, BorderLayout.CENTER);

		update();
	}

	/**
	 * Gets the shift note.
	 * 
	 * @param shift
	 * @return
	 */
	public static String getShiftNote(ShiftSlot shiftSlot) {
		WorkStatus status = shiftSlot.getStatus();
		
		Shift s = shiftSlot.getShift();
		int start = s.getStart();
		int end = s.getEnd();

		switch(status) {
			case ON_CALL:
				return "None";
			case ON_DUTY:
				return "On Duty ends @ " + end;
			case OFF_DUTY:
				return "Off Duty ends @ " + start;
			case ON_LEAVE:
				return "On Leave";
		}
		
		return "";
	}

	/**
	 * Gets the work shift.
	 * 
	 * @param shift
	 * @return
	 */
	public static String getWorkShift(ShiftSlot shift) {
		WorkStatus status = shift.getStatus();
		
		Shift s = shift.getShift();
		String shiftName = s.getName();
		
		switch(status) {
			case ON_CALL:
				return "On Call";
			case ON_DUTY, OFF_DUTY, ON_LEAVE:
				return shiftName;
		}

		return "";
	}
	
	/**
	 * Gets the work period.
	 * 
	 * @param shift
	 * @return
	 */
	public static String getWorkPeriod(ShiftSlot shift) {
		WorkStatus status = shift.getStatus();
		
		Shift s = shift.getShift();
		int start = s.getStart();
		int end = s.getEnd();

		switch(status) {
			case ON_CALL:
				return "Anytime";
			case ON_DUTY, OFF_DUTY:
				return start + " - " + end + " millisols";
			case ON_LEAVE:
				return "On Leave";
		}

		return "";
	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		if (shiftSlot != null) {
			
			String shift = getWorkShift(shiftSlot);
			
			if (!shiftCache.equalsIgnoreCase(shift)) {
				shiftCache = shift;
				shiftLabel.setText(shift);
			}
			
			String time = getWorkPeriod(shiftSlot);
			
			if (!timeCache.equalsIgnoreCase(time)) {
				timeCache = time;
				timeLabel.setText(time);
			}
			
			String status = Conversion.capitalize(shiftSlot.getStatus().toString());
					
			if (!statusCache.equalsIgnoreCase(status)) {
				statusCache = status;
				statusLabel.setText(status);
			}
					
			String shiftDesc = getShiftNote(shiftSlot);
			
			if (!noteCache.equalsIgnoreCase(shiftDesc)) {
				noteCache = shiftDesc;
				shiftNoteTF.setText(NOTE + shiftDesc);
			}
		}
		
		activityPanel.refresh();
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private class ActivityPanel extends JHistoryPanel<OneActivity> {
		private static final ColumnSpec[] COLUMNS = {
								new ColumnSpec(Msg.getString("TabPanelSchedule.column.description"), String.class),
								new ColumnSpec(Msg.getString("TabPanelSchedule.column.phase"), String.class),
								new ColumnSpec(Msg.getString("TabPanelSchedule.column.missionName"), String.class)
										};

		ActivityPanel(History<OneActivity> source) {
			super(source, COLUMNS);
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
