/**
 * Mars Simulation Project
 * GroundVehicleUIProxy.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

import java.awt.*;
import javax.swing.*;

/**
 * Standard user interface proxy for a ground vehicle. 
 */
 
public class GroundVehicleUIProxy extends VehicleUIProxy {
    
    private GroundVehicle groundVehicle;
    private static ImageIcon surfMapIcon = new ImageIcon("VehicleSymbol.gif");
    private static ImageIcon topoMapIcon = new ImageIcon("VehicleSymbolBlack.gif");
    private static Font mapLabelFont = new Font("Helvetica", Font.PLAIN, 9);
    private ImageIcon buttonIcon;
    private UnitDialog unitDialog;
    
    public GroundVehicleUIProxy(GroundVehicle groundVehicle, 
            UIProxyManager proxyManager, ImageIcon buttonIcon) {    
        super(groundVehicle, proxyManager);        
                
        this.groundVehicle = groundVehicle;
        this.buttonIcon = buttonIcon;
       
        unitDialog = null;
    }
    
    public boolean isMapDisplayed() { 
        if (groundVehicle.getSettlement() == null) return true;
        else return false;
   }
   
    public ImageIcon getSurfMapIcon() { return surfMapIcon; }
    
    public ImageIcon getTopoMapIcon() { return topoMapIcon; }
    
    public Color getSurfMapLabelColor() { return Color.white; }
    
    public Color getTopoMapLabelColor() { return Color.black; }
    
    public Font getMapLabelFont() { return mapLabelFont; }
    
    public double getMapClickRange() { return 40D; }
    
    public boolean isGlobeDisplayed() { 
        if (groundVehicle.getSettlement() == null) return true;
        else return false;
    }
   
    public Color getSurfGlobeColor() { return Color.white; }
    
    public Color getTopoGlobeColor() { return Color.black; }
    
    public ImageIcon getButtonIcon() { return buttonIcon; }
    
    public UnitDialog getUnitDialog(MainDesktopPane desktop) { 
        if (unitDialog == null) unitDialog = new GroundVehicleDialog(desktop, this);
        return unitDialog; 
    }
    
    public Unit getUnit() { return groundVehicle; }
}
