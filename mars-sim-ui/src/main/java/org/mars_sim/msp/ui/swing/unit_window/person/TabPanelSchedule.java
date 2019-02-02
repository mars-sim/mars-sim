/**
 * Mars Simulation Project
 * TabPanelFavorite.java
 * @version 3.1.0 2017-03-11
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.TaskSchedule;
import org.mars_sim.msp.core.person.TaskSchedule.OneActivity;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;
import com.alee.laf.text.WebTextField;
//import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The TabPanelSchedule is a tab panel showing the daily schedule a person.
 */
public class TabPanelSchedule extends TabPanel {

	private static final String SOL = "  Sol ";
	private static final String TWO_SPACES = "  ";

	// private int sol;

	private boolean hideRepeated;
	private boolean hideRepeatedCache;
	private boolean isRealTimeUpdate;
	private int todayCache = 1;
	private int today;
	private int start;
	private int end;
	private int theme;
	private int selectedSolCache;
	
	private Integer selectedSol;
	private Integer todayInteger;

	private ShiftType shiftType;
	private ShiftType shiftCache = null;

	private WebTable table;

	private WebCheckBox hideBox;
	private WebCheckBox realTimeBox;
	private WebTextField shiftTF;
	private WebLabel shiftLabel;

	private JComboBoxMW<Object> solBox;
	private DefaultComboBoxModel<Object> comboBoxModel;
	private ScheduleTableModel scheduleTableModel;

//	private Color fillColorCache;
	// private Color transparentFill;
	// private ModernBalloonStyle style;

	// private List<OneTask> tasks;
	private List<OneActivity> activities;
	private List<Integer> solList;
	// private Map <Integer, List<OneTask>> schedules;
	private Map<Integer, List<OneActivity>> allActivities;

	private Person person;
	private Robot robot;
	private TaskSchedule taskSchedule;
	private PlannerWindow plannerWindow;
	private MainDesktopPane desktop;

	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelSchedule(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(Msg.getString("TabPanelSchedule.title"), //$NON-NLS-1$
				null, Msg.getString("TabPanelSchedule.tooltip"), //$NON-NLS-1$
				unit, desktop);

		this.desktop = desktop;
		isRealTimeUpdate = true;

		// Prepare combo box
		if (unit instanceof Person) {
			person = (Person) unit;
			taskSchedule = person.getTaskSchedule();
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			taskSchedule = robot.getTaskSchedule();
		}

		// schedules = taskSchedule.getSchedules();
		allActivities = taskSchedule.getAllActivities();

		// Create label panel.
		WebPanel labelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Prepare label
		WebLabel label = new WebLabel(Msg.getString("TabPanelSchedule.label"), WebLabel.CENTER); //$NON-NLS-1$
		label.setFont(new Font("Serif", Font.BOLD, 16));
		labelPanel.add(label);

		// Create the button panel.
		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(buttonPane);// , BorderLayout.NORTH);

		if (unit instanceof Person) {

			shiftType = taskSchedule.getShiftType();
			shiftCache = shiftType;
			shiftLabel = new WebLabel(Msg.getString("TabPanelSchedule.shift.label"), WebLabel.CENTER); //$NON-NLS-1$

			TooltipManager.setTooltip(shiftLabel, Msg.getString("TabPanelSchedule.shift.toolTip"), TooltipWay.down);
			// balloonToolTip.createBalloonTip(shiftLabel,
			// Msg.getString("TabPanelSchedule.shift.toolTip")); //$NON-NLS-1$
			buttonPane.add(shiftLabel);

//    		fillColorCache = shiftLabel.getBackground();

			shiftTF = new WebTextField(shiftCache.toString());
			start = taskSchedule.getShiftStart();
			end = taskSchedule.getShiftEnd();
			shiftTF.setEditable(false);
			shiftTF.setColumns(6);

			shiftTF.setHorizontalAlignment(WebTextField.CENTER);
			buttonPane.add(shiftTF);

		}

		Box box = Box.createHorizontalBox();
		box.setBorder(new MarsPanelBorder());

//		centerContentPanel.add(infoPanel, BorderLayout.NORTH);
		centerContentPanel.add(box, BorderLayout.NORTH);

		// Create hideRepeatedTaskBox.
		hideBox = new WebCheckBox(Msg.getString("TabPanelSchedule.checkbox.showRepeatedTask")); //$NON-NLS-1$
		// hideRepeatedTasksCheckBox.setHorizontalTextPosition(SwingConstants.RIGHT);
		hideBox.setFont(new Font("Serif", Font.PLAIN, 12));
		// hideRepeatedTasksCheckBox.setToolTipText(Msg.getString("TabPanelSchedule.tooltip.showRepeatedTask"));
		// //$NON-NLS-1$
		TooltipManager.setTooltip(hideBox, Msg.getString("TabPanelSchedule.tooltip.showRepeatedTask"), TooltipWay.down);
		// balloonToolTip.createBalloonTip(hideBox,
		// Msg.getString("TabPanelSchedule.tooltip.showRepeatedTask")); //$NON-NLS-1$);
		hideBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (hideBox.isSelected()) {
					hideRepeated = true;
				} else {
					hideRepeated = false;
				}

			}
		});
		hideBox.setSelected(hideRepeated);
//		infoPanel.add(hideRepeatedTasksCheckBox);
		box.add(hideBox);
		box.add(Box.createHorizontalGlue());

		today = taskSchedule.getSolCache();
		todayInteger = (Integer) today;
		solList = new CopyOnWriteArrayList<Integer>();

		allActivities = taskSchedule.getAllActivities();
		for (int key : allActivities.keySet()) {
			solList.add(key);
		}

		if (!solList.contains(today))
			solList.add(today);

		// Create comboBoxModel
		Collections.sort(solList, Collections.reverseOrder());
		comboBoxModel = new DefaultComboBoxModel<Object>();
		// Using internal iterator in lambda expression
		solList.forEach(s -> comboBoxModel.addElement(s));

		// Create comboBox
		solBox = new JComboBoxMW<Object>(comboBoxModel);

		solBox.setSelectedItem(todayInteger);
		// comboBox.setOpaque(false);
		solBox.setRenderer(new PromptComboBoxRenderer());
		solBox.setMaximumRowCount(7);

		WebPanel solPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
//		solPanel.setMinimumSize(new Dimension(40, 15));
//		solPanel.setSize(new Dimension(40, 15));
		solPanel.add(solBox);

//		infoPanel.add(solPanel);
		box.add(solPanel);
		box.add(Box.createHorizontalGlue());

		selectedSol = (Integer) solBox.getSelectedItem();
		if (selectedSol == null)
			solBox.setSelectedItem(todayInteger);

		solBox.setSelectedItem((Integer) 1);
		solBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedSol = (Integer) solBox.getSelectedItem();
				if (selectedSol != null) // e.g. when first loading up
					scheduleTableModel.update(hideRepeated, (int) selectedSol);
				if (selectedSol == todayInteger)
					// Binds comboBox with realTimeUpdateCheckBox
					realTimeBox.setSelected(true);
			}
		});

		// Create realTimeUpdateCheckBox.
		realTimeBox = new WebCheckBox(Msg.getString("TabPanelSchedule.checkbox.realTimeUpdate")); //$NON-NLS-1$
		realTimeBox.setSelected(true);
		realTimeBox.setHorizontalTextPosition(SwingConstants.RIGHT);
		realTimeBox.setFont(new Font("Serif", Font.PLAIN, 12));
		// realTimeUpdateCheckBox.setToolTipText(Msg.getString("TabPanelSchedule.tooltip.realTimeUpdate"));
		// //$NON-NLS-1$
		// balloonToolTip.createBalloonTip(realTimeBox,
		// Msg.getString("TabPanelSchedule.tooltip.realTimeUpdate"));
		TooltipManager.setTooltip(realTimeBox, Msg.getString("TabPanelSchedule.tooltip.realTimeUpdate"),
				TooltipWay.down);
		realTimeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (realTimeBox.isSelected()) {
					isRealTimeUpdate = true;
					scheduleTableModel.update(hideRepeated, today);
					solBox.setSelectedItem(todayInteger);
				} else
					isRealTimeUpdate = false;
			}
		});
		box.add(realTimeBox);

		// Create schedule table model
		if (unit instanceof Person)
			scheduleTableModel = new ScheduleTableModel((Person) unit);
		else if (unit instanceof Robot)
			scheduleTableModel = new ScheduleTableModel((Robot) unit);

		// Create attribute scroll panel
		WebScrollPane scrollPanel = new WebScrollPane();
		scrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(scrollPanel);

		// Create schedule table
		table = new ZebraJTable(scheduleTableModel);
		TableStyle.setTableStyle(table);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(8);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(60);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);
		table.setRowSelectionAllowed(true);
		// table.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		scrollPanel.setViewportView(table);

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer);
		table.getColumnModel().getColumn(2).setCellRenderer(renderer);
		table.getColumnModel().getColumn(3).setCellRenderer(renderer);

		// SwingUtilities.invokeLater(() ->
		// ColumnResizer.adjustColumnPreferredWidths(table));

		// Added sorting
//		table.setAutoCreateRowSorter(true);

		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		int t = -1;

//		if (desktop.getMainScene() != null)
//			t = MainScene.getTheme();

		if (theme != t) {
			theme = t;
			TableStyle.setTableStyle(table);
		}

		if (person != null) {
			shiftType = taskSchedule.getShiftType();

			// if (shiftCache != null)
			if (shiftCache != shiftType) {
				shiftCache = shiftType;
				shiftTF.setText(shiftCache.toString());
			}

			start = taskSchedule.getShiftStart();
			end = taskSchedule.getShiftEnd();

//    		if (shiftCache != ShiftType.OFF && shiftTF != null)
//    			TooltipManager.setTooltip (shiftTF, Msg.getString("TabPanelSchedule.shiftTF.toolTip", shiftCache, start, end), TooltipWay.down);
//    		else
//    			TooltipManager.setTooltip (shiftTF, Msg.getString("TabPanelSchedule.shiftTF.toolTip.off"), TooltipWay.down);

			// System.out.println("fillColorCache is "+ fillColorCache);
//			if (fillColorCache != shiftTF.getBackground()) {
//				fillColorCache = shiftTF.getBackground();
//			//	System.out.println("Set fillColorCache to "+ fillColorCache);
//	    		balloonToolTip.createBalloonTip(shiftLabel, Msg.getString("TabPanelSchedule.shift.toolTip")); //$NON-NLS-1$
//	      		balloonToolTip.createBalloonTip(shiftTF, Msg.getString("TabPanelSchedule.shiftTF.toolTip", shiftCache, start, end));  //$NON-NLS-1$
//	    		balloonToolTip.createBalloonTip(hideBox, Msg.getString("TabPanelSchedule.tooltip.showRepeatedTask")); //$NON-NLS-1$);
//	    		balloonToolTip.createBalloonTip(realTimeBox, Msg.getString("TabPanelSchedule.tooltip.realTimeUpdate")); //$NON-NLS-1$
//	    		//SwingUtilities.updateComponentTreeUI(desktop);
//				desktop.updateToolWindowLF();
//			}

		}

		today = taskSchedule.getSolCache();
		todayInteger = (Integer) today;
		// System.out.println("today is " + today);
		selectedSol = (Integer) solBox.getSelectedItem(); // necessary or else if (isRealTimeUpdate) below will have
															// NullPointerException

		// Update the sol combobox at the beginning of a new sol
		if (today != todayCache) {
			solList.clear();
			if (!solList.contains(today))
				solList.add(today);
			// int max = todayCache;
			for (int key : allActivities.keySet()) {
				// System.out.println("key is " + key);
				// if (key > max) max = key;
				solList.add(key);
			}
			// OptionalInt max = solList.stream().mapToInt((x) -> x).max();
			// solList.add(max + 1);
			if (!solList.contains(today))
				solList.add(today);

			// Inserting and deleting is not working below
//    		int size = solList.size();
//    		//System.out.println("size is " + size);
//    		int max = TaskSchedule.NUM_SOLS;
//    		if (size > max) {
//    			//System.out.println("Removing a Sol ");
//    			solList.remove(0); // remove the first item at index 0
//    		}

			Collections.sort(solList, Collections.reverseOrder());
			DefaultComboBoxModel<Object> newComboBoxModel = new DefaultComboBoxModel<Object>();
			solList.forEach(s -> newComboBoxModel.addElement(s));

			// Update the solList comboBox
			solBox.setModel(newComboBoxModel);
			solBox.setRenderer(new PromptComboBoxRenderer());

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

		// Turn off the Real Time Update if the user is still looking at a previous
		// sol's schedule
		if (selectedSol != todayInteger) {
			isRealTimeUpdate = false;
			realTimeBox.setSelected(false);
		}

		// Detects if the Hide Repeated box has changed. If yes, call for update
		if (hideRepeatedCache != hideRepeated) {
			hideRepeatedCache = hideRepeated;
			scheduleTableModel.update(hideRepeated, selectedSol);
		}

		if (isRealTimeUpdate)
			scheduleTableModel.update(hideRepeated, todayInteger);

	}

	public void setViewer(PlannerWindow w) {
		this.plannerWindow = w;
	}

	/**
	 * Opens PlannerWindow
	 */
//	private void openPlannerWindow() {
//		// Create PlannerWindow
//		if (plannerWindow == null)
//			plannerWindow = new PlannerWindow(unit, desktop, this);
//	}

	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private String prompt;

		// public boolean isOptimizedDrawingEnabled();
		// private DefaultListCellRenderer defaultRenderer = new
		// DefaultListCellRenderer();
		public PromptComboBoxRenderer() {
			// defaultRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
			// settlementListBox.setRenderer(defaultRenderer);
			// setOpaque(false);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		public PromptComboBoxRenderer(String prompt) {
			this.prompt = prompt;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
//			JComponent result = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected,
//					cellHasFocus);
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			// Component component = super.getListCellRendererComponent(list, value, index,
			// isSelected, cellHasFocus);
			// component.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			if (value == null) {
				setText(prompt);
				// this.setForeground(Color.orange);
				// this.setBackground(new Color(184,134,11));
				return this;
			}

			setText(SOL + value + TWO_SPACES);

			// 184,134,11 mud yellow
			// 255,229,204 white-ish (super pale) yellow
			// (37, 85, 118) navy blue
			// 131,172,234 pale sky blue

			if (isSelected) {
				if (theme == 7) {
					c.setBackground(new Color(184, 134, 11, 255)); // 184,134,11 mud yellow
					c.setForeground(Color.white);// new Color(255,229,204)); // 255,229,204 white-ish (super pale)
														// yellow
				} else {// if (theme == 0 || theme == 6) {
					c.setBackground(new Color(37, 85, 118, 255)); // (37, 85, 118) navy blue
					c.setForeground(Color.white);// new Color(131,172,234)); // 131,172,234 pale sky blue
				}

			} else {
				// unselected, and not the DnD drop location
				if (theme == 7) {
					c.setForeground(new Color(184, 134, 11)); // 184,134,11 mud yellow
					c.setBackground(new Color(255, 229, 204, 40)); // 255,229,204 white-ish (super pale) yellow
				} else {// if (theme == 0 || theme == 6) {
					c.setForeground(new Color(37, 85, 118));// (37, 85, 118) navy blue
					c.setBackground(new Color(131, 172, 234, 40)); // 131,172,234 pale sky blue
				}
			}
			// result.setOpaque(false);
			return c;
		}
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private class ScheduleTableModel extends AbstractTableModel {

		DecimalFormat fmt = new DecimalFormat("000");

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
				dataType = String.class;
			if (columnIndex == 1)
				dataType = String.class;
			if (columnIndex == 2)
				dataType = String.class;
			if (columnIndex == 3)
				dataType = String.class;
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
			if (column == 0)
				return fmt.format(activities.get(row).getStartTime());
			else if (column == 1)
				return taskSchedule.convertTaskDescription(activities.get(row).getDescription());
			else if (column == 2)
				return taskSchedule.convertTaskPhase(activities.get(row).getPhase());
			else if (column == 3) {
				return taskSchedule.convertMissionName(activities.get(row).getMission());
				
//				int id = activities.get(row).getTaskName();
//				if (taskSchedule.convertTaskName(id) != null
//						|| taskSchedule.convertTaskName(id).equals(""))
//					return taskSchedule.convertTaskName(id);
//				else
//					return taskSchedule.convertMissionName(id);
				
			// formatClassName(taskSchedule.convertTaskName(activities.get(row).getTaskName()));
			}
			else
				return null;
		}

//		public String formatClassName(String s) {
//			String ss = s.replaceAll("(?!^)([A-Z])", " $1").replace("E V A ", "EVA ").replace("To ", "to ");
//			//System.out.println(ss + " <-- " + s);
//			return ss;
//		}

		/**
		 * Prepares a list of activities done on the selected day
		 * 
		 * @param hideRepeatedTasks
		 * @param selectedSol
		 */
		public void update(boolean hideRepeatedTasks, int selectedSol) {
			int todaySol = taskSchedule.getSolCache();
			OneActivity lastTask = null;
			String lastDes = null;
			OneActivity currentTask = null;
			String currentDes = null;

			if (selectedSolCache != selectedSol) {
				selectedSolCache = selectedSol;
				// Load previous day's schedule if selected
				if (todaySol == selectedSol) {
					// Load today's schedule
					activities = new ArrayList<OneActivity>(taskSchedule.getTodayActivities());
				} 
				
				else {
					// Load the schedule of a particular sol
					activities = new ArrayList<OneActivity>(allActivities.get(selectedSol));
				}
			}
			
			// Check if user selected hide repeated tasks checkbox
			if (activities != null && hideRepeatedTasks) {
				// Show only non-repeating consecutive tasks
				List<OneActivity> displaySchedule = new ArrayList<OneActivity>(activities);

				int size = displaySchedule.size();

				for (int i = size - 1; i >= 0; i--) {
					currentTask = displaySchedule.get(i);
					currentDes = taskSchedule.convertTaskDescription(currentTask.getDescription());
					// Make sure this is NOT the very first task (i = 0) of the day
					if (i != 0) {
						lastTask = displaySchedule.get(i - 1);
						lastDes = taskSchedule.convertTaskDescription(lastTask.getDescription());

						// Check if the last task is the same as the current task
						if (lastDes.equals(currentDes)) {
							displaySchedule.remove(i);
						}
					}
				}

//				activities = displaySchedule;
			}

			fireTableDataChanged();

		}

	}

	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		if (solBox != null)
			solBox.removeAllItems();
		if (comboBoxModel != null)
			comboBoxModel.removeAllElements();

//		if (tasks != null)
//        	tasks.clear();
//        if (solList != null)
//        	solList.clear();
//        if (schedules != null)
//        	schedules.clear();

		activities = null;
		allActivities = null;
		// todayActivities = null;
		solBox = null;
		comboBoxModel = null;
		// tasks = null;
		solList = null;
		// schedules = null;
		table = null;
		hideBox = null;
		realTimeBox = null;
		shiftTF = null;
		shiftLabel = null;
		scheduleTableModel = null;
//		fillColorCache = null;
		person = null;
		robot = null;
		taskSchedule = null;
		plannerWindow = null;
		desktop = null;
		// balloonToolTip = null;

	}

}