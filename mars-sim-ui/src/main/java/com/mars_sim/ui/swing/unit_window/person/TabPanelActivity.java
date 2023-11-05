/*
 * Mars Simulation Project
 * TabPanelActivity.java
 * @date 2023-09-11
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
import com.mars_sim.core.person.ai.mission.Mission;
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


	private JLabel scoreTextArea;

	private JLabel missionTextArea;
	private JLabel missionPhaseTextArea;

	private JButton monitorButton;
	private JButton missionButton;

	private Worker worker;

	private JTextArea taskStack;

	private TaskCacheModel cacheModel;

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

		// Prepare activity panel
		AttributePanel missionPanel = new AttributePanel(2);
		addBorder(missionPanel, "Mission");
		topPanel.add(missionPanel, BorderLayout.NORTH);
		
		missionTextArea = missionPanel.addTextField(Msg.getString("TabPanelActivity.missionDesc"), "", null); //$NON-NLS-1$
		missionPhaseTextArea = missionPanel.addTextField(Msg.getString("TabPanelActivity.missionPhase"), "", null); //$NON-NLS-1$

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
		
		JPanel taskPanel = new JPanel(new BorderLayout(1, 3));
		addBorder(taskPanel, "Task");
		topPanel.add(taskPanel, BorderLayout.SOUTH);
		taskStack = new JTextArea(3, 30);
		taskStack.setToolTipText("Show the description and phase of a task and its subtask(s)");
		taskPanel.add(taskStack, BorderLayout.CENTER);

		AttributePanel scorePanel = new AttributePanel(1);
		taskPanel.add(scorePanel, BorderLayout.NORTH);
		scoreTextArea = scorePanel.addTextField("Score", "", null);

		cacheModel = new TaskCacheModel();
		JPanel cachePanel = new JPanel(new BorderLayout(1, 3));
		addBorder(cachePanel, "Task Choices");
		JTable cacheTable = new JTable(cacheModel) {
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
		cacheTable.setDefaultRenderer(Double.class,
						new NumberCellRenderer(2, true));
		cacheTable.setPreferredScrollableViewportSize(new Dimension(225, 150));
		cacheTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		cacheTable.getColumnModel().getColumn(1).setPreferredWidth(150);

		// Add a scrolled window and center it with the table
		JScrollPane scroller = new JScrollPane(cacheTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		cachePanel.add(scroller, BorderLayout.CENTER);
		taskPanel.add(cachePanel, BorderLayout.SOUTH);
		
		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		TaskManager taskManager = worker.getTaskManager();

		Mission mission = worker.getMission();

		String prefix = "";
		StringBuilder newTaskText = new StringBuilder();
		for (Task t : taskManager.getTaskStack()) {
			if (prefix.length() > 0) {
				newTaskText.append("\n");
			}
			newTaskText.append(prefix);
			var phase = t.getPhase();
			if (phase != null) {
				newTaskText.append(phase.getName()).append(" - ");
			}
			newTaskText.append(t.getDescription());
			prefix = prefix + "-";
		}
		String newContent = newTaskText.toString();
		if (!newContent.equals(taskStack.getText())) {
			taskStack.setText(newContent);
		
			// Refresh task cache
			TaskCache newCache = taskManager.getLatestTaskProbability();
			cacheModel.setCache(newCache);
		}

		// Task has changed so update the score
		var scoreLabel = "";
		String scoreTooltip = null; 
		RatingScore score = taskManager.getScore();
		if (score != null) {
			scoreLabel = StyleManager.DECIMAL_PLACES2.format(score.getScore());
			scoreTooltip = score.getHTMLOutput();
		}

		updateLabel(scoreTextArea, scoreLabel);
		scoreTextArea.setToolTipText(scoreTooltip);

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
			if ((colIndex == 0) && (rowIndex < tasks.size())) {
				var t = tasks.get(rowIndex);
				return t.getScore().getHTMLOutput();
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
				return job.getScore().getScore();
			}
			else if (columnIndex == 1) {
				return job.getName();
			}

			return null;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return Double.class;
			}
			return String.class;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "Score";
			}
			return "Description";
		}
	}
}
