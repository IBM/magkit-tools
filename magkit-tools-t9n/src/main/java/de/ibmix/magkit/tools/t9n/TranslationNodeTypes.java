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

import org.apache.commons.lang3.ArrayUtils;

import javax.jcr.Node;
import java.util.Locale;
import java.util.function.Function;

import static info.magnolia.jcr.util.NodeTypes.MGNL_PREFIX;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Constants and utility methods for working with translation node types in the JCR repository.
 * <p>
 * <p><strong>Purpose:</strong></p>
 * Provides centralized constants for the translation workspace, node type names, property names,
 * and utility functions for handling translation nodes and locale-specific properties.
 * <p>
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Defines the translation workspace name</li>
 * <li>Provides constants for translation node types and property names</li>
 * <li>Includes utility functions for locale-to-property-name conversion</li>
 * <li>Offers methods to retrieve translated values with fallback logic</li>
 * </ul>
 *
 * @author diana.racho (IBM iX)
 * @since 2023-01-01
 */
public final class TranslationNodeTypes {
    public static final String WS_TRANSLATION = "translation";

    private TranslationNodeTypes() {
    }

    /**
     * Constants and utilities for the mgnl:translation node type.
     * <p>
     * This class defines the node type name, property names, and utility methods for working with
     * translation nodes, including locale-based property name generation and value retrieval with fallbacks.
     */
    public static final class Translation {

        public static final String PN_KEY = "key";

        private Translation() {
        }

        // Node type name
        public static final String NAME = MGNL_PREFIX + "translation";
        public static final String PREFIX_NAME = "translation_";

        /**
         * Function that converts a locale to an array of translation property names with fallback order.
         * For locales with country codes, returns both the country-specific and language-only property names.
         * For language-only locales, returns just the language property name.
         * <p>
         * Example: Locale.GERMANY returns ["translation_de_DE", "translation_de"]
         */
        public static final Function<Locale, String[]> LOCALE_TO_PROPERTY_NAMES = locale -> {
            final String[] i18nPropertyNames;
            final String language = locale.getLanguage();
            String i18nProperty = TranslationNodeTypes.Translation.PREFIX_NAME + language;

            final String country = locale.getCountry();
            if (isNotEmpty(country)) {
                i18nPropertyNames = new String[]{TranslationNodeTypes.Translation.PREFIX_NAME + language + "_" + country, i18nProperty};
            } else {
                i18nPropertyNames = new String[]{i18nProperty};
            }
            return i18nPropertyNames;
        };

        /**
         * Retrieves the translated value from a translation node using the specified property names with fallback.
         * Tries the first property name, and if empty or missing, falls back to the second property name if available.
         *
         * @param node the translation node to read from (may be null)
         * @param propertyNames the property names to try in order of preference
         * @return the translated value, or an empty string if not found or node is null
         */
        public static String retrieveValue(Node node, String[] propertyNames) {
            String foundMsg = EMPTY;
            if (node != null && ArrayUtils.getLength(propertyNames) > 0) {
                foundMsg = getString(node, propertyNames[0], EMPTY);
                if (isEmpty(foundMsg) && propertyNames.length > 1) {
                    foundMsg = getString(node, propertyNames[1], EMPTY);
                }
            }
            return foundMsg;
        }
    }
}
