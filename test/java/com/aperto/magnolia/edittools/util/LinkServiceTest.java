package com.aperto.magnolia.edittools.util;

import com.aperto.magnolia.edittools.setup.EditToolsModule;
import com.aperto.magnolia.edittools.setup.PublicLinkConfig;

import info.magnolia.module.site.ConfiguredSite;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.Map;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.WebContextStubbingOperation.stubContextPath;
import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
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

    @Before
    public void setUp() throws Exception {
        mockWebContext(stubContextPath("/author"));
        _linkService = new LinkService() {
            @Override
            protected String createExternalLink(final Node node) {
                return "http://www.domain.ch/author" + getPathIfPossible(node) + ".html";
            }
        };

        final Node site1Node = mockPageNode("/site1/de");
        final Node site2Node = mockPageNode("/site2/de");

        final SiteManager siteManager = mock(SiteManager.class);
        when(siteManager.getAssignedSite(site1Node)).thenReturn(createSite("site1"));
        when(siteManager.getAssignedSite(site2Node)).thenReturn(createSite("site2"));
        _linkService.setSiteManager(siteManager);

        final EditToolsModule editToolsModule = new EditToolsModule();
        final PublicLinkConfig publicLinkConfig = new PublicLinkConfig();
        publicLinkConfig.setExtendedLinkGeneration(true);
        Map<String, String> siteHosts = new HashMap<>();
        siteHosts.put("site2", "http://www.public.ch");
        publicLinkConfig.setSiteHosts(siteHosts);
        editToolsModule.setPublicLinkConfig(publicLinkConfig);
        _linkService.setEditToolsModule(editToolsModule);
    }

    private Site createSite(final String siteName) {
        ConfiguredSite site = new ConfiguredSite();
        site.setName(siteName);
        return site;
    }

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void testNotExistingNodePath() throws Exception {
        assertThat(_linkService.getPublicLink("/site1/not-existing"), equalTo(""));
    }

    @Test
    public void testDefaultLinkRendering() throws Exception {
        assertThat(_linkService.getPublicLink("/site1/de"), equalTo("http://www.domain.ch/site1/de.html"));
    }

    @Test
    public void testSiteLinkRendering() throws Exception {
        assertThat(_linkService.getPublicLink("/site2/de"), equalTo("http://www.public.ch/site2/de.html"));
    }
}