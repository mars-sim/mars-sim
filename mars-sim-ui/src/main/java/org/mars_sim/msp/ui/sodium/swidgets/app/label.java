package org.mars_sim.msp.ui.sodium.swidgets.app;

import java.awt.FlowLayout;

import javax.swing.JFrame;

import org.mars_sim.msp.ui.sodium.swidgets.SLabel;
import org.mars_sim.msp.ui.sodium.swidgets.STextField;

public class label {
    public static void main(String[] args) {
        JFrame frame = new JFrame("label");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        STextField msg = new STextField("Hello");
        SLabel lbl = new SLabel(msg.text);
        frame.add(msg);
        frame.add(lbl);
        frame.setSize(400, 160);
        frame.setVisible(true);
    }
}

