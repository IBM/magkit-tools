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

import de.ibmix.magkit.tools.edit.m6.action.OpenPagePropertiesAction;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.pages.app.action.EditElementAction;
import info.magnolia.pages.app.action.EditElementActionDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.core.utils.NodeUtils.isNodeType;
import static info.magnolia.jcr.util.NodeUtil.getNearestAncestorOfType;

/**
 * Extended action to execute page properties dialog even if current item is a sub item like area or component.
 *
 * @author Philipp Güttler (IBM iX)
 * @since 30.06.2015
 * @deprecated for Magnolia 6 use {@link OpenPagePropertiesAction}.
 */
@Deprecated
public class EditPageAction extends EditElementAction {
    private final AbstractElement _element;
    private final SubAppContext _subAppContext;
    private final EventBus _eventBus;
    private final FormDialogPresenterFactory _dialogPresenterFactory;
    private TemplateDefinitionRegistry _templateDefinitionRegistry;

    @Inject
    public EditPageAction(JcrItemAdapter item, EditElementActionDefinition definition, AbstractElement element, SubAppContext subAppContext, @Named(SubAppEventBus.NAME) EventBus eventBus, FormDialogPresenterFactory dialogPresenterFactory) {
        super(item, definition, element, subAppContext, eventBus, dialogPresenterFactory);

        _element = element;
        _subAppContext = subAppContext;
        _eventBus = eventBus;
        _dialogPresenterFactory = dialogPresenterFactory;
    }

    @Override
    public void execute() throws ActionExecutionException {
        Node pageNode = SessionUtil.getNode(_element.getWorkspace(), _element.getPath());

        if (pageNode != null) {
            if (isNodeType(pageNode, NodeTypes.Page.NAME)) {
                // we are on a page node, so we could use the origin implementation
                super.execute();
            } else {
                // we have to find the corresponding page node
                openDialogForCorrespondingPage(pageNode);
            }
        }
    }

    private void openDialogForCorrespondingPage(Node currentNode) throws ActionExecutionException {
        try {
            if (currentNode.getDepth() > 1) {
                Node pageNode = getNearestAncestorOfType(currentNode, NodeTypes.Page.NAME);
                if (isNodeType(pageNode, NodeTypes.Page.NAME)) {
                    final JcrNodeAdapter item = new JcrNodeAdapter(pageNode);
                    String templateId = NodeTypes.Renderable.getTemplate(pageNode);
                    DefinitionProvider<TemplateDefinition> templateDefinition = _templateDefinitionRegistry.getProvider(templateId);
                    String dialogId = templateDefinition.get().getDialog();
                    final FormDialogPresenter formDialogPresenter = _dialogPresenterFactory.createFormDialogPresenter(dialogId);
                    formDialogPresenter.start(item, dialogId, _subAppContext, new EditorCallback() {
                        @Override
                        public void onSuccess(String actionName) {
                            _eventBus.fireEvent(new ContentChangedEvent(item.getItemId()));
                            formDialogPresenter.closeDialog();
                        }

                        @Override
                        public void onCancel() {
                            formDialogPresenter.closeDialog();
                        }
                    });
                }
            }
        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }

    @Inject
    public void setTemplateDefinitionRegistry(final TemplateDefinitionRegistry templateDefinitionRegistry) {
        _templateDefinitionRegistry = templateDefinitionRegistry;
    }
}
