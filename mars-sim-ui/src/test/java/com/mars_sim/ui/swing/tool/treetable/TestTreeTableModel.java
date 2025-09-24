/*
 * www.javagl.de - JTreeTable
 *
 * Copyright (c) 2016 Marco Hutter - http://www.javagl.de
 */
package com.mars_sim.ui.swing.tool.treetable;

/**
 * Methods to create simple {@link TreeTableModel} instances
 */
public class TestTreeTableModel {
	
    // This test assumes certain characteristics of the Alpha Base 1 template
    public static final String ALPHA_BASE = "Alpha Base 1";

	private static final String COMMAND_STAFF = "Command Staff";
	
	private static final String CHIEF = "Chief";
	
	private static final String SPECIALIST = "Specialist";
	
	private static final String DIVISION = "Division";
	
	private static final String MANAGEMENT = "Management";
	
	private static final String JOB = "Job";
	
	private static final String ROLE = "Role";
	
//	private static final String SOLS = "Sols";
	
    /**
     * Create a simple dummy {@link TreeTableModel}
     * 
     * @return The {@link TreeTableModel}
     */
    public static TreeTableModel createModel() {
    	String name = ALPHA_BASE;
    	Object root = name;
        TreeTableModel treeTableModel = new AbstractTreeTableModel(root) {
        	
            @Override
            public int getChildCount(Object node) {
                if (node.toString().startsWith(name)) {
                    return 1;
                }
                if (node.toString().startsWith(COMMAND_STAFF)) {
                    return 8;
                }
                if (node.toString().startsWith(DIVISION)) {
                    return 2;
                }      
                if (node.toString().startsWith(ROLE)) {
                    return 1; // it varies
                }
                return 0;
            }

            @Override
            public Object getChild(Object node, int childIndex) {
                if (node.toString().startsWith(name)) {
                    return COMMAND_STAFF + childIndex;
                }
                if (node.toString().startsWith(COMMAND_STAFF)) {
                    return DIVISION + childIndex;
                }
                if (node.toString().startsWith(DIVISION)) {
                    return ROLE + childIndex;
                }
                if (node.toString().startsWith(ROLE)) {
                    return SPECIALIST + childIndex;
                }
                return null;
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getValueAt(Object node, int column) {
            	if (column == 0) {
            		return node + ", Base Structure " + column;
            	}
            	else if (column == 1) {
            		return node + ", Job " + column;
            	}
            	else if (column == 2) {
            		return node + ", Role " + column;
            	}
            	return node + ", Unknown " + column;
            }

            @Override
            public String getColumnName(int column) {
            	if (column == 0) {
            		return MANAGEMENT;
            	}
            	else if (column == 1) {
            		return JOB;
            	}
            	else if (column == 2) {
            		return ROLE;
            	}

                return "Unknown" + column;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0)
                {
                    return TreeTableModel.class;                    
                }
                return Object.class;
            }
        };
        return treeTableModel;
    }
}


