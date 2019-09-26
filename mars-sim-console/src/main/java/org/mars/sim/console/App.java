package org.mars.sim.console;

import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class App 
{
	

	public final static String SPACES_2 = "  ";
	public final static String SPACES_7 = "       ";
	
	public static String[][] SETTLEMENT_KEYS;
	
	static {
		SETTLEMENT_KEYS = new String[][] {
			{"weather", 		"the meteorological data from the weather station"},
			{"people",  		"who are associated with this settlement"},
//			"settler", 
//			"person", 
			{"robot", 			"what robots have been assigned to this settlement"},
//			"bot", 
			{"proposal", 		"what proposal have been represented"},
			{"vehicle range", 	"what are the range of the vehicles we have"},
//			{"dash", 			"what is on the settlement dashboard"},
			{"dashboard", 		"what is on the settlement dashboard"},
			{"repair", 			"what is the level of the repair effort"},
			{"maintenance", 	"what is the level of the maintenance effort"},
//			{"evasuit", 
			{"eva suit",		"what is the level of the EVA Suit manufacturing and maintenance effort"},
			{"mission plan", 	"a list of proposed mission plans"},
			{"mission now",		"what are the currently on-going missions"}, 
			{"objective", 		"what is the settlement's objective"},
			{"water", 			"how is the water production and consumption"},
//			"o2", 
			{"oxygen", 			"how is the oxgyen production and consumption"},
//			"co2", 
			{"carbon dioxide", 	"how is the carbon dioxide production and consumption"},
			{"job roster",		"what job does each settler have"},
			{"job demand", 		"what do the job demand, position filled and the deficit look like in this settlement"},
			{"job prospect", 	"what is the job prospect of a particular job in this settlement"},
			{"bed", 			"what is quarters arrangement in this settlement"},
			{"social", 			"how is this settlement doing socially"},
			{"science", 		"how is the science score in this settlement"},
			{"researchers", 	"who are conducting reseach in this settlement"}
		};
	};
			
    public static void main( String[] args ) {
//        System.out.println("SETTLEMENT_KEYS.length : " + SETTLEMENT_KEYS.length);
//		System.out.println("SETTLEMENT_KEYS[0].length : " + SETTLEMENT_KEYS[0].length); 
//		System.out.println(Arrays.deepToString(SETTLEMENT_KEYS));
    	
//    	SETTLEMENT_KEYS.length : 22
//    	SETTLEMENT_KEYS[0].length : 2
//		
		// To get a column:
//		int colToGet = 0;
//		String[] oCol = Arrays.stream(SETTLEMENT_KEYS).map(o -> o[colToGet]).toArray(String[]::new);
//		System.out.println(Arrays.deepToString(oCol));
		
		System.out.println(Arrays.deepToString(getColumn(SETTLEMENT_KEYS, 0))); // [weather, people, robot, proposal, vehicle range, dashboard,
		
		System.out.println(getKeywordList(SETTLEMENT_KEYS)); 
		
//		System.out.println(getKeywordList(SETTLEMENT_KEYS));
	}
    
    public static String[] getColumn(String[][] array, int colToGet) {
    	return Arrays.stream(array).map(o -> o[colToGet]).toArray(String[]::new);
    }
    
    /**
	 * Returns a list of keywords
	 * 
	 * @param keywords
	 * @return list
	 */
	public static String getKeywordList(String[][] keywords) {
		StringBuffer s = new StringBuffer();
		int rows = keywords.length;
		int cols = keywords[0].length;
		
		for (int i = 0; i < rows; i++) {
//			for (int j = 0; j < cols; j++) {
				s.append(SPACES_2 + keywords[i][0] + System.lineSeparator());
				s.append(SPACES_7 + keywords[i][1] + System.lineSeparator());
//			}
		}
		
//		Arrays.sort(keywords);
//		return Arrays.deepToString(keywords);
		
		return s.toString();
	}
}
