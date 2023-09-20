package de.ibmix.magkit.tools.edit.rule;

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

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
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
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockMgnlNode;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubType;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Philipp Güttler (IBM iX)
 * @since 01.07.2015
 */
public class IsElementEditableRuleTest {
    public static final String PATH_TO_PAGE = "/path/to/page";
    public static final String PATH_TO_AREA = PATH_TO_PAGE + "/area";
    public static final String PATH_TO_COMPONENT = PATH_TO_AREA + "/component";
    public static final String TEMPLATE_ID = "my-module:templates/page";
    public static final String DIALOG_ID = "my-module:dialogs/pages/pageProperties";
    public static final String COMPONENT_ID = "my-module:components/teaser";

    private IsElementEditableRule _rule;

    @Mock
    private TemplateDefinitionRegistry _templateDefinitionRegistry;
    @Mock
    private FormDialogPresenterFactory _formDialogPresenterFactory;
    @Mock
    private DefinitionProvider<TemplateDefinition> _provider;
    @Mock
    private TemplateDefinition _templateDefinition;
    @Mock
    private PageEditorPresenter _presenter;

    @Before
    public void startUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMgnlNode(PATH_TO_PAGE, WEBSITE, NodeTypes.Page.NAME, stubProperty(NodeTypes.Renderable.TEMPLATE, TEMPLATE_ID),
            stubNode(
                mockNode(PATH_TO_AREA, stubType(NodeTypes.Area.NAME),
                    stubNode(
                        mockNode(PATH_TO_COMPONENT, stubType(NodeTypes.Component.NAME))
                    )
                )
            )
        );

        when(_provider.get()).thenReturn(_templateDefinition);
        when(_formDialogPresenterFactory.createFormDialogPresenter(DIALOG_ID)).thenReturn(mock(FormDialogPresenter.class));

        when(_provider.get()).thenReturn(_templateDefinition);
        when(_templateDefinition.getDialog()).thenReturn(DIALOG_ID);
        when(_templateDefinition.getEditable()).thenReturn(Boolean.TRUE);
        when(_templateDefinitionRegistry.getProvider(TEMPLATE_ID)).thenReturn(_provider);

        _rule = new IsElementEditableRule(_presenter);
        _rule.setTemplateDefinitionRegistry(_templateDefinitionRegistry);
    }

    @After
    public void cleanUp() throws Exception {
        cleanContext();
    }

    @Test
    public void testIsAvailableForElementNull() throws Exception {
        assertThat(_rule.isAvailableForElement(null), is(false));
    }

    @Test
    public void testIsAvailableForElementPageElement() throws Exception {
        assertThat(_rule.isAvailableForElement(new PageElement(WEBSITE, PATH_TO_PAGE, DIALOG_ID)), is(true));
        assertThat(_rule.isAvailableForElement(new PageElement(WEBSITE, PATH_TO_PAGE, null)), is(false));
    }

    @Test
    public void testIsAvailableForElementAreaElement() throws Exception {
        assertThat(_rule.isAvailableForElement(new AreaElement(WEBSITE, PATH_TO_AREA, DIALOG_ID, COMPONENT_ID)), is(true));
        assertThat(_rule.isAvailableForElement(new AreaElement(WEBSITE, PATH_TO_AREA, null, COMPONENT_ID)), is(true));
    }

    @Test
    public void testIsAvailableForElementComponentElement() throws Exception {
        ComponentElement componentElement = new ComponentElement(WEBSITE, PATH_TO_COMPONENT, DIALOG_ID);
        componentElement.setEditable(true);
        assertThat(_rule.isAvailableForElement(componentElement), is(true));

        componentElement.setDialog(null);
        assertThat(_rule.isAvailableForElement(componentElement), is(true));
    }
}
