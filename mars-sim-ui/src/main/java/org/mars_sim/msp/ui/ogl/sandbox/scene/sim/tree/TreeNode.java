package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author stpa
 * 2014-02-24
 */
public class TreeNode
extends TreeElement
implements Comparable<TreeNode> {

	public TreeNode(String id) {
		this.setId(id);
	}

	public TreeNode(int id) {
		this(Integer.toString(id));
	}

	public TreeNode(long id) {
		this(Long.toString(id));
	}

	public void setId(int id) {
		this.setId(Integer.toString(id));
	}

	public void setId(String id) {
		this.setParam(Tree.PARAM_NODE_ID,id);
	}

	public String getId() {
		return this.getParamString(Tree.PARAM_NODE_ID);
	}

	public int getIdInt() {
		return Integer.parseInt(this.getId());
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("[");
		Iterator<Entry<String,Object>> iter = this.param.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String,Object> ero = iter.next();
			s.append(ero.getKey());
			s.append("=");
			s.append(ero.getValue());
			if (iter.hasNext()) {
				s.append(",");
			}
		}
		s.append("]");
		return s.toString();
	}

	public int compareTo(TreeNode other) {
		return Double.compare(this.getIdInt(),other.getIdInt());
	}
}
