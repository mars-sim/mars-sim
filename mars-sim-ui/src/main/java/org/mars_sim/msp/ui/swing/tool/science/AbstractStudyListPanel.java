/**
 * Mars Simulation Project
 * AbstractStudyListPanel.java
 * @version 3.2.0 2021-07-28
 * @author Barry Evans
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
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

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
	private static class StudyTableModel
		extends AbstractTableModel {
	
		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private List<ScientificStudy> studies;
		private String msgTag;

		/**
		 * Constructor.
		 */
		private StudyTableModel(String msgTag, List<ScientificStudy> initialStudies) {
			this.studies = initialStudies;
			this.msgTag = msgTag;
		}

		/**
		 * Returns the number of columns in the model. A JTable uses this method to determine 
		 * how many columns it should create and display by default. 
		 * @return the number of columns in the model
		 */
		public int getColumnCount() {
			return 6;
		}

		@Override
		public String getColumnName(int column) {
			String result = ""; //$NON-NLS-1$
			if (column == 0) 
				result = Msg.getString(msgTag + ".column.id"); //$NON-NLS-1$
			else if (column == 1) 
				result = Msg.getString(msgTag + ".column.study"); //$NON-NLS-1$
			else if (column == 2) 
				result = Msg.getString(msgTag + ".column.level"); //$NON-NLS-1$
			else if (column == 3) 
				result = Msg.getString(msgTag + ".column.phase"); //$NON-NLS-1$
			else if (column == 4)
				result = Msg.getString(msgTag + ".column.researcher"); //$NON-NLS-1$
			else if (column == 5)
				result = Msg.getString(msgTag + ".column.settlement"); //$NON-NLS-1$
			return result;
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
			String result = ""; //$NON-NLS-1$
			if ((rowIndex >= 0) && (rowIndex < studies.size())) {
				ScientificStudy study = studies.get(rowIndex);
				String researcherN = "";	
				String settlementN = "";
				if (study.getPrimaryResearcher() != null) {
					researcherN = study.getPrimaryResearcher().getName();
					
					if (study.getPrimarySettlement() != null) {
						settlementN = study.getPrimarySettlement().getName();
					}
				}

				if (columnIndex == 0) 
					result = study.getID() + "";
				else if (columnIndex == 1) 
					result = study.getScience().getName();
				else if (columnIndex == 2) 
					result = study.getDifficultyLevel() + "";
				else if (columnIndex == 3) 
					result = study.getPhase();
				else if (columnIndex == 4)
					result = researcherN;
				else if (columnIndex == 5)
					result = settlementN;
			}
			return result;
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
	}

	/**
	 * Creates the panel for the specified parent science window. The panle
	 * has a message tag used to resolve any localised labels.
	 * 
	 * @param scienceWindow
	 * @param msgTag
	 */
	public AbstractStudyListPanel(ScienceWindow scienceWindow, String msgTag) {
		super();
		this.scienceWindow = scienceWindow;

		setLayout(new BorderLayout());

		// Create title label.
		JLabel titleLabel = new JLabel(Msg.getString(msgTag + ".title"), JLabel.CENTER); //$NON-NLS-1$
		add(titleLabel, BorderLayout.NORTH);

		// Create list scroll pane.
		listScrollPane = new JScrollPane();
		listScrollPane.setBorder(new MarsPanelBorder());
		listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(listScrollPane, BorderLayout.CENTER);

		// Create study table model.
		studyTableModel = new StudyTableModel(msgTag, getStudies());

		// Create study table.
		studyTable = new JTable(studyTableModel);
		studyTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
		studyTable.setCellSelectionEnabled(false);
		studyTable.setRowSelectionAllowed(true);
		studyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		studyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting()) {
					int tableRow = studyTable.getSelectedRow();
					if (tableRow >= 0) {
						int modelRow = studyTable.convertRowIndexToModel(studyTable.getSelectedRow());
						ScientificStudy selectedStudy = studyTableModel.getStudy(modelRow);
						if (selectedStudy != null) setSelectedScientificStudy(selectedStudy);
					}
				}
			}
		});
		studyTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		studyTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		studyTable.getColumnModel().getColumn(2).setPreferredWidth(5);
		studyTable.getColumnModel().getColumn(3).setPreferredWidth(80);
		studyTable.getColumnModel().getColumn(4).setPreferredWidth(80);
		studyTable.getColumnModel().getColumn(5).setPreferredWidth(80);
		
		studyTable.setAutoCreateRowSorter(true);
		
		TableStyle.setTableStyle(studyTable);
		
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
		TableStyle.setTableStyle(studyTable);
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