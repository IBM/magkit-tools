# Magnolia Kit Tools Translation App #
App to manage translations for various languages form different properties files.

### Maven dependency
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-t9n</artifactId>
    <version>1.0.0</version>
</dependency>
```

## General Description ##
New app for maintaining message properties. All new messages are automatically added to the app during module update.
However, this requires adding the de.ibmix.magkit.tools.t9n.AddTranslationsTask for the corresponding properties files in the ModuleVersionHandler:
```java
new AddTranslationsTask("New translations",  "Check and add translation keys to translation app.", "mgnl-i18n.module-bmo-messages", GERMAN);
```

The module brings its own TranslationService, which is set in the module descriptor:
```xml
<component>
    <type>info.magnolia.i18nsystem.TranslationService</type>
    <implementation>com.aperto.magnolia.translation.MagnoliaTranslationServiceImpl</implementation>
    <scope>singleton</scope>
</component>
```

In the app there is an "Export CSV" function that exports all existing translations, 
and an "Import CSV" function that imports your translations into the magnolia.

There are 2 roles translation-user and translation-base, which have Read/Write or Read-only rights to the translation workspace.

The language fields in the dialog are based on the available languages in the multisite configuration.
This means that if a new language is added, the app and the dialog will automatically expand.

The language columns displayed in the app if you add an app decoration (translation.subApps.browser.workbench.contentViews.yaml).
```yaml
#  This is only one example:
- name : list
  columns: !override
    key:
      filterComponent:
      $type: textField
      width: 300
    translation_de:
      filterComponent:
      $type: textField
      expandRatio: 2
    translation_en:
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
