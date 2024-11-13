# Gradle Plugin : Include Subproject Source

- License: MIT

## Setup

- clone the project
- execute the task publishToMavenLocal
- edit your "build.gradle" and add the plugin

```groovy
plugins {
  id 'gradle-include-subproject-source' version '1.0.0'
}

includeSubprojectSource {
  subprojectNames = ["common"]
}
```