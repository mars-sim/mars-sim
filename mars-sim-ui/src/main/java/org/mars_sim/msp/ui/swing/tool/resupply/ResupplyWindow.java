/**
 * Mars Simulation Project
 * ResupplyWindow.java
 * @version 3.02 2012-04-03
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/**
 * Window for the resupply tool.
 */
public class ResupplyWindow extends ToolWindow {

    // Tool name
    public static final String NAME = "Resupply Tool";
    
    // Data members
    private IncomingListPanel incomingListPane;
    private ArrivedListPanel arrivedListPane;
    private ResupplyDetailPanel detailPane;
    
    /**
     * Constructor
     * @param desktop the main desktop panel.
     */
    public ResupplyWindow(MainDesktopPane desktop) {
        
        // Use the ToolWindow constructor.
        super(NAME, desktop);
        
        // Create main panel.
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);
        
        // Create list panel.
        JPanel listPane = new JPanel(new GridLayout(2, 1));
        mainPane.add(listPane, BorderLayout.WEST);
        
        // Create incoming list panel.
        incomingListPane = new IncomingListPanel();
        listPane.add(incomingListPane);
        
        // Create arrived list panel.
        arrivedListPane = new ArrivedListPanel();
        listPane.add(arrivedListPane);
        
        // Create detail panel.
        detailPane = new ResupplyDetailPanel();
        mainPane.add(detailPane, BorderLayout.CENTER);
        
        // Create button panel.
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mainPane.add(buttonPane, BorderLayout.SOUTH);
        
        // Create new button.
        JButton newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                createNewResupplyMission();
            }
        });
        buttonPane.add(newButton);
        
        // Create modify button.
        JButton modifyButton = new JButton("Modify");
        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                modifyResupplyMission();
            }
        });
        buttonPane.add(modifyButton);
        
        // Create cancel button.
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancelResupplyMission();
            }
        });
        buttonPane.add(cancelButton);
        
        pack();
    }
   
    private void createNewResupplyMission() {
        // TODO
    }
    
    private void modifyResupplyMission() {
        // TODO
    }
    
    private void cancelResupplyMission() {
        // TODO
    }
}