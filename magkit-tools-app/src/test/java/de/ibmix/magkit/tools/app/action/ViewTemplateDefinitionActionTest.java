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

    private ViewTemplateDefinitionActionDefinition _definition;
    private LocationController _locationController;
    private AbstractJcrNodeAdapter _item;
    private ViewTemplateDefinitionAction _action;

    @BeforeEach
    void setUp() throws RepositoryException {
        mockAggregationState(stubCharacterEncoding("UTF-8"));
        _definition = mock(ViewTemplateDefinitionActionDefinition.class);
        _locationController = mock(LocationController.class);
        _item = mock(AbstractJcrNodeAdapter.class);
        _action = new ViewTemplateDefinitionAction(_definition, _locationController, _item);
    }

    @AfterEach
    void tearDown() {
        cleanContext();
    }

    @Test
    void executeWithValidTemplateId() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail"));
        when(_item.getJcrItem()).thenReturn(node);

        _action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(_locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("template~myModule~pages/detail", capturedLocation.getNodePath());
    }

    @Test
    void executeWithTemplateIdContainingMultipleColons() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail"));
        when(_item.getJcrItem()).thenReturn(node);

        _action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(_locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("template~myModule~pages/detail", capturedLocation.getNodePath());
    }

    @Test
    void executeWithNullItem() {
        _action = new ViewTemplateDefinitionAction(_definition, _locationController, null);

        _action.execute();

        verify(_locationController, never()).goTo(org.mockito.ArgumentMatchers.any(BrowserLocation.class));
    }

    @Test
    void executeWithNullJcrItem() {
        when(_item.getJcrItem()).thenReturn(null);

        _action.execute();

        verify(_locationController, never()).goTo(org.mockito.ArgumentMatchers.any(BrowserLocation.class));
    }

    @Test
    void executeWithEmptyTemplateId() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate(""));
        when(_item.getJcrItem()).thenReturn(node);

        _action.execute();

        verify(_locationController, never()).goTo(org.mockito.ArgumentMatchers.any(BrowserLocation.class));
    }

    @Test
    void executeWithNullTemplateId() throws Exception {
        Node node = mockNode("website", "/test/path");
        when(_item.getJcrItem()).thenReturn(node);

        _action.execute();

        verify(_locationController, never()).goTo(org.mockito.ArgumentMatchers.any(BrowserLocation.class));
    }

    @Test
    void executeWithTemplateIdWithoutColon() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("invalidTemplate/detail"));
        when(_item.getJcrItem()).thenReturn(node);

        _action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(_locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("template~invalidTemplate/detail~", capturedLocation.getNodePath());
    }
}

