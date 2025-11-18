package de.ibmix.magkit.tools.app;

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

import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseApp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ToolsBaseApp.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-18
 */
class ToolsBaseAppTest {

    private static final String APP_NAME = "tools";
    private static final String VERSION_PRUNE_SUBAPP = "versionPrune";
    private static final String JCR_QUERIES_SUBAPP = "jcrQueries";

    private AppContext _appContext;
    private AppView _appView;
    private AppDescriptor _appDescriptor;
    private ToolsBaseApp _toolsBaseApp;

    @BeforeEach
    void setUp() {
        mockComponentInstance(SimpleTranslator.class);

        _appContext = mock(AppContext.class);
        _appView = mock(AppView.class);
        _appDescriptor = mock(AppDescriptor.class);

        when(_appContext.getAppDescriptor()).thenReturn(_appDescriptor);
        when(_appDescriptor.getName()).thenReturn(APP_NAME);

        _toolsBaseApp = new ToolsBaseApp(_appContext, _appView);
    }

    @AfterEach
    void tearDown() {
        ComponentsMockUtils.clearComponentProvider();
    }

    @Test
    void constructorCreatesInstance() {
        assertInstanceOf(BaseApp.class, _toolsBaseApp);
    }

    @Test
    void startCallsSuperStart() {
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME);

        _toolsBaseApp.start(location);

        verify(_appContext, times(3)).getAppDescriptor();
    }

    @Test
    void startOpensVersionPruneSubApp() {
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);

        _toolsBaseApp.start(location);

        verify(_appContext, times(2)).openSubApp(locationCaptor.capture());

        Location versionPruneLocation = locationCaptor.getAllValues().get(0);
        assertEquals(APP_NAME, versionPruneLocation.getAppName());
        assertEquals(VERSION_PRUNE_SUBAPP, versionPruneLocation.getSubAppId());
        assertEquals("", versionPruneLocation.getParameter());
    }

    @Test
    void startOpensJcrQueriesSubApp() {
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);

        _toolsBaseApp.start(location);

        verify(_appContext, times(2)).openSubApp(locationCaptor.capture());

        Location jcrQueriesLocation = locationCaptor.getAllValues().get(1);
        assertEquals(APP_NAME, jcrQueriesLocation.getAppName());
        assertEquals(JCR_QUERIES_SUBAPP, jcrQueriesLocation.getSubAppId());
        assertEquals("", jcrQueriesLocation.getParameter());
    }

    @Test
    void startOpensSubAppsInCorrectOrder() {
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);

        _toolsBaseApp.start(location);

        verify(_appContext, times(2)).openSubApp(locationCaptor.capture());

        assertEquals(VERSION_PRUNE_SUBAPP, locationCaptor.getAllValues().get(0).getSubAppId());
        assertEquals(JCR_QUERIES_SUBAPP, locationCaptor.getAllValues().get(1).getSubAppId());
    }

    @Test
    void startWithDifferentLocationOpensSubApps() {
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME, VERSION_PRUNE_SUBAPP, "someParameter");

        _toolsBaseApp.start(location);

        verify(_appContext, times(2)).openSubApp(ArgumentCaptor.forClass(Location.class).capture());
    }
}

