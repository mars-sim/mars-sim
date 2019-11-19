package org.mars_sim.msp.core.structure.building.function.farming;

//FILE: Pondlife.java
//A simple simulation program to model the fish and weeds in a pond

//import edu.colorado.simulations.*; // Provides Organism, Plant, Herbivore classes
import java.util.Vector;

import org.mars_sim.msp.core.tool.RandomUtil;

/******************************************************************************
* The <CODE>PondLife</CODE> Java application runs a simple simulation that
* models the fish and weeds in a pond.
*
* <P>The simulation is currently set up to use these values:
* <UL>
* <LI> Number of weeds in the pond: 2000
* <LI> Initial size of each weed: 15 ounces 
* <LI> Growth rate of weeds: 2.5 ounces/week  
* <LI> Initial number of fish in the pond: 300
* <LI> Fish size: 50 ounces 
* <LI> A fish must eat 0.5 times its size during one week, or it will die.
* <LI> Average number of weeds nibbled by a fish over a week: 30 
* <LI> At the end of each week, some fish have babies. The total number of new
*        fish born is the current number of fish times the 0.05 
*        (rounded down to an integer).
* <LI> Number of weeks to simulate: 38
* </UL>                   
*
* <p><b>Java Source Code for this class:</b>
*   <A HREF="../applications/PondLife.java">
*   http://www.cs.colorado.edu/~main/applications/PondLife.java
*   </A>
*
* @author Michael Main 
*   <A HREF="mailto:main@colorado.edu"> (main@colorado.edu) </A>
*
* @version Feb 10, 2016
*
* @see edu.colorado.simulations.Organism
* @see edu.colorado.simulations.Plant
* @see edu.colorado.simulations.Herbivore
******************************************************************************/
public class PondLife {
	
	// Convert from kg to ounce
	public static final double KG_PER_OUNCE = 0.02834952;
	
	// Convert from ounce to kg
	public static final double OUNCE_PER_KG = 35.27396195;
	
	// Number of weeds in the pond
	public static final int MANY_WEEDS = 2000;
	
	// Initial size of each weed, in ounces 
	public static final double WEED_SIZE = 15;
	
	// Growth rate of weeds, in ounces/week  
	public static final double WEED_RATE = 2.5; 
	
	// Initial number of fish in the pond 
	public static final int INIT_FISH = 300;
	
	// Fish size, in ounces 
	public static final double FISH_SIZE = 50; 
	
	// A fish must eat FRACTION times its size during one week, or it will die.
	public static final double FRACTION = 0.5;
	
	// Average number of weeds nibbled by a fish over a week 
	public static final int AVERAGE_NIBBLES = 30; 
	
	// At the end of each week, some fish have babies. The total number of new
	// fish born is the current number of fish times the BIRTH_RATE 
	// (rounded down to an integer).
	public static final double BIRTH_RATE = 0.05;
	
	// Number of weeks to simulate
	public static final int MANY_WEEKS  = 500; 
	
	
	/**
	* Run the simulation, using the values indicated in the documentation.
	* @param args
	*   not used in this implementation
	**/                                                    
	public static void main(String[ ] args)   
	{
	    int numFish = (int)(RandomUtil.getRandomDouble(1.0) * INIT_FISH);
	    int numWeeds = (int)(RandomUtil.getRandomDouble(1.0) * MANY_WEEDS);
	    
		Vector<Herbivore> fish = new Vector<Herbivore>(numFish);   // A Vector of our fish
	    Vector<Plant> weeds = new Vector<Plant>(numWeeds); // A Vector of our weeds
	    int i;                                 // Loop control variable
	
	    		
	    // Initialize the bags of fish and weeds
	    for (i = 0; i < numFish; i++)
	       fish.addElement(new Herbivore(FISH_SIZE, 0, FISH_SIZE * FRACTION));
	    for (i = 0; i < numWeeds; i++)
	       weeds.addElement(new Plant(WEED_SIZE, WEED_RATE));
	
	    System.out.println("Week \tNumber \tPlant Mass");
	    System.out.println("     \tof     \t(in kg)");
	    System.out.println("     \tFish");
	
	    // Simulate the weeks
	    for (i = 1; i <= MANY_WEEKS; i++)
	    {
	       pondWeek(fish, weeds);
	       System.out.print(i + "\t");
	       System.out.print(fish.size( ) + "\t");
	       System.out.print(Math.round(totalMass(weeds)/ OUNCE_PER_KG * 100.0)/100.0 + "\n");
	    }
	}
	
	
	/**
	* Simulate one week of life in the pond, using the values indicated in the
	* documentation.
	* @param fish
	*   Vector of fish that are in the pond at the start of the week
	* @param weeds
	*   Vector of weeds that are in the pond at the start of the week
	**/
	public static void pondWeek(Vector<Herbivore> fish, Vector<Plant> weeds)
	{
	   int i;
	   int manyIterations;
	   int index;
	   Herbivore nextFish;
	   Plant nextWeed;
	
	   // Have randomly selected fish nibble on randomly selected plants
	   manyIterations = AVERAGE_NIBBLES * fish.size( );
	   for (i = 0; i < manyIterations; i++)
	   {
	      index = (int) (RandomUtil.getRandomDouble(1.0) * fish.size( ));
	      nextFish = fish.elementAt(index);
	      index = (int) (RandomUtil.getRandomDouble(1.0) * weeds.size( ));
	      nextWeed = weeds.elementAt(index);
	      nextFish.nibble(nextWeed);
	   }
	
	   // Simulate the weeks for the fish
	   i = 0;
	   while (i < fish.size( ))
	   {
	      nextFish = fish.elementAt(i);
	      nextFish.growPerFrame( );
	      if (nextFish.isAlive( ))
	         i++;
	      else
	         fish.removeElementAt(i);
	   }
	
	   // Simulate the weeks for the weeds
	   for (i = 0; i <weeds.size( ); i++)
	   {
	      nextWeed = weeds.elementAt(i);
	      nextWeed.growPerFrame( );
	   }
	
	   // Create some new fish, according to the BIRTH_RATE constant
	   manyIterations = (int) (BIRTH_RATE * fish.size() * RandomUtil.getRandomDouble(2.0));
	   for (i = 0; i < manyIterations; i++)
	       fish.addElement(new Herbivore(FISH_SIZE, 0, FISH_SIZE * FRACTION));
	}
	
	
	/**
	* Calculate the total mass of a collection of <CODE>Organism</CODE>s.
	* @param organisms
	*   a <CODE>Vector</CODE> of <CODE>Organism</CODE> objects
	* @param <T>
	*   component type of the elements in the organisms Vector
	* <b>Precondition:</b>
	*   Every object in <CODE>organisms</CODE> is an <CODE>Organism</CODE>.
	* @return
	*   the total mass of all the objects in <CODE>Organism</CODE> (in ounces).
	**/
	public static <T extends Organism> double totalMass(Vector<T> organisms)
	{
	   double answer = 0;
	   
	   for (Organism next : organisms)
	   {
	      if (next != null)
	         answer += next.getSize( );
	   }
	   return answer;
	}

}