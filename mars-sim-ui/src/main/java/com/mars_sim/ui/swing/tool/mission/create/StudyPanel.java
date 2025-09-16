/**
 * Mars Simulation Project
 * StudyPanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.ui.swing.MarsPanelBorder;

/**
 * A wizard panel to select a scientific study for the mission.
 */
@SuppressWarnings("serial")
public class StudyPanel extends WizardPanel {

	// The wizard panel name.
	private static final String NAME = "Scientific Study";
	private static final String CANT = "Note : not valid for this mission";

	// Data members.
	private StudyTableModel studyTableModel;
	private JTable studyTable;
	private JLabel errorMessageLabel;

	public StudyPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);

		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Set the border.
		setBorder(new MarsPanelBorder());

		// Create the select study label.
		JLabel selectStudyLabel = createTitleLabel("Select a scientific study.");
		add(selectStudyLabel);

		// Create the study panel.
		JPanel studyPane = new JPanel(new BorderLayout(0, 0));
		studyPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
		studyPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(studyPane);

		// Create scroll panel for study list.
		JScrollPane studyScrollPane = new JScrollPane();
		studyPane.add(studyScrollPane, BorderLayout.CENTER);

		// Create the study table model.
		ScienceType studyScience = null;
		MissionType type = getWizard().getMissionData().getMissionType();
		if (MissionType.AREOLOGY == type) 
			studyScience = ScienceType.AREOLOGY;
		else if (MissionType.BIOLOGY == type) 
			studyScience = ScienceType.ASTROBIOLOGY;
		else if (MissionType.METEOROLOGY == type) 
			studyScience = ScienceType.ASTROBIOLOGY;
		studyTableModel = new StudyTableModel(studyScience);

		// Create the study table.
		studyTable = new JTable(studyTableModel);
		studyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/** default serial id. */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
            	// Clear the background from previous error cell
        		super.setBackground(null); 
				JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// If failure cell, mark background red.
				if (studyTableModel.isFailureCell(row, column))
					l.setBackground(Color.RED);

				return this;
			}
		});
		studyTable.setRowSelectionAllowed(true);
		studyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		studyTable.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) {
				int index = studyTable.getSelectedRow();
				if (index > -1) {
					if (studyTableModel.isFailureRow(index)) {
						errorMessageLabel.setText(CANT);
						getWizard().setButtons(false);
					} else {
						errorMessageLabel.setText(" ");
						getWizard().setButtons(true);
					}
				}
			}
		});
		studyTable.setPreferredScrollableViewportSize(studyTable.getPreferredSize());
		studyScrollPane.setViewportView(studyTable);

		// Create the error message label.
		errorMessageLabel = createErrorLabel();
		add(errorMessageLabel);

		// Add a vertical glue.
		add(Box.createVerticalGlue());
	}

	@Override
	void clearInfo() {
		studyTable.clearSelection();
		errorMessageLabel.setText(" ");
	}

	/**
	 * Commits changes from this wizard panel.
	 * 
	 * @param isTesting true if it's only testing conditions
	 * @return true if changes can be committed.
	 */
	@Override
	boolean commitChanges(boolean isTesting) {
		int selectedIndex = studyTable.getSelectedRow();
		ScientificStudy selectedStudy = studyTableModel.getStudy(selectedIndex);
		getWizard().getMissionData().setScientificStudy(selectedStudy);
		return true;
	}

	@Override
	String getPanelName() {
		return NAME;
	}

	@Override
	void updatePanel() {
		studyTableModel.updateTable();
		studyTable.setPreferredScrollableViewportSize(studyTable.getPreferredSize());
	}

	/**
	 * A table model for scientific studies.
	 */
	private static class StudyTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members
		private ScienceType studyScience;
		private String scienceName;
		private List<ScientificStudy> studies;

		private static ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();

		/**
		 * Constructor.
		 */
		private StudyTableModel(ScienceType studyScience) {
			// Use AbstractTableModel constructor.
			super();

			this.studyScience = studyScience;
			// Add all ongoing scientific studies to table sorted by name.
			studies = manager.getAllStudies(false);
			Collections.sort(studies);

			scienceName = studyScience.getName().substring(0, 1).toUpperCase() + studyScience.getName().substring(1);
		}

		/**
		 * Gets the scientific study in the table at a given index.
		 * 
		 * @param index the index of the study.
		 * @return study.
		 */
		private ScientificStudy getStudy(int index) {
			ScientificStudy result = null;
			if ((index >= 0) && (index < studies.size()))
				result = studies.get(index);
			return result;
		}

		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return number of columns.
		 */
		public int getColumnCount() {
			return 3;
		}

		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return number of rows.
		 */
		public int getRowCount() {
			return studies.size();
		}

		/**
		 * Returns the name of the column at columnIndex.
		 * 
		 * @param columnIndex the column index.
		 * @return column name.
		 */
		@Override
		public String getColumnName(int columnIndex) {
			String result = null;
			if (columnIndex == 0)
				result = "Study";
			else if (columnIndex == 1)
				result = "Phase";
			else if (columnIndex == 2)
				result = scienceName + " Researchers";
			return result;
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param row    the row whose value is to be queried
		 * @param column the column whose value is to be queried
		 * @return the value Object at the specified cell
		 */
		public Object getValueAt(int row, int column) {
			Object result = null;

			if (row < studies.size()) {
				ScientificStudy study = studies.get(row);

				if (column == 0)
					result = study.getName();
				else if (column == 1)
					result = study.getPhase();
				else if (column == 2)
					result = getScienceResearcherNum(study);
			}

			return result;
		}

		/**
		 * Gets the number of researchers for a particular science in a study.
		 * 
		 * @param study the scientific study.
		 * @return number of researchers.
		 */
		private int getScienceResearcherNum(ScientificStudy study) {
			int result = 0;

			if (study.getScience().equals(studyScience))
				result++;
			return result;
		}

		/**
		 * Updates the table data.
		 */
		void updateTable() {
			// Add all ongoing scientific studies to table sorted by name.
			studies = manager.getAllStudies(false);
			Collections.sort(studies);

			fireTableStructureChanged();
		}

		/**
		 * Checks if a table cell is a failure cell.
		 * 
		 * @param row    the table row.
		 * @param column the table column.
		 * @return true if cell is a failure cell.
		 */
		boolean isFailureCell(int row, int column) {
			boolean result = false;
			ScientificStudy study = studies.get(row);

			if (column == 1) {
				if (StudyStatus.RESEARCH_PHASE != study.getPhase())
					result = true;
			} else if ((column == 2) && (getScienceResearcherNum(study) == 0)) {
					result = true;
			}

			return result;
		}

		/**
		 * Checks if row contains a failure cell.
		 * 
		 * @param row the row index.
		 * @return true if row has failure cell.
		 */
		boolean isFailureRow(int row) {
			boolean result = false;
			for (int x = 0; x < getColumnCount(); x++) {
				if (isFailureCell(row, x))
					result = true;
			}
			return result;
		}
	}
}
