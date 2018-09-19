# Contributing


### Scala Style
This project mainly follows the scala style guide documented in the official [Scala docs](http://docs.scala-lang.org/style/naming-conventions.html) but also incorporates some conventions from the [DataBrick project](https://github.com/databricks/scala-style-guide).


#### Naming Conventions
For naming values, methods and objects in this project we follow the official naming convention declared by the [Scala docs](http://docs.scala-lang.org/style/naming-conventions.html). 

### Swagger Documentation


### Scala Annotations

#### Branching and Merges

#### Pull Requests
All pull request must have test.

#### Versioning

#### Releases


### Testing
As mentioned above, all pull request must have test. Use the applicable types(s).

#### Types
sbr-control-api tests are divided into three main categories;
* it - Integration Tests
    - Purpose: Tests the application's interaction and functioning with other components. 
* unit - Unit Testing
    - Purpose - Validate if intermediary operations and granular statements or functions react and perform as expected.
* server - Server/browser based Tests
    - Purpose - These test perform test directly to application objects and features to observe the request and response - ensuring both are valid.
    - Typically in a Play framework environment this will include testing the controllers.


