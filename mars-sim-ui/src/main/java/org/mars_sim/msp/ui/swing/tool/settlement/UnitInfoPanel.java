/**
 * Mars Simulation Project
 * UnitInfoPanel.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.LineBreakPanel;

import com.alee.laf.panel.WebPanel;

@SuppressWarnings("serial")
public class UnitInfoPanel extends WebPanel {

	public static final int MARGIN_WIDTH = 2;
	public static final int MARGIN_HEIGHT = 2;
	
	private MainDesktopPane desktop;
	
	public UnitInfoPanel(MainDesktopPane desktop) {
		super();
		this.desktop = desktop;
		setOpaque(false);
//		setBackground(new Color(51, 25, 0, 128));
		setBackground(new Color(0, 0, 0, 128));
    }
    
//	@Override
//	protected void paintComponent(Graphics g) {
////		super.paintComponent(g);
//        setBackground(new Color(51, 25, 0, 128));
//        
//		int x = 2;
//		int y = 2;
//		int w = getWidth() - MARGIN_WIDTH * 2;
//		int h = getHeight() - MARGIN_HEIGHT * 2;
//		int arc = 15;
//
//		Graphics2D g2 = (Graphics2D) g.create();
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2.setColor(new Color(51, 25, 0, 128));
//		g2.fillRoundRect(x, y, w, h, arc, arc);
//		g2.setStroke(new BasicStroke(3f));
//		g2.setColor(Color.orange);
//		g2.drawRoundRect(x, y, w, h, arc, arc);
//		g2.dispose();
//	}

	public void init(String unitName, String unitType, String unitDescription) {

		setOpaque(false);
//		setBackground(new Color(0, 0, 0, 128));
        setBackground(new Color(51, 25, 0, 128));
        
		setLayout(new BorderLayout(1, 1));
//		setSize(350, 400); // undecorated 301, 348 ; decorated : 303, 373
		setSize(PopUpUnitMenu.WIDTH_1 - 10, PopUpUnitMenu.HEIGHT_1 - 10); 
		setMaximumSize(PopUpUnitMenu.WIDTH_1 - 10, PopUpUnitMenu.HEIGHT_1 - 10); 
		
		String type = "Building Type: ";
		String description = "Descripion: ";
//		String text = unitName + "\n\n" 
//					+ type + "\n" + unitType + "\n\n"
//					+ description + "\n" + unitDescription + "\n\n";
		
    	List<String> list = new ArrayList<>();
    	list.add(unitName);
    	list.add(" \n");
    	list.add(type);
    	list.add(unitType);
    	list.add(" \n");
    	list.add(description);
    	list.add(unitDescription);
    	list.add(" \n");
    	
    	LineBreakPanel lineBreakPanel = new LineBreakPanel(list);
//        lineBreakPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lineBreakPanel, BorderLayout.CENTER);
		
//		WebPanel mainPanel = new WebPanel(new BorderLayout());
//		mainPanel.setOpaque(false);
//		mainPanel.setBackground(new Color(0, 0, 0, 128));
//		// setMinimumSize()
//		add(mainPanel, BorderLayout.NORTH);

//		WebPanel westPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));// new BorderLayout());
//		westPanel.setOpaque(false);
//		westPanel.setBackground(new Color(0, 0, 0, 128));
//		// setMinimumSize()
//		add(westPanel, BorderLayout.WEST);
//
//		WebPanel eastPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));// new BorderLayout());
//		eastPanel.setOpaque(false);
//		eastPanel.setBackground(new Color(0, 0, 0, 128));
//		// setMinimumSize()
//		add(eastPanel, BorderLayout.EAST);

		// Creating the text Input
//		WebTextField tf1 = new WebTextField(StyleId.textareaTransparent, "", 15);
//
//		tf1.setHorizontalAlignment(WebTextField.CENTER);
//		tf1.setOpaque(false);
//		tf1.setFocusable(false);
////		tf1.setBackground(new Color(92, 83, 55, 128));
//		tf1.setBackground(new Color(0, 0, 0, 128));
//		tf1.setColumns(20);
//		Border border = BorderFactory.createLineBorder(Color.gray, 2);
//		tf1.setBorder(border);
//		tf1.setText(unitName);
////		tf1.setForeground(Color.yellow.brighter());
//		tf1.setFont(new Font("Arial", Font.BOLD, 14));
//
//		add(tf1, BorderLayout.NORTH);
//
//		WebTextArea ta = new WebTextArea(StyleId.textareaTransparent);
		

//		ta.setLineWrap(true);
//		ta.setFocusable(false);
//		ta.setWrapStyleWord(true);
//		ta.setText(type + "\n");
//		ta.append(unitType + "\n\n");
//		ta.append(description + "\n");
//		ta.append(unitDescription);
//		ta.setCaretPosition(0);
//		ta.setEditable(false);
////		ta.setForeground(Color.WHITE); 
//		ta.setFont(new Font("Dialog", Font.PLAIN, 14));
//		ta.setOpaque(false);
////		ta.setBackground(new Color(92, 83, 55, 128));
//		ta.setBackground(new Color(0,0,0,128));
		
//		CustomScroll scr = new CustomScroll(this);
//		scr.setOpaque(false);
//		scr.setBackground(new Color(0,0,0,128));
//		scr.setSize(PopUpUnitMenu.WIDTH_0 - 10, PopUpUnitMenu.HEIGHT_2 - 10);
//		add(scr, BorderLayout.CENTER);
//		mainPanel.add(tf1, BorderLayout.NORTH);
		
//		WebPanel southPanel = new WebPanel();
//		add(southPanel, BorderLayout.SOUTH);
//		southPanel.setOpaque(false);
//		southPanel.setBackground(new Color(0, 0, 0, 128));
		
		setVisible(true);

        // Make panel drag-able
	    ComponentMover mover = new ComponentMover(this, desktop);
	    mover.registerComponent(this);	
	}

}
