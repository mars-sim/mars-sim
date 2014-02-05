/**
 * Mars Simulation Project
 * ResupplyWindow.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/**
 * Window for the resupply tool.
 */
public class ResupplyWindow extends ToolWindow implements ListSelectionListener {

    // Tool name
    public static final String NAME = "Resupply Tool";

    // Data members
    private IncomingListPanel incomingListPane;
    private ArrivedListPanel arrivedListPane;
    private TransportDetailPanel detailPane;
    private JButton modifyButton;
    private JButton cancelButton;

    /**
     * Constructor
     * @param desktop the main desktop panel.
     */
    public ResupplyWindow(MainDesktopPane desktop) {

        // Use the ToolWindow constructor.
        super(NAME, desktop);

        // Create main panel.
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(MainDesktopPane.newEmptyBorder());
        setContentPane(mainPane);

        // Create list panel.
        JPanel listPane = new JPanel(new GridLayout(2, 1));
        mainPane.add(listPane, BorderLayout.WEST);

        // Create incoming list panel.
        incomingListPane = new IncomingListPanel();
        incomingListPane.getIncomingList().addListSelectionListener(this);
        listPane.add(incomingListPane);

        // Create arrived list panel.
        arrivedListPane = new ArrivedListPanel();
        listPane.add(arrivedListPane);

        // Set incoming and arrived list panels to listen to each other's list selections.
        incomingListPane.getIncomingList().addListSelectionListener(arrivedListPane);
        arrivedListPane.getArrivedList().addListSelectionListener(incomingListPane);

        // Create detail panel.
        detailPane = new TransportDetailPanel();
        incomingListPane.getIncomingList().addListSelectionListener(detailPane);
        arrivedListPane.getArrivedList().addListSelectionListener(detailPane);
        mainPane.add(detailPane, BorderLayout.CENTER);

        // Create button panel.
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mainPane.add(buttonPane, BorderLayout.SOUTH);

        // Create new button.
        JButton newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                createNewTransportItem();
            }
        });
        buttonPane.add(newButton);

        // Create modify button.
        modifyButton = new JButton("Modify");
        modifyButton.setEnabled(false);
        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                modifyTransportItem();
            }
        });
        buttonPane.add(modifyButton);

        // Create cancel button.
        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancelTransportItem();
            }
        });
        buttonPane.add(cancelButton);

        pack();
    }

    /**
     * Opens a create dialog.
     */
    private void createNewTransportItem() {
        // Pause simulation.
        desktop.getMainWindow().pauseSimulation();

        // Create new transportItem dialog.
        new NewTransportItemDialog(desktop.getMainWindow().getFrame());

        // Unpause simulation.
        desktop.getMainWindow().unpauseSimulation();
    }

    /**
     * Opens a modify dialog for the currently selected transport item.
     */
    private void modifyTransportItem() {
        // Pause simulation.
        desktop.getMainWindow().pauseSimulation();

        // Get currently selected incoming transport item.
        Transportable transportItem = (Transportable) incomingListPane.getIncomingList().getSelectedValue();

        if ((transportItem != null)) {
            if (transportItem instanceof Resupply) {
                // Create modify resupply mission dialog.
                Resupply resupply = (Resupply) transportItem;
                String title = "Modify Resupply Mission";
                new ModifyTransportItemDialog(desktop.getMainWindow().getFrame(), title, resupply);
            }
            else if (transportItem instanceof ArrivingSettlement) {
                // Create modify arriving settlement dialog.
                ArrivingSettlement settlement = (ArrivingSettlement) transportItem;
                String title = "Modify Arriving Settlement";
                new ModifyTransportItemDialog(desktop.getMainWindow().getFrame(), title, settlement);
            }
        }
 
        // Unpause simulation.
        desktop.getMainWindow().unpauseSimulation();
    }

    /**
     * Cancels the currently selected transport item.
     */
    private void cancelTransportItem() {
        // Cancel the selected transport item.
        Transportable transportItem = (Transportable) incomingListPane.getIncomingList().getSelectedValue();
        if (transportItem != null) {
            Simulation.instance().getTransportManager().cancelTransportItem(transportItem);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            JList incomingList = (JList) evt.getSource();
            Object selected = incomingList.getSelectedValue();
            if (selected != null) {
                // Incoming transport item is selected, 
                // so enable modify and cancel buttons.
                modifyButton.setEnabled(true);
                cancelButton.setEnabled(true);
            }
            else {
                // Incoming transport item is unselected,
                // so disable modify and cancel buttons.
                modifyButton.setEnabled(false);
                cancelButton.setEnabled(false);
            }
        }
    }

    /**
     * Prepare this window for deletion.
     */
    public void destroy() {
        incomingListPane.destroy();
        arrivedListPane.destroy();
        detailPane.destroy();
    }
}