package org.mars_sim.msp.ui.swing.tool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.ui.steelseries.extras.Led;


public class TestLED {
	
	private boolean toggle_on_off = false;
	private String state = "Off";
	
	public TestLED() {
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
                return new Dimension(100, 100);
            }
        };
        
        //JPanel temperaturePanel = new JPanel(new FlowLayout());
        Led led = new Led();
        //led.setLedBlinking(true); 
		//temperatureL.setTitle("Air Temperature");
		//temperatureL.setUnitString(Msg.getString("temperature.sign.degreeCelsius"));
		//temperatureL.setValueAnimated(20);
		//temperatureL.setOrientation(Orientation.VERTICAL);
		//temperatureL.init(20, 200);
		//temperatureL.setMajorTickmarkType(new TickmarkType(LINE));
		//temperaturePanel.add(temperatureL);
		//dataPanel.add(temperaturePanel);

        panel.setLayout(new BorderLayout());
        panel.add(led, BorderLayout.CENTER);
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
                        led.setLedBlinking(false);
                    }
                    else {
                    	toggle_on_off = true;
                    	state = "On";
                    }
                    led.setLedOn(toggle_on_off);
                    button.setText(state);
                    //led.setBackgroundColor(BackgroundColor.WHITE);
                    //led.setLcdColor(LcdColor.STANDARD_GREEN_LCD);
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
    	new TestLED();
    }
}