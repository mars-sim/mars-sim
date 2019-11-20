// File: Animal.java from the package edu.colorado.simulations
// Complete documentation is available from the Animal link in
//   http://www.cs.colorado.edu/~main/docs/

package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;

/******************************************************************************
* An <CODE>Animal</CODE> is an <CODE>Organism</CODE> with extra methods that
* deal with eating.
*
* <b>Java Source Code for this class:</b>
*   <A HREF="../../../../edu/colorado/simulations/Animal.java">
*   http://www.cs.colorado.edu/~main/edu/colorado/simulations/Animal.java
*   </A>
*
* @author Michael Main 
*   <A HREF="mailto:main@colorado.edu"> (main@colorado.edu) </A>
*
* @version Feb 10, 2016
*
* @see Organism
* @see Herbivore
* @see Plant
******************************************************************************/
public class Animal extends Organism implements Serializable {
	
	/** default serial id. */
    private static final long serialVersionUID = 1L;
   // The period of time a fish can survive without eating
   public static final double ONE_SOL = 1000;
   private double needEachFrame;  // Amount of food needed (in ounces per frame)
   private double eatenThisFrame; // Ounces of food eaten so far this frame
   private double totalTime; // The cumulative amount of time

   /**
   * Construct an <CODE>Animal</CODE> with a specified size, growth rate, and
   * eating need.
   * @param initSize
   *   the initial size of this <CODE>Animal</CODE>, in ounces
   * @param initRate
   *   the initial growth rate of this <CODE>Animal</CODE>, in ounces
   * @param initNeed
   *   the initial eating requirement of this <CODE>Animal</CODE>, in
   *   ounces per frame
   * <b>Precondition:</b>
   *   <CODE>initSize &gt;= 0</CODE> and <CODE>initNeed &gt;= 0</CODE>.
   *   Also, if <CODE>initSize</CODE> is zero, then
   *   <CODE>initRate</CODE> must also be zero.
   * <b>Postcondition:</b>
   *   This <CODE>Animal</CODE> has been initialized. The value returned from
   *   <CODE>getSize()</CODE> is now <CODE>initSize</CODE>, the value
   *   returned from <CODE>getRate()</CODE> is now <CODE>initRate</CODE>, and
   *   this <CODE>Animal</CODE> must eat at least <CODE>initNeed</CODE> ounces
   *   of food each frame to survive.
   * @exception IllegalArgumentException
   *   Indicates that <CODE>initSize</CODE>, <CODE>initRate</CODE>, or  
   *   <CODE>initNeed</CODE> violates the precondition.
   **/   
   public Animal(double initSize, double initRate, double initNeed)
   {
      super(initSize, initRate);
      if (initNeed < 0)
         throw new IllegalArgumentException("initNeed is negative");
      needEachFrame = initNeed;
      // eatenThisFrame will be given its default value of zero.
   }

   
   /**
   *  Have this <CODE>Animal</CODE> eat a given amount of food.
   *  @param amount
   *    the amount of food for this <CODE>Animal</CODE> to eat (in ounces)
   *  <b>Precondition:</b>
   *    <CODE>amount &gt;= 0.</CODE>
   *  <b>Postcondition:</b>
   *    The given amount (in ounces) has been added to the amount of food that
   *    this <CODE>Animal</CODE> has eaten this frame.
   *  throw IllegalArgumentException
   *    Indicates that <CODE>amount</CODE> is negative.
   **/
   public void eat(double amount)
   {
       if(amount < 0)
         throw new IllegalArgumentException("amount is negative");
       eatenThisFrame += amount;
   }


   /**
   * Determine the amount of food that this <CODE>Animal</CODE> needs each
   * frame.
   * @return
   *   the total amount of food that this <CODE>Animal</CODE> needs to survive
   *   one frame (measured in ounces)
   **/
   public double getNeed( )   
   {
      return needEachFrame;
   }

   
   /**
   * Set the current growth food requirement of this <CODE>Animal</CODE>.
   * @param newNeed
   *   the new food requirement for this <CODE>Animal</CODE> (in ounces)
   * <b>Precondition:</b>
   *   <CODE>newNeed &gt;= 0.</CODE>
   * <b>Postcondition:</b>
   *   The food requirement for this <CODE>Animal</CODE> has been set to
   *   <CODE>newNeed</CODE>.
   * @exception IllegalArgumentException
   *   Indicates that <CODE>newNeed</CODE> is negative.
   **/
   public void setNeed(double newNeed)
   {
       if(newNeed < 0)
         throw new IllegalArgumentException("newNeed is negative");
       needEachFrame = newNeed;
   }
   
   
   /**
   * Simulate the passage in the life of this <CODE>Animal</CODE>.
   * <b>Postcondition:</b>
   *   The size of this <CODE>Animal</CODE> has been changed by its current 
   *   growth rate. If the new size is less than or equal to zero, then 
   *   <CODE>expire</CODE> is activated to set both size and growth rate to 
   *   zero. Also, if this <CODE>Animal</CODE> has eaten less than its need 
   *   over the past frame, then <CODE>expire</CODE> has been activated.
   **/
   public void growPerFrame(double time) 
   {
      super.growPerFrame( );
      totalTime += time;
      // For each day
      if (totalTime > ONE_SOL && eatenThisFrame < needEachFrame) {
         totalTime = totalTime - ONE_SOL;
    	 expire( );
         eatenThisFrame = 0;
      }
 
   }


   /**
   * Determine the amount of food that this <CODE>Animal</CODE> still needs to
   * survive this frame.
   * @return
   *   the ounces of food that this <CODE>Animal</CODE> still needs to survive 
   *   this frame (which is the total need minus the amount eaten so far).
   **/
   public double stillNeed( )
   {
      if (eatenThisFrame >= needEachFrame)
         return 0;
      else
         return needEachFrame - eatenThisFrame;
   }
   
}
