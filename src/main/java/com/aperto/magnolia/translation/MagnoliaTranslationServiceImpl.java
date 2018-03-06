package com.aperto.magnolia.translation;

import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.DefaultMessageBundlesLoader;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.i18nsystem.module.I18nModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.cms.util.QueryUtil.search;
import static info.magnolia.jcr.util.PropertyUtil.getString;
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
    private static final String QUERY_STATEMENT = "select * from [%s] where key = '%s'";

    @Inject
    public MagnoliaTranslationServiceImpl(Provider<I18nModule> i18nModuleProvider, Provider<DefaultMessageBundlesLoader> defaultMessageBundlesLoaderProvider) {
        super(i18nModuleProvider, defaultMessageBundlesLoaderProvider);
    }

    @Override
    public String translate(LocaleProvider localeProvider, String basename, String[] keys) {
        String message = super.translate(localeProvider, basename, keys);
        final String language = localeProvider.getLocale().getLanguage();
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
        if (!key.contains("'")) {
            String statement = String.format(QUERY_STATEMENT, Translation.NAME, key);
            try {
                // MGKT-466: Execute translation search in system context > MgnlContext is not always set.
                message = MgnlContext.doInSystemContext(() -> {
                        String message1 = EMPTY;
                        NodeIterator nodeIterator = search(WS_TRANSLATION, statement);
                        if (nodeIterator.hasNext()) {
                            message1 = getString(nodeIterator.nextNode(), Translation.PREFIX_NAME + language, message1);
                        }
                        return message1;
                    });
            } catch (RepositoryException e) {
                LOGGER.error("Error on querying translation node for query {}.", statement, e);
            }
        }
        return message;
    }
}