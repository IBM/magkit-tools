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
import info.magnolia.module.site.ConfiguredSite;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the LinkService.
 *
 * @author frank.sommer
 * @since 09.07.2015
 */
public class LinkServiceTest {

    private LinkService _linkService;
    private Node _site1Node;
    private Node _site2Node;

    @BeforeEach
    public void setUp() throws Exception {
        mockWebContext(stubContextPath("/author"));
        _linkService = new LinkService() {
            @Override
            protected String createExternalLink(final Node node) {
                return "https://www.domain.ch/author" + getPathIfPossible(node) + ".html";
            }
        };

        _site1Node = mockPageNode("/site1/de");
        _site2Node = mockPageNode("/site2/de");

        final SiteManager siteManager = mock(SiteManager.class);
        when(siteManager.getAssignedSite(_site1Node)).thenReturn(createSite("site1"));
        when(siteManager.getAssignedSite(_site2Node)).thenReturn(createSite("site2"));
        _linkService.setSiteManager(siteManager);

        final EditToolsModule editToolsModule = new EditToolsModule();
        final PublicLinkConfig publicLinkConfig = new PublicLinkConfig();
        publicLinkConfig.setExtendedLinkGeneration(true);
        Map<String, String> siteHosts = new HashMap<>();
        siteHosts.put("site2", "https://www.public.ch");
        publicLinkConfig.setSiteHosts(siteHosts);
        editToolsModule.setPublicLinkConfig(publicLinkConfig);
        _linkService.setEditToolsModule(editToolsModule);
    }

    private Site createSite(final String siteName) {
        ConfiguredSite site = new ConfiguredSite();
        site.setName(siteName);
        return site;
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
        assertEquals("https://www.domain.ch/site1/de.html", _linkService.getPublicLink(_site1Node));
    }

    @Test
    public void testSiteLinkRendering() {
        assertEquals("https://www.public.ch/site2/de.html", _linkService.getPublicLink(_site2Node));
    }
}
