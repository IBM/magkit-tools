package com.aperto.magnolia.edittools.rule;

import info.magnolia.ui.api.availability.AvailabilityRule;
import info.magnolia.ui.api.availability.AvailabilityRuleDefinition;

/**
 * @author Philipp Güttler (Aperto AG)
 * @since 18.11.14
 */
public class DuplicateComponentRuleDefinition implements AvailabilityRuleDefinition {
    @Override
    public Class<? extends AvailabilityRule> getImplementationClass() {
        return DuplicateComponentRule.class;
    }
}
