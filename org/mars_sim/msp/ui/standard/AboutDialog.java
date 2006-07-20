/**
 * Mars Simulation Project
 * AboutDialog.java
 * @version 2.78 2005-08-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

/** The AboutDialog is an information window that is called from the
 *  "About The Mars Simulation Project" item in the MainWindowMenu.
 *  It provides information about the project, credit to contributors and the GPL license.
 */
public class AboutDialog extends JDialog implements ActionListener, ComponentListener {

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
        buf.append("The Mars Simulation Project v2.79\n\n");
        buf.append("Web Site: http://mars-sim.sourceforge.net\n\n");

        buf.append("Developers:\n");
        buf.append("  Scott Davis - Java programming, graphics\n");
		buf.append("  Greg Whelan - Java programming, zoom map\n");
		buf.append("  Barry Evans - Java Programming\n");
        buf.append("  James Barnard - 3D graphics\n");
		buf.append("  Jani Patokallio - Java Programming\n");
		buf.append("  Dalen Kruse - Java Programming, debugging\n");
		buf.append("  Brian Donovan - Java programming\n");
		buf.append("  Jarred McCaffrey - Java programming\n");
        buf.append("  Cameron Riley - Ant script programming\n");
        buf.append("  Mike Jones - Orbital equations\n");
        buf.append("  Daniel L. Thomas - Java Programming\n");
		buf.append("  Hartmut Prochaska - Java Programming\n");
		buf.append("  Mihaly Gyulai - Java Programming\n");
		buf.append("  Kyur Thadeshwar - Java Programming\n");
		buf.append("  Kent Primrose - Java Programming, JUnit tests\n");
		buf.append("  Dima Stephanchuk - Java Programmer, sound\n");
		buf.append("  Paula Jenkins - Voice Actress\n\n");

        buf.append("Testing and Recommendations:\n");
        buf.append("  Trey Monty\n");
        buf.append("  Rik Declercq\n");
        buf.append("  Claude David\n");
        buf.append("  Paul Speed\n");
        buf.append("  Allen Bryan\n");
        buf.append("  Brian K. Smith\n");
        buf.append("  Dan Sepanski\n");
        buf.append("  Joe Wagner\n");
        buf.append("  Tom Zanoni\n\n");

        buf.append("8xZoom map results courtesy of the USGS PDS Planetary Atlas:\n");
		buf.append("http://pdsmaps.wr.usgs.gov/maps.html\n\n");
	
		buf.append("The following open source libraries are used in this project:\n");
		buf.append("JFreeChart: http://www.jfree.org/jfreechart/\n");
		buf.append("JUnit: http://www.junit.org\n");
		buf.append("Plexus: http://plexus.sourceforge.net\n");
		buf.append("Commons Collections: http://jakarta.apache.org/commons/collections/\n");
		buf.append("Log4J: http://logging.apache.org/log4j/\n\n");
	
        buf.append("Martian clock/calendar based on calendars by\n");
        buf.append("Shaun Moss: Areosynchronous Calendar\n(http://www.virtualmars.net/Calendar.asp)\n");
        buf.append("Tom Gangale: Darian Calendar\n(http://www.martiana.org/mars/mst/calendar_clock.htm)\n");
        buf.append("Frans Blok: The Rotterdam System\n(http://www.geocities.com/fra_nl/rotmonth.html)\n");
        buf.append("Bruce Mackenzie: Metric Time for Mars\n(http://members.nbci.com/_XMCM/mars_ultor/mars/other/mcknzfrm.htm)\n\n");

        buf.append("Sounds in the Mars Simulation Project are licensed under the Creative Commons Sampling Plus 1.0 License.\n");
        buf.append("http://creativecommons.org/licenses/sampling+/1.0/\n\n");
        
        buf.append("Vehicle sounds created from the following base sounds:\n\n");
        
        buf.append("http://freesound.iua.upf.edu/samplesViewSingle.php?id=6086\n");
        buf.append("http://freesound.iua.upf.edu/samplesViewSingle.php?id=2885\n");
        buf.append("Vance Dylan - Sonic Valley Productions: http://www.sonicvalley.com\n\n");
        
        buf.append("http://freesound.iua.upf.edu/samplesViewSingle.php?id=515\n");
        buf.append("http://freesound.iua.upf.edu/samplesViewSingle.php?id=517\n");
        buf.append("http://freesound.iua.upf.edu/samplesViewSingle.php?id=518\n");
        buf.append("Jen Carlile: http://www.atonaltrek.com\n\n");
        
        buf.append("Female settler voice by Paula Jenkins\n");
        buf.append("Male settler voice by Scott Davis\n\n");
        
        buf.append("The FreeSound Project: http://freesound.iua.upf.edu\n\n");
        
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
            System.err.println(e.toString());
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