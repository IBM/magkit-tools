package com.aperto.magnolia.translation.export;

import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.io.File;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;
import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.jcr.util.PropertyUtil.getString;

/**
 * Create CSV file for all translations.
 *
 * @author diana.racho (Aperto AG)
 */
public class ExportTranslationAsCsvAction extends AbstractAction<ConfiguredActionDefinition> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportTranslationAsCsvAction.class);
    private static final String QUERY_STATEMENT = "select * from [%s]";

    private Map<String, Map<String, String>> _entries;

    private I18nContentSupport _i18nContentSupport;

    public ExportTranslationAsCsvAction(ConfiguredActionDefinition definition, I18nContentSupport i18nContentSupport) throws ActionExecutionException {
        super(definition);
        _i18nContentSupport = i18nContentSupport;
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

        String statement = String.format(QUERY_STATEMENT, Translation.NAME);
        try {
            NodeIterator result = QueryUtil.search(WS_TRANSLATION, statement);
            while (result.hasNext()) {
                Node translationNode = result.nextNode();
                Map<String, String> translationProperties = new TreeMap<>();
                for (Locale locale : locales) {
                    translationProperties.put(PREFIX_NAME + locale.getLanguage(), getString(translationNode, PREFIX_NAME + locale.getLanguage()));
                }
                entries.put(getString(translationNode, Translation.PN_KEY), translationProperties);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error querying all translations with query {}.", statement, e);
        }
        _entries = entries;
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