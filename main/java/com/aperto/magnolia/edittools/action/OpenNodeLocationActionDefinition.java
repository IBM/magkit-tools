package com.aperto.magnolia.edittools.action;

/**
 * Action definition for {@link OpenNodeLocationAction}.
 * Used in decoration files in projects.
 *
 * @author jean-charles.robert
 * @since 08.05.18
 */
@SuppressWarnings("unused")
public class OpenNodeLocationActionDefinition extends AbstractOpenLocationActionDefinition {

    public OpenNodeLocationActionDefinition() {
        setImplementationClass(OpenNodeLocationAction.class);
    }

}
