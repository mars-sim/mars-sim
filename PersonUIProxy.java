/**
 * Mars Simulation Project
 * PersonUIProxy.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

import java.awt.*;
import javax.swing.*;

/**
 * Standard user interface proxy for a person. 
 */
 
public class PersonUIProxy extends UnitUIProxy {
    
    private static ImageIcon buttonIcon = new ImageIcon("PersonIcon.gif");
    private UnitDialog unitDialog;
    
    public PersonUIProxy(Person person, UIProxyManager proxyManager) {
        super(person, proxyManager);
     
        unitDialog = null;
    }
    
    public boolean isMapDisplayed() { return false; }
    
    public ImageIcon getSurfMapIcon() { return null; }
    
    public ImageIcon getTopoMapIcon() { return null; }
    
    public Color getSurfMapLabelColor() { return null; }
    
    public Color getTopoMapLabelColor() { return null; }
    
    public Font getMapLabelFont() { return null; }
    
    public double getMapClickRange() { return 0D; }
    
    public boolean isGlobeDisplayed() { return false; }
    
    public Color getSurfGlobeColor() { return null; }
    
    public Color getTopoGlobeColor() { return null; }
    
    public ImageIcon getButtonIcon() { return buttonIcon; }
    
    public UnitDialog getUnitDialog(MainDesktopPane desktop) { 
        if (unitDialog == null) unitDialog = new PersonDialog(desktop, this);
        return unitDialog; 
    }
}
        
