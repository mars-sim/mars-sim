/**
 * Mars Simulation Project
 * FinishedStudyListPanel.java
 * @version 3.1.0 2017-01-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.science;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

/**
 * A panel showing a selectable list of finished scientific studies.
 */
public class FinishedStudyListPanel extends JPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private ScienceWindow scienceWindow;
	private StudyTableModel studyTableModel;
	private JTable studyTable;
	private JScrollPane listScrollPane;

	private static ScientificStudyManager scientificStudyManager = Simulation.instance().getScientificStudyManager();

	/**
	 * Constructor
	 * 
	 * @param scienceWindow the science window.
	 */
	FinishedStudyListPanel(ScienceWindow scienceWindow) {
		// Use JPanel constructor.
		super();

		this.scienceWindow = scienceWindow;

		setLayout(new BorderLayout());

		JLabel titleLabel = new JLabel(Msg.getString("FinishedStudyListPanel.finishedScientificStudies"), //$NON-NLS-1$
				JLabel.CENTER);
		add(titleLabel, BorderLayout.NORTH);

		// Create list scroll pane.
		listScrollPane = new JScrollPane();
		listScrollPane.setBorder(new MarsPanelBorder());
		listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(listScrollPane, BorderLayout.CENTER);

		// Create study table model.
		studyTableModel = new StudyTableModel();

		// Create study table.
		studyTable = new JTable(studyTableModel);
		studyTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
		studyTable.setCellSelectionEnabled(false);
		studyTable.setRowSelectionAllowed(true);
		studyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		studyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting()) {
					int row = studyTable.getSelectedRow();
					if (row >= 0) {
						ScientificStudy selectedStudy = studyTableModel.getStudy(row);
						if (selectedStudy != null)
							setSelectedScientificStudy(selectedStudy);
					}
				}
			}
		});
		studyTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		studyTable.getColumnModel().getColumn(1).setPreferredWidth(7);
		studyTable.getColumnModel().getColumn(2).setPreferredWidth(80);
		studyTable.getColumnModel().getColumn(3).setPreferredWidth(80);
		studyTable.getColumnModel().getColumn(3).setPreferredWidth(80);
		
		studyTable.setAutoCreateRowSorter(true);

		TableStyle.setTableStyle(studyTable);

		listScrollPane.setViewportView(studyTable);
	}

	/**
	 * Updates the panel.
	 */
	void update() {
		TableStyle.setTableStyle(studyTable);
		studyTableModel.update();
		// Make sure study is selected.
		selectScientificStudy(scienceWindow.getScientificStudy(), false);
	}

	/**
	 * Sets the selected scientific study in table.
	 * 
	 * @param study the scientific study.
	 */
	void setSelectedScientificStudy(ScientificStudy study) {
		scienceWindow.setScientificStudy(study);
	}

	/**
	 * Selects a scientific study.
	 * 
	 * @param study           the scientific study.
	 * @param scrollSelection true if table should be scrolled so selected row is
	 *                        visible.
	 */
	void selectScientificStudy(ScientificStudy study, boolean scrollSelection) {
		int studyIndex = studyTableModel.getStudyIndex(study);
		int currentSelectedRow = studyTable.getSelectedRow();
		if (studyIndex != currentSelectedRow) {
			if (studyIndex >= 0) {
				studyTable.getSelectionModel().setSelectionInterval(studyIndex, studyIndex);

				if (scrollSelection) {
					Rectangle cellRect = studyTable.getCellRect(studyIndex, 0, true);
					listScrollPane.getViewport().setViewPosition(cellRect.getLocation());
				}
			} else
				studyTable.clearSelection();
		}
	}

	/**
	 * The study table model inner class.
	 */
	private static class StudyTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private List<ScientificStudy> studies;

		/**
		 * Constructor
		 */
		private StudyTableModel() {
			studies = scientificStudyManager.getCompletedStudies();
		}

		/**
		 * Returns the number of columns in the model. A JTable uses this method to
		 * determine how many columns it should create and display by default.
		 * 
		 * @return the number of columns in the model
		 */
		public int getColumnCount() {
			return 5;
		}

		@Override
		public String getColumnName(int column) {
			String result = new String();
			if (column == 0)
				result = Msg.getString("FinishedStudyListPanel.column.study"); //$NON-NLS-1$
			else if (column == 1) 
				result = Msg.getString("FinishedStudyListPanel.column.level"); //$NON-NLS-1$
			else if (column == 2)
				result = Msg.getString("FinishedStudyListPanel.column.phase"); //$NON-NLS-1$
			else if (column == 3)
				result = Msg.getString("FinishedStudyListPanel.column.researcher"); //$NON-NLS-1$
			else if (column == 4)
				result = Msg.getString("FinishedStudyListPanel.column.settlement"); //$NON-NLS-1$
			return result;
		}

		/**
		 * Returns the number of rows in the model. A JTable uses this method to
		 * determine how many rows it should display. This method should be quick, as it
		 * is called frequently during rendering.
		 * 
		 * @return the number of rows in the model
		 */
		public int getRowCount() {
			return studies.size();
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param rowIndex    the row whose value is to be queried
		 * @param columnIndex the column whose value is to be queried
		 * @return the value Object at the specified cell
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			String result = new String();
			if ((rowIndex >= 0) && (rowIndex < studies.size())) {
				ScientificStudy study = studies.get(rowIndex);
				String researcherN = "";	
				String settlementN = "";
				if (study.getPrimaryResearcher() != null) {
					researcherN = study.getPrimaryResearcher().getName();
//					System.out.println("Researcher : " + researcherN);
					
					if (study.getPrimarySettlement() != null) {
						settlementN = study.getPrimarySettlement().getName();
					}
				}
				
				if (columnIndex == 0) 
					result = Conversion.capitalize(study.getScienceName());
				else if (columnIndex == 1) 
					result = study.getDifficultyLevel() + "";
				else if (columnIndex == 2) 
					result = Conversion.capitalize(study.getPhase());
				else if (columnIndex == 3)
					result = Conversion.capitalize(researcherN);
				else if (columnIndex == 4)
					result = Conversion.capitalize(settlementN);
			}
			return result;
		}

		/**
		 * Updates the table model.
		 */
		private void update() {
			List<ScientificStudy> newStudies = scientificStudyManager.getCompletedStudies();
			if (!studies.equals(newStudies))
				studies = newStudies;
			fireTableDataChanged();
		}

		/**
		 * Gets the scientific study at a given row index.
		 * 
		 * @param row the row index.
		 * @return the study at the row index or null if none.
		 */
		private ScientificStudy getStudy(int row) {
			ScientificStudy result = null;

			if ((row >= 0) && (row < studies.size())) {
				result = studies.get(row);
			}

			return result;
		}

		/**
		 * Gets the row index of a given scientific study.
		 * 
		 * @param study the scientific study.
		 * @return the row index of the study or -1 if not in table.
		 */
		private int getStudyIndex(ScientificStudy study) {
			int result = -1;

			if (studies.contains(study))
				result = studies.indexOf(study);

			return result;
		}
	}
}