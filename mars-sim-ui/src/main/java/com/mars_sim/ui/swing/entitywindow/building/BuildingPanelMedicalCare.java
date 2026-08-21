/*
 * Mars Simulation Project
 * BuildingPanelMedicalCare.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.MedicalCare;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityModel;


/**
 * The MedicalCareBuildingPanel class is a building function panel representing
 * the medical info of a settlement building.
 */
public class BuildingPanelMedicalCare extends EntityTableTabPanel<Building> implements TemporalComponent {

	private static final String MEDICAL_ICON = "medical";
	
	// Data members
	/** The medical care. */
	private MedicalCare medical;
	/** Table of medical info. */
	private MedicalTableModel medicalTableModel;

	/** Label of number of physicians. */
	private JLabel physicianLabel;
	/** Label of number of patient. */
	private JLabel patientLabel;
	/** Label of number of beds in use. */
	private JLabel bedInUseLabel;
	
	// Data cache
	/** Cache of number of physicians. */
	private int physicianCache;
	/** Cache of number of patients. */
	private int patientCache;
	/** Cache of number of beds in use. */
	private int bedInUseCache;
	
	
	/**
	 * Constructor.
	 * 
	 * @param medical the medical care building this panel is for.
	 * @param context the UI context
	 */
	public BuildingPanelMedicalCare(MedicalCare medical, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelMedicalCare.title"), 
			ImageLoader.getIconByName(MEDICAL_ICON),
			Msg.getString("BuildingPanelMedicalCare.title"), 
			medical.getBuilding(), context
		);

		// Initialize data members
		this.medical = medical;
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected JPanel createInfoPanel() {

		// Create label panel
		AttributePanel labelPanel = new AttributePanel();
		
		// Create sick bed label
		labelPanel.addTextField(Msg.getString("BuildingPanelMedicalCare.BedCapacity"),
					 				Integer.toString(medical.getBedCapacity()), null);
		
		bedInUseCache = medical.getBedInUse();
		bedInUseLabel = labelPanel.addTextField(Msg.getString("BuildingPanelMedicalCare.numberOfBedsInUse"),
									  Integer.toString(bedInUseCache), null);
		
		// Create physician label
		physicianCache = medical.getPhysicianNum();
		physicianLabel = labelPanel.addTextField(Msg.getString("BuildingPanelMedicalCare.numberOfPhysicians"),
									  Integer.toString(physicianCache), null);
		
		patientCache = medical.getPatientNum();
		patientLabel = labelPanel.addTextField(Msg.getString("BuildingPanelMedicalCare.numberOfPatients"),
									  Integer.toString(patientCache), null);

		return labelPanel;
	}

	@Override
	protected TableModel createModel() {
		// Prepare medical table model
		medicalTableModel = new MedicalTableModel(medical);
		
		return medicalTableModel;
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {

		// Update physician label
		if (physicianCache != medical.getPhysicianNum()) {
			physicianCache = medical.getPhysicianNum();
			physicianLabel.setText(Integer.toString(physicianCache));
		}

		// Update patient label
		if (patientCache != medical.getPatientNum()) {
			patientCache = medical.getPatientNum();
			patientLabel.setText(Integer.toString(patientCache));
		}
		
		// Update beds in use label
		if (bedInUseCache != medical.getBedInUse()) {
			bedInUseCache = medical.getBedInUse();
			bedInUseLabel.setText(Integer.toString(bedInUseCache));
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
			healthProblems.addAll(medical.getProblemsAwaitingTreatment());
		}

		public int getRowCount() {
			return healthProblems.size();
		}

		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> String.class;
				case 1 -> String.class;
//				case 2 -> Boolean.class;
				default -> Object.class;
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> "Patient";
				case 1 -> "Condition";
//				case 2 -> "Have Bed";
				default -> "unknown";
			};
		}

		@Override
		public Object getValueAt(int row, int column) {

			HealthProblem problem = healthProblems.get(row);

			return switch (column) {
				case 0 -> problem.getSufferer().getName();
				case 1 -> problem.toString();
//				case 2 -> medical.doesPatientHaveBed(problem.getSufferer());
				default -> "unknown";
			};
		}

		public void update() {
			healthProblems = medical.getProblemsBeingTreated();
			healthProblems.addAll(medical.getProblemsAwaitingTreatment());
			
			fireTableDataChanged();
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			HealthProblem problem = healthProblems.get(row);
			return problem.getSufferer();
		}
	}
}
