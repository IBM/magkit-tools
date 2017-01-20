package com.aperto.magnolia.edittools.rule;

import com.aperto.magnolia.edittools.action.CopyNodeAction;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import java.util.Locale;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockComponentNode;
import static com.aperto.magkit.mockito.WebContextStubbingOperation.stubAttribute;
import static info.magnolia.context.Context.SESSION_SCOPE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link HasClipboardContentRule}.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 06.07.2015
 */
public class HasClipboardContentRuleTest {

    private HasClipboardContentRule _rule;

    @Before
    public void setUp() throws Exception {
        _rule = new HasClipboardContentRule(mock(PageEditorPresenter.class));
    }

    @After
    public void cleanUp() throws Exception {
        cleanContext();
    }

    @Test
    public void testIsAvailableForElement() throws Exception {
        Node node = mockComponentNode("test");
        JcrItemAdapter adapter = mock(JcrItemAdapter.class);
        when(adapter.isNode()).thenReturn(true);
        when(adapter.getJcrItem()).thenReturn(node);

        mockWebContext(stubAttribute(CopyNodeAction.class.getName(), adapter, SESSION_SCOPE));
        assertThat(_rule.isAvailableForElement(null), is(Boolean.TRUE));
    }

    @Test
    public void testIsNotAvailableForElement() throws Exception {
        mockWebContext(Locale.GERMAN);
        assertThat(_rule.isAvailableForElement(null), is(Boolean.FALSE));

        mockWebContext(stubAttribute(CopyNodeAction.class.getName(), mock(JcrItemAdapter.class), SESSION_SCOPE));
        assertThat(_rule.isAvailableForElement(null), is(Boolean.FALSE));
    }
}
