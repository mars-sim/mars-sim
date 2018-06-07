package nz.sodium.swidgets.examples;

import javax.swing.*;
import java.awt.FlowLayout;
//import swidgets.*;
import nz.sodium.*;
import nz.sodium.swidgets.SLabel;
import nz.sodium.swidgets.STextField;

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

