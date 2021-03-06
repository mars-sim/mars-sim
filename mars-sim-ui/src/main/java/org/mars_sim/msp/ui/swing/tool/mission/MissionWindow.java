/**
 * Mars Simulation Project
 * MissionWindow.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.mission.create.CreateMissionWizard;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.tabbedpane.WebTabbedPane;

/**
 * Window for the mission tool.
 */
@SuppressWarnings("serial")
public class MissionWindow extends ToolWindow {

	/** Tool name. */
	public static final String NAME = "Mission Tool";
	public static final int WIDTH = 640;
	public static final int HEIGHT = 640;
	
	// Private members
	private WebTabbedPane tabPane;
	private MainDetailPanel mainPanel;
	
	private JList<Settlement> settlementList;
	private JList<Mission> missionList;

	private SettlementListModel settlementListModel;
	private MissionListModel missionListModel;
	
	private Settlement settlement;
	private Mission mission;
	
	private NavpointPanel navpointPane;

	private CreateMissionWizard createMissionWizard;
//	private EditMissionDialog editMissionDialog;

	/**
	 * Constructor.
	 * 
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public MissionWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);

		// Create content panel.
		WebPanel mPane = new WebPanel(new BorderLayout());
		mPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mPane);

		// Create the left panel.
		WebPanel leftPane = new WebPanel(new BorderLayout());
		mPane.add(leftPane, BorderLayout.WEST);
		
		// Create the settlement list panel.
		WebPanel settlementListPane = new WebPanel(new BorderLayout());
		settlementListPane.setPreferredSize(new Dimension(200, 200));
		leftPane.add(settlementListPane, BorderLayout.NORTH);
		
		// Create the mission list panel.
		WebPanel missionListPane = new WebPanel(new BorderLayout());
		missionListPane.setPreferredSize(new Dimension(200, 400));
		leftPane.add(missionListPane, BorderLayout.CENTER);

		// Create the settlement list.
		settlementListModel = new SettlementListModel();
		settlementList = new JList<Settlement>(settlementListModel);
		settlementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		settlementListPane.add(new WebScrollPane(settlementList), BorderLayout.CENTER);
//		settlementList.addListSelectionListener(this);
		settlementList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if (me.getClickCount() == 1) {
					JList<Settlement> target = (JList<Settlement>)me.getSource();
					int index = target.locationToIndex(me.getPoint());
					if (index >= 0) {
						Settlement settlement = (Settlement)target.getModel().getElementAt(index);
						selectSettlement(settlement);
	      			
						// Update Nav tab's map
						navpointPane.updateCoords(settlement.getCoordinates());
	               }
	            }
	         }
		});
			
		// Create the mission list.
		missionListModel = new MissionListModel(this);
		missionList = new JList<Mission>(missionListModel);
		missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		missionListPane.add(new WebScrollPane(missionList), BorderLayout.CENTER);
		missionList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if (me.getClickCount() == 1) {
					JList<Mission> target = (JList<Mission>)me.getSource();
					int index = target.locationToIndex(me.getPoint());
					if (index >= 0) {
						Mission mission = (Mission)target.getModel().getElementAt(index);
//						target.setSelectedIndex(index);
						target.ensureIndexIsVisible(index);
						selectMission(mission);
	               }
	            }
	         }
		});
		
		// Create the info tab panel.
		tabPane = new WebTabbedPane();
		mPane.add(tabPane, BorderLayout.CENTER);

		// Create the main detail panel.
		mainPanel = new MainDetailPanel(desktop, this);
//		missionList.addListSelectionListener(infoPane);
		tabPane.add("Main", mainPanel);

		// Create the navpoint panel.
		navpointPane = new NavpointPanel(desktop);
		missionList.addListSelectionListener(navpointPane);
		tabPane.add("Navigation", navpointPane);

		// Create the button panel.
		WebPanel buttonPane = new WebPanel(new FlowLayout());
		mPane.add(buttonPane, BorderLayout.SOUTH);

		// Create the create mission button.
		WebButton createButton = new WebButton("Create New Mission");
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Create new mission.
				createNewMission();
			}
		});
		buttonPane.add(createButton);

		// Create the edit mission button.
		final WebButton editButton = new WebButton("Modify Mission");
		editButton.setEnabled(false);

//		editButton.addActionListener(
//				new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						// Edit the mission.
//						mission = (Mission) missionList.getSelectedValue();
//						if (mission != null) editMission(mission);
//					}
//				});
//		missionList.addListSelectionListener(
//			new ListSelectionListener() {
//				public void valueChanged(ListSelectionEvent e) {
//					// Enable button if mission is selected in list.
//					editButton.setEnabled(missionList.getSelectedValue() != null);
//				}
//			}
//		);

		buttonPane.add(editButton);

		// Create the abort mission button.
		final WebButton abortButton = new WebButton("Abort Mission");
		abortButton.setEnabled(false);

//		abortButton.addActionListener(
//				new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						// End the mission.
//						mission = (Mission) missionList.getSelectedValue();
//						if (mission != null) endMission(mission);
//					}
//				});
//		missionList.addListSelectionListener(
//				new ListSelectionListener() {
//					public void valueChanged(ListSelectionEvent e) {
//						abortButton.setEnabled(missionList.getSelectedValue() != null);
//					}
//				});

		buttonPane.add(abortButton);

		setSize(new Dimension(WIDTH, HEIGHT));
		setMaximizable(true);
		setResizable(false);

		setVisible(true);
		// pack();

		Dimension desktopSize = desktop.getSize();
		Dimension jInternalFrameSize = this.getSize();
		int width = (desktopSize.width - jInternalFrameSize.width) / 2;
		int height = (desktopSize.height - jInternalFrameSize.height) / 2;
		setLocation(width, height);

	}

	/**
	 * Selects a mission for display.
	 * 
	 * @param newMission the mission to select.
	 */
	public void selectMission(Mission newMission) {	
		if (newMission == null) {
			missionList.clearSelection();
			return;
		}
		
		// when clicking elsewhere to open up the Mission Tool
		Settlement s = newMission.getAssociatedSettlement();
		if (s == null) {
			// Since the mission is completed, use the recorded settlement name 
			// to get back the settlement instance
			s = CollectionUtils.findSettlement(newMission.getSettlmentName());
		}
		
		// Select the settlement to populate missions
		selectSettlement(s);
		
		Mission selected = missionList.getSelectedValue();

		if (selected != null) {
			if (this.mission == null) {
				this.mission = newMission;
				// Highlight the selected mission
//				missionList.setSelectedValue(newMission, true);	
//				System.out.println("selectMission 0");
				mainPanel.setMission(newMission);
			}
			
			else if (!selected.equals(newMission)) {
				this.mission = newMission;
				// Highlight the selected mission
//				missionList.setSelectedValue(newMission, true);	
//				System.out.println("selectMission 1");
				mainPanel.setMission(newMission);
			}
			
			else { // selected is the same as newMission
				this.mission = newMission;
				// Highlight the selected mission
//				missionList.setSelectedValue(newMission, true);	
//				System.out.println("selectMission 2");
				mainPanel.setMission(newMission);
			}
		}
		
		else {
			this.mission = newMission;
			// Highlight the selected mission
//			missionList.setSelectedValue(newMission, true);
//			System.out.println("selectMission 3" + mainPanel);
			mainPanel.setMission(newMission);
		}
	}

	/**
	 * Selects a mission for display.
	 * 
	 * @param mission the mission to select.
	 */
	public void selectSettlement(Settlement settlement) {
		if (settlement == null) {
			settlementList.clearSelection();
			return;
		}
		
		Settlement selected = settlementList.getSelectedValue();

		if (selected != null) {
			if (this.settlement == null) {
				this.settlement = settlement;
				mainPanel.clearInfo();
				// Highlight the selected mission
				settlementList.setSelectedValue(settlement, true);	
//				System.out.println("selectSettlement 0");
			}
			
			else if (!selected.equals(settlement)) {
				this.settlement = settlement;
				missionList.clearSelection();
				mainPanel.clearInfo();
				// Highlight the selected settlement
				settlementList.setSelectedValue(settlement, true);	
//				System.out.println("selectSettlement 1");
			}
			else { // selected is the same as settlement
				this.settlement = settlement;
				// Highlight the selected settlement
				settlementList.setSelectedValue(settlement, true);	
//				System.out.println("selectSettlement 2");
			}
		}
		else {
			this.settlement = settlement;
			mainPanel.clearInfo();
			// Highlight the selected settlement
			settlementList.setSelectedValue(settlement, true);
//			System.out.println("selectSettlement 3");
		}
		
		// Update Nav tab's map
		navpointPane.updateCoords(settlement.getCoordinates());
		
		// Populate the missions in this settlement
		missionListModel.populateMissions();
		
		if (missionListModel.getSize() == 0 || mission == null) {
//			if (mainPanel == null) System.out.println("mainPane == null");
			mainPanel.clearInfo();
			return;
		}
	}

	/**
	 * Open wizard to create a new mission.
	 */
	private void createNewMission() {
		createMissionWizard = new CreateMissionWizard(desktop, this);
	}
	    
//	/**
//	 * Open wizard to edit a mission.
//	 * @param mission the mission to edit.
//	 */
//	private void editMission(Mission mission) {
//
//		if (ms != null)  {
//			// Track the current pause state
//			boolean previous = ms.startPause();
//
//			editMissionDialog = new EditMissionDialog(desktop, mission, this);
//
//			ms.endPause(previous);
//
//		} else
//
//			editMissionDialog = new EditMissionDialog(desktop, mission, this);
//	}


	public CreateMissionWizard getCreateMissionWizard() {
		return createMissionWizard;
	}

	public MainDesktopPane getDesktop() {
		return desktop;
	}

	public boolean isNavPointsMapTabOpen() {
		if (tabPane.getSelectedIndex() == 1)
			return true;
		else
			return false;
	}

	public Settlement getSettlement() {
		return settlement;
	}
	
	public Mission getMission() {
		return mission;
	}
	
	public void selectFirstIndex() {
		missionList.setSelectedIndex(0);
	}
	
	public JList<Mission> getMissionList() {
		return missionList;
	}
	
	/**
	 * Prepares tool window for deletion.
	 */
	@Override
	public void destroy() {
//		missionList.clearSelection();
		missionListModel.destroy();
//		settlementList.clearSelection();
		settlementListModel.destroy();
		navpointPane.destroy();
	}
}
