# sbr-control-api
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)]()
[![Dependency Status](https://www.versioneye.com/user/projects/58e23bf2d6c98d00417476cc/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58e23bf2d6c98d00417476cc)
SBR Operational read/write API

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
​
```shell
sbt api/run
```
The default application port is 9000. To specify an alternative port use `-Dhttp.port=8080`.

HBase can be started locally by:
```shell
start-hbase.sh
```
You will of course need to setup tables and other configurations on HBase for the interaction to work - this is detailed at [sbr-hbase-connector](https://github.com/ONSdigital/sbr-hbase-connector) repository. We can apply these steps using HBase shell;
```sbtshell
hbase shell
```
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


#### API Documentation
Swagger API is used to document and expose swagger definitions of the routes and capabilities for this project.

 To see the full definition set use path:
 `http://localhost:9000/swagger.json`
 
 For a graphical interface using Swagger Ui use path:
 `http://localhost:9000/docs`
 
### Troubleshooting
See [FAQ](CONTRIBUTING.md) for possible and common solutions.

### Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details.

### License

Copyright ©‎ 2017, Office for National Statistics (https://www.ons.gov.uk)

Released under MIT license, see [LICENSE](LICENSE.md) for details.






