/*
 * Mars Simulation Project
 * TabPanelActivity.java
 * @date 2024-08-04
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.util.MissionRating;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.PersonTaskManager;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskCache;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.NumberCellRenderer;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.monitor.PersonTableModel;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.RatingScoreRenderer;

/**
 * The TabPanelActivity is a tab panel for a person's current tasks and
 * activities
 */
@SuppressWarnings("serial")
public class TabPanelActivity extends TabPanel implements ActionListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelActivity.class.getName());

	private static final String TASK_ICON = "task";

	private static final int MAX_LABEL = 30;
	private static final String EXTRA = "...";

	/** data cache */
	private String missionTextCache = "";
	/** data cache */
	private String missionPhaseCache = "";

	private JLabel missionTextArea;
	private JLabel missionPhaseTextArea;
	private JLabel missionScoreTextArea;
	private JLabel taskScoreTextArea;

	private JButton monitorButton;
	private JButton missionButton;

	private Worker worker;

	private JTextArea taskStack;
//	private JTextArea missionStack;
	
	private TaskCacheModel taskCacheModel;
	private MissionCacheModel missionCacheModel;
	
	private JTextArea pendingTasks;

	/**
	 * Constructor.
	 *
	 * @param unit    {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelActivity(Worker unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelActivity.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(TASK_ICON),	
			Msg.getString("TabPanelActivity.title"), //$NON-NLS-1$
			desktop
		);

		this.worker = unit;
	}

	@Override
	protected void buildUI(JPanel content) {

		JPanel topPanel = new JPanel(new BorderLayout());
		content.add(topPanel, BorderLayout.NORTH);

		// Create a task main panel
		JPanel taskMainPanel = createTaskPanel();
		topPanel.add(taskMainPanel, BorderLayout.SOUTH);		

		// Create a pending task panel
		JPanel pendingTaskPanel = new JPanel(new BorderLayout(1, 3));
		pendingTasks = new JTextArea(2, 30);
		pendingTaskPanel.add(pendingTasks, BorderLayout.CENTER);
		addBorder(pendingTaskPanel, "Pending Task");
		taskMainPanel.add(pendingTaskPanel, BorderLayout.SOUTH);
		
		// Prepare mission button panel.
		JPanel missionButtonPanel = new JPanel(new FlowLayout());
		topPanel.add(missionButtonPanel, BorderLayout.CENTER);

		// Prepare mission tool button.
		missionButton = new JButton(ImageLoader.getIconByName(MissionWindow.ICON)); //$NON-NLS-1$
		missionButton.setMargin(new Insets(1, 1, 1, 1));
		missionButton.setToolTipText(Msg.getString("TabPanelActivity.tooltip.mission"));
		missionButton.addActionListener(this);
		missionButtonPanel.add(missionButton);

		// Prepare mission monitor button
		monitorButton = new JButton(ImageLoader.getIconByName(MonitorWindow.ICON)); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.setToolTipText(Msg.getString("TabPanelActivity.tooltip.monitor"));
		monitorButton.addActionListener(this);
		missionButtonPanel.add(monitorButton);
		
		// Create a mission main panel
		JPanel missionMainPanel = createMissionPanel();
		topPanel.add(missionMainPanel, BorderLayout.NORTH);

		update();
	}

	/**
	 * Creates a mission panel.
	 * 
	 * @return
	 */
	private JPanel createMissionPanel() {
		JPanel missionMainPanel = new JPanel(new BorderLayout(1, 3));
		addBorder(missionMainPanel, "Mission");

		JPanel missionSubPanel = new JPanel(new BorderLayout(1, 3));
		missionMainPanel.add(missionSubPanel, BorderLayout.NORTH);

		// Prepare attribute panel
		AttributePanel missionAttributePanel = new AttributePanel(3);
		missionSubPanel.add(missionAttributePanel, BorderLayout.NORTH);
		missionTextArea = missionAttributePanel.addRow(Msg.getString("TabPanelActivity.missionDesc"), ""); //$NON-NLS-1$
		missionPhaseTextArea = missionAttributePanel.addRow(Msg.getString("TabPanelActivity.missionPhase"), ""); //$NON-NLS-1$
		missionScoreTextArea = missionAttributePanel.addRow("Best Score", "");
		
//		missionStack = new JTextArea(3, 30);
//		missionStack.setToolTipText("Show the description and phase of a mission and its submission(s)");
//		missionSubPanel.add(missionStack, BorderLayout.CENTER);

		missionCacheModel = new MissionCacheModel();
		JPanel missionCachePanel = new JPanel(new BorderLayout(1, 3));
		addBorder(missionCachePanel, "Mission Rating");
		
		JTable missionCacheTable = new JTable(missionCacheModel) {
		    @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
				var sorter = getRowSorter();
				if (sorter != null) {
					rowIndex = sorter.convertRowIndexToModel(rowIndex);
				}
				MissionCacheModel model = (MissionCacheModel) getModel();
				return model.getScoreText(rowIndex, colIndex);
            }
		};
		missionCacheTable.setDefaultRenderer(Double.class,
						new NumberCellRenderer(2, true));
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
		addBorder(taskMainPanel, "Task");

		JPanel taskSubPanel = new JPanel(new BorderLayout(1, 3));
		taskMainPanel.add(taskSubPanel, BorderLayout.NORTH);

		taskStack = new JTextArea(3, 30);
		taskStack.setToolTipText("Show the description and phase of a task and its subtask(s)");
		taskSubPanel.add(taskStack, BorderLayout.NORTH);

		AttributePanel taskScorePanel = new AttributePanel(1);
		taskSubPanel.add(taskScorePanel, BorderLayout.CENTER);
		taskScoreTextArea = taskScorePanel.addRow("Best Score", "");
		
		taskCacheModel = new TaskCacheModel();
		JPanel taskCachePanel = new JPanel(new BorderLayout(1, 3));
		addBorder(taskCachePanel, "Task Rating");
		
		JTable taskCacheTable = new JTable(taskCacheModel) {
		    @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
				var sorter = getRowSorter();
				if (sorter != null) {
					rowIndex = sorter.convertRowIndexToModel(rowIndex);
				}
				TaskCacheModel model = (TaskCacheModel) getModel();
				return model.getScoreText(rowIndex, colIndex);
            }
		};
		taskCacheTable.setDefaultRenderer(Double.class,
						new NumberCellRenderer(2, true));
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
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		Mission mission = worker.getMission();
		
		TaskManager taskManager = worker.getTaskManager();
		MissionRating selected = null;
		
		/////////////// Update mission /////////////////////
		if (worker instanceof Person) {
			selected = ((PersonTaskManager)taskManager).getSelectedMission();
			
			if (selected != null) {
				missionScoreTextArea.setText(Math.round(selected.getScore().getScore() * 100.0)/100.0 + "");
			}
			
			missionCacheModel.update(worker);
		}

		String newMissionText = "";
		String newMissionPhase = "";

		// Update mission text area if necessary.
		if (mission != null)
			newMissionText = mission.getName();
		if (mission != null)
			newMissionPhase = mission.getPhaseDescription();

		if (!missionTextCache.equals(newMissionText)) {
			missionTextCache = newMissionText;
			updateLabel(missionTextArea, newMissionText);
		}

		if (!missionPhaseCache.equals(newMissionPhase)) {
			missionPhaseCache = newMissionPhase;
			updateLabel(missionPhaseTextArea, newMissionPhase);
		}
		
		// Update mission and monitor buttons.
		missionButton.setEnabled(mission != null);
		monitorButton.setEnabled(mission != null);
		
		/////////////// Update tasks /////////////////////
		
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
			TaskCache newCache = taskManager.getLatestTaskProbability();
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
			scoreTooltip = "<html>" + RatingScoreRenderer.getHTMLFragment(score) + "</html>";

		}

		updateLabel(taskScoreTextArea, scoreLabel);
		taskScoreTextArea.setToolTipText(scoreTooltip);
	}

	private static void updateLabel(JLabel label, String text) {
		if (text.length() > MAX_LABEL) {
			text = text.substring(0, MAX_LABEL - EXTRA.length()) + EXTRA;
		}
		label.setText(text);
	}

	/**
	 * Action event occurs.
	 *
	 * @param event {@link ActionEvent} the action event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();		
		Mission mission = worker.getMission();
		if (mission != null) {
			if (source == missionButton) {
				getDesktop().showDetails(mission);
			} else if (source == monitorButton) {
				try {
					getDesktop().addModel(new PersonTableModel(mission));
				} catch (Exception e) {
					logger.severe("PersonTableModel cannot be added.");
				}
			}
		}
	}

	private static class MissionCacheModel extends AbstractTableModel {
		private List<MissionRating> missions = Collections.emptyList();

		void update(Worker worker) {
			
			if (worker instanceof Person) {
				List<MissionRating> newMissions =  ((PersonTaskManager)(worker.getTaskManager())).getMissionProbCache();
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
		public String getScoreText(int rowIndex, int colIndex) {
			if ((colIndex == 1) && (rowIndex < missions.size())) {
				var t = missions.get(rowIndex);
				return "<html>" + RatingScoreRenderer.getHTMLFragment(t.getScore()) + "</html>";

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
				return "Mission Description ";
			}
			return "Score";
		}
	}
	
	private static class TaskCacheModel extends AbstractTableModel {
		private List<TaskJob> tasks = Collections.emptyList();

		void setCache(TaskCache newCache) {
			if (newCache != null) {
				tasks = newCache.getTasks();
			}
			else {
				tasks = Collections.emptyList();
			}

			fireTableDataChanged();
		}

		public String getScoreText(int rowIndex, int colIndex) {
			if ((colIndex == 1) && (rowIndex < tasks.size())) {
				var t = tasks.get(rowIndex);
				return "<html>" + RatingScoreRenderer.getHTMLFragment(t.getScore()) + "</html>";

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
				return "Task Description";
			}
			return "Score";
		}
	}
}
