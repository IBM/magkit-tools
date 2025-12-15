# Magnolia Kit Tools Translation App (magkit-tools-t9n)

[![build-module](https://github.com/IBM/magkit-tools/actions/workflows/build.yaml/badge.svg)](https://github.com/IBM/magkit-tools/actions/workflows/build.yaml)
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-6.2-brightgreen.svg)](https://www.magnolia-cms.com)

## Overview
The Translation App allows editors to maintain internationalized messages stored in Magnolia message property files directly in the CMS UI. New languages and message updates can be rolled out without a deployment. The module also supports bulk operations (CSV import/export) and a headless REST endpoint so frontends can fetch all translations for a given locale.

### Main Features
- Central UI app for managing i18n message keys and values across multiple languages
- Automatic onboarding of new message keys via `AddTranslationsTask` during module updates
- Role-based access (read-only vs. read/write) to the translation workspace
- Configurable language columns through app decoration YAML
- CSV export of selected translations and CSV import for bulk updates or cross-instance synchronization
- Custom `TranslationService` implementation (transparent usage for Magnolia components)
- Headless REST endpoint returning all translations for a locale (`/.rest/i18n/v1/{lang}`)

## Usage
### Maven Dependency
Add the dependency to your Magnolia project (use the latest released version, see CHANGELOG):
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-t9n</artifactId>
    <version>1.1.0-SNAPSHOT</version> <!-- or a released version -->
</dependency>
```
See the [CHANGELOG](../CHANGELOG.md) for available versions.

### Register Translation Keys on Update
Add the `AddTranslationsTask` for each messages bundle you want to expose. Typically done in your `ModuleVersionHandler` update delta:
```java
// ...existing code...
DeltaBuilder.update("1.1", "Register translation keys")
    .addTask(new AddTranslationsTask("your-module.i18n.messages", Locale.ENGLISH, Locale.GERMAN))
    .addTask(new AddTranslationsTask("another-module.i18n.messages", Locale.ENGLISH))
// ...existing code...
```
If you omit locales the provided ones default to what you pass; add all locales you want initially created.

### Translation Service
The module contributes its own `TranslationService` via module descriptor:
```xml
<component>
    <type>info.magnolia.i18nsystem.TranslationService</type>
    <implementation>com.aperto.magnolia.translation.MagnoliaTranslationServiceImpl</implementation>
    <scope>singleton</scope>
</component>
```
You can request translations in code exactly as with Magnolia's default service (no further config required).

### Access Control
Two roles are provided:
- `translation-user` (read/write)
- `translation-base` (read-only)
Assign them to editors as needed.

### Optional App Column Decoration
Add a decoration file `translation.subApps.browser.workbench.contentViews.yaml` in your module to define which language columns appear:

(!) Note, that with version 1.1.0 and later the key column and a default language column are always added automatically by the `TranslationListViewDefinition`. You only need to define additional language columns and any metadata columns you want to keep.

When migrating from an earlier version, make sure to remove the key and default language columns from your decoration file.
```yaml
#  This is only one example:
- name : list
  columns: !override
    # The 'key' column and a default language column are added automatically by the TranslationListViewDefinition.
    # Only define additional language columns as needed
    translation_de:
      filterComponent:
      $type: textField
      expandRatio: 2
    # and add any metadata columns you want to keep
      jcrPublishingStatus:
        $type: jcrStatusColumn
        width: 100
      mgnl:lastModified:
        $type: dateColumn
        width: 180
      mgnl:lastModifiedBy:
        label: column.lastModUser.label
        width: 150
      mgnl:createdBy:
        label: column.createdByUser.label
        width: 150
```

## Examples
### 1. Fetching a Translation in Java
```java
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.objectfactory.Components;

public class GreetingPrinter {
    private final TranslationService translationService = Components.getComponent(TranslationService.class);

    public String greeting() {
        return translationService.translate("greeting.hello", Locale.ENGLISH); // falls back if missing
    }
}
```

### 2. REST Headless Endpoint
Fetch all German translations:
```bash
curl -s https://your-magnolia-instance/.rest/i18n/v1/de | jq '.translations[0:5]'
```
Example partial JSON response:
```json
{
  "locale": "de",
  "entries": [
    {"key": "greeting.hello", "value": "Hallo"},
    {"key": "button.save", "value": "Speichern"}
  ]
}
```

### 3. CSV Export / Import
Export selected translations via the app action; you receive a CSV similar to:
```csv
key;translation_en;translation_de
button.save;Save;Speichern
button.cancel;Cancel;Abbrechen
```
Edit values and import back (or into a different environment) using the import action.

### 4. Module Update Task Adding New Bundle
```java
new AddTranslationsTask("my.shop.i18n.messages", Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN);
```
This creates missing nodes for the keys found in `my.shop.i18n.messages.properties` for each locale.

## License
This project is licensed under the Apache License 2.0. See the [LICENSE](../LICENSE) file for full details or visit: http://www.apache.org/licenses/LICENSE-2.0

## Authors
Maintainers (see [MAINTAINERS](../MAINTAINERS.md)):
- Frank Sommer — frank.sommer1@ibm.com
- Wolf Bubenik — wolf.bubenik@ibm.com

## Contribution & Support
Contributions are welcome. Please read [CONTRIBUTING](../CONTRIBUTING.md) and follow security guidelines in [SECURITY](../SECURITY.md).

---
For release history consult the [CHANGELOG](../CHANGELOG.md).
