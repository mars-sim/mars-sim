package org.mars_sim.msp.ui.swing.tool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.ui.steelseries.gauges.DisplayMulti;
import org.mars_sim.msp.ui.steelseries.gauges.DisplayRectangular;
import org.mars_sim.msp.ui.steelseries.gauges.DisplaySingle;
import org.mars_sim.msp.ui.steelseries.tools.BackgroundColor;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;


public class TestLCD {
	
	private boolean toggle_on_off = false;
	private String state = "Off";
	
	public TestLCD() {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            createAndShowUI();
	        }
	    });
	}
    @SuppressWarnings("serial")
	private void createAndShowUI() {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);

        JPanel panel = new JPanel() {
            @Override 
            public Dimension getPreferredSize() {
                return new Dimension(200, 100);
            }
        };
        
        //JPanel temperaturePanel = new JPanel(new FlowLayout());
        DisplayRectangular lcd = new DisplayRectangular();
        //led.setLedBlinking(true); 
		//lcd.setTitle("Air Temperature");
		lcd.setLcdInfoString("Latitude");
		//lcd.setLcdText("Latitude");
		lcd.setLcdValueAnimated(20);
        lcd.setBackgroundColor(BackgroundColor.BEIGE);
        
        DisplaySingle lcd1 = new DisplaySingle();
        lcd1.setLcdText("Latitude");
        lcd1.setSectionsVisible(true);
        lcd1.setLcdTextScrolling(true);
        lcd1.setLcdUnitString("N");
        lcd1.setLcdValueAnimated(2.3);
        lcd1.setLcdInfoString("Lat");
        //lcd1.setGlowColor(Color.orange);
        //LcdColor LcdColor = LcdColor.BLUELIGHTBLUE_LCD
		lcd1.setLcdColor(LcdColor.BLUELIGHTBLUE_LCD);
        //lcd.setLcdColor(LcdColor.STANDARD_GREEN_LCD);
		//temperatureL.setOrientation(Orientation.VERTICAL);
		//lcd1.init(100, 50);
		lcd1.setSize(new Dimension(200, 100));
		//temperatureL.setMajorTickmarkType(new TickmarkType(LINE));
		//temperaturePanel.add(temperatureL);
		//dataPanel.add(temperaturePanel);

		
		DisplayMulti dm = new DisplayMulti();
		dm.setLcdUnitString("S");
		dm.setLcdValueAnimated(1.5);
		dm.setLcdColor(LcdColor.BLUELIGHTBLUE_LCD);
		dm.setLcdInfoString("Lat");
		
        panel.setLayout(new BorderLayout());
        panel.add(lcd1, BorderLayout.CENTER);
        frame.add(panel);

        JPanel buttonsPanel = new JPanel();
        //JLabel valueLabel = new JLabel("Value:");

        //final JTextField valueField = new JTextField(7);
        //valueField.setText("30");
        JButton button = new JButton(state);
        button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //double value = Double.valueOf(valueField.getText());
                    if (toggle_on_off) {
                    	toggle_on_off = false;
                    	state = "Off";
                        
                    }
                    else {
                    	toggle_on_off = true;
                    	state = "On";
                    }
                    //led.setLedOn(toggle_on_off);
                    button.setText(state);
                    //led.setKnobStyle(KnobStyle.BRASS);
                    //led.setColor(ColorDef.BLUE);
                    //led.setLedColor(LedColor.BLUE);
                } catch(NumberFormatException ex) { 
                    //TODO - handle invalid input 
                    System.err.println("invalid input");
                }
            }
        });

        //buttonsPanel.add(valueLabel);
        //buttonsPanel.add(valueField);
        buttonsPanel.add(button);

        frame.add(buttonsPanel, BorderLayout.NORTH);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
    	new TestLCD();
    }
}