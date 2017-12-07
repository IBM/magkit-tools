package com.aperto.magnolia.edittools.setup;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Edit tools module class.
 *
 * @author frank.sommer
 * @since 09.07.2015
 */
public class EditToolsModule implements ModuleLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditToolsModule.class);

    private StatusBarConfig _statusBarConfig;
    private PublicLinkConfig _publicLinkConfig;
    private List<String> _moveConfirmWorkspaces;

    @Override
    public void start(final ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Start editor-tools-module ...");
    }

    @Override
    public void stop(final ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Stop editor-tools-module ...");
    }

    public PublicLinkConfig getPublicLinkConfig() {
        return _publicLinkConfig;
    }

    public void setPublicLinkConfig(final PublicLinkConfig publicLinkConfig) {
        _publicLinkConfig = publicLinkConfig;
    }

    public List<String> getMoveConfirmWorkspaces() {
        return _moveConfirmWorkspaces != null ? _moveConfirmWorkspaces : emptyList();
    }

    public void setMoveConfirmWorkspaces(final List<String> moveConfirmWorkspaces) {
        _moveConfirmWorkspaces = moveConfirmWorkspaces;
    }

    public StatusBarConfig getStatusBarConfig() {
        return _statusBarConfig;
    }

    public void setStatusBarConfig(final StatusBarConfig statusBarConfig) {
        _statusBarConfig = statusBarConfig;
    }
}
