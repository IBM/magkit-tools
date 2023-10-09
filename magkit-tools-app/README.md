# Magnolia Kit Tools App #
Extended Tools Module for Magnolia.

### Maven dependency
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-app</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Features ##
### Version Prune ###
Since the databases of various test instances became too large, a version prune tool was developed.
This tool deletes content versions for the selected JCR workspace.

### Improved JCR query page ###
1. Also supports SQL2 queries that contain joins.
2. Represents besides the _Path Score_ and other _properties_.
3. Remembers last entered query in session

### Developer Tools ###  
**PageEdit Actions:**
1. View Source: opens selected page or component in new window.
2. View Dialog Definition: Displays the dialog definition in the definition app.
3. View Template Definition: Displays the template/component definition in the definition app.

> [!WARNING]
> The links are only shown if in the magnolia.properties the property 'magnolia.develop' is set to true, and you have the superuser-role.

### Acl Overview in User and Group Dialog ###
A new tab displays all ACLs assigned to the user or group. Acls are listed by repository.
