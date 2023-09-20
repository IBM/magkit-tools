package de.ibmix.magkit.tools.edit.views;

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

import de.ibmix.magkit.tools.edit.util.LinkService;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import info.magnolia.pages.app.editor.statusbar.activationstatus.ActivationStatusViewImpl;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Extends the status bar of the pages editor sub app.
 *
 * @author frank.sommer
 * @since 08.05.14
 */
public class EditorToolsActivationStatusViewImpl extends ActivationStatusViewImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditorToolsActivationStatusViewImpl.class);

    private static final long serialVersionUID = 8175070407228549735L;

    private final transient SubAppContext _subAppContext;
    private final LinkService _linkService;
    private boolean _linkStatus;

    @Inject
    public EditorToolsActivationStatusViewImpl(final SubAppContext subAppContext, final LinkService linkService) {
        super();
        _subAppContext = subAppContext;
        _linkService = linkService;
    }

    @Override
    public void setActivationStatus(final String status) {
        LOGGER.debug("Set the activation status in status bar.");
        super.setActivationStatus(status);

        HorizontalLayout activationStatusViewComponents = (HorizontalLayout) asVaadinComponent();
        if (activationStatusViewComponents.getComponentCount() == 2 && _linkStatus) {
            final DetailLocation location = DetailLocation.wrap(_subAppContext.getLocation());
            final String nodePath = location.getNodePath();
            final String publicUrl = _linkService.getPublicLink(nodePath);
            if (isNotBlank(publicUrl)) {
                exchangeComponent(activationStatusViewComponents, publicUrl);
            }
        }
    }

    private void exchangeComponent(final HorizontalLayout activationStatusViewComponents, final String publicUrl) {
        Component component = activationStatusViewComponents.getComponent(1);
        if (component instanceof Label) {
            Label statusLabel = (Label) component;
            Link publicLink = new Link(statusLabel.getValue(), new ExternalResource(publicUrl));
            publicLink.setTargetName("_blank");
            activationStatusViewComponents.removeComponent(statusLabel);
            activationStatusViewComponents.addComponent(publicLink, 1);
        }
    }

    @Override
    public void setIconStyle(final String iconStyle) {
        super.setIconStyle(iconStyle);
        _linkStatus = !iconStyle.contains("red");
    }
}
