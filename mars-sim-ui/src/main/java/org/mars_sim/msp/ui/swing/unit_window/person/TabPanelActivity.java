/**
 * Mars Simulation Project
 * TabPanelActivity.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
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
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.TaskManager;
import org.mars_sim.msp.core.person.ai.task.TaskPhase;
import org.mars_sim.msp.core.person.medical.DeathInfo;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.robot.ai.task.BotTaskManager;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.tool.monitor.RobotTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.managers.language.data.TooltipWay;
//import com.alee.managers.tooltip.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;


/**
 * The TabPanelActivity is a tab panel for a person's current tasks and activities
 */
public class TabPanelActivity
extends TabPanel
implements ActionListener {

	private static final int COL_WDITH = 16;

    private static final String DEAD_PHRASE = " " + Msg.getString("TabPanelActivity.dead.phrase"); //" (at the Moment of Death)" ;
    private static final String NONE = "None ";

	/** data cache */
	private String taskTextCache = ""; //$NON-NLS-1$
	/** data cache */
	private String taskPhaseCache = ""; //$NON-NLS-1$
	/** data cache */
	private String missionTextCache = ""; //$NON-NLS-1$
	/** data cache */
	private String missionPhaseCache = ""; //$NON-NLS-1$

	private JTextArea taskTextArea;
	private JTextArea taskPhaseArea;
	private JTextArea missionTextArea;
	private JTextArea missionPhaseTextArea;
	private JButton monitorButton;
	private JButton missionButton;


	/**
	 * Constructor.
	 * @param unit {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelActivity(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelActivity.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelActivity.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

	    Person person = null;
	    Robot robot = null;
		Mind mind = null;
		BotMind botMind = null;
		TaskManager taskManager = null;
		BotTaskManager botTaskManager = null;
		boolean dead = false;
		DeathInfo deathInfo = null;

	    if (unit instanceof Person) {
	    	person = (Person) unit;
			mind = person.getMind();
			taskManager = mind.getTaskManager();
			dead = person.getPhysicalCondition().isDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();
		}
		else if (unit instanceof Robot) {
	        robot = (Robot) unit;
			botMind = robot.getBotMind();
			botTaskManager = botMind.getBotTaskManager();
			dead = robot.getSystemCondition().isInoperable();
			//deathInfo = robot.getSystemCondition().getDeathDetails();
		}

		// Prepare activity label panel
		JPanel activityLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(activityLabelPanel);

		// Prepare activity label
		JLabel titleLabel = new JLabel(Msg.getString("TabPanelActivity.label"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		activityLabelPanel.add(titleLabel);

		// Prepare activity panel
		JPanel activityPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		centerContentPanel.add(activityPanel);

		// Prepare task top panel
		JPanel taskTopPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		taskTopPanel.setBorder(new MarsPanelBorder());
		activityPanel.add(taskTopPanel);

		// Prepare task panel
		JPanel taskPanel = new JPanel(new BorderLayout(0, 0));
		taskTopPanel.add(taskPanel);

		// Prepare task label
		JLabel taskLabel = new JLabel(Msg.getString("TabPanelActivity.task"), JLabel.CENTER); //$NON-NLS-1$
		taskPanel.add(taskLabel, BorderLayout.NORTH);

		// Prepare task text area
		if (dead)
			taskTextCache = deathInfo.getTask();
		else {
			if (person != null) {
				String t = taskManager.getTaskDescription(false);
				if (t != null && !t.toLowerCase().contains("walk"))
					taskTextCache = t;
			}

			else if (robot != null) {
				String t = botTaskManager.getTaskDescription(false);
				if (t != null && !t.toLowerCase().contains("walk"))
					taskTextCache = t;
			}
		}

		taskTextArea = new JTextArea(2, COL_WDITH);
		//if (taskText != null)
		taskTextArea.setText(taskTextCache);
		taskTextArea.setLineWrap(true);
		taskTextArea.setEditable(false);
		taskPanel.add(new JScrollPane(taskTextArea), BorderLayout.CENTER);

		// Prepare task phase panel
		JPanel taskPhasePanel = new JPanel(new BorderLayout(0, 0));
		taskTopPanel.add(taskPhasePanel);

		// Prepare task phase label
		JLabel taskPhaseLabel = new JLabel(Msg.getString("TabPanelActivity.taskPhase"), JLabel.CENTER); //$NON-NLS-1$
		taskPhasePanel.add(taskPhaseLabel, BorderLayout.NORTH);

		// Prepare task phase text area
		if (dead) {
			taskPhaseCache = deathInfo.getTaskPhase();
		}
		else {

			TaskPhase phase = null;

			if (person != null)
				phase = taskManager.getPhase();
			else if (robot != null)
				phase = botTaskManager.getPhase();


		    if (phase != null) {
		    	taskPhaseCache = phase.getName();
		    }
		    else {
		    	taskPhaseCache = "";
		    }
		}

		taskPhaseArea = new JTextArea(2, COL_WDITH);
		//if (taskPhaseText != null)
		taskPhaseArea.setText(taskPhaseCache);
		taskPhaseArea.setLineWrap(true);
		taskPhaseArea.setEditable(false);
		taskPhasePanel.add(new JScrollPane(taskPhaseArea), BorderLayout.CENTER);

		// Prepare mission top panel
		JPanel missionTopPanel = new JPanel(new BorderLayout(0, 0));//new FlowLayout(FlowLayout.CENTER));
		//missionTopPanel.setBorder(new MarsPanelBorder());
		activityPanel.add(missionTopPanel);

		// Prepare mission center panel
		JPanel missionCenterPanel = new JPanel(new BorderLayout(0, 0));//new FlowLayout(FlowLayout.CENTER));
		//missionCenterPanel.setBorder(new MarsPanelBorder());

		missionTopPanel.add(new JPanel(), BorderLayout.NORTH);
		missionTopPanel.add(missionCenterPanel, BorderLayout.CENTER);
		missionTopPanel.add(new JPanel(), BorderLayout.SOUTH);
		missionTopPanel.add(new JPanel(), BorderLayout.EAST);
		missionTopPanel.add(new JPanel(), BorderLayout.WEST);

		// Prepare mission panel
		JPanel missionTextPanel = new JPanel(new BorderLayout(0, 0));
		//missionLeftPanel.add(missionTextPanel);
		missionCenterPanel.add(missionTextPanel, BorderLayout.CENTER);

		// Prepare mission label
		JLabel missionLabel = new JLabel(Msg.getString("TabPanelActivity.mission"), JLabel.CENTER); //$NON-NLS-1$
		missionTextPanel.add(missionLabel, BorderLayout.NORTH);

		// Prepare mission text area

		String missionText = "";

		if (person != null) {

			if (dead)
				missionText = deathInfo.getMission();

			else if (mind.getMission() != null)  {
				missionText = mind.getMission().getDescription();
				//if (missionText == null)
				//	missionText = "";
			}
		}
		else if (robot != null) {

			if (dead)
				missionText = deathInfo.getMission();

			else if (botMind.getMission() != null)  {
				missionText = botMind.getMission().getDescription();
				//if (missionText == null)
				//	missionText = "";
			}
		}

		missionTextArea = new JTextArea(2, COL_WDITH);
		//if (missionText != null)
		missionTextArea.setText(missionText);
		missionTextArea.setLineWrap(true);
		missionTextArea.setEditable(false);
		missionTextPanel.add(new JScrollPane(missionTextArea), BorderLayout.CENTER);

		// Prepare mission phase panel
		JPanel missionPhasePanel = new JPanel(new BorderLayout(0, 0));
		//missionLeftPanel.add(missionPhasePanel);
		missionCenterPanel.add(missionPhasePanel, BorderLayout.SOUTH);

		// Prepare mission phase label
		JLabel missionPhaseLabel = new JLabel(Msg.getString("TabPanelActivity.missionPhase"), JLabel.CENTER); //$NON-NLS-1$
		missionPhasePanel.add(missionPhaseLabel, BorderLayout.NORTH);

		String missionPhaseText = "";
		if (person != null) {
			// Prepare mission phase text area
			if (dead)
				missionPhaseText = deathInfo.getMissionPhase();
			else if (mind.getMission() != null)
				missionPhaseText = mind.getMission().getPhaseDescription();
		}
		else if (robot != null) {
			// Prepare mission phase text area
			if (dead)
				missionPhaseText = deathInfo.getMissionPhase();
			else if (botMind.getMission() != null)
				missionPhaseText = botMind.getMission().getPhaseDescription();
		}


		missionPhaseTextArea = new JTextArea(2, COL_WDITH);
		//if (missionPhase.equals(""))
		missionPhaseTextArea.setText(missionPhaseText);
		missionPhaseTextArea.setLineWrap(true);
		missionPhaseTextArea.setEditable(false);
		missionPhasePanel.add(new JScrollPane(missionPhaseTextArea), BorderLayout.CENTER);


		// Prepare mission button panel.
		JPanel missionButtonPanel = new JPanel(new GridLayout(1, 6, 10, 10));
		missionButtonPanel.setSize(480, 20);
		missionCenterPanel.add(missionButtonPanel, BorderLayout.NORTH);

		// Prepare mission tool button.
		missionButton = new JButton(ImageLoader.getIcon(Msg.getString("img.mission"))); //$NON-NLS-1$
		missionButton.setSize(20, 20);
		missionButton.setMargin(new Insets(1, 1, 1, 1));
		TooltipManager.setTooltip (missionButton, Msg.getString("TabPanelActivity.tooltip.mission"), TooltipWay.down);
		//missionButton.setToolTipText(Msg.getString("TabPanelActivity.tooltip.mission")); //$NON-NLS-1$
		//balloonToolTip.createBalloonTip(missionButton, Msg.getString("TabPanelActivity.tooltip.mission")); //$NON-NLS-1$
		missionButton.addActionListener(this);


		if (person != null) {
			missionButton.setEnabled(mind.getMission() != null);
		}
		else if (robot != null) {
			missionButton.setEnabled(botMind.getMission() != null);
		}

		missionButtonPanel.add(new JPanel(new FlowLayout(5, 5, FlowLayout.CENTER)));
		missionButtonPanel.add(new JPanel(new FlowLayout(5, 5, FlowLayout.CENTER)));
		missionButtonPanel.add(missionButton);

		// Prepare mission monitor button
		monitorButton = new JButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setSize(20, 20);
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		TooltipManager.setTooltip (monitorButton, Msg.getString("TabPanelActivity.tooltip.monitor"), TooltipWay.down);
		//monitorButton.setToolTipText(Msg.getString("TabPanelActivity.tooltip.monitor")); //$NON-NLS-1$
		//balloonToolTip.createBalloonTip(monitorButton, Msg.getString("TabPanelActivity.tooltip.monitor")); //$NON-NLS-1$
		monitorButton.addActionListener(this);

		if (person != null) {
			monitorButton.setEnabled(mind.getMission() != null);
		}
		else if (robot != null) {
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
		Mind mind = null;
		BotMind botMind = null;
		TaskManager taskManager = null;
		BotTaskManager botTaskManager = null;
		boolean dead = false;
		DeathInfo deathInfo = null;

	    if (unit instanceof Person) {
	    	person = (Person) unit;
			mind = person.getMind();
			dead = person.isDeclaredDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();
		}
		else if (unit instanceof Robot) {
	        robot = (Robot) unit;
			botMind = robot.getBotMind();
			dead = robot.getSystemCondition().isInoperable();
			//deathInfo = robot.getSystemCondition().getDeathDetails();
		}


		Mission mission = null;

		String newTaskText = "";
		String newTaskPhase = "";
		String newMissionText = "";
		String newMissionPhase = "";

		// Prepare task text area
		if (dead) {
			String t = deathInfo.getTask();
			String tp = deathInfo.getTaskPhase();

			if (t == null || t.equals(""))
				newTaskText = NONE + DEAD_PHRASE;
			else
				newTaskText = t + DEAD_PHRASE;

			if (tp == null || tp.equals(""))
				newTaskPhase = NONE + DEAD_PHRASE;
			else
				newTaskPhase = tp + DEAD_PHRASE;
		}

		else {

			if (person != null) {
				taskManager = mind.getTaskManager();
				if (mind.hasActiveMission()) mission = mind.getMission();

			}
			else if (robot != null) {
				botTaskManager = botMind.getBotTaskManager();
				if (botMind.hasActiveMission()) mission = botMind.getMission();

			}

			if (person != null) {
				newTaskText = taskManager.getTaskDescription(false);
			}
			else if (robot != null) {
				newTaskText = botTaskManager.getTaskDescription(false);
			}

		    TaskPhase phase = null;

			if (person != null)
			    phase = taskManager.getPhase();
			else
			    phase = botTaskManager.getPhase();


		    if (phase != null) {
		    	newTaskPhase = phase.getName();
		    }
		    else {
		    	newTaskPhase = "";
		    }

		}

		if (!newTaskText.toLowerCase().contains("walk")) {
			if (!newTaskText.equals("") && !taskTextCache.equals(newTaskText)) {
				taskTextCache = newTaskText;
				taskTextArea.setText(newTaskText);
			}

			if (!newTaskPhase.equals("") && !taskPhaseCache.equals(newTaskPhase))  {
				taskPhaseCache = newTaskPhase;
				taskPhaseArea.setText(newTaskPhase);
			}
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


		if (!newMissionText.equals("") && !missionTextCache.equals(newMissionText)) {
			missionTextCache = newMissionText;
			missionTextArea.setText(newMissionText);
		}

		if (!newMissionPhase.equals("") && !missionPhaseCache.equals(newMissionPhase)) {
			missionPhaseCache = newMissionPhase;
			missionPhaseTextArea.setText(newMissionPhase);
		}

/*
		// Update mission phase text area if necessary.
		if (dead) {
			if (deathInfo.getMissionPhase() == null)
				missionPhaseCache = "None " + DEAD_PHRASE;
			else
				missionPhaseCache = deathInfo.getMissionPhase() + DEAD_PHRASE;
		}
		else {
			if (mission != null)
				missionPhaseCache = mission.getPhaseDescription();
			else missionPhaseCache = ""; //$NON-NLS-1$
		}

		if ((missionPhaseCache != null) && !missionPhaseCache.equals(missionPhaseTextArea.getText()))
			missionPhaseTextArea.setText(missionPhaseCache);
*/


		// Update mission and monitor buttons.
		missionButton.setEnabled(mission != null);
		monitorButton.setEnabled(mission != null);
	}

	/**
	 * Action event occurs.
	 * @param event {@link ActionEvent} the action event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if ((source == missionButton) || (source == monitorButton)) {

		    Person person = null;
		    Robot robot = null;
			Mind mind = null;
			BotMind botMind = null;

			boolean dead = false;
			DeathInfo deathInfo = null;

		    if (unit instanceof Person) {
		    	person = (Person) unit;
				mind = person.getMind();
				dead = person.getPhysicalCondition().isDead();
				deathInfo = person.getPhysicalCondition().getDeathDetails();

				if (!person.getPhysicalCondition().isDead()) {
					mind = person.getMind();
					if (mind.hasActiveMission()) {
						if (source == missionButton) {
							((MissionWindow) desktop.getToolWindow(MissionWindow.NAME)).selectMission(mind.getMission());
							getDesktop().openToolWindow(MissionWindow.NAME);
						}
						else if (source == monitorButton)
							desktop.addModel(new PersonTableModel(mind.getMission()));
					}
				}
			}
			else if (unit instanceof Robot) {
		        robot = (Robot) unit;
				botMind = robot.getBotMind();
				dead = robot.getSystemCondition().isInoperable();
				//deathInfo = robot.getSystemCondition().getDeathDetails();

				if (!robot.getSystemCondition().isInoperable()) {
					botMind = robot.getBotMind();
					if (botMind.hasActiveMission()) {
						if (source == missionButton) {
							((MissionWindow) desktop.getToolWindow(MissionWindow.NAME)).selectMission(botMind.getMission());
							getDesktop().openToolWindow(MissionWindow.NAME);
						}
						else if (source == monitorButton) desktop.addModel(new RobotTableModel(botMind.getMission()));
					}
				}
			}
		}
	}
}