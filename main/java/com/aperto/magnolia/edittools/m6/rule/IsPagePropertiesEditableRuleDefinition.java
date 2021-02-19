package com.aperto.magnolia.edittools.m6.rule;

import info.magnolia.ui.api.availability.AvailabilityRuleType;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;

/**
 * @author Philipp Güttler (Aperto GmbH)
 * @since 19.02.2021
 */
@AvailabilityRuleType("isPagePropertiesEditable")
public class IsPagePropertiesEditableRuleDefinition extends ConfiguredAvailabilityRuleDefinition {

    public IsPagePropertiesEditableRuleDefinition() {
        setImplementationClass(IsPagePropertiesEditableRule.class);
    }
}
