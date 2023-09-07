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

import com.vaadin.v7.data.Item;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.Context;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.vaadin.form.FormViewReduced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static info.magnolia.jcr.util.NodeUtil.getNodeIdentifierIfPossible;
import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static java.lang.String.valueOf;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.math.NumberUtils.toInt;

/**
 * VersionPruneSubApp prints the results of a version prune.
 *
 * @author frank.sommer
 * @since 1.5.0
 */
public class VersionPruneSubApp extends ToolsBaseSubApp<VersionPruneResultView> {
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionPruneSubApp.class);
    private static final String SPACER = "\n\n-------------------------------------------------------------\n";

    private final VersionPruneResultView _view;
    private final SimpleTranslator _simpleTranslator;
    private final Provider<Context> _contextProvider;
    private final FormViewReduced _formView;
    private List<String> _prunedHandles;
    private StringBuilder _resultMessages;

    @Inject
    public VersionPruneSubApp(final SubAppContext subAppContext, final FormViewReduced formView, final VersionPruneResultView view, final FormBuilder builder, final SimpleTranslator simpleTranslator, final Provider<Context> contextProvider) {
        super(subAppContext, formView, view, builder);
        _formView = formView;
        _view = view;
        _simpleTranslator = simpleTranslator;
        _contextProvider = contextProvider;
    }

    @Override
    public void doAction() {
        _resultMessages = new StringBuilder();
        _prunedHandles = new ArrayList<>();

        final Item item = _formView.getItemDataSource();
        String path = item.getItemProperty("path").getValue().toString();
        String workspace = item.getItemProperty("workspace").getValue().toString();
        int versions = toInt(item.getItemProperty("versions").getValue().toString());

        Node startNode = getNode(path, workspace);

        if (startNode != null) {
            try {
                NodeUtil.visit(startNode,
                    node -> {
                        LOGGER.debug("Check node path: [{}] and index [{}].", node.getPath(), node.getIndex());
                        if (node.getDepth() > 0) {
                            handleNode(node, versions);
                        }
                    }
                );
            } catch (Exception e) {
                LOGGER.error("Error on prune versions.", e);
            }
        } else {
            _resultMessages.append(_simpleTranslator.translate("versionPrune.path.wrong", path, workspace));
        }

        if (_prunedHandles.isEmpty()) {
            if (versions > 0) {
                _resultMessages.append(SPACER);
                _resultMessages.append(_simpleTranslator.translate("versionPrune.nothingPruned.prefix")).append(" ");
                _resultMessages.append(valueOf(versions));
                _resultMessages.append(" ").append(_simpleTranslator.translate("versionPrune.nothingPruned.postfix"));
                _resultMessages.append(SPACER);
            } else {
                _resultMessages.append(SPACER);
                _resultMessages.append(_simpleTranslator.translate("versionPrune.nothingPruned"));
                _resultMessages.append(SPACER);
            }
        } else {
            final String successfulPrunedMessage = SPACER + valueOf(_prunedHandles.size()) + " " + _simpleTranslator.translate("versionPrune.pruned") + "\n" + SPACER;
            _resultMessages.append(successfulPrunedMessage);
            for (String handle : _prunedHandles) {
                _resultMessages.append(handle).append("\n");
            }
            _resultMessages.append(successfulPrunedMessage);
        }

        _view.buildResultView(_resultMessages.toString());
    }

    private void handleNode(Node node, int versions) {
        LOGGER.debug("Check node with uuid [{}].", getNodeIdentifierIfPossible(node));

        VersionHistory versionHistory = getVersionHistory(node);

        if (versionHistory != null) {
            VersionIterator allVersions = getAllVersions(node, versionHistory);
            if (allVersions != null) {
                long indexToRemove = getIndexToRemove(allVersions, versions);

                if (indexToRemove > 0) {
                    // skip root version
                    allVersions.nextVersion();
                    // remove the version after rootVersion
                    while (indexToRemove > 0) {
                        Version currentVersion = allVersions.nextVersion();
                        String versionNameToRemove = getVersionName(currentVersion);
                        String errorMessage = EMPTY;
                        try {
                            versionHistory.removeVersion(versionNameToRemove);
                        } catch (UnsupportedRepositoryOperationException e) {
                            errorMessage = MessageFormat.format("Unversionable node with uuid [{0}].", getNodeIdentifierIfPossible(node));
                            LOGGER.warn(errorMessage, e);
                        } catch (ReferentialIntegrityException e) {
                            errorMessage = MessageFormat.format("Node with path [{0}] and VersionNumber [{1}] is referenced by [{2}] - uuid: [{3}]",
                                getPathIfPossible(node), getVersionName(currentVersion), getReferences(currentVersion), getNodeIdentifierIfPossible(node));
                            LOGGER.warn(errorMessage, e);
                        } catch (Exception e) {
                            errorMessage = MessageFormat.format("Unable to perform a versioning operation on node with uuid [{0}].", getNodeIdentifierIfPossible(node));
                            LOGGER.warn(errorMessage, e);
                        }

                        if (isEmpty(errorMessage)) {
                            String info = getPathIfPossible(node) + ", Version: " + versionNameToRemove;
                            LOGGER.info("Removed version [{}].", info);
                            _prunedHandles.add(info);
                        } else {
                            _resultMessages.append(errorMessage).append("\n");
                        }
                        indexToRemove--;
                    }
                }
            }
        }
    }

    private long getIndexToRemove(VersionIterator allVersions, int versions) {
        // size - 2 to skip root version
        return (allVersions.getSize() - 2) - ((versions > 0 ? versions - 1 : 0));
    }

    private VersionHistory getVersionHistory(Node node) {
        VersionHistory versionHistory = null;
        try {
            VersionManager versionManager = Components.getComponent(VersionManager.class);
            versionHistory = versionManager.getVersionHistory(node);
        } catch (RepositoryException e) {
            String message = MessageFormat.format("Unable to get versionsHistory from node with path [{0}].", getPathIfPossible(node));
            LOGGER.error(message, e);
            _resultMessages.append(message).append("\n");
        }
        return versionHistory;
    }

    private VersionIterator getAllVersions(Node node, VersionHistory versionHistory) {
        VersionIterator allVersions = null;

        try {
            allVersions = versionHistory.getAllVersions();
        } catch (RepositoryException e) {
            String message = MessageFormat.format("Unable to get all versions from node with path [{0}].", getPathIfPossible(node));
            LOGGER.error(message, e);
            _resultMessages.append(message).append("\n");
        }
        return allVersions;
    }

    private String getVersionName(Version version) {
        String returnValue = EMPTY;
        if (version != null) {
            try {
                returnValue = version.getName();
            } catch (RepositoryException e) {
                LOGGER.info(e.getLocalizedMessage());
            }
        }
        return returnValue;
    }

    private String getReferences(Version version) {
        String returnValue = EMPTY;
        if (version != null) {
            try {
                PropertyIterator references = version.getReferences();
                if (references != null) {
                    returnValue = references.toString();
                }
            } catch (RepositoryException e) {
                LOGGER.info(e.getLocalizedMessage());
            }
        }
        return returnValue;
    }

    private Node getNode(final String path, final String workspace) {
        Node node = null;
        try {
            final Session session = _contextProvider.get().getJCRSession(workspace);
            node = session.getNode(path);
        } catch (RepositoryException e) {
            LOGGER.error("Error message", e);
        }
        return node;
    }
}
