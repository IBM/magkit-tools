package de.ibmix.magkit.tools.app.field;

/*-
 * #%L
 * magkit-tools-app
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

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Field;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link AclFieldFactory}.
 *
 * @author wolf.bubenik
 * @since 2025-11-17
 */
class AclFieldFactoryTest {

    private AclFieldFactory _factory;

    @BeforeEach
    void setUp() {
        AclFieldDefinition definition = new AclFieldDefinition();
        Item relatedFieldItem = mock(Item.class);
        UiContext uiContext = mock(UiContext.class);
        I18NAuthoringSupport i18nAuthoringSupport = mock(I18NAuthoringSupport.class);
        SimpleTranslator i18n = mock(SimpleTranslator.class);

        _factory = new AclFieldFactory(
            definition,
            relatedFieldItem,
            uiContext,
            i18nAuthoringSupport,
            i18n
        );
    }

    @Test
    void testConstructor() {
        assertNotNull(_factory);
    }

    @Test
    void testCreateFieldComponent() {
        Field<Object> field = _factory.createFieldComponent();

        assertNotNull(field);
        assertInstanceOf(AclField.class, field);
    }

    @Test
    void testSetPropertyDataSourceAndDefaultValue() {
        Property property = mock(Property.class);

        _factory.setPropertyDataSourceAndDefaultValue(property);
    }

    @Test
    void testSetPropertyDataSourceAndDefaultValueWithNullProperty() {
        _factory.setPropertyDataSourceAndDefaultValue(null);
    }

    @Test
    void testGetFieldType() {
        Class<?> fieldType = _factory.getFieldType();

        assertNotNull(fieldType);
        assertEquals(String.class, fieldType);
    }
}

