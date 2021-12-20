/*
 * Mars Simulation Project
 * TabPanelActivity.java
 * @date 2021-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextArea;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The TabPanelActivity is a tab panel for a person's current tasks and
 * activities
 */
@SuppressWarnings("serial")
public class TabPanelActivity extends TabPanel implements ActionListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelActivity.class.getName());

	private static final int COL_WDITH = 16;

	private static final String DEAD_PHRASE = " " + Msg.getString("TabPanelActivity.dead.phrase"); // " (at the Moment
																									// of Death)" ;
	private static final String NONE = "None ";

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
	/** The Person instance. */
	private Person person = null;
	/** The Robot instance. */
	private Robot robot = null;

	private WebTextArea taskTextArea;
	private WebTextArea taskPhaseArea;

	private WebTextArea subTaskTextArea;
	private WebTextArea subTaskPhaseArea;

	private WebTextArea subTask2TextArea;
	private WebTextArea subTask2PhaseArea;

	private WebTextArea missionTextArea;
	private WebTextArea missionPhaseTextArea;

	private WebButton monitorButton;
	private WebButton missionButton;

	/**
	 * Constructor.
	 *
	 * @param unit    {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelActivity(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(Msg.getString("TabPanelActivity.title"), //$NON-NLS-1$
				null, Msg.getString("TabPanelActivity.tooltip"), //$NON-NLS-1$
				unit, desktop);

		if (unit instanceof Person) {
			person = (Person) unit;
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
		}

	}

	@Override
	protected void buildUI(JPanel content) {
		boolean dead = false;

		Mind mind = null;
		BotMind botMind = null;

		TaskManager taskManager = null;

		DeathInfo deathInfo = null;
		Unit unit = getUnit();
		if (unit instanceof Person) {
			person = (Person) unit;
			mind = person.getMind();
			taskManager = mind.getTaskManager();
			dead = person.getPhysicalCondition().isDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			botMind = robot.getBotMind();
			taskManager = botMind.getBotTaskManager();
			dead = robot.getSystemCondition().isInoperable();
			// deathInfo = robot.getSystemCondition().getDeathDetails();
		}

		// Prepare activity panel
		WebPanel activityPanel = new WebPanel(new BorderLayout(0, 0));
		content.add(activityPanel, BorderLayout.NORTH);

		// Prepare task top panel
		WebPanel taskTopPanel = new WebPanel(new GridLayout(6, 1, 0, 0));
//		taskTopPanel.setBorder(new MarsPanelBorder());
		activityPanel.add(taskTopPanel, BorderLayout.CENTER);

		/////////////////////////////////////////////////////////////////////////

		// Prepare current task panel
		WebPanel currentTaskPanel = new WebPanel(new BorderLayout(0, 0));
		taskTopPanel.add(currentTaskPanel);

		// Prepare task label
		WebLabel taskLabel = new WebLabel(Msg.getString("TabPanelActivity.task"), WebLabel.LEFT); //$NON-NLS-1$
		currentTaskPanel.add(taskLabel, BorderLayout.NORTH);

		// Prepare task text area
		if (dead)
			taskTextCache = deathInfo.getTask();
		else {
			String t = taskManager.getTaskDescription(false);
			if (t != null)
//						&& !t.toLowerCase().contains("walk"))
				taskTextCache = t;
		}

		taskTextArea = new WebTextArea(2, COL_WDITH);
		taskTextArea.setText(taskTextCache);
		taskTextArea.setLineWrap(true);
		taskTextArea.setEditable(false);
		currentTaskPanel.add(new WebScrollPane(taskTextArea), BorderLayout.CENTER);

		// Prepare task phase panel
		WebPanel taskPhasePanel = new WebPanel(new BorderLayout(0, 0));
		taskTopPanel.add(taskPhasePanel);

		// Prepare task phase label
		WebLabel taskPhaseLabel = new WebLabel(Msg.getString("TabPanelActivity.taskPhase"), WebLabel.LEFT); //$NON-NLS-1$
		taskPhasePanel.add(taskPhaseLabel, BorderLayout.NORTH);

		// Prepare task phase text area
		if (dead) {
			taskPhaseCache = deathInfo.getTaskPhase();
		} else {

			TaskPhase phase = taskManager.getPhase();
			if (phase != null) {
				taskPhaseCache = phase.getName();
			} else {
				taskPhaseCache = "";
			}
		}

		taskPhaseArea = new WebTextArea(2, COL_WDITH);
		taskPhaseArea.setText(taskPhaseCache);
		taskPhaseArea.setLineWrap(true);
		taskPhaseArea.setEditable(false);
		taskPhasePanel.add(new WebScrollPane(taskPhaseArea), BorderLayout.CENTER);

		/////////////////////////////////////////////////////////////////////////

		// Prepare sub task 1 panel
		WebPanel subTaskPanel = new WebPanel(new BorderLayout(0, 0));
		taskTopPanel.add(subTaskPanel);

		// Prepare sub task 1 label
		WebLabel subtaskLabel = new WebLabel(Msg.getString("TabPanelActivity.subTask"), WebLabel.LEFT); //$NON-NLS-1$
		subTaskPanel.add(subtaskLabel, BorderLayout.NORTH);

		// Prepare sub task 1 text area
		if (dead)
			subTaskTextCache = deathInfo.getSubTask();
		else {
			String t = taskManager.getSubTaskDescription();
			if (t != null)
//						&& !t.toLowerCase().contains("walk"))
				subTaskTextCache = t;
			else
				subTaskTextCache = "";
		}

		subTaskTextArea = new WebTextArea(2, COL_WDITH);
		subTaskTextArea.setText(subTaskTextCache);
		subTaskTextArea.setLineWrap(true);
		subTaskTextArea.setEditable(false);
		subTaskPanel.add(new WebScrollPane(subTaskTextArea), BorderLayout.CENTER);

		// Prepare sub task 1 phase panel
		WebPanel subTaskPhasePanel = new WebPanel(new BorderLayout(0, 0));
		taskTopPanel.add(subTaskPhasePanel);

		// Prepare sub task 1 phase label
		WebLabel subTaskPhaseLabel = new WebLabel(Msg.getString("TabPanelActivity.subTaskPhase"), WebLabel.LEFT); //$NON-NLS-1$
		subTaskPhasePanel.add(subTaskPhaseLabel, BorderLayout.NORTH);

		// Prepare sub task 1 phase text area
		if (dead) {
			subTaskPhaseCache = deathInfo.getSubTaskPhase();
		} else if (subTaskTextCache.equals("")) {
			subTaskPhaseCache = "";
		} else {

			TaskPhase phase = taskManager.getSubTaskPhase();

			if (phase != null) {
				subTaskPhaseCache = phase.getName();
			} else {
				subTaskPhaseCache = "";
			}
		}

		subTaskPhaseArea = new WebTextArea(2, COL_WDITH);
		subTaskPhaseArea.setText(subTaskPhaseCache);
		subTaskPhaseArea.setLineWrap(true);
		subTaskPhaseArea.setEditable(false);
		subTaskPhasePanel.add(new WebScrollPane(subTaskPhaseArea), BorderLayout.CENTER);

		/////////////////////////////////////////////////////////////////////////

		// Prepare sub task 2 panel
		WebPanel subTask2Panel = new WebPanel(new BorderLayout(0, 0));
		taskTopPanel.add(subTask2Panel);

		// Prepare sub task 2 label
		WebLabel subtask2Label = new WebLabel(Msg.getString("TabPanelActivity.subTask2"), WebLabel.LEFT); //$NON-NLS-1$
		subTask2Panel.add(subtask2Label, BorderLayout.NORTH);

		// Prepare sub task 2 text area
		if (dead)
			subTask2TextCache = deathInfo.getSubTask2();
		else {
			String t = taskManager.getSubTask2Description();
			if (t != null)
//						&& !t.toLowerCase().contains("walk"))
				subTask2TextCache = t;
			else
				subTask2TextCache = "";
		}

		subTask2TextArea = new WebTextArea(2, COL_WDITH);
		subTask2TextArea.setText(subTask2TextCache);
		subTask2TextArea.setLineWrap(true);
		subTask2TextArea.setEditable(false);
		subTask2Panel.add(new WebScrollPane(subTask2TextArea), BorderLayout.CENTER);

		// Prepare sub task 1 phase panel
		WebPanel subTask2PhasePanel = new WebPanel(new BorderLayout(0, 0));
		taskTopPanel.add(subTask2PhasePanel);

		// Prepare sub task 1 phase label
		WebLabel subTask2PhaseLabel = new WebLabel(Msg.getString("TabPanelActivity.subTask2Phase"), WebLabel.LEFT); //$NON-NLS-1$
		subTask2PhasePanel.add(subTask2PhaseLabel, BorderLayout.NORTH);

		// Prepare sub task 1 phase text area
		if (dead) {
			subTask2PhaseCache = deathInfo.getSubTask2Phase();
		} else if (subTask2TextCache.equals("")) {
			subTask2PhaseCache = "";
		} else {
			TaskPhase phase = taskManager.getSubTask2Phase();

			if (phase != null) {
				subTask2PhaseCache = phase.getName();
			} else {
				subTask2PhaseCache = "";
			}
		}

		subTask2PhaseArea = new WebTextArea(2, COL_WDITH);
		subTask2PhaseArea.setText(subTask2PhaseCache);
		subTask2PhaseArea.setLineWrap(true);
		subTask2PhaseArea.setEditable(false);
		subTask2PhasePanel.add(new WebScrollPane(subTask2PhaseArea), BorderLayout.CENTER);

		/////////////////////////////////////////////////////////////////////////

		// Prepare mission top panel
		WebPanel missionTopPanel = new WebPanel(new BorderLayout(0, 0));// new FlowLayout(FlowLayout.CENTER));
		// missionTopPanel.setBorder(new MarsPanelBorder());
		activityPanel.add(missionTopPanel, BorderLayout.SOUTH);

		// Prepare mission center panel
		WebPanel missionCenterPanel = new WebPanel(new BorderLayout(0, 0));// new FlowLayout(FlowLayout.CENTER));
		// missionCenterPanel.setBorder(new MarsPanelBorder());

//		missionTopPanel.add(new WebPanel(), BorderLayout.NORTH);
		missionTopPanel.add(missionCenterPanel, BorderLayout.CENTER);
//		missionTopPanel.add(new WebPanel(), BorderLayout.SOUTH);
//		missionTopPanel.add(new WebPanel(), BorderLayout.EAST);
//		missionTopPanel.add(new WebPanel(), BorderLayout.WEST);

		// Prepare mission panel
		WebPanel missionTextPanel = new WebPanel(new BorderLayout(0, 0));
		// missionLeftPanel.add(missionTextPanel);
		missionCenterPanel.add(missionTextPanel, BorderLayout.CENTER);

		// Prepare mission label
		WebLabel missionLabel = new WebLabel(Msg.getString("TabPanelActivity.missionDesc"), WebLabel.LEFT); //$NON-NLS-1$
		missionTextPanel.add(missionLabel, BorderLayout.NORTH);

		// Prepare mission text area

		String missionText = "";

		if (person != null) {

			if (dead)
				missionText = deathInfo.getMission();

			else if (mind.getMission() != null) {
				missionText = mind.getMission().getDescription();
				// if (missionText == null)
				// missionText = "";
			}
		} else if (robot != null) {

			if (dead)
				missionText = deathInfo.getMission();

			else if (botMind.getMission() != null) {
				missionText = botMind.getMission().getDescription();
				// if (missionText == null)
				// missionText = "";
			}
		}

		missionTextArea = new WebTextArea(2, COL_WDITH);
		missionTextArea.setText(missionText);
		missionTextArea.setLineWrap(true);
		missionTextArea.setEditable(false);
		missionTextPanel.add(new WebScrollPane(missionTextArea), BorderLayout.CENTER);

		// Prepare mission phase panel
		WebPanel missionPhasePanel = new WebPanel(new BorderLayout(0, 0));
		// missionLeftPanel.add(missionPhasePanel);
		missionCenterPanel.add(missionPhasePanel, BorderLayout.SOUTH);

		// Prepare mission phase label
		WebLabel missionPhaseLabel = new WebLabel(Msg.getString("TabPanelActivity.missionPhase"), WebLabel.LEFT); //$NON-NLS-1$
		missionPhasePanel.add(missionPhaseLabel, BorderLayout.NORTH);

		String missionPhaseText = "";
		if (person != null) {
			// Prepare mission phase text area
			if (dead)
				missionPhaseText = deathInfo.getMissionPhase();
			else if (mind.getMission() != null)
				missionPhaseText = mind.getMission().getPhaseDescription();
		} else if (robot != null) {
			// Prepare mission phase text area
			if (dead)
				missionPhaseText = deathInfo.getMissionPhase();
			else if (botMind.getMission() != null)
				missionPhaseText = botMind.getMission().getPhaseDescription();
		}

		missionPhaseTextArea = new WebTextArea(2, COL_WDITH);
		// if (missionPhase.equals(""))
		missionPhaseTextArea.setText(missionPhaseText);
		missionPhaseTextArea.setLineWrap(true);
		missionPhaseTextArea.setEditable(false);
		missionPhasePanel.add(new WebScrollPane(missionPhaseTextArea), BorderLayout.CENTER);

		// Prepare mission button panel.
		WebPanel missionButtonPanel = new WebPanel(new GridLayout(1, 6, 10, 10));
		missionButtonPanel.setSize(480, 20);
		missionCenterPanel.add(missionButtonPanel, BorderLayout.NORTH);

		// Prepare mission tool button.
		missionButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.mission"))); //$NON-NLS-1$
		missionButton.setSize(20, 20);
		missionButton.setMargin(new Insets(1, 1, 1, 1));
		TooltipManager.setTooltip(missionButton, Msg.getString("TabPanelActivity.tooltip.mission"), TooltipWay.down);
		// missionButton.setToolTipText(Msg.getString("TabPanelActivity.tooltip.mission"));
		// //$NON-NLS-1$
		// Msg.getString("TabPanelActivity.tooltip.mission")); //$NON-NLS-1$
		missionButton.addActionListener(this);

		if (person != null) {
			missionButton.setEnabled(mind.getMission() != null);
		} else if (robot != null) {
			missionButton.setEnabled(botMind.getMission() != null);
		}

		missionButtonPanel.add(new WebPanel(new FlowLayout(5, 5, FlowLayout.CENTER)));
		missionButtonPanel.add(new WebPanel(new FlowLayout(5, 5, FlowLayout.CENTER)));
		missionButtonPanel.add(missionButton);

		// Prepare mission monitor button
		monitorButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setSize(20, 20);
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		TooltipManager.setTooltip(monitorButton, Msg.getString("TabPanelActivity.tooltip.monitor"), TooltipWay.down);
		monitorButton.addActionListener(this);

		if (person != null) {
			monitorButton.setEnabled(mind.getMission() != null);
		} else if (robot != null) {
			monitorButton.setEnabled(botMind.getMission() != null);
		}

		missionButtonPanel.add(monitorButton);
		missionButtonPanel.add(new WebPanel(new FlowLayout(5, 5, FlowLayout.CENTER)));
		missionButtonPanel.add(new WebPanel(new FlowLayout(5, 5, FlowLayout.CENTER)));

	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		Person person = null;
		Robot robot = null;
		Mind mind = null;
		BotMind botMind = null;
		TaskManager taskManager = null;
		boolean dead = false;
		DeathInfo deathInfo = null;
		Unit unit = getUnit();
		
		if (unit instanceof Person) {
			person = (Person) unit;
			mind = person.getMind();
			dead = person.getPhysicalCondition().isDead();// .isDeclaredDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			botMind = robot.getBotMind();
			dead = robot.getSystemCondition().isInoperable();
			// deathInfo = robot.getSystemCondition().getDeathDetails();
		}

		Mission mission = null;

		String newTaskText = "";
		String newTaskPhase = "";

		String newSubTaskText = "";
		String newSubTaskPhase = "";

		String newSubTask2Text = "";
		String newSubTask2Phase = "";

		String newMissionText = "";
		String newMissionPhase = "";

		// Prepare task text area
		if (dead) {
			String t = deathInfo.getTask();
			String tp = deathInfo.getTaskPhase();
			String st = deathInfo.getSubTask();
			String stp = deathInfo.getSubTaskPhase();
			String st2 = deathInfo.getSubTask2();
			String st2p = deathInfo.getSubTask2Phase();

			if (t == null || t.equals(""))
				newTaskText = NONE + DEAD_PHRASE;
			else
				newTaskText = t + DEAD_PHRASE;

			if (tp == null || tp.equals(""))
				newTaskPhase = NONE + DEAD_PHRASE;
			else
				newTaskPhase = tp + DEAD_PHRASE;

			if (st == null || st.equals(""))
				newSubTaskText = NONE + DEAD_PHRASE;
			else
				newSubTaskText = st + DEAD_PHRASE;

			if (stp == null || stp.equals(""))
				newSubTaskPhase = NONE + DEAD_PHRASE;
			else
				newSubTaskPhase = stp + DEAD_PHRASE;

			if (st2 == null || st2.equals(""))
				newSubTask2Text = NONE + DEAD_PHRASE;
			else
				newSubTask2Text = st + DEAD_PHRASE;

			if (st2p == null || st2p.equals(""))
				newSubTask2Phase = NONE + DEAD_PHRASE;
			else
				newSubTask2Phase = stp + DEAD_PHRASE;
		}

		else {

			if (person != null) {
				taskManager = mind.getTaskManager();
				if (mind.hasActiveMission())
					mission = mind.getMission();

			} else if (robot != null) {
				taskManager = botMind.getBotTaskManager();
				if (botMind.hasActiveMission())
					mission = botMind.getMission();
			}


			newTaskText = taskManager.getTaskDescription(false);
			newSubTaskText = taskManager.getSubTaskDescription();
			newSubTask2Text = taskManager.getSubTask2Description();


			TaskPhase taskPhase = taskManager.getPhase();
			TaskPhase subTaskPhase = taskManager.getSubTaskPhase();
			TaskPhase subTask2Phase = taskManager.getSubTask2Phase();

			if (taskPhase != null) {
				newTaskPhase = taskPhase.getName();
			} else {
				newTaskPhase = "";
			}

			if (subTaskPhase != null) {
				newSubTaskPhase = subTaskPhase.getName();
			} else {
				newSubTaskPhase = "";
			}

			if (subTask2Phase != null) {
				newSubTask2Phase = subTask2Phase.getName();
			} else {
				newSubTask2Phase = "";
			}

		}

		if (!taskTextCache.equals(newTaskText)) {
			taskTextCache = newTaskText;
			taskTextArea.setText(newTaskText);
		}

		if (taskTextCache.equals(""))
			taskPhaseArea.setText("");

		else if (!taskPhaseCache.equals(newTaskPhase)) {
			taskPhaseCache = newTaskPhase;
			taskPhaseArea.setText(newTaskPhase);
		}


		if (!subTaskTextCache.equals(newSubTaskText)) {
			subTaskTextCache = newSubTaskText;
			subTaskTextArea.setText(newSubTaskText);
		}

		if (subTaskTextCache.equals(""))
			subTaskPhaseArea.setText("");

		else if (!subTaskPhaseCache.equals(newSubTaskPhase)) {
			subTaskPhaseCache = newSubTaskPhase;
			subTaskPhaseArea.setText(newSubTaskPhase);
		}

		if (!subTask2TextCache.equals(newSubTask2Text)) {
			subTask2TextCache = newSubTask2Text;
			subTask2TextArea.setText(newSubTask2Text);
		}

		if (subTask2TextCache.equals(""))
			subTask2PhaseArea.setText("");

		else if (!subTask2PhaseCache.equals(newSubTask2Phase)) {
			subTask2PhaseCache = newSubTask2Phase;
			subTask2PhaseArea.setText(newSubTask2Phase);
		}

		// Update mission text area if necessary.
		if (dead) {
			String m = deathInfo.getMission();
			String mp = deathInfo.getMissionPhase();

			if (m == null || m.equals(""))
				newMissionText = NONE + DEAD_PHRASE;
			else
				newMissionText = m + DEAD_PHRASE;

			if (mp == null || mp.equals(""))
				newMissionPhase = NONE + DEAD_PHRASE;
			else
				newMissionPhase = mp + DEAD_PHRASE;
		}

		else {

			if (mission != null)
				newMissionText = mission.getDescription();
			else
				newMissionText = ""; //$NON-NLS-1$

			if (mission != null)
				newMissionPhase = mission.getPhaseDescription();
			else
				newMissionPhase = ""; //$NON-NLS-1$
		}

		if (!missionTextCache.equals(newMissionText)) {
			missionTextCache = newMissionText;
			missionTextArea.setText(newMissionText);
		}

		if (!missionPhaseCache.equals(newMissionPhase)) {
			missionPhaseCache = newMissionPhase;
			missionPhaseTextArea.setText(newMissionPhase);
		}

		// Update mission phase text area if necessary.
//		if (dead) {
//			if (deathInfo.getMissionPhase() == null)
//				missionPhaseCache = "None " + DEAD_PHRASE;
//			else
//				missionPhaseCache = deathInfo.getMissionPhase() + DEAD_PHRASE;
//		}
//		else {
//			if (mission != null)
//				missionPhaseCache = mission.getPhaseDescription();
//			else missionPhaseCache = ""; //$NON-NLS-1$
//		}
//
//		if ((missionPhaseCache != null) && !missionPhaseCache.equals(missionPhaseTextArea.getText()))
//			missionPhaseTextArea.setText(missionPhaseCache);

		// Update mission and monitor buttons.
		missionButton.setEnabled(mission != null);
		monitorButton.setEnabled(mission != null);
	}

	/**
	 * Action event occurs.
	 *
	 * @param event {@link ActionEvent} the action event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if ((source == missionButton) || (source == monitorButton)) {

			boolean dead = false;
			DeathInfo deathInfo = null;
			Unit unit = getUnit();
			if (unit instanceof Person) {
				Person person = (Person) unit;
				Mind mind = person.getMind();
				dead = person.getPhysicalCondition().isDead();
				deathInfo = person.getPhysicalCondition().getDeathDetails();

				if (!person.getPhysicalCondition().isDead()) {
					mind = person.getMind();
					if (mind.hasActiveMission()) {
						if (source == missionButton) {
//							((MissionWindow) desktop.getToolWindow(MissionWindow.NAME))
//									.selectMission(mind.getMission());
							getDesktop().openToolWindow(MissionWindow.NAME, mind.getMission());
						} else if (source == monitorButton) {
							try {
								getDesktop().addModel(new PersonTableModel(mind.getMission()));
							} catch (Exception e) {
								logger.severe("PersonTableModel cannot be added.");
							}
						}
					}
				}
			} else if (unit instanceof Robot) {
				Robot robot = (Robot) unit;
				BotMind botMind = robot.getBotMind();
				dead = robot.getSystemCondition().isInoperable();
				// deathInfo = robot.getSystemCondition().getDeathDetails();

				if (!robot.getSystemCondition().isInoperable()) {
					botMind = robot.getBotMind();
//					if (botMind.hasActiveMission()) {
//						if (source == missionButton) {
//							((MissionWindow) desktop.getToolWindow(MissionWindow.NAME))
//									.selectMission(botMind.getMission());
//							getDesktop().openToolWindow(MissionWindow.NAME);
//						} else if (source == monitorButton)
//							desktop.addModel(new RobotTableModel(botMind.getMission()));
//					}
				}
			}
		}
	}
}
