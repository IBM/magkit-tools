# MagKit Tools

[![build-module](https://github.com/IBM/magkit-tools/actions/workflows/build.yaml/badge.svg)](https://github.com/IBM/magkit-tools/actions/workflows/build.yaml) [![Magnolia compatibility](https://img.shields.io/badge/magnolia-6.2-brightgreen.svg)](https://www.magnolia-cms.com)

## Overview

MagKit Tools is a comprehensive multi-module project that provides essential operational, development, and authoring utilities for Magnolia CMS 6.2 and above. It extends Magnolia with productivity-enhancing features designed for administrators, developers, and content editors.

### Main Features

The project consists of four specialized modules:

#### 1. **[magkit-tools-app](magkit-tools-app/README.md)** - Repository Introspection & Maintenance
- **Enhanced JCR Query Page**: Execute SQL2 (including joins) or XPath queries with detailed results
- **Version Prune**: Remove older versions of nodes to reclaim storage space while keeping the most recent ones
- **Developer PageEdit Actions**: Quick access to view source, dialog definitions, and template definitions (requires `magnolia.develop=true`)
- **ACL Overview Tab**: Consolidated view of all ACLs per repository for users and groups

#### 2. **[magkit-tools-edit](magkit-tools-edit/README.md)** - Authoring & Editing Enhancements
- **Edit Page Properties Anywhere**: Access page properties action directly in browser view, areas, and components
- **Move Confirmation**: Adds confirmation dialog before moving pages to prevent accidental drags
- **Public View Action**: Open the currently selected page on a public instance
- **Author Instance Link**: Render a direct link back to the author instance from public pages
- **Additional Browser Columns**: Last Modified and Creator columns for Pages and Assets apps
- **Extended Status Bar**: Shows asset usage count for configured workspaces
- **Single Page Export Filter**: Export only nodes required for a single page

#### 3. **magkit-tools-scheduler** - Task Scheduling (Work in Progress)
- Task scheduling utilities for Magnolia CMS

#### 4. **[magkit-tools-t9n](magkit-tools-t9n/README.md)** - Translation Management
- **Translation App**: Manage translations for various languages from properties files
- **CSV Export/Import**: Export marked translations and import edited translations
- **Automatic Translation Updates**: New messages are automatically added to the app during module updates
- **Custom TranslationService**: Provides transparent access to translations for any property key
- **Access Control**: Predefined roles for translation users and read-only access

## Usage

### Maven Dependencies

Add the desired module dependencies to your Magnolia project's `pom.xml`. You can include all modules or select only the ones you need:

#### magkit-tools-app
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-app</artifactId>
    <version>1.0.3</version>
</dependency>
```

#### magkit-tools-edit
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-edit</artifactId>
    <version>1.0.3</version>
</dependency>
```

#### magkit-tools-t9n
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-t9n</artifactId>
    <version>1.0.3</version>
</dependency>
```

#### magkit-tools-scheduler
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-scheduler</artifactId>
    <version>1.0.3</version>
</dependency>
```

> **Note**: Use `1.0.4-SNAPSHOT` version only if you need unreleased changes. For production, always use the latest released version.

### Maven Artifacts

The code is built by [GitHub actions](https://github.com/IBM/magkit-tools/actions/workflows/build.yaml).
You can browse available artifacts through [Magnolia's Nexus](https://nexus.magnolia-cms.com/#nexus-search;quick~magkit-tools).

## Examples

### 1. Executing a JCR Query (magkit-tools-app)

```java
// Obtain a JCR session from Magnolia context
Session session = MgnlContext.getJCRSession("website");
QueryManager qm = session.getWorkspace().getQueryManager();

// Execute SQL2 query
long start = System.currentTimeMillis();
Query query = qm.createQuery(
    "SELECT * FROM [mgnl:page] WHERE ISDESCENDANTNODE('/foo')", 
    Query.JCR_SQL2
);
QueryResult result = query.execute();
long duration = System.currentTimeMillis() - start;

// Build and display the UI table
QueryResultTable table = Components.getComponent(QueryResultTable.class);
table.buildResultTable(result, true, true, duration);
```

### 2. Adding Translation Task to Version Handler (magkit-tools-t9n)

```java
public class MyModuleVersionHandler extends DefaultModuleVersionHandler {
    
    public MyModuleVersionHandler() {
        register(DeltaBuilder.update("1.0.1", "")
            .addTask(new AddTranslationsTask(
                "my-module.i18n.messages", 
                Locale.ENGLISH
            ))
        );
    }
}
```

### 3. Creating Public Page Editor Link (magkit-tools-edit)

```java
// In your page model or template
@Inject
private LinkService linkService;

public String getEditorLink() {
    // Start server with -Dmagnolia.author.basePath=<URL of Author>
    return linkService.createPageEditorLink();
}
```

### 4. Configuring Move Confirmation (magkit-tools-edit)

Configure in your module's YAML to display confirmation before moving pages:

```yaml
# config:/modules/magkit-tools-edit/config.yaml
workspaces:
  - website
  - dam
```

For detailed examples and configuration options, please refer to the individual module README files linked above.

## Contributing

Any bug reports, improvement or feature pull requests are very welcome! Make sure your patches are well tested. Ideally create a topic branch for every separate change you make.

### Issue Tracking

Issues are tracked at [GitHub](https://github.com/IBM/magkit-tools/issues).

### How to Contribute

1. Fork the repo
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create a new Pull Request

See also [CONTRIBUTING](CONTRIBUTING.md), [SECURITY](SECURITY.md) and [MAINTAINERS](MAINTAINERS.md) for more details.

## License

The modules are published under the Apache 2.0 license. If you would like to see the detailed LICENSE click [here](LICENSE).

Copyright © 2025 IBM iX

## Authors

**Maintainers:**
- Frank Sommer - frank.sommer1@ibm.com
- Wolf Bubenik - wolf.bubenik@ibm.com

Developed by [IBM iX](https://www.ibm.com/de-de/services/ibmix) as part of the Magnolia Kit project.

