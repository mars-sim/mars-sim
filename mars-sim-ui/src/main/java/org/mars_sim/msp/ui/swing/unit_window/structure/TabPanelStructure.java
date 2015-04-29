/**
 * Mars Simulation Project
 * TabPanelStructure.java
 * @version 3.08 2015-04-28
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.apache.commons.lang3.text.WordUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelStructure is a tab panel showing the organizational structure of a settlement.
 */
public class TabPanelStructure
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private Settlement settlement;

	private JPanel infoPanel;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelStructure(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelStructure.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelStructure.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

	    settlement = (Settlement) unit;

		// Create label panel.
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePanel);

		// Prepare label
		JLabel tlabel = new JLabel(Msg.getString("TabPanelStructure.title"), JLabel.CENTER); //$NON-NLS-1$
		tlabel.setFont(new Font("Serif", Font.BOLD, 16));
		tlabel.setForeground(new Color(102, 51, 0)); // dark brown
		titlePanel.add(tlabel);

		// Prepare info panel.
		infoPanel = new JPanel(new GridLayout(1, 2, 0, 0));
		infoPanel.setBorder(new MarsPanelBorder());
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

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(settlement.getName());
		DefaultMutableTreeNode commanderNode = new DefaultMutableTreeNode("Commander");
		DefaultMutableTreeNode subCommanderNode = new DefaultMutableTreeNode("Sub-commander");
		root.add(commanderNode);
		root.add(subCommanderNode);

		JTree tree = new JTree(root);

	   	Collection<Person> people = settlement.getInhabitants();
    	Person commander = null;
    	Person subCommander = null;
    	DefaultMutableTreeNode cc = null;
    	DefaultMutableTreeNode cv = null;

    	for (Person p : people) {
    		if (p.getRole().getType() == RoleType.COMMANDER) {
    			commander = p;
    			cc = new DefaultMutableTreeNode(commander.getName());
    	    	commanderNode.add(cc);
    		}
    		else if (p.getRole().getType() == RoleType.SUB_COMMANDER) {
    			subCommander = p;
    	    	cv = new DefaultMutableTreeNode(subCommander.getName());
    	    	subCommanderNode.add(cv);
    		}
    		else {
    			DefaultMutableTreeNode node = new DefaultMutableTreeNode(p.getName());
    			root.add(node);
    		}
    	}

    	DefaultTreeModel mod = new DefaultTreeModel(root);
    	tree.setModel(mod);

    	for (int i = 0; i < tree.getRowCount(); i++)
    		tree.expandRow(i);

    	centerContentPanel.add(new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
    			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {


	}
}