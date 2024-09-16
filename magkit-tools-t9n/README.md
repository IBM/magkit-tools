# Magnolia Kit Tools Translation App #
App to manage translations for various languages from various properties files.

[![build-module](https://github.com/IBM/magkit-tools/actions/workflows/build.yaml/badge.svg)](https://github.com/IBM/magkit-tools/actions/workflows/build.yaml)
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-6.2-brightgreen.svg)](https://www.magnolia-cms.com)
### Maven dependency
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-t9n</artifactId>
    <version>1.0.3</version>
</dependency>
```

## General Description ##
This module gives you the possibility to maintain translations of global texts from 
message properties by an editor. By that changes in these texts or introducing new languages 
could be rolled out without a deployment. 
All new messages are automatically added to the app during the module update of your module. You 
only have to add the `de.ibmix.magkit.tools.t9n.AddTranslationsTask` for the corresponding properties files into your ModuleVersionHandler:
```java
new AddTranslationsTask("your-module.i18n.messages", Locale.ENGLISH);
```

The module brings its own TranslationService, which is set in the module descriptor:
```xml
<component>
    <type>info.magnolia.i18nsystem.TranslationService</type>
    <implementation>com.aperto.magnolia.translation.MagnoliaTranslationServiceImpl</implementation>
    <scope>singleton</scope>
</component>
```
By that it is fully transparent to get the translation for any property key.

Furthermore, the app provides a CSV export action to export marked translations, 
and a CSV import action to import edited translations into the same or to another Magnolia instance.

## Configuration

### Access control rights
There are 2 roles translation-user and translation-base, which have Read/Write or Read-only rights to the translation workspace.

### App columns
The language columns in the app could be enhanced by decoration. 
You could add an app decoration file (translation.subApps.browser.workbench.contentViews.yaml) in to your module to add any language column you like.
```yaml
#  This is only one example:
- name : list
  columns: !override
    key:
      filterComponent:
      $type: textField
      width: 300
    translation_en:
      filterComponent:
      $type: textField
      expandRatio: 2
    translation_de:
      filterComponent:
      $type: textField
      expandRatio: 2
    jcrPublishingStatus:
      $type: jcrStatusColumn
      width: 68
    mgnl:lastModified:
      $type: dateColumn
      width: 190
```
## Headless support

The module provides a REST endpoint for the headless integration. 
Your frontend could request the Magnolia backend for all translation for 
a language, e.g. all German translations `/.rest/i18n/v1/de`
