import play.sbt.PlayScala
import sbtbuildinfo.BuildInfoPlugin.autoImport._
import sbtassembly.AssemblyPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.Universal

val publishRepo = settingKey[String]("publishRepo")

licenses := Seq("MIT-License" -> url("https://github.com/ONSdigital/sbr-control-api/blob/master/LICENSE"))

publishRepo := sys.props.getOrElse("publishRepo", default = "Unused transient repository")

// key-bindings
lazy val ITest = config("it") extend Test

lazy val Versions = new {
  val scala = "2.11.11"
  val appVersion = "0.1-SNAPSHOT"
  val scapegoatVersion = "1.1.0"
  val hbase = "1.3.1"
}

lazy val Constant = new {
  val appName = "sbr-control-api"
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
    //"-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver
    "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures
    "-Ywarn-dead-code", // Warn when dead code is identified
    "-Ywarn-unused", // Warn when local and private vals, vars, defs, and types are unused
    "-Ywarn-unused-import", //  Warn when imports are unused (don't want IntelliJ to do it automatically)
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
    publishLocal := {},
    publish := {},
    name := Constant.appName,
    moduleName := "sbr-control-api",
    version := Versions.appVersion,
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
    // After universal:packageBin has run, the csv files cannot be found, so need to move them to the right place.
    // replace sample sql with actual sql date files for SQLConnect
    mappings in Universal += file("conf/sample/ch_2500_data.sql") -> "bin/conf/sample/ch_2500_data.sql",
    mappings in Universal += file("conf/sample/ent_2500_data.sql") -> "bin/conf/sample/ent_2500_data.sql",
    mappings in Universal += file("conf/sample/leu_2500_data.sql") -> "bin/conf/sample/leu_2500_data.sql",
    mappings in Universal += file("conf/sample/paye_2500_data.sql") -> "bin/conf/sample/paye_2500_data.sql",
    mappings in Universal += file("conf/sample/unit_links_2500_data.sql") -> "bin/conf/sample/unit_links_2500_data.sql",
    mappings in Universal += file("conf/sample/vat_2500_data.sql") -> "bin/conf/sample/vat_2500_data.sql",


    // Run with proper default env vars set for hbaseInMemory
    javaOptions in Test += "-Dsbr.hbase.inmemory=true",
    javaOptions in Universal ++= Seq(
      "-Dsbr.hbase.inmemory=true"
    ),
    // di router -> swagger
    routesGenerator := InjectedRoutesGenerator,
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime,
    libraryDependencies ++= Seq (
      filters,
      "org.scalatestplus.play"       %%    "scalatestplus-play"  %    "2.0.0"           % Test,
      "org.scalatest"                %%    "scalatest"           %    "3.0.0"           % Test,
      "org.webjars"                  %%    "webjars-play"        %    "2.5.0-3",
      "com.typesafe.scala-logging"   %%    "scala-logging"       %    "3.5.0",
      "com.typesafe"                 %     "config"              %    "1.3.1",
      //swagger
      "io.swagger"                   %%    "swagger-play2"       %    "1.5.3",
      "org.webjars"                  %     "swagger-ui"          %    "3.1.4"
      // hbase
      //"org.apache.hadoop"            %     "hadoop-common"       %    "2.6.0",
      //"org.apache.hbase"             %     "hbase-common"        %    Versions.hbase,
      //"org.apache.hbase"             %     "hbase-client"        %    Versions.hbase,
      //hbase test
      //"org.apache.hbase"             %     "hbase-server"        %    Versions.hbase    % Test,
      //"org.apache.hbase"             %     "hbase-testing-util"  %    Versions.hbase    % Test
      excludeAll ExclusionRule("commons-logging", "commons-logging")
    ),
    // assembly
    assemblyJarName in assembly := s"${Constant.appName}-${Versions.appVersion}.jar",
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
    mainClass in assembly := Some("play.core.server.ProdServerStart"),
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)
  )
