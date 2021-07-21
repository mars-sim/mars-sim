/**
 * Mars Simulation Project
 * LineBreakSample.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class LineBreakSample {//extends JApplet {
          
//    public void init() { 
//		buildUI(getContentPane());
//    }

    public void buildUI(Container container) {
//        try {
//            String cn = UIManager.getSystemLookAndFeelClassName();
//            UIManager.setLookAndFeel(cn);
//        } catch (Exception cnf) {
//        }
//    	container.setOpaque(false);
    	container.setBackground(new Color(0,0,0,128));

		String type = "Building Type: ";
		String description = "Descripion: ";
//		String text = unitName + "\n\n" 
//					+ type + "\n" + unitType + "\n\n"
//					+ description + "\n" + unitDescription + "\n\n";
		
    	List<String> list = new ArrayList<>();
    	list.add("EVA Airlock 1");
    	list.add(" \n");
    	list.add(type);
    	list.add("EVA Airlock");
    	list.add(" \n");
    	list.add(description);
    	list.add("Many people believe that Vincent van Gogh painted his best works " +
    	        "during the two-year period he spent in Provence. Here is where he " +
    	        "painted The Starry Night--which some consider to be his greatest " +
    	        "work of all. However, as his artistic brilliance reached new " +
    	        "heights in Provence, his physical and mental health plummeted. ");
//    	list.add(" \n");   	
    	
    	LineBreakPanel lineBreakPanel = new LineBreakPanel(list);
//        lineBreakPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		container.add(lineBreakPanel);//, BorderLayout.CENTER);
    }

    public static void main(String[] args) {

        JFrame f = new JFrame("Line Break Sample");
//    	f.setBackground(new Color(0,0,0,128));
    	
        f.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        LineBreakSample lineBreakSample = new LineBreakSample();
        lineBreakSample.buildUI(f.getContentPane());   
        
//       	f.getContentPane().setOpaque(false);
       	f.getContentPane().setBackground(new Color(0, 0, 0, 128)); //Color.white);
        
        f.setSize(new Dimension(400, 250));
        f.setVisible(true);
    }
}

