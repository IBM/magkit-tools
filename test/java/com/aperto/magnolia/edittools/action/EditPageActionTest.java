package com.aperto.magnolia.edittools.action;

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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubType;
import static com.aperto.magnolia.edittools.rule.IsElementEditableRuleTest.COMPONENT_ID;
import static com.aperto.magnolia.edittools.rule.IsElementEditableRuleTest.DIALOG_ID;
import static com.aperto.magnolia.edittools.rule.IsElementEditableRuleTest.PATH_TO_AREA;
import static com.aperto.magnolia.edittools.rule.IsElementEditableRuleTest.PATH_TO_COMPONENT;
import static com.aperto.magnolia.edittools.rule.IsElementEditableRuleTest.PATH_TO_PAGE;
import static com.aperto.magnolia.edittools.rule.IsElementEditableRuleTest.TEMPLATE_ID;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Philipp Güttler (Aperto AG)
 * @since 01.07.2015
 */
@RunWith(MockitoJUnitRunner.class)
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
        mockPageNode(PATH_TO_PAGE, stubProperty(NodeTypes.Renderable.TEMPLATE, TEMPLATE_ID),
            stubNode(
                mockNode(PATH_TO_AREA, stubType(NodeTypes.Area.NAME),
                    stubNode(
                        mockNode(PATH_TO_COMPONENT, stubType(NodeTypes.Component.NAME))
                    )
                )
            )
        );

        when(_provider.get()).thenReturn(_templateDefinition);
        when(_templateDefinition.getDialog()).thenReturn(DIALOG_ID);
        when(_templateDefinitionRegistry.getProvider(TEMPLATE_ID)).thenReturn(_provider);
        when(_formDialogPresenterFactory.createFormDialogPresenter(DIALOG_ID)).thenReturn(mock(FormDialogPresenter.class));
    }

    @After
    public void cleanUp() throws Exception {
        cleanContext();
    }

    @Test
    public void testExecutePageElement() throws Exception {
        _action = new EditPageAction(null, new EditPageActionDefinition(), new PageElement(WEBSITE, PATH_TO_PAGE, DIALOG_ID), mock(SubAppContext.class), mock(EventBus.class), _formDialogPresenterFactory);
        _action.setTemplateDefinitionRegistry(_templateDefinitionRegistry);
        _action.execute();
        verify(_formDialogPresenterFactory).createFormDialogPresenter(DIALOG_ID);
    }

    @Test
    public void testExecuteAreaElement() throws Exception {
        _action = new EditPageAction(null, new EditPageActionDefinition(), new AreaElement(WEBSITE, PATH_TO_AREA, GENERIC_DIALOG_ID, COMPONENT_ID), mock(SubAppContext.class), mock(EventBus.class), _formDialogPresenterFactory);
        _action.setTemplateDefinitionRegistry(_templateDefinitionRegistry);
        _action.execute();
        verify(_formDialogPresenterFactory).createFormDialogPresenter(DIALOG_ID);
    }

    @Test
    public void testExecuteComponentElement() throws Exception {
        _action = new EditPageAction(null, new EditPageActionDefinition(), new ComponentElement(WEBSITE, PATH_TO_COMPONENT, GENERIC_DIALOG_ID), mock(SubAppContext.class), mock(EventBus.class), _formDialogPresenterFactory);
        _action.setTemplateDefinitionRegistry(_templateDefinitionRegistry);
        _action.execute();
        verify(_formDialogPresenterFactory).createFormDialogPresenter(DIALOG_ID);
    }
}
