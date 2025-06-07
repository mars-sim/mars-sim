/*
 * Mars Simulation Project
 * TreeCheckFactory.java
 * @date 2025-06-06
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * This is a factory class that creates a JTree that renders nodes with a Checkbox if the UserObject 
 * is an instance of a SelectableNode.
 */
public class TreeCheckFactory {

    /**
     * This interface is the abstract of the user object that can be rendered by this tree.
     */
    public interface SelectableNode {
        public String getName();
        Icon getIcon();
        boolean isSelected();
        void toggleSelection();
    }

    /**
     * Will render the tree node as a CheckBox if the associated User Object is an instance
     * of SelectableNode.
     */
    private static class CheckBoxCellRenderer extends JPanel implements TreeCellRenderer {    
        private static final long serialVersionUID = -7341833835878991719L;    
        private JCheckBox checkBox;  
        private JLabel label;

        CheckBoxCellRenderer() {
            super();
            setLayout(new BorderLayout());
            checkBox = new JCheckBox();
            label = new JLabel();
            add(checkBox, BorderLayout.CENTER);
            add(label, BorderLayout.EAST);
            checkBox.setOpaque(false);
            label.setOpaque(false);
            setOpaque(false);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            var obj = node.getUserObject();    
            if (obj instanceof SelectableNode rendered) {      
                checkBox.setSelected(rendered.isSelected());
                checkBox.setText(rendered.getName());
               
                label.setIcon(rendered.getIcon());

                return this;
            }
            else {
                return new JLabel(obj.toString());
            }
        }      
    }

    /**
     * CReats a JTree that renderers nodes as CheckBoxes with an optional Icon
     */
    public static JTree createCheckTree(DefaultMutableTreeNode top) {

        //Create a tree that allows one selection at a time.
        var tree = new JTree(top);
        tree.setToggleClickCount(0);
        tree.setCellRenderer(new CheckBoxCellRenderer());

        // Overriding selection model by an empty one
        tree.setSelectionModel(null);

        // Calling checking mechanism on mouse click
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                JTree source = (JTree)arg0.getSource();

                TreePath tp = source.getPathForLocation(arg0.getX(), arg0.getY());
                if (tp == null) {
                    return;
                }
                DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                var clicked = (SelectableNode)tn.getUserObject();
                clicked.toggleSelection();

                // // Repainting tree after the data structures were updated
                source.repaint();                          
            }                
        });
        return tree;
    }
}
