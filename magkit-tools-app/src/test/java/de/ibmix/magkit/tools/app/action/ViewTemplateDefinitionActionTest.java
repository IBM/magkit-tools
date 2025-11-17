package de.ibmix.magkit.tools.app.action;

/*-
 * #%L
 * magkit-tools-app
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

import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubCharacterEncoding;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.node.PageNodeStubbingOperation.stubTemplate;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ViewTemplateDefinitionAction}.
 *
 * @author wolf.bubenik
 * @since 2025-11-14
 */
class ViewTemplateDefinitionActionTest {

    private ViewTemplateDefinitionActionDefinition definition;
    private LocationController locationController;
    private AbstractJcrNodeAdapter item;
    private ViewTemplateDefinitionAction action;

    @BeforeEach
    void setUp() throws RepositoryException {
        mockAggregationState(stubCharacterEncoding("UTF-8"));
        definition = mock(ViewTemplateDefinitionActionDefinition.class);
        locationController = mock(LocationController.class);
        item = mock(AbstractJcrNodeAdapter.class);
        action = new ViewTemplateDefinitionAction(definition, locationController, item);
    }

    @AfterEach
    void tearDown() {
        cleanContext();
    }

    @Test
    void executeWithValidTemplateId() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail"));
        when(item.getJcrItem()).thenReturn(node);

        action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("template~myModule~pages/detail", capturedLocation.getNodePath());
    }

    @Test
    void executeWithTemplateIdContainingMultipleColons() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail"));
        when(item.getJcrItem()).thenReturn(node);

        action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("template~myModule~pages/detail", capturedLocation.getNodePath());
    }

    @Test
    void executeWithNullItem() {
        action = new ViewTemplateDefinitionAction(definition, locationController, null);

        action.execute();

        verify(locationController, never()).goTo(org.mockito.ArgumentMatchers.any(BrowserLocation.class));
    }

    @Test
    void executeWithNullJcrItem() {
        when(item.getJcrItem()).thenReturn(null);

        action.execute();

        verify(locationController, never()).goTo(org.mockito.ArgumentMatchers.any(BrowserLocation.class));
    }

    @Test
    void executeWithEmptyTemplateId() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate(""));
        when(item.getJcrItem()).thenReturn(node);

        action.execute();

        verify(locationController, never()).goTo(org.mockito.ArgumentMatchers.any(BrowserLocation.class));
    }

    @Test
    void executeWithNullTemplateId() throws Exception {
        Node node = mockNode("website", "/test/path");
        when(item.getJcrItem()).thenReturn(node);

        action.execute();

        verify(locationController, never()).goTo(org.mockito.ArgumentMatchers.any(BrowserLocation.class));
    }

    @Test
    void executeWithTemplateIdWithoutColon() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("invalidTemplate/detail"));
        when(item.getJcrItem()).thenReturn(node);

        action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("template~invalidTemplate/detail~", capturedLocation.getNodePath());
    }
}

