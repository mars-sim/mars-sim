/**
 * Mars Simulation Project
 * TabPanelStructure.java
 * @version 3.08 2015-04-28
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

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
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

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
        tree.setVisibleRowCount(10);

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            private Icon personIcon = UIManager.getIcon("RadioButton.icon"); //OptionPane.errorIcon");
            private Icon roleIcon = UIManager.getIcon("FileChooser.detailsViewIcon");//OptionPane.informationIcon");
            private Icon homeIcon = UIManager.getIcon("FileChooser.homeFolderIcon");
            @Override
            public Component getTreeCellRendererComponent(JTree tree,
                    Object value, boolean selected, boolean expanded,
                    boolean isLeaf, int row, boolean focused) {
                Component c = super.getTreeCellRendererComponent(tree, value,
                        selected, expanded, isLeaf, row, focused);
                //if (selected)
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


	   	Collection<Person> people = settlement.getInhabitants();

    	Person commander = null;
    	Person subCommander = null;
    	DefaultMutableTreeNode cc = null;
    	DefaultMutableTreeNode cv = null;

    	for (Person p : people) {
    		if (p.getRole().getType() == RoleType.COMMANDER) {
    			commander = p;
    			cc = new DefaultMutableTreeNode(commander);
    	    	commanderNode.add(cc);
    		}
    		else if (p.getRole().getType() == RoleType.SUB_COMMANDER) {
    			subCommander = p;
    	    	cv = new DefaultMutableTreeNode(subCommander);
    	    	subCommanderNode.add(cv);
    		}
    		else {
    			DefaultMutableTreeNode node = new DefaultMutableTreeNode(p);
    			root.add(node);
    		}
    	}

    	DefaultTreeModel mod = new DefaultTreeModel(root);
    	tree.setModel(mod);

    	for (int i = 0; i < tree.getRowCount(); i++)
    		tree.expandRow(i);

    	centerContentPanel.add(new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
    			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

    	MouseListener ml = new MouseAdapter() {
    	    public void mousePressed(MouseEvent e) {
    	        int selRow = tree.getRowForLocation(e.getX(), e.getY());
    	        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
    	        if(selRow != -1) {
    	            if(e.getClickCount() == 2) {
    	    			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
    	    			Person person =  (Person) node.getUserObject();
    	    			if (person != null) {
    	    				desktop.openUnitWindow(person, false);
    	            	}
    	            }
    	        }
    	    }
    	};

    	tree.addMouseListener(ml);

	}

	public Person findPerson(String name) {
		//Person person = null;
		Collection<Person> people = settlement.getInhabitants();
		//List<Person> peopleList = new ArrayList<Person>(people);
		Person person = (Person) people.stream()
                .filter(p -> p.getName() == name);

		return person;
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {


	}
}