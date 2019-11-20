// File: Herbivore.java from the package edu.colorado.simulations
// Complete documentation is available from the Herbivore link in
//   http://www.cs.colorado.edu/~main/docs /

package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;

/******************************************************************************
* A <CODE>Herbivore</CODE> is an <CODE>Animal</CODE> with extra methods that
* allow it to eat <CODE>Plant</CODE> objects.
*
* <b>Java Source Code for this class:</b>
*   <A HREF="../../../../edu/colorado/simulations/Herbivore.java">
*   http://www.cs.colorado.edu/~main/edu/colorado/simulations/Herbivore.java
*   </A>
*
* @author Michael Main 
*   <A HREF="mailto:main@colorado.edu"> (main@colorado.edu) </A>
*
* @version Feb 10, 2016
*
* @see Animal
* @see Plant
******************************************************************************/

public class Herbivore extends Animal implements Serializable {
	
	/** default serial id. */
    private static final long serialVersionUID = 1L;

   /**
   * Construct an <CODE>Herbivore</CODE> with a specified size, growth rate, and
   * eating need.
   * @param initSize
   *   the initial size of this <CODE>Herbivore</CODE>, in ounces
   * @param initRate
   *   the initial growth rate of this <CODE>Herbivore</CODE>, in ounces
   * @param initNeed
   *   the initial eating requirement of this <CODE>Animal</CODE>, in
   *   ounces per frame
   * <b>Precondition:</b>
   *   <CODE>initSize &gt;= 0</CODE> and <CODE>initNeed &gt;= 0</CODE>.
   *   Also, if <CODE>initSize</CODE> is zero, then
   *   <CODE>initRate</CODE> must also be zero.
   * <b>Postcondition:</b>
   *   This <CODE>Herbivore</CODE> has been initialized. The value returned from
   *   <CODE>getSize()</CODE> is now <CODE>initSize</CODE>, the value
   *   returned from <CODE>getRate()</CODE> is now <CODE>initRate</CODE>, and
   *   this <CODE>Herbivore</CODE> must eat at least <CODE>initNeed</CODE> ounces
   *   of food each frame to survive.
   * @exception IllegalArgumentException
   *   Indicates that <CODE>initSize</CODE>, <CODE>initRate</CODE>, or  
   *   <CODE>initNeed</CODE> violates the precondition.
   **/   
   public Herbivore(double initSize, double initRate, double initNeed)
   {
      super(initSize, initRate, initNeed);
   }
   
   
   /**
   * Have this <CODE>Herbivore</CODE> eat part of a <CODE>Plant</CODE>.
   * @param meal
   *   the <CODE>Plant</CODE> that will be partly eaten
   * <b>Postcondition:</b>
   *   Part of the <CODE>Plant</CODE> has been eaten by this <CODE>Herbivore</CODE>,
   *   by activating both <CODE>eat(amount)</CODE> and 
   *   <CODE>meal.nibbledOn(amount)</CODE>. The <CODE>amount</CODE> is usually
   *   half of the <CODE>Plant</CODE>, but it will not be more than 10% of 
   *   this <CODE>Herbivore</CODE>�s weekly need nor more than the amount that 
   *   this <CODE>Herbivore</CODE> still needs to eat to survive this frame.
   **/
   public void nibble(Plant meal)
   {
      final double PORTION = 0.5; // Eat no more than this portion of plant
      final double MAX_FRACTION = 0.1; // Eat no more than this fraction of need
      double amount; // How many ounces of the plant will be eaten
      
      // Set amount to some portion of the plant, but no more than a given
      // maximum fraction of the total need, and no more than what the
      // herbivore still needs to eat this frame.
      amount = PORTION * meal.getSize( );
      if (amount > MAX_FRACTION * getNeed( ))
         amount = MAX_FRACTION * getNeed( );
      if (amount > stillNeed( ))
         amount = stillNeed( );

      // Eat the plant
      eat(amount);
      meal.nibbledOn(amount);
   }
   
}
