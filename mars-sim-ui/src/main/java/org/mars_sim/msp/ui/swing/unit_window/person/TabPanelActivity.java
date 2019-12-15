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

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.robot.ai.task.BotTaskManager;
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
//import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The TabPanelActivity is a tab panel for a person's current tasks and
 * activities
 */
@SuppressWarnings("serial")
public class TabPanelActivity extends TabPanel implements ActionListener {

	private static final int COL_WDITH = 16;

	private static final String DEAD_PHRASE = " " + Msg.getString("TabPanelActivity.dead.phrase"); // " (at the Moment
																									// of Death)" ;
	private static final String NONE = "None ";

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** current task text cache */
	private String taskTextCache = ""; //$NON-NLS-1$
	/** current phase text cache */
	private String taskPhaseCache = ""; //$NON-NLS-1$
	/** sub task text cache */
	private String subTaskTextCache = ""; //$NON-NLS-1$
	/** sub phase text cache */
	private String subTaskPhaseCache = ""; //$NON-NLS-1$
	/** data cache */
	private String missionTextCache = ""; //$NON-NLS-1$
	/** data cache */
	private String missionPhaseCache = ""; //$NON-NLS-1$
	/** The Person instance. */
	private Person person = null;
	/** The Robot instance. */
	private Robot robot = null;
	
	private WebTextArea taskTextArea;
	private WebTextArea taskPhaseArea;
	
	private WebTextArea subTaskTextArea;
	private WebTextArea subTaskPhaseArea;
	
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
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		boolean dead = false;
		
		Mind mind = null;
		BotMind botMind = null;
		
		TaskManager taskManager = null;
		BotTaskManager botTaskManager = null;
		
		DeathInfo deathInfo = null;

		if (unit instanceof Person) {
			person = (Person) unit;
			mind = person.getMind();
			taskManager = mind.getTaskManager();
			dead = person.getPhysicalCondition().isDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			botMind = robot.getBotMind();
			botTaskManager = botMind.getBotTaskManager();
			dead = robot.getSystemCondition().isInoperable();
			// deathInfo = robot.getSystemCondition().getDeathDetails();
		}

		// Prepare activity label panel
		WebPanel activityLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(activityLabelPanel);

		// Prepare activity label
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelActivity.label"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		activityLabelPanel.add(titleLabel);

		// Prepare activity panel
		WebPanel activityPanel = new WebPanel(new GridLayout(2, 1, 0, 0));
		centerContentPanel.add(activityPanel);

		// Prepare task top panel
		WebPanel taskTopPanel = new WebPanel(new GridLayout(4, 1, 0, 0));
//		taskTopPanel.setBorder(new MarsPanelBorder());
		activityPanel.add(taskTopPanel);

		// Prepare task panel
		WebPanel currentTaskPanel = new WebPanel(new BorderLayout(0, 0));
		taskTopPanel.add(currentTaskPanel);

		// Prepare task label
		WebLabel taskLabel = new WebLabel(Msg.getString("TabPanelActivity.task"), WebLabel.CENTER); //$NON-NLS-1$
		currentTaskPanel.add(taskLabel, BorderLayout.NORTH);

		// Prepare task text area
		if (dead)
			taskTextCache = deathInfo.getTask();
		else {
			if (person != null) {
				String t = taskManager.getTaskDescription(false);
				if (t != null) 
//						&& !t.toLowerCase().contains("walk"))
					taskTextCache = t;
			}

			else if (robot != null) {
				String t = botTaskManager.getTaskDescription(false);
				if (t != null)// && !t.toLowerCase().contains("walk"))
					taskTextCache = t;
			}
		}

		taskTextArea = new WebTextArea(1, COL_WDITH);
		// if (taskText != null)
		taskTextArea.setText(taskTextCache);
		taskTextArea.setLineWrap(true);
		taskTextArea.setEditable(false);
		currentTaskPanel.add(new WebScrollPane(taskTextArea), BorderLayout.CENTER);

		// Prepare task phase panel
		WebPanel taskPhasePanel = new WebPanel(new BorderLayout(0, 0));
		taskTopPanel.add(taskPhasePanel);

		// Prepare task phase label
		WebLabel taskPhaseLabel = new WebLabel(Msg.getString("TabPanelActivity.taskPhase"), WebLabel.CENTER); //$NON-NLS-1$
		taskPhasePanel.add(taskPhaseLabel, BorderLayout.NORTH);

		// Prepare task phase text area
		if (dead) {
			taskPhaseCache = deathInfo.getTaskPhase();
		} else {

			TaskPhase phase = null;

			if (person != null)
				phase = taskManager.getPhase();
			else if (robot != null)
				phase = botTaskManager.getPhase();

			if (phase != null) {
				taskPhaseCache = phase.getName();
			} else {
				taskPhaseCache = "";
			}
		}

		taskPhaseArea = new WebTextArea(1, COL_WDITH);
		// if (taskPhaseText != null)
		taskPhaseArea.setText(taskPhaseCache);
		taskPhaseArea.setLineWrap(true);
		taskPhaseArea.setEditable(false);
		taskPhasePanel.add(new WebScrollPane(taskPhaseArea), BorderLayout.CENTER);

		// Prepare sub task panel
		WebPanel subTaskPanel = new WebPanel(new BorderLayout(0, 0));
		taskTopPanel.add(subTaskPanel);
		
		// Prepare Sub-task label
		WebLabel subtaskLabel = new WebLabel(Msg.getString("TabPanelActivity.subTask"), WebLabel.CENTER); //$NON-NLS-1$
		subTaskPanel.add(subtaskLabel, BorderLayout.NORTH);

		// Prepare task text area
		if (dead)
			subTaskTextCache = deathInfo.getSubTask();
		else {
			if (person != null) {
				String t = taskManager.getSubTaskDescription();
				if (t != null) 
//						&& !t.toLowerCase().contains("walk"))
					subTaskTextCache = t;
				else
					subTaskTextCache = "";
			}

			else if (robot != null) {
				String t = botTaskManager.getSubTaskDescription();
				if (t != null)// && !t.toLowerCase().contains("walk"))
					subTaskTextCache = t;
				else
					subTaskTextCache = "";
			}
		}

		subTaskTextArea = new WebTextArea(1, COL_WDITH);
		// if (taskText != null)
		subTaskTextArea.setText(subTaskTextCache);
		subTaskTextArea.setLineWrap(true);
		subTaskTextArea.setEditable(false);
		subTaskPanel.add(new WebScrollPane(subTaskTextArea), BorderLayout.CENTER);

		// Prepare task phase panel
		WebPanel subTaskPhasePanel = new WebPanel(new BorderLayout(0, 0));
		taskTopPanel.add(subTaskPhasePanel);

		// Prepare task phase label
		WebLabel subTaskPhaseLabel = new WebLabel(Msg.getString("TabPanelActivity.subTaskPhase"), WebLabel.CENTER); //$NON-NLS-1$
		subTaskPhasePanel.add(subTaskPhaseLabel, BorderLayout.NORTH);

		// Prepare sub task phase text area
		if (dead) {
			subTaskPhaseCache = deathInfo.getSubTaskPhase();
		} else if (subTaskTextCache.equals("")) {
			subTaskPhaseCache = "";
		} else {

			TaskPhase phase = null;

			if (person != null)
				phase = taskManager.getSubTaskPhase();
			else if (robot != null)
				phase = botTaskManager.getSubTaskPhase();

			if (phase != null) {
				subTaskPhaseCache = phase.getName();
			} else {
				subTaskPhaseCache = "";
			}
		}

		subTaskPhaseArea = new WebTextArea(1, COL_WDITH);
		// if (taskPhaseText != null)
		subTaskPhaseArea.setText(subTaskPhaseCache);
		subTaskPhaseArea.setLineWrap(true);
		subTaskPhaseArea.setEditable(false);
		subTaskPhasePanel.add(new WebScrollPane(subTaskPhaseArea), BorderLayout.CENTER);

		
		// Prepare mission top panel
		WebPanel missionTopPanel = new WebPanel(new BorderLayout(0, 0));// new FlowLayout(FlowLayout.CENTER));
		// missionTopPanel.setBorder(new MarsPanelBorder());
		activityPanel.add(missionTopPanel);

		// Prepare mission center panel
		WebPanel missionCenterPanel = new WebPanel(new BorderLayout(0, 0));// new FlowLayout(FlowLayout.CENTER));
		// missionCenterPanel.setBorder(new MarsPanelBorder());

		missionTopPanel.add(new WebPanel(), BorderLayout.NORTH);
		missionTopPanel.add(missionCenterPanel, BorderLayout.CENTER);
		missionTopPanel.add(new WebPanel(), BorderLayout.SOUTH);
		missionTopPanel.add(new WebPanel(), BorderLayout.EAST);
		missionTopPanel.add(new WebPanel(), BorderLayout.WEST);

		// Prepare mission panel
		WebPanel missionTextPanel = new WebPanel(new BorderLayout(0, 0));
		// missionLeftPanel.add(missionTextPanel);
		missionCenterPanel.add(missionTextPanel, BorderLayout.CENTER);

		// Prepare mission label
		WebLabel missionLabel = new WebLabel(Msg.getString("TabPanelActivity.missionDesc"), WebLabel.CENTER); //$NON-NLS-1$
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

		missionTextArea = new WebTextArea(4, COL_WDITH);
		// if (missionText != null)
		missionTextArea.setText(missionText);
		missionTextArea.setLineWrap(true);
		missionTextArea.setEditable(false);
		missionTextPanel.add(new WebScrollPane(missionTextArea), BorderLayout.CENTER);

		// Prepare mission phase panel
		WebPanel missionPhasePanel = new WebPanel(new BorderLayout(0, 0));
		// missionLeftPanel.add(missionPhasePanel);
		missionCenterPanel.add(missionPhasePanel, BorderLayout.SOUTH);

		// Prepare mission phase label
		WebLabel missionPhaseLabel = new WebLabel(Msg.getString("TabPanelActivity.missionPhase"), WebLabel.CENTER); //$NON-NLS-1$
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

		missionPhaseTextArea = new WebTextArea(4, COL_WDITH);
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
		if (!uiDone)
			initializeUI();
		
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
		
		String newMissionText = "";
		String newMissionPhase = "";

		// Prepare task text area
		if (dead) {
			String t = deathInfo.getTask();
			String tp = deathInfo.getTaskPhase();
			String st = deathInfo.getSubTask();
			String stp = deathInfo.getSubTaskPhase();
			
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
		}

		else {

			if (person != null) {
				taskManager = mind.getTaskManager();
				if (mind.hasActiveMission())
					mission = mind.getMission();

			} else if (robot != null) {
				botTaskManager = botMind.getBotTaskManager();
//				if (botMind.hasActiveMission())
//					mission = botMind.getMission();

			}

			if (person != null) {
				newTaskText = taskManager.getTaskDescription(false);
				newSubTaskText = taskManager.getSubTaskDescription();
			} else if (robot != null) {
				newTaskText = botTaskManager.getTaskDescription(false);
				newSubTaskText = botTaskManager.getSubTaskDescription();
			}

			TaskPhase taskPhase = null;
			TaskPhase subTaskPhase = null;
			
			if (person != null) {
				taskPhase = taskManager.getPhase();
				subTaskPhase = taskManager.getSubTaskPhase();
			}
			else {
				taskPhase = botTaskManager.getPhase();
				subTaskPhase = botTaskManager.getSubTaskPhase();
			}

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
							((MissionWindow) desktop.getToolWindow(MissionWindow.NAME))
									.selectMission(mind.getMission());
							getDesktop().openToolWindow(MissionWindow.NAME);
						} else if (source == monitorButton)
							desktop.addModel(new PersonTableModel(mind.getMission()));
					}
				}
			} else if (unit instanceof Robot) {
				robot = (Robot) unit;
				botMind = robot.getBotMind();
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