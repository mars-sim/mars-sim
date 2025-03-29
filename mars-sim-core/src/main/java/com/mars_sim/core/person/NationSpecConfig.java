/*
 * Mars Simulation Project
 * NationSpecConfig.java
 * @date 2023-07-23
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.Nation;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.configuration.UserConfigurableConfig;

/**
 * Configuration class for loading the attributes of each country 
 * as well as loading person naming schemes that's unique to each country.
 */
public class NationSpecConfig extends UserConfigurableConfig<NationSpec> {

    private static final String COUNTRY_XSD = "country.xsd";
    
    private static final String COUNTRY = "country";

    private static final String ECONOMICS_DATA = "economics-data";

    private static final String LAST_NAME_LIST = "last-name-list";
    private static final String FIRST_NAME_LIST = "first-name-list";	
    private static final String MALE = "male";
    private static final String FEMALE = "female";
    private static final String LAST_NAME = "last-name";
    private static final String FIRST_NAME = "first-name";
    private static final String GENDER = "gender";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String POPULATION_CHARS = "characteristics";

    // Note: each of the predefined country below has a xml file
    private static final String[] COUNTRIES = {
                        "Austria",  "Belgium", "Brazil", 
                        "Canada", "China", "Czech Republic",
                        "Denmark", "Estonia", "Finland", "France", 
                        "Germany", "Greece",
                        "Hungary", "India", "Ireland", "Italy", 
                        "Japan", "Luxembourg",
                        "Netherlands", "Norway", 
                        "Poland", "Portugal", 
                        "Romania", "Russia",
                        "Saudi Arabia",
                        "South Korea", "Spain", "Sweden", "Switzerland", 
                        "United Arab Emirates",
                        "United Kingdom", "United States"};


    private static List<Nation> nations = new ArrayList<>();
    
    public NationSpecConfig(SimulationConfig config) {
        super(COUNTRY);

        setXSDName(COUNTRY_XSD, config);

        for (String name: COUNTRIES) {
        	Nation nation = new Nation(name);
        	nations.add(nation);
        }
        
        loadDefaults(COUNTRIES);  
    }

    @Override
    protected Document createItemDoc(NationSpec item) {
        throw new UnsupportedOperationException("Unimplemented method 'createItemDoc'");
    }

    @Override
    protected NationSpec parseItemXML(Document doc, boolean predefined) {

		Element countryElement = doc.getRootElement();

		String country = countryElement.getAttributeValue(NAME);
		NationSpec result = new NationSpec(country, predefined);

        Element popEl = countryElement.getChild(POPULATION_CHARS);
        if (popEl != null) {
            var p = ConfigHelper.parsePopulation(popEl);
            result.setPeople(p);
        }


		// Scan the economic data
		double []data = new double[4];
		
		Element econEl = countryElement.getChild(ECONOMICS_DATA);
        List<Element> nodes = econEl.getChildren();
		int size = nodes.size();
	
		for (int i = 0; i < size; i++) {
			Element element = nodes.get(i);
			String str = element.getAttributeValue(VALUE).trim().replace(",", "");
			data[i] = Double.parseDouble(str);
		}
		
		result.addData(data[0], data[1], data[2], data[3]);
		
        // Scan first names
        Element firstNameEl = countryElement.getChild(FIRST_NAME_LIST);
        List<Element> firstNamesList = firstNameEl.getChildren(FIRST_NAME);
        for (Element nameElement : firstNamesList) {

            String gender = nameElement.getAttributeValue(GENDER);
            String name = nameElement.getAttributeValue(VALUE);

            if (gender.equalsIgnoreCase(MALE)) {
                result.addMaleName(name);
            } else if (gender.equalsIgnoreCase(FEMALE)) {
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
    
    /**
     * Gets a list of nations.
     * 
     * @return
     */
    public final List<Nation> getNations() {
    	return nations;
    }
    
    public final String[] getCountries() {
    	return COUNTRIES;
    }
    
    /**
     * Gets the nation with a particular name.
     * 
     * @param name
     * @return
     */
    public static Nation getNation(String name) {
    	for (Nation n: nations) {
    		if (n.getName().equalsIgnoreCase(name)) {
    			return n;
    		}
    	}
    	return null;
    }
    
}
