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

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.User;
import info.magnolia.ui.field.EditorPropertyDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubUser;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the form creation.
 *
 * @author frank.sommer
 * @since 23.08.2022
 */
public class TranslationFormDefinitionTest {
    @Before
    public void setUp() throws Exception {
        final User user = mock(User.class);
        when(user.getLanguage()).thenReturn("en");
        mockWebContext(stubUser(user));
    }

    @Test
    public void testLocaleFields() {
        final I18nContentSupport i18nContentSupport = mockComponentInstance(I18nContentSupport.class);
        when(i18nContentSupport.getLocales()).thenReturn(List.of(Locale.ENGLISH, Locale.UK));

        final TranslationFormDefinition translationFormDefinition = new TranslationFormDefinition();
        final List<EditorPropertyDefinition> properties = translationFormDefinition.getProperties();

        assertThat(properties.size(), is(2));
        assertThat(properties.get(0).getLabel(), equalTo("English"));
        assertThat(properties.get(1).getLabel(), equalTo("English (United Kingdom)"));
    }

    @After
    public void tearDown() {
        cleanContext();
    }
}
