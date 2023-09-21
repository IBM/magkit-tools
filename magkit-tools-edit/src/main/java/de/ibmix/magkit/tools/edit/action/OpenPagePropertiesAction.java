package de.ibmix.magkit.tools.edit.action;

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

import com.vaadin.v7.data.Item;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.dialog.callback.DefaultEditorCallback;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.framework.action.OpenEditDialogActionDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;

import static de.ibmix.magkit.core.utils.NodeUtils.getTemplate;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Opens the page properties dialog for editing a page node.
 *
 * @author frank.sommer
 * @see OpenEditDialogActionDefinition
 * @since 1.2.3
 * @deprecated for Magnolia 6 use {@link de.ibmix.magkit.tools.edit.m6.action.OpenPagePropertiesAction}
 */
@Deprecated
public class OpenPagePropertiesAction extends AbstractAction<OpenEditDialogActionDefinition> {
    private final Item _itemToEdit;
    private final SimpleTranslator _i18n;
    private final FormDialogPresenterFactory _formDialogPresenterFactory;
    private final UiContext _uiContext;
    private final EventBus _eventBus;
    private final ContentConnector _contentConnector;

    @Inject
    public OpenPagePropertiesAction(OpenEditDialogActionDefinition definition, Item itemToEdit, FormDialogPresenterFactory formDialogPresenterFactory, UiContext uiContext, @Named(AdmincentralEventBus.NAME) final EventBus eventBus, SimpleTranslator i18n, ContentConnector contentConnector) {
        super(definition);
        _itemToEdit = itemToEdit;
        _i18n = i18n;
        _formDialogPresenterFactory = formDialogPresenterFactory;
        _uiContext = uiContext;
        _eventBus = eventBus;
        _contentConnector = contentConnector;
    }

    @Override
    public void execute() throws ActionExecutionException {
        String dialogId = retrieveDialog();

        if (isBlank(dialogId)) {
            _uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, _i18n.translate("ui-framework.actions.no.dialog.definition", getDefinition().getName()));
        } else {
            final FormDialogPresenter formDialogPresenter = _formDialogPresenterFactory.createFormDialogPresenter(dialogId);
            if (formDialogPresenter == null) {
                _uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, _i18n.translate("ui-framework.actions.dialog.not.registered", dialogId));
            } else {
                formDialogPresenter.start(_itemToEdit, dialogId, _uiContext, new DefaultEditorCallback(formDialogPresenter) {
                    @Override
                    public void onSuccess(String actionName) {
                        _eventBus.fireEvent(new ContentChangedEvent(_contentConnector.getItemId(_itemToEdit), true));
                        super.onSuccess(actionName);
                    }
                });
            }
        }
    }

    private String retrieveDialog() {
        String dialogId = EMPTY;
        if (_itemToEdit instanceof JcrNodeAdapter) {
            Node jcrItem = ((JcrNodeAdapter) _itemToEdit).getJcrItem();
            String template = getTemplate(jcrItem);
            if (isNotEmpty(template)) {
                TemplateDefinitionRegistry templateDefinitionRegistry = Components.getComponent(TemplateDefinitionRegistry.class);
                TemplateDefinition templateDefinition = templateDefinitionRegistry.getProvider(template).get();
                dialogId = templateDefinition.getDialog();
            }
        }
        return dialogId;
    }
}