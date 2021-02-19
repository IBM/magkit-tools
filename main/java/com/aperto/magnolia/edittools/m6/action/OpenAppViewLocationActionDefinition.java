package com.aperto.magnolia.edittools.m6.action;


import info.magnolia.ui.contentapp.action.OpenLocationActionDefinition;

/**
 * Definition for OpenAppViewLocationAction.
 *
 * @author jean-charles.robert
 * @see OpenLocationActionDefinition
 * @since 14.05.18
 */
public class OpenAppViewLocationActionDefinition extends OpenLocationActionDefinition {

    private String _viewType = OpenAppViewLocationAction.TREE_VIEW;

    public String getViewType() {
        return _viewType;
    }

    public void setViewType(String viewType) {
        _viewType = viewType;
    }
}
