package com.aperto.magnolia.edittools.util;

import com.google.common.base.Predicate;
import info.magnolia.rendering.template.TemplateDefinition;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

/**
 * Klasse um die TemplateDefinition als Baumstrukturen durchlaufen zu können.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 24.11.2014
 */
public class TemplateNamePredicate implements Predicate<TemplateDefinition> {

    private final String _name;

    public TemplateNamePredicate(final String templateName) {
        _name = templateName;
    }

    @Override
    public boolean apply(@Nullable final TemplateDefinition input) {
        return input != null && StringUtils.equals(_name, input.getName());
    }

    public String getName() {
        return _name;
    }
}
