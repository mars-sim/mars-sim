/*
 * Mars Simulation Project
 * TabPanelLog.java
 * @date 2023-01-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.data.MSolDataItem;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;


@SuppressWarnings("serial")
public class TabPanelLog extends TabPanel {

	private static final String LOG_ICON = "log"; //$NON-NLS-1$

	private static final String SOL = "   Sol ";
	
	// Data members
	private Integer selectedSol;
	private Integer todayInteger;
		
	private JTable table;
	private JComboBoxMW<Integer> solBox;
	
	private JTextField odometerTF;
	private JTextField maintTF;
	
	private ScheduleTableModel scheduleTableModel;
	
	/** The Vehicle instance. */
	private Vehicle vehicle;
	
	public TabPanelLog(Vehicle vehicle, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super(
			Msg.getString("TabPanelLog.title"),
			ImageLoader.getIconByName(LOG_ICON),
			Msg.getString("TabPanelLog.title"), //$NON-NLS-1$
			desktop
		);
		
		this.vehicle = vehicle;

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void buildUI(JPanel content) {
		
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		
        // Create spring layout dataPanel
        JPanel springPanel = new JPanel(new SpringLayout());
        northPanel.add(springPanel);

		odometerTF = addTextField(springPanel, Msg.getString("TabPanelLog.label.odometer"),
								  StyleManager.DECIMAL_PLACES2.format(vehicle.getOdometerMileage()), 8, null);

		maintTF = addTextField(springPanel, Msg.getString("TabPanelLog.label.maintDist"),
				  StyleManager.DECIMAL_PLACES2.format(vehicle.getDistanceLastMaintenance()), 8, null);

	    // Lay out the spring panel.
	    SpringUtilities.makeCompactGrid(springPanel,
	     		                               2, 2, //rows, cols
	     		                               50, 10,        //initX, initY
	    		                               7, 7);       //xPad, yPad     	
		
		todayInteger = getMarsClock().getMissionSol();

		ComboBoxModel<Integer> solModel = createSolModel(todayInteger);

		// Create comboBox
		solBox = new JComboBoxMW<>(solModel);
		solBox.setPreferredSize(new Dimension(80, 25));
		solBox.setPrototypeDisplayValue(new Dimension(80, 25));
		solBox.setWide(true);
		
		solBox.setRenderer(new PromptComboBoxRenderer());
		solBox.setMaximumRowCount(7);
				
		JPanel solPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		solPanel.add(solBox);
        northPanel.add(solPanel);
				
		Box box = Box.createHorizontalBox();
		northPanel.add(box);
		
        content.add(northPanel, BorderLayout.NORTH);

		box.add(Box.createHorizontalGlue());

		// Create schedule table model
		scheduleTableModel = new ScheduleTableModel();

		// Create attribute scroll panel
		JScrollPane scrollPanel = new JScrollPane();
		content.add(scrollPanel, BorderLayout.CENTER);

		// Create schedule table
		table = new JTable(scheduleTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.setRowSelectionAllowed(true);

		scrollPanel.setViewportView(table);

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer);

		solBox.setSelectedItem(todayInteger);
		solBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSolDisplayed((Integer) solBox.getSelectedItem());
			}
		});
		selectedSol = todayInteger;

		// Update will refresh data
		update();
		
	}

	/**
	 * Build a combox box modle that hold sthe Sols in the vehcile log
	 * @return
	 */
	private ComboBoxModel<Integer> createSolModel(Integer today) {

		// Create comboBoxModel by takign the sols from the vehicle log.
		DefaultComboBoxModel<Integer> solModel = new DefaultComboBoxModel<Integer>();

		// Using internal iterator in lambda expression
		for(int s = today; s > 0; s--) {
			solModel.addElement(s);
		}

		return solModel;
	}

	protected void setSolDisplayed(Integer selectedItem) {
		selectedSol = selectedItem;
		update();
	}

	/**
	 * Gets the mars clock instance.
	 * 
	 * @return
	 */
	private MarsClock getMarsClock() {
		return getSimulation().getMasterClock().getMarsClock();
	}


	@SuppressWarnings("unchecked")
	@Override
	public void update() {

		// Update the odometer reading
		odometerTF.setText(StyleManager.DECIMAL_PLACES2.format(vehicle.getOdometerMileage()));
				
		// Update distance last maintenance 
		maintTF.setText(StyleManager.DECIMAL_PLACES2.format(vehicle.getDistanceLastMaintenance()));
				
		int currentDay = getMarsClock().getMissionSol();

		// Update the sol combobox at the beginning of a new sol
		if (!todayInteger.equals(currentDay)) {
	

			// Update the solList comboBox
			solBox.setModel(createSolModel(currentDay));
			solBox.setRenderer(new PromptComboBoxRenderer());
			solBox.setMaximumRowCount(7);
			
			// If SelectedSol is the previous current day; then automatically advance to ne day
			if (selectedSol.equals(todayInteger)) {
				selectedSol = currentDay;
			}
			solBox.setSelectedItem(selectedSol);
			todayInteger = currentDay;
		}
		
		List<MSolDataItem<Set<StatusType>>> solStatus = vehicle.getVehicleLog().get(selectedSol);
		scheduleTableModel.update(solStatus);

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
				return this;
			}

			setText(SOL + value);// + WHITESPACES);

			return c;
		}
	}
	
	/**
	 * Internal class used as model for the attribute table.
	 */
	private class ScheduleTableModel extends AbstractTableModel {

		private List<MSolDataItem<Set<StatusType>>> oneDayStatuses;
		
		/**
		 * hidden constructor.
		 * 
		 * @param person {@link Person}
		 */
		ScheduleTableModel() {
		}

		@Override
		public int getRowCount() {
			if (oneDayStatuses != null)
				return oneDayStatuses.size();
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
			if (columnIndex == 0)
				dataType = Integer.class;
			if (columnIndex == 1)
				dataType = String.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return Msg.getString("TabPanelLog.column.time"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("TabPanelLog.column.status"); //$NON-NLS-1$
			else
				return null;
		}

		@Override
		public Object getValueAt(int row, int column) {	
			if (oneDayStatuses != null 
					&& !oneDayStatuses.isEmpty()) {
				MSolDataItem<Set<StatusType>> item = oneDayStatuses.get(row);
				if (column == 0) {
					return StyleManager.DECIMAL_PLACES0.format(item.getMsol());
				} 
				else if (column == 1) {
					return printStatusTypes(item.getData());
				}
			}

			return column;
		}

		/**
		 * Prints a string list of status types
		 * @param statusTypes the set of status types
		 * @return
		 */
		public String printStatusTypes(Set<StatusType> statusTypes) {
			String s = Conversion.capitalize(statusTypes.toString());
			return s.substring(1 , s.length() - 1).toLowerCase();
		}
		
		/**
		 * Prepares a list of activities done on the selected day
		 * @param solStatus
		 */
		public void update(List<MSolDataItem<Set<StatusType>>> solStatus) {
				
			oneDayStatuses = solStatus;

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

		solBox = null;
		table = null;
		scheduleTableModel = null;
	}	
}
