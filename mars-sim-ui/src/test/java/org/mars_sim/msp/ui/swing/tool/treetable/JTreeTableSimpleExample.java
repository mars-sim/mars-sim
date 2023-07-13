/*
 * www.javagl.de - JTreeTable
 *
 * Copyright (c) 2016 Marco Hutter - http://www.javagl.de
 */
package org.mars_sim.msp.ui.swing.tool.treetable;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * A simple example application for the JTreeTable
 */
@SuppressWarnings("javadoc")
public class JTreeTableSimpleExample
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                createAndShowGUI();
            }
        });
    }
    
    private static void createAndShowGUI()
    {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        TreeTableModel treeTableModel = TestTreeTableModel.createModel(); 
        JTreeTable treeTable = new JTreeTable(treeTableModel);
        f.getContentPane().add(new JScrollPane(treeTable));
        
        f.setSize(800,400);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
