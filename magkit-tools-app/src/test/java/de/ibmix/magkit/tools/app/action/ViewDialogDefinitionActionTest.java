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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import de.ibmix.magkit.test.cms.templating.TemplateMockUtils;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
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
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.node.PageNodeStubbingOperation.stubTemplate;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ViewDialogDefinitionAction}.
 *
 * @author wolf.bubenik
 * @since 2025-11-14
 */
class ViewDialogDefinitionActionTest {

    private ViewDialogDefinitionActionDefinition _definition;
    private AbstractJcrNodeAdapter _item;
    private LocationController _locationController;
    private TemplateDefinitionRegistry _templateRegistry;
    private ViewDialogDefinitionAction _action;

    @BeforeEach
    void setUp() throws RepositoryException {
        mockAggregationState(stubCharacterEncoding("UTF-8"));
        _definition = mock(ViewDialogDefinitionActionDefinition.class);
        _item = mock(AbstractJcrNodeAdapter.class);
        _locationController = mock(LocationController.class);
        _templateRegistry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        _action = new ViewDialogDefinitionAction(_definition, _item, _locationController, _templateRegistry);
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void executeWithValidDialogId() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail", stubDialog("dialogModule:dialogs/testDialog")));
        when(_item.getJcrItem()).thenReturn(node);

        _action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(_locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("dialog~dialogModule~dialogs/testDialog", capturedLocation.getNodePath());
    }

    @Test
    void executeWithDialogIdContainingMultipleColons() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail", stubDialog("module:nested:path:dialog")));
        when(_item.getJcrItem()).thenReturn(node);

        _action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(_locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("dialog~module~nested", capturedLocation.getNodePath());
    }

    @Test
    void executeWithNullItem() {
        _action = new ViewDialogDefinitionAction(_definition, null, _locationController, _templateRegistry);

        _action.execute();

        verify(_locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithNullJcrItem() {
        when(_item.getJcrItem()).thenReturn(null);

        _action.execute();

        verify(_locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithNullTemplateRegistry() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail"));
        when(_item.getJcrItem()).thenReturn(node);
        _action = new ViewDialogDefinitionAction(_definition, _item, _locationController, null);

        _action.execute();

        verify(_locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithNullTemplateDefinitionProvider() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail"));
        when(_item.getJcrItem()).thenReturn(node);
        when(_templateRegistry.getProvider(anyString())).thenReturn(null);

        _action.execute();

        verify(_locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithEmptyDialogId() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail", stubDialog("")));
        when(_item.getJcrItem()).thenReturn(node);

        _action.execute();

        verify(_locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithNullDialogId() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail"));
        when(_item.getJcrItem()).thenReturn(node);

        _action.execute();

        verify(_locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithDialogIdWithoutColon() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail", stubDialog("invalidDialog")));
        when(_item.getJcrItem()).thenReturn(node);

        _action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(_locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("dialog~invalidDialog~", capturedLocation.getNodePath());
    }
}

