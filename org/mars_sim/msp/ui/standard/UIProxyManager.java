/**
 * Mars Simulation Project
 * UIProxyManager.java
 * @version 2.73 2001-11-25
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import java.util.*;
import javax.swing.*;

/** Creates and manages a collection of unit UI proxies. */
public class UIProxyManager {

    // Data members
    private ArrayList proxies; // Collection of UnitUIProxies

    /** Constructs a UIProxyManager object 
     *  @units array of units
     */
    public UIProxyManager(UnitCollection units) {
        proxies = new ArrayList();

        UnitIterator i = units.iterator();
        while (i.hasNext()) {
            Unit unit = i.next();

            if (unit instanceof Person)
                proxies.add(new PersonUIProxy((Person) unit, this));

            if (unit instanceof Settlement)
                proxies.add(new SettlementUIProxy((Settlement) unit, this));

            if (unit instanceof GroundVehicle) {
                if (unit instanceof Rover) {
                    ImageIcon roverIcon = new ImageIcon("images/RoverIcon.gif");
                    proxies.add(new GroundVehicleUIProxy((GroundVehicle) unit, this, roverIcon));
                }
            }
        }
    }
    
    /** Gets an iterator to a collection of all the UnitUIProxies.
     *  @return an iterator to a collection of unit UI proxies
     */
    public Iterator getUIProxies() {
        ArrayList newProxies = new ArrayList(proxies);
        return newProxies.iterator();
    }

    /** Gets the UnitUIProxy for a given unit 
     *  @param unit the unit
     *  @return the unit's UI proxy
     */
    public UnitUIProxy getUnitUIProxy(Unit unit) {
        UnitUIProxy result = null;
        Iterator i = proxies.iterator();
        while (i.hasNext()) {
            UnitUIProxy proxy = (UnitUIProxy) i.next();
            if (proxy.getUnit() == unit) result = proxy;
        }
        return result;
    }

    /** Gets an ordered iterator of person UI proxies.
     *  @return an iterator of a collection of person UI proxies
     */
    public Iterator getOrderedPersonProxies() {
        ArrayList personProxies = new ArrayList();
        
        Iterator i = proxies.iterator();
        while (i.hasNext()) {
            UnitUIProxy proxy = (UnitUIProxy) i.next();
            if (proxy instanceof PersonUIProxy) personProxies.add(proxy);
        }

        return sortProxies(personProxies);
    }

    /** Gets an ordered iterator of settlement UI proxies.
     *  @return an iterator of a collection of settlement UI proxies
     */
    public Iterator getOrderedSettlementProxies() {
        ArrayList settlementProxies = new ArrayList();

        Iterator i = proxies.iterator();
        while (i.hasNext()) {
            UnitUIProxy proxy = (UnitUIProxy) i.next();
            if (proxy instanceof SettlementUIProxy) settlementProxies.add(proxy);
        }

        return sortProxies(settlementProxies);
    }

    /** Gets an ordered iterator of vehicle UI proxies.
     *  @return an iterator of a collection of vehicle UI proxies
     */
    public Iterator getOrderedVehicleProxies() {
        ArrayList vehicleProxies = new ArrayList();

        Iterator i = proxies.iterator();
        while (i.hasNext()) {
            UnitUIProxy proxy = (UnitUIProxy) i.next();
            if (proxy instanceof VehicleUIProxy) vehicleProxies.add(proxy);
        }

        return sortProxies(vehicleProxies);
    }

    /** Sorts a collection of unit UI proxies by name.
     *  @param proxyCollection the collection to be sorted
     *  @return an iterator to a collection of sorted unit UI proxies
     */
    public Iterator sortProxies(Collection proxyCollection) {
        ArrayList sortedCollection = new ArrayList();
        Iterator outer = proxyCollection.iterator();
        while (outer.hasNext()) {
            outer.next();
            String leastName = "ZZZZZZZZZZZZZZZZZZZ";
            UnitUIProxy leastProxy = null;
            Iterator inner = proxyCollection.iterator();
            while (inner.hasNext()) {
                UnitUIProxy proxy = (UnitUIProxy) inner.next();
                String name = proxy.getUnit().getName();
                if ((name.compareTo(leastName) < 0) && !sortedCollection.contains(proxy)) {
                    leastName = name;
                    leastProxy = proxy;
                }
            }
            sortedCollection.add(leastProxy);
        }
        
        return sortedCollection.iterator();
    }
}
