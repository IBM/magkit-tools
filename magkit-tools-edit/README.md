# Magnolia Kit Tools Edit #
Provides some tools to support the editor.

### Maven dependency
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-edit</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Editor Actions ##
### Edit page properties ###
The action has been made available in the browser view. This allows editing the page properties even without opening the page.  

In addition, the availability of the action has been revised for the detail view and is thus available in every context—regardless of whether it is an area or a component.

### Confirmation when moving pages ###
Since pages are easily moved by mistake in the Pages app, the MoveHandler has been replaced. This displays a message for configured workspaces to prevent incorrect moving.

for example:
> [!WARNING]
> Do you really want to move the following node?
> * /home/de/node

### Back to the author instance ###
A link directly to the maintenance page can be displayed on each page on the public instance. The following must be configured for this:
1. The server must be started with the parameter "-Dmagnolia.author.basePath=<URL of Author>".
   1. This makes sense only on the public servers.
   2. In NO case configure this on the live servers.
2. In the PageModel, the method "de.ibmix.magkit.tools.edit.util.LinkService.createPageEditorLink()" is referenced.
    ```java
    // This is only one example:

    @Inject
    private de.ibmix.magkit.tools.edit.util.LinkService _linkService;
   
    public String getAuthorLink() {
        return _linkService.createPageEditorLink();
    }
    ```
3. In the ftl template (e.g. main.ftl) the link is displayed:
    ```
    [#--  This is only one example: --]

    [#assign pageEditorLink = model.getAuthorLink()!]
    [#if cmsfn.publicInstance && pageEditorLink?has_content]
        <a href="${pageEditorLink}" style="color: #FF9900" target="_blank">EDIT mode on author</a>
    [/#if]
    ```
> [!NOTE]
> Link maintainable in config, initially leave empty.  
> possibly available as utl class directly from ftl?

### View on Public ###
Link in the action bar opens the current page on the Public in a new window.

#### Configuration ####
The generation of the public links is generated by default like this:
1. Create a external link from page node.
2. Remove the author context path.

Especially in multisite setups this can lead to conflicts, and therefore the public link rendering can be adjusted in the module configuration.
Here you can specify different hosts or base paths per site.

### New Columns in Browser Sub App ###
Added Columns 'Last Modified' and 'Creator' to Browser SubApp of Pages and Assets.

### Extension status bar ###
1. The StatusBar of the Assets app has been extended by the number of times the selected asset has been used.
2. In the module configuration you can set for which workspaces the respective feature should be supported.
