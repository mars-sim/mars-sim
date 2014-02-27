package org.mars_sim.msp.ui.ogl.sandbox.scene.sim.tree;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stpa
 * 2014-02-24
 */
public class TreeElement {

	protected Map<String,Object> param = new HashMap<String,Object>();	

	public void setParam(String parameter, Object valoro) {
		this.param.put(parameter,valoro);
	}
	
	public Object getParam(String parameter) {
		return this.param.get(parameter);
	}

	public String getParamString(String parameter) {
		return (String) this.param.get(parameter);
	}

	public Double getParamDouble(String parameter) {
		return (Double) this.param.get(parameter);
	}

	public Integer getParamInt(String parameter) {
		return (Integer) this.param.get(parameter);
	}

	public TreeNode getParamTreeNode(String parameter) {
		return (TreeNode) this.param.get(parameter);
	}

	public int compareStringParam(String parameter, TreeElement other) {
		String a = this.getParamString(parameter);
		String b = other.getParamString(parameter);
		if (a != null && b != null) {
			return a.compareTo(b);
		} else {
			return a == b ? 0 : -1;
		}
	}
	
	public void removeParam(String parameter) {
		this.param.remove(parameter);
	}
}
