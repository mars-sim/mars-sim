package com.mars_sim.ui.swing.tool.transportable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.map.MapPanel;
import com.mars_sim.ui.swing.tool.map.MineralMapLayer;
import com.mars_sim.ui.swing.tool.map.SurfacePOILayer;
import com.mars_sim.ui.swing.tool.map.UnitMapLayer;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

public class LandingSiteStep extends WizardStep<TransportState> {

    static final String ID = "Landing_Site";

    private SurfaceFeatures features;
    private Coordinates landingLocation;

    private JLabel selectedCoordinateLabel;
    private JLabel elevationLabel;
    private JLabel inclineLabel;

    private MapPanel mapPanel;

    private SurfacePOILayer pointLayer;

    public LandingSiteStep(TransportableWizard parent, TransportState state) {
        super(ID, parent);

        features = parent.getContext().getSimulation().getSurfaceFeatures();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Create the entry pane (for input)
        // JPanel entryPane = createEntryPane();
        // add(entryPane);
        var mapPane = createMapPane(state, parent.getContext());
        add(mapPane);

        // Create the site pane (displays selected coordinate)
        JPanel selectionPane = createSitePane();
        add(selectionPane);

        add(Box.createVerticalGlue());
    }

    /**
     * Creates the map pane with the map and click listener for selecting landing site.
     * @param state Current qizard state.
     * @param context UI context for accessing simulation and other resources.
     * @return New map pane.
     */
	private JComponent createMapPane(TransportState state, UIContext context) {
		// Create the map panel.
		mapPanel = new MapPanel(context);
		mapPanel.setBackground(new Color(0, 0, 0, 128));
		mapPanel.setOpaque(false);
					
		// Always add unit layer and where minerals are
		mapPanel.addMapLayer(new UnitMapLayer(mapPanel));
        var minLayer = new MineralMapLayer(mapPanel);
        minLayer.displayAll();  
        mapPanel.addMapLayer(minLayer);
        pointLayer = new SurfacePOILayer(mapPanel);
        mapPanel.addMapLayer(pointLayer);

        mapPanel.setMouseClickListener(c -> setLandingLocation(c));

		var mapPane = new JPanel(new BorderLayout());
		mapPane.setBorder(SwingHelper.createLabelBorder("Landing Site"));
        
		var dims = new Dimension(10, 200);
		mapPane.setPreferredSize(dims);
		mapPane.setMinimumSize(dims);
		mapPane.add(mapPanel, BorderLayout.CENTER);

       	return mapPane;	
	}

    /**
     * Creates the selection pane showing the currently selected coordinate.
     * 
     * @return the site pane
     */
    private JPanel createSitePane() {
        JPanel sitePane = new JPanel();
        sitePane.setLayout(new BoxLayout(sitePane, BoxLayout.Y_AXIS));
        sitePane.setBorder(new TitledBorder("Selection Landing Site"));
        
        // Create header label
        var details = new AttributePanel();
        sitePane.add(details);
        selectedCoordinateLabel = details.addTextField("Coordinates", "None", null);
        elevationLabel = details.addTextField("Elevation", "None", null);
        inclineLabel = details.addTextField("Incline", "None", null);
        
        return sitePane;
    }
    
    /**
     * User has selected a new landing location. Update the display and state accordingly.
     * @param newLocation New landing location of null to clear.
     */
    private void setLandingLocation(Coordinates newLocation) {
        this.landingLocation = newLocation;

        if (landingLocation != null) {
            selectedCoordinateLabel.setText(landingLocation.getFormattedString());
            double [] terrain = features.getTerrainElevation().getTerrainProfile(newLocation);
            elevationLabel.setText(String.format("%.2f", terrain[0]));
            inclineLabel.setText(String.format("%.2f", terrain[1]));

            // Set the selected POI on the map layer to show the landing site
            pointLayer.setSelection(new SurfacePOI() {
                @Override
                public Coordinates getCoordinates() {
                    return landingLocation;
                }

                @Override
                public String getName() {
                    return "Landing Site";
                }
            });

        } else {
            selectedCoordinateLabel.setText(null);
            elevationLabel.setText(null);
            inclineLabel.setText(null);

            pointLayer.setSelection(null);
        }
        mapPanel.repaint();
        setMandatoryDone(landingLocation != null);
    }

    @Override
    public void updateState(TransportState state) {
        state.setLandingSite(landingLocation);
    }

    /**
     * Release resources used by the map panel when this step is no longer needed.
     */
    @Override
    protected void release() {
        mapPanel.destroy();
        super.release();
    }

}
