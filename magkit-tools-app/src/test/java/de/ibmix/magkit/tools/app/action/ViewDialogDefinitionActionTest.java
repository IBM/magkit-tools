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

    private ViewDialogDefinitionActionDefinition definition;
    private AbstractJcrNodeAdapter item;
    private LocationController locationController;
    private TemplateDefinitionRegistry templateRegistry;
    private ViewDialogDefinitionAction action;

    @BeforeEach
    void setUp() throws RepositoryException {
        mockAggregationState(stubCharacterEncoding("UTF-8"));
        definition = mock(ViewDialogDefinitionActionDefinition.class);
        item = mock(AbstractJcrNodeAdapter.class);
        locationController = mock(LocationController.class);
        templateRegistry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        action = new ViewDialogDefinitionAction(definition, item, locationController, templateRegistry);
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void executeWithValidDialogId() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail", stubDialog("dialogModule:dialogs/testDialog")));
        when(item.getJcrItem()).thenReturn(node);

        action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("dialog~dialogModule~dialogs/testDialog", capturedLocation.getNodePath());
    }

    @Test
    void executeWithDialogIdContainingMultipleColons() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail", stubDialog("module:nested:path:dialog")));
        when(item.getJcrItem()).thenReturn(node);

        action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("dialog~module~nested", capturedLocation.getNodePath());
    }

    @Test
    void executeWithNullItem() {
        action = new ViewDialogDefinitionAction(definition, null, locationController, templateRegistry);

        action.execute();

        verify(locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithNullJcrItem() {
        when(item.getJcrItem()).thenReturn(null);

        action.execute();

        verify(locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithNullTemplateRegistry() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail"));
        when(item.getJcrItem()).thenReturn(node);
        action = new ViewDialogDefinitionAction(definition, item, locationController, null);

        action.execute();

        verify(locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithNullTemplateDefinitionProvider() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail"));
        when(item.getJcrItem()).thenReturn(node);
        when(templateRegistry.getProvider(anyString())).thenReturn(null);

        action.execute();

        verify(locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithEmptyDialogId() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail", stubDialog("")));
        when(item.getJcrItem()).thenReturn(node);

        action.execute();

        verify(locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithNullDialogId() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail"));
        when(item.getJcrItem()).thenReturn(node);

        action.execute();

        verify(locationController, never()).goTo(any(BrowserLocation.class));
    }

    @Test
    void executeWithDialogIdWithoutColon() throws Exception {
        Node node = mockPageNode("/test/path", stubTemplate("myModule:pages/detail", stubDialog("invalidDialog")));
        when(item.getJcrItem()).thenReturn(node);

        action.execute();

        ArgumentCaptor<BrowserLocation> locationCaptor = ArgumentCaptor.forClass(BrowserLocation.class);
        verify(locationController).goTo(locationCaptor.capture());

        BrowserLocation capturedLocation = locationCaptor.getValue();
        assertEquals("definitions-app", capturedLocation.getAppName());
        assertEquals("overview", capturedLocation.getSubAppId());
        assertEquals("dialog~invalidDialog~", capturedLocation.getNodePath());
    }
}

