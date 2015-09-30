package org.mars_sim.msp.helpGenerator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Conversion {

	public Conversion() throws FileNotFoundException {
	    String xmlFile = this.getClass().getResource("/docs/buildings.xml").toExternalForm();
	    Scanner scanner = new Scanner(new File(xmlFile)).useDelimiter("\\Z");
	    String xmlContent = scanner.next();
	    xmlContent = xmlContent.trim().replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\n", "<br />").replaceAll(" ", "&nbsp;");
	    PrintWriter out = new PrintWriter(xmlFile+".html");
	    out.println("<html><body>" + xmlContent + "</body></html>");
	    scanner.close();
	    out.close();
	}

	public static void main(String[] args) throws Exception {
		new Conversion();
	}

}
