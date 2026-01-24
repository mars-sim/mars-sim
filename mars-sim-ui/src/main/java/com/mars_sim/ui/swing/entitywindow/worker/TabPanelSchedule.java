/*
 * Mars Simulation Project
 * TabPanelSchedule.java
 * @date 2023-08-27
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.worker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.data.History;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.shift.Shift;
import com.mars_sim.core.person.ai.shift.ShiftSlot;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.util.OneActivity;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.JHistoryPanel;

/**
 * The TabPanelSchedule is a tab panel showing the daily schedule a person.
 */
@SuppressWarnings("serial")
public class TabPanelSchedule extends EntityTabPanel<Worker>
				implements EntityListener {

	private static final String SCH_ICON = "schedule";
	private static final String NOTE = "Note : ";
	
	private String noteCache; 
	private String shiftCache;
	private String timePeriodCache;
	private String statusCache;
	
	private JTextField shiftNoteTF;

	private JLabel shiftLabel;
	private JLabel timeLabel;
	private JLabel statusLabel;
	
	private ShiftSlot shiftSlot;

	private ActivityPanel activityPanel;

	/**
	 * Constructor.
	 * 
	 * @param worker  the worker for this panel.
	 * @param context the UI context.
	 */
	public TabPanelSchedule(Worker worker, UIContext context) {
		super(
			null,
			ImageLoader.getIconByName(SCH_ICON),
			Msg.getString("TabPanelSchedule.title"), //$NON-NLS-1$
			context, worker
		);

		if (worker instanceof Person person) {
			shiftSlot = person.getShiftSlot();
		} 
	}

	@Override
	protected void buildUI(JPanel content) {

		// Prepare label panel
		JPanel northPanel = new JPanel(new BorderLayout());
		content.add(northPanel, BorderLayout.NORTH);
				
		AttributePanel attrPanel = new AttributePanel();
		northPanel.add(attrPanel, BorderLayout.NORTH);
		
		// Create the shift panel.
		JPanel shiftPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		northPanel.add(shiftPane, BorderLayout.CENTER);
		
		if (shiftSlot != null) {

			shiftCache = getWorkShift(shiftSlot);
			
			shiftLabel = attrPanel.addRow(Msg.getString("TabPanelSchedule.shift.label"), //$NON-NLS-1$
					shiftCache);
			
			timePeriodCache = getWorkPeriod(shiftSlot);
			
			timeLabel = attrPanel.addRow(Msg.getString("TabPanelSchedule.shift.period.label"), //$NON-NLS-1$
					timePeriodCache);

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

		activityPanel = new ActivityPanel(getEntity().getTaskManager().getAllActivities());
		activityPanel.setPreferredSize(new Dimension(225, 100));

		content.add(activityPanel, BorderLayout.CENTER);

		if (shiftSlot != null) {
			updateShift();
		}
		activityPanel.refresh();
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
			case ON_CALL, ON_LEAVE:
				return status.getName();
			case ON_DUTY:
				return status.getName() + " ends @ " + end;
			case OFF_DUTY:
				return status.getName() + " ends @ " + start;
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
				return status.getName();
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
			case ON_DUTY, OFF_DUTY:
				return "From " + start + " to " + end + " millisols";
			case ON_CALL:
				return "Anytime";
			case ON_LEAVE:
				return "None";
		}

		return "";
	}
	
	/**
	 * Updates the info on this panel.
	 */
	private void updateShift() {
		
		String shift = getWorkShift(shiftSlot);
		
		if (!shiftCache.equalsIgnoreCase(shift)) {
			shiftCache = shift;
			shiftLabel.setText(shift);
		}
				
		String shiftDesc = getShiftNote(shiftSlot);
		if (!noteCache.equalsIgnoreCase(shiftDesc)) {
			noteCache = shiftDesc;
			shiftNoteTF.setText(NOTE + shiftDesc);
		}
		updateShiftStatus();
	}

	/**
	 * Updates the shift status info on this panel. These details are time dependent.
	 */
	private void updateShiftStatus() {
		String timePeriod = getWorkPeriod(shiftSlot);
		if (!timePeriodCache.equalsIgnoreCase(timePeriod)) {
			timePeriodCache = timePeriod;
			timeLabel.setText(timePeriod);
		}	
		String status = shiftSlot.getStatus().getName();		
		if (!statusCache.equalsIgnoreCase(status)) {
			statusCache = status;
			statusLabel.setText(status);
		}
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private class ActivityPanel extends JHistoryPanel<OneActivity> {
		private static final ColumnSpec[] COLUMNS = {
								new ColumnSpec(Msg.getString("entity.description"), String.class),
								new ColumnSpec(Msg.getString("task.phase"), String.class),
								new ColumnSpec(Msg.getString("mission.singular"), String.class)
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

	/**
	 * Listens for Shift events
	 * @param event
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		switch(event.getType()) {
			case ShiftSlot.SHIFT_EVENT -> updateShift();
			case TaskManager.TASK_EVENT, EntityEventType.TASK_ENDED_EVENT,
						EntityEventType.MISSION_EVENT -> {
							activityPanel.refresh();
							if (shiftSlot != null) {
								updateShiftStatus();
							}
						}
			default -> {
				// Only those events matter
				}
			}
	}
}
