package com.mars_sim.core.person;

import java.util.EnumMap;
import java.util.Map;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;


public class PersonBuilderTest extends AbstractMarsSimUnitTest {
    private static final String TEST_NAME = "test";
    private static final String TEST_COUNTRY = "Norway";
    private static final String TEST_RA = "NASA";

    public void testBasic() {
        var home = buildSettlement();
   
        Person p = Person.create(TEST_NAME, home, GenderType.MALE)
        		.build();

        assertNotNull("Person created", p);
        assertEquals("Person name", TEST_NAME, p.getName());
        assertEquals("Person gender", GenderType.MALE, p.getGender());
        assertTrue("Person minimum age", p.getAge() > 20);
        assertTrue("Person can carry at least EVA", p.getCarryingCapacity() > EVASuit.getEmptyMass());
    }

    public void testOptional() {
        var home = buildSettlement();

        AuthorityFactory af = getConfig().getReportingAuthorityFactory();
        var ra = af.getItem(TEST_RA);
        
        	    
		var country = new NationSpecConfig(getConfig()).getItem("Norway");
        
        Person p = Person.create(TEST_NAME, home, GenderType.FEMALE)
                        .setAge(30)
                        .setCountry(country)
                        .setSponsor(ra).build();

        assertNotNull("Person created", p);
        assertEquals("Person name", TEST_NAME, p.getName());
        assertEquals("Person gender", GenderType.FEMALE, p.getGender());
        assertEquals("Person country", TEST_COUNTRY, p.getCountry());
        assertEquals("Fixed age", 30, p.getAge());
        assertEquals("Person Sponsor", TEST_RA, p.getReportingAuthority().getName());
    }

    public void testAttributes() {
        var home = buildSettlement();
        
        Map<NaturalAttributeType, Integer> attrs = new EnumMap<>(NaturalAttributeType.class);
        attrs.put(NaturalAttributeType.AGILITY, 20);
        attrs.put(NaturalAttributeType.DISCIPLINE, 44);
        attrs.put(NaturalAttributeType.SPIRITUALITY, 78);

        Person p = Person.create(TEST_NAME, home, GenderType.MALE)
        		.setAttribute(attrs)
        		.build();
        
        assertEquals("Predefined attributes", attrs,
                        p.getNaturalAttributeManager().getAttributeMap());

    }
    
    public void testSkills() {
        var home = buildSettlement();

        Map<SkillType, Integer> attrs = new EnumMap<>(SkillType.class);
        attrs.put(SkillType.ASTROBIOLOGY, 20);
        attrs.put(SkillType.MECHANICS, 44);
        attrs.put(SkillType.MATHEMATICS, 78);

        Person p = Person.create(TEST_NAME, home, GenderType.MALE)
        		.setSkill(attrs)
        		.build();
        
        var skillMgr = p.getSkillManager();
        for(var s : skillMgr.getSkills()) {
            var st = s.getType();
            assertEquals("Predefined skill " + st.getName(), attrs.get(st).intValue(),
                        skillMgr.getSkillLevel(st));
        }
    }
}
