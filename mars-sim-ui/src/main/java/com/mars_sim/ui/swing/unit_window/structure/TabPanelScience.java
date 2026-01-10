/*
 * Mars Simulation Project
 * TabPanelScience.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityLauncher;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * A tab panel displaying a settlement's scientific studies and achievements.
 */
@SuppressWarnings("serial")
class TabPanelScience extends EntityTabPanel<Settlement> implements TemporalComponent{

	private static final String SCIENCE_ICON = "science";

	private JLabel totalAchievementLabel;

	private JTable achievementTable;
	private JTable studyTable;

	private StudyTableModel studyTableModel;
	private AchievementTableModel achievementTableModel;
	
	/**
	 * Constructor.
	 * 
	 * @param settlement the settlement.
	 * @param context the UI context.
	 */
	public TabPanelScience(Settlement settlement, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("ScientificStudy.science"), //$NON-NLS-1$
			ImageLoader.getIconByName(SCIENCE_ICON), null,
			context, settlement
		);
	}
	
	@Override
	protected void buildUI(JPanel content){

		// Create the main panel.
		JPanel mainPane = new JPanel(new GridLayout(2, 1, 0, 0));
		content.add(mainPane);

		// Create the studies panel.
		JPanel studiesPane = new JPanel(new BorderLayout(5, 5));
		mainPane.add(studiesPane);

		// Create the studies label.
		studiesPane.setBorder(SwingHelper.createLabelBorder(Msg.getString("ScientificStudy.plural")));

		// Create the study scroll panel.
		JScrollPane studyScrollPane = new JScrollPane();
		studyScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		studiesPane.add(studyScrollPane, BorderLayout.CENTER);

		var settlement = getEntity();
		// Create the study table.
		studyTableModel = new StudyTableModel(settlement, getContext());
		studyTable = new JTable(studyTableModel);
		EntityLauncher.attach(studyTable, getContext());
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		TableColumnModel studyColumns = studyTable.getColumnModel();
		studyColumns.getColumn(0).setCellRenderer(renderer);
		studyColumns.getColumn(1).setCellRenderer(renderer);
		studyColumns.getColumn(2).setCellRenderer(renderer);
		studyColumns.getColumn(3).setCellRenderer(renderer);
		studyColumns.getColumn(4).setCellRenderer(renderer);
		
		studyColumns.getColumn(0).setPreferredWidth(5);
		studyColumns.getColumn(1).setPreferredWidth(40);
		studyColumns.getColumn(2).setPreferredWidth(5);
		studyColumns.getColumn(3).setPreferredWidth(80);
		studyColumns.getColumn(4).setPreferredWidth(80);
		
		studyTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		studyTable.setRowSelectionAllowed(true);
		studyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		studyScrollPane.setViewportView(studyTable);

		// Added sorting
		studyTable.setAutoCreateRowSorter(true);

		// Create the achievement panel.
		JPanel achievementPane = new JPanel(new BorderLayout());
		mainPane.add(achievementPane);

		// Create the achievement label.
		achievementPane.setBorder(SwingHelper.createLabelBorder(Msg.getString("TabPanelScience.scientificAchievement")));

		var achievementSummary = new AttributePanel();
		achievementSummary.setBorder(BorderFactory.createEmptyBorder(2,0,2,0));
		String totalAchievementString = StyleManager.DECIMAL_PLACES1.format(getEntity().getTotalScientificAchievement());
		totalAchievementLabel = achievementSummary.addTextField(Msg.getString(
								"TabPanelScience.totalAchievementCredit"), totalAchievementString, null);
		achievementPane.add(achievementSummary, BorderLayout.NORTH);

		// Create the achievement scroll panel.
		JScrollPane achievementScrollPane = new JScrollPane();
		achievementScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		achievementPane.add(achievementScrollPane, BorderLayout.CENTER);

		// Create the achievement table.
		achievementTableModel = new AchievementTableModel(settlement);
		achievementTable = new JTable(achievementTableModel);
		achievementTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		achievementTable.setRowSelectionAllowed(true);
		achievementTable.setDefaultRenderer(Double.class, new NumberCellRenderer(1));
		achievementScrollPane.setViewportView(achievementTable);

		// Added sorting
		achievementTable.setAutoCreateRowSorter(true);

		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer1 = new DefaultTableCellRenderer();
		renderer1.setHorizontalAlignment(SwingConstants.CENTER);
		achievementTable.getColumnModel().getColumn(0).setCellRenderer(renderer1);
		achievementTable.getColumnModel().getColumn(1).setCellRenderer(renderer1);
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		// Get selected study in table if any.
		int selectedStudyIndex = studyTable.getSelectedRow();
		ScientificStudy selectedStudy = null;
		if (selectedStudyIndex >= 0) selectedStudy = studyTableModel.getStudy(selectedStudyIndex);

		// Update study table model.
		studyTableModel.update();

		// Reselect study in table.
		if (selectedStudy != null) {
			int newStudyIndex = studyTableModel.getStudyIndex(selectedStudy);
			if (newStudyIndex >= 0)
				studyTable.getSelectionModel().setSelectionInterval(newStudyIndex, newStudyIndex);
		}

		// Update achievement table model.
		achievementTableModel.update();

		// Update total achievement label.
		String totalAchievementString = StyleManager.DECIMAL_PLACES1.format(getEntity().getTotalScientificAchievement());
		totalAchievementLabel.setText(totalAchievementString); //$NON-NLS-1$
	}

	/**
	 * Inner class for study table model.
	 */
	private static class StudyTableModel extends AbstractTableModel
		implements EntityModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		private Settlement settlement;
		private List<ScientificStudy> studies;
		private ScientificStudyManager studyManager;

		/**
		 * Constructor.
		 * 
		 * @param settlement the settlement.
		 */
		private StudyTableModel(Settlement settlement, UIContext context) {
			// Use AbstractTableModel constructor.
			super();

			this.settlement = settlement;
			this.studyManager = context.getSimulation().getScientificStudyManager();
			// Get all studies the settlement is primary for.
			studies = studyManager.getAllStudies(settlement);
		}

		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return the number of columns in the model.
		 */
		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public String getColumnName(int column) {
			return switch (column) {
				case 0 -> Msg.getString("Entity.name");
				case 1 -> Msg.getString("ScientificStudy.science");
				case 2 -> Msg.getString("ScientificStudy.level");	
				case 3 -> Msg.getString("ScientificStudy.phase");
				case 4 -> Msg.getString("ScientificStudy.lead");
				default -> null;
			};
		}
		
		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return the number of rows in the model.
		 */
		@Override
		public int getRowCount() {
			return studies.size();
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param rowIndex the row whose value is to be queried.
		 * @param columnIndex the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String result = null;
			if ((rowIndex >= 0) && (rowIndex < studies.size())) {
				ScientificStudy study = studies.get(rowIndex);

				switch (columnIndex) {
					case 0 -> result = study.getName();
					case 1 -> result = study.getScience().getName();
					case 2 -> result = study.getDifficultyLevel() + "";
					case 3 -> result = study.getPhase().getName();
					default -> {
								String researcherN = "";	
								if (study.getPrimaryResearcher() != null) {
									researcherN = study.getPrimaryResearcher().getName();
									result = researcherN;
								}
							}
				}
			}
			return result;
		}

		/**
		 * Updates the table model.
		 */
		private void update() {
			List<ScientificStudy> newStudies = studyManager.getAllStudies(settlement);
			if (!newStudies.equals(studies)){
				studies = newStudies;
				fireTableDataChanged();
			}
			else if (!studies.isEmpty()) {
				fireTableRowsUpdated(0, studies.size() - 1);
			}
		}

			/**
		 * Gets the scientific study in the table at a given row index.
		 * @param rowIndex the row index in the table.
		 * @return scientific study or null if invalid index.
		 */
		private ScientificStudy getStudy(int rowIndex) {
			ScientificStudy result = null;
			if ((rowIndex >= 0) && (rowIndex < studies.size()))
				result = studies.get(rowIndex);
			return result;
		}

		/**
		 * Gets the row index of a given scientific study.
		 * @param study the scientific study.
		 * @return the table row index or -1 if not in table.
		 */
		private int getStudyIndex(ScientificStudy study) {
			int result = -1;
			if ((study != null) && studies.contains(study)) result = studies.indexOf(study);
			return result;
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return studies.get(row);
		}
	}

	/**
	 * Inner class for achievement table model.
	 */
	private static class AchievementTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		private Settlement settlement;
		private ScienceType[] sciences;

		/** hidden constructor. */
		private AchievementTableModel(Settlement settlement) {
			this.settlement = settlement;
			sciences = ScienceType.values();
		}

		/**
		 * Returns the number of columns in the model.
		 * @return the number of columns in the model.
		 */
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> Msg.getString("ScientificStudy.science");
				case 1 -> Msg.getString("TabPanelScience.column.achievementCredit");
				default -> null;
			};
		}

		/**
		 * Returns the most specific superclass for all the cell values in the column.
		 * @param columnIndex the index of the column.
		 * @return the common ancestor class of the object values in the model.
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = null;
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = Double.class;
			return dataType;
		}

		/**
		 * Returns the number of rows in the model.
		 * @return the number of rows in the model.
		 */
		@Override
		public int getRowCount() {
			return sciences.length;
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * @param rowIndex the row whose value is to be queried.
		 * @param columnIndex the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object result = null;
			if ((rowIndex >= 0) && (rowIndex < sciences.length)) {
				ScienceType science = sciences[rowIndex];
				if (columnIndex == 0) result = science.getName();
				else if (columnIndex == 1) {
					result = settlement.getScientificAchievement(science);
				}
			}
			return result;
		}

		/**
		 * Updates the table model.
		 */
		private void update() {
			fireTableDataChanged();
		}
	}
}
