package com.mars_sim.core.person;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;


public class PersonBuilderTest extends MarsSimUnitTest {
    private static final String TEST_NAME = "test";
    private static final String TEST_COUNTRY = "Norway";
    private static final String TEST_RA = "NASA";

    @Test
    public void testBasic() {
        var home = buildSettlement("mock");
   
        Person p = Person.create(TEST_NAME, home, GenderType.MALE)
        		.build();

        assertNotNull(p, "Person created");
        assertEquals(TEST_NAME, p.getName(), "Person name");
        assertEquals(GenderType.MALE, p.getGender(), "Person gender");
        assertTrue(p.getAge() > 20, "Person minimum age");
        assertTrue(p.getCarryingCapacity() > EVASuit.getEmptyMass(), "Person can carry at least EVA");
    }

    @Test
    public void testOptional() {
        var home = buildSettlement("mock");

        AuthorityFactory af = getConfig().getReportingAuthorityFactory();
        var ra = af.getItem(TEST_RA);
        
        	    
		var country = new NationSpecConfig(getConfig()).getItem("Norway");
        
        Person p = Person.create(TEST_NAME, home, GenderType.FEMALE)
                        .setAge(30)
                        .setCountry(country)
                        .setSponsor(ra).build();

        assertNotNull(p, "Person created");
        assertEquals(TEST_NAME, p.getName(), "Person name");
        assertEquals(GenderType.FEMALE, p.getGender(), "Person gender");
        assertEquals(TEST_COUNTRY, p.getCountry(), "Person country");
        assertEquals(30, p.getAge(), "Fixed age");
        assertEquals(TEST_RA, p.getReportingAuthority().getName(), "Person Sponsor");
    }

    @Test
    public void testAttributes() {
        var home = buildSettlement("mock");
        
        Map<NaturalAttributeType, Integer> attrs = new EnumMap<>(NaturalAttributeType.class);
        attrs.put(NaturalAttributeType.AGILITY, 20);
        attrs.put(NaturalAttributeType.DISCIPLINE, 44);
        attrs.put(NaturalAttributeType.SPIRITUALITY, 78);

        Person p = Person.create(TEST_NAME, home, GenderType.MALE)
        		.setAttribute(attrs)
        		.build();
        
        assertEquals(attrs, p.getNaturalAttributeManager().getAttributeMap(), "Predefined attributes");

    }
    
    @Test
    public void testSkills() {
        var home = buildSettlement("mock");

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
            assertEquals(attrs.get(st).intValue(), skillMgr.getSkillLevel(st), "Predefined skill " + st.getName());
        }
    }
}
