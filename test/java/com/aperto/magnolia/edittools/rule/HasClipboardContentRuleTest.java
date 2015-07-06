package com.aperto.magnolia.edittools.rule;

import com.aperto.magkit.mockito.ContextMockUtils;
import com.aperto.magkit.mockito.MagnoliaNodeMockUtils;
import com.aperto.magkit.mockito.WebContextStubbingOperation;
import com.aperto.magnolia.edittools.action.CopyNodeAction;
import info.magnolia.context.Context;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import java.util.Locale;

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
        ContextMockUtils.cleanContext();
        _rule = new HasClipboardContentRule(mock(PageEditorPresenter.class));
    }

    @Test
    public void testIsAvailableForElement() throws Exception {
        Node node = MagnoliaNodeMockUtils.mockComponentNode("test");
        JcrItemAdapter adapter = mock(JcrItemAdapter.class);
        when(adapter.isNode()).thenReturn(true);
        when(adapter.getJcrItem()).thenReturn(node);

        ContextMockUtils.mockWebContext(WebContextStubbingOperation.stubAttribute(CopyNodeAction.class.getName(), adapter, Context.SESSION_SCOPE));
        assertThat(_rule.isAvailableForElement(null), is(Boolean.TRUE));
    }

    @Test
    public void testIsNotAvailableForElement() throws Exception {
        ContextMockUtils.mockWebContext(Locale.GERMAN);
        assertThat(_rule.isAvailableForElement(null), is(Boolean.FALSE));

        ContextMockUtils.mockWebContext(WebContextStubbingOperation.stubAttribute(CopyNodeAction.class.getName(), mock(JcrItemAdapter.class), Context.SESSION_SCOPE));
        assertThat(_rule.isAvailableForElement(null), is(Boolean.FALSE));
    }
}
