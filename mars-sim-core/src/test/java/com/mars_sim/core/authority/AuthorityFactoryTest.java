package com.mars_sim.core.authority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.mars_sim.core.SimulationConfig;

class AuthorityFactoryTest {
    private AuthorityFactory factory;
    @BeforeEach
    void setUp() {
        var config = SimulationConfig.loadConfig();
        factory = config.getReportingAuthorityFactory();

    }

    @Test
    void testLoadAuthorities() {
        var authorities = factory.getKnownItems();

        assertNotNull(authorities, "Authorities loaded");

        var bundledAuthorities = authorities.stream()
                                        .filter(Authority::isBundled)
                                        .toList();
        assertFalse(bundledAuthorities.isEmpty(), "Authorities not empty");

    }

    @ParameterizedTest
    @CsvSource({
        "'AEB','Agencia Espacial Brasileira', 'Space Tracking', 'Brazil'",
        "'NASA','National Aeronautics and Space Administration', 'Finding Life', 'United States'"
        })
    void testAuthority(String code, String name, String agenda, String country) {
        var authority = factory.getItem(code);

        assertNotNull(authority);
        assertEquals(code, authority.getName());
        assertEquals(name, authority.getDescription());
        assertEquals(agenda, authority.getMissionAgenda().getName());
        assertFalse(authority.getCountries().isEmpty(), "Authority has countries");
        assertTrue(authority.getCountries().contains(country), "Authority has country " + country);
    }

    @Test
    void testSettlementNames() {
        var authority = factory.getItem("NASA");

        assertNotNull(authority);
        var settlementNames = authority.getSettlementNames();
        assertNotNull(settlementNames);
        assertFalse(settlementNames.getPotentials().isEmpty(), "Settlement names empty");

        // Use all the names
        Set<String> usedNames = new HashSet<>();
        for (int i = 0; i < settlementNames.getPotentials().size(); i++) {
            String name = settlementNames.generateName(usedNames);
            assertNotNull(name, "Generated name not null");
            usedNames.add(name);
        }
        // Now all names are used, the next call should return null
        String name = settlementNames.generateName(usedNames);
        assertNull(name, "No more names available");
        assertEquals(settlementNames.getPotentials().size(), usedNames.size(), "All names used");
    }

    @ParameterizedTest
    @CsvSource({
        "'Colony Construction', 'Build self-sustaining colonies'",
        "'Settling Mars', 'Make Mars the second home for humanity'"
        })
    void testGetAgenda(String name, String objective) {
        var agenda = factory.getAgenda(name);

        assertNotNull(agenda);
        assertEquals(name, agenda.getName());
        assertEquals(objective, agenda.getObjectiveName());
        assertFalse(agenda.getData().isEmpty(), "Agenda has data");
        assertFalse(agenda.getReports().isEmpty(), "Agenda has report");
        assertFalse(agenda.getCapabilities().isEmpty(), "Agenda has capabilities");
    }
}
