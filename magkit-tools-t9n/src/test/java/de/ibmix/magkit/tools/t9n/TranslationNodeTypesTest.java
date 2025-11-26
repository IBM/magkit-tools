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

import de.ibmix.magkit.test.jcr.SessionMockUtils;
import de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Locale;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TranslationNodeTypes}.
 *
 * @author frank.sommer
 * @since 2023-09-19
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
    public void valueWithEmptyPropertyNames() throws RepositoryException {
        final Node node = mockNode("translation", "/test/node");
        assertEquals("", Translation.retrieveValue(node, new String[0]));
        assertTrue(Translation.retrieveValue(node, new String[0]).isEmpty());
    }

    /**
     * Verifies retrieveValue returns the value of the first property when present and non-empty.
     */
    @Test
    public void retrieveValueReturnsFirstValue() throws javax.jcr.RepositoryException {
        final Node node = mockNode("translation", "/test/node", stubProperty("translation_de_DE", "Hallo"), stubProperty("translation_de", "HalloFallback"));
        final String[] propertyNames = new String[]{"translation_de_DE", "translation_de"};
        assertEquals("Hallo", TranslationNodeTypes.Translation.retrieveValue(node, propertyNames));
    }

    /**
     * Verifies retrieveValue falls back to second property when first is missing.
     */
    @Test
    public void retrieveValueFallsBackWhenFirstMissing() throws javax.jcr.RepositoryException {
        final Node node = mockNode("translation", "/test/node", stubProperty("translation_de", "HalloFallback"));
        final String[] propertyNames = new String[]{"translation_de_DE", "translation_de"};
        assertEquals("HalloFallback", TranslationNodeTypes.Translation.retrieveValue(node, propertyNames));
    }

    /**
     * Verifies retrieveValue falls back to second property when first is empty string.
     */
    @Test
    public void retrieveValueFallsBackWhenFirstEmpty() throws javax.jcr.RepositoryException {
        final Node node = mockNode("translation", "/test/node", stubProperty("translation_de_DE", ""), stubProperty("translation_de", "HalloFallback"));
        final String[] propertyNames = new String[]{"translation_de_DE", "translation_de"};
        assertEquals("HalloFallback", TranslationNodeTypes.Translation.retrieveValue(node, propertyNames));
    }

    /**
     * Verifies retrieveValue returns empty when both properties are missing or empty.
     */
    @Test
    public void retrieveValueReturnsEmptyWhenBothMissing() throws javax.jcr.RepositoryException {
        final Node node = mockNode("translation", "/test/node");
        final String[] propertyNames = new String[]{"translation_de_DE", "translation_de"};
        assertEquals("", TranslationNodeTypes.Translation.retrieveValue(node, propertyNames));
    }

    @AfterEach
    public void tearDown() {
        SessionMockUtils.cleanSession();
    }
}
