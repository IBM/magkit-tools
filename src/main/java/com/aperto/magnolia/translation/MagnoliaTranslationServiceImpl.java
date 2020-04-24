package com.aperto.magnolia.translation;

import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.DefaultMessageBundlesLoader;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.i18nsystem.module.I18nModule;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.function.Predicate;

import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.cms.util.QueryUtil.search;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.contains;

/**
 * Translation service for templates.
 * Extends existing TranslationServiceImpl to get translation from translation workspace.
 *
 * @author diana.racho (Aperto AG)
 */
@Singleton
public class MagnoliaTranslationServiceImpl extends TranslationServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(MagnoliaTranslationServiceImpl.class);
    public static final String BASE_QUERY = "select * from [" + Translation.NAME + "] where key = ";
    private static final Predicate<String> MESSAGE_CONDITION = StringUtils::isNotEmpty;

    @Inject
    public MagnoliaTranslationServiceImpl(Provider<I18nModule> i18nModuleProvider, Provider<DefaultMessageBundlesLoader> defaultMessageBundlesLoaderProvider) {
        super(i18nModuleProvider, defaultMessageBundlesLoaderProvider);
    }

    @Override
    public String translate(LocaleProvider localeProvider, String basename, String[] keys) {
        String message = getAppMessage(localeProvider, keys);
        message = escapeSingleQuotesIfPlaceholderMessage(message);
        return MESSAGE_CONDITION.test(message) ? message : super.translate(localeProvider, basename, keys);
    }

    private String escapeSingleQuotesIfPlaceholderMessage(final String message) {
        String escapedMessage = message;
        if (contains(escapedMessage, "'") && !contains(escapedMessage, "''") && escapedMessage.matches(".*\\{[0-9]}.*")) {
            escapedMessage = escapedMessage.replace("'", "''");
        }
        return escapedMessage;
    }

    private String getAppMessage(LocaleProvider localeProvider, String[] keys) {
        final String language = localeProvider.getLocale().getLanguage();
        final String i18nProperty = Translation.PREFIX_NAME + language;
        // get first acceptable translation for list of keys:
        String message = null;
        for (String key : keys) {
            if (!key.contains("'")) {
                String newMessage = doMessageQuery(key, i18nProperty);
                if (MESSAGE_CONDITION.test(newMessage)) {
                    message = newMessage;
                    break;
                }
            }
        }
        return message;
    }

    /**
     * Override for testing.
     */
    String doMessageQuery(final String key, final String i18nProperty) {
        String message = null;
        // Replace string formatting - simple string concatenation is about 8 times faster.
        String statement = BASE_QUERY + '\'' + key + '\'';
        try {
            // Execute translation search in system context > MgnlContext is not always set.
            message = MgnlContext.doInSystemContext(
                () -> {
                    NodeIterator nodeIterator = search(WS_TRANSLATION, statement);
                    return nodeIterator.hasNext() ? getString(nodeIterator.nextNode(), i18nProperty, EMPTY) : null;
                }
            );
        } catch (RepositoryException e) {
            LOGGER.error("Error on querying translation node for query {}.", statement, e);
        }
        return message;
    }
}