/*
 * Mars Simulation Project
 * TabPanelSchedule.java
 * @date 2023-07-03
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager.OneActivity;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Shift;
import org.mars_sim.msp.core.structure.ShiftSlot;
import org.mars_sim.msp.core.structure.ShiftSlot.WorkStatus;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelSchedule is a tab panel showing the daily schedule a person.
 */
@SuppressWarnings("serial")
public class TabPanelSchedule extends TabPanel {

	private static final String SCH_ICON = "schedule";
	private static final String CHOOSE_SOL = "Sols";
	private static final String SOL = "  Sol ";

	private boolean isRealTimeUpdate;
	private int todayCache = 1;
	private int today;
	private String shiftDescCache; 
	
	private Integer selectedSol;
	private Integer todayInteger;

	private JTable table;

	private JCheckBox realTimeBox;
	private JTextField shiftTF;
	private JLabel shiftLabel;

	private JComboBoxMW<Object> solBox;
	private DefaultComboBoxModel<Object> comboBoxModel;
	private ScheduleTableModel scheduleTableModel;

	private List<OneActivity> activities = new ArrayList<>();
	private List<Integer> solList;
	private Map<Integer, List<OneActivity>> allActivities;

	/** The Person instance. */
	private Person person = null;
	/** The Robot instance. */
	private Robot robot = null;
	
	private ShiftSlot taskSchedule;
	private TaskManager taskManager;
	
	private MasterClock masterClock;

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

		// Prepare combo box
		if (unit instanceof Person) {
			person = (Person) unit;
		} else {
			robot = (Robot) unit;
		}
		masterClock = getSimulation().getMasterClock();
	}

	@Override
	protected void buildUI(JPanel content) {

		isRealTimeUpdate = true;

		// Prepare combo box
		if (person != null) {
			taskSchedule = person.getShiftSlot();
			taskManager = person.getTaskManager();
		} 
		else {
			taskManager = robot.getTaskManager();
		}

		// Create the shift panel.
		JPanel shiftPane = new JPanel(new FlowLayout(FlowLayout.LEFT));

		Unit unit = getUnit();
		if (unit instanceof Person) {

			shiftLabel = new JLabel(Msg.getString("TabPanelSchedule.shift.label"), JLabel.CENTER); //$NON-NLS-1$
			shiftLabel.setToolTipText(Msg.getString("TabPanelSchedule.shift.toolTip")); //$NON-NLS-1$
			shiftPane.add(shiftLabel);

			shiftDescCache = getShiftDescription(taskSchedule);	
			
			shiftTF = new JTextField();
			shiftTF.setText(shiftDescCache);
			
			shiftTF.setEditable(false);
			shiftTF.setColumns(28);
			shiftTF.setHorizontalAlignment(JTextField.CENTER);
			
			shiftPane.add(shiftTF);
		}

		JPanel topPanel = new JPanel(new BorderLayout());
		content.add(topPanel, BorderLayout.NORTH);
		topPanel.add(shiftPane, BorderLayout.NORTH);

		today = masterClock.getMarsTime().getMissionSol();
		
		todayInteger = (Integer) today;
		solList = new CopyOnWriteArrayList<Integer>();

		allActivities = taskManager.getAllActivities();
		int lowerSol = today - TaskManager.NUM_SOLS;
		if (lowerSol < 1)
			lowerSol = 1;
		for (int i = lowerSol; i < today + 1; i++) {
			if (!solList.contains(i))
				solList.add(i);
		}
		
		// Create comboBoxModel
		Collections.sort(solList, Collections.reverseOrder());
		comboBoxModel = new DefaultComboBoxModel<Object>();
		// Using internal iterator in lambda expression
		solList.forEach(s -> comboBoxModel.addElement(s));

		// Create comboBox
		solBox = new JComboBoxMW<>(comboBoxModel);
//		solBox.setMaximumSize(new Dimension(80, 25));
//		solBox.setPrototypeDisplayValue(new Dimension(80, 25));
		solBox.setSelectedItem(todayInteger);
		solBox.setWide(false);
		
		solBox.setRenderer(new PromptComboBoxRenderer(CHOOSE_SOL));
		solBox.setMaximumRowCount(7);
		
		selectedSol = (Integer) solBox.getSelectedItem();
		
		if (selectedSol == null)
			solBox.setSelectedItem(todayInteger);

		solBox.setSelectedItem((Integer) 1);
		solBox.addActionListener(e -> {
			selectedSol = (Integer) solBox.getSelectedItem();
			if (selectedSol != null) // e.g. when first loading up
				scheduleTableModel.update((int) selectedSol);
			if (selectedSol != null && selectedSol.equals(todayInteger))
				// Binds comboBox with realTimeUpdateCheckBox
				realTimeBox.setSelected(true);
		});

		JPanel midPanel = new JPanel(new BorderLayout());
		content.add(midPanel, BorderLayout.CENTER);
		
		JPanel solPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topPanel.add(solPanel, BorderLayout.CENTER);
		
		// Create real time check box.
		realTimeBox = new JCheckBox(Msg.getString("TabPanelSchedule.checkbox.realTimeUpdate")); //$NON-NLS-1$
		realTimeBox.setSelected(true);
		realTimeBox.setHorizontalTextPosition(SwingConstants.RIGHT);
		realTimeBox.setToolTipText(Msg.getString("TabPanelSchedule.tooltip.realTimeUpdate"));
		realTimeBox.addActionListener(s -> {
			if (realTimeBox.isSelected()) {
				isRealTimeUpdate = true;
				scheduleTableModel.update(today);
				solBox.setSelectedItem(todayInteger);
			} else
				isRealTimeUpdate = false;
		});

		solPanel.add(realTimeBox);
		
		JLabel label = new JLabel("     Select a Sol: ");
		solPanel.add(label);
		solPanel.add(solBox);

		// Create schedule table model
		if (unit instanceof Person)
			scheduleTableModel = new ScheduleTableModel((Person) unit);
		else 
			scheduleTableModel = new ScheduleTableModel((Robot) unit);

		// Create attribute scroll panel
		JScrollPane scrollPanel = new JScrollPane();
		midPanel.add(scrollPanel, BorderLayout.CENTER);

		// Create schedule table
		table = new JTable(scheduleTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(7);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(60);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);
		table.setRowSelectionAllowed(true);

		scrollPanel.setViewportView(table);
		table.getColumnModel().getColumn(0).setCellRenderer(new NumberCellRenderer(2));

		update();

		// Do the following once only at the start of the sim
		if (isRealTimeUpdate)
			scheduleTableModel.update(todayInteger);
		else
			scheduleTableModel.update(selectedSol);
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
	@SuppressWarnings("unchecked")
	@Override
	public void update() {

		if (person != null) {
			
			String shiftDesc = getShiftDescription(taskSchedule);
			
			if (shiftDescCache.equalsIgnoreCase(shiftDesc))
				shiftTF.setText(shiftDesc);
		}

		today = masterClock.getMarsTime().getMissionSol();
		
		todayInteger = (Integer) today;
		selectedSol = (Integer) solBox.getSelectedItem(); 
		
		// necessary or else if (isRealTimeUpdate) below will have NPE
		
		// Update the sol combobox at the beginning of a new sol
		if (today != todayCache) {
			int lowerSol = today - TaskManager.NUM_SOLS;
			if (lowerSol < 1)
				lowerSol = 1;
			for (int i = lowerSol; i < today + 1; i++) {
				if (!solList.contains(i))
					solList.add(i);
			}
			
			// Update allActivities
			allActivities = taskManager.getAllActivities();

			Collections.sort(solList, Collections.reverseOrder());

			for (int s : solList) {
				// Check if this element exist
				if (comboBoxModel.getIndexOf(s) == -1) {
					comboBoxModel.addElement(s);
				}
			}
			
			// Update the solList comboBox
			solBox.setModel(comboBoxModel);
			solBox.setRenderer(new PromptComboBoxRenderer());
			solBox.setMaximumRowCount(7);
			
			// Note: Below is needed or else users will be constantly interrupted
			// as soon as the combobox got updated with the new day's schedule
			// and will be swapped out without warning.
			if (selectedSol != null)
				solBox.setSelectedItem(selectedSol);
			else {
				solBox.setSelectedItem(todayInteger);
				selectedSol = null;
			}

			todayCache = today;
		}

		// Checks if the user is still looking at a previous sol's schedule
		if (selectedSol != null && !selectedSol.equals(todayInteger)) {
			// If yes, turn off the Real Time Update automatically
			isRealTimeUpdate = false;
			realTimeBox.setSelected(false);
		}

		// Detects if the real Time Update box is checked 
		if (isRealTimeUpdate) {
			// If yes, need to refresh the schedule to show the latest activities	
			scheduleTableModel.update(todayInteger);
		}
	}

	/**
	 * This class allows add a prompt message to the first choice of the combo box.
	 */
	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private String prompt;
		public PromptComboBoxRenderer() {

			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		public PromptComboBoxRenderer(String prompt) {
			this.prompt = prompt;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value == null) {
				setText(prompt);
				// this.setForeground(Color.orange);
				// this.setBackground(new Color(184,134,11));
				return this;
			}

			setText(SOL + value);// + SPACES);

			// result.setOpaque(false);
			return c;
		}
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private class ScheduleTableModel extends AbstractTableModel {

		/**
		 * hidden constructor.
		 * 
		 * @param person {@link Person}
		 */
		private ScheduleTableModel(Unit unit) {
		}

		@Override
		public int getRowCount() {
			if (activities != null)
				return activities.size();
			else
				return 0;
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0)
				dataType = Double.class;
			else if (columnIndex == 1)
				dataType = String.class;
			else if (columnIndex == 2)
				dataType = String.class;
			else if (columnIndex == 3)
				dataType = String.class;
			else
				dataType = null;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return Msg.getString("TabPanelSchedule.column.time"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("TabPanelSchedule.column.description"); //$NON-NLS-1$
			else if (columnIndex == 2)
				return Msg.getString("TabPanelSchedule.column.phase"); //$NON-NLS-1$
			else if (columnIndex == 3)
				return Msg.getString("TabPanelSchedule.column.missionName"); //$NON-NLS-1$
			else
				return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			OneActivity activity = activities.get(row);
			if (column == 0)
				return activity.getStartTime();
			else if (column == 1)
				return activity.getDescription();
			else if (column == 2)
				return activity.getPhase();
			else if (column == 3) {
				return activity.getMission();
			}
			else
				return null;
		}


		/**
		 * Prepares a list of activities done on the selected sol.
		 * 
		 * @param selectedSol
		 */
		public void update(int selectedSol) {		
			List<OneActivity> activityList = new ArrayList<>();
			
			allActivities = taskManager.getAllActivities();
			// Load the schedule of a particular sol
			if (allActivities.containsKey(selectedSol))
				activityList.addAll(allActivities.get(selectedSol));

			activities = activityList;
			
			fireTableDataChanged();
		}
	}

	/**
	 * Prepares for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		if (solBox != null)
			solBox.removeAllItems();
		if (comboBoxModel != null)
			comboBoxModel.removeAllElements();

		activities = null;
		allActivities = null;
		solBox = null;
		comboBoxModel = null;
		solList = null;
		table = null;
		realTimeBox = null;
		shiftTF = null;
		shiftLabel = null;
		scheduleTableModel = null;
		person = null;
		robot = null;
		taskSchedule = null;
	}

}
