/**
 * Mars Simulation Project
 * SlideListener.java
 * @version 3.1.0 2016-11-24
 * @author Manny Kung
 */

// Adapted from http://www.codeproject.com/Articles/565425/Sliding-Panel-in-Java
// Original author : Shubhashish_Mandal, 22 Mar 2013

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mars_sim.msp.ui.swing.sidepanel;

/**
 *
 * @author root
 */
public interface StateListener {
   public void toggleState() ;
   public void reset() ;

}
