package com.aperto.magnolia.edittools.m6.action;

import info.magnolia.ui.api.action.ActionType;

/**
 * @author Philipp Güttler (Aperto GmbH)
 * @since 19.02.2021
 */
@ActionType("openPageExternalLocation")
public class OpenPageExternalLocationActionDefinition extends OpenAppViewLocationActionDefinition {

    public OpenPageExternalLocationActionDefinition() {
        setImplementationClass(OpenPageExternalLocationAction.class);
    }
}
