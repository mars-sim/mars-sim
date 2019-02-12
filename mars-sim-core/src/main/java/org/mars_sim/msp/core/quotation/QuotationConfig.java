/**
 * Mars Simulation Project
 * quotationConfig.java
 * @version 3.1.0 2017-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.quotation;

import org.jdom2.Document;
import org.jdom2.Element;

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads from quotations.xml
 */
public class QuotationConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	// Element names
	private static final String QUOTATION_LIST = "quotation-list";
	private static final String QUOTATION = "quotation";
	private static final String TEXT = "text";
	private static final String NAME = "name";

	private int count = 0;

	private Document quotationDoc;
	private Map<Integer, Quotation> quotations;

	/**
	 * Constructor
	 * 
	 * @param quotationDoc DOM document
	 */
	public QuotationConfig(Document quotationDoc) {
		this.quotationDoc = quotationDoc;
	}

	/**
	 * Gets a map of quotations.
	 * 
	 * @return map of quotations
	 * @throws Exception when quotations can not be parsed.
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer, Quotation> getQuotations() {

		if (quotations == null) {
			quotations = new HashMap<>();

			Element root = quotationDoc.getRootElement();
			List<Element> q_list = root.getChildren(QUOTATION);

			for (Element q : q_list) {

				String name = q.getAttributeValue(NAME);
				String text = q.getAttributeValue(TEXT);

				// String str = "'" + text + "' -" + name;

				// Get a new id.
				int id = count++;

				Quotation quote = new Quotation(name, text);
				// Add a quote.
				quotations.put(id, quote);
				// quotations.put(text, name);
			}
		}

		return quotations;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		quotationDoc = null;
		if (quotations != null) {

			quotations.clear();
			quotations = null;
		}
	}
}