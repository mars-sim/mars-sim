package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author stpa
 * 2014-02-24
 */
public class TreeEdge
extends TreeElement {

	public TreeEdge() {
		this(null,null);
	}
	
	public TreeEdge(TreeNode n1, TreeNode n2) {
		this.setSource(n1);
		this.setTarget(n2);
	}
	
	public TreeNode getSource() {
		return this.getParamTreeNode(Tree.PARAM_EDGE_SOURCE);
	}
	
	public TreeNode getTarget() {
		return this.getParamTreeNode(Tree.PARAM_EDGE_TARGET);
	}

	public void setSource(TreeNode source) {
		this.setParam(Tree.PARAM_EDGE_SOURCE,source);
	}

	public void setTarget(TreeNode target) {
		this.setParam(Tree.PARAM_EDGE_TARGET,target);
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("[");
		Iterator<Entry<String,Object>> iter = this.param.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String,Object> item = iter.next();
			s.append(item.getKey());
			s.append("=");
			s.append(item.getValue());
			if (iter.hasNext()) {
				s.append(",");
			}
		}
		s.append("]");
		return s.toString();
	}

	public boolean equals(TreeEdge other) {
		return this.param.equals(other.param);
	}
}
