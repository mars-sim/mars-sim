/**
 * Mars Simulation Project
 * TestSlidingPanel.java
 * @version 3.1.0 2016-11-24
 * @author Manny Kung
 */

// Adapted from http://www.codeproject.com/Articles/565425/Sliding-Panel-in-Java
// Original author : Shubhashish_Mandal, 22 Mar 2013

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mars_sim.msp.ui.swing.sidepanel;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author root
 */
    public class TestSlidingPanel extends JPanel {

    public TestSlidingPanel() {
        SlidePaneFactory factory = SlidePaneFactory.getInstance();
        BookForm bookForm = new BookForm();
        factory.add(bookForm,"Slide1",new ImageIcon("title.png").getImage(), true);

        bookForm = new BookForm();
        factory.add(bookForm,"Slide2",new ImageIcon("title.png").getImage());

        bookForm = new BookForm();
        factory.add(bookForm,"Slide3",new ImageIcon("title.png").getImage(), false);

        add(factory);

    }

    public static void main(String s[]) {
        JFrame frame = new JFrame();
        frame.setSize(330, 400);

        //frame.setLayout(new FlowLayout());
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(new TestSlidingPanel());
        frame.add(pane);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        //frame.setUndecorated(true);
        frame.setVisible(true);
    }
}
