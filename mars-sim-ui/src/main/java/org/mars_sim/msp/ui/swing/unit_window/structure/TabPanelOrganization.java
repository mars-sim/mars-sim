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
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

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

	private Settlement settlement;

	private JPanel infoPanel;

	private JTree tree;

	private DefaultMutableTreeNode root;
	
	private DefaultTreeModel defaultTreeModel;
	
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
		AssociatedSettlementListener settlementListener = new AssociatedSettlementListener();
		settlement.addUnitListener(settlementListener);

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

		createTree();
	}

	public void createTree() {

		root = new DefaultMutableTreeNode(settlement.getName());

		DefaultMutableTreeNode commanderStaffNode = new DefaultMutableTreeNode("Command Staff");
		DefaultMutableTreeNode commanderNode = new DefaultMutableTreeNode(RoleType.COMMANDER.toString());
		DefaultMutableTreeNode subCommanderNode = new DefaultMutableTreeNode(RoleType.SUB_COMMANDER.toString());

		DefaultMutableTreeNode divisionNode = new DefaultMutableTreeNode("Division");
		DefaultMutableTreeNode mayorNode = new DefaultMutableTreeNode(RoleType.MAYOR.toString());

		DefaultMutableTreeNode agricultureNode = new DefaultMutableTreeNode("Agriculture");
		DefaultMutableTreeNode agricultureSpecialistNode = new DefaultMutableTreeNode(
				RoleType.AGRICULTURE_SPECIALIST.toString());
		DefaultMutableTreeNode agricultureChiefNode = new DefaultMutableTreeNode(
				RoleType.CHIEF_OF_AGRICULTURE.toString());

		DefaultMutableTreeNode engineeringNode = new DefaultMutableTreeNode("Engineering");
		DefaultMutableTreeNode engineeringSpecialistNode = new DefaultMutableTreeNode(
				RoleType.ENGINEERING_SPECIALIST.toString());
		DefaultMutableTreeNode engineeringChiefNode = new DefaultMutableTreeNode(
				RoleType.CHIEF_OF_ENGINEERING.toString());

		DefaultMutableTreeNode logisticNode = new DefaultMutableTreeNode("Logistic");
		DefaultMutableTreeNode logisticSpecialistNode = new DefaultMutableTreeNode(
				RoleType.LOGISTIC_SPECIALIST.toString());
		DefaultMutableTreeNode logisticChiefNode = new DefaultMutableTreeNode(
				RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS.toString());

		DefaultMutableTreeNode missionNode = new DefaultMutableTreeNode("Mission");
		DefaultMutableTreeNode missionSpecialistNode = new DefaultMutableTreeNode(
				RoleType.MISSION_SPECIALIST.toString());
		DefaultMutableTreeNode missionChiefNode = new DefaultMutableTreeNode(
				RoleType.CHIEF_OF_MISSION_PLANNING.toString());

		DefaultMutableTreeNode safetyNode = new DefaultMutableTreeNode("Safety");
		DefaultMutableTreeNode safetySpecialistNode = new DefaultMutableTreeNode(RoleType.SAFETY_SPECIALIST.toString());
		DefaultMutableTreeNode safetyChiefNode = new DefaultMutableTreeNode(
				RoleType.CHIEF_OF_SAFETY_N_HEALTH.toString());

		DefaultMutableTreeNode scienceNode = new DefaultMutableTreeNode("Science");
		DefaultMutableTreeNode scienceSpecialistNode = new DefaultMutableTreeNode(
				RoleType.SCIENCE_SPECIALIST.toString());
		DefaultMutableTreeNode scienceChiefNode = new DefaultMutableTreeNode(RoleType.CHIEF_OF_SCIENCE);

		DefaultMutableTreeNode supplyNode = new DefaultMutableTreeNode("Supply");
		DefaultMutableTreeNode supplySpecialistNode = new DefaultMutableTreeNode(
				RoleType.RESOURCE_SPECIALIST.toString());
		DefaultMutableTreeNode supplyChiefNode = new DefaultMutableTreeNode(RoleType.CHIEF_OF_SUPPLY_N_RESOURCES);

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
			root.add(mayorNode);
			root.add(divisionNode);

		} else if (population >= ChainOfCommand.POPULATION_WITH_SUB_COMMANDER) {
			root.add(commanderStaffNode);
			commanderStaffNode.add(commanderNode);
			commanderStaffNode.add(subCommanderNode);
			root.add(divisionNode);
			
		} else {
			root.add(commanderNode);
			root.add(divisionNode);
		}


		tree = new JTree(root);
		tree.setVisibleRowCount(8);

		// Use treeSearchable
		TreeSearchable searchable = SearchableUtils.installSearchable(tree);
		searchable.setPopupTimeout(5000);
		searchable.setCaseSensitive(false);

		// String currentTheme = UIManager.getLookAndFeel().getClass().getName();
		// System.out.println("CurrentTheme is " + currentTheme);

//		if (desktop.getMainWindow() != null) {
//			if (!desktop.getMainWindow().getLookAndFeelTheme().equals("nimrod")) {
//				editIcons(tree);
//			}
//		} else {
//			(desktop.getMainScene() != null) {
//				if (!desktop.getMainScene().getLookAndFeelTheme().equals("nimrod")) {
//					editIcons(tree);
//				}
//		}

		// if (!currentTheme.equals("com.nilo.plaf.nimrod.NimRODLookAndFeel"));
		// //javax.swing.plaf.nimbus.NimbusLookAndFeel") )
		// editIcons(tree);

		Collection<Person> people = settlement.getAllAssociatedPeople(); // .getInhabitants();

		for (Person p : people) {
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

		defaultTreeModel = new DefaultTreeModel(root);
		tree.setModel(defaultTreeModel);

		for (int i = 0; i < tree.getRowCount(); i++)
			tree.expandRow(i);

		centerContentPanel.add(new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

		setupMouse();
	}
	
	public void setupMouse() {
		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 2) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
						Person person = null;
						Settlement settlement = null;
						// Check for node to avoid java.lang.ClassCastException:
						// java.lang.String cannot be cast to org.mars_sim.msp.core.person.Person
						if (node.getUserObject() instanceof Person) {
							person = (Person) node.getUserObject();
							if (person != null) {
								desktop.openUnitWindow(person, false);
							} else if (node.getUserObject() instanceof Settlement)
								settlement = (Settlement) node.getUserObject();
							update();
							tree.revalidate();
							tree.repaint();
						} else {
							update();
							tree.revalidate();
							tree.repaint();
						}
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
		// tree = null;
		// TODO: create a button to refresh instead of recreating the tree ?
		// createTree();
//		model = (DefaultTreeModel) tree.getModel();
		defaultTreeModel.reload(root); // notify changes to model 
		tree.expandPath(tree.getSelectionPath());
	}

	/**
	 * UnitListener inner class for settlements for associated people list.
	 */
	private class AssociatedSettlementListener implements UnitListener {

		/**
		 * Catch unit update event.
		 * 
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType eventType = event.getType();
			if (eventType == UnitEventType.ADD_ASSOCIATED_PERSON_EVENT
					|| eventType == UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT) {
				tree = null;
				// TODO: create a button to refresh instead of recreating the tree ?
				createTree();
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
				if (eventType == UnitManagerEventType.ADD_UNIT || eventType == UnitManagerEventType.REMOVE_UNIT) {
					tree = null;
					// TODO: create a button to refresh instead of recreating the tree ?
					createTree();
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