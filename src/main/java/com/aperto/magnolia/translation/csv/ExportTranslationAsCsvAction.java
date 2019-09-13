package com.aperto.magnolia.translation.csv;

import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;
import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static com.google.common.collect.Lists.newArrayList;
import static info.magnolia.jcr.util.NodeUtil.asIterable;
import static info.magnolia.jcr.util.NodeUtil.asList;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Create CSV file for all translations or selected elements.
 *
 * @author diana.racho (Aperto AG)
 */
public class ExportTranslationAsCsvAction extends AbstractAction<ConfiguredActionDefinition> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportTranslationAsCsvAction.class);
    private static final String QUERY_STATEMENT = "select * from [%s]";

    private Map<String, Map<String, String>> _entries;

    private I18nContentSupport _i18nContentSupport;
    private List<JcrItemAdapter> _items;

    public ExportTranslationAsCsvAction(ConfiguredActionDefinition definition, I18nContentSupport i18nContentSupport, JcrItemAdapter item) throws ActionExecutionException {
        this(definition, i18nContentSupport, newArrayList(item));
    }

    public ExportTranslationAsCsvAction(ConfiguredActionDefinition definition, I18nContentSupport i18nContentSupport, List<JcrItemAdapter> items) throws ActionExecutionException {
        super(definition);
        _i18nContentSupport = i18nContentSupport;
        _items = items;
    }

    @Override
    public void execute() throws ActionExecutionException {
        Collection<Locale> locales = _i18nContentSupport.getLocales();
        setEntries(locales);
        TranslationCsvWriter csvWriter = new TranslationCsvWriter(_entries, Path.getTempDirectory(), locales);
        File file = csvWriter.getFile();
        if (file != null) {
            csvWriter.writeCsv();
            streamFile(csvWriter);
        }
    }

    private void setEntries(Collection<Locale> locales) {
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
        _entries = entries;
    }

    private List<Node> retrieveTranslationNodes() {
        List<Node> t9nNodes = new ArrayList<>();
        if (CollectionUtils.isEmpty(_items) || containsOnlyRootNode()) {
            // query for all translation nodes
            String statement = String.format(QUERY_STATEMENT, Translation.NAME);
            try {
                t9nNodes = asList(asIterable(QueryUtil.search(WS_TRANSLATION, statement)));
            } catch (RepositoryException e) {
                LOGGER.error("Error querying all translations with query {}.", statement, e);
            }
        } else {
            for (JcrItemAdapter item : _items) {
                t9nNodes.add((Node) item.getJcrItem());
            }
        }
        return t9nNodes;
    }

    private boolean containsOnlyRootNode() {
        return _items.size() == 1 && isEmpty(NodeUtil.getName((Node) _items.get(0).getJcrItem()));
    }

    private void streamFile(final TranslationCsvWriter csvWriter) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return csvWriter.getStream();
            }
        };
        String fileName = csvWriter.getFile().getName();
        StreamResource resource = new StreamResource(source, fileName);
        resource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName + "\"");
        resource.setMIMEType("application/octet-stream");
        resource.setCacheTime(0);
        Page.getCurrent().open(resource, "csv export", true);
    }
}