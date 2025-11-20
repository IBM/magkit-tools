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

import info.magnolia.cms.security.User;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.field.EditorPropertyDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.cms.context.I18nContentSupportStubbingOperation.stubLocales;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubUser;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockUser;
import static de.ibmix.magkit.test.cms.security.UserStubbingOperation.stubLanguage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link TranslationFormDefinition}.
 *
 * @author frank.sommer
 * @since 2022-08-23
 */
public class TranslationFormDefinitionTest {
    private SimpleTranslator _simpleTranslator;

    @BeforeEach
    public void setUp() throws Exception {
        final User user = mockUser("Paul", stubLanguage("en"));
        mockWebContext(stubUser(user));
        _simpleTranslator = mockComponentInstance(SimpleTranslator.class);
        when(_simpleTranslator.translate(anyString())).thenReturn("Key");
    }

    /**
     * Verifies property names, ordering and country suffix handling.
     */
    @Test
    public void testPropertyNamesAndOrder() throws RepositoryException {
        mockI18nContentSupport(stubLocales(Locale.ENGLISH, Locale.GERMANY, Locale.FRANCE));
        final TranslationFormDefinition translationFormDefinition = new TranslationFormDefinition();
        final List<EditorPropertyDefinition> properties = translationFormDefinition.getProperties();
        assertEquals(4, properties.size());
        assertTrue(properties.stream().allMatch(p -> p.getLabel() != null));
        assertEquals(TranslationNodeTypes.Translation.PN_KEY, properties.get(0).getName());
        assertEquals(TranslationNodeTypes.Translation.PREFIX_NAME + Locale.ENGLISH.toString(), properties.get(1).getName());
        assertEquals(TranslationNodeTypes.Translation.PREFIX_NAME + Locale.GERMANY.toString(), properties.get(2).getName());
        assertEquals(TranslationNodeTypes.Translation.PREFIX_NAME + Locale.FRANCE.toString(), properties.get(3).getName());
        assertEquals("English", properties.get(1).getLabel());
        assertEquals("German (Germany)", properties.get(2).getLabel());
        assertEquals("French (France)", properties.get(3).getLabel());
    }

    /**
     * Verifies that user locale influences language and country display names.
     */
    @Test
    public void testUserLocaleInfluencesLabels() throws Exception {
        cleanContext();
        final User user = mockUser("Erika", stubLanguage("de"));
        mockWebContext(stubUser(user));
        _simpleTranslator = mockComponentInstance(SimpleTranslator.class);
        when(_simpleTranslator.translate(anyString())).thenReturn("Schlüssel");
        mockI18nContentSupport(stubLocales(Locale.FRANCE, Locale.ITALY));
        final TranslationFormDefinition translationFormDefinition = new TranslationFormDefinition();
        final List<EditorPropertyDefinition> properties = translationFormDefinition.getProperties();
        assertEquals(3, properties.size());
        assertEquals("Schlüssel", properties.get(0).getLabel());
        // German localized names (Java default locale data)
        assertEquals("Französisch (Frankreich)", properties.get(1).getLabel());
        assertEquals("Italienisch (Italien)", properties.get(2).getLabel());
    }

    /**
     * Verifies only key property is created when no locales are configured.
     */
    @Test
    public void testNoLocalesConfigured() throws RepositoryException {
        mockI18nContentSupport(stubLocales());
        final TranslationFormDefinition translationFormDefinition = new TranslationFormDefinition();
        final List<EditorPropertyDefinition> properties = translationFormDefinition.getProperties();
        assertEquals(1, properties.size());
        assertEquals(TranslationNodeTypes.Translation.PN_KEY, properties.get(0).getName());
        assertEquals("Key", properties.get(0).getLabel());
    }

    /**
     * Verifies translator invocation with expected key label pattern.
     */
    @Test
    public void testTranslatorInvocation() throws RepositoryException {
        mockI18nContentSupport(stubLocales(Locale.ENGLISH));
        final TranslationFormDefinition translationFormDefinition = new TranslationFormDefinition();
        translationFormDefinition.getProperties();
        verify(_simpleTranslator).translate("translation.jcrDetail.main.key.label");
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }
}
