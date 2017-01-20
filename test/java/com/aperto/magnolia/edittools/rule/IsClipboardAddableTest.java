package com.aperto.magnolia.edittools.rule;

import com.aperto.magnolia.edittools.action.CopyNodeAction;
import com.vaadin.data.Property;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.mockito.WebContextStubbingOperation.stubAttribute;
import static info.magnolia.context.Context.SESSION_SCOPE;
import static info.magnolia.jcr.util.NodeTypes.Renderable.TEMPLATE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Philipp Güttler (Aperto AG)
 * @since 06.07.2015
 */
@RunWith(MockitoJUnitRunner.class)
public class IsClipboardAddableTest {
    private static final String EXTERNAL_LINK_ID = "my-module:links/externalLink";
    private static final String EXTERNAL_LINK_NEW_ID = "my-module:links/externalLinkNew";
    private static final String TEXT_TEASER_ID = "my-module:teasers/textTeaser";

    private IsClipboardAddable _rule;

    @Mock
    private Property<String> _templateId;

    @Before
    public void setUp() throws Exception {
        _rule = new IsClipboardAddable(mock(PageEditorPresenter.class));

        when(_templateId.getValue()).thenReturn(EXTERNAL_LINK_ID);

        JcrItemAdapter adapter = mock(JcrItemAdapter.class);
        when(adapter.getItemProperty(TEMPLATE)).thenReturn(_templateId);

        mockWebContext(stubAttribute(CopyNodeAction.class.getName(), adapter, SESSION_SCOPE));
    }

    @After
    public void cleanUp() throws Exception {
        cleanContext();
    }

    @Test
    public void testIsAvailableForElement() throws Exception {
        AreaElement element = new AreaElement(EMPTY, EMPTY, EMPTY, TEXT_TEASER_ID + "," + EXTERNAL_LINK_ID);
        assertThat(_rule.isAvailableForElement(element), is(TRUE));
    }

    @Test
    public void testIsNotAvailableForElement() throws Exception {
        AreaElement element = new AreaElement(EMPTY, EMPTY, EMPTY, TEXT_TEASER_ID);
        assertThat(_rule.isAvailableForElement(element), is(FALSE));
    }

    @Test
    public void testIsAvailableForEmptyArea() throws Exception {
        AreaElement element = new AreaElement(EMPTY, EMPTY, EMPTY, EMPTY);
        assertThat(_rule.isAvailableForElement(element), is(FALSE));
    }

    @Test
    public void testIsNotAvailableForAlikeNamedElement() throws Exception {
        AreaElement element = new AreaElement(EMPTY, EMPTY, EMPTY, TEXT_TEASER_ID + "," + EXTERNAL_LINK_NEW_ID);
        assertThat(_rule.isAvailableForElement(element), is(FALSE));
    }
}
