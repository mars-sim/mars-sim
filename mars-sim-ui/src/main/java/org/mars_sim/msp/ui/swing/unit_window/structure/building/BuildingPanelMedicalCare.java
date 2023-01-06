/*
 * Mars Simulation Project
 * BuildingPanelMedicalCare.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;


/**
 * The MedicalCareBuildingPanel class is a building function panel representing
 * the medical info of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelMedicalCare
extends BuildingFunctionPanel {

	private static final String MEDICAL_ICON = Msg.getString("icon.medical"); //$NON-NLS-1$

	// Data members
	/** The medical care. */
	private MedicalCare medical;
	/** Label of number of physicians. */
	private JTextField physicianLabel;
	/** Table of medical info. */
	private MedicalTableModel medicalTableModel;

	// Data cache
	/** Cache of number of physicians. */
	private int physicianCache;

	/**
	 * Constructor.
	 * @param medical the medical care building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelMedicalCare(MedicalCare medical, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelMedicalCare.title"), 
			ImageLoader.getNewIcon(MEDICAL_ICON),
			medical.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.medical = medical;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create label panel
		JPanel labelPanel = new JPanel(new GridLayout(2, 2, 5, 1));
		center.add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));
		
		// Create sick bed label
		addTextField(labelPanel, Msg.getString("BuildingPanelMedicalCare.numberOfsickBeds"),
					 medical.getSickBedNum(), 5, null);

		// Create physician label
		physicianCache = medical.getPhysicianNum();
		physicianLabel = addTextField(labelPanel, Msg.getString("BuildingPanelMedicalCare.numberOfPhysicians"),
									  physicianCache, 5, null);

		// Create scroll panel for medical table
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setPreferredSize(new Dimension(160, 80));
		center.add(scrollPanel, BorderLayout.CENTER);
	    scrollPanel.getViewport().setOpaque(false);
	    scrollPanel.getViewport().setBackground(new Color(0, 0, 0, 0));
	    scrollPanel.setOpaque(false);
	    scrollPanel.setBackground(new Color(0, 0, 0, 0));
        //scrollPanel.setBorder( BorderFactory.createLineBorder(Color.orange) );

		// Prepare medical table model
		medicalTableModel = new MedicalTableModel(medical);

		// Prepare medical table
		JTable medicalTable = new ZebraJTable(medicalTableModel);
		medicalTable.setCellSelectionEnabled(false);
		scrollPanel.setViewportView(medicalTable);
	}

	/**
	 * Update this panel
	 */
	@Override
	public void update() {

		// Update physician label
		if (physicianCache != medical.getPhysicianNum()) {
			physicianCache = medical.getPhysicianNum();
			physicianLabel.setText(Integer.toString(physicianCache));
		}

		// Update medical table model.
		medicalTableModel.update();
	}

	/**
	 * Internal class used as model for the medical table.
	 */
	private static class MedicalTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private MedicalCare medical;
		private java.util.List<?> healthProblems;

		private MedicalTableModel(MedicalCare medical) {
			this.medical = medical;
			healthProblems = medical.getProblemsBeingTreated();
		}

		public int getRowCount() {
			return healthProblems.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = String.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Patient";
			else if (columnIndex == 1) return "Condition";
			else return "unknown";
		}

		public Object getValueAt(int row, int column) {

			HealthProblem problem = (HealthProblem) healthProblems.get(row);

			if (column == 0) return problem.getSufferer().getName();
			else if (column == 1) return problem.toString();
			else return "unknown";
		}

		public void update() {
			if (!healthProblems.equals(medical.getProblemsBeingTreated()))
				healthProblems = medical.getProblemsBeingTreated();

			fireTableDataChanged();
		}
	}
}
