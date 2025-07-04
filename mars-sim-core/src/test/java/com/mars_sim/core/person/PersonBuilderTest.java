package com.mars_sim.core.person;

import java.util.HashMap;
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
        NationSpecConfig nsc = new NationSpecConfig(getConfig());
        var country = nsc.getItem(TEST_COUNTRY);
        
        Person p = Person.create(TEST_NAME, home, GenderType.MALE)
        		.setCountry(country)
        		.build();

        assertNotNull("Person created", p);
        assertEquals("Person name", TEST_NAME, p.getName());
        assertEquals("Person gender", GenderType.MALE, p.getGender());
        assertTrue("Person minimum age", p.getAge() > 20);
        assertTrue("Person can carry at least EVA", p.getCarryingCapacity() > EVASuit.getEmptyMass());
    }

    public void testOptional() {
        var home = buildSettlement();
        NationSpecConfig nsc = new NationSpecConfig(getConfig());
        var country = nsc.getItem(TEST_COUNTRY);

        AuthorityFactory af = simConfig.getReportingAuthorityFactory();
        var ra = af.getItem(TEST_RA);
        
        Person p = Person.create(TEST_NAME, home, GenderType.FEMALE)
                        .setAge(30)
                        .setCountry(country)
                        .setSponsor(ra).build();

        assertNotNull("Person created", p);
        assertEquals("Person name", TEST_NAME, p.getName());
        assertEquals("Person gender", GenderType.MALE, p.getGender());
        assertEquals("Person country", TEST_COUNTRY, p.getCountry());
        assertEquals("Person Sponsor", TEST_RA, p.getReportingAuthority().getName());
    }

    public void testAttributes() {
        var home = buildSettlement();
        NationSpecConfig nsc = new NationSpecConfig(getConfig());
        var country = nsc.getItem(TEST_COUNTRY);
        
        Map<NaturalAttributeType, Integer> attrs = new HashMap<>();
        attrs.put(NaturalAttributeType.AGILITY, 20);
        attrs.put(NaturalAttributeType.DISCIPLINE, 44);
        attrs.put(NaturalAttributeType.SPIRITUALITY, 78);

        Person p = Person.create(TEST_NAME, home, GenderType.MALE)
        		.setCountry(country)
        		.setAttribute(attrs)
        		.build();
        
        assertEquals("Predefined attributes", attrs,
                        p.getNaturalAttributeManager().getAttributeMap());

    }
    
    public void testSkills() {
        var home = buildSettlement();
        NationSpecConfig nsc = new NationSpecConfig(getConfig());
        var country = nsc.getItem(TEST_COUNTRY);
        
        Map<SkillType, Integer> attrs = new HashMap<>();
        attrs.put(SkillType.BIOLOGY, 20);
        attrs.put(SkillType.MECHANICS, 44);
        attrs.put(SkillType.MATHEMATICS, 78);

        Person p = Person.create(TEST_NAME, home, GenderType.MALE)
        		.setCountry(country)
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
