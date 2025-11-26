# Magkit Tools Scheduler

[![build-module](https://github.com/IBM/magkit-tools/actions/workflows/build.yaml/badge.svg)](https://github.com/IBM/magkit-tools/actions/workflows/build.yaml)
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-6.2-brightgreen.svg)](https://www.magnolia-cms.com)

## Overview

Magkit Tools Scheduler extends Magnolia CMS scheduler functionality with enhanced UI components and utilities for testing and maintaining scheduled jobs. This module is designed for administrators and developers who need better visibility and control over scheduled tasks in Magnolia CMS.

### Main Features

- **Enhanced Job Browser Columns**: Additional custom column definitions for better job visibility
- **Enabled Status Column**: Visual indicator showing whether a scheduled job is enabled or disabled
- **Job Command Action**: Execute job commands directly from the scheduler app for testing purposes
- **Job Node Title Provider**: Improved display of job information in the UI
- **Map Converter**: Utility for converting job configuration maps

> **Note**: This module is currently marked as Work in Progress (WIP). Features and APIs may change in future releases.

## Usage

### Maven Dependency

Add the dependency to your Magnolia project's `pom.xml`:

```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-tools-scheduler</artifactId>
    <version>1.0.3</version><!-- or 1.0.4-SNAPSHOT for latest development version -->
</dependency>
```

No additional Java configuration is required. The module extends the standard Magnolia Scheduler module with enhanced UI components.

## Examples

### 1. Using the Enabled Column Definition

The `EnabledColumnDefinition` provides a visual indicator for job status in the scheduler browser:

```java
// This is automatically configured via module YAML
// Shows a checkmark icon for enabled jobs and an X for disabled jobs
```

### 2. Executing a Job Command

The `JobCommandAction` allows direct execution of job commands from the UI:

```java
// In your scheduler app configuration YAML:
actions:
  executeCommand:
    class: de.ibmix.magkit.tools.scheduler.JobCommandAction
    label: Execute Job Now
```

### 3. Custom Job Node Title Provider

The `JobNodeTitleProvider` enhances job display in the scheduler app:

```java
// Automatically provides better formatted job names in the UI
// Based on job definition properties and node structure
```

### 4. Map Converter for Job Configuration

The `MapConverter` helps with job configuration parameter conversion:

```java
// Used internally for converting job parameters
// between JCR properties and job definition maps
```

## Module Configuration

The scheduler tools integrate seamlessly with Magnolia's scheduler module. Configuration is done through the standard Magnolia configuration workspace under:

```
config:/modules/magkit-tools-scheduler/
```

## Requirements

- Magnolia CMS 6.2 or higher
- Magnolia Scheduler Module
- Magnolia UI Framework

## Known Limitations

As this module is still in development (WIP status), the following should be considered:

- API stability is not guaranteed between minor versions
- Some features may be incomplete or subject to change
- Documentation may be updated as features are finalized

## License

The module is published under the Apache 2.0 license. If you would like to see the detailed LICENSE click [here](../LICENSE).

Copyright © 2025 IBM iX

## Authors

**Maintainers:**
- Frank Sommer - frank.sommer1@ibm.com
- Wolf Bubenik - wolf.bubenik@ibm.com

Developed by [IBM iX](https://www.ibm.com/de-de/services/ibmix) as part of the Magnolia Kit project.
