package de.ibmix.magkit.tools.edit.util;

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

import de.ibmix.magkit.tools.edit.setup.EditToolsModule;
import de.ibmix.magkit.tools.edit.setup.PublicLinkConfig;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.init.MagnoliaConfigurationProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubCharacterEncoding;
import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubMainContentNode;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultExtension;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockContentNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockAssignedSite;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSiteManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LinkService}.
 *
 * @author frank.sommer
 * @since 2015-07-09
 */
public class LinkServiceTest {

    private LinkService _linkService;
    private Node _site1Node;
    private Node _site2Node;
    private EditToolsModule _editToolsModule;

    @BeforeEach
    public void setUp() throws Exception {
        mockServerConfiguration(stubDefaultExtension("html"), stubDefaultBaseUrl("https://test.ibmix.de"));
        mockWebContext(stubContextPath("/author"));
        mockAggregationState(stubCharacterEncoding("UTF-8"));
        _linkService = new LinkService();

        _site1Node = mockPageNode("/site1/de");
        _site2Node = mockPageNode("/site2/de");

        mockAssignedSite(_site1Node, "site1");
        mockAssignedSite(_site2Node, "site2");
        _linkService.setSiteManager(mockSiteManager());

        _editToolsModule = new EditToolsModule();
        final PublicLinkConfig publicLinkConfig = new PublicLinkConfig();
        publicLinkConfig.setExtendedLinkGeneration(true);
        Map<String, String> siteHosts = new HashMap<>();
        siteHosts.put("site2", "https://www.public.ch");
        publicLinkConfig.setSiteHosts(siteHosts);
        _editToolsModule.setPublicLinkConfig(publicLinkConfig);
        _linkService.setEditToolsModule(_editToolsModule);
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    @Test
    public void testNotExistingNodePath() {
        assertEquals("", _linkService.getPublicLink(null));
    }

    @Test
    public void testDefaultLinkRendering() {
        assertEquals("https://test.ibmix.de/site1/de.html", _linkService.getPublicLink(_site1Node));
    }

    @Test
    public void testSiteLinkRendering() {
        assertEquals("https://www.public.ch/site2/de.html", _linkService.getPublicLink(_site2Node));
    }

    @Test
    public void testPublicLinkRenderingForContentNodeUsesPageAncestor() throws Exception {
        Node contentNode = mockContentNode("/site1/de/main/content");
        mockAssignedSite(contentNode, "site1");
        assertEquals("https://test.ibmix.de/site1/de.html", _linkService.getPublicLink(contentNode));
    }

    @Test
    public void testSiteLinkRenderingWithoutExtendedGeneration() {
        PublicLinkConfig newConfig = new PublicLinkConfig();
        newConfig.setExtendedLinkGeneration(false);
        Map<String, String> siteHosts = new HashMap<>();
        siteHosts.put("site2", "https://www.public.ch");
        newConfig.setSiteHosts(siteHosts);
        _editToolsModule.setPublicLinkConfig(newConfig);
        assertEquals("https://test.ibmix.de/site2/de.html", _linkService.getPublicLink(_site2Node));
    }

    @Test
    public void testDefaultLinkRenderingWithoutContextPath() throws Exception {
        mockWebContext(stubContextPath(""));
        LinkService linkServiceNoCtx = new LinkService();
        mockAssignedSite(_site1Node, "site1");
        linkServiceNoCtx.setSiteManager(mockSiteManager());
        linkServiceNoCtx.setEditToolsModule(_editToolsModule);
        assertEquals("https://test.ibmix.de/site1/de.html", linkServiceNoCtx.getPublicLink(_site1Node));
    }

    @Test
    void getAuthorBasePath() {
        MagnoliaConfigurationProperties properties = mock(MagnoliaConfigurationProperties.class);
        when(properties.getProperty("magnolia.author.basePath")).thenReturn("/author");
        _linkService.setMagnoliaConfigurationProperties(properties);
        assertEquals("/author", _linkService.getAuthorBasePath());

        // call again to test caching
        when(properties.getProperty("magnolia.author.basePath")).thenReturn("/other");
        assertEquals("/author", _linkService.getAuthorBasePath());
    }

    @Test
    void createPageEditorLink() throws RepositoryException {
        MagnoliaConfigurationProperties properties = mock(MagnoliaConfigurationProperties.class);
        when(properties.getProperty("magnolia.author.basePath")).thenReturn("/author");
        _linkService.setMagnoliaConfigurationProperties(properties);
        assertEquals("", _linkService.createPageEditorLink());

        when(properties.getProperty("magnolia.author.basePath")).thenReturn("/author");
        AggregationState state = mockAggregationState();
        assertEquals("", _linkService.createPageEditorLink());

        stubMainContentNode("/site1/de").of(state);
        assertEquals("/author/.magnolia/admincentral#app:pages:detail;/site1/de:edit", _linkService.createPageEditorLink());
    }

    @Test
    void createPageEditorLinkWithoutBasePath() {
        MagnoliaConfigurationProperties properties = mock(MagnoliaConfigurationProperties.class);
        _linkService.setMagnoliaConfigurationProperties(properties);
        assertEquals("", _linkService.createPageEditorLink());
    }

    @Test
    void createExternalLink() throws RepositoryException {
        assertNull(_linkService.createExternalLink(null));
        Node page = mockPageNode("/site1/de");
        assertEquals("https://test.ibmix.de/site1/de.html", _linkService.createExternalLink(page));
    }
}
