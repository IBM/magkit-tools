package com.aperto.magnolia.edittools.action;

/**
 * Action definition for {@link OpenPageLocationAction}.
 *
 * @author jean-charles.robert
 * @since 08.05.18
 * @deprecated for Magnolia 6 use {@link com.aperto.magnolia.edittools.m6.action.OpenPageBrowserLocationActionDefinition}
 */
@Deprecated
public class OpenPageLocationActionDefinition extends AbstractOpenLocationActionDefinition {

    public OpenPageLocationActionDefinition() {
        setImplementationClass(OpenPageLocationAction.class);
    }

}
