/**
 * Mars Simulation Project
 * TabPanelOrganization.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TreeSearchable;

/**
 * The TabPanelStructure is a tab panel showing the organizational structure of
 * a settlement.
 */
@SuppressWarnings("serial")
public class TabPanelOrganization extends TabPanel {

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private JPanel infoPanel;

	private JTree tree;

	private DefaultMutableTreeNode root;
	
	private DefaultTreeModel defaultTreeModel;
	
	private DefaultMutableTreeNode commanderStaffNode;
	private DefaultMutableTreeNode commanderNode;
	private DefaultMutableTreeNode subCommanderNode;

	private DefaultMutableTreeNode divisionNode;
	private DefaultMutableTreeNode mayorNode;

	private DefaultMutableTreeNode agricultureNode;
	private DefaultMutableTreeNode agricultureSpecialistNode;
	private DefaultMutableTreeNode agricultureChiefNode;

	private DefaultMutableTreeNode engineeringNode;
	private DefaultMutableTreeNode engineeringSpecialistNode;
	private DefaultMutableTreeNode engineeringChiefNode;

	private DefaultMutableTreeNode logisticNode;
	private DefaultMutableTreeNode logisticSpecialistNode;
	private DefaultMutableTreeNode logisticChiefNode;

	private DefaultMutableTreeNode missionNode;
	private DefaultMutableTreeNode missionSpecialistNode;
	private DefaultMutableTreeNode missionChiefNode;

	private DefaultMutableTreeNode safetyNode;
	private DefaultMutableTreeNode safetySpecialistNode;
	private DefaultMutableTreeNode safetyChiefNode;

	private DefaultMutableTreeNode scienceNode;
	private DefaultMutableTreeNode scienceSpecialistNode;
	private DefaultMutableTreeNode scienceChiefNode;
	private DefaultMutableTreeNode supplyNode;
	private DefaultMutableTreeNode supplySpecialistNode;
	private DefaultMutableTreeNode supplyChiefNode;
	
	private Map<Person, RoleType> roles = new HashMap<>();
	
	private List<DefaultMutableTreeNode> nodes = new ArrayList<>();
	
	private Map<Person, PersonListener> listeners  = new HashMap<>();
	
	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelOrganization(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super("Org", //$NON-NLS-1$
				null, Msg.getString("TabPanelStructure.tooltip"), //$NON-NLS-1$
				unit, desktop);

		settlement = (Settlement) unit;
	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;		

		UnitManager unitManager = Simulation.instance().getUnitManager();
		LocalUnitManagerListener unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);

		// Create label panel.
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePanel);

		// Prepare label
		JLabel tlabel = new JLabel(Msg.getString("TabPanelStructure.title"), JLabel.CENTER); //$NON-NLS-1$
		tlabel.setFont(new Font("Serif", Font.BOLD, 16));
		// tlabel.setForeground(new Color(102, 51, 0)); // dark brown
		titlePanel.add(tlabel);

		// Prepare info panel.
		infoPanel = new JPanel(new GridLayout(1, 2, 0, 0));
//		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Create label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		infoPanel.add(labelPanel);

		// Prepare label
		JLabel label = new JLabel(Msg.getString("TabPanelStructure.label"), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(label);

		root = new DefaultMutableTreeNode(settlement.getName());

		tree = new JTree(root);
		tree.setVisibleRowCount(8);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		//Listen for when the selection changes.
//	    tree.addTreeSelectionListener(new MyTreeModelListener());
	    
		// Use treeSearchable
		TreeSearchable searchable = SearchableUtils.installSearchable(tree);
		searchable.setPopupTimeout(5000);
		searchable.setCaseSensitive(false);
		
		defaultTreeModel = new DefaultTreeModel(root);
		tree.setModel(defaultTreeModel);

		centerContentPanel.add(new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
	
		buildTreeNodes();
		
		initNodes();
	}
	
//	ImageIcon leafIcon = createImageIcon("images/middle.gif");
//	if (leafIcon != null) {
//	    DefaultTreeCellRenderer renderer = 
//	        new DefaultTreeCellRenderer();
//	    renderer.setLeafIcon(leafIcon);
//	    tree.setCellRenderer(renderer);
//	}
	
	/**
	 * Track tree changes
	 * 
	 * @param e TreeSelectionEvent
	 */
	public void valueChanged(TreeSelectionEvent e) {
		//Returns the last path element of the selection.
		// This method is useful only when the selection model allows a single selection.
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		                       tree.getLastSelectedPathComponent();

	    if (node == null)
	    //Nothing is selected.     
	    return;

	    Object nodeInfo = node.getUserObject();
	    if (node.isLeaf()) {
	        ;
	    } else {
	       ; 
	    }
	}
	
	public void initNodes() {
		
		constructNodes();
		
		considerRoles();
		
		setupMouseOnNodes();
		
		for (int i = 0; i < tree.getRowCount(); i++)
			tree.expandRow(i);
	}

	public void buildTreeNodes() {

		commanderStaffNode = new DefaultMutableTreeNode("Command Staff");
		commanderNode = new DefaultMutableTreeNode(RoleType.COMMANDER.toString());
		subCommanderNode = new DefaultMutableTreeNode(RoleType.SUB_COMMANDER.toString());

		nodes.add(commanderStaffNode);
		nodes.add(commanderNode);
		nodes.add(subCommanderNode);
		
		divisionNode = new DefaultMutableTreeNode("Division");
		mayorNode = new DefaultMutableTreeNode(RoleType.MAYOR.toString());

		nodes.add(divisionNode);
		nodes.add(mayorNode);
		
		agricultureNode = new DefaultMutableTreeNode("Agriculture");
		agricultureSpecialistNode = new DefaultMutableTreeNode(
				RoleType.AGRICULTURE_SPECIALIST.toString());
		agricultureChiefNode = new DefaultMutableTreeNode(
				RoleType.CHIEF_OF_AGRICULTURE.toString());

		nodes.add(agricultureNode);
		nodes.add(agricultureSpecialistNode);
		nodes.add(agricultureChiefNode);
		
		engineeringNode = new DefaultMutableTreeNode("Engineering");
		engineeringSpecialistNode = new DefaultMutableTreeNode(
				RoleType.ENGINEERING_SPECIALIST.toString());
		engineeringChiefNode = new DefaultMutableTreeNode(
				RoleType.CHIEF_OF_ENGINEERING.toString());

		nodes.add(engineeringNode);
		nodes.add(engineeringSpecialistNode);
		nodes.add(engineeringChiefNode);
		
		logisticNode = new DefaultMutableTreeNode("Logistic");
		logisticSpecialistNode = new DefaultMutableTreeNode(
				RoleType.LOGISTIC_SPECIALIST.toString());
		logisticChiefNode = new DefaultMutableTreeNode(
				RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS.toString());

		nodes.add(logisticNode);
		nodes.add(logisticSpecialistNode);
		nodes.add(logisticChiefNode);
		
		missionNode = new DefaultMutableTreeNode("Mission");
		missionSpecialistNode = new DefaultMutableTreeNode(
				RoleType.MISSION_SPECIALIST.toString());
		missionChiefNode = new DefaultMutableTreeNode(
				RoleType.CHIEF_OF_MISSION_PLANNING.toString());

		nodes.add(missionNode);
		nodes.add(missionSpecialistNode);
		nodes.add(missionChiefNode);
		
		safetyNode = new DefaultMutableTreeNode("Safety");
		safetySpecialistNode = new DefaultMutableTreeNode(RoleType.SAFETY_SPECIALIST.toString());
		safetyChiefNode = new DefaultMutableTreeNode(
				RoleType.CHIEF_OF_SAFETY_N_HEALTH.toString());

		nodes.add(safetyNode);
		nodes.add(safetySpecialistNode);
		nodes.add(safetyChiefNode);
		
		scienceNode = new DefaultMutableTreeNode("Science");
		scienceSpecialistNode = new DefaultMutableTreeNode(
				RoleType.SCIENCE_SPECIALIST.toString());
		scienceChiefNode = new DefaultMutableTreeNode(RoleType.CHIEF_OF_SCIENCE);

		nodes.add(scienceNode);
		nodes.add(scienceSpecialistNode);
		nodes.add(scienceChiefNode);
		
		supplyNode = new DefaultMutableTreeNode("Supply");
		supplySpecialistNode = new DefaultMutableTreeNode(
				RoleType.RESOURCE_SPECIALIST.toString());
		supplyChiefNode = new DefaultMutableTreeNode(RoleType.CHIEF_OF_SUPPLY_N_RESOURCES);

		nodes.add(supplyNode);
		nodes.add(supplySpecialistNode);
		nodes.add(supplyChiefNode);
	}
	
	public void deleteAllNodes() {
		nodes.clear();
		root.removeAllChildren();
	}
	
	public void constructNodes() {
		int population = settlement.getNumCitizens();

		if (population >= ChainOfCommand.POPULATION_WITH_CHIEFS) {
			
			divisionNode.add(agricultureNode);
			agricultureNode.add(agricultureChiefNode);
			agricultureNode.add(agricultureSpecialistNode);

			divisionNode.add(engineeringNode);
			engineeringNode.add(engineeringChiefNode);
			engineeringNode.add(engineeringSpecialistNode);

			divisionNode.add(logisticNode);
			logisticNode.add(logisticChiefNode);
			logisticNode.add(logisticSpecialistNode);

			divisionNode.add(missionNode);
			missionNode.add(missionChiefNode);
			missionNode.add(missionSpecialistNode);

			divisionNode.add(safetyNode);
			safetyNode.add(safetyChiefNode);
			safetyNode.add(safetySpecialistNode);

			divisionNode.add(scienceNode);
			scienceNode.add(scienceChiefNode);
			scienceNode.add(scienceSpecialistNode);

			divisionNode.add(supplyNode);
			supplyNode.add(supplyChiefNode);
			supplyNode.add(supplySpecialistNode);

		}
		
		else {
			
			divisionNode.add(agricultureNode);
			agricultureNode.add(agricultureSpecialistNode);

			divisionNode.add(engineeringNode);
			engineeringNode.add(engineeringSpecialistNode);

			divisionNode.add(logisticNode);
			logisticNode.add(logisticSpecialistNode);

			divisionNode.add(missionNode);
			missionNode.add(missionSpecialistNode);

			divisionNode.add(safetyNode);
			safetyNode.add(safetySpecialistNode);

			divisionNode.add(scienceNode);
			scienceNode.add(scienceSpecialistNode);

			divisionNode.add(supplyNode);
			supplyNode.add(supplySpecialistNode);
		}

		
		if (population >= ChainOfCommand.POPULATION_WITH_MAYOR) {
			root.add(commanderStaffNode);
			commanderStaffNode.add(mayorNode);
			commanderStaffNode.add(commanderNode);
			commanderStaffNode.add(subCommanderNode);
		}
		
		else if (population >= ChainOfCommand.POPULATION_WITH_SUB_COMMANDER) {
			root.add(commanderStaffNode);
			commanderStaffNode.add(commanderNode);
			commanderStaffNode.add(subCommanderNode);
		}
		
		else if (population >= ChainOfCommand.POPULATION_WITH_COMMANDER) {
			root.add(commanderStaffNode);
			commanderStaffNode.add(commanderNode);
		}

		root.add(divisionNode);
	}
	
	
	public void considerRoles() {
		
		Collection<Person> people = settlement.getAllAssociatedPeople(); // .getInhabitants();

		for (Person p : people) {
			PersonListener personListener = new PersonListener();
			p.addUnitListener(personListener);
			
			listeners.put(p, personListener);
			
			roles.clear();
			roles.put(p, p.getRole().getType());
			
			if (p.getRole().getType() == RoleType.COMMANDER) {
				commanderNode.add(new DefaultMutableTreeNode(p));
			} else if (p.getRole().getType() == RoleType.SUB_COMMANDER) {
				subCommanderNode.add(new DefaultMutableTreeNode(p));
			} else if (p.getRole().getType() == RoleType.MAYOR) {
				mayorNode.add(new DefaultMutableTreeNode(p));
				
			} else if (p.getRole().getType() == RoleType.CHIEF_OF_AGRICULTURE) {
				agricultureChiefNode.add(new DefaultMutableTreeNode(p));
			} else if (p.getRole().getType() == RoleType.AGRICULTURE_SPECIALIST) {
				agricultureSpecialistNode.add(new DefaultMutableTreeNode(p));
				
			} else if (p.getRole().getType() == RoleType.CHIEF_OF_ENGINEERING) {
				engineeringChiefNode.add(new DefaultMutableTreeNode(p));
			} else if (p.getRole().getType() == RoleType.ENGINEERING_SPECIALIST) {
				engineeringSpecialistNode.add(new DefaultMutableTreeNode(p));
				
			} else if (p.getRole().getType() == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS) {
				logisticChiefNode.add(new DefaultMutableTreeNode(p));
			} else if (p.getRole().getType() == RoleType.LOGISTIC_SPECIALIST) {
				logisticSpecialistNode.add(new DefaultMutableTreeNode(p));
				
			} else if (p.getRole().getType() == RoleType.CHIEF_OF_MISSION_PLANNING) {
				missionChiefNode.add(new DefaultMutableTreeNode(p));
			} else if (p.getRole().getType() == RoleType.MISSION_SPECIALIST) {
				missionSpecialistNode.add(new DefaultMutableTreeNode(p));
				
			} else if (p.getRole().getType() == RoleType.CHIEF_OF_SAFETY_N_HEALTH) {
				safetyChiefNode.add(new DefaultMutableTreeNode(p));
			} else if (p.getRole().getType() == RoleType.SAFETY_SPECIALIST) {
				safetySpecialistNode.add(new DefaultMutableTreeNode(p));
				
			} else if (p.getRole().getType() == RoleType.CHIEF_OF_SCIENCE) {
				scienceChiefNode.add(new DefaultMutableTreeNode(p));
			} else if (p.getRole().getType() == RoleType.SCIENCE_SPECIALIST) {
				scienceSpecialistNode.add(new DefaultMutableTreeNode(p));
				
			} else if (p.getRole().getType() == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
				supplyChiefNode.add(new DefaultMutableTreeNode(p));
			} else if (p.getRole().getType() == RoleType.RESOURCE_SPECIALIST) {
				supplySpecialistNode.add(new DefaultMutableTreeNode(p));
				
			} else {
				// anyone who does not belong will be placed in the root node
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(p);
				root.add(node);
			}
		}
	}
	
	public void setupMouseOnNodes() {
		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 2) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

						// Check for node to avoid java.lang.ClassCastException:
						// java.lang.String cannot be cast to org.mars_sim.msp.core.person.Person
						if (node.getUserObject() instanceof Person) {
							Person person = (Person) node.getUserObject();
							if (person != null) {
								desktop.openUnitWindow(person, false);
							} 

//							update();
//							tree.revalidate();
//							tree.repaint();
						} 
//						else {
//							update();
//							tree.revalidate();
//							tree.repaint();
//						}
					}
				}
			}
		};

		tree.addMouseListener(ml);

	}

	public void editIcons(JTree tree) {

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private Icon personIcon = UIManager.getIcon("RadioButton.icon"); // OptionPane.errorIcon");
			private Icon roleIcon = UIManager.getIcon("FileChooser.detailsViewIcon");// OptionPane.informationIcon");
			private Icon homeIcon = UIManager.getIcon("FileChooser.homeFolderIcon");

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean isLeaf, int row, boolean focused) {
				Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
				// if (selected)
				if (isLeaf)
					// this node is a person
					setIcon(personIcon);
				else if (row == 0)
					// this is the root node
					setIcon(homeIcon);
				else
					// this node is just a role
					setIcon(roleIcon);
				// TODO: how to detect a brand node that is empty ?
				return c;
			}
		});
	}

	public Person findPerson(String name) {
		// Person person = null;
		Collection<Person> people = settlement.getIndoorPeople();
		// List<Person> peopleList = new ArrayList<Person>(people);
		Person person = (Person) people.stream().filter(p -> p.getName() == name);

		return person;
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		// Check if anyone has a role change.
	}
	
	
	/**
	 * Reload the root
	 */
	public void reloadTree() {
		defaultTreeModel.reload(root); // notify changes to model 
		tree.expandPath(tree.getSelectionPath());
		for (int i = 0; i < tree.getRowCount(); i++)
			tree.expandRow(i);
	}

	/**
	 * Empty the nodes
	 */
	public void emptyNodes() {

		for (int i = 0; i < tree.getRowCount(); i++)
			tree.collapseRow(i);
	
		deleteAllNodes();
	}
	
	/**
	 * Removes the listener for all people
	 */
	public void removeAllListeners() {
		for (Person p : listeners.keySet()) {
			p.removeUnitListener(listeners.get(p));		
		}
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
			UnitEventType eventType = event.getType();
			Object o = event.getSource();
			Object t = event.getTarget();
			if (eventType == UnitEventType.ROLE_EVENT) {
//				System.out.println(eventType);
				if (o instanceof Person && eventType == UnitEventType.ROLE_EVENT) {
					Person p = (Person) o;
					if (p.getAssociatedSettlement() == settlement) {
//						String personName = p.getName();
//						RoleType rt = (RoleType) t;
//						String announcement = personName + " just got the new role of " + rt.getName() + " in " + settlement.getName() + ".";
//						System.out.println(announcement);
						// TOD: should only add/remove the affected person's listener and node
						removeAllListeners();
						emptyNodes();
						buildTreeNodes();
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
		 * Catch unit manager update event.
		 * 
		 * @param event the unit event.
		 */
		public void unitManagerUpdate(UnitManagerEvent event) {
			Unit unit = event.getUnit();
			UnitManagerEventType eventType = event.getEventType();
			if (unit instanceof Person) {
				if (eventType == UnitManagerEventType.ADD_UNIT) {
					// TOD: should only add/remove the affected person's listener and node
					removeAllListeners();
					emptyNodes();
					buildTreeNodes();
					initNodes();
					reloadTree();
				}
				
				else if (eventType == UnitManagerEventType.REMOVE_UNIT) {
					// TOD: should only add/remove the affected person's listener and node
					removeAllListeners();
					emptyNodes();
					buildTreeNodes();
					initNodes();
					reloadTree();
				}
			}
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions
		settlement = null;
		infoPanel = null;
		tree = null;
	}
}