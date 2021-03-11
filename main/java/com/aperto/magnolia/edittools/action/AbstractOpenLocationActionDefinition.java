package com.aperto.magnolia.edittools.action;

import info.magnolia.ui.framework.action.OpenLocationActionDefinition;

/**
 * Definition for AbstractOpenLocationAction.
 *
 * @author jean-charles.robert
 * @see OpenLocationActionDefinition
 * @since 14.05.18
 * @deprecated for Magnolia 6 use {@link com.aperto.magnolia.edittools.m6.action.OpenAppViewLocationActionDefinition}
 */
@Deprecated
public abstract class AbstractOpenLocationActionDefinition extends OpenLocationActionDefinition {

    private String _viewType = AbstractOpenLocationAction.TREE_VIEW;

    public String getViewType() {
        return _viewType;
    }

    public void setViewType(String viewType) {
        _viewType = viewType;
    }
}
