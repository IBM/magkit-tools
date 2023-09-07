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

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.ui.CustomField;
import info.magnolia.cms.security.Permission;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static info.magnolia.jcr.util.SessionUtil.getNodeByIdentifier;
import static info.magnolia.repository.RepositoryConstants.USER_GROUPS;
import static info.magnolia.repository.RepositoryConstants.USER_ROLES;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.substringAfter;

/**
 * Field that shows acl of current user for different repositories.
 *
 * @author diana.racho (IBM iX)
 */
public class AclField extends CustomField<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AclField.class);

    private static final String CSS_PREFIX = "dependencies-";
    private static final String LAYOUT_CSS_CLASS = CSS_PREFIX + "layout";
    private static final String SECTION_CSS_STYLE = CSS_PREFIX + "section";
    private static final String TXT_CSS_STYLE = CSS_PREFIX + "txt";
    private static final String ROW_CSS_STYLE = CSS_PREFIX + "row";

    private final Item _relatedFieldItem;
    private GridLayout _layout;

    private int _currentRow;

    private final SimpleTranslator _i18n;

    public AclField(Item relatedFieldItem, SimpleTranslator i18n) {
        _relatedFieldItem = relatedFieldItem;
        _layout = new GridLayout();
        _i18n = i18n;
    }

    @Override
    protected Component initContent() {
        AbstractJcrNodeAdapter item;
        _currentRow = 0;
        _layout.setColumns(3);
        _layout.setWidth("100%");
        _layout.setColumnExpandRatio(0, 0.6f);
        _layout.setColumnExpandRatio(1, 0.2f);
        _layout.setColumnExpandRatio(2, 0.25f);
        _layout.addStyleName(LAYOUT_CSS_CLASS);
        _layout.setSpacing(true);
        addHeaderRow();
        if (_relatedFieldItem instanceof AbstractJcrNodeAdapter) {
            item = (AbstractJcrNodeAdapter) _relatedFieldItem;
        } else {
            LOGGER.warn("Item {} is not a JcrItemAdapter. Field will not be initialized.", _relatedFieldItem);
            return null;
        }
        Node node = item.getJcrItem();
        List<Node> roles = getRoles(node);
        List<Node> rolesFromGroup = getRolesFromGroup(node);
        if (!rolesFromGroup.isEmpty()) {
            roles.addAll(rolesFromGroup);
        }
        if (!roles.isEmpty()) {
            Map<String, List<Acl>> repositories = new HashMap<>();
            for (Node role : roles) {
                Map<String, List<Acl>> newRepositories = getRepositories(role);
                if (!newRepositories.isEmpty()) {
                    repositories.putAll(newRepositories);
                }
            }
            if (!repositories.isEmpty()) {
                int size = repositories.size();
                for (Map.Entry<String, List<Acl>> map : repositories.entrySet()) {
                    size += (map.getValue()).size() + 1;
                }

                _layout.setRows(size);
                for (Map.Entry<String, List<Acl>> map : repositories.entrySet()) {
                    addReferencesToGrid(map.getKey(), map.getValue());
                }
            }
        }

        return _layout;
    }

    private void addHeaderRow() {
        Label permission = new Label();
        permission.addStyleName(SECTION_CSS_STYLE);
        permission.setValue("PERMISSION");
        Label path = new Label();
        path.addStyleName(SECTION_CSS_STYLE);
        path.setValue("PATH");
        Label role = new Label();
        role.addStyleName(SECTION_CSS_STYLE);
        role.setValue("ROLE");
        _layout.addComponent(path, 0, _currentRow);
        _layout.addComponent(permission, 1, _currentRow);
        _layout.addComponent(role, 2, _currentRow);
        _currentRow++;
    }

    private List<Node> getRoles(Node node) {
        List<Node> roleNodes = new ArrayList<>();
        try {
            Node roles = node.getNode("roles");
            if (roles != null) {
                List<Property> properties = getProperties(roles);
                for (Property property : properties) {
                    String roleUuid = property.getString();
                    Node role = getNodeByIdentifier(USER_ROLES, roleUuid);
                    if (role != null) {
                        roleNodes.add(role);
                    }
                }
            }
        } catch (RepositoryException e) {
            LOGGER.warn("Can't get acls for user role '" + getPathIfPossible(node) + "'.", e);
        }
        return roleNodes;
    }

    private List<Node> getRolesFromGroup(Node node) {
        List<Node> roleNodes = new ArrayList<>();
        try {
            Node groups = node.getNode("groups");
            if (groups != null) {
                List<Property> properties = getProperties(groups);
                for (Property property : properties) {
                    String groupUuid = property.getString();
                    Node group = getNodeByIdentifier(USER_GROUPS, groupUuid);
                    if (group != null) {
                        roleNodes.addAll(getRolesFromGroup(group));
                        roleNodes.addAll(getRoles(group));
                    }
                }
            }
        } catch (RepositoryException e) {
            LOGGER.warn("Can't get acls for user group '" + getPathIfPossible(node) + "'.", e);
        }
        return roleNodes;
    }

    private static List<Property> getProperties(Node node) {
        List<Property> result = new LinkedList<>();
        try {
            PropertyIterator properties = node.getProperties();
            while (properties.hasNext()) {
                Property property = properties.nextProperty();
                String name = property.getName();
                boolean isNamespacedProperty = StringUtils.contains(name, ":");

                if (!isNamespacedProperty) {
                    result.add(property);
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error("Could not get properties for node " + node, e);
        }
        return result;
    }

    private Map<String, List<Acl>> getRepositories(Node node) {
        Map<String, List<Acl>> repositoryMap = new HashMap<>();
        try {
            NodeIterator repositoryNodes = node.getNodes("acl_*");
            while (repositoryNodes.hasNext()) {
                Node repositoryNode = repositoryNodes.nextNode();
                NodeIterator aclNodes = repositoryNode.getNodes();
                List<Acl> aclList = new ArrayList<>();
                while (aclNodes.hasNext()) {
                    Node aclNode = aclNodes.nextNode();
                    String path = PropertyUtil.getString(aclNode, "path");
                    String permission = getPermissionString(PropertyUtil.getLong(aclNode, "permissions"));
                    if (isNotBlank(permission)) {
                        aclList.add(new Acl(permission, path, node.getName()));
                    }
                }
                if (!aclList.isEmpty()) {
                    repositoryMap.put(substringAfter(repositoryNode.getName(), "acl_"), aclList);
                }
            }
        } catch (RepositoryException e) {
            LOGGER.warn("Can't get acls for user '" + getPathIfPossible(node) + "'.", e);
        }
        return repositoryMap;
    }

    private String getPermissionString(Long permission) {
        String permissionString = EMPTY;
        if (permission != null) {
            if (permission == Permission.ALL) {
                permissionString = _i18n.translate("security.workspace.field.readWrite");
            } else if (permission == Permission.READ) {
                permissionString = _i18n.translate("security.workspace.field.readOnly");
            } else if (permission == Permission.NONE) {
                permissionString = _i18n.translate("security.workspace.field.denyAccess");
            }
        }
        return permissionString;
    }

    @Override
    public Class<?> getType() {
        return Object.class;
    }

    private void addReferencesToGrid(String headline, List<Acl> aclList) {
        Label headlineLabel = new Label();
        headlineLabel.setValue(headline);
        headlineLabel.addStyleName(SECTION_CSS_STYLE);
        _layout.addComponent(headlineLabel, 0, _currentRow, 2, _currentRow);
        _currentRow++;

        if (aclList.isEmpty()) {
            Label label = new Label();
            label.setValue("keine Rechte vorhanden");
            label.addStyleName(TXT_CSS_STYLE);
            _layout.insertRow(_currentRow);
            _layout.addComponent(label, 0, _currentRow, 2, _currentRow);
            _currentRow++;
        } else {
            for (Acl acl : aclList) {
                Label permission = new Label();
                permission.addStyleName(ROW_CSS_STYLE);
                permission.setValue(acl.getPermission());
                Label path = new Label();
                path.addStyleName(ROW_CSS_STYLE);
                path.setValue(acl.getPath());
                Label role = new Label();
                role.addStyleName(ROW_CSS_STYLE);
                role.setValue(acl.getRole());
                _layout.addComponent(path, 0, _currentRow);
                _layout.addComponent(permission, 1, _currentRow);
                _layout.addComponent(role, 2, _currentRow);
                _currentRow++;
            }
        }
    }

    private class Acl {
        private String _permission;
        private String _path;
        private String _role;

        Acl(String permission, String path, String role) {
            _permission = permission;
            _path = path;
            _role = role;
        }

        public String getPermission() {
            return _permission;
        }

        public void setPermission(String permission) {
            _permission = permission;
        }

        public String getPath() {
            return _path;
        }

        public void setPath(String path) {
            _path = path;
        }

        public String getRole() {
            return _role;
        }

        public void setRole(String role) {
            _role = role;
        }
    }
}
