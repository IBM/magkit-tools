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

import com.vaadin.server.Page;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.*;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ViewSourceAction}.
 *
 * @author wolf.bubenik
 * @since 2025-11-14
 */
class ViewSourceActionTest {

    private ViewSourceActionDefinition _definition;
    private AbstractJcrNodeAdapter _item;
    private ViewSourceAction _action;

    @BeforeEach
    void setUp() {
        mockServerConfiguration(stubDefaultBaseUrl("https://test.ibmix.de"), stubDefaultExtension("html"));
        _definition = mock(ViewSourceActionDefinition.class);
        _item = mock(AbstractJcrNodeAdapter.class);

        _action = new ViewSourceAction(_definition, _item);
    }

    @AfterEach
    void tearDown() {
        CurrentInstance.clearAll();
        cleanContext();
    }

    @Test
    void executeWithValidNode() throws Exception {
        Node node = mockPageNode("/test/page");
        when(_item.getJcrItem()).thenReturn(node);
        Page page = mockCurrentPage();
        _action.execute();

        verify(page).open(eq("https://test.ibmix.de/test/page.html"), eq("_blank"), eq(false));
    }

    @Test
    void executeWithNullItem() {
        _action = new ViewSourceAction(_definition, null);
        Page page = mockCurrentPage();
        _action.execute();

        verify(page, never()).open(anyString(), anyString(), anyBoolean());
    }

    @Test
    void executeWithNullJcrItem() {
        when(_item.getJcrItem()).thenReturn(null);
        Page page = mockCurrentPage();
        _action.execute();

        verify(page, never()).open(anyString(), anyString(), anyBoolean());
    }

    private Page mockCurrentPage() {
        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        when(ui.getPage()).thenReturn(page);
        CurrentInstance.set(UI.class, ui);
        return page;
    }
}

