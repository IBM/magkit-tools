package com.aperto.magnolia.edittools.action;

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.mockito.jcr.SessionMockUtils;
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
import org.junit.Before;
import org.junit.Test;

import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockMgnlNode;
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
public class EditPageActionTest {

    private static final String GENERIC_DIALOG_ID = "myID";
    private EditPageAction _action;
    private TemplateDefinitionRegistry _templateDefinitionRegistry;
    private FormDialogPresenterFactory _formDialogPresenterFactory;

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        SessionMockUtils.cleanSession();
        mockMgnlNode(PATH_TO_PAGE, WEBSITE, NodeTypes.Page.NAME, stubProperty(NodeTypes.Renderable.TEMPLATE, TEMPLATE_ID),
            stubNode(
                mockNode(PATH_TO_AREA, stubType(NodeTypes.Area.NAME),
                    stubNode(
                        mockNode(PATH_TO_COMPONENT, stubType(NodeTypes.Component.NAME))
                    )
                )
            )
        );

        TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        when(templateDefinition.getDialog()).thenReturn(DIALOG_ID);

        _templateDefinitionRegistry = mock(TemplateDefinitionRegistry.class);
        when(_templateDefinitionRegistry.getTemplateDefinition(TEMPLATE_ID)).thenReturn(templateDefinition);

        _formDialogPresenterFactory = mock(FormDialogPresenterFactory.class);
        when(_formDialogPresenterFactory.createFormDialogPresenter(DIALOG_ID)).thenReturn(mock(FormDialogPresenter.class));
    }

    @Test
    public void testExecutePageElement() throws Exception {
        _action = new EditPageAction(new EditPageActionDefinition(), new PageElement(WEBSITE, PATH_TO_PAGE, DIALOG_ID), mock(SubAppContext.class), mock(EventBus.class), _formDialogPresenterFactory);
        _action.setTemplateDefinitionRegistry(_templateDefinitionRegistry);
        _action.execute();
        verify(_formDialogPresenterFactory).createFormDialogPresenter(DIALOG_ID);
    }

    @Test
    public void testExecuteAreaElement() throws Exception {
        _action = new EditPageAction(new EditPageActionDefinition(), new AreaElement(WEBSITE, PATH_TO_AREA, GENERIC_DIALOG_ID, COMPONENT_ID), mock(SubAppContext.class), mock(EventBus.class), _formDialogPresenterFactory);
        _action.setTemplateDefinitionRegistry(_templateDefinitionRegistry);
        _action.execute();
        verify(_formDialogPresenterFactory).createFormDialogPresenter(DIALOG_ID);
    }

    @Test
    public void testExecuteComponentElement() throws Exception {
        _action = new EditPageAction(new EditPageActionDefinition(), new ComponentElement(WEBSITE, PATH_TO_COMPONENT, GENERIC_DIALOG_ID), mock(SubAppContext.class), mock(EventBus.class), _formDialogPresenterFactory);
        _action.setTemplateDefinitionRegistry(_templateDefinitionRegistry);
        _action.execute();
        verify(_formDialogPresenterFactory).createFormDialogPresenter(DIALOG_ID);
    }
}
