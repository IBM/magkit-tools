package com.aperto.magnolia.edittools.m6.ui;

import com.aperto.magnolia.edittools.setup.EditToolsModule;
import com.vaadin.shared.ui.dnd.DragSourceState;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.grid.DropLocation;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.components.grid.GridRowDragger;
import com.vaadin.ui.components.grid.TargetDataProviderUpdater;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.AlertBuilder;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.browser.drop.DropConstraint;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Item;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.List;

/**
 * Custom {@link info.magnolia.ui.contentapp.browser.drop.RowDragger} with additional confirmation dialog on drop.
 *
 * @param <T> item type
 * @author Philipp Güttler (Aperto GmbH)
 * @see info.magnolia.ui.contentapp.browser.drop.RowDragger
 * @since 1.4.1
 */
public class ConfirmRowDragger<T> extends GridRowDragger<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmRowDragger.class);

    private final SimpleTranslator _simpleTranslator;
    private final Provider<EditToolsModule> _moduleProvider;

    private T _target;
    private DropLocation _dropLocation;

    @Inject
    public ConfirmRowDragger(Datasource<T> datasource, Grid<T> grid, DropMode dropMode, DropConstraint<T> dropConstraint, SimpleTranslator simpleTranslator, Provider<EditToolsModule> moduleProvider) {
        super(grid, dropMode);

        _simpleTranslator = simpleTranslator;
        _moduleProvider = moduleProvider;

        getGridDragSource().clearDragDataGenerator(DragSourceState.DATA_TYPE_TEXT);
        getGridDropTarget().setDropEffect(DropEffect.MOVE);
        getGridDropTarget().setDropMode(DropMode.ON_TOP_OR_BETWEEN);
        setSourceDataProviderUpdater((dropEffect, dataProvider, items) -> {
        });
        setDropIndexCalculator(event -> {
            _dropLocation = event.getDropLocation();
            event.getDropTargetRow().ifPresent(row -> _target = row);
            return 0;
        });
        setTargetDataProviderUpdater((TargetDataProviderUpdater<T>) (dropEffect, dataProvider, index, items) -> {
            if (dropConstraint.isAllowedAt(items, _target, _dropLocation)) {
                if (showConfirmation(datasource)) {
                    AlertBuilder.confirmDialog(createConfirmTitle(items))
                        .withBody(createConfirmContent(items))
                        .withLevel(Notification.Type.WARNING_MESSAGE)
                        .withOkButtonCaption(_simpleTranslator.translate("magkit.moveItem.confirmText"))
                        .withDeclineButtonCaption(_simpleTranslator.translate("magkit.moveItem.cancelText"))
                        .withConfirmationHandler(() -> {
                            doMoveItems(datasource, grid, items);
                        })
                        .buildAndOpen();
                } else {
                    // move without confirmation
                    doMoveItems(datasource, grid, items);
                }
            }
        });
    }

    protected String createConfirmContent(final Collection<T> items) {
        StringBuilder bodyText = new StringBuilder("<ul>");

        for (T item : items) {
            try {
                // we can cast here without check because #showConfirmation is based on JcrDatasource
                // use Item to allow both Nodes and Properties
                String path = ((Item) item).getPath();
                bodyText.append("<li>").append(path).append("</li>");
            } catch (RepositoryException e) {
                LOGGER.warn("Error creating dialog content: {}", e.getMessage());
                LOGGER.debug(e.getMessage(), e);
            }
        }

        return bodyText.append("</ul>").toString();
    }

    protected void doMoveItems(final Datasource<T> datasource, final Grid<T> grid, final Collection<T> items) {
        datasource.moveItems(items, _target, _dropLocation);
        grid.getDataProvider().refreshAll();
    }

    protected boolean showConfirmation(final Datasource<T> datasource) {
        if (!(datasource instanceof JcrDatasource)) {
            return false;
        }

        EditToolsModule editToolsModule = _moduleProvider.get();
        List<String> workspaces = editToolsModule.getMoveConfirmWorkspaces();
        boolean isEnabled = false;

        try {
            isEnabled = workspaces.contains(((JcrDatasource) datasource)
                .getJCRSession()
                .getWorkspace()
                .getName());
        } catch (RepositoryException e) {
            LOGGER.warn("Error checking dialog visibility: {}", e.getMessage());
            LOGGER.debug(e.getMessage(), e);
        }

        return isEnabled;
    }

    protected String createConfirmTitle(Collection<T> items) {
        if (items.size() > 1) {
            return _simpleTranslator.translate("magkit.moveItem.m6.confirmationQuestionManyItems", items.size());
        }
        return _simpleTranslator.translate("magkit.moveItem.m6.confirmationQuestionOneItem");
    }
}
