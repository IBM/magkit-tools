package de.ibmix.magkit.tools.edit.ui;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.shared.ui.dnd.DragSourceState;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.grid.DropLocation;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.components.grid.GridRowDragger;
import com.vaadin.ui.components.grid.TargetDataProviderUpdater;
import de.ibmix.magkit.tools.edit.setup.EditToolsModule;
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
 * @author Philipp Güttler (IBM iX)
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
            if (dropConstraint.isAllowedAt(items, getTarget(), getDropLocation())) {
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
        datasource.moveItems(items, getTarget(), getDropLocation());
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
            return _simpleTranslator.translate("magkit.moveItem.confirmationQuestionManyItems", items.size());
        }
        return _simpleTranslator.translate("magkit.moveItem.confirmationQuestionOneItem");
    }

    protected T getTarget() {
        return _target;
    }

    protected DropLocation getDropLocation() {
        return _dropLocation;
    }
}
