import play.sbt.PlayScala
import sbtbuildinfo.BuildInfoPlugin.autoImport._
import sbtassembly.AssemblyPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerExposedPorts

val publishRepo = settingKey[String]("publishRepo")

licenses := Seq("MIT-License" -> url("https://github.com/ONSdigital/sbr-control-api/blob/master/LICENSE"))

publishRepo := sys.props.getOrElse("publishRepo", default = "Unused transient repository")

// key-bindings
lazy val ITest = config("it") extend Test

lazy val Versions = new {
  val scala = "2.11.11"
  val appVersion = "0.1-SNAPSHOT"
  val scapegoatVersion = "1.1.0"
}

lazy val Constant = new {
  val appName = "control-api"
  val projectStage = "alpha"
  val organisation = "ons"
  val team = "sbr"
}

lazy val Resolvers = Seq(
  Resolver.typesafeRepo("releases"),
  "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/"
)

lazy val testSettings = Seq(
  sourceDirectory in ITest := baseDirectory.value / "/test/it",
  resourceDirectory in ITest := baseDirectory.value / "/test/resources",
  scalaSource in ITest := baseDirectory.value / "test/it",
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
 */
lazy val commonSettings = Seq (
  scalaVersion := Versions.scala,
  scalacOptions in ThisBuild ++= Seq(
    "-language:experimental.macros",
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-language:reflectiveCalls",
    "-language:experimental.macros",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:postfixOps",
    "-deprecation", // warning and location for usages of deprecated APIs
    "-feature", // warning and location for usages of features that should be imported explicitly
    "-unchecked", // additional warnings where generated code depends on assumptions
    "-Xlint", // recommended additional warnings
    "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
    "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures
    "-Ywarn-dead-code", // Warn when dead code is identified
    "-Ywarn-unused", // Warn when local and private vals, vars, defs, and types are unused
    "-Ywarn-numeric-widen" // Warn when numerics are widened
  ),
  resolvers ++= Resolvers,
  coverageExcludedPackages := ".*Routes.*;.*ReverseRoutes.*;.*javascript.*"
)

lazy val api = (project in file("."))
  .enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt, PlayScala)
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
    // di router -> swagger
    routesGenerator := InjectedRoutesGenerator,
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime,
    libraryDependencies ++= Seq (
      filters,
      ws,
      "org.scalatestplus.play"       %%    "scalatestplus-play"  %    "2.0.0"           % Test,
      "org.scalatest"                %%    "scalatest"           %    "3.0.4"           % Test,
      "com.github.tomakehurst"       %     "wiremock"            %    "1.58"            % Test,
      "org.scalamock"                %%    "scalamock"           %    "4.1.0"           % Test,
      "org.webjars"                  %%    "webjars-play"        %    "2.5.0-3",
      "io.lemonlabs"                 %%    "scala-uri"           %    "0.5.0",
      "com.typesafe.scala-logging"   %%    "scala-logging"       %    "3.5.0",
      "com.typesafe"                 %     "config"              %    "1.3.1",
      // Swagger
      "io.swagger"                   %%    "swagger-play2"       %    "1.5.3",
      "org.webjars"                  %     "swagger-ui"          %    "3.1.4",
      // Hadoop & HBase (for creating the tableName)
      "org.apache.hadoop" % "hadoop-common" % "2.6.0",
      "org.apache.hbase" % "hbase-common" % "1.0.0",
      "org.apache.hbase" % "hbase-client" % "1.0.0"
      excludeAll ExclusionRule("commons-logging", "commons-logging")
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
