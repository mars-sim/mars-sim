/**
 * Mars Simulation Project
 * SettlementUIProxy.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.*;  
import java.awt.*;
import javax.swing.*;

/**
 * Standard user interface proxy for a settlement. 
 */
 
public class SettlementUIProxy extends UnitUIProxy {
    
    private static ImageIcon surfMapIcon = new ImageIcon("images/SettlementSymbol.gif");
    private static ImageIcon topoMapIcon = new ImageIcon("images/SettlementSymbolBlack.gif");
    private static Font mapLabelFont = new Font("Helvetica", Font.PLAIN, 12);
    private static ImageIcon buttonIcon = new ImageIcon("images/SettlementIcon.gif");
    private UnitDialog unitDialog;
    
    public SettlementUIProxy(Settlement settlement, UIProxyManager proxyManager) {
        super(settlement, proxyManager);
        
        unitDialog = null;
    }
    
    public boolean isMapDisplayed() { return true; }
    
    public ImageIcon getSurfMapIcon() { return surfMapIcon; }
    
    public ImageIcon getTopoMapIcon() { return topoMapIcon; }
    
    public Color getSurfMapLabelColor() { return Color.green; }
    
    public Color getTopoMapLabelColor() { return Color.black; }
    
    public Font getMapLabelFont() { return mapLabelFont; }
    
    public double getMapClickRange() { return 90D; }
    
    public boolean isGlobeDisplayed() { return true; }
    
    public Color getSurfGlobeColor() { return Color.green; }
    
     public Color getTopoGlobeColor() { return Color.black; }
    
    public ImageIcon getButtonIcon() { return buttonIcon; }
    
    public UnitDialog getUnitDialog(MainDesktopPane desktop) { 
        if (unitDialog == null) unitDialog = new SettlementDialog(desktop, this);
        return unitDialog; 
    }
}
