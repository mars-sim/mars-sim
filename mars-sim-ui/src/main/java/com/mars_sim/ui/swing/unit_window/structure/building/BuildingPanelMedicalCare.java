/*
 * Mars Simulation Project
 * BuildingPanelMedicalCare.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.structure.building.function.MedicalCare;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityModel;


/**
 * The MedicalCareBuildingPanel class is a building function panel representing
 * the medical info of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelMedicalCare extends TabPanelTable {

	private static final String MEDICAL_ICON = "medical";

	// Data members
	/** The medical care. */
	private MedicalCare medical;
	/** Label of number of physicians. */
	private JLabel physicianLabel;
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
			ImageLoader.getIconByName(MEDICAL_ICON),
			Msg.getString("BuildingPanelMedicalCare.title"), 
			desktop
		);

		// Initialize data members
		this.medical = medical;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected JPanel createInfoPanel() {

		// Create label panel
		AttributePanel labelPanel = new AttributePanel(2);
		
		// Create sick bed label
		labelPanel.addTextField(Msg.getString("BuildingPanelMedicalCare.numberOfsickBeds"),
					 				Integer.toString(medical.getSickBedNum()), null);

		// Create physician label
		physicianCache = medical.getPhysicianNum();
		physicianLabel = labelPanel.addTextField(Msg.getString("BuildingPanelMedicalCare.numberOfPhysicians"),
									  Integer.toString(physicianCache), null);
		return labelPanel;
	}

	@Override
	protected TableModel createModel() {
		// Prepare medical table model
		medicalTableModel = new MedicalTableModel(medical);
		return medicalTableModel;
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
	private static class MedicalTableModel extends AbstractTableModel
				implements EntityModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private MedicalCare medical;
		private java.util.List<HealthProblem> healthProblems;

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

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = String.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Patient";
			else if (columnIndex == 1) return "Condition";
			else return "unknown";
		}

		public Object getValueAt(int row, int column) {

			HealthProblem problem = healthProblems.get(row);

			if (column == 0) return problem.getSufferer().getName();
			else if (column == 1) return problem.toString();
			else return "unknown";
		}

		public void update() {
			if (!healthProblems.equals(medical.getProblemsBeingTreated()))
				healthProblems = medical.getProblemsBeingTreated();

			fireTableDataChanged();
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			HealthProblem problem = healthProblems.get(row);
			return problem.getSufferer();
		}
	}
}
