/**
 * Mars Simulation Project
 * ManufacturePanel.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JLabel;

import javax.swing.JProgressBar;

import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;

import com.alee.laf.panel.WebPanel;

/**
 * A panel showing information about a manufacturing process.
 */
public class ManufacturePanel extends WebPanel {

	// Data members
	private ManufactureProcess process;
	private BoundedRangeModel workBarModel;
	private BoundedRangeModel timeBarModel;

	/**
	 * Constructor
	 * @param process the manufacturing process.
	 * @param showBuilding is the building name shown?
	 * @param processStringWidth the max string width to display for the process name.
	 */
	public ManufacturePanel(ManufactureProcess process, boolean showBuilding, int processStringWidth) {
		// Call WebPanel constructor
		super();

		// Initialize data members.
		this.process = process;

        // Set layout
		if (showBuilding) setLayout(new GridLayout(4, 1, 0, 0));
		else setLayout(new GridLayout(3, 1, 0, 0));

        // Set border
        setBorder(new MarsPanelBorder());

        // Prepare name panel.
        WebPanel namePane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
        add(namePane);

        // Prepare cancel button.
        JButton cancelButton = new JButton(ImageLoader.getIcon("CancelSmall"));
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
//        		try {
        			getManufactureProcess().getWorkshop().endManufacturingProcess(getManufactureProcess(), true);
//        		}
//        		catch (BuildingException e) {}
	        }
        });
        cancelButton.setToolTipText("Cancel manufacturing process");
        namePane.add(cancelButton);

        // Prepare name label.
        String name = process.getInfo().getName();
        if (name.length() > 0) {
        	String firstLetter = name.substring(0, 1).toUpperCase();
        	name = " " + firstLetter + name.substring(1);
        }
        if (name.length() > processStringWidth) name = name.substring(0, processStringWidth) + "...";
		// Capitalize process names
        JLabel nameLabel = new JLabel(Conversion.capitalize(name), JLabel.CENTER);
        namePane.add(nameLabel);

        if (showBuilding) {
        	// Prepare building name label.
        	String buildingName = process.getWorkshop().getBuilding().getNickName();
        	JLabel buildingNameLabel = new JLabel(buildingName, JLabel.CENTER);
        	add(buildingNameLabel);
        }

        // Prepare work panel.
        WebPanel workPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        add(workPane);

        // Prepare work label.
        JLabel workLabel = new JLabel("Work: ", JLabel.LEFT);
        workPane.add(workLabel);

        // Prepare work progress bar.
        JProgressBar workBar = new JProgressBar();
        workBarModel = workBar.getModel();
        workBar.setStringPainted(true);
        workPane.add(workBar);

        // Prepare time panel.
        WebPanel timePane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        add(timePane);

        // Prepare time label.
        JLabel timeLabel = new JLabel("Time: ", JLabel.LEFT);
        timePane.add(timeLabel);

        // Prepare time progress bar.
        JProgressBar timeBar = new JProgressBar();
        timeBarModel = timeBar.getModel();
        timeBar.setStringPainted(true);
        timePane.add(timeBar);

        // Update progress bars.
        update();

        // Add tooltip.
        setToolTipText(getToolTipString(process.getInfo(), process.getWorkshop().getBuilding()));
	}

    /**
     * Updates the panel's information.
     */
    public void update() {		
    	// Update work progress bar.
    	double workTimeRequired = process.getInfo().getWorkTimeRequired();
        double workTimeRemaining = process.getWorkTimeRemaining();
        int workProgress = 100;
        if (workTimeRequired > 0D) workProgress = (int) (100D * (workTimeRequired - workTimeRemaining) / workTimeRequired);
        workBarModel.setValue(workProgress);

        // Update time progress bar.
        double timeRequired = process.getInfo().getProcessTimeRequired();
        double timeRemaining = process.getProcessTimeRemaining();
        int timeProgress = 100;
        if (timeRequired > 0D) timeProgress = (int) (100D * (timeRequired - timeRemaining) / timeRequired);
        timeBarModel.setValue(timeProgress);
    }

    /**
     * Gets the manufacture process.
     * @return process
     */
    public ManufactureProcess getManufactureProcess() {
    	return process;
    }

    /**
     * Gets a tool tip string for a manufacturing process.
     * @param info the manufacture process info.
     * @param building the manufacturing building (or null if none).
     */
    public static String getToolTipString(ManufactureProcessInfo info, Building building) {
        StringBuilder result = new StringBuilder("<html>");

        result.append("&emsp;&emsp;&emsp;&emsp;&emsp;&nbsp;Process : ").append(Conversion.capitalize(info.getName())).append("<br>");
    	//if (building != null) result.append("Building : ").append(building.getNickName()).append("<br>");
        result.append("&emsp;&emsp;&emsp;&emsp;&nbsp;Labor Req : ").append(info.getWorkTimeRequired()).append(" millisols<br>");
        result.append("&emsp;&emsp;&emsp;&emsp;&nbsp;&nbsp;Time Req : ").append(info.getProcessTimeRequired()).append(" millisols<br>");
        result.append("&emsp;&emsp;&emsp;&emsp;Power Req : ").append(info.getPowerRequired()).append(" kW<br>");
        result.append("&emsp;&emsp;&nbsp;Bldg Tech Req : Level ").append(info.getTechLevelRequired()).append("<br>");
        result.append("Mat Sci Skill Req : Level ").append(info.getSkillLevelRequired()).append("<br>");

    	// Add process inputs.
    	result.append("&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&nbsp;Inputs : ");
    	Iterator<ManufactureProcessItem> i = info.getInputList().iterator();
    	int ii = 0;
    	while (i.hasNext()) {
    		ManufactureProcessItem item = i.next();
    		// Capitalize process names
            if (ii ==0) result.append(getItemAmountString(item)).append(" ").append(Conversion.capitalize(item.getName())).append("<br>");
            else result.append("&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;").append(getItemAmountString(item)).append(" ").append(Conversion.capitalize(item.getName())).append("<br>");
            ii++;
    	}

    	// Add process outputs.
    	result.append("&emsp;&emsp;&emsp;&emsp;&emsp;&nbsp;&nbsp;Outputs : ");
    	Iterator<ManufactureProcessItem> j = info.getOutputList().iterator();
    	int jj = 0;
    	while (j.hasNext()) {
    		ManufactureProcessItem item = j.next();
    		//  Capitalize process names
            if (jj==0) result.append(getItemAmountString(item)).append(" ").append(Conversion.capitalize(item.getName())).append("<br>");
            else result.append("&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;").append(getItemAmountString(item)).append(" ").append(Conversion.capitalize(item.getName())).append("<br>");
            jj++;
    	}

    	result.append("</html>");

    	return result.toString();
    }

    /**
     * Gets a string representing an manufacture process item amount.
     * @param item the manufacture process item.
     * @return amount string.
     */
    private static String getItemAmountString(ManufactureProcessItem item) {
    	if (ItemType.AMOUNT_RESOURCE.equals(item.getType())) {
			return item.getAmount() + " kg";
    	}
		else return Integer.toString((int) item.getAmount());
    }
    
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
	    process = null;
		workBarModel = null;
		timeBarModel = null;
	}
}