package com.aperto.magnolia.edittools.ui;

import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.virtualuri.mapping.DefaultVirtualUriMapping;

import javax.inject.Inject;

import static info.magnolia.cms.util.RequestDispatchUtil.FORWARD_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.PERMANENT_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.REDIRECT_PREFIX;
import static org.apache.commons.lang.StringUtils.startsWithAny;

/**
 * Uri mapping for logo replacement.
 *
 * @deprecated will be deleted with version 1.4.0, no support in M6
 * @author frank.sommer
 * @since 14.06.2016
 */
@Deprecated
public class LogoVirtualUriMapping extends DefaultVirtualUriMapping {

    @Inject
    public LogoVirtualUriMapping(MagnoliaConfigurationProperties magnoliaConfigurationProperties) {
        setFromUri("/VAADIN/themes/admincentraltheme/img/logo-magnolia.svg");
        String logoUri = magnoliaConfigurationProperties.getProperty("magkit.admincentral.logo");
        if (startsWithAny(logoUri, new String[]{FORWARD_PREFIX, PERMANENT_PREFIX, REDIRECT_PREFIX})) {
            setToUri(logoUri);
        }
    }
}
