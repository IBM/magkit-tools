package de.ibmix.magkit.tools.edit.ui;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
 * %%
 * Copyright (C) 2025 IBM iX
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
import de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils;
import de.ibmix.magkit.tools.edit.setup.EditToolsModule;
import de.ibmix.magkit.tools.edit.setup.StatusBarConfig;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AssetUsageItemDescriber} covering item description logic and reference counting across workspaces.
 *
 * @author wolf.bubenik
 * @since 2025-11-19
 */
public class AssetUsageItemDescriberTest {

    private Provider<EditToolsModule> _moduleProvider;
    private AssetUsageItemDescriber _describer;

    @BeforeEach
    public void setUp() {
        _moduleProvider = mock(Provider.class);
        EditToolsModule editToolsModule = new EditToolsModule();
        when(_moduleProvider.get()).thenReturn(editToolsModule);
        _describer = new AssetUsageItemDescriber(_moduleProvider);
    }

    @AfterEach
    public void tearDown() {
        ContextMockUtils.cleanContext();
    }

    /**
     * Verifies apply returns super description for multiple items.
     */
    @Test
    public void testApplyMultipleItemsDelegatesToSuper() {
        JcrNodeWrapper node1 = mock(JcrNodeWrapper.class);
        JcrNodeWrapper node2 = mock(JcrNodeWrapper.class);
        Collection<JcrNodeWrapper> items = Arrays.asList(node1, node2);
        String description = _describer.apply(items);
        assertTrue(description.contains("2 items selected"));
    }

    /**
     * Verifies applySingle returns path only when no config set.
     */
    @Test
    public void testApplySingleWithoutUsageWorkspaces() throws RepositoryException {
        Node node = MagnoliaNodeMockUtils.mockContentNode("assets", "/assets/image.jpg");
        String identifier = node.getIdentifier();
        String path = node.getPath();
        JcrNodeWrapper wrapper = mock(JcrNodeWrapper.class);
        when(wrapper.getWrappedNode()).thenReturn(node);
        when(wrapper.getPath()).thenReturn(path);
        when(wrapper.getIdentifier()).thenReturn(identifier);

        EditToolsModule module = new EditToolsModule();
        StatusBarConfig cfg = new StatusBarConfig();
        cfg.setAssetUsageWorkspaces(Collections.emptyList());
        module.setStatusBarConfig(cfg);
        when(_moduleProvider.get()).thenReturn(module);

        String description = _describer.applySingle(wrapper);
        assertEquals(path, description);
    }

    /**
     * Verifies applySingle returns path with usage count when workspaces configured.
     */
    @Test
    public void testApplySingleWithUsageCount() throws Exception {
        Node node = mockNode("assets", "/assets/image2.jpg");
        String identifier = node.getIdentifier();
        String path = node.getPath();
        JcrNodeWrapper wrapper = mock(JcrNodeWrapper.class);
        when(wrapper.getWrappedNode()).thenReturn(node);
        when(wrapper.getPath()).thenReturn(path);
        when(wrapper.getIdentifier()).thenReturn(identifier);

        EditToolsModule module = new EditToolsModule();
        StatusBarConfig cfg = new StatusBarConfig();
        cfg.setAssetUsageWorkspaces(List.of("website", "shop"));
        module.setStatusBarConfig(cfg);
        when(_moduleProvider.get()).thenReturn(module);

        Node websitePage = MagnoliaNodeMockUtils.mockPageNode("/site/page1");
        Node shopPage = MagnoliaNodeMockUtils.mockContentNode("shop", "/shop/page1");
        String query = "SELECT * FROM [nt:base] AS base WHERE CONTAINS(base.*, 'jcr:" + identifier + "')";
        ContextMockUtils.mockQueryResult("website", Query.JCR_SQL2, query, websitePage);
        ContextMockUtils.mockQueryResult("shop", Query.JCR_SQL2, query, shopPage, shopPage);

        String description = _describer.applySingle(wrapper);
        assertEquals("/assets/image2.jpg (2)", description);
    }

    /**
     * Verifies reference counting handles repository exception gracefully.
     */
    @Test
    public void testGetReferencesHandlesRepositoryException() throws Exception {
        Node node = mockNode("assets", "/assets/image2.jpg");
        String identifier = node.getIdentifier();
        String path = node.getPath();
        JcrNodeWrapper wrapper = mock(JcrNodeWrapper.class);
        when(wrapper.getWrappedNode()).thenReturn(node);
        when(wrapper.getPath()).thenReturn(path);
        when(wrapper.getIdentifier()).thenReturn(identifier);

        EditToolsModule module = new EditToolsModule();
        StatusBarConfig cfg = new StatusBarConfig();
        cfg.setAssetUsageWorkspaces(List.of("broken"));
        module.setStatusBarConfig(cfg);
        when(_moduleProvider.get()).thenReturn(module);

        String queryString = "SELECT * FROM [nt:base] AS base WHERE CONTAINS(base.*, 'jcr:" + identifier + "')";
        Query query = ContextMockUtils.mockQuery("broken", Query.JCR_SQL2, queryString);
        when(query.execute()).thenThrow(new RepositoryException("fail"));

        int count = _describer.getReferencesToWorkspacesCount(wrapper, cfg.getAssetUsageWorkspaces());
        assertEquals(0, count);
    }
}
