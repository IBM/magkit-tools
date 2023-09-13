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

import au.com.bytecode.opencsv.CSVReader;
import de.ibmix.magkit.tools.t9n.TranslationNodeTypes;
import de.ibmix.magkit.tools.t9n.setup.TranslationModule;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.action.CommitAction;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.observation.DatasourceObservation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PN_KEY;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PREFIX_NAME;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Translation import csv action.
 *
 * @author frank.sommer
 * @since 1.0.5
 */
@Slf4j
public class ImportCsvAction extends CommitAction<Node> {

    private final Collection<Locale> _locales;
    private final NodeNameHelper _nodeNameHelper;
    private final FormView<Node> _form;

    private TranslationModule _translationModule;

    @Inject
    public ImportCsvAction(CommitActionDefinition definition, CloseHandler closeHandler, ValueContext<Node> valueContext, FormView<Node> form, Datasource<Node> datasource, DatasourceObservation.Manual datasourceObservation, I18nContentSupport i18nContentSupport) {
        super(definition, closeHandler, valueContext, form, datasource, datasourceObservation);
        _form = form;
        _locales = i18nContentSupport.getLocales();
        _nodeNameHelper = Components.getComponent(NodeNameHelper.class);
    }

    @Override
    public void write() {
        Optional<File> importCsv = _form.getPropertyValue("importCsv");
        String basePath = getTranslationModule().getBasePath();
        importCsv.ifPresent(csvFile -> doCsvImport(basePath, csvFile));
    }

    private void doCsvImport(final String basePath, final File csvFile) {
        try (InputStream inputStream = new FileInputStream(csvFile)) {
            CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream, getPropertyValue("encoding", UTF_8.name())), getPropertyValue("separator", ",").charAt(0));
            List<String[]> lines = csvReader.readAll();
            if (CollectionUtils.isNotEmpty(lines)) {
                Map<Integer, String> indexedPropertyNames = detectColumns(lines.get(0));
                persistTranslations(basePath, indexedPropertyNames, lines);
            } else {
                LOGGER.warn("No lines in csv file, skip ...");
            }
        } catch (IOException e) {
            LOGGER.error("Error importing csv data.", e);
        }
    }

    private String getPropertyValue(final String propertyId, final String defaultValue) {
        Optional<Option> propertyValue = _form.getPropertyValue(propertyId);
        return propertyValue.isPresent() ? propertyValue.get().getValue() : defaultValue;
    }

    private void persistTranslations(final String basePath, final Map<Integer, String> indexedPropertyNames, final List<String[]> lines) {
        try {
            Session jcrSession = MgnlContext.getJCRSession(WS_TRANSLATION);
            Node baseNode;
            if (isEmpty(basePath)) {
                baseNode = jcrSession.getRootNode();
            } else {
                baseNode = jcrSession.getNode(basePath);
            }
            createNodesForLines(baseNode, lines, indexedPropertyNames);
            jcrSession.save();
        } catch (RepositoryException e) {
            LOGGER.error("Error persisting CSV to JCR.", e);
        }
    }

    private void createNodesForLines(final Node baseNode, final List<String[]> lines, final Map<Integer, String> indexedPropertyNames) throws RepositoryException {
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i);
            String key = values[0];
            String keyNodeName = _nodeNameHelper.getValidatedName(key);

            Node t9nNode;
            if (!baseNode.hasNode(keyNodeName)) {
                t9nNode = baseNode.addNode(keyNodeName, TranslationNodeTypes.Translation.NAME);
                PropertyUtil.setProperty(t9nNode, PN_KEY, key);
            } else {
                t9nNode = baseNode.getNode(keyNodeName);
            }

            for (Map.Entry<Integer, String> column : indexedPropertyNames.entrySet()) {
                Integer keyIndex = column.getKey();
                if (keyIndex > 0) {
                    PropertyUtil.setProperty(t9nNode, column.getValue(), values[keyIndex]);
                }
            }
            NodeTypes.LastModified.update(t9nNode);
        }
    }

    private Map<Integer, String> detectColumns(final String[] headings) {
        Map<Integer, String> cols = new TreeMap<>();
        int index = 0;
        for (String heading : headings) {
            if (heading.equals(TranslationCsvWriter.COLUMN_KEY)) {
                cols.put(index, PN_KEY);
            } else {
                for (Locale locale : _locales) {
                    if (heading.equals(locale.getDisplayName())) {
                        cols.put(index, PREFIX_NAME + locale.getLanguage());
                        break;
                    }
                }
            }
            index++;
        }
        return cols;
    }

    private TranslationModule getTranslationModule() {
        if (_translationModule == null) {
            _translationModule = Components.getComponent(TranslationModule.class);
        }
        return _translationModule;
    }
}
