/*
 * Mars Simulation Project
 * TabPanelFavorite.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager.OneActivity;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;
//import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The TabPanelSchedule is a tab panel showing the daily schedule a person.
 */
@SuppressWarnings("serial")
public class TabPanelSchedule extends TabPanel {

	private static final String SCH_ICON = Msg.getString("icon.schedule"); //$NON-NLS-1$

	private static final String SOL = "  Sol ";

	private boolean isRealTimeUpdate;
	private int todayCache = 1;
	private int today;
	private int start;
	private int end;
	private int theme;
	
	private Integer selectedSol;
	private Integer todayInteger;

	private ShiftType shiftType;
	private ShiftType shiftCache = null;

	private JTable table;

	private WebCheckBox realTimeBox;
	private WebTextField shiftTF;
	private WebLabel shiftLabel;

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
	
	private TaskSchedule taskSchedule;
	private TaskManager taskManager;
	
	private static MarsClock marsClock;

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
			ImageLoader.getNewIcon(SCH_ICON),
			Msg.getString("TabPanelSchedule.title"), //$NON-NLS-1$
			unit, desktop
		);

		// Prepare combo box
		if (unit instanceof Person) {
			person = (Person) unit;
		} else {
			robot = (Robot) unit;
		}
	}

	@Override
	protected void buildUI(JPanel content) {
		
		if (marsClock == null)
			marsClock = getSimulation().getMasterClock().getMarsClock();

		isRealTimeUpdate = true;

		// Prepare combo box
		if (person != null) {
			taskSchedule = person.getTaskSchedule();
			taskManager = person.getTaskManager();
		} 
		else {
			taskManager = robot.getTaskManager();
		}

		// Create the button panel.
		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));

		Unit unit = getUnit();
		if (unit instanceof Person) {

			shiftType = taskSchedule.getShiftType();
			shiftCache = shiftType;
			shiftLabel = new WebLabel(Msg.getString("TabPanelSchedule.shift.label"), WebLabel.CENTER); //$NON-NLS-1$

			TooltipManager.setTooltip(shiftLabel, Msg.getString("TabPanelSchedule.shift.toolTip"), TooltipWay.down); //$NON-NLS-1$
			buttonPane.add(shiftLabel);

			shiftTF = new WebTextField();
			start = taskSchedule.getShiftStart();
			end = taskSchedule.getShiftEnd();
			
			if (shiftCache == ShiftType.OFF || shiftCache == ShiftType.ON_CALL)
				shiftTF.setText(shiftCache.toString());
			else
				shiftTF.setText(shiftCache.toString() + " : (" + start + " to " + end + ")");
			
			shiftTF.setEditable(false);
			shiftTF.setColumns(15);

			shiftTF.setHorizontalAlignment(WebTextField.CENTER);
			buttonPane.add(shiftTF);

		}

		WebPanel topPanel = new WebPanel(new BorderLayout());
		content.add(topPanel, BorderLayout.NORTH);
		topPanel.add(buttonPane, BorderLayout.NORTH);

		today = marsClock.getMissionSol();
		
		todayInteger = (Integer) today;
		solList = new CopyOnWriteArrayList<Integer>();

		allActivities = taskManager.getAllActivities();

		for (int i = 1; i < today + 1; i++) {
			if (!solList.contains(i))
				solList.add(i);
		}
		
		// Create comboBoxModel
		Collections.sort(solList, Collections.reverseOrder());
		comboBoxModel = new DefaultComboBoxModel<Object>();
		// Using internal iterator in lambda expression
		solList.forEach(s -> comboBoxModel.addElement(s));

		// Create comboBox
		solBox = new JComboBoxMW<Object>(comboBoxModel);
		solBox.setPreferredSize(new Dimension(80, 25));
		solBox.setPrototypeDisplayValue(new Dimension(80, 25));
		solBox.setSelectedItem(todayInteger);
		solBox.setWide(true);
		
		solBox.setRenderer(new PromptComboBoxRenderer());
		solBox.setMaximumRowCount(7);

		WebPanel solPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));	
		solPanel.add(solBox);

		topPanel.add(solPanel, BorderLayout.CENTER);

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

		// Create realTimeUpdateCheckBox.
		realTimeBox = new WebCheckBox(Msg.getString("TabPanelSchedule.checkbox.realTimeUpdate")); //$NON-NLS-1$
		realTimeBox.setSelected(true);
		realTimeBox.setHorizontalTextPosition(SwingConstants.RIGHT);
		realTimeBox.setFont(new Font("Serif", Font.PLAIN, 12));
		TooltipManager.setTooltip(realTimeBox, Msg.getString("TabPanelSchedule.tooltip.realTimeUpdate"),
				TooltipWay.down);
		realTimeBox.addActionListener(s -> {
			if (realTimeBox.isSelected()) {
				isRealTimeUpdate = true;
				scheduleTableModel.update(today);
				solBox.setSelectedItem(todayInteger);
			} else
				isRealTimeUpdate = false;
		});
		
		topPanel.add(realTimeBox, BorderLayout.WEST);
		topPanel.add(new WebPanel(new JLabel("                    ")), BorderLayout.EAST);
		
		// Create schedule table model
		if (unit instanceof Person)
			scheduleTableModel = new ScheduleTableModel((Person) unit);
		else 
			scheduleTableModel = new ScheduleTableModel((Robot) unit);

		// Create attribute scroll panel
		WebScrollPane scrollPanel = new WebScrollPane();
		content.add(scrollPanel);

		// Create schedule table
		table = new ZebraJTable(scheduleTableModel);
		TableStyle.setTableStyle(table);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(7);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(60);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);
		table.setRowSelectionAllowed(true);
		// table.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		// Apply sorting for multiple columns
//		table.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		
		scrollPanel.setViewportView(table);

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		
		DefaultTableCellRenderer renderer1 = new DefaultTableCellRenderer();
		renderer1.setHorizontalAlignment(SwingConstants.LEFT);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer1);
		table.getColumnModel().getColumn(2).setCellRenderer(renderer1);
		table.getColumnModel().getColumn(3).setCellRenderer(renderer1);

		// SwingUtilities.invokeLater(() ->
		// ColumnResizer.adjustColumnPreferredWidths(table));

		// Added sorting
//		table.setAutoCreateRowSorter(true);

		update();
		
		// Do the following once only at the start of the sim
		if (isRealTimeUpdate)
			scheduleTableModel.update(todayInteger);
		else
			scheduleTableModel.update(selectedSol);
	}

	/**
	 * Updates the info on this panel.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void update() {

		int t = -1;

		if (theme != t) {
			theme = t;
			TableStyle.setTableStyle(table);
		}

		if (person != null) {
			shiftType = taskSchedule.getShiftType();

			// if (shiftCache != null)
			if (shiftCache != shiftType) {
				shiftCache = shiftType;
				start = taskSchedule.getShiftStart();
				end = taskSchedule.getShiftEnd();
				
				if (shiftCache == ShiftType.OFF || shiftCache == ShiftType.ON_CALL)
					shiftTF.setText(shiftCache.toString());
				else
					shiftTF.setText(shiftCache.toString() + " : (" + start + " to " + end + ")");
			}
		}

		today = marsClock.getMissionSol();
		
		todayInteger = (Integer) today;
		selectedSol = (Integer) solBox.getSelectedItem(); 
		
		// necessary or else if (isRealTimeUpdate) below will have NPE
		
		// Update the sol combobox at the beginning of a new sol
		if (today != todayCache) {

			for (int i = 1; i < today + 1; i++) {
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

		DecimalFormat fmt = new DecimalFormat("000.00");

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
				return fmt.format(activity.getStartTime());
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
