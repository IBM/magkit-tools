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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQueryResult;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockSystemContext;
import static de.ibmix.magkit.test.cms.context.SystemContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MagnoliaTranslationServiceImpl}.
 *
 * @author frank.sommer
 * @since 2020-01-24
 */
public class MagnoliaTranslationServiceImplTest {

    private FixedLocaleProvider _localeProvider;
    private MagnoliaTranslationServiceImpl _translationService;

    @BeforeEach
    public void setUp() {
        _localeProvider = new FixedLocaleProvider(Locale.GERMAN);

        MessagesManager messagesManager = mockComponentInstance(MessagesManager.class);
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
                messagesFromApp.put("placeholderEndQuote.key", "end' {0}");
                return messagesFromApp.get(key);
            }
        };
    }

    /**
     * Existing value is a valid value.
     */
    @Test
    public void existingKey() {
        assertEquals("valueFromApp", _translationService.translate(_localeProvider, "", new String[]{"existing.key"}));
    }

    /**
     * Parametrized test: messages with quotes and optional placeholders are escaped; formatting removes escape doubling.
     */
    @ParameterizedTest
    @MethodSource("escapeVariants")
    public void replacementEscapingParameterized(final String key, final String expectedEscaped, final String expectedFormatted) {
        String translation = _translationService.translate(_localeProvider, "", new String[]{key});
        assertEquals(expectedEscaped, translation);
        if (expectedFormatted != null) {
            assertEquals(expectedFormatted, MessageFormatterUtils.format(translation, Locale.GERMAN, "replacement"));
        }
    }

    private static Stream<Arguments> escapeVariants() {
        return Stream.of(
            Arguments.of("quote.key", "key'without", null),
            Arguments.of("placeholder.key", "key''with {0}", "key'with replacement"),
            Arguments.of("escaped.key", "key''with {0}", "key'with replacement"),
            Arguments.of("placeholderEndQuote.key", "end'' {0}", "end' replacement")
        );
    }

    /**
     * Choose second key when first produces empty value and second has a valid value.
     */
    @Test
    public void chooseSecondKeyAfterEmptyFirst() {
        assertEquals("valueFromApp", _translationService.translate(_localeProvider, "", new String[]{"empty.key", "existing.key"}));
    }

    /**
     * Skip quoted key and use next valid key.
     */
    @Test
    public void skipQuotedKeyAndUseNext() {
        assertEquals("valueFromApp", _translationService.translate(_localeProvider, "", new String[]{"invalid'key", "existing.key"}));
    }

    /**
     * Fallback returns first key if all keys fail to produce a non-empty value.
     */
    @Test
    public void fallbackToFirstKeyWhenAllFail() {
        assertEquals("empty.key", _translationService.translate(_localeProvider, "", new String[]{"empty.key", "missing.key"}));
    }

    @Test
    void doMessageQuery() throws RepositoryException {
        mockSystemContext(stubJcrSession(TranslationNodeTypes.WS_TRANSLATION));
        String statement = MagnoliaTranslationServiceImpl.BASE_QUERY + "'anyKey'";
        mockQueryResult(TranslationNodeTypes.WS_TRANSLATION, Query.JCR_SQL2, statement);
        _translationService = new MagnoliaTranslationServiceImpl(null, null);
        String message = _translationService.doMessageQuery("anyKey", new String[]{"anyProperty"});
        assertEquals(EMPTY, message);

        Node messageNode = mockNode(TranslationNodeTypes.WS_TRANSLATION, "/message");
        mockQueryResult(TranslationNodeTypes.WS_TRANSLATION, Query.JCR_SQL2, statement, messageNode);
        message = _translationService.doMessageQuery("anyKey", new String[]{"anyProperty"});
        assertEquals(EMPTY, message);

        stubProperty("anyProperty", "foundMessage").of(messageNode);
        message = _translationService.doMessageQuery("anyKey", new String[]{"anyProperty"});
        assertEquals("foundMessage", message);

        stubProperty("anyProperty", EMPTY).of(messageNode);
        message = _translationService.doMessageQuery("anyKey", new String[]{"anyProperty"});
        assertEquals(EMPTY, message);

        stubProperty("otherProperty", "other message").of(messageNode);
        message = _translationService.doMessageQuery("anyKey", new String[]{"anyProperty", "otherProperty"});
        assertEquals("other message", message);

        stubProperty("anyProperty", "any message").of(messageNode);
        message = _translationService.doMessageQuery("anyKey", new String[]{"anyProperty", "otherProperty"});
        assertEquals("any message", message);
    }

    @AfterEach
    public void tearDown() {
        ComponentsMockUtils.clearComponentProvider();
    }
}
