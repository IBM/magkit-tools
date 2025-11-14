# Magkit Tools App

## Overview
Magkit Tools App extends Magnolia CMS with a set of operational and developer utilities focused on content repository introspection and maintenance. It is intended for administrators and developers needing to:

- Explore and debug JCR content via enhanced query execution
- Reduce repository size by pruning old JCR versions safely
- Accelerate template/component development with inline navigation helpers
- Inspect assigned ACLs for users and groups in a consolidated view

### Main Features
- Version Prune: Remove older versions of nodes while keeping the most recent ones (and the root version) to reclaim storage.
- Enhanced JCR Query Page: Execute SQL2 (including joins) or XPath queries; see paths, scores and selected property values; session remembers last query.
- Developer PageEdit Actions (shown only if `magnolia.develop=true` and user has superuser role):
  - View Source: Open the rendered source of a page/component in a new window.
  - View Dialog Definition: Jump to the dialog definition in the Definitions app.
  - View Template Definition: Jump to the template/component definition in the Definitions app.
- ACL Overview Tab: Extra tab in User and Group dialogs listing all ACLs per repository for quick auditing.

## Usage
Add the dependency to your Magnolia project. Prefer the latest released version for production; use the SNAPSHOT only if you need unreleased changes.

```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-app</artifactId>
    <version>1.0.3</version><!-- or 1.0.4-SNAPSHOT -->
</dependency>
```

No additional code configuration is required; after deployment the app and actions become available according to Magnolia permissions and the `magnolia.develop` property.

## Examples
Below are simplified Java snippets illustrating core functionality. They assume a running Magnolia context and proper dependency injection / component retrieval.

### 1. Executing and Displaying a JCR Query
```java
// Obtain a JCR session (e.g. from Magnolia context)
Session session = info.magnolia.context.MgnlContext.getJCRSession("website");
QueryManager qm = session.getWorkspace().getQueryManager();
long start = System.currentTimeMillis();
Query query = qm.createQuery("SELECT * FROM [mgnl:page] WHERE ISDESCENDANTNODE('/foo')", javax.jcr.query.Query.JCR_SQL2);
QueryResult result = query.execute();
long duration = System.currentTimeMillis() - start;
// Retrieve the table component (CDI / Magnolia component factory)
QueryResultTable table = info.magnolia.objectfactory.Components.getComponent(QueryResultTable.class);
// Build the UI table (show score + columns)
table.buildResultTable(result, true, true, duration);
```

### 2. Normalizing Column Names
```java
QueryResultTable table = info.magnolia.objectfactory.Components.getComponent(QueryResultTable.class);
String raw = "page.jcr:uuid"; // selector + namespaced property
String normalized = table.normalizeColumnName(raw); // returns "jcr:uuid" for cleaner display
```

### 3. Pruning Old Versions Programmatically
```java
// Typically triggered via UI form; simplified manual invocation:
VersionPruneSubApp subApp = info.magnolia.objectfactory.Components.getComponent(VersionPruneSubApp.class);
// The sub-app reads form data; conceptually we want to keep 5 latest versions under /foo
// (In UI you fill workspace = "website", path = "/foo", versions = 5 then click the action.)
subApp.doAction(); // Builds result view with pruned version info
```

### 4. Defining the View Source Action
```java
ViewSourceActionDefinition def = new ViewSourceActionDefinition();
def.setName("viewSource");
def.setLabel("View Source");
// Magnolia will use implementation class set in the constructor (ViewSourceAction)
```

### 5. Interpreting Prune Results (Result View)
The result view displays counts and individual node paths with removed version names. Errors (e.g. referential integrity) are listed inline so you can adjust your pruning strategy.

## License
This project is licensed under the Apache License, Version 2.0. You may obtain a copy at:
https://www.apache.org/licenses/LICENSE-2.0

Use of the software is subject to the terms and conditions of the license. Contributions are accepted under the same license.

## Authors
- Frank Sommer
- Wolf Bubenik

For maintenance and contact details see `MAINTAINERS.md` in the project root.
