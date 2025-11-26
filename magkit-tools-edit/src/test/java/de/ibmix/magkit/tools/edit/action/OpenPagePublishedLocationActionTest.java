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

import com.vaadin.server.Page;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;
import de.ibmix.magkit.tools.edit.util.LinkService;
import info.magnolia.ui.ValueContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import java.util.Optional;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OpenPagePublishedLocationAction}.
 *
 * @author wolf.bubenik
 * @since 2025-11-18
 */
class OpenPagePublishedLocationActionTest {

    private ValueContext<Node> _valueContext;
    private LinkService _linkService;
    private OpenPagePublishedLocationActionDefinition _definition;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        _valueContext = mock(ValueContext.class);
        _linkService = mock(LinkService.class);
        _definition = new OpenPagePublishedLocationActionDefinition();
    }

    @Test
    void testExecuteWithPageNode() throws Exception {
        Node pageNode = mockPageNode("/test/page");
        Page currentPage = mockCurrentPage();
        when(_valueContext.getSingle()).thenReturn(Optional.of(pageNode));
        when(_linkService.getPublicLink(pageNode)).thenReturn("https://example.com/test/page.html");

        new OpenPagePublishedLocationAction(_definition, _valueContext, _linkService).execute();

        verify(_linkService).getPublicLink(pageNode);
        verify(currentPage).open("https://example.com/test/page.html", "_blank");
    }

    @Test
    void testExecuteWithEmptyContext() {
        when(_valueContext.getSingle()).thenReturn(Optional.empty());

        new OpenPagePublishedLocationAction(_definition, _valueContext, _linkService).execute();

        verify(_linkService, times(0)).getPublicLink(null);
    }

    @Test
    void testExecuteWithNullLinkResult() throws Exception {
        Node pageNode = mockPageNode("/test/page");
        when(_valueContext.getSingle()).thenReturn(Optional.of(pageNode));
        when(_linkService.getPublicLink(pageNode)).thenReturn(null);

        new OpenPagePublishedLocationAction(_definition, _valueContext, _linkService).execute();

        verify(_linkService).getPublicLink(pageNode);
    }

    @Test
    void testExecuteWithEmptyLinkResult() throws Exception {
        Page currentPage = mockCurrentPage();

        Node pageNode = mockPageNode("/test/page");
        when(_valueContext.getSingle()).thenReturn(Optional.of(pageNode));

        new OpenPagePublishedLocationAction(_definition, _valueContext, _linkService).execute();

        verify(_linkService).getPublicLink(pageNode);
        verify(currentPage, never()).open("", "_blank");
    }

    Page mockCurrentPage() {
        Page currentPage = mock(Page.class);
        UI ui = mock(UI.class);
        when(ui.getPage()).thenReturn(currentPage);
        CurrentInstance.set(UI.class, ui);
        return currentPage;
    }
}

