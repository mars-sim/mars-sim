/**
 * Mars Simulation Project
 * UIProxyManager.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

import java.util.*;
import javax.swing.*;

/**
 * Creates and manages a collection of unit UI proxies. 
 */
 
public class UIProxyManager {
    
    private UnitUIProxy[] unitUIProxies;
   
    public UIProxyManager(Unit[] units) {
        
        Vector proxies = new Vector();
       
        for (int x=0; x < units.length; x++) {
           
            Unit unit = units[x];
           
            if (unit instanceof Person)
                proxies.addElement(new PersonUIProxy((Person) unit, this));
               
            if (unit instanceof Settlement)
                proxies.addElement(new SettlementUIProxy((Settlement) unit, this));
               
            if (unit instanceof GroundVehicle) {
                if (unit instanceof Rover) {
                    ImageIcon roverIcon = new ImageIcon("RoverIcon.gif");
                    proxies.addElement(new GroundVehicleUIProxy(
                            (GroundVehicle) unit, this, roverIcon));
                }
            }
        }
        
        unitUIProxies = new UnitUIProxy[proxies.size()];
        for (int x=0; x < unitUIProxies.length; x++) 
            unitUIProxies[x] = (UnitUIProxy) proxies.elementAt(x);
    }
    
    public UnitUIProxy[] getUIProxies() { 
        UnitUIProxy[] result = new UnitUIProxy[unitUIProxies.length];
        for (int x=0; x < unitUIProxies.length; x++) result[x] = unitUIProxies[x];
        return result; 
    }
    
    public UnitUIProxy getUnitUIProxy(Unit unit) {
        UnitUIProxy result = null;
        for (int x=0; x < unitUIProxies.length; x++) {
            if (unitUIProxies[x].getUnit() == unit) result = unitUIProxies[x];
        }
        return result;
    }
    
    public UnitUIProxy[] getOrderedPeopleProxies() {
        Vector peopleProxies = new Vector();
        
        for (int x=0; x < unitUIProxies.length; x++) {
            if (unitUIProxies[x] instanceof PersonUIProxy)
                peopleProxies.addElement(unitUIProxies[x]);
        }
        
        return sortProxies(peopleProxies);
    }
    
    public UnitUIProxy[] getOrderedSettlementProxies() {
        Vector settlementProxies = new Vector();
        
        for (int x=0; x < unitUIProxies.length; x++) {
            if (unitUIProxies[x] instanceof SettlementUIProxy)
                settlementProxies.addElement(unitUIProxies[x]);
        }
        
        return sortProxies(settlementProxies);
    }
    
    public UnitUIProxy[] getOrderedVehicleProxies() {
        Vector vehicleProxies = new Vector();
        
        for (int x=0; x < unitUIProxies.length; x++) {
            if (unitUIProxies[x] instanceof VehicleUIProxy)
                vehicleProxies.addElement(unitUIProxies[x]);
        }
        
        return sortProxies(vehicleProxies);
    }
    
    private UnitUIProxy[] sortProxies(Vector unsortedProxies) {

        UnitUIProxy sorterProxy = null;
        UnitUIProxy[] sortedProxies = new UnitUIProxy[unsortedProxies.size()];

        for (int x = 0; x < sortedProxies.length; x++) {
            sorterProxy = (UnitUIProxy) unsortedProxies.elementAt(0);
            for (int y = 0; y < unsortedProxies.size(); y++) {
                UnitUIProxy tempProxy = (UnitUIProxy) unsortedProxies.elementAt(y);
                if (tempProxy.getUnit().getName()
                        .compareTo(sorterProxy.getUnit().getName()) <= 0) {
                    sorterProxy = tempProxy;
                }
            }
            sortedProxies[x] = sorterProxy;
            unsortedProxies.removeElement(sorterProxy);
        }

        return sortedProxies;
    }
}
