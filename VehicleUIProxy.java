/**
 * Mars Simulation Project
 * VehicleUIProxy.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

/**
 * Abstract user interface proxy for a vehicle. 
 */
 
public abstract class VehicleUIProxy extends UnitUIProxy {

    public VehicleUIProxy(Vehicle vehicle, UIProxyManager proxyManager) {
        super(vehicle, proxyManager);
    }
}
