package org.mars_sim.msp.core.tool;


public class ArrayTest {
	private static int[][] missionModifiers // = new int[2][3];
	
//	missionModifiers 
	= {
		{0, 1},
		{2, 3},
		{4, 5},
	};
	
	public static void main(String[] args) {
		System.out.println(missionModifiers);
		System.out.println("missionModifiers[] " + missionModifiers[1]);
		System.out.println("missionModifiers[] " + missionModifiers[2][1]);
		System.out.println("missionModifiers.length " + missionModifiers.length);		
		System.out.println("missionModifiers[0].length " + missionModifiers[0].length);
//		array_name.length // row
//		array_name[0].length // col
		
	}
}
