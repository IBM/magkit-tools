package com.aperto.magnolia.edittools.util;

import com.google.common.collect.TreeTraverser;
import info.magnolia.rendering.template.TemplateDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

/**
 * Klasse um die TemplateDefinition als Baumstrukturen durchlaufen zu können.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 24.11.2014
 */
public class TemplateDefinitionTraverser extends TreeTraverser<TemplateDefinition> {

    @Override
    public Iterable<TemplateDefinition> children(final TemplateDefinition root) {
        List<TemplateDefinition> def = Collections.emptyList();

        if (root != null && isNotEmpty(root.getAreas())) {
            def = new ArrayList<>();
            def.addAll(root.getAreas().values());
        }

        return def;
    }
}
