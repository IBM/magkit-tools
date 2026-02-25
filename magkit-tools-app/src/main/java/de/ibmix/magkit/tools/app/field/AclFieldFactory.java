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
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import jakarta.inject.Inject;

/**
 * Factory for creating AclField instances in Magnolia forms.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Creates configured AclField instances</li>
 *   <li>Manages field lifecycle and dependencies</li>
 *   <li>Integrates with Magnolia's form framework</li>
 * </ul>
 *
 * @author diana.racho (IBM iX)
 * @since 2023-01-01
 */
public class AclFieldFactory extends AbstractFieldFactory<AclFieldDefinition, Object> {

    private final Item _relatedFieldItem;
    private final SimpleTranslator _i18n;

    /**
     * Constructs a new AclFieldFactory instance.
     *
     * @param definition the field definition
     * @param relatedFieldItem the JCR item to display ACL information for
     * @param uiContext the UI context
     * @param i18nAuthoringSupport the i18n authoring support
     * @param i18n the translator for i18n support
     */
    @Inject
    public AclFieldFactory(AclFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport, SimpleTranslator i18n) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
        _relatedFieldItem = relatedFieldItem;
        _i18n = i18n;
    }

    /**
     * Creates and returns a new AclField instance.
     *
     * @return the created ACL field component
     */
    @Override
    protected Field<Object> createFieldComponent() {
        return new AclField(_relatedFieldItem, _i18n);
    }

    /**
     * Sets the property data source and default value.
     * No specific action needed for ACL fields as they are read-only display fields.
     *
     * @param property the property (unused)
     */
    @Override
    public void setPropertyDataSourceAndDefaultValue(Property property) {
        // Do nothing specific
    }

    /**
     * Returns the type of field values.
     *
     * @return String.class
     */
    @Override
    protected Class<?> getFieldType() {
        return String.class;
    }
}
