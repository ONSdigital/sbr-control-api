# sbr-control-api
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)]()
[![Dependency Status](https://www.versioneye.com/user/projects/58e23bf2d6c98d00417476cc/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58e23bf2d6c98d00417476cc)

### What is this repository?
sbr-control-api is a Play Framework application written predominantly in Scala. This api, sbr-control-api, extends [sbr-api](https://github.com/ONSdigital/sbr-api) and acts as an intermediary that handles requests (from sbr-api) to query HBase. Its current privileges to the data are both read and write.

### Prerequisites

* Java 8 or higher
* SBT ([Download](http://www.scala-sbt.org/))

### Development Setup (MacOS)

To install SBT quickly you can use Homebrew ([Brew](http://brew.sh)):
```shell
brew install sbt
```
Similarly we can get Scala (for development purposes) using brew:
```shell
brew install scala
```
Install HBase locally with brew by using:
```shell
brew install hbase
```

### Running the App

To compile, build and run the application use the following command:
```shell
sbt "api/run -Dsbr.hbase.inmemory=true"
```
The default application port is 9000. To specify an alternative port use `-Dhttp.port=8080`.

#### Database Configuration
sbr-control-api provides the capability to easily switch between two database options: [HBase](https://github.com/ONSdigital/sbr-hbase-connector) and [SQL](https://github.com/ONSdigital/sbr-sql-connector). Although both share the same data source and thereby reveal the same result given the same parameters, the internal process of retrieval varies. Its usages at this point is merely experimental. By default the application uses HBase.

To override the `db.default.name` configuration we can add the following to our run command:
```shell
-Denv.default.db.default.name=sql
```

##### HBase
HBase can be started locally by:
```shell
start-hbase.sh
```
You will of course need to setup tables and other configurations on HBase for the interaction to work - this is detailed at [sbr-hbase-connector](https://github.com/ONSdigital/sbr-hbase-connector) repository. We can apply these steps using HBase shell;
```sbtshell
hbase shell
```

#### HBase REST

To start HBase, use the following command:

```shell
start-hbase.sh
```

We now need to start HBase REST.

```shell
hbase rest start
```

You can test that HBase REST is working by going to the following URL, [localhost:8080](http://localhost:8080).

It might take a little while to startup, after which you should see a list of HBase tables.

For metadata relating to HBase REST, go to [localhost:8085](http://localhost:8085).

#### Package

To package the project in a runnable fat-jar:
```shell
sbt assembly
```

#### Test

To test all test suites we can use:

```shell
sbt test
```

Testing an individual test suite can be specified by using the `testOnly`.

SBR Api uses its own test configuration settings for integration tests, the details of which can be found on the [ONS Confluence](https://collaborate2.ons.gov.uk/confluence/display/SBR/Scala+Testing).

To run integration test run:
```shell
sbt it:test
```
See [CONTRIBUTING](CONTRIBUTING.md) for more details on creating tests. 


#### API Documentation
Swagger API is used to document and expose swagger definitions of the routes and capabilities for this project.

 To see the full definition set use path:
 `http://localhost:9000/swagger.json`
 
 For a graphical interface using Swagger Ui use path:
 `http://localhost:9000/docs`
 
### Troubleshooting
See [FAQ](FAQ.md) for possible and common solutions.

### Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details.

### License

Copyright © 2017, Office for National Statistics (https://www.ons.gov.uk)

Released under MIT license, see [LICENSE](LICENSE.md) for details.
