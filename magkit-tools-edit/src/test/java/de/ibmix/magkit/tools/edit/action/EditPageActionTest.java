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

import de.ibmix.magkit.tools.edit.rule.IsElementEditableRuleTest;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubType;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Philipp Güttler (IBM iX)
 * @since 01.07.2015
 */
public class EditPageActionTest {
    private static final String GENERIC_DIALOG_ID = "myID";

    private EditPageAction _action;
    @Mock
    private TemplateDefinitionRegistry _templateDefinitionRegistry;
    @Mock
    private FormDialogPresenterFactory _formDialogPresenterFactory;
    @Mock
    private DefinitionProvider<TemplateDefinition> _provider;
    @Mock
    private TemplateDefinition _templateDefinition;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockPageNode(IsElementEditableRuleTest.PATH_TO_PAGE, stubProperty(NodeTypes.Renderable.TEMPLATE, IsElementEditableRuleTest.TEMPLATE_ID),
            stubNode(
                mockNode(IsElementEditableRuleTest.PATH_TO_AREA, stubType(NodeTypes.Area.NAME),
                    stubNode(
                        mockNode(IsElementEditableRuleTest.PATH_TO_COMPONENT, stubType(NodeTypes.Component.NAME))
                    )
                )
            )
        );

        when(_provider.get()).thenReturn(_templateDefinition);
        when(_templateDefinition.getDialog()).thenReturn(IsElementEditableRuleTest.DIALOG_ID);
        when(_templateDefinitionRegistry.getProvider(IsElementEditableRuleTest.TEMPLATE_ID)).thenReturn(_provider);
        when(_formDialogPresenterFactory.createFormDialogPresenter(IsElementEditableRuleTest.DIALOG_ID)).thenReturn(mock(FormDialogPresenter.class));
    }

    @After
    public void cleanUp() throws Exception {
        cleanContext();
    }

    @Test
    public void testExecutePageElement() throws Exception {
        _action = new EditPageAction(null, new EditPageActionDefinition(), new PageElement(WEBSITE, IsElementEditableRuleTest.PATH_TO_PAGE, IsElementEditableRuleTest.DIALOG_ID), mock(SubAppContext.class), mock(EventBus.class), _formDialogPresenterFactory);
        _action.setTemplateDefinitionRegistry(_templateDefinitionRegistry);
        _action.execute();
        verify(_formDialogPresenterFactory).createFormDialogPresenter(IsElementEditableRuleTest.DIALOG_ID);
    }

    @Test
    public void testExecuteAreaElement() throws Exception {
        _action = new EditPageAction(null, new EditPageActionDefinition(), new AreaElement(WEBSITE, IsElementEditableRuleTest.PATH_TO_AREA, GENERIC_DIALOG_ID, IsElementEditableRuleTest.COMPONENT_ID), mock(SubAppContext.class), mock(EventBus.class), _formDialogPresenterFactory);
        _action.setTemplateDefinitionRegistry(_templateDefinitionRegistry);
        _action.execute();
        verify(_formDialogPresenterFactory).createFormDialogPresenter(IsElementEditableRuleTest.DIALOG_ID);
    }

    @Test
    public void testExecuteComponentElement() throws Exception {
        _action = new EditPageAction(null, new EditPageActionDefinition(), new ComponentElement(WEBSITE, IsElementEditableRuleTest.PATH_TO_COMPONENT, GENERIC_DIALOG_ID), mock(SubAppContext.class), mock(EventBus.class), _formDialogPresenterFactory);
        _action.setTemplateDefinitionRegistry(_templateDefinitionRegistry);
        _action.execute();
        verify(_formDialogPresenterFactory).createFormDialogPresenter(IsElementEditableRuleTest.DIALOG_ID);
    }
}
