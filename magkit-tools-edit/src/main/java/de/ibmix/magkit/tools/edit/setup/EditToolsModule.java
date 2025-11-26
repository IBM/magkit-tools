package de.ibmix.magkit.tools.edit.setup;

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

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Main module class for the Magnolia Edit Tools module, providing enhanced editing capabilities and configurations
 * for the Magnolia AdminCentral. This module implements the {@link ModuleLifecycle} interface to manage
 * initialization and shutdown.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Configuration for public link generation with site-specific host mappings</li>
 * <li>Status bar configuration for asset usage and UUID display</li>
 * <li>Move confirmation settings per workspace</li>
 * </ul>
 *
 * <p><strong>Configuration Properties:</strong></p>
 * <ul>
 * <li>publicLinkConfig - Configuration for extended public link generation</li>
 * <li>statusBarConfig - Configuration for status bar enhancements</li>
 * <li>moveConfirmWorkspaces - List of workspaces requiring confirmation dialogs for move operations</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * This module is automatically loaded and configured by Magnolia's module framework.
 * Configuration is typically done through YAML files in the module configuration.
 *
 * @author frank.sommer
 * @since 2015-07-09
 */
public class EditToolsModule implements ModuleLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditToolsModule.class);

    private StatusBarConfig _statusBarConfig = new StatusBarConfig();
    private PublicLinkConfig _publicLinkConfig;
    private List<String> _moveConfirmWorkspaces;

    /**
     * Starts the module lifecycle. Called by Magnolia during module initialization.
     *
     * @param moduleLifecycleContext the module lifecycle context
     */
    @Override
    public void start(final ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Start editor-tools-module ...");
    }

    /**
     * Stops the module lifecycle. Called by Magnolia during module shutdown.
     *
     * @param moduleLifecycleContext the module lifecycle context
     */
    @Override
    public void stop(final ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Stop editor-tools-module ...");
    }

    /**
     * Returns the public link configuration.
     *
     * @return the public link configuration, or null if not configured
     */
    public PublicLinkConfig getPublicLinkConfig() {
        return _publicLinkConfig;
    }

    /**
     * Sets the public link configuration for extended link generation.
     *
     * @param publicLinkConfig the public link configuration
     */
    public void setPublicLinkConfig(final PublicLinkConfig publicLinkConfig) {
        _publicLinkConfig = publicLinkConfig;
    }

    /**
     * Returns the list of workspace names that require confirmation dialogs for move operations.
     *
     * @return the list of workspace names, or an empty list if not configured
     */
    public List<String> getMoveConfirmWorkspaces() {
        return _moveConfirmWorkspaces != null ? _moveConfirmWorkspaces : emptyList();
    }

    /**
     * Sets the list of workspace names that require confirmation dialogs for move operations.
     *
     * @param moveConfirmWorkspaces the list of workspace names
     */
    public void setMoveConfirmWorkspaces(final List<String> moveConfirmWorkspaces) {
        _moveConfirmWorkspaces = moveConfirmWorkspaces;
    }

    /**
     * Returns the status bar configuration.
     *
     * @return the status bar configuration
     */
    public StatusBarConfig getStatusBarConfig() {
        return _statusBarConfig;
    }

    /**
     * Sets the status bar configuration for enhanced status bar display.
     *
     * @param statusBarConfig the status bar configuration
     */
    public void setStatusBarConfig(final StatusBarConfig statusBarConfig) {
        _statusBarConfig = statusBarConfig;
    }
}
