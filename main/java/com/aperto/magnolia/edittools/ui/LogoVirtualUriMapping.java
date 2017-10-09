package com.aperto.magnolia.edittools.ui;

import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.init.MagnoliaConfigurationProperties;

import javax.inject.Inject;

import static info.magnolia.cms.util.RequestDispatchUtil.FORWARD_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.PERMANENT_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.REDIRECT_PREFIX;
import static org.apache.commons.lang.StringUtils.startsWithAny;

/**
 * Uri mapping for logo replacement.
 *
 * @author frank.sommer
 * @since 14.06.2016
 */
public class LogoVirtualUriMapping extends DefaultVirtualURIMapping {

    @Inject
    public LogoVirtualUriMapping(MagnoliaConfigurationProperties magnoliaConfigurationProperties) {
        setFromURI("/VAADIN/themes/admincentraltheme/img/logo-magnolia.svg");
        String logoUri = magnoliaConfigurationProperties.getProperty("magkit.admincentral.logo");
        if (startsWithAny(logoUri, new String[]{FORWARD_PREFIX, PERMANENT_PREFIX, REDIRECT_PREFIX})) {
            setToURI(logoUri);
        }
    }
}
