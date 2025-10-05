/*
 * Mars Simulation Project
 * SelectionDialog.java
 * @date 2025-09-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.mars_sim.core.configuration.UserConfigurable;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.UserConfigurableListRenderer;
import com.mars_sim.ui.swing.utils.SortedComboBoxModel;

/**
 * This is a modal dialog used to allow the user to complete a selection of a list of options.
 * It presents a Run and cancel button.
 */
@SuppressWarnings("serial")
class SelectionDialog extends JDialog {
    private int selected = -1;
    private static final int RUN = 1;
    private static final int CANCEL = 0;

    /**
     * Create and display the dialog. The user selections are defined in a content panel rendered above the buttons.
     * @param frame
     * @param content
     * @param title
     * @return
     */
    public static boolean showDialog(Dialog frame,
                                    JPanel content,
                                    String title) {
        var dialog = new SelectionDialog(frame,
                                content,
                                Msg.getString("SelectionDialog.title", title));
        dialog.setVisible(true);

        return dialog.getChoice() == RUN;
    }
 

    @SuppressWarnings("unchecked")
    protected static<T extends UserConfigurable> JPanel createComboPane(String label, Collection<T> potentials,
                                                    Consumer<T> listener) {
        var contentPane = new JPanel();
        contentPane.setBorder(StyleManager.createLabelBorder(label));
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        var selectPanel = new JPanel();
        contentPane.add(selectPanel);
        selectPanel.add(new JLabel(Msg.getString("SelectionDialog.select"))); // -NLS-1$

        ComboBoxModel<T> model = new SortedComboBoxModel<>(potentials, Comparator.comparing(UserConfigurable::getName));

        JComboBox<T> selector = new JComboBox<>(model);
        selector.setRenderer(new UserConfigurableListRenderer());
        selectPanel.add(selector);

        var desc = new JTextArea(2, 20);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);

        contentPane.add(desc);

        selector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                T item = (T) e.getItem();
                desc.setText(item.getDescription());
                listener.accept(item);
            }
        });

        // Set initial selection
        T inital = (T) model.getSelectedItem();
        desc.setText(inital.getDescription());
        listener.accept(inital);

        return contentPane;
    }

    private SelectionDialog(Dialog owner, JPanel mainPane, String title) {
        super(JOptionPane.getFrameForComponent(owner), title, true);
        setLocationRelativeTo(owner);

        //Create and initialize the buttons.
        JButton cancelButton = new JButton(Msg.getString("SelectionDialog.cancel")); // -NLS-1$
        cancelButton.addActionListener(e -> choiceMade(CANCEL));
        //
        final JButton setButton = new JButton(Msg.getString("SelectionDialog.run")); // -NLS-1$
        setButton.addActionListener(e -> choiceMade(RUN));
        getRootPane().setDefaultButton(setButton);

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);
 
        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(mainPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);
 
        //Initialize values.
        pack();
    }
 
    private synchronized void choiceMade(int choice) {
        selected = choice;
        dispose();
		notifyAll();
	}
    
    /**
     * Gets the choice made by the user. This method blocks until a choice is made.
     * @return The choice made by the user; either RUN or CANCEL.
     */
    private synchronized int getChoice() {
        while (selected == -1) {
            try {
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
            }
        }
        return selected;
    }
}

