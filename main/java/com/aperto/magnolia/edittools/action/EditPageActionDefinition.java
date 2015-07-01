package com.aperto.magnolia.edittools.action;

import info.magnolia.pages.app.action.EditElementActionDefinition;

/**
 * Action definition for {@link EditPageAction EditPageAction}.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 30.06.2015
 */
public class EditPageActionDefinition extends EditElementActionDefinition {

    public EditPageActionDefinition() {
        setImplementationClass(EditPageAction.class);
    }
}
