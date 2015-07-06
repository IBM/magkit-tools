package com.aperto.magnolia.edittools.action;

import info.magnolia.ui.api.action.Action;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;

/**
 * @author Philipp Güttler (Aperto AG)
 * @since 04.07.2015
 */
public class PasteNodeActionDefinition extends ConfiguredActionDefinition {

    @Override
    public Class<? extends Action> getImplementationClass() {
        return PasteNodeAction.class;
    }
}
