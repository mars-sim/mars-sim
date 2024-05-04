/*
 * Mars Simulation Project
 * TabPanelScienceStudy.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.NumberCellRenderer;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.science.ScienceWindow;
import com.mars_sim.ui.swing.unit_window.TabPanel;


/**
 * A tab panel displaying a person's scientific studies and achievements.
 */
@SuppressWarnings("serial")
public class TabPanelScienceStudy extends TabPanel {

	private static final String SCIENCE_ICON = "science";
	
	/** The Person instance. */
	private Person person = null;

	private JTable studyTable;
	

	private JButton scienceToolButton;
	private JLabel totalAchievementLabel;

	private StudyTableModel studyTableModel;
	private AchievementTableModel achievementTableModel;

	private ScientificStudyManager scienceManager;

	/**
	 * Constructor.
	 * 
	 * @param person  the person.
	 * @param desktop the main desktop.
	 */
	public TabPanelScienceStudy(Person person, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null, 
			ImageLoader.getIconByName(SCIENCE_ICON),
			Msg.getString("TabPanelScience.title"), //$NON-NLS-1$
			person, desktop
		);

		this.person = person;
		this.scienceManager = desktop.getSimulation().getScientificStudyManager();
	}

	@Override
	protected void buildUI(JPanel content) {
  JTable achievementTable;
		// Create the main panel.
		JPanel mainPane = new JPanel(new GridLayout(2, 1, 0, 0));
		content.add(mainPane);

		// Create the studies panel.
		JPanel studiesPane = new JPanel(new BorderLayout());
		mainPane.add(studiesPane);

		// Create the studies label.
		JLabel studiesLabel = new JLabel(Msg.getString("TabPanelScience.scientificStudies"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(studiesLabel);
		studiesPane.add(studiesLabel, BorderLayout.NORTH);

		// Create the study scroll panel.
		JScrollPane studyScrollPane = new JScrollPane();
		studyScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		studiesPane.add(studyScrollPane, BorderLayout.CENTER);

		// Create the study table.
		studyTableModel = new StudyTableModel(person, scienceManager);
		studyTable = new JTable(studyTableModel);
		studyTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		studyTable.setRowSelectionAllowed(true);
		studyTable.setDefaultRenderer(Double.class, new NumberCellRenderer(1));
		studyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		studyTable.getSelectionModel().addListSelectionListener(event -> {
			if (event.getValueIsAdjusting() && (studyTable.getSelectedRow() >= 0)) {
				setEnabledScienceToolButton(true);
			}
		});
		studyScrollPane.setViewportView(studyTable);

		studyTable.setAutoCreateRowSorter(true);

		// Create the button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		studiesPane.add(buttonPane, BorderLayout.SOUTH);

		// Create the science tool button.
		scienceToolButton = new JButton(ImageLoader.getIconByName(ScienceWindow.ICON)); //$NON-NLS-1$
		scienceToolButton.setEnabled(false);
		scienceToolButton.setMargin(new Insets(1, 1, 1, 1));
		scienceToolButton.setToolTipText(Msg.getString("TabPanelScience.tooltip.science"));
		scienceToolButton.addActionListener(arg0 -> displayStudyInScienceTool());
		buttonPane.add(scienceToolButton);

		// Create the achievement panel.
		JPanel achievementPane = new JPanel(new BorderLayout());
		mainPane.add(achievementPane);

		// Create achievement label panel.
		JPanel achievementLabelPane = new JPanel(new GridLayout(2, 1, 0, 0));
		achievementPane.add(achievementLabelPane, BorderLayout.NORTH);

		// Create the achievement label.
		JLabel achievementLabel = new JLabel(Msg.getString("TabPanelScience.scientificAchievement"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(achievementLabel);
		achievementLabelPane.add(achievementLabel);

		String totalAchievementString = StyleManager.DECIMAL_PLACES1.format(person.getTotalScientificAchievement());
		totalAchievementLabel = new JLabel(
				Msg.getString("TabPanelScience.totalAchievementCredit", totalAchievementString), SwingConstants.CENTER); //$NON-NLS-1$
		achievementLabelPane.add(totalAchievementLabel);

		// Create the achievement scroll panel.
		JScrollPane achievementScrollPane = new JScrollPane();
		achievementScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		achievementPane.add(achievementScrollPane, BorderLayout.CENTER);

		// Create the achievement table.
		achievementTableModel = new AchievementTableModel(person, scienceManager);
		achievementTable = new JTable(achievementTableModel);
		achievementTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		achievementTable.setRowSelectionAllowed(true);
		achievementTable.setDefaultRenderer(Double.class, new NumberCellRenderer(1));
		
		achievementScrollPane.setViewportView(achievementTable);

		achievementTable.setAutoCreateRowSorter(true);

	}

	@Override
	public void update() {
		// Get selected study in table if any.
		int selectedStudyIndex = studyTable.getSelectedRow();
		ScientificStudy selectedStudy = null;
		if (selectedStudyIndex >= 0)
			selectedStudy = studyTableModel.getStudy(selectedStudyIndex);

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
		String totalAchievementString = StyleManager.DECIMAL_PLACES1.format(person.getTotalScientificAchievement());
		totalAchievementLabel.setText(Msg.getString("TabPanelScience.totalAchievementCredit", totalAchievementString)); //$NON-NLS-1$
	}

	/**
	 * Sets if the science tool button is enabled or not.
	 * 
	 * @param enabled true if button enabled.
	 */
	private void setEnabledScienceToolButton(boolean enabled) {
		scienceToolButton.setEnabled(enabled);
	}

	/**
	 * Displays the scientific study selected in the table in the science tool.
	 */
	private void displayStudyInScienceTool() {
		int selectedStudyIndex = studyTable.getSelectedRow();
		if (selectedStudyIndex >= 0) {
			ScientificStudy selectedStudy = studyTableModel.getStudy(selectedStudyIndex);
			getDesktop().showDetails(selectedStudy);
		}
	}

	/**
	 * Inner class for study table model.
	 */
	private static class StudyTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private static final int NAME_COL = 0;
		private static final int ROLE_COL = 1;
		private static final int PHASE_COL = 2;
		private static final int RESEARCH_COL = 3;
		private static final int PAPER_COL = 4;

		// Data members.
		private Person person;
		private List<ScientificStudy> studies;

		private ScientificStudyManager manager;

		/**
		 * Constructor.
		 * 
		 * @param person the person.
		 */
		private StudyTableModel(Person person, ScientificStudyManager manager)  {
			// Use AbstractTableModel constructor.
			super();

			this.person = person;
			this.manager = manager;

			// Get all studies the person is or has been involved in.
			studies = manager.getAllStudies(person);
		}

		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return the number of columns in the model.
		 */
		public int getColumnCount() {
			return PAPER_COL + 1;
		}

		@Override
		public String getColumnName(int column) {
			return switch (column) {
				case NAME_COL -> Msg.getString("TabPanelScience.column.study"); //$NON-NLS-1$
				case ROLE_COL -> Msg.getString("TabPanelScience.column.role"); //$NON-NLS-1$
				case PHASE_COL -> Msg.getString("TabPanelScience.column.phase"); //$NON-NLS-1$
				case RESEARCH_COL -> Msg.getString("TabPanelScience.column.researchTime"); //$NON-NLS-1$
				case PAPER_COL -> Msg.getString("TabPanelScience.column.paperTime"); //$NON-NLS-1$
				default -> null;
			};
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return switch (column) {
				case NAME_COL -> String.class;
				case ROLE_COL -> String.class;
				case PHASE_COL -> String.class;
				case RESEARCH_COL -> Double.class;
				case PAPER_COL -> Double.class;
				default -> null;
			};
		}

		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return the number of rows in the model.
		 */
		public int getRowCount() {
			return studies.size();
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param rowIndex    the row whose value is to be queried.
		 * @param columnIndex the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object result = null;
			if ((rowIndex >= 0) && (rowIndex < studies.size())) {
				ScientificStudy study = studies.get(rowIndex);
				switch (columnIndex) {
					case NAME_COL:
						result = study.getName();
						break;
					case ROLE_COL:
						if (person.equals(study.getPrimaryResearcher()))
							result = Msg.getString("TabPanelScience.primary"); //$NON-NLS-1$
						else if (study.getCollaborativeResearchers().contains(person))
							result = Msg.getString("TabPanelScience.collaborator"); //$NON-NLS-1$
						break;
					case PHASE_COL:
						result = study.getPhase().getName();
						break;
					case RESEARCH_COL:
						if (study.getPrimaryResearcher().equals(person))
							result = study.getPrimaryResearchWorkTimeCompleted();
						else if (study.getCollaborativeResearchers().contains(person))
							result = study.getCollaborativeResearchWorkTimeCompleted(person);
						break;
					case PAPER_COL:
						if (study.getPrimaryResearcher().equals(person))
							result = study.getPrimaryPaperWorkTimeCompleted();
						else if (study.getCollaborativeResearchers().contains(person))
							result = study.getCollaborativePaperWorkTimeCompleted(person);
						break;
					default:
				}
			}
			return result;
		}

		/**
		 * Updates the table model.
		 */
		private void update() {
			List<ScientificStudy> newStudies = manager.getAllStudies(person);
			if (!newStudies.equals(studies))
				studies = newStudies;
			fireTableDataChanged();
		}

		/**
		 * Gets the scientific study in the table at a given row index.
		 * 
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
		 * 
		 * @param study the scientific study.
		 * @return the table row index or -1 if not in table.
		 */
		private int getStudyIndex(ScientificStudy study) {
			int result = -1;
			if ((study != null) && studies.contains(study))
				result = studies.indexOf(study);
			return result;
		}
	}

	/**
	 * Inner class for achievement table model.
	 */
	private static class AchievementTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		private Person person;
		private ScienceType[] sciences;
		private ScientificStudyManager manager;

		private AchievementTableModel(Person person, ScientificStudyManager manager) {
			// Use AbstractTableModel constructor.
			super();

			this.person = person;
			this.manager = manager;
			sciences = ScienceType.values();
		}

		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return the number of columns in the model.
		 */
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return Msg.getString("TabPanelScience.column.science"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("TabPanelScience.column.numPrimary"); //$NON-NLS-1$
			else if (columnIndex == 2)
				return Msg.getString("TabPanelScience.column.numCollab"); //$NON-NLS-1$
			else if (columnIndex == 3)
				return Msg.getString("TabPanelScience.column.achievementCredit"); //$NON-NLS-1$
			else
				return null;
		}

		/**
		 * Returns the most specific superclass for all the cell values in the column.
		 * 
		 * @param columnIndex the index of the column.
		 * @return the common ancestor class of the object values in the model.
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0)
				dataType = String.class;
			else if (columnIndex == 1)
				dataType = Integer.class;
			else if (columnIndex == 2)
				dataType = Integer.class;
			else if (columnIndex == 3)
				dataType = Double.class;
			return dataType;
		}

		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return the number of rows in the model.
		 */
		public int getRowCount() {
			return sciences.length;
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param rowIndex    the row whose value is to be queried.
		 * @param columnIndex the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object result = null;
			if ((rowIndex >= 0) && (rowIndex < sciences.length)) {
				ScienceType science = sciences[rowIndex];
				if (columnIndex == 0)
					result = science.getName();
				else if (columnIndex == 1)
					result = manager.getNumCompletedPrimaryStudies(person);
				else if (columnIndex == 2)
					result = manager.getNumCompletedCollaborativeStudies(person);
				else if (columnIndex == 3) {
					result = person.getScientificAchievement(science);
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
