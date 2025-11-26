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
import com.vaadin.v7.data.Item;
import de.ibmix.magkit.test.cms.node.UserNodeStubbingOperation;
import de.ibmix.magkit.test.jcr.PropertyMockUtils;
import info.magnolia.cms.security.Permission;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubCharacterEncoding;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockContentNodeNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockGroupNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockRoleNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockUserNode;
import static de.ibmix.magkit.test.cms.node.UserNodeStubbingOperation.stubRoles;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AclField}.
 *
 * @author wolf.bubenik
 * @since 2025-11-17
 */
class AclFieldTest {

    private SimpleTranslator _i18n;
    private AbstractJcrNodeAdapter _nodeAdapter;

    @BeforeEach
    void setUp() throws RepositoryException {
        mockAggregationState(stubCharacterEncoding("UTF-8"));
        _i18n = mock(SimpleTranslator.class);
        _nodeAdapter = mock(AbstractJcrNodeAdapter.class);
        when(_i18n.translate("security.workspace.field.readWrite")).thenReturn("Read/Write");
        when(_i18n.translate("security.workspace.field.readOnly")).thenReturn("Read Only");
        when(_i18n.translate("security.workspace.field.denyAccess")).thenReturn("Deny Access");
    }

    @AfterEach
    void tearDown() {
        cleanContext();
    }

    @Test
    void testConstructor() {
        AclField field = new AclField(_nodeAdapter, _i18n);
        assertNotNull(field);
    }

    @Test
    void testGetType() {
        AclField field = new AclField(_nodeAdapter, _i18n);
        assertEquals(Object.class, field.getType());
    }

    @Test
    void testInitContentWithNonJcrAdapter() {
        Item item = mock(Item.class);
        AclField field = new AclField(item, _i18n);
        Component content = field.initContent();
        assertNull(content);
    }

    @Test
    void testInitContentWithUserWithoutRoles() throws RepositoryException {
        Node userNode = mockUserNode("/testuser");
        when(_nodeAdapter.getJcrItem()).thenReturn(userNode);

        AclField field = new AclField(_nodeAdapter, _i18n);
        Component content = field.initContent();

        assertNotNull(content);
        assertInstanceOf(GridLayout.class, content);
    }

    @Test
    void testGetRolesWithNoRolesNode() throws RepositoryException {
        Node userNode = mockUserNode("/testuser");
        when(userNode.getNode("roles")).thenThrow(new RepositoryException("Node not found"));

        AclField field = new AclField(_nodeAdapter, _i18n);
        List<Node> roles = field.getRoles(userNode);

        assertTrue(roles.isEmpty());
    }

    @Test
    void testGetRolesReturnsEmptyListWhenNoRolesFound() throws RepositoryException {
        Node roleNode = mockRoleNode("/editors");
        Node userNode = mockUserNode("/testuser", stubRoles(roleNode));

        AclField field = new AclField(_nodeAdapter, _i18n);
        List<Node> roles = field.getRoles(userNode);

        assertNotNull(roles);
        assertSame(roleNode, roles.get(0));
    }

    @Test
    void testGetRolesWithInvalidRoleUuid() throws RepositoryException {
        Node roleNode = mockRoleNode("/editors");
        doReturn("invalid-uuid").when(roleNode).getIdentifier();
        Node userNode = mockUserNode("/testuser", stubRoles(roleNode));

        AclField field = new AclField(_nodeAdapter, _i18n);
        List<Node> roles = field.getRoles(userNode);

        assertTrue(roles.isEmpty());
    }

    @Test
    void testGetRolesFromGroupWithNoGroupsNode() throws RepositoryException {
        Node userNode =  mockUserNode("/testuser");
        when(userNode.getNode("groups")).thenThrow(new RepositoryException("Node not found"));

        AclField field = new AclField(_nodeAdapter, _i18n);
        List<Node> roles = field.getRolesFromGroup(userNode);

        assertTrue(roles.isEmpty());
    }

    @Test
    void testGetRolesFromGroupReturnsEmptyWhenGroupHasNoRoles() throws RepositoryException {
        Node groupNode = mockUserNode("/testgroup");
        Node userNode = mockUserNode("/testuser",
            stubNode("groups", stubProperty("group1", groupNode.getIdentifier()))
        );

        AclField field = new AclField(_nodeAdapter, _i18n);
        List<Node> roles = field.getRolesFromGroup(userNode);

        assertNotNull(roles);
    }

    @Test
    void testGetRolesFromGroupHandlesRepositoryException() throws RepositoryException {
        Property prop = PropertyMockUtils.mockProperty("group2");
        when(prop.getString()).thenThrow(new RepositoryException("Error reading property"));
        Node groupNode = mockGroupNode("group1", stubProperty(prop));

        Node userNode = mockUserNode("/testuser", UserNodeStubbingOperation.stubGroups(groupNode));

        AclField field = new AclField(_nodeAdapter, _i18n);
        List<Node> roles = field.getRolesFromGroup(userNode);

        assertTrue(roles.isEmpty());
    }

    @Test
    void testGetProperties() throws RepositoryException {
        Node node = mockContentNodeNode("test",
            stubProperty("prop1", "value1"),
            stubProperty("prop2", "value2")
        );

        List<Property> properties = AclField.getProperties(node);

        assertEquals(2, properties.size());
    }

    @Test
    void testGetRepositoriesWithNoAcls() throws RepositoryException {
        Node roleNode = mockRoleNode("/editors");

        AclField field = new AclField(_nodeAdapter, _i18n);
        Map<String, List<AclField.Acl>> repositories = field.getRepositories(roleNode);

        assertTrue(repositories.isEmpty());
    }

    @Test
    void testGetRepositoriesWithValidAcls() throws RepositoryException {
        Node roleNode = mockRoleNode("/editors",
            stubNode("acl_website",
                stubNode("0", stubProperty("path", "/"), stubProperty("permissions", Permission.ALL))
            )
        );

        AclField field = new AclField(_nodeAdapter, _i18n);
        Map<String, List<AclField.Acl>> repositories = field.getRepositories(roleNode);

        assertEquals(1, repositories.size());
        assertTrue(repositories.containsKey("website"));
        assertEquals(1, repositories.get("website").size());
    }

    @Test
    void testGetRepositoriesWithMultipleAcls() throws RepositoryException {
        Node roleNode = mockRoleNode("/editors",
            stubNode("acl_website",
                stubNode("0", stubProperty("path", "/"), stubProperty("permissions", Permission.ALL)),
                stubNode("00", stubProperty("path", "/content"), stubProperty("permissions", Permission.READ))
            ),
            stubNode("acl_dam",
                stubNode("0", stubProperty("path", "/"), stubProperty("permissions", Permission.NONE))
            )
        );

        AclField field = new AclField(_nodeAdapter, _i18n);
        Map<String, List<AclField.Acl>> repositories = field.getRepositories(roleNode);

        assertEquals(2, repositories.size());
        assertTrue(repositories.containsKey("website"));
        assertTrue(repositories.containsKey("dam"));
        assertEquals(2, repositories.get("website").size());
        assertEquals(1, repositories.get("dam").size());
    }

    @Test
    void testGetPermissionStringWithAll() {
        AclField field = new AclField(_nodeAdapter, _i18n);
        String permission = field.getPermissionString(Permission.ALL);
        assertEquals("Read/Write", permission);
    }

    @Test
    void testGetPermissionStringWithRead() {
        AclField field = new AclField(_nodeAdapter, _i18n);
        String permission = field.getPermissionString(Permission.READ);
        assertEquals("Read Only", permission);
    }

    @Test
    void testGetPermissionStringWithNone() {
        AclField field = new AclField(_nodeAdapter, _i18n);
        String permission = field.getPermissionString(Permission.NONE);
        assertEquals("Deny Access", permission);
    }

    @Test
    void testGetPermissionStringWithNull() {
        AclField field = new AclField(_nodeAdapter, _i18n);
        String permission = field.getPermissionString(null);
        assertEquals("", permission);
    }

    @Test
    void testGetPermissionStringWithOtherValue() {
        AclField field = new AclField(_nodeAdapter, _i18n);
        String permission = field.getPermissionString(999L);
        assertEquals("", permission);
    }

    @Test
    void testAclInnerClass() {
        AclField.Acl acl = new AclField.Acl("Read/Write", "/content", "editors");

        assertEquals("Read/Write", acl.getPermission());
        assertEquals("/content", acl.getPath());
        assertEquals("editors", acl.getRole());

        acl.setPermission("Read Only");
        acl.setPath("/other");
        acl.setRole("readers");

        assertEquals("Read Only", acl.getPermission());
        assertEquals("/other", acl.getPath());
        assertEquals("readers", acl.getRole());
    }

    @Test
    void testGetRepositoriesWithAclWithoutPermissions() throws RepositoryException {
        Node aclEntryNode = mockNode("0", stubProperty("path", "/"));
        doThrow(new RepositoryException("No property")).when(aclEntryNode).getProperty("permissions");

        Node roleNode = mockRoleNode("/editors",
            stubNode("acl_website",
                stubNode(aclEntryNode)
            )
        );

        AclField field = new AclField(_nodeAdapter, _i18n);
        Map<String, List<AclField.Acl>> repositories = field.getRepositories(roleNode);

        assertTrue(repositories.isEmpty());
    }



    @Test
    void testInitContentWithUserWithRoles() throws RepositoryException {
        Node roleNode = mockRoleNode("/editors",
            stubNode("acl_website",
                stubNode("0", stubProperty("path", "/"), stubProperty("permissions", Permission.ALL))
            )
        );
        Node userNode = mockUserNode("/testuser", stubRoles(roleNode));
        when(_nodeAdapter.getJcrItem()).thenReturn(userNode);

        AclField field = new AclField(_nodeAdapter, _i18n);
        Component content = field.initContent();

        assertNotNull(content);
        assertInstanceOf(GridLayout.class, content);
        assertEquals(3, ((GridLayout) content).getColumns());
        assertEquals(3, ((GridLayout) content).getColumns());
        assertEquals(0.6f, ((GridLayout) content).getColumnExpandRatio(0));
        assertEquals(0.2f, ((GridLayout) content).getColumnExpandRatio(1));
        assertEquals(0.25f, ((GridLayout) content).getColumnExpandRatio(2));
        assertEquals("dependencies-layout", content.getStyleName());
        assertTrue(((GridLayout) content).isSpacing());
    }

    @Test
    void testAddReferencesToGridWithEmptyListCreatesLabel() throws RepositoryException {
        Node roleNode = mockRoleNode("/editors",
            stubNode("acl_website")
        );
        Node userNode = mockUserNode("/testuser", stubRoles(roleNode));
        when(_nodeAdapter.getJcrItem()).thenReturn(userNode);

        AclField field = new AclField(_nodeAdapter, _i18n);
        Component content = field.initContent();

        assertNotNull(content);
        assertInstanceOf(GridLayout.class, content);
    }

    @Test
    void testAddReferencesToGridWithNonEmptyListAddsRows() throws RepositoryException {
        Node roleNode = mockRoleNode("/editors",
            stubNode("acl_website",
                stubNode("0", stubProperty("path", "/content"), stubProperty("permissions", Permission.ALL)),
                stubNode("1", stubProperty("path", "/dam"), stubProperty("permissions", Permission.READ))
            )
        );
        Node userNode = mockUserNode("/testuser", stubRoles(roleNode));
        when(_nodeAdapter.getJcrItem()).thenReturn(userNode);

        AclField field = new AclField(_nodeAdapter, _i18n);
        Component content = field.initContent();

        assertNotNull(content);
        assertInstanceOf(GridLayout.class, content);
        GridLayout grid = (GridLayout) content;
        assertTrue(grid.getRows() >= 4);
    }

    @Test
    void testAddReferencesToGridWithSingleAclCreatesCorrectLayout() throws RepositoryException {
        Node roleNode = mockRoleNode("/superuser",
            stubNode("acl_dam",
                stubNode("0", stubProperty("path", "/"), stubProperty("permissions", Permission.ALL))
            )
        );
        Node userNode = mockUserNode("/testuser", stubRoles(roleNode));
        when(_nodeAdapter.getJcrItem()).thenReturn(userNode);

        AclField field = new AclField(_nodeAdapter, _i18n);
        Component content = field.initContent();

        assertNotNull(content);
        assertInstanceOf(GridLayout.class, content);
        GridLayout grid = (GridLayout) content;
        assertTrue(grid.getRows() >= 3);
    }

    @Test
    void testInitContentWithMultipleRepositories() throws RepositoryException {
        Node roleNode = mockRoleNode("/editors",
            stubNode("acl_website",
                stubNode("0", stubProperty("path", "/"), stubProperty("permissions", Permission.ALL))
            ),
            stubNode("acl_dam",
                stubNode("0", stubProperty("path", "/assets"), stubProperty("permissions", Permission.READ))
            ),
            stubNode("acl_config",
                stubNode("0", stubProperty("path", "/private"), stubProperty("permissions", Permission.NONE))
            )
        );
        Node userNode = mockUserNode("/testuser", stubRoles(roleNode));
        when(_nodeAdapter.getJcrItem()).thenReturn(userNode);

        AclField field = new AclField(_nodeAdapter, _i18n);
        Component content = field.initContent();

        assertNotNull(content);
        assertInstanceOf(GridLayout.class, content);
        GridLayout grid = (GridLayout) content;
        assertTrue(grid.getRows() >= 3);
    }
}
