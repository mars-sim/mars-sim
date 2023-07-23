/*
 * Mars Simulation Project
 * PersonNameSpecConfig.java
 * @date 2023-07-23
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.configuration.UserConfigurableConfig;

/**
 * Configurationclass to load person naming schemes. Usually based on country
 */
public class PersonNameSpecConfig extends UserConfigurableConfig<PersonNameSpec> {

	private static final String LAST_NAME_LIST = "last-name-list";
	private static final String FIRST_NAME_LIST = "first-name-list";
	private static final String LAST_NAME = "last-name";
	private static final String FIRST_NAME = "first-name";
	private static final String GENDER = "gender";
    private static final String NAME = "name";
    private static final String VALUE = "value";

    // These are teh countries predefined
    private String[] COUNTRIES = {"Austria",  "Belgium", "Brazil", "Canada", "China", "Czech Republic",
                        "Denmark", "Estonia", "Finland", "France", "Germany", "Greece",
                        "Hungary", "India", "Ireland", "Italy", "Japan", "Luxembourg",
                        "Norway", "Poland", "Portugal", "Romania", "Russia",
                        "South Korea", "Spain", "Sweden", "Switzerland", "The Netherlands",
                        "UK", "USA"};

    public PersonNameSpecConfig() {
        super("country");

        setXSDName("country.xsd");

        loadDefaults(COUNTRIES);
    }

    @Override
    protected Document createItemDoc(PersonNameSpec item) {
        throw new UnsupportedOperationException("Unimplemented method 'createItemDoc'");
    }

    @Override
    protected PersonNameSpec parseItemXML(Document doc, boolean predefined) {

		Element countryElement = doc.getRootElement();

		String country = countryElement.getAttributeValue(NAME);
		PersonNameSpec result = new PersonNameSpec(country, predefined);

        // Scan first names
        Element firstNameEl = countryElement.getChild(FIRST_NAME_LIST);
        List<Element> firstNamesList = firstNameEl.getChildren(FIRST_NAME);
        for (Element nameElement : firstNamesList) {

            String gender = nameElement.getAttributeValue(GENDER);
            String name = nameElement.getAttributeValue(VALUE);

            if (gender.equals("male")) {
                result.addMaleName(name);
            } else if (gender.equals("female")) {
                result.addFemaleName(name);
            }
        }

        // Scan last names
        Element lastNameEl = countryElement.getChild(LAST_NAME_LIST);
        List<Element> lastNamesList = lastNameEl.getChildren(LAST_NAME);
        for (Element nameElement : lastNamesList) {
            result.addLastName(nameElement.getAttributeValue(VALUE));
        }

        return result;
    }
}
