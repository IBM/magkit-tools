package com.aperto.magnolia.edittools.m6.action;

import com.aperto.magnolia.edittools.m6.command.JcrExportSinglePageCommand;
import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.contentapp.action.JcrCommandActionDefinition;

/**
 * @author ala.abudheileh
 * 06.09.2023
 */
@ActionType("exportSinglePageAction")
public class JcrExportSinglePageActionDefinition extends JcrCommandActionDefinition {
    private JcrExportSinglePageCommand.Format _format;

    public JcrExportSinglePageActionDefinition() {
        _format = JcrExportSinglePageCommand.Format.YAML;
        setImplementationClass(JcrExportSinglePageAction.class);
        setCommand("exportSinglePage");
    }

    public void setAsynchronous(boolean asynchronous) {
        throw new IllegalArgumentException("This action doesn't support asynchronous execution");
    }

    public void setFormat(JcrExportSinglePageCommand.Format format) {
        _format = format;
    }

    public JcrExportSinglePageCommand.Format getFormat() {
        return _format;
    }
}