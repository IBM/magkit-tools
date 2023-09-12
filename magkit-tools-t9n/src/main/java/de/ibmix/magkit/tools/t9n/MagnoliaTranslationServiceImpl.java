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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.Locale;
import java.util.function.Predicate;

import static info.magnolia.cms.util.QueryUtil.search;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Translation service for templates.
 * Extends existing TranslationServiceImpl to get translation from translation workspace.
 *
 * @author diana.racho (IBM iX)
 */
@Singleton
public class MagnoliaTranslationServiceImpl extends TranslationServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(MagnoliaTranslationServiceImpl.class);
    public static final String BASE_QUERY = "select * from [" + TranslationNodeTypes.Translation.NAME + "] where key = ";
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
        final Locale locale = localeProvider.getLocale();
        final String language = locale.getLanguage();
        String i18nProperty = TranslationNodeTypes.Translation.PREFIX_NAME + language;
        String fallbackProperty = null;

        final String country = locale.getCountry();
        if (isNotEmpty(country)) {
            fallbackProperty = i18nProperty;
            i18nProperty = TranslationNodeTypes.Translation.PREFIX_NAME + language + "_" + country;
        }
        // get first acceptable translation for list of keys:
        String message = null;
        for (String key : keys) {
            if (!contains(key, "'")) {
                String newMessage = doMessageQuery(key, i18nProperty, fallbackProperty);
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
    String doMessageQuery(final String key, final String i18nProperty, final String fallbackProperty) {
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
                        foundMsg = getString(node, i18nProperty, EMPTY);
                        if (isEmpty(foundMsg) && isNotEmpty(fallbackProperty)) {
                            foundMsg = getString(node, fallbackProperty, EMPTY);
                        }
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
