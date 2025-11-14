package de.ibmix.magkit.tools.t9n;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Translation
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

import de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation;
import info.magnolia.test.mock.jcr.MockNode;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the translation node type.
 *
 * @author frank.sommer
 * @since 19.09.2023
 */
public class TranslationNodeTypesTest {
    @Test
    public void languagePropertyName() {
        final String[] propertyNames = Translation.LOCALE_TO_PROPERTY_NAMES.apply(Locale.GERMAN);
        assertEquals(1, propertyNames.length);
        assertEquals("translation_de", propertyNames[0]);
    }

    @Test
    public void localePropertyNames() {
        final String[] propertyNames = Translation.LOCALE_TO_PROPERTY_NAMES.apply(Locale.CANADA_FRENCH);
        assertEquals(2, propertyNames.length);
        assertEquals("translation_fr_CA", propertyNames[0]);
        assertEquals("translation_fr", propertyNames[1]);
    }

    @Test
    public void valueFromNull() {
        assertEquals("", Translation.retrieveValue(null, new String[]{"1"}));
    }

    @Test
    public void valueWithEmptyPropertyNames() {
        final Node node = new MockNode();
        assertEquals("", Translation.retrieveValue(node, new String[0]));
        assertTrue(Translation.retrieveValue(node, new String[0]).isEmpty());
    }
}
