# Audio

A wrapper for the famous Fmod library, with javadoc generated from a local copy of the docs hosted on their website.
Does nothing on its own.

## Generate javadoc

To generate the wrapper classes with javadoc just run the main inside [GenBindings.java](audio/src/main/java/com/alcadrial/kirry/audio/GenBindings.java),
the results will be saved in src/gen/java.

## Build
To build the project run
```bash
./gradlew :audio:build
```
To publish to a maven repository run
```bash
./gradlew :audio:publish
```
