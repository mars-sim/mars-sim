/*
 * Mars Simulation Project
 * DateDialog.java
 * @date 2022-07-10
 * @author Manny Kung
 * Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
 * http://www.astroarts.com/products/orbitviewer/
 * http://neo.jpl.nasa.gov/
 */

package com.mars_sim.ui.swing.astroarts;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.astroarts.ATime;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;


/**
*  Date Setting Dialog
*/
@SuppressWarnings("serial")
public class DateDialog extends JDialog {
			
	protected JTextField		tfYear;
	protected JTextField		tfDate;
	protected JComboBox<String>		monthCB;
	
	protected JButton		buttonOk;
	protected JButton		buttonCancel;

	protected JRadioButton buttonSimDate;
	protected JRadioButton buttonToday;
	protected JRadioButton button1986;
	protected JRadioButton buttonCustom;
	
	protected OrbitViewer	viewer;
	
	public DateDialog(OrbitViewer viewer, ATime atime, LocalDateTime earthTime) {
		super();
		setTitle("Input Date");// iconifiable
		setModal(true);
		this.viewer = viewer;
		
		// Set the layout.
		setLayout(new BorderLayout());
		
		JPanel currentPanel = new JPanel(new BorderLayout());
		currentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(currentPanel, BorderLayout.CENTER);

		currentPanel.setBorder(StyleManager.createLabelBorder("Current Date"));	

		AttributePanel attrPanel = new AttributePanel(3, 1);
		currentPanel.add(attrPanel, BorderLayout.NORTH);
		
		// Controls
		monthCB = new JComboBox<>();
		for (int i = 0; i < 12; i++) {
			monthCB.addItem(ATime.getMonthAbbr(i + 1));
		}
		monthCB.setSelectedIndex(atime.getMonth()-1);
		DefaultListCellRenderer listRenderer = new DefaultListCellRenderer();
	    listRenderer.setHorizontalAlignment(SwingConstants.CENTER);
	    monthCB.setRenderer(listRenderer);
		monthCB.setToolTipText("Choose the Month");
		attrPanel.addLabelledItem("Month", monthCB);

		Integer iDate = Integer.valueOf(atime.getDay());
		tfDate = new JTextField(iDate.toString(), 2);
		tfDate.setHorizontalAlignment(SwingConstants.CENTER);
		tfDate.setToolTipText("Choose the Day");
		attrPanel.addLabelledItem("Day", tfDate);
		
		Integer iYear = Integer.valueOf(atime.getYear());
		tfYear = new JTextField(iYear.toString(), 4);
		tfYear.setHorizontalAlignment(SwingConstants.CENTER);
		tfYear.setToolTipText("Choose the Year");
		attrPanel.addLabelledItem("Year", tfYear);
		
		
		JPanel choosePanel = new JPanel(new BorderLayout());
		choosePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(choosePanel, BorderLayout.NORTH);
		
		JPanel chooseDatePanel = new JPanel(new GridLayout(4, 1, 0, 0));			
		chooseDatePanel.setBorder(StyleManager.createLabelBorder("Choose Your Date"));	
		choosePanel.add(chooseDatePanel, BorderLayout.CENTER);
		
		buttonSimDate = new JRadioButton("Simulation Date");
		buttonSimDate.setBorder(new EmptyBorder(0, 15, 0, 0));
		chooseDatePanel.add(buttonSimDate);
		buttonSimDate.addActionListener(e -> {
				monthCB.setSelectedIndex(earthTime.getMonthValue() - 1);
				tfDate.setText(Integer.toString(earthTime.getDayOfMonth()));
				tfYear.setText(Integer.toString(earthTime.getYear()));    					

				viewer.setSelectedDate(1);
				viewer.repaint();
		});
		
		if (viewer.getSelectedDate() == 1) {
			buttonSimDate.setSelected(true);
		}
				
		buttonToday = new JRadioButton("Machine Date");
		buttonToday.setBorder(new EmptyBorder(0, 15, 0, 0));
		chooseDatePanel.add(buttonToday);	
		buttonToday.addActionListener(e -> {
				LocalDate currentdate = LocalDate.now();
				monthCB.setSelectedIndex(currentdate.getMonth().getValue() - 1);
				tfDate.setText(Integer.toString(currentdate.getDayOfMonth()));
				tfYear.setText(Integer.toString(currentdate.getYear()));    					

				viewer.setSelectedDate(2);
				viewer.repaint();
		});
		
		if (viewer.getSelectedDate() == 2) {
			buttonToday.setSelected(true);
		}
				
		button1986 = new JRadioButton("1986 Halley Return");
		button1986.setBorder(new EmptyBorder(0, 15, 0, 0));
		chooseDatePanel.add(button1986);
		button1986.addActionListener(e -> {
					monthCB.setSelectedIndex(2);
					tfDate.setText(9 + "");
					tfYear.setText(1986 + ""); 
					
					viewer.setSelectedDate(3);
    				viewer.repaint();
		});

		if (viewer.getSelectedDate() == 3) {
			button1986.setSelected(true);
		}
						
		buttonCustom = new JRadioButton("Custom Date");
		buttonCustom.setBorder(new EmptyBorder(0, 15, 0, 0));
		buttonCustom.setSelected(true);
		chooseDatePanel.add(buttonCustom);
		buttonCustom.addActionListener(e -> {
					monthCB.setSelectedIndex(monthCB.getSelectedIndex());
					tfDate.setText(tfDate.getText());
					tfYear.setText(tfYear.getText());  
					
					viewer.setSelectedDate(4);
    				viewer.repaint();
		});
		
		if (viewer.getSelectedDate() == 4) {
			buttonCustom.setSelected(true);
		}
		
		ButtonGroup group = new ButtonGroup();
		group.add(buttonSimDate);
		group.add(buttonToday);
		group.add(button1986);
		group.add(buttonCustom);
						
		JPanel southPanel = new JPanel(new GridLayout(1, 2, 4, 4));
		add(southPanel, BorderLayout.SOUTH);
		
		buttonOk = new JButton("OK");
		southPanel.add(buttonOk);
		buttonOk.addActionListener(e -> {
    				ATime at = null;

					int nYear = Integer.parseInt(tfYear.getText());
					int nMonth = monthCB.getSelectedIndex() + 1;
					int nDate  = Integer.parseInt(tfDate.getText());
					if (1600 <= nYear && nYear <= 2199 &&
								1 <= nMonth && nMonth <= 12 &&
								1 <= nDate  && nDate  <= 31) {
						at = new ATime(nYear, nMonth, nDate, 0.0);
					}
    				
    				dispose();
    				viewer.endDateDialog(at);
		});
	
		
		buttonCancel = new JButton("Cancel");
		southPanel.add(buttonCancel);
		buttonCancel.addActionListener(e -> setVisible(false));
		
		pack();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);	
	}
}
