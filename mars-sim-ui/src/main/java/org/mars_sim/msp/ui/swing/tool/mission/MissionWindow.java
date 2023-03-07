/*
 * Mars Simulation Project
 * MissionWindow.java
 * @date 2022-07-31
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionManagerListener;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.mission.create.CreateMissionWizard;
import org.mars_sim.msp.ui.swing.tool.mission.edit.EditMissionDialog;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;

/**
 * Window for the mission tool.
 */
@SuppressWarnings("serial")
public class MissionWindow extends ToolWindow {

	/** Tool name. */
	public static final String NAME = "Mission Tool";
	public static final String ICON = "mission";
	
	public static final int WIDTH = 640;
	public static final int LEFT_PANEL_WIDTH = 200;
	public static final int HEIGHT = 640;

	// Private members
	private MainDetailPanel mainPanel;
	private Mission missionCache;

	private NavpointPanel navpointPane;

	private CreateMissionWizard createMissionWizard;

	private EditMissionDialog editMissionDialog;

	private MissionManager missionMgr;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode missionRoot;
	private Map<Settlement,DefaultMutableTreeNode> settlementNodes = new HashMap<>();
	private Map<Mission,DefaultMutableTreeNode> missionNodes = new HashMap<>();
	private JButton abortButton;
	private JButton editButton;
	private JTree missionTree;

	/**
	 * Constructor.
	 *
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public MissionWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);

		this.missionMgr = desktop.getSimulation().getMissionManager();
		this.missionMgr.addListener(new MissionManagerListener() {

			@Override
			public void addMission(Mission mission) {
				addMissionNode(mission);
			}

			@Override
			public void removeMission(Mission mission) {
				removeMissionNode(mission);
			}
			
		});

		// Create content panel.
		JPanel mPane = new JPanel(new BorderLayout());
		mPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mPane);

		// Create the left panel.
		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.setBorder(StyleManager.createLabelBorder("Missions"));
		mPane.add(treePanel, BorderLayout.WEST);

		missionRoot = new DefaultMutableTreeNode("Settlements");
		treeModel = new DefaultTreeModel(missionRoot);
		missionTree = new JTree(treeModel);
		missionTree.setMinimumSize(new Dimension(LEFT_PANEL_WIDTH, HEIGHT - 10));
		missionTree.setExpandsSelectedPaths(true);              
		missionTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
                        .getPath().getLastPathComponent();
				Object selection = node.getUserObject();

				if (selection instanceof Mission) {
					Mission m = (Mission) selection;
					selectMission(m);
				}
				else {
					selectMission(null);
				}
            }
        });
		treePanel.add(missionTree, BorderLayout.CENTER);


		for(Mission m : missionMgr.getMissions()) {
			addMissionNode(m);
		}

		// Create the info tab panel.
		JTabbedPane tabPane = new JTabbedPane();
		mPane.add(tabPane, BorderLayout.CENTER);

		// Create the main detail panel.
		mainPanel = new MainDetailPanel(desktop, this);
		mainPanel.setSize(new Dimension(WIDTH - LEFT_PANEL_WIDTH, HEIGHT));
		tabPane.add("Main", mainPanel);

		// Create the navpoint panel.
		navpointPane = new NavpointPanel(this);
		tabPane.add("Navigation", navpointPane);

		JSplitPane spliter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, tabPane);
		mPane.add(spliter, BorderLayout.CENTER);

		// Create the button panel.
		JPanel buttonPane = new JPanel(new FlowLayout());
		mPane.add(buttonPane, BorderLayout.SOUTH);

		// Create the create mission button.
		JButton createButton = new JButton("Create New Mission");
		createButton.addActionListener(e -> 
				// Create new mission.
				createNewMission()
		);
		buttonPane.add(createButton);

		// Create the edit mission button.
		editButton = new JButton("Modify Mission");
		editButton.setEnabled(false);

		editButton.addActionListener(e -> {
			if (missionCache != null) editMission(missionCache);
		});

		buttonPane.add(editButton);

		// Create the abort mission button.
		abortButton = new JButton("Abort Mission");
		abortButton.setEnabled(false);
		abortButton.addActionListener(e -> {
			// End the mission.
			if (missionCache != null) missionCache.abortMission();
		});
	

		buttonPane.add(abortButton);

		setSize(new Dimension(WIDTH, HEIGHT));
		setResizable(true);

		setVisible(true);
	}

	private DefaultMutableTreeNode addMissionNode(Mission m) {
		Settlement s = m.getAssociatedSettlement();
		DefaultMutableTreeNode sNode = settlementNodes.get(s);
		if (sNode == null) {
			sNode = new DefaultMutableTreeNode(s.getName(), true);
			//missionRoot.add(parent);
			treeModel.insertNodeInto(sNode, missionRoot, missionRoot.getChildCount());
			settlementNodes.put(s, sNode);
		}

		DefaultMutableTreeNode mNode = new DefaultMutableTreeNode(m, false);
		treeModel.insertNodeInto(mNode, sNode, sNode.getChildCount());
		missionNodes.put(m, mNode);

		// Open Tree
		missionTree.expandPath(new TreePath(sNode.getPath()));

		return mNode;
	}

	private void removeMissionNode(Mission m) {
		DefaultMutableTreeNode mNode = missionNodes.get(m);
		if (mNode == null) {
			return;
		}
		treeModel.removeNodeFromParent(mNode);
	}
	
	
	/**
	 * Selects a mission for display.
	 *
	 * @param newMission the mission to select.
	 */
	private void selectMission(Mission newMission) {	
		
		if (missionCache == null || !missionCache.equals(newMission)) {
			// Update the cache
			missionCache = newMission;

			abortButton.setEnabled(missionCache != null);
			editButton.setEnabled(missionCache != null);

			// Highlight the selected mission in Main tab
			mainPanel.setMission(newMission);
			// Highlight the selected mission in Nav tab
			navpointPane.setMission(newMission);
		}
	}

	/**
	 * Open wizard to create a new mission.
	 */
	private void createNewMission() {
		createMissionWizard = new CreateMissionWizard(desktop, this);
	}

	/**
	 * Open wizard to edit a mission.
	 * @param mission the mission to edit.
	 */
	private void editMission(Mission mission) {
		editMissionDialog = new EditMissionDialog(desktop, mission, this);
	}

	public CreateMissionWizard getCreateMissionWizard() {
		return createMissionWizard;
	}

	public Mission getMission() {
		return missionCache;
	}

	/**
	 * Time has advanced
	 * @param pulse The clock change
	 */
	@Override
	public void update(ClockPulse pulse) {
		navpointPane.update(pulse);
	}

	/**
	 * Prepares tool window for deletion.
	 */
	@Override
	public void destroy() {
		navpointPane.destroy();
	}

	/**
	 * External tools has asked to open a Mission
	 * @param mission Mission to display
	 */
	public void openMission(Mission mission) {
		DefaultMutableTreeNode found = missionNodes.get(mission);
		if (found == null) {
			// Should never happen
			found = addMissionNode(mission);
		}

		TreeNode[] path = found.getPath();
		missionTree.setSelectionPath(new TreePath(path));
	}
}
