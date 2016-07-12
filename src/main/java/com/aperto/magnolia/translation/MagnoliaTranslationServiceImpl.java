package com.aperto.magnolia.translation;

import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.LinkedList;
import java.util.List;

import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;
import static info.magnolia.context.MgnlContext.getJCRSession;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static javax.jcr.query.Query.JCR_SQL2;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Translation service for templates.
 * Extends existing TranslationServiceImpl to get translation from translation workspace.
 *
 * @author diana.racho (Aperto AG)
 */
@Singleton
public class MagnoliaTranslationServiceImpl extends TranslationServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(MagnoliaTranslationServiceImpl.class);


    private static final String WORKSPACE_TRANSLATION = "translation";

    @Override
    public String translate(LocaleProvider localeProvider, String basename, String[] keys) {
        final String language = localeProvider.getLocale().getLanguage();
        String message = super.translate(localeProvider, basename, keys);
        for (String key : keys) {
            String newMessage = getMessage(key, language);
            if (isNotBlank(newMessage)) {
                message = newMessage;
                break;
            }
        }
        return message;
    }

    private String getMessage(String key, String language) {
        String message = EMPTY;
        List<Node> results = search(createQuery("Select * from [nt:base] where key = '" + key + "'", WORKSPACE_TRANSLATION));
        if (!results.isEmpty()) {
            Node translation = results.get(0);
            message = getString(translation, PREFIX_NAME + language, message);
        }
        return message;
    }

    private Query createQuery(final String queryString, String workspace) {
        Query query = null;
        try {
            final Session jcrSession = getJCRSession(workspace);
            final QueryManager queryManager = jcrSession.getWorkspace().getQueryManager();
            query = queryManager.createQuery(queryString, JCR_SQL2);
        } catch (RepositoryException e) {
            LOGGER.error("Can't get translation for templates. Can't create query'" + queryString + "'.", e);
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
            LOGGER.error("Can't get translations for templates.", e);
        }
        if (iterator != null && iterator.getSize() > 0) {
            while (iterator.hasNext()) {
                itemsList.add(iterator.nextNode());
            }
        }
        return itemsList;
    }
}