/**
 * Mars Simulation Project
 * MasterClock.java
 * @version 2.72 2001-02-18
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The MasterClock represents the simulated time clock on virtual
 *  Mars. Virtual Mars has only one master clock. The master clock
 *  delivers a clock pulse the virtual Mars every second or so, which
 *  represents 10 minutes of simulated time.  All actions taken with
 *  virtual Mars and its units are synchronized with this clock pulse.
 *
 *  Note: Later the master clock will control calendaring information
 *  as well, so Martian calendars and clocks can be displayed.
 */
class MasterClock extends Thread {

    // Data members
    private VirtualMars mars;     // Virtual Mars
    private EarthClock earthTime; // Earth Clock

    /** sleep duration in milliseconds */
    private final static int SLEEP_DURATION = 1000;

    /** Constructs a MasterClock object
     *  @param mars the virtual mars that uses the clock
     */
    public MasterClock(VirtualMars mars) {
        // Initialize data members
        this.mars = mars;
	
        // Create an Earth clock
        earthTime = new EarthClock();

        System.out.println("Earth Time (GMT): " + earthTime.getDateTimeString());
    }

    /** Run clock */
    public void run() {
        // Endless clock pulse loop
        while (true) {
            try {
                sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {}

            // Send virtual Mars a clock pulse representing 10 minutes (600 seconds)
            mars.clockPulse(600);

            // Add ten minutes to Earth clock
            earthTime.addTenMinutes();

            System.out.println("Earth Time (GMT): " + earthTime.getDateTimeString());
        }
    }
}

