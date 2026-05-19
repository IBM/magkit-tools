# Magkit Tools App

## Overview
Magkit Tools App extends Magnolia CMS with a set of operational and developer utilities focused on content repository introspection and maintenance. It is intended for administrators and developers needing to:

- Explore and debug JCR content via enhanced query execution
- Reduce repository size by pruning old JCR versions safely
- Speed up template/component development with inline navigation helpers
- Inspect assigned ACLs for users and groups in a consolidated view

### Main Features
- Version Prune: Remove older versions of nodes while keeping the most recent ones (and the root version) to reclaim storage.
- Enhanced JCR Query Page: Execute SQL2 (including joins) or XPath queries; see paths, scores and selected property values; session remembers the last query.
- Developer PageEdit Actions (add this by decoration by your own):
  - View Source: Open the rendered source of a page/component in a new window.
  - View Dialog Definition: Jump to the dialog definition in the Definitions app.
  - View Template Definition: Jump to the template/component definition in the Definitions app.
- ACL Overview: In User and Group dialogs listing all ACLs per repository for quick auditing.
- Extension of the rename page dialog by restricting the title to one row and showing the page node identifier

## Usage
Add the dependency to your Magnolia project. Prefer the latest released version for production; use the SNAPSHOT only if you need unreleased changes.

```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-app</artifactId>
    <version>1.2.0</version>
</dependency>
```

No additional code configuration is required.

## License
This project is licensed under the Apache License, Version 2.0. You may obtain a copy at:
https://www.apache.org/licenses/LICENSE-2.0

Use of the software is subject to the terms and conditions of the license. Contributions are accepted under the same license.

## Authors
- Frank Sommer
- Wolf Bubenik

For maintenance and contact details see `MAINTAINERS.md` in the project root.
