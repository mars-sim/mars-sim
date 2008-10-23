/**
 * Mars Simulation Project
 * TradeMissionCustomInfoPanel.java
 * @version 2.85 2008-10-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.ai.mission.Mining;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionEvent;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ResourceException;
import org.mars_sim.msp.simulation.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.NumberCellRenderer;

/**
 * A panel for displaying mining mission information.
 */
public class MiningMissionCustomInfoPanel extends MissionCustomInfoPanel {

	// Data members
	private Mining mission;
	private MainDesktopPane desktop;
	private JButton luvButton;
	private ConcentrationTableModel concentrationTableModel;
	private ExcavationTableModel excavationTableModel;
	
	/**
	 * Constructor
	 * @param desktop the main desktop.
	 */
	MiningMissionCustomInfoPanel(MainDesktopPane desktop) {
		// Use JPanel constructor
		super();
		
        // Set the layout.
        setLayout(new BorderLayout());
        
		// Initialize data members.
		this.desktop = desktop;
		
		// Create LUV panel.
		JPanel luvPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(luvPane, BorderLayout.NORTH);
		
		// Create LUV label.
		JLabel luvLabel = new JLabel("Light Utility Vehicle: ");
		luvPane.add(luvLabel);
		
		// Create LUV button.
		luvButton = new JButton("   ");
		luvButton.setVisible(false);
		luvButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Open window for light utility vehicle.
				LightUtilityVehicle luv = mission.getLightUtilityVehicle();
				if (luv != null) getDesktop().openUnitWindow(luv, false);
			}
		});
		luvPane.add(luvButton);
		
		// Create center panel.
		JPanel centerPane = new JPanel(new GridLayout(2, 1));
		add(centerPane, BorderLayout.CENTER);
		
		// Create concentration panel.
		JPanel concentrationPane = new JPanel(new BorderLayout());
		centerPane.add(concentrationPane);
		
		// Create concentration label.
		JLabel concentrationLabel = new JLabel("Estimated Mineral Concentrations at Site:");
		concentrationPane.add(concentrationLabel, BorderLayout.NORTH);
		
		// Create concentration scroll panel.
		JScrollPane concentrationScrollPane = new JScrollPane();
		concentrationScrollPane.setPreferredSize(new Dimension(-1, -1));
		concentrationPane.add(concentrationScrollPane, BorderLayout.CENTER);
		
		// Create concentration table.
		concentrationTableModel = new ConcentrationTableModel();
		JTable concentrationTable = new JTable(concentrationTableModel);
		concentrationScrollPane.setViewportView(concentrationTable);
		
		// Create excavation panel.
		JPanel excavationPane = new JPanel(new BorderLayout());
		centerPane.add(excavationPane);
		
		// Create excavation label.
		JLabel excavationLabel = new JLabel("Minerals Excavated at Site:");
		excavationPane.add(excavationLabel, BorderLayout.NORTH);
		
		// Create excavation scroll panel.
		JScrollPane excavationScrollPane = new JScrollPane();
		excavationScrollPane.setPreferredSize(new Dimension(-1, -1));
		excavationPane.add(excavationScrollPane, BorderLayout.CENTER);
		
		// Create excavation tabel.
		excavationTableModel = new ExcavationTableModel();
		JTable excavationTable = new JTable(excavationTableModel);
		excavationTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2));
		excavationScrollPane.setViewportView(excavationTable);
	}
	
    /**
     * Gets the main desktop.
     * @return desktop.
     */
    private MainDesktopPane getDesktop() {
    	return desktop;
    }

	@Override
	public void updateMission(Mission mission) {
		if (mission instanceof Mining) {
			this.mission = (Mining) mission;
			updateLUVButton();
			concentrationTableModel.updateTable();
			excavationTableModel.updateTable();
		}
	}

	@Override
	public void updateMissionEvent(MissionEvent e) {
		if (e.getType().equals(Mining.EXCAVATE_MINERALS_EVENT) || 
				e.getType().equals(Mining.COLLECT_MINERALS_EVENT))
			excavationTableModel.updateTable();
	}
	
	/**
	 * Updates the LUV button.
	 */
	private void updateLUVButton() {
		LightUtilityVehicle luv = mission.getLightUtilityVehicle();
		luvButton.setText(luv.getName());
		luvButton.setVisible(true);
	}
	
	/**
	 * Concentration table model.
	 */
	private class ConcentrationTableModel extends AbstractTableModel {
		
		// Data members.
		protected Map<String, Double> concentrationMap;
    	
    	/**
    	 * Constructor
    	 */
    	private ConcentrationTableModel() {
    		// Use AbstractTableModel constructor.
    		super();
    		
    		// Initialize concentration map.
    		concentrationMap = new HashMap<String, Double>();
    	}
    	
    	/**
    	 * Returns the number of rows in the model.
    	 * @return number of rows.
    	 */
    	public int getRowCount() {
    		return concentrationMap.size();
    	}

    	/**
    	 * Returns the number of columns in the model.
    	 * @return number of columns.
    	 */
    	public int getColumnCount() {
    		return 2;
    	}
    	
    	@Override
    	public String getColumnName(int columnIndex) {
    		if (columnIndex == 0) return "Mineral";
    		else return "Concentration %";
        }
    	
    	/**
    	 * Returns the value for the cell at columnIndex and rowIndex.
    	 * @param row the row whose value is to be queried.
    	 * @param column the column whose value is to be queried.
    	 * @return the value Object at the specified cell.
    	 */
    	public Object getValueAt(int row, int column) {
    		Object result = "unknown";
    		
            String[] minerals = concentrationMap.keySet().toArray(
            		new String[concentrationMap.size()]);
            if ((row >= 0) && (row < minerals.length)) { 
            	if (column == 0) result = minerals[row];
            	else result = concentrationMap.get(minerals[row]).intValue();
            }
            
            return result;
        }
    	
    	/**
    	 * Updates the table data.
    	 */
    	private void updateTable() {
    		if (mission.getMiningSite() != null)
    			concentrationMap = mission.getMiningSite().getEstimatedMineralConcentrations();
    		else concentrationMap.clear();
    		fireTableDataChanged();	
    	}
	}
	
	/**
	 * Excavation table model.
	 */
	private class ExcavationTableModel extends AbstractTableModel {
		
		// Data members.
		protected Map<AmountResource, Double> excavationMap;
    	
    	/**
    	 * Constructor
    	 */
    	private ExcavationTableModel() {
    		// Use AbstractTableModel constructor.
    		super();
    		
    		// Initialize excavation map.
    		excavationMap = new HashMap<AmountResource, Double>();
    	}
    	
    	/**
    	 * Returns the number of rows in the model.
    	 * @return number of rows.
    	 */
    	public int getRowCount() {
    		return excavationMap.size();
    	}

    	/**
    	 * Returns the number of columns in the model.
    	 * @return number of columns.
    	 */
    	public int getColumnCount() {
    		return 2;
    	}
    	
    	@Override
    	public String getColumnName(int columnIndex) {
    		if (columnIndex == 0) return "Mineral";
    		else return "Excavated (kg)";
        }
    	
    	@Override
        public Class<?> getColumnClass(int columnIndex) {
            Class dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = String.class;
            else if (columnIndex == 1) dataType = Double.class;
            return dataType;
        }
    	
    	/**
    	 * Returns the value for the cell at columnIndex and rowIndex.
    	 * @param row the row whose value is to be queried.
    	 * @param column the column whose value is to be queried.
    	 * @return the value Object at the specified cell.
    	 */
    	public Object getValueAt(int row, int column) {
    		Object result = "unknown";
    		
            AmountResource[] minerals = excavationMap.keySet().toArray(
            		new AmountResource[excavationMap.size()]);
            if ((row >= 0) && (row < minerals.length)) { 
            	if (column == 0) result = minerals[row];
            	else result = excavationMap.get(minerals[row]);
            }
            
            return result;
        }
    	
    	/**
    	 * Updates the table data.
    	 */
    	private void updateTable() {
    		excavationMap.clear();
    		String[] mineralNames = Simulation.instance().getMars().getSurfaceFeatures().
    				getMineralMap().getMineralTypeNames();
    		for (int x = 0; x < mineralNames.length; x++) {
    			try {
    				AmountResource mineral = AmountResource.findAmountResource(mineralNames[x]);
    				double amount = mission.getTotalMineralExcavatedAmount(mineral);
    				if (amount > 0D) excavationMap.put(mineral, amount);
    			}
    			catch (ResourceException e) {}
    		}
    		
    		fireTableDataChanged();	
    	}
	}
}