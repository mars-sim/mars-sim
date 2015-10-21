/**
 * Copyright (c) 2011-2013 Bernhard Pauler, Tim Molderez.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 3-Clause BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/BSD-3-Clause
 */

package net.java.balloontip;

import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import net.java.balloontip.positioners.BalloonTipPositioner;
import net.java.balloontip.styles.BalloonTipStyle;

/**
 * A balloon tip that can attach itself to a node in a JTree
 * @author Tim Molderez
 */
public class TreeNodeBalloonTip extends CustomBalloonTip {
	protected TreePath path; // Path to the tree node that our balloon tip is attached to
	
	// Shows/hides the balloon tip when nodes are expanded/collapsed
	private TreeExpansionListener expansionListener = new TreeExpansionListener() {
		public void treeExpanded(TreeExpansionEvent e) {
			if(getTree().isVisible(path)) {
				visibilityControl.setCriterionAndUpdate("treeExpansion", true);
			}
			setTreePath(path);
		}
		
		public void treeCollapsed(TreeExpansionEvent e) {
			if(!getTree().isVisible(path)) {
				visibilityControl.setCriterionAndUpdate("treeExpansion", false);
			} else {
				setTreePath(path);
			}
		}
	};
	
	// Adjusts the balloon tip when the tree is modified
	private TreeModelListener modelListener = new TreeModelListener() {
		public void treeStructureChanged(TreeModelEvent e) {
			setTreePath(path);
		}
		
		public void treeNodesRemoved(TreeModelEvent e) {
			boolean closeBalloon = false;
			for (Object child : e.getChildren()) {
				// Is this.path a descendant of the path being removed?
				if (e.getTreePath().pathByAddingChild(child).isDescendant(path)) {
					closeBalloon = true;
				}
			}
			
			if (closeBalloon) {
				closeBalloon();
			} else {
				setTreePath(path);
			}
		}
		
		public void treeNodesInserted(TreeModelEvent e) {
			setTreePath(path);
		}
		
		public void treeNodesChanged(TreeModelEvent e) {
			setTreePath(path);
		}
	};

	/**
	 * @see net.java.balloontip.BalloonTip#BalloonTip(JComponent, JComponent, BalloonTipStyle, Orientation, AttachLocation, int, int, boolean)
	 * @param tree		the tree to attach the balloon tip to (may not be null)
	 * @param path		path to the tree node the balloon tip should be attached to
	 */
	public TreeNodeBalloonTip(JTree tree, JComponent component, TreePath path, BalloonTipStyle style, Orientation alignment, AttachLocation attachLocation, int horizontalOffset, int verticalOffset, boolean useCloseButton) {
		super(tree, component, tree.getPathBounds(path), style, alignment, attachLocation, horizontalOffset, verticalOffset, useCloseButton);
		setup(path);
	}

	/**
	 * @see net.java.balloontip.BalloonTip#BalloonTip(JComponent, JComponent, BalloonTipStyle, BalloonTipPositioner, JButton)
	 * @param tree		the tree to attach the balloon tip to (may not be null)
	 * @param path		path to the tree node the balloon tip should be attached to
	 */
	public TreeNodeBalloonTip(JTree tree, JComponent component, TreePath path, BalloonTipStyle style, BalloonTipPositioner positioner, JButton closeButton) {
		super(tree, component, tree.getPathBounds(path), style, positioner, closeButton);
		setup(path);
	}

	/**
	 * Set the tree node the balloon tip should attach to
	 * @param path		path identifying the tree node that the balloon tip should attach to
	 */
	public void setTreePath(TreePath path) {
		this.path = path;
		Rectangle bounds = getTree().getPathBounds(path);
		if (bounds!=null) { // Might be null if the path is currently invisible..
			setOffset(bounds);
		}
	}

	public void closeBalloon() {
		JTree tree = getTree();
		tree.removeTreeExpansionListener(expansionListener);
		tree.getModel().removeTreeModelListener(modelListener);
		super.closeBalloon();
	}

	/*
	 * A helper method needed when constructing a TreeNodeBalloonTip instance
	 * @param path		the path describing the node we're attached to
	 */
	private void setup(TreePath path) {
		this.path = path;
		if(!getTree().isVisible(path)) {
			visibilityControl.setCriterionAndUpdate("treePath", false);
		}
		
		JTree tree = getTree();
		tree.addTreeExpansionListener(expansionListener);
		tree.getModel().addTreeModelListener(modelListener);
	}
	
	/*
	 * Retrieve the JTree the balloon tip is attached to
	 * @return			the tree
	 */
	private JTree getTree() {
		return ((JTree)attachedComponent);
	}
	
	private static final long serialVersionUID = -7270789090236631717L;
}
