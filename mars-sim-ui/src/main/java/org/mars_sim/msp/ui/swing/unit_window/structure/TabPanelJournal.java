/**
 * Mars Simulation Project
 * MissionTabPanel.java
 * @version 3.07 2014-12-01
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.vehicle.TabPanelMission;

/**
 * Tab panel displaying a list of settlement missions.<br>
 * Renamed to plural form to be distinguishable from
 * {@link TabPanelMission}, which displays a vehicle's
 * single mission's details.
 */
public class TabPanelJournal
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private Settlement settlement;
	private List<Mission> missionsCache;
	private DefaultListModel<Mission> missionListModel;
	private JList<Mission> missionList;
	private JButton missionButton;
	private JButton monitorButton;
	private JCheckBox overrideCheckbox;

	/**
	 * Constructor.
	 * @param settlement {@link Settlement} the settlement this tab panel is for.
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public TabPanelJournal(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelJournal.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelJournal.tooltip"), //$NON-NLS-1$
			settlement, desktop
		);

		// Initialize data members.
		this.settlement = settlement;

		// Create label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Create title label.
		JLabel label = new JLabel(Msg.getString("TabPanelJournal.label"), JLabel.CENTER); //$NON-NLS-1$
		label.setFont(new Font("Serif", Font.BOLD, 16));
		labelPanel.add(label);

		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(centerPanel, BorderLayout.CENTER);
		
		JournalArea textArea = new JournalArea();
		centerPanel.add(textArea);
		
		// Create mission scroll panel.
		//		JScrollPane missionScrollPanel = new JScrollPane();
		//		missionScrollPanel.setPreferredSize(new Dimension(190, 220));
		//		missionListPanel.add(missionScrollPanel);
				
		
		
		/*
		// Create center panel.
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(centerPanel, BorderLayout.CENTER);

		// Create mission list panel.
		JPanel missionListPanel = new JPanel();
		centerPanel.add(missionListPanel, BorderLayout.CENTER);

		// Create mission scroll panel.
		JScrollPane missionScrollPanel = new JScrollPane();
		missionScrollPanel.setPreferredSize(new Dimension(190, 220));
		missionListPanel.add(missionScrollPanel);

		// Create mission list model.
		missionListModel = new DefaultListModel<Mission>();
		MissionManager manager = Simulation.instance().getMissionManager();
		missionsCache = manager.getMissionsForSettlement(settlement);
		Iterator<Mission> i = missionsCache.iterator();
		while (i.hasNext()) missionListModel.addElement(i.next());

		// Create mission list.
		missionList = new JList<Mission>(missionListModel);
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		missionList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				boolean missionSelected = !missionList.isSelectionEmpty();
				missionButton.setEnabled(missionSelected);
				monitorButton.setEnabled(missionSelected);
			}
		});
		missionScrollPanel.setViewportView(missionList);

		// Create button panel.
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		centerPanel.add(buttonPanel, BorderLayout.EAST);

		// Create inner button panel.
		JPanel innerButtonPanel = new JPanel(new GridLayout(2, 1, 0, 2));
		buttonPanel.add(innerButtonPanel, BorderLayout.NORTH);

		// Create mission button.
		missionButton = new JButton(ImageLoader.getIcon(Msg.getString("img.mission"))); //$NON-NLS-1$
		missionButton.setMargin(new Insets(1, 1, 1, 1));
		missionButton.setToolTipText(Msg.getString("TabPanelJournal.tooltip.mission")); //$NON-NLS-1$
		missionButton.setEnabled(false);
		missionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openMissionTool();
			}
		});
		innerButtonPanel.add(missionButton);

		// Create monitor button.
		monitorButton = new JButton(ImageLoader.getIcon(Msg.getString("img.monitor"))); //$NON-NLS-1$
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.setToolTipText(Msg.getString("TabPanelJournal.tooltip.monitor")); //$NON-NLS-1$
		monitorButton.setEnabled(false);
		monitorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openMonitorTool();
			}
		});
		innerButtonPanel.add(monitorButton);

		// Create bottom panel.
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(bottomPanel, BorderLayout.SOUTH);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelJournal.checkbox.overrideMissionCreation")); //$NON-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelJournal.tooltip.overrideMissionCreation")); //$NON-NLS-1$
		overrideCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setMissionCreationOverride(overrideCheckbox.isSelected());
			}
		});
		overrideCheckbox.setSelected(settlement.getMissionCreationOverride());
		bottomPanel.add(overrideCheckbox);
*/
	}


		
		class JournalArea extends JTextArea  {
		
			private static final long serialVersionUID = 1L;
		public JournalArea()   {  }  
		    //Override JTextArea paint method  
		 //Enables it to paint JTextArea background image  
		public void paint(Graphics g)  {  
			  //Make JTextArea transparent  
			  setOpaque(false);  
			   
			  //add wrap  
			  setLineWrap(true);  
			   
			  //Make JTextArea word wrap  
			  setWrapStyleWord(true);  
			   
			  //Get image that we use as JTextArea background
			  //Choose your own image directory in parameters.
			  //ImageIcon ii=new ImageIcon("one.jpg");  
			  
			  //Image i=ii.getImage();  
			  
			   Image img;
			   String IMAGE_DIR = "/images/";
	           String fullImageName = "LanderHab.png";
	            String fileName = fullImageName.startsWith("/") ?
	                	fullImageName :
	                	IMAGE_DIR + fullImageName;
	            URL resource = ImageLoader.class.getResource(fileName);
				Toolkit kit = Toolkit.getDefaultToolkit();
				img = kit.createImage(resource);
			   
			  //Draw JTextArea background image  
			  g.drawImage(img,0,0,null,this);  
			   
			  //Call super.paint to see TextArea
			  super.paint(g);  
		  }
		}
		 
	@Override
	public void update() {
/*
		// Get all missions for the settlement.
		MissionManager manager = Simulation.instance().getMissionManager();
		List<Mission> missions = manager.getMissionsForSettlement(settlement);

		// Update mission list if necessary.
		if (!missions.equals(missionsCache)) {
			Mission selectedMission = (Mission) missionList.getSelectedValue();

			missionsCache = missions;
			missionListModel.clear();
			Iterator<Mission> i = missionsCache.iterator();
			while (i.hasNext()) missionListModel.addElement(i.next());

			if ((selectedMission != null) && missionListModel.contains(selectedMission))
				missionList.setSelectedValue(selectedMission, true);
		}

		// Update mission override check box if necessary.
		if (settlement.getMissionCreationOverride() != overrideCheckbox.isSelected()) 
			overrideCheckbox.setSelected(settlement.getMissionCreationOverride());
	*/
	}

	/**
	 * Opens the mission tool to the selected mission in the mission list.
	 */
	private void openMissionTool() {
		Mission mission = (Mission) missionList.getSelectedValue();
		if (mission != null) {
			((MissionWindow) getDesktop().getToolWindow(MissionWindow.NAME)).selectMission(mission);
			getDesktop().openToolWindow(MissionWindow.NAME);
		}
	}

	/**
	 * Opens the monitor tool with a mission tab for the selected mission 
	 * in the mission list.
	 */
	private void openMonitorTool() {
		Mission mission = (Mission) missionList.getSelectedValue();
		if (mission != null) getDesktop().addModel(new PersonTableModel(mission));
	}

	/**
	 * Sets the settlement mission creation override flag.
	 * @param override the mission creation override flag.
	 */
	private void setMissionCreationOverride(boolean override) {
		settlement.setMissionCreationOverride(override);
	}
}