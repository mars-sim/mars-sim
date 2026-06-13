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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AchievementTableModel;
import com.mars_sim.ui.swing.utils.EntityLauncher;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.model.BaseScienceStudyModel;


/**
 * A tab panel displaying a person's scientific studies and achievements.
 */
@SuppressWarnings("serial")
class TabPanelScienceStudy extends EntityTabPanel<Person>
	implements EntityManagerListener, TemporalComponent {

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
			Msg.getString("scientificstudy.science"), 
			ImageLoader.getIconByName(SCIENCE_ICON),
			null,
			context, person
		);

		this.scienceManager = context.getSimulation().getScientificStudyManager();
	}

	@Override
	protected void buildUI(JPanel content) {
    JTable studyTable;
		// Create the main panel.
		JPanel mainPane = new JPanel(new GridLayout(2, 1, 0, 0));
		content.add(mainPane);

		var person = getEntity();

		// Create the studies panel.
		JPanel studiesPane = new JPanel(new BorderLayout());
		studiesPane.setBorder(SwingHelper.createLabelBorder(Msg.getString("scientificstudy.plural"))); //$NON-NLS-1$
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
		collabCompletedLabel = achievementLabelPane.addTextField(Msg.getString("scientificstudy.collaborator.plural"), //$NON-NLS-1$
						"", null);

		// Create the achievement table.
		achievementTableModel = new AchievementTableModel(s -> person.getResearchStudy().getScientificAchievement(s));
		achievementTableModel.update();
		var achievementTable = SwingHelper.createScrolledTable(achievementTableModel, getContext(), null, new Dimension(225, -1));
		achievementPane.add(achievementTable, BorderLayout.CENTER);
	}

	/**
	 * Remove the listener on the scientific study manager.
	 */
	@Override
	public void destroy() {
		scienceManager.removeListener(this);
		if (studyTableModel != null) {
			studyTableModel.release();
		}
		super.destroy();
	}
	
	@Override
	public void entityAdded(Entity newEntity) {
		if (studyTableModel != null) {
			studyTableModel.update();
		}
	}

	@Override
	public void entityRemoved(Entity removedEntity) {
		if (studyTableModel != null) {
			studyTableModel.update();
		}
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		// If study table changed, also update achievement table model.
		achievementTableModel.update();

		if (studyTableModel != null) {
			studyTableModel.refresh();
		}

		var person = getEntity();

		// Update total achievement label.
		String totalAchievementString = StyleManager.DECIMAL_PLACES1.format(person.getResearchStudy().getTotalScientificAchievement());
		totalAchievementLabel.setText(totalAchievementString); //$NON-NLS-1$
		primaryCompletedLabel.setText(Integer.toString(scienceManager.getNumCompletedPrimaryStudies(person)));
		collabCompletedLabel.setText(Integer.toString(scienceManager.getNumCompletedCollaborativeStudies(person)));
	}

	/**
	 * Refreshes the UI components forces a reset of study table.
	 */
	@Override
	public void refreshUI() {
		if (studyTableModel != null) {
			studyTableModel.update();
		}
	}

	/**
	 * Inner class for study table model.
	 */
	private static class StudyTableModel extends BaseScienceStudyModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private static final String COLLABORATOR = Msg.getString("scientificstudy.collaborator"); //$NON-NLS-1$
		private static final String LEAD = Msg.getString("scientificstudy.lead"); //$NON-NLS-1$

		private static final int ROLE_VAL = 10;
		private static final int RESEARCH_VAL = 11;
		private static final int PAPER_VAL = 12;

		private static final EntityColumnSpec ROLE = new EntityColumnSpec(
						new ColumnSpec(ROLE_VAL, Msg.getString("TabPanelScience.column.role"), String.class),
						null);
		private static final EntityColumnSpec RESEARCH = new EntityColumnSpec(
						new ColumnSpec(RESEARCH_VAL, Msg.getString("TabPanelScience.column.researchTime"), Double.class),
						null);
		private static final EntityColumnSpec PAPER = new EntityColumnSpec(
						new ColumnSpec(PAPER_VAL, Msg.getString("TabPanelScience.column.paperTime"), Double.class),
						null);

		// Data members.
		private Person person;
		private ScientificStudyManager manager;

		/**
		 * Constructor.
		 * 
		 * @param person the person.
		 */
		private StudyTableModel(Person person, ScientificStudyManager manager)  {
			super(NAME, ROLE, PHASE, RESEARCH, PAPER);

			this.person = person;
			this.manager = manager;

			// Get all studies the person is or has been involved in.
			setEntities(manager.getAllStudies(person));
		}

		@Override
		protected Object getEntityValue(com.mars_sim.core.science.ScientificStudy entity, int valueIndex) {
			return switch (valueIndex) {
				case ROLE_VAL -> {
					if (person.equals(entity.getPrimaryResearcher())) {
						yield LEAD;
					} else if (entity.getCollaborativeResearchers().contains(person)) {
						yield COLLABORATOR;
					} else {
						yield null;
					}
				}
				case RESEARCH_VAL -> {
					if (entity.getPrimaryResearcher().equals(person)) {
						yield entity.getPrimaryResearchWorkTimeCompleted();
					} else if (entity.getCollaborativeResearchers().contains(person)) {
						yield entity.getCollaborativeResearchWorkTimeCompleted(person);
					} else {
						yield null;
					}
				}
				case PAPER_VAL -> {
					if (entity.getPrimaryResearcher().equals(person)) {
						yield entity.getPrimaryPaperWorkTimeCompleted();
					} else if (entity.getCollaborativeResearchers().contains(person)) {
						yield entity.getCollaborativePaperWorkTimeCompleted(person);
					} else {
						yield null;
					}
				}
				default -> super.getEntityValue(entity, valueIndex);
			};
		}

		/**
		 * Updates the table model.
		 */
		private boolean update() {
			return setEntities(manager.getAllStudies(person));
		}

		private void refresh() {
			if (getRowCount() > 0) {
				fireTableRowsUpdated(0, getRowCount() - 1);
			}
		}
	}
}