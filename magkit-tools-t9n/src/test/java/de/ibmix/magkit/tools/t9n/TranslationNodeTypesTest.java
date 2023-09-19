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
import org.junit.Test;

import javax.jcr.Node;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

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
        assertThat(propertyNames.length, is(1));
        assertThat(propertyNames[0], equalTo("translation_de"));
    }

    @Test
    public void localePropertyNames() {
        final String[] propertyNames = Translation.LOCALE_TO_PROPERTY_NAMES.apply(Locale.CANADA_FRENCH);
        assertThat(propertyNames.length, is(2));
        assertThat(propertyNames[0], equalTo("translation_fr_CA"));
        assertThat(propertyNames[1], equalTo("translation_fr"));
    }

    @Test
    public void valueFromNull() {
        assertThat(Translation.retrieveValue(null, new String[]{"1"}), equalTo(""));
    }

    @Test
    public void valueWithEmptyPropertyNames() {
        final Node node = new MockNode();
        assertThat(Translation.retrieveValue(node, new String[0]), equalTo(""));
    }
}
