/**
 * Mars Simulation Project
 * CommanderWindow.java
 * @version 3.1.2 2020-09-02
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
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Commander;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SmartScroller;
import org.mars_sim.msp.ui.swing.tool.VerticalLabelUI;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import com.alee.extended.list.CheckBoxCellData;
import com.alee.extended.list.CheckBoxListModel;
import com.alee.extended.list.WebCheckBoxList;
import com.alee.laf.label.WebLabel;
import com.alee.laf.list.ListDataAdapter;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.managers.style.StyleId;

/**
 * Window for the Commander Dashboard.
 */
@SuppressWarnings("serial")
public class CommanderWindow extends ToolWindow {

	/** Tool name. */
	public static final String NAME = "Commander Dashboard";
	
	public static final String LEADERSHIP_TAB = "Leadership";
	
	public static final String AGRICULTURE_TAB = "Agriculture";
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
//	private int deletingTaskIndex;
	
//	private boolean changed = true;
	
	private String deletingTaskType;
	private String taskName;
	
	private JTabbedPane tabPane;
	private DefaultComboBoxModel<String> comboBoxModel;
	private JComboBoxMW<String> comboBox;
	private ListModel listModel;
	private JScrollPane listScrollPanel;
	private JList<String> list;
//	private JLabel leadershipPointsLabel;
	private JTextArea jta;

	private WebPanel emptyPanel = new WebPanel();
	private WebPanel mainPane;
	private WebPanel policyMainPanel;
	private WebPanel innerPanel;
	
	private WebScrollPane WebScrollPane;
	
	private JRadioButton r0;
	private JRadioButton r1;
	private JRadioButton r2;
	private JRadioButton r3;
	private JRadioButton r4;		
	
	private WebCheckBoxList<?> settlementMissionList;
	
	private Commander commander = SimulationConfig.instance().getPersonConfig().getCommander();

	private Person person;
	
	private Settlement settlement;
	
	private List<String> taskCache;

	
	/**
	 * Constructor.
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public CommanderWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);

		person = GameManager.commanderPerson;
		settlement = person.getAssociatedSettlement();
		
		// Create content panel.
		mainPane = new WebPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create the mission list panel.
//		JPanel listPane = new JPanel(new BorderLayout());
//		listPane.setPreferredSize(new Dimension(200, 200));
//		mainPane.add(listPane, BorderLayout.WEST);

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
		
		createEngineeringPanel();
		
		createLeadershipPanel();
		
		createLogisticPanel();
			
		createMissionPanel();
		
		createResourcePanel();

		createSafetyPanel();
		
		createSciencePanel();
		
		setSize(new Dimension(640, 480));
		setMaximizable(true);
		setResizable(false);

		setVisible(true);
		//pack();

		Dimension desktopSize = desktop.getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);

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
	
	public void createEngineeringPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(ENGINEERING_TAB, panel);
		
		JPanel topPanel = new JPanel(new BorderLayout(20, 20));
		panel.add(topPanel, BorderLayout.NORTH);	
	}
	
	public void createLogisticPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		tabPane.add(LOGISTIC_TAB, panel);
		tabPane.setSelectedComponent(panel);
		
	    JPanel topPanel = new JPanel(new FlowLayout());
	    panel.add(topPanel, BorderLayout.NORTH);

		// Create a button panel
	    JPanel buttonPanel = new JPanel(new BorderLayout());
	    JButton addButton = new JButton(Msg.getString("BuildingPanelFarming.addButton")); //$NON-NLS-1$
		addButton.setPreferredSize(new Dimension(60, 20));
		addButton.setFont(new Font("Serif", Font.PLAIN, 9));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				taskName = (String) comboBox.getSelectedItem();
				person.getMind().getTaskManager().addAPendingTask(taskName);
				jta.append("Add '" + taskName + "' to the list.\n");
		        listUpdate();
				repaint();
			}
			});
		buttonPanel.add(addButton, BorderLayout.NORTH);
		topPanel.add(buttonPanel);
				
		// Create a delete button
		JButton delButton = new JButton(Msg.getString("BuildingPanelFarming.delButton")); //$NON-NLS-1$
		delButton.setPreferredSize(new Dimension(60, 20));
		delButton.setFont(new Font("Serif", Font.PLAIN, 9));

		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!list.isSelectionEmpty() && (list.getSelectedValue() != null)) {
					deleteATask();
					listUpdate();
	            	repaint();
				}
			}
			});
		buttonPanel.add(delButton, BorderLayout.CENTER);

       	// Set up combo box model.
		List<String> taskList = GameManager.commanderPerson.getPreference().getTaskStringList();
		taskCache = new ArrayList<>(taskList);
		comboBoxModel = new DefaultComboBoxModel<String>();

		Iterator<String> i = taskCache.iterator();
//		int j = 0;
		while (i.hasNext()) {
			String n = i.next();
	    	comboBoxModel.addElement(n);
		}
		
		// Create comboBox.
		comboBox = new JComboBoxMW<String>(comboBoxModel);
		comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	taskName = (String) comboBox.getSelectedItem();
            }
        });
		comboBox.setMaximumRowCount(10);
//		comboBox.setSelectedIndex(-1);
	    topPanel.add(comboBox, BorderLayout.CENTER);

	    JPanel queueListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

	    WebLabel taskQueueLabel = new WebLabel("     Task Queue    ");
		taskQueueLabel.setUI(new VerticalLabelUI(false));
	    taskQueueLabel.setFont( new Font( "Dialog", Font.PLAIN, 14) );
		taskQueueLabel.setBorder(new MarsPanelBorder());
		
	    JPanel taskQueuePanel = new JPanel(new BorderLayout());
	    taskQueuePanel.add(taskQueueLabel, BorderLayout.NORTH);
		queueListPanel.add(taskQueuePanel);
		
		panel.add(queueListPanel, BorderLayout.CENTER); // 2nd add
	    
		// Create scroll panel for population list.
		listScrollPanel = new JScrollPane();
		listScrollPanel.setPreferredSize(new Dimension(240, 120));
		listScrollPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		// Create list model
		listModel = new ListModel();
		
		// Create list
		list = new JList<String>(listModel);
		listScrollPanel.setViewportView(list);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
		        if (!event.getValueIsAdjusting() && event != null){
					deleteATask();
		        }
		    }
		});
		queueListPanel.add(listScrollPanel);
		
//		Border title = BorderFactory.createTitledBorder("Log Book");
		WebLabel logLabel = new WebLabel("       Log Book        ");
		logLabel.setUI(new VerticalLabelUI(true));
		logLabel.setFont( new Font( "Dialog", Font.PLAIN, 14) );
		logLabel.setBorder(new MarsPanelBorder());
		
		// Create an text area
		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));//new FlowLayout(FlowLayout.CENTER, 18, 5));
		jta = new JTextArea(10, 28);
		jta.setEditable(false);
		JScrollPane scrollTextArea = new JScrollPane (jta, 
				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//		scrollTextArea.setSize(new Dimension(100, 100));
		// Monitor the vertical scroll of jta
		new SmartScroller(scrollTextArea, SmartScroller.VERTICAL, SmartScroller.END);
			
		textPanel.add(scrollTextArea);
		
//		JPanel titlePanel = new JPanel(new BorderLayout(0, 0));
//		titlePanel.setBorder(title);
//		titlePanel.add(textPanel, BorderLayout.CENTER);
		
	    JPanel logPanel = new JPanel(new BorderLayout());
	    logPanel.add(logLabel, BorderLayout.NORTH);
	    textPanel.add(logPanel);
		
		panel.add(textPanel, BorderLayout.SOUTH);
		
	}
	
	/**
	 * Creates the mission tab panel
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
//		buttonPanel.setPreferredSize(new Dimension(250, 120));
		policyMainPanel.add(buttonPanel, BorderLayout.CENTER);
		
		buttonPanel.setBorder(BorderFactory.createTitledBorder("Trading policy "));
		buttonPanel.setToolTipText("Select your trading policy with other settlements");
		
		ButtonGroup group0 = new ButtonGroup();
		ButtonGroup group1 = new ButtonGroup();
	
		r0 = new JRadioButton(CAN_INITIATE, true);
		r1 = new JRadioButton(CANNOT_INITIATE);

		// Set up initial conditions
		if (settlement.isMissionDisable(Trade.DEFAULT_DESCRIPTION)) {
			r0.setSelected(false);
			r1.setSelected(true);
		}
		else {
			r0.setSelected(true);
			r1.setSelected(false);
		}

		// Set up initial conditions
		boolean noTrading = false;
//		if (settlement.isTradeMissionAllowedFromASettlement(settlement)) {
//			List<Settlement> list = getOtherSettlements();
////			List<Settlement> allowedSettlements = settlementMissionList.getCheckedValues();
//			for (Settlement s: list) {
//				if (!settlement.isTradeMissionAllowedFromASettlement(s)) {
//					noTrading = true;
//					break;
//				}
//			}
//		}
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
		        if (selected) {
		        	settlement.setAllowTradeMissionFromASettlement(s, true);
		        }
		        else {
		        	settlement.setAllowTradeMissionFromASettlement(s, false); 	
		        }
		    }
		} );
		
		settlementMissionList.setVisibleRowCount(3);
		innerPanel.add(settlementMissionList, BorderLayout.CENTER);
		
		WebScrollPane = new WebScrollPane(innerPanel);
		WebScrollPane.setMaximumWidth(250);

//		mainPanel.add(WebScrollPane, BorderLayout.EAST);
		
		if (noTrading) {			
			r2.setSelected(true);
			r3.setSelected(false);
			policyMainPanel.remove(WebScrollPane);
			policyMainPanel.add(emptyPanel, BorderLayout.EAST);
//			settlementMissionList.setEnabled(false);
		}
		else {
			r2.setSelected(false);
			r3.setSelected(true);
			r3.setText(ACCEPT + SEE_RIGHT);
			policyMainPanel.remove(emptyPanel);
			policyMainPanel.add(WebScrollPane, BorderLayout.EAST);
//			settlementMissionList.setEnabled(true);
		}
		
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
	        	settlement.setMissionDisable(Trade.DEFAULT_DESCRIPTION, false);
	        } else if (button == r1) {
	        	settlement.setMissionDisable(Trade.DEFAULT_DESCRIPTION, true);
	        } else if (button == r2) {
//	        	SwingUtilities.invokeLater(() -> {
					System.out.println("r2 selected");
		        	disableAllCheckedSettlement();
	//	        	settlementMissionList.setEnabled(false);
					r3.setText(ACCEPT);
					policyMainPanel.remove(WebScrollPane);
					policyMainPanel.add(emptyPanel, BorderLayout.EAST);
//	        	});
	        } else if (button == r3) {
				System.out.println("r3 selected");
//	        	changed = true;
//	        	settlementMissionList.setEnabled(true);
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
    	List<Settlement> list = new ArrayList<Settlement>(unitManager.getSettlements());
    	list.remove(settlement);
//    	list.removeIf(x -> list.contains(settlement));
        return list;
//        return new ArrayList<Settlement>(unitManager.getSettlements()).remove(settlement);
    }
    
	public MainDesktopPane getDesktop() {
		return desktop;
	}
	
	/**
	 * Picks a task and delete it
	 */
	public void deleteATask() {
		String n = (String) list.getSelectedValue();
		if (n != null) {
			deletingTaskType = n;
//			deletingTaskIndex = list.getSelectedIndex();
			person.getMind().getTaskManager().deleteAPendingTask(deletingTaskType);
			jta.append("Delete '" + n + "' from the list.\n");
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
//		comboBox.setRenderer(new PromptComboBoxRenderer("A list of tasks"));
//		comboBox.setSelectedIndex(-1);
	}
	
	public boolean isNavPointsMapTabOpen() {
		if (tabPane.getSelectedIndex() == 1)
			return true;
		else
			return false;
	}
	
	public void update() {
//		leadershipPointsLabel.setText(commander.getLeadershipPoint() + "");
		
		// Update list
		listUpdate();
		
//		// Update the settlement that are being checked
//		if (changed) { //r3.isSelected()) {
//			List<?> allowedSettlements =  settlementMissionList.getCheckedValues();
//			for (Object o: allowedSettlements) {
//				Settlement s = (Settlement) o;
//				if (!settlement.isTradeMissionAllowedFromASettlement(s))
//					// If this settlement hasn't been set to allow trade mission, allow it now
//					settlement.setAllowTradeMissionFromASettlement(s, true);
//			}
//			changed = false;
//		}
	}
	
	public void disableAllCheckedSettlement() {
		List<?> allowedSettlements = settlementMissionList.getCheckedValues();
		int size = allowedSettlements.size();
//		System.out.println(allowedSettlements);
		for (int i=0; i<size; i++) {
			if (settlementMissionList.isCheckBoxSelected(i)) {
				Settlement s = (Settlement) allowedSettlements.get(i);
//				System.out.println("i : " + i + "  " + s);
				settlementMissionList.setCheckBoxSelected(i, false);
				settlement.setAllowTradeMissionFromASettlement(s, false);
			}	
		}
	}
	
	/**
	 * List model for the tasks in queue.
	 */
	private class ListModel extends AbstractListModel<String> {

	    /** default serial id. */
	    private static final long serialVersionUID = 1L;

	    private List<String> list;

	    private ListModel() {

        	List<String> c = person.getMind().getTaskManager().getPendingTasks();//.getPreference().getTaskStringList();
	        if (c != null)
	        	list = new ArrayList<String>(c);
	        else 
	        	list = null;
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
         * Update the list model.
         */
        public void update() {

        	List<String> c = person.getMind().getTaskManager().getPendingTasks();
    		// if the list contains duplicate items, it somehow pass this test
    		if (list.size() != c.size() || !list.containsAll(c) || !c.containsAll(list)) {
                List<String> oldList = list;
                List<String> tempList = new ArrayList<String>(c);
                //Collections.sort(tempList);

                list = tempList;
                fireContentsChanged(this, 0, getSize());

                oldList.clear();
           }
        }
	}
	
	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;
		private String prompt;

		/*
		 *  Set the text to display when no item has been selected.
		 */
		public PromptComboBoxRenderer(String prompt) {
			this.prompt = prompt;
		}

		/*
		 *  Custom rendering to display the prompt text when no item is selected
		 */
		// Add color rendering
		public Component getListCellRendererComponent(
				JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value == null) {
				setText(Conversion.capitalize(prompt));
				return this;
			}

			if (c instanceof WebLabel) {

	            if (isSelected) {
	                //c.setBackground(Color.orange);
	            } else {
	                //c.setBackground(Color.white);
	                //c.setBackground(new Color(51,25,0,128));
	            }

	        } else {
	        	//c.setBackground(Color.white);
	            //c.setBackground(new Color(51,25,0,128));
	            c = super.getListCellRendererComponent(
	                    list, value, index, isSelected, cellHasFocus);
	        }
	        return c;
		}
	}
	
	/**
	 * Prepares tool window for deletion.
	 */
	@Override
	public void destroy() {
		tabPane = null;
		comboBoxModel = null;
		comboBox = null;
		listModel = null;
		listScrollPanel = null;
		list = null;
//		leadershipPointsLabel = null;
		commander = null;
		person = null;
		taskCache = null;
	}
}
