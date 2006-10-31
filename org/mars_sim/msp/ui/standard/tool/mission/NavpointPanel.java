/**
 * Mars Simulation Project
 * NavpointPanel.java
 * @version 2.80 2006-10-24
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionEvent;
import org.mars_sim.msp.simulation.person.ai.mission.MissionListener;
import org.mars_sim.msp.simulation.person.ai.mission.TravelMission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.tool.map.MapPanel;
import org.mars_sim.msp.ui.standard.tool.map.NavpointMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitLabelMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.VehicleTrailMapLayer;


public class NavpointPanel extends JPanel implements ListSelectionListener,
		MissionListener {

	private Mission currentMission;
	private MapPanel mapPane;
	private VehicleTrailMapLayer trailLayer;
	private NavpointMapLayer navpointLayer;
	
	NavpointPanel() {
		
		setLayout(new BorderLayout());
		
		Box mainPane = Box.createVerticalBox();
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);
		
		JPanel mapDisplayPane = new JPanel(new BorderLayout(0, 0));
		mainPane.add(mapDisplayPane);
		
		mapPane = new MapPanel();
		mapPane.addMapLayer(new UnitIconMapLayer(mapPane));
		mapPane.addMapLayer(new UnitLabelMapLayer());
		trailLayer = new VehicleTrailMapLayer();
		mapPane.addMapLayer(trailLayer);
		navpointLayer = new NavpointMapLayer(this);
		mapPane.addMapLayer(navpointLayer);
		mapDisplayPane.add(mapPane, BorderLayout.CENTER);
		
		JButton northButton = new JButton("^");
		mapDisplayPane.add(northButton, BorderLayout.NORTH);
		
		JButton westButton = new JButton("<");
		mapDisplayPane.add(westButton, BorderLayout.WEST);
		
		JButton eastButton = new JButton(">");
		mapDisplayPane.add(eastButton, BorderLayout.EAST);
		
		JButton southButton = new JButton("v");
		mapDisplayPane.add(southButton, BorderLayout.SOUTH);
	}
	
	public void valueChanged(ListSelectionEvent e) {
		if (currentMission != null) currentMission.removeListener(this);
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();
		if (mission != null) {
			mission.addListener(this);
			currentMission = mission;
			if (mission.getPeopleNumber() > 0) {
				if (mission instanceof VehicleMission) 
					trailLayer.setSingleVehicle(((VehicleMission) mission).getVehicle());
				if (mission instanceof TravelMission)
					navpointLayer.setSingleMission((TravelMission) mission);
				mapPane.showMap(currentMission.getPeople().get(0).getCoordinates());
			}
		}
		else {
			currentMission = null;
			trailLayer.setSingleVehicle(null);
			navpointLayer.setSingleMission(null);
		}
	}

	public void missionUpdate(MissionEvent event) {
	}
}