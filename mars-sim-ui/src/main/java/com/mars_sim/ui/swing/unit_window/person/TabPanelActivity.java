/*
 * Mars Simulation Project
 * TabPanelActivity.java
 * @date 2024-08-04
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.Unit;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.mission.util.MissionRating;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.CacheCreator;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.PersonTaskManager;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.RatingScoreRenderer;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;

/**
 * The TabPanelActivity is a tab panel for a person's current tasks and
 * activities. This listens for Entity events related to Task & Mission.
 */
@SuppressWarnings("serial")
public class TabPanelActivity extends EntityTabPanel<Worker>
		implements EntityListener {

	private static final String BEST_SCORE = "Best Score";

	private static final int MAX_LABEL = 30;
	
	private static final String TASK_ICON = "task";
	
	private static final String HTML = "<html>";
	private static final String END_HTML = "</html>";
	private static final String DESCRIPTION = Msg.getString("Entity.description");
	private static final String SCORE = Msg.getString("Rating.score");

	/** data cache */
	private String missionPhaseCache = "";

	private EntityLabel missionLabel;
	private JLabel missionPhase;
	private JLabel missionScore;
	private JLabel taskScoreTextArea;

	private JTextArea taskStack;
	
	private TaskCacheModel taskCacheModel;
	private MissionCacheModel missionCacheModel;
	
	private JTextArea pendingTasks;

	private Mission trackedMission;


	/**
	 * Constructor.
	 *
	 * @param unit    {@link Unit} the unit to display.
	 * @param context {@link UIContext} the UI context.
	 */
	public TabPanelActivity(Worker unit, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelActivity.title"), //-NLS-1$
			ImageLoader.getIconByName(TASK_ICON),	
			Msg.getString("TabPanelActivity.title"), //-NLS-1$
			context, unit
		);

	}

	@Override
	protected void buildUI(JPanel content) {

		JPanel topPanel = new JPanel(new BorderLayout());
		content.add(topPanel, BorderLayout.CENTER);

		// Create a task main panel
		JPanel taskMainPanel = createTaskPanel();
		topPanel.add(taskMainPanel, BorderLayout.CENTER);		

		// Create a pending task panel
		JPanel pendingTaskPanel = new JPanel(new BorderLayout(1, 3));
		pendingTasks = new JTextArea(2, 30);
		pendingTaskPanel.add(pendingTasks, BorderLayout.CENTER);
		addBorder(pendingTaskPanel, "Pending Task");
		taskMainPanel.add(pendingTaskPanel, BorderLayout.SOUTH);
		
		// Create a mission main panel
		JPanel missionMainPanel = createMissionPanel();
		topPanel.add(missionMainPanel, BorderLayout.SOUTH);

		updateMissionPanel();
		updateTaskPanel();
	}

	/**
	 * Creates a mission panel.
	 * 
	 * @return
	 */
	private JPanel createMissionPanel() {
		JPanel missionMainPanel = new JPanel(new BorderLayout(1, 3));
		missionMainPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("Mission.singular")));

		JPanel missionSubPanel = new JPanel(new BorderLayout(1, 3));
		missionMainPanel.add(missionSubPanel, BorderLayout.NORTH);

		// Prepare attribute panel
		AttributePanel missionAttributePanel = new AttributePanel();
		missionSubPanel.add(missionAttributePanel, BorderLayout.NORTH);
		missionLabel = new EntityLabel(getDesktop());
		missionAttributePanel.addLabelledItem(Msg.getString("Entity.description"), missionLabel);
		missionPhase = missionAttributePanel.addRow(Msg.getString("Mission.phase"), ""); //-NLS-1$
		missionScore = missionAttributePanel.addRow(BEST_SCORE, "");

		missionCacheModel = new MissionCacheModel();
		JPanel missionCachePanel = new JPanel(new BorderLayout(1, 3));
		missionCachePanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("Rating.plural")));
		
		JTable missionCacheTable = new JTable(missionCacheModel) {
		    @Override
            public String getToolTipText(MouseEvent e) {
                return ToolTipTableModel.extractToolTip(e, this);
            }
		};
		missionCacheTable.setDefaultRenderer(Double.class,
						new NumberCellRenderer(2));
		missionCacheTable.setPreferredScrollableViewportSize(new Dimension(225, 80));
		missionCacheTable.getColumnModel().getColumn(0).setPreferredWidth(170);
		missionCacheTable.getColumnModel().getColumn(1).setPreferredWidth(20);
		// Add sorting
		missionCacheTable.setAutoCreateRowSorter(true);
		missionCacheTable.setRowSelectionAllowed(true);
				
		// Add a scrolled window and center it with the table
		JScrollPane scroller = new JScrollPane(missionCacheTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		missionCachePanel.add(scroller, BorderLayout.CENTER);
		missionMainPanel.add(missionCachePanel, BorderLayout.CENTER);
		
		return missionMainPanel;
	}
	
	/**
	 * Creates a task panel.
	 * 
	 * @return
	 */
	private JPanel createTaskPanel() {
		JPanel taskMainPanel = new JPanel(new BorderLayout(1, 3));
		taskMainPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("Task.singular")));

		JPanel taskSubPanel = new JPanel(new BorderLayout(1, 3));
		taskMainPanel.add(taskSubPanel, BorderLayout.NORTH);

		taskStack = new JTextArea(3, 30);
		taskStack.setToolTipText("Show the description and phase of a task and its subtask(s)");
		taskSubPanel.add(taskStack, BorderLayout.NORTH);

		AttributePanel taskScorePanel = new AttributePanel(1);
		taskSubPanel.add(taskScorePanel, BorderLayout.CENTER);
		taskScoreTextArea = taskScorePanel.addRow(BEST_SCORE, "");
		
		taskCacheModel = new TaskCacheModel();
		JPanel taskCachePanel = new JPanel(new BorderLayout(1, 3));
		taskCachePanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("Rating.plural")));
		
		JTable taskCacheTable = new JTable(taskCacheModel) {
		    @Override
            public String getToolTipText(MouseEvent e) {
                return ToolTipTableModel.extractToolTip(e, this);
            }
		};
		taskCacheTable.setDefaultRenderer(Double.class,
						new NumberCellRenderer(2));
		taskCacheTable.setPreferredScrollableViewportSize(new Dimension(225, 120));
		taskCacheTable.getColumnModel().getColumn(0).setPreferredWidth(170);
		taskCacheTable.getColumnModel().getColumn(1).setPreferredWidth(20);
		// Add sorting
		taskCacheTable.setAutoCreateRowSorter(true);
		taskCacheTable.setRowSelectionAllowed(true);
				
		// Add a scrolled window and center it with the table
		JScrollPane scroller = new JScrollPane(taskCacheTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		taskCachePanel.add(scroller, BorderLayout.CENTER);
		taskMainPanel.add(taskCachePanel, BorderLayout.CENTER);
		
		return taskMainPanel;
	}
	
	
	@Override
	public void entityUpdate(EntityEvent event) {
		switch (event.getType()) {
			case EntityEventType.TASK_PHASE_EVENT, EntityEventType.TASK_SUBTASK_EVENT,
							TaskManager.TASK_EVENT, EntityEventType.TASK_ENDED_EVENT:
				updateTaskPanel();
				break;
			case EntityEventType.MISSION_EVENT, Mission.PHASE_EVENT:
				updateMissionPanel();
				break;
			default:
				// Do nothing
				break;
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	private void updateMissionPanel() {
		var worker = getEntity();
		Mission mission = worker.getMission();

		// Change the mission tracked
		if ((trackedMission == null && mission != null) ||
				(trackedMission != null && !trackedMission.equals(mission))) {
			if (trackedMission != null) {
				trackedMission.removeEntityListener(this);
			}
			trackedMission = mission;
			if (trackedMission != null) {
				trackedMission.addEntityListener(this);
			}
		}
		
		TaskManager taskManager = worker.getTaskManager();
		MissionRating selected = null;
		
		if (worker instanceof Person) {
			selected = ((PersonTaskManager)taskManager).getSelectedMission();
			
			if (selected != null) {
				missionScore.setText(Math.round(selected.getScore().getScore() * 100.0)/100.0 + "");
			}
			missionCacheModel.update(worker);
		}

		missionLabel.setEntity(mission);

		String newMissionPhase = (mission != null) ? mission.getPhaseDescription() : "";
		if (!missionPhaseCache.equals(newMissionPhase)) {
			missionPhaseCache = newMissionPhase;
			missionPhase.setText(Conversion.trim(newMissionPhase, MAX_LABEL));
		}
	}

	/**
	 * Remove the trackign on mission when destroying the panel.
	 */
	@Override
	public void destroy() {
		if (trackedMission != null) {
			trackedMission.removeEntityListener(this);
		}
		super.destroy();
	}

	/**
	 * Updates the info on this panel.
	 */
	private void updateTaskPanel() {
		var worker = getEntity();		
		TaskManager taskManager = worker.getTaskManager();

		boolean addLine = false;
		StringBuilder prefix = new StringBuilder();
		StringBuilder newTaskText = new StringBuilder();
		for (Task t : taskManager.getTaskStack()) {
			if (addLine) {
				newTaskText.append("\n");
			}
			newTaskText.append(prefix);
			var phase = t.getPhase();
			if (phase != null) {
				newTaskText.append(phase.getName()).append(" - ");
			}
			newTaskText.append(t.getDescription());
			prefix.append('-');
			addLine = true;
		}
		String newContent = newTaskText.toString();
		if (!newContent.equals(taskStack.getText())) {
			taskStack.setText(newContent);
		
			// Refresh task cache
			CacheCreator<TaskJob> newCache = taskManager.getLatestTaskProbability();
			taskCacheModel.setCache(newCache);
		}

		// Pending text
		String newPendingText = taskManager.getPendingTasks().stream()
								.map(s -> s.job().getName() + " @ " + s.when().getTruncatedDateTimeStamp())
								.collect(Collectors.joining("\n"));
		if (!newPendingText.equals(pendingTasks.getText())) {
			pendingTasks.setText(newPendingText);
		}

		// Task has changed so update the score
		var scoreLabel = "Directly Assigned";
		String scoreTooltip = null; 
		RatingScore score = taskManager.getScore();
		if (score != null) {
			scoreLabel = StyleManager.DECIMAL_PLACES2.format(score.getScore());
			scoreTooltip = HTML + RatingScoreRenderer.getHTMLFragment(score) + END_HTML;

		}

		taskScoreTextArea.setText(Conversion.trim(scoreLabel, MAX_LABEL));
		taskScoreTextArea.setToolTipText(scoreTooltip);
	}

	@SuppressWarnings("serial")
	private static class MissionCacheModel extends AbstractTableModel
			implements ToolTipTableModel {
		
		private List<MissionRating> missions = Collections.emptyList();

		void update(Worker worker) {
			
			if (missions != null && worker instanceof Person) {
				List<MissionRating> newMissions = ((PersonTaskManager)(worker.getTaskManager())).getMissionProbCache();
				if (!missions.equals(newMissions)) {
					missions = newMissions;
					fireTableDataChanged();
				}
			}
			else {
				missions = Collections.emptyList();
			}

		}

		/**
		 * Gets the html output of a list of rating scores. 
		 * 
		 * @param rowIndex
		 * @param colIndex
		 * @return
		 */
		@Override
		public String getToolTipAt(int rowIndex, int colIndex) {
			if ((colIndex == 1) && (rowIndex < missions.size())) {
				var t = missions.get(rowIndex);
				return HTML + RatingScoreRenderer.getHTMLFragment(t.getScore()) + END_HTML;

			}
			return null;
		}

		@Override
		public int getRowCount() {
			if (missions != null)
				return missions.size();
			return 0;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			MissionRating mr = null;
			
			if (missions != null) {
				mr = missions.get(rowIndex);

				if (columnIndex == 0) {
					return mr.getName();
				}
				else if (columnIndex == 1) {
					return mr.getScore().getScore();
				}
			}

			return mr;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return String.class;
			}
			return Double.class;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return DESCRIPTION;
			}
			return SCORE;
		}
	}
	
	private static class TaskCacheModel extends AbstractTableModel
			implements ToolTipTableModel {
		private List<TaskJob> tasks = Collections.emptyList();

		void setCache(CacheCreator<TaskJob> newCache) {
			if (newCache != null) {
				tasks = newCache.getCache();
			}
			else {
				tasks = Collections.emptyList();
			}

			fireTableDataChanged();
		}

		@Override
		public String getToolTipAt(int rowIndex, int colIndex) {
			if ((colIndex == 1) && (rowIndex < tasks.size())) {
				var t = tasks.get(rowIndex);
				return HTML + RatingScoreRenderer.getHTMLFragment(t.getScore()) + END_HTML;

			}
			return null;
		}

		@Override
		public int getRowCount() {
			return tasks.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			TaskJob job = tasks.get(rowIndex);

			if (columnIndex == 0) {
				return job.getName();
			}
			else if (columnIndex == 1) {
				return job.getScore().getScore();
			}

			return null;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return String.class;
			}
			return Double.class;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return DESCRIPTION;
			}
			return SCORE;
		}
	}
}
