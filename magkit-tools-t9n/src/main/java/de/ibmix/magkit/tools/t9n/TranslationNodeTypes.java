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
 * Node type of translation.
 *
 * @author diana.racho (IBM iX)
 */
public final class TranslationNodeTypes {
    public static final String WS_TRANSLATION = "translation";

    private TranslationNodeTypes() {
    }

    /**
     * Represents the mgnl:translation node type.
     */
    public static final class Translation {

        public static final String PN_KEY = "key";

        private Translation() {
        }

        // Node type name
        public static final String NAME = MGNL_PREFIX + "translation";
        public static final String PREFIX_NAME = "translation_";

        /**
         * Converts a locale to the translation app property names.
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
         * Retrieve the translated label from a translation node.
         *
         * @param node          translation node
         * @param propertyNames translation property names
         * @return translated label
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
