/*
 * Mars Simulation Project
 * JMarsTimeEditor.java
 * @date 2024-03-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MarsTimeFormat;

/**
 * A component for editing MarsTime values. It shows an error messsage if the entered value is not valid,
 * and allows a listener to be notified when a valid time is entered.
 */
public class JMarsTimeEditor extends JPanel {
    private static final String FORMAT = "orbit-month-sol:millisol";
    private static final int MIN_LENGTH = 8; // Minimum length for a valid MarsTime string

    private JTextField entryField;
    private JLabel errorField;
    private MarsTime parsedTime = null;
    private Consumer<MarsTime> listener;

    public JMarsTimeEditor() {
        entryField = new JTextField(FORMAT);
        entryField.setColumns(FORMAT.length());
        entryField.setToolTipText("Accepted format: " + FORMAT);
        entryField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                parseMarsTime();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                parseMarsTime();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                parseMarsTime();
            }
        });

        add(entryField);
        errorField = new JLabel("");
        add(errorField);
    }

    /**
     * Get the currently entered MarsTime, or null if the entry is invalid.
     * @return
     */
    public MarsTime getMarsTime() {
        return parsedTime;
    }
    
    private void parseMarsTime() {
        String text = entryField.getText();
        if (text.length() < MIN_LENGTH) {
            return;
        }

        try {
            parsedTime = null;
            parsedTime = MarsTimeFormat.fromDateString(text);
            errorField.setText("");
            if (listener != null) {
                listener.accept(parsedTime);
            }
        } catch (RuntimeException e) {
            errorField.setText("Invalid format: " + e.getMessage());
        }
    }

    /**
     * Add a listener that will be called whenever a valid MarsTime is entered.
     * @param listener Notified with the parsed MarsTime whenever a valid time is entered.
     */
    public void addListener(Consumer<MarsTime> listener) {
        this.listener = listener;
    }
}
