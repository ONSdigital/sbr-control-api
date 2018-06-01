* [sbr-control-api](#sbr-control-api)
        * [What is this repository?](#what-is-this-repository)
        * [Endpoints](#endpoints)
        * [Prerequisites](#prerequisites)
        * [Development Setup (MacOS)](#development-setup-macos)
        * [Running the App](#running-the-app)
                * [HBase REST](#hbase-rest)
            * [Package](#package)
            * [Test](#test)
            * [API Documentation](#api-documentation)
        * [Running the App in Production Mode](#running-the-app-in-production-mode)
        * [Troubleshooting](#troubleshooting)
        * [Contributing](#contributing)
        * [License](#license)

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
brew install gradle
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
gradle runPlayBinary
```
The default application port is 9000. To specify an alternative port update the `build.gradle` with:

```
tasks.withType(PlayRun) { 
    httpPort = 9010
}
```

##### HBase REST

HBase can be started locally by:
```shell
start-hbase.sh
```

Now that HBase has started, we can open the shell and create the namespace and tables.
```shell
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
gradle playBinary
```

#### Test

To test all test suites we can use:

```shell
gradle test
```

Testing an individual test suite can be specified by using `testOnly`. For example:

```shell
gradle test --tests *ReportingUnitAcceptanceSpec
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

### Running the App in Production Mode

Running the application in Production Mode is needed to eliminate the overhead imposed by the auto-reload functionality of dev mode.
In production mode, the auto-reload is turned off and there is no overhead due to SBT constantly monitoring and watching for file changes.

Running in production mode requires that `play.crypto.secret` be defined in the application.conf with a non-default value (default value is `changeme`, which is not accepted). Note that the actual key itself is Play Version dependent (for example it is `play.http.secret.key` for Play 2.6.x)

However, the best practice is to create a separate production.conf that inherits from application.conf and overrides the above value.
A simpler alternative is to pass in the secret as a system property as shown below

First, generate a secret using a Play utility task:
```shell
sbt playGenerateSecret | grep -i 'generated'
```

Use the key generate above to run in Production Mode by using a utility provided by play called `testProd`:

```shell
# For Play Framework 2.5.x (current)
JAVA_OPTS="-DAPPLICATION_SECRET='E<n06iqsi>XL4<=;wqeZV]H2/b5R>jcJjzcqitkILZbUry=mNQHrOsDiWg734/Zn'"  sbt testProd
```

If you have logging enabled, you should see the following line in the output if the server has started successfully in Prod mode:

```
[info] play.api.Play - Application started (Prod)
```

(Note that the command used to run in Production mode is different between Play versions. 
For the latest Play Framework version, 2.6.x as of this writing, the command is:

```shell
# For Play Framework 2.6.x
sbt runProd
```

)

#### Application Tracing
[kamon](http://kamon.io) is used to automatically instrument the application and report trace spans to
[zipkin](https://zipkin.io/).

The AspectJ Weaver is required to make this happen, see [adding-the-aspectj-weaver](http://kamon.io/documentation/1.x/recipes/adding-the-aspectj-weaver/)
for further details.  Note that this is not currently activated when running tests.

To manually test, run a Zipkin 2 server.  The simplest way to do this is via docker:

    docker run --rm -d -p 9411:9411 openzipkin/zipkin:2.10.4

Then run the application via `sbt run`, and exercise an endpoint.
The trace information should be available in the Zipkin UI at
[http://localhost:9411/zipkin/](http://localhost:9411/zipkin/).

### Troubleshooting
See [FAQ](FAQ.md) for possible and common solutions.

### Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details.

### License

Copyright © 2017, Office for National Statistics (https://www.ons.gov.uk)

Released under MIT license, see [LICENSE](LICENSE.md) for details.
