package com.aperto.magnolia.edittools.action;

import com.aperto.magnolia.edittools.rule.DuplicateComponentRuleDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.framework.action.DuplicateNodeActionDefinition;

import static java.util.Arrays.asList;

/**
 * @author Philipp Güttler (Aperto AG)
 * @since 17.11.14
 */
public class DuplicateComponentActionDefinition extends DuplicateNodeActionDefinition {

    public DuplicateComponentActionDefinition() {
        setImplementationClass(DuplicateComponentAction.class);
        ((ConfiguredAvailabilityDefinition) getAvailability()).setRules(
            asList(new DuplicateComponentRuleDefinition())
        );
    }
}
