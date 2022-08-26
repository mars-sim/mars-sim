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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ListDataEvent;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SmartScroller;
import org.mars_sim.msp.ui.swing.tool.VerticalLabelUI;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;

import com.alee.extended.list.CheckBoxCellData;
import com.alee.extended.list.CheckBoxListModel;
import com.alee.extended.list.WebCheckBoxList;
import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.list.ListDataAdapter;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.managers.style.StyleId;

/**
 * Window for the Commanders Dashboard.
 */
@SuppressWarnings("serial")
public class CommanderWindow extends ToolWindow {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CommanderWindow.class.getName());

	/** Tool name. */
	public static final int LIST_WIDTH = 300;
	public static final int COMBOBOX_WIDTH = 200;

	public static final String NAME = "Commander Dashboard";

	public static final String LEADERSHIP_TAB = "Leadership";

	public static final String AGRICULTURE_TAB = "Agriculture";
	public static final String COMPUTING_TAB = "Computing";
	public static final String ENGINEERING_TAB = "Engineering";
	public static final String LOGISTIC_TAB = "Logistic";
	public static final String MISSION_TAB = " Mission";
	public static final String RESOURCE_TAB = "Resource";
	public static final String SAFETY_TAB = "Safety";
	public static final String SCIENCE_TAB = "Science";

	public static final String CAN_INITIATE = "Can initiate Trading Mission";
	public static final String CANNOT_INITIATE = "Cannot initiate Trading Mission";
	public static final String ACCEPT = "Accept Trading initiated by other settlements";
	public static final String ACCEPT_NO = "Accept NO Trading initiated by other settlements";
	public static final String SEE_RIGHT = ".    -->";

	// Private members
	private String deletingTaskType;

	private final Font SERIF = new Font("Serif", Font.PLAIN, 10);
	private final Font DIALOG = new Font( "Dialog", Font.PLAIN, 14);

	private JTabbedPane tabPane;
	
	private JComboBoxMW<String> taskComboBox;
	private JComboBoxMW<Person> personComboBox;
	/** Settlement Combo box */
	private WebComboBox settlementListBox;
	
	private ListModel listModel;
	private JList<String> list;
	private JTextArea logBookTA;

	private WebPanel emptyPanel = new WebPanel();
	private WebPanel mainPane;
	private WebPanel policyMainPanel;
	private WebPanel innerPanel;

	private JScrollPane listScrollPanel;
	private WebScrollPane WebScrollPane;

	private JRadioButton r0;
	private JRadioButton r1;
	private JRadioButton r2;
	private JRadioButton r3;
	private JRadioButton r4;

	private WebCheckBoxList<?> settlementMissionList;

	/** Settlement Combo box model. */
	private SettlementComboBoxModel settlementCBModel;
	
	private Person cc;

	private Settlement settlement;
	
	private List<Settlement> settlementList;

	private List<String> taskCache;

	/** The MarsClock instance. */
	private MarsClock marsClock;
	private MasterClock masterClock;
	private Simulation sim = Simulation.instance();
	private UnitManager unitManager = sim.getUnitManager();

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public CommanderWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);

		this.masterClock = sim.getMasterClock();
		this.marsClock = masterClock.getMarsClock();

		settlementList = new ArrayList<>(unitManager.getSettlements());
		Collections.sort(settlementList);
		settlement = settlementList.get(0);
		cc = settlement.getCommander();
		
		// Create content panel.
		mainPane = new WebPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		JPanel topPane = new WebPanel(new GridLayout(1, 3));
		mainPane.add(topPane, BorderLayout.NORTH);
		
		buildSettlementComboBox();
		topPane.add(new WebPanel(new JLabel("            ")));
		topPane.add(settlementListBox);
		topPane.add(new WebPanel(new JLabel("            ")));

		WebPanel bottomPane = new WebPanel(new GridLayout(1, 4));
		bottomPane.setPreferredSize(new Dimension(-1, 50));
		mainPane.add(bottomPane, BorderLayout.SOUTH);

//		JPanel leadershipPane = new JPanel(new BorderLayout());
//		leadershipPane.setPreferredSize(new Dimension(200, 50));
//		bottomPane.add(leadershipPane);

//		JLabel leadershipLabel = new JLabel("Leadership Points : ", JLabel.RIGHT);
//		bottomPane.add(leadershipLabel);

//		leadershipPointsLabel = new JLabel("", JLabel.LEFT);
//		bottomPane.add(leadershipPointsLabel);
//		bottomPane.add(new JLabel());
//		bottomPane.add(new JLabel());
//
//		leadershipPointsLabel.setText(commander.getLeadershipPoint() + "");

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
	@SuppressWarnings("unchecked")
	public void buildSettlementComboBox() {

		settlementCBModel = new SettlementComboBoxModel();

		settlementListBox = new WebComboBox(StyleId.comboboxHover, settlementCBModel);
		settlementListBox.setWidePopup(true);
		settlementListBox.setMaximumSize(getNameLength() * 8, 40);
		settlementListBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		settlementListBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		DefaultListCellRenderer listRenderer = new DefaultListCellRenderer();
		listRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER); // center-aligned items
		settlementListBox.setRenderer(listRenderer);
		
		settlementListBox.addItemListener(event -> {
				Settlement s = (Settlement) event.getItem();
				if (s != null) {
					// Change the person list in person combobox
					if (settlement != s)
						setUpPersonComboBox(s);
				
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
	public void setUpPersonComboBox(Settlement s) {
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
		personComboBox.addActionListener(e -> {
			//	Nothing for now selectedPerson = (Person) comboBox.getSelectedItem();
        });
		
		personComboBox.setMaximumRowCount(8);
		personComboBox.setSelectedItem(cc);
	}
	
	/**
	 * Changes the map display to the selected settlement.
	 *
	 * @param s
	 */
	public void changeSettlement(Settlement s) {
		// Set the selected settlement
		settlement = s;
		// Set the box opaque
		settlementListBox.setOpaque(false);
	}
	
    /**
     * Gets the length of the most lengthy settlement name
     *
     * @return
     */
    private int getNameLength() {
    	Collection<Settlement> slist = unitManager.getSettlements();
    	int max = 12;
    	for (Settlement s: slist) {
    		int size = s.getNickName().length();
    		if (max < size)
    			max = size;
    	}
    	return max;
    }
    
	public void createLeadershipPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(LEADERSHIP_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}

	public void createAgriculturePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(AGRICULTURE_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}

	public void createComputingPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(COMPUTING_TAB, panel);

		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);
	}
	
	public void createEngineeringPanel() {
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
	public void createPersonCombobox(JPanel panel) {
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
	public void createTaskCombobox(JPanel panel) {
      	// Set up combo box model.
		List<String> taskList = cc.getPreference().getTaskStringList();
		taskCache = new ArrayList<>(taskList);
		DefaultComboBoxModel<String> taskComboBoxModel = new DefaultComboBoxModel<String>();

		Iterator<String> i = taskCache.iterator();

		while (i.hasNext()) {
			String n = i.next();
	    	taskComboBoxModel.addElement(n);
		}

		// Create comboBox.
		taskComboBox = new JComboBoxMW<>(taskComboBoxModel);
		taskComboBox.addActionListener(e -> {
			// nothing for now taskName = (String) comboBox.getSelectedItem();
        });
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
				String taskName = (String) taskComboBox.getSelectedItem();
				selected.getMind().getTaskManager().addAPendingTask(taskName, true);

				logBookTA.append(marsClock.getTrucatedDateTimeStamp()
						+ " - Assigning '" + taskName + "' to " + selected + "\n");
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
	public void createTaskQueueList(JPanel panel) {

	    WebLabel label = new WebLabel("  Task Queue  ");
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
		list = new JList<String>(listModel);
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
	public void createLogBookPanel(JPanel panel) {

//		Use Border title = BorderFactory.createTitledBorder("Log Book");
		WebLabel logLabel = new WebLabel("          Log Book          ");
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
	public void createLogisticPanel() {
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
	public void createMissionPanel() {
		WebPanel panel = new WebPanel(new BorderLayout());
		tabPane.add(MISSION_TAB, panel);

		policyMainPanel = new WebPanel(new BorderLayout());
		panel.add(policyMainPanel, BorderLayout.NORTH);
		policyMainPanel.setPreferredSize(new Dimension(200, 125));
		policyMainPanel.setMaximumSize(new Dimension(200, 125));

		// Create a button panel
		WebPanel buttonPanel = new WebPanel(new GridLayout(4,1));
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

		WebLabel selectLabel = new WebLabel(" Choose :");
		selectLabel.setMinimumSize(new Dimension(150, 25));
		selectLabel.setPreferredSize(150, 25);

		innerPanel = new WebPanel(new BorderLayout());
		innerPanel.add(selectLabel, BorderLayout.NORTH);

		// Set settlement check boxes
		settlementMissionList = new WebCheckBoxList<>(StyleId.checkboxlist, createModel(getOtherSettlements()));
		settlementMissionList.addListDataListener(new ListDataAdapter() {
		    @Override
		    public void contentsChanged(final ListDataEvent e) {
		        final int index = e.getIndex0();
		        final boolean selected = settlementMissionList.isCheckBoxSelected(index);
	        	List<?> allowedSettlements = settlementMissionList.getCheckedValues();
	        	Settlement s =  (Settlement) allowedSettlements.get(index);
                settlement.setAllowTradeMissionFromASettlement(s, selected);
		    }
		} );

		settlementMissionList.setVisibleRowCount(3);
		innerPanel.add(settlementMissionList, BorderLayout.CENTER);

		WebScrollPane = new WebScrollPane(innerPanel);
		WebScrollPane.setMaximumWidth(250);

//		if (noTrading) {
//			r2.setSelected(true);
//			r3.setSelected(false);
//			policyMainPanel.remove(WebScrollPane);
//			policyMainPanel.add(emptyPanel, BorderLayout.EAST);
//		}
//		
//		else {
			r2.setSelected(false);
			r3.setSelected(true);
			r3.setText(ACCEPT + SEE_RIGHT);
			policyMainPanel.remove(emptyPanel);
			policyMainPanel.add(WebScrollPane, BorderLayout.EAST);
//		}

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
				policyMainPanel.remove(WebScrollPane);
				policyMainPanel.add(emptyPanel, BorderLayout.EAST);
	        } else if (button == r3) {
	        	logger.config("r3 selected");
				r3.setText(ACCEPT + SEE_RIGHT);
				policyMainPanel.remove(emptyPanel);
				policyMainPanel.add(WebScrollPane, BorderLayout.EAST);
	        }
	    }
	}

	public void createResourcePanel() {
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
     * Returns sample check box list model.
     *
     * @param data sample data
     * @return sample check box list model
     */
    protected static CheckBoxListModel<Settlement> createModel (final List<Settlement> settlements) {
        final CheckBoxListModel<Settlement> model = new CheckBoxListModel<Settlement>();
        for (final Settlement element : settlements) {
            model.add(new CheckBoxCellData<Settlement>(element));
        }
        return model;
    }

    /**
     * Returns a list of other settlements.
     *
     * @return sample long list data
     */
    protected List<Settlement> getOtherSettlements() {
    	List<Settlement> list0 = new ArrayList<>(desktop.getSimulation().getUnitManager().getSettlements());
    	list0.remove(settlement);
        return list0;

    }

	/**
	 * Picks a task and delete it.
	 */
	public void deleteATask() {
		String n = (String) list.getSelectedValue();
		if (n != null) {
			deletingTaskType = n;
			((Person) personComboBox.getSelectedItem()).getMind().getTaskManager().deleteAPendingTask(deletingTaskType);
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
//		leadershipPointsLabel.setText(commander.getLeadershipPoint() + "");

		// Update list
		listUpdate();
	}

	public void disableAllCheckedSettlement() {
		List<?> allowedSettlements = settlementMissionList.getCheckedValues();
		int size = allowedSettlements.size();
		for (int i=0; i<size; i++) {
			if (settlementMissionList.isCheckBoxSelected(i)) {
				Settlement s = (Settlement) allowedSettlements.get(i);
				settlementMissionList.setCheckBoxSelected(i, false);
				settlement.setAllowTradeMissionFromASettlement(s, false);
			}
		}
	}

	/**
	 * Lists model for the tasks in queue.
	 */
	private class ListModel extends AbstractListModel<String> {

	    /** default serial id. */
	    private static final long serialVersionUID = 1L;

	    private List<String> list = new ArrayList<>();

	    private ListModel() {
	    	Person selected = (Person) personComboBox.getSelectedItem();

	    	if (selected != null) {
	        	List<String> tasks = selected.getMind().getTaskManager().getPendingTasks();
		        if (tasks != null)
		        	list.addAll(tasks);
	    	}
	    }

        @Override
        public String getElementAt(int index) {
        	String result = null;

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

        	List<String> newTasks = ((Person) personComboBox.getSelectedItem()).getMind().getTaskManager().getPendingTasks();

        	if (newTasks != null) {
	    		// if the list contains duplicate items, it somehow pass this test
	    		if (list.size() != newTasks.size() || !list.containsAll(newTasks) || !newTasks.containsAll(list)) {
	                List<String> oldList = list;
	                List<String> tempList = new ArrayList<String>(newTasks);
	
	                list = tempList;
	                fireContentsChanged(this, 0, getSize());

	                oldList.clear();
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
				setText(Conversion.capitalize(prompt));
				return this;
			}

	        return c;
		}
	}
	
	/**
	 * Inner class combo box model for settlements.
	 */
	public class SettlementComboBoxModel extends DefaultComboBoxModel<Object>
		implements UnitManagerListener, UnitListener {

		/**
		 * Constructor.
		 */
		public SettlementComboBoxModel() {
			// User DefaultComboBoxModel constructor.
			super();
			// Initialize settlement list.
			updateSettlements();
			// Add this as a unit manager listener.
			unitManager.addUnitManagerListener(this);

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
		public void unitManagerUpdate(UnitManagerEvent event) {
			if (event.getUnit().getUnitType() == UnitType.SETTLEMENT) {
				updateSettlements();
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
			unitManager.removeUnitManagerListener(this);
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
		taskComboBox = null;
		listModel = null;
		listScrollPanel = null;
		list = null;
		cc = null;
		taskCache = null;
	}
}
