package com.aperto.magnolia.edittools.rule;

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.mockito.MagnoliaNodeMockUtils;
import com.aperto.magkit.mockito.jcr.SessionMockUtils;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubType;
import static com.aperto.magnolia.edittools.rule.IsElementEditableRuleTest.DIALOG_ID;
import static com.aperto.magnolia.edittools.rule.IsElementEditableRuleTest.TEMPLATE_ID;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Philipp Güttler (Aperto AG)
 * @since 01.07.2015
 */
@RunWith(MockitoJUnitRunner.class)
public class IsElementEditableRuleTest {

    public static final String PATH_TO_PAGE = "path/to/page";
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
        ContextMockUtils.cleanContext();
        SessionMockUtils.cleanSession();
        MagnoliaNodeMockUtils.mockMgnlNode(PATH_TO_PAGE, WEBSITE, NodeTypes.Page.NAME, stubProperty(NodeTypes.Renderable.TEMPLATE, TEMPLATE_ID),
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
        assertThat(_rule.isAvailableForElement(new ComponentElement(WEBSITE, PATH_TO_COMPONENT, DIALOG_ID)), is(true));
        assertThat(_rule.isAvailableForElement(new ComponentElement(WEBSITE, PATH_TO_COMPONENT, null)), is(true));
    }
}
