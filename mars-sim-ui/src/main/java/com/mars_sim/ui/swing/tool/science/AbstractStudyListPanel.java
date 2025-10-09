/**
 * Mars Simulation Project
 * AbstractStudyListPanel.java
 * @version 3.2.0 2021-07-28
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.science;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.NumberCellRenderer;

/**
 * Class to creates a panel containing a sorted table of ScientificStudy
 * entries. Any selected row is calledback to the parent ScienceWindow.
 */
public abstract class AbstractStudyListPanel extends JPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private ScienceWindow scienceWindow;
	private StudyTableModel studyTableModel;
	private JTable studyTable;

	private JScrollPane listScrollPane;

	/**
	 * The study table model inner class.
     */
	private static class StudyTableModel extends AbstractTableModel {
	
		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private static final int NAME_COLUMN = 0;
		private static final int SCIENCE_COLUMN = NAME_COLUMN + 1;
		private static final int PHASE_COLUMN = SCIENCE_COLUMN + 1;
		private static final int PERC_COLUMN = PHASE_COLUMN + 1;
		private static final int RESEARCHER_COLUMN = PERC_COLUMN + 1;
		private static final int SETTLEMENT_COLUMN = RESEARCHER_COLUMN + 1;
		private static final int NUM_COLUMN = SETTLEMENT_COLUMN + 1;

		private String[] names = new String[NUM_COLUMN];

		// Data members
		private List<ScientificStudy> studies;

		/**
		 * Constructor.
		 */
		private StudyTableModel(String msgTag, List<ScientificStudy> initialStudies) {
			this.studies = initialStudies;
			names[NAME_COLUMN] = "Name";
			names[SCIENCE_COLUMN] = Msg.getString(msgTag + ".column.study"); 
			names[PERC_COLUMN] = "%";
			names[PHASE_COLUMN] =  Msg.getString(msgTag + ".column.phase"); //$NON-NLS-1$
			names[RESEARCHER_COLUMN] =  Msg.getString(msgTag + ".column.researcher"); //$NON-NLS-1$
			names[SETTLEMENT_COLUMN] =  Msg.getString(msgTag + ".column.settlement"); //$NON-NLS-1
		}

		/**
		 * Returns the number of columns in the model. A JTable uses this method to determine 
		 * how many columns it should create and display by default. 
		 * @return the number of columns in the model
		 */
		public int getColumnCount() {
			return NUM_COLUMN;
		}

		@Override
		public String getColumnName(int column) {
			return names[column];
		}


		/**
		 * Returns the number of rows in the model. A JTable uses this method to determine 
		 * how many rows it should display. This method should be quick, as it is called 
		 * frequently during rendering. 
		 * @return the number of rows in the model
		 */
		public int getRowCount() {
			return studies.size();
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * @param rowIndex the row whose value is to be queried
		 * @param columnIndex the column whose value is to be queried
		 * @return the value Object at the specified cell
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			if ((rowIndex >= 0) && (rowIndex < studies.size())) {
				ScientificStudy study = studies.get(rowIndex);

				switch(columnIndex) {
					case NAME_COLUMN:
						return study.getName();
					case SETTLEMENT_COLUMN:
						var s = study.getPrimarySettlement();
						return (s != null ? s.getName() : null);
					case SCIENCE_COLUMN:
						return study.getScience().getName();
					case RESEARCHER_COLUMN:
						var p  = study.getPrimaryResearcher();
						return (p != null ? p.getName() : null);
					case PHASE_COLUMN:
						return study.getPhase().getName();
					case PERC_COLUMN:
						return study.getPhaseProgress() * 100;
					default:
						return null;
				}
			}
			return null;
		}

		/**
		 * Updates the table model.
		 */
		private void update(List<ScientificStudy> newStudies) {
			studies = newStudies;
			fireTableDataChanged();
		}

		/**
		 * Gets the scientific study at a given row index.
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
		 * @param study the scientific study.
		 * @return the row index of the study or -1 if not in table.
		 */
		private int getStudyIndex(ScientificStudy study) {
			int result = -1;

			if (studies.contains(study)) result = studies.indexOf(study);

			return result;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == PERC_COLUMN) {
				return Double.class;
			}
			return String.class;
		}
	}

	/**
	 * Creates the panel for the specified parent science window. The panle
	 * has a message tag used to resolve any localised labels.
	 * 
	 * @param scienceWindow
	 * @param msgTag
	 */
	protected AbstractStudyListPanel(ScienceWindow scienceWindow, String msgTag) {
		super();
		this.scienceWindow = scienceWindow;

		setLayout(new BorderLayout());

		// Create title label.
		JLabel titleLabel = new JLabel(Msg.getString(msgTag + ".title"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(titleLabel);
		add(titleLabel, BorderLayout.NORTH);

		// Create list scroll pane.
		listScrollPane = new JScrollPane();
		listScrollPane.setBorder(new MarsPanelBorder());
		listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(listScrollPane, BorderLayout.CENTER);

		// Create study table model.
		studyTableModel = new StudyTableModel(msgTag, getStudies());

		// Create study table.
		studyTable = new JTable(studyTableModel);
		studyTable.setPreferredScrollableViewportSize(new Dimension(600, 260));
		studyTable.setCellSelectionEnabled(false);
		studyTable.setRowSelectionAllowed(true);
		studyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		studyTable.getSelectionModel().addListSelectionListener(event -> {
			if (event.getValueIsAdjusting()) {
				int tableRow = studyTable.getSelectedRow();
				if (tableRow >= 0) {
					int modelRow = studyTable.convertRowIndexToModel(studyTable.getSelectedRow());
					ScientificStudy selectedStudy = studyTableModel.getStudy(modelRow);
					if (selectedStudy != null) setSelectedScientificStudy(selectedStudy);
				}
			}
		});
		studyTable.getColumnModel().getColumn(StudyTableModel.NAME_COLUMN).setPreferredWidth(65);
		studyTable.getColumnModel().getColumn(StudyTableModel.SCIENCE_COLUMN).setPreferredWidth(50);
		studyTable.getColumnModel().getColumn(StudyTableModel.PERC_COLUMN).setPreferredWidth(5);
		studyTable.getColumnModel().getColumn(StudyTableModel.PHASE_COLUMN).setPreferredWidth(70);
		studyTable.getColumnModel().getColumn(StudyTableModel.RESEARCHER_COLUMN).setPreferredWidth(80);
		studyTable.getColumnModel().getColumn(StudyTableModel.SETTLEMENT_COLUMN).setPreferredWidth(80);
		studyTable.getColumnModel().getColumn(StudyTableModel.PERC_COLUMN).setCellRenderer(new NumberCellRenderer(0));

		studyTable.setAutoCreateRowSorter(true);
				
		listScrollPane.setViewportView(studyTable);
	}
	
	/**
	 * Method return the studies to be displayed.
	 * @return
	 */
	protected abstract List<ScientificStudy> getStudies();

	/**
	 * Updates the panel.
	 */
	protected void update() {
		studyTableModel.update(getStudies());
		// Make sure study is selected.
		selectScientificStudy(scienceWindow.getScientificStudy(), false);
	}

	/**
	 * Sets the selected scientific study in table.
	 * @param study the scientific study.
	 */
	private void setSelectedScientificStudy(ScientificStudy study) {
		scienceWindow.setScientificStudy(study);
	}

	/**
	 * Selects a scientific study.
	 * @param study the scientific study.
	 * @param scrollSelection true if table should be scrolled so selected row is visible.
	 */
	protected void selectScientificStudy(ScientificStudy study, boolean scrollSelection) {
		int studyModelIndex = studyTableModel.getStudyIndex(study);
		int newSelectionIndex = -1;
		if (studyModelIndex >= 0) {
			newSelectionIndex = studyTable.convertRowIndexToView(studyModelIndex);
		}
		
		int currentSelectedRow = studyTable.getSelectedRow();
		if (newSelectionIndex != currentSelectedRow) {
			if (newSelectionIndex >= 0) {
				studyTable.getSelectionModel().setSelectionInterval(newSelectionIndex, newSelectionIndex);
	
				if (scrollSelection) {
					Rectangle cellRect = studyTable.getCellRect(newSelectionIndex, 0, true);
					listScrollPane.getViewport().setViewPosition(cellRect.getLocation());
				}
			}
			else studyTable.clearSelection();
		}
	}
}