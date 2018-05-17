# sbr-control-api
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)]()
[![Dependency Status](https://www.versioneye.com/user/projects/58e23bf2d6c98d00417476cc/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58e23bf2d6c98d00417476cc)

### What is this repository?
sbr-control-api is a Play Framework application written predominantly in Scala. This api, sbr-control-api, extends [sbr-api](https://github.com/ONSdigital/sbr-api) and acts as an intermediary that handles requests (from sbr-api) to query HBase. Its current privileges to the data are both read and write.

### Endpoints

| method | endpoint                                                  | example                                                              |
|--------|-----------------------------------------------------------|----------------------------------------------------------------------|
| GET    | /v1/units/:id                                             | /v1/units/1234567890                                                 |
| GET    | /v1/periods/:period/types/:unitType/units/:id             | /v1/periods/201802/types/ENT/units/1234567890                        |
| GET    | /v1/enterprises/:id                                       | /v1/enterprises/1234567890                                           |
| GET    | /v1/periods/:period/enterprises/:id                       | /v1/periods/201802/enterprises/1234567890                            |
| GET    | /v1/enterprises/:ern/periods/:period/localunits/:lurn     | /v1/enterprises/1234567890/periods/201802/localunits/123456789       |
| GET    | /v1/enterprises/:ern/periods/:period/localunits           | /v1/enterprises/1234567890/periods/201802/localunits                 |
| GET    | /v1/enterprises/:ern/periods/:period/reportingunits/:rurn | /v1/enterprises/1234567890/periods/201802/reportingunits/33000000000 |
| GET    | /v1/enterprises/:ern/periods/:period/reportingunits       | /v1/enterprises/1234567890/periods/201802/reportingunits             |

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
sbt run
```
The default application port is 9000. To specify an alternative port use `-Dhttp.port=8080`.

##### HBase REST

HBase can be started locally by:
```shell
start-hbase.sh
```

Now that HBase has started, we can open the shell and create the namespace and tables.
```sbtshell
hbase shell
create_namespace 'sbr_control_db'
create 'sbr_control_db:enterprise', 'd'
create 'sbr_control_db:unit_links', 'l'
```

For instructions on loading some test data into HBase, see the [HBASE.md](./HBASE.md).

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

Testing an individual test suite can be specified by using `testOnly`. For example:

```shell
sbt "testOnly *ReportingUnitAcceptanceSpec"
```

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
