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

import de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import info.magnolia.cms.core.FileSystemHelper;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
 * Create CSV file for all translations or selected elements.
 *
 * @author diana.racho (IBM iX)
 */
public class ExportTranslationAsCsvAction extends AbstractAction<ConfiguredActionDefinition> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportTranslationAsCsvAction.class);
    private static final String QUERY_STATEMENT = "select * from [%s]";

    private final ValueContext<Node> _valueContext;
    private final I18nContentSupport _i18nContentSupport;
    private final FileSystemHelper _fileSystemHelper;

    @Inject
    public ExportTranslationAsCsvAction(ConfiguredActionDefinition definition, ValueContext<Node> valueContext, I18nContentSupport i18nContentSupport, FileSystemHelper fileSystemHelper) {
        super(definition);
        _valueContext = valueContext;
        _i18nContentSupport = i18nContentSupport;
        _fileSystemHelper = fileSystemHelper;
    }

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

    private Map<String, Map<String, String>> getEntries(Collection<Locale> locales) {
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

    private List<Node> retrieveTranslationNodes() {
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

    private boolean containsOnlyRootNode() {
        Optional<Node> firstItem = _valueContext.getSingle();
        return firstItem.isPresent() && isEmpty(getName(firstItem.get()));
    }

    private void streamFile(final TranslationCsvWriter csvWriter) {
        StreamResource.StreamSource source = (StreamResource.StreamSource) csvWriter::getStream;
        String fileName = csvWriter.getFile().getName();
        StreamResource resource = new StreamResource(source, fileName);
        resource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName + "\"");
        resource.setMIMEType("application/octet-stream");
        resource.setCacheTime(0);
        Page.getCurrent().open(resource, "csv export", true);
    }
}
