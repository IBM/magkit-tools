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

import info.magnolia.ui.editor.CurrentItemProviderDefinition;
import info.magnolia.ui.editor.EditorDefinition;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.field.ConfiguredComplexPropertyDefinition;
import info.magnolia.ui.field.FieldType;
import lombok.EqualsAndHashCode;

import javax.jcr.Node;

/**
 * View definition for the {@link AclOverviewView} custom view.
 *
 * <p><strong>Usage:</strong></p>
 * Used in form definitions where ACL information needs to be displayed
 * for users or groups in the Magnolia admin interface.
 *
 * @author diana.racho (IBM iX)
 * @since 2023-01-01
 */
@EqualsAndHashCode(callSuper = true)
@FieldType("aclOverview")
public class AclOverviewDefinition extends ConfiguredComplexPropertyDefinition<Node> implements EditorDefinition<Node> {
    public AclOverviewDefinition() {
        Class<?> clazz = AclOverviewView.class;
        setImplementationClass((Class<EditorView<Node>>) clazz);
        setItemProvider(new CurrentItemProviderDefinition<>());
    }
}
