/**
 * Mars Simulation Project
 * TabPanelLog.java
 * @version 3.1.0 2019-09-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.vehicle;

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
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

@SuppressWarnings("serial")
public class TabPanelLog extends TabPanel {

	private static final String SOL = "   Sol ";
	private static final String WHITESPACES = "   ";
	
	// Data members
	private int today;
	private int selectedSol;
	private int selectedSolCache;
	private int theme;
	
	private JTable table;
	private JComboBoxMW<Object> solBox;
	private DefaultComboBoxModel<Object> comboBoxModel;
	private ScheduleTableModel scheduleTableModel;

	private List<Integer> solList;
	private List<Integer> millisolList;
	private Map<Integer, Map<Integer, List<StatusType>>> allStatuses;
	private Map<Integer, List<StatusType>> oneDayStatuses;
	private List<StatusType> oneMillisolStatuses;
	
	private Vehicle vehicle;
	private MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();

	public TabPanelLog(Vehicle vehicle, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super(
			Msg.getString("TabPanelLog.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelLog.tooltip"), //$NON-NLS-1$
			vehicle,
			desktop
		);
		
		this.vehicle = vehicle;
		
		// Create towing label.
		WebPanel panel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelLog.title"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		panel.add(titleLabel);
		topContentPanel.add(panel);
		
		today = marsClock.getMissionSol();
		solList = new CopyOnWriteArrayList<Integer>();

		allStatuses = vehicle.getVehicleLog();
		oneDayStatuses = allStatuses.get(today);
		
		for (int key : allStatuses.keySet()) {
			solList.add(key);
		}

//		if (!solList.contains(today))
//			solList.add(today);

		// Create comboBoxModel
		Collections.sort(solList);
		comboBoxModel = new DefaultComboBoxModel<Object>();
		// Using internal iterator in lambda expression
		solList.forEach(s -> comboBoxModel.addElement(s));

		// Create comboBox
		solBox = new JComboBoxMW<Object>(comboBoxModel);
//		solBox.setWide(true);
		solBox.setPreferredSize(new Dimension(80, 25));
		solBox.setPrototypeDisplayValue(new Dimension(80, 25));
		solBox.setRenderer(new PromptComboBoxRenderer());
		solBox.setMaximumRowCount(7);

		WebPanel solPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
//		solPanel.setMinimumSize(new Dimension(40, 15));
//		solPanel.setSize(new Dimension(100, 15));
		solPanel.add(solBox);
		centerContentPanel.add(solPanel, BorderLayout.NORTH);
				
		Box box = Box.createHorizontalBox();
		centerContentPanel.add(box, BorderLayout.CENTER);
		
		box.add(Box.createHorizontalGlue());

		if (solBox.getSelectedItem() == null) {
			solBox.setSelectedItem(today);
			selectedSol = (int) today;
		}
		else {
			selectedSol = (int) solBox.getSelectedItem();
		}

//		solBox.setSelectedItem((Integer) 1);
		solBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedSol = (int) solBox.getSelectedItem();
//				if ((int)selectedSol == today)
			}
		});
		
		// Create schedule table model
		if (unit instanceof Vehicle)
			scheduleTableModel = new ScheduleTableModel((Vehicle) unit);


		// Create attribute scroll panel
		WebScrollPane scrollPanel = new WebScrollPane();
//		scrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(scrollPanel);

		// Create schedule table
		table = new ZebraJTable(scheduleTableModel);
		TableStyle.setTableStyle(table);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.setRowSelectionAllowed(true);
		// table.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		scrollPanel.setViewportView(table);

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer);

		// SwingUtilities.invokeLater(() ->
		// ColumnResizer.adjustColumnPreferredWidths(table));

		// Added sorting
//		table.setAutoCreateRowSorter(true);

		update();
		
	}


	@Override
	public void update() {
		int t = -1;

		if (theme != t) {
			theme = t;
			TableStyle.setTableStyle(table);
		}
		
		scheduleTableModel.update();
		
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
		ScheduleTableModel(Unit unit) {
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
					&& !oneDayStatuses.isEmpty()
					&& millisolList.size() > row) {
				int msol = millisolList.get(row);
				if (column == 0) {
					return fmt.format(msol);
				} 
				else if (column == 1) {
					oneMillisolStatuses = oneDayStatuses.get(msol);
					String s = "";
					int size = oneMillisolStatuses.size();
					for (int i = 0; i < size; i++) {
						StatusType t = oneMillisolStatuses.get(i);
						s = t.getName();
						if (i != size - 1)
							s += ", ";
					}
					return s;
				}
			}

			return column;
		}

		/**
		 * Prepares a list of activities done on the selected day
		 */
		public void update() {
			today = marsClock.getMissionSol();

			allStatuses = vehicle.getVehicleLog();
		
//			solList = new ArrayList<>(allStatuses.keySet());
			
			for (int key : allStatuses.keySet()) {
				if (!solList.contains(key)) {
					solList.add(key);
				}
			}

			Collections.sort(solList);
//			solList.forEach(s -> comboBoxModel.addElement(s));
					
			for (int s : solList) {
				// Check if this element exist
				if (comboBoxModel.getIndexOf(s) == -1) {
					comboBoxModel.addElement(s);
				}
			}
			
			if (selectedSolCache != selectedSol) {
				selectedSolCache = selectedSol;

				if (today == selectedSolCache) {
					// Load today's schedule
					oneDayStatuses = allStatuses.get(today);
				} 
				
				else {
					// Load the schedule of a particular sol
					oneDayStatuses = allStatuses.get(selectedSolCache);
				}
				
				if (oneDayStatuses != null && !oneDayStatuses.isEmpty()) {
					millisolList = new ArrayList<>(oneDayStatuses.keySet());
					if (millisolList.size() > 1)
						Collections.sort(millisolList);
				}
				else
					millisolList.clear();
				
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

		oneDayStatuses = null;
		allStatuses = null;
		solBox = null;
		comboBoxModel = null;
		solList = null;
		table = null;
		scheduleTableModel = null;
	}
	
}
