package com.aperto.magnolia.edittools.m6.action;

import com.aperto.magnolia.edittools.m6.command.JcrExportSinglePageCommand;
import com.machinezoo.noexception.Exceptions;
import com.vaadin.ui.UI;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.ExportJcrNodeToYamlCommand;
import info.magnolia.context.Context;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.action.JcrCommandAction;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.framework.util.TempFileStreamResource;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Map;

/**
 * @author ala.abudheileh
 * 06.09.2023
 */
public class JcrExportSinglePageAction extends JcrCommandAction<Node, JcrExportSinglePageActionDefinition> {

    static final String WINDOW_NAME = "_parent";
    private TempFileStreamResource _tempFileStreamResource;
    private JcrExportSinglePageCommand.Format _format;

    private final SimpleTranslator _simpleTranslator;
    private final UI _ui;
    //CHECKSTYLE:OFF
    @Inject
    JcrExportSinglePageAction(JcrExportSinglePageActionDefinition definition, CommandsManager commandsManager, ValueContext<Node> valueContext,
                              Context context, AsyncActionExecutor asyncActionExecutor, JcrDatasource datasource, SimpleTranslator simpleTranslator, UI ui) {
        super(definition, commandsManager, valueContext, context, asyncActionExecutor, datasource);
        _simpleTranslator = simpleTranslator;
        _ui = ui;
        _format = getDefinition().getFormat();
    }


    @Deprecated
    public JcrExportSinglePageAction(JcrExportSinglePageActionDefinition definition, CommandsManager commandsManager, ValueContext<Node> valueContext, Context context, AsyncActionExecutor asyncActionExecutor, JcrDatasource datasource) {
        this(definition, commandsManager, valueContext, context, asyncActionExecutor, datasource, Components.getComponent(SimpleTranslator.class), UI.getCurrent());
    }
    //CHECKSTYLE:ON
    @Override
    public void execute() {
        String extension = _format.name().toLowerCase();
        Node node = getValueContext().getSingleOrThrow();
        _tempFileStreamResource = new TempFileStreamResource();
        try {
            _tempFileStreamResource.setTempFileName(node.getName());
            super.execute();
            _tempFileStreamResource.setFilename(getFileNameFromNodePath(node, extension));
            _tempFileStreamResource.setMIMEType("application/" + extension);
            openResourceForDownload();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private void openResourceForDownload() {
        _ui.getPage().open(_tempFileStreamResource, WINDOW_NAME, true);
    }

    String getFileNameFromNodePath(Node node, String extension) throws RepositoryException {
        String pathToFileName = DataTransporter.encodePath(
                DataTransporter.createExportPath(Exceptions.wrap().get(node::getPath)),
                DataTransporter.DOT,
                DataTransporter.UTF8
        );

        String repository = node.getSession().getWorkspace().getName();

        if (DataTransporter.DOT.equals(pathToFileName)) {
            return String.join(DataTransporter.DOT, repository, extension);
        } else {
            return repository.concat(String.join(DataTransporter.DOT, pathToFileName, extension));
        }
    }

    @Override
    protected Map<String, Object> buildParams(Node jcrItem) {
        try {
            Map<String, Object> params = super.buildParams(jcrItem);

            params.put("format", _format);
            params.put(ExportJcrNodeToYamlCommand.EXPORT_OUTPUT_STREAM, _tempFileStreamResource.getTempFileOutputStream());
            return params;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to bind command to temp file output stream: ", e);
        }
    }
}