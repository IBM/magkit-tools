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
import jakarta.inject.Inject;

/**
 * Base application class for the administrative tools app in Magnolia CMS.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Opens and manages the version prune sub-app</li>
 *   <li>Opens and manages the JCR queries sub-app</li>
 *   <li>Coordinates multiple sub-apps within the tools application</li>
 * </ul>
 * <p><strong>Known Issues:</strong></p>
 * Due to a known bug, the sub-app that is in focus on app launch must be the last one opened.
 * Therefore, the app defaults to having the Query sub-app opened last on launch.
 *
 * @author frank.sommer
 * @since 1.5.1
 */
public class ToolsBaseApp extends BaseApp {

    /**
     * Constructs a new ToolsBaseApp instance.
     *
     * @param appContext the application context
     * @param view the application view
     */
    @Inject
    public ToolsBaseApp(final AppContext appContext, final AppView view) {
        super(appContext, view);
    }

    /**
     * Starts the application and opens all sub-apps in the correct order.
     * Opens the version prune sub-app first, then the JCR queries sub-app to ensure
     * the queries sub-app has focus on launch.
     *
     * @param location the location to start the app at
     */
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
