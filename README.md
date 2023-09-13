# platform.shared.cms-import

Project to import Proprietary Content CMS into Contentstack

Sub-projects:

```
commons - contains classes used across 2+ modules
cm-exporter - java tool for mfssync and package loader data exporting from content stack
project-importer - java tool for creating and updating new projects 
image-importer - java tool for importing images
project-closer - java tool for project cleanup in contantstack
cm-importer - contains classes for importing purposes such as POV and article import
```

#### Build all tools and services run:
```
-->mvnw clean package
```
#### Build a single tool/module run:
```
-->mvnw clean package -pl {module-name} -am

Example to build only cm-exporter tool run:
-->mvnw clean package -pl cm-exporter -am
```

