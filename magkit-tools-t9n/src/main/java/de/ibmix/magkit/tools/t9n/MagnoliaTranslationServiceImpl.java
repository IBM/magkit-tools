package de.ibmix.magkit.tools.t9n;

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

import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.DefaultMessageBundlesLoader;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.i18nsystem.module.I18nModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.Locale;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.contains;

/**
 * Enhanced translation service that retrieves translations from the Magnolia translation workspace.
 * <p>
 * <p><strong>Purpose:</strong></p>
 * Extends Magnolia's default translation service to fetch translations from the JCR-based translation
 * workspace instead of or in addition to properties files, enabling runtime translation management
 * through the Magnolia UI.
 * <p>
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Retrieves translations from the translation workspace via JCR queries</li>
 * <li>Falls back to standard property file translations if workspace lookup fails</li>
 * <li>Supports locale-specific and language-only translation lookups</li>
 * <li>Automatically escapes single quotes in placeholder messages for MessageFormat compatibility</li>
 * <li>Operates in system context for consistent access regardless of user permissions</li>
 * <li>Validates keys to prevent SQL injection in JCR queries</li>
 * </ul>
 * <p>
 * <p><strong>Null Handling:</strong></p>
 * Returns the translation key itself if no translation is found, ensuring non-null return values.
 * <p>
 * <p><strong>Thread Safety:</strong></p>
 * This service is thread-safe as a singleton and uses Magnolia's context mechanism for request isolation.
 *
 * @author diana.racho (IBM iX)
 * @since 2023-01-01
 */
@Slf4j
@Singleton
public class MagnoliaTranslationServiceImpl extends TranslationServiceImpl {
    public static final String BASE_QUERY = "select * from [" + TranslationNodeTypes.Translation.NAME + "] where key = ";
    private static final Predicate<String> MESSAGE_CONDITION = StringUtils::isNotEmpty;

    /**
     * Creates a new translation service with the required providers.
     *
     * @param i18nModuleProvider provider for the i18n module configuration
     * @param defaultMessageBundlesLoaderProvider provider for loading message bundles from properties files
     */
    @Inject
    public MagnoliaTranslationServiceImpl(Provider<I18nModule> i18nModuleProvider, Provider<DefaultMessageBundlesLoader> defaultMessageBundlesLoaderProvider) {
        super(i18nModuleProvider, defaultMessageBundlesLoaderProvider);
    }

    /**
     * Translates the given keys by first attempting to retrieve the translation from the workspace,
     * then falling back to the parent implementation if not found.
     * Automatically escapes single quotes in messages containing placeholders for proper MessageFormat handling.
     *
     * @param localeProvider provides the target locale for the translation
     * @param basename the basename of the message bundle (may be empty when using workspace translations)
     * @param keys array of translation keys to try in order of preference
     * @return the translated message, or the first key if no translation is found
     */
    @Override
    public String translate(LocaleProvider localeProvider, String basename, String[] keys) {
        String message = getAppMessage(localeProvider, keys);
        message = escapeSingleQuotesIfPlaceholderMessage(message);
        return MESSAGE_CONDITION.test(message) ? message : super.translate(localeProvider, basename, keys);
    }

    String escapeSingleQuotesIfPlaceholderMessage(final String message) {
        String escapedMessage = message;
        if (contains(escapedMessage, "'") && !contains(escapedMessage, "''") && escapedMessage.matches(".*\\{[0-9]}.*")) {
            escapedMessage = escapedMessage.replace("'", "''");
        }
        return escapedMessage;
    }

    String getAppMessage(LocaleProvider localeProvider, String[] keys) {
        final Locale locale = localeProvider.getLocale();

        final String[] i18nPropertyNames = TranslationNodeTypes.Translation.LOCALE_TO_PROPERTY_NAMES.apply(locale);

        // get first acceptable translation for list of keys:
        String message = null;
        for (String key : keys) {
            if (!contains(key, "'")) {
                String newMessage = doMessageQuery(key, i18nPropertyNames);
                if (MESSAGE_CONDITION.test(newMessage)) {
                    message = newMessage;
                    break;
                }
            }
        }
        return message;
    }

    /**
     * Executes a JCR query to retrieve the translation for the given key.
     * This method is package-private to allow testing with mock implementations.
     *
     * @param key the translation key to look up
     * @param i18nPropertyNames the property names to check for translations, in order of preference
     * @return the translated message, or null if not found
     */
    String doMessageQuery(final String key, final String[] i18nPropertyNames) {
        String message = null;
        // Replace string formatting - simple string concatenation is about 8 times faster.
        String statement = BASE_QUERY + '\'' + key + '\'';
        try {
            // Execute translation search in system context > MgnlContext is not always set.
            message = MgnlContext.doInSystemContext(
                () -> {
                    String foundMsg = EMPTY;
                    NodeIterator nodeIterator = QueryUtil.search(TranslationNodeTypes.WS_TRANSLATION, statement);
                    if (nodeIterator.hasNext()) {
                        final Node node = nodeIterator.nextNode();
                        foundMsg = TranslationNodeTypes.Translation.retrieveValue(node, i18nPropertyNames);
                    }
                    return foundMsg;
                }
            );
        } catch (RepositoryException e) {
            LOGGER.error("Error on querying translation node for query {}.", statement, e);
        }
        return message;
    }
}
