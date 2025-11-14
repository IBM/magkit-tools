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

import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.i18nsystem.DefaultMessageBundlesLoader;
import info.magnolia.i18nsystem.FixedLocaleProvider;
import info.magnolia.i18nsystem.util.MessageFormatterUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the translation service.
 *
 * @author frank.sommer
 * @since 24.01.2020
 */
public class MagnoliaTranslationServiceImplTest {

    private FixedLocaleProvider _localeProvider;
    private MagnoliaTranslationServiceImpl _translationService;

    @BeforeEach
    public void setUp() {
        _localeProvider = new FixedLocaleProvider(Locale.GERMAN);

        MessagesManager messagesManager = ComponentsMockUtils.mockComponentInstance(MessagesManager.class);
        when(messagesManager.getDefaultLocale()).thenReturn(Locale.ENGLISH);

        DefaultMessageBundlesLoader messageBundlesLoader = mock(DefaultMessageBundlesLoader.class);

        _translationService = new MagnoliaTranslationServiceImpl(null, () -> messageBundlesLoader) {
            @Override
            String doMessageQuery(final String key, final String[] i18nPropertyNames) {
                Map<String, String> messagesFromApp = new HashMap<>();
                messagesFromApp.put("invalid'key", "---");
                messagesFromApp.put("empty.key", "");
                messagesFromApp.put("blank.key", " ");
                messagesFromApp.put("existing.key", "valueFromApp");
                messagesFromApp.put("quote.key", "key'without");
                messagesFromApp.put("placeholder.key", "key'with {0}");
                messagesFromApp.put("escaped.key", "key''with {0}");
                return messagesFromApp.get(key);
            }
        };
    }

    /**
     * Do no search query and handle like a message key.
     */
    @Test
    public void invalidKey() {
        assertEquals("invalid'key", _translationService.translate(_localeProvider, "", new String[]{"invalid'key"}));
    }

    /**
     * On missing key, the key itself is returned.
     */
    @Test
    public void missingKey() {
        assertEquals("missing.key", _translationService.translate(_localeProvider, "", new String[]{"missing.key"}));
    }

    /**
     * Handles empty key like missing key.
     */
    @Test
    public void emptyKey() {
        assertEquals("empty.key", _translationService.translate(_localeProvider, "", new String[]{"empty.key"}));
    }

    /**
     * Blank value is a valid value.
     */
    @Test
    public void blankKey() {
        assertEquals(" ", _translationService.translate(_localeProvider, "", new String[]{"blank.key"}));
    }

    /**
     * Existing value is a valid value.
     */
    @Test
    public void existingKey() {
        assertEquals("valueFromApp", _translationService.translate(_localeProvider, "", new String[]{"existing.key"}));
    }

    /**
     * Values with single quote with placeholder and without.
     */
    @Test
    public void replacementEscaping() {
        assertEquals("key'without", _translationService.translate(_localeProvider, "", new String[]{"quote.key"}));

        String placeholderValue = _translationService.translate(_localeProvider, "", new String[]{"placeholder.key"});
        assertEquals("key''with {0}", placeholderValue);
        assertEquals("key'with replacement", MessageFormatterUtils.format(placeholderValue, Locale.GERMAN, "replacement"));

        placeholderValue = _translationService.translate(_localeProvider, "", new String[]{"escaped.key"});
        assertEquals("key''with {0}", placeholderValue);
        assertEquals("key'with replacement", MessageFormatterUtils.format(placeholderValue, Locale.GERMAN, "replacement"));
    }

    @AfterEach
    public void tearDown() {
        ComponentsMockUtils.clearComponentProvider();
    }
}
