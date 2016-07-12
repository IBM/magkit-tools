package com.aperto.magnolia.translation.export;

import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static com.aperto.magnolia.translation.AbstractTranslationDialogAction.WORKSPACE_TRANSLATION;
import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;
import static info.magnolia.context.MgnlContext.getJCRSession;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static javax.jcr.query.Query.JCR_SQL2;

/**
 * Create CSV file for all translations.
 *
 * @author diana.racho (Aperto AG)
 */
public class ExportTranslationAsCsvAction extends AbstractAction<ConfiguredActionDefinition> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportTranslationAsCsvAction.class);

    private Map<String, Map<String, String>> _entries;

    private TranslationCsvWriter _writer;
    private I18nContentSupport _i18nContentSupport;

    public ExportTranslationAsCsvAction(ConfiguredActionDefinition definition, I18nContentSupport i18nContentSupport) throws ActionExecutionException {
        super(definition);
        _i18nContentSupport = i18nContentSupport;
    }

    @Override
    public void execute() throws ActionExecutionException {
        Collection<Locale> locales = _i18nContentSupport.getLocales();
        setEntries(locales);
        _writer = new TranslationCsvWriter(_entries, Path.getTempDirectory(), locales);
        _writer.createFile();
        streamFile(_writer.getFile().getName());
    }

    private void setEntries(Collection<Locale> locales) {
        List<Node> result = search(createQuery("SELECT * FROM [mgnl:translation]", WORKSPACE_TRANSLATION));
        Map<String, Map<String, String>> entries = new TreeMap<>();
        if (!result.isEmpty()) {
            for (Node translationNode : result) {
                Map<String, String> translationProperties = new TreeMap<>();
                for (Locale locale : locales) {
                    translationProperties.put(PREFIX_NAME + locale.getLanguage(), getString(translationNode, PREFIX_NAME + locale.getLanguage()));
                }
                entries.put(getString(translationNode, "key"), translationProperties);
            }
        }
        _entries = entries;
    }

    private void streamFile(final String fileName) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
            private FileInputStream _stream = _writer.getStream();

            @Override
            public InputStream getStream() {
                return _stream;
            }
        };

        StreamResource resource = new StreamResource(source, fileName);
        resource.setCacheTime(-1);
        resource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName + "\"");
        resource.setMIMEType("application/octet-stream");
        resource.setCacheTime(0);
        Page.getCurrent().open(resource, "", true);
    }

    private Query createQuery(final String queryString, String workspace) {
        Query query = null;
        try {
            final Session jcrSession = getJCRSession(workspace);
            final QueryManager queryManager = jcrSession.getWorkspace().getQueryManager();
            query = queryManager.createQuery(queryString, JCR_SQL2);
        } catch (RepositoryException e) {
            LOGGER.error("Can't get translation for export. Can't create query '" + queryString + "'.", e);
        }
        return query;
    }

    private List<Node> search(Query query) {
        List<Node> itemsList = new LinkedList<>();
        NodeIterator iterator = null;
        try {
            final QueryResult result = query.execute();
            iterator = result.getNodes();
        } catch (RepositoryException e) {
            LOGGER.error("Can't get translation for export.", e);
        }
        if (iterator != null && iterator.getSize() > 0) {
            while (iterator.hasNext()) {
                itemsList.add(iterator.nextNode());
            }
        }
        return itemsList;
    }
}