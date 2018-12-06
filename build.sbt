import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerExposedPorts
import play.sbt.PlayScala
import sbtassembly.AssemblyPlugin.autoImport._
import sbtbuildinfo.BuildInfoPlugin.autoImport._

val publishRepo = settingKey[String]("publishRepo")

licenses := Seq("MIT-License" -> url("https://github.com/ONSdigital/sbr-control-api/blob/master/LICENSE"))

publishRepo := sys.props.getOrElse("publishRepo", default = "Unused transient repository")

// key-bindings
lazy val ITest = config("it") extend Test

lazy val Versions = new {
  val scala = "2.12.7"
  val appVersion = "0.1-SNAPSHOT"
}

lazy val Constant = new {
  val appName = "control-api"
  val projectStage = "alpha"
  val organisation = "ons"
  val team = "sbr"
}

lazy val Resolvers = Seq(
  Resolver.typesafeRepo("releases")
)

lazy val testSettings = Seq(
  sourceDirectory in ITest := baseDirectory.value / "/it",
  resourceDirectory in ITest := baseDirectory.value / "/test/resources",
  scalaSource in ITest := baseDirectory.value / "/it",
  // test setup
  parallelExecution in Test := false
)

lazy val publishingSettings = Seq(
  publishArtifact := false,
  publishTo := Some("Artifactory Realm" at publishRepo.value),
  releaseTagComment := s"Releasing $name ${(version in ThisBuild).value}",
  releaseCommitMessage := s"Setting Release tag to ${(version in ThisBuild).value}",
  // no commit - ignore zip and other package files
  releaseIgnoreUntrackedFiles := true
)

/*
 * -Ywarn-unused-import was removed because otherwise a large number of warnings are generated for
 *                      sbr-control-api/conf/routes which is a Play issue we can do nothing about
 *
 * In the upgrade to Scala 2.12 we have unfortunately had to disable unused warnings completely with
 * -Xlint:-unused (i.e enable Xlint except for unused).
 * This is because in addition to some Play requirements that were being flagged as unused, private
 * implicit vals were being flagged as unused even when used as a result of implicit resolution.
 */
lazy val commonSettings = Seq (
  scalaVersion := Versions.scala,
  resolvers ++= Resolvers,
  coverageExcludedPackages := ".*Routes.*;.*ReverseRoutes.*;.*javascript.*",
  scapegoatVersion in ThisBuild := "1.3.8"
)

lazy val api = (project in file("."))
  .enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt, PlayScala, JavaAgent)
  .configs(ITest)
  .settings(inConfig(ITest)(Defaults.testSettings) : _*)
  .settings(commonSettings: _*)
  .settings(testSettings:_*)
  .settings(publishingSettings:_*)
  .settings(
    routesImport += "extensions.Binders._",
    publishLocal := {},
    publish := {},
    organizationName := "ons",
    organization := "uk.gov.ons",
    moduleName := s"${Constant.appName}",
    name := s"${organizationName.value}-${Constant.team}-${moduleName.value}",
    version := (version in ThisBuild).value,
    buildInfoPackage := "controllers",
    // gives us last compile time and tagging info
    buildInfoKeys := Seq[BuildInfoKey](
      organization,
      name,
      version,
      scalaVersion,
      sbtVersion,
      BuildInfoKey.action("gitVersion") {
        git.gitTagToVersionNumber.?.value.getOrElse(Some(Constant.projectStage))+"@"+ git.formattedDateVersion.?.value.getOrElse("")
    }),
    javaOptions in Test += "-DSBR_DB_PORT=8075",
    javaOptions in Universal += "-Dorg.aspectj.tracing.factory=default",
    javaAgents += "org.aspectj" % "aspectjweaver" % "1.9.2",
    // di router -> swagger
    routesGenerator := InjectedRoutesGenerator,
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime,
    conflictManager := ConflictManager.strict,
    libraryDependencies ++= Seq(
      filters,
      ws,
      guice,
      "org.scalatestplus.play"       %%    "scalatestplus-play"  %    "3.1.2"           % Test,
      "org.scalatest"                %%    "scalatest"           %    "3.0.5"           % Test,
      "com.github.tomakehurst"       %     "wiremock"            %    "2.19.0"          % Test,
      "org.scalamock"                %%    "scalamock"           %    "4.1.0"           % Test,
      "com.typesafe.scala-logging"   %%    "scala-logging"       %    "3.9.0",
      "com.typesafe"                 %     "config"              %    "1.3.3",
      // kamon (for tracing)
      "io.kamon"                     %%    "kamon-play-2.6"      %    "1.1.1",
      "io.kamon"                     %%    "kamon-zipkin"        %    "1.0.0",
      "io.kamon"                     %%    "kamon-logback"       %    "1.0.3",
      // Swagger
      "io.swagger"                   %%    "swagger-play2"       %    "1.6.0",
      "org.webjars"                  %     "swagger-ui"          %    "3.19.5"
    ),
    dependencyOverrides ++= Seq(
      "org.scala-lang.modules"     %% "scala-parser-combinators" % "1.1.0",
      "org.reactivestreams"         % "reactive-streams"         % "1.0.2",
      "com.typesafe"                % "config"                   % "1.3.3",
      "io.kamon"                   %% "kamon-core"               % "1.1.0",
      "com.google.code.findbugs"    % "jsr305"                   % "3.0.2",
      "org.apache.commons"          % "commons-lang3"            % "3.6",
      "org.scalatest"              %% "scalatest"                % "3.0.5",
      "com.google.guava"            % "guava"                    % "22.0",
      "com.typesafe.play"          %% "play-test"                % "2.6.20",
      "com.typesafe.play"          %% "play-ws"                  % "2.6.20",
      "com.typesafe.play"          %% "play-ahc-ws"              % "2.6.20",
      "com.fasterxml.jackson.core"  % "jackson-databind"         % "2.8.11.2",
      "org.apache.httpcomponents"   % "httpclient"               % "4.5.5",

      // wiremock requires jetty 9.2.24.v20180105 but play-test's selenium dependency is transitively pulling in a binary incompatible 9.4.5.v20170502
      "org.eclipse.jetty" % "jetty-http" % "9.2.24.v20180105",
      "org.eclipse.jetty" % "jetty-io"   % "9.2.24.v20180105",
      "org.eclipse.jetty" % "jetty-util" % "9.2.24.v20180105",

      // conflicts resulting from io.swagger:swagger-play2 (treat swagger as low priority and select latest versions)
      "com.typesafe.play" %% "twirl-api"             % "1.3.15",
      "com.typesafe.play" %% "play-server"           % "2.6.20",
      "com.typesafe.play" %% "filters-helpers"       % "2.6.20",
      "com.typesafe.play" %% "play-logback"          % "2.6.20",
      "com.typesafe.play" %% "play-akka-http-server" % "2.6.20",
      "org.slf4j"          % "slf4j-api"             % "1.7.25"
    ),
    // Assembly
    assemblyJarName in assembly := s"${name.value}-${version.value}.jar",
    assemblyMergeStrategy in assembly := {
      case PathList("io", "netty", xs@_*)                                => MergeStrategy.last
      case PathList("javax", "xml", xs@_*)                               => MergeStrategy.last
      case PathList("org", "apache", xs @ _*)                            => MergeStrategy.last
      case PathList("org", "slf4j", xs @ _*)                             => MergeStrategy.first
      case PathList("META-INF", "io.netty.versions.properties", xs @ _*) => MergeStrategy.last
      case "META-INF/native/libnetty-transport-native-epoll.so"          => MergeStrategy.last
      case "application.conf"                                            => MergeStrategy.first
      case "logback.xml"                                                 => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    name in Universal := s"${Constant.organisation}-${Constant.appName}",
    packageName in Universal := s"${name.value}-${version.value}",
    mainClass in assembly := Some("play.core.server.ProdServerStart"),
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),
    dockerBaseImage := "openjdk:8-jre",
    dockerExposedPorts := Seq(9000)
  )
