/**
 * Mars Simulation Project
 * PeopleXmlReader.java
 * @version 2.73 2001-12-14
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.*;
import com.microstar.xml.*;

/** The PeopleXmlReader class parses the people.xml XML file and
 *  creates person unit objects.
 */
class PeopleXmlReader extends MspXmlReader {

    // XML element types
    private static final int PEOPLE_LIST = 0;
    private static final int PERSON = 1;
    private static final int NAME = 2;
    private static final int SETTLEMENT = 3;
    private static final int SKILL = 4;
    private static final int SKILL_NAME = 5;
    private static final int SKILL_LEVEL = 6;
    private static final int ATTRIBUTE = 7;
    private static final int ATTRIBUTE_NAME = 8;
    private static final int ATTRIBUTE_LEVEL = 9;

    // Data members
    private int elementType; // The current element type being parsed
    private PersonCollection people; // The collection of created people
    private VirtualMars mars; // The virtual Mars instance
    private UnitManager manager; // The unit manager
    private String currentName; // The current person name parsed
    private Settlement currentSettlement; // The current settlement
    private Vector skills; // The collection of skills for the current person
    private String currentSkillName; // The current skill name
    private int currentSkillLevel; // The current skill level
    private HashMap attributes; // The collection of natural attributes for the current person
    private String currentAttributeName; // The current attribute name
    private int currentAttributeLevel; // The current attribute level

    /** Constructor
     *  @param manager the unit manager
     *  @param mars the virtual Mars instance
     */
    public PeopleXmlReader(UnitManager manager, VirtualMars mars) {
        super("conf/people.xml");

        this.manager = manager;
        this.mars = mars;
    }

    /** Returns the collection of people created from the XML file
     *  @return the collection of people
     */
    public PersonCollection getPeople() {
        return people;
    }

    /** Handle the start of an element by printing an event.
     *  @param name the name of the started element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) throws Exception {
        super.startElement(name);

        if (name.equals("PEOPLE_LIST")) {
            elementType = PEOPLE_LIST;
            people = new PersonCollection();
        }
        if (name.equals("PERSON")) {
            elementType = PERSON;
            currentName = "";
            currentSettlement = null;
            skills = new Vector();
            attributes = new HashMap();
        }
        if (name.equals("NAME")) {
            if (elementType == PERSON) elementType = NAME;
            else if (elementType == SKILL) elementType = SKILL_NAME;
            else if (elementType == ATTRIBUTE) elementType = ATTRIBUTE_NAME;
        }
        if (name.equals("SETTLEMENT")) elementType = SETTLEMENT;
        if (name.equals("SKILL")) {
            elementType = SKILL;
            currentSkillName = "";
            currentSkillLevel = 0;
        } 
        if (name.equals("ATTRIBUTE")) {
            elementType = ATTRIBUTE;
            currentAttributeName = "";
            currentAttributeLevel = 0;
        }
        if (name.equals("LEVEL")) {
            if (elementType == SKILL) elementType = SKILL_LEVEL;
            else if (elementType == ATTRIBUTE) elementType = ATTRIBUTE_LEVEL;
        }
    }

    /** Handle the end of an element by printing an event.
     *  @param name the name of the ending element
     *  @throws Exception throws an exception if there is an error
     *  @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) throws Exception {
        super.endElement(name);
     
        switch (elementType) {
            case NAME:
            case SETTLEMENT:
                elementType = PERSON;
                break;
            case SKILL:
                Skill skill = new Skill(currentSkillName);
                skill.setLevel(currentSkillLevel);
                skills.addElement(skill);
                elementType = PERSON;
                break;
            case SKILL_NAME:
            case SKILL_LEVEL:
                elementType = SKILL;
                break;
            case ATTRIBUTE:
                attributes.put(currentAttributeName, new Integer(currentAttributeLevel));
                elementType = PERSON;
                break;
            case ATTRIBUTE_NAME:
            case ATTRIBUTE_LEVEL: 
                elementType = ATTRIBUTE;
                break;
            case PERSON:
                Person person = createPerson();
                if (person != null) people.add(person);
                elementType = PEOPLE_LIST;
                break;
        }
    }

    /** Handle character data by printing an event.
     *  @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();

        switch (elementType) {
            case NAME:
                currentName = data;
                break;
            case SETTLEMENT:
                currentSettlement = manager.getSettlements().getSettlement(data);
                break;
            case SKILL_NAME:
                currentSkillName = data;
                break;
            case SKILL_LEVEL:
                currentSkillLevel = Integer.parseInt(data);
                break;
            case ATTRIBUTE_NAME:
                currentAttributeName = data;
                break;
            case ATTRIBUTE_LEVEL:
                currentAttributeLevel = Integer.parseInt(data);
                break;
        }
    }

    /** Creates a person object.
     *  @return a person or null if person could not be constructed.
     */
    private Person createPerson() {

        Person person = null;

        if (currentSettlement != null) {
            person = new Person(currentName, currentSettlement, mars);
        }
        else {
            try {
                person = new Person(currentName, mars, manager);
            }
            catch (Exception e) {}
        }
        if (person != null) {
            Iterator i = skills.iterator();
            while(i.hasNext()) {
                person.getSkillManager().addNewSkill((Skill) i.next());
            }

            i = attributes.keySet().iterator();
            NaturalAttributeManager attributeManager = person.getNaturalAttributeManager();
            while(i.hasNext()) {
                String attributeName = (String) i.next();
                int attributeLevel = ((Integer) attributes.get(attributeName)).intValue();
                attributeManager.setAttribute(attributeName, attributeLevel);
            } 
        }

        return person;
    }
}
