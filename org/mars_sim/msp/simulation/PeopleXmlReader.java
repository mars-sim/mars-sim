/**
 * Mars Simulation Project
 * PeopleXmlReader.java
 * @version 2.73 2001-11-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.*;
import java.util.*;
import com.microstar.xml.*;

class PeopleXmlReader extends MspXmlReader {

    private static int PERSON_LIST = 0;
    private static int PERSON = 1;
    private static int PERSON_NAME = 2;
    private static int SETTLEMENT = 3;
    private static int SKILL = 4;
    private static int SKILL_NAME = 5;
    private static int SKILL_LEVEL = 6;

    private int elementType;

    private Vector people;
    private VirtualMars mars;
    private UnitManager manager;
    private String currentName;
    private String currentSettlement;
    private Vector skills;
    private String currentSkillName;
    private int currentSkillLevel;

    public PeopleXmlReader(UnitManager manager, VirtualMars mars) {
        super("conf/people.xml");

        this.manager = manager;
        this.mars = mars;
    }

    public Vector getPeople() {
        return people;
    }

    /**
     * Handle the start of an element by printing an event.
     * @see com.microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) {
        super.startElement(name);

        if (name.equals("PEOPLE_LIST")) {
            elementType = PERSON_LIST;
            people = new Vector();
        }
        if (name.equals("PERSON")) {
            elementType = PERSON;
            currentName = "";
            currentSettlement = "";
            skills = new Vector();
        }
        if (name.equals("SKILL")) {
            elementType = SKILL;
            currentSkillName = "";
            currentSkillLevel = 0;
        } 
        if (name.equals("NAME")) {
            if (elementType == PERSON) elementType = PERSON_NAME;
            if (elementType == SKILL) elementType = SKILL_NAME;
        }
        if (name.equals("SETTLEMENT")) elementType = SETTLEMENT;
        if (name.equals("LEVEL")) elementType = SKILL_LEVEL;
    }

    /**
     * Handle the end of an element by printing an event.
     * @see com.microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) {
        super.endElement(name);
      
        if (elementType == PERSON_NAME) {
            elementType = PERSON;
            return;
        }
        if (elementType == SETTLEMENT) {
            elementType = PERSON;
            return;
        }
        if (elementType == SKILL) {
            elementType = PERSON;
            Skill skill = new Skill(currentSkillName);
            skill.setLevel(currentSkillLevel);
            skills.addElement(skill);
            return;
        }
        if (elementType == SKILL_NAME) {
            elementType = SKILL;
            return;
        }
        if (elementType == SKILL_LEVEL) {
            elementType = SKILL;
            return;
        }
        if (elementType == PERSON) {
            elementType = PERSON_LIST;
            Settlement settlement = manager.getSettlement(currentSettlement);
            Person currentPerson = new Person(currentName, settlement.getCoordinates(), mars);
            currentPerson.setSettlement(settlement);
            for (int x=0; x < skills.size(); x++) {
                Skill skill = (Skill) skills.elementAt(x);
                currentPerson.getSkillManager().addNewSkill(skill);
            }
            people.addElement(currentPerson);
            return;
        }
    }

    /**
     * Handle character data by printing an event.
     * @see com.microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        super.charData(ch, start, length);

        String data = new String(ch, start, length).trim();

        if (elementType == PERSON_NAME) currentName = data;
        if (elementType == SETTLEMENT) currentSettlement = data;
        if (elementType == SKILL_NAME) currentSkillName = data;
        if (elementType == SKILL_LEVEL) currentSkillLevel = Integer.parseInt(data);
    }
}

