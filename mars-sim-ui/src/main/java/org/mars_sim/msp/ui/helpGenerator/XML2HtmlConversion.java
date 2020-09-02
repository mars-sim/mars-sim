/**
 * Mars Simulation Project
 * XML2HtmlConversion.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.helpGenerator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class XML2HtmlConversion {

	public XML2HtmlConversion() throws FileNotFoundException {
	    String xmlFile = this.getClass().getResource("/xml/buildings.xml").toExternalForm();
//	    Scanner scanner = new Scanner(new File(xmlFile)).useDelimiter("\\Z");
	    
	    File f = new File(xmlFile);
	    Scanner scanner = new Scanner(f);
	    scanner.useDelimiter("\\Z");
	    
	    String xmlContent = scanner.next();
	    xmlContent = xmlContent.trim().replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\n", "<br />").replaceAll(" ", "&nbsp;");
	    PrintWriter out = new PrintWriter(xmlFile+".html");
	    out.println("<html><body>" + xmlContent + "</body></html>");
	    scanner.close();
	    out.close();
	}

	public static void main(String[] args) throws Exception {
		new XML2HtmlConversion();
	}

}
