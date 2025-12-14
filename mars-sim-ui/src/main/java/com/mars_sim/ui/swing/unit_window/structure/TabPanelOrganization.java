/*
 * Mars Simulation Project
 * TabPanelOrganization.java
 * @date 2023-11-15
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitManagerEvent;
import com.mars_sim.core.UnitManagerEventType;
import com.mars_sim.core.UnitManagerListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.Role;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The TabPanelOrganization is a tab panel showing the organizational structure of
 * a settlement.
 * 
 * @See https://docs.oracle.com/javase/tutorial/uiswing/components/tree.html#display
 */
@SuppressWarnings("serial")
public class TabPanelOrganization extends TabPanel {

	private static final String ORG_ICON = "organisation";
	
	/** The Settlement instance. */
	private Settlement settlement;

	private JTree tree;
	
	private DefaultMutableTreeNode root;

	private DefaultTreeModel defaultTreeModel;

	private Map<Person, RoleType> roles = new HashMap<>();

	private Map<RoleType,DefaultMutableTreeNode> roleNodes = new EnumMap<>(RoleType.class);

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
		var labelPanel = new AttributePanel();
		content.add(labelPanel, BorderLayout.NORTH);

		// Prepare label
		labelPanel.addLabelledItem(Msg.getString("Entity.authority"),
					new EntityLabel(settlement.getReportingAuthority(), getDesktop()));
		var gov = settlement.getChainOfCommand().getGovernance();
		labelPanel.addTextField("Governance Model", gov.getName(), null);
		labelPanel.addTextField("Job Approvals", Boolean.toString(gov.needJobApproval()), null);
		labelPanel.addTextField("Mission Min. Reviewers", Integer.toString(gov.getUniqueReviewers()), null);

		root = new DefaultMutableTreeNode("  " + settlement.getName() + "  -  " + settlement.getUnitType().getName() + "  ");

		// Will figure out how to change font in ((DefaultMutableTreeNode) root.getParent()).getUserObject().setFont(labelFont)
		
		defaultTreeModel = new DefaultTreeModel(root);
		
		tree = new JTree(defaultTreeModel);
		// Note : will allow changing role name in future : tree.setEditable(true)
		
		tree.getSelectionModel().setSelectionMode
		        (TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setVisibleRowCount(8);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(tree);
		scrollPane.setBorder(SwingHelper.createLabelBorder("Role Assignments"));
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
		var supported = settlement.getChainOfCommand().getGovernance().getAllRoles();
		var command = buildCommandSubTree(supported);
		if (command != null) {
			root.add(command);
		}
		var crew = buildCrewTree(supported);
		if (crew != null) {
			root.add(crew);
		}
		var division = buildDivisionTree(supported);
		if (division != null) {
			root.add(division);
		}
	}

	/**
	 * If the requested role is supported then create a TreeNode and add to the nodes.
	 * @param supported
	 * @param roleType
	 * @param nodes
	 */
	private void selectRole(List<RoleType> supported, RoleType roleType, List<MutableTreeNode> nodes) {	
		if (supported.contains(roleType)) {
			nodes.add(getRoleNode(roleType));
		}
	}

	/**
	 * Builds a subtree with the given name and children.
	 * @param name
	 * @param children
	 * @return The parent node or null if no children.
	 */
	private MutableTreeNode buildSubTree(String name, List<MutableTreeNode> children) {
		if (children.isEmpty()) {
			return null;
		}
		var parentNode = new DefaultMutableTreeNode(name);
		for (var n : children) {
			parentNode.add(n);
		}		
		return parentNode;
	}

	/**
	 * Build a subtree for the Command roles
	 * @param supportedRoles
	 * @return Nodes for Command roles or null if no Command roles supported
	 */
	private MutableTreeNode buildCommandSubTree(List<RoleType> supportedRoles) {
	
		List<MutableTreeNode> nodes = new ArrayList<>();

		// Find all the supported Command roles
		selectRole(supportedRoles, RoleType.MAYOR, nodes);
		selectRole(supportedRoles, RoleType.ADMINISTRATOR, nodes);
		selectRole(supportedRoles, RoleType.DEPUTY_ADMINISTRATOR, nodes);
		selectRole(supportedRoles, RoleType.COMMANDER, nodes);
		selectRole(supportedRoles, RoleType.SUB_COMMANDER, nodes);

		return buildSubTree("Command Staff", nodes);
	}

	/**
	 * Build a subtree for the Crew roles
	 * @param supportedRoles
	 * @return Nodes for Crew roles or null if no Crew roles supported
	 */
	private MutableTreeNode buildCrewTree(List<RoleType> supportedRoles) {
	
		List<MutableTreeNode> nodes = new ArrayList<>();

		// Find all the Crew roles
		RoleUtil.getCrewRoles().forEach(c -> selectRole(supportedRoles, c, nodes));

		return buildSubTree("Crew", nodes);
	}

	/**
	 * Builds a division node based on the chief role. Any specialist role is derived from the Chief role.
	 * @param name
	 * @param chief
	 * @param supportedRoles
	 * @param divisions
	 */
	private void buildDivisionNode(String name, RoleType chief, List<RoleType> supportedRoles, List<MutableTreeNode> divisions) {
		var specialist = RoleType.getChiefSpeciality(chief);
		if (supportedRoles.contains(chief) || supportedRoles.contains(specialist)) {
			var divisionNode = new DefaultMutableTreeNode(name);
			if (supportedRoles.contains(chief)) {
				divisionNode.add(getRoleNode(chief));
			}
			if (supportedRoles.contains(specialist)) {
				divisionNode.add(getRoleNode(specialist));
			}
			divisions.add(divisionNode);
		}
	}

	/**
	 * Build a subtree for the Division roles
	 * @param supportedRoles
	 * @return Nodes for Division roles or null if no Division roles supported
	 */
	private MutableTreeNode buildDivisionTree(List<RoleType> supportedRoles) {
		List<MutableTreeNode> divisions = new ArrayList<>();
		buildDivisionNode("Agriculture", RoleType.CHIEF_OF_AGRICULTURE, supportedRoles, divisions);
		buildDivisionNode("Computing", RoleType.CHIEF_OF_COMPUTING, supportedRoles, divisions);
		buildDivisionNode("Engineering", RoleType.CHIEF_OF_ENGINEERING, supportedRoles, divisions);
		buildDivisionNode("Logistic", RoleType.CHIEF_OF_LOGISTIC_OPERATION, supportedRoles, divisions);
		buildDivisionNode("Mission", RoleType.CHIEF_OF_MISSION_PLANNING, supportedRoles, divisions);
		buildDivisionNode("Safety", RoleType.CHIEF_OF_SAFETY_HEALTH_SECURITY, supportedRoles, divisions);
		buildDivisionNode("Science", RoleType.CHIEF_OF_SCIENCE, supportedRoles, divisions);
		buildDivisionNode("Supply", RoleType.CHIEF_OF_SUPPLY_RESOURCE, supportedRoles, divisions);

		return buildSubTree("Division", divisions);
	}

	private void considerRoles() {

		Collection<Person> people = settlement.getAllAssociatedPeople();
		
		for (Person p : people) {

			addListener(p);

			roles.clear();

			RoleType rt = p.getRole().getType();

			roles.put(p, rt);
			
			DefaultMutableTreeNode parent = root;
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(p);
			if (rt != null) {
				var roleNode = getRoleNode(rt);
				if (roleNode != null) {
					parent = roleNode;
				}
			}
 			parent.add(node);
		}
	}

	private void setupMouseOnNodes() {
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && e.getClickCount() == 2) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

					// Check for node to avoid java.lang.ClassCastException:
					// java.lang.String cannot be cast to com.mars_sim.core.person.Person
					if (node.getUserObject() instanceof Person person) {
						getDesktop().showDetails(person);
					}
				}
			}
		};

		tree.addMouseListener(ml);
	}

	/**
	 * Reloads the root.
	 */
	private void reloadTree() {
		defaultTreeModel.reload(root); // notify changes to model
		tree.expandPath(tree.getSelectionPath());
		for (int i = 0; i < tree.getRowCount(); i++)
			tree.expandRow(i);
	}

	/**
	 * Empties the nodes.
	 */
	private void emptyNodes() {

		for (int i = 0; i < tree.getRowCount(); i++)
			tree.collapseRow(i);

		root.removeAllChildren();
		roleNodes.clear();
	}

	/**
	 * Removes the listener for a person.
	 */
	private void removeListener(Person p) {
		p.removeEntityListener(listeners.get(p));

		listeners.remove(p);
	}

	/**
	 * Removes the listener for a person.
	 */
	private void addListener(Person p) {
		PersonListener pl = new PersonListener();
		p.addEntityListener(pl);
		listeners.put(p, pl);
	}
	/**
	 * PersonListener class listens to the change of each settler in a settlement.
	 */
	private class PersonListener implements EntityListener {

		/**
		 * Catch unit update event.
		 *
		 * @param event the unit event.
		 */
		@Override
		public void entityUpdate(EntityEvent event) {
			if (event.getType().equals(Role.ROLE_EVENT)
					&& event.getSource() instanceof Person p
					&& settlement.equals(p.getAssociatedSettlement())) {
				emptyNodes();
				initNodes();
				reloadTree();
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
