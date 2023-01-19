/*
 * Mars Simulation Project
 * CommanderWindow.java
 * @date 2022-07-28
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.commander;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.task.util.BasicTaskJob;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SmartScroller;
import org.mars_sim.msp.ui.swing.tool.VerticalLabelUI;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;


/**
 * Window for the Commanders Dashboard.
 */
@SuppressWarnings("serial")
public class CommanderWindow extends ToolWindow {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CommanderWindow.class.getName());

	/** Tool name. */
	private static final int LIST_WIDTH = 300;

	public static final String NAME = "Commander Dashboard";

	private static final String LEADERSHIP_TAB = "Leadership";

	private static final String AGRICULTURE_TAB = "Agriculture";
	private static final String COMPUTING_TAB = "Computing";
	private static final String ENGINEERING_TAB = "Engineering";
	private static final String LOGISTIC_TAB = "Logistic";
	private static final String MISSION_TAB = " Mission";
	private static final String RESOURCE_TAB = "Resource";
	private static final String SAFETY_TAB = "Safety";
	private static final String SCIENCE_TAB = "Science";

	private static final String CAN_INITIATE = "Can initiate Trading Mission";
	private static final String CANNOT_INITIATE = "Cannot initiate Trading Mission";
	private static final String ACCEPT = "Accept Trading initiated by other settlements";
	private static final String ACCEPT_NO = "Accept NO Trading initiated by other settlements";
	private static final String SEE_RIGHT = ".    -->";

	private final Font SERIF = new Font("Serif", Font.PLAIN, 10);
	private final Font DIALOG = new Font( "Dialog", Font.PLAIN, 14);

	private JTabbedPane tabPane;
	
	private JComboBoxMW<Person> personComboBox;
	/** Settlement Combo box */
	private JComboBox<Settlement> settlementListBox;
	
	private ListModel listModel;
	private JList<TaskJob> list;
	private JTextArea logBookTA;

	private JPanel policyMainPanel;

	private JScrollPane listScrollPanel;

	private JRadioButton r0;
	private JRadioButton r1;
	private JRadioButton r2;
	private JRadioButton r3;
	private JRadioButton r4;
	
	private Person cc;

	private Settlement settlement;

	//private List<String> taskCache;

	/** The MarsClock instance. */
	private MarsClock marsClock;
	private MasterClock masterClock;
	private UnitManager unitManager;

	private JPanel tradingPartnersPanel;
	private Map<String,Settlement> tradingPartners;

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public CommanderWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);

		this.masterClock = desktop.getSimulation().getMasterClock();
		this.marsClock = masterClock.getMarsClock();
		unitManager = desktop.getSimulation().getUnitManager();

		List<Settlement> settlementList = new ArrayList<>(unitManager.getSettlements());
		Collections.sort(settlementList);
		settlement = settlementList.get(0);
		cc = settlement.getCommander();
		
		// Create content panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		JPanel topPane = new JPanel(new FlowLayout());
		mainPane.add(topPane, BorderLayout.NORTH);
		
		buildSettlementComboBox();
		topPane.add(settlementListBox);


		JPanel bottomPane = new JPanel(new GridLayout(1, 4));
		bottomPane.setPreferredSize(new Dimension(-1, 50));
		mainPane.add(bottomPane, BorderLayout.SOUTH);

		// Create the info tab panel.
		tabPane = new JTabbedPane();
		mainPane.add(tabPane, BorderLayout.CENTER);
		
		createAgriculturePanel();
		createComputingPanel();
		createEngineeringPanel();
		createLeadershipPanel();
		createLogisticPanel();
		createMissionPanel();
		createResourcePanel();
		createSafetyPanel();
		createSciencePanel();

		setSize(new Dimension(640, 640));
		setMaximizable(true);
		setResizable(false);

		setVisible(true);
	
		Dimension desktopSize = desktop.getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);

	}

	/**
     * Builds the settlement name combo box.
     */
	private void buildSettlementComboBox() {

		SettlementComboBoxModel settlementCBModel = new SettlementComboBoxModel();

		settlementListBox = new JComboBox<>(settlementCBModel);
		settlementListBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		settlementListBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		DefaultListCellRenderer listRenderer = new DefaultListCellRenderer();
		listRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER); // center-aligned items
		settlementListBox.setRenderer(listRenderer);
		
		settlementListBox.addItemListener(event -> {
				Settlement s = (Settlement) event.getItem();
				if (s != null) {
					// Update the selected settlement instance
					changeSettlement(s);
				}
		});
		
		settlementListBox.setSelectedIndex(0);
	}
	
	/**
	 * Sets up the person combo box.
	 * 
	 * @param s
	 */
	private void setUpPersonComboBox(Settlement s) {
		List<Person> people = new ArrayList<>(s.getAllAssociatedPeople());
		Collections.sort(people);
			
		DefaultComboBoxModel<Person> comboBoxModel = new DefaultComboBoxModel<>();
		
		if (personComboBox == null) {
			personComboBox = new JComboBoxMW<>(comboBoxModel);
		}
		else {
			personComboBox.removeAll();
			personComboBox.replaceModel(comboBoxModel);
		}
		
		Iterator<Person> i = people.iterator();
		while (i.hasNext()) {
			Person n = i.next();
	    	comboBoxModel.addElement(n);
		}
		
		personComboBox.setMaximumRowCount(8);
		personComboBox.setSelectedItem(cc);
	}
	
	/**
	 * Changes the map display to the selected settlement.
	 *
	 * @param s
	 */
	private void changeSettlement(Settlement s) {
		// Change the person list in person combobox
		if (settlement != s) {
			setUpPersonComboBox(s);
											
			// Set the selected settlement
			settlement = s;
			// Set the box opaque
			settlementListBox.setOpaque(false);

			setupTradingSettlements();
		}
	}

	private void createLeadershipPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(LEADERSHIP_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}

	private void createAgriculturePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(AGRICULTURE_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}

	private void createComputingPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(COMPUTING_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}
	
	private void createEngineeringPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(ENGINEERING_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}

	/**
	 * Creates the person combo box.
	 *
	 * @param panel
	 */
	private void createPersonCombobox(JPanel panel) {
      	// Set up combo box model.
		setUpPersonComboBox(settlement);

		JPanel comboBoxPanel = new JPanel(new BorderLayout());
		comboBoxPanel.add(personComboBox);

		JPanel crewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		crewPanel.add(comboBoxPanel);

		crewPanel.setBorder(BorderFactory.createTitledBorder(" Crew Member "));
		crewPanel.setToolTipText("Choose the crew member to give a task order");
		personComboBox.setToolTipText("Choose the crew member to give a task order");

	    panel.add(crewPanel, BorderLayout.NORTH);
	}

	/**
	 * Creates the task combo box.
	 *
	 * @param panel
	 */
	private void createTaskCombobox(JPanel panel) {
		DefaultComboBoxModel<FactoryMetaTask> taskComboBoxModel = new DefaultComboBoxModel<>();
      	// Set up combo box model.
		for(FactoryMetaTask n : MetaTaskUtil.getPersonMetaTasks()) {
	    	taskComboBoxModel.addElement(n);
		}

		// Create comboBox.
		JComboBoxMW<FactoryMetaTask> taskComboBox = new JComboBoxMW<>(taskComboBoxModel);
		taskComboBox.setMaximumRowCount(10);

		JPanel comboBoxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		comboBoxPanel.add(taskComboBox);

		JPanel taskPanel = new JPanel(new BorderLayout());
		taskPanel.add(comboBoxPanel, BorderLayout.CENTER);

		taskPanel.setBorder(BorderFactory.createTitledBorder(" Task Order "));
		taskPanel.setToolTipText("Choose a task order to give");
		taskComboBox.setToolTipText("Choose a task order to give");

		// Create a button panel
	    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	    taskPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Create the add button
	    JButton addButton = new JButton(Msg.getString("BuildingPanelFarming.addButton")); //$NON-NLS-1$
		addButton.setPreferredSize(new Dimension(60, 25));
		addButton.setFont(SERIF);
		addButton.addActionListener(e -> {
				Person selected = (Person) personComboBox.getSelectedItem();
				FactoryMetaTask task = (FactoryMetaTask) taskComboBox.getSelectedItem();
				selected.getMind().getTaskManager().addPendingTask(new BasicTaskJob(task, 1D), true);

				logBookTA.append(marsClock.getTrucatedDateTimeStamp()
						+ " - Assigning '" + task.getName() + "' to " + selected + "\n");
		        listUpdate();
				repaint();
		});
		buttonPanel.add(addButton);
		
		// Create the delete button
		JButton delButton = new JButton(Msg.getString("BuildingPanelFarming.delButton")); //$NON-NLS-1$
		delButton.setPreferredSize(new Dimension(60, 25));
		delButton.setFont(SERIF);
		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!list.isSelectionEmpty() && (list.getSelectedValue() != null)) {
					deleteATask();
					listUpdate();
	            	repaint();
				}
			}
		});
		buttonPanel.add(delButton);

	    panel.add(taskPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates the task queue list.
	 *
	 * @param panel
	 */
	private void createTaskQueueList(JPanel panel) {

	    JLabel label = new JLabel("  Task Queue  ");
		label.setUI(new VerticalLabelUI(false));
	    label.setFont(DIALOG);
		label.setBorder(new MarsPanelBorder());

	    JPanel taskQueuePanel = new JPanel(new BorderLayout());
	    taskQueuePanel.add(label, BorderLayout.NORTH);

	    JPanel queueListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		queueListPanel.add(taskQueuePanel);

		panel.add(queueListPanel, BorderLayout.CENTER); // 2nd add

		// Create scroll panel for population list.
		listScrollPanel = new JScrollPane();
		listScrollPanel.setPreferredSize(new Dimension(LIST_WIDTH, 120));
		listScrollPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		// Create list model
		listModel = new ListModel();

		// Create list
		list = new JList<>(listModel);
		listScrollPanel.setViewportView(list);
		list.addListSelectionListener(event -> {
		        if (!event.getValueIsAdjusting() && event != null){
					deleteATask();
		        }
		});

		queueListPanel.add(listScrollPanel);
	}


	/**
	 * Creates the log book panel for recording task orders.
	 *
	 * @param panel
	 */
	private void createLogBookPanel(JPanel panel) {

		JLabel logLabel = new JLabel("          Log Book          ");
		logLabel.setUI(new VerticalLabelUI(false));
		logLabel.setFont(DIALOG);
		logLabel.setBorder(new MarsPanelBorder());

	    JPanel logPanel = new JPanel(new BorderLayout());
	    logPanel.add(logLabel, BorderLayout.NORTH);

		// Create an text area
		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	    textPanel.add(logPanel);

	    panel.add(textPanel, BorderLayout.CENTER);

		logBookTA = new JTextArea(14, 35);
		logBookTA.setEditable(false);
		JScrollPane scrollTextArea = new JScrollPane (logBookTA,
				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				   JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// Monitor the vertical scroll of jta
		new SmartScroller(scrollTextArea, SmartScroller.VERTICAL, SmartScroller.END);

		textPanel.add(scrollTextArea);
	}


	/**
	 * Creates the logistic panel for operation of tasks.
	 */
	private void createLogisticPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		tabPane.add(LOGISTIC_TAB, mainPanel);
		tabPane.setSelectedComponent(mainPanel);

	    JPanel centerPanel = new JPanel(new BorderLayout());
	    mainPanel.add(centerPanel, BorderLayout.CENTER);
	    mainPanel.add(new JLabel("  "), BorderLayout.WEST);
	    mainPanel.add(new JLabel("  "), BorderLayout.EAST);

		JPanel topPanel = new JPanel(new BorderLayout());
		centerPanel.add(topPanel, BorderLayout.NORTH);

		JPanel topBorderPanel = new JPanel(new BorderLayout());
		topPanel.add(topBorderPanel, BorderLayout.NORTH);

		JPanel midPanel = new JPanel(new BorderLayout());
		centerPanel.add(midPanel, BorderLayout.CENTER);

		JPanel southPanel = new JPanel(new BorderLayout());
		centerPanel.add(southPanel, BorderLayout.SOUTH);

		// Create the person combo box
		createPersonCombobox(topBorderPanel);

		// Create the task combo box
		createTaskCombobox(topBorderPanel);

		// Create the task queue list
		createTaskQueueList(midPanel);

		// Create the log book panel
		createLogBookPanel(southPanel);
	}

	/**
	 * Creates the mission tab panel.
	 */
	private void createMissionPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(MISSION_TAB, panel);

		policyMainPanel = new JPanel(new BorderLayout());
		panel.add(policyMainPanel, BorderLayout.NORTH);
		policyMainPanel.setPreferredSize(new Dimension(200, 125));
		policyMainPanel.setMaximumSize(new Dimension(200, 125));

		// Create a button panel
		JPanel buttonPanel = new JPanel(new GridLayout(4,1));
		policyMainPanel.add(buttonPanel, BorderLayout.CENTER);

		buttonPanel.setBorder(BorderFactory.createTitledBorder("Trading policy "));
		buttonPanel.setToolTipText("Select your trading policy with other settlements");

		ButtonGroup group0 = new ButtonGroup();
		ButtonGroup group1 = new ButtonGroup();

		r0 = new JRadioButton(CAN_INITIATE, true);
		r1 = new JRadioButton(CANNOT_INITIATE);

		// Set up initial conditions
		if (settlement.isMissionEnable(MissionType.TRADE)) {
			r0.setSelected(true);
			r1.setSelected(false);
		}
		else {
			r0.setSelected(false);
			r1.setSelected(true);
		}

		// Set up initial conditions
		boolean noTrading = false;

		r2 = new JRadioButton(ACCEPT_NO, noTrading);
		r3 = new JRadioButton(ACCEPT, !noTrading);

		JLabel selectLabel = new JLabel(" Choose :");
		selectLabel.setMinimumSize(new Dimension(150, 25));
		selectLabel.setPreferredSize(new Dimension(150, 25));

		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.add(selectLabel, BorderLayout.NORTH);

		// Set settlement check boxes
		tradingPartnersPanel = new JPanel();
		tradingPartnersPanel.setLayout(new BoxLayout(tradingPartnersPanel, BoxLayout.Y_AXIS));
		setupTradingSettlements();

		//settlementMissionList.setVisibleRowCount(3);
		innerPanel.add(tradingPartnersPanel, BorderLayout.CENTER);

		JScrollPane innerScroll = new JScrollPane(innerPanel);
		//ScrollPane.setMaximumWidth(250);

		r2.setSelected(false);
		r3.setSelected(true);
		r3.setText(ACCEPT + SEE_RIGHT);
		policyMainPanel.add(innerScroll, BorderLayout.EAST);

		group0.add(r0);
		group0.add(r1);

		group1.add(r2);
		group1.add(r3);

		buttonPanel.add(r0);
		buttonPanel.add(r1);
		buttonPanel.add(r2);
		buttonPanel.add(r3);

		PolicyRadioActionListener actionListener = new PolicyRadioActionListener();
		r0.addActionListener(actionListener);
		r1.addActionListener(actionListener);
		r2.addActionListener(actionListener);
		r3.addActionListener(actionListener);

	}

	private void setupTradingSettlements() {
		tradingPartnersPanel.removeAll();

		tradingPartners = new HashMap<>();
		for(Settlement s : getOtherSettlements()) {
			JCheckBox cb = new JCheckBox(s.getName(), settlement.isAllowedTradeMission(s));
			cb.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean selected = e.getStateChange() == ItemEvent.SELECTED;
					Settlement s = tradingPartners.get(((JCheckBox) e.getSource()).getText());
					settlement.setAllowTradeMissionFromASettlement(s, selected);
				}
			});


			tradingPartnersPanel.add(cb);
			tradingPartners.put(s.getName(), s);
		}
	}

	class PolicyRadioActionListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent event) {
	        JRadioButton button = (JRadioButton) event.getSource();

	        if (button == r0) {
				logger.config("r0 selected");
	        	settlement.setMissionDisable(MissionType.TRADE, false);
	        } else if (button == r1) {
	        	logger.config("r1 selected");
	        	settlement.setMissionDisable(MissionType.TRADE, true);
	        } else if (button == r2) {
	        	logger.config("r2 selected");
		        disableAllCheckedSettlement();
				r3.setText(ACCEPT);
				policyMainPanel.setEnabled(false);
	        } else if (button == r3) {
	        	logger.config("r3 selected");
				r3.setText(ACCEPT + SEE_RIGHT);
				policyMainPanel.setEnabled(true);
	        }
	    }
	}

	private void createResourcePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(RESOURCE_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);

		// Create a button panel
		JPanel buttonPanel = new JPanel(new GridLayout(5,1));
		topPanel.add(buttonPanel);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(" Pausing Interval"));
		buttonPanel.setToolTipText("Select the time interval for automatic simulation pausing");

		ButtonGroup group = new ButtonGroup();

		r0 = new JRadioButton("None", true);
		r1 = new JRadioButton("250 millisols");
		r2 = new JRadioButton("333 millisols");
		r3 = new JRadioButton("500 millisols");
		r4 = new JRadioButton("1 sol");

		group.add(r0);
		group.add(r1);
		group.add(r2);
		group.add(r3);
		group.add(r4);

		buttonPanel.add(r0);
		buttonPanel.add(r1);
		buttonPanel.add(r2);
		buttonPanel.add(r3);
		buttonPanel.add(r4);

		RadioButtonActionListener actionListener = new RadioButtonActionListener();
		r0.addActionListener(actionListener);
		r1.addActionListener(actionListener);
		r2.addActionListener(actionListener);
		r3.addActionListener(actionListener);
		r4.addActionListener(actionListener);

	}

	class RadioButtonActionListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent event) {
	        JRadioButton button = (JRadioButton) event.getSource();

	        if (button == r0) {
	        	masterClock.setCommandPause(false, 1000);
	        } else if (button == r1) {
	        	masterClock.setCommandPause(true, 250);
	        } else if (button == r2) {
	        	masterClock.setCommandPause(true, 333.333);
	        } else if (button == r3) {
	        	masterClock.setCommandPause(true, 500);
	        } else if (button == r4) {
	        	masterClock.setCommandPause(true, 999.999);
	        }
	    }
	}

	public void createSafetyPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(SAFETY_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}

	public void createSciencePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(SCIENCE_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}

    /**
     * Returns a list of other settlements.
     *
     * @return sample long list data
     */
    protected List<Settlement> getOtherSettlements() {
    	List<Settlement> list0 = new ArrayList<>(unitManager.getSettlements());
    	list0.remove(settlement);
        return list0;

    }

	/**
	 * Picks a task and delete it.
	 */
	public void deleteATask() {
		TaskJob n = list.getSelectedValue();
		if (n != null) {
			((Person) personComboBox.getSelectedItem()).getMind().getTaskManager().deleteAPendingTask(n);
			logBookTA.append("Delete '" + n + "' from the list of task orders.\n");
		}
		else
			listUpdate();
	}

	public void listUpdate() {
		listModel.update();
 		list.validate();
 		list.revalidate();
 		list.repaint();
 		listScrollPanel.validate();
 		listScrollPanel.revalidate();
 		listScrollPanel.repaint();
	}

	public boolean isNavPointsMapTabOpen() {
        return tabPane.getSelectedIndex() == 1;
	}

	@Override
	public void update() {

		// Update list
		listUpdate();
	}

	private void disableAllCheckedSettlement() {
		for(Component c : tradingPartnersPanel.getComponents()) {
			((JCheckBox) c).setSelected(false);
		}
	}

	/**
	 * Lists model for the tasks in queue.
	 */
	private class ListModel extends AbstractListModel<TaskJob> {

	    /** default serial id. */
	    private static final long serialVersionUID = 1L;

	    private List<TaskJob> list = new ArrayList<>();

	    private ListModel() {
	    	Person selected = (Person) personComboBox.getSelectedItem();

	    	if (selected != null) {
	        	List<TaskJob> tasks = selected.getMind().getTaskManager().getPendingTasks();
		        if (tasks != null)
		        	list.addAll(tasks);
	    	}
	    }

        @Override
        public TaskJob getElementAt(int index) {
        	TaskJob result = null;

            if ((index >= 0) && (index < list.size())) {
                result = list.get(index);
            }

            return result;
        }

        @Override
        public int getSize() {
        	if (list == null)
        		return 0;
        	return list.size();
        }

        /**
         * Updates the list model.
         */
        public void update() {

        	List<TaskJob> newTasks = ((Person) personComboBox.getSelectedItem()).getMind().getTaskManager().getPendingTasks();

        	if (newTasks != null) {
	    		// if the list contains duplicate items, it somehow pass this test
	    		if (list.size() != newTasks.size() || !list.containsAll(newTasks) || !newTasks.containsAll(list)) {	
	                list = new ArrayList<>(newTasks);
	                fireContentsChanged(this, 0, getSize());
	    		}
        	}
        }
	}

	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;
		private String prompt;

		/**
		 *  Set the text to display when no item has been selected.
		 */
		public PromptComboBoxRenderer(String prompt) {
			this.prompt = prompt;
		}

		/**
		 *  Custom rendering to display the prompt text when no item is selected.
		 */
		public Component getListCellRendererComponent(
				JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value == null) {
				setText(prompt);
				return this;
			}

	        return c;
		}
	}
	
	/**
	 * Inner class combo box model for settlements.
	 */
	public class SettlementComboBoxModel extends DefaultComboBoxModel<Settlement>
		implements UnitListener {

		/**
		 * Constructor.
		 */
		public SettlementComboBoxModel() {
			// User DefaultComboBoxModel constructor.
			super();
			// Initialize settlement list.
			updateSettlements();

			// Add addUnitListener
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<>(settlements);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().addUnitListener(this);
			}

		}

		/**
		 * Updates the list of settlements.
		 */
		private void updateSettlements() {
			// Clear all elements
			removeAllElements();

			List<Settlement> settlements = new ArrayList<>();

			// Add the command dashboard button
			if (GameManager.getGameMode() == GameMode.COMMAND) {
				settlements = unitManager.getCommanderSettlements();
			}

			else if (GameManager.getGameMode() == GameMode.SANDBOX) {
				settlements.addAll(unitManager.getSettlements());
			}

			Collections.sort(settlements);

			Iterator<Settlement> i = settlements.iterator();
			while (i.hasNext()) {
				addElement(i.next());
			}
		}

		@Override
		public void unitUpdate(UnitEvent event) {
			// Note: Easily 100+ UnitEvent calls every second
			UnitEventType eventType = event.getType();
			if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
				Object target = event.getTarget();
				Building building = (Building) target; // overwrite the dummy building object made by the constructor
				BuildingManager mgr = building.getBuildingManager();
				Settlement s = mgr.getSettlement();
				// Set the selected settlement
				changeSettlement(s);
				// Updated ComboBox
				settlementListBox.setSelectedItem(s);
			}

			else if (eventType == UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT) {
				// Update the number of citizens
				Settlement s = (Settlement) settlementListBox.getSelectedItem();
				// Set the selected settlement
				changeSettlement(s);
				
				setUpPersonComboBox(s);
		
				// Set the box opaque
				settlementListBox.setOpaque(false);
			}
		}

		/**
		 * Prepares class for deletion.
		 */
		public void destroy() {
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<>(settlements);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().removeUnitListener(this);
			}
		}
	}
	

	/**
	 * Prepares tool window for deletion.
	 */
	@Override
	public void destroy() {
		tabPane = null;
		personComboBox = null;
		listModel = null;
		listScrollPanel = null;
		list = null;
		cc = null;
	}
}
