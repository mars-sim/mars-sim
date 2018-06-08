package org.mars_sim.msp.core.test;

public class CoordinatesTransformationTest {

    public static void main(String [] args) {

    	String ss[] = new String[] {"16.4 E", "16.0 E", "5.0 E", "5.4 E", "16. E", "16 E", "5. E", "5 E"};

    	for (String s : ss) {
	    	System.out.print(s);
	
	    	s = s.replaceAll("[n]+", "N").replaceAll("[s]+", "S")
					.replaceAll("[e]+", "E").replaceAll("[w]+", "W")
		    		// remove multiple dot to one single dot
					.replaceAll("[\\.]+", ".")
					// remove a dot followed by a whitespace and a direction with just the direction
					.replaceAll("\\.\\s", ".0 ")
					//replaceAll(". W", " W").replaceAll(". N", " N").replaceAll(". S", " S")
					// remove all whitespace 
					.replaceAll("[ ]+", "")
					// insert a whitespace right before the directional notation
					.replaceAll("E", " E").replaceAll("W", " W").replaceAll("N", " N").replaceAll("S", " S")
					// remove multiple comma to one single comma
					.replaceAll("[\\,]+", ",")
					//.replaceAll("[\\`\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\+\\-\\=\\{\\}\\|\\;\\'\\:\\,\\/\\<\\>\\?]+", "");
					//.replaceAll("[`~!@#$%^&*()_+-={}|;':,/<>?]+", "");
		    		// remove all underscores
					.replaceAll("[\\_]+", "")
		    		// remove everything except for dot, letters, digits, underscores and whitespace.
					.replaceAll("[^\\w\\s&&[^.]&&[^,]]+", "");
	    	
	    	
	    	System.out.println("  -->  " + s);
    	}
    	//for (int i= 0; i < 100; i++) {
        //	double rand = RandomUtil.getGaussianDouble();
    	//	System.out.println(rand);
    	//}
    }
	
}
