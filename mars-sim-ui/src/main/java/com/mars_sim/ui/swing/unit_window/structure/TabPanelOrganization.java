/*
 * Mars Simulation Project
 * TabPanelOrganization.java
 * @date 2023-11-15
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitManagerEvent;
import com.mars_sim.core.UnitManagerEventType;
import com.mars_sim.core.UnitManagerListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;
import com.mars_sim.core.structure.ChainOfCommand;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelOrganization is a tab panel showing the organizational structure of
 * a settlement.
 * 
 * @See https://docs.oracle.com/javase/tutorial/uiswing/components/tree.html#display
 */
@SuppressWarnings("serial")
public class TabPanelOrganization extends TabPanel {

	/** Default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelOrganization.class.getName());

	private static final String ORG_ICON = "organisation";
	
	/** The Settlement instance. */
	private Settlement settlement;

	private JTree tree;
	
	private DefaultMutableTreeNode root;

	private DefaultTreeModel defaultTreeModel;

	private Map<Person, RoleType> roles = new HashMap<>();

	private Map<RoleType,DefaultMutableTreeNode> roleNodes = new HashMap<>();

	private Map<Person, PersonListener> listeners  = new HashMap<>();

	private LocalUnitManagerListener unitManagerListener;

	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelOrganization(Settlement unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null, 
			ImageLoader.getIconByName(ORG_ICON),
			Msg.getString("TabPanelStructure.title"), //$NON-NLS-1$
			desktop);

		settlement = unit;

	}

	@Override
	protected void buildUI(JPanel content) {
		UnitManager unitManager = getSimulation().getUnitManager();
		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(UnitType.PERSON, unitManagerListener);

		// Create label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		content.add(labelPanel, BorderLayout.NORTH);

		// Prepare label
		JLabel label = new JLabel(Msg.getString("TabPanelStructure.label"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(label);
		labelPanel.add(label);

		root = new DefaultMutableTreeNode("  " + settlement.getName() + "  -  " + settlement.getUnitType().getName() + "  ");

		// Will figure out how to change font in ((DefaultMutableTreeNode) root.getParent()).getUserObject().setFont(labelFont)
		
		defaultTreeModel = new DefaultTreeModel(root);
		// Note : will allow changing role name in future : defaultTreeModel.addTreeModelListener(new MyTreeModelListener())
		
		tree = new JTree(defaultTreeModel);
		// Note : will allow changing role name in future : tree.setEditable(true)
		
		tree.getSelectionModel().setSelectionMode
		        (TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setVisibleRowCount(8);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(tree);
		content.add(scrollPane, BorderLayout.CENTER);
		
		initNodes();
	}

	/**
	 * Tracks tree changes.
	 *
	 * @param e TreeSelectionEvent
	 */
	public void valueChanged(TreeSelectionEvent e) {
		
		emptyNodes();
				
		initNodes();
	}

	protected void initNodes() {

		constructNodes();

		considerRoles();

		setupMouseOnNodes();

		for (int i = 0; i < tree.getRowCount(); i++)
			tree.expandRow(i);
	}

	private DefaultMutableTreeNode getRoleNode(RoleType roleType) {
		return roleNodes.computeIfAbsent(roleType, rt -> new DefaultMutableTreeNode(rt.getName()));
	}

	private void constructNodes() {
		int population = settlement.getNumCitizens();
		
		if (population <= ChainOfCommand.POPULATION_WITH_COMMANDER) {
			root.add(getRoleNode(RoleType.COMMANDER));
			var crewNode = new DefaultMutableTreeNode("Crew");
			for(var c : RoleUtil.getCrewRoles()) {
				crewNode.add(getRoleNode(c));
			}
			root.add(crewNode);

			return;
		}
		
		root.add(buildCommandNode(population));
		root.add(buildDivisionNode(population));
	}

	private DefaultMutableTreeNode buildCommandNode(int pop) {
		var commanderStaffNode = new DefaultMutableTreeNode("(A). Command Staff");
	
		// In terms of electing a president, it will take place among all 
		// the settlements under the same jurisdiction/authority
		
//		if (pop >= ChainOfCommand.POPULATION_WITH_PRESIDENT) {
//			commanderStaffNode.add(getRoleNode(RoleType.PRESIDENT));
//			commanderStaffNode.add(getRoleNode(RoleType.MAYOR));
//		}
//			
		if (pop >= ChainOfCommand.POPULATION_WITH_MAYOR) {
			commanderStaffNode.add(getRoleNode(RoleType.MAYOR));
		}
		
		else if (pop >= ChainOfCommand.POPULATION_WITH_ADMINISTRATOR) {
			commanderStaffNode.add(getRoleNode(RoleType.ADMINISTRATOR));
		}

		else if (pop >= ChainOfCommand.POPULATION_WITH_SUB_COMMANDER) {
			commanderStaffNode.add(getRoleNode(RoleType.COMMANDER));
			commanderStaffNode.add(getRoleNode(RoleType.SUB_COMMANDER));
		}

		else if (pop > ChainOfCommand.POPULATION_WITH_COMMANDER) {
			commanderStaffNode.add(getRoleNode(RoleType.COMMANDER));
		}
		return commanderStaffNode;
	}

	private DefaultMutableTreeNode buildDivisionNode(int pop) {
		var divisionNode = new DefaultMutableTreeNode("(B). Division");

		var agricultureNode = new DefaultMutableTreeNode("(1). Agriculture");
		divisionNode.add(agricultureNode);

		var computingNode = new DefaultMutableTreeNode("(2). Computing");
		divisionNode.add(computingNode);

		var engineeringNode = new DefaultMutableTreeNode("(3). Engineering");
		divisionNode.add(engineeringNode);

		var logisticNode = new DefaultMutableTreeNode("(4). Logistic");
		divisionNode.add(logisticNode);

		var missionNode = new DefaultMutableTreeNode("(5). Mission");
		divisionNode.add(missionNode);

		var	safetyNode = new DefaultMutableTreeNode("(6). Safety");
		divisionNode.add(safetyNode);

		var scienceNode = new DefaultMutableTreeNode("(7). Science");
		divisionNode.add(scienceNode);

		var supplyNode = new DefaultMutableTreeNode("(8). Supply");
		divisionNode.add(supplyNode);

		
		// Chiefs appear first
		if (pop >= ChainOfCommand.POPULATION_WITH_CHIEFS) {
			agricultureNode.add(getRoleNode(RoleType.CHIEF_OF_AGRICULTURE));
			computingNode.add(getRoleNode(RoleType.CHIEF_OF_COMPUTING));
			engineeringNode.add(getRoleNode(RoleType.CHIEF_OF_ENGINEERING));
			logisticNode.add(getRoleNode(RoleType.CHIEF_OF_LOGISTIC_OPERATION));
			missionNode.add(getRoleNode(RoleType.CHIEF_OF_MISSION_PLANNING));
			safetyNode.add(getRoleNode(RoleType.CHIEF_OF_SAFETY_HEALTH_SECURITY));
			scienceNode.add(getRoleNode(RoleType.CHIEF_OF_SCIENCE));
			supplyNode.add(getRoleNode(RoleType.CHIEF_OF_SUPPLY_RESOURCE));
		}

		agricultureNode.add(getRoleNode(RoleType.AGRICULTURE_SPECIALIST));
		computingNode.add(getRoleNode(RoleType.COMPUTING_SPECIALIST));
		engineeringNode.add(getRoleNode(RoleType.ENGINEERING_SPECIALIST));
		logisticNode.add(getRoleNode(RoleType.LOGISTIC_SPECIALIST));
		missionNode.add(getRoleNode(RoleType.MISSION_SPECIALIST));
		safetyNode.add(getRoleNode(RoleType.SAFETY_SPECIALIST));
		scienceNode.add(getRoleNode(RoleType.SCIENCE_SPECIALIST));
		supplyNode.add(getRoleNode(RoleType.RESOURCE_SPECIALIST));
		
		return divisionNode;
	}

	public void considerRoles() {

		Collection<Person> people = settlement.getAllAssociatedPeople();

		int population = people.size();
		
		for (Person p : people) {

			addListener(p);

			roles.clear();

			RoleType rt = p.getRole().getType();

			roles.put(p, rt);
			
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(p);
			if (rt != null) {
				var roleNode = getRoleNode(rt);
				roleNode.add(new DefaultMutableTreeNode(p));
			}
 			else {
				// anyone who does not belong will be placed in the root node
				root.add(node);
			}
		}
	}

	public void setupMouseOnNodes() {
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 2) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

						// Check for node to avoid java.lang.ClassCastException:
						// java.lang.String cannot be cast to com.mars_sim.core.person.Person
						if (node.getUserObject() instanceof Person person) {
							getDesktop().showDetails(person);
						}
					}
				}
			}
		};

		tree.addMouseListener(ml);

	}

	public void editIcons(JTree tree) {

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private Icon personIcon = UIManager.getIcon("RadioButton.icon");
			private Icon roleIcon = UIManager.getIcon("FileChooser.detailsViewIcon");
			private Icon homeIcon = UIManager.getIcon("FileChooser.homeFolderIcon");

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean isLeaf, int row, boolean focused) {
				Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
				if (isLeaf)
					// this node is a person
					setIcon(personIcon);
				else if (row == 0)
					// this is the root node
					setIcon(homeIcon);
				else
					// this node is just a role
					setIcon(roleIcon);
				return c;
			}
		});
	}


	/**
	 * Reloads the root.
	 */
	public void reloadTree() {
		defaultTreeModel.reload(root); // notify changes to model
		tree.expandPath(tree.getSelectionPath());
		for (int i = 0; i < tree.getRowCount(); i++)
			tree.expandRow(i);
	}

	/**
	 * Empties the nodes.
	 */
	public void emptyNodes() {

		for (int i = 0; i < tree.getRowCount(); i++)
			tree.collapseRow(i);

		root.removeAllChildren();
		roleNodes.clear();
	}

	/**
	 * Removes the listener for a person.
	 */
	public void removeListener(Person p) {
		p.removeUnitListener(listeners.get(p));

		listeners.remove(p);
	}

	/**
	 * Removes the listener for a person.
	 */
	public void addListener(Person p) {
		PersonListener pl = new PersonListener();
		p.addUnitListener(pl);
		listeners.put(p, pl);
	}
	/**
	 * PersonListener class listens to the change of each settler in a settlement.
	 */
	private class PersonListener implements UnitListener {

		/**
		 * Catch unit update event.
		 *
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			if (event.getType() == UnitEventType.ROLE_EVENT) {
				Unit unit = (Unit)event.getSource();
				if (unit.getUnitType() == UnitType.PERSON) {
					Person p = (Person) unit;
					if (p.getAssociatedSettlement() == settlement) {
						emptyNodes();
						initNodes();
						reloadTree();
					}
				}
			}
		}
	}

	/**
	 * UnitManagerListener inner class.
	 */
	private class LocalUnitManagerListener implements UnitManagerListener {

		/**
		 * Catches unit manager update event.
		 *
		 * @param event the unit event.
		 */
		public void unitManagerUpdate(UnitManagerEvent event) {
			Unit unit = event.getUnit();
			UnitManagerEventType eventType = event.getEventType();
			if (unit.getUnitType() == UnitType.PERSON) {
				if (eventType == UnitManagerEventType.ADD_UNIT) {
					addListener((Person) unit);
					emptyNodes();
					initNodes();
					reloadTree();
				}

				else if (eventType == UnitManagerEventType.REMOVE_UNIT) {
					removeListener((Person) unit);
					emptyNodes();
					initNodes();
					reloadTree();
				}
			}
		}
	}

	class MyTreeModelListener implements TreeModelListener {
	    public void treeNodesChanged(TreeModelEvent e) {
	        DefaultMutableTreeNode node;
	        node = (DefaultMutableTreeNode)
	                 (e.getTreePath().getLastPathComponent());
	        /*
	         * If the event lists children, then the changed
	         * node is the child of the node we have already
	         * gotten.  Otherwise, the changed node and the
	         * specified node are the same.
	         */
	        
	        try {
	            int index = e.getChildIndices()[0];
	            node = (DefaultMutableTreeNode)
	                   (node.getChildAt(index));
	        } catch (NullPointerException exc) {}

	        logger.info(settlement, "The user has finished editing the node.");
	        logger.info(settlement, "New value: " + node.getUserObject());
	    }
	    public void treeNodesInserted(TreeModelEvent e) {
	    }
	    public void treeNodesRemoved(TreeModelEvent e) {
	    }
	    public void treeStructureChanged(TreeModelEvent e) {
	    }
	}
	
	/**
	 * Prepares objects for garbage collection.
	 */
	@Override
	public void destroy() {		
		UnitManager unitManager = getSimulation().getUnitManager();
		unitManager.removeUnitManagerListener(UnitType.PERSON, unitManagerListener);

		super.destroy();
	}
}
