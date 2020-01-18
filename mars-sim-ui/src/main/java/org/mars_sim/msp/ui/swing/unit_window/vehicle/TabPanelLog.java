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
import javax.swing.SpringLayout;
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
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;

@SuppressWarnings("serial")
public class TabPanelLog extends TabPanel {

	private static final String SOL = "   Sol ";
	private static final String WHITESPACES = "   ";
	
	// Data members
	private int selectedSolCache;
	
	private Integer selectedSol;
	private Integer todayInteger;
	private Integer todayCache;
	
	private int theme;
	
	private JTable table;
	private JComboBoxMW<Integer> solBox;
	
	private WebTextField odometerTF;
	private WebTextField maintTF;
	
	private DefaultComboBoxModel<Integer> comboBoxModel;
	private ScheduleTableModel scheduleTableModel;

	
	private List<Integer> solList;
	private List<Integer> millisolList;
	private Map<Integer, Map<Integer, List<StatusType>>> allStatuses;
	private Map<Integer, List<StatusType>> oneDayStatuses;
	private List<StatusType> oneMillisolStatuses;
	
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Vehicle instance. */
	private Vehicle vehicle;
	
	private static MarsClock marsClock;

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

	}

	public boolean isUIDone() {
		return uiDone;
	}
	
	@SuppressWarnings("unchecked")
	public void initializeUI() {
		uiDone = true;
		
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();
		
		// Create towing label.
		WebPanel panel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelLog.title"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		panel.add(titleLabel);
		topContentPanel.add(panel, BorderLayout.NORTH);
		
        // Create spring layout dataPanel
        WebPanel springPanel = new WebPanel(new SpringLayout());
		topContentPanel.add(springPanel, BorderLayout.CENTER);
		
	    WebLabel odometerLabel = new WebLabel(Msg.getString("TabPanelLog.label.odometer"), WebLabel.RIGHT);
	    odometerLabel.setOpaque(false);
	    odometerLabel.setFont(new Font("Serif", Font.PLAIN, 12));
	    springPanel.add(odometerLabel);

		WebPanel wrapper = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
		odometerTF = new WebTextField(Math.round(vehicle.getTotalDistanceTraveled()*100.0)/100.0 + "");
		odometerTF.setEditable(false);
		odometerTF.setColumns(8);
		odometerTF.setOpaque(false);
		odometerTF.setFont(new Font("Serif", Font.PLAIN, 12));
	    wrapper.add(odometerTF);
	    springPanel.add(wrapper);
	        
	    WebLabel maintLabel = new WebLabel(Msg.getString("TabPanelLog.label.maintDist"), WebLabel.RIGHT);
	    maintLabel.setOpaque(false);
	    maintLabel.setFont(new Font("Serif", Font.PLAIN, 12));
	    springPanel.add(maintLabel);

		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEFT));
		maintTF = new WebTextField(Math.round(vehicle.getDistanceLastMaintenance()*100.0)/100.0 + "");
		maintTF.setEditable(false);
		maintTF.setColumns(8);
		maintTF.setOpaque(false);
		maintTF.setFont(new Font("Serif", Font.PLAIN, 12));
	    wrapper1.add(maintTF);
	    springPanel.add(wrapper1);
	    
	    // Lay out the spring panel.
	    SpringUtilities.makeCompactGrid(springPanel,
	     		                                2, 2, //rows, cols
	     		                               50, 10,        //initX, initY
	    		                               7, 7);       //xPad, yPad     	
		
		todayInteger = marsClock.getMissionSol();
		solList = new CopyOnWriteArrayList<Integer>();

		allStatuses = vehicle.getVehicleLog();
		oneDayStatuses = allStatuses.get(todayInteger);
		
		for (int i = 1; i < todayInteger + 1; i++) {
			if (!solList.contains(i))
				solList.add(i);
		}
		
//		for (int key : allStatuses.keySet()) {
//			solList.add(key);
//		}
//		
//		if (!solList.contains(today))
//			solList.add(today);

		// Create comboBoxModel
		Collections.sort(solList, Collections.reverseOrder());
		comboBoxModel = new DefaultComboBoxModel<Integer>();
		// Using internal iterator in lambda expression
		solList.forEach(s -> comboBoxModel.addElement(s));

		// Create comboBox
		solBox = new JComboBoxMW<>(comboBoxModel);
		solBox.setPreferredSize(new Dimension(80, 25));
		solBox.setPrototypeDisplayValue(new Dimension(80, 25));
		solBox.setSelectedItem(todayInteger);
		solBox.setWide(true);
		
		solBox.setRenderer(new PromptComboBoxRenderer());
		solBox.setMaximumRowCount(7);
				
		WebPanel solPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		solPanel.add(solBox);
		centerContentPanel.add(solPanel, BorderLayout.NORTH);
				
		Box box = Box.createHorizontalBox();
		centerContentPanel.add(box, BorderLayout.CENTER);
		
		box.add(Box.createHorizontalGlue());

//		if (solBox.getSelectedItem() == null) {
//			solBox.setSelectedItem(today);
//			selectedSol = (int) today;
//		}
//		else {
//			selectedSol = (int) solBox.getSelectedItem();
//		}
//
////		solBox.setSelectedItem((Integer) 1);
//		solBox.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				selectedSol = (int) solBox.getSelectedItem();
////				if ((int)selectedSol == today)
//			}
//		});
		
		selectedSol = (Integer) solBox.getSelectedItem();
		
		if (selectedSol == null)
			solBox.setSelectedItem(todayInteger);

		solBox.setSelectedItem((Integer) 1);
		solBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedSol = (Integer) solBox.getSelectedItem();
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


	@SuppressWarnings("unchecked")
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		int t = -1;

		if (theme != t) {
			theme = t;
			TableStyle.setTableStyle(table);
		}
		
		
		// Update the odometer reading
		odometerTF.setText(Math.round(vehicle.getTotalDistanceTraveled()*100.0)/100.0 + "");
				
		// Update distance last maintenance 
		maintTF.setText(Math.round(vehicle.getDistanceLastMaintenance()*100.0)/100.0 + "");
				
		todayInteger = marsClock.getMissionSol();

		allStatuses = vehicle.getVehicleLog();
		oneDayStatuses = allStatuses.get(todayInteger);
		
		selectedSol = (Integer) solBox.getSelectedItem(); 

		// Update the sol combobox at the beginning of a new sol
		if (todayInteger != todayCache) {
	
			for (int i = 1; i < todayInteger + 1; i++) {
				if (!solList.contains(i))
					solList.add(i);
			}
					
			Collections.sort(solList, Collections.reverseOrder());
			
//			DefaultComboBoxModel<Object> newComboBoxModel = new DefaultComboBoxModel<Object>();
//			solList.forEach(s -> newComboBoxModel.addElement(s));

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

			todayCache = todayInteger;
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
					if (oneMillisolStatuses != null) {
						int size = oneMillisolStatuses.size();
						for (int i = 0; i < size; i++) {
							StatusType t = oneMillisolStatuses.get(i);
							s = t.getName();
							if (i != size - 1)
								s += ", ";
						}
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
				
			for (int s : solList) {
				// Check if this element exist
				if (comboBoxModel.getIndexOf(s) == -1) {
					comboBoxModel.addElement(s);
				}
			}
			
			if (selectedSolCache != selectedSol) {
				selectedSolCache = selectedSol;

				if (todayInteger == (Integer) selectedSolCache) {
					// Load today's schedule
					oneDayStatuses = allStatuses.get(todayInteger);
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
