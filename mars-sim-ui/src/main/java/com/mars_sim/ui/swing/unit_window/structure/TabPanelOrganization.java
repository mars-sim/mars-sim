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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.Role;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The TabPanelOrganization is a tab panel showing the organizational structure of
 * a settlement.
 * 
 */
@SuppressWarnings("serial")
class TabPanelOrganization extends EntityTabPanel<Settlement> {

	private static final String ORG_ICON = "organisation";

	private JTree tree;
	
	private DefaultMutableTreeNode root;

	private DefaultTreeModel defaultTreeModel;

	private Map<RoleType,DefaultMutableTreeNode> roleNodes = new EnumMap<>(RoleType.class);

	// Shared listener for Role changes
	private RoleChangeListener roleChangeListener = new RoleChangeListener();

	// Keep track of persons we have added listener to
	private Set<Person> tracked = new HashSet<>();

	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param context the UI context.
	 */
	public TabPanelOrganization(Settlement unit, UIContext context) {
		// Use the TabPanel constructor
		super(
			"Organisation",
			ImageLoader.getIconByName(ORG_ICON), null,
			context, unit);
	}

	@Override
	protected void buildUI(JPanel content) {

		// Create label panel.
		var labelPanel = new AttributePanel();
		content.add(labelPanel, BorderLayout.NORTH);

		var settlement = getEntity();

		// Prepare label
		labelPanel.addLabelledItem(Msg.getString("authority.singular"),
					new EntityLabel(settlement.getReportingAuthority(), getContext()));
		var gov = settlement.getChainOfCommand().getGovernance();
		labelPanel.addTextField("Governance Model", gov.getName(), null);
		labelPanel.addTextField("Job Approvals", Boolean.toString(gov.needJobApproval()), null);
		labelPanel.addTextField("Mission Min. Reviewers", Integer.toString(gov.getUniqueReviewers()), null);

		root = new DefaultMutableTreeNode("Governance Roles");

		// Will figure out how to change font in ((DefaultMutableTreeNode) root.getParent()).getUserObject().setFont(labelFont)
		
		defaultTreeModel = new DefaultTreeModel(root);
		
		tree = new JTree(defaultTreeModel);
		// Note : will allow changing role name in future : tree.setEditable(true)
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setVisibleRowCount(8);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(tree);
		scrollPane.setBorder(SwingHelper.createLabelBorder("Role Assignments"));
		content.add(scrollPane, BorderLayout.CENTER);

		setupDoubleClickOnPersonNodes();

		initNodes(settlement);
	}

	private void initNodes(Settlement settlement) {

		constructNodes(settlement);

		addPersons(settlement);

		for (int i = 0; i < tree.getRowCount(); i++)
			tree.expandRow(i);
	}

	private DefaultMutableTreeNode getRoleNode(RoleType roleType) {
		return roleNodes.computeIfAbsent(roleType, rt -> new DefaultMutableTreeNode(rt.getName()));
	}

	private void constructNodes(Settlement settlement) {
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

	/**
	 * Remove listeners from tracked persons.
	 */
	@Override
	public void destroy() {
		tracked.forEach(p -> p.removeEntityListener(roleChangeListener));
		tracked.clear();
		super.destroy();
	}

	/**
	 * Add persons to the tree under their respective role nodes.
	 * @param settlement Settlement being scanned
	 */
	private void addPersons(Settlement settlement) {

		Collection<Person> people = settlement.getAllAssociatedPeople();
		
		for (Person p : people) {
			p.addEntityListener(roleChangeListener);
			tracked.add(p);

			RoleType rt = p.getRole().getType();
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

	/**
	 * Setup mouse listener on Person node so a double clock will show the details.
	 */
	private void setupDoubleClickOnPersonNodes() {
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
						getContext().showDetails(person);
					}
				}
			}
		};

		tree.addMouseListener(ml);
		tree.setToolTipText(Msg.getString("Entity.doubleClick"));
	}

	/**
	 * Reloads the root.
	 */
	private void reloadTree() {
		// Clear out old nodes
		emptyNodes();
		initNodes(getEntity());

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

		tracked.forEach(p -> p.removeEntityListener(roleChangeListener));
	}

	@Override
	public void refreshUI() {
		// Reload the tree to reflect any changes
		reloadTree();
	}

	/**
	 * PersonListener class listens to the change of each settler in a settlement.
	 */
	private class RoleChangeListener implements EntityListener {

		/**
		 * Catch unit update event.
		 *
		 * @param event the unit event.
		 */
		@Override
		public void entityUpdate(EntityEvent event) {
			if (event.getType().equals(Role.ROLE_EVENT)
					&& event.getSource() instanceof Person p
					&& getEntity().equals(p.getAssociatedSettlement())) {

				reloadTree();
			}
		}

		@Override
		public String toString() {
			return getEntity().getName() + " OrganisationTab:RoleChangeListener";
		}
	}
}
