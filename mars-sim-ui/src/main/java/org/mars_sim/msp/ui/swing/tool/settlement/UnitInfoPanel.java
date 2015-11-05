/**
 * Mars Simulation Project
 * UnitInfoPanel.java
 * @version 3.07 2015-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.CustomScroll;


@SuppressWarnings("serial")
public class UnitInfoPanel extends JPanel {
// JDialog {

	private JEditorPane pane;

    public UnitInfoPanel(MainDesktopPane desktop) {
		super();
    	setOpaque(false);
    	//setBackground(new Color(51,25,0,128));
    	setBackground(new Color(0,0,0,128));
    }

    @Override
	protected void paintComponent(Graphics g) {

        int x = 20;
        int y = 27;
        int w = getWidth() - 40;
        int h = getHeight() - 40;
        int arc = 15;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(51,25,0,128));
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(3f));
        //g2.setColor(Color.lightGray);
        g2.drawRoundRect(x, y, w, h, arc, arc);
        g2.dispose();
    }

    public void init(String unitName, String unitType, String unitDescription) {

    	//JPanel panel = new JPanel(new BorderLayout(20, 10));
    	//LayoutManager mgr = new LayoutManager();
    	//this.setContentPane(panel);
    	//JLayeredPane lp = getLayeredPane();
    	this.setOpaque(false);
        this.setBackground(new Color(0,0,0,128));
        this.setLayout(new BorderLayout(20, 10));
    	//this.setSize(350, 400); // undecorated 301, 348 ; decorated : 303, 373

        JPanel mainPanel = new JPanel(new FlowLayout());//new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBackground(new Color(0,0,0,128));
        //setMinimumSize()
        this.add(mainPanel, BorderLayout.NORTH);

        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));//new BorderLayout());
        westPanel.setOpaque(false);
        westPanel.setBackground(new Color(0,0,0,128));
        //setMinimumSize()
        this.add(westPanel, BorderLayout.WEST);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));//new BorderLayout());
        eastPanel.setOpaque(false);
        eastPanel.setBackground(new Color(0,0,0,128));
        //setMinimumSize()
        this.add(eastPanel, BorderLayout.EAST);

        // Creating the text Input
        JTextField tf1 = new JTextField("", 15);

        tf1.setHorizontalAlignment(JTextField.CENTER);
        tf1.setOpaque(false);
        tf1.setFocusable(false);
        tf1.setBackground(new Color(0,0,0,180));
        tf1.setColumns(20);
        Border border = BorderFactory.createLineBorder(Color.gray, 2);
        tf1.setBorder(border);
        tf1.setText(unitName);
        tf1.setForeground(Color.YELLOW); // orange font
        tf1.setFont( new Font("Arial", Font.BOLD, 14 ) );

        mainPanel.add(tf1);

        //JTextArea ta = new JTextArea();//290, 300);
        pane = new JEditorPane("text/html", "");  
/*        DefaultStyledDocument document = new DefaultStyledDocument();
        StyleContext context = new StyleContext();
        // build a style
        Style style = context.addStyle("test", null);
        // set some style properties
        StyleConstants.setForeground(style, Color.ORANGE);
        // add some data to the document
        try {
			document.insertString(0, "", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
*/
        pane.setEditable(false);
        //String type = "<html><b>TYPE: </b></html>";
        //String description = "<html><b>DESCRIPTION: </b></html>";
        String type = "<b>TYPE: </b>";
        String description = "<b>DESCRIPTION: </b>";
        //Font font = pane.getFont();  
        //pane.setFont(font.deriveFont(Font.ITALIC));

        //ta.setLineWrap(true);
        pane.setFocusable(false);
        //ta.setWrapStyleWord(true);
        pane.setText("<html><font color='orange'>" + type + "<br>" 
        		+ unitType + "<br><br>"
        		+ description + "<br>"
        		+ unitDescription + "</font></html>");
        
        //append(unitType + "\n\n");
        //append(description + "\n");
        //append(unitDescription);
        pane.setForeground(Color.ORANGE); // orange font
        pane.setFont( new Font( "Dialog", Font.PLAIN, 14 ) );
        pane.setOpaque(false);
        pane.setBackground(new Color(0, 0, 0, 128)); //0

        CustomScroll scr = new CustomScroll(pane);
        this.add(scr, BorderLayout.CENTER);

        this.setVisible(true);

    }

    public void append(String s) {
    	   try {
    	      Document doc = pane.getDocument();
    	      doc.insertString(doc.getLength(), s, null);
    	   } catch(BadLocationException exc) {
    	      exc.printStackTrace();
    	   }
	}

}