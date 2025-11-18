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

import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.ContentBrowserSubApp.BrowserLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Optional;

import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubCharacterEncoding;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockContentNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OpenPageBrowserLocationAction}.
 *
 * @author wolf.bubenik
 * @since 2025-11-18
 */
class OpenPageBrowserLocationActionTest {

    private LocationController _locationController;
    private ValueContext<Node> _valueContext;
    private OpenPageBrowserLocationActionDefinition _definition;

    @BeforeEach
    void setUp() throws RepositoryException {
        mockAggregationState(stubCharacterEncoding("UTF-8"));
        _locationController = mock(LocationController.class);
        _valueContext = mock(ValueContext.class);
        _definition = new OpenPageBrowserLocationActionDefinition();
        _definition.setAppName("pages");
        _definition.setSubAppId("browser");
    }

    @Test
    void testExecuteWithPageNode() throws Exception {
        Node pageNode = mockPageNode("/test/page");
        when(_valueContext.getSingle()).thenReturn(Optional.of(pageNode));

        OpenPageBrowserLocationAction action = new OpenPageBrowserLocationAction(_definition, _valueContext, _locationController);
        action.execute();

        verify(_locationController).goTo(any(BrowserLocation.class));
    }

    @Test
    void testExecuteWithContentNodeUnderPage() throws Exception {
        mockPageNode("/test/page");
        Node contentNode = mockContentNode("/test/page/content");
        when(_valueContext.getSingle()).thenReturn(Optional.of(contentNode));

        OpenPageBrowserLocationAction action = new OpenPageBrowserLocationAction(_definition, _valueContext, _locationController);
        action.execute();

        verify(_locationController).goTo(any(BrowserLocation.class));
    }

    @Test
    void testExecuteWithEmptyContext() throws ActionExecutionException {
        when(_valueContext.getSingle()).thenReturn(Optional.empty());

        OpenPageBrowserLocationAction action = new OpenPageBrowserLocationAction(_definition, _valueContext, _locationController);
        action.execute();

        verify(_locationController).goTo(any(BrowserLocation.class));
    }

    @Test
    void testGetNodePathWithPageNode() throws Exception {
        Node pageNode = mockPageNode("/test/page");
        when(_valueContext.getSingle()).thenReturn(Optional.of(pageNode));

        OpenPageBrowserLocationAction action = new OpenPageBrowserLocationAction(_definition, _valueContext, _locationController);
        String nodePath = action.getNodePath();

        assertEquals("/test/page", nodePath);
    }

    @Test
    void testGetNodePathWithContentNode() throws Exception {
        mockPageNode("/test/page");
        Node contentNode = mockContentNode("/test/page/content");
        when(_valueContext.getSingle()).thenReturn(Optional.of(contentNode));

        OpenPageBrowserLocationAction action = new OpenPageBrowserLocationAction(_definition, _valueContext, _locationController);
        String nodePath = action.getNodePath();

        assertEquals("/test/page", nodePath);
    }

    @Test
    void testGetNodePathWithEmptyContext() {
        when(_valueContext.getSingle()).thenReturn(Optional.empty());

        OpenPageBrowserLocationAction action = new OpenPageBrowserLocationAction(_definition, _valueContext, _locationController);
        String nodePath = action.getNodePath();

        assertEquals("", nodePath);
    }

    @Test
    void testExecuteWithRootPage() throws Exception {
        Node rootPage = mockPageNode("/rootPage");
        when(_valueContext.getSingle()).thenReturn(Optional.of(rootPage));

        OpenPageBrowserLocationAction action = new OpenPageBrowserLocationAction(_definition, _valueContext, _locationController);
        action.execute();

        verify(_locationController).goTo(any(BrowserLocation.class));
    }
}

