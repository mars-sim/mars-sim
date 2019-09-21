package org.mars_sim.msp.ui.swing.tool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.steelseries.gauges.DigitialRadial;
import org.mars_sim.msp.ui.steelseries.tools.LedColor;
import org.mars_sim.msp.ui.steelseries.tools.Orientation;


public class TestLinear {
    @SuppressWarnings("serial")
	private static void createAndShowUI() {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);

        JPanel panel = new JPanel() {
            @Override 
            public Dimension getPreferredSize() {
                return new Dimension(50, 300);
            }
        };
        
        //JPanel temperaturePanel = new JPanel(new FlowLayout());
        DigitialRadial temperatureL = new DigitialRadial();
		temperatureL.setTitle("Air Temperature");
		temperatureL.setUnitString(Msg.getString("temperature.sign.degreeCelsius"));
		temperatureL.setValueAnimated(20);
		temperatureL.setOrientation(Orientation.VERTICAL);
		temperatureL.init(20, 200);
		//temperatureL.setMajorTickmarkType(new TickmarkType(LINE));
		//temperaturePanel.add(temperatureL);
		//dataPanel.add(temperaturePanel);

        panel.setLayout(new BorderLayout());
        panel.add(temperatureL, BorderLayout.CENTER);
        frame.add(panel);

        JPanel buttonsPanel = new JPanel();
        JLabel valueLabel = new JLabel("Value:");

        final JTextField valueField = new JTextField(7);
        valueField.setText("30");
        JButton button = new JButton("Set");
        button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.valueOf(valueField.getText());
                    temperatureL.setValueAnimated(value);
                    //temperatureL.setBackgroundColor(BackgroundColor.WHITE);
                    //temperatureL.setLcdColor(LcdColor.STANDARD_GREEN_LCD);
                    //temperatureL.setKnobStyle(KnobStyle.BRASS);
                    //temperatureL.setColor(ColorDef.BLUE);
                    temperatureL.setLedColor(LedColor.BLUE);
                } catch(NumberFormatException ex) { 
                    //TODO - handle invalid input 
                    System.err.println("invalid input");
                }
            }
        });

        buttonsPanel.add(valueLabel);
        buttonsPanel.add(valueField);
        buttonsPanel.add(button);

        frame.add(buttonsPanel, BorderLayout.NORTH);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowUI();
            }
        });
    }
}