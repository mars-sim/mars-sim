/**
 * Mars Simulation Project
 * BuildingInfoDialog.java
 * @version 3.07 2014-12-31
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;

import javax.swing.*;

@SuppressWarnings("serial")
public class BuildingInfoDialog extends JDialog {

    public BuildingInfoDialog(String buildingName, String text) {
        this.setSize(305, 373);
        this.setResizable(false);
        this.setTitle(buildingName);
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(205, 133, 63));//Color.ORANGE);
        panel.setOpaque(false);

        // Creating the text Input
        JTextField tf1 = new JTextField("", 15);
        //tf1.setBounds(5, 5, this.getWidth() - 120, 30);
        tf1.setBounds(6, 5, 210, 30);
        tf1.setColumns(10);
        tf1.setText(buildingName);

        panel.add(tf1);
        // Creating the button
        JButton button1 = new JButton("Search");
        button1.setBounds(220, 7, 70, 25);
        panel.add(button1);
        JTextArea ta1 = new JTextArea(290, 300);
        JScrollPane scr = new JScrollPane(ta1,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scr.setBounds(5, 40, 290, 300);
        scr.setOpaque(false);
        ta1.setBounds(5, 40, 290, 300);
        ta1.setLineWrap(true);
        ta1.setWrapStyleWord(true);
        ta1.setOpaque(false);
        ta1.setText(text);
        ta1.setEditable(false);
        //ta1.setBackground(new Color(205, 133, 63));//Color.ORANGE);
        ta1.setBackground(new Color(139, 90, 0)); // dark brown
        ta1.setForeground( Color.ORANGE); 
        panel.add(scr);
        this.add(panel);
        this.setVisible(true);
    }
}