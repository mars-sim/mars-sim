/**
 * Mars Simulation Project
 * AboutDialog.java
 * @version 2.74 2002-01-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

/** The AboutDialog is an information window that is called from the
 *  "About The Mars Simulation Project" item in the MainWindowMenu.
 *  It provides information about the project, credit to contributors and the GPL license.
 */
public class AboutDialog extends JDialog implements ActionListener,
        ComponentListener {

    // Data members
    private JButton closeButton; // The close button
    private JViewport viewPort; // The view port for the text pane

    /** Constructs an AboutDialog object 
     *  @param mainWindow the main window
     */
    public AboutDialog(MainWindow mainWindow) {

        // Use JDialog constructor
        super(mainWindow, "About The Mars Simulation Project", true);

        // Create the main panel
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);

        // Create the text panel
        // I'd like to replace this with an HTML parser text pane sometime <Scott>
        JTextPane textPane = new JTextPane();
        DefaultStyledDocument document = new DefaultStyledDocument();
        textPane.setStyledDocument(document);
        textPane.setBackground(Color.lightGray);
        textPane.setBorder(new EmptyBorder(2, 2, 2, 2));
        textPane.setEditable(false);

        // Create the document contents string
        StringBuffer buf = new StringBuffer();
        buf.append("The Mars Simulation Project v2.74\n\n");
        buf.append("Web Site: http://mars-sim.sourceforge.net\n\n");

        buf.append("Developers:\n");
        buf.append("  Scott Davis - Java programming, graphics\n");
        buf.append("  James Barnard - 3D graphics\n");
        buf.append("  Greg Whelan - Java programming, zoom map\n");
        buf.append("  Cameron Riley - Ant script programming\n");
        buf.append("  Mike Jones - Orbital equations\n");
        buf.append("  Brian Donovan - Java programming\n");
        buf.append("  Jarred McCaffrey - Java programming\n");
        buf.append("  Dalen Kruse - Java Programming, debugging\n");
        buf.append("  Daniel L. Thomas - Java Programming\n");
        buf.append("  Barry Evans - Java Programming\n\n");

        buf.append("Testing and Recommendations:\n");
        buf.append("  Paul Speed\n");
        buf.append("  Allen Bryan\n");
        buf.append("  Brian K. Smith\n");
        buf.append("  Kent Primrose\n");
        buf.append("  Dan Sepanski\n");
        buf.append("  Joe Wagner\n");
        buf.append("  Tom Zanoni\n\n");

        buf.append("Martian clock/calendar based on calendars by\n");
        buf.append("Shaun Moss: Areosynchronous Calendar\n(http://www.virtualmars.net/Calendar.asp)\n");
        buf.append("Tom Gangale: Darian Calendar\n(http://members.xoom.com/mars_ultor/mars/mst/darifrm.htm)\n");
        buf.append("Frans Blok: The Rotterdam System\n(http://www.geocities.com/fra_nl/rotmonth.html)\n");
        buf.append("Bruce Mackenzie: Metric Time for Mars\n(http://members.nbci.com/_XMCM/mars_ultor/mars/other/mcknzfrm.htm)\n\n");

        buf.append("Map images and data courtesy of NASA JPL ");
        buf.append("(www.jpl.nasa.gov) and Malin Space Science Systems ");
        buf.append("(www.msss.com).\n\n");

        buf.append("This program is free software; you can redistribute it ");
        buf.append("and/or modify it under the terms of the GNU General ");
        buf.append("Public License as published by the Free Software ");
        buf.append("Foundation; either version 2 of the License, or (at your ");
        buf.append("option) any later version.\n\n");

        buf.append("This program is distributed in the hope that it will be ");
        buf.append("useful, but WITHOUT ANY WARRANTY; without even the implied ");
        buf.append("warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR ");
        buf.append("PURPOSE.  See the GNU General Public License for more details.");

        // Create the document
        try {
            document.insertString(0, buf.toString(), null);
        } catch (BadLocationException e) {
            System.out.println(e.toString());
        }

        JScrollPane scrollPane = new JScrollPane(textPane);
        viewPort = scrollPane.getViewport();
        viewPort.addComponentListener(this);
        viewPort.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        mainPane.add(scrollPane);

        // Create close button panel
        JPanel closeButtonPane = new JPanel(new FlowLayout());
        closeButtonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPane.add(closeButtonPane, "South");

        // Create close button
        closeButton = new JButton("Close");
        closeButton.addActionListener(this);
        closeButtonPane.add(closeButton);

        // Set the size of the window
        setSize(400, 400);

        // Center the window on the parent window.
        Point parentLocation = mainWindow.getLocation();
        int Xloc = (int) parentLocation.getX() + ((mainWindow.getWidth() - 350) / 2);
        int Yloc = (int) parentLocation.getY() + ((mainWindow.getHeight() - 400) / 2);
        setLocation(Xloc, Yloc);

        // Prevent the window from being resized by the user.
        setResizable(false);

        // Show the window
        setVisible(true);
    }

    // Implementing ActionListener method
    public void actionPerformed(ActionEvent event) {
        dispose();
    }

    // Implement ComponentListener interface.
    // Make sure the text is scrolled to the top.
    // Need to find a better way to do this <Scott>
    public void componentResized(ComponentEvent e) {
        viewPort.setViewPosition(new Point(0, 0));
    }
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
}
