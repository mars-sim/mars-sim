/*
 * Mars Simulation Project
 * TabPanelScienceStudy.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityLauncher;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.SwingHelper;


/**
 * A tab panel displaying a person's scientific studies and achievements.
 */
@SuppressWarnings("serial")
class TabPanelScienceStudy extends EntityTabPanel<Person> implements EntityManagerListener {

	private static final String SCIENCE_ICON = "science";
	
	private JLabel totalAchievementLabel;

	private StudyTableModel studyTableModel;
	private AchievementTableModel achievementTableModel;

	private ScientificStudyManager scienceManager;

	private JLabel primaryCompletedLabel;

	private JLabel collabCompletedLabel;

	/**
	 * Constructor.
	 * 
	 * @param person  the person.
	 * @param context the UI context.
	 */
	public TabPanelScienceStudy(Person person, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("ScientificStudy.singular"), 
			ImageLoader.getIconByName(SCIENCE_ICON),
			null,
			context, person
		);

		this.scienceManager = context.getSimulation().getScientificStudyManager();
	}

	@Override
	protected void buildUI(JPanel content) {
    JTable studyTable;
  		JTable achievementTable;
		// Create the main panel.
		JPanel mainPane = new JPanel(new GridLayout(2, 1, 0, 0));
		content.add(mainPane);

		var person = getEntity();

		// Create the studies panel.
		JPanel studiesPane = new JPanel(new BorderLayout());
		studiesPane.setBorder(SwingHelper.createLabelBorder(Msg.getString("ScientificStudy.plural"))); //$NON-NLS-1$
		mainPane.add(studiesPane);

		// Create the study scroll panel.
		JScrollPane studyScrollPane = new JScrollPane();
		studyScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		studiesPane.add(studyScrollPane, BorderLayout.CENTER);

		// Create the study table by finding all Studies this Person is involved in.
		studyTableModel = new StudyTableModel(person, scienceManager);
		scienceManager.addListener(this);

		studyTable = new JTable(studyTableModel);
		studyTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		studyTable.setRowSelectionAllowed(true);
		studyTable.setDefaultRenderer(Double.class, new NumberCellRenderer(1));
		studyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		studyScrollPane.setViewportView(studyTable);

		studyTable.setAutoCreateRowSorter(true);
		EntityLauncher.attach(studyTable, getContext());

		// Create the achievement panel.
		JPanel achievementPane = new JPanel(new BorderLayout());
		achievementPane.setBorder(SwingHelper.createLabelBorder(Msg.getString("TabPanelScience.scientificAchievement")));
		mainPane.add(achievementPane);

		AttributePanel achievementLabelPane = new AttributePanel(3);
		achievementPane.add(achievementLabelPane, BorderLayout.NORTH);

		totalAchievementLabel = achievementLabelPane.addTextField(Msg.getString("TabPanelScience.totalAchievementCredit"), //$NON-NLS-1$
						"", null);
		primaryCompletedLabel = achievementLabelPane.addTextField(Msg.getString("TabPanelScience.numPrimary"), //$NON-NLS-1$
						"", null);
		collabCompletedLabel = achievementLabelPane.addTextField(Msg.getString("TabPanelScience.numCollab"), //$NON-NLS-1$
						"", null);

		// Create the achievement scroll panel.
		JScrollPane achievementScrollPane = new JScrollPane();
		achievementScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		achievementPane.add(achievementScrollPane, BorderLayout.CENTER);

		// Create the achievement table.
		achievementTableModel = new AchievementTableModel(person);
		achievementTable = new JTable(achievementTableModel);
		achievementTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		achievementTable.setRowSelectionAllowed(true);
		achievementTable.setDefaultRenderer(Double.class, new NumberCellRenderer(1));
		
		achievementScrollPane.setViewportView(achievementTable);

		achievementTable.setAutoCreateRowSorter(true);
	}

	/**
	 * Remove the listener on the scientific study manager.
	 */
	@Override
	public void destroy() {
		scienceManager.removeListener(this);
		if (studyTableModel != null) {
			studyTableModel.destroy();
		}
		super.destroy();
	}

	/**	
	 * Possible update due to assigned study change. The StudyTableModel is updated,
	 * and if it changed, the achievement table model and total achievement label are also updated.
	 */
	private void possibleAssignedStudyChange() {
		// Update study table model.
		if (studyTableModel.update()) {
			// If study table changed, also update achievement table model.
			achievementTableModel.update();


			var person = getEntity();

			// Update total achievement label.
			String totalAchievementString = StyleManager.DECIMAL_PLACES1.format(person.getResearchStudy().getTotalScientificAchievement());
			totalAchievementLabel.setText(totalAchievementString); //$NON-NLS-1$
			primaryCompletedLabel.setText(Integer.toString(scienceManager.getNumCompletedPrimaryStudies(person)));
			collabCompletedLabel.setText(Integer.toString(scienceManager.getNumCompletedCollaborativeStudies(person)));
		}
	}

	
	@Override
	public void entityAdded(Entity newEntity) {
		// New study so refresh study table
		possibleAssignedStudyChange();
	}

	@Override
	public void entityRemoved(Entity removedEntity) {
		// Study removed so refresh study table
		possibleAssignedStudyChange();
	}

	/**
	 * Inner class for study table model.
	 */
	private static class StudyTableModel extends AbstractTableModel 
		implements EntityModel, EntityListener {

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
			super();

			this.person = person;
			this.manager = manager;
			this.studies = Collections.emptyList();

			// Get all studies the person is or has been involved in.
			setMonitoredStudies(manager.getAllStudies(person));
		}

		/**
		 * Sets the list of monitored studies.
		 * @param newStudies Studies to monitor
		 */
		private void setMonitoredStudies(List<ScientificStudy> newStudies) {
			// Remove this as listener from old studies.
			for (ScientificStudy study : this.studies) {
				study.removeEntityListener(this);
			}
			
			this.studies = newStudies;

			// Add this as listener to new studies.
			for (ScientificStudy study : this.studies) {
				study.addEntityListener(this);
			}
		}	

		private void destroy() {
			// Remove this as listener from monitored studies.
			for (ScientificStudy study : this.studies) {
				study.removeEntityListener(this);
			}
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
				case NAME_COL -> Msg.getString("Entity.name");
				case ROLE_COL -> Msg.getString("TabPanelScience.column.role");
				case PHASE_COL -> Msg.getString("ScientificStudy.phase");
				case RESEARCH_COL -> Msg.getString("TabPanelScience.column.researchTime");
				case PAPER_COL -> Msg.getString("TabPanelScience.column.paperTime");
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
			ScientificStudy study = getStudy(rowIndex);
			if (study == null) {
				return null;
			}

			Object result = null;
			switch (columnIndex) {
				case NAME_COL:
					result = study.getName();
					break;
				case ROLE_COL:
					if (person.equals(study.getPrimaryResearcher()))
						result = Msg.getString("ScientificStudy.lead");
					else if (study.getCollaborativeResearchers().contains(person))
						result = Msg.getString("ScientificStudy.collaborator.singular"); //$NON-NLS-1$
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
			return result;
		}

		/**
		 * Updates the table model.
		 */
		private boolean update() {
			List<ScientificStudy> newStudies = manager.getAllStudies(person);
			boolean hasChanged = !newStudies.equals(studies);
			
			if (hasChanged) {
				setMonitoredStudies(newStudies);
				fireTableDataChanged();
			}
			return hasChanged;
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

		@Override
		public Entity getAssociatedEntity(int row) {
			return getStudy(row);
		}

		/**
		 * Monitored study has changed so fire a change for the corresponding row.
		 */
		@Override
		public void entityUpdate(EntityEvent event) {
			// A monitored study has changed so fire table data changed.
			if (event.getSource() instanceof ScientificStudy ss) {
				var idx = studies.indexOf(ss);
				fireTableRowsUpdated(idx, idx);
			}
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

		private AchievementTableModel(Person person) {
			// Use AbstractTableModel constructor.
			super();

			this.person = person;
			sciences = ScienceType.values();
		}

		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return the number of columns in the model.
		 */
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
		 * 
		 * @param columnIndex the index of the column.
		 * @return the common ancestor class of the object values in the model.
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> String.class;
				case 1 -> Double.class;
				default -> Object.class;
			};
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
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object result = null;
			if ((rowIndex >= 0) && (rowIndex < sciences.length)) {
				ScienceType science = sciences[rowIndex];
				if (columnIndex == 0)
					result = science.getName();
				else if (columnIndex == 1) {
					result = person.getResearchStudy().getScientificAchievement(science);
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
