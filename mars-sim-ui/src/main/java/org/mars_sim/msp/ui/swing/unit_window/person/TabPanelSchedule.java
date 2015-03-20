/**
 * Mars Simulation Project
 * TabPanelFavorite.java
 * @version 3.08 2015-03-19 

 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.TaskSchedule;
import org.mars_sim.msp.core.person.TaskSchedule.DailyTask;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelSchedule is a tab panel showing the daily schedule a person.
 */
public class TabPanelSchedule
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private ScheduleTableModel scheduleTableModel;
	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelSchedule(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSchedule.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelSchedule.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Person person = (Person) unit;

		// Create label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Prepare label
		JLabel label = new JLabel(Msg.getString("TabPanelSchedule.label"), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(label);

		// Prepare info panel.
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		//infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Prepare main dish name label
		JLabel scheduleLabel = new JLabel(Msg.getString("TabPanelSchedule.yesterday"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(scheduleLabel);

		// Create schedule table model
		scheduleTableModel = new ScheduleTableModel(person);

		// Create attribute scroll panel
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(scrollPanel);

		// Create schedule table
		JTable table = new JTable(scheduleTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setPreferredWidth(70);
		table.getColumnModel().getColumn(1).setPreferredWidth(70);
		table.setCellSelectionEnabled(false);
		// table.setDefaultRenderer(Integer.class, new NumberCellRenderer());
		scrollPanel.setViewportView(table);

	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		scheduleTableModel.update();
	}
	

	/** 
	 * Internal class used as model for the attribute table.
	 */
	private static class ScheduleTableModel
	extends AbstractTableModel {

		//private List<Map<String,NaturalAttribute>> attributes;

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private TaskSchedule taskSchedule;
		private List<DailyTask> tasks;
		
		DecimalFormat fmt = new DecimalFormat("0000"); 

		/**
		 * hidden constructor.
		 * @param person {@link Person}
		 */
		private ScheduleTableModel(Unit unit) {
	        Person person = null;
	        Robot robot = null;     
	        if (unit instanceof Person) {
	         	person = (Person) unit;  
	         	taskSchedule = person.getTaskSchedule();
	        }
	        else if (unit instanceof Robot) {
	        	robot = (Robot) unit;
	        	//taskSchedule = robot.getTaskSchedule();
	        }
			
	        // Obtain all schedules
	        Map <Integer, List<DailyTask>> schedules = taskSchedule.getSchedules();
	       
	        // TODO: show weekly schedule?
	        
	        int sol = taskSchedule.getSolCache();
	        if (sol > 1)
		        // For now, pick only yesterday's schedule
	        	tasks = schedules.get(sol-1);
	        else 
	        	tasks = null;
			
		}

		@Override
		public int getRowCount() {
			if (tasks != null)
				return tasks.size();
			else 
				return 0;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			if (columnIndex == 1) dataType = String.class;
			//if (columnIndex == 2) dataType = String.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelSchedule.column.time"); //$NON-NLS-1$
			//else if (columnIndex == 1) return Msg.getString("TabPanelSchedule.column.task"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelSchedule.column.activity"); //$NON-NLS-1$
			else return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) return fmt.format(tasks.get(row).getStartTime());
			//else if (column == 1) return tasks.get(row).getTaskName();
			else if (column == 1) return tasks.get(row).getDoAction();
			else return null;
		}
		
		public void update() {
	        int sol = taskSchedule.getSolCache();			
			if (sol > 1) {
				Map <Integer, List<DailyTask>> schedules = taskSchedule.getSchedules();
		        // Obtain yesterday's schedule
				tasks = schedules.get(sol-1);
	        	fireTableDataChanged();
			}
		}
		 

	}
	
}