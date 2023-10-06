# Magnolia Kit Tools Translation App #
App to manage translations for various languages form different properties files.

## General Description ##
New app for maintaining message properties. All new messages are automatically added to the app during module update.
However, this requires adding the de.ibmix.magkit.tools.t9n.AddTranslationTask for the corresponding properties files in the ModuleVersionHandler:
```
new AddTranslationTask("New translations",  "Check and add translation keys to translation app.", "mgnl-i18n.module-bmo-messages", GERMAN);
```

The module brings its own TranslationService, which is set in the module descriptor:
```
<component>
    <type>info.magnolia.i18nsystem.TranslationService</type>
    <implementation>com.aperto.magnolia.translation.MagnoliaTranslationServiceImpl</implementation>
    <scope>singleton</scope>
</component>
```

In the app there is an "Export to CSV" function that exports all existing translations, 
and an "Import to CSV" function that imports your translations into the magnolia.

There are 2 roles translation-user and translation-base, which have Read/Write or Read-only rights to the translation workspace.

The language columns displayed in the app and language fields in the dialog are based on the available languages in the multisite configuration.
This means that if a new language is added, the app and the dialog will automatically expand.
