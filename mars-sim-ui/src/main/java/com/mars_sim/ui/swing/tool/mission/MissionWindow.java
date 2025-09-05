/*
 * Mars Simulation Project
 * MissionWindow.java
 * @date 2023-06-18
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.mars_sim.core.person.ai.mission.AbstractMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.MissionManagerListener;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.mission.create.CreateMissionWizard;
import com.mars_sim.ui.swing.tool.mission.edit.ModifyMissionDialog;
import com.mars_sim.ui.swing.tool_window.ToolWindow;

/**
 * Window for the mission tool.
 */
@SuppressWarnings("serial")
public class MissionWindow extends ToolWindow implements ConfigurableWindow {

	/** Tool name. */
	public static final String NAME = "mission";
	public static final String ICON = "mission";
	public static final String TITLE = "Mission Tool";

	
	private static final int PADDING = 20;
	static final int LEFT_PANEL_WIDTH = 220;
	private static final int MAP_WIDTH = 512;
	private static final int MAP_HEIGHT = 512;
	static final int WIDTH = LEFT_PANEL_WIDTH + MAP_WIDTH + PADDING * 2;
	
	private static final int MAP_BOX_HEIGHT = MAP_HEIGHT;
	static final int TABLE_HEIGHT = 160;
	static final int HEIGHT = MAP_BOX_HEIGHT + TABLE_HEIGHT;
	
	private static final String MISSIONNAME_PROP = "selected";

	// Private members
	private MainDetailPanel mainPanel;
	private Mission missionCache;

	private NavpointPanel navpointPane;

	private CreateMissionWizard createMissionWizard;

	private MissionManager missionMgr;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode missionRoot;
	private Map<Settlement,DefaultMutableTreeNode> settlementNodes = new HashMap<>();
	private Map<Mission,DefaultMutableTreeNode> missionNodes = new HashMap<>();
	private JButton abortButton;
	private JButton editButton;
	private JButton approveButton;
	private JTree missionTree;
	private JButton rejectButton;

	/**
	 * Constructor.
	 *
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public MissionWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, TITLE, desktop);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);//.HIDE_ON_CLOSE);

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

		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		// Create content panel.
		JPanel mPane = new JPanel(new BorderLayout());
		mPane.setBorder(StyleManager.newEmptyBorder());
		setContentPane(mPane);

		// Create the left panel.
		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.setBorder(StyleManager.createLabelBorder("Missions"));
		mPane.add(treePanel, BorderLayout.WEST);

		missionRoot = new DefaultMutableTreeNode("Settlements");
		treeModel = new DefaultTreeModel(missionRoot);
		missionTree = new JTree(treeModel);
		missionTree.setExpandsSelectedPaths(true);    
		missionTree.setCellRenderer(new MissionTreeRenderer());          
		missionTree.addTreeSelectionListener(e -> {
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
		            .getPath().getLastPathComponent();
			Object selection = node.getUserObject();

			if (selection instanceof Mission m) {
				selectMission(m);
			}
			else {
				selectMission(null);
			}
		});

		JScrollPane scroller = new JScrollPane(missionTree);
		
		scroller.setSize(new Dimension(LEFT_PANEL_WIDTH, HEIGHT - PADDING));
		scroller.setMinimumSize(new Dimension(LEFT_PANEL_WIDTH, HEIGHT - PADDING));
		
		treePanel.add(scroller, BorderLayout.CENTER);

		for(Mission m : missionMgr.getMissions()) {
			addMissionNode(m);
		}

		// Create the info tab panel.
		JTabbedPane tabPane = new JTabbedPane();
		mPane.add(tabPane, BorderLayout.CENTER);

		// Create the main detail panel.
		mainPanel = new MainDetailPanel(desktop, this);
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
		JButton createButton = new JButton("Create");
		createButton.addActionListener(e -> 
				// Create new mission.
				createNewMission()
		);
		buttonPane.add(createButton);

		// Create the edit mission button.
		editButton = new JButton("Modify");
		editButton.setEnabled(false);

		editButton.addActionListener(e -> {
			if (missionCache != null) editMission(missionCache);
		});
		buttonPane.add(editButton);

		// Create the approve mission button.
		approveButton = new JButton("Approve");
		approveButton.setEnabled(false);
		approveButton.addActionListener(e -> {
			if (missionCache != null) reviewMission(missionCache, true);
		});
		buttonPane.add(approveButton);
		rejectButton = new JButton("Reject");
		rejectButton.setEnabled(false);
		rejectButton.addActionListener(e -> {
			if (missionCache != null) reviewMission(missionCache, false);
		});
		buttonPane.add(rejectButton);

		// Create the abort mission button.
		abortButton = new JButton("Abort");
		abortButton.setEnabled(false);
		abortButton.addActionListener(e -> {
			// End the mission.
			if (missionCache != null) missionCache.abortMission(AbstractMission.MISSION_ABORTED_BY_PLAYER.getName());
		});
		buttonPane.add(abortButton);

		setSize(new Dimension(WIDTH, HEIGHT));
		setResizable(true);

		// Reselect previous mission
		Properties userSettings = desktop.getMainWindow().getConfig().getInternalWindowProps(NAME);
		String selectedMission = (userSettings != null ? userSettings.getProperty(MISSIONNAME_PROP) : null);
		if (selectedMission != null) {
			for(Mission m : missionMgr.getMissions()) {
				if (m.getName().equals(selectedMission)) {
					openMission(m);
					break;
				}
			}
		}
	}

	private DefaultMutableTreeNode addMissionNode(Mission m) {
		Settlement s = m.getAssociatedSettlement();
		DefaultMutableTreeNode sNode = settlementNodes.computeIfAbsent(s, k -> {
			var n = new DefaultMutableTreeNode(k.getName(), true);
			treeModel.insertNodeInto(n, missionRoot, missionRoot.getChildCount());
			return n;
		});

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
		
		// Update the cache
		missionCache = newMission;

		abortButton.setEnabled(missionCache != null);
		editButton.setEnabled(missionCache != null);

		boolean preMission = ((missionCache != null) && (missionCache.getStage() == Stage.PREPARATION));
		approveButton.setEnabled(preMission);
		rejectButton.setEnabled(preMission);

		// Highlight the selected mission in Main tab
		mainPanel.setMission(newMission);
		// Highlight the selected mission in Nav tab
		navpointPane.setMission(newMission);
	}

	/**
	 * Open wizard to create a new mission.
	 */
	private void createNewMission() {
		createMissionWizard = new CreateMissionWizard(desktop, this);
	}

	/**
	 * Approve a mission being review
	 * @param mission the mission to review
	 */
	private void reviewMission(Mission mission, boolean approved) {
		MissionPlanning plan = mission.getPlan();
		if ((plan != null) && plan.getStatus() == PlanType.PENDING) {
			missionMgr.approveMissionPlan(plan, (approved ?
								PlanType.APPROVED : PlanType.NOT_APPROVED), 0);
			selectMission(mission); // Force a full refresh
		}
	}


	/**
	 * Opens wizard to edit a mission.
	 * 
	 * @param mission the mission to edit.
	 */
	private void editMission(Mission mission) {
		new ModifyMissionDialog(desktop, mission, this);
	}

	public CreateMissionWizard getCreateMissionWizard() {
		return createMissionWizard;
	}

	public Mission getMission() {
		return missionCache;
	}

	/**
	 * Time has advanced.
	 * 
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

	/**
	 * Get the current status of the window in terms of User experience.
	 */
	@Override
	public Properties getUIProps() {
		Properties results = new Properties();
		if (missionCache != null) {
			results.setProperty(MISSIONNAME_PROP, missionCache.getName());
		}
		return results;
	}

	private static class MissionTreeRenderer extends DefaultTreeCellRenderer {
		private static final Icon COMPLETED = ImageLoader.getIconByName("mission/completed");
		private static final Icon IN_PROGRESS = ImageLoader.getIconByName("mission/inprogress");
		private static final Icon REVIEW = ImageLoader.getIconByName("mission/review");
	  
	  
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
													  boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object userObject = node.getUserObject();
			if (userObject instanceof Mission m) {
				Icon mIcon = IN_PROGRESS;
				MissionPlanning mp = m.getPlan();
				if ((mp != null) && (mp.getStatus() == PlanType.PENDING)) {
					mIcon = REVIEW;
				}
				else if (m.isDone()) {
					mIcon = COMPLETED;
				}
				this.setText(m.getName());
				this.setIcon(mIcon);
			}
			return this;
		}
	  }
}
