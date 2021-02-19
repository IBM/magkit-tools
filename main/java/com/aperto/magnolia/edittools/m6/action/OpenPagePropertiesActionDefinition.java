package com.aperto.magnolia.edittools.m6.action;

import info.magnolia.ui.api.action.ActionType;

/**
 * @author Philipp Güttler (Aperto GmbH)
 * @since 19.02.2021
 */
@ActionType("openPageProperties")
public class OpenPagePropertiesActionDefinition extends OpenAppViewLocationActionDefinition {

    public OpenPagePropertiesActionDefinition() {
        setImplementationClass(OpenPagePropertiesAction.class);
    }
}
