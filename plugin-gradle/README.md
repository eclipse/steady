# vulas-gradle-plugin

## Gradle/Maven integration

Building a gradle plugin with maven can be difficult task. This is a hybrid solution.

## Implementation details

- only the build phase is delegated to Gradle
- dependencies must be declared in pom.xml
- Maven is copying over all declared dependencies (with transitive ones) to ```target/dependencies/compile``` folder
- Gradle is taking them from there
- Maven is in charge of deployment

## Development

- Refresh dependencies for Gradle project (in root folder): ```mvn -pl plugin-gradle -am clean compile```
- First time import the project into IDE as gradle project (tested with IDEA, and Eclipse)
- Run tests ```mvn -pl plugin-gradle -am clean integration-test```
- Publish plugin to local maven repository with (in root folder): ```mvn -DskipTests -pl plugin-gradle -am clean install```

### Inconveniences

- i.e. in Eclipse, the imported project doesn't know the outside world, if changes applied to dependencies, they must be refreshed with maven
- sources are not accessible by default


Maybe a better solution exists, but this would require some more research.
