package de.ibmix.magkit.tools.edit.action;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
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

import de.ibmix.magkit.core.utils.NodeUtils;
import com.google.inject.Inject;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.location.LocationController;
import org.apache.commons.lang.StringUtils;

import javax.jcr.Node;

/**
 * Action to open the jump to the browser sub app with the current location.
 *
 * @author frank.sommer
 * @since 15.01.16
 */
public class OpenPageBrowserLocationAction extends OpenAppViewLocationAction {

    private final ValueContext<Node> _valueContext;

    @Inject
    public OpenPageBrowserLocationAction(final OpenPageBrowserLocationActionDefinition definition, final ValueContext<Node> valueContext, final LocationController locationController) {
        super(definition, locationController);
        _valueContext = valueContext;
    }

    @Override
    protected String getNodePath() {
        return _valueContext.getSingle()
            .map(node -> NodeUtils.getAncestorOrSelf(node, NodeUtils.IS_PAGE))
            .map(NodeUtil::getPathIfPossible)
            .orElse(StringUtils.EMPTY);
    }
}
