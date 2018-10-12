package org.mars_sim.msp.ui.sodium.swidgets.app;
import javax.swing.*;

import org.mars_sim.msp.ui.sodium.swidgets.SButton;
import org.mars_sim.msp.ui.sodium.swidgets.STextField;

import java.awt.FlowLayout;
//import swidgets.*;
import nz.sodium.*;

public class clearfield {
    public static void main(String[] args) {
        JFrame frame = new JFrame("clearfield");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        SButton clear = new SButton("Clear");
        Stream<String> sClearIt = clear.sClicked.map(u -> "");
        STextField text = new STextField(sClearIt, "Hello");
        frame.add(text);
        frame.add(clear);
        frame.setSize(400, 160);
        frame.setVisible(true);
    }
}

