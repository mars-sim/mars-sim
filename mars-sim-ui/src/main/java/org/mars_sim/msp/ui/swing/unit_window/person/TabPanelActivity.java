/*
 * Mars Simulation Project
 * TabPanelActivity.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelActivity is a tab panel for a person's current tasks and
 * activities
 */
@SuppressWarnings("serial")
public class TabPanelActivity extends TabPanel implements ActionListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelActivity.class.getName());

	private static final String TASK_ICON = "task";
	
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

	private JTextArea taskTextArea;
	private JTextArea taskPhaseArea;

	private JTextArea subTaskTextArea;
	private JTextArea subTaskPhaseArea;

	private JTextArea subTask2TextArea;
	private JTextArea subTask2PhaseArea;

	private JTextArea missionTextArea;
	private JTextArea missionPhaseTextArea;

	private JButton monitorButton;
	private JButton missionButton;

	/**
	 * Constructor.
	 *
	 * @param unit    {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelActivity(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelActivity.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(TASK_ICON),	
			Msg.getString("TabPanelActivity.title"), //$NON-NLS-1$
			unit, desktop
		);

		if (unit.getUnitType() == UnitType.PERSON) {
			person = (Person) unit;
		} else {
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
		if (unit.getUnitType() == UnitType.PERSON) {
			person = (Person) unit;
			mind = person.getMind();
			taskManager = mind.getTaskManager();
			dead = person.getPhysicalCondition().isDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();
		} else {
			robot = (Robot) unit;
			botMind = robot.getBotMind();
			taskManager = botMind.getBotTaskManager();
			dead = robot.getSystemCondition().isInoperable();
			// deathInfo = robot.getSystemCondition().getDeathDetails();
		}

		// Prepare activity panel
		JPanel activityPanel = new JPanel(new BorderLayout(0, 0));
		content.add(activityPanel, BorderLayout.NORTH);

		// Prepare task top panel
		JPanel taskTopPanel = new JPanel(new GridLayout(6, 1, 0, 0));
//		taskTopPanel.setBorder(new MarsPanelBorder());
		activityPanel.add(taskTopPanel, BorderLayout.CENTER);

		/////////////////////////////////////////////////////////////////////////

		// Prepare current task panel
		JPanel currentTaskPanel = new JPanel(new BorderLayout(0, 0));
		taskTopPanel.add(currentTaskPanel);

		// Prepare task label
		JLabel taskLabel = new JLabel(Msg.getString("TabPanelActivity.task"), JLabel.LEFT); //$NON-NLS-1$
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

		taskTextArea = new JTextArea(2, COL_WDITH);
		taskTextArea.setText(taskTextCache);
		taskTextArea.setLineWrap(true);
		taskTextArea.setEditable(false);
		currentTaskPanel.add(new JScrollPane(taskTextArea), BorderLayout.CENTER);

		// Prepare task phase panel
		JPanel taskPhasePanel = new JPanel(new BorderLayout(0, 0));
		taskTopPanel.add(taskPhasePanel);

		// Prepare task phase label
		JLabel taskPhaseLabel = new JLabel(Msg.getString("TabPanelActivity.taskPhase"), JLabel.LEFT); //$NON-NLS-1$
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

		taskPhaseArea = new JTextArea(2, COL_WDITH);
		taskPhaseArea.setText(taskPhaseCache);
		taskPhaseArea.setLineWrap(true);
		taskPhaseArea.setEditable(false);
		taskPhasePanel.add(new JScrollPane(taskPhaseArea), BorderLayout.CENTER);

		/////////////////////////////////////////////////////////////////////////

		// Prepare sub task 1 panel
		JPanel subTaskPanel = new JPanel(new BorderLayout(0, 0));
		taskTopPanel.add(subTaskPanel);

		// Prepare sub task 1 label
		JLabel subtaskLabel = new JLabel(Msg.getString("TabPanelActivity.subTask"), JLabel.LEFT); //$NON-NLS-1$
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

		subTaskTextArea = new JTextArea(2, COL_WDITH);
		subTaskTextArea.setText(subTaskTextCache);
		subTaskTextArea.setLineWrap(true);
		subTaskTextArea.setEditable(false);
		subTaskPanel.add(new JScrollPane(subTaskTextArea), BorderLayout.CENTER);

		// Prepare sub task 1 phase panel
		JPanel subTaskPhasePanel = new JPanel(new BorderLayout(0, 0));
		taskTopPanel.add(subTaskPhasePanel);

		// Prepare sub task 1 phase label
		JLabel subTaskPhaseLabel = new JLabel(Msg.getString("TabPanelActivity.subTaskPhase"), JLabel.LEFT); //$NON-NLS-1$
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

		subTaskPhaseArea = new JTextArea(2, COL_WDITH);
		subTaskPhaseArea.setText(subTaskPhaseCache);
		subTaskPhaseArea.setLineWrap(true);
		subTaskPhaseArea.setEditable(false);
		subTaskPhasePanel.add(new JScrollPane(subTaskPhaseArea), BorderLayout.CENTER);

		/////////////////////////////////////////////////////////////////////////

		// Prepare sub task 2 panel
		JPanel subTask2Panel = new JPanel(new BorderLayout(0, 0));
		taskTopPanel.add(subTask2Panel);

		// Prepare sub task 2 label
		JLabel subtask2Label = new JLabel(Msg.getString("TabPanelActivity.subTask2"), JLabel.LEFT); //$NON-NLS-1$
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

		subTask2TextArea = new JTextArea(2, COL_WDITH);
		subTask2TextArea.setText(subTask2TextCache);
		subTask2TextArea.setLineWrap(true);
		subTask2TextArea.setEditable(false);
		subTask2Panel.add(new JScrollPane(subTask2TextArea), BorderLayout.CENTER);

		// Prepare sub task 1 phase panel
		JPanel subTask2PhasePanel = new JPanel(new BorderLayout(0, 0));
		taskTopPanel.add(subTask2PhasePanel);

		// Prepare sub task 1 phase label
		JLabel subTask2PhaseLabel = new JLabel(Msg.getString("TabPanelActivity.subTask2Phase"), JLabel.LEFT); //$NON-NLS-1$
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

		subTask2PhaseArea = new JTextArea(2, COL_WDITH);
		subTask2PhaseArea.setText(subTask2PhaseCache);
		subTask2PhaseArea.setLineWrap(true);
		subTask2PhaseArea.setEditable(false);
		subTask2PhasePanel.add(new JScrollPane(subTask2PhaseArea), BorderLayout.CENTER);

		/////////////////////////////////////////////////////////////////////////

		// Prepare mission top panel
		JPanel missionTopPanel = new JPanel(new BorderLayout(0, 0));// new FlowLayout(FlowLayout.CENTER));
		// missionTopPanel.setBorder(new MarsPanelBorder());
		activityPanel.add(missionTopPanel, BorderLayout.SOUTH);

		// Prepare mission center panel
		JPanel missionCenterPanel = new JPanel(new BorderLayout(0, 0));// new FlowLayout(FlowLayout.CENTER));
		// missionCenterPanel.setBorder(new MarsPanelBorder());

//		missionTopPanel.add(new JPanel(), BorderLayout.NORTH);
		missionTopPanel.add(missionCenterPanel, BorderLayout.CENTER);
//		missionTopPanel.add(new JPanel(), BorderLayout.SOUTH);
//		missionTopPanel.add(new JPanel(), BorderLayout.EAST);
//		missionTopPanel.add(new JPanel(), BorderLayout.WEST);

		// Prepare mission panel
		JPanel missionTextPanel = new JPanel(new BorderLayout(0, 0));
		// missionLeftPanel.add(missionTextPanel);
		missionCenterPanel.add(missionTextPanel, BorderLayout.CENTER);

		// Prepare mission label
		JLabel missionLabel = new JLabel(Msg.getString("TabPanelActivity.missionDesc"), JLabel.LEFT); //$NON-NLS-1$
		missionTextPanel.add(missionLabel, BorderLayout.NORTH);

		// Prepare mission text area

		String missionText = "";

		if (mind != null) {

			if (dead)
				missionText = deathInfo.getMission();

			else if (mind.getMission() != null) {
				missionText = mind.getMission().getName();
				// if (missionText == null)
				// missionText = "";
			}
		} else if (botMind != null) {

			if (dead)
				missionText = deathInfo.getMission();

			else if (botMind.getMission() != null) {
				missionText = botMind.getMission().getName();
				// if (missionText == null)
				// missionText = "";
			}
		}

		missionTextArea = new JTextArea(2, COL_WDITH);
		missionTextArea.setText(missionText);
		missionTextArea.setLineWrap(true);
		missionTextArea.setEditable(false);
		missionTextPanel.add(new JScrollPane(missionTextArea), BorderLayout.CENTER);

		// Prepare mission phase panel
		JPanel missionPhasePanel = new JPanel(new BorderLayout(0, 0));
		// missionLeftPanel.add(missionPhasePanel);
		missionCenterPanel.add(missionPhasePanel, BorderLayout.SOUTH);

		// Prepare mission phase label
		JLabel missionPhaseLabel = new JLabel(Msg.getString("TabPanelActivity.missionPhase"), JLabel.LEFT); //$NON-NLS-1$
		missionPhasePanel.add(missionPhaseLabel, BorderLayout.NORTH);

		String missionPhaseText = "";
		if (mind != null) {
			// Prepare mission phase text area
			if (dead)
				missionPhaseText = deathInfo.getMissionPhase();
			else if (mind.getMission() != null)
				missionPhaseText = mind.getMission().getPhaseDescription();
		} else if (botMind != null) {
			// Prepare mission phase text area
			if (dead)
				missionPhaseText = deathInfo.getMissionPhase();
			else if (botMind.getMission() != null)
				missionPhaseText = botMind.getMission().getPhaseDescription();
		}

		missionPhaseTextArea = new JTextArea(2, COL_WDITH);
		// if (missionPhase.equals(""))
		missionPhaseTextArea.setText(missionPhaseText);
		missionPhaseTextArea.setLineWrap(true);
		missionPhaseTextArea.setEditable(false);
		missionPhasePanel.add(new JScrollPane(missionPhaseTextArea), BorderLayout.CENTER);

		// Prepare mission button panel.
		JPanel missionButtonPanel = new JPanel(new GridLayout(1, 6, 10, 10));
		missionButtonPanel.setSize(480, 20);
		missionCenterPanel.add(missionButtonPanel, BorderLayout.NORTH);

		// Prepare mission tool button.
		missionButton = new JButton(ImageLoader.getIconByName(MissionWindow.ICON)); //$NON-NLS-1$
		missionButton.setSize(20, 20);
		missionButton.setMargin(new Insets(1, 1, 1, 1));
		missionButton.setToolTipText(Msg.getString("TabPanelActivity.tooltip.mission"));
		// missionButton.setToolTipText(Msg.getString("TabPanelActivity.tooltip.mission"));
		// //$NON-NLS-1$
		// Msg.getString("TabPanelActivity.tooltip.mission")); //$NON-NLS-1$
		missionButton.addActionListener(this);

		if (mind != null) {
			missionButton.setEnabled(mind.getMission() != null);
		} else if (botMind != null) {
			missionButton.setEnabled(botMind.getMission() != null);
		}

		missionButtonPanel.add(new JPanel(new FlowLayout(5, 5, FlowLayout.CENTER)));
		missionButtonPanel.add(new JPanel(new FlowLayout(5, 5, FlowLayout.CENTER)));
		missionButtonPanel.add(missionButton);

		// Prepare mission monitor button
		monitorButton = new JButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setSize(20, 20);
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.setToolTipText(Msg.getString("TabPanelActivity.tooltip.monitor"));
		monitorButton.addActionListener(this);

		if (mind != null) {
			monitorButton.setEnabled(mind.getMission() != null);
		} else if (botMind != null) {
			monitorButton.setEnabled(botMind.getMission() != null);
		}

		missionButtonPanel.add(monitorButton);
		missionButtonPanel.add(new JPanel(new FlowLayout(5, 5, FlowLayout.CENTER)));
		missionButtonPanel.add(new JPanel(new FlowLayout(5, 5, FlowLayout.CENTER)));

	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		Person person = null;
		Robot robot = null;

		boolean dead = false;
		Unit unit = getUnit();
		
		if (unit.getUnitType() == UnitType.PERSON) {
			person = (Person) unit;
			dead = person.getPhysicalCondition().isDead();
		} else {
			robot = (Robot) unit;
			dead = robot.getSystemCondition().isInoperable();
		}

		if (dead) {
			updateDead();
		}
		else {
			updateAlive();
		}
		
	}
	
	private void updateDead() {
		Person person = null;
		Robot robot = null;
		Mind mind = null;
		BotMind botMind = null;
		TaskManager taskManager = null;
		boolean dead = false;
		DeathInfo deathInfo = null;
		Unit unit = getUnit();
		
		if (unit.getUnitType() == UnitType.PERSON) {
			person = (Person) unit;
			mind = person.getMind();
			dead = person.getPhysicalCondition().isDead();// .isDeclaredDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();
		} else {
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

		if (!missionTextCache.equals(newMissionText)) {
			missionTextCache = newMissionText;
			missionTextArea.setText(newMissionText);
		}

		if (!missionPhaseCache.equals(newMissionPhase)) {
			missionPhaseCache = newMissionPhase;
			missionPhaseTextArea.setText(newMissionPhase);
		}
		
		// Update mission and monitor buttons.
		missionButton.setEnabled(mission != null);
		monitorButton.setEnabled(mission != null);		
	}
	
	private void updateAlive() {
		Person person = null;
		Robot robot = null;
		Mind mind = null;
		BotMind botMind = null;
		TaskManager taskManager = null;
		boolean dead = false;
		DeathInfo deathInfo = null;
		Unit unit = getUnit();
		
		if (unit.getUnitType() == UnitType.PERSON) {
			person = (Person) unit;
			mind = person.getMind();
			dead = person.getPhysicalCondition().isDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();
		} else {
			robot = (Robot) unit;
			botMind = robot.getBotMind();
			dead = robot.getSystemCondition().isInoperable();
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
		if (mission != null)
			newMissionText = mission.getName();
		else
			newMissionText = "";

		if (mission != null)
			newMissionPhase = mission.getPhaseDescription();
		else
			newMissionPhase = "";

		if (!missionTextCache.equals(newMissionText)) {
			missionTextCache = newMissionText;
			missionTextArea.setText(newMissionText);
		}

		if (!missionPhaseCache.equals(newMissionPhase)) {
			missionPhaseCache = newMissionPhase;
			missionPhaseTextArea.setText(newMissionPhase);
		}
		
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
			if (unit.getUnitType() == UnitType.PERSON) {
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
			} else {
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
