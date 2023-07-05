/*
 * Mars Simulation Project
 * TabPanelActivity.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

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

	/** current task text cache */
	private String taskTextCache = "";
	/** current phase text cache */
	private String taskPhaseCache = "";
	/** sub task 1 text cache */
	private String subTaskTextCache = "";
	/** sub phase 1 text cache */
	private String subTaskPhaseCache = "";
	/** sub task 2 text cache */
	private String subTask2TextCache = "";
	/** sub phase 2 text cache */
	private String subTask2PhaseCache = "";
	/** data cache */
	private String missionTextCache = "";
	/** data cache */
	private String missionPhaseCache = "";


	private JLabel taskTextArea;
	private JLabel taskPhaseArea;

	private JLabel subTaskTextArea;
	private JLabel subTaskPhaseArea;

	private JLabel subTask2TextArea;
	private JLabel subTask2PhaseArea;

	private JLabel missionTextArea;
	private JLabel missionPhaseTextArea;

	private JButton monitorButton;
	private JButton missionButton;

	private Worker worker;

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
		AttributePanel activityPanel = new AttributePanel(8);
		topPanel.add(activityPanel, BorderLayout.CENTER);

		// Prepare task labels. Create empty and then update
		taskTextArea = activityPanel.addTextField(Msg.getString("TabPanelActivity.task"), "", null); //$NON-NLS-1$
		taskPhaseArea = activityPanel.addTextField(Msg.getString("TabPanelActivity.taskPhase"), "", null); //$NON-NLS-1$
		subTaskTextArea = activityPanel.addTextField(Msg.getString("TabPanelActivity.subTask"), "", null); //$NON-NLS-1$
		subTaskPhaseArea = activityPanel.addTextField(Msg.getString("TabPanelActivity.subTaskPhase"), "", null); //$NON-NLS-1$
		subTask2TextArea = activityPanel.addTextField(Msg.getString("TabPanelActivity.subTask2"), "", null); //$NON-NLS-1$
		subTask2PhaseArea = activityPanel.addTextField(Msg.getString("TabPanelActivity.subTask2Phase"), "", null); //$NON-NLS-1$
		missionTextArea = activityPanel.addTextField(Msg.getString("TabPanelActivity.missionDesc"), "", null); //$NON-NLS-1$
		missionPhaseTextArea = activityPanel.addTextField(Msg.getString("TabPanelActivity.missionPhase"), "", null); //$NON-NLS-1$

		// Prepare mission button panel.
		JPanel missionButtonPanel = new JPanel(new FlowLayout());
		topPanel.add(missionButtonPanel, BorderLayout.SOUTH);

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

		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		TaskManager taskManager = worker.getTaskManager();

		Mission mission = worker.getMission();

		String newTaskPhase = "";

		String newSubTaskPhase = "";

		String newSubTask2Phase = "";

		String newMissionText = "";
		String newMissionPhase = "";

		String newTaskText = taskManager.getTaskDescription(false);
		String newSubTaskText = taskManager.getSubTaskDescription();
		String newSubTask2Text = taskManager.getSubTask2Description();

		TaskPhase taskPhase = taskManager.getPhase();
		TaskPhase subTaskPhase = taskManager.getSubTaskPhase();
		TaskPhase subTask2Phase = taskManager.getSubTask2Phase();

		if (taskPhase != null) {
			newTaskPhase = taskPhase.getName();
		} 
		if (subTaskPhase != null) {
			newSubTaskPhase = subTaskPhase.getName();
		} 
		if (subTask2Phase != null) {
			newSubTask2Phase = subTask2Phase.getName();
		} 

		if (!taskTextCache.equals(newTaskText)) {
			taskTextCache = newTaskText;
			updateLabel(taskTextArea, newTaskText);
		}

		if (taskTextCache.equals(""))
			updateLabel(taskPhaseArea, "");

		else if (!taskPhaseCache.equals(newTaskPhase)) {
			taskPhaseCache = newTaskPhase;
			updateLabel(taskPhaseArea, newTaskPhase);
		}


		if (!subTaskTextCache.equals(newSubTaskText)) {
			subTaskTextCache = newSubTaskText;
			updateLabel(subTaskTextArea, newSubTaskText);
		}

		if (subTaskTextCache.equals(""))
			updateLabel(subTaskPhaseArea,"");

		else if (!subTaskPhaseCache.equals(newSubTaskPhase)) {
			subTaskPhaseCache = newSubTaskPhase;
			updateLabel(subTaskPhaseArea, newSubTaskPhase);
		}

		if (!subTask2TextCache.equals(newSubTask2Text)) {
			subTask2TextCache = newSubTask2Text;
			updateLabel(subTask2TextArea, newSubTask2Text);
		}

		if (subTask2TextCache.equals(""))
			updateLabel(subTask2PhaseArea, "");

		else if (!subTask2PhaseCache.equals(newSubTask2Phase)) {
			subTask2PhaseCache = newSubTask2Phase;
			updateLabel(subTask2PhaseArea, newSubTask2Phase);
		}

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
		label.setToolTipText(text);
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
}
