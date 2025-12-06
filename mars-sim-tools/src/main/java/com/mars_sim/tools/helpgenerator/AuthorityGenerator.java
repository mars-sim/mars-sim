/*
 * Mars Simulation Project
 * AuthorityGenerator.java
 * @date 2025-02-01
 * @author Barry Evans
 */
package com.mars_sim.tools.helpgenerator;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.authority.Authority;

public class AuthorityGenerator extends TypeGenerator<Authority> {

    public static final String TYPE_NAME = "authority";

    protected AuthorityGenerator(HelpContext parent) {
        super(parent, TYPE_NAME, "Reporting Authority",
                "Reporting Authorities that control the Settlements", "governance");
        setChangeViaEditor(true);
    }

    @Override
    protected List<Authority> getEntities() {
        var factory = getConfig().getReportingAuthorityFactory();

        return factory.getKnownItems().stream()
                    .filter(Authority::isBundled)
                    .sorted(Comparator.comparing(Authority::getName))
                    .toList();
    }

    @Override
    protected String getEntityName(Authority v) {
        return v.getName();
    }
}
