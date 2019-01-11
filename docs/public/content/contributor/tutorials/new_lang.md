# Support new language

!!! info
    This tutorial is under construction, the information provided is expected to be incomplete...

Extending @@PROJECT_NAME@@ to cover new languages requires the following steps:

* Extend enumerations comprised in component `shared`
* Create a new component `lang-xyz` that is able to extract all constructs from source and compiled code as well as packages of the respective programming language
* Add `RUNTIME` dependencies on `lang-xyz` to the client-side scan tools and `patch-analyzer`

## Extend the enumerations

Extend enumeration `com.sap.psr.vulas.shared.enums.ProgrammingLanguage` to cover the new programming language. Right now, there exist the three values `JAVA`, `PY` (Python) and `JS` (JavaScript). However, even if the enumeration value for JavaScript exists, the other parts have not been implemented yet (see next sections).

## Create new component `lang-xyz`

Create a new Maven module `lang-xyz` and add it to the root `pom.xml`. You can use `lang-python` as a template.

The purpose of the component is to extract all constructs of source and compiled code as well as packaged artifacts (e.g., wheels in Python).

...
