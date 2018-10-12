package org.mars_sim.msp.ui.sodium.swidgets.app;

import javax.swing.*;

import org.mars_sim.msp.core.sodium.*;

import java.awt.*;

public class spinme {
    public static void main(String[] args) {
        JFrame view = new JFrame("spinme");
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        view.setLayout(new FlowLayout());
        Transaction.runVoid(() -> {
            SSpinner spnr = new SSpinner(0);
            view.add(spnr);
        });
        view.setSize(400, 160);
        view.setVisible(true);
    }
}

