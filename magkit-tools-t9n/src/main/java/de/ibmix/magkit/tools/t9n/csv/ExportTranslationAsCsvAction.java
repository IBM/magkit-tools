package de.ibmix.magkit.tools.t9n.csv;

/*-
 * #%L
 * magkit-tools-t9n
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

import com.vaadin.server.DownloadStream;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation;
import info.magnolia.cms.core.FileSystemHelper;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PREFIX_NAME;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.jcr.util.NodeUtil.asIterable;
import static info.magnolia.jcr.util.NodeUtil.asList;
import static info.magnolia.jcr.util.NodeUtil.getName;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Action for exporting translations from the Magnolia translation workspace to CSV format.
 * <p><strong>Purpose:</strong></p>
 * Enables bulk export of translation data to CSV files, allowing editors to work with
 * translations in external tools like spreadsheet applications for review, editing, or translation.
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Exports all translations or only selected nodes</li>
 * <li>Generates CSV with UTF-8 encoding and locale-specific columns</li>
 * <li>Automatically triggers file download in the browser</li>
 * <li>Supports hierarchical translation structures</li>
 * <li>Handles root node selection to export entire workspace</li>
 * </ul>
 * <p><strong>Usage:</strong></p>
 * This action is typically configured in the translation app definition and can be triggered
 * on selected translation nodes or on the entire workspace root.
 * <p><strong>CSV Format:</strong></p>
 * The exported CSV contains a header row with "Key" and locale names, followed by rows with
 * translation keys and their values for each locale.
 *
 * @author diana.racho (IBM iX)
 * @since 2023-01-01
 */
@Slf4j
public class ExportTranslationAsCsvAction extends AbstractAction<ConfiguredActionDefinition> {
    private static final String QUERY_STATEMENT = "select * from [%s]";

    private final ValueContext<Node> _valueContext;
    private final I18nContentSupport _i18nContentSupport;
    private final FileSystemHelper _fileSystemHelper;

    /**
     * Creates a new CSV export action with all required dependencies.
     *
     * @param definition the action definition configuration
     * @param valueContext the context providing access to selected nodes
     * @param i18nContentSupport the i18n support providing configured locales
     * @param fileSystemHelper the helper for accessing temporary file storage
     */
    @Inject
    public ExportTranslationAsCsvAction(ConfiguredActionDefinition definition, ValueContext<Node> valueContext, I18nContentSupport i18nContentSupport, FileSystemHelper fileSystemHelper) {
        super(definition);
        _valueContext = valueContext;
        _i18nContentSupport = i18nContentSupport;
        _fileSystemHelper = fileSystemHelper;
    }

    /**
     * Executes the export by retrieving translation data, generating a CSV file, and triggering a download.
     */
    @Override
    public void execute() {
        Collection<Locale> locales = _i18nContentSupport.getLocales();
        Map<String, Map<String, String>> entries = getEntries(locales);
        TranslationCsvWriter csvWriter = new TranslationCsvWriter(entries, _fileSystemHelper.getTempDirectory(), locales);
        File file = csvWriter.getFile();
        if (file != null) {
            csvWriter.writeCsv();
            streamFile(csvWriter);
        }
    }

    Map<String, Map<String, String>> getEntries(Collection<Locale> locales) {
        Map<String, Map<String, String>> entries = new TreeMap<>();

        List<Node> t9nNodes = retrieveTranslationNodes();
        for (Node t9nNode : t9nNodes) {
            Map<String, String> translationProperties = new TreeMap<>();
            for (Locale locale : locales) {
                String propertyName = PREFIX_NAME + locale.getLanguage();
                translationProperties.put(propertyName, getString(t9nNode, propertyName));
            }
            if (isNotBlank(getString(t9nNode, Translation.PN_KEY))) {
                entries.put(getString(t9nNode, Translation.PN_KEY), translationProperties);
            }
        }
        return entries;
    }

    List<Node> retrieveTranslationNodes() {
        List<Node> t9nNodes = new ArrayList<>();
        if (_valueContext.getSingle().isEmpty() || containsOnlyRootNode()) {
            // query for all translation nodes
            String statement = String.format(QUERY_STATEMENT, Translation.NAME);
            try {
                t9nNodes = asList(asIterable(QueryUtil.search(WS_TRANSLATION, statement)));
            } catch (RepositoryException e) {
                LOGGER.error("Error querying all translations with query {}.", statement, e);
            }
        } else {
            t9nNodes = _valueContext.get().collect(Collectors.toList());
        }
        return t9nNodes;
    }

    boolean containsOnlyRootNode() {
        Optional<Node> firstItem = _valueContext.getSingle();
        return firstItem.isPresent() && isEmpty(getName(firstItem.get()));
    }

    void streamFile(final TranslationCsvWriter csvWriter) {
        StreamResource.StreamSource source = csvWriter::getStream;
        String fileName = csvWriter.getFile().getName();
        StreamResource resource = new StreamResource(source, fileName) {
            @Override
            public DownloadStream getStream() {
                DownloadStream stream = super.getStream();
                if (stream != null) {
                    stream.setParameter("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                    stream.setCacheTime(0);
                }
                return stream;
            }
        };
        resource.setMIMEType("application/octet-stream");
        Page.getCurrent().open(resource, "csv export", true);
    }
}
