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

import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseApp;

import javax.inject.Inject;

/**
 * Tools app base class.
 * This class opens the tools sub apps.
 * <p>
 * Due to a known bug, the sub app that is in focus on app launch must be the last one opened.
 * So the app defaults now to having the Query sub app opened on launch.
 *
 * @author frank.sommer
 * @since 1.5.1
 */
public class ToolsBaseApp extends BaseApp {

    @Inject
    public ToolsBaseApp(final AppContext appContext, final AppView view) {
        super(appContext, view);
    }

    @Override
    public void start(final Location location) {
        super.start(location);

        final AppContext appContext = getAppContext();
        final String appName = getAppContext().getAppDescriptor().getName();
        final String[] subAppNames = {"versionPrune", "jcrQueries"};

        for (String subAppName : subAppNames) {
            appContext.openSubApp(new DefaultLocation(Location.LOCATION_TYPE_APP, appName, subAppName, ""));
        }
    }
}
